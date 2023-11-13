package panes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import UtilitiesFx.graphicalTools.Histogram;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.PieChartTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.Agents;
import dataLoader.MapLoader;
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
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import main.OpenTabs;
import model.Lattice;

public class DataDisplay {

	PieChart pieChartColorAFts;

	MapLoader M;
	public static Tab tab;
	public static VBox vbox = new VBox();
	public static Pane graphDemand;
	public static RadioButton[] radioColor;
	public static BarChart<String, Number> histogrameCapitalFequency;

	public DataDisplay(MapLoader M) {
		this.M = M;
	}

	public Tab colorWorld() {
		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		histogrameCapitalFequency = new BarChart<String, Number>(xAxis, yAxis);
		histogrameCapitalFequency.setTitle("Frequency of appearance of capital values (%)");
		int length = Lattice.getCapitalsName().size() + 1;
		radioColor = new RadioButton[length + MapLoader.GISNames.size()];
		VBox colorBox = new VBox();
		for (int i = 0; i < Lattice.getCapitalsName().size(); i++) {
			radioColor[i] = new RadioButton(Lattice.getCapitalsName().get(i));
			colorBox.getChildren().add(radioColor[i]);

			int k = i;
			radioColor[k].setOnAction(e -> {
				for (int j = 0; j < Lattice.getCapitalsName().size() + MapLoader.GISNames.size() + 1; j++) {
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
							Lattice.getCapitalsName().get(k));
				}
				Lattice.colorMap(Lattice.getCapitalsName().get(k));
			});
		}
		radioColor[Lattice.getCapitalsName().size()] = new RadioButton("AFTs Distribution");
		radioColor[Lattice.getCapitalsName().size()].setSelected(true);
		colorBox.getChildren().add(radioColor[Lattice.getCapitalsName().size()]);
		radioColor[Lattice.getCapitalsName().size()].setOnAction(e -> {
			for (int j = 0; j < Lattice.getCapitalsName().size() + MapLoader.GISNames.size() + 1; j++) {
				if (j != Lattice.getCapitalsName().size()) {
					if (radioColor[j] != null) {
						radioColor[j].setSelected(false);
						OpenTabs.choiceScenario.setDisable(true);
						OpenTabs.year.setDisable(true);
					}
				}
			}
			histogrameCapitalFequency.getData().clear();
			Lattice.colorMap("FR");
		});

		for (int i = 0; i < MapLoader.GISNames.size(); i++) {
			if (MapLoader.GISNames.get(i).equalsIgnoreCase("lad19nm")
					|| MapLoader.GISNames.get(i).equalsIgnoreCase("nuts318nm")
					|| MapLoader.GISNames.get(i).equalsIgnoreCase("regions")) {
				radioColor[Lattice.getCapitalsName().size() + 1 + i] = new RadioButton(MapLoader.GISNames.get(i));
				int k = i + Lattice.getCapitalsName().size() + 1;
				radioColor[k].setOnAction(e -> {
					for (int j = 0; j < Lattice.getCapitalsName().size() + 1 + MapLoader.GISNames.size(); j++) {
						if (k != j) {
							if (radioColor[j] != null) {
								radioColor[j].setSelected(false);
							}
						}
					}
					histogrameCapitalFequency.getData().clear();
					Lattice.colorMap(MapLoader.GISNames.get(k - Lattice.getCapitalsName().size() - 1));
					Lattice.setRegioneselected(MapLoader.GISNames.get(k - Lattice.getCapitalsName().size() - 1));
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
		Lattice.getCellsSet().forEach(c -> {
			dset.add(c.getCapitals().get(capitalName));
		});

		Histogram.histo(vbox, capitalName + "_" + year + "_" + Paths.getScenario(), histograme, dset);
	}

	void updatePieChartColorAFts(MapLoader M, PieChart chart) {
		HashMap<String, Double> hashAgentNbr = Agents.hashAgentNbr();
		HashMap<String, Color> color = new HashMap<>();
		Agents.aftReSet.forEach((name, a) -> {
			color.put(name, a.getColor());
		});

		new PieChartTools().updateChart(M, hashAgentNbr, color, chart);
		chart.setLegendSide(Side.LEFT);
		// * add menu to PiChart*//
		HashMap<String, Consumer<String>> newItemMenu = new HashMap<>();
		Consumer<String> reset = x -> {
			Agents.agentsColorinitialisation();
			Agents.aftReSet.forEach((name, a) -> {
				color.put(name, a.getColor());
			});
			new PieChartTools().updateChart(M, hashAgentNbr, color, chart);
			Lattice.colorMap("FR");
		};

		Consumer<String> saveInPutData = x -> {
			Agents.updateColorsInputData();
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

	public static Pane graphDemand() {

		LineChart<Number, Number> Ch = new LineChart<>(new NumberAxis(), new NumberAxis());
		LineChartTools.lineChart(vbox, Ch, Lattice.getDemand());

		return new VBox(Ch);

	}

}
