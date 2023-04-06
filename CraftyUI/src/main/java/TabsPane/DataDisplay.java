package TabsPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import Main.Main_CraftyFx;
import UtilitiesFx.ColorsTools;
import UtilitiesFx.CsvTools;
import UtilitiesFx.LineChartTools;
import UtilitiesFx.Path;
import UtilitiesFx.PieChartTools;
import UtilitiesFx.Tools;
import WorldPack.Map;
import WorldPack.Patch;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;

public class DataDisplay {

	PieChart chart;
	Pane graphDemand;
	Map M;

	public DataDisplay(Map M) {
	this.M=M;
	}

	public TitledPane colorWorld() {
		VBox vbox = new VBox();
		
		ChoiceBox<String> choiceScenario = Tools.chois(Path.senariosList);
		choiceScenario.setValue(Path.senario);
		ArrayList<String> listYears = Path.nameOfFile("\\capitals\\",Path.senario);
		ChoiceBox<String> years = Tools.chois(listYears);
		years.setValue(listYears.get(0));
			
		
		int length = Patch.capitalsName.size()+3;

		RadioButton[] radioColor = new RadioButton[length];

		
		Node imageView = image();
		imageView.setVisible(false);
		VBox colorBox = new VBox();
		for (int i = 0; i < length; i++) {
			if (i < Patch.capitalsName.size()) {
				radioColor[i] = new RadioButton(Patch.capitalsName.get(i));
			} else if (i == Patch.capitalsName.size()) {
				radioColor[i] = new RadioButton("FR");
			} else if (i == Patch.capitalsName.size()+1) {
				radioColor[i] = new RadioButton("LAD19NM");
			} else if (i == Patch.capitalsName.size()+2) {
				radioColor[i] = new RadioButton("nuts318nm");
			}
			colorBox.getChildren().add(radioColor[i]);
			int k = i;
			radioColor[i].setOnAction(e -> {
				for (int j = 0; j < length; j++) {
					if (k != j) {
						radioColor[j].setSelected(false);
						imageView.setVisible(Patch.capitalsName.contains(radioColor[k].getText()));
						}
					}
						if (k < Patch.capitalsName.size()) {
							M.colorMap(Patch.capitalsName.get(k));
						} else if (k == Patch.capitalsName.size()) {
							M.colorMap("FR");
						}else if (k == Patch.capitalsName.size()+1) {
														M.colorMap("LAD19NM");
						}else if (k == Patch.capitalsName.size()+2) {
							M.colorMap("nuts318nm");
						}
			});
		}
		
		 graphDemand = graphDemand();
		
		choiceScenario.setOnAction(e -> {
			Path.setSenario(choiceScenario.getValue());
			M.demandTable = CsvTools.csvReader(Path.fileFilter(Path.senario, "demand").get(0));
			vbox.getChildren().remove(graphDemand);
			graphDemand = graphDemand();
			vbox.getChildren().add(graphDemand);
			ArrayList<String>  newlistYears=Path.nameOfFile("\\capitals\\",Path.senario);
			listYears.clear();
			listYears.addAll(newlistYears);
			years.getSelectionModel().clearSelection();
			years.setValue(newlistYears.get(0));
		});
		
		years.setOnAction(e -> {
			try {				
				M.RCPi_SSPi(Path.fileFilter(years.getValue()).get(0));
				for (int i = 3; i < length; i++) {
					if (radioColor[i].isSelected()) {
						if(i<Patch.capitalsName.size()) {
							M.colorMap(Patch.capitalsName.get(i));}
							else {
								M.colorMap("FR");
							}
					}
				}
			} catch (IOException e2) {
			}
		});
		



		chart = new PieChart();
		
		updateChartData(M, chart);

		

	

	
		
		vbox.getChildren().addAll(
				Tools.hBox(new Text(" Scenario = "), choiceScenario ,new Text(" Year = "),years), new Separator(),
				Tools.T("Visualize spatial data", true,
						Tools.hBox(colorBox, new Separator(), new Separator(), new Separator(),  imageView)),
				chart, graphDemand);
		
		TitledPane titel = Tools.T("Displays Capitals data: ", true,vbox);
		
		titel.setStyle(" -fx-base: #d6d9df;");
		titel.setMaxWidth(Main_CraftyFx.sceneWidth);
		titel.setMinWidth(Main_CraftyFx.sceneWidth);
		titel.setMaxHeight(Screen.getPrimary().getBounds().getHeight());
		titel.setMinHeight(Screen.getPrimary().getBounds().getHeight());
		return titel;
	}

	Node image() {
		HBox h = new HBox();
		ImageView imageView = new ImageView(ColorsTools.createColorScaleImage(65, 200, 1));

		Slider slider = Tools.slider(0, 1, 0);
		slider.setMajorTickUnit(0.2f);
		slider.setMaxHeight(200);
		slider.setOrientation(Orientation.VERTICAL);
		imageView.setTranslateX(-90);
		
		h.getChildren().addAll(slider, imageView);
		return h;
	}




	void updateChartData(Map M, PieChart chart) {
		HashMap<String, Double> hash = new HashMap<>();
		HashMap<String, Color> color = new HashMap<>();
		M.agents.AFT.forEach((a) -> {
			hash.put(a.label, a.Mypaches.size() * 1.);
			color.put(a.label, a.color);
		});

		new PieChartTools().updateChart(M,hash, color, chart);
		chart.setTitle("Agents Distribution");
		chart.setLegendSide(Side.LEFT);
	}

	Pane graphDemand() {
		double[][] vect = new double [M.demandTable.length][M.demandTable[0].length];
		for(int i=0;i<vect.length;i++) {
			for(int j=0;j<vect[0].length;j++) {
				vect[i][j]=Tools.sToD(M.demandTable[i][j]);
			}
		}
	
		String[] lis = M.demandTable[0];

		HashMap<String, Number[]> hash = new HashMap<>();

		for (int i = 2; i < lis.length; i++) {
			Number[] v = new Number[vect.length - 1];
			for (int j = 0; j < vect.length - 1; j++) {
				v[j] = vect[j + 1][i];
			}
			hash.put(lis[i], v);
		}
		LineChartTools a = new LineChartTools();
		NumberAxis X = new NumberAxis(Path.startYear, Path.endtYear, 10);
		Pane A = (Pane) a.graph("Demands",hash,X);

		return A;

	}

}
