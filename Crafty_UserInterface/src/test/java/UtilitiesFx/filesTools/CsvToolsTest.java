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
		demandFiles("C3pulses",4.517221531565974);
		demandFiles("FloodRegulation",8.947317179136961);
		demandFiles("C3fruitveg",5.482352443191113);
		demandFiles("Softwood",10.934606743000856);
		demandFiles("Hardwood",11.429365046232567);
		demandFiles("C3starchyroots",5.835639730856504);
		demandFiles("BioenergyG1",6.368460163497004);
		demandFiles("Biodiversity",8.448771370308279);
		demandFiles("BioenergyG2",26.05545199907801);
		demandFiles("Carbon",8.613496013910156);
		demandFiles("SusProd",7.7346213505587285);
		demandFiles("GFmilk",8.886989061203632);
		demandFiles("CES",7.680113238722144);
		demandFiles("C4crops",7.230685378602165);
		demandFiles("Ldiversity",7.476656296839283);
		demandFiles("Foddercrops",17.030438543825618);
		demandFiles("Recreation",8.327013850542208);
		demandFiles("Employment",8.241254544491785);
		demandFiles("SolarEnergy",36.04185853841488);
		demandFiles("C3cereals",5.49133490369705);
		demandFiles("C3oilcrops",7.402962558589438);
		demandFiles("GFmeat",8.43339882235723);
	}

	void demandFiles(String service, double p) {
		System.out.println(service);
		String path = "C:\\Users\\byari-m\\Desktop\\data-DE_3kmgrid\\worlds\\demand\\ssp585_demands_DE.csv";
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
