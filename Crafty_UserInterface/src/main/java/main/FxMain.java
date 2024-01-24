package main;

import java.io.File;
import java.io.InputStream;

import UtilitiesFx.cameraTools.Camera;
import UtilitiesFx.filesTools.ConvertScotlanddata;
import controllers.MenuBAR;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.MaskRestrictions;


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

	@Override
	public void start(Stage primaryStage) throws Exception {
		double w = Screen.getPrimary().getBounds().getWidth();
		double h = Screen.getPrimary().getBounds().getHeight();

//	ConvertScotlanddata.initilasiation();
//		ConvertScotlanddata.scotlandcells(
//				"C:\\Users\\byari-m\\Documents\\Data\\data_Wales\\worlds\\UK\\LandUseControl\\UrbanMask\\SSP5"
//				); 
		

		//new MaskRestrictions().setToMaskInitialisation("C:\\Users\\byari-m\\Documents\\Data\\Scotland\\worlds\\LandUseControl\\UrbanMask\\SSP1\\UrbanMask_SSP1_2070.csv");
	       

		FxMain.primaryStage = primaryStage;
		subScene = new SubScene(root, w * .45, h *.95);

		MenuBar menuBar = new MenuBAR();

		


		InputStream imageStream = getClass().getResourceAsStream("/craftylogo.png");
		Image image = new Image(imageStream);
		imageView.setImage(image);
		
		anchor.getChildren().add(imageView);
		imageView.setTranslateX(w / 3);
		imageView.setTranslateY(h / 3);
		imageView.setScaleX(.75);
		imageView.setScaleY(.75);

		primaryStage.setTitle(" CRAFTY User Interface ");

		VBox vbox = new VBox(menuBar, anchor);
		Scene scene = new Scene(vbox, w * .8, h * .8);
		primaryStage.setScene(scene);

		primaryStage.setMaximized(true);
		primaryStage.show();
		primaryStage.setOnCloseRequest(event -> Platform.exit());
	}

	public static void main(String[] args) {
		launch(args);
	}

}
