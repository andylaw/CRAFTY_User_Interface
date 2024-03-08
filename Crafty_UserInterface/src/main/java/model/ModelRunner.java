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

public class ModelRunner implements Runnable {

	public CellsLoader cells;
	public String colorDisplay = "FR";
	public boolean mapSynchronisation = true;
	public int mapSynchronisationGap = 5;
	public boolean writeCsvFiles = false;
	public int writeCsvFilesGap = 5;
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
		compositionAFT = new String[Paths.getEndtYear() - Paths.getStartYear() + 2][cells.AFtsSet.size()];
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

		CellsSet.getCells().parallelStream().forEach(c -> {
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
		System.out.println(supply);
	}

	void calculeDistributionMean() {
		distributionMean = Collections.synchronizedMap(new HashMap<>());
		HashMap<String, Integer> AFTnbr = AFTsLoader.hashAgentNbr();
		CellsSet.getCells().parallelStream().forEach(c -> {
			if (c.getOwner() != null) {
				double sup = c.utility(c.getOwner());
				if (distributionMean.containsKey(c.getOwner().getLabel())) {
					distributionMean.put(c.getOwner().getLabel(), distributionMean.get(c.getOwner().getLabel()) + sup);
				} else {
					distributionMean.put(c.getOwner().getLabel(), sup);
				}
			}
		});
		distributionMean.forEach((aftName, total) -> {
			distributionMean.put(aftName, total / AFTnbr.get(aftName));
		});
	}

	void calculeMarginalUtility(int year, boolean removeNegative) {

		supply.forEach((serviceName, serviceVal) -> {
			double marg = removeNegative
					? Math.max(CellsSet.getDemand().get(serviceName)[year - Paths.getStartYear()] - serviceVal, 0)
					: CellsSet.getDemand().get(serviceName)[year - Paths.getStartYear()] - serviceVal;
			marginal.put(serviceName, marg);
		});
	}

	public void go() {
		int year = Paths.getCurrentYear() < Paths.getEndtYear() ? Paths.getCurrentYear() : Paths.getEndtYear();
		System.out.print("cells.updateCapitals| ");
		cells.updateCapitals(year);

		// calcule supply
		System.out.print("Calcule System Supply...");
		calculeSystemSupply();

		System.out.println("Done");

		// update demande & calcule marginal
		System.out.print("calculeMarginalUtility...");
		calculeMarginalUtility(year, removeNegative);
		System.out.println("Done");

		if (usegiveUp) {
			System.out.print("calculeDistributionMean...");
			calculeDistributionMean();
			System.out.println("Done");
		}
		System.out.print("Competition process...");
		CellsSet.getCells().parallelStream().forEach(c -> {
			c.putservices();
			// Randomly select percentageCells% of the land available to compete on, and set
			// the competition
			if (Math.random() < percentageCells || c.getOwner() == null) {
				// Abandonment of lands
				if (c.getOwner() != null) {
					if (usegiveUp) {
						double sum = 0;
						for (int i = 0; i < c.getServices().values().toArray().length; i++) {
							sum += (double) c.getServices().values().toArray()[i];
						}
						if (sum < distributionMean.get(c.getOwner().getLabel()) * (c.getOwner().getGiveUpMean()
								+ c.getOwner().getGiveUpSD() * new Random().nextGaussian())
								&& c.getOwner().getGiveUpProbabilty() > Math.random()) {
							c.setOwner(null);
						}
					}
				}
				// set the competition
				Manager agent = (Manager) cells.AFtsSet.getAftHash().values().toArray()[new Random()
						.nextInt(cells.AFtsSet.size())];
				c.Competition(agent, isMutated, mutationIntval);
			}
			if (NeighboorEffect) {
				CellsSubSets.actionInNeighboorSameLabel(c);
			}
		});
		System.out.println("Done");
		// display Map
		if (mapSynchronisation && ((Paths.getCurrentYear() - Paths.getStartYear()) % mapSynchronisationGap == 0
				|| Paths.getCurrentYear() == Paths.getEndtYear())) {
			CellsSet.colorMap(colorDisplay);
		}
		// creat .csv output files: servises and AFT for each land
		if (writeCsvFiles) {
			outPutChartsToCsv(year);
			if  ((Paths.getCurrentYear() - Paths.getStartYear()) % writeCsvFilesGap == 0
					|| Paths.getCurrentYear() == Paths.getEndtYear()){
			System.out.print("writeCsvFiles...");
			writOutPutMap(year);
			
			System.out.println("Done");
		}}
	}

	void outPutChartsToCsv(int year) {
		AtomicInteger m = new AtomicInteger();
		int y = year - Paths.getStartYear() + 1;

		CellsSet.getServicesNames().forEach(name -> {
			servicedemand[y][m.get()] = supply.get(name) + "";
			servicedemand[y][m.get()
					+ CellsSet.getServicesNames().size()] = CellsSet.getDemand().get(name)[year - Paths.getStartYear()]
							+ "";
			m.getAndIncrement();
		});
//	A switch should be added here because it consumes a lot of calculations for large projects (EU-1km). and that could be produiced after simulation
//		HashMap<String, Integer> AgentNbr = AFTsLoader.hashAgentNbr();
//		AtomicInteger N = new AtomicInteger();
//		AgentNbr.forEach((name, value) -> {
//			compositionAFT[y][N.getAndIncrement()] = value + "";
//		});
	}

	

	private void writOutPutMap(int year) {
		String dir = PathTools.makeDirectory(Paths.getProjectPath() + "\\output\\");
		dir = PathTools.makeDirectory(dir + Paths.getScenario());
		dir = PathTools.makeDirectory(dir + "\\" + ModelRunnerController.outPutFolderName);
		CsvTools.exportToCSV(dir + "\\" + Paths.getScenario() + "-Cell-" + year + ".csv");
	}

	@Override
	public void run() {
		go();

	}

}
