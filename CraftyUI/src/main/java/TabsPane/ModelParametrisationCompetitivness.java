package TabsPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Main.Main_CraftyFx;
import UtilitiesFx.Path;
import UtilitiesFx.ReadFile;
import UtilitiesFx.Tools;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;

public class ModelParametrisationCompetitivness {

	
	LineChart<Number, Number> lineChart = Tools.lineChart("Competitiveness curves");
	Series<Number, Number>[] series = new Series[Path.servicesList.size()];

	TextField percentageCells = Tools.textField(4, "5");
	TextField percentageTakeOvers = Tools.textField(4, "5");
	RadioButton relativeThresholding = new RadioButton(" Relative Thresholding");
	RadioButton[] L1Competition = Tools.radio(" Remove Negative", " Normalise Cell Residual", " Normalise Cell Supply");



	public TitledPane pane() {

		VBox vbox = new VBox();
	
		for (int i = 0; i < series.length; i++) {
			series[i] = new XYChart.Series<Number, Number>();
			lineChart.getData().add(series[i]);
		}

		vbox.getChildren().addAll(
				Tools.hBox(new Text("   Percentage Cells :"), percentageCells, new Text("%  "),
						new Text("   Percentage Cells :"), percentageTakeOvers, new Text("%  "), relativeThresholding),
				// Tools.T("RegionalDemand", false, Tools.createVBox(new Text(" GRAF DEMAD:"))),
				Tools.T("Curve Competitiveness Model", true,
						Tools.vBox(Tools.hBox(L1Competition),
								Tools.T("",true,lineChart), 								
								Tools.T("Modifies the competitiveness curve ",false,	equationsBTN(Path.servicesList))
						)));

		TitledPane titel = new TitledPane("World_withoutSN_multiplicativeNoise_linearcompetition_relative: ", vbox);
		titel.setStyle(" -fx-base: #d6d9df;");
		titel.setMaxWidth(Main_CraftyFx.sceneWidth);
		titel.setMinWidth(Main_CraftyFx.sceneWidth);
		titel.setMaxHeight(Screen.getPrimary().getBounds().getHeight());
		titel.setMinHeight(Screen.getPrimary().getBounds().getHeight());
		findEquations(Path.fileFilter("\\xml\\","Competition_",Path.senario).get(0));
		return titel;

	}

	void findEquations(String path) {
		HashMap<String, String> str_eq = new ReadFile().findEquations(path);
		
		double[][] eq = new double[Path.servicesList.size()][3];
		
		str_eq.forEach((name, e) -> {
			String[] A = e.split("([^\\d.]|\\B\\.|\\.\\B)+");
		
			int i = 0;
			for (int x = 0; x < A.length; x++) {
				if (A[x] != "") {
					eq[Path.servicesList.indexOf(name)][i] = Tools.sToD(A[x]);
					i++;
				}
			}
		});
		for (int i = 0; i < eq.length; i++) {
			graph(i, eq[i]);
		}
	}



	void graph(int index, double[] eq) {
		series[index].getData().clear();
		double y = 0;
		for (int j = 0; j < 100; j++) {
			double x = j * 0.04;
			if (eq[2] == 0) {
				y = eq[0] * x + eq[1];
				series[index].setName(Path.servicesList.get(index) + ": y=" + eq[0] + " x +" + eq[1]);
			} else {
				y = eq[0] + eq[1] * Math.exp(x * eq[2]);
				series[index]
						.setName(Path.servicesList.get(index) + ": y=" + eq[0] + " + " + eq[1] + " exp(x*" + eq[2] + ")");
			}

			series[index].getData().add(new XYChart.Data<>(x, y));
		}
	}

	

	Pane equationsBTN(List<String> list) {
		RadioButton[] linrear = new RadioButton[Path.servicesList.size()];
		RadioButton[] exp = new RadioButton[Path.servicesList.size()]; 
		ArrayList<TextField> A = new ArrayList<>();
		ArrayList<TextField> B = new ArrayList<>();
		ArrayList<TextField> C = new ArrayList<>();
		VBox V = new VBox();
		Text  t1 = Tools.text("Linear Function: y=ax+b", Color.BLUE);
		Text  t2 = Tools.text("Exponential Function   y=a+b*exp(x*c)", Color.BLUE);
		V.getChildren().addAll(t1, t2);
		Button[] bt = new Button[list.size()];
		double[] v = new double[3];
		for (int i = 0; i < linrear.length; i++) {
			bt[i] = Tools.button("plot and Add", "b6e7c9");
			A.add(Tools.textField(3, "a"));
			B.add(Tools.textField(3, "b"));
			C.add(Tools.textField(3, "c"));
			Text  tc = new Text("c=");
			tc.setVisible(false);
			C.get(i).setVisible(false);
			linrear[i] = new RadioButton(" Linear Function    ");
			linrear[i].setSelected(true);
			exp[i] = new RadioButton(" Exponential Function   ");
			V.getChildren().addAll( Tools.T(list.get(i), false, 
					Tools.hBox(linrear[i], exp[i]),
					Tools.hBox(new Text("a ="), A.get(i), new Text("b="),B.get(i), tc, C.get(i), new Separator(), bt[i]))
					);
			int k = i;
			linrear[i].setOnAction(e -> {
				exp[k].setSelected(false);
				C.get(k).setVisible(exp[k].isSelected());
				tc.setVisible(exp[k].isSelected());
			});
			exp[i].setOnAction(e -> {
				linrear[k].setSelected(false);
				C.get(k).setVisible(exp[k].isSelected());
				tc.setVisible(exp[k].isSelected());
			});
			bt[i].setOnAction(e -> {
				v[0] = Tools.sToD(A.get(k).getText());
				v[1] = Tools.sToD(B.get(k).getText());
				v[2] = Tools.sToD(C.get(k).getText());
				graph(k, v);
			});
		}
		return V;
	}

}
