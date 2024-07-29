package UtilitiesFx.filesTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dataLoader.CellsLoader;
import model.Cell;
import model.CellsSet;

public class CsvTools {
	private static final Logger LOGGER = LogManager.getLogger(CsvTools.class);
	/**
	 * @author Mohamed Byari
	 *
	 */

	public static List<String> csvReaderAsVector(String filePath) {
		LOGGER.info("Read as a list file:  " + filePath );
		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				String values = line;
				lines.add(values);
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("Unable to read : " + filePath );
			return null;
		}

		return lines;
	}

	public static String[][] csvReader(String filePath) {
		LOGGER.info("Read as a table file: " + filePath );
		List<String[]> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(","); // Assumes CSV uses comma as delimiter
				lines.add(values);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Convert List<String[]> to String[][]
		String[][] array = new String[lines.size()][];
		for (int i = 0; i < lines.size(); i++) {
			array[i] = lines.get(i);
		}
		return array;
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

//    public static HashMap<String, String[]> ReadAsaHash(String filePath) {//csvReaderAsHash
//    	System.out.print("Read: " + filePath + "...");
//        HashMap<String, List<String>> tempMap = new HashMap<>();
//        String[] headers = null;
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            if ((line = br.readLine()) != null) {
//                // Process headers
//                headers = line.split(",");
//                for (String header : headers) {
//                    tempMap.put(header, new ArrayList<>());
//                }
//            }
//            while ((line = br.readLine()) != null) {
//                String[] values = line.split(",");
//                for (int i = 0; i < values.length; i++) {
//                    tempMap.get(headers[i]).add(values[i]);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // Convert lists to arrays
//        HashMap<String, String[]> resultMap = new HashMap<>();
//        for (String header : tempMap.keySet()) {
//            List<String> valuesList = tempMap.get(header);
//            String[] valuesArray = new String[valuesList.size()];
//            valuesArray = valuesList.toArray(valuesArray);
//            resultMap.put(header, valuesArray);
//        }
//        System.out.println(" Done");
//        return resultMap;
//    }

	public static void cleanCsvFile(String filePath) {
		List<String> vect = csvReaderAsVector(filePath);
		String[][] vect2 = new String[vect.size()][1];
		for (int i = 0; i < vect.size(); i++) {
			vect2[i][0] = vect.get(i).replace(" ", "").replace("\"", "");
		}

		writeCSVfile(vect2, filePath);
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
		LOGGER.info("writing CSV file: " + filePath);
		
		File file = new File(filePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < tabl.length; i++) {
				for (int j = 0; j < tabl[0].length - 1; j++) {
					bw.write(tabl[i][j]!=null?tabl[i][j] + ",":",");
				}
				bw.write(tabl[i][tabl[0].length - 1] != null ? tabl[i][tabl[0].length - 1] :   "");
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
		}
	LOGGER.info("Writing CSV file...  Done");
	}


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

	public static void exportToCSV(String filePath) {
		LOGGER.info("Processing data to write a csv file...");

		List<String> serviceImmutableList = Collections.unmodifiableList(CellsSet.getServicesNames());
		// Process the cells in parallel to transform each Cell into a CSV string
		Set<String> csvLines = CellsLoader.hashCell.values().parallelStream().map(c -> {
			String capitalsFlattened = flattenHashMap(c, serviceImmutableList);
			return String.join(",", c.getIndex() + "", c.getX() + "", c.getY() + "", c.getOwner() != null ? c.getOwner().getLabel() : "null",capitalsFlattened);
		}).collect(Collectors.toSet());
		
		LOGGER.info("Writing processed lines to the CSV file : "+filePath);
		// Write the processed lines to the CSV file
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write("Index,X,Y,Agent," + String.join(",", serviceImmutableList) + "\n"); // CSV header
			for (String line : csvLines) {
				writer.write(line+"\n");
			}

		} catch (IOException e) {
			LOGGER.error("Unable to export file: " + filePath+"\n"+ e.getMessage());
		}
	}





	private static String flattenHashMap(Cell c, List<String> serviceImmutableList) {
		List<String> service = Collections.synchronizedList(new ArrayList<>());
		serviceImmutableList.forEach(ServiceName -> {
			service.add(c.getServices().get(ServiceName) + "");
		});
		return String.join(",", service);
	}
}
