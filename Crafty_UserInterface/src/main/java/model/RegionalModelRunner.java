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
import dataLoader.AFTsLoader;
import dataLoader.PathsLoader;
import dataLoader.ServiceSet;
import fxmlControllers.ModelRunnerController;
import utils.analysis.CustomLogger;
import utils.analysis.Tracker;
import utils.filesTools.CsvTools;
import utils.filesTools.PathTools;
import utils.graphicalTools.Tools;

/**
 * @author Mohamed Byari
 *
 */

public class RegionalModelRunner {
	private static final CustomLogger LOGGER = new CustomLogger(RegionalModelRunner.class);
	ConcurrentHashMap<String, Double> totalSupply;
	private ConcurrentHashMap<String, Double> marginal = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Manager, Double> distributionMean;
	public Region R;

	private String[][] compositionAftListener;
	private String[][] servicedemandListener;
	public String[][] DSEquilibriumListener;

	public RegionalModelRunner(String regionName) {
		R = RegionClassifier.regions.get(regionName);
		initializeListeners();
	}

	private void initializeListeners() {
		compositionAftListener = new String[PathsLoader.getEndtYear() - PathsLoader.getStartYear()
				+ 2][AFTsLoader.getAftHash().size() + 1];
		servicedemandListener = new String[PathsLoader.getEndtYear() - PathsLoader.getStartYear()
				+ 2][ServiceSet.getServicesList().size() * 2 + 1];
		servicedemandListener[0][0] = "Year";
		for (int i = 1; i < ServiceSet.getServicesList().size() + 1; i++) {
			servicedemandListener[0][i] = "ServiceSupply:" + ServiceSet.getServicesList().get(i - 1);
			servicedemandListener[0][i + ServiceSet.getServicesList().size()] = "Demand:"
					+ ServiceSet.getServicesList().get(i - 1);
		}
		compositionAftListener[0][0] = "Year";
		int j = 1;
		for (String label : AFTsLoader.getAftHash().keySet()) {
			compositionAftListener[0][j++] = label;
		}
		DSEquilibriumListener = new String[ServiceSet.getServicesList().size() + 1][2];
		DSEquilibriumListener[0][0] = "Service";
		DSEquilibriumListener[0][1] = "Calibration_Factor";

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
			double marg = ModelRunner.remove_negative_marginal_utility ? Math.max(serviceDemand - serviceSupply, 0)
					: serviceDemand - serviceSupply;
			if (ModelRunner.averaged_residual_demand_per_cell) {
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
		if (RegionClassifier.regionalization) {
			productivityForAll();
		} else {
			productivityForAllExecutor();
		}

		LOGGER.info("Region: [" + R.getName() + "] Total Supply calculation");
		calculeRegionsSupply();

	}

	public void initialDSEquilibrium(ConcurrentHashMap<String, Service> ServiceHash,
			ConcurrentHashMap<String, Double> supply) {
		supply.forEach((serviceName, serviceSuplly) -> {
			double factor = 1;
			if (serviceSuplly != 0) {
				if (ServiceHash.get(serviceName).getDemands().get(1) == 0) {
					LOGGER.warn("Demand for " + serviceName + " = 0");
				} else {
					factor = ServiceHash.get(serviceName).getDemands().get(1) / (serviceSuplly);
				}
			} else {
				LOGGER.warn("Supply for " + serviceName + " = 0 (The AFT baseline map is unable to produce  "
						+ serviceName + " service)");
			}
			ServiceHash.get(serviceName).setCalibration_Factor(factor != 0 ? factor : 1);
		});
		for (int i = 0; i < ServiceSet.getServicesList().size(); i++) {
			DSEquilibriumListener[i + 1][0] = ServiceSet.getServicesList().get(i);
			DSEquilibriumListener[i + 1][1] = ServiceHash.get(ServiceSet.getServicesList().get(i))
					.getCalibration_Factor() + "";
		}
	}

	public void initialDSEquilibrium() {
		regionalSupply();
		initialDSEquilibrium(R.getServicesHash(), totalSupply);
		R.getServicesHash().values().forEach(s -> {
			s.getDemands().forEach((year, value) -> {
				s.getDemands().put(year, value / s.getCalibration_Factor());
			});
		});
		LOGGER.info(
				"Initial Demand Service Equilibrium Factor= " + R.getName() + ": " + R.getServiceCalibration_Factor());
	}

	public void go(int year) {
		boolean outputFilesCreation = ModelRunner.generate_csv_files && RegionClassifier.regions.size() > 1;

		if (outputFilesCreation) {
			outPutservicedemandToCsv(year);
			Tracker.trackSupply(year, R.getName());
			compositionAFT(year);
			updateCSVFiles();
		}

		LOGGER.info("Rigion: [" + R.getName() + "] Total Supply = " + totalSupply);
		calculeMarginal(year);
		LOGGER.info("Rigion: [" + R.getName() + "] Calculating Distribution Mean & Land abandonment");
		calculeDistributionMean();

		if (ModelRunner.use_abandonment_threshold) {
			ConcurrentHashMap<String, Cell> randomCellsubSetForGiveUp = CellsSet.getRandomSubset(R.getCells(),
					ModelRunner.land_abandonment_percentage);
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
				ModelRunner.participating_cells_percentage);
		if (randomCellsubSet != null) {
			List<ConcurrentHashMap<String, Cell>> subsubsets = CellsSet.splitIntoSubsets(randomCellsubSet,
					ModelRunner.marginal_utility_calculations_per_tick);
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
		AtomicInteger m = new AtomicInteger(1);
		int y = year - PathsLoader.getStartYear() + 1;
		servicedemandListener[y][0] = year + "";
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
		compositionAftListener[y][0] = year + "";
		AFTsLoader.hashAgentNbrRegions.get(R.getName()).forEach((name, value) -> {
			compositionAftListener[y][Tools.indexof(name, compositionAftListener[0])] = value + "";
		});
	}

	private void updateCSVFiles() {
		String dir = PathTools.makeDirectory(ModelRunnerController.outPutFolderName + File.separator + "region_"
				+ R.getName() + File.separator + "");
		if (ModelRunner.generate_csv_files) {
			Path aggregateAFTComposition = Paths.get(dir + "region_" + R.getName() + "-AggregateAFTComposition.csv");
			CsvTools.writeCSVfile(compositionAftListener, aggregateAFTComposition);
			Path aggregateServiceDemand = Paths.get(dir + "region_" + R.getName() + "-AggregateServiceDemand.csv");
			CsvTools.writeCSVfile(servicedemandListener, aggregateServiceDemand);
			boolean oneTime = true;
			if (oneTime) {
				oneTime = false;
				Path DSEquilibriumPath = Paths.get(dir + "region_" + R.getName() + "-DemandServicesEquilibrium.csv");
				CsvTools.writeCSVfile(DSEquilibriumListener, DSEquilibriumPath);
			}
		}

	}

}
