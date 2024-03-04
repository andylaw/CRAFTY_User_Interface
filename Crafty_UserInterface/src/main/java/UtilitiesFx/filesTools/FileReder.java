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
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.graphicalTools.Tools;
import dataLoader.CellsLoader;
import model.Cell;
import model.CellsSet;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.AddCellToColumnException;

public class FileReder {

	private static void correctAddCellToColumnException(Table T, String filePath, AddCellToColumnException e) {
		// writeValueCSVfile(filePath, (int) e.getRowNumber(), (int) e.getColumnIndex(),
		// "0");
		try {
			T = Table.read().csv(filePath);
		} catch (AddCellToColumnException s) {
			System.out.println("\n" + s.getLocalizedMessage());
			correctAddCellToColumnException(T, filePath, s);
		}
	}

	public static HashMap<String, ArrayList<String>> ReadAsaHash(String filePath) {
		return ReadAsaHash(filePath, false);

	}

	public static HashMap<String, ArrayList<String>> ReadAsaHash(String filePath, boolean ignoreIfFileNotExists) {
		System.out.print("Read: " + filePath + "...");
		HashMap<String, ArrayList<String>> hash = new HashMap<>();
		Table T = null;
		try {
			T = Table.read().csv(filePath);
		} catch (AddCellToColumnException s) {
			System.err.println(s.getMessage());
			/* correctAddCellToColumnException(T, filePath, s); */}
//		 catch (Exception e) {
//			if (ignoreIfFileNotExists) {
//				return null;
//			} else {
//				filePath = WarningWindowes.alterErrorNotFileFound("The file path could not be found:", filePath);
//				T = Table.read().csv(filePath);
//			}
//		}
		List<String> columnNames = T.columnNames();

		for (Iterator<String> iterator = columnNames.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();

			ArrayList<String> tmp = new ArrayList<String>();
			for (int i = 0; i < T.column(name).size(); i++) {
				tmp.add(T.column(name).getString(i));
			}
			hash.put(name, tmp);
		}

		System.out.println(" Done");
		return hash;
	}

	public static void processCSV(CellsLoader cells,String filePath) {// file has capitals services and AFTs ? defind in the beginers the
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		ConcurrentHashMap<String, Integer> indexof = new ConcurrentHashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String[] line1 = br.readLine().split(",");
			for (int i = 0; i < line1.length; i++) {
				indexof.put(line1[i], i);
			}

			String line;
			while ((line = br.readLine()) != null) {
				final String data = line;

				executor.submit(() -> {
					if(cells==null) {
						associateCapitalsToCells(indexof, data);}
					else {
						createCells( cells, indexof,  data);
					}
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			executor.shutdown();
			try {
				// Wait for all tasks to finish
				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
			} catch (InterruptedException e) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}

	static void associateCapitalsToCells(ConcurrentHashMap<String, Integer> indexof, String data) {
		List<String> immutableList = Collections.unmodifiableList(Arrays.asList(data.split(",")));
		int x = (int) Tools.sToD(immutableList.get(indexof.get("X")));
		int y = (int) Tools.sToD(immutableList.get(indexof.get("Y")));

		CellsSet.getCapitalsName().forEach(capital_name -> {
			double capital_value = Tools.sToD(immutableList.get(indexof.get(capital_name)));
			CellsSet.getCellsSet().getCell(x, y).getCapitals().put(capital_name, capital_value);
		});
	}

	static void createCells(CellsLoader cells, ConcurrentHashMap<String, Integer> indexof, String data) {
		List<String> immutableList = Collections.unmodifiableList(Arrays.asList(data.split(",")));
		int x = (int) Tools.sToD(immutableList.get(indexof.get("X")));
		int y = (int) Tools.sToD(immutableList.get(indexof.get("Y")));

		Cell c = new Cell(x, y);

		if (c != null) {
			c.setOwner(cells.AFtsSet.getAftHash().get(immutableList.get(indexof.get("FR"))));
		
			CellsLoader.hashCell.put(x + "," + y, c);
			c.setIndex(CellsLoader.hashCell.size());
		}
		CellsSet.getCapitalsName().forEach(capital_name -> {
			double capital_value = Tools.sToD(immutableList.get(indexof.get(capital_name)));
			c.getCapitals().put(capital_name, capital_value);//
		});
	}

}
