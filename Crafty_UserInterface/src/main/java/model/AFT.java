package model;

import java.util.HashMap;
import java.util.Random;

import javafx.scene.paint.Color;

/**
 * @author Mohamed Byari
 *
 */

public class AFT {
	private String label;
	private String name;
	private HashMap<String, Double> sensitivty = new HashMap<>();
	private HashMap<String, Double> productivityLevel = new HashMap<>();
	private double giveInMean = 0, giveInSD = 0, giveUpMean = 0, giveUpSD = 0, serviceLevelNoiseMin = 0,
			serviceLevelNoiseMax = 0, giveUpProbabilty = 0;
	private Color color;

	public AFT() {
		this.label = "";
		this.name = "";
		Lattice.getCapitalsName().forEach((Cn) -> {
			Lattice.getServicesNames().forEach((Sn) -> {
				sensitivty.put((Cn + "_" + Sn), 0.);
			});
		});
		for (int i = 0; i < Lattice.getServicesNames().size(); i++) {
			productivityLevel.put(Lattice.getServicesNames().get(i), 0.0);
		}
	}

	public AFT(AFT other, double intervale) {
		if (other != null) {
			this.label = other.label;
			this.color = other.color;
			other.sensitivty.forEach((n, v) -> {
				this.sensitivty.put(n, v * (1 + intervale * (new Random().nextDouble(2) - 1)));

			});
			other.productivityLevel.forEach((n, v) -> {
				this.productivityLevel.put(n, v * (1 + intervale * (new Random().nextDouble(2) - 1)));
			});
			this.giveInMean = other.giveInMean * (1 + intervale * (new Random().nextDouble(2) - 1));
			this.giveUpMean = other.giveUpMean * (1 + intervale * (new Random().nextDouble(2) - 1));
			this.giveUpProbabilty = other.giveUpProbabilty * (1 + intervale * (new Random().nextDouble(2) - 1));
		}

	}

	public AFT(String label, double LevelIntervale) {
		this.label = label;
		this.color = Color.color(Math.random(), Math.random(), Math.random());// ColorsTools.colorlist(new
																				// Random().nextInt(17));
		Lattice.getCapitalsName().forEach((Cn) -> {
			Lattice.getServicesNames().forEach((Sn) -> {
				this.sensitivty.put((Cn + "_" + Sn), Math.random() > 0.5 ? Math.random() : 0);
			});
		});
		Lattice.getServicesNames().forEach((Sn) -> {
			this.productivityLevel.put(Sn, LevelIntervale * Math.random());
		});
		this.giveInMean = Math.random();
		this.giveUpMean = Math.random();
		this.giveUpProbabilty = Math.random();
	}

	public void landStored(Cell C) {// should be delete and remplace by production in cell
		double sum = 0;
		for (int i = 0; i < Lattice.getServicesNames().size(); i++) {
			String sname = Lattice.getServicesNames().get(i);
			sum += C.prodactivity(this, sname) * this.productivityLevel.get(sname);
		}
		C.setTmpValueCell(sum);
	}

	public HashMap<String, Double> getSensitivty() {
		return sensitivty;
	}

	public double getServiceLevelNoiseMin() {
		return serviceLevelNoiseMin;
	}

	public void setServiceLevelNoiseMin(double serviceLevelNoiseMin) {
		this.serviceLevelNoiseMin = serviceLevelNoiseMin;
	}

	public double getServiceLevelNoiseMax() {
		return serviceLevelNoiseMax;
	}

	public void setServiceLevelNoiseMax(double serviceLevelNoiseMax) {
		this.serviceLevelNoiseMax = serviceLevelNoiseMax;
	}

	public double getGiveInMean() {
		return giveInMean;
	}

	public void setGiveInMean(double giveInMean) {
		this.giveInMean = giveInMean;
	}

	public double getGiveInSD() {
		return giveInSD;
	}

	public void setGiveInSD(double giveInSD) {
		this.giveInSD = giveInSD;
	}

	public double getGiveUpMean() {
		return giveUpMean;
	}

	public void setGiveUpMean(double giveUpMean) {
		this.giveUpMean = giveUpMean;
	}

	public double getGiveUpSD() {
		return giveUpSD;
	}

	public void setGiveUpSD(double giveUpSD) {
		this.giveUpSD = giveUpSD;
	}

	public double getGiveUpProbabilty() {
		return giveUpProbabilty;
	}

	public void setGiveUpProbabilty(double giveUpProbabilty) {
		this.giveUpProbabilty = giveUpProbabilty;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public HashMap<String, Double> getProductivityLevel() {
		return productivityLevel;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AFT(String label) {
		this.label = label;
	}

	public AFT(AFT other) {
		this(other, 0);
	}

	@Override
	public String toString() {
		return "AFT [label=" + label + " ,\n sensitivty=" + sensitivty + ",\n productivityLevel=" + productivityLevel
				+ ",\n giveIn=" + giveInMean + ", giveUp=" + giveUpMean + ", giveUpProbabilty=" + giveUpProbabilty
				+ "]";
	}
}
