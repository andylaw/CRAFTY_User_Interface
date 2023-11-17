package model;

import java.util.HashMap;
import javafx.scene.paint.Color;
/**
 * @author Mohamed Byari
 *
 */
public abstract class AbstractManager {
	String label;
	String completeName;
	HashMap<String, Double> sensitivty = new HashMap<>();
	HashMap<String, Double> productivityLevel = new HashMap<>();
	double giveInMean = 0, giveInSD = 0, giveUpMean = 0, giveUpSD = 0, serviceLevelNoiseMin = 0,
			serviceLevelNoiseMax = 0, giveUpProbabilty = 0;
	Color color;
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

	public String getCompleteName() {
		return completeName;
	}

	public void setCompleteName(String name) {
		this.completeName = name;
	}


}
