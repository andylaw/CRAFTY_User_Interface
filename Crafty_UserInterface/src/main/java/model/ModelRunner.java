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
	public String colorDisplay = "FR";
	public static boolean mapSynchronisation = true;
	public static int mapSynchronisationGap = 5;
	public static boolean generate_csv_files = ConfigLoader.config.generate_csv_files;
	public static boolean initial_demand_supply_equilibrium = ConfigLoader.config.initial_demand_supply_equilibrium;
	public static boolean use_abandonment_threshold = ConfigLoader.config.use_abandonment_threshold;
	public static boolean mutate_on_competition_win = ConfigLoader.config.mutate_on_competition_win;
	public static double MostCompetitorAFTProbability = ConfigLoader.config.MostCompetitorAFTProbability;
	public static boolean averaged_residual_demand_per_cell = ConfigLoader.config.averaged_residual_demand_per_cell;
	public static boolean use_neighbor_priority = ConfigLoader.config.use_neighbor_priority;
	public static int neighbor_radius = ConfigLoader.config.neighbor_radius;
	public static double participating_cells_percentage = ConfigLoader.config.participating_cells_percentage;
	public static int marginal_utility_calculations_per_tick = ConfigLoader.config.marginal_utility_calculations_per_tick;
	public static double mutation_interval = ConfigLoader.config.mutation_interval;
	public static double land_abandonment_percentage = ConfigLoader.config.land_abandonment_percentage;

	public ConcurrentHashMap<String, Double> totalSupply;
	public static ConcurrentHashMap<String, RegionalModelRunner> regionsModelRunner;
	static Listener listner= new Listener();

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
		if (generate_csv_files) {
			listner.outPutserviceDemandToCsv(year, totalSupply);
			listner.compositionAFT(year);
		}
		Tracker.trackSupply(year);

		regionsModelRunner.values().forEach(RegionalRunner -> {
			RegionalRunner.go(year);
		});
		if (generate_csv_files) {
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
