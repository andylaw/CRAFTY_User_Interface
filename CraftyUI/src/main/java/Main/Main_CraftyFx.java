package Main;

import java.io.File;
import java.io.IOException;
import java.util.List;

import CameraPack.Camera;
import MenubarPack.MenuBAR;
import UtilitiesFx.CsvTools;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.DataFrameReader;
import javafx.scene.layout.BorderPane;

public class Main_CraftyFx extends Application { //
	public static Camera camera = new Camera();
	Scene scene;
	public static SubScene subScene;
	private BorderPane  rootPane = new BorderPane ();
	public static Group root = new Group();
	ImageView imageView = new ImageView();
	public static double sceneWidth;
	public static  Stage primaryStage; 
	public static TabPane tabPane = new TabPane();

	@Override
	public void start(Stage primaryStage) throws IOException {
//		AtomicInteger m=new AtomicInteger();
//		  Rules.fo(c->{
//			 m.getAndAdd(c);
//			 System.out.println("m= "+m);
//			  });
//		  System.out.println("last one = "+m);
//      System.setProperty("user.dir", "C:\\");
		
		sceneWidth = Screen.getPrimary().getBounds().getWidth()/3;
		Main_CraftyFx.primaryStage=primaryStage;
		subScene = new SubScene(root,sceneWidth*2,Screen.getPrimary().getBounds().getHeight()*0.8);
		
		//subScene.widthProperty().bind(primaryStage.widthProperty());
       // subScene.heightProperty().bind(primaryStage.heightProperty().subtract(tabPane.heightProperty()));
		
		 
		
		
		//anualCapitalTable.printAll();
	//	System.out.println(anualCapitalTable);
		
		subScene.setCamera(camera);
		subScene.setFocusTraversable(true);
		
		camera.cameraMousControl(subScene);
		camera.cameraKeyCodeControl(subScene);
		camera.newzoom(subScene);
		camera.defaultcamera(root);
		//camera.defaultcamera(-500, -3000, -7500); 

		
//		if (Screen.getPrimary().getBounds().getWidth() < 1300) {
//			sceneWidth = Screen.getPrimary().getBounds().getWidth() / 1.5;
//		}
		scene = new Scene(rootPane,Screen.getPrimary().getBounds().getWidth()*0.9,Screen.getPrimary().getBounds().getHeight()*0.9);

		// tabPane.setStyle(" -fx-base: #d6d9df;");

		MenuBar menuBar = new MenuBAR(imageView);
		Image image = new Image("C:\\Users\\byari-m\\Desktop\\Inkscap-Project\\craftylogo.PNG");//"C:\\Users\\byari-m\\Documents\\Data\\CraftyLOGO.png");
		imageView.setImage(image);
		rootPane.getChildren().add(imageView);
		imageView.setTranslateX(Screen.getPrimary().getBounds().getWidth() / 3);
		imageView.setTranslateY(Screen.getPrimary().getBounds().getHeight() / 3);
		//Main_CraftyFx.tabPane.getTabs().addAll(new Tab("Run Model", new RunTab().pane()));
		// tabPane.getSelectionModel().select(3);
		
        
//        tabPane.setPrefHeight(300);  // Set the preferred height
		HBox contentBox = new HBox();
	//	contentBox.getChildren().addAll(/*new RunTab().pane()*/,);
        rootPane.setTop(menuBar);
        rootPane.setLeft(tabPane);
        rootPane.setRight(subScene);
        tabPane.setPrefWidth(sceneWidth);
        tabPane.setMaxWidth(sceneWidth);
        tabPane.setMinWidth(sceneWidth); 
        BorderPane.setAlignment(menuBar, Pos.TOP_CENTER);
        BorderPane.setAlignment(contentBox, Pos.CENTER);
        BorderPane.setMargin(contentBox, new javafx.geometry.Insets(1));


		System.out.println("rootPane.getChildren().size()  =  " + rootPane.getChildren().size());
		primaryStage.setTitle("User Interface CRAFTY");
		primaryStage.getIcons().add(new Image("C:\\Users\\byari-m\\Desktop\\Inkscap-Project\\bitma.png"));

		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.show();

//CsvTools.addRefProductionInBehvoirFileFullPath("C:\\Users\\byari-m\\Documents\\Data\\data_EUpaper_nocsv\\agents\\RCP8_5-SSP3"); 
				
		
	}



	public static void main(String[] args) {
	//	Table anualCapitalTable2 =   Table.read().csv("C:\\Users\\byari-m\\Documents\\Data\\data_EUpaper_nocsv\\csv\\Capitals.csv");
	//	System.out.println(anualCapitalTable2.printAll());
		

		launch(args);
		
	}
}