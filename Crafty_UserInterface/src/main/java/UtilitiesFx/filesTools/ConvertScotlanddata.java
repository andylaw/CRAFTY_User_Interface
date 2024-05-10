package UtilitiesFx.filesTools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import UtilitiesFx.graphicalTools.Tools;

public class ConvertScotlanddata {
	static List<String> set = new ArrayList<>();

	public static void initilasiation() {
		String p = "C:\\Users\\byari-m\\Documents\\Data\\data_Wales\\worlds\\Wales\\Base_line_map_Wales.csv";
		HashMap<String, ArrayList<String>> baseline = ReaderFile
				.ReadAsaHash(p);
		for (int i = 0; i < baseline.values().iterator().next().size(); i++) {
			set.add(baseline.get("X").get(i) + "," + baseline.get("Y").get(i));
		}
	}

	public static void scotlandcells(String path) {
		List<File> files = CsvTools.detectFiles(path);
		files.forEach(file -> {
			String[][] m = CsvTools.csvReader(file.getAbsolutePath());
			ArrayList<String[]> lines = new ArrayList<>();
			lines.add(m[0]);
			for (int i = 0; i < m.length; i++) {
				if (set.contains(m[i][0] + "," + m[i][1])) {
					lines.add(m[i]);
				}
			}
			String[][] newCapi = new String[lines.size()][m[0].length];
			for (int i = 0; i < lines.size(); i++) {
				newCapi[i] = lines.get(i);
			}

			String newPath = file.getParent() + "\\" + file.getName().replace("UK", "Wales");
			CsvTools.writeCSVfile(newCapi, newPath);
			System.out.println(file.getName());
		});

	}

	static HashMap<String, Double> csvtoHashmap(String path) {
		String[][] csv = CsvTools.csvReader(path);
		HashMap<String, Double> hash = new HashMap<>();
		for (int i = 1; i < csv.length; i++) {
			for (int j = 1; j < csv[0].length; j++) {
				hash.put(csv[i][0] + "_" + csv[0][j], Tools.sToD(csv[i][j]));
			}
		}
		return hash;
	}

	public static void creatsensitivty() {
		String path = "C:\\Users\\byari-m\\Desktop\\Sensitivity_configuration_DE\\";
		String aft_capital_path = path + "ac_DE.csv";
		String aft_services_path = path + "as_javaGPT.csv";
		String services_capitals_path = path + "sc_DE.csv";

		ArrayList<String> capital = new ArrayList<>(ReaderFile.ReadAsaHash(aft_capital_path).keySet());
		ArrayList<String> services = new ArrayList<>(ReaderFile.ReadAsaHash(aft_services_path).keySet());
		ArrayList<String> aft = new ArrayList<>(ReaderFile.ReadAsaHash(path + "AFT_list.csv").keySet());

		capital.remove(capital.indexOf(""));
		services.remove(services.indexOf(""));

		HashMap<String, Double> aft_capital = csvtoHashmap(aft_capital_path);
		HashMap<String, Double> aft_services = csvtoHashmap(aft_services_path);
		HashMap<String, Double> services_capitals = csvtoHashmap(services_capitals_path);
		
		aft.forEach(a->{
		//String a= "Solar";
			
			System.out.println(a);
		String[][] tab = new String[services.size() + 1][capital.size() + 2];
		tab[0][0] = "";
		if (aft.contains(a)) {
			for (int i = 0; i < capital.size(); i++) {
				tab[0][i + 1] = capital.get(i);
				for (int j = 0; j < services.size(); j++) {
					tab[j + 1][0] = services.get(j);
				//	System.out.println(services.get(j) + "_" + capital.get(i)+"-->"+ services_capitals.get(services.get(j) + "_" + capital.get(i)));
					double sc=services_capitals.get(services.get(j) + "_" + capital.get(i));
					double ac=aft_capital.get(a + "_" + capital.get(i));
					double as=aft_services.get(a + "_" + services.get(j));
					
				//	tab[j + 1][i + 1] =(double) ((int)(1000*(Math.min(Math.min(ac, as),sc))))/1000+ "";
					tab[j + 1][i + 1] =as*ac*sc+ "";
				//	tab[j + 1][i + 1] = Math.min((0.75*ac+0.25*sc)*as,1)+"";
				}
			}
			tab[0][capital.size()+1] = "Production";
			for (int j = 0; j < services.size(); j++) {
				double sum=0;
				for (int i = 0; i < capital.size(); i++) {
					sum+=Tools.sToD(tab[j + 1][i+1]);
				}
			tab[j + 1][capital.size()+1] = (sum/capital.size())+ "";
			}
		}
		CsvTools.writeCSVfile(tab, path + "Production_parametres\\" + a + ".csv");
});
	}

}
