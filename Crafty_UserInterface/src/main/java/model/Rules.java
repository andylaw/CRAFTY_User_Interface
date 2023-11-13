package model;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import dataLoader.Agents;
import dataLoader.MapLoader;
import dataLoader.Paths;

public class Rules {

	public MapLoader M = new MapLoader();
	public String colorDisplay = "FR";
	public boolean mapSynchronisation = true;
	public boolean writeCsvFiles = false;
	public boolean removeNegative = false;
	public boolean usegiveUp = false;
	public boolean isMutated = false;
	public boolean NeighboorEffect = false;
	public double percentageCells = 0.05;

	public double mutationIntval = 0.1;
//	public HashMap<String,double[]> demand = new HashMap<>();
	public HashMap<String, Double> supply;
	static HashMap<String, Double> marginal = new HashMap<>();
	public static HashMap<String, Double> distributionMean;

	public String[][] compositionAFT;
	public String[][] servicedemand;

	public Rules(MapLoader M) {
		this.M = M;
//		demandUpdate();
		compositionAFT = new String[Paths.getEndtYear() - Paths.getStartYear() + 1][Agents.aftReSet.size()];
		servicedemand = new String[Paths.getEndtYear() - Paths.getStartYear() + 1][Lattice.getServicesNames().size() * 2];
		for (int i = 0; i < Lattice.getServicesNames().size(); i++) {
			servicedemand[0][i] = "ServiceSupply:" + Lattice.getServicesNames().get(i);
			servicedemand[0][i + Lattice.getServicesNames().size()] = "Demand:" + Lattice.getServicesNames().get(i);
		}
		AtomicInteger s = new AtomicInteger();
		Agents.aftReSet.keySet().forEach((label) -> {
			compositionAFT[0][s.getAndIncrement()] = label;
		});
	}

//	public void demandUpdate() {
//		HashMap<String, String[]> data = CsvTools.ReadAsaHash(Path.fileFilter(Path.scenario, "demands").get(0));
//		data.forEach((name, vect) -> {
//			if (!name.equals("year")) {
//				double[] v = new double[vect.length];
//				for (int i = 0; i < vect.length; i++) {
//					demand.put((name + "_" + data.get("Year")[i]).replace(" ", ""), Tools.sToD(vect[i]));
//				}}
//		});
//	}

	void calculeSystemSupply() {
		supply = new HashMap<>();
		Lattice.getCellsSet().forEach(c -> {
			if (c.getOwner() != null) {

				Lattice.getServicesNames().forEach(s -> {
					double sup = c.prodactivity(c.getOwner(), s);
					if (supply.containsKey(s)) {
						supply.put(s, supply.get(s) + sup);
					} else {
						supply.put(s, sup);
					}
				});
			}
		});

	}

	void calculeDistributionMean() {
		distributionMean = new HashMap<>();
		HashMap<String, Integer> AFTnbr = new HashMap<>();
		Lattice.getCellsSet().forEach(c -> {
			if (c.getOwner() != null) {
				Lattice.getServicesNames().forEach(s -> {
					double sup = c.prodactivity(c.getOwner(), s);
					if (distributionMean.containsKey(c.getOwner().getLabel())) {
						distributionMean.put(c.getOwner().getLabel(), distributionMean.get(c.getOwner().getLabel()) + sup);
					} else {
						distributionMean.put(c.getOwner().getLabel(), sup);
					}
				});
				if (AFTnbr.containsKey(c.getOwner().getLabel()))
					AFTnbr.put(c.getOwner().getLabel(), AFTnbr.get(c.getOwner().getLabel()) + 1);
				else
					AFTnbr.put(c.getOwner().getLabel(), 1);
			}
		});
		distributionMean.forEach((aftName, total) -> {
			distributionMean.put(aftName, total / AFTnbr.get(aftName));
		});
	}

	void calculeMarginalUtility(int year, boolean removeNegative) {

		supply.forEach((name, val) -> {
			double marg = removeNegative ? Math.max(Lattice.getDemand().get(name)[year-Paths.getStartYear() ] - val, 0)
					: Lattice.getDemand().get(name)[year-Paths.getStartYear() ] - val;
			marginal.put(name, marg);
		});
	}

	public void go(int year, String outPutFolderName) {
		year = year < Paths.getStartYear() ? year : Paths.getEndtYear();
		M.updateCapitals(year);

		// calcule supply
		calculeSystemSupply();
		if (usegiveUp) {
			calculeDistributionMean();
		}
		// update demande & calcule marginal
		calculeMarginalUtility(year, removeNegative);

		Lattice.getCellsSet().forEach(c -> {
			c.putservices();
			// Abandonment of lands
			if (c.getOwner() != null) {
				if (usegiveUp) {
					double sum = 0;
					for (int i = 0; i < c.getServices().values().toArray().length; i++) {
						sum += (double) c.getServices().values().toArray()[i];
					}
					if (sum < distributionMean.get(c.getOwner().getLabel())
							* (c.getOwner().getGiveUpMean() + c.getOwner().getGiveUpSD() * new Random().nextGaussian())
							&& c.getOwner().getGiveUpProbabilty() > Math.random()) {
						c.setOwner(null);
					}
				}
			}
			// Randomly select percentageCells% of the land available to compete on, and set
			// the
			// competition
			if (Math.random() < percentageCells || c.getOwner() == null) {
				AFT agent = (AFT) Agents.aftReSet.values().toArray()[new Random().nextInt(Agents.aftReSet.size())];
				c.Competition(agent, isMutated, mutationIntval);
			}
			if (NeighboorEffect) {
				c.checkNeighboorSameLabel();
			}
		});
		// display Map
		if (mapSynchronisation) {
			Lattice.colorMap(colorDisplay);
		}
		// creat .csv output files: servises and AFT for each land
		if (writeCsvFiles) {
			outPutToCsv(year, outPutFolderName);
		}
	}

	void outPutToCsv(int year, String outPutFolderName) {
		writOutPutMap(year, outPutFolderName);
		AtomicInteger m = new AtomicInteger();
		int y = year - Paths.getStartYear() + 1;
		Lattice.getServicesNames().forEach(name -> {
			servicedemand[y][m.get()] = supply.get(name) + "";
			servicedemand[y][m.get() + Lattice.getServicesNames().size()] = Lattice.getDemand().get(name)[year-Paths.getStartYear() ] + "";
			m.getAndIncrement();
		});

		HashMap<String, Double> AgentNbr = Agents.hashAgentNbr();
		AtomicInteger N = new AtomicInteger();
		AgentNbr.forEach((name, value) -> {
			compositionAFT[y][N.getAndIncrement()] = value + "";
		});
	}

	void writOutPutMap(int year, String folderName) {
		String[][] output = new String[Lattice.getHashCell().size() + 1][Lattice.getServicesNames().size() + 3];
		output[0][0] = "X";
		output[0][1] = "Y";
		output[0][2] = "Agent";
		for (int j = 0; j < Lattice.getServicesNames().size(); j++) {
			output[0][j + 3] = "Service:" + Lattice.getServicesNames().get(j);
		}
		AtomicInteger i = new AtomicInteger(1);
		Lattice.getHashCell().forEach((coor, c) -> {
			output[i.get()][0] = c.getX() + "";
			output[i.get()][1] = c.getY() + "";
			output[i.get()][2] = c.getOwner() != null ? c.getOwner().getLabel() : "lazy";
			for (int j = 0; j < Lattice.getServicesNames().size(); j++) {
				output[i.get()][j + 3] = c.getServices().get(Lattice.getServicesNames().get(j)) + "";
			}
			i.getAndIncrement();
		});
		String dir = PathTools.makeDirectory(Paths.getProjectPath() + "\\output\\");
		dir = PathTools.makeDirectory(dir + Paths.getScenario());
		dir = PathTools.makeDirectory(dir + "\\" + folderName);

		CsvTools.writeCSVfile(output, dir + "\\" + Paths.getScenario() + "-Cell-" + year + ".csv");
	}

}
