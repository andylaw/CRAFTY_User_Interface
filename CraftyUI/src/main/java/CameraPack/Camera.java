package CameraPack;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;

public class Camera extends PerspectiveCamera {
	public CameraTransformer cameraTransform = new CameraTransformer();
	public double mousePosX;
	public double mousePosY;
	private double mouseOldX;
	private double mouseOldY;
	private double mouseDeltaX;
	private double mouseDeltaY;

	public Camera() {
		cameraTransform.getChildren().addAll(this);
//		setFarClip(1000000.0);
//		setTranslateZ(-3000);
//		setTranslateX(4000);
//		setTranslateY(3500);
	}

	public void cameraKeyCodeControl(SubScene subScene) {
		subScene.setOnKeyPressed(event -> {
			double change = 100.0;
			// Add shift modifier to simulate "Running Speed"
			if (event.isShiftDown()) {
				change = 500.0;
			}
			// What key did the user press?
			KeyCode keycode = event.getCode();
			// Step 2c: Add Zoom controls
			if (keycode == KeyCode.S) {
				setTranslateZ(getTranslateZ() + change);
				System.out.println("SSSSSSSSSSSSSSSS");
			}
			if (keycode == KeyCode.M) {
				setTranslateZ(getTranslateZ() - change);
			}
			// Step 2d: Add Strafe controls
			if (keycode == KeyCode.D) {
				setTranslateX(getTranslateX() - change);
			}
			if (keycode == KeyCode.A) {
				setTranslateX(getTranslateX() + change);
			}
			if (keycode == KeyCode.W) {
				setTranslateY(getTranslateY() + change);
			}
			if (keycode == KeyCode.X) {
				setTranslateY(getTranslateY() - change);
			}
			// default camera
			if (keycode == KeyCode.H) {
				// defaultcamera(4000, 3500, -3000);
			}

		});
	}

	/*
	 * public void defaultcamera(double X, double Y, double Z) {
	 * cameraTransform.t.setX(0); cameraTransform.t.setY(0); setTranslateX(X);
	 * setTranslateY(Y); setTranslateZ(Z); }
	 */

	public void defaultcamera(Group root) {
		root.boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> {
			// Calculate the center point of the bounding box
			double centerX = newBounds.getMinX() + newBounds.getWidth() /2;
			double centerY = newBounds.getMinY() + newBounds.getHeight() / 2;
			 double centerZ = newBounds.getMinZ() + newBounds.getDepth()/2 ;

			// Calculate the maximum dimension of the bounding box
			double maxDimension = Math.max(newBounds.getWidth(), Math.max(newBounds.getHeight(), newBounds.getDepth()));

			// Adjust the camera position based on the bounding box
			double distance = maxDimension * 1.1; // Adjust the factor as needed
			setTranslateX(centerX);
			setTranslateY(centerY);
			setTranslateZ(centerZ-distance);

			// Adjust the camera's field of view to fit the bounding box
			double fov = Math.toDegrees(2 * Math.atan(maxDimension / (2 * distance))); // Implement this method
			setFieldOfView(fov);
		});
	}

	public void cameraMousControl(SubScene scene) {

		scene.setOnMousePressed((MouseEvent me) -> {
			mousePosX = me.getSceneX();
			mousePosY = me.getSceneY();
			mouseOldX = me.getSceneX();
			mouseOldY = me.getSceneY();
		});
		scene.setOnMouseDragged((MouseEvent me) -> {
			if (!isHover()) {
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX);
				mouseDeltaY = (mousePosY - mouseOldY);

				double modifier = 60.0;
				double modifierFactor = 0.1;

				if (me.isControlDown()) {
					modifier = 0;
				}
				if (me.isShiftDown()) {
					modifier = 500.0;
				}
				if (me.isPrimaryButtonDown()) {
					cameraTransform.t.setX(cameraTransform.t.getX() - mouseDeltaX * modifierFactor * modifier * 0.3); // -
					cameraTransform.t.setY(cameraTransform.t.getY() - mouseDeltaY * modifierFactor * modifier * 0.3); // -

				} else if (me.isSecondaryButtonDown()) {
					double z = getTranslateZ();
					double newZ = z + (mouseDeltaX - mouseDeltaY) * modifierFactor * modifier;
					setTranslateZ(newZ);
				}

			}
		});
	}

	public void newzoom(SubScene scene) {
		scene.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				double deltaY = event.getDeltaY();

				setTranslateZ(getTranslateZ() + deltaY);

				event.consume();
//				System.out.println(
//						cameraTransform.t.getX()+","+
//								cameraTransform.t.getY()+","+
//								getTranslateX()+","+
//								getTranslateY()+","+
//								getTranslateZ());
			}
		});
	}

	public void cameraFocusBox(Rectangle rec, double Z) {
		cameraTransform.t.setX(0);
		cameraTransform.t.setY(0);
		setTranslateX(getTranslateX() + rec.getX());
		setTranslateY(getTranslateY() + rec.getY());
		setTranslateZ(Z);
	}
}
