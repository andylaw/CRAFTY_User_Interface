package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Cell {
	private static int size=5;
	private int index;
	private int x, y;
	private HashMap<String, Double> capitals = new HashMap<>();
	private HashMap<String, Double> services = new HashMap<>();
	private HashMap<String, String> GisNameValue = new HashMap<>();//
	private AFT owner;
	private double tmpValueCell=0;// a changer
	 
	
	public int getX() {
		return x;
	}


	public void setX(int x) {
		this.x = x;
	}


	public int getY() {
		return y;
	}


	public void setY(int y) {
		this.y = y;
	}


	public AFT getOwner() {
		return owner;
	}


	public void setOwner(AFT owner) {
		this.owner = owner;
	}


	public double getTmpValueCell() {
		return tmpValueCell;
	}


	public void setTmpValueCell(double tmpValueCell) {
		this.tmpValueCell = tmpValueCell;
	}


	public HashMap<String, Double> getCapitals() {
		return capitals;
	}


	public HashMap<String, Double> getServices() {
		return services;
	}


	public HashMap<String, String> getGisNameValue() {
		return GisNameValue;
	}


	public static int getSize() {
		return size;
	}


	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}


	public Color color = Color.TRANSPARENT;
	

	public Cell(int x, int y) {
		this.x = x;
		this.y = y; 
		
	}
	public void ColorP(Color color) {
		this.color=color;
		Lattice.getGc().setFill(color);
		Lattice.getGc().fillRect(x * Cell.size, (Lattice.getMaxY()-y) * Cell.size, Cell.size, Cell.size);
	}


	public void ColorP() {
		ColorP(color);
	}
	public void ColorP(GraphicsContext gc, Color color) {
		gc.setFill(color);
		gc.fillRect(x * Cell.size, (Lattice.getMaxY()-y) * Cell.size, Cell.size, Cell.size);
	}

	// ----------------------------------
	public double prodactivity(AFT a, String serviceName) {
		if (a == null)
			return 0;
		double tmp = 1;
		for (Iterator<String> j = capitals.keySet().iterator(); j.hasNext();) {
			String cname = (String) j.next();
			double cvalue = capitals.get(cname);
			tmp = tmp * Math.pow(cvalue, a.getSensitivty().get(cname + "_" + serviceName));
		}

		return tmp * a.getProductivityLevel().get(serviceName);
	}

	double utility(AFT a) {
		if (a == null)
			return 0;
		double sum = 0;
		for (int i = 0; i < Lattice.getServicesNames().size(); i++) {
			String sname = Lattice.getServicesNames().get(i);
			sum += Rules.marginal.get(sname) * prodactivity(a, sname) * a.getProductivityLevel().get(sname);
		}
		return sum;
	}

	void Competition(AFT competitor, boolean ismutated, double mutationInterval) {
		double uC = utility(competitor);
		double uO = utility(owner);

		if (owner == null) {
			if (uC > 0)
				owner = newOwner(competitor, ismutated, mutationInterval);
		} else {
			double nbr = Rules.distributionMean!=null
					? (Rules.distributionMean.get(owner.getLabel()) * (owner.getGiveInMean()+owner.getGiveInSD()*new Random().nextGaussian()))
					: 0;
			if (uO + nbr  < uC) {
				owner = newOwner(competitor, ismutated, mutationInterval);
			}
		}
	}

	AFT newOwner(AFT agent, boolean ismutated, double intervale) {
		return ismutated ? new AFT(agent, intervale) : agent;
	}

	void putservices() {
		Lattice.getServicesNames().forEach(sname -> {
			services.put(sname, prodactivity(owner, sname));
		});
	}
//------------------------------------------//
	void checkNeighboorSameLabel() {
		if (owner != null) {
			AtomicInteger sum = new AtomicInteger();
			neighborhoodOnAction(c -> {
				if(c.owner!=null)
				if (c.owner.getLabel().equals(owner.getLabel())) {
					sum.getAndIncrement();
				}
			});
			owner.setGiveInMean(owner.getGiveInMean()+sum.get());
			
//			owner.productivityLevel.forEach((n, v) -> {
//				owner.productivityLevel.put(n, v +  (sum.get()));
//			});
		}
	}

	void neighborhoodOnAction(Consumer<Cell> action) {
		getMooreNeighborhood().forEach(c -> {
			action.accept(c);
		});
	}

	Set<Cell> getMooreNeighborhood() {
		Set<Cell> neighborhood = new HashSet<>();
		for (int i = (x - 1); i <= x + 1; i++) {
			for (int j = (y - 1); j <= (y) + 1; j++) {
				if (Lattice.getHashCell().containsKey(i + "," + j)) {
					neighborhood.add(Lattice.getHashCell().get(i + "," + j));
				}
			}
		}
		neighborhood.remove(Lattice.getHashCell().get(x + "," + y));
		return neighborhood;
	}


	@Override
	public String toString() {
		return " ------------------------------------------- \n" + "Patch [Index= " + index + "  x=" + x + " y= " + y
				+ "\n capitalsValue=" + capitals + "\n ow=" + owner.getLabel() + "\n" + owner.toString() + "] \n "
				+ "------------------------------------------- \n";

	}


}
