package TabsPane;

import java.io.File;
import java.io.IOException;

import Main.Main_CraftyFx;
import UtilitiesFx.CSVTableView;
import UtilitiesFx.Path;
import UtilitiesFx.ReadFile;
import UtilitiesFx.Tools;
import WorldPack.Agents;
import WorldPack.Lattice;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class OpenTabs {

	Lattice M;
	Agents As;

	public OpenTabs() throws IOException {

		ScrollPane sp = new ScrollPane();
		sp.setContent(senarioPane());
		M = new Lattice();
		
		
		
	}

	public ScrollPane senarioPane() throws IOException {
		
		
		VBox vbox = new VBox();
	//	vbox.getChildren().addAll(new SelectSenario().select(), creatSenarioBox());
		Tools.showWaitingDialog(x -> {
			Main_CraftyFx.tabPane.getTabs().clear();
			Main_CraftyFx.root.getChildren().clear();
			M.creatMap();
			M.creatMapGIS();
			M.plotPatchs(); 
			M.colorMap("FR");
			
			
			
			try {
				Main_CraftyFx.tabPane.getTabs().addAll(
						  new DataDisplay(M).colorWorld()
					//	,new Tab("Production", new ModelParametrisationCompetitivness().pane())
						,new Agnets_Configuration(M).pane()
					//	,new Tab("CRAFTY CoBRA Interface", new RunTab(M).pane())
						, new CA_Pane(M).pane()
					//	, new SimulationOutPut(M).pane()
								
								);
			} catch (IOException e) {}
			
			
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
		TextField startTick = Tools.textField(5, Path.startYear+"");
		TextField endTick = Tools.textField(5, Path.endtYear+"");
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
		 * try { OutPutter.line(1, startTick,Path.startYear+"",endTick,"2086"); } catch
		 * (FileNotFoundException e) {}
		 */
		if (startTick.length() == 0) {
			startTick = Path.startYear+"";
		}
		if (endTick.length() == 0) {
			endTick = Path.endtYear+"";
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
