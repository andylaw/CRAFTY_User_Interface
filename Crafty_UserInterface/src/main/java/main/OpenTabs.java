package main;

import java.util.ArrayList;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import UtilitiesFx.graphicalTools.WarningWindowes;
import controllers.AFTs_Controller;
import controllers.Map_Controller;
import controllers.GlobalView;
import controllers.OutPut_Controller;
import controllers.ModelRunner_Controller;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.CellsSet;

/**
 * @author Mohamed Byari
 *
 */

public class OpenTabs {

	CellsLoader M;
	public static VBox vbox = new VBox();
	public static ChoiceBox<String> choiceScenario;
	public static ChoiceBox<String> year;

	public OpenTabs() {
		FxMain.imageView.setVisible(false);
		M = new CellsLoader();
		senarioPane();

	}

	public void senarioPane() {
		TabPane tabPane = new TabPane();
		WarningWindowes.showWaitingDialog(x -> {
			tabPane.setStyle(" -fx-base: #ffffff;");
			tabPane.getTabs().clear();
			FxMain.root.getChildren().clear();
			
			PathTools.writePathRecentProject("RecentProject.txt", "\n" + Paths.getProjectPath());
			M.loadCapitalsAndServiceList();
			M.loadMap();
			
			M.loadGisData();
			CellsSet.setCellsSet(M);
			CellsSet.plotCells();
			ModelRunner_Controller R = new ModelRunner_Controller(M);
			
			
			tabPane.getTabs().addAll(GlobalView.globaldataview(), new Map_Controller(M).colorWorld(),
					new AFTs_Controller(M).pane(), R.pane(), new OutPut_Controller(M).pane());
			for (Tab tab : tabPane.getTabs()) {
				tab.setClosable(false);
			}
		});
		vbox.getChildren().clear();
		vbox.getChildren().addAll(globalBox(), tabPane);
	}

	HBox globalBox() {
		choiceScenario = Tools.choiceBox(Paths.getScenariosList());
		choiceScenario.setValue(Paths.getScenario());
		ArrayList<String> listYears = new ArrayList<>();
		for (int i = Paths.getStartYear(); i < Paths.getEndtYear(); i++) {
			listYears.add(i + "");
		}
		year = Tools.choiceBox(listYears);
	
		year.setOnAction(event -> {
			if (year.getValue() != null) {
				Paths.setCurrentYear((int) Tools.sToD(year.getValue()));
				M.updateCapitals(Paths.getCurrentYear());
				if(Map_Controller.tab.isSelected()) {
				for (int i = 0; i < CellsSet.getCapitalsName().size()+1; i++) {
					if (Map_Controller.radioColor[i].isSelected()) {
						if (i < CellsSet.getCapitalsName().size()) {
							CellsSet.colorMap(CellsSet.getCapitalsName().get(i));
							Map_Controller.histogrameCapitals(Map_Controller.histogrameCapitalFequency, Paths.getCurrentYear() + "",
									CellsSet.getCapitalsName().get(i));
						} else {
							CellsSet.colorMap("FR");
						}
					}
				}
				}

			}
		});
		
		choiceScenario.setOnAction(e -> {
			Paths.setScenario(choiceScenario.getValue());
			CellsLoader.updateDemand();// = CsvTools.csvReader(Path.fileFilter(Path.scenario, "demand").get(0));
			Parent par = Map_Controller.graphDemand.getParent();
			((Pane)par).getChildren().remove(Map_Controller.graphDemand);
			Map_Controller.graphDemand = new Map_Controller(M).graphDemand();
			((Pane)par).getChildren().add(Map_Controller.graphDemand);
			year.fireEvent(e);
			M.AFtsSet.updateAFTs();
			AFTs_Controller.choiceAgnet.fireEvent(e);
			Paths.setScenario(choiceScenario.getValue());
			
		});

		HBox hbox = new HBox(new Text(" Scenario = "), choiceScenario, new Text("Current Year = "), year);

		return hbox;
	}

}
