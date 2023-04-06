package TabsPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import UtilitiesFx.CSVTableView;
import UtilitiesFx.ColorsTools;
import UtilitiesFx.Path;
import UtilitiesFx.Tools;
import WorldPack.AgentFX;
import WorldPack.Agents;
import eu.hansolo.fx.charts.ChartType;
import eu.hansolo.fx.charts.YChart;
import eu.hansolo.fx.charts.YPane;
import eu.hansolo.fx.charts.data.ValueChartItem;
import eu.hansolo.fx.charts.series.YSeries;
import Main.Main_CraftyFx;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
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
	Agents agents;

	public Agnets_Configuration(Agents agents) {
		this.agents = agents;
	}

	public TitledPane pane() {

		ChoiceBox<String> choiceVersion = Tools.chois(Path.version, true);
		ChoiceBox<String> choiceSenario = Tools.chois(Path.senariosList);
		ChoiceBox<String> choiceAgnet = Tools.chois(Path.nameOfFile("\\production\\", Path.senario));
		choiceSenario.setValue(Path.senario);

		choiceVersion.setOnAction(e -> {
			Path.setversion(choiceVersion.getValue());
		});

		choiceSenario.setOnAction(e -> {
			Path.setSenario(choiceSenario.getValue());
			choiceAgnet.setValue(Path.nameOfFile("\\production\\", Path.senario).get(0));
		});

		Agnetconfig(choiceAgnet.getValue(), agents.AFT.get(0));
		choiceAgnet.setStyle(" -fx-base: #b6e7c9;");

		vbox = Tools.vBox(Tools.hBox(new Text(" Version  = "), choiceVersion, new Text(" Senario  = "), choiceSenario, // regionalisation,
				new Text("  Agnet Type   "), Tools.hBox(choiceAgnet)), tables);

		TitledPane titel = new TitledPane("Agents Configuration: ", vbox);
		titel.setStyle(" -fx-base: #d6d9df;");
		// titel.setMaxWidth(500);

		choiceAgnet.setOnAction(e -> {

			agents.AFT.forEach(agent -> {
				if (agent.label.equals(choiceAgnet.getValue())) {
					Agnetconfig(choiceAgnet.getValue(), agent);
				}
			});

		});
		titel.setMaxWidth(Main_CraftyFx.sceneWidth);// Screen.getPrimary().getBounds().getWidth()
		titel.setMinWidth(Main_CraftyFx.sceneWidth);
		titel.setMaxHeight(Screen.getPrimary().getBounds().getHeight());
		titel.setMinHeight(Screen.getPrimary().getBounds().getHeight());

		return titel;
	}

	public void Agnetconfig(String name, AgentFX agentFX) {
		vbox.getChildren().remove(tables);
		tables = agentPane(agentFX);
		vbox.getChildren().add(tables);
	}

	Pane agentPane(AgentFX agentfx) {
		VBox vbox = new VBox();
		try {
			agents.AgnetsDataImport();
			CSVTableView tabV = new CSVTableView(agentfx.productionMatrix, 20, 1, true);
			tabV.pane = this;

			vbox.getChildren().addAll(Tools.T("Agent Functional Roles", true, tabV, Tools.hBox(chartBox(agentfx))),
					Tools.T("Agent Functionality Type Parameters:", true, AgentParametre(agentfx)));

		} catch (IOException e) {
		}
		return vbox;
	}

	ScrollPane chartBox(AgentFX agentfx) {

		HBox h = new HBox();
		String[] line0 = agentfx.productionMatrix[0];
		ScrollPane sp = new ScrollPane();
		for (int i = -1; i < line0.length - 1; i++) {
			if (i != 0) {
				VBox vbox = new VBox();
				vbox.setAlignment(Pos.CENTER);
				Text text = new Text(line0[i != -1 ? i : line0.length - 1]);
				text.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
				text.setFill(Color.BLUE);
			    vbox.getChildren().addAll(ychart(agentfx, i, i != -1 ? 10 : 1), text);
				h.getChildren().add(vbox);
			}
		}
		sp.setContent(h);
		sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		return sp;
	}

	public YChart<ValueChartItem> ychart(AgentFX agentfx, int index, double scale) {
		boolean isProduction = true;// plot one avector with the index vec
		String[] column1 = Tools.columnFromsMtrix(0, agentfx.productionMatrix);
		String[] line0 = agentfx.productionMatrix[0];
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
				v2 = Tools.columnFromsMtrix(index, agentfx.productionMatrix);
			} else
				v2 = Tools.columnFromsMtrix(n + 1, agentfx.productionMatrix);
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

	Pane AgentParametre(AgentFX agentfx) throws IOException {
		String[][] table = agentfx.aftParamIdTable;
		Slider[] parametrSlider = new Slider[table[0].length - 1];
		TextField[] parametrValue = new TextField[parametrSlider.length];
		HBox[][] h = new HBox[3][parametrSlider.length];

		for (int i = 0; i < parametrValue.length; i++) {
			parametrSlider[i] = Tools.slider(0, 1, Tools.sToD(table[1][i]));
			parametrValue[i] = Tools.textField(5, table[1][i]);
			h[0][i] = Tools.hBox(28, new Text(table[0][i]));
			h[1][i] = Tools.hBox(28, parametrSlider[i]);
			h[2][i] = Tools.hBox(28, parametrValue[i]);
			int k = i;
			parametrSlider[i].valueProperty().addListener((ov, oldval, newval) -> {
				parametrValue[k].setText("" + parametrSlider[k].getValue());
				agentfx.aftParamIdTable[1][k] = "" + parametrSlider[k].getValue();
			});
			parametrValue[i].setOnKeyPressed(event -> {
				if (event.getCode().equals(KeyCode.ENTER)) {
					parametrSlider[k].setValue(Tools.sToD(parametrValue[k].getText()));
				}
			});
		}
		return Tools.vBox(Tools.hBox(Tools.vBox(h[0]), Tools.vBox(h[1]), Tools.vBox(h[2])));
	}

}
