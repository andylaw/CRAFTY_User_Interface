package model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.analysis.Tracker;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.DemandModel;
import dataLoader.PathsLoader;
import dataLoader.ServiceSet;
import fxmlControllers.MasksPaneController;
import fxmlControllers.ModelRunnerController;
import fxmlControllers.TabPaneController;
import main.ConfigLoader;

/**
 * @author Mohamed Byari
 *
 */

public class ModelRunner {
	private static final Logger LOGGER = LogManager.getLogger(ModelRunner.class);
	public String colorDisplay = "FR";
	public static boolean mapSynchronisation = true;
	public static int mapSynchronisationGap = 5;
	public static boolean generate_csv_files = ConfigLoader.config.generate_csv_files;
	public static int csv_output_frequency = ConfigLoader.config.csv_output_frequency;
	public static boolean initial_demand_supply_equilibrium = ConfigLoader.config.initial_demand_supply_equilibrium;
	public static boolean remove_negative_marginal_utility = ConfigLoader.config.remove_negative_marginal_utility;
	public static boolean use_abandonment_threshold = ConfigLoader.config.use_abandonment_threshold;
	public static boolean mutate_on_competition_win = ConfigLoader.config.mutate_on_competition_win;
	public static double MostCompetitorAFTProbability = ConfigLoader.config.MostCompetitorAFTProbability;
	public static boolean averaged_residual_demand_per_cell = ConfigLoader.config.averaged_residual_demand_per_cell;
	public static boolean use_neighbor_priority = ConfigLoader.config.use_neighbor_priority;
	public static double neighbor_priority_probability = ConfigLoader.config.neighbor_priority_probability;
	public static int neighbor_radius = ConfigLoader.config.neighbor_radius;
	public static double participating_cells_percentage = ConfigLoader.config.participating_cells_percentage;
	public static int marginal_utility_calculations_per_tick = ConfigLoader.config.marginal_utility_calculations_per_tick;
	public static double mutation_interval = ConfigLoader.config.mutation_interval;
	public static double land_abandonment_percentage = ConfigLoader.config.land_abandonment_percentage;
	public static boolean track_changes = ConfigLoader.config.track_changes;

	public ConcurrentHashMap<String, Double> totalSupply;

	public static String[][] compositionAftListener;
	public static String[][] servicedemandListener;
	private static String[][] DSEquilibriumListener;
	public static ConcurrentHashMap<String, RegionalModelRunner> regionsModelRunner;

	public ModelRunner() {
		initializeRegions();
	}

	private static void initializeListeners() {
		servicedemandListener = new String[PathsLoader.getEndtYear() - PathsLoader.getStartYear()
				+ 2][ServiceSet.getServicesList().size() * 2 + 1];
		servicedemandListener[0][0] = "Year";
		for (int i = 1; i < ServiceSet.getServicesList().size() + 1; i++) {
			servicedemandListener[0][i] = "ServiceSupply:" + ServiceSet.getServicesList().get(i - 1);
			servicedemandListener[0][i + ServiceSet.getServicesList().size()] = "Demand:"
					+ ServiceSet.getServicesList().get(i - 1);
		}
		compositionAftListener = new String[PathsLoader.getEndtYear() - PathsLoader.getStartYear()
				+ 2][AFTsLoader.getAftHash().size() + 1];
		compositionAftListener[0][0] = "Year";
		int k = 1;
		for (String label : AFTsLoader.getAftHash().keySet()) {
			compositionAftListener[0][k++] = label;
		}
		DSEquilibriumListener = new String[ServiceSet.getServicesList().size() + 1][RegionClassifier.regions.size()
				+ 1];
		DSEquilibriumListener[0][0] = "Service";
		int j = 1;
		for (String gerionName : RegionClassifier.regions.keySet()) {
			DSEquilibriumListener[0][j++] = gerionName;
		}
		for (int i = 0; i < ServiceSet.getServicesList().size(); i++) {
			DSEquilibriumListener[i + 1][0] = ServiceSet.getServicesList().get(i);
		}

	}

	public static void initializeRegions() {
		regionsModelRunner = new ConcurrentHashMap<>();
		RegionClassifier.regions.keySet().forEach(regionName -> {
			regionsModelRunner.put(regionName, new RegionalModelRunner(regionName));
		});
		initializeListeners();
	}

	public void go() {
		int year = Math.min(Math.max(PathsLoader.getCurrentYear(), PathsLoader.getStartYear()),
				PathsLoader.getEndtYear());
		totalSupply = new ConcurrentHashMap<>();
		LOGGER.info("Cells.updateCapitals");
		TabPaneController.cellsLoader.updateCapitals(year);
		AFTsLoader.updateAFTs();
		MasksPaneController.Maskloader.CellSetToMaskLoader(year);
		regionsModelRunner.values()/* .parallelStream() */ .forEach(RegionalRunner -> {
			RegionalRunner.regionalSupply();
			RegionalRunner.totalSupply.forEach((key, value) -> totalSupply.merge(key, value, Double::sum));
		});
		if (generate_csv_files) {
			outPutserviceDemandToCsv(year);
			compositionAFT(year);
			updateCSVFiles();
			DSEquilibriumListener();
		}
		Tracker.trackSupply(year);

		regionsModelRunner.values()/* .parallelStream() */ .forEach(RegionalRunner -> {
			RegionalRunner.go(year);
		});

		if (generate_csv_files) {

			writOutPutMap(year);
		}

		if (mapSynchronisation
				&& ((PathsLoader.getCurrentYear() - PathsLoader.getStartYear()) % mapSynchronisationGap == 0
						|| PathsLoader.getCurrentYear() == PathsLoader.getEndtYear())) {
			CellsSet.colorMap(colorDisplay);
		}
		AFTsLoader.hashAgentNbr();
	}

//	ConcurrentHashMap<String, Cell> trackeMasks() {
//		ConcurrentHashMap<String, Cell> hashMask = new ConcurrentHashMap<>();
//		CellsLoader.hashCell.values().forEach(c -> {
//			if (c.getOwner() != null&& c.getOwner().getLabel().equals("Water"))
//				hashMask.put(c.getX()+","+c.getY(), c);
//		});
//		return hashMask;
//	}
//	void trackeMasksNBR() {
//		ConcurrentHashMap<String, Integer> hashMaskNbr = new ConcurrentHashMap<>();
//		CellsLoader.hashCell.values().forEach(c -> {
//			if (c.getOwner() != null&& !c.getOwner().isActive())
//				hashMaskNbr.merge(c.getOwner().getLabel(), 1, Integer::sum);
//		});
//		System.out.println("|| "+hashMaskNbr);
//	}

	private void outPutserviceDemandToCsv(int year) {
		AtomicInteger m = new AtomicInteger(1);
		int y = year - PathsLoader.getStartYear() + 1;
		servicedemandListener[y][0] = year + "";

		ServiceSet.getServicesList().forEach(service -> {
			servicedemandListener[y][m.get()] = totalSupply.get(service) + "";
			servicedemandListener[y][m.get() + ServiceSet.getServicesList().size()] = ServiceSet.worldService
					.get(service).getDemands().get(year - PathsLoader.getStartYear()) + "";
			m.getAndIncrement();
		});
	}

	void compositionAFT(int year) {
		int y = year - PathsLoader.getStartYear() + 1;
		compositionAftListener[y][0] = year + "";
		AFTsLoader.hashAgentNbr.forEach((name, value) -> {
			compositionAftListener[y][Tools.indexof(name, compositionAftListener[0])] = value + "";
		});
	}

	void DSEquilibriumListener() {
		for (RegionalModelRunner rr : regionsModelRunner.values()) {
			for (int j = 0; j < ServiceSet.getServicesList().size(); j++) {
				DSEquilibriumListener[j + 1][Tools.indexof(rr.R.getName(), DSEquilibriumListener[0])] = ""
						+ rr.DSEquilibriumListener[j + 1][1];
			}
		}
	}
	boolean oneTime = true;
	private void updateCSVFiles() {
		Path aggregateAFTComposition = Paths.get(ModelRunnerController.outPutFolderName + File.separator
				+ PathsLoader.getScenario() + "-AggregateAFTComposition.csv");
		CsvTools.writeCSVfile(compositionAftListener, aggregateAFTComposition);
		Path aggregateServiceDemand = Paths.get(ModelRunnerController.outPutFolderName + File.separator
				+ PathsLoader.getScenario() + "-AggregateServiceDemand.csv");
		CsvTools.writeCSVfile(servicedemandListener, aggregateServiceDemand);
		
		if (oneTime) {
			oneTime = false;
			Path DSEquilibriumPath = Paths.get(ModelRunnerController.outPutFolderName + File.separator
					+ PathsLoader.getScenario() + "-AggregateDemandServicesEquilibrium.csv");
			CsvTools.writeCSVfile(DSEquilibriumListener, DSEquilibriumPath);
		}
	}

	private void writOutPutMap(int year) {
		if ((PathsLoader.getCurrentYear() - PathsLoader.getStartYear()) % csv_output_frequency == 0
				|| PathsLoader.getCurrentYear() == PathsLoader.getEndtYear()) {
			CsvTools.exportToCSV(ModelRunnerController.outPutFolderName + File.separator + PathsLoader.getScenario()
					+ "-Cell-" + year + ".csv");
		}
	}

}
