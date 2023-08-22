package TabsPane;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import UtilitiesFx.CsvTools;
import UtilitiesFx.LineChartTools;
import UtilitiesFx.Path;
import UtilitiesFx.ReadFile;
import UtilitiesFx.Tools;
import WorldPack.AFT;
import WorldPack.Agents;
import WorldPack.Lattice;
import WorldPack.Cell;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class SimulationOutPut {

	Lattice M;

	public SimulationOutPut(Lattice M) {
		this.M = M;
	}

	public Tab pane() {

		try {
			servicesAndOwner(Path.startYear + "");
		} catch (IOException e) {
		}

		

		int length = Lattice.servicesNames.size() + 1;
		RadioButton[] radioColor = new RadioButton[length];

		ArrayList<String> senariosList = ReadFile.findFolder(new File(Path.projectPath + "\\output"), true);
		ChoiceBox<String> choiceScenario = new ChoiceBox<>();
		choiceScenario.getItems().addAll(senariosList);
		choiceScenario.setValue(senariosList.get(0));

		ArrayList<String> yearList = new ArrayList<>();
		for (int i = Path.startYear; i < Path.endtYear; i++) {
			yearList.add(i + "");
		}
		for (int i = 0; i < yearList.size(); i++) {
			String[] temp = yearList.get(i).split("-");
			yearList.set(i, temp[temp.length - 1].replace(".csv", ""));
		}

		GridPane gridPane = new GridPane();

		ChoiceBox<String> choiceYear = new ChoiceBox<>();
		choiceYear.getItems().addAll(yearList);
		choiceYear.setValue(yearList.get(0));
		choiceScenario.setOnAction(e -> {
			newOutPut(radioColor, choiceScenario.getValue(), choiceYear.getValue());
			Graphs(gridPane, false);
		});
		choiceYear.setOnAction(e -> {
			choiceScenario.fireEvent(e);
		});
		HBox hbox = new HBox();
		hbox.getChildren().add(Tools.hBox(choiceScenario, choiceYear/* ,display */));

		for (int i = 0; i < length; i++) {
			if (i < Lattice.servicesNames.size()) {
				radioColor[i] = new RadioButton(Lattice.servicesNames.get(i));
			} else if (i == Lattice.servicesNames.size()) {
				radioColor[i] = new RadioButton("Agent");
			}
			hbox.getChildren().add(radioColor[i]);
			int k = i;
			radioColor[i].setOnAction(e -> {
				for (int j = 0; j < length; j++) {
					if (k != j) {
						radioColor[j].setSelected(false);
					}
				}
				if (k < Lattice.servicesNames.size()) {
					M.colorMap(Lattice.servicesNames.get(k));
				} else if (k == Lattice.servicesNames.size()) {
					M.colorMap("Agent");
				}
			});
		}
		TitledPane titel = Tools.T("Displays Services OutPut: ", true, Tools.vBox(hbox,gridPane)
				);

		titel.setStyle(" -fx-base: #d6d9df;");
		Tab caPane = new Tab("OutPut", titel);
		Tools.initialisPane(caPane,choiceScenario,0.6);

		return caPane;
	}

	void newOutPut(RadioButton[] radioColor, String senario, String year) {
		try {
			Path.scenario = senario;
			servicesAndOwner(year);
		} catch (IOException e) {
		}
		for (int i = 0; i < radioColor.length; i++) {
			if (radioColor[i].isSelected()) {
				radioColor[i].fire();
				radioColor[i].setSelected(true);			}
		}
	}

	void Graphs(GridPane gridPane, boolean isUpdating) {
		gridPane.getChildren().clear();
		ArrayList<LineChart<Number, Number>> lineChart = new ArrayList<>();
		gridPane.setHgap(10);
		gridPane.setVgap(10);

		HashMap<String, String[]> reder = CsvTools
				.ReadAsaHash(Path.fileFilter("\\output\\", Path.scenario, "-AggregateServiceDemand.csv").get(0));

		ArrayList<HashMap<String, double[]>> has = new ArrayList<>();
		Lattice.servicesNames.forEach(servicename -> {
			HashMap<String, double[]> ha = new HashMap<>();
			reder.forEach((name, value) -> {
				double[] tmp = new double[value.length];
				for (int i = 0; i < value.length; i++) {
					tmp[i] = Tools.sToD(value[i]);
//						System.out.println(name+"  "+value[i]+"-->"+Tools.sToD(value[i]));
					
					
				}
				if (name.contains(servicename.toLowerCase())) {
					ha.put(name, tmp);
				}
			});
			has.add(ha);
			lineChart.add(new LineChart<>(new NumberAxis(), new NumberAxis()));
		});
		has.add(updatComposition("-AggregateAFTComposition.csv"));
		lineChart.add(new LineChart<>(new NumberAxis(), new NumberAxis()));
		// has.add(updatComposition("-AggregateAFTCompetitiveness.csv"));
		//lineChart.add(new LineChart<>(new NumberAxis(), new NumberAxis()));

		int j = 0, k = 0;
		for (int i = 0; i < has.size(); i++) {
			HashMap<String, double[]> e = has.get(i);
			LineChart<Number, Number> Ch = lineChart.get(i);
			if (isUpdating)
				LineChartTools.Chartupdate(Ch, e);
			else
				LineChartTools.Charte(Ch, e);
			gridPane.add(Ch, j++, k);
			if (j % 3 == 0) {
				k++;
				j = 0;
			}
		}
	}

	HashMap<String, double[]> updatComposition(String nameFile) {
		HashMap<String, String[]> reder = CsvTools
				.ReadAsaHash(Path.fileFilter("\\output\\", Path.scenario, nameFile).get(0));
		HashMap<String, double[]> has = new HashMap<>();

		reder.forEach((name, value) -> {
			double[] tmp = new double[value.length];
			for (int i = 0; i < value.length; i++) {
				tmp[i] = Tools.sToD(value[i]);
			}
				has.put(name.toUpperCase(), tmp);
			
		});
		return has;
	}

	public void servicesAndOwner(String year) throws IOException {
		HashMap<String, String[]> hash = CsvTools
				.ReadAsaHash(Path.fileFilter("\\output\\", Path.scenario, "-Cell-" + year + ".csv").get(0));
		for (int i = 1; i < hash.values().iterator().next().length; i++) {
			Cell p = M.hashCell.get(hash.get("x")[i] + "," + hash.get("y")[i]);

			for (int j = 0; j < Lattice.servicesNames.size(); j++) {
				if (p != null)
					p.services.put(Lattice.servicesNames.get(j),
							Tools.sToD(hash.get("service:" + Lattice.servicesNames.get(j))[i]));
			}
			int ii = i;
			
			Agents.aftReSet.forEach((name, agent) -> {
				if (name.equals(hash.get("agent")[ii])) {
					p.owner = new AFT(agent);
					// a.Mypaches.add(p);
				}
			});
		}
	}

}
