package model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import dataLoader.AFTsLoader;
import dataLoader.ServiceSet;
import fxmlControllers.MasksPaneController;
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
		CellsSet.pixelWriter.setColor(getX(), /* CellsSet.getMaxY() - */getY(), color);
	}

	// ----------------------------------//
	public double productivity(Manager a, String service) {
		if (a == null || !a.isInteract())
			return 0;
		double product = capitals.entrySet().stream()
				.mapToDouble(e -> Math.pow(e.getValue(), a.getSensitivity().get(e.getKey() + "_" + service)))
				.reduce(1.0, (x, y) -> x * y);
		return product * a.getProductivityLevel().get(service);
	}

	public void productivity(Service service) {
		if (owner == null || !owner.isInteract())
			return;
		double pr = 1.0;
		for (Map.Entry<String, Double> entry : capitals.entrySet()) {
			double value = Math.pow(entry.getValue(),
					owner.getSensitivity().get(entry.getKey() + "_" + service.getName()));
			pr *= value;
		}
		pr = pr * owner.getProductivityLevel().get(service.getName());

		currentProductivity.put(service.getName(), pr);
	}

	double utility(Manager a, ConcurrentHashMap<String, Double> marginal) {
		if (a == null || !a.isInteract()) {
			return 0;
		}
		return ServiceSet.getServicesList().stream().mapToDouble(sname -> marginal.get(sname) * productivity(a, sname))
				.sum();
	}

	double utility(ConcurrentHashMap<String, Double> marginal) {
		if (owner == null || !owner.isInteract()) {
			return 0;
		}
		try {
			return ServiceSet.getServicesList().stream()
					.mapToDouble(sname -> marginal.get(sname) * currentProductivity.get(sname)).sum();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	private void Competition(Manager competitor, ConcurrentHashMap<String, Double> marginal,
			ConcurrentHashMap<Manager, Double> distributionMean) {
		if (competitor == null || !competitor.isInteract()) {
			return;
		}

		boolean makeCopetition = true;
		if (getMaskType() != null) {
			HashMap<String, Boolean> mask = MasksPaneController.restrictions.get(getMaskType());
			if (mask != null) {
				if (owner == null) {
					makeCopetition = mask.get(competitor.getLabel() + "_" + competitor.getLabel());
				} else {
					makeCopetition = mask.get(owner.getLabel() + "_" + competitor.getLabel());
				}
			}
		}

		if (makeCopetition) {
			double uC = utility(competitor, marginal);
			double uO = utility(marginal);

			if (owner == null || owner.isAbandoned()) {
				if (uC > 0)
					owner = ModelRunner.isMutated ? new Manager(competitor) : competitor;
			} else {
				double nbr = distributionMean != null
						? (distributionMean.get(owner)
								* (owner.getGiveInMean() + owner.getGiveInSD() * new Random().nextGaussian()))
						: 0;
				if ((uC - uO > nbr) && uC > 0) {
					owner = ModelRunner.isMutated ? new Manager(competitor) : competitor;
				}
			}
		}
	}

	Manager mostCompetitiveAgent(Collection<Manager> setAfts, ConcurrentHashMap<String, Double> marginal) {
		if (setAfts.size() == 0) {
			return owner;
		}
		double uti = 0;
		Manager theBestAFT = setAfts.iterator().next();
		for (Manager agent : setAfts) {
			double u = utility(agent, marginal);
			if (u > uti) {
				uti = u;
				theBestAFT = agent;
			}
		}
		return theBestAFT;
	}

	void competition(ConcurrentHashMap<String, Double> marginal, ConcurrentHashMap<Manager, Double> distributionMean,
			Region R) {
		boolean Neighboor = ModelRunner.NeighboorEffect && ModelRunner.probabilityOfNeighbor > Math.random();
		Collection<Manager> afts = Neighboor
				? CellsSubSets.detectExtendedNeighboringAFTs(this, ModelRunner.NeighborRaduis)
				: AFTsLoader.getActivateAFTsHash().values();

		if (Math.random() < ModelRunner.MostCompetitorAFTProbability) {
			Competition(mostCompetitiveAgent(afts, marginal), marginal, distributionMean);
		} else {
			Competition(AFTsLoader.getRandomAFT(afts), marginal, distributionMean);
		}
	}

	public void calculateCurrentProductivity(Region R) {
		currentProductivity.clear();
		R.getServicesHash().values().forEach(serviceName -> {
			productivity(serviceName);
		});
	}

	void giveUp(ConcurrentHashMap<String, Double> marginal, ConcurrentHashMap<Manager, Double> distributionMean,
			Region R) {
		if (getOwner() != null && getOwner().isInteract()) {
			double utility = utility(marginal);
			double averageutility = distributionMean.get(getOwner());
			if ((utility < averageutility
					* (getOwner().getGiveUpMean() + getOwner().getGiveUpSD() * new Random().nextGaussian())
					&& getOwner().getGiveUpProbabilty() > Math.random())) {
				setOwner(null);
				R.getUnmanageCellsR().add(this);
			}
		}
	}
//------------------------------------------//

	public void landStored(Manager a, Region R) {
		double sum = 0;

		for (String s : ServiceSet.getServicesList()) {
			sum += productivity(a, s) * a.getProductivityLevel().get(s);
		}
		setTmpValueCell(sum);
	}

	@Override
	public String toString() {
		return "Cell [index=" + index + ", x=" + x + ", y=" + y + ", CurrentRegion=" + CurrentRegion + "\n, Mask="
				+ getMaskType() + ", getOwner()=" + (getOwner() != null ? getOwner().getLabel() : "Unmanaged")
				+  ", getCapitals()=" + getCapitals() + ", getCurrentProductivity()=" +
				getCurrentProductivity() +
					  "]";
	}

//	@Override
//	public String toString() {
//		return " ------------------------------------------- \n" + "Patch [Index= " + index + "  x=" + x + " y= " + y
//				+ "\n capitalsValue=" + capitals + "\n ow=" + owner.getLabel() + "\n" + owner.toString() + "] \n "
//				+ "------------------------------------------- \n";
//	}

}
