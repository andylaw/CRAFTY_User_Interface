package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import UtilitiesFx.filesTools.CsvTools;
import UtilitiesFx.filesTools.PathTools;
import UtilitiesFx.graphicalTools.CSVTableView;
import UtilitiesFx.graphicalTools.ColorsTools;
import UtilitiesFx.graphicalTools.Histogram;
import UtilitiesFx.graphicalTools.MousePressed;
import UtilitiesFx.graphicalTools.NewWindow;
import UtilitiesFx.graphicalTools.Tools;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import eu.hansolo.fx.charts.Category;
import eu.hansolo.fx.charts.ChartType;
import eu.hansolo.fx.charts.YChart;
import eu.hansolo.fx.charts.YPane;
import eu.hansolo.fx.charts.data.ValueChartItem;
import eu.hansolo.fx.charts.series.YSeries;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import main.OpenTabs;
import model.Manager;
import model.CellsSet;

/**
 * @author Mohamed Byari
 *
 */

public class AFTs_Controller {
	CellsLoader M;

	public AFTs_Controller(CellsLoader M) {
		this.M = M;
	}

	VBox vbox = new VBox();
	public static ChoiceBox<String> choiceAgnet;

	public Tab pane() {
		choiceAgnet = Tools.choiceBox(new ArrayList<>(M.AFtsSet.getAftHash().keySet()));
		choiceAgnet.setStyle(" -fx-base: #b6e7c9;");
		RadioButton plotInitialDistrebution = new RadioButton("  Distribution map ");
		RadioButton plotOptimalLandon = new RadioButton("Cumulative expected service productivity");
		Manager AFT0 = M.AFtsSet.getAftHash().get(choiceAgnet.getValue());
		Text AFTname = new Text(AFT0.getCompleteName());
		Rectangle rectangleColor = new Rectangle(40, 20, AFT0.getColor());
		Button save = Tools.button(" Save  modifications to the input data", "b6e7c9");
		Button AftAnalyse = Tools.button(" AFT analysis", "b6e7c9");
		String[][] production = productionTable(AFT0);
		Button productionFire = new Button();
		Button sensitivtyFire = new Button();
		Button deletAFT = new Button("Removing the AFT");
		Button resetAFT = new Button("Reset the AFT");
		NewAFT_Controller newAftPane = new NewAFT_Controller(M);
		NewWindow Analysewin = new NewWindow();

		Button addAFT = Tools.button("Add new AFT", "b6e7c9");
		addAFT.setOnAction(e -> {
			newAftPane.addaft();
		});
		AftAnalyse.setOnAction(e -> {
			VBox v = Tools.vBox(plotInitialDistrebution, plotOptimalLandon);
			Analysewin.creatwindows("", v);
		});

		Consumer<String> actionP = x -> {
			productionFire.fire();
		};

		TableView<ObservableList<String>> tableProduction = CSVTableView.newtable(production, actionP);
		BarChart<String, Number> histogram = new BarChart<String, Number>(new CategoryAxis(), new NumberAxis());
		// Histogram.histo(null, "Productivity levels", histogram,
		// AFT0.productivityLevel);
		GridPane gridSensitivityChart = Tools.grid(10, 15);

		Consumer<String> action = x -> {
			sensitivtyFire.fire();
		};

		TableView<ObservableList<String>> tableSensetivty = CSVTableView.newtable(sensitivityTable(AFT0), action);
		// updateSensitivty(AFT0.getLabel(), gridSensitivityChart, tableSensetivty);

		AFTname.setFill(Color.DARKBLUE);
		AFTname.setFont(Font.font("Arial", FontWeight.BOLD, 20));

		GridPane gridBehevoirButtons = new GridPane();
		AgentParametre(AFT0, gridBehevoirButtons);

		vbox = Tools.vBox(Tools.hBox(new Text("  Agent Functional Type  (AFT) "), choiceAgnet, new Separator(), AFTname,
				new Separator(), rectangleColor, new Separator(), deletAFT, save, resetAFT, new Separator(), addAFT),
				AftAnalyse,
				Tools.T("Optimal Production Levels", true,
						Tools.hBox(
								Tools.vBox(tableProduction, Tools.T(" Behevoir Parametrs ", true, gridBehevoirButtons)),
								histogram)),
				Tools.T(" Weighting Factor Specific to Capital (Sensitivity Factors)", true, tableSensetivty),
				Tools.T("Radar Chart of Sensitivity Factors", true, gridSensitivityChart));
		plotInitialDistrebution.setSelected(true);

		choiceAgnet.setOnAction(e -> {
			M.AFtsSet.forEach(a -> {
				if (a.getLabel().equals(choiceAgnet.getValue())) {
					AFTname.setText(a.getCompleteName());
					rectangleColor.setFill(a.getColor());
					showOnlyOneAFT(a);
					Histogram.histo(vbox, "Productivity levels", histogram, a.getProductivityLevel());
					ubdateRadarchart(a, gridSensitivityChart);
					AgentParametre(a, gridBehevoirButtons);
					CSVTableView.updateTableView(sensitivityTable(a), action, tableSensetivty);
					CSVTableView.updateTableView(productionTable(a), actionP, tableProduction);
				}
			});
		});

		sensitivtyFire.setOnAction(e2 -> {
			updateSensitivty(M.AFtsSet.getAftHash().get(choiceAgnet.getValue()), gridSensitivityChart, tableSensetivty);
		});
		productionFire.setOnAction(e3 -> {
			updateProduction(M.AFtsSet.getAftHash().get(choiceAgnet.getValue()), tableProduction);
			Histogram.histo(vbox, "Productivity levels", histogram,
					M.AFtsSet.getAftHash().get(choiceAgnet.getValue()).getProductivityLevel());
		});
		plotOptimalLandon.setOnAction(e2 -> {
			plotInitialDistrebution.setSelected(false);
			colorland(M.AFtsSet.getAftHash().get(choiceAgnet.getValue()));
		});
		deletAFT.setOnAction(e2 -> {
			M.AFtsSet.getAftHash().remove(choiceAgnet.getValue());
			choiceAgnet.getItems().remove(choiceAgnet.getValue());
			choiceAgnet.setValue(M.AFtsSet.getAftHash().keySet().iterator().next());
		});
		save.setOnAction(e4 -> {
			creatCsvFiles(M.AFtsSet.getAftHash().get(choiceAgnet.getValue()), "");
		});
		plotInitialDistrebution.setOnAction(e2 -> {
			plotOptimalLandon.setSelected(false);
			showOnlyOneAFT(M.AFtsSet.getAftHash().get(choiceAgnet.getValue()));
		});
		resetAFT.setOnAction(e2 -> {
			ArrayList<String> pFiles = PathTools.fileFilter("\\production\\", Paths.getScenario(),
					"\\" + choiceAgnet.getValue() + ".csv");
			M.AFtsSet.initializeAFTProduction(pFiles.get(0));
			ArrayList<String> bFiles = PathTools.fileFilter("\\agents\\", Paths.getScenario(),
					"AftParams_" + choiceAgnet.getValue() + ".csv");
			M.AFtsSet.initializeAFTBehevoir(bFiles.get(0));
			choiceAgnet.fireEvent(e2);
		});

		TitledPane titel = new TitledPane("AFTs Configuration: ", vbox);
		titel.setStyle(" -fx-base: #d6d9df;");
		Tab tab = new Tab("AFTs Configuration", vbox);
		tab.setOnSelectionChanged(e -> {
			showOnlyOneAFT(M.AFtsSet.getAftHash().get(choiceAgnet.getValue()));
			Histogram.histo(vbox, "Productivity levels", histogram,
					M.AFtsSet.getAftHash().get(choiceAgnet.getValue()).getProductivityLevel());
			ubdateRadarchart(M.AFtsSet.getAftHash().get(choiceAgnet.getValue()), gridSensitivityChart);
			OpenTabs.choiceScenario.setDisable(false);
			OpenTabs.year.setDisable(false);
		});
		return tab;
	}

	void colorland(Manager a) {
		CellsSet.getCellsSet().forEach(C -> {
			C.landStored(a);
		});
		CellsSet.colorMap("tmp");
	}

	void showOnlyOneAFT(Manager a) {
		CellsSet.getCellsSet().forEach((cell) -> {
			if (cell.getOwner() == null || !cell.getOwner().getLabel().equals(a.getLabel())) {
				cell.ColorP(Color.gray(0.65));
			} else {
				cell.ColorP(a.getColor());
			}
		});
	}

	String[][] sensitivityTable(Manager a) {
		String[][] sensetivtyTable = new String[CellsSet.getServicesNames().size() + 1][CellsSet.getCapitalsName().size()
				+ 1];
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			sensetivtyTable[i + 1][0] = CellsSet.getServicesNames().get(i);
			for (int j = 0; j < CellsSet.getCapitalsName().size(); j++) {
				sensetivtyTable[0][j + 1] = CellsSet.getCapitalsName().get(j);
				sensetivtyTable[i + 1][j + 1] = a.getSensitivty()
						.get(CellsSet.getCapitalsName().get(j) + "_" + CellsSet.getServicesNames().get(i)) + "";
			}

		}
		return sensetivtyTable;
	}

	String[][] productionTable(Manager a) {
		String[][] production = new String[2][CellsSet.getServicesNames().size()];

		for (int j = 0; j < CellsSet.getServicesNames().size(); j++) {
			production[0][j] = (String) CellsSet.getServicesNames().toArray()[j];
			production[1][j] = a.getProductivityLevel().get(CellsSet.getServicesNames().get(j)) + "";
		}
		return production;
	}

	Pane agentPane(Manager agent) {
		VBox vbox = new VBox();

		String[][] sensitivityMatrix = new String[CellsSet.getServicesNames().size()
				+ 1][CellsSet.getCapitalsName().size() + 1];
		sensitivityMatrix[0][0] = " ";
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			sensitivityMatrix[i + 1][0] = CellsSet.getServicesNames().get(i);
			for (int j = 0; j < CellsSet.getCapitalsName().size(); j++) {
				sensitivityMatrix[i + 1][j + 1] = agent.getSensitivty()
						.get(CellsSet.getCapitalsName().get(j) + "_" + CellsSet.getServicesNames().get(i)) + "";
				sensitivityMatrix[0][j + 1] = CellsSet.getCapitalsName().get(j);
			}
		}

		TableView<ObservableList<String>> tabV = CSVTableView.newtable(sensitivityMatrix);
		vbox.getChildren().addAll(Tools.T("Sensitivity", true, tabV), Tools.T("Chart", false, chartBox(agent, 4)));

		return vbox;
	}

	ScrollPane chartBox(Manager agent, int nbrColumn) {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(15);
		ScrollPane sp = new ScrollPane();
		int j = 0, k = 0;
		for (int i = 1; i < CellsSet.getServicesNames().size(); i++) {
			VBox vbox = new VBox();
			vbox.setAlignment(Pos.CENTER);
			Text text = new Text(CellsSet.getServicesNames().get(i));
			text.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
			text.setFill(Color.BLUE);
			vbox.getChildren().addAll(
					ychart(vbox, agent, CellsSet.getServicesNames().get(i)/* , (paneSize - 100) / nbrColumn */), text);
			grid.add(vbox, j++, k);
			if (j % nbrColumn == 0) {
				k++;
				j = 0;
			}

		}
		sp.setContent(grid);
		sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		return sp;
	}

	static void AgentParametre(Manager agent, GridPane grid) {

		grid.getChildren().clear();

		Slider[] parametrSlider = new Slider[7];
		TextField[] parametrValue = new TextField[7];
		parametrSlider[0] = new Slider(0, 100, agent.getGiveInMean());
		parametrSlider[1] = new Slider(0, 100, agent.getGiveInSD());
		parametrSlider[2] = new Slider(0, 100, agent.getGiveUpMean());
		parametrSlider[3] = new Slider(0, 100, agent.getGiveUpSD());
		parametrSlider[4] = new Slider(0, 1, agent.getServiceLevelNoiseMin());
		parametrSlider[5] = new Slider(0, 1, agent.getServiceLevelNoiseMax());
		parametrSlider[6] = new Slider(0, 1, agent.getGiveUpProbabilty());

		grid.add(new Text(" GiveIn Mean "), 0, 0);
		grid.add(new Text(" GiveIn Standard Deviation"), 0, 1);
		grid.add(new Text(" GiveUp Mean "), 0, 2);
		grid.add(new Text(" GiveUp Standard Deviation"), 0, 3);
		grid.add(new Text(" Service Level Noise Min"), 0, 4);
		grid.add(new Text(" Service Level Noise Max"), 0, 5);
		grid.add(new Text(" GiveUp Probabilty"), 0, 6);
		for (int i = 0; i < parametrValue.length; i++) {
			parametrValue[i] = new TextField(parametrSlider[i].getValue() + "");
			grid.add(parametrSlider[i], 1, i);
			grid.add(parametrValue[i], 2, i);

			int k = i;
			parametrSlider[i].valueProperty().addListener((ov, oldval, newval) -> {
				parametrValue[k].setText("" + parametrSlider[k].getValue());
				switch (k) {
				case 0:
					agent.setGiveInMean(parametrSlider[k].getValue());
					break;
				case 1:
					agent.setGiveInSD(parametrSlider[k].getValue());
					break;
				case 2:
					agent.setGiveUpMean(parametrSlider[k].getValue());
					break;
				case 3:
					agent.setGiveUpSD(parametrSlider[k].getValue());
					break;
				case 4:
					agent.setServiceLevelNoiseMin(parametrSlider[k].getValue());
					break;
				case 5:
					agent.setServiceLevelNoiseMax(parametrSlider[k].getValue());
					break;
				case 6:
					agent.setGiveUpProbabilty(parametrSlider[k].getValue());
					break;
				default:
					break;
				}
			});
			parametrValue[i].setOnKeyPressed(event -> {
				if (event.getCode().equals(KeyCode.ENTER)) {
					parametrSlider[k].setValue(Tools.sToD(parametrValue[k].getText()));
					parametrSlider[k].fireEvent(event);
				}
			});
		}
	}

	static void updateSensitivty(Manager newAFT, GridPane grid, TableView<ObservableList<String>> tabV) {
		String[][] tab = CSVTableView.tableViewToArray(tabV);
		for (int i = 1; i < tab.length; i++) {
			for (int j = 1; j < tab[0].length; j++) {
				newAFT.getSensitivty().put(tab[0][j] + "_" + tab[i][0], Tools.sToD(tab[i][j]));
			}
		}
		ubdateRadarchart(newAFT, grid);
	}

	static void ubdateRadarchart(Manager newAFT, GridPane grid) {
		grid.getChildren().clear();
		int j = 0, k = 0, nbrColumn = 4;
		for (int i = 0; i < CellsSet.getServicesNames().size(); i++) {
			VBox vbox = new VBox();
			vbox.setAlignment(Pos.CENTER);
			Text text = new Text(CellsSet.getServicesNames().get(i));
			text.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
			text.setFill(Color.BLUE);
			vbox.getChildren()
					.addAll(ychart(vbox, newAFT,
							CellsSet.getServicesNames().get(i)/* , (FxMain.sceneWidth * 1.3 - 100) / nbrColumn */),
							text);
			grid.add(vbox, j++, k);
			if (j % nbrColumn == 0) {
				k++;
				j = 0;
			}
		}

	}

	static void updateProduction(Manager newAFT, TableView<ObservableList<String>> tabV) {
		String[][] tab = CSVTableView.tableViewToArray(tabV);
		for (int i = 0; i < tab[0].length; i++) {
			newAFT.getProductivityLevel().put(tab[0][i], Tools.sToD(tab[1][i]));
		}
	}

	static void creatCsvFiles(Manager a, String descreption) {
		String[][] tab = new String[CellsSet.getServicesNames().size() + 1][CellsSet.getCapitalsName().size() + 2];
		tab[0][0] = "";
		tab[0][CellsSet.getCapitalsName().size() + 1] = "Production";
		for (int i = 0; i < CellsSet.getCapitalsName().size(); i++) {
			tab[0][i + 1] = CellsSet.getCapitalsName().get(i);

			for (int j = 0; j < CellsSet.getServicesNames().size(); j++) {
				tab[j + 1][0] = CellsSet.getServicesNames().get(j);
				tab[j + 1][i + 1] = a.getSensitivty()
						.get(CellsSet.getCapitalsName().get(i) + "_" + CellsSet.getServicesNames().get(j)) + "";
				tab[j + 1][CellsSet.getCapitalsName().size() + 1] = a.getProductivityLevel()
						.get(CellsSet.getServicesNames().get(j)) + "";
			}
		}

		String folder = new File(PathTools.fileFilter("\\production\\", Paths.getScenario()).get(0)).getParent();
		CsvTools.writeCSVfile(tab, folder + "\\" + a.getLabel() + ".csv");
		String[][] tab2 = new String[2][7];
		tab2[0] = "givingInDistributionMean,givingInDistributionSD,givingUpDistributionMean,givingUpDistributionSD,serviceLevelNoiseMin,serviceLevelNoiseMax,givingUpProb"
				.split(",");
		tab2[1][0] = a.getGiveInMean() + "";
		tab2[1][1] = a.getGiveInSD() + "";
		tab2[1][2] = a.getGiveUpMean() + "";
		tab2[1][3] = a.getGiveUpSD() + "";
		tab2[1][4] = a.getServiceLevelNoiseMin() + "";
		tab2[1][5] = a.getServiceLevelNoiseMax() + "";
		tab2[1][6] = a.getGiveUpProbabilty() + "";
		String folder2 = new File(PathTools.fileFilter("\\agents\\", Paths.getScenario()).get(0)).getParent();
		CsvTools.writeCSVfile(tab2, folder2 + "\\AftParams_" + a.getLabel() + ".csv");
		// add also in csv folder
		String pathCSV = PathTools.fileFilter("\\csv\\", "AFTsMetaData").get(0);
		String[][] tmp = CsvTools.csvReader(PathTools.fileFilter("\\csv\\", "AFTsMetaData").get(0));
		boolean isExiste = false;
		for (int i = 0; i < tmp.length; i++) {
			if (a.getLabel().equalsIgnoreCase(tmp[i][Tools.indexof("Label", tmp[0])])) {
				isExiste = true;
				break;
			}
		}
		if (!isExiste) {
			String[][] tmp2 = new String[tmp.length + 1][tmp[0].length];
			for (int i = 0; i < tmp2.length - 1; i++) {
				for (int j = 0; j < tmp2[0].length; j++) {
					tmp2[i][j] = tmp[i][j].replace(",", ".").replace("\"", "");
				}
			}
			tmp2[tmp.length][Tools.indexof("Label", tmp[0])] = a.getLabel();
			tmp2[tmp.length][Tools.indexof("name", tmp[0])] = a.getCompleteName();
			tmp2[tmp.length][Tools.indexof("Color", tmp[0])] = ColorsTools.toHex(a.getColor());
			tmp2[tmp.length][Tools.indexof("Description", tmp[0])] = descreption.replace(",", ".").replace("\"", "")
					.replace("\n", " ");
			CsvTools.writeCSVfile(tmp2, pathCSV);
		}
	}

	public static YChart<ValueChartItem> ychart(Pane box, Manager agent, String servicesName) {
		List<ValueChartItem> listvalues = new ArrayList<>();
		CellsSet.getCapitalsName().forEach(cname -> {
			double y = Math.min(100, agent.getSensitivty().get(cname + "_" + servicesName) * 100);
			listvalues.add(new ValueChartItem(y, ""));
		});

		YSeries<ValueChartItem> series = new YSeries<ValueChartItem>(listvalues, ChartType.RADAR_SECTOR// SMOOTH_RADAR_POLYGON//
				, new RadialGradient(0, 0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
						ColorsTools.color(new Random().nextInt(4))),
				Color.GRAY);
		List<Category> categories = new ArrayList<>();
		for (int i = 0; i < CellsSet.getCapitalsName().size(); i++) {
			categories.add(new Category(CellsSet.getCapitalsName().get(i)));
		}
		YChart<ValueChartItem> chart = new YChart(new YPane(categories, series));
		// chart.setPrefSize(scale, scale);
		MousePressed.mouseControle(box, chart);
		return chart;
	}

}
