package model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.CurvesLoader;
import dataLoader.DemandModel;
import dataLoader.MaskRestrictionDataLoader;
import dataLoader.Paths;
import fxmlControllers.ModelRunnerController;

/**
 * @author Mohamed Byari
 *
 */

public class ModelRunner implements Runnable {
	private static final Logger LOGGER = LogManager.getLogger(ModelRunner.class);
	public CellsLoader cells;
	public String colorDisplay = "FR";
	public boolean mapSynchronisation = true;
	public int mapSynchronisationGap = 5;
	public boolean writeCsvFiles = false;
	public int writeCsvFilesGap = 5;
	public boolean removeNegative = false;
	public boolean usegiveUp = false;
	public boolean isMutated = false;
	public boolean withBestAFT = false;
	public boolean isAveragedPerCellResidualDemand = false;
	public boolean NeighboorEffect = false;
	public double percentageCells = 0.015;
	public int nbrOfSubSet = 10;
	public double mutationIntval = 0.1;

	public ConcurrentHashMap<String, Double> totalSupply;
	static ConcurrentHashMap<String, Double> marginal = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, Double> distributionMean;

	public String[][] compositionAFT;
	public String[][] servicedemand;

	public ModelRunner(CellsLoader cells) {
		this.cells = cells;
		compositionAFT = new String[Paths.getEndtYear() - Paths.getStartYear() + 2][cells.AFtsSet.size()];
		servicedemand = new String[Paths.getEndtYear() - Paths.getStartYear() + 2][CellsSet.getServicesNames().size()
				* 2];
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			servicedemand[0][i] = "ServiceSupply:" + CellsSet.getServicesNames().get(i);
			servicedemand[0][i + CellsSet.getServicesNames().size()] = "Demand:" + CellsSet.getServicesNames().get(i);
		}
		AtomicInteger s = new AtomicInteger();
		AFTsLoader.getAftHash().keySet().forEach((label) -> {
			compositionAFT[0][s.getAndIncrement()] = label;
		});

	}

	void calculeSystemSupply() {

		LOGGER.info("Total Supply calculation");
		totalSupply = new ConcurrentHashMap<>();
		CellsLoader.hashCell.values().parallelStream().forEach(c -> {
			c.services.forEach((s, v) -> {
				totalSupply.merge(s, v, Double::sum);
			});
		});

		LOGGER.info("Total Supply = " + totalSupply);
	}

	void productivityForAll() {
		LOGGER.info("Services productivity calculation for all cells ");
//		CellsLoader.hashCell.values().parallelStream().forEach(c -> {
//			c.putservices();
//		});
		int processors = Runtime.getRuntime().availableProcessors();
		try (ForkJoinPool customThreadPool = new ForkJoinPool(processors * 10)) {
			try {
				customThreadPool.submit(() -> CellsLoader.hashCell.values().parallelStream().forEach(Cell::putservices))
						.join();
			} finally {
				customThreadPool.shutdown();
			}
		}
	}

	void calculeDistributionMean() {
		distributionMean = new ConcurrentHashMap<>();
		CellsLoader.hashCell.values().parallelStream().forEach(c -> {
			if (c.getOwner() != null) {
				distributionMean.merge(c.getOwner().getLabel(), c.utility(), Double::sum);
			}
		});

		// Calculate the mean distribution
		distributionMean.forEach((aftName, total) -> {
			distributionMean.put(aftName, total / AFTsLoader.hashAgentNbr.get(aftName));
		});
	}

	void calculeMarginal(int year, boolean removeNegative) {

		totalSupply.forEach((serviceName, serviceVal) -> {
			double demand = DemandModel.getDemand(serviceName, year);
			double marg = removeNegative ? Math.max(demand - serviceVal, 0) : demand - serviceVal;
			if (isAveragedPerCellResidualDemand) {
				marg = marg / CellsLoader.getNbrOfCells();
			}
			marg = CurvesLoader.hashServicesCurves.get(serviceName).linearFunction(marg);
			marginal.put(serviceName, marg);
		});
	}

	public void go() {
		int year = Paths.getCurrentYear() < Paths.getEndtYear() ? Paths.getCurrentYear() : Paths.getEndtYear();

		LOGGER.info("Cells.updateCapitals");
		cells.updateCapitals(year);

		// calcule supply
		productivityForAll();

		calculeSystemSupply();

		// update demande & calcule marginal
		LOGGER.info("Marginal Utility Supply calculation");
		calculeMarginal(year, removeNegative);

		if (usegiveUp) {
			LOGGER.info("Calculating Distribution Mean");
			calculeDistributionMean();
			System.out.println("-->" + distributionMean);
			LOGGER.info("Distribution Mean... done");
		}
		// upDateMaskif needed
		MaskRestrictionDataLoader.updateCellsmask(year);
		LOGGER.info("taking over unmanage cell...");
		// take over unmanage cells
		takeOverUnCells();
		LOGGER.info("Launching the competition process...");
		// Randomly select % of the land available for competition
		ConcurrentHashMap<String, Cell> randomCellsubSet = CellsSet.getRandomSubset(CellsLoader.hashCell,
				percentageCells);
		if (randomCellsubSet != null) {

			List<ConcurrentHashMap<String, Cell>> subsubsets = CellsSet.splitIntoSubsets(randomCellsubSet, nbrOfSubSet);
			ConcurrentHashMap<String, Double> servicesBeforeCompetition = new ConcurrentHashMap<>();
			ConcurrentHashMap<String, Double> servicesAfterCompetition = new ConcurrentHashMap<>();

			subsubsets.forEach(subsubset -> {
				subsubset.values().parallelStream().forEach(c -> {
					c.getServices().forEach((key, value) -> servicesBeforeCompetition.merge(key, value, Double::sum));
					if (usegiveUp) {
						c.giveUp();
					}
					// set the competition
					if (withBestAFT) {
						c.CompetitionWithThebestAFt(isMutated, mutationIntval);
					} else {
						c.CompetitionWithRandomAFt(isMutated, mutationIntval);
					}
					c.getServices().forEach((key, value) -> servicesAfterCompetition.merge(key, value, Double::sum));
				});

				servicesBeforeCompetition.forEach((key, value) -> totalSupply.merge(key, -value, Double::sum));
				servicesAfterCompetition.forEach((key, value) -> totalSupply.merge(key, value, Double::sum));
				calculeMarginal(year, removeNegative);
			});
		} else {
			LOGGER.error("Faild to select a random subset of cells");
		}
		LOGGER.info("Competition Process Completed");
		// display Map
		if (mapSynchronisation && ((Paths.getCurrentYear() - Paths.getStartYear()) % mapSynchronisationGap == 0
				|| Paths.getCurrentYear() == Paths.getEndtYear())) {
			CellsSet.colorMap(colorDisplay);
		}

		if (writeCsvFiles) {
			// create .csv output files: services and AFT for each land
			outPutChartsToCsv(year);
			writOutPutMap(year);
		}
		AFTsLoader.hashAgentNbr();
	}

	void takeOverUnCells() {
		CellsLoader.getUnmanageCells().parallelStream().forEach(c -> {
			if (c.getOwner() == null) {
				c.owner = c.mostCompetitiveAgent();
			}
			CellsLoader.getUnmanageCells().remove(c);
		});
	}

	void outPutChartsToCsv(int year) {
		AtomicInteger m = new AtomicInteger();
		int y = year - Paths.getStartYear() + 1;

		CellsSet.getServicesNames().forEach(name -> {
			servicedemand[y][m.get()] = totalSupply.get(name) + "";
			servicedemand[y][m.get() + CellsSet.getServicesNames().size()] = DemandModel.getDemand(name, year) + "";
			m.getAndIncrement();
		});
//	A switch should be added here because it consumes a lot of calculations for large projects (EU-1km). and that could be produiced after simulation
//		HashMap<String, Integer> AgentNbr = AFTsLoader.hashAgentNbr();
//		AtomicInteger N = new AtomicInteger();
//		AgentNbr.forEach((name, value) -> {
//			compositionAFT[y][N.getAndIncrement()] = value + "";
//		});
	}

	private void writOutPutMap(int year) {
		if ((Paths.getCurrentYear() - Paths.getStartYear()) % writeCsvFilesGap == 0
				|| Paths.getCurrentYear() == Paths.getEndtYear()) {
			String dir = PathTools.makeDirectory(Paths.getProjectPath() + "\\output\\");
			dir = PathTools.makeDirectory(dir + Paths.getScenario());
			dir = PathTools.makeDirectory(dir + "\\" + ModelRunnerController.outPutFolderName);
			CsvTools.exportToCSV(dir + "\\" + Paths.getScenario() + "-Cell-" + year + ".csv");
		}
	}

	@Override
	public void run() {
		go();

	}

}
