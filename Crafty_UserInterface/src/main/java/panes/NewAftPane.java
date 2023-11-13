package panes;

import java.util.function.Consumer;

import UtilitiesFx.graphicalTools.CSVTableView;
import UtilitiesFx.graphicalTools.Histogram;
import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.AFT;
import model.Lattice;

/**
 * @author Mohamed Byari
 *
 */

public class NewAftPane extends AFT_Configuration {
	static VBox vbox = new VBox();
	public static void addaft() {
		TextField fieldText = new TextField( "AFT_Name");
		NewWindow windowAddAFT = new NewWindow();
		BorderPane rootPane = new BorderPane();
		Button addToThisSimulation = Tools.button("Add To This Simulation", "b6e7c9");
		Button addToDATA = Tools.button("ADD To Input Data", "b6e7c9");
		ColorPicker colorPicker = new ColorPicker();
		TextArea textArea = new TextArea();
		
		
		windowAddAFT.creatwindows("Add New Agent Functional Type", 0.7, 0.9, rootPane);
		AFT newAFT = new AFT();
		newAFT.setLabel("newAFT");
		
		colorPicker.setOnAction(e -> {
			newAFT.setColor(colorPicker.getValue());
		});
		
		String[][] production = new String[2][Lattice.getServicesNames().size()];
	
		for (int j = 0; j < newAFT.getProductivityLevel().keySet().toArray().length; j++) {
			production[0][j] = (String) newAFT.getProductivityLevel().keySet().toArray()[j];
			production[1][j] = "0.0";
		}
		Button productionFire = new Button();
		Consumer<String> actionP = x -> {
			productionFire.fire();
		};

		TableView<ObservableList<String>> tableProduction = CSVTableView.newtable(production, actionP);
		BarChart<String, Number> histogram = new BarChart<String, Number>(new CategoryAxis(), new NumberAxis());

		productionFire.setOnAction(e -> {
			updateProduction(newAFT, tableProduction);
			Histogram.histo(vbox, "Productivity levels", histogram, newAFT.getProductivityLevel());
		});

		String[][] sensetivtyTable = new String[Lattice.getServicesNames().size() + 1][Lattice.getCapitalsName().size() + 1];
		for (int i = 0; i < Lattice.getServicesNames().size(); i++) {
			sensetivtyTable[i + 1][0] = Lattice.getServicesNames().get(i);
			for (int j = 0; j < Lattice.getCapitalsName().size(); j++) {
				sensetivtyTable[0][j + 1] = Lattice.getCapitalsName().get(j);
				sensetivtyTable[i + 1][j + 1] = "0.0";
			}
		}
		Button sensitivtyFire = new Button();
		Consumer<String> action = x -> {
			sensitivtyFire.fire();
		};

		GridPane gridBehevoir = new GridPane();
		AgentParametre(newAFT, gridBehevoir);

		TableView<ObservableList<String>> tableSensetivty = CSVTableView.newtable(sensetivtyTable, action);
		

		GridPane gridRadar = Tools.grid(10, 15);
		Text name = new Text();

		sensitivtyFire.setOnAction(e -> {
			updateSensitivty(newAFT, gridRadar, tableSensetivty);
		});
		fieldText.setOnAction(e -> {
			String n = fieldText.getText();
			name.setText(n);
			newAFT.setName(n);
			newAFT.setLabel(n) ;

		});
		addToDATA.setOnAction(e -> {
			addToThisSimulation.fire();
			creatCsvFiles(newAFT,textArea.getText());
		});
		addToThisSimulation.setOnAction(e -> {
			AFTsLoader.aftReSet.put(newAFT.getLabel(), newAFT);
			AFT_Configuration.choiceAgnet.getItems().clear();
			AFT_Configuration.choiceAgnet.getItems().addAll(AFTsLoader.aftReSet.keySet());
			AFT_Configuration.choiceAgnet.setValue(AFTsLoader.aftReSet.keySet().iterator().next());
		 });

		sensitivtyFire.fire();
		productionFire.fire();
		vbox.getChildren().addAll(
				Tools.hBox(Tools.text("Agent Functional Type name:   ", Color.BLUE), fieldText, name,new Text("AFT Color"),colorPicker),
				Tools.hBox(Tools.vBox(tableProduction, Tools.T(" Behevoir Parametrs ", true,gridBehevoir), new Text("Description"),textArea), histogram), tableSensetivty,
				Tools.T("", true,gridRadar));
		rootPane.setCenter(vbox);
		rootPane.setBottom(Tools.hBox(addToThisSimulation,addToDATA));
	}






}
