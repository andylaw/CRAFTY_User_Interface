package dataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import model.Cell;
import model.CellsSet;
import tech.tablesaw.api.Table;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import UtilitiesFx.graphicalTools.WarningWindowes;

/**
 * @author Mohamed Byari
 *
 */

public class CellsLoader  extends HashSet<Cell>{

	private static final long serialVersionUID = 1L;
	public static List<String> GISRegionsNames = new ArrayList<>();//
	public  HashMap<String, Cell> hashCell = new HashMap<>();
	public AFTsLoader AFtsSet;
	public void loadMap() {
		
		AFtsSet= new AFTsLoader();
		HashMap<String, String[]> patchData = loadBaselineCapital("\\worlds\\",	"Baseline_map");
		
		String[] X = patchData.get(patchData.containsKey("x") ? "x" : "X");
		String[] Y = patchData.get(patchData.containsKey("y") ? "y" : "Y");
		hashCell.clear();
		this.clear();
		for (int i = 0; i < X.length; i++) {
			if (Tools.sToD(X[i]) != 0 && Tools.sToD(Y[i]) != 0) {
				Cell c = new Cell((int) Tools.sToD(X[i]), (int) Tools.sToD(Y[i]));
				if (c != null) {
					c.setOwner(AFtsSet.getAftHash().get(patchData.get("FR")[i]));
					add(c);
					hashCell.put(X[i] + "," + Y[i], c);
					c.setIndex(size());
				}
				for (int j = 0; j < CellsSet.getCapitalsName().size(); j++) {
					c.getCapitals().put(CellsSet.getCapitalsName().get(j), Tools.sToD(patchData.get(CellsSet.getCapitalsName().get(j))[i]));
				}
			}
		}
		updateDemand();
	}


	public void ResetMap() {
		HashMap<String, String[]> patchData = loadBaselineCapital("\\worlds\\" ,
				"Baseline_map");
		String[] X = patchData.get(patchData.containsKey("x") ? "x" : "X");
		String[] Y = patchData.get(patchData.containsKey("y") ? "y" : "Y");

		for (int i = 0; i < X.length; i++) {
			if (Tools.sToD(X[i]) != 0 && Tools.sToD(Y[i]) != 0) {
				Cell c = hashCell.get(X[i] + "," + Y[i]);
				if (c != null) {
					c.setOwner( AFtsSet.getAftHash().get(patchData.get("FR")[i]));
				}
				for (int j = 0; j < CellsSet.getCapitalsName().size(); j++) {
					c.getCapitals().put(CellsSet.getCapitalsName().get(j), Tools.sToD(patchData.get(CellsSet.getCapitalsName().get(j))[i]));
				}
			}
		}
		updateDemand();
	}
	public static void updateDemand() {
		HashMap<String, String[]> d = CsvTools.ReadAsaHash(PathTools.fileFilter(Paths.getScenario(), "demand").get(0));
		d.forEach((name,vect)->{
			double [] v=new double[vect.length];
			for (int i = 0; i <v.length; i++) {
				v[i]=Tools.sToD(vect[i]);
			}
			CellsSet.getDemand().put(name, v);
		});
	}
	HashMap<String, String[]> loadBaselineCapital(String... findBaseLine) {
		Paths.setAllfilesPathInData(PathTools.findAllFiles(Paths.getProjectPath()));
		ArrayList<String> baselineMap;
		baselineMap = PathTools.fileFilter(findBaseLine);
		HashMap<String, String[]> patchData = new HashMap<>();

		if (baselineMap.size() == 0) {
			WarningWindowes.showWarningMessage("Baseline Capital Not Found", "Try Again", x -> {
				loadBaselineCapital(findBaseLine);
			}, "Select Baseline Capital File", y -> {
				File selectedDirectory = PathTools.selecFile(Paths.getProjectPath());
				for (int i = 0; i < findBaseLine.length; i++) {
					findBaseLine[i] = selectedDirectory.getAbsolutePath();
				}
				loadBaselineCapital(selectedDirectory.getAbsolutePath());
			});
			patchData = loadBaselineCapital(findBaseLine);
		} else {
			patchData = CsvTools.ReadAsaHash(baselineMap.iterator().next());
		}
		return patchData;
	}

	public void loadCapitalsAndServiceList() {
		String[] line0 = CsvTools.columnFromscsv(0, PathTools.fileFilter("\\Capitals.csv").get(0));
		CellsSet.getCapitalsName().clear();
		for (int n = 1; n < line0.length; n++) {
			CellsSet.getCapitalsName().add(line0[n]);
		}
		CellsSet.getServicesNames().clear();
		String[] line0s = CsvTools.columnFromscsv(0, PathTools.fileFilter("\\Services.csv").get(0));
		for (int n = 1; n < line0s.length; n++) {
			CellsSet.getServicesNames().add(line0s[n]);
		}
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
		System.out.println("Regions Names= "+GISRegionsNames);
	}


	public void updateCapitals(int year) {
		year=Math.min(year, Paths.getEndtYear());
		if (!Paths.getScenario().equalsIgnoreCase("Baseline")) {
			HashMap<String, String[]> patchData = CsvTools
					.ReadAsaHash(PathTools.fileFilter(year + "", Paths.getScenario(), "\\capitals\\").get(0));
			String x = patchData.keySet().contains("x") ? "x" : "X";
			String y = patchData.keySet().contains("y") ? "y" : "Y";
			for (int i = 0; i < patchData.values().iterator().next().length; i++) {
				int ii = i;
				patchData.forEach((name, vect) -> {
					Cell c = hashCell.get(patchData.get(x)[ii] + "," + patchData.get(y)[ii]);
					if (c != null) {
						if (CellsSet.getCapitalsName().contains(name))
							c.getCapitals().put(name, Tools.sToD(vect[ii]));
					}
				});
			}
		}
	}
	
	public void servicesAndOwner(String year, String outputpath)  {
		Paths.setAllfilesPathInData(PathTools.findAllFiles(Paths.getProjectPath()));
		HashMap<String, String[]> hash = CsvTools
				.ReadAsaHash(PathTools.fileFilter(outputpath, "-Cell-" + year + ".csv").get(0));
		String x = hash.containsKey("x") ? "x" : "X";
		String y = hash.containsKey("y") ? "y" : "Y";
		System.out.println(CellsSet.getServicesNames());
		for (int i = 0; i < hash.values().iterator().next().length; i++) {
			int ii = i;
			Cell c = hashCell.get(hash.get(x)[i] + "," + hash.get(y)[i]);
			c.getServices().clear();
			CellsSet.getServicesNames().forEach(name -> {
				if (c != null)
					c.getServices().put(name, Tools.sToD(hash.get("Service:" + name)[ii]));
			});

			AFtsSet.getAftHash().forEach((name, agent) -> {
				if (name.equals(hash.get("Agent")[ii])) {
					c.setOwner(agent);
				}
			});
		}
	}


	public   Cell getCell(int i, int j) {
		return hashCell.get(i + "," + j);
	}

}
