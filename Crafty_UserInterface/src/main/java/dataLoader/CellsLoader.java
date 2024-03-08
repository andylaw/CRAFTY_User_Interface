package dataLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import model.Cell;
import model.CellsSet;
import tech.tablesaw.api.Table;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.FileReder;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;

/**
 * @author Mohamed Byari
 *
 */

public class CellsLoader {

	public static List<String> GISRegionsNames = new ArrayList<>();//
	public static ConcurrentHashMap<String, Cell> hashCell = new ConcurrentHashMap<>();
	public Set<Cell> cells = Collections.synchronizedSet(new HashSet<>());
	public AFTsLoader AFtsSet;

	public void loadMap() {

		AFtsSet = new AFTsLoader();
		hashCell.clear();
		cells.clear();

		String baseLindPath = PathTools.fileFilter("\\worlds\\", "Baseline_map").iterator().next();
		System.out.print("Importing data from the baseline map : " + baseLindPath + "...");
		FileReder.processCSV(this, baseLindPath,"Baseline");
		cells.addAll(hashCell.values());
		System.out.println(" Done");
		updateDemand();
	}

	public static void updateDemand() {
		HashMap<String, ArrayList<String>> d = FileReder
				.ReadAsaHash(PathTools.fileFilter(Paths.getScenario(), "demand").get(0));
		d.forEach((name, vect) -> {
			double[] v = new double[vect.size()];
			for (int i = 0; i < v.length; i++) {
				v[i] = Tools.sToD(vect.get(i));
			}
			CellsSet.getDemand().put(name, v);
		});
	}

	public void loadCapitalsAndServiceList() {
		String[] line0 = CsvTools.columnFromscsv(0, PathTools.fileFilter("\\Capitals.csv").get(0));
		CellsSet.getCapitalsName().clear();
		for (int n = 1; n < line0.length; n++) {
			CellsSet.getCapitalsName().add(line0[n]);
		}
		System.out.println("Capitals size=" + CellsSet.getCapitalsName().size() + " CellsSet.getCapitalsName() "
				+ CellsSet.getCapitalsName());
		CellsSet.getServicesNames().clear();
		String[] line0s = CsvTools.columnFromscsv(0, PathTools.fileFilter("\\Services.csv").get(0));
		for (int n = 1; n < line0s.length; n++) {
			CellsSet.getServicesNames().add(line0s[n]);
		}
		System.out.println("Services size=" + CellsSet.getServicesNames().size() + "CellsSet.getServicesNames()="
				+ CellsSet.getServicesNames());
	}

	public void loadGisData() {
		Table T = Table.read().csv(PathTools.fileFilter("\\GIS\\").get(0));
		GISRegionsNames = T.columnNames();
		String x = T.column("x") != null ? "x" : "X";
		String y = T.column("y") != null ? "y" : "Y";

		for (int i = 0; i < T.columns().iterator().next().size(); i++) {
			String coor = T.column(x).get(i) + "," + T.column(y).get(i);
			int ii = i;
			if (hashCell.get(coor) != null) {
				GISRegionsNames.forEach(name -> {
					if (T.column(name).get(ii) != null)
						hashCell.get(coor).getGisNameValue().put(name, T.column(name).get(ii).toString());
				});
			}
		}

	}

	public void updateCapitals(int year) {
		year = Math.min(year, Paths.getEndtYear());
		if (!Paths.getScenario().equalsIgnoreCase("Baseline")) {
			String path = PathTools.fileFilter(year + "", Paths.getScenario(), "\\capitals\\").get(0);
			System.out.print("Updating Capitals from : " + path + "...");
			FileReder.processCSV(this,path,"Capitals");
			System.out.println(" Done");
		}
	}

	public void servicesAndOwneroutPut(String year, String outputpath){
		Paths.setAllfilesPathInData(PathTools.findAllFiles(Paths.getProjectPath()));		
		String path = PathTools.fileFilter(outputpath, "-Cell-" + year + ".csv").get(0);
		System.out.print("Updating Services and AFTs from : " + path + "...");
		FileReder.processCSV(this,path,"Services");
		System.out.println(" Done");
	}

	public Cell getCell(int i, int j) {
		return hashCell.get(i + "," + j);
	}

}
