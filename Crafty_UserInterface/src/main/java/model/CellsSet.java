package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import UtilitiesFx.filesTools.SaveAs;
import UtilitiesFx.graphicalTools.ColorsTools;
import controllers.CellWindow;
import controllers.NewRegion_Controller;
import dataLoader.CellsLoader;
import dataLoader.MaskRestrictionDataLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import main.FxMain;

/**
 * @author Mohamed Byari
 *
 */

public class CellsSet {
	private static Canvas canvas;
	private static GraphicsContext gc;
	private static int maxX, maxY;
	private static String regioneselected = "Region_Code";
	private static String colortype = "FR";
	private static CellsLoader cellsSet;
	private static HashMap<String, double[]> demand = new HashMap<>();
	private static List<String> capitalsName = new ArrayList<>();
	private static List<String> servicesNames = new ArrayList<>();

	public static void plotCells() {
		ArrayList<Integer> X = new ArrayList<>();
		ArrayList<Integer> Y = new ArrayList<>();
		cellsSet.forEach(c -> {
			X.add(c.getX());
			Y.add(c.getY());
		});
		maxX = Collections.max(X);
		maxY = Collections.max(Y);
		int minX = Collections.min(X);
		int minY = Collections.min(Y);

		canvas = new Canvas((maxX - minX) * Cell.getSize(), (maxY - minY) * Cell.getSize());

		gc = canvas.getGraphicsContext2D();
		// FxMain.subScene = new SubScene(FxMain.root, canvas.getWidth(),
		// canvas.getHeight());

//		 gc.setFill(Color.color(Math.random(), Math.random(), Math.random()));
//		 gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		colorMap("FR");

		FxMain.root.getChildren().clear();
		FxMain.root.getChildren().add(new VBox(canvas));
		FxMain.subScene.setCamera(FxMain.camera);
		FxMain.camera.defaultcamera(canvas, FxMain.subScene);
		// FxMain.camera.adjustCamera(FxMain.root,FxMain.subScene);

		MapControlerBymouse();
	}

	public static void colorMapClean() {
		cellsSet.forEach(c -> {
			c.ColorP(Color.GRAY);
		});
	}

	public static void colorMap(String str) {
		colortype = str;
		colorMap();
	}

	public static void colorMap() {
		Set<Double> values = Collections.synchronizedSet(new HashSet<>());
		if (colortype.equalsIgnoreCase("FR") || colortype.equalsIgnoreCase("Agent")) {

			cellsSet.forEach(c -> {
				if (c.getOwner() != null) {
					c.ColorP(c.getOwner().getColor());
				} else {
					c.ColorP(Color.WHITE);
				}
			});
		} else if (capitalsName.contains(colortype)) {

			cellsSet.forEach(c -> {
				if (c.getCapitals().get(colortype) != null)
					c.ColorP(ColorsTools.getColorForValue(c.getCapitals().get(colortype)));
			});
		} else if (servicesNames.contains(colortype)) {

			cellsSet.parallelStream().forEach(c -> {
				if (c.getServices().get(colortype) != null)
					values.add(c.getServices().get(colortype));
			});

			double max = values.size() > 0 ? Collections.max(values) : 0;

			cellsSet.forEach(c -> {
				if (c.getServices().get(colortype) != null)
					c.ColorP(ColorsTools.getColorForValue(max, c.getServices().get(colortype)));
			});
		} else if (colortype.equalsIgnoreCase("tmp")) {

			cellsSet.parallelStream().forEach(c -> {
				values.add(c.getTmpValueCell());
			});

			double max = Collections.max(values);
			cellsSet.forEach(c -> {
				c.ColorP(ColorsTools.getColorForValue(max, c.getTmpValueCell()));
			});
		} else if (colortype.equalsIgnoreCase("Mask")) {
//			HashMap<String, Color> maskToColor = new HashMap<>();
//			
//			cellsSet.parallelStream().forEach(c -> {
//				maskToColor.put(c.getMaskType(), ColorsTools.colorlist(new Random().nextInt(24)));
//			});
			ArrayList<String > listOfMasks= new ArrayList<>(MaskRestrictionDataLoader.ListOfMask.keySet());
			
			cellsSet.forEach(c -> {
				if(c.getMaskType()!=null)
					c.ColorP(ColorsTools.colorlist(listOfMasks.indexOf(c.getMaskType())));
				else {c.ColorP(Color.GRAY);}
			});
		} else /* if (name.equals("LAD19NM") || name.equals("nuts318nm")) */ {
			HashMap<String, Color> colorGis = new HashMap<>();

			cellsSet.parallelStream().forEach(c -> {

				colorGis.put(c.getGisNameValue().get(colortype), ColorsTools.RandomColor());
			});

			cellsSet.forEach(c -> {
				c.ColorP(c.getColor().interpolate(colorGis.get(c.getGisNameValue().get(colortype)), 0.3));
			});

		}
	}

	public static void MapControlerBymouse() {
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getButton() == MouseButton.SECONDARY) {
				// Convert mouse coordinates to "pixel" coordinates
				int pixelX = (int) (event.getX() - (event.getX() % Cell.getSize()));
				int pixelY = (int) (event.getY() - (event.getY() % Cell.getSize()));
				// Convert pixel coordinates to cell coordinates
				int cx = (int) (pixelX / Cell.getSize());
				int cy = (int) (maxY - pixelY / Cell.getSize());
				if (cellsSet.hashCell.get(cx + "," + cy) != null) {
					gc.setFill(Color.BLACK);
					gc.fillRect(pixelX, pixelY, Cell.getSize(), Cell.getSize());
					HashMap<String, Consumer<String>> menu = new HashMap<>();
					if (!NewRegion_Controller.patchsInRergion.contains(cellsSet.hashCell.get(cx + "," + cy))) {

						CellWindow localData = new CellWindow(cellsSet.hashCell.get(cx + "," + cy));
						Consumer<String> creatWindos = (x) -> {
							localData.windosLocalInfo();
						};
						menu.put("Access to Cell (" + cx + "," + cy + ") information", creatWindos);

						menu.put("Print Info", e -> {
							System.out.println(cellsSet.hashCell.get(cx + "," + cy));
						});
						menu.put("Save Map as PNG", e -> {
							SaveAs.png(canvas);
						});
						menu.put("Select region ", e -> {
							CellsSubSets.selectZone(cellsSet.hashCell.get(cx + "," + cy), regioneselected);
						});

//						menu.put("Detach", (x) -> {
//							List<Integer> findpath = Tools.findIndexPath(canvas, canvas.getParent());
//							Tools.reinsertChildAtIndexPath(new Separator(), canvas.getParent(), findpath);
//							NewWindow win = new NewWindow();
//							win.creatwindows("", canvas);
//							win.setOnCloseRequest(event2 -> {
//								Tools.reinsertChildAtIndexPath(canvas, canvas.getParent(), findpath);
//							});
//						});

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

	public static void setRegioneselected(String regioneselected) {
		CellsSet.regioneselected = regioneselected;
	}

	public static HashMap<String, double[]> getDemand() {
		return demand;
	}

	public static List<String> getCapitalsName() {
		return capitalsName;
	}

	public static List<String> getServicesNames() {
		return servicesNames;
	}

	public static Canvas getCanvas() {
		return canvas;
	}

	public static void setCanvas(Canvas canvas) {
		CellsSet.canvas = canvas;
	}

}
