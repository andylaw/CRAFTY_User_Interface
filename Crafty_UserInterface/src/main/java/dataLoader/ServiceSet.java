package dataLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import model.RegionClassifier;
import model.Service;

public class ServiceSet {
	private static final Logger LOGGER = LogManager.getLogger(ServiceSet.class);
	private static List<String> servicesList;
	public static ConcurrentHashMap<String, Service> worldService = new ConcurrentHashMap<>();

	public static void initialseServices() {
		RegionClassifier.regions.values().forEach(r -> {
			getServicesList().forEach(n -> {
				r.getServicesHash().put(n, new Service(n));
			});
		});
		getServicesList().forEach((ns) -> {
			worldService.put(ns, new Service(ns));
		});
	}

	public static void loadServiceList() {
		servicesList = Collections.synchronizedList(new ArrayList<>());
		String[] line0s = CsvTools.columnFromscsv(0, PathTools.fileFilter(File.separator + "Services.csv").get(0));
		for (int n = 1; n < line0s.length; n++) {
			servicesList.add(line0s[n]);
		}
		LOGGER.info("Services size=  " + servicesList.size() + "  CellsSet.getServicesList()=" + servicesList);
	}

	public static boolean isRegionalServicesExisted() {
		for (String r : RegionClassifier.regions.keySet()) {
			ArrayList<Path> paths = PathTools.fileFilter(PathsLoader.getScenario(), PathTools.asFolder("demand"), r);
			if (paths == null) {
				return false;
			}
		}
		return true;
	}

	public static List<String> getServicesList() {
		return servicesList;
	}
}
