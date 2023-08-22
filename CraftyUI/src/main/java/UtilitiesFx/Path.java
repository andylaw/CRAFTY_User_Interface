package UtilitiesFx;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public final class Path {
	
	public static String[] minFoldersNaisserly= {"agents","csv","production","worlds","GIS","institutions","lara","xml"}; 
	
	public static int startYear=2020;
	public  static int endtYear=2080;
	
	
	public static String projectPath ="";//"C:\\Users\\byari-m\\Documents\\Data\\data_EUpaper_nocsv";//"C:\\Users\\byari-m\\Documents\\Data\\data_UK";//  
	public static ArrayList<String> scenariosList = new  ArrayList<>();
	public static ArrayList<String> worldNameList = new  ArrayList<>();
	public static String referqnceWorld ;
	
	public static ArrayList<String> AllfilePatheInData;
	public static String scenario ;

	public static String production ;
	public static String version;
	
	public static void initialisation(String str) {
		Path.projectPath = str;
		System.out.println(projectPath + "\\production");
		scenariosList = ReadFile.findFolder(new File(projectPath + "\\production"), true);
		worldNameList = ReadFile.findFolder(new File(projectPath + "\\worlds"), true);
		if(worldNameList.contains("UK")) referqnceWorld ="UK";
		else if(worldNameList.contains("DE")) referqnceWorld ="DE";
		else if(worldNameList.contains("EU")) referqnceWorld ="EU";
		
		//referqnceWorld = worldNameList.contains("UK")? "UK":"EU";
		startYear= worldNameList.contains("UK")||worldNameList.contains("DE") ? 2020:2020;
		endtYear= worldNameList.contains("UK")? 2079:2086;
		AllfilePatheInData = Path.pathWithconditions();
		
		
		
		scenario = scenariosList.get(scenariosList.size()-1);
		
		production = projectPath + "\\production";
		version = projectPath + "\\agents";
	}

	public static ArrayList<String> pathWithconditions() {
		ArrayList<String> capitalsList = new ArrayList<>();
		try {
			capitalsList = ReadFile.findAllFile(projectPath);
		} catch (IOException e) {
		}
		return capitalsList;
	}

//	public static ArrayList<String> nameOfFile(String... condition) {
//		ArrayList<String> turn = new ArrayList<>();
//		ArrayList<String> tmp = fileFilter(condition);
//		tmp.forEach(e -> {
//			turn.add(new File(e).getName().replace(".", " ").split(" ")[0]);
//		});
//		return turn;
//	}

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
		//if(turn.size()==0)  {turn.add(" Non file funded ");}
		return turn;
	}

	public static String getProjectPath() {
		return projectPath;
	}



	static public List<String> checkfolders(String path) {
		List<String> listOfFilesMissing=new ArrayList<>(); 
		List<File> folders = CsvTools.detectFolders(path);
		List<String> foldersname = new ArrayList<>();
		folders.forEach(e->{foldersname.add(e.getName());});
		for (int i = 0; i < minFoldersNaisserly.length; i++) {
			if(!foldersname.contains(minFoldersNaisserly[i])) {
				listOfFilesMissing.add(folders.get(0).getParent()+"\\"+minFoldersNaisserly[i]);
			}
		}
		
		return listOfFilesMissing;
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
		return scenario;
	}

	public static void setSenario(String senario) {
		Path.scenario = senario;
	}

}
