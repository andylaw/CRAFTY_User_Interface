package model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import fxmlControllers.MasksPaneController;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Mohamed Byari
 *
 */

public class Cell extends AbstractCell {

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void ColorP() {
		ColorP(color);
	}

	public void ColorP(Color color) {
		this.color = color;
		ColorP(CellsSet.getGc(), color);
	}

	public void ColorP(GraphicsContext gc, Color color) {
		gc.setFill(color);
		gc.fillRect(x * Cell.size, (CellsSet.getMaxY() - y) * Cell.size, Cell.size, Cell.size);
	}

	// ----------------------------------
	public double prodactivity(Manager a, String serviceName) {
		if (a == null)
			return 0;
		double tmp = 1;
		for (Iterator<String> j = capitals.keySet().iterator(); j.hasNext();) {
			String cname = (String) j.next();
			double cvalue = capitals.get(cname);
			tmp = tmp * Math.pow(cvalue, a.getSensitivty().get(cname + "_" + serviceName));
		}
//		System.out.println(serviceName+"->"+a.getProductivityLevel().containsKey(serviceName));
		return tmp * a.getProductivityLevel().get(serviceName);
	}

	double utility(Manager a) {
		if (a == null)
			return 0;
		double sum = 0;
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			String sname = CellsSet.getServicesNames().get(i);
			sum += ModelRunner.marginal.get(sname) * prodactivity(a, sname) * a.getProductivityLevel().get(sname);
		}
		return sum;
	}

	void Competition(Manager competitor, boolean ismutated, double mutationInterval) {
		// if this land is protected then check if the compeititvenese should happend.
		boolean makeCopetition = true;
		if (getMaskType() != null) {
			HashMap<String, Boolean> mask = MasksPaneController.restrictions.get(getMaskType());
			if (owner == null) {
				makeCopetition = mask.get(competitor.getLabel() + "_" + competitor.getLabel());
			} else {
				makeCopetition = mask.get(owner.getLabel() + "_" + competitor.getLabel());
			}
		}
		if (makeCopetition) {
			double uC = utility(competitor);
			double uO = utility(owner);

			if (owner == null) {
				if (uC > 0)
					// here should uC> average(competitor utility).

					owner = ismutated ? new Manager(competitor, mutationInterval) : competitor;
			} else {
				double nbr = ModelRunner.distributionMean != null
						? (ModelRunner.distributionMean.get(owner.getLabel())
								* (owner.getGiveInMean() + owner.getGiveInSD() * new Random().nextGaussian()))
						: 0;
				if (uO + nbr < uC) {

					owner = ismutated ? new Manager(competitor, mutationInterval) : competitor;
				}
			}
		}
	}

	void putservices() {
		CellsSet.getServicesNames().forEach(sname -> {
			services.put(sname, prodactivity(owner, sname));
		});
	}

//------------------------------------------//

	public void landStored(Manager a) {
		double sum = 0;
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			String sname = CellsSet.getServicesNames().get(i);
			sum += prodactivity(a, sname) * a.getProductivityLevel().get(sname);
		}
		setTmpValueCell(sum);
	}

	@Override
	public String toString() {
		return " ------------------------------------------- \n" + "Patch [Index= " + index + "  x=" + x + " y= " + y
				+ "\n capitalsValue=" + capitals + "\n ow=" + owner.getLabel() + "\n" + owner.toString() + "] \n "
				+ "------------------------------------------- \n";

	}

}
