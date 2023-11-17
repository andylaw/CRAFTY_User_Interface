package controllers;

import java.util.function.Consumer;

import UtilitiesFx.graphicalTools.CSVTableView;
import UtilitiesFx.graphicalTools.Histogram;
import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
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
import model.Manager;
import model.CellsSet;

/**
 * @author Mohamed Byari
 *
 */

public class NewAFT_Controller extends AFTs_Controller {
	
	public NewAFT_Controller(CellsLoader M) {
		super(M);
		
	}
	static VBox vbox = new VBox();
	public  void addaft() {
		TextField fieldText = new TextField( "AFT_Name");
		NewWindow windowAddAFT = new NewWindow();
		BorderPane rootPane = new BorderPane();
		Button addToThisSimulation = Tools.button("Add To This Simulation", "b6e7c9");
		Button addToDATA = Tools.button("ADD To Input Data", "b6e7c9");
		ColorPicker colorPicker = new ColorPicker();
		TextArea textArea = new TextArea();
		
		
		windowAddAFT.creatwindows("Add New Agent Functional Type", 0.7, 0.9, rootPane);
		Manager newAFT = new Manager();
		newAFT.setLabel("newAFT");
		
		colorPicker.setOnAction(e -> {
			newAFT.setColor(colorPicker.getValue());
		});
		
		String[][] production = new String[2][CellsSet.getServicesNames().size()];
	
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

		String[][] sensetivtyTable = new String[CellsSet.getServicesNames().size() + 1][CellsSet.getCapitalsName().size() + 1];
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			sensetivtyTable[i + 1][0] = CellsSet.getServicesNames().get(i);
			for (int j = 0; j < CellsSet.getCapitalsName().size(); j++) {
				sensetivtyTable[0][j + 1] = CellsSet.getCapitalsName().get(j);
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
			newAFT.setCompleteName(n);
			newAFT.setLabel(n) ;

		});
		addToDATA.setOnAction(e -> {
			addToThisSimulation.fire();
			creatCsvFiles(newAFT,textArea.getText());
		});
		
		addToThisSimulation.setOnAction(e -> {
			M.AFtsSet.getAftHash().put(newAFT.getLabel(), newAFT);
			AFTs_Controller.choiceAgnet.getItems().clear();
			AFTs_Controller.choiceAgnet.getItems().addAll(M.AFtsSet.getAftHash().keySet());
			AFTs_Controller.choiceAgnet.setValue(M.AFtsSet.getAftHash().keySet().iterator().next());
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
