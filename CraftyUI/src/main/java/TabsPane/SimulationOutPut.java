package TabsPane;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import Main.Main_CraftyFx;
import UtilitiesFx.CsvTools;
import UtilitiesFx.LineChartTools;
import UtilitiesFx.Path;
import UtilitiesFx.ReadFile;
import UtilitiesFx.Tools;
import WorldPack.Map;
import WorldPack.Patch;

import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

public class SimulationOutPut {

	Map M;

	public SimulationOutPut(Map M) {
		this.M = M;
	}

	public TitledPane pane() {

		try {
			servicesAndOwner("RCP2_6-SSP1", "2020");
		} catch (IOException e) {
		}

		VBox vbox = new VBox();

		int length = Patch.servicesName.size() + 1;
		RadioButton[] radioColor = new RadioButton[length];

		ArrayList<String> senariosList = ReadFile.findFolder(new File(Path.projectPath + "\\output"), true);
		ChoiceBox<String> choiceSenaio = new ChoiceBox<>();
		choiceSenaio.getItems().addAll(senariosList);
		choiceSenaio.setValue(senariosList.get(0));
		ArrayList<String> yearList = ReadFile.findFolder(new File(Path.projectPath + "\\output\\" + choiceSenaio.getValue()),
				"-Cell-", false);

		for (int i = 0; i < yearList.size(); i++) {
			 String[] temp = yearList.get(i).split("-");
			yearList.set(i, temp[temp.length - 1].replace(".csv", ""));
		}

		ChoiceBox<String> choiceYear = new ChoiceBox<>();
		choiceYear.getItems().addAll(yearList);
		choiceYear.setValue(yearList.get(0));
		choiceSenaio.setOnAction(e -> {
			newOutPut(radioColor, choiceSenaio.getValue(), choiceYear.getValue());
		});
		choiceYear.setOnAction(e -> {
			choiceSenaio.fireEvent(e);
		});

		vbox.getChildren().add(Tools.hBox(choiceSenaio, choiceYear));

		for (int i = 0; i < length; i++) {
			if (i < Patch.servicesName.size()) {
				radioColor[i] = new RadioButton(Patch.servicesName.get(i).replace("Service:", ""));
			} else if (i == Patch.servicesName.size()) {
				radioColor[i] = new RadioButton("Agent");
			}
			vbox.getChildren().add(radioColor[i]);
			int k = i;
			radioColor[i].setOnAction(e -> {
				for (int j = 0; j < length; j++) {
					if (k != j) {
						radioColor[j].setSelected(false);
					}
				}
				if (k < Patch.servicesName.size()) {
					M.colorMap(Patch.servicesName.get(k));
				} else if (k == Patch.servicesName.size()) {
					M.colorMap("Agent");
				}

			});

		}

		vbox.getChildren().addAll(chartOutput("RCP8_5-SSP5", "AggregateAFTComposition"),
				chartOutput("RCP8_5-SSP5", "AggregateAFTCompetitiveness"));
		TitledPane titel = Tools.T("Displays Services OutPut: ", true, Tools.T("Visualize spatial data", true,
				Tools.hBox(vbox, new Separator(), new Separator(), new Separator())));

		titel.setStyle(" -fx-base: #d6d9df;");
		titel.setMaxWidth(Main_CraftyFx.sceneWidth);
		titel.setMinWidth(Main_CraftyFx.sceneWidth);
		titel.setMaxHeight(Screen.getPrimary().getBounds().getHeight());
		titel.setMinHeight(Screen.getPrimary().getBounds().getHeight());
		return titel;
	}

	void newOutPut(RadioButton[] radioColor, String senario, String year) {
		try {
			servicesAndOwner(senario, year);
		} catch (IOException e) {
		}
		for (int i = 0; i < radioColor.length; i++) {
			if (radioColor[i].isSelected()) {
				radioColor[i].fire();
			}

		}

	}

	public void servicesAndOwner(String senario, String year) throws IOException {
		String[][] vect = CsvTools.csvReader("C:\\Users\\byari-m\\Documents\\Data\\data_UK\\output\\" + senario
				+ "\\" + senario + "-0-99-UK-Cell-" + year + ".csv");
		Patch.servicesName.clear();
		String[] line0 = vect[0];
		for (int i = 0; i < line0.length; i++) {
			if (line0[i].contains("Service")) {
				Patch.servicesName.add(line0[i]);
			}
		}

		for (int i = 1; i < vect.length; i++) {
			Patch p = M.HashPatchs.get(vect[i][1] + "," + vect[i][2]);
			for (int j = 0; j < Patch.servicesName.size(); j++) {
				if (p != null)
					p.servicesValue.put(Patch.servicesName.get(j), Tools.sToD(vect[i][j + 3]));
			}
			int ii = i;
			M.agents.AFT.forEach(a -> {
				if (a.label.equals(vect[ii][32])) {
					p.owner = a;
					a.Mypaches.add(p);
				}
			});

		}
	}

	Pane chartOutput(String senario, String fileName) {
		String[][] vect = CsvTools.csvReader("C:\\Users\\byari-m\\Documents\\Data\\data_UK\\output\\" + senario
				+ "\\" + senario + "-0-99-UK-" + fileName + ".csv");
		String[] line0 = vect[0];
		HashMap<String, Number[]> hash = new HashMap<>();
		for (int m = 0; m < line0.length; m++) {
			String[] valuesSTR = CsvTools.columnFromsMatrix(m, vect);
			Number[] values = new Number[valuesSTR.length];
			for (int i = 1; i < values.length; i++) {
				values[i] = Tools.sToD(valuesSTR[i]);
			}
			hash.put(line0[m], values);
		}

		LineChartTools a = new LineChartTools();
		 NumberAxis X = new NumberAxis(2020, 2080, 10);
		Pane A = (Pane) a.graph("Output",hash, X);

		return A;

	}

}
