package fxmlControllers;

import javafx.fxml.FXML;

import javafx.scene.control.TextField;
import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.Paths;
import javafx.event.ActionEvent;
import javafx.scene.control.Slider;
import javafx.scene.control.CheckBox;

public class RunCofigController {
	@FXML
	private CheckBox removeNegative;
	@FXML
	private CheckBox gUP;
	@FXML
	private CheckBox mutationM;
	@FXML
	private Slider cellsPersS;
	@FXML
	private TextField CellPersT;
	@FXML
	private CheckBox MapSync;
	@FXML
	private CheckBox chartSync;
	@FXML
	private CheckBox creatCSV;
//	@FXML
//	private CheckBox neighboursCollaboration;
	@FXML
	private Slider MapSync_GapS;
	@FXML
	private TextField MapSync_GapT;
	@FXML
	private Slider chartSync_GapS;
	@FXML
	private TextField chartSync_GapT;
	@FXML
	private TextField CSV_GapT;
	@FXML
	private Slider CSV_GapS;

	static public ModelRunnerController CA;

//	mutationInterval.setValue(CA.R.mutationIntval );
//	mutationInterval.valueProperty().addListener((ov, oldval, newval) -> {
//		CA.R.mutationIntval = mutationInterval.getValue();
//	});

	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		removeNegative.setSelected(CA.R.removeNegative);
		MapSync.setSelected(CA.R.mapSynchronisation);
		creatCSV.setSelected(CA.R.writeCsvFiles);
		gUP.setSelected(CA.R.usegiveUp);
		mutationM.setSelected(CA.R.isMutated);
		// neighboursCollaboration.setSelected(CA.R.NeighboorEffect);
		chartSync.setSelected(ModelRunnerController.chartSynchronisation);
		cellsPersS.setValue(CA.R.percentageCells * 100);
		CellPersT.setText((int) cellsPersS.getValue() + "");
		cellsPersS.valueProperty().addListener((ov, oldval, newval) -> {
			CA.R.percentageCells = cellsPersS.getValue() / 100;
			CellPersT.setText((int) cellsPersS.getValue() + "");
		});

		MapSync_GapS.setValue(CA.R.mapSynchronisationGap);
		MapSync_GapT.setText((int) MapSync_GapS.getValue() + "");
		MapSync_GapS.valueProperty().addListener((ov, oldval, newval) -> {
			CA.R.mapSynchronisationGap = (int) MapSync_GapS.getValue();
			MapSync_GapT.setText((int) MapSync_GapS.getValue() + "");
		});

		chartSync_GapS.setValue(ModelRunnerController.chartSynchronisationGap);
		chartSync_GapT.setText((int) chartSync_GapS.getValue() + "");
		chartSync_GapS.valueProperty().addListener((ov, oldval, newval) -> {
			ModelRunnerController.chartSynchronisationGap = (int) chartSync_GapS.getValue();
			chartSync_GapT.setText((int) chartSync_GapS.getValue() + "");
		});
		CSV_GapS.setValue(CA.R.writeCsvFilesGap);
		CSV_GapT.setText((int) CSV_GapS.getValue() + "");
		CSV_GapS.valueProperty().addListener((ov, oldval, newval) -> {
			CA.R.writeCsvFilesGap = (int) CSV_GapS.getValue();
			CSV_GapT.setText((int) CSV_GapS.getValue() + "");
		});
	}

	// Event Listener on CheckBox[#removeNegative].onAction
	@FXML
	public void removeNegativeMarginal(ActionEvent event) {
		CA.R.removeNegative = removeNegative.isSelected();
	}

	// Event Listener on CheckBox[#MapSync].onAction
	@FXML
	public void mapSyn(ActionEvent event) {
		CA.R.mapSynchronisation = MapSync.isSelected();
	}

	// Event Listener on CheckBox[#gUP].onAction
	@FXML
	public void giveUpMechanisme(ActionEvent event) {
		CA.R.usegiveUp = gUP.isSelected();

		/***/
		// CorrelationChiSquare.ceartCorelationMatrix(CA.M);
		// System.out.println(Paths.getAllfilesPathInData());
		for (int i = 0; i < 10; i++) {
			long startTime = System.currentTimeMillis();
			CsvTools.exportSetToCSV("C:\\Users\\byari-m\\Desktop\\folder\\Parallel"+i+".csv");
			long endTime = System.currentTimeMillis();
			long delayForNextTick = endTime - startTime;
			long startTime2 = System.currentTimeMillis();
			CsvTools.exportToCSV("C:\\Users\\byari-m\\Desktop\\folder\\NotParallel"+i+".csv");
			long endTime2 = System.currentTimeMillis();
			long delayForNextTick2 = endTime2 - startTime2;
			long portion = delayForNextTick2 / delayForNextTick;
			System.out.println("P= " + delayForNextTick + " notP= " + delayForNextTick2 + "  == " + portion);
		}
	}

	// Event Listener on CheckBox.onAction
//	@FXML
//	public void NeighboursCollaboration(ActionEvent event) {
//		CA.R.NeighboorEffect = neighboursCollaboration.isSelected();
//	}
	// Event Listener on CheckBox[#mutationM].onAction
	@FXML
	public void MutationMechanism(ActionEvent event) {
		CA.R.isMutated = mutationM.isSelected();
	}

	// Event Listener on TextField[#CellPersT].onAction
	@FXML
	public void cellspersT(ActionEvent event) {
		CA.R.percentageCells = Tools.sToD(CellPersT.getText()) / 100;
		cellsPersS.setValue((int) Tools.sToD(CellPersT.getText()));
	}

	@FXML
	public void mapSync_GapAction(ActionEvent event) {
		// CA.R.percentageCells = Tools.sToD(MapSync_GapT.getText()) / 100;
		// //MapSync_GapT //MapSync_GapS
		MapSync_GapS.setValue((int) Tools.sToD(MapSync_GapT.getText()));
	}

	@FXML
	public void chartSyncAction(ActionEvent event) {
		// CA.R.percentageCells = Tools.sToD(chartSync_GapT.getText()) /
		// 100;//chartSync_GapT//chartSync_GapS
		chartSync_GapS.setValue((int) Tools.sToD(chartSync_GapT.getText()));
	}

	@FXML
	public void CSVAction(ActionEvent event) {
		// CA.R.percentageCells = Tools.sToD(chartSync_GapT.getText()) /
		// 100;//chartSync_GapT//chartSync_GapS
		CSV_GapS.setValue((int) Tools.sToD(CSV_GapT.getText()));
	}

	// Event Listener on CheckBox[#chartSync].onAction
	@FXML
	public void chartSyn(ActionEvent event) {
		ModelRunnerController.chartSynchronisation = chartSync.isSelected();
	}

	// Event Listener on CheckBox[#creatCSV].onAction
	@FXML
	public void creatCSV(ActionEvent event) {
		CA.R.writeCsvFiles = creatCSV.isSelected();

	}
}
