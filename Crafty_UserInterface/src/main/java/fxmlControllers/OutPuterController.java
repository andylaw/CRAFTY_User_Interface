package fxmlControllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.filesTools.SaveAs;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.ImageExporter;
import UtilitiesFx.graphicalTools.ImagesToPDF;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.SankeyPlotGraph;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import model.CellsSet;
import model.Manager;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

public class OutPuterController {
	CellsLoader M;

	@FXML
	private Button selectoutPut;
	@FXML
	private Button saveAllFilAsPNG;
	@FXML
	private ChoiceBox<String> yearChoice;
	@FXML
	private GridPane gridChart;
	@FXML
	private ScrollPane scroll;
	@FXML
	private Button selecserivce;

	String outputpath = "";

	NewWindow sankeyPlotWindow;

	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		M = TabPaneController.M;
		yearChoice.setValue(Paths.getCurrentYear() + "");
		scroll.setPrefHeight(Screen.getPrimary().getBounds().getHeight() * 0.8);
		gridChart.prefWidthProperty().bind(scroll.widthProperty());
		sankeyPlotWindow = new NewWindow();
	}

	HashMap<Manager, HashMap<Manager, Integer>> stateToHashSankey(int startYear, int lastYear) {
		HashMap<String, ArrayList<String>> startYearFile = new HashMap<>();
		HashMap<String, ArrayList<String>> lastYearFile = new HashMap<>();

		for (String str : PathTools.findAllFiles(outputpath)) {
			if (str.contains(startYear + "")) {
				startYearFile = ReaderFile.ReadAsaHash(str);
			}
			if (str.contains(lastYear + "")) {
				lastYearFile = ReaderFile.ReadAsaHash(str);
			}
		}

		HashMap<String, String> startCellToOwner = new HashMap<>();
		HashMap<String, String> lastCellToOwner = new HashMap<>();

		for (int i = 0; i < startYearFile.values().iterator().next().size(); i++) {
			startCellToOwner.put(startYearFile.get("X").get(i) + "," + startYearFile.get("Y").get(i),
					startYearFile.get("Agent").get(i));
		}
		for (int i = 0; i < lastYearFile.values().iterator().next().size(); i++) {
			lastCellToOwner.put(lastYearFile.get("X").get(i) + "," + lastYearFile.get("Y").get(i),
					lastYearFile.get("Agent").get(i));
		}

		HashMap<String, Integer> h = new HashMap<>();

		lastCellToOwner.forEach((coor, label) -> {
			h.merge(startCellToOwner.get(coor) + "," + label, 1, Integer::sum);
		});
		HashMap<Manager, HashMap<Manager, Integer>> hash = new HashMap<>();

		AFTsLoader.getActivateAFTsHash().keySet().forEach(label -> {
			HashMap<Manager, Integer> h1 = new HashMap<>();
			h.forEach((k, v) -> {
				if (k.split(",")[0].equals(label)) {
					Manager reciver = AFTsLoader.getActivateAFTsHash().get(k.split(",")[1]);
					if (reciver != null)
						h1.put(reciver, v);
				}
			});
			hash.put(AFTsLoader.getActivateAFTsHash().get(label), h1);
		});

		return hash;

	}

	@FXML
	public void selectoutPut() {
		File selectedDirectory = PathTools.selectFolder(Paths.getProjectPath() + "\\output");

		if (selectedDirectory != null) {
			outputpath = selectedDirectory.getAbsolutePath();

			ArrayList<String> yearList = new ArrayList<>();
			PathTools.findAllFiles(outputpath).forEach(str -> {
				File file = new File(str);
				String tmp = new File(file.getParent()).getName() + "\\" + file.getName();

				if (tmp.contains("-Cell-"))
					yearList.add(tmp/*
									 * .replace(".csv", "").replace("-Cell-", "").replace(Paths.getScenario(), "")
									 */);
			});
			System.out.println("yearList---> " + yearList);
			yearChoice.getItems().addAll(yearList);
			yearChoice.setValue(yearList.get(0));
			System.out.println("yearList---> " + yearChoice.getValue());
			OutPutTabController.radioColor[OutPutTabController.radioColor.length - 1].setSelected(true);
			newOutPut(yearChoice.getValue());
			Graphs(gridChart);

		}
	}

	@FXML
	public void saveAllFilAsPNGAction() {

		for (int i = 0; i < OutPutTabController.radioColor.length; i++) {
			int ii = i;
			if (OutPutTabController.radioColor[i].getText().contains("Agent")) {
				String newfolder = PathTools
						.makeDirectory(outputpath + "\\" + OutPutTabController.radioColor[ii].getText());
				yearChoice.getItems().forEach(filepath -> {
					M.servicesAndOwneroutPut(filepath, outputpath);
					OutPutTabController.radioColor[ii].fire();
					String fileyear = new File(filepath).getName().replace(".csv", "").replace("-Cell-", "");
					for (String scenario : Paths.getScenariosList()) {
						fileyear = fileyear.replace(scenario, "");
					}
					ImageExporter.NodeToImage(CellsSet.getCanvas(), newfolder + "\\" + fileyear + ".PNG");
				});
			}
		}
		String newfolder = PathTools.makeDirectory(outputpath + "\\" + "Charts");
		gridChart.getChildren().forEach(chart -> {
			VBox container = (VBox) chart;
			@SuppressWarnings("unchecked")
			LineChart<Number, Number> ch = (LineChart<Number, Number>) container.getChildren().iterator().next();
			ImageExporter.NodeToImage(chart, newfolder + "\\" + ch.getTitle() + ".PNG");
		});
		List<File> foders = PathTools.detectFolders(outputpath);
		for (File folder : foders) {
			ImagesToPDF.createPDFWithImages(folder.getAbsolutePath(), folder.getName() + ".pdf", 4, 4);
		}

	}

	@FXML
	public void yearChoice() {
		// Paths.setCurrentYear((int) Tools.sToD(yearChoice.getValue()));

		if (outputpath.length() > 0) {
			newOutPut(yearChoice.getValue()/* Paths.getCurrentYear() + "" */);
		}
	}

	@FXML
	public void sankeyPlot() {
		if (outputpath.length() > 0) {
			HashMap<Manager, HashMap<Manager, Integer>> h = stateToHashSankey(Paths.getStartYear(),Paths.getEndtYear());
			SankeyPlotGraph.AFtsToSankeyPlot(h, 0);
			sankeyPlotWindow.creatwindows("Sankey Plot", new StackPane(SankeyPlotGraph.sankey));
		}
	}

	void newOutPut(String year) {
		M.servicesAndOwneroutPut(year, outputpath);
		for (int i = 0; i < OutPutTabController.radioColor.length; i++) {
			if (OutPutTabController.radioColor[i].isSelected()) {
				OutPutTabController.radioColor[i].fire();
				OutPutTabController.radioColor[i].setSelected(true);
			}
		}
	}

	void Graphs(GridPane gridPane) {
		gridPane.getChildren().clear();
		ArrayList<LineChart<Number, Number>> lineChart = new ArrayList<>();
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		HashMap<String, ArrayList<String>> reder = ReaderFile
				.ReadAsaHash(PathTools.fileFilter(outputpath, "AggregateServiceDemand.csv").get(0));

		ArrayList<HashMap<String, ArrayList<Double>>> has = new ArrayList<>();
		CellsSet.getServicesNames().forEach(servicename -> {
			HashMap<String, ArrayList<Double>> ha = new HashMap<>();
			reder.forEach((name, value) -> {
				ArrayList<Double> tmp = new ArrayList<>();
				for (int i = 0; i < value.size() - 2; i++) {
					tmp.add(Tools.sToD(value.get(i)));
				}
				if (name.contains(servicename)) {
					ha.put(name, tmp);
				}
			});

			has.add(ha);
			LineChart<Number, Number> chart = new LineChart<>(new NumberAxis(), new NumberAxis());
			chart.setTitle(servicename);
			lineChart.add(chart);
		});

		has.add(updatComposition(outputpath, "-AggregateAFTComposition.csv"));
		LineChart<Number, Number> chart = new LineChart<>(new NumberAxis(Paths.getStartYear(), Paths.getEndtYear(), 5),
				new NumberAxis());
		chart.setTitle("Land use trends");
		lineChart.add(chart);
		int j = 0, k = 0;
		for (int i = 0; i < has.size(); i++) {

			LineChart<Number, Number> Ch = lineChart.get(i);

			new LineChartTools().lineChart(M, (Pane) Ch.getParent(), Ch, has.get(i));

			// this for coloring the Chart by the AFTs color after the creation of the chart
			if (i == has.size() - 1) {
				coloringCartByAFts(Ch);
			}
			gridPane.add(Tools.vBox(Ch), j++, k);
			MousePressed.mouseControle((Pane) Ch.getParent(), Ch);
			if (j % 3 == 0) {
				k++;
				j = 0;
			}

			String ItemName = "Save as CSV";
			Consumer<String> action = x -> {
				SaveAs.exportLineChartDataToCSV(Ch);
			};
			HashMap<String, Consumer<String>> othersMenuItems = new HashMap<>();
			othersMenuItems.put(ItemName, action);
			MousePressed.mouseControle((Pane) Ch.getParent(), Ch, othersMenuItems);
		}
	}

	void coloringCartByAFts(LineChart<Number, Number> Ch) {
		Ch.setCreateSymbols(false);
		for (int k2 = 0; k2 < Ch.getData().size(); k2++) {
			Manager a = AFTsLoader.getAftHash().get(Ch.getData().get(k2).getName());
			Ch.getData().get(k2).getNode().lookup(".chart-series-line")
					.setStyle("-fx-stroke: " + ColorsTools.getStringColor(a.getColor()) + ";");
		}

		LineChartTools.labelcolor(M, Ch);
	}

	HashMap<String, ArrayList<Double>> updatComposition(String path, String nameFile) {
		try {
			HashMap<String, ArrayList<String>> reder = ReaderFile
					.ReadAsaHash(PathTools.fileFilter(path, nameFile).get(0));
			HashMap<String, ArrayList<Double>> has = new HashMap<>();

			reder.forEach((name, value) -> {
				ArrayList<Double> tmp = new ArrayList<>();
				for (int i = 0; i < value.size() - 2; i++) {
					tmp.add(Tools.sToD(value.get(i)));
				}
				has.put(name, tmp);

			});
			return has;
		} catch (NullPointerException e) {
			return null;
		}
	}
}
