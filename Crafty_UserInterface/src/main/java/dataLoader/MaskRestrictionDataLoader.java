package dataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.FileReder;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import model.Cell;
import model.CellsSet;

public class MaskRestrictionDataLoader {

	// Set of cells will be Masked
	// matrix repesent which AFTs could compite for this set
	public static HashMap<String, String> ListOfMask;

	public void MaskAndRistrictionLaoder() {
		ListOfMask = new HashMap<>();

		ArrayList<String> listOfMaskFile = PathTools.fileFilter(true, "LandUseControl", "ProtectedAreaMask", ".csv");
		if (listOfMaskFile != null) {
			listOfMaskFile.forEach(file -> {
				if (!file.contains("Restriction")) {
					String name = new File(file).getName().replaceAll(".csv", "");
					ListOfMask.put(name, file);
				}
			});
		}

	}

	public void CellSetToMaskLoader(String maskType) {
		HashMap<String, ArrayList<String>> csv = FileReder
				.ReadAsaHash(ListOfMask.get(maskType), true);
		String protec = "PROTECTED";
		if (csv != null) {
			for (Iterator<String> iterator = csv.keySet().iterator(); iterator.hasNext();) {
				String n = iterator.next();
				if (n.contains(protec)) {
					protec = n;
				}
			}
			for (int i = 1; i < csv.values().iterator().next().size(); i++) {
				if (csv.get(protec).get(i).contains("1")) {
					Cell c = CellsSet.getCellsSet().getCell((int) Tools.sToD(csv.get("X").get(i)),
							(int) Tools.sToD(csv.get("Y").get(i)));
					if (c != null) {
						c.setMaskType(maskType);
					}
				}
			}
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
