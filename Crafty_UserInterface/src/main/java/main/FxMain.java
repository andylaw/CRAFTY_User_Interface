package main;


import UtilitiesFx.cameraTools.Camera;
import UtilitiesFx.graphicalTools.GraphicConsol;
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
	public static AnchorPane B= new AnchorPane();
	@Override
	public void start(Stage primaryStage) throws Exception {
		double w = Screen.getPrimary().getBounds().getWidth();
		double h = Screen.getPrimary().getBounds().getHeight();
		
		FxMain.primaryStage = primaryStage;
		subScene = new SubScene(root, w * .45, h*0.8);

		MenuBar menuBar = new MenuBAR();
		
		
		imageView.setImage(new Image("file:///C:/Users/byari-m/Desktop/Inkscap-Project/craftylogo.png"));
		B.getChildren().add(imageView);
		imageView.setTranslateX(w / 3);
		imageView.setTranslateY(h / 3);
		imageView.setScaleX(.75);
		imageView.setScaleY(.75);
		
		primaryStage.setTitle(" CRAFTY User Interface ");

		VBox vbox = new VBox(menuBar,B);
		Scene scene = new Scene(vbox, w*.8 , h*.8);
		primaryStage.setScene(scene);

		
		primaryStage.setMaximized(true);
		primaryStage.show();
		primaryStage.setOnCloseRequest(event -> Platform.exit());
	}

	
	public static void main(String[] args) {
		launch(args);

	}
	

}
