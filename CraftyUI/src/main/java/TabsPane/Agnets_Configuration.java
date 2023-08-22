package TabsPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.volante.abm.serialization.CsvTools;

import Main.Main_CraftyFx;
import UtilitiesFx.CSVTableView;
import UtilitiesFx.ColorsTools;
import UtilitiesFx.Path;
import UtilitiesFx.Tools;
import WorldPack.AFT;
import WorldPack.Agents;
import WorldPack.Lattice;
import eu.hansolo.fx.charts.ChartType;
import eu.hansolo.fx.charts.YChart;
import eu.hansolo.fx.charts.YPane;
import eu.hansolo.fx.charts.data.ValueChartItem;
import eu.hansolo.fx.charts.series.YSeries;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;

public class Agnets_Configuration {

	Pane tables;
	VBox vbox = new VBox();
	Lattice M;

	public Agnets_Configuration(Lattice M) {
		this.M = M;
	}

	public Tab pane() {
		ChoiceBox<String> choiceVersion = Tools.chois(Path.version, true);
		ChoiceBox<String> choiceSenario = Tools.chois(Path.scenariosList);
		ArrayList<String> agentslistName=new ArrayList<>();
		Agents.aftReSet.forEach((n,a)->{agentslistName.add(n);});;
		ChoiceBox<String> choiceAgnet = Tools.chois(agentslistName);
		choiceVersion.setOnAction(e -> {
			Path.setversion(choiceVersion.getValue());
		});
		choiceSenario.setOnAction(e -> {
			Path.setSenario(choiceSenario.getValue());
			choiceAgnet.setValue(Path.scenario);
		});

		Agnetconfig(choiceAgnet.getValue(), Agents.aftReSet.values().iterator().next());
		choiceAgnet.setStyle(" -fx-base: #b6e7c9;");

		vbox = Tools.vBox(Tools.hBox(new Text(" Version  = "), choiceVersion, new Text(" Senario  = "), choiceSenario, // regionalisation,
				new Text("  Agnet Type   "), Tools.hBox(choiceAgnet)), tables);

		choiceAgnet.setOnAction(e -> {

			Agents.aftReSet.forEach((name, agent) -> {
				if (name.equals(choiceAgnet.getValue())) {
					Agnetconfig(choiceAgnet.getValue(), agent);
					M.hashCell.forEach((coor, cell) -> {
						if (cell.owner != null)
							if (cell.owner.label.equals(agent.label)) {
								cell.ColorP(agent.color);
							} else {
								cell.ColorP(Color.gray(0.65));
							}

					});
				}
			});

		});
//		 GridPane grid = new GridPane();
//		 grid.add(vbox, 0, 0);
//		 double with=Screen.getPrimary().getBounds().getWidth()/3;
//		 Main_CraftyFx.subScene.setWidth(with*2);
//		 grid.setHgap(100); 
//		 grid.add(Main_CraftyFx.subScene, 1, 0);

		TitledPane titel = new TitledPane("Agents Configuration: ", vbox);
		titel.setStyle(" -fx-base: #d6d9df;");
		// titel.setMaxWidth(500);
		Tab tab = new Tab("Agents Configuration", titel);

		tab.setOnSelectionChanged(e -> {
			Main_CraftyFx.tabPane.setPrefWidth(Main_CraftyFx.sceneWidth * 1.3);
			Main_CraftyFx.tabPane.setMaxWidth(Main_CraftyFx.sceneWidth * 1.3);
			Main_CraftyFx.tabPane.setMinWidth(Main_CraftyFx.sceneWidth * 1.3);
			choiceSenario.setValue(Path.getSenario());
		});
		return tab;
	}

	public void Agnetconfig(String name, AFT agent) {
		vbox.getChildren().remove(tables);
		tables = agentPane(agent);
		vbox.getChildren().add(tables);
	}

	Pane agentPane(AFT agent) {
		VBox vbox = new VBox();
		try {
			String[][] productionMatrix = CsvTools
					.csvReader(Path.fileFilter(agent.label, Path.scenario, "\\production\\").get(0));

			CSVTableView tabV = new CSVTableView(productionMatrix, 25, 2, false);
			tabV.pane = this;

			vbox.getChildren().addAll(Tools.T("Agent Functional Roles", true, tabV, chartBox(agent)),
					Tools.T("Agent Functionality Type Parameters:", true, AgentParametre(agent))//
			);

		} catch (IOException e) {
		}
		return vbox;
	}

	ScrollPane chartBox(AFT agent) {
		String[][] productionMatrix = CsvTools
				.csvReader(Path.fileFilter(agent.label, Path.scenario, "\\production\\").get(0));
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		String[] line0 = productionMatrix[0];// capital
		ScrollPane sp = new ScrollPane();
		int j = 0, k = 0;
		for (int i = 1; i < line0.length - 1; i++) {
			VBox vbox = new VBox();
			vbox.setAlignment(Pos.CENTER);
			Text text = new Text(line0[i != -1 ? i : line0.length - 1]);
			text.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
			text.setFill(Color.BLUE);
			vbox.getChildren().addAll(ychart(agent, i, i != -1 ? 10 : 1), text);
			grid.add(vbox, k, j++);
			if (j % 2 == 0) {
				k++;
				j = 0;
			}

		}
		sp.setContent(grid);
		sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		return sp;
	}

	public YChart<ValueChartItem> ychart(AFT agent, int index, double scale) {
		boolean isProduction = true;// plot one avector with the index vec

		String[][] productionMatrix = CsvTools
				.csvReader(Path.fileFilter(agent.label, Path.scenario, "\\production\\").get(0));
		String[] column1 = Tools.columnFromsMtrix(0, productionMatrix);
		String[] line0 = productionMatrix[0];
		YSeries[] series = new YSeries[line0.length - 2];
		int size = column1.length - 1;

		int nbr = series.length;
		if (isProduction) {
			nbr = 1;
			series = new YSeries[1];
		}
		for (int n = 0; n < nbr; n++) {
			String[] v2;
			if (isProduction) {
				v2 = Tools.columnFromsMtrix(index, productionMatrix);
			} else
				v2 = Tools.columnFromsMtrix(n + 1, productionMatrix);
			double[] v = new double[size];
			for (int i = 0; i < size; i++) {
				v[i] = Tools.sToD(v2[i + 1]);
			}

			List<ValueChartItem> listvalues = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				ValueChartItem dataPoint;
				double y = Math.min(100, v[i] * 100 * scale);// (1 - Math.exp(-v[i])) * 100;
				dataPoint = new ValueChartItem(y, "");
				listvalues.add(dataPoint);
			}

			series[n] = new YSeries<ValueChartItem>(listvalues, ChartType.SMOOTH_RADAR_POLYGON// RADAR_POLYGON//
					, new RadialGradient(0, 0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
							ColorsTools.color(new Random().nextInt(4))),
					Color.TRANSPARENT);

		}
		List<String> categories = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			categories.add(column1[i + 1]);
		}

		YPane<ValueChartItem> yPane = new YPane<ValueChartItem>(series);
		YChart<ValueChartItem> chart = new YChart<ValueChartItem>(yPane);
		return chart;
	}

	Pane AgentParametre(AFT agent) throws IOException {
		String[][] table = CsvTools.csvReader(Path.fileFilter(agent.label, Path.scenario, "\\agents\\").get(0));
		Slider[] parametrSlider = new Slider[table[0].length - 1];
		TextField[] parametrValue = new TextField[parametrSlider.length];
		GridPane grid = new GridPane();

		for (int i = 0; i < parametrValue.length; i++) {
			parametrSlider[i] = Tools.slider(0, 1, Tools.sToD(table[1][i]));
			parametrValue[i] = Tools.textField(5, table[1][i]);
			grid.add(new Text(table[0][i]), 0, i);
			grid.add(parametrSlider[i], 1, i);
			grid.add(parametrValue[i], 2, i);
			int k = i;
			parametrSlider[i].valueProperty().addListener((ov, oldval, newval) -> {
				parametrValue[k].setText("" + parametrSlider[k].getValue());
				table[1][k] = "" + parametrSlider[k].getValue();
			});
			parametrValue[i].setOnKeyPressed(event -> {
				if (event.getCode().equals(KeyCode.ENTER)) {
					parametrSlider[k].setValue(Tools.sToD(parametrValue[k].getText()));
				}
			});
		}
		return grid;
	}

}
