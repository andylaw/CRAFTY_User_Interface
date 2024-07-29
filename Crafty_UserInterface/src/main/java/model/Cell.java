package model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import dataLoader.AFTsLoader;
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
		// gc.setFill(color);
		// gc.fillRect(x * Cell.size, (CellsSet.getMaxY() - y) * Cell.size, Cell.size,
		// Cell.size);
		CellsSet.pixelWriter.setColor(getX(), /* CellsSet.getMaxY() - */getY(), color);
	}

	// ----------------------------------//

	public double productivity(Manager a, String serviceName) {
		if (a == null || !a.isActive())
			return 0;
		double product = capitals.entrySet().stream()
				.mapToDouble(e -> Math.pow(e.getValue(), a.getSensitivity().get(e.getKey() + "_" + serviceName)))
				.reduce(1.0, (x, y) -> x * y);
		return product * a.getProductivityLevel().get(serviceName);
	}

	public void productivity(String serviceName) {
		if (owner == null || !owner.isActive())
			return;

		double pr = 1.0;
		for (Map.Entry<String, Double> entry : capitals.entrySet()) {
			double value = Math.pow(entry.getValue(), owner.getSensitivity().get(entry.getKey() + "_" + serviceName));
			pr *= value;
		}
		pr = pr * owner.getProductivityLevel().get(serviceName);

		currentProductivity.put(serviceName, pr);
	}

	double utility(Manager a, ConcurrentHashMap<String, Double> marginal) {
		if (a == null || !a.isActive()) {
			return 0;
		}
		return CellsSet.getServicesNames().stream()
				.mapToDouble(
						sname -> marginal.get(sname) * productivity(a, sname) * a.getProductivityLevel().get(sname))
				.sum();
	}

	double utility(ConcurrentHashMap<String, Double> marginal) {
		if (owner == null) {
			return 0;
		}
		try {
			return CellsSet.getServicesNames().stream().mapToDouble(sname -> marginal.get(sname)
					* currentProductivity.get(sname) * owner.getProductivityLevel().get(sname)).sum();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	private void Competition(Manager competitor, ConcurrentHashMap<String, Double> marginal,
			ConcurrentHashMap<Manager, Double> distributionMean) {
		if (competitor == null || !competitor.isActive()) {
			return;
		}
		if (owner != null && !owner.isActive()) {
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
			double uC = utility(competitor, marginal);
			double uO = utility(marginal);

			if (owner == null) {
				if (uC > 0)
					owner = ModelRunner.isMutated ? new Manager(competitor) : competitor;
			} else {
				double nbr = distributionMean != null
						? (distributionMean.get(owner)
								* (owner.getGiveInMean() + owner.getGiveInSD() * new Random().nextGaussian()))
						: 0;
				if ((uC - uO) > nbr) {
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

	void competition(ConcurrentHashMap<String, Double> marginal, ConcurrentHashMap<Manager, Double> distributionMean) {
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

	public void getCurrentProductivity() {
		currentProductivity.clear();
		CellsSet.getServicesNames().forEach(serviceName -> {
			productivity(serviceName);
		});
	}

	void giveUp(ConcurrentHashMap<String, Double> marginal, ConcurrentHashMap<Manager, Double> distributionMean,
			String region) {
		if (owner != null && owner.isActive()) {
			double cUtility = utility(marginal);

			double averageutility = distributionMean.get(getOwner());

			if ((cUtility < averageutility
					* (getOwner().getGiveUpMean() + getOwner().getGiveUpSD() * new Random().nextGaussian())
					&& getOwner().getGiveUpProbabilty() > Math.random()) /* || (cUtility < 0) */) {
				setOwner(null);
				RegionClassifier.unmanageCellsR.get(region).add(this);
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
