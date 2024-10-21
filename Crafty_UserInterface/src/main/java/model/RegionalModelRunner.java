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
import dataLoader.CurvesLoader;
import dataLoader.DemandModel;
import dataLoader.PathsLoader;
import fxmlControllers.ModelRunnerController;

/**
 * @author Mohamed Byari
 *
 */

public class RegionalModelRunner {
	private static final Logger LOGGER = LogManager.getLogger(RegionalModelRunner.class);
	private String regionName;
	ConcurrentHashMap<String, Double> totalSupply;
	private ConcurrentHashMap<String, Double> marginal = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Manager, Double> distributionMean;
	private ConcurrentHashMap<String, Cell> hashRegionCell = new ConcurrentHashMap<>();

	private String[][] compositionAftListener;
	private String[][] servicedemandListener;

	public RegionalModelRunner(String regionName) {
		this.regionName = regionName;
		hashRegionCell = RegionClassifier.regions.get(regionName);
		initializeListeners();
	}

	private void initializeListeners() {
		compositionAftListener = new String[PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 2][AFTsLoader
				.getAftHash().size()];
		servicedemandListener = new String[PathsLoader.getEndtYear() - PathsLoader.getStartYear()
				+ 2][CellsSet.getServicesNames().size() * 2];
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			servicedemandListener[0][i] = "ServiceSupply:" + CellsSet.getServicesNames().get(i);
			servicedemandListener[0][i + CellsSet.getServicesNames().size()] = "Demand:"
					+ CellsSet.getServicesNames().get(i);
		}
		int i = 0;
		for (String label : AFTsLoader.getAftHash().keySet()) {
			compositionAftListener[0][i++] = label;
		}
	}

	private void calculeRegionsSupply() {
		totalSupply = new ConcurrentHashMap<>();
		hashRegionCell.values().parallelStream().forEach(c -> {
			c.currentProductivity.forEach((s, v) -> {
				totalSupply.merge(s, v, Double::sum);
			});
		});
	}

	private void productivityForAll() {
		hashRegionCell.values().parallelStream().forEach(Cell::getCurrentProductivity);
	}

	private void calculeDistributionMean() {
		distributionMean = new ConcurrentHashMap<>();
		hashRegionCell.values().parallelStream().forEach(c -> {
			if (c.getOwner() != null) {
				distributionMean.merge(c.getOwner(), c.utility(marginal), Double::sum);
			}
		});
		AFTsLoader.getActivateAFTsHash().values().forEach(a -> distributionMean.computeIfAbsent(a, key -> 0.));

		// Calculate the mean distribution
		distributionMean.forEach((a, total) -> {
			distributionMean.put(a, total / AFTsLoader.hashAgentNbrRegions.get(regionName).get(a.label));
		});
	}

	private void calculeMarginal(int year) {
		totalSupply.forEach((serviceName, serviceVal) -> {
			double demand = DemandModel.getRegionalDemand(serviceName, year, regionName);
			double marg = ModelRunner.removeNegative ? Math.max(demand - serviceVal, 0) : demand - serviceVal;
			if (ModelRunner.isAveragedPerCellResidualDemand) {
				marg = marg / hashRegionCell.size();
			}
			marg = CurvesLoader.hashServicesCurves.get(serviceName).linearFunction(marg);
			marginal.put(serviceName, marg);// 0.);//
		});
		// LOGGER.trace("Region: [" + regionName +"] Marginal" + marginal);
	}

	void takeOverUnmanageCells() {
		RegionClassifier.unmanageCellsR.get(regionName).parallelStream().forEach(c -> {
			if (c.getOwner() == null) {
				c.competition(marginal, distributionMean);
				RegionClassifier.unmanageCellsR.get(regionName).remove(c);
			}
		});
	}

	public void regionalSupply() {
		// LOGGER.info("Region: [" + regionName + "] Productivity calculation for all
		// cells ");
		if (CellsLoader.regionalization) {
			productivityForAll();
		} else {
			productivityForAllExecutor();
		}

		// LOGGER.info("Region: [" + regionName + "] Total Supply calculation");
		calculeRegionsSupply();

	}

	public void go(int year) {
		boolean outputFilesCreation = ModelRunner.writeCsvFiles && DemandModel.getDemandsRegions().size() > 1;

//		totalSupply.forEach((serviceName, serviceVal) -> {
//		System.out.println("demandFiles(\""+serviceName+"\","+(DemandModel.getRegionalDemand(serviceName, year, regionName)/serviceVal)+");");
//	});

		if (outputFilesCreation) {
			outPutservicedemandToCsv(year);
			Tracker.trackSupply(year, regionName);
		}

		LOGGER.info("Rigion: [" + regionName + "] Total Supply = " + totalSupply);
		calculeMarginal(year);
		LOGGER.info("Rigion: [" + regionName + "] Calculating Distribution Mean & Land abandonment");
		calculeDistributionMean();

		if (ModelRunner.usegiveUp) {
			ConcurrentHashMap<String, Cell> randomCellsubSetForGiveUp = CellsSet.getRandomSubset(hashRegionCell,
					ModelRunner.percentageOfGiveUp);
			if (randomCellsubSetForGiveUp != null) {
				randomCellsubSetForGiveUp.values().parallelStream().forEach(c -> {
					c.giveUp(marginal, distributionMean, regionName);
					// System.out.println("giveUp"+ RegionClassifier.unmanageCellsR.size());
				});
			}
		}
		// LOGGER.info("Region: [" + regionName + "] Take over unmanaged cells &
		// Launching the competition process...");
		 takeOverUnmanageCells();//
		// Randomly select % of the land available for competition
		ConcurrentHashMap<String, Cell> randomCellsubSet = CellsSet.getRandomSubset(hashRegionCell,
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
							c.getServices()
									.forEach((key, value) -> servicesBeforeCompetition.merge(key, value, Double::sum));
							if (ModelRunner.usegiveUp) {
								c.giveUp(marginal, distributionMean, regionName);
							}
							c.competition(marginal, distributionMean);
							c.getCurrentProductivity();
							c.getServices()
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
		AFTsLoader.hashAgentNbr(regionName);

	}

	private void productivityForAllExecutor() {
		LOGGER.info("Productivity calculation for all cells ");
		final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Map<String, Cell>> partitions = partitionMap(hashRegionCell, 10); // Partition into 10
																				// sub-maps
		try {
			for (Map<String, Cell> subMap : partitions) {
				executor.submit(() -> subMap.values().parallelStream().forEach(Cell::getCurrentProductivity));
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

		CellsSet.getServicesNames().forEach(name -> {
			servicedemandListener[y][m.get()] = totalSupply.get(name) + "";
			servicedemandListener[y][m.get() + CellsSet.getServicesNames().size()] = DemandModel.getRegionalDemand(name,
					year, regionName) + "";
			m.getAndIncrement();
		});
	}

	private void compositionAFT(int year) {
		int y = year - PathsLoader.getStartYear() + 1;
		AFTsLoader.hashAgentNbrRegions.get(regionName).forEach((name, value) -> {
			compositionAftListener[y][Tools.indexof(name, compositionAftListener[0])] = value + "";
		});
	}

	private void updateCSVFiles() {
		String dir = PathTools.makeDirectory(
				ModelRunnerController.outPutFolderName + File.separator + "region_" + regionName + File.separator + "");
		if (ModelRunner.writeCsvFiles) {
			Path aggregateAFTComposition = Paths.get(dir + "region_" + regionName + "-AggregateAFTComposition.csv");
			CsvTools.writeCSVfile(compositionAftListener, aggregateAFTComposition);
			Path aggregateServiceDemand = Paths.get(dir + "region_" + regionName + "-AggregateServiceDemand.csv");
			CsvTools.writeCSVfile(servicedemandListener, aggregateServiceDemand);
		}

	}

}
