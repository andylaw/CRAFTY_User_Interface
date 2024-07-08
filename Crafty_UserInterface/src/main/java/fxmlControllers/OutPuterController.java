package fxmlControllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.filesTools.SaveAs;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.ImageExporter;
import UtilitiesFx.graphicalTools.ImagesToPDF;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.SankeyPlotGraph;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import model.CellsSet;
import model.Manager;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class OutPuterController {
	CellsLoader M;

	@FXML
	private Button saveAllFilAsPNG;
	@FXML
	private ChoiceBox<String> yearChoice;
	@FXML
	private ChoiceBox<String> sankeyBox;
	@FXML
	private GridPane gridChart;
	@FXML
	private ScrollPane scroll;
	@FXML
	private Button selecserivce;
	@FXML
	private VBox borderPane;
	String outputpath = "";

	ArrayList<CheckBox> radioListOfAFTs = new ArrayList<>();
	private static final Logger LOGGER = LogManager.getLogger(OutPuterController.class);

	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		M = TabPaneController.M;
		selectoutPut();
		scroll.setPrefHeight(Screen.getPrimary().getBounds().getHeight() * 0.8);
		gridChart.prefWidthProperty().bind(scroll.widthProperty());
	}

	HashMap<Manager, HashMap<Manager, Integer>> stateToHashSankey(String lastYear) {

		HashMap<String, String> copyfirstYearHash = new HashMap<>();
		CellsLoader.hashCell.forEach((coor, c) -> {
			if (c.getOwner() != null)
				copyfirstYearHash.put(coor, c.getOwner().getLabel());
		});
		// Find file in the correct folder and update hashCell

		yearChoice.getItems().stream().filter(str -> str.contains(lastYear)).findFirst().ifPresent(this::newOutPut);

		HashMap<String, Integer> h = new HashMap<>();
		copyfirstYearHash.forEach((coor, label) -> {
			Manager owner = CellsLoader.hashCell.get(coor).getOwner();
			if (owner != null) {
				h.merge(label + "," + owner.getLabel(), 1, Integer::sum);
			}
		});

		copyfirstYearHash.clear();
		HashMap<Manager, HashMap<Manager, Integer>> hash = new HashMap<>();

		AFTsLoader.getActivateAFTsHash().keySet().forEach(label -> {
			HashMap<Manager, Integer> h1 = new HashMap<>();
			h.forEach((k, v) -> {
				String[] vect = k.split(",");
				if (vect.length == 2) {
					if (vect[0].equals(label)) {
						Manager reciver = AFTsLoader.getActivateAFTsHash().get(vect[1]);
						if (reciver != null)
							h1.put(reciver, v);
					}
				}
			});
			hash.put(AFTsLoader.getActivateAFTsHash().get(label), h1);
		});
		return hash;
	}

	public void selectoutPut() {
		File selectedDirectory = PathTools.selectFolder(Paths.getProjectPath() + "\\output");
		if (selectedDirectory != null) {
			outputpath = selectedDirectory.getAbsolutePath();

			ArrayList<String> yearList = new ArrayList<>();
			PathTools.findAllFiles(outputpath).forEach(str -> {
				File file = new File(str);
				String tmp = new File(file.getParent()).getName() + "\\" + file.getName();

				if (tmp.contains("-Cell-"))
					yearList.add(tmp);
			});
			LOGGER.info("output files List---> " + yearList);
			yearChoice.getItems().addAll(yearList);
			yearChoice.setValue(yearList.get(0));
			sankeyBox.getItems().addAll(yearList);
			sankeyBox.setValue(yearList.get(yearList.size() - 1));
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
		if (outputpath.length() > 0) {
			newOutPut(yearChoice.getValue());
		}
	}

	@FXML
	public void sankeyPlot() {
		if (outputpath.length() > 0) {
			Text txt2 = Tools.text(new File(yearChoice.getValue()).getName(), Color.BLUE);
			HashMap<Manager, HashMap<Manager, Integer>> h = stateToHashSankey(sankeyBox.getValue());
			Set<Manager> setManagers = new HashSet<>();
			VBox boxOfAftRadios = new VBox();
			AFTsLoader.getActivateAFTsHash().keySet().forEach(n -> {
				CheckBox radio = new CheckBox(n);
				radioListOfAFTs.add(radio);
				boxOfAftRadios.getChildren().add(radio);
				radio.setSelected(true);
				setManagers.add(AFTsLoader.getActivateAFTsHash().get(radio.getText()));
				radio.setOnAction(e->{
					if(radio.isSelected()) {
						setManagers.add(AFTsLoader.getActivateAFTsHash().get(radio.getText()));
					}
					else {
						setManagers.remove(AFTsLoader.getActivateAFTsHash().get(radio.getText()));
					}
					updateSankeyPlot( txt2,boxOfAftRadios, h, setManagers);
				});
			});
			
			updateSankeyPlot( txt2,boxOfAftRadios, h, setManagers);

		}
	}
	
	void updateSankeyPlot(Text txt2,VBox boxOfAftRadios,HashMap<Manager, HashMap<Manager, Integer>> h, Set<Manager> setManagers) {
		Text txt = new Text("Create a Sankey diagram for  ");
		Text txt3 = new Text("  To  ");
		Text txt4 = Tools.text(new File(sankeyBox.getValue()).getName(), Color.RED);
		SankeyPlotGraph.AFtsToSankeyPlot(h, setManagers);
		MousePressed.mouseControle(borderPane, SankeyPlotGraph.sankey);
		borderPane.getChildren().clear();
		borderPane.getChildren().addAll(Tools.hBox(txt, txt2, txt3, txt4),
				Tools.hBox(boxOfAftRadios , SankeyPlotGraph.sankey));
		SankeyPlotGraph.sankey.setPrefWidth(scroll.getPrefWidth());
	}

	void newOutPut(String year) {
		M.servicesAndOwneroutPut(year, outputpath);
		for (int i = 0; i < OutPutTabController.radioColor.length; i++) {
			if (OutPutTabController.radioColor[i].isSelected()) {
				OutPutTabController.radioColor[i].fire();
				OutPutTabController.radioColor[i].setSelected(true);
			}
		}
		yearChoice.setValue(year);
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
