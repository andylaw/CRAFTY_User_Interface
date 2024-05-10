package dataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import model.Cell;
import model.CellsSet;

public class MaskRestrictionDataLoader {

	// Set of cells will be Masked
	// matrix repesent which AFTs could compite for this set
	public static HashMap<String, String> ListOfMask;
	public static HashMap<Cell, HashMap<Integer, String>> hash_cells_masks = new HashMap<>();
	public static HashMap<String, ArrayList<String>> listOfyears = new HashMap<>();

	private static final Logger LOGGER = LogManager.getLogger(MaskRestrictionDataLoader.class);

	public void MaskAndRistrictionLaoder() {
		ListOfMask = new HashMap<>();

		ArrayList<String> listOfMaskFile = PathTools.fileFilter(true, "LandUseControl",Paths.getScenario(), "ProtectedAreaMask", ".csv");
		if(listOfMaskFile == null) {
			listOfMaskFile = PathTools.fileFilter(true, "LandUseControl", "ProtectedAreaMask", ".csv");
		}
		if (listOfMaskFile != null) {
			listOfMaskFile.forEach(file -> {
				if (!file.contains("Restriction")) {
					String name = new File(file).getName().replaceAll(".csv", "");
					ListOfMask.put(name, file);
				}
			});
		}
	}

	private ArrayList<String> getlistOfYears(HashMap<String, ArrayList<String>> csv) {
		ArrayList<String> yearList = new ArrayList<>();
		for (int j = Paths.getStartYear(); j < Paths.getEndtYear(); j++) {
			if (csv.keySet().contains("Year_" + j)) {
				yearList.add(j + "");
			}
		}
		return yearList;
	}

	public void cleanType(String maskType) {
		Iterator<Map.Entry<Cell, HashMap<Integer, String>>> iterator = hash_cells_masks.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Cell, HashMap<Integer, String>> entry = iterator.next();
			if (entry.getValue().values().contains(maskType)) {
				entry.getKey().setMaskType(null);
				iterator.remove();
			}
		}
	}

	public static void updateCellsmask(String maskType, int year) {
		if (listOfyears.get(maskType) != null) {
			if (listOfyears.get(maskType).contains(year + "")) {
				LOGGER.info("Masks used - Mask name = " + maskType + "  year = " + year);
				hash_cells_masks.forEach((c, valueHash) -> {
					if (c != null) {
						if (c.getMaskType() != null) {
							if (c.getMaskType().equals(maskType)) {
								c.setMaskType(valueHash.get(year));
							}
						} else {
							c.setMaskType(valueHash.get(year));
						}
					}
				});
			} else {
				System.out.println("No mask Update are used for " + maskType);
			}
		}
	}

	public static void updateCellsmask(int year) {
		if (hash_cells_masks.size() > 0) {
			ListOfMask.keySet().forEach(maskType -> {
				updateCellsmask(maskType, year);
			});
		}
	}

	public void CellSetToMaskLoader(String maskType) {
		HashMap<String, ArrayList<String>> csv = ReaderFile.ReadAsaHash(ListOfMask.get(maskType), true);
		listOfyears.put(maskType, getlistOfYears(csv));

		System.out.println("listOfyears--> " + listOfyears);

		if (csv != null) {
			for (int i = 0; i < csv.values().iterator().next().size(); i++) {
				Cell c = CellsSet.getCellsSet().getCell((int) Tools.sToD(csv.get("X").get(i)),
						(int) Tools.sToD(csv.get("Y").get(i)));
				HashMap<Integer, String> year_value = new HashMap<>();
				int ii = i;
				csv.keySet().forEach(key -> {
					if (key.contains("Year_") && csv.get(key).get(ii).contains("1")) {
						year_value.put((int) Tools.sToD(key.replace("Year_", "")), maskType);
					}
				});
				if (year_value.size() != 0) {
					hash_cells_masks.put(c, year_value);
				}
			}
		} else {
			LOGGER.warn("Cannot find the mask files..." + ListOfMask.get(maskType));
		}

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
