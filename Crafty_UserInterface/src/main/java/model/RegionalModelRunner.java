package model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.CurvesLoader;
import dataLoader.DemandModel;
import dataLoader.Paths;
import fxmlControllers.MasksPaneController;
import fxmlControllers.ModelRunnerController;

/**
 * @author Mohamed Byari
 *
 */

public class RegionalModelRunner {
	private static final Logger LOGGER = LogManager.getLogger(RegionalModelRunner.class);
	public CellsLoader cells;
	public String colorDisplay = "FR";
	public static boolean mapSynchronisation = true;
	public static int mapSynchronisationGap = 5;
	public static boolean writeCsvFiles = true;
	public static int writeCsvFilesGap = 10;
	public static boolean removeNegative = false;
	public static boolean usegiveUp = true;
	public static boolean isMutated = false;
	public static double MostCompetitorAFTProbability = 0.8;
	public static boolean isAveragedPerCellResidualDemand = false;
	public static boolean NeighboorEffect = true;
	public static double probabilityOfNeighbor = 0.95;
	public static int NeighborRaduis = 2;
	public static double percentageCells = 0.01;
	public static int nbrOfSubSet = 10;
	public static double mutationIntval = 0.1;
	public static double percentageOfGiveUp = 0.05;

	public ConcurrentHashMap<String, Double> totalSupply;
	static ConcurrentHashMap<String, Double> marginal = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, Double> distributionMean;
	public String[][] compositionAFT;
	public String[][] servicedemand;
	
	ConcurrentHashMap<String, Cell> hashRegionCell = new ConcurrentHashMap<>();

	

	public RegionalModelRunner(CellsLoader cells, String regionName) {
		this.cells = cells;
		compositionAFT = new String[Paths.getEndtYear() - Paths.getStartYear() + 2][cells.AFtsSet.size()];
		servicedemand = new String[Paths.getEndtYear() - Paths.getStartYear() + 2][CellsSet.getServicesNames().size()
				* 2];
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			servicedemand[0][i] = "ServiceSupply:" + CellsSet.getServicesNames().get(i);
			servicedemand[0][i + CellsSet.getServicesNames().size()] = "Demand:" + CellsSet.getServicesNames().get(i);
		}
		int i = 0;
		for (String label : AFTsLoader.getAftHash().keySet()) {
			compositionAFT[0][i++] = label;
		}
//		hashRegionCell=	ModelRunner.regions.get(regionName);
//		System.out.println(hashRegionCell.size());
	}

	void calculeSystemSupply() {

		LOGGER.info("Total Supply calculation");
		totalSupply = new ConcurrentHashMap<>();
		hashRegionCell.values().parallelStream().forEach(c -> {
//			c.getCurrentProductivity().forEach((s, v) -> {
//				totalSupply.merge(s, v, Double::sum);
//			});
		});
		LOGGER.info("Total Supply = " + totalSupply);
	}


	void productivityForAll() {
		LOGGER.info("Productivity calculation for all cells ");
		hashRegionCell.values().parallelStream().forEach(Cell::getCurrentProductivity);
	}

	void calculeDistributionMean() {
		distributionMean = new ConcurrentHashMap<>();
		hashRegionCell.values().parallelStream().forEach(c -> {
			if (c.getOwner() != null) {
				distributionMean.merge(c.getOwner().getLabel(), c.utility(), Double::sum);
			}
		});
		// Calculate the mean distribution
		distributionMean.forEach((aftName, total) -> {
			distributionMean.put(aftName, total / AFTsLoader.hashAgentNbr.get(aftName));//++
		});
	}

	void calculeMarginal(int year) {

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
		productivityForAll();

		calculeSystemSupply();
		outPutservicedemandToCsv(year);
		LOGGER.info("Marginal Utility Supply calculation");
		calculeMarginal(year);
		LOGGER.info("Calculating Distribution Mean");
		calculeDistributionMean();
		MasksPaneController.Maskloader.CellSetToMaskLoader(year);
		LOGGER.info("taking over unmanage cell...");

//		if (usegiveUp) {
//			ConcurrentHashMap<String, Cell> randomCellsubSetForGiveUp = CellsSet.getRandomSubset(hashRegionCell,
//					percentageOfGiveUp);
//			if (randomCellsubSetForGiveUp != null) {
//				randomCellsubSetForGiveUp.values().parallelStream().forEach(c -> {
//					c.giveUp();
//				});
//			}
//		}

		LOGGER.info("Take over unmanage cells");
		takeOverUnmanageCells();
		LOGGER.info("Launching the competition process...");

		// Randomly select % of the land available for competition
		ConcurrentHashMap<String, Cell> randomCellsubSet = CellsSet.getRandomSubset(hashRegionCell,
				percentageCells);
		if (randomCellsubSet != null) {
			List<ConcurrentHashMap<String, Cell>> subsubsets = CellsSet.splitIntoSubsets(randomCellsubSet, nbrOfSubSet);
			ConcurrentHashMap<String, Double> servicesBeforeCompetition = new ConcurrentHashMap<>();
			ConcurrentHashMap<String, Double> servicesAfterCompetition = new ConcurrentHashMap<>();

			subsubsets.forEach(subsubset -> {
				if (subsubset != null) {
					subsubset.values().parallelStream().forEach(c -> {
						c.getServices()
								.forEach((key, value) -> servicesBeforeCompetition.merge(key, value, Double::sum));
//						if (usegiveUp) {
//							c.giveUp();
//						}
						c.competition();
						c.getCurrentProductivity();
						c.getServices()
								.forEach((key, value) -> servicesAfterCompetition.merge(key, value, Double::sum));
					});
				}
				servicesBeforeCompetition.forEach((key, value) -> totalSupply.merge(key, -value, Double::sum));
				servicesAfterCompetition.forEach((key, value) -> totalSupply.merge(key, value, Double::sum));
				calculeMarginal(year);
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
		AFTsLoader.hashAgentNbr();

		if (writeCsvFiles) {
			compositionAFT(year);
			writOutPutMap(year);
		}

	}

	void takeOverUnmanageCells() {
		CellsLoader.getUnmanageCells().parallelStream().forEach(c -> {
			c.competition();
			CellsLoader.getUnmanageCells().remove(c);
		});
	}

	void outPutservicedemandToCsv(int year) {
		AtomicInteger m = new AtomicInteger();
		int y = year - Paths.getStartYear() + 1;

		CellsSet.getServicesNames().forEach(name -> {
			servicedemand[y][m.get()] = totalSupply.get(name) + "";
			servicedemand[y][m.get() + CellsSet.getServicesNames().size()] = DemandModel.getDemand(name, year) + "";
			m.getAndIncrement();
		});
	}

	void compositionAFT(int year) {
		int y = year - Paths.getStartYear() + 1;
		AFTsLoader.hashAgentNbr.forEach((name, value) -> {
			compositionAFT[y][Tools.indexof(name, compositionAFT[0])] = value + "";
		});
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

}
