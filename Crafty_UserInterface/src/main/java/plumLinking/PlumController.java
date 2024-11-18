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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


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
import utils.analysis.CustomLogger;
import utils.filesTools.PathTools;
import utils.graphicalTools.GraphicConsol;
import utils.graphicalTools.Tools;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import ac.ed.lurg.ModelConfig;
import ac.ed.lurg.ModelMain;

public class PlumController {
	@FXML
	private VBox box;
	@FXML
	private ScrollPane scroll;
	private static final CustomLogger LOGGER = new CustomLogger(PlumController.class);

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
	List<Map<String, String>> country_demand;
	List<Map<String, String>> domestic;
	HashMap<String, Set<String>> FilterHash = new HashMap<>();
	List<Map<String, String>> bio_crop_demand;

	public void initialize() {
		// scroll.setPrefHeight(Screen.getPrimary().getBounds().getHeight() * 0.85);
		// System.out.println("|| "+ModelConfig.OUTPUT_DIR);
		allpaths = PathTools.findAllFiles(Paths.get(ModelConfig.OUTPUT_DIR));
		FilterHash.put("Country", new HashSet<>(Arrays.asList(EuCountries)));
		staticFilesinitialisation();
		iterativeFileReadingAndFilter(2020);
//		bio_crop_demand = bio_crop_demand_df();
//		List<Map<String, String>> domistic = domestic_prod();
//		domistic.forEach(map -> System.out.println(map));
//		 domistic.forEach(map -> System.out.println(map));
		// Tranfer each crop as a colmun
		// # Split demands for various countries based on their population fractions in
		// different SSP scenarios
//		aggregate_Demands(domistic);
	}

	void aggregate_Demands(List<Map<String, String>> domistic) {
		// split by countries
		Map<String, List<Map<String, String>>> countriesDemands = new HashMap<>();

		for (int i = 0; i < EuCountries.length; i++) {
			List<Map<String, String>> d = new ArrayList<>();
			int j = i;
			domistic.forEach(line -> {
				if (line.get("Country").equals(EuCountries[j])) {
					d.add(line);
				}
			});
			countriesDemands.put(EuCountries[i], d);
		}

		Map<String, Map<String, Double>> finalCountriesDemands = new HashMap<>();
		countriesDemands.forEach((country, list) -> {
			Map<String, Double> map = new HashMap<>();
			for (Map<String, String> line : list) {
				double Fodder_crops = getCrop(line, "wheat", "Rum_feed_produced")
						+ getCrop(line, "wheat", "Mon_feed_produced") + getCrop(line, "maize", "Rum_feed_produced")
						+ getCrop(line, "maize", "Mon_feed_produced") + getCrop(line, "rice", "Rum_feed_produced")
						+ getCrop(line, "rice", "Mon_feed_produced")
						+ getCrop(line, "oilcropsNFix", "Rum_feed_produced")
						+ getCrop(line, "oilcropsNFix", "Mon_feed_produced");
				map.merge("Fodder_crops", Fodder_crops, Double::sum);
				double Bioenergy1G = getCrop(line, "wheat", "Bioenergy_produced")
						+ getCrop(line, "maize", "Bioenergy_produced") + getCrop(line, "rice", "Bioenergy_produced")
						+ getCrop(line, "oilcropsNFix", "Bioenergy_produced");
				map.merge("Bioenergy1G", Bioenergy1G, Double::sum);
				map.merge("Pasture_fodder", getCrop(line, "pasture", "Rum_feed_produced"), Double::sum);
				map.merge("C4cereals", getCrop(line, "maize", "Food_produced"), Double::sum);
				map.merge("C3rice", getCrop(line, "rice", "Food_produced"), Double::sum);//
				map.merge("C3oilNFix", getCrop(line, "oilcropsNFix", "Food_produced"), Double::sum);//
				map.merge("C3oilcropsOther", getCrop(line, "oilcropsOther", "Food_produced"), Double::sum);
				map.merge("C3starchyroots", getCrop(line, "starchyRoots", "Food_produced"), Double::sum);
				map.merge("C3cereals", getCrop(line, "wheat", "Food_produced"), Double::sum);
				map.merge("C3fruitveg", getCrop(line, "fruitveg", "Food_produced"), Double::sum);
				map.merge("Bioenergy2G", getCrop(line, "energycrops", "Bioenergy_produced"), Double::sum);
			}
			finalCountriesDemands.put(country, map);
		});
//		finalCountriesDemands.forEach((country, demands) -> {
//			System.out.println(country + ":" + demands.get("C3fruitveg"));
//		});
	}

	double getCrop(Map<String, String> map, String val1, String key2) {
		return map.get("Crop").equals(val1) ? Tools.sToD(map.get(key2)) : 0;

	}

	void staticFilesinitialisation() {
		bio_fractions = filterMapsByCriteria(
				readCsvIntoList(PathTools.fileFilter(allpaths, "bio_fractions_df.csv").get(0)), FilterHash);
		waste_df = readCsvIntoList(PathTools.fileFilter(allpaths, "waste_df.csv").get(0));
	}

	void iterativeFileReadingAndFilter(int year) {
		FilterHash.put("Year", Set.of(String.valueOf(year)));
		country_demand = filterMapsByCriteria(
				readCsvIntoList(PathTools.fileFilter(allpaths, "countryDemand.txt").get(0)), FilterHash);
		domestic = filterMapsByCriteria(
				readCsvIntoList(PathTools.fileFilter(allpaths, "domestic.txt").get(0)), FilterHash);
	}

	List<Map<String, String>> bio_crop_demand_df() {
		List<Map<String, String>> merge = left_join_many_to_many(country_demand, bio_fractions, "Country", "Commodity");
		merge = left_join(merge, waste_df, "Crop");
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
		List<Map<String, String>> left_join = left_join(domestic, bio_crop_demand, "Year", "Country", "Crop");

		left_join.forEach(map -> {
			double tmp = Tools.sToD(map.get("Production")) + Tools.sToD(map.get("Net_imports"));
			map.put("Supply", String.valueOf(tmp));
		});

		left_join.forEach(map -> {
			double tmp = Tools.sToD(map.get("BioenergyDemand"));
			if (map.get("Crop").equals("energycrops")) {
				tmp = Tools.sToD(map.get("Supply"));
			} else if (map.get("Crop").equals("pasture") || map.get("Crop").equals("setaside")) {
				tmp = 0;
			}
			map.put("BioenergyDemand", String.valueOf(tmp));
		});
		left_join.forEach(map -> {
			double tmp = Tools.sToD(map.get("Supply")) - Tools.sToD(map.get("Mon_feed_amount"))
					- Tools.sToD(map.get("Rum_feed_amount")) - Tools.sToD(map.get("BioenergyDemand"));
			map.put("Food", String.valueOf(tmp));
		});

		left_join.forEach(map -> {
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
		left_join.forEach(map -> {
			double tmp = Tools.sToD(map.get("Supply")) > 0
					? Tools.sToD(map.get("Production")) / Tools.sToD(map.get("Supply"))
					: 0;
			map.put("ProductionRatio", String.valueOf(tmp));
			// Food_produced = Food * ProductionRatio
			double tmp2 = Tools.sToD(map.get("Food")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Food_produced", String.valueOf(tmp2));
			// Bioenergy_produced = Bioenergy * ProductionRatio;
			double tmp3 = Tools.sToD(map.get("Bioenergy")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Bioenergy_produced", String.valueOf(tmp3));
			// Mon_feed_produced = Mon_feed * ProductionRatio
			double tmp4 = Tools.sToD(map.get("Mon_feed")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Mon_feed_produced", String.valueOf(tmp4));
			// Rum_feed_produced = Rum_feed * ProductionRatio
			double tmp5 = Tools.sToD(map.get("Rum_feed")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Rum_feed_produced", String.valueOf(tmp5));
		});
		//
		left_join.forEach(map -> {
			map.remove("Supply");
			map.remove("Net_imports");
			map.remove("Production");
			map.remove("Rum_feed");
			map.remove("Bioenergy");
			map.remove("Mon_feed");
			map.remove("Food");
			map.remove("ProductionRatio");
		});
		return left_join;
	}
	// Split demands for various countries based on their population fractions in
	// different SSP scenarios

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


	public static List<Map<String, String>> left_join(List<Map<String, String>> csv1, List<Map<String, String>> csv2,
			String... keys) {
		List<Map<String, String>> result = new ArrayList<>();

		// Iterate through each row in csv1
		for (Map<String, String> row1 : csv1) {
			boolean matchFound = false;

			// Iterate through each row in csv2 to find a match based on keys
			for (Map<String, String> row2 : csv2) {
				boolean isMatch = true;

				// Check if all keys match between the two rows
				for (String key : keys) {
					if (!Objects.equals(row1.get(key), row2.get(key))) {
						isMatch = false;
						break;
					}
				}

				if (isMatch) {
					matchFound = true;
					// Create a new row by merging row1 and row2
					Map<String, String> mergedRow = new HashMap<>(row1);
					for (Map.Entry<String, String> entry : row2.entrySet()) {
						// Avoid overwriting existing keys from row1
						if (!mergedRow.containsKey(entry.getKey()) || !Arrays.asList(keys).contains(entry.getKey())) {
							mergedRow.put(entry.getKey(), entry.getValue());
						}
					}
					result.add(mergedRow);
				}
			}

			// If no match was found, add row1 as it is (with null values for csv2's
			// columns)
			if (!matchFound) {
				result.add(new HashMap<>(row1));
			}
		}

		return result;
	}
	
	public static List<Map<String, String>> left_join_many_to_many(List<Map<String, String>> csv1, List<Map<String, String>> csv2, String... keys) {
        List<Map<String, String>> result = new ArrayList<>();
        
        // Iterate through each row in csv1
        for (Map<String, String> row1 : csv1) {
            boolean matchFound = false;
            
            // Iterate through each row in csv2 to find all matches based on keys
            for (Map<String, String> row2 : csv2) {
                boolean isMatch = true;
                
                // Check if all key values match between row1 and row2
                for (String key : keys) {
                    if (!Objects.equals(row1.get(key), row2.get(key))) {
                        isMatch = false;
                        break;
                    }
                }
                
                // If there is a match, merge the two rows
                if (isMatch) {
                    matchFound = true;
                    Map<String, String> mergedRow = new HashMap<>(row1);
                    for (Map.Entry<String, String> entry : row2.entrySet()) {
                        // Avoid overwriting existing keys from row1 unless they are not join keys
                        if (!mergedRow.containsKey(entry.getKey()) || !Arrays.asList(keys).contains(entry.getKey())) {
                            mergedRow.put(entry.getKey(), entry.getValue());
                        }
                    }
                    result.add(mergedRow);  // Add merged row to result
                }
            }
            
            // If no match is found, add row1 to result with nulls for missing csv2 columns
            if (!matchFound) {
                Map<String, String> noMatchRow = new HashMap<>(row1);
                
                // Add keys from csv2 to ensure all columns are present in the output
                for (Map<String, String> row2 : csv2) {
                    for (String key : row2.keySet()) {
                        if (!noMatchRow.containsKey(key)) {
                            noMatchRow.put(key, null);
                        }
                    }
                }
                
                result.add(noMatchRow);
            }
        }
        result.forEach(map -> System.out.println(map));
        return result;
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
	

	public static List<Map<String, String>> filterMapsByCriteria(List<Map<String, String>> listMaps,
			HashMap<String, Set<String>> hash) {
		List<Map<String, String>> returnlistMaps = new ArrayList<>();
		listMaps.forEach(map -> {
			boolean tmp1 = true;
			for (String key : hash.keySet()) {
				boolean tmp0 = false;
				for (String v : hash.get(key)) {
					if (map.get(key).equals(v)) {
						tmp0 = true;
						break;
					}
				}
				tmp1 = tmp0;
				if (!tmp1) {
					break;
				}
			}
			if (tmp1)
				returnlistMaps.add(map);
		});
		return returnlistMaps;
	}

}