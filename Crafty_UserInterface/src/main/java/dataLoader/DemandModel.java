package dataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.graphicalTools.Tools;
import UtilitiesFx.graphicalTools.WarningWindowes;
import model.CellsSet;
import model.RegionClassifier;

public class DemandModel {
	private static final Logger LOGGER = LogManager.getLogger(DemandModel.class);
	private static ConcurrentHashMap<String, ArrayList<Double>> golbalDemand = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Double>>> demandsRegions = new ConcurrentHashMap<>();

	public static ConcurrentHashMap<String, ArrayList<Double>> getGolbalDemand() {
		return golbalDemand;
	}

	public static ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Double>>> getDemandsRegions() {
		return demandsRegions;
	}

	public static double getGolbalDemand(String key, int year) {
		return getGolbalDemand(key, year, true);
	}

	public static double getGolbalDemand(String key, int year, boolean showMessage) {
		int tick = year - Paths.getStartYear();
		try {
			return golbalDemand.get(key).get(tick);
		} catch (IndexOutOfBoundsException e) {
			tick = golbalDemand.values().iterator().next().size() - 1;
			if (showMessage) {
				LOGGER.warn("There are no demand \'" + key + "\' for this year: \"" + year
						+ "\" using the latest available demands " + (tick + Paths.getStartYear()));
			}
			return golbalDemand.get(key).get(tick);
		}

	}
	
	

	public static double getAveragedPerCellDemand(String key, int index) {

		return golbalDemand.get(key)
				.get(Math.min(index - Paths.getStartYear(), golbalDemand.values().iterator().next().size() - 1))
				/ CellsLoader.getNbrOfCells();
	}
	
	private static String demandPath(){
		AtomicReference<String> path = new AtomicReference<>("");
		try {
			path.set(PathTools.fileFilter(Paths.getScenario(), "\\worlds\\demand", Paths.WorldName).get(0));
		} catch (NullPointerException e) {
			String txt = "The GIS data file name does not match any of the demand file names. \n" +
		             "Please change the file name from: GIS\\" + Paths.WorldName + ".csv " +
		             "to GIS\\" +  "newWorldName_Regions.csv\" and then click continue.";
			LOGGER.warn(txt);
			WarningWindowes.showWarningMessage(txt, "continue", x -> {
				Paths.allfilesPathInData = PathTools.findAllFiles(Paths.getProjectPath());
				String p = PathTools.fileFilter(true, "\\GIS\\").get(0);
				Paths.WorldName = new File(p).getName().replace("_Regions", "").replace(".csv", "");
				path.set(PathTools.fileFilter(Paths.getScenario(), "\\worlds\\demand", Paths.WorldName).get(0));
				RegionClassifier.initialation(false);
			});
		}
		return path.get();
	}

	public static void updateDemand() {

		String path = demandPath();

		HashMap<String, ArrayList<String>> hashDemand = ReaderFile.ReadAsaHash(path);
		LOGGER.info("Update Demand: " + path);
		hashDemand.forEach((name, vect) -> {
			if (CellsSet.getServicesNames().contains(name)) {
				ArrayList<Double> v = new ArrayList<>();
				for (int i = 0; i < Paths.getEndtYear() - Paths.getStartYear() + 1; i++) {
					if (i < vect.size()) {
						v.add(Tools.sToD(vect.get(i)));
					} else {
						v.add(Tools.sToD(vect.get(vect.size() - 1)));
						LOGGER.info("There are no demand \'" + name + "\' for this year: \""
								+ (i + Paths.getStartYear()) + "\" using the latest available demands "
								+ (vect.size() - 1 + Paths.getStartYear()));
					}
				}
				golbalDemand.put(name, v);
			}
		});
	}

	public static void updateRegionsDemand() {
		demandsRegions.clear();
		RegionClassifier.regions.keySet().forEach(r -> {
			updateDemand(r);
		});
	}

	public static boolean isRegionalDemandExisted() {
		for (String r : RegionClassifier.regions.keySet()) {
			ArrayList<String> path = PathTools.fileFilter(Paths.getScenario(), "\\worlds\\demand", r);
			if (path == null) {
				return false;
			}
		}
		return true;
	}

	private static void updateDemand(String region) {
		String path = "";
		try {
			path = PathTools.fileFilter(Paths.getScenario(), "\\worlds\\demand", region).get(0);
		} catch (NullPointerException e) {
			LOGGER.warn("No demand file fund for region: |" + region + "|");
			return;
		}
		HashMap<String, ArrayList<String>> hashDemand = ReaderFile.ReadAsaHash(path);
		LOGGER.info("Update Demand for [" + region + "]: " + path);
		ConcurrentHashMap<String, ArrayList<Double>> demandR = new ConcurrentHashMap<>();
		hashDemand.forEach((name, vect) -> {
			if (CellsSet.getServicesNames().contains(name)) {
				ArrayList<Double> v = new ArrayList<>();
				for (int i = 0; i < Paths.getEndtYear() - Paths.getStartYear() + 1; i++) {
					if (i < vect.size()) {
						v.add(Tools.sToD(vect.get(i)));
					} else {
						v.add(Tools.sToD(vect.get(vect.size() - 1)));
						LOGGER.info("There are no demand \'" + name + "\' for this year: \""
								+ (i + Paths.getStartYear()) + "\" using the latest available demands "
								+ (vect.size() - 1 + Paths.getStartYear()));
					}
				}
				demandR.put(name, v);
			}
		});
		demandsRegions.put(region, demandR);
	}

	public static double getRegionalDemand(String key, int year, String region) {
		if (demandsRegions.get(region) == null) {
			LOGGER.fatal("FATAL: no demands for this region fund :  |" + region
					+ "|  demands for this region will all be = 0");
			return 0;
		}
		int tick = year - Paths.getStartYear();
		try {
			return demandsRegions.get(region).get(key).get(tick);
		} catch (IndexOutOfBoundsException e) {
			tick = demandsRegions.get(region).values().iterator().next().size() - 1;

			return demandsRegions.get(region).get(key).get(tick);
		}
	}
}
