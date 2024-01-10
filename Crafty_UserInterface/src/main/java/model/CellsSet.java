package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import UtilitiesFx.filesTools.SaveAs;
import UtilitiesFx.graphicalTools.ColorsTools;
import controllers.CellWindow;
import controllers.NewRegion_Controller;
import dataLoader.CellsLoader;
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
	private static int maxX,maxY;
	private static String regioneselected = "Regions";
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
		
		canvas = new Canvas((maxX-minX)* Cell.getSize(), (maxY-minY) * Cell.getSize());
		gc = canvas.getGraphicsContext2D();
		
	/*
		gc.setFill(Color.RED);
	    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	*/
	        
		colorMap("FR");
		FxMain.root.getChildren().clear();
		FxMain.root.getChildren().add(new VBox(canvas));
		FxMain.subScene.setCamera(FxMain.camera);
		
		FxMain.camera.adjustCamera(FxMain.root,FxMain.subScene);
		//FxMain.camera.defaultcamera(FxMain.root,FxMain.subScene);
		MapControlerBymouse();
	}
	
	static public void colorMap(String str) {
		colortype = str;
		colorMap();
	}

	static public void colorMap() {
		List<Double> values = new ArrayList<>();
		if (colortype.equalsIgnoreCase("FR") || colortype.equalsIgnoreCase("Agent")) {
			
			cellsSet.forEach(c -> {
				if (c.getOwner() != null) {
					c.ColorP(c.getOwner().getColor());
					}
				else {
					c.ColorP(Color.WHITE);
				}
			});
		} else if (capitalsName.contains(colortype)) {

			cellsSet.forEach(c -> {
				if (c.getCapitals().get(colortype) != null)
					values.add(c.getCapitals().get(colortype));
			});

			double max = Collections.max(values);

			cellsSet.forEach(c -> {
				if (c.getCapitals().get(colortype) != null)
					// patch.ColorP(ColorsTools.getColorForValue(max, patch.capitals.get(name)));
					c.ColorP(ColorsTools.colorPalette(max, c.getCapitals().get(colortype)));
			});
		} else if (servicesNames.contains(colortype)) {

			cellsSet.forEach(c -> {
				if (c.getServices().get(colortype) != null)
					values.add(c.getServices().get(colortype));
			});

			double max = values.size() > 0 ? Collections.max(values) : 0;

			cellsSet.forEach(c -> {
				if (c.getServices().get(colortype) != null)
					c.ColorP(ColorsTools.colorPalette(max, c.getServices().get(colortype)));
			});
		} else if (colortype.equalsIgnoreCase("tmp")) {

			cellsSet.forEach(c -> {
				values.add(c.getTmpValueCell());
			});

			double max = Collections.max(values);
			cellsSet.forEach(c -> {
				c.ColorP(ColorsTools.colorPalette(max, c.getTmpValueCell()));
			});

		} else /* if (name.equals("LAD19NM") || name.equals("nuts318nm")) */ {
			HashMap<String, Color> colorGis = new HashMap<>();

			cellsSet.forEach(c -> {

				colorGis.put(c.getGisNameValue().get(colortype), ColorsTools.RandomColor());
			});

			cellsSet.forEach(c -> {
				c.ColorP(colorGis.get(c.getGisNameValue().get(colortype)));
			});

		}
	}
	
	
	public static void MapControlerBymouse() {
		canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getButton() == MouseButton.SECONDARY) {
				// Convert mouse coordinates to "pixel" coordinates
				int pixelX = (int) (event.getX() - (event.getX() % Cell.getSize() ));
				int pixelY = (int) (event.getY() - (event.getY() % Cell.getSize() ));
				// Convert pixel coordinates to cell coordinates
				int cx = (int) (pixelX / Cell.getSize() );
				int cy = (int) (maxY - pixelY / Cell.getSize() );
				if (cellsSet.hashCell.get(cx + "," + cy) != null) {
					gc.setFill(Color.BLACK);
					gc.fillRect(pixelX, pixelY, Cell.getSize() , Cell.getSize() );
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
							CellsSubSets.selectZone(cellsSet.hashCell.get(cx + "," + cy),regioneselected);
						});
						menu.put("Save camera view ", e -> {
							System.out.println(
									FxMain.camera.getTranslateX()+","+
									FxMain.camera.getTranslateY()+","+
									FxMain.camera.getTranslateZ()+","
									);
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
}
