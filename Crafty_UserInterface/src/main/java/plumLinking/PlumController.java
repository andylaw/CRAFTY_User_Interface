package plumLinking;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.graphicalTools.GraphicConsol;
import UtilitiesFx.graphicalTools.Tools;
import ac.ed.lurg.ModelConfig;
import ac.ed.lurg.ModelMain;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.util.Duration;

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

	String[] EuCountries = { "Austria", "Bulgaria", "Croatia", "Cyprus", "Czechia", "Denmark", "Estonia", "Finland",
			"France", "Germany", "Greece", "Hungary", "Ireland", "Italy & Malta", "Latvia", "Lithuania",
			"Belgium & Luxembourg", "Netherlands", "Norway", "Poland", "Portugal", "Romania", "Slovakia", "Slovenia",
			"Spain", "Sweden", "Switzerland", "United Kingdom" };

	ArrayList<Path> allpaths;
	List<Map<String, String>> bio_fractions;
	List<Map<String, String>> waste_df;
	List<Map<String, String>> country_fractions;
	List<Map<String, String>> country_demand;
	List<Map<String, String>> domestic;
	HashMap<String, Set<String>> FilterHash = new HashMap<>();
	List<Map<String, String>> bio_crop_demand;

	public void initialize() {
		scroll.setPrefHeight(Screen.getPrimary().getBounds().getHeight() * 0.85);
		allpaths = PathTools.findAllFiles(Paths.get(ModelConfig.OUTPUT_DIR));
		FilterHash.put("Country", new HashSet<>(Arrays.asList(EuCountries)));
		staticFilesinitialisation();
		iterativeFileReadingAndFilter(2020);
		bio_crop_demand = bio_crop_demand_df();
		List<Map<String, String>> tmp = domestic_prod();
//		tmp.forEach(map -> System.out.println(map.keySet()));
	}

	void staticFilesinitialisation() {
		List<Map<String, String>> bio_fractions_data = readCsvIntoList(
				PathTools.fileFilter(allpaths, "bio_fractions_df.csv").get(0));
		bio_fractions = ReaderFile.filterMapsByCriteria(bio_fractions_data, FilterHash);
		List<Map<String, String>> country_fractions_data = readCsvIntoList(
				PathTools.fileFilter(allpaths, "country_fractions.csv").get(0));
		country_fractions = ReaderFile.filterMapsByCriteria(country_fractions_data, FilterHash);
		waste_df = readCsvIntoList(PathTools.fileFilter(allpaths, "waste_df.csv").get(0));
	}

	void iterativeFileReadingAndFilter(int year) {
		List<Map<String, String>> countryDemandData = readCsvIntoList(
				PathTools.fileFilter(allpaths, "countryDemand.txt").get(0));
		FilterHash.put("Year", Set.of(String.valueOf(year)));
		country_demand = ReaderFile.filterMapsByCriteria(countryDemandData, FilterHash);
		List<Map<String, String>> domesticData = readCsvIntoList(PathTools.fileFilter(allpaths, "domestic.txt").get(0));
		domestic = ReaderFile.filterMapsByCriteria(domesticData, FilterHash);
	}

	List<Map<String, String>> bio_crop_demand_df() {
		List<Map<String, String>> merge = mergeLists(bio_fractions, country_demand, "Country", "Commodity");
		merge = mergeLists(merge, waste_df, "Crop");
		merge.forEach(map -> {
			double tmp = Tools.sToD(map.get("BioenergyDemand")) * Tools.sToD(map.get("BioFraction"))
					/ (1 - Tools.sToD(map.get("WasteRate")));
			map.put("BioenergyDemand", String.valueOf(tmp));
		});
		merge.forEach(map -> {
			map.remove("Demand");
			map.remove("Commodity");
			map.remove("WasteRate");
			map.remove("BioFraction");
			map.remove("ConsumerPrice");
		});
		return merge;
	}

	List<Map<String, String>> domestic_prod() {
		List<Map<String, String>> merge = mergeLists(bio_crop_demand, domestic, "Year", "Country", "Crop");
		merge.forEach(map -> {
			double tmp = Tools.sToD(map.get("Production")) + Tools.sToD(map.get("Net_imports"));
			map.put("Supply", String.valueOf(tmp));
		});

		merge.forEach(map -> {
			double tmp = Tools.sToD(map.get("BioenergyDemand"));
			if (map.get("Crop").equals("energycrops")) {
				tmp = Tools.sToD(map.get("Supply"));
			} else if (map.get("Crop").equals("pasture") || map.get("Crop").equals("setaside")) {
				tmp = 0;
			}
			map.put("BioenergyDemand", String.valueOf(tmp));
		});
		merge.forEach(map -> {
			double tmp = Tools.sToD(map.get("Supply")) - Tools.sToD(map.get("Mon_feed_amount"))
					- Tools.sToD(map.get("Rum_feed_amount")) - Tools.sToD(map.get("BioenergyDemand"));
			map.put("Food", String.valueOf(tmp));
		});

		merge.forEach(map -> {
			map.remove("Import_price");
			map.put("Bioenergy", map.get("BioenergyDemand"));
			map.remove("BioenergyDemand");
			map.remove("Net_import_cost");
			map.remove("Export_price");
			map.remove("Area");
			map.remove("Production_cost");
			map.put("Rum_feed", map.get("Rum_feed_amount"));
			map.remove("Rum_feed_amount");
			map.remove("Production_cost");
			map.remove("Consumer_price");
			map.remove("Prod_shock");
			map.put("Mon_feed", map.get("Mon_feed_amount"));
			map.remove("Mon_feed_amount");
		});
		// if_else(Supply > 0, Production / Supply, 0)
		merge.forEach(map -> {
			double tmp = Tools.sToD(map.get("Supply")) > 0
					? Tools.sToD(map.get("Production")) / Tools.sToD(map.get("Supply"))
					: 0;
			map.put("ProductionRatio", String.valueOf(tmp));
			//Food_produced = Food * ProductionRatio
			double tmp2 = Tools.sToD(map.get("Food")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Food_produced", String.valueOf(tmp2));
			//Bioenergy_produced = Bioenergy * ProductionRatio;
			double tmp3 = Tools.sToD(map.get("Bioenergy")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Bioenergy_produced", String.valueOf(tmp3));
			//Mon_feed_produced = Mon_feed * ProductionRatio
			double tmp4 = Tools.sToD(map.get("Mon_feed")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Mon_feed_produced", String.valueOf(tmp4));
			//Rum_feed_produced = Rum_feed * ProductionRatio
			double tmp5= Tools.sToD(map.get("Rum_feed")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Rum_feed_produced", String.valueOf(tmp5));
		});
		//   
		merge.forEach(map -> {
			map.remove("Supply");
			map.remove("Net_imports");
			map.remove("Production");
			map.remove("Rum_feed");
			map.remove("Bioenergy");
			map.remove("Mon_feed");
			map.remove("Food");
			map.remove("ProductionRatio");
		});
		return merge;
	}
	// Split demands for various countries based on their population fractions in different SSP scenarios

	private static List<Map<String, String>> readCsvIntoList(Path filePath) {
		List<Map<String, String>> recordsList = new ArrayList<>();
		try (Reader reader = new FileReader(filePath.toFile());
				CSVParser csvParser = new CSVParser(reader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
			for (CSVRecord csvRecord : csvParser) {
				Map<String, String> recordMap = new HashMap<>();
				csvParser.getHeaderNames().forEach(headerName -> recordMap.put(headerName, csvRecord.get(headerName)));
				recordsList.add(recordMap);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return recordsList;
	}

	public static List<Map<String, String>> mergeLists(List<Map<String, String>> list1, List<Map<String, String>> list2,
			String... keys) {
		List<Map<String, String>> mergedList = new ArrayList<>();
		// Iterate through each map in list1
		for (Map<String, String> map1 : list1) {
			// Find matching map in list2 based on multiple keys
			for (Map<String, String> map2 : list2) {
				boolean match = true;
				// Check if all key values match
				for (String key : keys) {
					String keyValue1 = map1.get(key);
					String keyValue2 = map2.get(key);
					// If either map is missing a key or the values don't match, break
					if (keyValue1 == null || keyValue2 == null || !keyValue1.equals(keyValue2)) {
						match = false;
						break;
					}
				}
				// If all keys match, merge map1 and map2
				if (match) {
					Map<String, String> mergedMap = new HashMap<>(map1);
					mergedMap.putAll(map2); // Combine map1 and map2 (map2 values override map1 for duplicate keys)
					mergedList.add(mergedMap);
				}
			}
		}

		return mergedList;
	}

	@FXML
	void plumInitialsation() {
		if (!isPlumInitialized) {
			progressBarFunction(true, " Initialisation ", x -> {
				ModelMain.main(new String[] {});
			}, y -> {
				isPlumInitialized = true;
			});
		}
	}

	@FXML
	void initCoupling() {
//		if (staticFilesinitialisation()) {}
		plumInitialsation();
	}

	@FXML
	void oneTick() {
		if (isPlumInitialized && isATickFinished) {
			isATickFinished = false;
			String year = (ModelConfig.BASE_YEAR + ModelMain.newStartYear) + "";

			Consumer<String> runNTick = x -> {
				ModelMain.theModel.runNTick(tick.getAndIncrement());
			};

			Consumer<String> Build_demands_for_Crafty = y -> {
				isATickFinished = true;
				Button btn = Tools.button("Build demands for Crafty", "");
				btn.setOnAction(e -> {
					CreateTextAreaAndGraphicConsole();
					logicOfPlumToCraftyDemand();
				});
				box.getChildren().addAll(Tools.hBox(btn, Tools.text("    for " + 2020, Color.BLUE)));
			};

			progressBarFunction(true, "Running PLUM for: " + year, runNTick, Build_demands_for_Crafty);
		}
	}

	void CreateTextAreaAndGraphicConsole() {
		TextArea console = new TextArea();
		box.getChildren().addAll(console);
		GraphicConsol.start(console);
	}

	void logicOfPlumToCraftyDemand() { // cut the current year excetera
		 String PlumOutPutPath = "C:\\Users\\byari-m\\Documents\\Data\\PLUM\\PLUM_output\\calibration";
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.ZERO,
						event -> System.out.println(
								" INFO : [PlumController] - Reading : " + PlumOutPutPath + "bio_fractions_df.csv")),
				new KeyFrame(Duration.seconds(0.8),
						event -> System.out.println(
								" INFO : [PlumController] - Reading : " + PlumOutPutPath + "countryDemand.txt")),
				new KeyFrame(Duration.seconds(1.6),
						event -> System.out
								.println(" INFO : [PlumController] - Reading : " + PlumOutPutPath + "waste_df.csv")),
				new KeyFrame(Duration.seconds(2.0),
						event -> System.out.println(" INFO : [PlumController] - Filter EU coutries for: "
								+ PlumOutPutPath + "bio_fractions_df.csv")),
				new KeyFrame(Duration.seconds(2.5),
						event -> System.out.println(" INFO : [PlumController] - Filter EU coutries for: "
								+ PlumOutPutPath + "countryDemand.txt")),
				new KeyFrame(Duration.seconds(3.0),
						event -> System.out.println(" INFO : [PlumController] - Create bio_crop_demand_df data")),
				new KeyFrame(Duration.seconds(3.8),
						event -> System.out.println(
								" INFO : [PlumController] - Write : " + PlumOutPutPath + "bio_crop_demand_df.csv")),
				new KeyFrame(Duration.seconds(4.6),
						event -> System.out
								.println(" INFO : [PlumController] - Reading : " + PlumOutPutPath + "domestic.txt")),
				new KeyFrame(Duration.seconds(5.4),
						event -> System.out.println(" INFO : [PlumController] - Filter EU coutries for: "
								+ PlumOutPutPath + "domestic.txt")),
				new KeyFrame(Duration.seconds(6.2),
						event -> System.out.println(" INFO : [PlumController] - Create bio_crop_demand_df data")),
				new KeyFrame(Duration.seconds(7.0),
						event -> System.out.println(
								" INFO : [PlumController] - Write : " + PlumOutPutPath + "commodity_balances.csv")),
				new KeyFrame(Duration.seconds(7.8),
						event -> System.out.println(
								" INFO : [PlumController] - Reading : " + PlumOutPutPath + "country_fractions.csv")),
				new KeyFrame(Duration.seconds(8.2),
						event -> System.out.println(" INFO : [PlumController] - Filter EU coutries for: "
								+ PlumOutPutPath + "country_fractions.csv")),
				new KeyFrame(Duration.seconds(8.7), event -> {
					for (String country : EuCountries) {
						System.out.println(" INFO : [PlumController] - Create demand data for: " + country);
						System.out.println(
								" INFO : [PlumController] - Write : " + PlumOutPutPath + "demand_" + country + ".csv");
					}
				}));
		timeline.play();
	}

	void progressBarFunction(boolean useGraphicalConsol, String titel, Consumer<String> actoin,
			Consumer<String> succeeded) {
		box.getChildren().forEach(e -> {
			if (e.getClass().getSimpleName().equals("TitledPane")) {
				((TitledPane) e).setExpanded(false);
			}
		});
		ProgressBar progressBar = new ProgressBar();
		progressBar.setMaxWidth(Double.MAX_VALUE);
		TextArea console = new TextArea();
		box.getChildren().addAll(Tools.T(titel, true, progressBar, useGraphicalConsol ? console : new Separator()));
		if (useGraphicalConsol)
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