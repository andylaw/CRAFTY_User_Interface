package UtilitiesFx.filesTools;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import UtilitiesFx.graphicalTools.Tools;

class CsvToolsTest {
	List<File> list;

	// @Test
	void setUp() {
		list = CsvTools.detectFiles("C:\\Users\\byari-m\\Downloads\\productivity_EU");
		String[][] finalefile = CsvTools.csvReader(list.get(0).getAbsolutePath());

		list.forEach(f -> {
			if (f.getAbsolutePath().contains(".csv")) {
				String[][] temp = CsvTools.csvReader(f.getAbsolutePath());
				for (int j = 0; j < finalefile.length; j++) {
					finalefile[j][0] = temp[j][0].replace("Pasture ", "Pasture");
				}

				CsvTools.writeCSVfile(finalefile, f.getAbsolutePath());
			}
		});

	}

	@Test
	void controlServiceProductivity() {
		// t2("ssp1a26", "C3pulses", 1);
//		demandFiles("C3pulses",0.13527636918327202);
//		demandFiles("FloodRegulation",0.9733520768163937);
//		demandFiles("C3fruitveg",4.853223279253991);
//		demandFiles("Softwood",56.433641665145515);
//		demandFiles("Hardwood",14.674398776688333);
//		demandFiles("C3starchyroots",10.202133630983639);
//		demandFiles("BioenergyG1",15.664839915279625);
//		demandFiles("Biodiversity",0.12501485593332193);
//		demandFiles("BioenergyG2",0.03292595385268868);
//		demandFiles("Carbon",1.9419064114127658);
//		demandFiles("SusProd",0.15975422362682426);
//		demandFiles("GFmilk",4.518631625737583);
//		demandFiles("CES",0.9747658911392871);
//		demandFiles("C4crops",0.556416643644531);
//		demandFiles("Ldiversity",0.9699928087239089);
//		demandFiles("Foddercrops",13.591087750524043);
//		demandFiles("Recreation",0.17333766154627805);
//		demandFiles("Employment",0.9698247840141905);
//		demandFiles("SolarEnergy",22.03988483410523);
//		demandFiles("C3cereals",3.0856942321448435);
//		demandFiles("C3oilcrops",0.1543187534121523);
//		demandFiles("GFmeat",0.15361902449023654);
	}

	void demandFiles(String service, double p) {
		System.out.println(service);
		String path = "C:\\Users\\byari-m\\Desktop\\data-DE\\worlds\\demand\\ssp1a26_demands_DE.csv";
		String[][] filereder = CsvTools.csvReader(path);
		int index = Tools.indexof(service, filereder[0]);
		for (int i = 1; i < filereder.length; i++) {
			filereder[i][index] =  Tools.sToD(filereder[i][index])/p+"";
			
		}
		for (int i = 0; i < filereder.length; i++) {
			System.out.println(Arrays.toString(filereder[i]));
		}
	CsvTools.writeCSVfile(filereder, path);

	}

	void t2(String scenario, String service, double p) {
		String path = "C:\\Users\\byari-m\\Desktop\\data-DE\\production\\" + scenario;
		List<File> l = CsvTools.detectFiles(path);
		l.forEach(file -> {
			String[][] filereder = CsvTools.csvReader(file.getAbsolutePath());
			String[] production = new String[filereder.length];
			String[] services = new String[filereder.length];

			for (int i = 0; i < filereder.length; i++) {
				services[i] = filereder[i][0];
				production[i] = filereder[i][filereder[0].length - 1];
			}

			int index = Tools.indexof(service, services);

			production[index] = Tools.sToD(production[index]) * p + "";
			String[][] outputmatrix = new String[filereder.length][filereder[0].length];
			for (int i = 0; i < outputmatrix.length; i++) {
				for (int j = 0; j < outputmatrix[0].length; j++) {
					if (j < filereder[0].length - 1)
						outputmatrix[i][j] = filereder[i][j];
					else
						outputmatrix[i][j] = production[i];
				}
			}
			CsvTools.writeCSVfile(outputmatrix, file.getAbsolutePath());
		});
	}

	// @Test
	void t() {
		String str = "C:\\Users\\byari-m\\Desktop\\data-DE\\worlds\\LandUseControl\\ProtectedAreaMask";
		String[][] file = CsvTools.csvReader(str + "\\UrbanMask.csv");
		String[][] tmp = new String[file.length][3];
		for (int k = 3; k < file[0].length; k++) {
			for (int i = 0; i < file.length; i++) {
				for (int j = 0; j < 2; j++) {
					tmp[i][j] = file[i][j + 1];
				}
				tmp[i][2] = file[i][k];
			}
			// CsvTools.writeCSVfile(tmp, str+"\\UrbanMask_SSP5_"+file[0][k]+".csv");
		}
	}

	// @Test
	void test() {
		list = CsvTools.detectFiles("C:\\Users\\byari-m\\Desktop\\EU_demands");

		String[][] finalefile = CsvTools.csvReader(list.get(0).getAbsolutePath());
		double[][] m = new double[finalefile.length][finalefile[0].length];

		list.forEach(f -> {
			String[][] temp = CsvTools.csvReader(f.getAbsolutePath());
			for (int i = 1; i < finalefile.length; i++) {
				for (int j = 1; j < finalefile[0].length; j++) {
					m[i][j] += Tools.sToD(temp[i][j]);
				}
			}
		});

		for (int i = 1; i < m.length; i++) {
			for (int j = 1; j < m[0].length; j++) {
				finalefile[i][j] = m[i][j] + "";
			}
		}
		CsvTools.writeCSVfile(finalefile, "C:\\Users\\byari-m\\Desktop\\EU_demands\\DemandsEU.csv");
	}

}
