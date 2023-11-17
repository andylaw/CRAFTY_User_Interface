package main;


import UtilitiesFx.cameraTools.Camera;
import UtilitiesFx.graphicalTools.GraphicConsol;
import UtilitiesFx.graphicalTools.Tools;
import controllers.MenuBAR;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
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
	public static TitledPane titelMapPane;

	@Override
	public void start(Stage primaryStage) throws Exception {
		FxMain.primaryStage = primaryStage;
		subScene = new SubScene(root, Screen.getPrimary().getBounds().getWidth() * .45, Screen.getPrimary().getBounds().getHeight()*0.7);
		VBox g = new VBox();
		new GraphicConsol();
		new GraphicConsol();
		titelMapPane = Tools.T("Map",true,subScene);
		g.getChildren().addAll(titelMapPane/*,GraphicConsol.start()*/);
		

		
		double w = Screen.getPrimary().getBounds().getWidth();
		double h = Screen.getPrimary().getBounds().getHeight();
		
		MenuBar menuBar = new MenuBAR();

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(OpenTabs.vbox,g);
        
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(splitPane);
        AnchorPane.setTopAnchor(splitPane, 0.0);
        AnchorPane.setBottomAnchor(splitPane, 0.0);
        AnchorPane.setLeftAnchor(splitPane, 0.0);
        AnchorPane.setRightAnchor(splitPane, 0.0);
        
        VBox vbox = new VBox(menuBar,anchorPane);


		titelMapPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
            if (isNowExpanded) {
            	subScene.setWidth(Screen.getPrimary().getBounds().getWidth() *.45);
            } else {
            	subScene.setWidth(10);
            }
        });

		
		
		imageView.setImage(new Image("file:///C:/Users/byari-m/Desktop/Inkscap-Project/craftylogo.png"));
		root.getChildren().add(imageView);
//		imageView.setTranslateX(w / 10);
//		imageView.setTranslateY(h / 3);
		imageView.setScaleX(.75);
		imageView.setScaleY(.75);
		
		primaryStage.setTitle("CRAFTY User Interface ");

		Scene scene = new Scene(vbox, w * 0.9, h * 0.9);
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.show();
		primaryStage.setOnCloseRequest(event -> Platform.exit());
	}

	public static void main(String[] args) {
		launch(args);

	}
	

}
