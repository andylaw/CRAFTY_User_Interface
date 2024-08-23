package fxmlControllers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.filesTools.SaveAs;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.DemandModel;
import dataLoader.MaskRestrictionDataLoader;
import dataLoader.PathsLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import model.CellsSet;
import model.ModelRunner;
import javafx.scene.layout.GridPane;

public class ModelRunnerController {

	@FXML
	private VBox vbox;

	@FXML
	private Label tickTxt;

	@FXML
	private Button oneStep;
	@FXML
	private Button run;
	@FXML
	private Button pause;
	@FXML
	private Button stop;
	@FXML
	private GridPane gridPaneLinnChart;
	@FXML
	private ScrollPane scroll;
	CellsLoader M;
	public static String outPutFolderName;
	public ModelRunner R;
	Timeline timeline;
	AtomicInteger tick;
	ArrayList<LineChart<Number, Number>> lineChart;

	public static boolean chartSynchronisation = true;
	public static int chartSynchronisationGap = 5;

	double KeyFramelag = 1;
//	private final long desiredTickMillis = 1000;
	RadioButton[] radioColor;
	NewWindow colorbox = new NewWindow();

	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		M = TabPaneController.M;
		R = new ModelRunner(M);
		tick = new AtomicInteger(PathsLoader.getStartYear());
		tickTxt.setText(tick.toString());

		lineChart = new ArrayList<>();
		// lineChart = (ArrayList<LineChart<Number, Number>>)
		// Collections.synchronizedList(lineChart);

		Collections.synchronizedList(lineChart);
		outPutFolderName = PathsLoader.getScenario();
		initilaseChart(lineChart);
		initialzeRadioColorBox();

		// gridPaneLinnChart.setMinWidth(Screen.getPrimary().getBounds().getWidth()/3);
		// gridPaneLinnChart.setPrefWidth(scroll.getWidth()/3);

		scroll.setPrefHeight(Screen.getPrimary().getBounds().getHeight() * 0.8);

		initializeGridpane(2);
		gridPaneLinnChart.prefWidthProperty().bind(scroll.widthProperty());
	}

	void initializeGridpane(int colmunNBR) {
		int j = 0, k = 0;
		for (int m = 0; m < lineChart.size(); m++) {
			gridPaneLinnChart.add(Tools.hBox(lineChart.get(m)), j++, k);
			if (j % colmunNBR == 0) {
				k++;
				j = 0;
			}
		}
	}

	void initialzeRadioColorBox() {
		radioColor = new RadioButton[CellsSet.getServicesNames().size() + 1];
		radioColor[radioColor.length - 1] = new RadioButton("FR");
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			radioColor[i] = new RadioButton(CellsSet.getServicesNames().get(i));
		}

		for (int i = 0; i < radioColor.length; i++) {
			int m = i;
			radioColor[i].setOnAction(e -> {
				R.colorDisplay = radioColor[m].getText();
				CellsSet.colorMap(radioColor[m].getText());
				for (int I = 0; I < radioColor.length; I++) {
					if (I != m) {
						radioColor[I].setSelected(false);
					}
				}
			});
		}
	}

	@FXML
	void selecserivce() {
		if (!colorbox.isShowing()) {
			VBox g = new VBox();
			g.getChildren().addAll(radioColor);
			colorbox.creatwindows("Display Services and AFT distribution", g);
		}

	}

	@FXML
	public void oneStep() {
		System.out.println("------------------- Start of Tick  |" + tick.get() + "| -------------------");
		PathsLoader.setCurrentYear(tick.get());
		R.go();
		tickTxt.setText(tick.toString());
		if (chartSynchronisation && ((PathsLoader.getCurrentYear() - PathsLoader.getStartYear()) % chartSynchronisationGap == 0
				|| PathsLoader.getCurrentYear() == PathsLoader.getEndtYear())) {
			AtomicInteger m = new AtomicInteger();
			CellsSet.getServicesNames().forEach(name -> {
				lineChart.get(m.get()).getData().get(0).getData()
						.add(new XYChart.Data<>(tick.get(), DemandModel.getGolbalDemand(name, tick.get())));
				lineChart.get(m.get()).getData().get(1).getData()
						.add(new XYChart.Data<>(tick.get(), R.totalSupply.get(name)));
				m.getAndIncrement();
			 });
			ObservableList<Series<Number, Number>> observable = lineChart.get(lineChart.size() - 1).getData();
			List<String> listofNames = observable.stream().map(Series::getName).collect(Collectors.toList());
			AFTsLoader.hashAgentNbr.forEach((name, value) -> {
				observable.get(listofNames.indexOf(name)).getData().add(new XYChart.Data<>(tick.get(), value));

			});
		}
		tick.getAndIncrement();
	}

	@FXML
	public void run() {
//		popUpRunWindowz();
		run.setDisable(true);
		simulationFolderName();
		DemandModel.updateDemand();
		DemandModel.updateRegionsDemand();
		scheduleIteravitveTicks(Duration.millis(1000));
	}
	private void displayRunAsOutput() {
		OutPuterController.isCurrentResult=true;
		OutPutTabController.getInstance().createNewTab("Current simulation");
		TabPane tab = TabPaneController.getInstance().getTabpane();
		tab.getSelectionModel().select(tab.getTabs().size()-1);
	}

	private void scheduleIteravitveTicks(Duration delay) {
		if (PathsLoader.getCurrentYear() >= PathsLoader.getEndtYear() - 1) {
			// Stop if max iterations reached
			displayRunAsOutput();
			return;
		}
		if (ModelRunner.writeCsvFiles) {
			Path aggregateAFTComposition= Paths.get(outPutFolderName + File.separator  + PathsLoader.getScenario() + "-AggregateAFTComposition.csv");
			CsvTools.writeCSVfile(R.compositionAftListener, aggregateAFTComposition);
			Path aggregateServiceDemand= Paths.get(outPutFolderName + File.separator  + PathsLoader.getScenario() + "-AggregateServiceDemand.csv");
			CsvTools.writeCSVfile(R.servicedemandListener,aggregateServiceDemand );
		}
		// Stop the old timeline if it's running
		if (timeline != null) {
			timeline.stop();
		}
		// Create a new timeline for the next tick
		timeline = new Timeline(new KeyFrame(delay, event -> {
			long startTime = System.currentTimeMillis();
			// Perform the simulation update
			Platform.runLater(() -> {
				oneStep();
			});
			long endTime = System.currentTimeMillis();
			// Calculate the delay for the next tick to maintain the rhythm
			long delayForNextTick = Math.max(300, (endTime - startTime) / 3);

			// Schedule the next tick
			scheduleIteravitveTicks(Duration.millis(delayForNextTick));
			System.out.println("Delay For Last Tick=  " + delay + " Delay For Next Tick " + delayForNextTick + " ms");
		}));
		timeline.play();
	}

	@FXML
	public void pause() {
		timeline.stop();
		run.setDisable(false);
	}

	@FXML
	public void stop() {
		R.cells.loadMap();
		CellsSet.colorMap();
		try {
			timeline.stop();
		} catch (RuntimeException e) {
		}
		run.setDisable(false);
		tick.set(PathsLoader.getStartYear());
		PathsLoader.setCurrentYear(PathsLoader.getStartYear());
		gridPaneLinnChart.getChildren().clear();
		lineChart.clear();
		initilaseChart(lineChart);
		int j = 0, k = 0;
		for (int m = 0; m < lineChart.size(); m++) {
			gridPaneLinnChart.add(lineChart.get(m), j++, k);
			if (j % 3 == 0) {
				k++;
				j = 0;
			}
		}
	}

	void initilaseChart(ArrayList<LineChart<Number, Number>> lineChart) {
		CellsSet.getServicesNames().forEach(name -> {
			Series<Number, Number> s1 = new XYChart.Series<Number, Number>();
			Series<Number, Number> s2 = new XYChart.Series<Number, Number>();
			s1.setName("Demand " + name);
			s2.setName("Supply " + name);
			LineChart<Number, Number> l = new LineChart<>(new NumberAxis(PathsLoader.getStartYear(), PathsLoader.getEndtYear(), 5),
					new NumberAxis());
			l.getData().add(s1);
			l.getData().add(s2);
			LineChartTools.configurexAxis(l, PathsLoader.getStartYear(), PathsLoader.getEndtYear());
			lineChart.add(l);
			LineChartTools.addSeriesTooltips(l);

			String ItemName = "Save as CSV";
			Consumer<String> action = x -> {
				SaveAs.exportLineChartDataToCSV(l);
			};
			HashMap<String, Consumer<String>> othersMenuItems = new HashMap<>();
			othersMenuItems.put(ItemName, action);

			MousePressed.mouseControle(vbox, l, othersMenuItems);
		});
		LineChart<Number, Number> l = new LineChart<>(new NumberAxis(PathsLoader.getStartYear(), PathsLoader.getEndtYear(), 5),
				new NumberAxis());
		lineChart.add(l);

		AFTsLoader.getAftHash().forEach((name, a) -> {
			Series<Number, Number> s = new XYChart.Series<Number, Number>();
			s.setName(name);
			l.getData().add(s);
			s.getNode().lookup(".chart-series-line")
					.setStyle("-fx-stroke: " + ColorsTools.getStringColor(a.getColor()) + ";");
		});
		l.setCreateSymbols(false);
		LineChartTools.addSeriesTooltips(l);
		MousePressed.mouseControle(vbox, l);
		LineChartTools.labelcolor(R.cells, l);
	}

	Alert simulationFolderName() {
		if (!ModelRunner.writeCsvFiles) {
			return null;
		}
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setHeaderText("Please enter OutPut folder name and any comments");
		String competitionType = "Select The most competitive AFT for land competition Percentage: "
				+ (ModelRunner.MostCompetitorAFTProbability * 100) + "% \n"
				+ "Randomly select an AFT for land competition Percentage: "
				+ (100 - ModelRunner.MostCompetitorAFTProbability * 100) + "%";

		String neighbour = ModelRunner.NeighboorEffect
				? "   with probabilty " + (ModelRunner.probabilityOfNeighbor * 100) + "% " + "Neighborhood radius: "
						+ ModelRunner.NeighborRaduis + "\n"
				: "";

		String cofiguration = "Remove negative marginal utility values:   " + ModelRunner.removeNegative + "\n"
				+ "Land abondenmant (Give-up mechanism):  " + ModelRunner.usegiveUp + "\n"
				+ "Land abondenmant percentage: " + (ModelRunner.percentageOfGiveUp * 100) + "\n"
				+ "Averaged Per Cell Residual Demand: " + ModelRunner.isAveragedPerCellResidualDemand + "\n"
				+ "Considering mutation:  " + ModelRunner.isMutated + "\n" + competitionType + "\n"
				+ "Priority given to neighbouring AFTs for land competition: |" + ModelRunner.NeighboorEffect + "| \n"
				+ neighbour + "Percentage of land use that could be changed:  " + (ModelRunner.percentageCells * 100)
				+ "%" + "\n" + "Number of sub-assemblies and residual demand update during the waiting period: "
				+ ModelRunner.nbrOfSubSet + "\n" + "Types of land mask restrictions considered:  "
				+ MaskRestrictionDataLoader.hashMasksPaths.keySet() + "\n \n" + "Add your comments..";

		TextField textField = new TextField();
		textField.setPromptText("RunName");
		Text txt = new Text(PathsLoader.getProjectPath() + File.separator+"output"+File.separator + PathsLoader.getScenario() + File.separator+"...");
		TextArea textArea = new TextArea();
		textArea.setText(cofiguration);
		VBox v = new VBox(txt, textField, textArea);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setContent(v);
		Window window = alert.getDialogPane().getScene().getWindow();
		((Stage) window).setAlwaysOnTop(true);
		alert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(x -> {
			outPutFolderName = textField.getText();
			String dir = PathTools.makeDirectory(PathsLoader.getProjectPath() + File.separator+"output"+File.separator);
			dir = PathTools.makeDirectory(dir + PathsLoader.getScenario());
			dir = PathTools.makeDirectory(dir + File.separator  + outPutFolderName);
			outPutFolderName=dir;
			PathTools.writeFile(outPutFolderName+ File.separator+"readme.txt", textArea.getText(), false);
		});

		return alert;
	}

//	private void popUpRunWindowz() {
//		NewWindow dialog = new NewWindow();
//		if (!dialog.isShowing()) {
//				
//			Button yes= new Button("Yes");
//			Slider CSV_GapS= Tools.slider(1, 30, 15) ;
//			CSV_GapS.setValue(R.writeCsvFilesGap);
//			CSV_GapS.valueProperty().addListener((ov, oldval, newval) -> {
//				R.writeCsvFilesGap = (int) CSV_GapS.getValue();
//			});
//			Button no= new Button("No");
//				yes.setOnAction(e -> {
//					dialog.close();
//					R.writeCsvFiles=true;
//					simulationFolderName();
//				});
//				no.setOnAction(e -> {
//					dialog.close();
//				});
//				
//				VBox vbox= Tools.vBox(
//						Tools.text("save Output into CSV Files", Color.BLUE)
//						,
//						Tools.hBox(yes,CSV_GapS,new Separator(),no)
//						);
//				Group g = new Group(vbox);
//				dialog.creatwindows("Run Configuration", g);
//		}
//	}

}
