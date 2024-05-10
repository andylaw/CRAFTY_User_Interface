package dataLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.Cell;
import model.CellsSet;
import tech.tablesaw.api.Table;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.filesTools.PathTools;

/**
 * @author Mohamed Byari
 *
 */

public class CellsLoader {
	private static final Logger LOGGER = LogManager.getLogger(CellsLoader.class);
	public static List<String> GISRegionsNames = new ArrayList<>();
	public static ConcurrentHashMap<String, Cell> hashCell = new ConcurrentHashMap<>();
	private static Set<Cell> unmanageCells = ConcurrentHashMap.newKeySet();

//	public Set<Cell> cells = Collections.synchronizedSet(new HashSet<>());
	public AFTsLoader AFtsSet;
	
	private static int nbrOfCells=0;


	public void loadMap() {

		AFtsSet = new AFTsLoader();
		hashCell.clear();

		String baseLindPath = PathTools.fileFilter("\\worlds\\", "Baseline_map").iterator().next();
		
		ReaderFile.processCSV(this, baseLindPath, "Baseline");
		
		nbrOfCells=hashCell.size();
		if(nbrOfCells<1000) {
			Cell.setSize(200);
		}
		AFTsLoader.hashAgentNbr();
		LOGGER.info("Number of cells for each AFT: "+AFTsLoader.hashAgentNbr);
		DemandModel.updateDemand();

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
			ReaderFile.processCSV(this, path, "Capitals");
		}
	}

	public void servicesAndOwneroutPut(String year, String outputpath) {
		Paths.setAllfilesPathInData(PathTools.findAllFiles(Paths.getProjectPath()));
		String path = PathTools.fileFilter( year ).get(0);
		ReaderFile.processCSV(this, path, "Services");
	}

	public Cell getCell(int i, int j) {
		return hashCell.get(i + "," + j);
	}
	
	public static int getNbrOfCells() {
		return nbrOfCells;
	}



	public static Set<Cell> getUnmanageCells() {
		return unmanageCells;
	}




}
