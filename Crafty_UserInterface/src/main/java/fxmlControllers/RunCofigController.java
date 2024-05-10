package fxmlControllers;

import javafx.fxml.FXML;

import javafx.scene.control.TextField;
import UtilitiesFx.graphicalTools.Tools;
import javafx.event.ActionEvent;
import javafx.scene.control.Slider;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;

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
	@FXML
	private CheckBox isAveragedPerCellResidualDemand;
	@FXML
	private RadioButton withBestAFT;
	@FXML
	private RadioButton withRandomAFT;
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
		CellPersT.setText( Math.round(cellsPersS.getValue() * 10) / 10.+ "");
		cellsPersS.valueProperty().addListener((ov, oldval, newval) -> {
			CA.R.percentageCells = cellsPersS.getValue() / 100;
			CellPersT.setText(Math.round(cellsPersS.getValue() * 10) / 10.  + ""); //  ;


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
	}

	@FXML
	public void averagedPerCellResidualDemand(ActionEvent event) {
		CA.R.isAveragedPerCellResidualDemand = isAveragedPerCellResidualDemand.isSelected();
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

	@FXML
	public void bestAFT(ActionEvent event) {
		CA.R.withBestAFT = withBestAFT.isSelected();
		if (withBestAFT.isSelected()) {
			withRandomAFT.setSelected(false);
		} else {
			withRandomAFT.setSelected(true);
		}
	}

	@FXML
	public void randomAFT(ActionEvent event) {
		CA.R.withBestAFT = !withRandomAFT.isSelected();
		if (withRandomAFT.isSelected()) {
			withBestAFT.setSelected(false);
		} else {
			withBestAFT.setSelected(true);
		}

	}

	// Event Listener on TextField[#CellPersT].onAction
	@FXML
	public void cellspersT(ActionEvent event) {
		CA.R.percentageCells = Tools.sToD(CellPersT.getText()) / 100;
		cellsPersS.setValue((int) Tools.sToD(CellPersT.getText()));
	}

	@FXML
	public void mapSync_GapAction(ActionEvent event) {
		MapSync_GapS.setValue((int) Tools.sToD(MapSync_GapT.getText()));
	}

	@FXML
	public void chartSyncAction(ActionEvent event) {
		chartSync_GapS.setValue((int) Tools.sToD(chartSync_GapT.getText()));
	}

	@FXML
	public void CSVAction(ActionEvent event) {
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
