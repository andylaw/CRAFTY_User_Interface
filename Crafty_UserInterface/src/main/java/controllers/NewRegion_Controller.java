package controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import dataLoader.CellsLoader;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.PieChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Cell;
import model.CellsSet;
import utils.cameraTools.Camera;
import utils.graphicalTools.CSVTableView;
import utils.graphicalTools.ColorsTools;
import utils.graphicalTools.NewWindow;
import utils.graphicalTools.PieChartTools;
import utils.graphicalTools.Tools;
import javafx.scene.layout.BorderPane;

/**
 * @author Mohamed Byari
 *
 */

public class NewRegion_Controller {
	
	static Canvas canvas;

	public static Set<Cell> patchsInRergion = new HashSet<>();
	public static Set<Rectangle> patchCopy = new HashSet<>();
	static HashMap<Rectangle, Cell> rectToPach = new HashMap<>();
	
	static NewWindow window = new NewWindow();
	static Group root = new Group();
	static SubScene subScene;
	static Camera camera = new Camera();
	static Scene scene;
	static BorderPane rootPane = new BorderPane();

	static GraphicsContext gc;
	
	public static void RegionWindow() {

		subScene = new NewWindow().subSceneWithCamera(rootPane, root);
		TitledPane pane = colorWorld();

		window.creatwindows("CRAFTY Regional Analyse", 0.8, 0.9, rootPane);
		CreateRegionMap(root);
		HBox contentBox = new HBox();
        contentBox.getChildren().addAll(pane, subScene);
        rootPane.setCenter(contentBox);
        BorderPane.setAlignment(contentBox, Pos.CENTER);
        BorderPane.setMargin(contentBox, new javafx.geometry.Insets(1));

	}

	static void CreateRegionMap(Group g) {
		g.getChildren().clear();

		canvas = new Canvas(CellsSet.getMaxX()*Cell.getSize(), CellsSet.getMaxY()*Cell.getSize());
        gc = canvas.getGraphicsContext2D();
		patchsInRergion.forEach(c -> {
			if(c.getOwner()!=null) {
			c.ColorP(c.getOwner().getColor());
			}
		});
		g.getChildren().add(canvas);
	}




	public static void colorMap(String name) {
		if (name.equals("FR")) {
			patchsInRergion.forEach(p -> {
				if (p.getOwner() != null) {
					p.ColorP(p.getOwner().getColor());
				}
			});
		} else {
			List<Double> values = new ArrayList<>();
			patchsInRergion.forEach(c -> {
				if (c.getCapitals().get(name) != null)
					values.add(c.getCapitals().get(name));
			});

			double max = Collections.max(values);

			patchsInRergion.forEach(c -> {
				if (c.getCapitals().get(name) != null)
				c.ColorP(ColorsTools.getColorForValue(max, c.getCapitals().get(name)));
			});
		}
	}
	




	public static TitledPane colorWorld() {
		VBox vbox = new VBox();

		int length = CellsLoader.getCapitalsList().size() + 1;

		RadioButton[] radioColor = new RadioButton[length];

	

		for (int i = 0; i < length; i++) {
			if (i < CellsLoader.getCapitalsList().size()) {
				radioColor[i] = new RadioButton(CellsLoader.getCapitalsList().get(i));
			} else if (i == CellsLoader.getCapitalsList().size()) {
				radioColor[i] = new RadioButton("FR");
			} 
			vbox.getChildren().add(radioColor[i]);
			int k = i;
			radioColor[i].setOnAction(e -> {
				for (int j = 0; j < length; j++) {
					if (k != j) {
						radioColor[j].setSelected(false);
					}
				}
				if (k < CellsLoader.getCapitalsList().size()) {
					colorMap(CellsLoader.getCapitalsList().get(k));
				} else if (k == CellsLoader.getCapitalsList().size()) {
					colorMap("FR");
				} 
			});

		}

		TitledPane titel = Tools.T("Displays Capitals data: ", true,
				Tools.T("Visualize spatial data", true,
						Tools.hBox(vbox, new Separator(), new Separator(), new Separator())),
				updateChart(), statisticAvregeCapital());

		titel.setStyle(" -fx-base: #d6d9df;");
		return titel;
	}



	static PieChart updateChart() {
		PieChart chart = new PieChart();
		ConcurrentHashMap<String, Double> nbrOfPatchInAgent = new ConcurrentHashMap<>();
		HashMap<String, Color> color = new HashMap<>();

		patchsInRergion.forEach(p -> {
			if (p.getOwner() != null) {
				if (nbrOfPatchInAgent.containsKey(p.getOwner().getLabel())) {
					nbrOfPatchInAgent.put(p.getOwner().getLabel(), nbrOfPatchInAgent.get(p.getOwner().getLabel()) + 1);
				} else {
					nbrOfPatchInAgent.put(p.getOwner().getLabel(), 1.);
				}
				color.put(p.getOwner().getLabel(), p.getOwner().getColor());
			}
		});

		new PieChartTools().updateChart(nbrOfPatchInAgent, color, chart, false);
		chart.setTitle("Agents Distribution");
		chart.setLegendSide(Side.LEFT);

		return chart;
	}

	static HBox statisticAvregeCapital() {
		ConcurrentHashMap<String, Double> acumulationOfCapital = new ConcurrentHashMap<>();
		patchsInRergion.forEach(c -> {
			c.getCapitals().forEach((k, v) -> {
				if (acumulationOfCapital.containsKey(k)) {
					acumulationOfCapital.put(k, acumulationOfCapital.get(k) + v);
				} else {
					acumulationOfCapital.put(k, v);
				}

			});

		});
		String[][] tableStr = new String[acumulationOfCapital.size() + 1][2];
		tableStr[0][0] = "Capital";
		tableStr[0][1] = "Value";
		AtomicInteger i = new AtomicInteger(1);
		HashMap<String, Color> color = new HashMap<>();
		acumulationOfCapital.forEach((k, v) -> {
			tableStr[i.get()][0] = k;
			tableStr[i.getAndIncrement()][1] = (v / patchsInRergion.size()) + "";
			color.put(k, ColorsTools.RandomColor());
		});
		TableView<ObservableList<String>> table = CSVTableView.newtable(tableStr);
		

		PieChart chart = new PieChart();
		new PieChartTools().updateChart(acumulationOfCapital, color, chart, true);
		chart.setTitle("Average Capital Distribution");
		// chart.setLegendSide(Side.LEFT);

		return Tools.hBox(table, chart);
	}
	
	public static HashMap<String, Consumer<String>> creatMenu() {
		HashMap<String, Consumer<String>> menu = new HashMap<>();
		menu.put("Access to the Selected regions", x -> {
			RegionWindow();
		});
		menu.put("Deselect regions",x-> {
			patchsInRergion.clear();
			CellsSet.colorMap();
		});
		return menu;
	}

}
