package WorldPack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import CameraPack.Camera;
import TabsPane.NewWindow;
import UtilitiesFx.CSVTableView;
import UtilitiesFx.ColorsTools;
import UtilitiesFx.MouseLeftPressed;
import UtilitiesFx.PieChartTools;
import UtilitiesFx.Tools;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

public class Regional {

	public static Set<Cell> patchsInRergion = new HashSet<>();
	public static Set<Rectangle> patchCopy = new HashSet<>();
	static HashMap<Rectangle, Cell> rectToPach = new HashMap<>();

	static NewWindow window = new NewWindow();
	static Group root = new Group();
	static SubScene subScene;
	static Camera camera = new Camera();
	static Scene scene;
	static BorderPane rootPane = new BorderPane();

	public static void RegionWindow() {

		subScene = new NewWindow().subSceneWithCamera(rootPane, root);
		TitledPane pane = colorWorld();

	//	rootPane.getChildren().addAll(subScene, pane);
	//	rootPane.setAlignment(Pos.TOP_LEFT);

		window.creatwindows("CRAFTY Regional Analyse", 0.8, 0.9, rootPane);

		CreatGegionalMap(root);
//		double sceneWidth= Screen.getPrimary().getBounds().getWidth() / 4;
		HBox contentBox = new HBox();
        contentBox.getChildren().addAll(pane, subScene);
        rootPane.setCenter(contentBox);
//        pane.setPrefWidth(sceneWidth);
//        pane.setMaxWidth(sceneWidth);
//        pane.setMinWidth(sceneWidth);
        BorderPane.setAlignment(contentBox, Pos.CENTER);
        BorderPane.setMargin(contentBox, new javafx.geometry.Insets(1));

	}

	static void rightPressed(Rectangle rec) {

		rec.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
			if (e.isSecondaryButtonDown()) {
				eventfilter(window, rec);

			}
		});
	}

	static void eventfilter(Stage stage, Rectangle rec) {
		PatchWindow localData = new PatchWindow(rectToPach.get(rec));

		Consumer<String> creatWindos = (x) -> {
			localData.windosLocalInfo();
		};
		Consumer<String> printInfo = (x) -> {
			System.out.println(localData.toString());
		};

		HashMap<String, Consumer<String>> menu = new HashMap<>();

		menu.put("Access to Cell (" + rectToPach.get(rec).x + "," + rectToPach.get(rec).y
				+ ") information", creatWindos);
		menu.put("Print Info", printInfo);

		MouseLeftPressed.smartMenu(rec, menu);

	}

	static void CreatGegionalMap(Group g) {
		int size = 10;
		ArrayList<Integer> X = new ArrayList<>();
		ArrayList<Integer> Y = new ArrayList<>();
		patchsInRergion.forEach(p -> {
			X.add(p.x);
			Y.add(p.y);
		});
		double minX = Collections.min(X), minY = Collections.min(Y);

		patchsInRergion.forEach(p -> {
			Rectangle rec = new Rectangle(size * (minX > 0 ? p.x - minX : p.y + minX),
					-size * (minY > 0 ? p.y- minY : p.y + minY), size, size);
			rec.setFill(p.color);
			rectToPach.put(rec, p);
			patchCopy.add(rec);
			rightPressed(rec);
			g.getChildren().add(rec);

		});

		System.out.println(g.getChildren().size() + "  -->  " + patchsInRergion.size());
	}

	public static void creatMenu(Cell p) {
		HashMap<String, Consumer<String>> menu = new HashMap<>();
		menu.put("Access to the Selected Area", (x) -> {
			RegionWindow();
		});
		MouseLeftPressed.smartMenu(p, menu);

	}

	public static void colorMap(String name) {
		List<Double> values = new ArrayList<>();
		if (name.equals("FR")) {
			patchCopy.forEach(p -> {
				if (rectToPach.get(p).owner != null)
					p.setFill(rectToPach.get(p).owner.color);
			});
		} else if (name.equals("LAD19NM") || name.equals("nuts318nm")) {
			HashMap<String, Color> colorGis = new HashMap<>();

			patchCopy.forEach(p -> {
				colorGis.put(rectToPach.get(p).GIS.get("\"" + name + "\""), ColorsTools.RandomColor());
			});

			patchCopy.forEach(p -> {
				p.setFill(colorGis.get(rectToPach.get(p).GIS.get("\"" + name + "\"")));
			});

		} else {

			patchCopy.forEach(p -> {
				if (rectToPach.get(p).capitals.get(name) != null)
					values.add(rectToPach.get(p).capitals.get(name));
			});

			double max = Collections.max(values);

			patchCopy.forEach(p -> {
				if (rectToPach.get(p).capitals.get(name) != null)
					p.setFill(ColorsTools.getColorForValue(max, rectToPach.get(p).capitals.get(name)));
			});
		}
	}

	public static TitledPane colorWorld() {
		VBox vbox = new VBox();

		int length = Lattice.capitalsName.size() + 3;

		RadioButton[] radioColor = new RadioButton[length];

		Node imageView = image();
		imageView.setVisible(false);

		for (int i = 0; i < length; i++) {
			if (i < Lattice.capitalsName.size()) {
				radioColor[i] = new RadioButton(Lattice.capitalsName.get(i));
			} else if (i == Lattice.capitalsName.size()) {
				radioColor[i] = new RadioButton("FR");
			} else if (i == Lattice.capitalsName.size() + 1) {
				radioColor[i] = new RadioButton("LAD19NM");
			} else if (i == Lattice.capitalsName.size() + 2) {
				radioColor[i] = new RadioButton("nuts318nm");
			}
			vbox.getChildren().add(radioColor[i]);
			int k = i;
			radioColor[i].setOnAction(e -> {
				for (int j = 0; j < length; j++) {
					if (k != j) {
						radioColor[j].setSelected(false);
						// key.setVisible(!Lattice.capitalsName.contains(radioColor[k].getText()));
						imageView.setVisible(Lattice.capitalsName.contains(radioColor[k].getText()));
					}
				}
				if (k < Lattice.capitalsName.size()) {
					colorMap(Lattice.capitalsName.get(k));
				} else if (k == Lattice.capitalsName.size()) {
					colorMap("FR");
				} else if (k == Lattice.capitalsName.size() + 1) {
					colorMap("LAD19NM");
				} else if (k == Lattice.capitalsName.size() + 2) {
					colorMap("nuts318nm");
				}
			});

		}

		TitledPane titel = Tools.T("Displays Capitals data: ", true,
				Tools.T("Visualize spatial data", true,
						Tools.hBox(vbox, new Separator(), new Separator(), new Separator(), /* key, */ imageView)),
				updateChart(), statisticAvregeCapital());

		titel.setStyle(" -fx-base: #d6d9df;");
		return titel;
	}

	static Node image() {
		HBox h = new HBox();
		ImageView imageView = new ImageView(ColorsTools.createColorScaleImage(65, 200, 1));

		Slider slider = Tools.slider(0, 1, 0);
		slider.setMajorTickUnit(0.2f);
		slider.setMaxHeight(200);
		slider.setOrientation(Orientation.VERTICAL);
		h.getChildren().addAll(slider, imageView);
		return h;
	}

	static PieChart updateChart() {
		PieChart chart = new PieChart();
		HashMap<String, Double> nbrOfPatchInAgent = new HashMap<>();
		HashMap<String, Color> color = new HashMap<>();

		patchsInRergion.forEach(p -> {
			if (p.owner != null) {
				if (nbrOfPatchInAgent.containsKey(p.owner.label)) {
					nbrOfPatchInAgent.put(p.owner.label, nbrOfPatchInAgent.get(p.owner.label) + 1);
				} else {
					nbrOfPatchInAgent.put(p.owner.label, 1.);
				}
				color.put(p.owner.label, p.owner.color);
			}
		});

		new PieChartTools().updateChart(nbrOfPatchInAgent, color, chart, false);
		chart.setTitle("Agents Distribution");
		chart.setLegendSide(Side.LEFT);

		return chart;
	}

	static HBox statisticAvregeCapital() {
		HashMap<String, Double> acumulationOfCapital = new HashMap<>();
		patchsInRergion.forEach(p -> {
			p.capitals.forEach((k, v) -> {
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
		CSVTableView table = null;
		try {
			table = new CSVTableView(tableStr, 0, 1, false);
		} catch (IOException e) {
		}

		PieChart chart = new PieChart();
		new PieChartTools().updateChart(acumulationOfCapital, color, chart, true);
		chart.setTitle("Average Capital Distribution");
		// chart.setLegendSide(Side.LEFT);

		return Tools.hBox(table, chart);
	}

}
