package UtilitiesFx.filesTools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConvertScotlanddata {
	static List<String> set = new ArrayList<>();

	public static void initilasiation() {
		String p = "C:\\Users\\byari-m\\Documents\\Data" + "\\Scotland\\worlds\\"
				+ "Baseline_map_Scotland.csv";
		HashMap<String, String[]> baseline = CsvTools.ReadAsaHash(p);
		for (int i = 0; i < baseline.values().iterator().next().length; i++) {
			set.add(baseline.get("X")[i] + "," + baseline.get("Y")[i]);
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

			String newPath = file.getParent() + "\\" + file.getName().replace("UK", "Scotland");
			CsvTools.writeCSVfile(newCapi, newPath);
			System.out.println(file.getName());
		});

	}

}
