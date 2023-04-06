package Main;

import java.io.IOException;


import CameraPack.Camera;
import MenubarPack.MenuBAR;
import UtilitiesFx.Tools;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import mainA.A;

public class Main_CraftyFx extends Application { //
	public static Camera camera = new Camera();
	Scene scene;
	private SubScene subScene;
	private StackPane rootPane = new StackPane();
	public static Group root = new Group();
	ImageView imageView = new ImageView();
	public static double sceneWidth;
	public static  Stage primaryStage; 
	public static TabPane tabPane = new TabPane();

	@Override
	public void start(Stage primaryStage) throws IOException {
		
//		
		System.out.println(A.a);
		
		
		 
		Main_CraftyFx.primaryStage=primaryStage;
		subScene = new SubScene(root, 400, 400);
		subScene.setFocusTraversable(true);
		subScene.widthProperty().bind(rootPane.widthProperty());
		subScene.heightProperty().bind(rootPane.heightProperty());
		subScene.setCamera(camera);

		camera.cameraKeyCodeControl(subScene);
		camera.cameraMousControl(subScene);
		camera.newzoom(subScene);
		camera.defaultcamera(-500, -3000, -7500);

		sceneWidth = Screen.getPrimary().getBounds().getWidth() / 3;
		if (Screen.getPrimary().getBounds().getWidth() < 1300) {
			sceneWidth = Screen.getPrimary().getBounds().getWidth() / 1.5;
		}
		scene = new Scene(rootPane);

		// tabPane.setStyle(" -fx-base: #d6d9df;");

		MenuBar menuBar = new MenuBAR(imageView);
		Image image = new Image("C:\\Users\\byari-m\\Desktop\\Inkscap-Project\\craftylogo.PNG");//"C:\\Users\\byari-m\\Documents\\Data\\CraftyLOGO.png");
		imageView.setImage(image);

		// tabPane.getSelectionModel().select(3);

		rootPane.getChildren().addAll(Tools.vBox(menuBar, tabPane), subScene, imageView/**/);
		// rootPane.getChildren().add();

		System.out.println("rootPane.getChildren().size()  =  " + rootPane.getChildren().size());
		primaryStage.setTitle("User Interface CRAFTY");
		primaryStage.getIcons().add(new Image("C:\\Users\\byari-m\\Desktop\\Inkscap-Project\\bitma.png"));

		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.show();

		
			
	}
	
	

	public static void main(String[] args) {
		//System.out.println(new A().a);

		launch(args);
		
	}
}