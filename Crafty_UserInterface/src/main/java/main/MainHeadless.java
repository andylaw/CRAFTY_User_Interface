package main;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import dataLoader.CellsLoader;
import dataLoader.MaskRestrictionDataLoader;
import dataLoader.PathsLoader;
import dataLoader.ServiceSet;
import fxmlControllers.ModelRunnerController;
import fxmlControllers.TabPaneController;
import model.CellsSet;
import model.ModelRunner;
import plumLinking.PlumCommodityMapping;
import utils.analysis.CustomLogger;

public class MainHeadless {
	private static final CustomLogger LOGGER = new CustomLogger(MainHeadless.class);
	static PlumCommodityMapping plumMaper= new PlumCommodityMapping();

	public static void main(String[] args) {
		LOGGER.info(/* "\u001B[33m"+ */"--Starting runing CRAFTY--"/* +"\u001B[0m" */);
		modelInitialisation();
		plumMaper.initialize();
	//	run();
	}

	static void modelInitialisation() {
		System.out.println("is linux:  " + System.getProperty("os.name").toLowerCase().contains("linux"));
		PathsLoader.initialisation(Paths.get(ConfigLoader.config.project_path));
		PathsLoader.setScenario(ConfigLoader.config.scenario);
		CellsLoader.loadCapitalsList();
		ServiceSet.loadServiceList();
		TabPaneController.cellsLoader.loadMap();
		CellsSet.setCellsSet(TabPaneController.cellsLoader);
		MaskRestrictionDataLoader.allMaskAndRistrictionUpdate();
	}

	static void run() {
		ModelRunner runner = new ModelRunner();
		ModelRunnerController.tick = new AtomicInteger(PathsLoader.getStartYear());
		ModelRunnerController.outputfolderPath(ConfigLoader.config.output_folder_name);
		if (ConfigLoader.config.export_LOGGER) {
			CustomLogger
					.configureLogger(Paths.get(ModelRunnerController.outPutFolderName + File.separator + "LOGGER.txt"));
		}
		ModelRunnerController.demandEquilibrium();

		for (int i = 0; i <= PathsLoader.getEndtYear() - PathsLoader.getStartYear(); i++) {
			PathsLoader.setCurrentYear(ModelRunnerController.tick.get());
			LOGGER.info("-------------   " + PathsLoader.getCurrentYear() + "   --------------");
			runner.go();
			ModelRunnerController.tick.getAndIncrement();
		}
	}

}
