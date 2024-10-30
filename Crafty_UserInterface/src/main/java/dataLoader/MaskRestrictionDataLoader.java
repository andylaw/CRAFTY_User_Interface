package dataLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import fxmlControllers.MasksPaneController;
import model.Cell;
import model.CellsSet;
import model.Manager;

public class MaskRestrictionDataLoader {

	public static HashMap<String, List<Path>> hashMasksPaths;

	private static final Logger LOGGER = LogManager.getLogger(MaskRestrictionDataLoader.class);

	public static void MaskAndRistrictionLaoderUpdate() {
		hashMasksPaths = new HashMap<>();
		List<File> LandUseControlFolder = PathTools
				.detectFolders(PathsLoader.getProjectPath() + PathTools.asFolder("worlds") + "LandUseControl");
		if (LandUseControlFolder != null) {
			for (File folder : LandUseControlFolder) {
				ArrayList<Path> listOfMaskFilesInScenario = PathTools.fileFilter(true, folder.getAbsolutePath(),
						PathsLoader.getScenario());
				if (listOfMaskFilesInScenario != null) {
					List<Path> maks = new ArrayList<>();
					for (Path file : listOfMaskFilesInScenario) {
						if (!file.toString().contains("Restrictions")) {
							maks.add(file);
						}
					}
					hashMasksPaths.put(folder.getName(), maks);
				} else {
					listOfMaskFilesInScenario = PathTools.fileFilter(true, folder.getAbsolutePath());
					List<Path> maks = new ArrayList<>();
					for (Path csv : listOfMaskFilesInScenario) {
						if (!csv.toString().contains("Restrictions")) {
							maks.add(csv);
						}
						hashMasksPaths.put(folder.getName(), maks);
					}
				}
			}
		}
	}

	public static void MaskAndRistrictionLaoderUpdate(String maskType) {
		ArrayList<Path> listOfMaskFilesInScenario = PathTools.fileFilter(true,
				PathsLoader.getProjectPath() + PathTools.asFolder("worlds") + "LandUseControl",
				PathsLoader.getScenario(), PathTools.asFolder(maskType));
		if (listOfMaskFilesInScenario != null) {
			List<Path> mask = new ArrayList<>();
			for (Path file : listOfMaskFilesInScenario) {
				if (!file.toString().contains("Restrictions")) {
					mask.add(file);
				}
			}
			hashMasksPaths.put(maskType, mask);
		}

	}

	public void CellSetToMaskLoader(String maskType, int year) {
		Path path = hashMasksPaths.get(maskType).stream()
				.filter(filePath -> filePath.toString().contains(String.valueOf(year))).findFirst().orElse(null);
		if (path != null) {
			HashMap<String, ArrayList<String>> csv = ReaderFile.ReadAsaHash(path, true);
			if (csv != null) {
				cleanMaskType(maskType);
				for (int i = 0; i < csv.values().iterator().next().size(); i++) {
					Cell c = CellsSet.getCellsSet().getCell((int) Tools.sToD(csv.get("X").get(i)),
							(int) Tools.sToD(csv.get("Y").get(i)));
					int ii = i;
					csv.keySet().forEach(key -> {
						if (key.contains("Year_") && csv.get(key).get(ii).contains("1")) {
							if (c != null) {
								c.setMaskType(maskType);
								maskToOwner(c, maskType);
							}
						}
					});
				}
				LOGGER.info("Update Mask: " + maskType + "[" + path + "]");
			} else {
				LOGGER.warn("Cannot find the mask files..." + path);
			}
		} else {
			LOGGER.info("Mask file not found for  [" + maskType + " for the year:" + year
					+ "]  use the latest year available");
		}

	}

	void maskToOwner(Cell c, String maskType) {

		for (Manager a : AFTsLoader.getAftHash().values()) {
			if (maskType.contains(a.getLabel())) {
				c.setOwner(a);
				break;
			}
		}
	}

	public void CellSetToMaskLoader(int year) {
		hashMasksPaths.keySet().forEach(maskType -> {
			CellSetToMaskLoader(maskType, year);
			updateRestrections(maskType, year + "", MasksPaneController.restrictions.get(maskType));
		});

	}


	public void cleanMaskType(String maskType) {
		CellsLoader.hashCell.values().parallelStream().forEach(c -> {
			if (c.getMaskType() != null && c.getMaskType().equals(maskType)) {
				c.setMaskType(null);
				if (c.getOwner() != null && maskType.contains(c.getOwner().getLabel())) {
					c.setOwner(null);
				}
			}
		});
	}

	public HashMap<String, Boolean> restrictionsInitialize(String maskType) {
		String[] def = { "LandUseControl", "Restrictions", maskType, ".csv" };
		String[] defInScenario = PathTools.aggregateArrays(def, PathsLoader.getScenario());
		ArrayList<Path> restrictionsFile = PathTools.fileFilter(defInScenario);
		if (restrictionsFile == null || restrictionsFile.isEmpty()) {
			restrictionsFile = PathTools.fileFilter(def);
		}
		return importResrection(restrictionsFile.get(0));
	}

	public void updateRestrections(String maskType, String currentyear, HashMap<String, Boolean> restriction) {
		ArrayList<Path> restrictionsFile = PathTools.fileFilter(currentyear, PathsLoader.getScenario(),
				"LandUseControl", "Restrictions", maskType, ".csv");
		if (restrictionsFile == null || restrictionsFile.isEmpty()) {
			return;
		}
		restriction.clear();
		String[][] matrix = CsvTools.csvReader(restrictionsFile.get(0));
		if (matrix != null) {
			for (int i = 1; i < matrix.length; i++) {
				for (int j = 1; j < matrix[0].length; j++) {
					restriction.put(matrix[i][0] + "_" + matrix[0][j], matrix[i][j].contains("1"));
				}
			}
		}
		LOGGER.info(maskType + " Restrections updated ");
	}

	HashMap<String, Boolean> importResrection(Path path) {
		HashMap<String, Boolean> restric = new HashMap<>();
		String[][] matrix = CsvTools.csvReader(path);
		if (matrix != null) {
			for (int i = 1; i < matrix.length; i++) {
				for (int j = 1; j < matrix[0].length; j++) {
					restric.put(matrix[i][0] + "_" + matrix[0][j], matrix[i][j].contains("1"));
				}
			}
			return restric;
		} else
			return null;
	}

}
