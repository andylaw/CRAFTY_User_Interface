package TabsPane;


import CameraPack.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;

public class  NewWindow extends Stage {


	
	public  void creatwindows(String name,double Width ,double Height , Node... nodes) {
				
		Scene scene;
		StackPane rootPane = new StackPane();
		
		rootPane.getChildren().addAll(nodes);
		
		scene = new Scene(rootPane, Screen.getPrimary().getBounds().getWidth()* Width,
				Screen.getPrimary().getBounds().getHeight() * Height);
	
		setTitle(name);
		setScene(scene);
		show();
	}
	
	public  void creatwindows(String name, Node... nodes) {
		Scene scene;
		StackPane rootPane = new StackPane();
		rootPane.getChildren().addAll(nodes);
		scene = new Scene(rootPane);
	
		setTitle(name);
		setScene(scene);
		show();
	}
	
	public SubScene subSceneWithCamera(BorderPane rootPane, Node... nodes) {
		
		 Group root = new Group();
		 SubScene subScene;
		 Camera camera = new Camera();
		
		

		subScene = new SubScene(root,Screen.getPrimary().getBounds().getWidth()*0.5,Screen.getPrimary().getBounds().getHeight()*.8);
		subScene.setFocusTraversable(true);
	//	subScene.widthProperty().bind(rootPane.widthProperty());
	//	subScene.heightProperty().bind(rootPane.heightProperty());
		subScene.setCamera(camera);
		camera.cameraKeyCodeControl(subScene);
		camera.cameraMousControl(subScene);
		camera.newzoom(subScene);
		root.getChildren().addAll(nodes);
	    camera.defaultcamera(root);
		
    
	    

		return subScene;
	}
}
