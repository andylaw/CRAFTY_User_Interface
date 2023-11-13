package dataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Cell;
import model.Lattice;
import tech.tablesaw.api.Table;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import UtilitiesFx.graphicalTools.WarningWindowes;

public class MapLoader {

	
	public static List<String> GISNames = new ArrayList<>();//

	public void loadMap() {
		HashMap<String, String[]> patchData = loadBaselineCapital("\\worlds\\",	"Baseline_map");
		
		String[] X = patchData.get(patchData.containsKey("x") ? "x" : "X");
		String[] Y = patchData.get(patchData.containsKey("y") ? "y" : "Y");

		for (int i = 0; i < X.length; i++) {
			if (Tools.sToD(X[i]) != 0 && Tools.sToD(Y[i]) != 0) {
				Cell c = new Cell((int) Tools.sToD(X[i]), (int) Tools.sToD(Y[i]));
				if (c != null) {
					c.setOwner(Agents.aftReSet.get(patchData.get("FR")[i]));
					Lattice.getCellsSet().add(c);
					Lattice.getHashCell().put(X[i] + "," + Y[i], c);
					c.setIndex(Lattice.getCellsSet().size());
				}
				for (int j = 0; j < Lattice.getCapitalsName().size(); j++) {
					c.getCapitals().put(Lattice.getCapitalsName().get(j), Tools.sToD(patchData.get(Lattice.getCapitalsName().get(j))[i]));
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
				Cell c = Lattice.getHashCell().get(X[i] + "," + Y[i]);
				if (c != null) {
					c.setOwner( Agents.aftReSet.get(patchData.get("FR")[i]));
				}
				for (int j = 0; j < Lattice.getCapitalsName().size(); j++) {
					c.getCapitals().put(Lattice.getCapitalsName().get(j), Tools.sToD(patchData.get(Lattice.getCapitalsName().get(j))[i]));
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
			Lattice.getDemand().put(name, v);
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
		Lattice.getCapitalsName().clear();
		for (int n = 1; n < line0.length; n++) {
			Lattice.getCapitalsName().add(line0[n]);
		}
		Lattice.getServicesNames().clear();
		String[] line0s = CsvTools.columnFromscsv(0, PathTools.fileFilter("\\Services.csv").get(0));
		for (int n = 1; n < line0s.length; n++) {
			Lattice.getServicesNames().add(line0s[n]);
		}
	}

	public void loadGisData() {
		Table T = Table.read().csv(PathTools.fileFilter("\\GIS\\").get(0));
		GISNames = T.columnNames();
		String x = T.column("x") != null ? "x" : "X";
		String y = T.column("y") != null ? "y" : "Y";

		for (int i = 0; i < T.columns().iterator().next().size(); i++) {
			String coor = T.column(x).get(i) + "," + T.column(y).get(i);
			int ii = i;
			if (Lattice.getHashCell().get(coor) != null) {
				GISNames.forEach(name -> {
					if (T.column(name).get(ii) != null)
						Lattice.getHashCell().get(coor).getGisNameValue().put(name, T.column(name).get(ii).toString());
				});
			}
		}
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
					Cell c = Lattice.getHashCell().get(patchData.get(x)[ii] + "," + patchData.get(y)[ii]);
					if (c != null) {
						if (Lattice.getCapitalsName().contains(name))
							c.getCapitals().put(name, Tools.sToD(vect[ii]));
					}
				});
			}
		}
	}
	
	public static void servicesAndOwner(String year, String outputpath)  {
		Paths.setAllfilesPathInData(PathTools.findAllFiles(Paths.getProjectPath()));
		HashMap<String, String[]> hash = CsvTools
				.ReadAsaHash(PathTools.fileFilter(outputpath, "-Cell-" + year + ".csv").get(0));
		String x = hash.containsKey("x") ? "x" : "X";
		String y = hash.containsKey("y") ? "y" : "Y";
		System.out.println(Lattice.getServicesNames());
		for (int i = 0; i < hash.values().iterator().next().length; i++) {
			int ii = i;
			Cell c = Lattice.getHashCell().get(hash.get(x)[i] + "," + hash.get(y)[i]);
			c.getServices().clear();
			Lattice.getServicesNames().forEach(name -> {
				if (c != null)
					c.getServices().put(name, Tools.sToD(hash.get("Service:" + name)[ii]));
			});

			Agents.aftReSet.forEach((name, agent) -> {
				if (name.equals(hash.get("Agent")[ii])) {
					c.setOwner(agent);
				}
			});
		}
	}

}
