package UtilitiesFx.filesTools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import model.Cell;
import model.CellsSet;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.AddCellToColumnException;

public class ReaderFile {
	private static final Logger LOGGER = LogManager.getLogger(ReaderFile.class);

	public static HashMap<String, ArrayList<String>> ReadAsaHash(String filePath) {
		return ReadAsaHash(filePath, false);

	}

	public static HashMap<String, ArrayList<String>> ReadAsaHash(String filePath, boolean ignoreIfFileNotExists) {
		LOGGER.info("Reading : " + filePath);
		HashMap<String, ArrayList<String>> hash = new HashMap<>();
		Table T = null;
		try {
			T = Table.read().csv(filePath);
		} catch (AddCellToColumnException s) {

			LOGGER.error(s.getMessage());
			/* correctAddCellToColumnException(T, filePath, s); */} catch (Exception e) {
			if (ignoreIfFileNotExists) {
				LOGGER.error(e.getMessage() + " \n     return null");
				return null;
			} else {
				e.printStackTrace();
				LOGGER.error(e.getMessage() + " \n     return null");
				// filePath = WarningWindowes.alterErrorNotFileFound("The file path could not be
				// found:", filePath);
				// T = Table.read().csv(filePath);
			}
		}
		List<String> columnNames = T.columnNames();

		for (Iterator<String> iterator = columnNames.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();

			ArrayList<String> tmp = new ArrayList<String>();
			for (int i = 0; i < T.column(name).size(); i++) {
				tmp.add(T.column(name).getString(i));
			}
			hash.put(name, tmp);
		}

		return hash;
	}

	public static void processCSV(CellsLoader cells, String filePath, String type) {
		LOGGER.info("Importing data for " + type + " from : " + filePath + "...");
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		ConcurrentHashMap<String, Integer> indexof = new ConcurrentHashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String[] line1 = br.readLine().split(",");
			for (int i = 0; i < line1.length; i++) {
				indexof.put(line1[i].replace("Service:", "").replace("\"", "").toUpperCase(), i);
			}

			String line;
			while ((line = br.readLine()) != null) {
				final String data = line;

				executor.submit(() -> {

					switch (type) {
					case "Capitals":
						// System.out.println(type +"-->"+ data);
						associateCapitalsToCells(indexof, data);
						break;
					case "Services":
						associateOutPutServicesToCells(cells, indexof, data);
						break;
					case "Baseline":
						createCells(cells, indexof, data);
						break;

					}
				});
			}
		} catch (IOException e) {

			LOGGER.error(e.getMessage());
		} finally {
			executor.shutdown();
			try {
				// Wait for all tasks to finish
				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
				LOGGER.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
		}
	}



	static void associateCapitalsToCells(ConcurrentHashMap<String, Integer> indexof, String data) {
		List<String> immutableList = Collections.unmodifiableList(Arrays.asList(data.split(",")));
		int x = (int) Tools.sToD(immutableList.get(indexof.get("X")));
		int y = (int) Tools.sToD(immutableList.get(indexof.get("Y")));
		CellsSet.getCapitalsName().forEach(capital_name -> {
			double capital_value = Tools.sToD(immutableList.get(indexof.get(capital_name.toUpperCase())));
			CellsSet.getCellsSet().getCell(x, y).getCapitals().put(capital_name, capital_value);
		});
	}

	static void createCells(CellsLoader cells, ConcurrentHashMap<String, Integer> indexof, String data) {
		List<String> immutableList = Collections.unmodifiableList(Arrays.asList(data.split(",")));
		int x = (int) Tools.sToD(immutableList.get(indexof.get("X")));
		int y = (int) Tools.sToD(immutableList.get(indexof.get("Y")));

		Cell c = new Cell(x, y);

		if (c != null) {
			// if(AFTsLoader.getAftHash().contains(immutableList.get(indexof.get("FR")))){}
			c.setOwner(AFTsLoader.getAftHash().get(immutableList.get(indexof.get("FR"))));

			CellsLoader.hashCell.put(x + "," + y, c);
			c.setIndex(CellsLoader.hashCell.size());
		}
		CellsSet.getCapitalsName().forEach(capital_name -> {
			double capital_value = Tools.sToD(immutableList.get(indexof.get(capital_name.toUpperCase())));
			c.getCapitals().put(capital_name, capital_value);//
		});
	}

	static void associateOutPutServicesToCells(CellsLoader cells, ConcurrentHashMap<String, Integer> indexof,
			String data) {
		List<String> immutableList = Collections.unmodifiableList(Arrays.asList(data.split(",")));
		int x = (int) Tools.sToD(immutableList.get(indexof.get("X")));
		int y = (int) Tools.sToD(immutableList.get(indexof.get("Y")));
		String aft_name = immutableList.get(indexof.get("AGENT"));

		Cell c = CellsLoader.hashCell.get(x + "," + y);

		c.setOwner(AFTsLoader.getAftHash().get(aft_name));
		c.getServices().clear();
		CellsSet.getServicesNames().forEach(service_name -> {
			double service_value = Tools.sToD(immutableList.get(indexof.get(service_name.toUpperCase())));
			
			c.getServices().put(service_name, service_value);
		});
		// System.out.println(c.getServices());

	}

}
