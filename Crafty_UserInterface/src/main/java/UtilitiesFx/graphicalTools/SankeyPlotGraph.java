package UtilitiesFx.graphicalTools;

import java.util.HashMap;

import eu.hansolo.fx.charts.SankeyPlot;
import eu.hansolo.fx.charts.SankeyPlot.StreamFillMode;
import eu.hansolo.fx.charts.SankeyPlotBuilder;
import eu.hansolo.fx.charts.data.PlotItem;
import javafx.scene.paint.Color;
import model.Manager;

public class SankeyPlotGraph {

	public static SankeyPlot sankey;

	public static void AFtsToSankeyPlot(HashMap<Manager, HashMap<Manager, Integer>> h, int level) {
		sankey = SankeyPlotBuilder.create().prefSize(1200, 800).build();
		HashMap<Manager, HashMap<Manager, PlotItem>> hashItems = new HashMap<>();
		HashMap<Manager, PlotItem> senderHash = new HashMap<>();
		HashMap<Manager, PlotItem> plothash = new HashMap<>();

		h.forEach((sender, hash) -> {
			hash.forEach((reciever, value) -> {
				PlotItem plotItem2 = new PlotItem(reciever.getLabel(), reciever.getColor(), level + 1);
				plothash.put(reciever, plotItem2);
			});
			hashItems.put(sender, plothash);
			PlotItem plotItem1 = new PlotItem(sender.getLabel(), sender.getColor(), level);
			senderHash.put(sender, plotItem1);
			sankey.addItem(plotItem1);
		});
		plothash.values().forEach((v) -> {
			sankey.addItem(v);
		});

		// define relation (valus) between items
		h.forEach((sender, hash) -> {
			hash.forEach((reciever, value) -> {
				senderHash.get(sender).addToOutgoing(hashItems.get(sender).get(reciever), value);

			});
		});

		sankey.setStreamFillMode(StreamFillMode.GRADIENT);
		sankey.setSelectionColor(Color.rgb(0, 0, 250, 0.5));
	}

}