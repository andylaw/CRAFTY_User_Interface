package dataLoader;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		int tick = year - PathsLoader.getStartYear();
		try {
			return golbalDemand.get(key).get(tick);
		} catch (IndexOutOfBoundsException e) {
			tick = golbalDemand.values().iterator().next().size() - 1;
			if (showMessage) {
				LOGGER.warn("There are no demand \'" + key + "\' for this year: \"" + year
						+ "\" using the latest available demands " + (tick + PathsLoader.getStartYear()));
			}
			return golbalDemand.get(key).get(tick);
		}

	}

	public static double getAveragedPerCellDemand(String key, int index) {

		return golbalDemand.get(key)
				.get(Math.min(index - PathsLoader.getStartYear(), golbalDemand.values().iterator().next().size() - 1))
				/ CellsLoader.getNbrOfCells();
	}

	private static Path demandPath() {
		AtomicReference<String> path = new AtomicReference<>("");
		try {
			path.set(
					PathTools
							.fileFilter(PathsLoader.getScenario(),
									PathTools.asFolder("worlds") + "demand", PathsLoader.WorldName)
							.get(0).toString());
		} catch (NullPointerException e) {
			String txt = "The GIS data file name does not match any of the demand file names. \n"
					+ "Please change the file name from: GIS" + File.separator + "" + PathsLoader.WorldName + ".csv "
					+ "to GIS" + File.separator + "" + "newWorldName_Regions.csv\" and then click continue.";
			LOGGER.warn(txt);
			WarningWindowes.showWarningMessage(txt, "continue", x -> {
				PathsLoader.allfilesPathInData = PathTools.findAllFiles(PathsLoader.getProjectPath());
				Path p = PathTools.fileFilter(true, File.separator + "GIS" + File.separator + "").get(0);
				PathsLoader.WorldName = p.toFile().getName().replace("_Regions", "").replace(".csv", "");
				path.set(PathTools.fileFilter(PathsLoader.getScenario(), PathTools.asFolder("worlds") + "demand",
						PathsLoader.WorldName).get(0).toString());
				RegionClassifier.initialation(false);
			});
		}
		return Paths.get(path.get());
	}

	public static void updateDemand() {

		Path path = demandPath();

		HashMap<String, ArrayList<String>> hashDemand = ReaderFile.ReadAsaHash(path);
		LOGGER.info("Update Demand: " + path);
		hashDemand.forEach((name, vect) -> {
			if (CellsSet.getServicesNames().contains(name)) {
				ArrayList<Double> v = new ArrayList<>();
				for (int i = 0; i < PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 1; i++) {
					if (i < vect.size()) {
						v.add(Tools.sToD(vect.get(i)));
					} else {
						v.add(Tools.sToD(vect.get(vect.size() - 1)));
						LOGGER.info("There are no demand \'" + name + "\' for this year: \""
								+ (i + PathsLoader.getStartYear()) + "\" using the latest available demands "
								+ (vect.size() - 1 + PathsLoader.getStartYear()));
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
			ArrayList<Path> paths = PathTools.fileFilter(PathsLoader.getScenario(),
					File.separator + "worlds" + File.separator + "demand", r);
			if (paths == null) {
				LOGGER.warn("Regionalization is not possible. The demand for the region "+r+" does not exist.");
				return false;
			}
		}
		return true;
	}

	private static void updateDemand(String region) {
		Path path;
		try {
			path = PathTools.fileFilter(PathsLoader.getScenario(),
					File.separator + "worlds" + File.separator + "demand", region).get(0);
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
				for (int i = 0; i < PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 1; i++) {
					if (i < vect.size()) {
						v.add(Tools.sToD(vect.get(i)));
					} else {
						v.add(Tools.sToD(vect.get(vect.size() - 1)));
						LOGGER.info("There are no demand \'" + name + "\' for this year: \""
								+ (i + PathsLoader.getStartYear()) + "\" using the latest available demands "
								+ (vect.size() - 1 + PathsLoader.getStartYear()));
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
		int tick = year - PathsLoader.getStartYear();
		try {
			return demandsRegions.get(region).get(key).get(tick);
		} catch (IndexOutOfBoundsException e) {
			tick = demandsRegions.get(region).values().iterator().next().size() - 1;

			return demandsRegions.get(region).get(key).get(tick);
		}
	}
}
