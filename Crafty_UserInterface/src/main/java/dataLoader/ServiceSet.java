package dataLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import model.RegionClassifier;
import model.Service;

public class ServiceSet {
	private static final Logger LOGGER = LogManager.getLogger(ServiceSet.class);
	//private static ConcurrentHashMap<String, Service> servicesHash;
	private static List<String> servicesList;
	
	public static void initialseServices() {
		RegionClassifier.regions.values().forEach(r -> {
			ServiceSet.getServicesList().forEach(n -> {
				r.getServicesHash().put(n, new Service(n));
			});
		});
	}

	public static void loadServiceList() {
	//	servicesHash = new ConcurrentHashMap<>();
		servicesList = Collections.synchronizedList(new ArrayList<>());
	//	servicesHash.clear();
		String[] line0s = CsvTools.columnFromscsv(0, PathTools.fileFilter(File.separator + "Services.csv").get(0));
		for (int n = 1; n < line0s.length; n++) {
//			servicesHash.put(line0s[n], new Service(line0s[n]));
			servicesList.add(line0s[n]);
		}
		LOGGER.info("Services size=" + servicesList.size() + "CellsSet.getServicesNames()=" + servicesList);
//		servicesList.addAll(servicesHash.keySet());
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
