package model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.analysis.Tracker;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.DemandModel;
import dataLoader.Paths;
import fxmlControllers.MasksPaneController;
import fxmlControllers.ModelRunnerController;

/**
 * @author Mohamed Byari
 *
 */

public class ModelRunner {
	private static final Logger LOGGER = LogManager.getLogger(ModelRunner.class);
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
	public static boolean traker = false;
	public ConcurrentHashMap<String, Double> totalSupply;

	public String[][] compositionAftListener;
	public String[][] servicedemandListener;
	public static ConcurrentHashMap<String, RegionalModelRunner> regions;

	public ModelRunner(CellsLoader cells) {
		this.cells = cells;
		initializeListeners();
		initializeRegions();
	}

	private void initializeListeners() {
		compositionAftListener = new String[Paths.getEndtYear() - Paths.getStartYear() + 2][AFTsLoader.getAftHash()
				.size()];
		servicedemandListener = new String[Paths.getEndtYear() - Paths.getStartYear()
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

	public static void initializeRegions() {
		regions = new ConcurrentHashMap<>();
		RegionClassifier.regions.keySet().forEach(regionName -> {
			regions.put(regionName, new RegionalModelRunner(regionName));
		});
	}

	public void go() {
		int year = Paths.getCurrentYear() < Paths.getEndtYear() ? Paths.getCurrentYear() : Paths.getEndtYear();
		totalSupply = new ConcurrentHashMap<>();
		LOGGER.info("Cells.updateCapitals");
		cells.updateCapitals(year);
		MasksPaneController.Maskloader.CellSetToMaskLoader(year);

		regions.values()/* .parallelStream() */ .forEach(RegionalRunner -> {
			RegionalRunner.regionalSupply();
			RegionalRunner.totalSupply.forEach((key, value) -> totalSupply.merge(key, value, Double::sum));
		});

		if (writeCsvFiles) {
			outPutservicedemandToCsv(year);
			if (traker) {
				Tracker.trackSupply(year);
			}
		}

		regions.values()/* .parallelStream() */ .forEach(RegionalRunner -> {
			RegionalRunner.go(year);
		});

		if (writeCsvFiles) {
			compositionAFT(year);
			writOutPutMap(year);
		}

		if (mapSynchronisation && ((Paths.getCurrentYear() - Paths.getStartYear()) % mapSynchronisationGap == 0
				|| Paths.getCurrentYear() == Paths.getEndtYear())) {
			CellsSet.colorMap(colorDisplay);
		}
		AFTsLoader.hashAgentNbr();
	}

	private void outPutservicedemandToCsv(int year) {
		AtomicInteger m = new AtomicInteger();
		int y = year - Paths.getStartYear() + 1;

		CellsSet.getServicesNames().forEach(name -> {
			servicedemandListener[y][m.get()] = totalSupply.get(name) + "";
			servicedemandListener[y][m.get() + CellsSet.getServicesNames().size()] = DemandModel.getGolbalDemand(name,
					year) + "";
			m.getAndIncrement();
		});
	}

	void compositionAFT(int year) {
		int y = year - Paths.getStartYear() + 1;
		AFTsLoader.hashAgentNbr.forEach((name, value) -> {
			compositionAftListener[y][Tools.indexof(name, compositionAftListener[0])] = value + "";
		});
	}

	private void writOutPutMap(int year) {
		if ((Paths.getCurrentYear() - Paths.getStartYear()) % writeCsvFilesGap == 0
				|| Paths.getCurrentYear() == Paths.getEndtYear()) {
			CsvTools.exportToCSV(
					ModelRunnerController.outPutFolderName + "\\" + Paths.getScenario() + "-Cell-" + year + ".csv");
		}
	}

}
