package model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import fxmlControllers.ModelRunnerController;

/**
 * @author Mohamed Byari
 *
 */

public class ModelRunner implements Runnable{

	public CellsLoader cells;
	public String colorDisplay = "FR";
	public boolean mapSynchronisation = true;
	public boolean writeCsvFiles = false;
	public boolean removeNegative = false;
	public boolean usegiveUp = false;
	public boolean isMutated = false;
	public boolean NeighboorEffect = false;
	public double percentageCells = 0.05;
	public double mutationIntval = 0.1;

	public Map<String, Double> supply;
	static HashMap<String, Double> marginal = new HashMap<>();
	public static Map<String, Double> distributionMean;

	public String[][] compositionAFT;
	public String[][] servicedemand;

	public ModelRunner(CellsLoader cells) {
		this.cells = cells;
//		demandUpdate();
		compositionAFT =new String[Paths.getEndtYear() - Paths.getStartYear() + 2][cells.AFtsSet.size()];
		servicedemand = new String[Paths.getEndtYear() - Paths.getStartYear() + 2][CellsSet.getServicesNames().size()
				* 2];
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			servicedemand[0][i] = "ServiceSupply:" + CellsSet.getServicesNames().get(i);
			servicedemand[0][i + CellsSet.getServicesNames().size()] = "Demand:" + CellsSet.getServicesNames().get(i);
		}
		AtomicInteger s = new AtomicInteger();
		cells.AFtsSet.getAftHash().keySet().forEach((label) -> {
			compositionAFT[0][s.getAndIncrement()] = label;
		});
	}

	void calculeSystemSupply() {
		supply = Collections.synchronizedMap(new HashMap<>());
		CellsSet.getCellsSet() .parallelStream() /**/.forEach(c -> {
			if (c.getOwner() != null) {
				CellsSet.getServicesNames().forEach(s -> {
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
		distributionMean = Collections.synchronizedMap(new HashMap<>());
		HashMap<String, Integer> AFTnbr = new HashMap<>();
		CellsSet.getCellsSet() .parallelStream().forEach(c -> {
			if (c.getOwner() != null) {
				CellsSet.getServicesNames().forEach(s -> {
					double sup = c.prodactivity(c.getOwner(), s);
					if (distributionMean.containsKey(c.getOwner().getLabel())) {
						distributionMean.put(c.getOwner().getLabel(),
								distributionMean.get(c.getOwner().getLabel()) + sup);
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
			double marg = removeNegative
					? Math.max(CellsSet.getDemand().get(name)[year - Paths.getStartYear()] - val, 0)
					: CellsSet.getDemand().get(name)[year - Paths.getStartYear()] - val;
			marginal.put(name, marg);
		});
	}

	public void go() {
		int year = Paths.getCurrentYear() < Paths.getEndtYear() ? Paths.getCurrentYear() : Paths.getEndtYear();
		System.out.println("cells.updateCapitals...");
		cells.updateCapitals(year);

		// calcule supply
		System.out.print("calculeSystemSupply...");
		calculeSystemSupply();
		System.out.println("Done");
		if (usegiveUp) {
			System.out.print("calculeDistributionMean...");
			calculeDistributionMean();
			System.out.println("Done");
		}
		// update demande & calcule marginal
		System.out.print("calculeMarginalUtility...");
		calculeMarginalUtility(year, removeNegative);
		System.out.println("Done");
		
		CellsSet.getCellsSet().parallelStream().forEach(c -> {
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
			// the competition
			if (Math.random() < percentageCells || c.getOwner() == null) {
				Manager agent = (Manager) cells.AFtsSet.getAftHash().values().toArray()[new Random()
						.nextInt(cells.AFtsSet.size())];
				c.Competition(agent, isMutated, mutationIntval);
			}
			if (NeighboorEffect) {
				CellsSubSets.actionInNeighboorSameLabel(c);
			}
		});
		// display Map
		if (mapSynchronisation) {
			CellsSet.colorMap(colorDisplay);
		}
		// creat .csv output files: servises and AFT for each land
		if (writeCsvFiles) {
			outPutToCsv(year);
		}
	}

	void outPutToCsv(int year) {
		writOutPutMap(year);
		AtomicInteger m = new AtomicInteger();
		int y = year - Paths.getStartYear() + 1;

		CellsSet.getServicesNames().forEach(name -> {
			servicedemand[y][m.get()] = supply.get(name) + "";
			servicedemand[y][m.get()
					+ CellsSet.getServicesNames().size()] = CellsSet.getDemand().get(name)[year - Paths.getStartYear()]
							+ "";
			m.getAndIncrement();
		});

		HashMap<String, Double> AgentNbr = AFTsLoader.hashAgentNbr();
		AtomicInteger N = new AtomicInteger();
		AgentNbr.forEach((name, value) -> {
			compositionAFT[y][N.getAndIncrement()] = value + "";
		});
	}

	void writOutPutMap(int year) {
		String[][] output = new String[CellsSet.getCellsSet().size() + 1][CellsSet.getServicesNames().size() + 3];
		output[0][0] = "X";
		output[0][1] = "Y";
		output[0][2] = "Agent";
		for (int j = 0; j < CellsSet.getServicesNames().size(); j++) {
			output[0][j + 3] = "Service:" + CellsSet.getServicesNames().get(j);
		}
		AtomicInteger i = new AtomicInteger(1);
		CellsSet.getCellsSet().forEach(c -> {
			output[i.get()][0] = c.getX() + "";
			output[i.get()][1] = c.getY() + "";
			output[i.get()][2] = c.getOwner() != null ? c.getOwner().getLabel() : "lazy";
			for (int j = 0; j < CellsSet.getServicesNames().size(); j++) {
				output[i.get()][j + 3] = c.getServices().get(CellsSet.getServicesNames().get(j)) + "";
			}
			i.getAndIncrement();
		});
		String dir = PathTools.makeDirectory(Paths.getProjectPath() + "\\output\\");
		dir = PathTools.makeDirectory(dir + Paths.getScenario());
		dir = PathTools.makeDirectory(dir + "\\" + ModelRunnerController.outPutFolderName);

		CsvTools.writeCSVfile(output, dir + "\\" + Paths.getScenario() + "-Cell-" + year + ".csv");
	}

	@Override
	public void run() {
		go();
		
	}

}
