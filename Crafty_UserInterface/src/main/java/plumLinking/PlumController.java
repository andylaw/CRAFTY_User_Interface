package plumLinking;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

//import ac.ed.lurg.ModelConfig;
//import ac.ed.lurg.ModelMain;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.util.Duration;
import utils.filesTools.CsvTools;
import utils.graphicalTools.GraphicConsol;
import utils.graphicalTools.Tools;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlumController {
	@FXML
	private VBox box;
	@FXML
	private ScrollPane scroll;
	private static final Logger LOGGER = LogManager.getLogger(PlumController.class);

	boolean isPlumInitialized = false;
	boolean isATickFinished = true;
	AtomicInteger tick = new AtomicInteger(1);
	String PlumOutPutPath = "C:\\Users\\byari-m\\Documents\\Data\\PLUM\\PLUM_output\\calibration\\";
	String[] EuCountries = { "Austria", "Bulgaria", "Croatia", "Cyprus", "Czechia", "Denmark", "Estonia", "Finland",
			"France", "Germany", "Greece", "Hungary", "Ireland", "Italy & Malta", "Latvia", "Lithuania",
			"Belgium & Luxembourg", "Netherlands", "Norway", "Poland", "Portugal", "Romania", "Slovakia", "Slovenia",
			"Spain", "Sweden", "Switzerland", "United Kingdom" };

	public void initialize() {
		scroll.setPrefHeight(Screen.getPrimary().getBounds().getHeight() * 0.85);

	}

	void bio_crop_demand_df() {
//		String[][] waste_df = CsvTools.csvReader(PlumOutPutPath + "waste_df.csv");
//		String[][] bio_fractions_df = CsvTools.csvReader(PlumOutPutPath + "bio_fractions_df.csv");
//		String[][] country_demand = CsvTools.csvReader(PlumOutPutPath + "countryDemand.txt");
		try {
			List<Map<String, String>> bio_fractions_df = readCsvIntoList(PlumOutPutPath + "bio_fractions_df.csv");
			List<Map<String, String>> country_demand = readCsvIntoList(PlumOutPutPath + "countryDemand.txt");
			List<Map<String, String>> merge = mergeLists(bio_fractions_df, country_demand);
		} catch (IOException e) {
		}
	}

	private static List<Map<String, String>> readCsvIntoList(String filePath) throws IOException {
		List<Map<String, String>> recordsList = new ArrayList<>();
		try (Reader reader = new FileReader(filePath);
				CSVParser csvParser = new CSVParser(reader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
			for (CSVRecord csvRecord : csvParser) {
				Map<String, String> recordMap = new HashMap<>();
				csvParser.getHeaderNames().forEach(headerName -> recordMap.put(headerName, csvRecord.get(headerName)));
				recordsList.add(recordMap);
			}
		}
		return recordsList;
	}

	private static List<Map<String, String>> mergeLists(List<Map<String, String>> list1,
			List<Map<String, String>> list2) {
		List<Map<String, String>> mergedList = new ArrayList<>();
		for (Map<String, String> map1 : list1) {
			String plumCountryValue = map1.get("PLUM_country");
			String commodityValue = map1.get("Commodity");

			// System.out.println(map1);

			// Find a matching map in list2
			Map<String, String> matchingMap = list2.stream()
					.filter(map2 -> map2.get("PLUM_country").equals(plumCountryValue)
							&& map2.get("Commodity").equals(commodityValue))
					.findFirst().orElse(null);

			if (matchingMap != null) {
				// Create a new map to store the merged data
				Map<String, String> mergedMap = new HashMap<>(map1);
				mergedMap.putAll(matchingMap); // This will overwrite duplicate keys in map1 with values from
												// matchingMap
				mergedList.add(mergedMap);

//                System.out.println(matchingMap);
//                System.out.println(mergedMap);
			}
		}

		return mergedList;
	}

	static ArrayList<String> filterCSV(Path path, String... countries) {
		List<String> T = CsvTools.csvReaderAsVector(path);
		ArrayList<String> listFiltred = new ArrayList<>();
		listFiltred.add(T.get(0));
		for (int i = 1; i < T.size(); i++) {
			for (int j = 0; j < countries.length; j++) {
				if (T.get(i).contains(countries[j])) {
					listFiltred.add(T.get(i));
				}
			}
		}
		return listFiltred;
	}

	@FXML
	void link() {
//		if (!isPlumInitialized) {
//			progressBarFunction(" Initialisation ", x -> {
//				ModelMain.main(new String[] {});
//			}, y -> {
//				isPlumInitialized = true;
//
//			});
//		}
	}

	@FXML
	void oneTick() {
//		if (isPlumInitialized && isATickFinished) {
//			isATickFinished = false;
//			String year = (ModelConfig.BASE_YEAR + ModelMain.newStartYear) + "";
//			progressBarFunction("Running PLUM for: " + year, x -> {
//				ModelMain.theModel.runNTick(tick.getAndIncrement());
//			}, y -> {
//				isATickFinished = true;
//				Button btn = Tools.button("Build demands for Crafty", "");
//				btn.setOnAction(e -> {
//					TextArea console = new TextArea();
//					box.getChildren().addAll(console);
//					GraphicConsol.start(console);
//					logicOfPlumToCraftyDemand();
//				});
//				box.getChildren().addAll(Tools.hBox(btn, Tools.text("    for " + 2020, Color.BLUE)));
//			});
//		}
	}

//	void logicOfPlumToCraftyDemand() {
//		Timeline timeline = new Timeline(
//				new KeyFrame(Duration.ZERO,
//						event -> System.out.println(
//								" INFO : [PlumController] - Reading : " + PlumOutPutPath + "bio_fractions_df.csv")),
//				new KeyFrame(Duration.seconds(0.8),
//						event -> System.out.println(
//								" INFO : [PlumController] - Reading : " + PlumOutPutPath + "countryDemand.txt")),
//				new KeyFrame(Duration.seconds(1.6),
//						event -> System.out
//								.println(" INFO : [PlumController] - Reading : " + PlumOutPutPath + "waste_df.csv")),
//				new KeyFrame(Duration.seconds(2.0),
//						event -> System.out.println(" INFO : [PlumController] - Filter EU coutries for: "
//								+ PlumOutPutPath + "bio_fractions_df.csv")),
//				new KeyFrame(Duration.seconds(2.5),
//						event -> System.out.println(" INFO : [PlumController] - Filter EU coutries for: "
//								+ PlumOutPutPath + "countryDemand.txt")),
//				new KeyFrame(Duration.seconds(3.0),
//						event -> System.out.println(" INFO : [PlumController] - Create bio_crop_demand_df data")),
//				new KeyFrame(Duration.seconds(3.8),
//						event -> System.out.println(
//								" INFO : [PlumController] - Write : " + PlumOutPutPath + "bio_crop_demand_df.csv")),
//				new KeyFrame(Duration.seconds(4.6),
//						event -> System.out
//								.println(" INFO : [PlumController] - Reading : " + PlumOutPutPath + "domestic.txt")),
//				new KeyFrame(Duration.seconds(5.4),
//						event -> System.out.println(" INFO : [PlumController] - Filter EU coutries for: "
//								+ PlumOutPutPath + "domestic.txt")),
//				new KeyFrame(Duration.seconds(6.2),
//						event -> System.out.println(" INFO : [PlumController] - Create bio_crop_demand_df data")),
//				new KeyFrame(Duration.seconds(7.0),
//						event -> System.out.println(
//								" INFO : [PlumController] - Write : " + PlumOutPutPath + "commodity_balances.csv")),
//				new KeyFrame(Duration.seconds(7.8),
//						event -> System.out.println(
//								" INFO : [PlumController] - Reading : " + PlumOutPutPath + "country_fractions.csv")),
//				new KeyFrame(Duration.seconds(8.2),
//						event -> System.out.println(" INFO : [PlumController] - Filter EU coutries for: "
//								+ PlumOutPutPath + "country_fractions.csv")),
//				new KeyFrame(Duration.seconds(8.7), event -> {
//					for (String country : EuCountries) {
//						System.out.println(" INFO : [PlumController] - Create demand data for: " + country);
//						System.out.println(
//								" INFO : [PlumController] - Write : " + PlumOutPutPath + "demand_" + country + ".csv");
//					}
//				}));
//		timeline.play();
//	}

	void progressBarFunction(String titel, Consumer<String> actoin, Consumer<String> succeeded) {
		box.getChildren().forEach(e -> {
			if (e.getClass().getSimpleName().equals("TitledPane")) {
				((TitledPane) e).setExpanded(false);
			}
		});
		ProgressBar progressBar = new ProgressBar();
		progressBar.setMaxWidth(Double.MAX_VALUE);
		TextArea console = new TextArea();
		box.getChildren().addAll(Tools.T(titel, true, progressBar, console));
		GraphicConsol.start(console);
		progressBar.setVisible(true); // Show the progress bar when task starts
		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				actoin.accept("");
				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				succeeded.accept("");
				progressBar.setVisible(false);
				GraphicConsol.restoreOutput();
			}

			@Override
			protected void failed() {
				super.failed();
				progressBar.setVisible(false); // Hide on failure as well
			}
		};
		new Thread(task).start();
	}

}
