package utils.filesTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import dataLoader.CellsLoader;
import dataLoader.ServiceSet;
import model.Cell;
import utils.analysis.CustomLogger;

public class CsvTools {
	private static final CustomLogger LOGGER = new CustomLogger(CsvTools.class);

	/**
	 * @author Mohamed Byari
	 *
	 */

	public static List<String> csvReaderAsVector(Path filePath) {
		LOGGER.info("Read as a list file:  " + filePath);
		List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				String values = line;
				lines.add(values);
			}
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.error("Unable to read : " + filePath);
			return null;
		}

		return lines;
	}

	public static String[][] csvReader(Path filePath) {
		LOGGER.info("Read as a table file: " + filePath);
		List<String[]> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
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


	public static void cleanCsvFile(Path filePath) {
		List<String> vect = csvReaderAsVector(filePath);
		String[][] vect2 = new String[vect.size()][1];
		for (int i = 0; i < vect.size(); i++) {
			vect2[i][0] = vect.get(i).replace(" ", "").replace("\"", "");
		}

		writeCSVfile(vect2, filePath);
	}

	public static String[] columnFromscsv(int colunNumber, Path path) {
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

	public static void writeCSVfile(String[][] tabl, Path filePath) {
		LOGGER.info("writing CSV file: " + filePath);
		File file = filePath.toFile();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
			BufferedWriter bw = new BufferedWriter(fw);

			for (int i = 0; i < tabl.length; i++) {
				for (int j = 0; j < tabl[0].length - 1; j++) {
					bw.write(tabl[i][j] != null ? tabl[i][j] + "," : ",");
				}
				bw.write(tabl[i][tabl[0].length - 1] != null ? tabl[i][tabl[0].length - 1] : "");
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
		}
	}

	public static void writeNewLineCSVfile(Path filePath, int lineNumber, String... content) {
		String[][] M = csvReader(filePath);
		M[lineNumber] = content;
		writeCSVfile(M, filePath);
	}

	public static void writeValueCSVfile(Path filePath, int lineNumber, int columnNbr, String content) {
		String[][] M = csvReader(filePath);
		M[lineNumber][columnNbr] = content;
		writeCSVfile(M, filePath);
	}

	public static void ModefieOneElementCSVFile(int i, int j, String newValue, Path filePath) {
		String[][] M = csvReader(filePath);
		M[i][j] = newValue;
		writeCSVfile(M, filePath);

	}

	public static void addRefProductionInBehvoirFileFullPath(Path folderPath) {
		List<File> Bfiles = detectFiles(folderPath);
		Bfiles.forEach(file -> {
			if (file.getName().contains(".csv")) {
				String[][] M = csvReader(file.toPath());
				M[1][8] = file.getAbsolutePath().replace("AftParams_", "").replace(
						File.separator + "agents" + File.separator, File.separator + "production" + File.separator);
				writeCSVfile(M, file.toPath());
			}
		});
	}

	public static List<File> detectFiles(Path folderPath) {
		List<File> filePaths = new ArrayList<>();
		File folder = folderPath.toFile();

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
		List<String> serviceImmutableList = Collections.unmodifiableList(ServiceSet.getServicesList());
		// Process the cells in parallel to transform each Cell into a CSV string
		Set<String> csvLines = CellsLoader.hashCell.values().parallelStream().map(c -> {
			String servicesFlattened = flattenHashMap(c, serviceImmutableList);
			return String.join(",", c.getIndex() + "", c.getX() + "", c.getY() + "",
					c.getOwner() != null ? c.getOwner().getLabel() : "null", servicesFlattened);
		}).collect(Collectors.toSet());

		LOGGER.info("Writing processed lines to the CSV file : " + filePath);
		// Write the processed lines to the CSV file
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			writer.write("Index,X,Y,Agent," + String.join(",", serviceImmutableList) + "\n"); // CSV header
			for (String line : csvLines) {
				writer.write(line + "\n");
			}

		} catch (IOException e) {
			LOGGER.error("Unable to export file: " + filePath + "\n" + e.getMessage());
		}
	}

	private static String flattenHashMap(Cell c, List<String> serviceImmutableList) {
		List<String> service = Collections.synchronizedList(new ArrayList<>());
		serviceImmutableList.forEach(ServiceName -> {
//			service.add(c.getServices().get(ServiceName) + "");
			if (c.getCurrentProductivity().get(ServiceName) != null) {
				service.add(c.getCurrentProductivity().get(ServiceName) + "");
			}else {service.add("0");}
//			
		});

		return String.join(",", service);
	}
}
