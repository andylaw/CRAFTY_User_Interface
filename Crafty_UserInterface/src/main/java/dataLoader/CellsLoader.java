package dataLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.Cell;
import model.CellsSet;
import model.ModelRunner;
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
	private static final Logger LOGGER = LogManager.getLogger(ModelRunner.class);
	public static List<String> GISRegionsNames = new ArrayList<>();//
	public static ConcurrentHashMap<String, Cell> hashCell = new ConcurrentHashMap<>();
	public Set<Cell> cells = Collections.synchronizedSet(new HashSet<>());
	public AFTsLoader AFtsSet;

	public void loadMap() {

		AFtsSet = new AFTsLoader();
		hashCell.clear();
		cells.clear();

		String baseLindPath = PathTools.fileFilter("\\worlds\\", "Baseline_map").iterator().next();
		LOGGER.info("Importing data from the baseline map : " + baseLindPath + "...");
		FileReder.processCSV(this, baseLindPath, "Baseline");
		cells.addAll(hashCell.values());

		updateDemand();

	}

	public static void updateDemand() {
		String path = PathTools.fileFilter(Paths.getScenario(), "demand").get(0);
		HashMap<String, ArrayList<String>> d = FileReder.ReadAsaHash(path);
		LOGGER.info("Update Demand: " +path);
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
		LOGGER.info("Capitals size=" + CellsSet.getCapitalsName().size() + " CellsSet.getCapitalsName() "
				+ CellsSet.getCapitalsName());
		CellsSet.getServicesNames().clear();
		String[] line0s = CsvTools.columnFromscsv(0, PathTools.fileFilter("\\Services.csv").get(0));
		for (int n = 1; n < line0s.length; n++) {
			CellsSet.getServicesNames().add(line0s[n]);
		}
		LOGGER.info("Services size=" + CellsSet.getServicesNames().size() + "CellsSet.getServicesNames()="
				+ CellsSet.getServicesNames());
	}

	public void loadGisData() {
		try {
		String path = PathTools.fileFilter(true,"\\GIS\\").get(0);
		Table T = Table.read().csv(path);
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
		}catch(NullPointerException e) {
			LOGGER.warn("The Regionalization File is not Found in the GIS Folder, this Data Will be Ignored - No Regionalization Will be Possible.");
		}

	}

	public void updateCapitals(int year) {
		year = Math.min(year, Paths.getEndtYear());
		
		if (!Paths.getScenario().equalsIgnoreCase("Baseline")) {
			String path = PathTools.fileFilter(year + "", Paths.getScenario(), "\\capitals\\").get(0);
			LOGGER.info("Updating Capitals from : " + path );
			FileReder.processCSV(this, path, "Capitals");
		}
	}

	public void servicesAndOwneroutPut(String year, String outputpath) {
		System.out.println(year);
		Paths.setAllfilesPathInData(PathTools.findAllFiles(Paths.getProjectPath()));
		String path = PathTools.fileFilter( year ).get(0);
		LOGGER.info("Updating Services and AFTs Distribution from : " + path );
		FileReder.processCSV(this, path, "Services");
	}

	public Cell getCell(int i, int j) {
		return hashCell.get(i + "," + j);
	}

}
