package WorldPack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import UtilitiesFx.CsvTools;
import UtilitiesFx.Path;
import UtilitiesFx.Tools;

public class Rules {

	Lattice L = new Lattice();
	public String colorDisplay = "FR";
	public boolean mapSynchronisation = false;
	public boolean removeNegative = false;
	public boolean usegiveUp = false;
	public HashMap<String, Double> demand = new HashMap<>();
	public HashMap<String, Double> supply;
	static HashMap<String, Double> marginal = new HashMap<>();
	public static HashMap<String, Double> distributionMean;

	public Rules(Lattice L) {
		this.L = L;
		demandUpdate();
	}

	public void demandUpdate() {
		HashMap<String, String[]> data = CsvTools.ReadAsaHash(Path.fileFilter(Path.scenario, "demands").get(0));
		data.forEach((name, vect) -> {
			if (!name.equals("year"))
				for (int i = 1; i < vect.length; i++) {
					demand.put((name + "_" + data.get("year")[i]).replace(" ", ""), Tools.sToD(vect[i]));
				}
		});
	}

	void calculeSystemSupply() {
		supply = new HashMap<>();

		Lattice.P.forEach(c -> {
			if (c.owner != null) {
				Lattice.servicesNames.forEach(s -> {
					double sup = c.prodactivity(c.owner, s);
					if (supply.containsKey(s)) {
						supply.put(s, supply.get(s) + sup);
					} else {
						supply.put(s, sup);
					}
				});
			}
		});
		distributionMean = new HashMap<>();
		if (usegiveUp) {
			HashMap<String, Integer> AFTnbr = new HashMap<>();
			Lattice.P.forEach(c -> {
				if (c.owner != null) {
					Lattice.servicesNames.forEach(s -> {
						double sup = c.prodactivity(c.owner, s);
						if (distributionMean.containsKey(c.owner.label)) {
							distributionMean.put(c.owner.label, distributionMean.get(c.owner.label) + sup);
						} else {
							distributionMean.put(c.owner.label, sup);
						}
					});
					if (AFTnbr.containsKey(c.owner.label))
						AFTnbr.put(c.owner.label, AFTnbr.get(c.owner.label) + 1);
					else
						AFTnbr.put(c.owner.label, 1);
				}
			});

			distributionMean.forEach((aftName, total) -> {
				distributionMean.put(aftName, total / AFTnbr.get(aftName));
			});

		}

	}

	void calculeMarginalUtility(int year, boolean removeNegative) {
		supply.forEach((name, val) -> {
			double marg = removeNegative ? Math.max(demand.get(name + "_" + year) - val, 0)
					: demand.get(name + "_" + year) - val;
			marginal.put(name, marg);
		});
	}

	public void go(int year) {
		try {
			L.updateCapitals(year);
		} catch (IOException e) {
		}
		// calcule supply
		calculeSystemSupply();
		// update demande & calcule marginal
		calculeMarginalUtility(year, removeNegative);
		Lattice.P.forEach(c -> {
			c.putservices();
			// Land abandonment does not provide minimum ecosystem services utility.
			if (c.owner != null) {
				if (usegiveUp) {
					double sum = 0;
					for (int i = 0; i < c.services.values().toArray().length; i++) {
						sum += (double) c.services.values().toArray()[i];
					}
					if (sum < distributionMean.get(c.owner.label) * (c.owner.giveUp / 100)
							&& c.owner.giveUpProbabilty > Math.random()) {
						c.owner = null;
					}
				}
			}
			// Randomly select x% of the land available to compete on, and set the
			// competition
			if (Math.random() < 0.1) {
				AFT agent = (AFT) Agents.aftReSet.values().toArray()[new Random().nextInt(Agents.aftReSet.size())];
				c.Competition(agent);
			}
		});
		// display Map
		if (mapSynchronisation) {
			L.colorMap(colorDisplay);
		}
		// creat .csv output files: servises and AFT for each land
		// writOutPut(year);
	}

	public void foreach(Consumer<Cell> c) {
		int threadNBR = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(threadNBR);
		int chunkSize = (Lattice.P.size() / threadNBR);

		for (int i = 0; i < threadNBR; i++) {
			int start = (i * chunkSize);
			int end = i != threadNBR - 1 ? (i + 1) * chunkSize : Lattice.P.size();
			executorService.submit(() -> {
				for (int I = start; I < end; I++) {
					c.accept(Lattice.P.get(I));
				}
			});
		}

		try {
			executorService.shutdown();
			executorService.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	void threadingSupply() {

		int threadNBR = 10;
		List<HashMap<String, Double>> list = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(threadNBR);
		int chunkSize = (Lattice.P.size() / threadNBR);

		for (int i = 0; i < threadNBR; i++) {
			HashMap<String, Double> supply = new HashMap<>();
			int start = (i * chunkSize);
			int end = i != threadNBR - 1 ? (i + 1) * chunkSize : Lattice.P.size();
			executorService.submit(() -> {

				for (int I = start; I < end; I++) {
					int K = I;
					Lattice.servicesNames.forEach(s -> {
						if (supply.containsKey(s))
							supply.put(s, supply.get(s) + Lattice.P.get(K).prodactivity(Lattice.P.get(K).owner, s));
						else {
							supply.put(s, 0.);
						}
					});
				}
			});
			list.add(supply);
		}

		try {
			executorService.shutdown();
			executorService.awaitTermination(100, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// HashMap<String, Double>supply= new HashMap<>();
		list.forEach(h -> {
			h.forEach((serviceName, value) -> {
				if (supply.keySet().contains(serviceName))
					supply.put(serviceName, supply.get(serviceName) + value);
				else {
					supply.put(serviceName, 0.);
				}
			});
		});
		// System.out.println("supply2= " + supply);
	}

	void writOutPut(int year) {
		String[][] output = new String[L.hashCell.size() + 1][Lattice.servicesNames.size() + 3];
		output[0][0] = "x";
		output[0][1] = "y";
		output[0][2] = "agent";
		for (int j = 0; j < Lattice.servicesNames.size(); j++) {
			output[0][j + 3] = "Service:" + Lattice.servicesNames.get(j);
		}
		AtomicInteger i = new AtomicInteger(1);
		L.hashCell.forEach((coor, c) -> {
			output[i.get()][0] = c.x + "";
			output[i.get()][1] = c.y + "";
			output[i.get()][2] = c.owner != null ? c.owner.label : "lazy";
			for (int j = 0; j < Lattice.servicesNames.size(); j++) {
				output[i.get()][j + 3] = c.services.get(Lattice.servicesNames.get(j)) + "";
			}
			i.getAndIncrement();
		});
		String dir = CsvTools.makeDirectory(Path.projectPath + "\\output\\");
		dir = CsvTools.makeDirectory(Path.projectPath + "\\output\\" + Path.getSenario());

		CsvTools.writeCSVfile(output, dir + "\\" + Path.scenario + "-Cell-" + year + ".csv");
	}

}
