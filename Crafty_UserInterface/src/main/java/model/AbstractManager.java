package model;

import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.paint.Color;

/**
 * @author Mohamed Byari
 *
 */
public abstract class AbstractManager {
	String label;
	String completeName;
	ManagerTypes type = ManagerTypes.AFT;
	ConcurrentHashMap<String, Double> sensitivity = new ConcurrentHashMap<>();
	ConcurrentHashMap<String, Double> productivityLevel = new ConcurrentHashMap<>();
	double giveInMean = 0, giveInSD = 0, giveUpMean = 0, giveUpSD = 0, serviceLevelNoiseMin = 0,
			serviceLevelNoiseMax = 0, giveUpProbabilty = 0;
	Color color;
	

	public ManagerTypes getType() {
		return type;
	}

	public void setType(ManagerTypes type) {
		this.type = type;
	}
	
	public boolean isActive() {
		return type == ManagerTypes.AFT || type == ManagerTypes.UNMANAGED;
	}
	public boolean isInteract() {
		return type == ManagerTypes.AFT;
	}

	public boolean isUnmanaged() {
		return type == ManagerTypes.UNMANAGED;
	}



	public ConcurrentHashMap<String, Double> getSensitivity() {
		return sensitivity;
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

	public ConcurrentHashMap<String, Double> getProductivityLevel() {
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
