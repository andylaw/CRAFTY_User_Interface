package UtilitiesFx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import Main.Main_CraftyFx;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;



public class ReadFile {
	static File file = new File(Path.projectPath+"\\DefaultSENARIO.xml");

	public static void CopyPast(String filePath) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), false);
		try (BufferedWriter bw = new BufferedWriter(fw)) {
			Scanner scanner = new Scanner(new File(filePath));
			while (scanner.hasNextLine()) {
				bw.write(scanner.nextLine());
				bw.newLine();
			}
			bw.close();
			scanner.close();
		} catch (FileNotFoundException e) {
		}
	}

	public void WrightLine(String str) throws IOException {
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		try (BufferedWriter bw = new BufferedWriter(fw)) {
			bw.write(str);
			bw.newLine();
			bw.close();
		}
	}

	@SuppressWarnings("resource")
	public void cleanfile() throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		new FileWriter(file.getAbsoluteFile(), false);
	}



	public static ArrayList<String> findFolder(final File folder,boolean onlyFolder) {
		return findFolder( folder, "",onlyFolder);
	}

	public static ArrayList<String> findFolder(final File folder, String condition,boolean onlyFolder) {
		ArrayList<String> stringList =new ArrayList<>();
		for (final File fileEntry : folder.listFiles()) {
			if(onlyFolder) {
				if (fileEntry.getName().contains(condition)&& !fileEntry.getName().contains(".")) {
					stringList.add(fileEntry.getName());
				}
			}
			else {
			if (fileEntry.getName().contains(condition)) {
				stringList.add(fileEntry.getName().replace(".csv", ""));
			}}
		}
		return stringList;

	}
	
	
	static void creatListPath(final File folder, ArrayList<String> Listpathe) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				creatListPath(fileEntry, Listpathe);
			} else {
					Listpathe.add(fileEntry.getPath());
			}
		}
	}
	
	static ArrayList<String>  findAllFile(String path) throws IOException {
		ArrayList<String> Listpathe = new ArrayList<>();
		final File folder = new File(path);
		creatListPath(folder,Listpathe);
		return Listpathe;
	}


	public String redOneLine(String filePath, int nbr) {
		Scanner scanner;
		String line = "";
		try {
			scanner = new Scanner(new File(filePath));
			int i = 0;
			while (scanner.hasNextLine() && i != nbr) {

				line = scanner.nextLine();
				i++;
			}
		} catch (FileNotFoundException e) {
		}
		return line;
	}

	public List<String> findProductionList(String path) {
		List<String> list = new ArrayList<>();
		try {
			Scanner scanner = new Scanner(new File(path));

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("curve service=")) {
					list.add(line.replace("<curve service=\"", "").replace("\">", "").replace("	", ""));
				}
			}
		} catch (FileNotFoundException e) {
		}

		return list;
	}

	public HashMap<String, String> findEquations(String path) {
		HashMap<String, String> equation = new HashMap<>();
		 List<String> curveList = findProductionList(path);
		try {
			Scanner scanner = new Scanner(new File(path));
			String str = "<curve class=\"com.moseph.modelutils.curve";
			int n = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains(str) && !line.contains("<!--")) {
					String[] var = line.split(" ");
					String temp = "";
					for (int i = 0; i < var.length; i++) {
						if (!var[i].contains("class=") && var[i].contains("=")) {
							temp = temp + "" + var[i];
						}
						equation.put(curveList.get(n), temp);
					}
					n++;
				}
			}
		} catch (FileNotFoundException e) {
		}
		return equation;
	}

	public static File selecFolder(String projectPath) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select Project");
		File initialDirectory = new File(projectPath);
		chooser.setInitialDirectory(initialDirectory);
		File selectedDirectory = chooser.showDialog(Main_CraftyFx.primaryStage);
		return selectedDirectory;
	}

	public static File selecFile(String projectPath) {
		FileChooser  chooser = new FileChooser ();
		chooser.setTitle("Select Project");
		File initialDirectory = new File(projectPath);
		chooser.setInitialDirectory(initialDirectory);
		File selectedDirectory = chooser.showOpenDialog(Main_CraftyFx.primaryStage);
		return selectedDirectory;
	}


}

