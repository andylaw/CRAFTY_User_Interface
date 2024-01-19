package UtilitiesFx.filesTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

import UtilitiesFx.graphicalTools.Tools;
import UtilitiesFx.graphicalTools.WarningWindowes;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.AddCellToColumnException;
import tech.tablesaw.io.csv.CsvReadOptions;

public class CsvTools {

	/**
	 * @author Mohamed Byari
	 *
	 */

	public static String[][] csvReader(String filePath) {
		// System.out.println("Read " + filePath);
		String[][] csv = null;
		try {
			int X = 0, Y = 0;
			Scanner scanner = new Scanner(new File(filePath));

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				Y = line.split(",").length;
				X++;
			}
			scanner.close();
			csv = new String[X][Y];
			scanner = new Scanner(new File(filePath));

			int i = 0;
			while (scanner.hasNextLine()) {
				String[] tokens = scanner.nextLine().split(",");
				csv[i] = tokens;
				i++;
			}
			scanner.close();

		} catch (FileNotFoundException e) {
		}
		return csv;
	}

	public static String[] lineFromscsv(int lineNumber, String path) {
		int i = 0;
		Scanner scanner;
		try {
			scanner = new Scanner(new File(path));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().toLowerCase();
				if (i == lineNumber) {
					return line.split(",");
				}
				i++;
			}
			scanner.close();
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	public static HashMap<String, String> lineFromscsvHash(int lineNumber, String path) {
		HashMap<String, String> ret = new HashMap<>();
		String[] names = lineFromscsv(0, path);
		String[] val = lineFromscsv(lineNumber, path);
		for (int i = 0; i < val.length; i++) {
			ret.put(names[i], val[i]);
		}

		return ret;
	}

	private static void correctAddCellToColumnException(Table T,String filePath,AddCellToColumnException e) {
		writeValueCSVfile(filePath, (int) e.getRowNumber(), (int) e.getColumnIndex(), "0");
		try {
			T = Table.read().csv(filePath);
		} catch (AddCellToColumnException s) {
			System.out.println("\n"+s.getLocalizedMessage());
			correctAddCellToColumnException(T,filePath,s);
		}
	}
	
	public static HashMap<String, String[]> ReadAsaHash(String filePath) {
		System.out.print("Read: " + filePath + "...");
		HashMap<String, String[]> hash = new HashMap<>();
		Table T = null;
		try {
			T = Table.read().csv(filePath);
		} catch (AddCellToColumnException s) {
			correctAddCellToColumnException(T,filePath,s);
		} catch (Exception e) {
			filePath = WarningWindowes.alterErrorNotFileFound("The file path could not be found:", filePath);
			T = Table.read().csv(filePath);
		}
			List<String> columnNames = T.columnNames();

			for (Iterator<String> iterator = columnNames.iterator(); iterator.hasNext();) {
				String name = (String) iterator.next();

				String[] tmp = new String[T.column(name).size()];
				for (int i = 0; i < tmp.length; i++) {
					tmp[i] = T.column(name).getString(i);
				}
				hash.put(name, tmp);
			}

		
		System.out.println(" Done");
		return hash;
	}

	public static void tmp() {
		String[][] T = csvReader("C:\\Users\\byari-m\\Downloads\\anpp.csv");
		String[] line0 = CsvTools.lineFromscsv(0, "C:\\Users\\byari-m\\Downloads\\anpp.csv");

		String filePath = "C:\\Users\\byari-m\\Downloads\\anpp2000.csv";
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
			BufferedWriter bw = new BufferedWriter(fw);
			for (int j = 0; j < line0.length; j++) {
				if (j != 2)
					bw.write(line0[j] + ",");
			}
			bw.newLine();
			for (int i = 0; i < T.length; i++) {
				if (T[i][2].equals("2000")) {
					for (int j = 0; j < T[0].length - 1; j++) {
						if (j != 2)
							bw.write(T[i][j] + ",");
					}
					bw.write(T[i][T[0].length - 1]);
					bw.newLine();
				}
			}
			bw.close();

		} catch (IOException e) {
		}
	}

	public static double[][] csvReaderDouble(String filePath) {
		String[][] csvStr = csvReader(filePath);
		double[][] csv = new double[csvStr.length][csvStr[0].length];
		for (int i = 0; i < csvStr.length; i++) {
			for (int j = 0; j < csvStr[0].length; j++) {
				csv[i][j] = Tools.sToD(csvStr[i][j]);
			}
		}
		return csv;
	}

	public static String[] columnFromscsv(int colunNumber, String path) {
		String[][] csvR = csvReader(path);
		return columnFromsMatrix(colunNumber, csvR);
	}

	public static String[] columnFromsMatrix(int colunNumber, String[][] csvR) {

		String[] vect = new String[csvR.length];
		if (colunNumber == -1) {
			colunNumber = csvR[0].length - 1;
		}
		for (int i = 0; i < csvR.length; i++) {
			vect[i] = csvR[i][colunNumber];
		}
		return vect;
	}

	public static HashMap<String, Integer> columnindexof(String[] list) {
		HashMap<String, Integer> temp = new HashMap<>();
		for (int i = 0; i < list.length; i++) {
			temp.put(list[i], i);
		}
		return temp;
	}

	public static void writeCSVfile(String[][] tabl, String filePath) {
		System.out.println("write: " + filePath);
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < tabl.length; i++) {
				for (int j = 0; j < tabl[0].length - 1; j++) {
					bw.write(tabl[i][j] + ",");
				}
				bw.write(tabl[i][tabl[0].length - 1] != null ? tabl[i][tabl[0].length - 1] : 0 + "");
				bw.newLine();
			}
			bw.close();

		} catch (IOException e) {
		}
	}

//	public static void writeCSVfileFromHash(HashMap<String, String[]> hash, String filePath) {
//		File file = new File(filePath);
//		try {
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//			FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
//			BufferedWriter bw = new BufferedWriter(fw);
//			String [][] tmp= new String [hash.size()][hash.values().iterator().next().length+1];
//
//			hash.forEach((k, v) -> {
//				try {
//					for (int j = 0; j < v.length; j++) {
//						bw.write(v[j] + ",");
//					}
//					bw.newLine();
//				} catch (IOException e) {
//				}
//			});
//
//			bw.close();
//
//		} catch (IOException e) {
//		}
//	}

	public static void writeNewLineCSVfile(String filePath, int lineNumber, String... content) {
		String[][] M = csvReader(filePath);
		M[lineNumber] = content;
		writeCSVfile(M, filePath);
	}

	public static void writeValueCSVfile(String filePath, int lineNumber, int columnNbr, String content) {
		String[][] M = csvReader(filePath);
		M[lineNumber][columnNbr] = content;
		writeCSVfile(M, filePath);
	}

	public static void ModefieOneElementCSVFile(int i, int j, String newValue, String filePath) {
		String[][] M = csvReader(filePath);
		M[i][j] = newValue;
		writeCSVfile(M, filePath);

	}

	public static void addRefProductionInBehvoirFileFullPath(String folderPath) {
		List<File> Bfiles = detectFiles(folderPath);
		Bfiles.forEach(file -> {
			if (file.getName().contains(".csv")) {
				String[][] M = csvReader(file.getAbsolutePath());
				M[1][8] = file.getAbsolutePath().replace("AftParams_", "").replace("\\agents\\", "\\production\\");
				writeCSVfile(M, file.getAbsolutePath());
			}
		});
	}

	public static List<File> detectFiles(String folderPath) {
		List<File> filePaths = new ArrayList<>();
		File folder = new File(folderPath);

		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("Input path is not a directory.");
		}

		File[] files = folder.listFiles();

		if (files == null) {
			throw new RuntimeException("Error occurred while retrieving files.");
		}

		for (File file : files) {
			if (file.isFile()) {
				filePaths.add(file);
			}
		}

		return filePaths;
	}

}
