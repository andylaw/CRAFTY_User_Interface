package fxmlControllers;

import javafx.fxml.FXML;

import javafx.scene.control.TextField;
import model.ModelRunner;
import UtilitiesFx.graphicalTools.Tools;
import javafx.event.ActionEvent;
import javafx.scene.control.Slider;
import javafx.scene.control.CheckBox;

public class RunCofigController {

	@FXML
	private CheckBox InitialEquilibrium;
	@FXML
	private CheckBox removeNegative;
	@FXML
	private CheckBox gUP;
	@FXML
	private CheckBox neighbours;
	@FXML
	private Slider NeighbourRadiusS;
	@FXML
	private TextField NeighbourRadiusT;
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
	private Slider BestAftS;
	@FXML
	private TextField BestAftT;
	@FXML
	private Slider RandomAftS;
	@FXML
	private TextField RandomAftT;
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
	@FXML
	private Slider nbrOfSubSetS;
	@FXML
	private TextField nbrOfSubSetT;
	@FXML
	private Slider percentageOfGiveUpS;
	@FXML
	private TextField percentageOfGiveUpT;
	@FXML
	private CheckBox traker;

	static public ModelRunnerController CA;

//	mutationInterval.setValue(CA.R.mutationIntval );
//	mutationInterval.valueProperty().addListener((ov, oldval, newval) -> {
//		CA.R.mutationIntval = mutationInterval.getValue();
//	});

	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		InitialEquilibrium.setSelected(ModelRunner.initial_demand_supply_equilibrium);
		removeNegative.setSelected(ModelRunner.remove_negative_marginal_utility);
		MapSync.setSelected(ModelRunner.mapSynchronisation);
		neighbours.setSelected(ModelRunner.use_neighbor_priority);
		creatCSV.setSelected(ModelRunner.generate_csv_files);
		gUP.setSelected(ModelRunner.use_abandonment_threshold);
//		mutationM.setSelected(ModelRunner.isMutated);
		// neighboursCollaboration.setSelected(CA.R.NeighboorEffect);
		chartSync.setSelected(ModelRunnerController.chartSynchronisation);

		cellsPersS.setValue(ModelRunner.participating_cells_percentage * 100);
		CellPersT.setText(Math.round(cellsPersS.getValue() * 10) / 10. + "");
		cellsPersS.valueProperty().addListener((ov, oldval, newval) -> {
			ModelRunner.participating_cells_percentage = cellsPersS.getValue() / 100;
			CellPersT.setText(Math.round(cellsPersS.getValue() * 10) / 10. + ""); // ;
		});

		MapSync_GapS.setValue(ModelRunner.mapSynchronisationGap);
		MapSync_GapT.setText((int) MapSync_GapS.getValue() + "");
		MapSync_GapS.valueProperty().addListener((ov, oldval, newval) -> {
			ModelRunner.mapSynchronisationGap = (int) MapSync_GapS.getValue();
			MapSync_GapT.setText((int) MapSync_GapS.getValue() + "");
		});

		chartSync_GapS.setValue(ModelRunnerController.chartSynchronisationGap);
		chartSync_GapT.setText((int) chartSync_GapS.getValue() + "");
		chartSync_GapS.valueProperty().addListener((ov, oldval, newval) -> {
			ModelRunnerController.chartSynchronisationGap = (int) chartSync_GapS.getValue();
			chartSync_GapT.setText((int) chartSync_GapS.getValue() + "");
		});
		CSV_GapS.setValue(ModelRunner.csv_output_frequency);
		CSV_GapT.setText((int) CSV_GapS.getValue() + "");
		CSV_GapS.valueProperty().addListener((ov, oldval, newval) -> {
			ModelRunner.csv_output_frequency = (int) CSV_GapS.getValue();
			CSV_GapT.setText((int) CSV_GapS.getValue() + "");
		});
//		nbrOfSubSetS.setValue(ModelRunner.nbrOfSubSet);
//		nbrOfSubSetT.setText((int) nbrOfSubSetS.getValue() + "");
//		nbrOfSubSetS.valueProperty().addListener((ov, oldval, newval) -> {
//			ModelRunner.nbrOfSubSet = (int) nbrOfSubSetS.getValue();
//			nbrOfSubSetT.setText((int) nbrOfSubSetS.getValue() + "");
//		});
		NeighbourRadiusS.setValue(ModelRunner.neighbor_radius);
		NeighbourRadiusT.setText((int) NeighbourRadiusS.getValue() + "");
		NeighbourRadiusS.valueProperty().addListener((ov, oldval, newval) -> {
			ModelRunner.neighbor_radius = (int) NeighbourRadiusS.getValue();
			NeighbourRadiusT.setText((int) NeighbourRadiusS.getValue() + "");
		});

		percentageOfGiveUpS.setValue(ModelRunner.land_abandonment_percentage * 100);
		percentageOfGiveUpT.setText(Math.round(percentageOfGiveUpS.getValue() * 10) / 10. + "");
		percentageOfGiveUpS.valueProperty().addListener((ov, oldval, newval) -> {
			ModelRunner.land_abandonment_percentage = percentageOfGiveUpS.getValue() / 100;
			percentageOfGiveUpT.setText(Math.round(percentageOfGiveUpS.getValue() * 10) / 10. + ""); // ;
		});

		BestAftS.setValue(ModelRunner.MostCompetitorAFTProbability * 100);
		BestAftT.setText(Math.round(BestAftS.getValue() * 10) / 10. + "");
		BestAftS.valueProperty().addListener((ov, oldval, newval) -> {
			ModelRunner.MostCompetitorAFTProbability = BestAftS.getValue() / 100;
			BestAftT.setText(Math.round(BestAftS.getValue() * 10) / 10. + "");
			RandomAftT.setText(Math.round(1000 - BestAftS.getValue() * 10) / 10. + "");
			RandomAftS.setValue(Tools.sToD(RandomAftT.getText()));
		});

		RandomAftS.setValue(100 - ModelRunner.MostCompetitorAFTProbability * 100);
		RandomAftT.setText(100 - Math.round(BestAftS.getValue() * 10) / 10. + "");
		RandomAftS.valueProperty().addListener((ov, oldval, newval) -> {
			ModelRunner.MostCompetitorAFTProbability = 1 - RandomAftS.getValue() / 100;
			RandomAftT.setText(Math.round(RandomAftS.getValue() * 10) / 10. + ""); // ;
			BestAftT.setText(Math.round(1000 - RandomAftS.getValue() * 10) / 10. + "");
			BestAftS.setValue(Tools.sToD(BestAftT.getText()));
		});
		traker.setSelected(ModelRunner.track_changes);
	}

	@FXML
	public void initialEquilibrium(ActionEvent event) {
		ModelRunner.initial_demand_supply_equilibrium = InitialEquilibrium.isSelected();
	}

	// Event Listener on CheckBox[#removeNegative].onAction
	@FXML
	public void removeNegativeMarginal(ActionEvent event) {
		ModelRunner.remove_negative_marginal_utility = removeNegative.isSelected();
	}

	// Event Listener on CheckBox[#MapSync].onAction
	@FXML
	public void mapSyn(ActionEvent event) {
		ModelRunner.mapSynchronisation = MapSync.isSelected();
	}

	// Event Listener on CheckBox[#gUP].onAction
	@FXML
	public void giveUpMechanisme(ActionEvent event) {
		ModelRunner.use_abandonment_threshold = gUP.isSelected();

	}

	@FXML
	public void percentageOfGiveUpT(ActionEvent event) {
		ModelRunner.land_abandonment_percentage = Tools.sToD(percentageOfGiveUpT.getText()) / 100;
		percentageOfGiveUpS.setValue((int) Tools.sToD(percentageOfGiveUpT.getText()));

	}

	@FXML
	public void NeighboursAction(ActionEvent event) {
		ModelRunner.use_neighbor_priority = neighbours.isSelected();

		NeighbourRadiusS.setDisable(!neighbours.isSelected());
		NeighbourRadiusT.setDisable(!neighbours.isSelected());

	}

	@FXML
	public void averagedPerCellResidualDemand(ActionEvent event) {
		ModelRunner.averaged_residual_demand_per_cell = isAveragedPerCellResidualDemand.isSelected();
	}

	@FXML
	public void NeighbourRadiusT(ActionEvent event) {
		ModelRunner.neighbor_radius = (int) Tools.sToD(NeighbourRadiusT.getText());
		NeighbourRadiusS.setValue((int) Tools.sToD(NeighbourRadiusT.getText()));
	}

	// Event Listener on CheckBox.onAction
//	@FXML
//	public void NeighboursCollaboration(ActionEvent event) {
//		CA.R.NeighboorEffect = neighboursCollaboration.isSelected();
//	}
	// Event Listener on CheckBox[#mutationM].onAction
	@FXML
	public void MutationMechanism(ActionEvent event) {
		ModelRunner.mutate_on_competition_win = mutationM.isSelected();
	}

	@FXML
	public void BestAftT(ActionEvent event) {
		ModelRunner.MostCompetitorAFTProbability = Tools.sToD(BestAftT.getText()) / 100;
		BestAftS.setValue(Tools.sToD(BestAftT.getText()));
		RandomAftS.setValue(100 - Tools.sToD(BestAftT.getText()));
	}

	@FXML
	public void RandomAftT(ActionEvent event) {
		ModelRunner.MostCompetitorAFTProbability = 1 - Tools.sToD(RandomAftT.getText()) / 100;
		RandomAftS.setValue(Tools.sToD(RandomAftT.getText()));
		BestAftS.setValue(100 - Tools.sToD(RandomAftT.getText()));
	}

	// Event Listener on TextField[#CellPersT].onAction
	@FXML
	public void cellspersT(ActionEvent event) {
		ModelRunner.participating_cells_percentage = Tools.sToD(CellPersT.getText()) / 100;
		cellsPersS.setValue((int) Tools.sToD(CellPersT.getText()));
	}

	@FXML
	public void nbrOfSubSetT(ActionEvent event) {
		ModelRunner.marginal_utility_calculations_per_tick = (int) Tools.sToD(nbrOfSubSetT.getText()) / 100;
		nbrOfSubSetS.setValue((int) Tools.sToD(nbrOfSubSetT.getText()));
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
		ModelRunner.generate_csv_files = creatCSV.isSelected();

	}

	@FXML
	public void trakerAction() {
		ModelRunner.track_changes = traker.isSelected();
	}
}
