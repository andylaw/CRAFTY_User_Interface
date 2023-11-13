package panes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.CSVTableView;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.PieChartTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.Paths;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Cell;
import model.Lattice;

public class CellWindow {

	private Cell cell;

	public CellWindow(Cell patch) {
		this.cell = patch;
	}

	public void windosLocalInfo() {

		VBox box = new VBox();

		PieChart pieChart = new PieChart();
		HashMap<String, Color> color = new HashMap<>();
		cell.getCapitals().forEach((k, v) -> {
			color.put(k, ColorsTools.RandomColor());
		});

		new PieChartTools().updateChart(cell.getCapitals(), color, pieChart, true);

//		ChoiceBox<String> choiceScenario = Tools.chois(Path.production, false);
//
//		choiceScenario.setOnAction(e -> {
//			WarningWindowes.showWaitingDialog(x -> {
//				if (!choiceScenario.getValue().equals("Baseline")) {
//					Path.scenario = choiceScenario.getValue();
//					HashMap<String, Number[]>[] CS = outputData();
////					HashMap<String, Number[]> hash = //sspData(choiceScenario.getValue());
////					CS[1];
//					NumberAxis X = new NumberAxis(Path.startYear, Path.endtYear, 10);
//					LineChartTools a = new LineChartTools();
//					HBox A = (HBox) a.graph(choiceScenario.getValue(), CS[0], X);
//					HBox B = (HBox) a.graph(choiceScenario.getValue(), CS[1], X);
//					// HBox C = (HBox) a.graph(choiceScenario.getValue(), CS[2], X);
//					box.getChildren().addAll(Tools.hBox(A, B)/* ,C */);
//				}
//			});
//		});

		box.getChildren().addAll(Tools.hBox(Tools.text(Paths.getScenario()+"  ",Color.BLUE),Tools.text(Paths.getCurrentYear()+"  ",Color.BLUE)),
				Tools.hBox(cellDataTable(), pieChart));
		ScrollPane sp = new ScrollPane();
		sp.setContent(box);
		new NewWindow().creatwindows("Patch  (" + (int) cell.getX() + "," + (int) cell.getY() + ")", 0.4, 0.8, sp);

	}

	TableView<ObservableList<String>> cellDataTable() {

		String[][] data = new String[cell.getCapitals().size() + cell.getGisNameValue().size()][2];
		AtomicInteger i = new AtomicInteger(0);
		cell.getCapitals().forEach((k, v) -> {
			data[i.get()][0] = k;
			if (data[i.get()][0].equals("FR")) {
				data[i.get()][1] = cell.getOwner().getLabel();
			} else if (data[i.get()][0].equals("Region")) {
				// data[i.get()][1] = patch.country;
			} else {
				data[i.get()][1] = v + "";
			}
			i.getAndIncrement();
		});
		cell.getGisNameValue().forEach((k, v) -> {
			data[i.get()][0] = k;
			data[i.get()][1] = v;
			i.getAndIncrement();
		});

		data[0][0] = "Attribute Name";
		data[0][1] = "Value";

		return CSVTableView.newtable(data);

	}

	HashMap<String, Number[]> sspData(String senario) {
		HashMap<Integer, HashMap<String, Double>> ssp = new HashMap<>();
		HashMap<String, Number[]> hash = new HashMap<>();

		ArrayList<String> list = PathTools.fileFilter(senario, /* Path.referqnceWorld, */ "capitals");
		AtomicInteger year = new AtomicInteger(2015);
		list.forEach(e -> {
			try {
				ssp.put(year.getAndIncrement(), RCPi_SSPiCapital(e));
			} catch (IOException e2) {
			}
		});

		Number[][] temp = new Number[ssp.size()][];
		AtomicInteger i = new AtomicInteger();

		ssp.forEach((key, hashSD) -> {
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
			hash.put(Lattice.getCapitalsName().get(colunNumber), nbr[colunNumber]);
		}
		return hash;
	}

	HashMap<String, Number[]>[] outputData() {
		HashMap<String, Number[]>[] hash = new HashMap[3];
		hash[0] = new HashMap<>();
		hash[1] = new HashMap<>();
		hash[2] = new HashMap<>();
		ArrayList<String> list = PathTools.fileFilter(/* Path.referqnceWorld, */ "\\output\\" + Paths.getScenario(), "-Cell-");
		ArrayList<String> agent = new ArrayList<>();
		ArrayList<Double> competitiveness = new ArrayList<>();
		ArrayList<HashMap<String, Double>> listOfcapihash = new ArrayList<>();
		ArrayList<HashMap<String, Double>> listOfserviceHash = new ArrayList<>();
		list.forEach(file -> {
			HashMap<String, String> celldata = CsvTools.lineFromscsvHash(cell.getIndex() - 1, file);
			HashMap<String, Double> capitalData = new HashMap<>();
			HashMap<String, Double> serviceData = new HashMap<>();
			celldata.forEach((name, value) -> {
				if (name.contains("service:")) {
					serviceData.put(name, Tools.sToD(value));
				} else if (name.contains("capital:")) {
					capitalData.put(name, Tools.sToD(value));
				} else if (name.contains("agent")) {
					agent.add(value);
				} else if (name.contains("competitiveness")) {
					competitiveness.add(Tools.sToD(value));
				}
			});
			listOfcapihash.add(capitalData);
			listOfserviceHash.add(serviceData);

		});

		Number[][] nbr = new Number[Lattice.getCapitalsName().size()][listOfcapihash.size()];
		AtomicInteger i = new AtomicInteger();

		listOfcapihash.forEach(has -> {
			AtomicInteger j = new AtomicInteger();
			has.forEach((name, v) -> {
				nbr[j.getAndIncrement()][i.get()] = v;
			});
			i.getAndIncrement();
		});
		for (int j = 0; j < nbr.length; j++) {
			hash[0].put(Lattice.getCapitalsName().get(j), nbr[j]);
		}

		Number[][] nbrS = new Number[Lattice.getServicesNames().size()][listOfserviceHash.size()];
		AtomicInteger iS = new AtomicInteger();

		listOfserviceHash.forEach(has -> {
			AtomicInteger j = new AtomicInteger();
			has.forEach((name, v) -> {
				nbrS[j.getAndIncrement()][iS.get()] = v;
			});
			iS.getAndIncrement();
		});
		for (int j = 0; j < nbrS.length; j++) {
			hash[1].put(Lattice.getServicesNames().get(j), nbrS[j]);
		}

		Number[] com = new Number[competitiveness.size()];
		for (int j = 0; j < com.length; j++) {
			com[j] = competitiveness.get(j);
		}
		hash[2].put("Competitiveness", com);
		HashMap<Integer, String> hashAgntes = new HashMap<>();
		return hash;
	}

	@Override
	public String toString() {
		return " [patch=" + cell + "] \n  ";
	}

	HashMap<String, Double> RCPi_SSPiCapital(String Path) throws IOException {

		HashMap<String, String> celldata = CsvTools.lineFromscsvHash(cell.getIndex(), Path);
		HashMap<String, Double> capitalsV = new HashMap<>();
		celldata.forEach((key, val) -> {
			if (Lattice.getCapitalsName().contains(key.replace("\"", ""))) {
				capitalsV.put(key, Tools.sToD(val));
			}
		});
		return capitalsV;

	}

	HashMap<String, Double> RCPi_SSPiService(String Path) throws IOException {

		HashMap<String, String> celldata = CsvTools.lineFromscsvHash(cell.getIndex(), Path);
		HashMap<String, Double> serviceV = new HashMap<>();
		celldata.forEach((key, val) -> {
			if (Lattice.getServicesNames().contains(key.replace("\"", "").replace(" ", ""))) {
				serviceV.put(key, Tools.sToD(val));
			}
		});
		return serviceV;

	}

}
