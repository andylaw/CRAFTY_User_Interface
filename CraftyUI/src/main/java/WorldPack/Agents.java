package WorldPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import CameraPack.Camera;
import Main.Main_CraftyFx;
import UtilitiesFx.CsvTools;
import UtilitiesFx.Path;

import javafx.stage.Stage;

public class Agents {

	public List<AgentFX> AFT = new ArrayList<>();
	public Map M;
	
	public Agents (Map M) {
		this.M=M;
		AgnetsName();
	}
	
	
	

	public void initialise() {
		M.creatMap(Main_CraftyFx.camera);
		M.creatMapGIS();
		AgnetsDataImport();
		
	}
	
	public void AgnetsDataImport() {
		AFT.forEach(agent -> {
			agent.productionMatrix = CsvTools
					.csvReader(Path.fileFilter("\\production\\", agent.label, Path.senario).get(0));
			agent.aftParamIdTable = CsvTools.csvReader(Path.fileFilter("\\agents\\", agent.label, Path.senario).get(0));
		});
	}
	
	 void AgnetsName() {
		 HashSet<String> AgentNameList = new HashSet<>(Path.nameOfFile("\\production\\") );
		AgentNameList.forEach(str->{
						AFT.add(new AgentFX(str));

			});
	}
	
	
	public HashMap<String, Double> nbrPa (){
		HashMap<String,Double> AgentHasPatch= new HashMap<>();
		AFT.forEach(a->{
			AgentHasPatch.put(a.label, (double) a.Mypaches.size());
		});
		return AgentHasPatch;}

}
