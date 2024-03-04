package main;


import java.io.InputStream;

import UtilitiesFx.cameraTools.Camera;
import UtilitiesFx.graphicalTools.Tools;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author Mohamed Byari
 *
 */

public class FxMain extends Application {
	public static Camera camera = new Camera();

	public static SubScene subScene;
	public static Group root = new Group();
	public static ImageView imageView = new ImageView();
	public static Stage primaryStage;
	public static AnchorPane anchor = new AnchorPane();
	public static Scene scene = new Scene(anchor);

	@Override
	public void start(Stage primaryStage) throws Exception {
		double w = Screen.getPrimary().getBounds().getWidth();
		double h = Screen.getPrimary().getBounds().getHeight();

//		long startTime2 = System.currentTimeMillis();
//		CsvTools.readerAsHash("C:\\Users\\byari-m\\Documents\\Data\\data-DE\\worlds\\Baseline_map_DE.csv");
//		long endTime2 = System.currentTimeMillis();
//		long re2 = endTime2 - startTime2;
//		
//			System.out.println("i= "+i+" TablSaw= "+re1+"   BufferedReader= "+ re2+ "propotion "+ (re1/re2));
//		}
//	List<File> folderList = PathTools.detectFolders("C:\\Users\\byari-m\\Documents\\Data\\data-DE\\worlds\\Capitals");
//	for (int i = 0; i < folderList.size(); i++) {
//		List<File> vect = CsvTools.detectFiles(folderList.get(i).getAbsolutePath());
//		
//		for (int j = 0; j < vect.size(); j++) {
//			System.out.println( vect.get(j).getAbsolutePath());
//			CsvTools.cleanCsvFile( vect.get(j).getAbsolutePath());
//	}
//    	

//    	System.out.println("cleaned");

//		Map<String, String[]> tst = processCSV("C:\\Users\\byari-m\\Downloads\\Baseline_map.csv");
//		System.out.println(tst.size());

		
		FxMain.primaryStage = primaryStage;
		subScene = new SubScene(root, w * .45, h * .95);

		InputStream imageStream = getClass().getResourceAsStream("/craftylogo.png");
		imageView = Tools.logo(imageStream, w / 3, h / 3, 0.65);
		anchor.getChildren().add(imageView);

		primaryStage.setTitle(" CRAFTY User Interface ");

		VBox vbox = new VBox(FXMLLoader.load(getClass().getResource("/fxmlControllers/MenuBar.fxml")), anchor);
		scene = new Scene(vbox, w * .8, h * .8);
		scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

		primaryStage.setScene(scene);

		primaryStage.setMaximized(true);
		primaryStage.show();
		primaryStage.setOnCloseRequest(event -> Platform.exit());
	}

	public static void main(String[] args) {
		launch(args);
	}


}
