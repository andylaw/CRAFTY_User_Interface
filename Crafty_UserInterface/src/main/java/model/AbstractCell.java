package model;

import java.util.HashMap;

import javafx.scene.paint.Color;
/**
 * @author Mohamed Byari
 *
 */
public class AbstractCell {
	 static int size=1;
	 int index;
	 int x, y;
	 HashMap<String, Double> capitals = new HashMap<>();
	 HashMap<String, Double> services = new HashMap<>();
	 HashMap<String, String> GisNameValue = new HashMap<>();//
	 Manager owner;
	 double tmpValueCell=0;// a changer
	 Color color = Color.TRANSPARENT;
	 
	 
		public int getX() {
			return x;
		}


		public void setX(int x) {
			this.x = x;
		}


		public int getY() {
			return y;
		}


		public void setY(int y) {
			this.y = y;
		}


		public Manager getOwner() {
			return owner;
		}


		public void setOwner(Manager owner) {
			this.owner = owner;
		}


		public double getTmpValueCell() {
			return tmpValueCell;
		}


		public void setTmpValueCell(double tmpValueCell) {
			this.tmpValueCell = tmpValueCell;
		}


		public HashMap<String, Double> getCapitals() {
			return capitals;
		}


		public HashMap<String, Double> getServices() {
			return services;
		}


		public HashMap<String, String> getGisNameValue() {
			return GisNameValue;
		}


		public static int getSize() {
			return size;
		}


		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
}
