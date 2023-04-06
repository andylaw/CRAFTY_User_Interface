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
import java.util.Scanner;
import java.util.stream.Collectors;

public class CsvTools {

	public static String[][] csvReader(String filePath) {
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
				String line = scanner.nextLine();
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
	
	public static String[][] multLineFromscsv(String path, int ...linesNumber ) {
		
		List<Integer> numberList = Arrays.stream(linesNumber).boxed().collect(Collectors.toList()); 
		Collections.sort(numberList);
       

		int i = 0;
		int k = 0;
		String [][] newM = new String[linesNumber.length][];
		Scanner scanner;
		try {
			scanner = new Scanner(new File(path));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				if (k<numberList.size()&&i == numberList.get(k)) {
					newM[k]= new String[line.length()];
					newM[k]=line.split(",");
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
		return columnFromsMatrix( colunNumber, csvR);
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

	public static void writeNewLineCSVfile(String filePath,int lineNumber, String ... content ) {
		String[][] M = csvReader(filePath);
		M[lineNumber] = content;
		writeCSVfile(M, filePath);
	}
	
	public static void ModefieOneElementCSVFile(int i, int j, String newValue, String filePath) {
		String[][] M = csvReader(filePath);
		M[i][j] = newValue;
		writeCSVfile(M, filePath);

	}

}
