package dataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.FileReder;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.Tools;
import javafx.scene.paint.Color;
import model.Manager;
import model.CellsSet;
import tech.tablesaw.api.Table;

/**
 * @author Mohamed Byari
 *
 */

public class AFTsLoader extends HashSet<Manager>{


	private static final long serialVersionUID = 1L;
	private  HashMap<String, Manager> hash = new HashMap<>();


	public AFTsLoader() {
		initializeAFTs();
		addAll(hash.values());
		agentsColorinitialisation();
	}
	public  void agentsColorinitialisation(){
		List<String> colorFiles = PathTools.fileFilter("\\csv\\", "AFTsMetaData");
		if (colorFiles.size() > 0) {
			HashMap<String, ArrayList<String>> T = FileReder.ReadAsaHash(colorFiles.iterator().next());
			
			forEach(a -> {
				for (int i = 0; i < T.get("Color").size(); i++) {
					if (T.get("Label").get(i).equalsIgnoreCase(a.getLabel())) {
						a.setColor(Color.web(T.get("Color").get(i)));
						if (T.keySet().contains("Name")) {
							a.setCompleteName(T.get("Name").get(i)) ;
						} else {
							a.setCompleteName("--");
						}
					}
				}
			});
		}
		
	}

	public  void updateColorsInputData(){
		List<String> colorFiles = PathTools.fileFilter("\\csv\\", "AFTsMetaData");
		if (colorFiles.size() > 0) {
			HashMap<String, ArrayList<String>> T = FileReder.ReadAsaHash(colorFiles.iterator().next());
			ArrayList<String> tmp = new ArrayList<>();
			forEach( a -> {
				for (int i = 0; i < T.get("Color").size(); i++) {
					System.out.println(T.get("Label").get(i)+" ?-> "+ a.getLabel());
					if (T.get("Label").get(i).replace("	", "").equalsIgnoreCase(a.getLabel())) {
						tmp.add(ColorsTools.toHex(a.getColor()));
					}
				}
			});
			T.put("Color", tmp);
			String[][] writer = new String[size() + 1][T.size()];
			for (int i = 0; i < writer[0].length; i++) {
				writer[0][i] = (String) T.keySet().toArray()[i];
			}
			for (int i = 0; i < writer[0].length; i++) {
				for (int j = 0; j < size(); j++) {
					writer[j + 1][i] = T.get(writer[0][i]).get(j).replace(",", ".");
				}
			}
			CsvTools.writeCSVfile(writer, colorFiles.iterator().next());
		}
	}

	 void initializeAFTs() {
		hash.clear();
		List<String> pFiles = PathTools.fileFilter("\\production\\", Paths.getScenario());
		pFiles.forEach(f -> {
			initializeAFTProduction(f);
		});
		
		
		List<String> bFiles = PathTools.fileFilter("\\agents\\", Paths.getScenario());
		bFiles.forEach(f -> {
			initializeAFTBehevoir(f);
		});
	}
	
	public  void updateAFTs() {
		List<String> pFiles = PathTools.fileFilter("\\production\\", Paths.getScenario());
		pFiles.forEach(f -> {
			File file = new File(f);
			updateAFTProduction(hash.get(file.getName().replace(".csv", "")),  file) ;
		});
		List<String> bFiles = PathTools.fileFilter("\\agents\\", Paths.getScenario());
		bFiles.forEach(f -> {
			File file = new File(f);
			updateAFTBehevoir(hash.get(file.getName().replace(".csv", "").replace("AftParams_", "")), file);
		});
	}

	public  void initializeAFTBehevoir(String aftPath) {
		File file = new File(aftPath);
		Manager a = hash.get(file.getName().replace(".csv", "").replace("AftParams_", ""));
		updateAFTBehevoir( a, file);
	}
	public static void updateAFTBehevoir(Manager a,File file) {
		HashMap<String, ArrayList<String>> reder = FileReder.ReadAsaHash(file.getAbsolutePath());
		a.setGiveInMean(Tools.sToD(reder.get("givingInDistributionMean").get(0)));
		a.setGiveUpMean (Tools.sToD(reder.get("givingUpDistributionMean").get(0)));
		a.setGiveInSD (Tools.sToD(reder.get("givingInDistributionSD").get(0)));
		a.setGiveUpSD ( Tools.sToD(reder.get("givingUpDistributionSD").get(0)));
		a.setServiceLevelNoiseMin ( Tools.sToD(reder.get("serviceLevelNoiseMin").get(0)));
		a.setServiceLevelNoiseMax ( Tools.sToD(reder.get("serviceLevelNoiseMax").get(0)));
		a.setGiveUpProbabilty ( Tools.sToD(reder.get("givingUpProb").get(0)));
	}
	
	public  void initializeAFTProduction(String aftPath) {
		File file = new File(aftPath);
		Manager a = new Manager(file.getName().replace(".csv", ""));
		a.setColor(Color.color(Math.random(), Math.random(), Math.random()));
		hash.put(a.getLabel(), a);
		updateAFTProduction( a, file);
	
	}
	public static void updateSensitivty(Manager a, File file) {
		Table T = Table.read().csv(file);
		CellsSet.getCapitalsName().forEach((Cn) -> {
			CellsSet.getServicesNames().forEach((Sn) -> {
				a.getSensitivty().put((Cn + "_" + Sn), Tools.sToD(T.column(Cn).getString(T.column(0).indexOf(Sn))));
			});
		});
	}
	
	public static void 	updateAFTProduction(Manager a, File file) {
		HashMap<String, ArrayList<String>> matrix = FileReder.ReadAsaHash(file.getAbsolutePath());
		
		for (int i = 0; i < matrix.get("C0").size(); i++) {
			if(CellsSet.getServicesNames().contains(matrix.get("C0").get(i))) {//
				a.getProductivityLevel().put(matrix.get("C0").get(i), Tools.sToD(matrix.get("Production").get(i)));}
			else {System.out.println(matrix.get("C0").get(i)+"  is not existe in Services List");}
		}
//		System.out.println(a.getLabel()+ " -> ProductivityLevel= "+ a.getProductivityLevel().keySet());
		updateSensitivty(a,file);
	}
	
	
	public static HashMap<String, Integer> hashAgentNbr() {
		HashMap<String, Integer> hashAgentNbr = new HashMap<>();
		CellsSet.getCells().forEach(p -> {
			if (p.getOwner() != null)
				if (hashAgentNbr.containsKey(p.getOwner().getLabel())) {
					hashAgentNbr.put(p.getOwner().getLabel(), hashAgentNbr.get(p.getOwner().getLabel()) + 1);
				} else {
					hashAgentNbr.put(p.getOwner().getLabel(), 1);
				}
		});
		return hashAgentNbr;
	}
	
	public  HashMap<String, Manager> getAftHash() {
		return hash;
	}

}
