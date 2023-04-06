package WorldPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import UtilitiesFx.MouseLeftPressed;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Patch extends Rectangle {

	Point2D coor;
	public HashMap<String, Double> capitalsValue = new HashMap<>();
	public static List<String> capitalsName = new ArrayList<>();
	public HashMap<String, Double> servicesValue = new HashMap<>();
	public static List<String> servicesName = new ArrayList<>();
	String country;
	public AgentFX owner;
	HashMap<Integer, HashMap<String, Double>> ssp = new HashMap<>();
	int index;
	HashMap<String, String> GIS = new HashMap<>();
	public Color color = Color.TRANSPARENT;

	public Patch(Map M, Point2D coor) {
		super(coor.getX() * 5, -coor.getY() * 5, 5, 5);
		this.coor = coor;
		rightPressed(M);
	}

	void rightPressed(Map M) {

		addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
			if (e.isSecondaryButtonDown()) {
				if (!Regional.patchsInRergion.contains(this)) {
					craetMenu(M);
				} else {
					Regional.creatMenu(this);
				}
			}
		});
	}

	void craetMenu(Map M) {
		PatchWindow localData = new PatchWindow(this);
		Consumer<String> creatWindos = (x) -> {localData.windosLocalInfo();	};
		HashMap<String, Consumer<String>> menu = new HashMap<>();

		menu.put("Access to Cell (" + coor.getX() + "," + coor.getY() + ") information", creatWindos);
		menu.put("Print Info", (x) -> {System.out.println(localData.toString());});
		menu.put("Select Area ", (x) -> {selectZone(M, this, "\"LAD19NM\"");});

		MouseLeftPressed.smartMenu(this, menu);
	}

	void ColorP(Paint paint, Paint paint2) {
		setFill(paint);
		setStroke(paint2);
		color = (Color) paint;
	}

	void ColorP(Paint paint) {
		ColorP(paint, paint);
	}

	@Override
	public String toString() {
		return "Patch [Index= " + index + "  coor=" + coor + "\n capitalsValue=" + capitalsValue + "\n capitalsName="
				+ capitalsName + "\n country=" + country + "\n ow=" + owner.label + "]";
	}

	public void selectZone(Map M, Patch patch, String zonetype) {
		M.P.forEach(p -> {
			if (p.GIS.get(zonetype) != null) {
				if (p.GIS.get(zonetype).equals(patch.GIS.get(zonetype))) {
					p.ColorP(p.color);
					Regional.patchsInRergion.add(p);
				}
			}
		});
		Regional.patchsInRergion.forEach(p -> {
			p.setFill(Color.BLACK);
			p.setStroke(Color.BLACK);
		});
	}

}
