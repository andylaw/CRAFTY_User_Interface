package dataLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import model.Cell;
import model.CellsSet;
import model.RegionClassifier;
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
	public static boolean regionalization = true;
//	private static Set<Cell> unmanageCells = ConcurrentHashMap.newKeySet();

//	public Set<Cell> cells = Collections.synchronizedSet(new HashSet<>());
	public AFTsLoader AFtsSet;

	private static int nbrOfCells = 0;

	public void loadMap() {

		AFtsSet = new AFTsLoader();
		hashCell.clear();

		Path baseLindPath = PathTools.fileFilter(File.separator +"worlds"+File.separator, "Baseline_map").iterator().next();

		ReaderFile.processCSV(this, baseLindPath, "Baseline");

		nbrOfCells = hashCell.size();
		if (nbrOfCells < 1000) {
			Cell.setSize(200);
		}

		loadGisData();
		RegionClassifier.initialation(false);
		DemandModel.updateDemand();
		AFTsLoader.hashAgentNbr();
		AFTsLoader.hashAgentNbrRegions();

		LOGGER.info("Number of cells for each AFT: " + AFTsLoader.hashAgentNbr);

	}

	public void loadCapitalsAndServiceList() {
		String[] line0 = CsvTools.columnFromscsv(0, PathTools.fileFilter(File.separator +"Capitals.csv").get(0));
		CellsSet.getCapitalsName().clear();
		for (int n = 1; n < line0.length; n++) {
			CellsSet.getCapitalsName().add(line0[n]);
		}
		LOGGER.info("Capitals size=" + CellsSet.getCapitalsName().size() + " CellsSet.getCapitalsName() "
				+ CellsSet.getCapitalsName());
		CellsSet.getServicesNames().clear();
		String[] line0s = CsvTools.columnFromscsv(0, PathTools.fileFilter(File.separator +"Services.csv").get(0));
		for (int n = 1; n < line0s.length; n++) {
			CellsSet.getServicesNames().add(line0s[n]);
		}
		LOGGER.info("Services size=" + CellsSet.getServicesNames().size() + "CellsSet.getServicesNames()="
				+ CellsSet.getServicesNames());
	}

	public void loadGisData() {
		try {
			Path path = PathTools.fileFilter(true, File.separator +"GIS"+File.separator).get(0);
			PathsLoader.WorldName = path.toFile().getName().replace("_Regions", "").replace(".csv", "");
			LOGGER.info("WorldName = " + PathsLoader.WorldName);
			Table T = Table.read().csv(path.toFile());
			GISRegionsNames = T.columnNames();
			for (int i = 0; i < T.columns().iterator().next().size(); i++) {
				String coor = T.column("X").get(i) + "," + T.column("Y").get(i);
				int ii = i;
				if (hashCell.get(coor) != null) {
					GISRegionsNames.forEach(name -> {
						if (T.column(name).get(ii) != null && name.contains("Region_Code"))
							hashCell.get(coor).getGisNameValue().put(name, T.column(name).get(ii).toString());
					});
				}
			}
			hashCell.values().forEach(c -> {
				c.setCurrentRegion(c.getGisNameValue().values().iterator().next());
			});
		} catch (NullPointerException | IOException e) {
			regionalization = false;
			LOGGER.warn(
					"The Regionalization File is not Found in the GIS Folder, this Data Will be Ignored - No Regionalization Will be Possible.");
			
		}
	}

	public void updateCapitals(int year) {
		year = Math.min(year, PathsLoader.getEndtYear());

		if (!PathsLoader.getScenario().equalsIgnoreCase("Baseline")) {
			Path path = PathTools.fileFilter(year + "", PathsLoader.getScenario(), File.separator +"capitals"+File.separator).get(0);
			ReaderFile.processCSV(this, path, "Capitals");
		}
	}

	public void servicesAndOwneroutPut(String year, String outputpath) {
		PathsLoader.setAllfilesPathInData(PathTools.findAllFiles(PathsLoader.getProjectPath()));
		Path path = PathTools.fileFilter(year, ".csv").get(0);

		ReaderFile.processCSV(this, path, "Services");
	}

	public Cell getCell(int i, int j) {
		return hashCell.get(i + "," + j);
	}

	public static int getNbrOfCells() {
		return nbrOfCells;
	}

//	public static Set<Cell> getUnmanageCells() {
//		return unmanageCells;
//	}

}
