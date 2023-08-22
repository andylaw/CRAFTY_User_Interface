package WorldPack;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import UtilitiesFx.CsvTools;
import UtilitiesFx.Path;
import UtilitiesFx.Tools;
import javafx.scene.paint.Color;


public class Agents {

	public static HashMap<String, AFT> aftReSet = new HashMap<>();
	
	
	
	
	public static HashMap<String, Double> hashAgentNbr() {
		HashMap<String, Double> hashAgentNbr = new HashMap<>();
		Lattice.P.forEach(p->{
			if(p.owner!=null)
				if(hashAgentNbr.containsKey(p.owner.label)) {
				hashAgentNbr.put(p.owner.label, hashAgentNbr.get(p.owner.label)+1);}
				else {
					hashAgentNbr.put(p.owner.label,1.);
				}
		});
		return hashAgentNbr;}
	

	

	
	void initialseAFT() {
		List<String> pFiles=Path.fileFilter("\\production\\",  Path.scenario);
		pFiles.forEach(f -> {
			File file= new File(f);
			AFT a = new AFT(file.getName().replace(".csv", ""));
			a.color = Color.color(Math.random(), Math.random(), Math.random());
			aftReSet.put(a.label, a);

			HashMap<String, String[]> data = CsvTools.ReadAsaHash(file.getAbsolutePath());
			String[] pr = data.get("production");
			String[] cpi = data.get("");
			for (int i = 1; i < cpi.length; i++) {
				a.productivityLevel.put(cpi[i].toLowerCase().replace(" ",""), Tools.sToD(pr[i]));
			}
			data.forEach((name, vect) -> {
				if (!name.equals("") && !name.equals("production")) {
					for (int i = 1; i < vect.length; i++) {
						a.sensitivty.put((name+"_"+data.get("")[i]).toLowerCase().replace(" ", ""),Tools.sToD(vect[i]));
					}
				}
			});
		});

		List<String> bFiles=Path.fileFilter("\\agents\\",  Path.scenario);
		

		bFiles.forEach(f -> {
			File file= new File(f);
			AFT a = aftReSet.get(file.getName().replace(".csv", "").replace("AftParams_", ""));
			HashMap<String, String[]> reder = CsvTools.ReadAsaHash(file.getAbsolutePath());
			a.giveIn = Tools.sToD(reder.get("givingInDistributionMean".toLowerCase())[1]);
			a.giveUp = Tools.sToD(reder.get("givingUpDistributionMean".toLowerCase())[1]);
			a.giveUpProbabilty = Tools.sToD(reder.get("givingUpProb".toLowerCase())[1]);
		});
	}


}
