package WorldPack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import Main.Main_CraftyFx;
import UtilitiesFx.ColorsTools;
import UtilitiesFx.CsvTools;
import UtilitiesFx.Path;
import UtilitiesFx.ReadFile;
import UtilitiesFx.Tools;
import UtilitiesFx.WarningWindowes;
import javafx.scene.paint.Color;

public class Lattice {

	public static List<Cell> P = new ArrayList<>();
	public HashMap<String, Cell> hashCell = new HashMap<>();
	public  Agents agents = new Agents();
	public static List<String> capitalsName = new ArrayList<>();
	public static List<String> servicesNames = new ArrayList<>();

	public String[][] demandTable;

	public void creatMap() {
		agents.initialseAFT();
		capitalsandServiceList();
		System.out.println("Path.worldNameList  " + Path.worldNameList);
		System.out.println("Path.referqnceWorld  " + Path.referqnceWorld);
		HashMap<String, String[]> patchData =baselineCapital("\\worlds\\" + Path.referqnceWorld + "\\", "Baseline_map");

		String[] X = patchData.get("x");
		String[] Y = patchData.get("y");
		
//		System.out.println("===>"+ patchData);
//		System.out.println("===>"+ Patch.capitalsName);
		
		for (int i = 0; i < X.length; i++) {
			if (Tools.sToD(X[i]) != 0 && Tools.sToD(Y[i]) != 0) {
				Cell p = new Cell((int) Tools.sToD(X[i]), (int)Tools.sToD(Y[i]));
				if(p!=null) {
				p.owner = Agents.aftReSet.get(patchData.get("fr")[i]);//new AFT(Agents.aftReSet.get(patchData.get("fr")[i]),1);
				P.add(p);
				hashCell.put(X[i] + "," + Y[i], p);
				p.index = P.size();
				} 
				
				for (int j = 0; j < capitalsName.size(); j++) {
					p.capitals.put(capitalsName.get(j).replace(" ",""),
							Tools.sToD(patchData.get(capitalsName.get(j))[i]));
				}
			}

		}
		
		
		demandTable = CsvTools.csvReader(Path.fileFilter(Path.scenario, "demand").get(0));

	}
	
	HashMap<String, String[]> baselineCapital(String...findBaseLine) {
		Path.AllfilePatheInData = Path.pathWithconditions();
		ArrayList<String> baselineMap;
		baselineMap = Path.fileFilter(findBaseLine);
		HashMap<String, String[]> patchData = new HashMap<>();
		
		if(baselineMap.size() == 0) {
			WarningWindowes.showWarningMessage("Baseline Capital Not Found",
					"Try Again",x->{ baselineCapital(findBaseLine);},
					"Select Baseline Capital File",y->{
						File selectedDirectory =ReadFile.selecFile(Path.projectPath) ;
						for (int i = 0; i < findBaseLine.length; i++) {
							findBaseLine[i]=selectedDirectory.getAbsolutePath();
						}
						baselineCapital(selectedDirectory.getAbsolutePath());}
					);
					patchData=baselineCapital(findBaseLine);}
		else {patchData = CsvTools.ReadAsaHash(baselineMap.iterator().next());}
		return patchData;
	}
	
	


	void capitalsandServiceList() {
		System.out.println("**-> "+Path.fileFilter("\\Capitals.csv"));
		String[] line0 = CsvTools.columnFromscsv(0, Path.fileFilter("\\Capitals.csv").get(0));
		capitalsName.clear();
		for (int n = 1; n < line0.length; n++) {
			capitalsName.add(line0[n].toLowerCase());
		}
		servicesNames.clear();
		String[] line0s = CsvTools.columnFromscsv(0, Path.fileFilter("\\Services.csv").get(0));
		for (int n = 1; n < line0s.length; n++) {
			servicesNames.add(line0s[n].toLowerCase());
		}
		System.out.println("Capitals->"+capitalsName);
		System.out.println("Services->"+servicesNames);
	}

	public void creatMapGIS() {
		try {
			
			String GisPath = Path.fileFilter("\\GIS\\").get(0);
			// String[][] GIS_Data = CsvTools.csvReader(GisPath);

			HashMap<String, String[]> GIS_Datah = CsvTools.ReadAsaHash(GisPath);
			Cell.GISNames = new ArrayList<>(GIS_Datah.keySet());
		
			String[] colmun = GIS_Datah.values().iterator().next();
			
			
			for (int i = 0; i < colmun.length; i++) {
				int ii = i;
				GIS_Datah.forEach((name, vect) -> {
					if(hashCell.get(GIS_Datah.get("x")[ii] + "," + GIS_Datah.get("y")[ii])!=null) {
						hashCell.get(GIS_Datah.get("x")[ii] + "," + GIS_Datah.get("y")[ii]).GIS.put(name, vect[ii]);}
				});
			}
		} catch (IndexOutOfBoundsException e) {

		}

	}

	public void plotPatchs() {
		P.forEach(patch -> {
			Main_CraftyFx.root.getChildren().add(patch);
		});
	}

	public void colorMap(String name) {
		List<Double> values = new ArrayList<>();
		if (name.equalsIgnoreCase("FR") || name.equalsIgnoreCase("Agent")) {
			P.forEach(p -> {
				if (p.owner != null)
					p.ColorP(p.owner.color);
				else {p.ColorP(Color.WHITE);}
			});
		} else if (capitalsName.contains(name)) {

			P.forEach(patch -> {
				if (patch.capitals.get(name) != null)
					values.add(patch.capitals.get(name));
			});

			double max = Collections.max(values);

			P.forEach(patch -> {
				if (patch.capitals.get(name) != null)
					patch.ColorP(ColorsTools.getColorForValue(max, patch.capitals.get(name)));
			});
		} else if (servicesNames.contains(name)) {

			P.forEach(patch -> {
				if (patch.services.get(name) != null)
					values.add(patch.services.get(name));
			});

			double max = values.size()>0? Collections.max(values):0;

			P.forEach(patch -> {
				if (patch.services.get(name) != null)
					patch.ColorP(ColorsTools.getColorForValue(max, patch.services.get(name)));
			});
		} else /* if (name.equals("LAD19NM") || name.equals("nuts318nm")) */ {
			HashMap<String, Color> colorGis = new HashMap<>();

			P.forEach(p -> {

				colorGis.put(p.GIS.get(name), ColorsTools.RandomColor());
			});

			P.forEach(p -> {
				p.ColorP(colorGis.get(p.GIS.get(name)));
			});

		}
	}

	public void updateCapitals(int year) throws IOException {
		if (!Path.scenario.equalsIgnoreCase("Baseline")) {
			HashMap<String, String[]> patchData = CsvTools
					.ReadAsaHash(Path.fileFilter(year + "", Path.scenario, "\\capitals\\").get(0));

			for (int i = 0; i < patchData.values().iterator().next().length; i++) {
				int ii = i;
				patchData.forEach((name, vect) -> {
					Cell p = hashCell.get(patchData.get("x")[ii] + "," + patchData.get("y")[ii]);
					if (p != null) {
						if (capitalsName.contains(name))
							p.capitals.put(name, Tools.sToD(vect[ii]));
					}
				});
			}
		}
	}

}
