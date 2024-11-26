package plumLinking;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ac.ed.lurg.ModelConfig;
import utils.filesTools.PathTools;
import utils.graphicalTools.Tools;

public class PlumCommodityMapping {
	ArrayList<Path> allpaths;
	private List<Map<String, String>> bio_fractions;
	private List<Map<String, String>> waste_df;
	private List<Map<String, String>> country_demand;
	private List<Map<String, String>> domestic;
	private HashMap<String, Set<String>> FilterHash = new HashMap<>();
	Map<String, String> countryShortNameMap = new HashMap<>();

	void Eu_countries() {
		String[] EuCountries = { "Cyprus", "Czechia", "Portugal", "Greece", "Austria", "Latvia", "Netherlands",
				"Sweden", "Ireland", "Belgium & Luxembourg", "Poland", "Slovakia", "Slovenia", "Bulgaria", "France",
				"Lithuania", "Croatia", "Italy & Malta", "Romania", "Hungary", "United Kingdom", "Switzerland", "Spain",
				"Norway", "Finland", "Denmark", "Germany", "Estonia" };
		String[] shortNames = { "CY", "CZ", "PT", "EL", "AT", "LV", "NL", "SE", "IE", "BE", "PL", "SK", "SI", "BG",
				"FR", "LT", "HR", "MT", "RO", "HU", "UK", "CH", "ES", "NO", "FI", "DK", "DE", "EE" };

		for (int i = 0; i < EuCountries.length; i++) {
			countryShortNameMap.put(EuCountries[i], shortNames[i]);
		}
	}

	public Map<String, Map<String, Double>> finalCountriesDemands = new HashMap<>();

	public void initialize() {
		allpaths = PathTools.findAllFiles(Paths.get(ModelConfig.OUTPUT_DIR));
		Eu_countries();
		FilterHash.put("Country", new HashSet<>(countryShortNameMap.keySet()));
		staticFilesinitialisation();

	}

	void fromPlumTickToCraftyDemands(int tick) {
		iterativeFileReadingAndFilter(tick);
		mappingDemands();
	}

	void staticFilesinitialisation() {
		bio_fractions = LinkingTools.filterMapsByCriteria(
				LinkingTools.readCsvIntoList(PathTools.fileFilter(allpaths, "bio_fractions_df.csv").get(0)),
				FilterHash);
		waste_df = LinkingTools.readCsvIntoList(PathTools.fileFilter(allpaths, "waste_df.csv").get(0));
	}

	void iterativeFileReadingAndFilter(int year) {
		FilterHash.put("Year", Set.of(String.valueOf(year)));
		country_demand = LinkingTools.filterMapsByCriteria(
				LinkingTools.readCsvIntoList(PathTools.fileFilter(allpaths, "countryDemand.txt").get(0)), FilterHash);
		domestic = LinkingTools.filterMapsByCriteria(
				LinkingTools.readCsvIntoList(PathTools.fileFilter(allpaths, "domestic.txt").get(0)), FilterHash);
	}

	List<Map<String, String>> bio_crop_demand_df() {
		List<Map<String, String>> merge = LinkingTools.left_join_many_to_many(country_demand, bio_fractions, "Country",
				"Commodity");
		merge = LinkingTools.left_join(merge, waste_df, "Crop");
		merge.forEach(map -> {
			double tmp = Tools.sToD(map.get("BioenergyDemand")) * Tools.sToD(map.get("BioFraction"))
					/ (1 - Tools.sToD(map.get("WasteRate")));
			map.put("BioenergyDemand", String.valueOf(tmp));
		});
		merge.forEach(map -> {
			map.remove("Demand");
			map.remove("Commodity");
			map.remove("WasteRate");
			map.remove("BioFraction");
			map.remove("ConsumerPrice");
		});
		return merge;
	}

	void mappingDemands() {
		List<Map<String, String>> domistic = domestic_prod();
		// split by countries
		Map<String, List<Map<String, String>>> countriesDemands = new HashMap<>();
			for(String country: countryShortNameMap.keySet()) {
			List<Map<String, String>> d = new ArrayList<>();
			domistic.forEach(line -> {
				if (line.get("Country").equals(country)) {
					d.add(line);
				}
			});
			countriesDemands.put(countryShortNameMap.get(country), d);
		}

		countriesDemands.forEach((country, demandMap) -> {
			Map<String, Double> map = new HashMap<>();
			for (Map<String, String> line : demandMap) {
				double Fodder_crops = getCrop(line, "wheat", "Rum_feed_produced")
						+ getCrop(line, "wheat", "Mon_feed_produced") + getCrop(line, "maize", "Rum_feed_produced")
						+ getCrop(line, "maize", "Mon_feed_produced") + getCrop(line, "rice", "Rum_feed_produced")
						+ getCrop(line, "rice", "Mon_feed_produced")
						+ getCrop(line, "oilcropsNFix", "Rum_feed_produced")
						+ getCrop(line, "oilcropsNFix", "Mon_feed_produced");
				map.merge("Foddercrops", Fodder_crops, Double::sum);
				double Bioenergy1G = getCrop(line, "wheat", "Bioenergy_produced")
						+ getCrop(line, "maize", "Bioenergy_produced") + getCrop(line, "rice", "Bioenergy_produced")
						+ getCrop(line, "oilcropsNFix", "Bioenergy_produced");
				map.merge("BioenergyG1", Bioenergy1G, Double::sum);
				map.merge("Pasture", getCrop(line, "pasture", "Rum_feed_produced"), Double::sum);
				map.merge("C4crops", getCrop(line, "maize", "Food_produced"), Double::sum);
				map.merge("C3rice", getCrop(line, "rice", "Food_produced"), Double::sum);//
				map.merge("C3oilNFix", getCrop(line, "oilcropsNFix", "Food_produced"), Double::sum);//
				map.merge("C3oilcrops", getCrop(line, "oilcropsOther", "Food_produced"), Double::sum);
				map.merge("C3starchyroots", getCrop(line, "starchyRoots", "Food_produced"), Double::sum);
				map.merge("C3cereals", getCrop(line, "wheat", "Food_produced"), Double::sum);
				map.merge("C3fruitveg", getCrop(line, "fruitveg", "Food_produced"), Double::sum);
				map.merge("BioenergyG2", getCrop(line, "energycrops", "Bioenergy_produced"), Double::sum);
			}
			finalCountriesDemands.put(country, map);
		});
	}

	List<Map<String, String>> domestic_prod() {
		List<Map<String, String>> bio_crop_demand = bio_crop_demand_df();
		List<Map<String, String>> left_join = LinkingTools.left_join(domestic, bio_crop_demand, "Year", "Country",
				"Crop");

		left_join.forEach(map -> {
			double tmp = Tools.sToD(map.get("Production")) + Tools.sToD(map.get("Net_imports"));
			map.put("Supply", String.valueOf(tmp));
		});

		left_join.forEach(map -> {
			double tmp = Tools.sToD(map.get("BioenergyDemand"));
			if (map.get("Crop").equals("energycrops")) {
				tmp = Tools.sToD(map.get("Supply"));
			} else if (map.get("Crop").equals("pasture") || map.get("Crop").equals("setaside")) {
				tmp = 0;
			}
			map.put("BioenergyDemand", String.valueOf(tmp));
		});
		left_join.forEach(map -> {
			double tmp = Tools.sToD(map.get("Supply")) - Tools.sToD(map.get("Mon_feed_amount"))
					- Tools.sToD(map.get("Rum_feed_amount")) - Tools.sToD(map.get("BioenergyDemand"));
			map.put("Food", String.valueOf(tmp));
		});

		left_join.forEach(map -> {
			map.remove("Import_price");
			map.put("Bioenergy", map.get("BioenergyDemand"));
			map.remove("BioenergyDemand");
			map.remove("Net_import_cost");
			map.remove("Export_price");
			map.remove("Area");
			map.remove("Production_cost");
			map.put("Rum_feed", map.get("Rum_feed_amount"));
			map.remove("Rum_feed_amount");
			map.remove("Production_cost");
			map.remove("Consumer_price");
			map.remove("Prod_shock");
			map.put("Mon_feed", map.get("Mon_feed_amount"));
			map.remove("Mon_feed_amount");
		});
		left_join.forEach(map -> {
			double tmp = Tools.sToD(map.get("Supply")) > 0
					? Tools.sToD(map.get("Production")) / Tools.sToD(map.get("Supply"))
					: 0;
			map.put("ProductionRatio", String.valueOf(tmp));
			double tmp2 = Tools.sToD(map.get("Food")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Food_produced", String.valueOf(tmp2));
			double tmp3 = Tools.sToD(map.get("Bioenergy")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Bioenergy_produced", String.valueOf(tmp3));
			double tmp4 = Tools.sToD(map.get("Mon_feed")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Mon_feed_produced", String.valueOf(tmp4));
			double tmp5 = Tools.sToD(map.get("Rum_feed")) * Tools.sToD(map.get("ProductionRatio"));
			map.put("Rum_feed_produced", String.valueOf(tmp5));
		});
		left_join.forEach(map -> {
			map.remove("Supply");
			map.remove("Net_imports");
			map.remove("Production");
			map.remove("Rum_feed");
			map.remove("Bioenergy");
			map.remove("Mon_feed");
			map.remove("Food");
			map.remove("ProductionRatio");
		});
		return left_join;
	}

	double getCrop(Map<String, String> map, String val1, String key2) {
		return map.get("Crop").equals(val1) ? Tools.sToD(map.get(key2)) : 0;

	}
}
