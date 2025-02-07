package dataLoader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import model.Region;
import model.RegionClassifier;
import utils.analysis.CustomLogger;
import utils.filesTools.PathTools;
import utils.filesTools.ReaderFile;
import utils.graphicalTools.Tools;

// Service Weight loader calss
public class S_WeightLoader {
	private static final CustomLogger LOGGER = new CustomLogger(S_WeightLoader.class);

	private static Path weighWolrldtPath() {
		AtomicReference<String> path = new AtomicReference<>("");
		try {
			path.set(PathTools.fileFilter(PathsLoader.getScenario(), PathTools.asFolder("Service_Utility_Weights"),
					PathsLoader.WorldName).get(0).toString());
		} catch (NullPointerException e) {
			LOGGER.warn("No Weight file fund for region: |" + PathsLoader.WorldName
					+ "| will use 1 for all Service Utility Weights ");
			return null;
		}
		return Paths.get(path.get());
	}

	public static void updateWorldWeight() {
		if (RegionClassifier.regions.size() > 1) {
			Path path = weighWolrldtPath();
			if (path != null) {
				HashMap<String, ArrayList<String>> hashDemand = ReaderFile.ReadAsaHash(path);
				LOGGER.info("Update Demand: " + path);
				hashDemand.forEach((name, vect) -> {
					if (ServiceSet.getServicesList().contains(name)) {
						for (int i = 0; i < PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 1; i++) {
							if (i < vect.size()) {
								ServiceSet.worldService.get(name).getWeights().put(i, Tools.sToD(vect.get(i)));
							} else {
								ServiceSet.worldService.get(name).getWeights().put(i,
										Tools.sToD(vect.get(vect.size() - 1)));
								LOGGER.info("There are no demand \'" + name + "\' for this year: \""
										+ (i + PathsLoader.getStartYear()) + "\" using the latest available demands "
										+ (vect.size() - 1 + PathsLoader.getStartYear()));
							}
						}
					}
				});
			} else {
				ServiceSet.getServicesList().forEach((serviceName) -> {
					for (int i = 0; i < PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 1; i++) {
						ServiceSet.worldService.get(serviceName).getWeights().put(i, 1.);
					}

				});
			}
		}
	}

	public static void updateRegionsWeight() {
		RegionClassifier.regions.values().forEach(r -> {
			r.getServicesHash().values().forEach(s -> {
				s.getWeights().clear();
			});
			updateWeight(r);
		});
	}

	private static void updateWeight(Region R) {
		Path path;
		try {
			path = PathTools
					.fileFilter(PathsLoader.getScenario(), PathTools.asFolder("Service_Utility_Weights"), R.getName())
					.get(0);
		} catch (NullPointerException e) {
			LOGGER.warn("No Weight file fund for region: |" + R.getName()
					+ "|  will use 1 for all Service Utility Weights ");
			ServiceSet.getServicesList().forEach((serviceName) -> {
				ConcurrentHashMap<Integer, Double> dv = new ConcurrentHashMap<>();
				for (int i = 0; i < PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 1; i++) {
					dv.put(i+PathsLoader.getStartYear(), 1.);
				}
				R.getServicesHash().get(serviceName).setWeights(dv);

			});
			return;
		}
		HashMap<String, ArrayList<String>> hashWeight = ReaderFile.ReadAsaHash(path);
		LOGGER.info("Update Weight for [" + R + "]: " + path);

		hashWeight.forEach((serviceName, vect) -> {
			if (ServiceSet.getServicesList().contains(serviceName)) {
				ConcurrentHashMap<Integer, Double> dv = new ConcurrentHashMap<>();
				for (int i = 0; i < PathsLoader.getEndtYear() - PathsLoader.getStartYear() + 1; i++) {
					if (i < vect.size()) {
						dv.put(i+PathsLoader.getStartYear(), Tools.sToD(vect.get(i)));
					}
				}
				R.getServicesHash().get(serviceName).setWeights(dv);
			}
		});
	}

}
