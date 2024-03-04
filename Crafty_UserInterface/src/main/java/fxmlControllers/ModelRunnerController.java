package fxmlControllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.MaskRestrictionDataLoader;
import dataLoader.Paths;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
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
	Timeline timeline = new Timeline();
	AtomicInteger tick;
	ArrayList<LineChart<Number, Number>> lineChart;
	NewWindow runConfiguration;
	public static boolean chartSynchronisation = true;
	double KeyFramelag = 1;
//	private final long desiredTickMillis = 1000;
	RadioButton[] radioColor;
	NewWindow colorbox = new NewWindow();
	
	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		M = TabPaneController.M;
		R = new ModelRunner(M);
		tick = new AtomicInteger(Paths.getStartYear());
		tickTxt.setText(tick.toString());

		lineChart = new ArrayList<>();
		outPutFolderName = Paths.getScenario();
		runConfiguration = new NewWindow();
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
	public void configuration() {

		RunCofigController.CA = this;
		if (!runConfiguration.isShowing()) {
			try {
				Group g = new Group();
				g.getChildren().add(FXMLLoader.load(getClass().getResource("/fxmlControllers/RunCofig.fxml")));
				runConfiguration.creatwindows("Run Configuration", g);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@FXML
	public void oneStep() {
		Paths.setCurrentYear(tick.get());
		R.run();
		tickTxt.setText(tick.toString());

		if (chartSynchronisation) {
			AtomicInteger m = new AtomicInteger();
			CellsSet.getServicesNames().forEach(name -> {
				lineChart.get(m.get()).getData().get(0).getData().add(new XYChart.Data<>(tick.get(),
						CellsSet.getDemand().get(name)[tick.get() - Paths.getStartYear()]));
				lineChart.get(m.get()).getData().get(1).getData()
						.add(new XYChart.Data<>(tick.get(), R.supply.get(name)));
				m.getAndIncrement();
			});
			HashMap<String, Integer> AgentNbr = AFTsLoader.hashAgentNbr();
			AtomicInteger N = new AtomicInteger();
			AgentNbr.forEach((name, value) -> {
				lineChart.get(lineChart.size() - 1).getData().get(N.get()).getData()
						.add(new XYChart.Data<>(tick.get(), value));
				N.getAndIncrement();
			});
		}
		tick.getAndIncrement();

	}

	@FXML
	public void run() {
		run.setDisable(true);
		simulationFolderName();
		CellsLoader.updateDemand();
		scheduleIteravitveTicks(Duration.millis(1000));

	}

	private void scheduleIteravitveTicks(Duration delay) {

		if (Paths.getCurrentYear() >= Paths.getEndtYear()) {
			// Stop if max iterations reached
			if (R.writeCsvFiles) {
				CsvTools.writeCSVfile(R.compositionAFT, Paths.getProjectPath() + "\\output\\" + Paths.getScenario()
						+ "\\" + outPutFolderName + "\\" + Paths.getScenario() + "-AggregateAFTComposition.csv");
				CsvTools.writeCSVfile(R.servicedemand, Paths.getProjectPath() + "\\output\\" + Paths.getScenario()
						+ "\\" + outPutFolderName + "\\" + Paths.getScenario() + "-AggregateServiceDemand.csv");
			}
			return;
		}
		// Stop the old timeline if it's running
		if (timeline != null) {
			timeline.stop();
		}
		// Create a new timeline for the next tick
		timeline = new Timeline(new KeyFrame(delay, event -> {
			long startTime = System.currentTimeMillis();
			// Perform the simulation update
			oneStep();
			long endTime = System.currentTimeMillis();
			// Calculate the delay for the next tick to maintain the rhythm
			long delayForNextTick = Math.max(300, endTime - startTime);

			// Schedule the next tick
			scheduleIteravitveTicks(Duration.millis(delayForNextTick / 2));
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
		tick.set(Paths.getStartYear());
		Paths.setCurrentYear(Paths.getStartYear());
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
			LineChart<Number, Number> l = new LineChart<>(new NumberAxis(Paths.getStartYear(), Paths.getEndtYear(), 5),
					new NumberAxis());
			l.getData().add(s1);
			l.getData().add(s2);
			lineChart.add(l);
			MousePressed.mouseControle(vbox, l);
		});
		LineChart<Number, Number> l = new LineChart<>(new NumberAxis(Paths.getStartYear(), Paths.getEndtYear(), 5),
				new NumberAxis());
		lineChart.add(l);

		R.cells.AFtsSet.getAftHash().forEach((name, a) -> {
			Series<Number, Number> s = new XYChart.Series<Number, Number>();
			s.setName(name);
			l.getData().add(s);
			s.getNode().lookup(".chart-series-line")
					.setStyle("-fx-stroke: " + ColorsTools.getStringColor(a.getColor()) + ";");
		});
		l.setCreateSymbols(false);
		MousePressed.mouseControle(vbox, l);
		new LineChartTools().labelcolor(R.cells, l);
	}

	Alert simulationFolderName() {
		if (!R.writeCsvFiles) {
			return null;
		}
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setHeaderText("Please enter OutPut folder name and any comments");

		String cofiguration = "Remove negative marginal utility values =   " + R.removeNegative + "\n"
				+ "Land abondenmant (Give-up mechanism) =  " + R.usegiveUp + "\n" + "Considering mutation =  "
				+ R.isMutated + "\n" + "Percentage of land use that could be changed =  " +(int) (R.percentageCells * 100)+"%"
				+ "\n" + "Types of land mask restrictions considered =  " +MaskRestrictionDataLoader.ListOfMask.keySet()
				+ "\n \n" + "Add your comments..";

		TextField textField = new TextField();
		textField.setPromptText("RunName");
		Text txt = new Text(Paths.getProjectPath() + "\\output\\" + Paths.getScenario() + "\\...");
		TextArea textArea = new TextArea();
		textArea.setText(cofiguration);
		VBox v = new VBox(txt, textField, textArea);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setContent(v);
		Window window = alert.getDialogPane().getScene().getWindow();
		((Stage) window).setAlwaysOnTop(true);
		alert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(x -> {
			outPutFolderName = textField.getText();
			String dir = PathTools.makeDirectory(Paths.getProjectPath() + "\\output\\");
			dir = PathTools.makeDirectory(dir + Paths.getScenario());
			dir = PathTools.makeDirectory(dir + "\\" + outPutFolderName);
			PathTools.writeFile(Paths.getProjectPath() + "\\output\\" + Paths.getScenario() + "\\" + outPutFolderName
					+ "\\readme.txt", textArea.getText(), false);
		});

		return alert;
	}

}
