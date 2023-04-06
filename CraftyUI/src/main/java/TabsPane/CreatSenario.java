package TabsPane;

import java.io.File;
import java.io.IOException;

import Main.Main_CraftyFx;
import UtilitiesFx.CSVTableView;
import UtilitiesFx.Path;
import UtilitiesFx.ReadFile;
import UtilitiesFx.Tools;
import WorldPack.Agents;
import WorldPack.Map;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class CreatSenario {

	Map M;
	Agents As;

	public CreatSenario() throws IOException {

		ScrollPane sp = new ScrollPane();
		sp.setContent(senarioPane());
		M = new Map();
		
		
		
	}

	public ScrollPane senarioPane() throws IOException {
		
		
		VBox vbox = new VBox();
	//	vbox.getChildren().addAll(new SelectSenario().select(), creatSenarioBox());
		Tools.showWaitingDialog(x -> {
			As = new Agents(M);
			As.initialise();
			As.M.plotPatchs(Main_CraftyFx.root);
			As.M.colorMap("FR");
			Main_CraftyFx.tabPane.getTabs().addAll(
					 new Tab("Data display", new DataDisplay(M).colorWorld())
					,new Tab("Production", new ModelParametrisationCompetitivness().pane())
					,new Tab("Agents Configuration", new Agnets_Configuration(As).pane())
					,new Tab("OutPut", new SimulationOutPut(M).pane())		
							);
		});
		ScrollPane turn =new ScrollPane(vbox);
	
		vbox.setOnMouseDragged(event -> {
		    double newHeight = event.getY();
		    double newWidth = event.getX();
		    vbox.setPrefWidth(newWidth);		  
		    vbox.setPrefHeight(newHeight);
		});
		return turn;
	}

	public VBox creatSenarioBox() throws IOException {

		ReadFile writeFile = new ReadFile();

		
		/*********** ligne 1 *************************/
		TextField startTick = Tools.textField(5, "2016");
		TextField endTick = Tools.textField(5, "2086");
	//	ChoiceBox<String> choiceword = Tools.chois(Path.getWorlds(), true);
		//Path.setWorldName("UK");
		
		/*********** ligne 4 *************************/
		

		
		/*********** ligne 5 *************************/
	//	ChoiceBox<String> choicecapital = Tools.chois(Path.csv, "Capital", false);
	//	RadioButton capitalsIndexed = new RadioButton(" Indexed");
		/*********** ligne 6 *************************/
//		ChoiceBox<String> choiceServices = Tools.chois(Path.csv, "Services", false);
	//	RadioButton ServicesIndexed = new RadioButton(" Indexed");

		/*********** AddAll **************************/
		Button CreateScenario = Tools.button("Create a Scenario", "b6e7c9");
		
		VBox vbox = new VBox();
		vbox.getChildren().addAll(
				Tools.hBox(new Text(" Start Tick = "), startTick, new Text("End Tick = "), endTick /*,choiceword*/),
				new Separator(),
			    Tools.hBox( Tools.text("Capital: ",Color.BLUE),new CSVTableView(new File(Path.fileFilter("\\Capitals.csv").get(0)), .1, 1, true)),
				new Text(""),
				Tools.hBox( Tools.text(" Services: ",Color.BLUE),new CSVTableView(new File(Path.fileFilter("\\Services.csv").get(0) ), 1, 1, false))
		);
		vbox.setAlignment(Pos.CENTER);
		CreateScenario.setOnAction(e -> {
			try {
				/*********** write **************************/
				writeFile.cleanfile();
				writeFile.WrightLine(line1(startTick.getText(), endTick.getText(), "UK"));
			  //writeFile.WrightLine(line2(choiceVersion.getValue()));
			  //writeFile.WrightLine(line3(regionalisation.isSelected()));
			  //writeFile.WrightLine(line5("Capital", regionalisation.isSelected()));
				writeFile.WrightLine(line6("Services", false));
				writeFile.WrightLine(
						"   <outputs outputDirectory=\"output/%v/%s\" filePattern=\"%s-%i-%r-%o\" tickPattern=\"%s-%i-%r-%o-%y\">");
			//	outPut.writelines(writeFile);

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		});


		
		return vbox;

	}





	String line1(String startTick, String endTick, String choiceword) {

		/*
		 * try { OutPutter.line(1, startTick,"2016",endTick,"2086"); } catch
		 * (FileNotFoundException e) {}
		 */
		if (startTick.length() == 0) {
			startTick = "2016";
		}
		if (endTick.length() == 0) {
			endTick = "2086";
		}
		return "<scenario startTick=\"" + startTick + "\" endTick=\"" + endTick + "\" world=\"" + choiceword + "\"";
	}

	String line2(String version) {
		return " version=\"" + version + "\"";
	}

	String line3(boolean regionalisation) {
		if (regionalisation) {
			return " regionalisation=\"Regionalisation\"";
		} else {
			return " regionalisation=\"NoRegionalisation\"";
		}

	}

	String line4(String senario) {
		return "  scenario=\"" + senario + "\">";

	}

	String line5(String file, boolean indexed) {
		return "  <capitals class=\"org.volante.abm.serialization.CSVCapitalLoader\" file=\"csv/" + file
				+ "\" indexed=\"" + indexed + "\"/>";

	}

	String line6(String file, boolean indexed) {
		return "  <services class=\"org.volante.abm.serialization.CSVServiceLoader\" file=\"csv/" + file
				+ "\" indexed=\"" + indexed + "\"/>";
	}

}
