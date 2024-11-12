package dataLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.filesTools.ReaderFile;
import UtilitiesFx.graphicalTools.Tools;
import model.Region;
import model.RegionClassifier;
import model.Service;

public class DemandModel {
	private static final Logger LOGGER = LogManager.getLogger(DemandModel.class);


	public static void updateRegionsDemand() {
		RegionClassifier.regions.values().forEach(r -> {
			updateDemand(r);
		});
	}


	private static void updateDemand(Region R) {
		Path path;
		try {
			path = PathTools.fileFilter(PathsLoader.getScenario(), PathTools.asFolder("demand"), R.getName()).get(0);
		} catch (NullPointerException e) {
			LOGGER.warn("No demand file fund for region: |" + R.getName() + "|");
			return;
		}

		HashMap<String, ArrayList<String>> hashDemand = ReaderFile.ReadAsaHash(path);
		LOGGER.info("Update Demand for [" + R + "]: " + path);
		hashDemand.forEach((serviceName, vect) -> {
			if (ServiceSet.getServicesList().contains(serviceName)) {
				ConcurrentHashMap<Integer, Double> dv = new ConcurrentHashMap<>();
				for (int i = 0; i < PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 1; i++) {
					if (i < vect.size()) {
						dv.put(i, Tools.sToD(vect.get(i)));
//						dv.put(i,
//								Tools.sToD(vect.get(i)) / R.getServicesHash().get(serviceName).getCalibration_Factor());
					}
				}
				R.getServicesHash().get(serviceName).setDemands(dv);
			}
		});
	}

	public static Map<String, ArrayList<Double>> serialisationWorldDemand() {
		Map<String, ArrayList<Double>> serviceSerialisation = new HashMap<>();
		ServiceSet.worldService.forEach((serviceName, service) -> {
			ArrayList<Double> sv = new ArrayList<Double>();
			for (int i = 0; i < PathsLoader.getEndtYear() - PathsLoader.getStartYear()+1; i++) {
				sv.add(service.getDemands().get(i));
			}
			serviceSerialisation.put(serviceName, sv);
		});
		return serviceSerialisation;
	}
}
