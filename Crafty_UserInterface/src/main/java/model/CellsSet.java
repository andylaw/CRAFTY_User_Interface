package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import controllers.CellWindow;
import controllers.NewRegion_Controller;
import dataLoader.CellsLoader;
import dataLoader.MaskRestrictionDataLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import main.FxMain;
import utils.analysis.CustomLogger;
import utils.filesTools.SaveAs;
import utils.graphicalTools.ColorsTools;
import utils.graphicalTools.NewWindow;
import utils.graphicalTools.Tools;

/**
 * @author Mohamed Byari
 *
 */

public class CellsSet {
	private static final CustomLogger LOGGER = new CustomLogger(CellsSet.class);
	public static boolean isPlotedMap = false;
	private static Canvas canvas;
	public static GraphicsContext gc;
	public static PixelWriter pixelWriter;
	public static WritableImage writableImage;
	static int maxX;
	public static int maxY;
	private static String regioneselected = "Region_Code";
	private static String colortype = "FR";
	private static CellsLoader cellsSet;

	public static void plotCells() {
		isPlotedMap = true;
		ArrayList<Integer> X = new ArrayList<>();
		ArrayList<Integer> Y = new ArrayList<>();
		CellsLoader.hashCell.values().forEach(c -> {
			X.add(c.getX());
			Y.add(c.getY());
		});
		maxX = Collections.max(X) + 1;
		maxY = Collections.max(Y) + 1;
		int minX = Collections.min(X);
		int minY = Collections.min(Y);
		LOGGER.info("matrix size: " + (maxX - minX) + "," + (maxY - minY));
		canvas = new Canvas((maxX - minX) * Cell.getSize(), (maxY - minY) * Cell.getSize());
		gc = canvas.getGraphicsContext2D();

		writableImage = new WritableImage(maxX, maxY);
		pixelWriter = writableImage.getPixelWriter();

//		 FxMain.subScene = new SubScene(FxMain.root, canvas.getWidth(),
//		 canvas.getHeight());
//		 gc.setFill(Color.color(Math.random(), Math.random( ), Math.random()));
//		 gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//		 colorMap("FR");

		FxMain.root.getChildren().clear();
		FxMain.root.getChildren().add(canvas);
		FxMain.subScene.setCamera(FxMain.camera);
		FxMain.camera.defaultcamera(canvas, FxMain.subScene);
		// FxMain.camera.adjustCamera(FxMain.root,FxMain.subScene);
		LOGGER.info("Number of cells = " + CellsLoader.hashCell.size());

		MapControlerBymouse();

	}

	public static ConcurrentHashMap<String, Cell> getRandomSubset(ConcurrentHashMap<String, Cell> cellsHash,
			double percentage) {

		int numberOfElementsToSelect = (int) (cellsHash.size() * (percentage));

		// Use parallel stream for better performance on large maps
		List<String> keys = new ArrayList<>(cellsHash.keySet());
		ConcurrentHashMap<String, Cell> randomSubset = new ConcurrentHashMap<>();

		Collections.shuffle(keys, new Random()); // Shuffling the keys for randomness
		keys.parallelStream().unordered() // This improve performance by eliminating the need for maintaining order
				.limit(numberOfElementsToSelect).forEach(key -> randomSubset.put(key, cellsHash.get(key)));
		return randomSubset;
	}

	public static ConcurrentHashMap<String, Cell> getSubset(ConcurrentHashMap<String, Cell> cellsHash,
			double percentage) {

		int numberOfElementsToSelect = (int) (cellsHash.size() * (percentage));
		ConcurrentHashMap<String, Cell> subset = new ConcurrentHashMap<>();
		cellsHash.keySet().parallelStream().unordered().limit(numberOfElementsToSelect)
				.forEach(key -> subset.put(key, cellsHash.get(key)));
		return subset;
	}

	public static List<ConcurrentHashMap<String, Cell>> splitIntoSubsets(ConcurrentHashMap<String, Cell> cellsHash,
			int n) {
		// Create a list to hold the n subsets
		List<ConcurrentHashMap<String, Cell>> subsets = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			subsets.add(new ConcurrentHashMap<>());
		}

		// Distribute keys randomly across the n subsets
		cellsHash.keySet().parallelStream().forEach(key -> {
			int subsetIndex = ThreadLocalRandom.current().nextInt(n);
			subsets.get(subsetIndex).put(key, cellsHash.get(key));
		});

		return subsets;
	}

	public static void colorMap(String str) {
		colortype = str;
		colorMap();
	}

	public static void showOnlyOneAFT(Manager a) {
		CellsLoader.hashCell.values().parallelStream().forEach(cell -> {
			if (cell.getOwner() == null || !cell.getOwner().getLabel().equals(a.getLabel())) {
				cell.ColorP(Color.gray(0.65));
			} else {
				cell.ColorP(a.getColor());
			}
		});
		gc.drawImage(writableImage, 0, 0);
	}

	public static void colorMap() {
		if (!isPlotedMap) {
			return;
		}
		LOGGER.info("Changing the map colors...");
		Set<Double> values = Collections.synchronizedSet(new HashSet<>());
		if (colortype.equalsIgnoreCase("FR") || colortype.equalsIgnoreCase("Agent")) {
			CellsLoader.hashCell.values().parallelStream().forEach(c -> {
				if (c.getOwner() != null) {
					c.ColorP(c.getOwner().getColor());
				} else {
					c.ColorP(Color.WHITE);
				}
			});
		} else if (CellsLoader.getCapitalsList().contains(colortype)) {
			CellsLoader.hashCell.values().parallelStream().forEach(c -> {
				c.ColorP(ColorsTools.getColorForValue(c.getCapitals().get(colortype)));

			});

		} /*
			 * else if (ServiceSet.getServicesList().contains(colortype)) {
			 * CellsLoader.hashCell.values().parallelStream().forEach(c -> { Service s =
			 * ServiceSet.getServicesHash().get(colortype); if
			 * (c.getCurrentProductivity().get(ServiceSet.getServicesHash().get(colortype))
			 * != null) values.add(c.getCurrentProductivity().get(s)); }); double max =
			 * values.size() > 0 ? Collections.max(values) : 0;
			 * 
			 * CellsLoader.hashCell.values().parallelStream().forEach(c -> {
			 * 
			 * if
			 * (c.getCurrentProductivity().get(ServiceSet.getServicesHash().get(colortype))
			 * != null) { c.ColorP(ColorsTools.getColorForValue(max,
			 * c.getCurrentProductivity().get(ServiceSet.getServicesHash().get(colortype))))
			 * ; } else { c.ColorP(ColorsTools.getColorForValue(max, 0)); } }); }
			 */ else if (colortype.equalsIgnoreCase("tmp")) {

			CellsLoader.hashCell.values().parallelStream().forEach(c -> {
				values.add(c.getTmpValueCell());
			});
			double max = Collections.max(values);

			CellsLoader.hashCell.values().parallelStream().forEach(c -> {
				c.ColorP(ColorsTools.getColorForValue(max, c.getTmpValueCell()));
			});

		} else if (colortype.equalsIgnoreCase("Mask")) {
			ArrayList<String> listOfMasks = new ArrayList<>(MaskRestrictionDataLoader.hashMasksPaths.keySet());
			CellsLoader.hashCell.values().parallelStream().forEach(c -> {
				if (c.getMaskType() != null) {
					c.ColorP(ColorsTools.colorlist(listOfMasks.indexOf(c.getMaskType())));
				} else {
					c.ColorP(Color.gray(0.75));
				}
			});
		} else {
			HashMap<String, Color> colorGis = new HashMap<>();

			CellsLoader.hashCell.values().parallelStream().forEach(c -> {
				colorGis.put(c.getCurrentRegion()/* .get(colortype) */, ColorsTools.RandomColor());
			});
			CellsLoader.hashCell.values().parallelStream().forEach(c -> {
				c.ColorP(colorGis.get(c.getCurrentRegion()/* .getGisNameValue().get(colortype) */));
			});
		}
		gc.drawImage(writableImage, 0, 0);
	}

	public static void MapControlerBymouse() {
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getButton() == MouseButton.SECONDARY) {
				// Convert mouse coordinates to "pixel" coordinates
				int pixelX = (int) (event.getX() - (event.getX() % Cell.getSize()));
				int pixelY = (int) (event.getY() - (event.getY() % Cell.getSize()));
				// Convert pixel coordinates to cell coordinates
				int cx = (int) (pixelX / Cell.getSize());
				int cy = (int) (/* maxY - */ pixelY / Cell.getSize());
				if (CellsLoader.hashCell.get(cx + "," + cy) != null) {
					gc.setFill(Color.RED);
					gc.fillRect(pixelX, pixelY, Cell.getSize(), Cell.getSize());
					HashMap<String, Consumer<String>> menu = new HashMap<>();
					if (!NewRegion_Controller.patchsInRergion.contains(CellsLoader.hashCell.get(cx + "," + cy))) {

						CellWindow localData = new CellWindow(CellsLoader.hashCell.get(cx + "," + cy));
						Consumer<String> creatWindos = (x) -> {
							localData.windosLocalInfo();

						};
						menu.put("Access to Cell (" + cx + "," + cy + ") information", creatWindos);

						menu.put("Print Info into the Console", e -> {
							System.out.println(CellsLoader.hashCell.get(cx + "," + cy));
						});
						menu.put("Save Map as PNG", e -> {
							SaveAs.png(canvas);
						});
						menu.put("Select region ", e -> {
							CellsSubSets.selectZone(CellsLoader.hashCell.get(cx + "," + cy), regioneselected);
						});

						menu.put("Detach", (x) -> {
							try {
								VBox mapBox = (VBox) FxMain.subScene.getParent();
								VBox parent = (VBox) FxMain.subScene.getParent().getParent();
								List<Integer> findpath = Tools.findIndexPath(mapBox, parent);
								Tools.reInsertChildAtIndexPath(new Separator(), parent, findpath);
								NewWindow win = new NewWindow();
								win.creatwindows("", mapBox);
								win.setOnCloseRequest(event2 -> {
									parent.getChildren().add(mapBox);
								});
							} catch (ClassCastException d) {
								LOGGER.warn(d.getMessage());
							}
						});

					} else {
						menu = NewRegion_Controller.creatMenu();
					}
					ContextMenu cm = new ContextMenu();

					MenuItem[] item = new MenuItem[menu.size()];
					AtomicInteger i = new AtomicInteger();
					menu.forEach((k, v) -> {
						item[i.get()] = new MenuItem(k);
						cm.getItems().add(item[i.get()]);
						item[i.get()].setOnAction(e -> {
							v.accept(k);
						});
						i.getAndIncrement();
					});
					cm.show(canvas.getScene().getWindow(), event.getScreenX(), event.getScreenY());
					event.consume();
				}
			}
		});
	}

	public static GraphicsContext getGc() {
		return gc;
	}

	public static int getMaxX() {
		return maxX;
	}

	public static int getMaxY() {
		return maxY;
	}

	public static CellsLoader getCellsSet() {
		return cellsSet;
	}

	public static void setCellsSet(CellsLoader cellsSet) {
		CellsSet.cellsSet = cellsSet;
	}

	public static Canvas getCanvas() {
		return canvas;
	}

	public static void setCanvas(Canvas canvas) {
		CellsSet.canvas = canvas;
	}

}
