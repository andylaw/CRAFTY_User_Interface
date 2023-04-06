package WorldPack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import CameraPack.Camera;
import UtilitiesFx.ColorsTools;
import UtilitiesFx.CsvTools;
import UtilitiesFx.Path;
import UtilitiesFx.Tools;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Map {

	public Set<Patch> P = new HashSet<>();
	public HashMap<String, Patch> HashPatchs = new HashMap<>();
	public Agents agents = new Agents(this);
	
	public String[][] demandTable;

	public void creatMap( Camera camera) {
		 capitalsList() ;

		String[][] patchData = CsvTools.csvReader(Path.fileFilter("Baseline_map_UK", "UK").get(0));// "C:\\Users\\byari-m\\Documents\\Data\\data_EUpaper_nocsv\\worlds\\EU\\regionalisations\\28\\capitals\\Baseline_map.csv");//

		HashMap<String, Integer> indexofcolumn = CsvTools.columnindexof(patchData[0]);

		for (int i = 0; i < patchData.length; i++) {
			Patch p = new Patch( this, new Point2D((int) Tools.sToD(patchData[i][indexofcolumn.get("X")]),
					Tools.sToD(patchData[i][indexofcolumn.get("Y")])));

			P.add(p);
			p.index = P.size();
			for (int j = 0; j < Patch.capitalsName.size(); j++) {
				p.capitalsValue.put(Patch.capitalsName.get(j),
						Tools.sToD(patchData[i][indexofcolumn.get(Patch.capitalsName.get(j))]));
				HashPatchs.put(patchData[i][0] + "," + patchData[i][1], p);
			}

			int ii = i;
			agents.AFT.forEach(a -> {
				if (a.label.equals(patchData[ii][indexofcolumn.get("FR")])) {
					p.owner = a;
					a.Mypaches.add(p);

				}
			});

		}

		

		demandTable = CsvTools.csvReader(Path.fileFilter(Path.senario, "demand").get(0));

	}
	
	
	void capitalsList() {
		 String[] line0 = CsvTools.columnFromscsv(0, Path.fileFilter("\\Capitals.csv").get(0));

		for (int n = 1; n < line0.length; n++) {
			Patch.capitalsName.add(line0[n]);
		}
		
	}

	public void creatMapGIS() {
		String GisPath = Path.fileFilter("\\GIS\\").get(0);
		String[][] GIS_Data = CsvTools.csvReader(GisPath);
		String[] line0 = GIS_Data[0];

		for (int i = 1; i < GIS_Data.length; i++) {
			for (int j = 4; j < GIS_Data[0].length; j++) {
				HashPatchs.get(GIS_Data[i][2] + "," + GIS_Data[i][3]).GIS.put(line0[j], GIS_Data[i][j]);
			}
		}
	}

	public void plotPatchs(Group g) {
		Rectangle R = new Rectangle(6000, 6000);
		R.setTranslateY(-5000);
		R.setTranslateX(-1000);
		R.setFill(Color.TRANSPARENT);
		g.getChildren().add(R);
		// System.out.println(P.size()+" 000");
		P.forEach(patch -> {
			g.getChildren().add(patch);
			// System.out.println(patch.index);
		});

	
		// System.out.println("Finished plotPatchs");
	}

	public void colorMap(String name) {
		List<Double> values = new ArrayList<>();
		if (name.equals("FR") || name.equals("Agent")) {
			P.forEach(p -> {
				if (p.owner != null)
					p.ColorP(p.owner.color);
			});
		} else if (name.equals("LAD19NM") || name.equals("nuts318nm")) {
			HashMap<String, Color> colorGis = new HashMap<>();

			P.forEach(p -> {
				colorGis.put(p.GIS.get("\"" + name + "\""), ColorsTools.RandomColor());
			});

			P.forEach(p -> {
				p.ColorP(colorGis.get(p.GIS.get("\"" + name + "\"")));
			});

		} else if (Patch.capitalsName.contains(name)) {

			P.forEach(patch -> {
				if (patch.capitalsValue.get(name) != null)
					values.add(patch.capitalsValue.get(name));
			});

			double max = Collections.max(values);

			P.forEach(patch -> {
				if (patch.capitalsValue.get(name) != null)
					patch.ColorP(ColorsTools.getColorForValue(max, patch.capitalsValue.get(name)));
			});
		} else if (Patch.servicesName.contains(name)) {

			P.forEach(patch -> {
				if (patch.servicesValue.get(name) != null)
					values.add(patch.servicesValue.get(name));
			});

			double max = Collections.max(values);

			P.forEach(patch -> {
				if (patch.servicesValue.get(name) != null)
					patch.ColorP(ColorsTools.getColorForValue(max, patch.servicesValue.get(name)));
			});
		}
	}

	public void RCPi_SSPi(String path) throws IOException {
		double[][] vect = CsvTools.csvReaderDouble(path);

		for (int i = 1; i < vect.length; i++) {
			Patch p = HashPatchs.get((int) vect[i][0] + "," + (int) vect[i][1]);
			for (int j = 2; j < vect[0].length; j++) {
				if (p != null)
					p.capitalsValue.put(Patch.capitalsName.get(j - 2), vect[i][j]);
			}
		}
	}

}
