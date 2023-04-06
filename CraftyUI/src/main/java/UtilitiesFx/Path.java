package UtilitiesFx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public final class Path {
	public static int startYear=2020;
	public static int endtYear=2100;

	public static String projectPath = "C:\\Users\\byari-m\\Documents\\Data\\data_UK";// "C:\\Users\\byari-m\\Documents\\Data\\data_EUsmall";//
	public static ArrayList<String> senariosList = ReadFile.findFolder(new File(projectPath + "\\production"),
			true);
	public static ArrayList<String> worldNameList = ReadFile.findFolder(new File(projectPath + "\\worlds"),
			true);
	public static ArrayList<String> servicesList=new  ArrayList<>();

	
	public static ArrayList<String> AllfilePatheInData ;
	public static String senario = senariosList.get(senariosList.size()-1);

	public static String production = projectPath + "\\production";
	public static String version = projectPath + "\\agents";

	public static ArrayList<String> pathWithconditions() {
		ArrayList<String> capitalsList = new ArrayList<>();
		try {
			capitalsList = ReadFile.findAllFile(projectPath);
		} catch (IOException e) { 
		}
		return capitalsList;
	}

	public static ArrayList<String> nameOfFile(String... condition) {
		ArrayList<String> turn = new ArrayList<>();
		ArrayList<String> tmp = fileFilter(condition);
		tmp.forEach(e -> {
			turn.add(new File(e).getName().replace(".", " ").split(" ")[0]);
		});
		return turn;
	}

	public static ArrayList<String> fileFilter(String... condition) {

		ArrayList<String> turn = new ArrayList<>();

		AllfilePatheInData.forEach(e -> {
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

		return turn;
	}

	public static String getProjectPath() {
		return projectPath;
	}

	public static void setProjectPath(String projectPath) {
		Path.projectPath = projectPath;
	}




	public static String getProduction() {
		return production;
	}

	public static void setProduction(String production) {
		Path.production = production;
	}

	public static String getversion() {
		return version;
	}

	public static void setversion(String version) {
		Path.version = version;
	}

	public static String getSenario() {
		return senario;
	}

	public static void setSenario(String senario) {
		Path.senario = senario;
	}

}
