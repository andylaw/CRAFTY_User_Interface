package UtilitiesFx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CsvTools {

	public static String[][] csvReader(String filePath) {
		System.out.println("Read " + filePath);
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

	public static HashMap<String, String[]> ReadAsaHash(String filePath) {
		String[][] M = csvReader(filePath);
		HashMap<String, String[]> hash = new HashMap<>();
		String[] line0 = lineFromscsv(0, filePath);
		for (int i = 0; i < line0.length; i++) {
			hash.put(line0[i].toLowerCase().replace("\"", ""), columnFromsMatrix(i, M));
		}
		return hash;
	}

	public static String[][] multLineFromscsv(String path, int... linesNumber) {

		List<Integer> numberList = Arrays.stream(linesNumber).boxed().collect(Collectors.toList());
		Collections.sort(numberList);

		int i = 0;
		int k = 0;
		String[][] newM = new String[linesNumber.length][];
		Scanner scanner;
		try {
			scanner = new Scanner(new File(path));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (k < numberList.size() && i == numberList.get(k)) {
					newM[k] = new String[line.length()];
					newM[k] = line.split(",");
					k++;
				}
				i++;
			}
			scanner.close();
		} catch (FileNotFoundException e) {
		}
		return newM;
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
				bw.write(tabl[i][tabl[0].length - 1]);
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

	public static List<File> detectFolders(String folderPath) {
		List<File> filePaths = new ArrayList<>();
		File folder = new File(folderPath);

		// Check if the folder exists and is a directory
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();

			// Iterate over the files in the folder
			for (File file : files) {
				// Check if it is a directory
				if (file.isDirectory()) {
					filePaths.add(file);
				}
			}
		} else {
			System.out.println("Folder not found: " + folderPath);
		}
		return filePaths;
	}

	public static String makeDirectory(String dir) {
		File directory = new File(dir);
		if (!directory.exists()) {
			directory.mkdir();
		}
		return dir;
	}
}
