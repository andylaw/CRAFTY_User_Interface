package Main;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ConsoleExample extends Application {

    private static final int IMAGE_WIDTH = 300;
    private static final int IMAGE_HEIGHT = 50;
    private static final int COLOR_SCALE_WIDTH = 10;
    private static final int COLOR_SCALE_HEIGHT = IMAGE_HEIGHT;

    public void start(Stage primaryStage) {
        // Create a VBox to hold the image and scaling numbers
         VBox vbox = new VBox();

        // Create a color scale image
        WritableImage colorScaleImage = createColorScaleImage();

        // Create an ImageView for the color scale image
         ImageView imageView = new ImageView(colorScaleImage);
        vbox.getChildren().add(imageView);

        // Create scaling numbers
        vbox.getChildren().add(createScalingNumbers());

        // Set up the scene
        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Color Scale Image Example");
        primaryStage.show();
    }

    private WritableImage createColorScaleImage() {
        WritableImage image = new WritableImage(IMAGE_WIDTH, IMAGE_HEIGHT);
        PixelWriter pixelWriter = image.getPixelWriter();

        // Create a gradient color scale
        for (int x = 0; x < COLOR_SCALE_WIDTH; x++) {
            double colorValue = (double) x / COLOR_SCALE_WIDTH;
            Color color = Color.hsb(colorValue * 360.0, 1.0, 1.0);

            for (int y = 0; y < IMAGE_HEIGHT; y++) {
                pixelWriter.setColor(x, y, color);
            }
        }

        return image;
    }

    private Text createScalingNumbers() {
        Text text = new Text();
        text.setFont(Font.font(12));

        // Set the scaling numbers text
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= COLOR_SCALE_WIDTH; i++) {
            double value = (double) i / COLOR_SCALE_WIDTH;
            sb.append(String.format("%.2f ", value));
        }
        text.setText(sb.toString());

        return text;
    }

    public static void main(String[] args) {
        launch(args);
    }
}