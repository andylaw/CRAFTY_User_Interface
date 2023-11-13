package dataLoader;

import java.util.List;

import UtilitiesFx.filesTools.PathTools;
import java.io.File;
import java.util.ArrayList;

public final class Paths {
	private static String[] foldersNecessary = { "agents", "csv", "production", "worlds", "GIS" };
	private static int startYear = 2020;
	private static int endtYear = 2080;
	private static int currentYear = startYear;
	private static String projectPath = "";
	private static ArrayList<String> scenariosList = new ArrayList<>();
	private static ArrayList<String> allfilesPathInData;
	private static String scenario;

	public static void initialisation(String str) {
		projectPath = str;
		scenariosList = PathTools.findFolder(new File(projectPath + "\\production"),"", true);
		allfilesPathInData = PathTools.findAllFiles(projectPath);
		scenario = scenariosList.get(scenariosList.size() - 1);
	}



	static public List<String> checkfolders(String path) {
		List<String> listOfFilesMissing = new ArrayList<>();
		List<File> folders = PathTools.detectFolders(path);
		List<String> foldersname = new ArrayList<>();
		folders.forEach(e -> {
			foldersname.add(e.getName());
		});
		for (int i = 0; i < foldersNecessary.length; i++) {
			if (!foldersname.contains(foldersNecessary[i])) {
				listOfFilesMissing.add(folders.get(0).getParent() + "\\" + foldersNecessary[i]);
			}
		}

		return listOfFilesMissing;
	}

	public static ArrayList<String> getAllfilesPathInData() {
		return allfilesPathInData;
	}

	public static int getStartYear() {
		return startYear;
	}

	public static void setStartYear(int startYear) {
		Paths.startYear = startYear;
	}

	public static int getEndtYear() {
		return endtYear;
	}

	public static void setEndtYear(int endtYear) {
		Paths.endtYear = endtYear;
	}

	public static int getCurrentYear() {
		return currentYear;
	}

	public static void setCurrentYear(int currentYear) {
		Paths.currentYear = currentYear;
	}

	public static String getProjectPath() {
		return projectPath;
	}

	public static ArrayList<String> getScenariosList() {
		return scenariosList;
	}

	public static void setAllfilesPathInData(ArrayList<String> allfilesPathInData) {
		Paths.allfilesPathInData = allfilesPathInData;
	}

	public static String getScenario() {
		return scenario;
	}

	public static void setScenario(String scenario) {
		Paths.scenario = scenario;
	}

}
