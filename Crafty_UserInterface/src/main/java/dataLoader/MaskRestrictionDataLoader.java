package dataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import model.Cell;
import model.CellsSet;
import model.Manager;

public class MaskRestrictionDataLoader {

	public static HashMap<String, List<String>> hashMasks;

	private static final Logger LOGGER = LogManager.getLogger(MaskRestrictionDataLoader.class);

	public static void MaskAndRistrictionLaoderUpdate() {
		hashMasks = new HashMap<>();
		List<File> LandUseControlFolder = PathTools.detectFolders(Paths.getProjectPath() + "\\worlds\\LandUseControl");
		if (LandUseControlFolder != null) {
			for (File folder : LandUseControlFolder) {
				ArrayList<String> listOfMaskFilesInScenario = PathTools.fileFilter(true, folder.getAbsolutePath(),
						Paths.getScenario());
				if (listOfMaskFilesInScenario != null) {
					List<String> maks = new ArrayList<>();
					for (String file : listOfMaskFilesInScenario) {
						maks.add(file);
					}
					hashMasks.put(folder.getName(), maks);
				} else {
					listOfMaskFilesInScenario = PathTools.fileFilter(true, folder.getAbsolutePath());

					List<String> maks = new ArrayList<>();
					for (String csv : listOfMaskFilesInScenario) {
						if (!csv.contains("Restrictions")) {
							maks.add(csv);
						}
						hashMasks.put(folder.getName(), maks);
					}
				}
			}

		}
	}

	public void CellSetToMaskLoader(String maskType, int year) {
		String path = hashMasks.get(maskType).stream().filter(filePath -> filePath.contains(String.valueOf(year)))
				.findFirst().orElse(null);
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
							c.setMaskType(maskType);
							maskToOwner(c, maskType);
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
		hashMasks.keySet().forEach(maskType -> {
			CellSetToMaskLoader(maskType, year);
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

	public HashMap<String, Boolean> restrictionsRulsUpload(String maskType) {
		// Read the file
		// put in restriction hashMap owner-competitor - boolean the key it will be the
		// owner copititro string
		HashMap<String, Boolean> restric = new HashMap<>();
		String[][] matrix = CsvTools
				.csvReader(PathTools.fileFilter("LandUseControl", "Restrictions", maskType, ".csv").get(0));
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
