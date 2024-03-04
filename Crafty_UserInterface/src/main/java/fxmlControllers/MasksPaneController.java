package fxmlControllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.MaskRestrictionDataLoader;
import eu.hansolo.fx.charts.CircularPlot;
import eu.hansolo.fx.charts.CircularPlotBuilder;
import eu.hansolo.fx.charts.data.PlotItem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import model.CellsSet;

public class MasksPaneController {
	@FXML
	private VBox boxMaskTypes;
	@FXML
	ScrollPane scroll;
	MaskRestrictionDataLoader Maskloader = new MaskRestrictionDataLoader();
	ArrayList<CheckBox> radioList = new ArrayList<>();
	// cell.getMaskTyp->hash(owner_competitor-> true or false)
	public static HashMap<String,HashMap<String,Boolean>> restrictions= new HashMap<>();

	public void initialize() {
		scroll.setPrefHeight(Screen.getPrimary().getBounds().getHeight() * .9);
		Maskloader.MaskAndRistrictionLaoder();
		MaskRestrictionDataLoader.ListOfMask.keySet().forEach(n -> {
			CheckBox r = new CheckBox(n);
			radioList.add(r);
			boxMaskTypes.getChildren().add(r);
		});
		CircularPlot[] circularPlot = new CircularPlot[radioList.size()];
		TitledPane[] T = new TitledPane[radioList.size()];
		radioList.forEach(r -> {
			r.setOnAction(e -> {
				int i = radioList.indexOf(r);
				if (r.isSelected()) {
					ChoiceBox<String> AFTChoisButton = new ChoiceBox<>();
					Tools.choiceBox(AFTChoisButton, new ArrayList<>(TabPaneController.M.AFtsSet.getAftHash().keySet()));
					Maskloader.CellSetToMaskLoader(r.getText());
					HashMap<String, Boolean> restrictionsRul = Maskloader.restrictionsRulsUpload(r.getText());
					restrictions.put(r.getText(), restrictionsRul);
					List<PlotItem> items = circularPlot(restrictionsRul, AFTChoisButton.getValue());
					circularPlot[i] = CircularPlotBuilder.create().items(items).decimals(0).minorTickMarksVisible(false).build();
					VBox boxMask = new VBox();
					T[i] = Tools.T("Possible Interactions for " + r.getText() + " Restriction ", true, boxMask);
					Text text = Tools.text("Select an AFT to display the possible interactions:", Color.BLUE);
					boxMask.getChildren().addAll(Tools.hBox(text, AFTChoisButton), circularPlot[i]);
					AFTChoisButton.setOnAction(action -> {
						circularPlot[i].getItems().clear();
						circularPlot[i].setItems(circularPlot(restrictionsRul, AFTChoisButton.getValue()));
					});
					MousePressed.mouseControle(boxMask, circularPlot[i]);
					int place = boxMaskTypes.getChildren().indexOf(r) + 1;
					boxMaskTypes.getChildren().add(place, T[i]);
				} else {
					CellsSet.getCells().forEach(c -> {
						if (c.getMaskType() != null && c.getMaskType().equals(r.getText()))
							c.setMaskType(null);
					});
					restrictions.remove(r.getText());
					boxMaskTypes.getChildren().removeAll(T[i]);

				}
				CellsSet.colorMap("Mask");
				System.out.println(restrictions.keySet());
			});

		});

	}

	// Event Listener on Button[#handButton].onAction
	@FXML
	public void clear(ActionEvent event) {
//		CellsSet.getCellsSet().forEach(c -> {
//			c.setMaskType(null);
//		});
		CellsSet.colorMap("Mask");
		radioList.forEach(r -> {
			r.setSelected(false);
			r.fireEvent(event);
		});
	}

	private List<PlotItem> circularPlot(HashMap<String, Boolean> restrictions, String ow) {
		ArrayList<PlotItem> itemsList = new ArrayList<>();
		TabPaneController.M.AFtsSet.forEach(a -> {
			itemsList.add(new PlotItem(a.getLabel(), 10, a.getColor()));
		});
		// itemsList.forEach(owner -> {});

		PlotItem own = null;
		for (Iterator<PlotItem> iterator = itemsList.iterator(); iterator.hasNext();) {
			PlotItem plotItem = (PlotItem) iterator.next();
			if (plotItem.getName().equals(ow)) {
				own = plotItem;
				break;
			}
		}
		PlotItem owner = own;
		itemsList.forEach(competitor -> {
			int nbr = restrictions.get(owner.getName() + "_" + competitor.getName()) ? 1 : -1;
			owner.addToOutgoing(competitor, nbr);
		});

		PlotItem[] its = new PlotItem[itemsList.size()];
		for (int i = 0; i < its.length; i++) {
			its[i] = itemsList.get(i);
		}
		List<PlotItem> items = List.of(its);

		return items;
	}

}
