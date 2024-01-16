package fxmlControllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import model.CellsSet;
import model.Manager;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

public class OutPuterController {
	 CellsLoader M;
	@FXML
	private HBox outputcolorBox;
	@FXML
	private Button selectoutPut;
	@FXML
	private ChoiceBox<String> yearChoice;
	@FXML
	private GridPane gridChart;
	@FXML
	private ScrollPane scroll;
	String outputpath = "";
	RadioButton[] radioColor;

	public void initialize() {
		System.out.println("initialize "+getClass().getSimpleName());
		M=TabPaneController.M;
		radioColor = new RadioButton[CellsSet.getServicesNames().size() + 1];

		ArrayList<String> yearList = new ArrayList<>();
		for (int i = Paths.getStartYear(); i < Paths.getEndtYear(); i++) {
			yearList.add(i + "");
		}

		yearChoice.getItems().addAll(yearList);
		yearChoice.setValue(Paths.getCurrentYear() + "");

		for (int i = 0; i < radioColor.length; i++) {
			if (i < CellsSet.getServicesNames().size()) {
				radioColor[i] = new RadioButton(CellsSet.getServicesNames().get(i));

			} else if (i == CellsSet.getServicesNames().size()) {
				radioColor[i] = new RadioButton("Agent");
			}

			int k = i;
			radioColor[i].setOnAction(e -> {
				for (int j = 0; j < radioColor.length; j++) {
					if (k != j) {
						radioColor[j].setSelected(false);
					}
				}
				if (k < CellsSet.getServicesNames().size()) {
					CellsSet.colorMap(CellsSet.getServicesNames().get(k));
				} else if (k == CellsSet.getServicesNames().size()) {
					CellsSet.colorMap("FR");
				}
			});
		}
		outputcolorBox.getChildren().addAll(radioColor);
		
		scroll.setPrefHeight(Screen.getPrimary().getBounds().getHeight()*0.8);
		gridChart.prefWidthProperty().bind(scroll.widthProperty());
	}

	@FXML
	public void selectoutPut() {
		File selectedDirectory = PathTools.selectFolder(Paths.getProjectPath() + "\\output");
		if (selectedDirectory != null) {
			outputpath = selectedDirectory.getAbsolutePath();
			newOutPut(yearChoice.getValue());
			Graphs(gridChart);
		}
	}

	@FXML
	public void yearChoice() {
		Paths.setCurrentYear((int) Tools.sToD(yearChoice.getValue()));

		if (outputpath.length() > 0) {
			newOutPut(Paths.getCurrentYear() + "");
		}
	}

	void newOutPut(String year) {
		M.servicesAndOwner(year, outputpath);

		for (int i = 0; i < radioColor.length; i++) {
			if (radioColor[i].isSelected()) {
				radioColor[i].fire();
				radioColor[i].setSelected(true);
			}
		}
	}

	void Graphs(GridPane gridPane) {
		gridPane.getChildren().clear();
		ArrayList<LineChart<Number, Number>> lineChart = new ArrayList<>();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		HashMap<String, String[]> reder = CsvTools
				.ReadAsaHash(PathTools.fileFilter(outputpath, "-AggregateServiceDemand.csv").get(0));

		ArrayList<HashMap<String, double[]>> has = new ArrayList<>();
		CellsSet.getServicesNames().forEach(servicename -> {
			HashMap<String, double[]> ha = new HashMap<>();
			reder.forEach((name, value) -> {
				double[] tmp = new double[value.length];
				for (int i = 0; i < value.length; i++) {
					tmp[i] = Tools.sToD(value[i]);
				}
				if (name.contains(servicename)) {
					ha.put(name, tmp);
				}
			});
			has.add(ha);
			lineChart.add(
					new LineChart<>(new NumberAxis(Paths.getStartYear(), Paths.getEndtYear(), 5), new NumberAxis()));
		});
		has.add(updatComposition(outputpath, "-AggregateAFTComposition.csv"));
		lineChart.add(new LineChart<>(new NumberAxis(Paths.getStartYear(), Paths.getEndtYear(), 5), new NumberAxis()));
		int j = 0, k = 0;
		for (int i = 0; i < has.size(); i++) {

			HashMap<String, double[]> data = has.get(i);
			LineChart<Number, Number> Ch = lineChart.get(i);
			new LineChartTools().lineChart(M, (Pane) Ch.getParent(), Ch, data);
			if (i == has.size() - 1) {
				Ch.setCreateSymbols(false);
				for (int k2 = 0; k2 < Ch.getData().size(); k2++) {
					Manager a = M.AFtsSet.getAftHash().get(Ch.getData().get(k2).getName());
					Ch.getData().get(k2).getNode().lookup(".chart-series-line")
							.setStyle("-fx-stroke: " + ColorsTools.getStringColor(a.getColor()) + ";");
				}

				new LineChartTools().labelcolor(M, Ch);
				
			}
			gridPane.add(Tools.vBox(Ch), j++, k);
			MousePressed.mouseControle((Pane)Ch.getParent(), Ch);
			if (j % 3 == 0) {
				k++;
				j = 0;
			}
		}
	}

	HashMap<String, double[]> updatComposition(String path, String nameFile) {
		HashMap<String, String[]> reder = CsvTools.ReadAsaHash(PathTools.fileFilter(path, nameFile).get(0));
		HashMap<String, double[]> has = new HashMap<>();

		reder.forEach((name, value) -> {
			double[] tmp = new double[value.length];
			for (int i = 0; i < value.length; i++) {
				tmp[i] = Tools.sToD(value[i]);
			}
			has.put(name, tmp);

		});
		return has;
	}
}
