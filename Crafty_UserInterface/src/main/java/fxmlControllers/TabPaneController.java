package fxmlControllers;

import java.util.ArrayList;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.GraphicConsol;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import main.FxMain;
import model.CellsSet;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;

public class TabPaneController {
	@FXML
	private VBox vbox;
	@FXML
	private ChoiceBox<String> scenarioschoice;
	@FXML
	private ChoiceBox<String> yearchoice;
	@FXML
	private TabPane tabpane;
	@FXML
	private TitledPane mapTitelPane;
	@FXML
	private Tab dataPane;
//	@FXML
//	private TextArea consoleArea;

	public static CellsLoader M = new CellsLoader();
	private boolean isNotInitialsation = false;

	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		mapTitelPane.setContent(FxMain.subScene);
		PathTools.writePathRecentProject("RecentProject.txt", "\n" + Paths.getProjectPath());
		scenarioschoice.getItems().addAll(Paths.getScenariosList());
		scenarioschoice.setValue(Paths.getScenario());
		ArrayList<String> listYears = new ArrayList<>();
		for (int i = Paths.getStartYear(); i < Paths.getEndtYear(); i++) {
			listYears.add(i + "");
		}
		yearchoice.getItems().addAll(listYears);
		yearchoice.setValue(listYears.get(0));
		isNotInitialsation = true;
//	    FxMain.subScene.setWidth(Screen.getPrimary().getBounds().getWidth()-tabpane.getWidth());
//		mapTitelPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
//          if (isNowExpanded) {
//        	  FxMain.subScene.setWidth(Screen.getPrimary().getBounds().getWidth()-tabpane.getWidth());
//          } else {
//        	  FxMain.subScene.setWidth(0);
//          }
//      });
//		 GraphicConsol.start(consoleArea);
	}

	@FXML
	public void scenarioschoice() {
		if (isNotInitialsation) {
			Paths.setScenario(scenarioschoice.getValue());
			CellsLoader.updateDemand();// = CsvTools.csvReader(Path.fileFilter(Path.scenario, "demand").get(0));
			LineChart<Number, Number> chart = SpatialDataController.getInstance().getDemandsChart();
			new LineChartTools().lineChart(M, (Pane) chart.getParent(), chart, CellsSet.getDemand());
			yearchoice();
			M.AFtsSet.updateAFTs();
			// AFTsConfigurationController.choiceAgnetSetOnAction();
		}
	}

	@FXML
	public void yearchoice() {
		if (isNotInitialsation) {
			if (yearchoice.getValue() != null) {
				Paths.setCurrentYear((int) Tools.sToD(yearchoice.getValue()));
				M.updateCapitals(Paths.getCurrentYear());
				if (dataPane.isSelected()) {
					for (int i = 0; i < CellsSet.getCapitalsName().size() + 1; i++) {
						if (SpatialDataController.radioColor[i].isSelected()) {
							if (i < CellsSet.getCapitalsName().size()) {
								CellsSet.colorMap(CellsSet.getCapitalsName().get(i));
								SpatialDataController.getInstance().histogrameCapitals(Paths.getCurrentYear() + "",
										CellsSet.getCapitalsName().get(i));
							} else {
								CellsSet.colorMap("FR");
							}
						}
					}
				}
			}
		}
	}
}
