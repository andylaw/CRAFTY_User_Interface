package fxmlControllers;

import javafx.fxml.FXML;

import javafx.scene.control.TextField;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.AFTsLoader;
import javafx.event.ActionEvent;

import javafx.scene.control.Slider;

import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import main.CorrelationChiSquare;
import model.CellsSet;
import model.MaskRestrictions;
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
		System.out.println("initialize "+getClass().getSimpleName());
		removeNegative.setSelected(CA.R.removeNegative);
		MapSync.setSelected(CA.R.mapSynchronisation);
		creatCSV.setSelected(CA.R.writeCsvFiles);
		gUP.setSelected(CA.R.usegiveUp);
		mutationM.setSelected(CA.R.isMutated);
	//	neighboursCollaboration.setSelected(CA.R.NeighboorEffect);
		chartSync.setSelected(ModelRunnerController.chartSynchronisation);
		cellsPersS.setValue(CA.R.percentageCells *100);
		
		cellsPersS.valueProperty().addListener((ov, oldval, newval) -> {
			CA.R.percentageCells = cellsPersS.getValue()/100;
			CellPersT.setText((int)cellsPersS.getValue()+"");
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
	//	new MaskRestrictions().setToMaskInitialisation("C:\\Users\\byari-m\\Documents\\Data\\Scotland\\worlds\\LandUseControl\\UrbanMask\\SSP1\\UrbanMask_SSP1_2070.csv");
	/***/
	//CorrelationChiSquare.ceartCorelationMatrix(CA.M);
		/***/
//		for (int i = 0; i < 100; i++) {
//		long start=System.nanoTime();
//		CA.M .parallelStream() .forEach(c -> {
//	    	c.setIndex(c.getIndex()+1);
//	        c.setOwner(null);
//	    });	
//		long end=System.nanoTime()-start;
//		System.out.println("Time in miliscond->  "+end/1_000+"mics");
//		}
//		CA.M.forEach(c->{
//			if(c.getOwner()==null) {c.ColorP(Color.RED);}
//		});
		
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
		CA.R.percentageCells = Tools.sToD(CellPersT.getText())/100;
		cellsPersS.setValue((int)Tools.sToD(CellPersT.getText()));
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
