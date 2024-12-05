package model;

import java.util.concurrent.ConcurrentHashMap;

import dataLoader.AFTsLoader;
import dataLoader.PathsLoader;
import fxmlControllers.MasksPaneController;
import fxmlControllers.TabPaneController;
import main.ConfigLoader;
import output.Listener;
import utils.analysis.CustomLogger;
import utils.analysis.Tracker;

/**
 * @author Mohamed Byari
 *
 */

public class ModelRunner {
	private static final CustomLogger LOGGER = new CustomLogger(ModelRunner.class);
	public String colorDisplay = "AFT";
	public static boolean mapSynchronisation = true;
	public static int mapSynchronisationGap = 5;

	public ConcurrentHashMap<String, Double> totalSupply;
	public static ConcurrentHashMap<String, RegionalModelRunner> regionsModelRunner;
	static Listener listner= new Listener() ;

	public ModelRunner() {
		initializeRegions();
	}

	public static void initializeRegions() {
		regionsModelRunner = new ConcurrentHashMap<>();
		RegionClassifier.regions.keySet().forEach(regionName -> {
			regionsModelRunner.put(regionName, new RegionalModelRunner(regionName));
		});
		listner.initializeListeners();
	}

	public void go() {
		int year = Math.min(Math.max(PathsLoader.getCurrentYear(), PathsLoader.getStartYear()),
				PathsLoader.getEndtYear());
		totalSupply = new ConcurrentHashMap<>(); 
		LOGGER.info("Cells.updateCapitals");
		TabPaneController.cellsLoader.updateCapitals(year);
		AFTsLoader.updateAFTs();
		MasksPaneController.Maskloader.CellSetToMaskLoader(year);
		
		aggregateTotalSupply();
		if (ConfigLoader.config.generate_csv_files) {
			listner.compositionAFT(year);
		}
		Tracker.trackSupply(year);

		regionsModelRunner.values().forEach(RegionalRunner -> {
			RegionalRunner.go(year);
		});
		RegionClassifier.aggregateServicesToWorldService(year- PathsLoader.getStartYear());
		if (ConfigLoader.config.generate_csv_files) {
			listner.outPutserviceDemandToCsv(year, totalSupply);
			listner.writOutPutMap(year);
			listner.updateCSVFilesWolrd();
		}

		if (mapSynchronisation
				&& ((PathsLoader.getCurrentYear() - PathsLoader.getStartYear()) % mapSynchronisationGap == 0
						|| PathsLoader.getCurrentYear() == PathsLoader.getEndtYear())) {
			CellsSet.colorMap(colorDisplay);
		}
		AFTsLoader.hashAgentNbr();
	}

	private void aggregateTotalSupply() {
		regionsModelRunner.values().forEach(RegionalRunner -> {
			RegionalRunner.regionalSupply();
			RegionalRunner.regionalSupply.forEach((key, value) -> totalSupply.merge(key, value, Double::sum));
		});
	}

}
