package UtilitiesFx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import TabsPane.NewWindow;
import WorldPack.Map;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

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
		mouseControle(chart, "PieChart");
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
	
	public void updateChart(Map M,HashMap<String, Double> hash, HashMap<String, Color> color, PieChart chart) {

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

	void legendColorPicker(Map M, HashMap<String, Color> color, PieChart chart) {

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
						String  dataName = data.get(j).getName();
						M.agents.AFT.forEach(agent -> {
							if (dataName.equals(agent.label)) {
								agent.color = colorPicker.getValue();
								M.colorMap("FR");}
						});
					}
				}
			});
			i++;
		}

	}
	


	public void mouseControle(PieChart chart, String titel) {

		chart.setOnMouseDragged(event -> {
			chart.setPrefHeight(event.getY());
		});
		HashMap<String, Consumer<String>> hashm = new HashMap<>();
		hashm.put("Save as PNG", (x) -> {
			SaveAs.png(chart, "C:\\Users\\byari-m\\Desktop\\folder\\" + titel + ".png");
		});

		Consumer<String> c = (x) -> {
			Pane tst = (Pane) chart.getParent();
			tst.getChildren().remove(chart);
		};
		hashm.put("Delet", c);
		hashm.put("Open Window", (x) -> {
			new NewWindow().creatwindows("OutPut Configuration", chart);
		});

		MouseLeftPressed.smartMenu(chart, hashm);
	}
}
