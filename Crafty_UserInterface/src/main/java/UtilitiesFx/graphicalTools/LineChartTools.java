package UtilitiesFx.graphicalTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dataLoader.AFTsLoader;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @author Mohamed Byari
 *
 */

public class LineChartTools extends Node {

	LineChart<Number, Number> lineChart;
	
	public static void lineChart(Pane box,LineChart<Number, Number> lineChart, HashMap<String, double[]> hash) {
		Series<Number, Number>[] series = new XYChart.Series[hash.size()];

		AtomicInteger i = new AtomicInteger();
		hash.forEach((key, value) -> {
			if (value != null) {
				series[i.get()] = new XYChart.Series<Number, Number>();
				series[i.get()].setName(key);
				lineChart.getData().add(series[i.get()]);
				i.getAndIncrement();
			}
		});
		AtomicInteger k = new AtomicInteger();
		hash.forEach((key, value) -> {
			if (value != null) {
				for (int j = 1; j < value.length; j++) {
					series[k.get()].getData().add(new XYChart.Data<>(j, (Number) (value[j])));
				}
				k.getAndIncrement();
			}
		});
		if(hash.size()>8) {
		AtomicInteger K = new AtomicInteger();
		hash.forEach((key, value) -> {
			if (value != null) {
				for (int j = 1; j < value.length; j++) {
					series[K.get()].getData().add(new XYChart.Data<>( j,
							  + value[j]));
					series[K.get()].getNode().lookup(".chart-series-line").setStyle(
							"-fx-stroke: " + ColorsTools.getStringColor(ColorsTools.colorlist(K.get())) + ";");
				}
				K.getAndIncrement();
			}
		});
		labelcolor(lineChart);
		lineChart.setCreateSymbols(false);
		}
		
		
		MousePressed.mouseControle(box,lineChart);
	}

	public Node graph(Pane box,String titel, HashMap<String, Number[]> hash, NumberAxis X, NumberAxis Y) {

		lineChart = new LineChart<>(X, Y);
		lineChart.setCreateSymbols(false);

		XYChart.Series<Number, Number>[] series = new XYChart.Series[hash.size()];

		AtomicInteger i = new AtomicInteger();
		hash.forEach((key, value) -> {
			if (value != null) {
				series[i.get()] = new XYChart.Series<Number, Number>();
				series[i.get()].setName(key);
				lineChart.getData().add(series[i.get()]);
				i.getAndIncrement();
			}
		});
		AtomicInteger k = new AtomicInteger();
		hash.forEach((key, value) -> {
			if (value != null) {
				for (int j = 1; j < value.length; j++) {
					series[k.get()].getData().add(new XYChart.Data<>(X.getLowerBound() + j,
							(Number) (Y.getLowerBound() + value[j].doubleValue())));
					series[k.get()].getNode().lookup(".chart-series-line").setStyle(
							"-fx-stroke: " + ColorsTools.getStringColor(ColorsTools.colorlist(k.get())) + ";");
				}
				k.getAndIncrement();
			}
		});


		labelcolor(lineChart);
		lineChart.setTitle(titel);
		MousePressed.mouseControle(box,lineChart);
		return lineChart;
	}

	 private void addSeriesToChart( XYChart.Series<Number, Number> series) {
	        if (!lineChart.getData().contains(series)) {
	            lineChart.getData().add(series);
	        }
	        else
	        lineChart.getData().remove(series);
	    }


	public static void gridCahrt(int nbrInChart,GridPane gridPane, ArrayList<LineChart<Number, Number>> lineChart) {
		 gridPane = Tools.grid(10, 10);

		int j = 0, k = 0;
		for (int m = 0; m < lineChart.size(); m++) {
			// lineChart.get(m).setMinSize(scale, scale);
			gridPane.add(lineChart.get(m), j++, k);
			if (j % nbrInChart == 0) {
				k++;
				j = 0;
			}
		}

	}

	public static void labelcolor(LineChart<Number, Number> lineChart) {
		int m = 0;
		for (Node item : lineChart.lookupAll("Label.chart-legend-item")) {
			Label label = (Label) item;//
			Color co = AFTsLoader.aftReSet.get(label.getText()) != null ? AFTsLoader.aftReSet.get(label.getText()).getColor()
					: ColorsTools.colorlist(m);
			final Rectangle rectangle = new Rectangle(10, 10, co);
			label.setGraphic(rectangle);
			m++;
		}
	}


	public Node graph(Pane box,String titel, HashMap<String, Number[]> hash) {
		return graph(box,titel, hash, new NumberAxis(), new NumberAxis());
	}

	public Node graph(Pane box,String titel, HashMap<String, Number[]> hash, NumberAxis X) {
		return graph(box,titel, hash, X, new NumberAxis());
	}




}
