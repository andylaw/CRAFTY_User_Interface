package TabsPane;

import java.io.FileNotFoundException;
import java.io.IOException;

import UtilitiesFx.Path;
import UtilitiesFx.ReadFile;
import UtilitiesFx.Tools;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class OutPutter {
	
	static String path="C:\\Users\\byari-m\\Documents\\Data\\data_EUsmall\\RefSen.xml";
	
	
	/*********** ligne 7 *************************/
	RadioButton[] L7 = Tools.radio(" PerRegion", " Add Region", " Add Cell Region", " Add Services", " Add Capitals",
			" Add Competitiveness", " Add LandUseIndex", " Add LandUse", " Add Agent", " Add Pre Alloc Competitiveness",
			" Add Pre Alloc LandUse", " Add Gi Threshold", " Add Pre Alloc Gu Threshold");
	TextField startYear = Tools.textField(5, Path.startYear+"");
	TextField everyNYears = Tools.textField(5, "N");
	/*********** ligne 8 *************************/
	RadioButton[] L8 = Tools.radio(" Output Sums", " Per Region");
	TextField startYearAgg = Tools.textField(5, Path.startYear+"");
	TextField everyNYearsAgg = Tools.textField(5, "N");
	/*********** ligne 9 *************************/
	RadioButton perRegionAgg9 = new RadioButton(" Per Region");
	TextField startYearAgg9 = Tools.textField(5, Path.startYear+"");
	TextField everyNYearsAgg9 = Tools.textField(5, "N");
	/*********** ligne 10 *************************/
	RadioButton perRegionAgg10 = new RadioButton(" Per Region");
	TextField startYearAgg10 = Tools.textField(5, Path.startYear+"");
	TextField everyNYearsAgg10 = Tools.textField(5, "N");
	/*********** ligne 11 *************************/
	RadioButton[] L11 = Tools.radio(" Per Region", " Add Region");
	TextField everyNYearsAgg11 = Tools.textField(5, "N");
	RadioButton filePerTick11 = new RadioButton(" file PerTick");
	/*********** ligne 12 *************************/
	RadioButton[] L12 = Tools.radio(" Per Region", " file PerTick", " Add Region");
	TextField startYear12 = Tools.textField(5, Path.startYear+"");
	TextField everyNYears12 = Tools.textField(5, "N");
	/*********** ligne 13 *************************/
	RadioButton[] L13 = Tools.radio(" Per Region", " file PerTick");
	TextField startYear13 = Tools.textField(5, Path.startYear+"");
	TextField everyNYears13 = Tools.textField(5, "N");


	public VBox paneOutPut() throws IOException {

		VBox vbox = new VBox();
		vbox.getChildren().addAll(new Separator(),
				Tools.T("Cell Table  :", true,
						Tools.hBox(
								Tools.vBox( L7[0], L7[1], L7[2], L7[3], new Text(" Start Year = "), startYear),
								Tools.vBox( L7[4], L7[5], L7[6], L7[7], new Text(" Every N Years = "),
										everyNYears),
								Tools.vBox( L7[8], L7[9], L7[10], L7[11], L7[12]))),
				new Separator(),
				Tools.T(" Aggregate AFT Composition CSV Outputter :", true,
						Tools.vBox( Tools.hBox( L8),
								Tools.hBox( new Text(" Start Year = "), startYearAgg,
										new Text(" Every N Years = "), everyNYearsAgg))),
				new Separator(),
				Tools.T(" Aggregate AFT Competitiveness CSV Outputterr :", true,
						Tools.vBox(
								Tools.hBox( perRegionAgg9, new Text(" Start Year = "), startYearAgg9,
										new Text(" Every N Years = "), everyNYearsAgg9))),
				Tools.T(" Aggregate Demand Supply CSV Outputter :", true,
						Tools.vBox(
								Tools.hBox( perRegionAgg10, new Text(" Start Year = "), startYearAgg10,
										new Text(" Every N Years = "), everyNYearsAgg10))),
				Tools.T(" Takeover Cell Outputter :", true,
						Tools.vBox(
								Tools.hBox( L11[0], new Text(" Every N Years = "), everyNYearsAgg11,
										filePerTick11, L11[1]))),
				Tools.T(" Giving In Statistics Outputter :", true,
						Tools.vBox( Tools.hBox( L12),
								Tools.hBox( new Text(" startYear = "), startYear12,
										new Text(" Every N Years = "), everyNYears12))),
				Tools.T(" Action CSV Outputte :", true,
						Tools.vBox( Tools.hBox( L13), Tools.hBox(
								new Text(" startYear = "), startYear13, new Text(" Every N Years = "), everyNYears13)))
		);

		
	//	TitledPane titel = new TitledPane("OutPut Configuration", new ScrollPane());
		
	//	titel.setExpanded(true);
		return vbox;
	}
	
	//HBox 

	void writelines(ReadFile writeFile) throws IOException {
		

		boolean[] s = new boolean[L7.length];
		for (int i = 0; i < L7.length; i++) {
			s[i] = L7[i].isSelected();
		}
		writeFile.WrightLine(line(8, startYear.getText(), Path.startYear+"", everyNYears.getText(), "1", s));

		writeFile.WrightLine(
				line(9, startYearAgg.getText(), everyNYearsAgg.getText(), L8[0].isSelected(), L8[1].isSelected()));
		writeFile.WrightLine(
				line(10, startYearAgg9.getText(), Path.startYear+"", everyNYearsAgg9.getText(), "10", perRegionAgg9.isSelected()));
		writeFile.WrightLine(line(11, startYearAgg10.getText(), Path.startYear+"", everyNYearsAgg10.getText(), "10",
				perRegionAgg10.isSelected()));
		writeFile.WrightLine(line(12, everyNYearsAgg11.getText(), "10", L11[0].isSelected(), filePerTick11.isSelected(),
				L11[1].isSelected()));
		writeFile.WrightLine(line(13, startYear12.getText(), Path.startYear+"", everyNYears12.getText(), "10", L12[0].isSelected(),
				L12[1].isSelected(), L12[2].isSelected()));
		writeFile.WrightLine(line(14, startYear13.getText(), Path.startYear+"", everyNYears13.getText(), "10",
				L13[0].isSelected(), L13[1].isSelected()));

		writeFile.WrightLine(line(path,15));
		writeFile.WrightLine(line(path,16));
		writeFile.WrightLine(line(path,17));
	}

	static String line(String path, int nmr) throws FileNotFoundException {
		return new ReadFile().redOneLine(path,nmr);
	}

	static String line(String path,int nmr, boolean... bs) throws FileNotFoundException {
		String str = new ReadFile().redOneLine(path,nmr);
		String[] tokens = str.split(" ");
		int j = 0;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].contains("true") || tokens[i].contains("false")) {
				tokens[i] = tokens[i].replace("true", bs[j] + "").replace("false", bs[j] + "");
				j++;
			}
		}
		String tmp = "";
		for (int i = 0; i < tokens.length; i++) {
			tmp = tmp + " " + tokens[i];
		}

		return tmp;
	}

	static String line(int nmr, String str1, String I1, boolean... bs) throws FileNotFoundException {
		String str = line(path,nmr, bs);
		if (str1.length() == 0) {
			str1 = I1;
		}
		String[] tokens = str.split(" ");

		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].matches(".*\\d.*")) {
				tokens[i]=tokens[i].replace(tokens[i].replaceAll("[^0-9]+", "")+"",str1);
				break;
			}
		}
		String tmp = "";
		for (int i = 0; i < tokens.length; i++) {
			tmp = tmp + " " + tokens[i];
		}

		return tmp;
	}

	static String line(int nmr, String str1, String I1, String str2, String I2, boolean... bs) throws FileNotFoundException {
		String str = line( nmr, str1, I1, bs);
		if (str2.length() == 0) {
			str2 = I2;
		}
		String[] tokens = str.split(" ");

		for (int i =  tokens.length-1; i >=0; i--) {
			if (tokens[i].matches(".*\\d.*")) {
				tokens[i]=tokens[i].replace(tokens[i].replaceAll("[^0-9]+", "")+"",str2);
				break;
			}
		}
		String tmp = "";
		for (int i = 0; i < tokens.length; i++) {
			tmp = tmp + " " + tokens[i];
		}

		return tmp;
	}
	


}
