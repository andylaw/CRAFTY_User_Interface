package WorldPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import UtilitiesFx.MouseLeftPressed;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Cell extends Rectangle {

	public int x;
	public int y;
	public HashMap<String, Double> capitals = new HashMap<>();
	public HashMap<String, Double> services = new HashMap<>();
	public AFT owner;

	int index;
	public HashMap<String, String> GIS = new HashMap<>();
	public static List<String> GISNames = new ArrayList<>();
	public Color color = Color.TRANSPARENT;
	public static String regioneselected = "regions";

	public Cell(int x, int y) {
		super(x * 10, -y * 10, 10, 10);
		this.x = x;
		this.y = y;
		rightPressed();
	}

	// ----------------------------------
	double prodactivity(AFT a, String serviceName) {
		if (a == null)
			return 0;
		double tmp = 1;
		for (Iterator<String> j = capitals.keySet().iterator(); j.hasNext();) {
			String cname = (String) j.next();
			double cvalue = capitals.get(cname);

			tmp = tmp * Math.pow(cvalue, a.sensitivty.get(cname + "_" + serviceName));
		}
		return tmp * a.productivityLevel.get(serviceName);
	}

	double utility(AFT a) {
		if (a == null)
			return 0;
		double sum = 0;
		for (int i = 0; i < Lattice.servicesNames.size(); i++) {
			String sname = Lattice.servicesNames.get(i);
			sum += Rules.marginal.get(sname) * prodactivity(a, sname) * a.productivityLevel.get(sname);
		}
		return sum;
	}

	void Competition(AFT competitor) {
		double uC = utility(competitor);
		if (owner == null) {
			if (uC > 0)
				owner = new AFT(competitor,1);
		} else {
			if (utility(owner) + (Rules.distributionMean.size()>0? (Rules.distributionMean.get(owner.label) * owner.giveIn / 100):0) < uC) {
				owner = new AFT(competitor,1);
			}
		}
	}

	void putservices() {
		Lattice.servicesNames.forEach(sname -> {
			services.put(sname, prodactivity(owner, sname));
		});
	}


//--------------------------------------------
	void rightPressed() {
		addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
			if (e.isSecondaryButtonDown()) {
				if (!Regional.patchsInRergion.contains(this)) {
					craetMenu();	
						System.out.println("2. "+owner.label+"   "+owner.sensitivty);
					
				} else {
					Regional.creatMenu(this);
				}
			}
		});
	}

	void craetMenu() {
		PatchWindow localData = new PatchWindow(this);
		Consumer<String> creatWindos = (x) -> {
			localData.windosLocalInfo();
		};
		HashMap<String, Consumer<String>> menu = new HashMap<>();

		menu.put("Access to Cell (" + x + "," + y + ") information", creatWindos);
		menu.put("Print Info", (x) -> {
			System.out.println(localData.toString());
		});
		menu.put("Select Area ", (x) -> {
			selectZone(this, regioneselected);
		});

		MouseLeftPressed.smartMenu(this, menu);
	}



	public void ColorP(Paint paint) {
		setFill(paint);
		setStroke(paint);
		color = (Color) paint;
	}

	public void ColorP() {
		ColorP(color);
	}

	@Override
	public String toString() {
		return "Patch [Index= " + index + "  x=" + x + " y= " + y + "\n capitalsValue=" + capitals + "\n ow="
				+ owner.label + "]";

	}

	public void selectZone(Cell patch, String zonetype) {
		Lattice.P.forEach(p -> {
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
