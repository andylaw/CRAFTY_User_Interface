package plumLinking;

import dataLoader.PathsLoader;
import dataLoader.ServiceSet;
import fxmlControllers.ModelRunnerController;
import model.ModelRunner;
import model.Region;
import model.RegionClassifier;

public class PlumToCrafty {
	PlumCommodityMapping mapper = new PlumCommodityMapping();

	public void initialize() {
		mapper.initialize();
		mapper.fromPlumTickToCraftyDemands(PathsLoader.getStartYear());
		replaceCraftyDemands(PathsLoader.getStartYear());
		initialDSEquilibrium();
	}

	public void iterative(int year) {
		mapper.fromPlumTickToCraftyDemands(year);
		replaceCraftyDemands(year);
	}

	void replaceCraftyDemands(int year) {
		int y = year - PathsLoader.getStartYear();
		mapper.finalCountriesDemands.forEach((country, map) -> {
			if (RegionClassifier.regions.keySet().contains(country)) {
				map.forEach((serviceName, value) -> {
					if (ServiceSet.getServicesList().contains(serviceName)) {
						Region R = RegionClassifier.regions.get(country);
						R.getServicesHash().get(serviceName).getDemands().put(y, value);
					}
				});
			}
		});
	}

	public void initialDSEquilibrium() {
		ModelRunnerController.init();
		ModelRunner.regionsModelRunner.values().forEach(rRunner -> {
			rRunner.regionalSupply();
			rRunner.initialDSEquilibrium(rRunner.R.getServicesHash(), rRunner.getRegionalSupply());
			rRunner.R.getServicesHash().values().forEach(s -> {
				s.getDemands().forEach((year, value) -> {
					s.getDemands().put(year, value / s.getCalibration_Factor());
				});
			});
		});
	}

}
