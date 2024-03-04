package fxmlControllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.chart.BarChart;
import javafx.scene.control.TableView;
import javafx.scene.control.ChoiceBox;
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
import model.CellsSet;
import model.Manager;

public class AFTsConfigurationController {

	@FXML
	private ChoiceBox<String> AFTChoisButton;
	@FXML
	private Label AFTNameLabel;
	@FXML
	Rectangle rectangleColor;
	@FXML
	private Button addNewAftBtn;
	@FXML
	private Button removeBtn;
	@FXML
	private Button saveModeficationBtn;
	@FXML
	private Button ResetBtn;
	@FXML
	private Button AFTAnalisisBtn;
	@FXML
	private TableView<ObservableList<String>> productivityTable;
	@FXML
	private Label SNoiseMaxS;
	@FXML
	private Slider GiveInSDS;
	@FXML
	private Slider GiveUpMeanS;
	@FXML
	private Slider GiveUpSDS;
	@FXML
	private Slider SNoiseMinS;
	@FXML
	private Slider ServiceLevelNoiseMaxS;
	@FXML
	private Slider GiveUpProbabiltyS;
	@FXML
	private TextField GiveInMeanT;
	@FXML
	private TextField GiveInSDT;
	@FXML
	private TextField GiveUpMeanT;
	@FXML
	private TextField GiveUpSDT;
	@FXML
	private TextField SNoiseMinT;
	@FXML
	private TextField ServiceLevelNoiseMaxT;
	@FXML
	private TextField GiveUpProbabiltyT;
	@FXML
	private Slider GiveInMeanS;
	@FXML
	private BarChart<String, Number> histogramePlevel;
	@FXML
	private TableView<ObservableList<String>> sensitivtyTable;
	@FXML
	private GridPane radarChartsGridPane;
	@FXML
	private GridPane gridBehevoirButtons;
	@FXML
	private ScrollPane scrollgrid;
	@FXML
	private ScrollPane scrollProduction;

	public CellsLoader M;
	NewAFT_Controller newAftPane;
	RadioButton plotInitialDistrebution = new RadioButton("  Distribution map ");
	RadioButton plotOptimalLandon = new RadioButton("Cumulative expected service productivity");
	NewWindow Analysewin = new NewWindow();;
	Button productionFire = new Button();
	Button sensitivtyFire = new Button();
	private boolean isNotInitialsation = false;

	public void initialize() {
		System.out.println("initialize " + getClass().getSimpleName());
		M = TabPaneController.M;
		newAftPane = new NewAFT_Controller(this);
		sensitivtyTable.setEditable(true);
		productivityTable.setEditable(true);

		Tools.choiceBox(AFTChoisButton, new ArrayList<>(M.AFtsSet.getAftHash().keySet()));

		sensitivtyFire.setOnAction(e2 -> {
			updateSensitivty(M.AFtsSet.getAftHash().get(AFTChoisButton.getValue()), radarChartsGridPane,
					sensitivtyTable);
		});
		productionFire.setOnAction(e3 -> {
			updateProduction(M.AFtsSet.getAftHash().get(AFTChoisButton.getValue()), productivityTable);
			Histogram.histo((Pane) histogramePlevel.getParent(), "Productivity levels", histogramePlevel,
					M.AFtsSet.getAftHash().get(AFTChoisButton.getValue()).getProductivityLevel());

		});

		plotOptimalLandon.setOnAction(e2 -> {
			plotInitialDistrebution.setSelected(false);
			colorland(M.AFtsSet.getAftHash().get(AFTChoisButton.getValue()));
		});
		isNotInitialsation = true;

		//scrollgrid.setPrefHeight(Screen.getPrimary().getBounds().getHeight()*0.8);
		radarChartsGridPane.prefWidthProperty().bind(scrollgrid.widthProperty());

	}

	@FXML
	public void choiceAgnetSetOnAction() {
		Manager a = M.AFtsSet.getAftHash().get(AFTChoisButton.getValue());
		AFTNameLabel.setText(a.getCompleteName());
		rectangleColor.setFill(a.getColor());
		if (isNotInitialsation) {
			showOnlyOneAFT(a);
		}
		Histogram.histo((Pane) histogramePlevel.getParent(), "Productivity levels", histogramePlevel,
				a.getProductivityLevel());
		ubdateRadarchart(a, radarChartsGridPane);
		AgentParametre(a, gridBehevoirButtons);
		CSVTableView.updateTableView(sensitivityTable(a), x -> sensitivtyFire.fire(), sensitivtyTable);
		CSVTableView.updateTableView(productionTable(a), x -> productionFire.fire(), productivityTable);
	}

	void colorland(Manager a) {
		CellsSet.getCells().forEach(C -> {
			C.landStored(a);
		});
		CellsSet.colorMap("tmp");
	}

	static void updateProduction(Manager newAFT, TableView<ObservableList<String>> tabV) {
		String[][] tab = CSVTableView.tableViewToArray(tabV);
		for (int i = 0; i < tab[0].length; i++) {
			newAFT.getProductivityLevel().put(tab[0][i], Tools.sToD(tab[1][i]));
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

	@FXML
	public void addAFTSetOnAction() {
		newAftPane.addaft();
	};

	@FXML
	public void removeBtnSetOnAction() {
		M.AFtsSet.getAftHash().remove(AFTChoisButton.getValue());
		AFTChoisButton.getItems().remove(AFTChoisButton.getValue());
		AFTChoisButton.setValue(M.AFtsSet.getAftHash().keySet().iterator().next());
	}

	@FXML
	public void saveModefication() {
		creatCsvFiles(M.AFtsSet.getAftHash().get(AFTChoisButton.getValue()), "");
	}

	@FXML
	public void AftAnalyseSetOnAction() {
		VBox v = Tools.vBox(plotInitialDistrebution, plotOptimalLandon);
		Analysewin.creatwindows("", v);
	};

	void showOnlyOneAFT(Manager a) {
		CellsSet.getCells().forEach((cell) -> {
			if (cell.getOwner() == null || !cell.getOwner().getLabel().equals(a.getLabel())) {
				cell.ColorP(Color.gray(0.65));
			} else {
				cell.ColorP(a.getColor());
			}
		});
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

	public static YChart<ValueChartItem> ychart(Pane box, Manager agent, String servicesName) {
		List<ValueChartItem> listvalues = new ArrayList<>();
		CellsSet.getCapitalsName().forEach(cname -> {
			double y = Math.min(100, agent.getSensitivty().get(cname + "_" + servicesName) * 100);
			listvalues.add(new ValueChartItem(y, ""));
		});

		YSeries<ValueChartItem> series = new YSeries<ValueChartItem>(listvalues, ChartType.RADAR_SECTOR// SMOOTH_RADAR_POLYGON//
				, new RadialGradient(0, 0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
						ColorsTools.colorYchart(new Random().nextInt(4))),
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

	String[][] sensitivityTable(Manager a) {
		String[][] sensetivtyTable = new String[CellsSet.getServicesNames().size()
				+ 1][CellsSet.getCapitalsName().size() + 1];
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
	
	public void updaChoisButton() {
		//AFTChoisButton.getItems().clear();
		Set <String> set= new HashSet<>();
		M.AFtsSet.getAftHash().keySet().forEach(name->{
			if(!AFTChoisButton.getItems().contains(name)) {
				set.add(name);
			}
		});
		AFTChoisButton.getItems().addAll(set);
		//AFTChoisButton.setValue(M.AFtsSet.getAftHash().keySet().iterator().next());
	}
}
