package TabsPane;


import CameraPack.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

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
	
	public SubScene subSceneWithCamera(StackPane rootPane, Node... nodes) {
		
		 Group root = new Group();
		 SubScene subScene;
		 Camera camera = new Camera();
		
		

		subScene = new SubScene(root, 400, 400);
		subScene.setFocusTraversable(true);
		subScene.widthProperty().bind(rootPane.widthProperty());
		subScene.heightProperty().bind(rootPane.heightProperty());
		subScene.setCamera(camera);
		camera.cameraKeyCodeControl(subScene);
		camera.cameraMousControl(subScene);
		camera.newzoom(subScene);
		camera.defaultcamera(-2000,-1500,-5000);
		
    
	    root.getChildren().addAll(nodes);
	       

		return subScene;
	}
}
