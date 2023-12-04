package controllers;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.CSVTableView;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.Paths;
import javafx.collections.ObservableList;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import main.OpenTabs;

/**
 * @author Mohamed Byari
 *
 */

public class GlobalView {

	public static Tab globaldataview() {

		TextField startTick = new TextField(Paths.getStartYear() + "");
		startTick.setOnAction(e -> {
			Paths.setStartYear((int) Tools.sToD(startTick.getText()));
		});
		TextField endTick = new TextField(Paths.getEndtYear() + "");
		endTick.setOnAction(e -> {
			Paths.setEndtYear((int) Tools.sToD(endTick.getText()));
		});
		VBox vbox = Tools.vBox();
		Text txtC = Tools.text("Capitals: ", Color.BLUE);
		TableView<ObservableList<String>> tabC = CSVTableView.newtable(PathTools.fileFilter("\\Capitals.csv").get(0));
		Text txtS = Tools.text(" Services: ", Color.BLUE);
		TableView<ObservableList<String>> tabS = CSVTableView.newtable(PathTools.fileFilter("\\Services.csv").get(0));

		Text txtSc = Tools.text(" Scenarios: ", Color.BLUE);
		TableView<ObservableList<String>> tabSc = CSVTableView.newtable(PathTools.fileFilter("\\scenarios.csv").get(0));

		TableView<ObservableList<String>> tabAFts = CSVTableView
				.newtable(PathTools.fileFilter("\\AFTsMetaData.csv").get(0));

		vbox.getChildren().addAll(Tools.hBox(new Text(" Start Year = "), startTick, new Text(" End Year = "), endTick),
				new Separator(),
				Tools.T("Capitals, Services and Scenarios", true, Tools.hBox(txtC, tabC, txtS, tabS, txtSc, tabSc)),
				new Separator(), Tools.T("Agent Functional Types (AFTs): ", true, tabAFts));

		Tab tab = new Tab("Global View", vbox);
		tab.setOnSelectionChanged(e -> {
			OpenTabs.choiceScenario.setDisable(true);
			OpenTabs.year.setDisable(true);
		});
		return tab;

	}
}
