package model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.graphicalTools.Tools;
import javafx.scene.paint.Color;

public class MaskRestrictions {

	Set<Cell> cellsToMask= new HashSet<>();
	// Set of cells will be Masked
	// matrix repesent which AFTs could compite for this set
	HashMap<String,Boolean> restriction= new HashMap<>();
	
	public void setToMaskInitialisation(String path) {
		String[][] matrix = CsvTools.csvReader(path);
		for (int i = 1; i < matrix.length; i++) {
			if(matrix[i][2].contains("1")) {
				Cell c=CellsSet.getCellsSet().getCell((int)Tools.sToD(matrix[i][0]), (int)Tools.sToD(matrix[i][1]));
				if(c!=null)
					cellsToMask.add(c);
			}
		}
	}

	
	public void restrictionsRulsUpload(String path) {
		//Read the file 
		//put in restriction hashMap owner-competitor - boolean the key it will be the owner copititro string
		String[][] matrix = CsvTools.csvReader(path);
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 1; j < matrix[0].length; j++) {
				restriction.put(matrix[i][0]+"_"+matrix[0][j], matrix[i][j].contains("1"));
			}
		}
	}
	
}
