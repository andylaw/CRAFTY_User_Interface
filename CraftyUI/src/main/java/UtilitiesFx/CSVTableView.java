package UtilitiesFx;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import TabsPane.Agnets_Configuration;



public final class CSVTableView extends TableView<String> {
	String[] firstRow ;
	String path;
	public Agnets_Configuration pane;
	
	public CSVTableView(File file, double Width, double Height, boolean isEditable) throws IOException {
		this(Files.readAllLines(Paths.get(file.toURI())), Width, Height, isEditable);
		path=file.getPath();
		
	}

	public CSVTableView(String[][] tab, double Width, double Height, boolean isEditable) throws IOException {
		this(tabToLines(tab), Width, Height, isEditable);
	}
	
	public CSVTableView(List<String> lines, double Width, double Height, boolean isEditable) throws IOException {
		
		firstRow = lines.get(0).split(",");
		TableColumn<String, String> column = null;
		for (String columnName : firstRow) {
			column = new TableColumn<>(columnName);
			column.setCellValueFactory(cellValue -> {
				String values = cellValue.getValue();
				String[] cells = values.split(",");
				int columnIndex = cellValue.getTableView().getColumns().indexOf(cellValue.getTableColumn());
				if (columnIndex >= cells.length) {
					return new SimpleStringProperty("");
				} else {
					return new SimpleStringProperty(cells[columnIndex]);
				}
			});

			this.setItems(FXCollections.observableArrayList(lines));
			// Remove header row, as it will be added to the data at this point
			// this only works if we're sure that our CSV file has a header,
			// otherwise, we're just deleting data at this point.
			this.getItems().remove(0);
			setFixedCellSize(25);
			prefHeightProperty().bind(fixedCellSizeProperty().multiply(Bindings.size(getItems()).add(Height)));
			minHeightProperty().bind(prefHeightProperty());
			maxHeightProperty().bind(prefHeightProperty());
			// VBox vbox=new VBox();
			// prefHeightProperty().bind(stage.heightProperty());
			prefWidthProperty().bind(fixedCellSizeProperty().multiply(Bindings.size(getItems()).add(Width)));
			minWidthProperty().bind(prefWidthProperty());
			maxWidthProperty().bind(prefWidthProperty());/**/

			this.getColumns().add(column);

			/******************** Editable Table ***********************/
			if (isEditable) {
				this.setEditable(true);
				HashMap<TableCell<String, String>, Integer> indexRow = new HashMap<>();
				AtomicInteger count = new AtomicInteger();
				
				column.setCellFactory(col -> {
					TableCell<String, String> cell = TextFieldTableCell.<String>forTableColumn().call(col);
					indexRow.put(cell, count.getAndIncrement());

					cell.itemProperty().addListener((obs, oldValue, newValue) -> {
						if (oldValue != null && newValue != null && oldValue != newValue && indexRow.get(cell)>0) {
//							int i = indexRow.get(cell);
//							int j = getColumns().indexOf(cell.getTableColumn());
//								//CsvTools.ModefieOneElementCSVFile ( i,  j,  newValue,path) ;
//								
//								if(this.pane!=null) {
//								String [] temp =path.replace("\\"," ").split(" ");
//								String choiseName=temp[temp.length-1].replace(".csv","");
//							//	pane.Agnetconfig(choiseName);
//								}
						}
					});
					return cell;	
				});
			}
			/******** end Editable Table *************/

		}

	}





	public String[][] printTable() {
		String[][] tabl = new String[getItems().size()+1][getItems().get(0).length()];
		tabl[0]=firstRow;
		for (int i = 0; i < getItems().size(); i++) {
			tabl[i+1] = getItems().get(i).split(",");
		}
		return tabl;
	}
	
	static List<String> tabToLines(String[][] tab) {
		List<String> lines = new ArrayList<>();
		for (int i = 0; i < tab.length; i++) {
			String temp = "";
			for (int j = 0; j < tab[0].length; j++) {
				temp = temp + "," + tab[i][j];
			}
			lines.add(temp);
		}
		return lines;
	}

	

}