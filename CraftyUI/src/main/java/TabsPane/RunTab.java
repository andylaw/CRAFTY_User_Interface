package TabsPane;

import org.volante.abm.serialization.ModelRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.CsvTools;
import UtilitiesFx.LineChartTools;
import UtilitiesFx.Path;
import UtilitiesFx.Tools;
import WorldPack.Agents;
import WorldPack.Lattice;
import WorldPack.Cell;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.layout.GridPane;

public class RunTab {
	Lattice M;

	RunTab(Lattice M) {
		this.M = M;
	}

	ModelRunner C = new ModelRunner();

	public TitledPane pane() {
		VBox vbox = new VBox();
		// ChoiceBox<String> choiceVersion = Tools.chois(Path.version, true);
		ChoiceBox<String> choiceSenario = Tools.chois(Path.scenariosList);
		choiceSenario.setValue(Path.scenario);
		Button setup = Tools.button("Setup", "472c78");
		Button OneStep = Tools.button("One Step", "472c78");
		Button run = Tools.button("RUN", "472c78");
		Text state = Tools.text(Path.startYear + "", Color.BLUE);

		setup.setOnAction(e -> {
//AtomicInteger i = new AtomicInteger(Path.startYear);
//			Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
////				R.go(i.get());
//				System.out.println(i);
//
//				i.getAndIncrement();
//			}));
//			
//			timeline.setCycleCount(70);
//			timeline.play();
			 try {
				ModelRunner.main( null) ;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		AtomicInteger tick = new AtomicInteger(Path.startYear);
		GridPane gridPane = new GridPane();
		OneStep.setOnAction(e -> {
			Tools.showWaitingDialog(x -> {
				try {
					C.DORUN(tick.getAndIncrement(), tick.get());
					state.setText(tick.toString());
				} catch (Exception e1) {
				}
			});
		});

		run.setOnAction(e -> {
			//Graphs(gridPane, true);
			AtomicInteger j = new AtomicInteger(Path.startYear);
			Timeline timeline = new Timeline();
			timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000), event -> {
				try {
					servicesAndOwner(j.getAndIncrement());
					M.colorMap("FR");
				} catch (IOException e1) {
				}
				state.setText(j.getAndIncrement() + "");
			}));

			timeline.setCycleCount(80);
			timeline.play();

		});

		// OutPutter outPut = new OutPutter();

		vbox.getChildren().addAll(Tools.hBox(choiceSenario), Tools.hBox(setup, OneStep, new Separator(), run, state),
				gridPane
		// ,outPut.paneOutPut()
		);

		TitledPane titel = Tools.T("Simulation Monitoring: ", true, vbox);

		titel.setStyle(" -fx-base: #d6d9df;");
		return titel;
	}

	public void servicesAndOwner(int year) throws IOException {
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
			Agents.aftReSet.forEach((name,agent)-> {
				if (name.equals(hash.get("agent")[ii])) {
					p.owner = agent;
					//a.Mypaches.add(p);
				}
			});
		}
	}





}
