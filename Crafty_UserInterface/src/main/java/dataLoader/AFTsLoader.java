package dataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.ReaderFile;
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

public class AFTsLoader extends HashSet<Manager> {

	private static final Logger LOGGER = LogManager.getLogger(AFTsLoader.class);
	private static final long serialVersionUID = 1L;
	private static ConcurrentHashMap<String, Manager> hash = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, Manager> activateAFTsHash = new ConcurrentHashMap<>();

	public static ConcurrentHashMap<String, Integer> hashAgentNbr=new ConcurrentHashMap<>();

	public AFTsLoader() {
		initializeAFTs();
		updateAftTypes();
		addAll(hash.values());
		hash.entrySet().stream().filter(entry -> entry.getValue().isActive())
				.forEach(entry -> activateAFTsHash.put(entry.getKey(), entry.getValue()));

		agentsColorinitialisation();
	}

	public void agentsColorinitialisation() {
		List<String> colorFiles = PathTools.fileFilter("\\csv\\", "AFTsMetaData");
		if (colorFiles.size() > 0) {
			HashMap<String, ArrayList<String>> T = ReaderFile.ReadAsaHash(colorFiles.iterator().next());

			forEach(a -> {
				for (int i = 0; i < T.get("Color").size(); i++) {
					if (T.get("Label").get(i).equalsIgnoreCase(a.getLabel())) {
						a.setColor(Color.web(T.get("Color").get(i)));
						if (T.keySet().contains("Name")) {
							a.setCompleteName(T.get("Name").get(i));
						} else {
							a.setCompleteName("--");
						}
					}
				}
			});
		}

	}

	public void updateColorsInputData() {
		List<String> colorFiles = PathTools.fileFilter("\\csv\\", "AFTsMetaData");
		if (colorFiles.size() > 0) {
			HashMap<String, ArrayList<String>> T = ReaderFile.ReadAsaHash(colorFiles.iterator().next());
			ArrayList<String> tmp = new ArrayList<>();
			forEach(a -> {
				for (int i = 0; i < T.get("Color").size(); i++) {
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
		System.out.println(Paths.getScenario());
		List<String> pFiles = PathTools.fileFilter("\\production\\", Paths.getScenario());
		pFiles.forEach(f -> {
			initializeAFTProduction(f);
		});

		List<String> bFiles = PathTools.fileFilter("\\agents\\", Paths.getScenario());
		bFiles.forEach(f -> {
			initializeAFTBehevoir(f);
		});
		checkAFTsBehevoireParametres(bFiles);
	}

	public void updateAFTs() {
		List<String> pFiles = PathTools.fileFilter("\\production\\", Paths.getScenario());
		pFiles.forEach(f -> {
			File file = new File(f);
			updateAFTProduction(hash.get(file.getName().replace(".csv", "")), file);
		});
		List<String> bFiles = PathTools.fileFilter("\\agents\\", Paths.getScenario());
		bFiles.forEach(f -> {
			File file = new File(f);
			try {
				updateAFTBehevoir(hash.get(file.getName().replace(".csv", "").replace("AftParams_", "")), file);
			} catch (NullPointerException e) {
				LOGGER.error("AFT Not in the List: " + file);
			}
		});
		checkAFTsBehevoireParametres(bFiles);
	}

	public void initializeAFTBehevoir(String aftPath) {
		File file = new File(aftPath);
		Manager a = hash.get(file.getName().replace(".csv", "").replace("AftParams_", ""));
		updateAFTBehevoir(a, file);
	}

	void checkAFTsBehevoireParametres(List<String> bFiles) {

		List<String> bf = new ArrayList<>();
		bFiles.forEach(f -> {
			bf.add(new File(f).getName().replace(".csv", "").replace("AftParams_", ""));
		});
		hash.keySet().forEach(label -> {
			if (!bf.contains(label)) {
				LOGGER.warn("no behevoir parametrs for the AFT:  " + label);
			}
		});
	}

	public static void updateAFTBehevoir(Manager a, File file) {
		HashMap<String, ArrayList<String>> reder = ReaderFile.ReadAsaHash(file.getAbsolutePath());
		a.setGiveInMean(Tools.sToD(reder.get("givingInDistributionMean").get(0)));
		a.setGiveUpMean(Tools.sToD(reder.get("givingUpDistributionMean").get(0)));
		a.setGiveInSD(Tools.sToD(reder.get("givingInDistributionSD").get(0)));
		a.setGiveUpSD(Tools.sToD(reder.get("givingUpDistributionSD").get(0)));
		a.setServiceLevelNoiseMin(Tools.sToD(reder.get("serviceLevelNoiseMin").get(0)));
		a.setServiceLevelNoiseMax(Tools.sToD(reder.get("serviceLevelNoiseMax").get(0)));
		a.setGiveUpProbabilty(Tools.sToD(reder.get("givingUpProb").get(0)));
	}

	public void initializeAFTProduction(String aftPath) {
		File file = new File(aftPath);
		Manager a = new Manager(file.getName().replace(".csv", ""));
		a.setColor(Color.color(Math.random(), Math.random(), Math.random()));
		hash.put(a.getLabel(), a);
		updateAFTProduction(a, file);

	}

	void updateAftTypes() {// mask or AFT
		String aftsmetadataPath = PathTools.fileFilter("\\csv\\", "AFTsMetaData").iterator().next();
		HashMap<String, ArrayList<String>> matrix = ReaderFile.ReadAsaHash(aftsmetadataPath);
		if (matrix.get("Type") != null) {
			for (int i = 0; i < matrix.get("Label").size(); i++) {
				String label = matrix.get("Label").get(i);
				hash.get(label).setActive(matrix.get("Type").get(i).equals("AFT"));
			}

			hash.values().forEach(a -> {
				System.out.println(a.getLabel() + "-->" + a.isActive());
			});
		}
	}

	public static void updateSensitivty(Manager a, File file) {
		Table T = Table.read().csv(file);
		CellsSet.getCapitalsName().forEach((Cn) -> {
			CellsSet.getServicesNames().forEach((Sn) -> {
				a.getSensitivity().put((Cn + "_" + Sn), Tools.sToD(T.column(Cn).getString(T.column(0).indexOf(Sn))));
			});
		});
	}

	public static void updateAFTProduction(Manager a, File file) {
		HashMap<String, ArrayList<String>> matrix = ReaderFile.ReadAsaHash(file.getAbsolutePath());
		String c0 = matrix.keySet().contains("C0") ? "C0" : "Unnamed: 0";

		for (int i = 0; i < matrix.get(c0).size(); i++) {
			if (CellsSet.getServicesNames().contains(matrix.get(c0).get(i))) {
				a.getProductivityLevel().put(matrix.get(c0).get(i), Tools.sToD(matrix.get("Production").get(i)));
			} else {
				LOGGER.warn(matrix.get(c0).get(i) + "  is not existe in Services List, will be ignored");
			}
		}
		LOGGER.info(a.getLabel() + " -> ProductivityLevel= " + a.getProductivityLevel());
		updateSensitivty(a, file);
	}

	public static void hashAgentNbr() {
		hashAgentNbr.clear();
		CellsLoader.hashCell.values().parallelStream().forEach(c -> {
			if (c.getOwner() != null)
				hashAgentNbr.merge(c.getOwner().getLabel(), 1, Integer::sum);
		});
	}

	public static ConcurrentHashMap<String, Manager> getAftHash() {
		return hash;
	}
	

	public static ConcurrentHashMap<String, Manager> getActivateAFTsHash() {
		return activateAFTsHash;
	}


	public static Manager getRandomAFT() {
		return getRandomAFT(activateAFTsHash.values());
	}

	public static Manager getRandomAFT(Collection<Manager> afts) {
		if (afts.size() != 0) {
			int index = ThreadLocalRandom.current().nextInt(afts.size());
			Manager aft = afts.stream().skip(index).findFirst().orElse(null);
				return aft;
		}
		return null;// select from outside "hash"
	}

}
