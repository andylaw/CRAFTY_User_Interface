package panes;

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
import dataLoader.MapLoader;
import dataLoader.Paths;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import main.OpenTabs;
import model.Lattice;
import model.Rules;

/**
 * @author Mohamed Byari
 *
 */

public class RunPane {

	Rules R;
	Timeline timeline;
	String outPutFolderName;
	VBox vbox = new VBox();
	public static boolean chartSynchronisation = true;

	public RunPane(MapLoader M) {
		this.R = new Rules(M);
	}

	public Tab pane() {
		Button runConfigurtion = Tools.button("Run Configuration", "b6e7c9");
		Button OneStep = new Button("One Step");
		Button run = Tools.button("RUN", "b6e7c9");
		Button stop = new Button("Stop & Reset");
		Button pause = new Button("Pause");
		Text tickTxt = Tools.text(Paths.getStartYear() + "", Color.BLUE);
		AtomicInteger tick = new AtomicInteger(Paths.getStartYear());
		ArrayList<LineChart<Number, Number>> lineChart = new ArrayList<>();
		outPutFolderName = Paths.getScenario();
		NewWindow runConfiguration = new NewWindow();
		GridPane gridPane = new GridPane();

		stop.setOnAction(e -> {
			R.M.ResetMap();
			Lattice.colorMap();
			timeline.stop();
			run.setDisable(false);
			tick.set(Paths.getStartYear());
			Paths.setCurrentYear(Paths.getStartYear());
			gridPane.getChildren().clear();
			lineChart.clear();
			initilaseChart(lineChart);
			int j = 0, k = 0;
			for (int m = 0; m < lineChart.size(); m++) {
				gridPane.add(lineChart.get(m), j++, k);
				if (j % 3 == 0) {
					k++;
					j = 0;
				}
			}
		});
		pause.setOnAction(e -> {
			timeline.stop();
			run.setDisable(false);
		});

		initilaseChart(lineChart);

		runConfigurtion.setOnAction(e -> {
			if (!runConfiguration.isShowing())
				RunConfiguration.runConfiguration(this, runConfiguration);
		});

		run.setOnAction(e -> {
			run.setDisable(true);
			RunName();
			MapLoader.updateDemand();// to resolve (update demand when you change scenarion+ demand shouldn't be a rule variable but lattice )
			timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
				OneStep.fire();
			}));
			timeline.setCycleCount(Paths.getEndtYear() - Paths.getStartYear());
			timeline.play();
			timeline.setOnFinished(m -> {
				CsvTools.writeCSVfile(R.compositionAFT, Paths.getProjectPath() + "\\output\\" + Paths.getScenario() + "\\"
						+ outPutFolderName + "\\" + Paths.getScenario() + "-AggregateAFTComposition.csv");
				CsvTools.writeCSVfile(R.servicedemand, Paths.getProjectPath() + "\\output\\" + Paths.getScenario() + "\\"
						+ outPutFolderName + "\\" + Paths.getScenario() + "-AggregateServiceDemand.csv");
			});
		});

		OneStep.setOnAction(e -> {
			R.go(tick.get(), outPutFolderName);
			tickTxt.setText(tick.toString());
			Paths.setCurrentYear(tick.get());
			if (chartSynchronisation) {
				AtomicInteger m = new AtomicInteger();
				Lattice.getServicesNames().forEach(name -> {
					lineChart.get(m.get()).getData().get(0).getData().add(
							new XYChart.Data<>(tick.get() - Paths.getStartYear(), Lattice.getDemand().get(name)[tick.get()-Paths.getStartYear() ]));
					lineChart.get(m.get()).getData().get(1).getData()
							.add(new XYChart.Data<>(tick.get() - Paths.getStartYear(), R.supply.get(name)));
					m.getAndIncrement();
				});
				HashMap<String, Double> AgentNbr = AFTsLoader.hashAgentNbr();
				AtomicInteger N = new AtomicInteger();
				AgentNbr.forEach((name, value) -> {
					lineChart.get(lineChart.size() - 1).getData().get(N.get()).getData()
							.add(new XYChart.Data<>(tick.get() - Paths.getStartYear(), value));
					N.getAndIncrement();
				});
			}
			tick.getAndIncrement();
		});

		HBox colorRadioHbox = new HBox();
		RadioButton[] radioColor = new RadioButton[Lattice.getServicesNames().size() + 1];
		radioColor[radioColor.length - 1] = new RadioButton("FR");
		for (int i = 0; i < Lattice.getServicesNames().size(); i++) {
			radioColor[i] = new RadioButton(Lattice.getServicesNames().get(i));
		}
		colorRadioHbox.getChildren().addAll(radioColor);
		for (int i = 0; i < radioColor.length; i++) {
			int m = i;
			radioColor[i].setOnAction(e -> {
				R.colorDisplay = radioColor[m].getText();
				Lattice.colorMap(radioColor[m].getText());
				for (int I = 0; I < radioColor.length; I++) {
					if (I != m) {
						radioColor[I].setSelected(false);
					}
				}
			});
		}
		
		vbox.getChildren().addAll(Tools.hBox( new Separator(), tickTxt, colorRadioHbox), new Separator(),
				Tools.hBox(runConfigurtion, OneStep, new Separator(), run, pause, stop));

		int j = 0, k = 0;
		for (int m = 0; m < lineChart.size(); m++) {
			gridPane.add(Tools.hBox(lineChart.get(m)), j++, k);
			if (j % 3 == 0) {
				k++;
				j = 0;
			}
		}
		vbox.getChildren().addAll(gridPane);
		Tab tab = new Tab("Run ", vbox);
		tab.setOnSelectionChanged(e -> {
				Paths.setCurrentYear(Paths.getStartYear());
				R.M.ResetMap();
				OpenTabs.choiceScenario.setDisable(false);
				OpenTabs.year.setDisable(false);
		});
		return tab;
	}

	void initilaseChart(ArrayList<LineChart<Number, Number>> lineChart) {
		Lattice.getServicesNames().forEach(name -> {
			Series<Number, Number> s1 = new XYChart.Series<Number, Number>();
			Series<Number, Number> s2 = new XYChart.Series<Number, Number>();
			s1.setName("Demand " + name);
			s2.setName("Supply " + name);
			LineChart<Number, Number> l = new LineChart<>(new NumberAxis(), new NumberAxis());
			l.getData().add(s1);
			l.getData().add(s2);
			lineChart.add(l);
			MousePressed.mouseControle(vbox,l);
		});
		LineChart<Number, Number> l = new LineChart<>(new NumberAxis(), new NumberAxis());
		lineChart.add(l);
		AFTsLoader.aftReSet.forEach((name, a) -> {
			Series<Number, Number> s = new XYChart.Series<Number, Number>();
			s.setName(name);
			l.getData().add(s);
			s.getNode().lookup(".chart-series-line")
					.setStyle("-fx-stroke: " + ColorsTools.getStringColor(a.getColor()) + ";");
		});
		l.setCreateSymbols(false);
		MousePressed.mouseControle(vbox,l);
		LineChartTools.labelcolor(l);
	}

	Alert RunName() {
		if (!R.writeCsvFiles) {
			return null;
		}
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setHeaderText("Please enter OutPut folder name and any comments");

		TextField textField = new TextField();
		textField.setPromptText("RunName");
		Text txt = new Text(Paths.getProjectPath() + "\\output\\" + Paths.getScenario() + "\\...");
		TextArea textArea = new TextArea();
		textArea.setPromptText("Add comments");
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
			PathTools.writeFile(
					Paths.getProjectPath() + "\\output\\" + Paths.getScenario() + "\\" + outPutFolderName + "\\readme.txt",
					textArea.getText(),false);
		});

		return alert;
	}
}
