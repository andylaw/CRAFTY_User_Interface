package utils.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dataLoader.AFTsLoader;
import dataLoader.CellsLoader;
import dataLoader.ServiceSet;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.Pane;
import main.MainHeadless;
import model.Cell;
import model.Manager;
import utils.graphicalTools.LineChartTools;
import utils.graphicalTools.MousePressed;
import utils.graphicalTools.NewWindow;

public class AFT_analyzer {

	// 1. Initilase the model
	// 2. Generate a random values set for each Capital
	// 3. For each AFT calculate the productivity for each random capital

	// 4. generate a set of product
	// 5. Calculate frequancy of values (egnor null values).
	// retrun AFT1 Hash map

	public static void main(String[] args) {
		MainHeadless.modelInitialisation();
		Manager a = AFTsLoader.getAftHash().values().iterator().next();
		System.out.println(a.getLabel());
		productivitySample(5000, 100, a);
	}

	static ConcurrentHashMap<String, Double> capitalRandomGenerator() {
		ConcurrentHashMap<String, Double> RadnomCapitalSample = new ConcurrentHashMap<>();
		CellsLoader.getCapitalsList().forEach(cn -> {
			RadnomCapitalSample.put(cn, Math.random());
		});
		return RadnomCapitalSample;
	}

	static ConcurrentHashMap<String, Double> productivityCalculator(Manager a) {
		if (a != null) {
			Cell c = new Cell(0, 0);
			c.getCapitals().putAll(capitalRandomGenerator());
			ConcurrentHashMap<String, Double> services = new ConcurrentHashMap<>();
			ServiceSet.getServicesList().forEach(s -> {
				double product = c.getCapitals().entrySet().stream()
						.mapToDouble(e -> Math.pow(e.getValue(), a.getSensitivity().get(e.getKey() + "_" + s)))
						.reduce(1.0, (x, y) -> x * y);
				services.put(s, product * a.getProductivityLevel().get(s));
			});
			return services;
		}
		return null;
	}

	public static Map<String, ArrayList<Double>> productivitySample(int sampleSize, int subIntervalNbr, Manager a) {
		Set<ConcurrentHashMap<String, Double>> set = new HashSet<>();
		for (int i = 0; i < sampleSize; i++) {
			set.add(productivityCalculator(a));
		}

		Map<String, ArrayList<Double>> fq = new HashMap<>();

		ServiceSet.getServicesList().forEach(s -> {
			ArrayList<Double> numbers = new ArrayList<>();
			set.forEach(hash -> {
				numbers.add(hash.get(s));
			});

			ArrayList<Double> intList = logNumbersInIntervals(numbers, subIntervalNbr);
			System.out.println(a.getLabel() + ", " + s + " " + intList);
//			ArrayList<Double> doubleList = intList.stream().map(Integer::doubleValue)
//					.collect(Collectors.toCollection(ArrayList::new));
			if (!isAllZero(intList))
				fq.put(s, intList);
		});
		return fq;
	}

	public static ArrayList<Double> logNumbersInIntervals(ArrayList<Double> numbers, int intervalNBR) {
		int[] counts = new int[intervalNBR + 1];
		for (Double number : numbers) {
			counts[(int) (number * intervalNBR)]++;
		}
		ArrayList<Double> result = new ArrayList<>();
		for (int count : counts) {
			result.add(Math.log(count + 1));
		}
		return result;
	}

	public static boolean isAllZero(ArrayList<Double> list) {
		for (int i = 1; i < list.size(); i++) {
			if (list.get(i) != 0.0) {
				return false;
			}
		}

		return true;
	}

	public static void generateChart(int sampleSize, int subIntervalNbr, Manager a) {
		LineChart<Number, Number> chart = new LineChart<>(new NumberAxis(), new NumberAxis());
		chart.setTitle(a.getLabel());
		lineChart(chart, productivitySample(sampleSize, subIntervalNbr, a), a.getLabel());
		NewWindow win = new NewWindow();
		win.creatwindows("", chart);
	}

	public static void lineChart(LineChart<Number, Number> lineChart, Map<String, ArrayList<Double>> hash,
			String titel) {
		Series<Number, Number>[] series = new XYChart.Series[hash.size()];

		AtomicInteger i = new AtomicInteger();
		List<String> sortedKeys = new ArrayList<>(hash.keySet());
		Collections.sort(sortedKeys);
		for (String key : sortedKeys) {
			ArrayList<Double> value = hash.get(key);
			if (value != null) {
				series[i.get()] = new XYChart.Series<Number, Number>();
				series[i.get()].setName(key);
				lineChart.getData().add(series[i.get()]);
				i.getAndIncrement();
			}
		}

		AtomicInteger k = new AtomicInteger();
		sortedKeys.forEach((key) -> {
			ArrayList<Double> value = hash.get(key);
			for (int j = 0; j < value.size(); j++) {
				series[k.get()].getData().add(new XYChart.Data<>(((j + 5 * Math.random()) / 100.), value.get(j)));
			}
			k.getAndIncrement();

		});

		MousePressed.mouseControle((Pane) lineChart.getParent(), lineChart, titel);

		LineChartTools.addSeriesTooltips(lineChart);
	}

}
