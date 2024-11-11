package main;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dataLoader.CellsLoader;
import dataLoader.MaskRestrictionDataLoader;
import dataLoader.PathsLoader;
import dataLoader.ServiceSet;
import fxmlControllers.ModelRunnerController;
import fxmlControllers.TabPaneController;
import model.CellsSet;
import model.ModelRunner;

public class MainHeadless {
	private static final Logger LOGGER = LogManager.getLogger(MainHeadless.class);


	public static void main(String[] args) {
		LOGGER.info(/* "\u001B[33m"+ */"--Starting runing CRAFTY--"/* +"\u001B[0m" */);
		 dataImport();
		 run();
		System.out.println("Hello from Java!");
	}

	static void dataImport() {
		System.out.println("is linux:  "+ System.getProperty("os.name").toLowerCase().contains("linux"));
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
		ModelRunnerController.demandEquilibrium();
		for (int i = 0; i <= PathsLoader.getEndtYear() - PathsLoader.getStartYear(); i++) {
			PathsLoader.setCurrentYear(ModelRunnerController.tick.get());
			System.out.println("-------------   " + PathsLoader.getCurrentYear() + "   --------------");
			runner.go();
			ModelRunnerController.tick.getAndIncrement();
		}
	}

}
