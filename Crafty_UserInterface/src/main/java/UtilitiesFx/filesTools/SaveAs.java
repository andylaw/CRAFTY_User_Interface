package UtilitiesFx.filesTools;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import dataLoader.Paths;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SaveAs {
	
	
	public static void png(Node node) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.setInitialFileName("title.png"); // set initial file name
        File initialDirectory = new File(Paths.getProjectPath());
        fileChooser.setInitialDirectory(initialDirectory);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Files", "*.png"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File file2 = fileChooser.showSaveDialog((Stage) node.getScene().getWindow());
        if (file2 != null) {
            // user selected a file, save HBox as PNG
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setDepthBuffer(true);
            WritableImage snapshot = node.snapshot(parameters, null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file2);
                
            } catch (IOException e) {System.out.println("Failed to save HBox as PNG file: " + e.getMessage());
            }
        }            
}

}
