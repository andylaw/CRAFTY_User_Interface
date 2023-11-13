package dataLoader;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.Tools;
import javafx.scene.paint.Color;
import model.AFT;
import model.Lattice;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;

/**
 * @author Mohamed Byari
 *
 */
public class AFTsLoader {

	public static HashMap<String, AFT> aftReSet = new HashMap<>();

	public static void agentsColorinitialisation() {
		List<String> colorFiles = PathTools.fileFilter("\\csv\\", "Colors");
		if (colorFiles.size() > 0) {
			HashMap<String, String[]> T = CsvTools.ReadAsaHash(colorFiles.iterator().next());
			System.out.println(T);
			aftReSet.forEach((label, a) -> {
				for (int i = 0; i < T.get("Color").length; i++) {
					if (T.get("Label")[i].equalsIgnoreCase(label)) {
						a.setColor(Color.web(T.get("Color")[i]));
						if (T.keySet().contains("Name")) {
							a.setName(T.get("Name")[i]) ;
						} else {
							a.setName("--");
						}
					}
				}
			});
		}
	}

	public static void updateColorsInputData(){
		List<String> colorFiles = PathTools.fileFilter("\\csv\\", "Colors");
		if (colorFiles.size() > 0) {
			HashMap<String, String[]> T = CsvTools.ReadAsaHash(colorFiles.iterator().next());
			String[] tmp = new String[aftReSet.size()];
			aftReSet.forEach((label, a) -> {
				for (int i = 0; i < T.get("Color").length; i++) {
					if (T.get("Label")[i].equalsIgnoreCase(label)) {
						tmp[i] = ColorsTools.toHex(a.getColor());
					}
				}
			});
			T.put("Color", tmp);
			String[][] writer = new String[aftReSet.size() + 1][T.size()];
			for (int i = 0; i < writer[0].length; i++) {
				writer[0][i] = (String) T.keySet().toArray()[i];
			}
			for (int i = 0; i < writer[0].length; i++) {
				for (int j = 0; j < aftReSet.size(); j++) {
					writer[j + 1][i] = T.get(writer[0][i])[j].replace(",", ".");
				}
			}
			CsvTools.writeCSVfile(writer, colorFiles.iterator().next());
		}
	}

	public static void initializeAFTs() {
	//	aftReSet.clear();
		List<String> pFiles = PathTools.fileFilter("\\production\\", Paths.getScenario());
		pFiles.forEach(f -> {
			initializeAFTProduction(f);
		});
		List<String> bFiles = PathTools.fileFilter("\\agents\\", Paths.getScenario());
		bFiles.forEach(f -> {
			initializeAFTBehevoir(f);
		});
		agentsColorinitialisation();
	}
	
	public static void updateAFTs() {
		List<String> pFiles = PathTools.fileFilter("\\production\\", Paths.getScenario());
		pFiles.forEach(f -> {
			File file = new File(f);
			updateAFTProduction(aftReSet.get(file.getName().replace(".csv", "")),  file) ;
		});
		List<String> bFiles = PathTools.fileFilter("\\agents\\", Paths.getScenario());
		bFiles.forEach(f -> {
			File file = new File(f);
			updateAFTBehevoir(aftReSet.get(file.getName().replace(".csv", "").replace("AftParams_", "")), file);
		});
	}

	public static void initializeAFTBehevoir(String aftPath) {
		File file = new File(aftPath);
		AFT a = aftReSet.get(file.getName().replace(".csv", "").replace("AftParams_", ""));
		updateAFTBehevoir( a, file);
	}
	public static void updateAFTBehevoir(AFT a,File file) {
		HashMap<String, String[]> reder = CsvTools.ReadAsaHash(file.getAbsolutePath());
		a.setGiveInMean(Tools.sToD(reder.get("givingInDistributionMean")[0]));
		a.setGiveUpMean (Tools.sToD(reder.get("givingUpDistributionMean")[0]));
		a.setGiveInSD (Tools.sToD(reder.get("givingInDistributionSD")[0]));
		a.setGiveUpSD ( Tools.sToD(reder.get("givingUpDistributionSD")[0]));
		a.setServiceLevelNoiseMin ( Tools.sToD(reder.get("serviceLevelNoiseMin")[0]));
		a.setServiceLevelNoiseMax ( Tools.sToD(reder.get("serviceLevelNoiseMax")[0]));
		a.setGiveUpProbabilty ( Tools.sToD(reder.get("givingUpProb")[0]));
	}
	
	public static void initializeAFTProduction(String aftPath) {
		File file = new File(aftPath);
		AFT a = new AFT(file.getName().replace(".csv", ""));
		a.setColor(Color.color(Math.random(), Math.random(), Math.random()));
		aftReSet.put(a.getLabel(), a);
		updateAFTProduction( a, file);
	}
	public static void updateAFTProduction(AFT a, File file) {
		Table T = Table.read().csv(file);
		Column<?> pr = T.column("Production");
		for (int i = 0; i < Lattice.getServicesNames().size(); i++) {
			a.getProductivityLevel().put(Lattice.getServicesNames().get(i), Tools.sToD(pr.getString(i)));
		}
		Lattice.getCapitalsName().forEach((Cn) -> {
			Lattice.getServicesNames().forEach((Sn) -> {
				a.getSensitivty().put((Cn + "_" + Sn), Tools.sToD(T.column(Cn).getString(T.column(0).indexOf(Sn))));
			});
		});
	}
	
	
	public static HashMap<String, Double> hashAgentNbr() {
		HashMap<String, Double> hashAgentNbr = new HashMap<>();
		Lattice.getCellsSet().forEach(p -> {
			if (p.getOwner() != null)
				if (hashAgentNbr.containsKey(p.getOwner().getLabel())) {
					hashAgentNbr.put(p.getOwner().getLabel(), hashAgentNbr.get(p.getOwner().getLabel()) + 1);
				} else {
					hashAgentNbr.put(p.getOwner().getLabel(), 1.);
				}
		});
		return hashAgentNbr;
	}
}
