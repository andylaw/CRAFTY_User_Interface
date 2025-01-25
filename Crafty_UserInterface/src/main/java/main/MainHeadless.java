package main;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.MaskRestrictionDataLoader;
import dataLoader.PathsLoader;
import dataLoader.S_WeightLoader;
import dataLoader.ServiceSet;
import fxmlControllers.ModelRunnerController;
import fxmlControllers.TabPaneController;
import model.CellsSet;
import model.RegionClassifier;
import output.Listener;
import utils.analysis.CustomLogger;
import utils.filesTools.PathTools;

public class MainHeadless {
	private static final CustomLogger LOGGER = new CustomLogger(MainHeadless.class);

	public static void main(String[] args) {
		LOGGER.info(/* "\u001B[33m"+ */"--Starting running CRAFTY--"/* +"\u001B[0m" */);
		CraftyOptions options = OptionsParser.parseArguments(args);
		ConfigLoader.config = ConfigLoader.loadConfig(options.getConfigFilePath());
		String projectDirectoryPath = options.getProjectDirectoryPath();
		if (projectDirectoryPath != null) {
			ConfigLoader.config.project_path = projectDirectoryPath;
		}
		modelInitialisation();
		runHeadless();
		System.out.println("------------CRAFTY executed from Java-----------------");
	}

	public static void modelInitialisation() {
		System.out.println("is linux:  " + System.getProperty("os.name").toLowerCase().contains("linux"));
		PathsLoader.initialisation(Paths.get(ConfigLoader.config.project_path));
		PathsLoader.setScenario(ConfigLoader.config.scenario);
		CellsLoader.loadCapitalsList();
		ServiceSet.loadServiceList();
		TabPaneController.cellsLoader.loadMap();
		RegionClassifier.initialation();
		S_WeightLoader.updateWorldWeight();
		AFTsLoader.hashAgentNbrRegions();
		CellsSet.setCellsSet(TabPaneController.cellsLoader);
		MaskRestrictionDataLoader.allMaskAndRistrictionUpdate();
	}

	static void runHeadless() {
		ModelRunnerController.init();
		ModelRunnerController.tick = new AtomicInteger(PathsLoader.getStartYear());
		Listener.outputfolderPath(ConfigLoader.config.output_folder_name);
		if (ConfigLoader.config.export_LOGGER) {
			CustomLogger
					.configureLogger(Paths.get(ConfigLoader.config.output_folder_name + File.separator + "LOGGER.txt"));
			PathTools.writeFile(ConfigLoader.config.output_folder_name + File.separator + "config.txt",
					ModelRunnerController.exportConfigurationFile(), false);
		}
		ModelRunnerController.demandEquilibrium();

		for (int i = 0; i <= PathsLoader.getEndtYear() - PathsLoader.getStartYear(); i++) {
			PathsLoader.setCurrentYear(ModelRunnerController.tick.get());
			LOGGER.info("-------------   " + PathsLoader.getCurrentYear() + "   --------------");
			System.out.println("-------------   " + PathsLoader.getCurrentYear() + "   --------------");
			ModelRunnerController.runner.step();
			ModelRunnerController.tick.getAndIncrement();
		}
	}

}
