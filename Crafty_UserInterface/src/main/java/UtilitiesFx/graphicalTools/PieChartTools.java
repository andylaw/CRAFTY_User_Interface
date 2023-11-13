package UtilitiesFx.graphicalTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import dataLoader.Agents;
import dataLoader.MapLoader;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import model.Lattice;

public class PieChartTools {
	ArrayList<PieChart.Data> data = new ArrayList<>();

	public void updateChart(HashMap<String, Double> hash, PieChart chart) {

		chart.getData().clear();
//		data.clear();
		if (hash != null)
			hash.forEach((k, v) -> {
				PieChart.Data d = new PieChart.Data(k, v);
				data.add(d);
				chart.getData().add(d);
			});
		
	}

	public void updateChart(HashMap<String, Double> hash, HashMap<String, Color> color, PieChart chart, boolean isInteractive) {
		updateChart(hash, chart);
		for (int i = 0; i < data.size(); i++) {
			for (Node n : chart.lookupAll(".data" + i)) {
				n.setStyle("-fx-pie-color: " + ColorsTools.getStringColor(color.get(data.get(i).getName())) + ";");
			}
		}
		if(isInteractive)
			legendColorPicker(color, chart);
		else
			legendColorStatic(color, chart) ;
	}
	
	public void updateChart(MapLoader M,HashMap<String,Double> hash, HashMap<String, Color> color, PieChart chart) {
		updateChart(hash, chart);
		for (int i = 0; i < data.size(); i++) {
			for (Node n : chart.lookupAll(".data" + i)) {
				n.setStyle("-fx-pie-color: " + ColorsTools.getStringColor(color.get(data.get(i).getName())) + ";");
			}
		}

		legendColorPicker(M,color, chart);

	}
	
	void legendColorStatic( HashMap<String, Color> color,PieChart chart) {
		int i = 0;
		for (Node item : chart.lookupAll("Label.chart-legend-item")) {
			Label label = (Label) item;
			Rectangle colorPicker = new Rectangle(10,10,color.get(data.get(i).getName()));
			label.setGraphic(colorPicker);
			i++;
		}
		
	}

	void legendColorPicker(HashMap<String, Color> color, PieChart chart) {

		int i = 0;
		for (Node item : chart.lookupAll("Label.chart-legend-item")) {
			Label label = (Label) item;
			ColorPicker colorPicker = new ColorPicker();
			colorPicker.setStyle("-fx-color-label-visible: false ;-fx-base: #f0f0f0;");
			colorPicker.setValue(color.get(data.get(i).getName()));
			colorPicker.setShape(new Circle());
			label.setGraphic(colorPicker);

			colorPicker.setOnAction(e -> {
				for (int j = 0; j < data.size(); j++) {
					if (data.get(j).getName().equals(label.getText())) {
						for (Node n : chart.lookupAll(".data" + j)) {
							n.setStyle("-fx-pie-color: " + ColorsTools.getStringColor(colorPicker.getValue()) + ";");
						}
					}
				}
			});
			i++;
		}
	}

	void legendColorPicker(MapLoader M, HashMap<String, Color> color, PieChart chart) {
		int i = 0;
		for (Node item : chart.lookupAll("Label.chart-legend-item")) {
			Label label = (Label) item;
			ColorPicker colorPicker = new ColorPicker();
			colorPicker.setStyle("-fx-color-label-visible: false ;-fx-base: #f0f0f0;");
			colorPicker.setValue(color.get(data.get(i).getName()));
			colorPicker.setShape(new Circle());
			label.setGraphic(colorPicker);

			colorPicker.setOnAction(e -> {
				for (int j = 0; j < data.size(); j++) {
					if (data.get(j).getName().equals(label.getText())) {
						for (Node n : chart.lookupAll(".data" + j)) {
							n.setStyle("-fx-pie-color: " + ColorsTools.getStringColor(colorPicker.getValue()) + ";");
						}
						Agents.aftReSet.get(data.get(j).getName()).setColor(colorPicker.getValue());
						Lattice.colorMap("FR");
//						Agents.aftReSet.forEach((name,agent) -> {
//							if (dataName.equals(name)) {
//								agent.color = colorPicker.getValue();
//								Lattice.colorMap("FR");}
//						});
					}
				}
			});
			i++;
		}

	}
	
}
