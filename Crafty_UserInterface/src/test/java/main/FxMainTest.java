package main;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import UtilitiesFx.filesTools.CsvTools;


class FxMainTest {

	 @BeforeAll
	    public static void setUpClass() {
	 
	    }

	    @Test
	    public void testLabelContainsCorrectText() {
			 String[] file2 = CsvTools.csvReaderAsVector(
						"C:\\Users\\byari-m\\Downloads\\EU_capitals.csv");
			 String[] file1 = CsvTools.csvReaderAsVector(
						"C:\\Users\\byari-m\\Downloads\\all_AFTs_randomised.csv");
			 String[][] fileResult= new String[file2.length] [2];		 
			 for (int i = 0; i < file2.length; i++) {
				 fileResult[i][0]=file2[i];
				}
				fileResult[0][1]="FR";
				for (int i = 1; i < file1.length; i++) {
					fileResult[i][1]=file1[i];
				}
				// CsvTools.writeCSVfile(fileResult, "C:\\Users\\byari-m\\Downloads\\Baseline_map.csv");
	    }

}
