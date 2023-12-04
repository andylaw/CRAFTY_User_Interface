package controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import UtilitiesFx.graphicalTools.Histogram;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.PieChartTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import main.OpenTabs;
import model.CellsSet;

/**
 * @author Mohamed Byari
 *
 */

public class Map_Controller {

	PieChart pieChartColorAFts;

	CellsLoader M;
	public static Tab tab;
	public static VBox vbox;
	public static Pane graphDemand;
	public static RadioButton[] radioColor;
	public static BarChart<String, Number> histogrameCapitalFequency;

	public Map_Controller(CellsLoader M) {
		this.M = M;
	}

	public Tab colorWorld() {
		vbox = new VBox();
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		histogrameCapitalFequency = new BarChart<String, Number>(xAxis, yAxis);
		histogrameCapitalFequency.setTitle("Frequency of appearance of capital values (%)");
		int length = CellsSet.getCapitalsName().size() + 1;
		radioColor = new RadioButton[length + CellsLoader.GISNames.size()];
		VBox colorBox = new VBox();
		for (int i = 0; i < CellsSet.getCapitalsName().size(); i++) {
			radioColor[i] = new RadioButton(CellsSet.getCapitalsName().get(i));
			colorBox.getChildren().add(radioColor[i]);

			int k = i;
			radioColor[k].setOnAction(e -> {
				for (int j = 0; j < CellsSet.getCapitalsName().size() + CellsLoader.GISNames.size() + 1; j++) {
					if (j != k) {
						if (radioColor[j] != null) {
							radioColor[j].setSelected(false);
							OpenTabs.choiceScenario.setDisable(false);
							OpenTabs.year.setDisable(false);
						}
					}
				}
				histogrameCapitalFequency.getData().clear();
				if (!Paths.getScenario().equalsIgnoreCase("Baseline")) {
					histogrameCapitals(histogrameCapitalFequency, Paths.getCurrentYear() + "",
							CellsSet.getCapitalsName().get(k));
				}
				CellsSet.colorMap(CellsSet.getCapitalsName().get(k));
			});
		}
		radioColor[CellsSet.getCapitalsName().size()] = new RadioButton("AFTs Distribution");
		radioColor[CellsSet.getCapitalsName().size()].setSelected(true);
		colorBox.getChildren().add(radioColor[CellsSet.getCapitalsName().size()]);
		radioColor[CellsSet.getCapitalsName().size()].setOnAction(e -> {
			for (int j = 0; j < CellsSet.getCapitalsName().size() + CellsLoader.GISNames.size() + 1; j++) {
				if (j != CellsSet.getCapitalsName().size()) {
					if (radioColor[j] != null) {
						radioColor[j].setSelected(false);
						OpenTabs.choiceScenario.setDisable(true);
						OpenTabs.year.setDisable(true);
					}
				}
			}
			histogrameCapitalFequency.getData().clear();
			CellsSet.colorMap("FR");
		});

		for (int i = 0; i < CellsLoader.GISNames.size(); i++) {
			if (CellsLoader.GISNames.get(i).equalsIgnoreCase("lad19nm")
					|| CellsLoader.GISNames.get(i).equalsIgnoreCase("nuts318nm")
					|| CellsLoader.GISNames.get(i).equalsIgnoreCase("regions")) {
				radioColor[CellsSet.getCapitalsName().size() + 1 + i] = new RadioButton(CellsLoader.GISNames.get(i));
				int k = i + CellsSet.getCapitalsName().size() + 1;
				radioColor[k].setOnAction(e -> {
					for (int j = 0; j < CellsSet.getCapitalsName().size() + 1 + CellsLoader.GISNames.size(); j++) {
						if (k != j) {
							if (radioColor[j] != null) {
								radioColor[j].setSelected(false);
							}
						}
					}
					histogrameCapitalFequency.getData().clear();
					CellsSet.colorMap(CellsLoader.GISNames.get(k - CellsSet.getCapitalsName().size() - 1));
					CellsSet.setRegioneselected(CellsLoader.GISNames.get(k - CellsSet.getCapitalsName().size() - 1));
				});
				colorBox.getChildren().add(radioColor[k]);
			}
		}
		graphDemand = graphDemand();

		pieChartColorAFts = new PieChart();
		updatePieChartColorAFts(M, pieChartColorAFts);
		vbox.getChildren().addAll(new Separator(),
				Tools.T("Visualize spatial data", true, Tools.hBox(colorBox, histogrameCapitalFequency)),
				Tools.T("Agents Distribution", true,pieChartColorAFts), Tools.T("Demand", true,graphDemand));
		tab = new Tab("Spatial data", vbox);
		tab.setOnSelectionChanged(e -> {
			radioColor[0].fire();
	});
		return tab;
	}

	public static void histogrameCapitals(BarChart<String, Number> histograme, String year, String capitalName) {
		Set<Double> dset = new HashSet<>();
		CellsSet.getCellsSet().forEach(c -> {
			dset.add(c.getCapitals().get(capitalName));
		});

		Histogram.histo(vbox, capitalName + "_" + year + "_" + Paths.getScenario(), histograme, dset);
	}

	void updatePieChartColorAFts(CellsLoader M, PieChart chart) {
		HashMap<String, Double> hashAgentNbr = AFTsLoader.hashAgentNbr();
		HashMap<String, Color> color = new HashMap<>();
		M.AFtsSet.getAftHash().forEach((name, a) -> {
			color.put(name, a.getColor());
		});

		new PieChartTools().updateChart(M, hashAgentNbr, color, chart);
		chart.setLegendSide(Side.LEFT);
		// * add menu to PiChart*//
		HashMap<String, Consumer<String>> newItemMenu = new HashMap<>();
		Consumer<String> reset = x -> {
			M.AFtsSet.agentsColorinitialisation();
			M.AFtsSet.forEach(( a) -> {
				color.put(a.getLabel(), a.getColor());
			});
			new PieChartTools().updateChart(M, hashAgentNbr, color, chart);
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
		MousePressed.mouseControle(vbox, chart, hashm);
	}

	public Pane graphDemand() {
		LineChart<Number, Number> Ch = new LineChart<>(new NumberAxis(Paths.getStartYear(), Paths.getEndtYear(), 5), new NumberAxis());
		new LineChartTools().lineChart(M,vbox, Ch, CellsSet.getDemand());
		return new VBox(Ch);

	}

}
