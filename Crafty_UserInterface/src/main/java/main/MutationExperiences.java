package main;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.Agents;
import dataLoader.MapLoader;
import dataLoader.Paths;
import model.AFT;
import model.Lattice;
import model.Rules;

public class MutationExperiences {
	MapLoader M = new MapLoader();

	public void importSpace() {
		Paths.initialisation("C:\\Users\\byari-m\\Documents\\Data\\data_EUpaper_nocsv");
		// Path.scenario="RCP4_5-SSP4";
		M.loadCapitalsAndServiceList();
		// M.agents.initialseAFT();
		creatRandomAFTs(17);
		M.loadMap();
		asosieteRandomAAFTTocells();
		// M.creatMapGIS();
		Lattice.plotCells();
		Lattice.colorMap("FR");
		System.out.println("cells number " + FxMain.root.getChildren().size());
		run(1);
//		for (int i = 2; i < 10; i++) {
//		selectWinnersAndInitialiseNextRun();
//		run(i);
//		}

	}

	public void creatRandomAFTs(int nbrOfAFTs) {
		for (int i = 0; i < nbrOfAFTs; i++) {
			AFT a = new AFT("AFT" + i, 500.);
			Agents.aftReSet.put(a.getLabel(), a);
		}
	}

	void selectWinnersAndInitialiseNextRun() {
		List<String> losers = Tools.getKeysInSortedOrder(Agents.hashAgentNbr());
		System.out.println(losers);
		for (int i = 0; i < losers.size() / 2; i++) {
			Agents.aftReSet.remove(losers.get(i));
			AFT a = new AFT(losers.get(i), 500.);
			Agents.aftReSet.put(losers.get(i), a);
		}
		asosieteRandomAAFTTocells();

	}

	public void asosieteRandomAAFTTocells() {
		Lattice.getCellsSet().forEach(c -> {
			c.setOwner(Agents.aftReSet
					.get(Agents.aftReSet.keySet().toArray()[new Random().nextInt(Agents.aftReSet.keySet().size())]));
		});
	}

	void run(int simNBR) {
		int endStat = 70;
		String[][] servicedemand = new String[endStat + 2][Lattice.getServicesNames().size() * 2];
		for (int i = 0; i < Lattice.getServicesNames().size(); i++) {
			servicedemand[0][i] = "ServiceSupply:" + Lattice.getServicesNames().get(i);
			servicedemand[0][i + Lattice.getServicesNames().size()] = "Demand:" + Lattice.getServicesNames().get(i);
		}
		Rules R = new Rules(M);
		R.removeNegative = false;
		R.mapSynchronisation = true;
		R.usegiveUp = true;
//		R.isMutated =true;
//		R.mutationIntval=0.5;

		AtomicInteger tick = new AtomicInteger(2016);
		for (int i = 0; i < endStat; i++) {
			R.go(tick.get(), "path");
			tick.getAndIncrement();

			AtomicInteger m = new AtomicInteger();
			Lattice.getServicesNames().forEach(name -> {
				servicedemand[tick.get() - 2015][m.get()] = R.supply.get(name) + "";
				servicedemand[tick.get() - 2015][m.get()
						+ Lattice.getServicesNames().size()] = Lattice.getDemand().get(name)[tick.get() - Paths.getStartYear()]
								+ "";
				m.getAndIncrement();
			});
		}

		String dir = PathTools
				.makeDirectory(Paths.getProjectPath() + "\\output\\" + Paths.getScenario() + "\\" + simNBR);
		writOutPut(dir);
		writeProduction(dir);
		CsvTools.writeCSVfile(servicedemand, dir + "\\" + Paths.getScenario() + "-AggregateServiceDemand.csv");
		HashMap<String, Double> nbr = Agents.hashAgentNbr();
		String[][] NBR = new String[2][nbr.size()];
		NBR[0] = nbr.keySet().toArray(new String[0]);
		NBR[2] = nbr.values().stream().map(String::valueOf).toArray(String[]::new);
		CsvTools.writeCSVfile(NBR, dir + "\\" + Paths.getScenario() + "-NBR.csv");

	}

	void writeProduction(String dir) {
		Agents.aftReSet.forEach((name, a) -> {
			String[][] tablPruduction = new String[Lattice.getServicesNames().size() + 1][Lattice.getCapitalsName().size() + 2];

			tablPruduction[0][0] = "";
			for (int i = 1; i < tablPruduction.length; i++) {
				tablPruduction[i][0] = Lattice.getServicesNames().get(i - 1);
			}

			for (int i = 1; i < tablPruduction[0].length - 1; i++) {
				tablPruduction[0][i] = Lattice.getCapitalsName().get(i - 1);
			}
			tablPruduction[0][tablPruduction[0].length - 1] = "Production";
			for (int i = 1; i < tablPruduction.length; i++) {
				tablPruduction[i][tablPruduction[0].length - 1] = a.getProductivityLevel()
						.get(Lattice.getServicesNames().get(i - 1)) + "";
			}

			for (int i = 1; i < tablPruduction[0].length - 1; i++) {
				for (int j = 1; j < tablPruduction.length; j++) {
					tablPruduction[j][i] = "" + a.getSensitivty()
							.get(Lattice.getCapitalsName().get(i - 1) + "_" + Lattice.getServicesNames().get(j - 1));
				}
			}

			CsvTools.writeCSVfile(tablPruduction, dir + "\\" + name + ".csv");

		});
	}

	void writOutPut(String dir) {
		String[][] output = new String[Lattice.getHashCell().size() + 1][Lattice.getServicesNames().size() + 3];
		output[0][0] = "X";
		output[0][1] = "Y";
		output[0][2] = "Agent";
		for (int j = 0; j < Lattice.getServicesNames().size(); j++) {
			output[0][j + 3] = "Service:" + Lattice.getServicesNames().get(j);
		}
		AtomicInteger i = new AtomicInteger(1);
		Lattice.getCellsSet().forEach((c) -> {
			output[i.get()][0] = c.getX() + "";
			output[i.get()][1] = c.getY() + "";
			output[i.get()][2] = c.getOwner() != null ? c.getOwner().getLabel() : "lazy";
			for (int j = 0; j < Lattice.getServicesNames().size(); j++) {
				output[i.get()][j + 3] = c.getServices().get(Lattice.getServicesNames().get(j)) + "";
			}
			i.getAndIncrement();
		});

		CsvTools.writeCSVfile(output, dir + "\\" + Paths.getScenario() + "-Cell-" + 2080 + ".csv");
	}
}
