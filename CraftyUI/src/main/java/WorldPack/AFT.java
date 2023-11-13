package WorldPack;

import java.util.HashMap;
import java.util.Random;

import javafx.scene.paint.Color;

public class AFT {
	public String label;
	HashMap<String, Double> sensitivty = new HashMap<>();
	HashMap<String, Double> productivityLevel = new HashMap<>();
	double giveIn, giveUp, giveUpProbabilty;
	public Color color;

	AFT(String label) {
		this.label = label;
	}
	public AFT(AFT other) {
		this(other,0);
	}
	public AFT(AFT other, double intervale) {
		if(other!=null) {
		this.label = other.label;
        this.color=other.color;
        
        other.sensitivty.forEach((n, v) -> {
        	this.sensitivty.put(n, Math.max(v + v * intervale * (new Random().nextDouble(2) - 1), 0));
		});
        other.productivityLevel.forEach((n, v) -> {
        	this.productivityLevel.put(n, Math.max(v + v * intervale * (new Random().nextDouble(2) - 1), 0));
		});
        this.giveIn = Math
				.max(other.giveIn +other.giveIn * intervale * (new Random().nextDouble(2) - 1), 0);
        this.giveUp = Math
				.max(other.giveUp + other.giveUp * intervale * (new Random().nextDouble(2) - 1), 0);
        this.giveUpProbabilty = Math.max(other.giveUpProbabilty
				+ other.giveUpProbabilty * intervale * (new Random().nextDouble(2) - 1), 0);

    }
}
}
