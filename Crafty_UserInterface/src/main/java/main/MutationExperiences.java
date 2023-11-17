package main;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import model.Manager;
import model.CellsSet;
import model.ModelRunner;

/**
 * @author Mohamed Byari
 *
 */

public class MutationExperiences {
	CellsLoader M = new CellsLoader();

	public void importSpace() {
		Paths.initialisation("C:\\Users\\byari-m\\Documents\\Data\\data_EUpaper_nocsv");
		// Path.scenario="RCP4_5-SSP4";
		M.loadCapitalsAndServiceList();
		// M.agents.initialseAFT();
		creatRandomAFTs(17);
		
		asosieteRandomAAFTTocells();
		// M.creatMapGIS();
		CellsSet.plotCells();
		CellsSet.colorMap("FR");
		System.out.println("cells number " + FxMain.root.getChildren().size());
		run(1);
//		for (int i = 2; i < 10; i++) {
//		selectWinnersAndInitialiseNextRun();
//		run(i);
//		}

	}

	public void creatRandomAFTs(int nbrOfAFTs) {
		for (int i = 0; i < nbrOfAFTs; i++) {
			Manager a = new Manager("AFT" + i, 500.);
			M.AFtsSet.getAftHash().put(a.getLabel(), a);
		}
	}

	void selectWinnersAndInitialiseNextRun() {
		List<String> losers = Tools.getKeysInSortedOrder(AFTsLoader.hashAgentNbr());
		System.out.println(losers);
		for (int i = 0; i < losers.size() / 2; i++) {
			M.AFtsSet.getAftHash().remove(losers.get(i));
			Manager a = new Manager(losers.get(i), 500.);
			M.AFtsSet.getAftHash().put(losers.get(i), a);
		}
		asosieteRandomAAFTTocells();

	}

	public void asosieteRandomAAFTTocells() {
		CellsSet.getCellsSet().forEach(c -> {
			c.setOwner(M.AFtsSet.getAftHash()
					.get(M.AFtsSet.getAftHash().keySet().toArray()[new Random().nextInt(M.AFtsSet.getAftHash().keySet().size())]));
		});
	}

	void run(int simNBR) {
		int endStat = 70;
		String[][] servicedemand = new String[endStat + 2][CellsSet.getServicesNames().size() * 2];
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			servicedemand[0][i] = "ServiceSupply:" + CellsSet.getServicesNames().get(i);
			servicedemand[0][i + CellsSet.getServicesNames().size()] = "Demand:" + CellsSet.getServicesNames().get(i);
		}
		ModelRunner R = new ModelRunner(M);
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
			CellsSet.getServicesNames().forEach(name -> {
				servicedemand[tick.get() - 2015][m.get()] = R.supply.get(name) + "";//----------------------- 2015??
				servicedemand[tick.get() - 2015][m.get()//----------------------- 2015??
						+ CellsSet.getServicesNames().size()] = CellsSet.getDemand().get(name)[tick.get() - Paths.getStartYear()]
								+ "";
				m.getAndIncrement();
			});
		}

		String dir = PathTools
				.makeDirectory(Paths.getProjectPath() + "\\output\\" + Paths.getScenario() + "\\" + simNBR);
		writOutPut(dir);
		writeProduction(dir);
		CsvTools.writeCSVfile(servicedemand, dir + "\\" + Paths.getScenario() + "-AggregateServiceDemand.csv");
		HashMap<String, Double> nbr = AFTsLoader.hashAgentNbr();
		String[][] NBR = new String[2][nbr.size()];
		NBR[0] = nbr.keySet().toArray(new String[0]);
		NBR[2] = nbr.values().stream().map(String::valueOf).toArray(String[]::new);
		CsvTools.writeCSVfile(NBR, dir + "\\" + Paths.getScenario() + "-NBR.csv");

	}

	void writeProduction(String dir) {
		M.AFtsSet.forEach(( a) -> {
			String[][] tablPruduction = new String[CellsSet.getServicesNames().size() + 1][CellsSet.getCapitalsName().size() + 2];

			tablPruduction[0][0] = "";
			for (int i = 1; i < tablPruduction.length; i++) {
				tablPruduction[i][0] = CellsSet.getServicesNames().get(i - 1);
			}

			for (int i = 1; i < tablPruduction[0].length - 1; i++) {
				tablPruduction[0][i] = CellsSet.getCapitalsName().get(i - 1);
			}
			tablPruduction[0][tablPruduction[0].length - 1] = "Production";
			for (int i = 1; i < tablPruduction.length; i++) {
				tablPruduction[i][tablPruduction[0].length - 1] = a.getProductivityLevel()
						.get(CellsSet.getServicesNames().get(i - 1)) + "";
			}

			for (int i = 1; i < tablPruduction[0].length - 1; i++) {
				for (int j = 1; j < tablPruduction.length; j++) {
					tablPruduction[j][i] = "" + a.getSensitivty()
							.get(CellsSet.getCapitalsName().get(i - 1) + "_" + CellsSet.getServicesNames().get(j - 1));
				}
			}

			CsvTools.writeCSVfile(tablPruduction, dir + "\\" + a.getLabel() + ".csv");

		});
	}

	void writOutPut(String dir) {
		String[][] output = new String[CellsSet.getCellsSet().size() + 1][CellsSet.getServicesNames().size() + 3];
		output[0][0] = "X";
		output[0][1] = "Y";
		output[0][2] = "Agent";
		for (int j = 0; j < CellsSet.getServicesNames().size(); j++) {
			output[0][j + 3] = "Service:" + CellsSet.getServicesNames().get(j);
		}
		AtomicInteger i = new AtomicInteger(1);
		CellsSet.getCellsSet().forEach((c) -> {
			output[i.get()][0] = c.getX() + "";
			output[i.get()][1] = c.getY() + "";
			output[i.get()][2] = c.getOwner() != null ? c.getOwner().getLabel() : "lazy";
			for (int j = 0; j < CellsSet.getServicesNames().size(); j++) {
				output[i.get()][j + 3] = c.getServices().get(CellsSet.getServicesNames().get(j)) + "";
			}
			i.getAndIncrement();
		});

		CsvTools.writeCSVfile(output, dir + "\\" + Paths.getScenario() + "-Cell-" + 2080 + ".csv");
	}
}
