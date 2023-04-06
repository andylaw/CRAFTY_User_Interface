package WorldPack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import TabsPane.NewWindow;
import UtilitiesFx.CSVTableView;
import UtilitiesFx.ColorsTools;
import UtilitiesFx.CsvTools;
import UtilitiesFx.LineChartTools;
import UtilitiesFx.Path;
import UtilitiesFx.PieChartTools;
import UtilitiesFx.Tools;
import javafx.scene.Group;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PatchWindow {

	Group root = new Group();
	Patch patch;

	public PatchWindow(Patch patch) {
		this.patch = patch;
	}

	void windosLocalInfo() {

		VBox box = new VBox();

		PieChart pieChart = new PieChart();
		HashMap<String, Color> color = new HashMap<>();
		patch.capitalsValue.forEach((k, v) -> {
			color.put(k, ColorsTools.RandomColor());
		});

		new PieChartTools().updateChart(patch.capitalsValue, color, pieChart, true);

		ChoiceBox<String> choiceScenario = Tools.chois(Path.production, false);

		choiceScenario.setOnAction(e -> {
			Tools.showWaitingDialog(x -> {
				if (!choiceScenario.getValue().equals("Baseline")) {
					HashMap<String, Number[]> hash = sspData(choiceScenario.getValue());

					NumberAxis X = new NumberAxis(2016, 2100, 10);
					LineChartTools a = new LineChartTools();
					HBox A = (HBox) a.graph(choiceScenario.getValue(), hash, X);
					box.getChildren().addAll(A);
				}
			});
		});

		box.getChildren().addAll(Tools.hBox(cellDataTable(), pieChart), choiceScenario);
		ScrollPane sp = new ScrollPane();
		sp.setContent(box);
		new NewWindow().creatwindows("Patch  (" + (int) patch.coor.getX() + "," + (int) patch.coor.getY() + ")", 0.4,
				0.8, sp);

	}

	TableView<String> cellDataTable() {

		String[][] data = new String[patch.capitalsValue.size() + patch.GIS.size()][2];
		AtomicInteger i = new AtomicInteger(0);
		patch.capitalsValue.forEach((k, v) -> {
			data[i.get()][0] = k;
			if (data[i.get()][0].equals("FR")) {
				data[i.get()][1] = patch.owner.label;
			} else if (data[i.get()][0].equals("Region")) {
				data[i.get()][1] = patch.country;
			} else {
				data[i.get()][1] = v + "";
			}
			i.getAndIncrement();
		});
		patch.GIS.forEach((k, v) -> {
			data[i.get()][0] = k;
			data[i.get()][1] = v;
			i.getAndIncrement();
		});

		data[0][0] = "Attribute Name";
		data[0][1] = "Value";

		try {
			return new CSVTableView(data, 1, 1, false);
		} catch (IOException e) {
		}
		return null;

	}

	HashMap<String, Number[]> sspData(String senario) {
		HashMap<String, Number[]> hash = new HashMap<>();

		 ArrayList<String> list = Path.fileFilter(senario, Path.worldNameList.get(2), "capitals");
		AtomicInteger year = new AtomicInteger(2020);
		list.forEach(e -> {
			try {
				patch.ssp.put(year.getAndIncrement(), RCPi_SSPi(e));
			} catch (IOException e2) {
			}
		});

		Number[][] temp = new Number[patch.ssp.size()][];
		AtomicInteger i = new AtomicInteger();

		patch.ssp.forEach((key, hashSD) -> {
			temp[i.get()] = new Number[hashSD.size()];
			AtomicInteger j = new AtomicInteger();
			hashSD.forEach((name, value) -> {
				if (value > 0 && value <= 1) {

					temp[i.get()][j.get()] = value;
				} else {
					temp[i.get()][j.get()] = 0;
				}
				j.getAndIncrement();
			});

			i.getAndIncrement();
		});

		Number[][] nbr = new Number[temp[0].length][temp.length];

		for (int colunNumber = 0; colunNumber < temp[0].length; colunNumber++) {
			for (int n = 0; n < temp.length; n++) {
				nbr[colunNumber][n] = temp[n][colunNumber];
			}
			hash.put(Patch.capitalsName.get(colunNumber), nbr[colunNumber]);
		}
		return hash;
	}

	@Override
	public String toString() {
		return " [patch=" + patch + "] \n  ";
	}

	HashMap<String, Double> RCPi_SSPi(String Path) throws IOException {

		String[] celldata = CsvTools.lineFromscsv(patch.index, Path);
		HashMap<String, Double> capitalsV = new HashMap<>();

		for (int i = 0; i < Patch.capitalsName.size(); i++) {
			capitalsV.put(Patch.capitalsName.get(i), Tools.sToD(celldata[i + 2]));

		}

		return capitalsV;

	}

}
