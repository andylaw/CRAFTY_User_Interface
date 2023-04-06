package UtilitiesFx;

import java.util.Random;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

public class ColorsTools {
	
	public static Color  colorlist(int nbr ) {
		switch(nbr) {
		case 0:
			   return Color.RED;
		case 1:
			   return Color.ORANGE;
		case 2:
			   return Color.YELLOW;
		case 3:
			   return Color.GREEN;
		case 4:
			   return Color.BLUE;	 
		case 5:
			   return Color.PURPLE;	   
		case 6:
			   return Color.PINK;	 
		case 7:
			   return Color.BROWN;	  
		case 8:
			   return Color.MAGENTA;	  
		case 9:
			   return Color.INDIGO;
		case 11:
			   return Color.LIME;  
		case 12:
			   return Color.TEAL;  
		case 13:
			   return Color.VIOLET;  
		case 14:
			   return Color.OLIVE;  
		case 15:
			   return Color.MAROON;  
		case 16:
			   return Color.GREY;  
		case 17:
			   return Color.BLACK;  
		default:
		   return new Color(new Random().nextDouble(), new Random().nextDouble(), new Random().nextDouble(), 1.0);
		}
			}

	public static Image createColorScaleImage(int width, int height, double MAX) {
		WritableImage image = new WritableImage(width, height);
		PixelWriter pixelWriter = image.getPixelWriter();
		for (int y = 0; y < height; y++) {
			double value = MAX - (MAX) * y / height;
			Color color = getColorForValue(MAX, 1 - value);
			for (int x = 0; x < width; x++) {
				pixelWriter.setColor(x, y, color);
			}
		}

		return image;
	}

	public static Color getColorForValue(double MAX, double value) {
		double BLUE_HUE = Color.BLUE.getHue();
		double RED_HUE = Color.RED.getHue();
		if (value > MAX) {
			return Color.WHITE;
		}
		if (value == -1) {
			return Color.WHITE;
		}

		double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value) / (MAX);
		return Color.hsb(hue, 1.0, 1.0);
	}

	public static Color[] descrretColor(int size) {
		Color[] color = new Color[size];
		for (int i = 0; i < size; i++) {
			color[i] = Color.rgb(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
		}
		return color;
	}

	public static Stop[] color(int nbr) {
		if (nbr % 4 == 0)
			return new Stop[] { new Stop(0.0, Color.rgb(0, 0, 255, 0.25)), new Stop(0.5, Color.rgb(255, 0, 0, 0.5)),
					new Stop(1.0, Color.rgb(50, 0, 255, 0.75)) };
		else if (nbr % 4 == 1)
			return new Stop[] { new Stop(0.0, Color.rgb(0, 255, 255, 0.25)), new Stop(0.5, Color.rgb(255, 255, 0, 0.5)),
					new Stop(1.0, Color.rgb(255, 0, 255, 0.75)) };
		else if (nbr % 4 == 2)
			return new Stop[] { new Stop(0.0, Color.rgb(0, 255, 255, 0.25)), new Stop(0.5, Color.rgb(0, 255, 255, 0.5)),
					new Stop(1.0, Color.rgb(0, 0, 255, 0.75)) };
		else if (nbr % 5 == 2)
			return new Stop[] { new Stop(0.0, Color.rgb(240, 255, 240, 0.25)),
					new Stop(0.5, Color.rgb(176, 196, 222, 0.5)), new Stop(1.0, Color.rgb(112, 128, 144, 0.75)) };
		else
			return new Stop[] { new Stop(0.0, Color.rgb(0, 0, 255, 0.25)), new Stop(0.5, Color.rgb(0, 255, 255, 0.5)),
					new Stop(1.0, Color.rgb(0, 0, 200, 0.75)) };

	}

	public static Color RandomColor() {
		return Color.rgb(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
	}

	public static String getStringColor(Color color) {
		int r = (int) (255 * color.getRed());
		int g = (int) (255 * color.getGreen());
		int b = (int) (255 * color.getBlue());

		return String.format("#%02x%02x%02x", r, g, b);
	}
}
