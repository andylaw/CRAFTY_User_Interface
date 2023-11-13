package panes;

import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.Tools;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * @author Mohamed Byari
 *
 */

public class RunConfiguration {

	public static void runConfiguration(RunPane CA,NewWindow runConfiguration) {
		RadioButton removeNegative = new RadioButton("Remove negative Marginal utility");
		RadioButton mapSynchronisation = new RadioButton("Map Synchronisation");
		RadioButton writeCSV = new RadioButton("Creat .csv output files");
		RadioButton isMutated = new RadioButton("Mutation mechanism");
		RadioButton NeighboorEffect = new RadioButton("Neighbours collaboration");
		RadioButton useGiveUp = new RadioButton("Give Up mechanism");
		Slider mutationInterval = Tools.slider(0, 1, 0.5);
		Slider percentageCells = Tools.slider(0, 100, 30);
		
		RadioButton chartSynchronisation = new RadioButton("Chart Synchronisation");
		removeNegative.setSelected(CA.R.removeNegative);
		removeNegative.setOnAction(s -> { 
			CA.R.removeNegative = removeNegative.isSelected();
		});
		mapSynchronisation.setSelected(CA.R.mapSynchronisation);
		mapSynchronisation.setOnAction(s -> {
			CA.R.mapSynchronisation = mapSynchronisation.isSelected();
		});
		writeCSV.setSelected(CA.R.writeCsvFiles);
		writeCSV.setOnAction(s -> {
			CA.R.writeCsvFiles = writeCSV.isSelected();
		});
		useGiveUp.setSelected(CA.R.usegiveUp);
		useGiveUp.setOnAction(s -> {
			CA.R.usegiveUp = useGiveUp.isSelected();
		});
		isMutated.setSelected(CA.R.isMutated);
		isMutated.setOnAction(s -> {
			CA.R.isMutated = isMutated.isSelected();
		});
		NeighboorEffect.setSelected(CA.R.NeighboorEffect);
		NeighboorEffect.setOnAction(s -> {
			CA.R.NeighboorEffect = NeighboorEffect.isSelected();
		});
		chartSynchronisation.setSelected(RunPane.chartSynchronisation);
		chartSynchronisation.setOnAction(s -> {
			RunPane.chartSynchronisation = chartSynchronisation.isSelected();
		});
		mutationInterval.setValue(CA.R.mutationIntval );
		mutationInterval.valueProperty().addListener((ov, oldval, newval) -> {
			CA.R.mutationIntval = mutationInterval.getValue();
		});
		
		percentageCells.setValue(CA.R.percentageCells *100);
		Text percentageCellsText = new Text(CA.R.percentageCells*100+"");
		percentageCells.valueProperty().addListener((ov, oldval, newval) -> {
			CA.R.percentageCells = percentageCells.getValue()/100;
			percentageCellsText.setText((int)percentageCells.getValue()+"%");
		});
		HBox hPercentageCells= Tools.hBox(new Text("Percentage Cells "),percentageCells,percentageCellsText);
		
		GridPane gridRadioButton = new GridPane();
		TitledPane T1 = Tools.T("Mechanismes", true, removeNegative,useGiveUp,NeighboorEffect,isMutated,hPercentageCells);
		TitledPane T2 = Tools.T("Synchronisation", true, mapSynchronisation,chartSynchronisation);
		TitledPane T3 = Tools.T("OutPut files", true, writeCSV/* Tools.NodeWithAltern(, "") */ );
		gridRadioButton.add(T1, 0, 0);
		//gridRadioButton.add(mutationInterval, 0, 4);
		gridRadioButton.add(T2, 0, 1);
		gridRadioButton.add(T3, 0, 2);

		
		runConfiguration.creatwindows("Run Configuration", gridRadioButton);

		
		//return runConfiguration;
	}
}
