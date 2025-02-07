package model;

import java.util.concurrent.ConcurrentHashMap;

import dataLoader.AFTsLoader;
import dataLoader.PathsLoader;
import fxmlControllers.MasksPaneController;
import fxmlControllers.TabPaneController;
import main.Config;
import main.ConfigLoader;
import output.Listener;
import utils.analysis.Tracker;

/**
 * @author Mohamed Byari
 *
 */

public class ModelRunner  {
//	private static final CustomLogger LOGGER = new CustomLogger(ModelRunner.class);
	public String colorDisplay = "AFT";
	public ConcurrentHashMap<String, Double> totalSupply;
	public static ConcurrentHashMap<String, RegionalModelRunner> regionsModelRunner;
	public static Listener listner = new Listener();

	
	public static void setup() {
		regionsModelRunner = new ConcurrentHashMap<>();
		RegionClassifier.initialation();
		AFTsLoader.hashAgentNbrRegions();
//		S_WeightLoader.updateWorldWeight();
		RegionClassifier.regions.keySet().forEach(regionName -> {
			regionsModelRunner.put(regionName, new RegionalModelRunner(regionName));
		});
		listner.initializeListeners();
	}


	public void step() {
		int year = Math.min(Math.max(PathsLoader.getCurrentYear(), PathsLoader.getStartYear()),
				PathsLoader.getEndtYear());

		totalSupply = new ConcurrentHashMap<>();
		TabPaneController.cellsLoader.updateCapitals(year);
		AFTsLoader.updateAFTs();
		MasksPaneController.Maskloader.CellSetToMaskLoader(year);
		aggregateTotalSupply();
		regionsModelRunner.values().forEach(RegionalRunner -> {
			RegionalRunner.step(year);
		});
		listnerOutput(year);
		mapSynchronisation();
		AFTsLoader.hashAgentNbr();
	}

	private void mapSynchronisation() {
		if (Config.mapSynchronisation
				&& ((PathsLoader.getCurrentYear() - PathsLoader.getStartYear()) % Config.mapSynchronisationGap == 0
						|| PathsLoader.getCurrentYear() == PathsLoader.getEndtYear())) {
			CellsSet.colorMap(colorDisplay);
		}
	}

	private void listnerOutput(int year) {
		if (ConfigLoader.config.generate_csv_files) {
			Tracker.trackSupply(year);
			listner.compositionAFT(year);
			listner.outPutserviceDemandToCsv(year, totalSupply);
			listner.writOutPutMap(year);
			listner.updateCSVFilesWolrd();
		}
	}

	private void aggregateTotalSupply() {
		regionsModelRunner.values().forEach(RegionalRunner -> {
			RegionalRunner.regionalSupply();
			RegionalRunner.regionalSupply.forEach((key, value) -> totalSupply.merge(key, value, Double::sum));
		});
	}

}
