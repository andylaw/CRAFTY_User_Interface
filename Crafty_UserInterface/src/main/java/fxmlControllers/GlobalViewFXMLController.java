package fxmlControllers;

import java.util.HashSet;
import java.util.Set;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.CSVTableView;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

public class GlobalViewFXMLController {
	@FXML
	private TableView<ObservableList<String>> TablCapitals;
	@FXML
	private TableView<ObservableList<String>> TablServices;
	@FXML
	private TableView<ObservableList<String>> TabScenarios;
	@FXML
	private TableView<ObservableList<String>> TablAFTs;
	@FXML
	VBox vbox;

	public void initialize() {

		initilaseTabls();

	}

	void initilaseTabls() {
		// Paths.initialisation("C:\\Users\\byari-m\\Documents\\Data\\data_EUpaper_nocsv");
		System.out.println("initialize " + getClass().getSimpleName());
		CSVTableView.updateTableView(CsvTools.csvReader(PathTools.fileFilter("\\AFTsMetaData.csv").get(0)), null,
				TablAFTs);
		CSVTableView.updateTableView(CsvTools.csvReader(PathTools.fileFilter("\\Capitals.csv").get(0)), null,
				TablCapitals);
		CSVTableView.updateTableView(CsvTools.csvReader(PathTools.fileFilter("\\Services.csv").get(0)), null,
				TablServices);
		CSVTableView.updateTableView(CsvTools.csvReader(PathTools.fileFilter("\\scenarios.csv").get(0)), null,
				TabScenarios);
		Set<TitledPane> panes = new HashSet<>();

		vbox.getChildren().forEach(node -> {
			if (node instanceof TitledPane) {
//				((TitledPane)node).setExpanded(false);
				panes.add(((TitledPane) node));
			}
		});
	}
	
	void closeTitelPanes(Set<TitledPane> panes) {
		panes.forEach(p->{
			
		});
	}

}
