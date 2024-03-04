package model;

import java.util.HashMap;
import java.util.Random;

import javafx.scene.paint.Color;

/**
 * @author Mohamed Byari
 *
 */

public class Manager extends AbstractManager{
	


	public Manager() {
		label = "";
		completeName = "";
		CellsSet.getCapitalsName().forEach((Cn) -> {
			CellsSet.getServicesNames().forEach((Sn) -> {
				sensitivty.put((Cn + "_" + Sn), 0.);
			});
		});
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			productivityLevel.put(CellsSet.getServicesNames().get(i), 0.0);
		}
	}

	public Manager(Manager other, double intervale) {// shloud modefie 
		if (other != null) {
			this.label = other.label;
			this.color = other.color;
			other.sensitivty.forEach((n, v) -> {
				this.sensitivty.put(n, v * (1 + intervale * (2*new Random().nextDouble() - 1)));

			});
			other.productivityLevel.forEach((n, v) -> {
				this.productivityLevel.put(n, v * (1 + intervale * (2*new Random().nextDouble() - 1)));
			});
			this.giveInMean = other.giveInMean * (1 + intervale * (2*new Random().nextDouble() - 1));
			this.giveUpMean = other.giveUpMean * (1 + intervale * (2*new Random().nextDouble() - 1));
			this.giveUpProbabilty = other.giveUpProbabilty * (1 + intervale * (2*new Random().nextDouble() - 1));
		}

	}

	public Manager(String label, double LevelIntervale) {
		this.label = label;
		this.color = Color.color(Math.random(), Math.random(), Math.random());// ColorsTools.colorlist(new
																				// Random().nextInt(17));
		CellsSet.getCapitalsName().forEach((Cn) -> {
			CellsSet.getServicesNames().forEach((Sn) -> {
				this.sensitivty.put((Cn + "_" + Sn), Math.random() > 0.5 ? Math.random() : 0);
			});
		});
		CellsSet.getServicesNames().forEach((Sn) -> {
			this.productivityLevel.put(Sn, LevelIntervale * Math.random());
		});
		this.giveInMean = Math.random();
		this.giveUpMean = Math.random();
		this.giveUpProbabilty = Math.random();
	}


	public Manager(String label) {
		this.label = label;
	}



	@Override
	public String toString() {
		return "AFT [label=" + label + " ,\n sensitivty=" + sensitivty + ",\n productivityLevel=" + productivityLevel
				+ ",\n giveIn=" + giveInMean + ", giveUp=" + giveUpMean + ", giveUpProbabilty=" + giveUpProbabilty
				+ "]";
	}
}
