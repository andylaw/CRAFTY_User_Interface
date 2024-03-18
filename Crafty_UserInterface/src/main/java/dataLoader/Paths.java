package dataLoader;

import java.util.List;

import UtilitiesFx.filesTools.FileReder;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Mohamed Byari
 *
 */

public final class Paths {
	private static String[] foldersNecessary = { "agents", "csv", "production", "worlds", "GIS" };
	private static int startYear;
	private static int endtYear;
	private static int currentYear = startYear;
	private static String projectPath = "";
	private static ArrayList<String> scenariosList = new ArrayList<>();
	private static HashMap<String, String> scenariosHash = new HashMap<>();
	private static ArrayList<String> allfilesPathInData;
	private static String scenario;

	public static void initialisation(String str) {
		projectPath = str;
		allfilesPathInData = PathTools.findAllFiles(projectPath);
		initialSenarios();
	}

	static void initialSenarios() {
		HashMap<String, ArrayList<String>> hash = FileReder
				.ReadAsaHash(PathTools.fileFilter("\\scenarios.csv").iterator().next());
		setScenariosList(hash.get("Name"));
		for (String scenario : scenariosList) {
			scenariosHash.put(scenario, hash.get("startYear").get(hash.get("Name").indexOf(scenario)) + "_"
					+ hash.get("endtYear").get(hash.get("Name").indexOf(scenario)));
		}
		setScenario(getScenariosList().get(getScenariosList().size() - 1));
		

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

	public static void setScenariosList(ArrayList<String> list) {
		scenariosList = new ArrayList<>(list);
	}

	public static void setAllfilesPathInData(ArrayList<String> allfilesPathInData) {
		Paths.allfilesPathInData = allfilesPathInData;
	}

	public static String getScenario() {
		return scenario;
	}

	public static void setScenario(String scenario) {
		Paths.scenario = scenario;
		String[] temp = scenariosHash.get(scenario).split("_");
		startYear = (int) Tools.sToD(temp[0]);
		endtYear = (int) Tools.sToD(temp[1]);
		//System.out.println(scenario+"--> startYear= "+ startYear+", endtYear "+ endtYear);
	}

}
