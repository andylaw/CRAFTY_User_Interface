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
//		demandFiles("C3pulses",0.4420218624720767);
//		demandFiles("FloodRegulation",1.2710805509458118);
//		demandFiles("C3fruitveg",0.7207912724458584);
//		demandFiles("Softwood",2.544589260243189);
//		demandFiles("Hardwood",2.158342168749489);
//		demandFiles("C3starchyroots",0.428763476691158);
//		demandFiles("BioenergyG1",1.0000000000981601);
//		demandFiles("Biodiversity",1.3827478623957956);
//		demandFiles("BioenergyG2",0.9999999999918013);
//		demandFiles("Carbon",1.1695837149661976);
//		demandFiles("SusProd",2.228891912514655);
//		demandFiles("GFmilk",1.9841795192630278);
//		demandFiles("CES",1.2618978240497665);
//		demandFiles("C4crops",0.7202804742142568);
//		demandFiles("Ldiversity",1.5914447295337868);
//		demandFiles("Foddercrops",1.2711607611942122);
//		demandFiles("Recreation",1.6500570403055466);
//		demandFiles("Employment",1.3391139486612418);
////		demandFiles("SolarEnergy",10.000749136646766);
//		demandFiles("C3cereals",1.1270248716655467);
//		demandFiles("C3oilcrops",0.5146069571245985);
//		demandFiles("C3pulses",1.1842971006584717);
//		demandFiles("GFmilk",0.7533327304271545);
//		demandFiles("C3cereals",0.7310635718933262);
//		demandFiles("GFmeat",0.7283311230391097);
	}

	void demandFiles(String service, double p) {
		System.out.println(service);
		String path = "C:\\Users\\byari-m\\Desktop\\data-DE\\worlds\\demand\\ssp1a26_demands_DE.csv";
		String[][] filereder = CsvTools.csvReader(path);
		int index = Tools.indexof(service, filereder[0]);
		for (int i = 1; i < filereder.length; i++) {
			filereder[i][index] =  Tools.sToD(filereder[i][index])/p+"";
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
