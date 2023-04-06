package UtilitiesFx;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import TabsPane.NewWindow;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class LineChartTools extends Node {

	LineChart<Number, Number> lineChart;

	public Node graph(String titel, HashMap<String, Number[]> hash, NumberAxis X, NumberAxis Y) {

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

		VBox vbox = new VBox();
		RadioButton[] radio = new RadioButton[series.length];
		for (int n = 0; n < series.length; n++) {
			radio[n] = new RadioButton(series[n].getName());
			radio[n].setSelected(true);
			vbox.getChildren().addAll(radio[n], new Separator());
		}

		for (int n = 0; n < series.length; n++) {
			int m = n;
			radio[n].setOnAction(e -> {
				if (!radio[m].isSelected()) {
					deletItem(series[m].getName());
				} else {
					addItem(series[m]);
					
				}

			});
		}

		labelcolor();
		lineChart.setTitle(titel);
		 HBox turn = Tools.hBox(lineChart, new Text(), vbox);
		mouseControle(turn, titel);
		return turn;
	}

	void mouseControle(Pane pane, String titel) {

		pane.setOnMouseDragged(event -> {
			pane.setPrefHeight(event.getY());
		});
		HashMap<String, Consumer<String>> hashm = new HashMap<>();
		hashm.put("Save as PNG", (x) -> {
			SaveAs.png(lineChart, "C:\\Users\\byari-m\\Desktop\\folder\\" + titel + ".png");
		});
		hashm.put("Delet", (x) -> {
			pane.getChildren().clear();
		});
		hashm.put("Open Window", (x) -> {
			new NewWindow().creatwindows("OutPut Configuration", pane);
		});

		MouseLeftPressed.smartMenu(pane, hashm);
	}

	void labelcolor() {
		int m = 0;
		for (Node item : lineChart.lookupAll("Label.chart-legend-item")) {
			Label label = (Label) item;
			final Rectangle rectangle = new Rectangle(10, 10, ColorsTools.colorlist(m));
			label.setGraphic(rectangle);
			m++;
		}
	}

	public void deletItem(String name) {
		lineChart.getData().removeIf(series -> series.getName().equals(name));
	}

	void addItem(XYChart.Series<Number, Number> serie) {
		lineChart.getData().add(serie);
		serie.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: "
				+ ColorsTools.getStringColor(ColorsTools.colorlist(lineChart.getData().indexOf(serie))) + ";");
	}

	public Node graph(String titel, HashMap<String, Number[]> hash) {
		return graph(titel, hash, new NumberAxis(), new NumberAxis());
	}

	public Node graph(String titel, HashMap<String, Number[]> hash, NumberAxis X) {
		return graph(titel, hash, X, new NumberAxis());
	}
}
