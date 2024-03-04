package fxmlControllers;

import javafx.fxml.FXML;

import javafx.scene.control.TextField;
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
		System.out.println(Paths.getAllfilesPathInData());

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
