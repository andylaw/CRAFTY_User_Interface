package model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dataLoader.CellsLoader;
import dataLoader.DemandModel;
import dataLoader.Paths;

public class RegionClassifier {

	private static final Logger LOGGER = LogManager.getLogger(RegionClassifier.class);
	public static ConcurrentHashMap<String, ConcurrentHashMap<String, Cell>> regions;
	public static ConcurrentHashMap<String, Set<Cell>> unmanageCellsR = new ConcurrentHashMap<>();

	public static void initialation(boolean isRegionalized) {
		regions = new ConcurrentHashMap<>();
//		try {
			if (isRegionalized) {
				CellsLoader.hashCell.entrySet().parallelStream().forEach(entry -> {
					String region = entry.getValue().getCurrentRegion();
					regions.computeIfAbsent(region, k -> new ConcurrentHashMap<>()).put(entry.getKey(),
							entry.getValue());
				});
				if (!DemandModel.isRegionalDemandExisted()) {
					initialation(false);
				}
			} else {
				regions.put(Paths.WorldName, CellsLoader.hashCell);
			}
			
			DemandModel.updateRegionsDemand();
			regions.keySet().forEach(region -> {
				unmanageCellsR.put(region, ConcurrentHashMap.newKeySet());
			});

			LOGGER.info("Regions: " + regions.keySet());

//		} catch (NullPointerException e) {
//			LOGGER.warn("The Regionalization Files is not Found");
//		}

	}

}
