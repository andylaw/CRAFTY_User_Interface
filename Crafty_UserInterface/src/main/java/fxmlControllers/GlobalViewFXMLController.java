package fxmlControllers;


import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.CSVTableView;
import dataLoader.Paths;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class GlobalViewFXMLController {
	@FXML
	private TableView<ObservableList<String>> TablCapitals;
	@FXML
	private TableView<ObservableList<String>> TablServices;
	@FXML
	private TableView<ObservableList<String>> TabScenarios;
	@FXML
	private TableView<ObservableList<String>> TablAFTs;
	
	
    public void initialize() {

    	initilaseTabls();
        
    }
    void initilaseTabls() {
    	//Paths.initialisation("C:\\Users\\byari-m\\Documents\\Data\\data_EUpaper_nocsv");
    	System.out.println("initialize "+getClass().getSimpleName());
    	
    	CSVTableView.updateTableView(CsvTools.csvReader(PathTools.fileFilter("\\Capitals.csv").get(0)), null, TablCapitals);
    	CSVTableView.updateTableView(CsvTools.csvReader(PathTools.fileFilter("\\Services.csv").get(0)), null, TablServices);
    	CSVTableView.updateTableView(CsvTools.csvReader(PathTools.fileFilter("\\scenarios.csv").get(0)), null, TabScenarios);
    	CSVTableView.updateTableView(CsvTools.csvReader(PathTools.fileFilter("\\AFTsMetaData.csv").get(0)), null, TablAFTs);
    	
    }


}
