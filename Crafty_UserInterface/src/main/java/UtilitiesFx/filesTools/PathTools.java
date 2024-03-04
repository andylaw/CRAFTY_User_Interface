package UtilitiesFx.filesTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import UtilitiesFx.graphicalTools.WarningWindowes;
import dataLoader.Paths;
import main.FxMain;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * @author Mohamed Byari
 *
 */

public class PathTools {

	public static ArrayList<String> findFolder(final File folder, String condition, boolean onlyFolder) {
		ArrayList<String> stringList = new ArrayList<>();
		for (final File fileEntry : folder.listFiles()) {
			if (onlyFolder) {
				if (fileEntry.getName().contains(condition) && !fileEntry.getName().contains(".")) {
					stringList.add(fileEntry.getName());
				}
			} else {
				if (fileEntry.getName().contains(condition)) {
					stringList.add(fileEntry.getName().replace(".csv", ""));
				}
			}
		}
		return stringList;

	}

	static void creatListPaths(final File folder, ArrayList<String> Listpathe) {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				creatListPaths(fileEntry, Listpathe);
			} else {
				Listpathe.add(fileEntry.getPath());
			}
		}
	}
	public static ArrayList<String> fileFilter( String... condition){
		return fileFilter(false,condition);
	}
	public static ArrayList<String> fileFilter( boolean ignoreIfFileNotExists,String... condition) {

		ArrayList<String> turn = new ArrayList<>();

		Paths.getAllfilesPathInData().forEach(e -> {
			boolean testCodition = true;
			for (int j = 0; j < condition.length; j++) {
				if (!e.contains(condition[j])) {
					testCodition = false;
					break;
				}
			}
			if (testCodition)
				turn.add(e);
		});
		String str = "";
		for (int j = 0; j < condition.length; j++) {
			str = condition[j] + " " + str;
		}

		if (turn.size() == 0) {
			if(ignoreIfFileNotExists) {return null;}
			return null;
//			return fileFilter(ignoreIfFileNotExists,WarningWindowes.alterErrorNotFileFound("The file path could not be found:",str));
		} else {
			return turn;
		}
	}
	
	public static ArrayList<String> findAllFiles(String path)  {
		ArrayList<String> Listpathe = new ArrayList<>();
		final File folder = new File(path);
		creatListPaths(folder, Listpathe);
		return Listpathe;
	}

	public static String read(String filePath) {
		Scanner scanner;
		String line = "";
		try {
			scanner = new Scanner(new File(filePath));

			while (scanner.hasNextLine()) {
				line =line+"\n"+ scanner.nextLine();
			}
		} catch (FileNotFoundException e) {
		}
		return line;
	}

	public static File selectFolder(String projectPath) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select Project");
		File initialDirectory = new File(projectPath);
		chooser.setInitialDirectory(initialDirectory);
		File selectedDirectory = chooser.showDialog(FxMain.primaryStage);
		return selectedDirectory;
	}

	public static File selecFile(String projectPath) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select Project");
		File initialDirectory = new File(projectPath);
		chooser.setInitialDirectory(initialDirectory);
		File selectedDirectory = chooser.showOpenDialog(FxMain.primaryStage);
		return selectedDirectory;
	}

	static public void writeFile(String path, String text, boolean keepTxt) {
		File file = new File(path);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, keepTxt))) {
			writer.write(text);
		} catch (IOException ex) {
			System.err.println("Error writing to file: " + ex.getMessage());
		}
	}
	static public void writePathRecentProject(String path, String text) {
		String paths = PathTools.read(path);
		if(!paths.contains(text)) {
		File file = new File(path);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
			writer.write(text);
		} catch (IOException ex) {
			System.err.println("Error writing to file: " + ex.getMessage());
		}
		}
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
