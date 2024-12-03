package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import dataLoader.AFTsLoader;
import dataLoader.PathsLoader;
import main.ConfigLoader;
import utils.analysis.CustomLogger;
import utils.genaralTools.Utils;
import output.ListenerByRegion;

/**
 * @author Mohamed Byari
 *
 */

public class RegionalModelRunner {
	private static final CustomLogger LOGGER = new CustomLogger(RegionalModelRunner.class);
	ConcurrentHashMap<String, Double> regionalSupply;
	private ConcurrentHashMap<String, Double> marginal = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Manager, Double> distributionMean;
	public Region R;

	public ListenerByRegion listner;

	public RegionalModelRunner(String regionName) {
		R = RegionClassifier.regions.get(regionName);
		listner = new ListenerByRegion(R);
		listner.initializeListeners();
	}

	private void calculeRegionsSupply() {
		regionalSupply = new ConcurrentHashMap<>();
		R.getCells().values().parallelStream().forEach(c -> {
			c.currentProductivity.forEach((s, v) -> {
				regionalSupply.merge(s, v, Double::sum);
			});
		});
	}

	private void productivityForAll() {
		R.getCells().values().parallelStream().forEach(cell -> cell.calculateCurrentProductivity(R));
	}

	private void calculeDistributionMean() {
		LOGGER.info("Region: [" + R.getName() + "] Calculating Distribution Mean & Land abandonment");
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
		LOGGER.info("Rigion: [" + R.getName() + "] Total Supply = " + regionalSupply);
		int tick = year - PathsLoader.getStartYear();
		regionalSupply.forEach((serviceName, serviceSupply) -> {
			Service s = R.getServicesHash().get(serviceName);
			double serviceDemand = s.getDemands().get(tick);
			double marg = ConfigLoader.config.remove_negative_marginal_utility
					? Math.max(serviceDemand - serviceSupply, 0)
					: serviceDemand - serviceSupply;
			if (ModelRunner.averaged_residual_demand_per_cell) {
				marg = marg / R.getCells().size();
			}
			marg = marg * s.getWeights().get(tick);
			marginal.put(serviceName, marg);
		});
	}

	void takeOverUnmanageCells() {
		LOGGER.trace("Region: [" + R.getName() + "] Take over unmanaged cells & Launching the competition process...");
		R.getUnmanageCellsR().parallelStream().forEach(c -> {
			c.competition(marginal, distributionMean, R);
			if (c.getOwner() != null && !c.getOwner().isAbandoned()) {
				R.getUnmanageCellsR().remove(c);
			}
		});
	}

	public void regionalSupply() {
		if (RegionClassifier.regionalization) {
			productivityForAll();
		} else {
			productivityForAllExecutor();
		}
		calculeRegionsSupply();
		LOGGER.info("Region: [" + R.getName() + "] Total Supply calculation" + regionalSupply);

	}

	public void initialDSEquilibrium() {
		regionalSupply();
		regionalSupply.forEach((serviceName, serviceSuplly) -> {
			double factor = 1;
			if (serviceSuplly != 0) {
				if (R.getServicesHash().get(serviceName).getDemands().get(0) == 0) {
					LOGGER.warn("Demand for " + serviceName + " = 0");
				} else {
					factor = R.getServicesHash().get(serviceName).getDemands().get(0) / (serviceSuplly);
				}
			} else {
				factor = Double.MAX_VALUE;
				LOGGER.warn("Supply for " + serviceName + " = 0 (The AFT baseline map is unable to produce  "
						+ serviceName + " service)");
			}
			R.getServicesHash().get(serviceName).setCalibration_Factor(factor != 0 ? factor : 1);
		});
		listner.fillDSEquilibriumListener(R.getServicesHash());
		LOGGER.info(
				"Initial Demand Service Equilibrium Factor= " + R.getName() + ": " + R.getServiceCalibration_Factor());
	}

//	public void initialDSEquilibrium() {
//		regionalSupply();
//		initialDSEquilibrium(R.getServicesHash(), regionalSupply);
//		R.getServicesHash().values().forEach(s -> {
//			s.getDemands().forEach((year, value) -> {
//				s.getDemands().put(year, value / s.getCalibration_Factor());
//			});
//		});
//		LOGGER.info(
//				"Initial Demand Service Equilibrium Factor= " + R.getName() + ": " + R.getServiceCalibration_Factor());
//	}

	public void go(int year) {
		calibrateDemands(year);
		listner.exportFiles(year, regionalSupply);
		calculeMarginal(year);
		calculeDistributionMean();
		giveUp();
		takeOverUnmanageCells();
		competition(year);
		AFTsLoader.hashAgentNbr(R.getName());
	}

	private void calibrateDemands(int year) {
		int tick = year - PathsLoader.getStartYear();
		R.getServicesHash().values().forEach(s -> {
			s.getDemands().put(tick, s.getDemands().get(tick) / s.getCalibration_Factor());
		});

	}

	private void giveUp() {
		if (ModelRunner.use_abandonment_threshold) {
			ConcurrentHashMap<String, Cell> randomCellsubSetForGiveUp = CellsSet.getRandomSubset(R.getCells(),
					ModelRunner.land_abandonment_percentage);
			if (randomCellsubSetForGiveUp != null) {
				randomCellsubSetForGiveUp.values().parallelStream().forEach(c -> {
					c.giveUp(marginal, distributionMean, R);
				});
			}
		}
	}

	private void competition(int year) {
		// Randomly select % of the land available for competition
		ConcurrentHashMap<String, Cell> randomCellsubSet = CellsSet.getRandomSubset(R.getCells(),
				ModelRunner.participating_cells_percentage);
		if (randomCellsubSet != null) {
			List<ConcurrentHashMap<String, Cell>> subsubsets = Utils.splitIntoSubsets(randomCellsubSet,
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
				servicesBeforeCompetition.forEach((key, value) -> regionalSupply.merge(key, -value, Double::sum));
				servicesAfterCompetition.forEach((key, value) -> regionalSupply.merge(key, value, Double::sum));
				calculeMarginal(year);
			});
		}
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

}
