package UtilitiesFx;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import Main.Main_CraftyFx;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.ButtonBar;


public class Tools {

	public static VBox vBox(Node... children) {
		VBox vbox = new VBox();
		vbox.getChildren().addAll(children);
		return vbox;
	}
	


	public static HBox hBox(double fixeSize,Node... children) {
	    HBox h = new HBox();
		h.getChildren().addAll(children);
		h.setMinHeight(fixeSize);
		h.setPrefHeight(fixeSize);
		h.setMaxHeight(fixeSize);
		return h;
	}
	public static HBox hBox(Node... children) {
		HBox h = new HBox();
		h.getChildren().addAll(children);
		return h;
	}

	public static Button button(String string, String color) {
		Button button = new Button(string);
		if (!"".equals(color))
			button.setStyle(" -fx-base: #" + color + ";");
		return button;
	}

	public static TextField textField(int size, String string) {
		TextField T = new TextField(string);
		T.setPrefColumnCount(size);
		//T.setPromptText(string);
		return T;
	}

	public static Slider slider( double a,  double b, double d) {
		return slider(  a,   b,  d, true);
	}
	
	public static Slider slider( double a,  double b, double d,boolean ShowTick) {
		Slider slider = new Slider(a, b, d);
		slider.setShowTickLabels(ShowTick);
		slider.setShowTickMarks(ShowTick);
		return slider;
	}
	
	

	public static ChoiceBox<String> chois(ArrayList<String> list) {
		if(list.size()==0) {list.add("Empty");}
		ChoiceBox<String> choice = new ChoiceBox<>();
		choice.getItems().addAll(list);
		choice.setValue(list.get(0));
		return choice;
		
	}


	public static ChoiceBox<String> chois(String path, String condition,boolean onlyFolder) {
		ArrayList<String> wordslist = ReadFile.findFolder(new File(path), condition,onlyFolder);
		ChoiceBox<String> choice = new ChoiceBox<>();
		choice.getItems().addAll(wordslist);
		choice.setValue(wordslist.get(0));
		return choice;
	}

	public static ChoiceBox<String> chois(String path,boolean onlyFolder) {
		return chois(path, "",onlyFolder);
	}

	public static TitledPane T(String name, boolean open, Node... children) {
		TitledPane spatial = new TitledPane(name,vBox(children) );
		spatial.setExpanded(open);
		spatial.setStyle(" -fx-base: #d6d9df;");
		return spatial;
	}

	public static RadioButton[] radio(String... bs) {
		RadioButton[] R = new RadioButton[bs.length];
		for (int i = 0; i < bs.length; i++) {
			R[i] = new RadioButton(bs[i]);
		}

		return R;
	}

	public static LineChart<Number, Number> lineChart(String name) {
		LineChart<Number, Number> lineChart = new LineChart<>(new NumberAxis(), new NumberAxis());
		lineChart.setAnimated(false);
		lineChart.setCreateSymbols(false);
		lineChart.setTitle(name);
		// lineChart.setPrefWidth(size);
		return lineChart;
	}

	public static Text text(String txt, Color color) {
		Text t1 = new Text(txt);
		t1.setFill(color);
		return t1;
	}

	public static double sToD(String str) {
		try {
			return Double.parseDouble(str);
		}catch(NumberFormatException e) {
			return 0;
		}
//		if (str.matches("([0-9]*)\\.([0-9]*)") || str.matches("\\-([0-9]*)\\.([0-9]*)") || str.matches("[0-9]+")
//				|| str.matches("\\-[0-9]+")) 
		
	}

	public static String[] columnFromsMtrix(int colunNumber, String [][] M) {
		
		String[] vect=new String[M.length];
		if(colunNumber==-1) {colunNumber=M[0].length-1;}
		for (int i = 0; i < M.length; i++) {
			vect[i]=M[i][colunNumber];
			}
		return vect;
	}
	
	public static void showWaitingDialog(Consumer<String> method) {
		Stage waitingDialog = new Stage();
		waitingDialog.initModality(Modality.APPLICATION_MODAL);
		waitingDialog.initStyle(StageStyle.UNDECORATED);

		Label label = new Label("Please wait...");
		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setCenterShape(true);
		VBox root = new VBox();
		root.setAlignment(Pos.CENTER);
		root.setSpacing(10);
		root.getChildren().addAll(label, progressIndicator);

		Scene scene = new Scene(root, 200, 100);
		waitingDialog.setScene(scene);
		waitingDialog.show();

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				 Thread.sleep(5);
				// method.accept("");
				return null;
			}
 
			@Override
			protected void succeeded() {
				super.succeeded();
				waitingDialog.close();
			}
		};
		
		task.setOnSucceeded(wse -> {method.accept("");});// here

		Thread thread = new Thread(task);
		thread.start();
		thread.setPriority(Thread.MAX_PRIORITY);
		
	}
	
	  static public void modifyChoiceBoxList(ChoiceBox<String> choiceBox,ArrayList<String>  newlist) {
	        choiceBox.getItems().clear();
	        choiceBox.getItems().addAll(newlist);
	        choiceBox.getSelectionModel().selectFirst();
	        choiceBox.getSelectionModel().selectFirst();
	        choiceBox.setValue(newlist.get(0));
	    }
		public static void initialisPane(Tab caPane,ChoiceBox<String> choiceSenario,double scale) {
			
			

			caPane.setOnSelectionChanged(e -> {
				Main_CraftyFx.tabPane.setPrefWidth(Screen.getPrimary().getBounds().getWidth()* scale);
				Main_CraftyFx.tabPane.setMaxWidth(Screen.getPrimary().getBounds().getWidth() * scale);
				Main_CraftyFx.tabPane.setMinWidth(Screen.getPrimary().getBounds().getWidth() * scale);
				choiceSenario.setValue(Path.getSenario());
			});
		}

}
