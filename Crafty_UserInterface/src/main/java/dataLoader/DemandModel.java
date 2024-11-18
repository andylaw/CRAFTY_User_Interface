package dataLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import model.Region;
import model.RegionClassifier;
import utils.analysis.CustomLogger;
import utils.filesTools.PathTools;
import utils.filesTools.ReaderFile;
import utils.graphicalTools.Tools;

public class DemandModel {

	private static final CustomLogger LOGGER = new CustomLogger(DemandModel.class);
	public static void updateRegionsDemand() {
		RegionClassifier.regions.values().forEach(r -> {
			updateDemand(r);
		});
	}


	private static void updateDemand(Region R) {
		Path path = null;
		try {
			path = PathTools.fileFilter(PathsLoader.getScenario(), PathTools.asFolder("demand"), "_"+R.getName()).get(0);
		} catch (NullPointerException e) {
			LOGGER.warn("No demand file fund for region: |" + R.getName() + "|");
			return;
		}

		HashMap<String, ArrayList<String>> hashDemand = ReaderFile.ReadAsaHash(path);
		LOGGER.info("Update Demand for [" + R.getName() + "]: " + path);
		hashDemand.forEach((serviceName, vect) -> {
			if (ServiceSet.getServicesList().contains(serviceName)) {
				ConcurrentHashMap<Integer, Double> dv = new ConcurrentHashMap<>();
				for (int i = 0; i < PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 1; i++) {
					if (i < vect.size()) {
						dv.put(i, Tools.sToD(vect.get(i)));
					}
				}
				R.getServicesHash().get(serviceName).setDemands(dv);
			}
		});
	}

	public static Map<String, ArrayList<Double>> serialisationWorldDemand() {
		Map<String, ArrayList<Double>> serviceSerialisation = new HashMap<>();
		ServiceSet.worldService.forEach((serviceName, service) -> {
			serviceSerialisation.put(serviceName, new ArrayList<>(service.getDemands().values()));
		});
		return serviceSerialisation;
	}
}
