package main;

import panes.AFT_Configuration;
import panes.DataDisplay;
import panes.GlobalView;
import panes.OutPutPane;
import panes.RunPane;

import java.util.ArrayList;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.Tools;
import UtilitiesFx.graphicalTools.WarningWindowes;
import dataLoader.AFTsLoader;
import dataLoader.MapLoader;
import dataLoader.Paths;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Lattice;

/**
 * @author Mohamed Byari
 *
 */

public class OpenTabs {

	MapLoader M;
	public static VBox vbox = new VBox();
	public static ChoiceBox<String> choiceScenario;
	public static ChoiceBox<String> year;

	public OpenTabs() {
		FxMain.imageView.setVisible(false);
		M = new MapLoader();
		senarioPane();

	}

	public void senarioPane() {
		TabPane tabPane = new TabPane();

		WarningWindowes.showWaitingDialog(x -> {
			tabPane.setStyle(" -fx-base: #ffffff;");
			tabPane.getTabs().clear();
			FxMain.root.getChildren().clear();
			AFTsLoader.aftReSet.clear();
			PathTools.writePathRecentProject("RecentProject.txt", "\n" + Paths.getProjectPath());
			M.loadCapitalsAndServiceList();
			AFTsLoader.initializeAFTs();
			M.loadMap();
			M.loadGisData();
			Lattice.plotCells();
			tabPane.getTabs().addAll(GlobalView.globaldataview(), new DataDisplay(M).colorWorld(),
					new AFT_Configuration().pane(), new RunPane(M).pane(), new OutPutPane(M).pane());
			for (Tab tab : tabPane.getTabs()) {
				tab.setClosable(false);
			}
		});
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
				if(DataDisplay.tab.isSelected()) {
				for (int i = 0; i < Lattice.getCapitalsName().size()+1; i++) {
					if (DataDisplay.radioColor[i].isSelected()) {
						if (i < Lattice.getCapitalsName().size()) {
							Lattice.colorMap(Lattice.getCapitalsName().get(i));
							DataDisplay.histogrameCapitals(DataDisplay.histogrameCapitalFequency, Paths.getCurrentYear() + "",
									Lattice.getCapitalsName().get(i));
						} else {
							Lattice.colorMap("FR");
						}
					}
				}
				}

			}
		});
		
		choiceScenario.setOnAction(e -> {
			Paths.setScenario(choiceScenario.getValue());
			MapLoader.updateDemand();// = CsvTools.csvReader(Path.fileFilter(Path.scenario, "demand").get(0));
			Parent par = DataDisplay.graphDemand.getParent();
			((Pane)par).getChildren().remove(DataDisplay.graphDemand);
			DataDisplay.graphDemand = DataDisplay.graphDemand();
			((Pane)par).getChildren().add(DataDisplay.graphDemand);
			year.fireEvent(e);
			AFTsLoader.updateAFTs();
			//AFT_Configuration.choiceAgnet.fireEvent(e);
			Paths.setScenario(choiceScenario.getValue());
			
		});

		HBox hbox = new HBox(new Text(" Scenario = "), choiceScenario, new Text("Current Year = "), year);

		return hbox;
	}

}
