package model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.analysis.Tracker;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.PathsLoader;
import dataLoader.ServiceSet;
import fxmlControllers.ModelRunnerController;

/**
 * @author Mohamed Byari
 *
 */

public class RegionalModelRunner {
	private static final Logger LOGGER = LogManager.getLogger(RegionalModelRunner.class);
	ConcurrentHashMap<String, Double> totalSupply;
	private ConcurrentHashMap<String, Double> marginal = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Manager, Double> distributionMean;
	public Region R;

	private String[][] compositionAftListener;
	private String[][] servicedemandListener;

	public RegionalModelRunner(String regionName) {
		R = RegionClassifier.regions.get(regionName);
		initializeListeners();
	}

	private void initializeListeners() {
		compositionAftListener = new String[PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 2][AFTsLoader
				.getAftHash().size()];
		servicedemandListener = new String[PathsLoader.getEndtYear() - PathsLoader.getStartYear()
				+ 2][ServiceSet.getServicesList().size() * 2];
		for (int i = 0; i < ServiceSet.getServicesList().size(); i++) {
			servicedemandListener[0][i] = "ServiceSupply:" + ServiceSet.getServicesList().get(i);
			servicedemandListener[0][i + ServiceSet.getServicesList().size()] = "Demand:"
					+ ServiceSet.getServicesList().get(i);
		}
		int i = 0;
		for (String label : AFTsLoader.getAftHash().keySet()) {
			compositionAftListener[0][i++] = label;
		}
	}

	private void calculeRegionsSupply() {
		totalSupply = new ConcurrentHashMap<>();
		R.getCells().values().parallelStream().forEach(c -> {
			c.currentProductivity.forEach((s, v) -> {
				totalSupply.merge(s, v, Double::sum);
			});
		});
	}

	private void productivityForAll() {
		R.getCells().values().parallelStream().forEach(cell -> cell.calculateCurrentProductivity(R));
	}

	private void calculeDistributionMean() {
		distributionMean = new ConcurrentHashMap<>();
		R.getCells().values().parallelStream().forEach(c -> {
			if (c.getOwner() != null) {
				distributionMean.merge(c.getOwner(), c.utility(marginal), Double::sum);
			}
		});
		AFTsLoader.getActivateAFTsHash().values().forEach(a -> distributionMean.computeIfAbsent(a, key -> 0.));

		// Calculate the mean distribution
		distributionMean.forEach((a, total) -> {
			distributionMean.put(a, total / AFTsLoader.hashAgentNbrRegions.get(R.getName()).get(a.label));
		});
	}

	private void calculeMarginal(int year) {
		int tick = year - PathsLoader.getStartYear();
		totalSupply.forEach((serviceName, serviceSupply) -> {
			double serviceDemand = R.getServicesHash().get(serviceName).getDemands().get(tick);
			double marg = ModelRunner.removeNegative ? Math.max(serviceDemand - serviceSupply, 0)
					: serviceDemand - serviceSupply;
			if (ModelRunner.isAveragedPerCellResidualDemand) {
				marg = marg / R.getCells().size();
			}
			marg = marg * R.getServicesHash().get(serviceName).getWeights().get(tick);
			marginal.put(serviceName, marg);
		});
		// LOGGER.trace("Region: [" + regionName +"] Marginal" + marginal);
	}

	void takeOverUnmanageCells() {
		R.getUnmanageCellsR().parallelStream().forEach(c -> {
			c.competition(marginal, distributionMean, R);
			if (c.getOwner() != null && !c.getOwner().isAbandoned()) {
				R.getUnmanageCellsR().remove(c);
			}
		});
	}

	public void regionalSupply() {
		LOGGER.info("Region: [" + R.getName() + "] Productivity calculation for allcells ");
		if (CellsLoader.regionalization) {
			productivityForAll();
		} else {
			productivityForAllExecutor();
		}

		LOGGER.info("Region: [" + R.getName() + "] Total Supply calculation");
		calculeRegionsSupply();

	}

	public static void initialDSEquilibrium(ConcurrentHashMap<String, Service> ServiceHash,
			ConcurrentHashMap<String, Double> supply) {
		supply.forEach((serviceName, serviceSuplly) -> {
			double factor = 1;
			if (serviceSuplly != 0) {
				if (ServiceHash.get(serviceName).getDemands().get(0) == 0) {
					LOGGER.warn("Demand for " + serviceName + " = 0");
				} else {
					factor = ServiceHash.get(serviceName).getDemands().get(0) / (serviceSuplly);
				}
			} else {
				LOGGER.warn("Supply for " + serviceName + " = 0 (The AFT baseline map is unable to produce  "
						+ serviceName + " service)");
			}

			ServiceHash.get(serviceName).setCalibration_Factor(factor != 0 ? factor : 1);
		});
	}

	public void initialDSEquilibrium() {
		regionalSupply();
		initialDSEquilibrium(R.getServicesHash(), totalSupply);
		LOGGER.info(
				"Initial Demand Service Equilibrium Factor= " + R.getName() + ": " + R.getServiceCalibration_Factor());
	}

	public void go(int year) {
		boolean outputFilesCreation = ModelRunner.writeCsvFiles && RegionClassifier.regions.size() > 1;

		if (outputFilesCreation) {
			outPutservicedemandToCsv(year);
			Tracker.trackSupply(year, R.getName());
		}

		LOGGER.info("Rigion: [" + R.getName() + "] Total Supply = " + totalSupply);
		calculeMarginal(year);
		LOGGER.info("Rigion: [" + R.getName() + "] Calculating Distribution Mean & Land abandonment");
		calculeDistributionMean();

		if (ModelRunner.usegiveUp) {
			ConcurrentHashMap<String, Cell> randomCellsubSetForGiveUp = CellsSet.getRandomSubset(R.getCells(),
					ModelRunner.percentageOfGiveUp);
			if (randomCellsubSetForGiveUp != null) {
				randomCellsubSetForGiveUp.values().parallelStream().forEach(c -> {
					c.giveUp(marginal, distributionMean, R);
				});
			}
		}
		// LOGGER.info("Region: [" + regionName + "] Take over unmanaged cells &
		// Launching the competition process...");
		takeOverUnmanageCells();
		// Randomly select % of the land available for competition
		ConcurrentHashMap<String, Cell> randomCellsubSet = CellsSet.getRandomSubset(R.getCells(),
				ModelRunner.percentageCells);
		if (randomCellsubSet != null) {
			List<ConcurrentHashMap<String, Cell>> subsubsets = CellsSet.splitIntoSubsets(randomCellsubSet,
					ModelRunner.nbrOfSubSet);
			ConcurrentHashMap<String, Double> servicesBeforeCompetition = new ConcurrentHashMap<>();
			ConcurrentHashMap<String, Double> servicesAfterCompetition = new ConcurrentHashMap<>();

			subsubsets.forEach(subsubset -> {
				if (subsubset != null) {
					subsubset.values().parallelStream().forEach(c -> {
						if (c.getOwner() != null && c.getOwner().isActive()) {
							c.getCurrentProductivity()
									.forEach((key, value) -> servicesBeforeCompetition.merge(key, value, Double::sum));
							c.competition(marginal, distributionMean, R);
							c.calculateCurrentProductivity(R);
							c.getCurrentProductivity()
									.forEach((key, value) -> servicesAfterCompetition.merge(key, value, Double::sum));
						}
					});
				}
				servicesBeforeCompetition.forEach((key, value) -> totalSupply.merge(key, -value, Double::sum));
				servicesAfterCompetition.forEach((key, value) -> totalSupply.merge(key, value, Double::sum));
				calculeMarginal(year);
			});
		} else {
			// LOGGER.error("Region: [" + regionName + "] Failed to select a random subset
			// of
			// cells");
		}
		// LOGGER.info("Region: [" + regionName + "] Competition Process Completed");
		if (outputFilesCreation) {
			compositionAFT(year);
			updateCSVFiles();
		}
		AFTsLoader.hashAgentNbr(R.getName());

	}

	private void productivityForAllExecutor() {
		LOGGER.info("Productivity calculation for all cells ");
		final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Map<String, Cell>> partitions = partitionMap(R.getCells(), 10); // Partition into 10
																				// sub-maps
		try {
			for (Map<String, Cell> subMap : partitions) {
				executor.submit(() -> subMap.values().parallelStream().forEach(c -> c.calculateCurrentProductivity(R)));
			}
		} finally {
			executor.shutdown();
			try {
				executor.awaitTermination(10, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
			} // Wait for all tasks to complete
		}
	}

	private static List<Map<String, Cell>> partitionMap(Map<String, Cell> originalMap, int numberOfPartitions) {
		List<Map<String, Cell>> partitions = new ArrayList<>();
		int size = originalMap.size() / numberOfPartitions;
		Iterator<Map.Entry<String, Cell>> iterator = originalMap.entrySet().iterator();
		for (int i = 0; i < numberOfPartitions; i++) {
			Map<String, Cell> part = new HashMap<>();
			for (int j = 0; j < size && iterator.hasNext(); j++) {
				Map.Entry<String, Cell> entry = iterator.next();
				part.put(entry.getKey(), entry.getValue());
			}
			partitions.add(part);
		}
		return partitions;
	}

	private void outPutservicedemandToCsv(int year) {
		AtomicInteger m = new AtomicInteger();
		int y = year - PathsLoader.getStartYear() + 1;
		ServiceSet.getServicesList().forEach(name -> {
			servicedemandListener[y][m.get()] = totalSupply.get(name) + "";
			servicedemandListener[y][m.get() + ServiceSet.getServicesList().size()] = R.getServicesHash().get(name)
					.getDemands().get(year - PathsLoader.getStartYear()) + "";
			// DemandModel.getRegionalDemand(name, year, R.getName()) + "";
			m.getAndIncrement();
		});
	}

	private void compositionAFT(int year) {
		int y = year - PathsLoader.getStartYear() + 1;
		AFTsLoader.hashAgentNbrRegions.get(R.getName()).forEach((name, value) -> {
			compositionAftListener[y][Tools.indexof(name, compositionAftListener[0])] = value + "";
		});
	}

	private void updateCSVFiles() {
		String dir = PathTools.makeDirectory(ModelRunnerController.outPutFolderName + File.separator + "region_"
				+ R.getName() + File.separator + "");
		if (ModelRunner.writeCsvFiles) {
			Path aggregateAFTComposition = Paths.get(dir + "region_" + R.getName() + "-AggregateAFTComposition.csv");
			CsvTools.writeCSVfile(compositionAftListener, aggregateAFTComposition);
			Path aggregateServiceDemand = Paths.get(dir + "region_" + R.getName() + "-AggregateServiceDemand.csv");
			CsvTools.writeCSVfile(servicedemandListener, aggregateServiceDemand);
		}

	}

}
