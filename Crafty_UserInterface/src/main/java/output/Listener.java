package output;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dataLoader.AFTsLoader;
import dataLoader.PathsLoader;
import dataLoader.ServiceSet;
import main.ConfigLoader;
import model.ModelRunner;
import model.RegionClassifier;
import model.RegionalModelRunner;
import model.Service;
import utils.filesTools.CsvTools;
import utils.filesTools.PathTools;
import utils.graphicalTools.Tools;

public class Listener {
//	public static String outPutFolderName;
	public static String[][] compositionAftListener;
	public static String[][] servicedemandListener;
	private static String[][] DSEquilibriumListener;

	public void initializeListeners() {
		servicedemandListener = new String[PathsLoader.getEndtYear() - PathsLoader.getStartYear()
				+ 2][ServiceSet.getServicesList().size() * 2 + 1];
		servicedemandListener[0][0] = "Year";
		for (int i = 1; i < ServiceSet.getServicesList().size() + 1; i++) {
			servicedemandListener[0][i] = "Supply:" + ServiceSet.getServicesList().get(i - 1);
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

	public void outPutserviceDemandToCsv(int year, ConcurrentHashMap<String, Double> totalSupply) {
		AtomicInteger m = new AtomicInteger(1);
		int y = year - PathsLoader.getStartYear() + 1;
		servicedemandListener[y][0] = year + "";

		ServiceSet.getServicesList().forEach(serviceName -> {
			servicedemandListener[y][m.get()] = totalSupply.get(serviceName) + "";
			Service s = ServiceSet.worldService.get(serviceName);
			System.out.print(s.getName() + "||  " + s.getCalibration_Factor() + "-->");
			System.out.println(s.getDemands().get(year - PathsLoader.getStartYear()));
			servicedemandListener[y][m.get() + ServiceSet.getServicesList().size()] = (s.getDemands()
					.get(year - PathsLoader.getStartYear())/ s.getCalibration_Factor() /* */) + "";
			m.getAndIncrement();
		});
	}

	public void compositionAFT(int year) {
		int y = year - PathsLoader.getStartYear() + 1;
		compositionAftListener[y][0] = year + "";
		AFTsLoader.hashAgentNbr.forEach((name, value) -> {
			compositionAftListener[y][Tools.indexof(name, compositionAftListener[0])] = value + "";
		});
	}

	public static void DSEquilibriumListener() {
		for (RegionalModelRunner rr : ModelRunner.regionsModelRunner.values()) {
			for (int j = 0; j < ServiceSet.getServicesList().size(); j++) {
				DSEquilibriumListener[j + 1][Tools.indexof(rr.R.getName(),
						DSEquilibriumListener[0])] = rr.listner.DSEquilibriumListener[j + 1][1];
			}
		}
	}

	public void updateCSVFilesWolrd() {
		Path aggregateAFTComposition = Paths.get(ConfigLoader.config.output_folder_name + File.separator
				+ PathsLoader.getScenario() + "Total-AggregateAFTComposition.csv");
		CsvTools.writeCSVfile(compositionAftListener, aggregateAFTComposition);
		Path aggregateServiceDemand = Paths.get(ConfigLoader.config.output_folder_name + File.separator
				+ PathsLoader.getScenario() + "Total-AggregateServiceDemand.csv");
		CsvTools.writeCSVfile(servicedemandListener, aggregateServiceDemand);

		Path DSEquilibriumPath = Paths.get(ConfigLoader.config.output_folder_name + File.separator
				+ PathsLoader.getScenario() + "Total-AggregateDemandServicesEquilibrium.csv");
		DSEquilibriumListener();
		CsvTools.writeCSVfile(DSEquilibriumListener, DSEquilibriumPath);
	}

	public void writOutPutMap(int year) {
		if ((PathsLoader.getCurrentYear() - PathsLoader.getStartYear()) % ConfigLoader.config.csv_output_frequency == 0
				|| PathsLoader.getCurrentYear() == PathsLoader.getEndtYear()) {
			CsvTools.exportToCSV(ConfigLoader.config.output_folder_name + File.separator + PathsLoader.getScenario()
					+ "-Cell-" + year + ".csv");
		}
	}

	public static void outputfolderPath(String textFieldGetText) {
		if (textFieldGetText.equals("") || textFieldGetText.equalsIgnoreCase("Default")) {
			ConfigLoader.config.output_folder_name = "Default simulation folder";
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");
			String formattedDate = now.format(formatter);
			ConfigLoader.config.output_folder_name = "Default_Run_Output_" + formattedDate;
		} else {
			ConfigLoader.config.output_folder_name = textFieldGetText;
		}

		String dir = PathTools.makeDirectory(PathsLoader.getProjectPath() + PathTools.asFolder("output"));
		dir = PathTools.makeDirectory(dir + PathsLoader.getScenario());
		dir = PathTools.makeDirectory(dir + File.separator + ConfigLoader.config.output_folder_name);
		ConfigLoader.config.output_folder_name = dir;
	}

}
