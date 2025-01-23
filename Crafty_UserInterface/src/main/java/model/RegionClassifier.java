package model;

import java.util.concurrent.ConcurrentHashMap;

import dataLoader.CellsLoader;
import dataLoader.DemandModel;
import dataLoader.PathsLoader;
import dataLoader.S_WeightLoader;
import dataLoader.ServiceSet;
import main.ConfigLoader;
import utils.analysis.CustomLogger;

public class RegionClassifier {

	private static final CustomLogger LOGGER = new CustomLogger(RegionClassifier.class);
	public static ConcurrentHashMap<String, Region> regions;
	public static boolean regionalization = ConfigLoader.config.regionalization;

	public static void initialation() {
		regions = new ConcurrentHashMap<>();
		if (regionalization) {
			CellsLoader.regionsNamesSet.forEach(regionName -> {
				regions.put(regionName, new Region(regionName));
			});
			CellsLoader.hashCell.values()/* .parallelStream() */.forEach(c -> {
				if (c.getCurrentRegion() != null) {
					regions.get(c.getCurrentRegion()).getCells().put(c.getX() + "," + c.getY(), c);
				}
			});

			if (!ServiceSet.isRegionalServicesExisted()) {
				regionalization = false;
				initialation();
			}
		} else {
			String name = PathsLoader.WorldName;
			regions.put(name, new Region(name));
			regions.get(name).setCells(CellsLoader.hashCell);
		}

		ServiceSet.initialseServices();
		serviceupdater();

		LOGGER.info("Regions: " + regions.keySet());
	}

	public static void serviceupdater() {
		DemandModel.updateRegionsDemand();
		S_WeightLoader.updateRegionsWeight();
		S_WeightLoader.updateWorldWeight();
		aggregateDemandToWorldServiceDemand();
	}

	public static void aggregateDemandToWorldServiceDemand() {
		for (int i = PathsLoader.getStartYear(); i <= PathsLoader.getEndtYear(); i++) {
			int year = i;
			ServiceSet.worldService.forEach((ns, s) -> {
				s.getDemands().put(year, 0.);
			});
			regions.values().forEach(r -> {
				r.getServicesHash().forEach((ns, s) -> {
					ServiceSet.worldService.get(ns).getDemands().merge(year, s.getDemands().get(year), Double::sum);
				});
			});
		}
	}

}
