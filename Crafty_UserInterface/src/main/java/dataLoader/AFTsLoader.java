package dataLoader;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import UtilitiesFx.filesTools.CsvTools;
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
			HashMap<String, String[]> T = CsvTools.ReadAsaHash(colorFiles.iterator().next());
			
			forEach(a -> {
				for (int i = 0; i < T.get("Color").length; i++) {
					if (T.get("Label")[i].equalsIgnoreCase(a.getLabel())) {
						a.setColor(Color.web(T.get("Color")[i]));
						if (T.keySet().contains("Name")) {
							a.setCompleteName(T.get("Name")[i]) ;
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
			HashMap<String, String[]> T = CsvTools.ReadAsaHash(colorFiles.iterator().next());
			String[] tmp = new String[size()];
			forEach( a -> {
				for (int i = 0; i < T.get("Color").length; i++) {
					System.out.println(T.get("Label")[i]+" ?-> "+ a.getLabel());
					if (T.get("Label")[i].replace("	", "").equalsIgnoreCase(a.getLabel())) {
						tmp[i] = ColorsTools.toHex(a.getColor());
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
					writer[j + 1][i] = T.get(writer[0][i])[j].replace(",", ".");
				}
			}
			CsvTools.writeCSVfile(writer, colorFiles.iterator().next());
		}
	}

	private void initializeAFTs() {
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
		HashMap<String, String[]> reder = CsvTools.ReadAsaHash(file.getAbsolutePath());
		a.setGiveInMean(Tools.sToD(reder.get("givingInDistributionMean")[0]));
		a.setGiveUpMean (Tools.sToD(reder.get("givingUpDistributionMean")[0]));
		a.setGiveInSD (Tools.sToD(reder.get("givingInDistributionSD")[0]));
		a.setGiveUpSD ( Tools.sToD(reder.get("givingUpDistributionSD")[0]));
		a.setServiceLevelNoiseMin ( Tools.sToD(reder.get("serviceLevelNoiseMin")[0]));
		a.setServiceLevelNoiseMax ( Tools.sToD(reder.get("serviceLevelNoiseMax")[0]));
		a.setGiveUpProbabilty ( Tools.sToD(reder.get("givingUpProb")[0]));
	}
	
	public  void initializeAFTProduction(String aftPath) {
		File file = new File(aftPath);
		Manager a = new Manager(file.getName().replace(".csv", ""));
		a.setColor(Color.color(Math.random(), Math.random(), Math.random()));
		hash.put(a.getLabel(), a);
		updateAFTProduction( a, file);
		
	}
	public static void updateAFTProduction2(Manager a, File file) {
		Table T = Table.read().csv(file);
		CellsSet.getCapitalsName().forEach((Cn) -> {
			CellsSet.getServicesNames().forEach((Sn) -> {
				a.getSensitivty().put((Cn + "_" + Sn), Tools.sToD(T.column(Cn).getString(T.column(0).indexOf(Sn))));
			});
		});
	}
	
	public static void 	updateAFTProduction(Manager a, File file) {
		HashMap<String, String[]> matrix = CsvTools.ReadAsaHash(file.getAbsolutePath());
		for (int i = 0; i < matrix.get("C0").length; i++) {
			if(CellsSet.getServicesNames().contains(matrix.get("C0")[i]))
				a.getProductivityLevel().put(matrix.get("C0")[i], Tools.sToD(matrix.get("Production")[i]));
			else {System.out.println(matrix.get("C0")[i]+"  is not existe in Services List");}
		}
		updateAFTProduction2(a,file);
	}
	
	
	public static HashMap<String, Double> hashAgentNbr() {
		HashMap<String, Double> hashAgentNbr = new HashMap<>();
		CellsSet.getCellsSet().forEach(p -> {
			if (p.getOwner() != null)
				if (hashAgentNbr.containsKey(p.getOwner().getLabel())) {
					hashAgentNbr.put(p.getOwner().getLabel(), hashAgentNbr.get(p.getOwner().getLabel()) + 1);
				} else {
					hashAgentNbr.put(p.getOwner().getLabel(), 1.);
				}
		});
		return hashAgentNbr;
	}
	
	public  HashMap<String, Manager> getAftHash() {
		return hash;
	}

}
