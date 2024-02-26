package fxmlControllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.PieChartTools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.CellsSet;

public class SpatialDataController {

	public static RadioButton[] radioColor;
	@FXML
	private VBox vboxForSliderColors;
	@FXML
	private BarChart<String, Number> histogramCapitals;
	@FXML
	private PieChart pieChartColor;
	@FXML
	private LineChart<Number, Number> demandsChart;

	CellsLoader M;
	private boolean isNotInitialsation = false;

	private static SpatialDataController instance;

	public SpatialDataController() {
		instance = this;
	}

	public static SpatialDataController getInstance() {
		return instance;
	}

	public LineChart<Number, Number> getDemandsChart() {
		return demandsChart;
	}

	public BarChart<String, Number> getHistogramCapitals() {
		return histogramCapitals;
	}

	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		M = TabPaneController.M;
		TabPaneController.M.loadCapitalsAndServiceList();
		TabPaneController.M.loadMap();
		TabPaneController.M.loadGisData();
		CellsSet.setCellsSet(TabPaneController.M);
		CellsSet.plotCells();
		new LineChartTools().lineChart(M, (Pane) demandsChart.getParent(), demandsChart, CellsSet.getDemand());
		updatePieChartColorAFts(pieChartColor);
		mapColorAndCapitalHistogrameInitialisation();
		radioColor[0].fire();
		isNotInitialsation = true;

	}

	private void mapColorAndCapitalHistogrameInitialisation() {

		radioColor = new RadioButton[CellsSet.getCapitalsName().size() + 1];
		for (int i = 0; i < CellsSet.getCapitalsName().size(); i++) {
			radioColor[i] = new RadioButton(CellsSet.getCapitalsName().get(i));
			vboxForSliderColors.getChildren().add(radioColor[i]);

			int k = i;
			radioColor[k].setOnAction(e -> {
				for (int j = 0; j < CellsSet.getCapitalsName().size() + 1; j++) {
					if (j != k) {
						if (radioColor[j] != null) {
							radioColor[j].setSelected(false);
//							OpenTabs.choiceScenario.setDisable(false);
//							OpenTabs.year.setDisable(false);

						}
					}
				}
				if (isNotInitialsation) {
					histogramCapitals.getData().clear();
					if (!Paths.getScenario().equalsIgnoreCase("Baseline")) {
						histogrameCapitals(Paths.getCurrentYear() + "", CellsSet.getCapitalsName().get(k));
					}
					CellsSet.colorMap(CellsSet.getCapitalsName().get(k));
				}
			});
		}
		radioColor[CellsSet.getCapitalsName().size()] = new RadioButton("AFTs Distribution");
		radioColor[CellsSet.getCapitalsName().size()].setSelected(true);
		vboxForSliderColors.getChildren().add(radioColor[CellsSet.getCapitalsName().size()]);
		radioColor[CellsSet.getCapitalsName().size()].setOnAction(e -> {
			for (int j = 0; j < CellsSet.getCapitalsName().size() + 1; j++) {
				if (j != CellsSet.getCapitalsName().size()) {
					if (radioColor[j] != null) {
						radioColor[j].setSelected(false);
					}
				}
			}
			histogramCapitals.getData().clear();
			CellsSet.colorMap("FR");
		});
	}

	void histogrameCapitals(String year, String capitalName) {

		Set<Double> dset = new HashSet<>();
		CellsSet.getCellsSet().forEach(c -> {
			dset.add(c.getCapitals().get(capitalName));
		});

		XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
		List<Integer> numbersInInterval = countNumbersInIntervals(dset, 100);

		dataSeries.setName(capitalName + "_" + year + "_" + Paths.getScenario());
		for (int i = 0; i < numbersInInterval.size(); i++) {
			Integer v = numbersInInterval.get(i);
			dataSeries.getData().add(new XYChart.Data<>((i) + "", v));
		}
		histogramCapitals.getData().add(dataSeries);
		String ItemName = "Clear Histogram";
		Consumer<String> action = x -> {
			histogramCapitals.getData().clear();
		};
		HashMap<String, Consumer<String>> othersMenuItems = new HashMap<>();
		othersMenuItems.put(ItemName, action);
		MousePressed.mouseControle((Pane) histogramCapitals.getParent(), histogramCapitals, othersMenuItems);
	}

	public static List<Integer> countNumbersInIntervals(Set<Double> numbers, int intervalNBR) {
		int[] counts = new int[intervalNBR];

		for (Double number : numbers) {
			if (number >= 0.0 && number <= 1.0) {
				int index = (int) (number * intervalNBR);
				// Handle the edge case where a number is exactly 1.0
				if (index == intervalNBR) {
					index = intervalNBR - 1;
				}
				counts[index]++;
			}
		}

		List<Integer> result = new ArrayList<>();
		for (int count : counts) {
			result.add(count);
		}
		return result;
	}

	private void updatePieChartColorAFts(PieChart chart) {
		HashMap<String, Double> convertedMap = new HashMap<>(AFTsLoader.hashAgentNbr().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().doubleValue())));
		HashMap<String, Color> color = new HashMap<>();
		M.AFtsSet.getAftHash().forEach((name, a) -> {
			color.put(name, a.getColor());
		});

		new PieChartTools().updateChart(M, convertedMap, color, chart);
		chart.setLegendSide(Side.LEFT);
		// * add menu to PiChart*//
		HashMap<String, Consumer<String>> newItemMenu = new HashMap<>();
		Consumer<String> reset = x -> {
			M.AFtsSet.agentsColorinitialisation();
			M.AFtsSet.forEach((a) -> {
				color.put(a.getLabel(), a.getColor());
			});
			new PieChartTools().updateChart(M, convertedMap, color, chart);
			CellsSet.colorMap("FR");
		};

		Consumer<String> saveInPutData = x -> {
			M.AFtsSet.updateColorsInputData();
		};

		newItemMenu.put("Reset Colors", reset);
		newItemMenu.put("Save new Colors to Input Data", saveInPutData);

		chart.setOnMouseDragged(event -> {
			chart.setPrefHeight(event.getY());
		});
		HashMap<String, Consumer<String>> hashm = new HashMap<>();

		newItemMenu.forEach((name, action) -> {
			hashm.put(name, action);
		});
		MousePressed.mouseControle((Pane) chart.getParent(), chart, hashm);
	}

}
