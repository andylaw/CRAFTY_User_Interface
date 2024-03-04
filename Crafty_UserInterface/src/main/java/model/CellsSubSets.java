package model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import controllers.NewRegion_Controller;
import javafx.scene.paint.Color;

public class CellsSubSets {

	public static void actionInNeighboorSameLabel(Cell c) {
		if (c.getOwner() != null) {
			AtomicInteger sum = new AtomicInteger();
			neighborhoodOnAction(c, vc -> {
				if (vc.owner != null)
					if (vc.owner.getLabel().equals(c.owner.getLabel())) {
						sum.getAndIncrement();
					}
			});
			c.owner.setGiveInMean(c.owner.getGiveInMean() + sum.get());

//			owner.productivityLevel.forEach((n, v) -> {
//				owner.productivityLevel.put(n, v +  (sum.get()));
//			});
		}
	}

	static void neighborhoodOnAction(Cell c, Consumer<Cell> action) {
		getMooreNeighborhood(c).forEach(c0 -> {
			action.accept(c0);
		});
	}

	static Set<Cell> getMooreNeighborhood(Cell c) {
		Set<Cell> neighborhood = new HashSet<>();
		for (int i = (c.x - 1); i <= c.x + 1; i++) {
			for (int j = (c.y - 1); j <= (c.y) + 1; j++) {
				if (CellsSet.getCellsSet().getCell(i, j) != null) {
					neighborhood.add(CellsSet.getCellsSet().getCell(i, j));
				}
			}
		}
		neighborhood.remove(CellsSet.getCellsSet().getCell(c.x, c.y));

		return neighborhood;
	}

	public static void selectZone(Cell patch, String zonetype) {

		CellsSet.getCells().forEach(p -> {
			if (p.getGisNameValue().get(zonetype) != null) {
				if (p.getGisNameValue().get(zonetype).equals(patch.getGisNameValue().get(zonetype))) {
					p.ColorP(p.color);
					NewRegion_Controller.patchsInRergion.add(p);
				}
			}
		});
		NewRegion_Controller.patchsInRergion.forEach(p -> {
			p.ColorP(Color.BLACK);
		});
	}

}
