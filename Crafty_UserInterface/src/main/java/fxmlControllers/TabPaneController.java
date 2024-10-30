package fxmlControllers;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.LineChartTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.DemandModel;
import dataLoader.MaskRestrictionDataLoader;
import dataLoader.PathsLoader;
import dataLoader.S_WeightLoader;
import dataLoader.ServiceSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import main.FxMain;
import model.CellsSet;
import model.ModelRunner;
import model.RegionClassifier;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;

public class TabPaneController {

	@FXML
	private ChoiceBox<String> scenarioschoice;
	@FXML
	private ChoiceBox<String> yearchoice;
	@FXML
	private TabPane tabpane;
	@FXML
	private VBox mapBox;
	@FXML
	private Tab dataPane;
	@FXML
	CheckBox regionalBox;
//	@FXML
//	private TextArea consoleArea;

	public static CellsLoader M = new CellsLoader();
	private boolean isNotInitialsation = false;

	private static TabPaneController instance;

	public TabPaneController() {
		instance = this;
	}

	public static TabPaneController getInstance() {
		return instance;
	}

	public TabPane getTabpane() {
		return tabpane;
	}

	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		mapBox.getChildren().add(FxMain.subScene);
		PathTools.writePathRecentProject("RecentProject.txt", "\n" + PathsLoader.getProjectPath());
		scenarioschoice.getItems().addAll(PathsLoader.getScenariosList());
		scenarioschoice.setValue(PathsLoader.getScenario());
		ArrayList<String> listYears = new ArrayList<>();
		for (int i = PathsLoader.getStartYear(); i < PathsLoader.getEndtYear(); i++) {
			listYears.add(i + "");
		}
		yearchoice.getItems().addAll(listYears);
		yearchoice.setValue(listYears.get(0));
		isNotInitialsation = true;
		tabpane.setPrefWidth(Screen.getPrimary().getBounds().getWidth() / 1.5);
//	    FxMain.subScene.setWidth(Screen.getPrimary().getBounds().getWidth()-tabpane.getWidth());
//		mapTitelPane.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
//          if (isNowExpanded) {
//        	  FxMain.subScene.setWidth(Screen.getPrimary().getBounds().getWidth()-tabpane.getWidth());
//          } else {
//        	  FxMain.subScene.setWidth(0);
//          }
//      });
//		 GraphicConsol.start(consoleArea);

		regionalBox.setDisable(!CellsLoader.regionalization);

	}

	@FXML
	public void regionalization() {
		RegionClassifier.initialation(regionalBox.isSelected());
		ModelRunner.initializeRegions();
		AFTsLoader.hashAgentNbrRegions();

		AtomicInteger nbr = new AtomicInteger();
		RegionClassifier.regions.values().forEach(hash -> {
			Color color = ColorsTools.colorlist(nbr.getAndIncrement());
			hash.getCells().values().forEach(c -> {
				c.ColorP(color);
			});
		});
		CellsSet.gc.drawImage(CellsSet.writableImage, 0, 0);
		regionalBox.setSelected(CellsLoader.regionsNamesSet.size() > 1);
		// if there is no regionalisation enable to be selected and return a warn
		// make an indecation that you select a region / only here not in the
		// initialsation
		// add some text to explain what is the meaning of regionalisation
	}

	@FXML
	public void scenarioschoice() {
		if (isNotInitialsation) {
			M.loadMap();
			PathsLoader.setScenario(scenarioschoice.getValue());
			// DemandModel.updateDemand();// =
			// CsvTools.csvReader(Path.fileFilter(Path.scenario, "demand").get(0));
			ServiceSet.initialseServices();
//			DemandModel.updateWorldDemand();
			DemandModel.updateRegionsDemand();
			S_WeightLoader.updateWorldWeight();
			S_WeightLoader.updateRegionsWeight();
			LineChart<Number, Number> chart = SpatialDataController.getInstance().getDemandsChart();
			new LineChartTools().lineChart(M, (Pane) chart.getParent(), chart, DemandModel.serialisationWorldDemand());
			M.AFtsSet.updateAFTsForsenario();
			yearchoice();
			MaskRestrictionDataLoader.MaskAndRistrictionLaoderUpdate();
			MasksPaneController.getInstance().clear(new ActionEvent());
			MasksPaneController.initialiseMask();
		}
	}

	@FXML
	public void yearchoice() {
		if (isNotInitialsation) {
			if (yearchoice.getValue() != null) {
				PathsLoader.setCurrentYear((int) Tools.sToD(yearchoice.getValue()));
				M.updateCapitals(PathsLoader.getCurrentYear());
				AFTsLoader.updateAFTs();
				if (dataPane.isSelected()) {
					for (int i = 0; i < CellsLoader.getCapitalsName().size() + 1; i++) {
						if (SpatialDataController.radioColor[i].isSelected()) {
							if (i < CellsLoader.getCapitalsName().size()) {
								CellsSet.colorMap(CellsLoader.getCapitalsName().get(i));
								SpatialDataController.getInstance().histogrameCapitals(
										PathsLoader.getCurrentYear() + "", CellsLoader.getCapitalsName().get(i));
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
