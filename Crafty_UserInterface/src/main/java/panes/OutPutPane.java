package panes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.Agents;
import dataLoader.MapLoader;
import dataLoader.Paths;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.OpenTabs;
import model.AFT;
import model.Lattice;

public class OutPutPane {

	MapLoader M;
	String outputpath = "";
	RadioButton[] radioColor;
	VBox vbox = new VBox();
	public OutPutPane(MapLoader M) {
		this.M = M;
	}

	public Tab pane() {
		radioColor = new RadioButton[Lattice.getServicesNames().size() + 1];
		ChoiceBox<String> choiceYear = new ChoiceBox<>();
		GridPane gridPane = new GridPane();

		Button select = new Button("Select outPut Folder");
		select.setOnAction(e -> {
			File selectedDirectory = PathTools.selecFolder(Paths.getProjectPath() + "\\output");
			if (selectedDirectory != null) {
				outputpath = selectedDirectory.getAbsolutePath();
				newOutPut(choiceYear.getValue());
				Graphs(gridPane);
			}
		});

		ArrayList<String> yearList = new ArrayList<>();
		for (int i = Paths.getStartYear(); i < Paths.getEndtYear(); i++) {
			yearList.add(i + "");
		}

		choiceYear.getItems().addAll(yearList);
		choiceYear.setValue(Paths.getCurrentYear()+"");

		choiceYear.setOnAction(e -> {
			Paths.setCurrentYear((int)Tools.sToD(choiceYear.getValue()));
			if (outputpath.length() > 0)
				newOutPut(Paths.getCurrentYear()+"");
		});
		HBox hbox = new HBox();
		hbox.getChildren().add(Tools.hBox(select, choiceYear));

		for (int i = 0; i < radioColor.length; i++) {
			if (i < Lattice.getServicesNames().size()) {
				radioColor[i] = new RadioButton(Lattice.getServicesNames().get(i));
				
			} else if (i == Lattice.getServicesNames().size()) {
				radioColor[i] = new RadioButton("Agent");
			}
			hbox.getChildren().add(radioColor[i]);
			int k = i;
			radioColor[i].setOnAction(e -> {
				for (int j = 0; j < radioColor.length; j++) {
					if (k != j) {
						radioColor[j].setSelected(false);
					}
				}
				if (k < Lattice.getServicesNames().size()) {
					Lattice.colorMap(Lattice.getServicesNames().get(k));
				} else if (k == Lattice.getServicesNames().size()) {
					Lattice.colorMap("FR");
				}
//				if (!DataDisplay.winColor.isShowing()) {
//					ColorsTools.windowzpalette(DataDisplay.winColor);
//				}
			});
		}
		 
		vbox.getChildren().addAll(hbox, gridPane);
		TitledPane titel = Tools.T("Displays Services OutPut: ", true, vbox);
		Tab tab = new Tab("OutPut", titel);
		tab.setOnSelectionChanged(e -> {
			choiceYear.setValue(Paths.getCurrentYear()+"");
			OpenTabs.choiceScenario.setDisable(true);
			OpenTabs.year.setDisable(true);
		});
		return tab;
	}

	void newOutPut(String year) {
			MapLoader.servicesAndOwner(year,outputpath);
		
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
		Lattice.getServicesNames().forEach(servicename -> {
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
			lineChart.add(new LineChart<>(new NumberAxis(), new NumberAxis()));
		});
		has.add(updatComposition(outputpath, "-AggregateAFTComposition.csv"));
		lineChart.add(new LineChart<>(new NumberAxis(), new NumberAxis()));
		int j = 0, k = 0;
		for (int i = 0; i < has.size(); i++) {

			HashMap<String, double[]> data = has.get(i);
			LineChart<Number, Number> Ch = lineChart.get(i);
			LineChartTools.lineChart(vbox,Ch, data);
			if (i == has.size() - 1) {
				Ch.setCreateSymbols(false);
				for (int k2 = 0; k2 < Ch.getData().size(); k2++) {
					AFT a = Agents.aftReSet.get(Ch.getData().get(k2).getName());
					Ch.getData().get(k2).getNode().lookup(".chart-series-line")
							.setStyle("-fx-stroke: " + ColorsTools.getStringColor(a.getColor()) + ";");
				}

				LineChartTools.labelcolor(Ch);
			}
			gridPane.add(Tools.vBox(Ch), j++, k);
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

//	void circleGraph() {
//		// red all filles
//		// creat a csv file x,y,owenrs1,owner2,...
//		String[][] ows = new String[Lattice.P.size()][Path.endtYear - Path.startYear];
//		for (int year = Path.startYear; year < Path.endtYear; year++) {
//			HashMap<String, String[]> reder = CsvTools
//					.ReadAsaHash(Path.fileFilter("\\output\\", Path.scenario, "-Cell-" + year).get(0));
//		}
//
//		// creat a hashmap<String,i++> (String= aft1_ft2)
//		// hashMap cell first owner and second owner
//		//
//
//	}



}
