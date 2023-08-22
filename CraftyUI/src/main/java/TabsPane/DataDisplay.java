package TabsPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import UtilitiesFx.ColorsTools;
import UtilitiesFx.CsvTools;
import UtilitiesFx.LineChartTools;
import UtilitiesFx.Path;
import UtilitiesFx.PieChartTools;
import UtilitiesFx.Tools;
import WorldPack.Agents;
import WorldPack.Lattice;
import WorldPack.Cell;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class DataDisplay {

	PieChart chart;
	Pane graphDemand;
	Lattice M;

	public DataDisplay(Lattice M) {
		this.M = M;
	}

	public Tab colorWorld() throws IOException {
		VBox vbox = new VBox();

		ChoiceBox<String> choiceScenario = Tools.chois(Path.scenariosList);
		ArrayList<String> listYears=new ArrayList<>();
		for (int i = Path.startYear; i < Path.endtYear; i++) {
			listYears.add(i+"");
		}
		ChoiceBox<String> years = Tools.chois(listYears);
		LineChart<Number, Number> Ch = new LineChart<>(new NumberAxis(), new NumberAxis());

		int length = Lattice.capitalsName.size() + 1;

		RadioButton[] radioColor = new RadioButton[length + Cell.GISNames.size()];

		Node imageView = image();
		imageView.setVisible(false);
		VBox colorBox = new VBox();
		for (int i = 0; i < Lattice.capitalsName.size(); i++) {
			radioColor[i] = new RadioButton(Lattice.capitalsName.get(i));
			colorBox.getChildren().add(radioColor[i]);
			
			int k = i;
			radioColor[k].setOnAction(e -> {
				for (int j = 0; j < Lattice.capitalsName.size() + Cell.GISNames.size()+1; j++) {
					if (j !=k) {
						if (radioColor[j] != null) {
							radioColor[j].setSelected(false);
							imageView.setVisible(Lattice.capitalsName.contains(radioColor[k].getText()));
							choiceScenario.setDisable(false);
							years.setDisable(false);
						}
					}
				}
				Ch.getData().clear();
				if(!Path.scenario.equalsIgnoreCase("Baseline")) {
				 LineChartTools.Charte(Ch, capital_graph(radioColor[k].getText()));}
				M.colorMap(Lattice.capitalsName.get(k));	
			});
		}
		radioColor[Lattice.capitalsName.size()] = new RadioButton("FR");
		colorBox.getChildren().add(radioColor[Lattice.capitalsName.size()]);
		radioColor[Lattice.capitalsName.size()].setOnAction(e -> {
			for (int j = 0; j < Lattice.capitalsName.size() + Cell.GISNames.size()+1; j++) {
				if (j !=Lattice.capitalsName.size()) {
					if (radioColor[j] != null) {
						radioColor[j].setSelected(false);
						imageView.setVisible(Lattice.capitalsName.contains(radioColor[Lattice.capitalsName.size()].getText()));
						choiceScenario.setDisable(true);
						years.setDisable(true);
					}
				}
			}
			Ch.getData().clear();
			M.colorMap("FR");
			
		});
		
		
		for (int i = 0; i < Cell.GISNames.size(); i++) {
			if (Cell.GISNames.get(i).equals("lad19nm") || Cell.GISNames.get(i).equals("nuts318nm")
					|| Cell.GISNames.get(i).equals("regions")) {
				radioColor[Lattice.capitalsName.size() + 1 + i] = new RadioButton(Cell.GISNames.get(i));
				int k = i + Lattice.capitalsName.size() + 1;
				radioColor[k].setOnAction(e -> {
					for (int j = 0; j < Lattice.capitalsName.size() + 1 + Cell.GISNames.size(); j++) {
						if (k != j) {
							if (radioColor[j] != null) {
								radioColor[j].setSelected(false);
								imageView.setVisible(Lattice.capitalsName.contains(radioColor[k].getText()));
							}
						}
					}
					Ch.getData().clear();
					M.colorMap(Cell.GISNames.get(k - Lattice.capitalsName.size() - 1));
					Cell.regioneselected= Cell.GISNames.get(k - Lattice.capitalsName.size() - 1);
				});
				colorBox.getChildren().add(radioColor[k]);
			}
		}

		graphDemand = graphDemand();

		choiceScenario.setOnAction(e -> {
			Path.setSenario(choiceScenario.getValue());
//			/// only one time to compleat the data
//			try {
//				capitasVectors(choiceScenario.getValue());
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			M.demandTable = CsvTools.csvReader(Path.fileFilter(Path.scenario, "demand").get(0));
			vbox.getChildren().remove(graphDemand);
			graphDemand = graphDemand();
			vbox.getChildren().add(graphDemand);
			
			

			// System.out.println("
			// <-->"+Path.fileFilter(years.getValue(),Path.senario,"\\capitals\\"));
			// years.getSelectionModel().clearSelection();
			try {
				Tools.modifyChoiceBoxList(years, listYears);
			} catch (NullPointerException e2) {
			}
		});

		years.setOnAction(event -> {
			if (years.getValue() != null) {
				try {
					M.updateCapitals((int)Tools.sToD(years.getValue()));
					for (int i = 0; i < length; i++) {
						if (radioColor[i].isSelected()) {
							if (i < Lattice.capitalsName.size()) {
								M.colorMap(Lattice.capitalsName.get(i));
							} else {
								M.colorMap("FR");
							}
						}
					}
				} catch (IOException e2) {
				}
			}
		});

		chart = new PieChart();
		
		updateChartData(M, chart);
//		 GridPane grid = new GridPane();
//		 grid.add(vbox, 0, 0);
//		 double with=Screen.getPrimary().getBounds().getWidth()/3;
//		 Main_CraftyFx.subScene.setWidth(with*2);
//		 grid.setHgap(100); 
//		 grid.add(Main_CraftyFx.subScene, 1, 0);
		 
		vbox.getChildren().addAll(Tools.hBox(new Text(" Scenario = "),choiceScenario, new Text(" Year = "), years),
				new Separator(),
				Tools.T("Visualize spatial data", true,
						Tools.hBox(colorBox, new Separator(), new Separator(), new Separator(), imageView,Ch)),
				chart, graphDemand);

		TitledPane titel = Tools.T("Displays Capitals data: ", true, vbox);

Tab tab =new Tab("Spatial data", titel);
Tools.initialisPane(tab,choiceScenario,.3);

		return tab;
	}
	
	HashMap<String, double[]> capital_graph(String name) {
		 ArrayList<String> paths = Path.fileFilter("Vectors", Path.scenario, "\\capitals\\");
		HashMap<String, String[]> hash= new HashMap<>();
		HashMap<String, double[]> hashD= new HashMap<>();
		if(paths.size()>0) {
		hash = CsvTools.ReadAsaHash(paths.iterator().next());
		
		double[] b= new double [hash.values().iterator().next().length];
		for (int i = 0; i < hash.get(name).length; i++) {
			b[i]=Tools.sToD(hash.get(name)[i]);
		}
		hashD.put(name, b);}
		return hashD;
	}
	
	void capitasVectors(String scenario) throws IOException {
		ArrayList<String> files = Path.fileFilter(scenario, "\\capitals\\");
		String[][] sumVect=new String[files.size()+1][Lattice.capitalsName.size()];
		
		for (int i = 0; i < Lattice.capitalsName.size(); i++) {
			sumVect[0][i]=Lattice.capitalsName.get(i);
		}
		int j=1;
		for (Iterator<String> iterator = files.iterator(); iterator.hasNext();) {
			String file = (String) iterator.next();
			HashMap<String, String[]> hash = CsvTools.ReadAsaHash(file);
			
			for (int i = 0; i < Lattice.capitalsName.size(); i++) {
				String[] capitalvector = hash.get(Lattice.capitalsName.get(i));
				double sum = 0;
				for (int m = 0; m < capitalvector.length; m++) {
				sum += Tools.sToD(capitalvector[m]);
			}
			sumVect[j][i]=sum+"";	
			}
			j++;
		}
		CsvTools.writeCSVfile(sumVect,files.get(0).replace(".csv", "BB.csv"));
	}

	Node image() {
		HBox h = new HBox();
		ImageView imageView = new ImageView(ColorsTools.createColorScaleImage(65, 200, 1));

		Slider slider = Tools.slider(0, 1, 0);
		slider.setMajorTickUnit(0.2f);
		slider.setMaxHeight(200);
		slider.setOrientation(Orientation.VERTICAL);
		imageView.setTranslateX(-50);

		h.getChildren().addAll(slider, imageView);
		return h;
	}

	void updateChartData(Lattice M, PieChart chart) {
		HashMap<String, Double> hashAgentNbr = Agents.hashAgentNbr();
		HashMap<String, Color> color = new HashMap<>();
		

		
		Agents.aftReSet.forEach((name,a) -> {
			color.put(name, a.color);
		});

		new PieChartTools().updateChart(M, hashAgentNbr, color, chart);
		chart.setTitle("Agents Distribution");
		chart.setLegendSide(Side.LEFT);
	}

	Pane graphDemand() {
		
		double[][] vect = new double[M.demandTable.length][M.demandTable[0].length];
		for (int i = 0; i < vect.length; i++) {
			for (int j = 0; j < vect[0].length; j++) {
				vect[i][j] = Tools.sToD(M.demandTable[i][j]);
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
		Pane A = (Pane) a.graph("Demands", hash, X);

		return A;

	}

}
