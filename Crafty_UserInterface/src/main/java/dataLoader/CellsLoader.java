package dataLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import model.RegionClassifier;
import tech.tablesaw.api.Table;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.graphicalTools.Tools;
import UtilitiesFx.filesTools.PathTools;

/**
 * @author Mohamed Byari
 *
 */

public class CellsLoader {
	private static final Logger LOGGER = LogManager.getLogger(CellsLoader.class);
	public static Set<String> regionsNamesSet = new HashSet<>();
	public static ConcurrentHashMap<String, Cell> hashCell = new ConcurrentHashMap<>();
	private static List<String> capitalsList;
	public AFTsLoader AFtsSet;

	private static int nbrOfCells = 0;

	public void loadMap() {

		AFtsSet = new AFTsLoader();
		hashCell.clear();

		Path baseLindPath = PathTools.fileFilter(PathTools.asFolder("worlds"), "Baseline_map").iterator().next();
		ReaderFile.processCSV(this, baseLindPath, "Baseline");
		upscalingMap(2);
		nbrOfCells = hashCell.size();
		if (nbrOfCells < 1000) {// temporal for very small maps visualization
			Cell.setSize(200);
		}

		loadGisData();
		RegionClassifier.initialation();
//		DemandModel.updateWorldDemand();
		S_WeightLoader.updateWorldWeight();
		AFTsLoader.hashAgentNbr();
		AFTsLoader.hashAgentNbrRegions();

		LOGGER.info("Number of cells for each AFT: " + AFTsLoader.hashAgentNbr);

	}

	void upscalingMap(int scale) {
		ConcurrentHashMap<String, Cell> newMap = new ConcurrentHashMap<>();
		hashCell.values().forEach(c -> {
			int x = c.getX() / scale;
			int y = c.getY() / scale;
			c.setX(x);
			c.setY(y);
			newMap.put(x + "," + y, c);
		});
		hashCell.clear();
		hashCell = newMap;
		String[][] csv = new String[newMap.size() + 1][CellsLoader.capitalsList.size() + 5];
		int i = 1;
		csv[0][0] = "ID";
		csv[0][1] = "X";
		csv[0][2] = "Y";
		csv[0][3] = "FR";
		for (int j = 0; j < CellsLoader.capitalsList.size(); j++) {
			csv[0][4 + j] = CellsLoader.capitalsList.get(j);
		}
		for (Cell c : newMap.values()) {
			csv[i][0] = String.valueOf(c.getIndex());
			csv[i][1] = String.valueOf(c.getX());
			csv[i][2] = String.valueOf(c.getY());
			csv[i][3] = c.getOwner() != null ? c.getOwner().getLabel() : "null";
			for (int j = 0; j < CellsLoader.capitalsList.size(); j++) {
				csv[i][4 + j] = String.valueOf(c.getCapitals().get(CellsLoader.capitalsList.get(j)));
			}
			i++;
		}

		CsvTools.writeCSVfile(csv, Paths.get("C:\\Users\\byari-m\\Desktop\\newBaseLineMapUpscaled.csv"));
	}

	public static void loadCapitalsList() {
		capitalsList = Collections.synchronizedList(new ArrayList<>());
		String[] line0 = CsvTools.columnFromscsv(0, PathTools.fileFilter(File.separator + "Capitals.csv").get(0));
		getCapitalsList().clear();
		for (int n = 1; n < line0.length; n++) {
			getCapitalsList().add(line0[n]);
		}
		LOGGER.info("Capitals size=" + getCapitalsList().size() + " CellsSet.getCapitalsName() " + getCapitalsList());

	}

	public static List<String> getCapitalsList() {
		return capitalsList;
	}

	public void loadGisData() {
		try {
			Path path = PathTools.fileFilter(true, File.separator + "GIS" + File.separator).get(0);
			PathsLoader.WorldName = path.toFile().getName().replace("_Regions", "").replace(".csv", "");
			LOGGER.info("WorldName = " + PathsLoader.WorldName);
			Table T = Table.read().csv(path.toFile());
			for (int i = 0; i < T.columns().iterator().next().size(); i++) {
				String coor = T.column("X").get(i) + "," + T.column("Y").get(i);
				int ii = i;
				if (hashCell.get(coor) != null) {
					T.columnNames().forEach(name -> {
						if (T.column(name).get(ii) != null && name.contains("Region_Code")) {
							hashCell.get(coor).setCurrentRegion(T.column(name).get(ii).toString());
							regionsNamesSet.add(T.column(name).get(ii).toString());
						}
					});
				}
			}
		} catch (NullPointerException | IOException e) {
			RegionClassifier.regionalization = false;
			LOGGER.warn(
					"The Regionalization File is not Found in the GIS Folder, this Data Will be Ignored - No Regionalization Will be Possible.");

		}
	}

	public void updateCapitals(int year) {
		year = Math.min(year, PathsLoader.getEndtYear());

		if (!PathsLoader.getScenario().equalsIgnoreCase("Baseline")) {
			Path path = PathTools.fileFilter(year + "", PathsLoader.getScenario(), PathTools.asFolder("capitals"))
					.get(0);
			ReaderFile.processCSV(this, path, "Capitals");
		}
	}

//	void upscaleCapital(int year, int scale) {
//		Path path = PathTools.fileFilter(year + "", PathsLoader.getScenario(), PathTools.asFolder("capitals")).get(0);
//		HashMap<String, ArrayList<String>> reader = ReaderFile.ReadAsaHash(path);
////		reader.forEach((colmunName,lines) -> {
////			int x = (int) Tools.sToD(reader);
////		});
//		;
//		for (int i = 0; i <reader.get("X").size(); i++) {
//
//		}
//		int x = (int) Tools.sToD(reader);
//		int y = (int) Tools.sToD(immutableList.get(indexof.get("Y")));
//		CellsLoader.getCapitalsList().forEach(capital_name -> {
//			double capital_value = Tools.sToD(immutableList.get(indexof.get(capital_name.toUpperCase())));
//			CellsSet.getCellsSet().getCell(x, y).getCapitals().put(capital_name, capital_value);
//		});
//	}

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

}
