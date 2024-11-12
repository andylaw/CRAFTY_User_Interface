package controllers;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.graphicalTools.CSVTableView;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.PieChartTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.PathsLoader;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Cell;

/**
 * @author Mohamed Byari
 *
 */

public class CellWindow {

	private Cell cell;

	public CellWindow(Cell patch) {
		this.cell = patch;
	}

	public void windosLocalInfo() {

		VBox box = new VBox();

		PieChart pieChart = new PieChart();
		HashMap<String, Color> color = new HashMap<>();
		cell.getCapitals().forEach((k, v) -> {
			color.put(k, ColorsTools.RandomColor());
		});

		new PieChartTools().updateChart(cell.getCapitals(), color, pieChart, true);

		box.getChildren()
				.addAll(Tools.hBox(Tools.text(PathsLoader.getScenario() + "  ", Color.BLUE),
						Tools.text(PathsLoader.getCurrentYear() + "  ", Color.BLUE)),
						Tools.hBox(cellDataTable(), pieChart));
		ScrollPane sp = new ScrollPane();
		sp.setContent(box);
		new NewWindow().creatwindows("Patch  (" + (int) cell.getX() + "," + (int) cell.getY() + ")", 0.4, 0.8, sp);

	}

	TableView<ObservableList<String>> cellDataTable() {

		String[][] data = new String[cell.getCapitals().size() + 1][2];
		AtomicInteger i = new AtomicInteger(0);
		cell.getCapitals().forEach((k, v) -> {
			data[i.get()][0] = k;
			if (data[i.get()][0].equals("FR")) {
				data[i.get()][1] = cell.getOwner().getLabel();
			} else if (data[i.get()][0].equals("Region")) {
				// data[i.get()][1] = patch.country;
			} else {
				data[i.get()][1] = v + "";
			}
			i.getAndIncrement();
		});
		data[0][0] = "Attribute Name";
		data[0][1] = "Value";

		return CSVTableView.newtable(data);

	}

	@Override
	public String toString() {
		return " [patch=" + cell + "] \n  ";
	}

}
