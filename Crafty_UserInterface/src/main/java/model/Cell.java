package model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
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

	public double productivity(Manager a, String serviceName) {
		if (a == null)
			return 0;
		double product = capitals.entrySet().stream()
				.mapToDouble(e -> Math.pow(e.getValue(), a.getSensitivity().get(e.getKey() + "_" + serviceName)))
				.reduce(1.0, (x, y) -> x * y);
		return product * a.getProductivityLevel().get(serviceName);
	}

	public void productivity(String serviceName) {
		if (owner == null)
			return;
//		double product = capitals.entrySet().stream()
//				.mapToDouble(e -> Math.pow(e.getValue(), owner.getSensitivity().get(e.getKey() + "_" + serviceName)))
//				.reduce(1.0, (x, y) -> x * y);
//		currentProductivity.put(serviceName, product * owner.getProductivityLevel().get(serviceName));

		double product = 1.0;
		for (Map.Entry<String, Double> entry : capitals.entrySet()) {
			double value = Math.pow(entry.getValue(), owner.getSensitivity().get(entry.getKey() + "_" + serviceName));
			product *= value;
		}
		double finalProduct = product * owner.getProductivityLevel().get(serviceName);
		currentProductivity.put(serviceName, finalProduct);
	}

	double utility(Manager a) {
		if (a == null) {
			return 0;
		}
		return CellsSet.getServicesNames().stream().mapToDouble(
				sname -> ModelRunner.marginal.get(sname) * productivity(a, sname) * a.getProductivityLevel().get(sname))
				.sum();
	}

	double utility() {
		if (owner == null) {
			return 0;
		}		
		try {
		return CellsSet.getServicesNames().stream().mapToDouble(sname -> ModelRunner.marginal.get(sname)
				* currentProductivity.get(sname) * owner.getProductivityLevel().get(sname)).sum();}
		catch(NullPointerException e) {
			return 0;}
	}

	void Competition(Manager competitor, boolean ismutated, double mutationInterval) {
		// if this land is protected then check if the compeititvenese should happend.
		if (competitor == null) {
			return;
		}
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
			double uO = utility();

			if (owner == null) {
				if (uC > 0)
					// here should uC> average(competitor utility).

					owner = ismutated ? new Manager(competitor, mutationInterval) : competitor;
			} else {
				double nbr = ModelRunner.distributionMean != null
						? (ModelRunner.distributionMean.get(owner.getLabel())
								* (owner.getGiveInMean() + owner.getGiveInSD() * new Random().nextGaussian()))
						: 0;
				if ((uC - uO) > nbr) {

					owner = ismutated ? new Manager(competitor, mutationInterval) :competitor;
				}

			}
		}
	}

	Manager mostCompetitiveAgent() {
		return mostCompetitiveAgent(AFTsLoader.getActivateAFTsHash().values());
	}

	Manager mostCompetitiveAgent(Collection<Manager> setAfts) {
		if (setAfts.size() == 0) {
			return owner;
		}
		double uti = 0;
		Manager theBestAFT = setAfts.iterator().next();
		for (Manager agent : setAfts) {
			double u = utility(agent);
			if (u > uti) {
				uti = u;
				theBestAFT = agent;
			}
		}
		return theBestAFT;
	}

	void competition(boolean ismutated, double mutationInterval, boolean isTheBest, boolean neighbor,
			double probabilityOfNeighbor) {
		if (neighbor && probabilityOfNeighbor > Math.random()) {
			Collection<Manager> afts = CellsSubSets.detectExtendedNeighboringAFTs(this,ModelRunner.NeighborRaduis);
			if (isTheBest) {
				Competition(mostCompetitiveAgent(afts), ismutated, mutationInterval);
			} else {
				Competition(AFTsLoader.getRandomAFT(afts), ismutated, mutationInterval);
			}
		} else {
			if (isTheBest) {
				Competition(mostCompetitiveAgent(), ismutated, mutationInterval);
			} else {
				Competition(AFTsLoader.getRandomAFT(), ismutated, mutationInterval);
			}
		}
	}

	public void getCurrentProductivity() {
		CellsSet.getServicesNames().forEach(serviceName -> {
			productivity(serviceName);
		});
	}

	void giveUp() {
		if (owner != null) {
			double cUtility = utility();
			double averageutility = ModelRunner.distributionMean.get(getOwner().getLabel());

			if ((cUtility < averageutility
					* (getOwner().getGiveUpMean() + getOwner().getGiveUpSD() * new Random().nextGaussian())
					&& getOwner().getGiveUpProbabilty() > Math.random()) /* || (cUtility < 0) */) {
				setOwner(null);
				CellsLoader.getUnmanageCells().add(this);
			}
		}
	}
//------------------------------------------//

	public void landStored(Manager a) {
		double sum = 0;
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			String sname = CellsSet.getServicesNames().get(i);
			sum += productivity(a, sname) * a.getProductivityLevel().get(sname);
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
