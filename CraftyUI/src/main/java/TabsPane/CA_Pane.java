package TabsPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.CsvTools;
import UtilitiesFx.LineChartTools;
import UtilitiesFx.Path;
import UtilitiesFx.Tools;
import WorldPack.Agents;
import WorldPack.Lattice;
import WorldPack.Rules;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class CA_Pane {

	Lattice M;
	Rules R;
	Timeline timeline;

	CA_Pane(Lattice M) {
		this.M = M;
		this.R = new Rules(M);
	}


	public Tab pane() {
		Button OneStep = Tools.button("One Step", "472c78");
		Button run = Tools.button("RUN", "472c78");
		Text state = Tools.text(Path.startYear + "", Color.BLUE);
		RadioButton removeNegative = new RadioButton("Remove negative Marginal utility");
		RadioButton mapSynchronisation = new RadioButton("Map Synchronisation");
		RadioButton chartSynchronisation = new RadioButton("Chart Synchronisation");
		RadioButton useGiveUp = new RadioButton("Give Up mechanism");
		AtomicInteger tick = new AtomicInteger(Path.startYear);
		ChoiceBox<String> choiceSenario = Tools.chois(Path.scenariosList);

		choiceSenario.setOnAction(e -> {
			Path.setSenario(choiceSenario.getValue());
			R.demandUpdate();
		});

		ArrayList<LineChart<Number, Number>> lineChart = new ArrayList<>();
		initilaseChart(lineChart);
		Button stopButton = new Button("Pause");
		stopButton.setOnAction(e -> {
			timeline.stop();
		});
		String[][] compositionAFT = new String[Path.endtYear - Path.startYear + 1][Agents.aftReSet.size()];
		String[][] servicedemand = new String[Path.endtYear - Path.startYear + 1][Lattice.servicesNames.size() * 2];

		AtomicInteger L = new AtomicInteger();
		Agents.aftReSet.forEach((label, agents) -> {
			compositionAFT[0][L.getAndIncrement()] = label;
		});
		for (int i = 0; i < Lattice.servicesNames.size(); i++) {
			servicedemand[0][i] = "ServiceSupply:" + Lattice.servicesNames.get(i);
			servicedemand[0][i + Lattice.servicesNames.size()] = "Demand:" + Lattice.servicesNames.get(i);

		}

		run.setOnAction(e -> {
			timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
				OneStep.fire();
			}));
			timeline.setCycleCount(Path.endtYear - Path.startYear);
			timeline.play();
//			timeline.setOnFinished(m -> {
//				CsvTools.writeCSVfile(compositionAFT,
//						Path.projectPath + "\\output\\" + Path.scenario + "\\" + Path.scenario + "-AggregateAFTComposition.csv");
//				CsvTools.writeCSVfile(servicedemand,
//						Path.projectPath + "\\output\\" + Path.scenario + "\\" + Path.scenario + "-AggregateServiceDemand.csv");
//			});

		});
		removeNegative.setOnAction(s -> {
			R.removeNegative = removeNegative.isSelected();
		});
		mapSynchronisation.setOnAction(s -> {
			R.mapSynchronisation = mapSynchronisation.isSelected();
		});
		useGiveUp.setOnAction(s -> {
			R.usegiveUp = useGiveUp.isSelected();
		});


		OneStep.setOnAction(e -> {
			R.go(tick.get());
			state.setText(tick.toString());
			if (chartSynchronisation.isSelected()) {
				AtomicInteger m = new AtomicInteger();
				Lattice.servicesNames.forEach(name -> {
					lineChart.get(m.get()).getData().get(0).getData().add(
							new XYChart.Data<>(tick.get() - Path.startYear, R.demand.get(name + "_" + tick.get())));
					lineChart.get(m.get()).getData().get(1).getData()
							.add(new XYChart.Data<>(tick.get() - Path.startYear, R.supply.get(name)));
					servicedemand[tick.get() - Path.startYear + 1][m.get()] = R.supply.get(name) + "";
					servicedemand[tick.get() - Path.startYear + 1][m.get() + Lattice.servicesNames.size()] = R.demand
							.get(name + "_" + tick.get()) + "";
					m.getAndIncrement();
				});
				HashMap<String, Double> nbr = Agents.hashAgentNbr();
				AtomicInteger N = new AtomicInteger();
				nbr.forEach((name, value) -> {
					lineChart.get(lineChart.size() - 1).getData().get(N.get()).getData()
							.add(new XYChart.Data<>(tick.get() - Path.startYear, value));
					compositionAFT[tick.get() - Path.startYear + 1][N.get()] = value + "";
					N.getAndIncrement();
				});
			}
			tick.getAndIncrement();
		});

		HBox colorRadioHbox = new HBox();
		RadioButton[] radioColor = new RadioButton[Lattice.servicesNames.size() + 1];
		radioColor[0] = new RadioButton("FR");
		for (int i = 0; i < Lattice.servicesNames.size(); i++) {
			radioColor[i + 1] = new RadioButton(Lattice.servicesNames.get(i));
		}
		colorRadioHbox.getChildren().addAll(radioColor);
		for (int i = 0; i < radioColor.length; i++) {
			int m = i;
			radioColor[i].setOnAction(e -> {
				R.colorDisplay = radioColor[m].getText();
				M.colorMap(radioColor[m].getText());
				for (int I = 0; I < radioColor.length; I++) {
					if (I != m) {
						radioColor[I].setSelected(false);
					}
				}
			});
		}
		VBox vbox = new VBox();
		vbox.getChildren().addAll(Tools.hBox(choiceSenario, new Separator(), state, colorRadioHbox),
				Tools.hBox(OneStep, new Separator(), run, stopButton,useGiveUp, removeNegative, mapSynchronisation,chartSynchronisation));
		GridPane gridPane = LineChartTools.gridCahrt(3, lineChart);
		TitledPane titel = Tools.T("Simulation Monitoring: ", true, vbox, new ScrollPane(gridPane));
		Tab caPane = new Tab("New Perspective", titel);
		Tools.initialisPane(caPane, choiceSenario, 0.6);

		return caPane;
	}

	void initilaseChart(ArrayList<LineChart<Number, Number>> lineChart) {
		Lattice.servicesNames.forEach(name -> {
			Series<Number, Number> s1 = new XYChart.Series<Number, Number>();
			Series<Number, Number> s2 = new XYChart.Series<Number, Number>();
			s1.setName("Demand " + name);
			s2.setName("Supply " + name);
			LineChart<Number, Number> l = new LineChart<>(new NumberAxis(), new NumberAxis());
			l.getData().add(s1);
			l.getData().add(s2);
			lineChart.add(l);
			LineChartTools.mouseControle(l, "Demand & Supply");
		});
		LineChart<Number, Number> l = new LineChart<>(new NumberAxis(), new NumberAxis());
		lineChart.add(l);
		Agents.aftReSet.forEach((name, agnet) -> {
			Series<Number, Number> s = new XYChart.Series<Number, Number>();
			s.setName(name.toUpperCase());
			l.getData().add(s);
			LineChartTools.mouseControle(l, "Agents distribution");
		});
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
			if (name.contains("aft:")) {
				has.put(name.replace("aft:", "").toUpperCase(), tmp);
			}
		});
		return has;
	}

}
