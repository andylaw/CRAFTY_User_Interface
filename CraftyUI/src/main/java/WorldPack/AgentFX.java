package WorldPack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javafx.scene.paint.Color;

public class AgentFX {
	
	public String label;
	int index;
	public Set<Patch> Mypaches =new HashSet<>();
	public String [][] productionMatrix; 
	public String[] [] aftParamIdTable;
	public Color color= Color.rgb(new Random().nextInt(255),new Random().nextInt(255), new Random().nextInt(255));



	public AgentFX(String label) {
		this.label = label;
	}

	



	@Override
	public String toString() {
		return "AgentFX [label=" + label + "\n, index=" + index + "\n, Mypaches=" + Mypaches + "\n, sensetivitymatrix="
				+ Arrays.toString(productionMatrix) + "\n, aftParamId=" + Arrays.toString(aftParamIdTable) + "]";
	}

	
}
