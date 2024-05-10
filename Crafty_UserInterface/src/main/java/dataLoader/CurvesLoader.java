package dataLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.graphicalTools.Tools;
import model.CellsSet;
import model.Curvelinear;

public class CurvesLoader {
	private static final Logger LOGGER = LogManager.getLogger(CurvesLoader.class);

	public static Map<String, Curvelinear> hashServicesCurves = new HashMap<>();

	public static void loadcurves() {
		try {
		String path = PathTools.fileFilter("\\Curves.csv").get(0);
		loadcurves(path);}
		catch (NullPointerException e) {
			LOGGER.warn("Unable to find the file for the competitiveness curves model: the default curves for all services will be used. \' y=1x+0 \'");
			CellsSet.getServicesNames().forEach(serviceName->{
				hashServicesCurves.put(serviceName,new Curvelinear(1,0,serviceName));
			});
		}
	}
	public static void loadcurves(String path) {
	
		HashMap<String, ArrayList<String>> hash = ReaderFile.ReadAsaHash(path);
		try {
			ArrayList<String> eqs = hash.get("Equation");
			System.out.println(eqs);
			for (int i = 0; i < eqs.size(); i++) {
				String eq = eqs.get(i);		
				String serviceName = hash.get("Service").get(i);
				Curvelinear curve = new Curvelinear(serviceName);
				String[] vec = eq.split("x");
				if (vec.length != 2) {
					LOGGER.warn("The equation  \"" + eq + "\" for service " + serviceName
							+ " is not in the correct format \"ax=b\"   The curve for " + serviceName
							+ " will be y=1x+0.");
					curve.setAB(1, 0);
				} else {
					curve.setAB(Tools.sToD(vec[0]), Tools.sToD(vec[1]));
				}
				hashServicesCurves.put(serviceName,curve);
				
			}
		} catch (NullPointerException e) {
			LOGGER.fatal("The file is not in the correct format:" + path);
		}
		LOGGER.info("Curves Considered"+ hashServicesCurves);
	}

}
