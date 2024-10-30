package model;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dataLoader.CellsLoader;
import dataLoader.DemandModel;
import dataLoader.PathsLoader;
import dataLoader.S_WeightLoader;
import dataLoader.ServiceSet;

public class RegionClassifier {

	private static final Logger LOGGER = LogManager.getLogger(RegionClassifier.class);
	public static ConcurrentHashMap<String, Region> regions;

	public static void initialation(boolean isRegionalized) {
		regions = new ConcurrentHashMap<>();
		if (isRegionalized) {
			CellsLoader.regionsNamesSet.forEach(regionName -> {
				regions.put(regionName, new Region(regionName));
			});
			CellsLoader.hashCell.values().parallelStream().forEach(c -> {
				String region = c.getCurrentRegion();
				regions.get(region).getCells().put(c.getX() + "," + c.getY(), c);
			});

			if (!ServiceSet.isRegionalServicesExisted()) {
				initialation(false);
			}
		} else {
			String name = PathsLoader.WorldName;
			regions.put(name, new Region(name));
			regions.get(name).setCells(CellsLoader.hashCell);
		}
		ServiceSet.initialseServices();
		DemandModel.updateRegionsDemand();
		S_WeightLoader.updateRegionsWeight();

		if (regions.size() == 1) {
			DemandModel.worldService = regions.values().iterator().next().getServicesHash();
			DemandModel.worldService.values().forEach(s -> {
				System.out.println(s.getName() + "  " + s.getDemands());
			});
		} else {
			regions.values().forEach(r -> {
				r.getServicesHash().forEach((ns, s) -> {
					s.getDemands().forEach((year, value) -> {
						DemandModel.worldService.get(ns).getDemands().merge(year, value, Double::sum);
					});
				});
			});
		}

//		ServiceSet.getServicesList().forEach((ns) -> {
//			System.out.println("|||...  " + ns + ":   " + DemandModel.worldService.get(ns).getDemands());
//			;
//		});

		LOGGER.info("Regions: " + regions.keySet());

	}

}
