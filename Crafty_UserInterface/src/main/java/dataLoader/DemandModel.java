package dataLoader;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.graphicalTools.Tools;
import model.CellsSet;

public class DemandModel {
	private static final Logger LOGGER = LogManager.getLogger(DemandModel.class);

	private static HashMap<String, ArrayList<Double>> demand = new HashMap<>();

	public static HashMap<String, ArrayList<Double>> getDemand() {
		return demand;
	}

	public static double getDemand(String key, int year) {
		return getDemand(key, year, true);
	}

	public static double getDemand(String key, int year, boolean showMessage) {
		int tick = year - Paths.getStartYear();
		try {
			return demand.get(key).get(tick);
		} catch (IndexOutOfBoundsException e) {
			tick = demand.values().iterator().next().size() - 1;
			if (showMessage) {
				LOGGER.warn("There are no demand \'" + key + "\' for this year: \"" + year
						+ "\" using the latest available demands " + (tick + Paths.getStartYear()));
			}
			return demand.get(key).get(tick);
		}

	}

	public static double getAveragedPerCellDemand(String key, int index) {

		return demand.get(key).get(Math.min(index - Paths.getStartYear(), demand.values().iterator().next().size() - 1))
				/ CellsLoader.getNbrOfCells();
	}

	public static void updateDemand() {
		String path = PathTools.fileFilter(Paths.getScenario(), "demand").get(0);
		HashMap<String, ArrayList<String>> hashDemand = ReaderFile.ReadAsaHash(path);
		LOGGER.info("Update Demand: " + path);
		hashDemand.forEach((name, vect) -> {
			if (CellsSet.getServicesNames().contains(name)) {
				ArrayList<Double> v = new ArrayList<>();
				for (int i = 0; i < Paths.getEndtYear() - Paths.getStartYear()+1; i++) {
					if (i < vect.size()) {
						v.add(Tools.sToD(vect.get(i)));
					} else {
						v.add(Tools.sToD(vect.get(vect.size() - 1)));
						LOGGER.info("There are no demand \'" + name + "\' for this year: \""
								+ (i + Paths.getStartYear()) + "\" using the latest available demands "
								+ (vect.size() - 1 + Paths.getStartYear()));
					}
				}
				demand.put(name, v);
			}
		});
	}
}
