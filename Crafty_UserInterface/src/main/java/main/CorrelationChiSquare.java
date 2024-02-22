package main;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import UtilitiesFx.filesTools.CsvTools;
import dataLoader.CellsLoader;
import dataLoader.Paths;
import model.CellsSet;
import model.Manager;

public class CorrelationChiSquare {

	public static void ceartCorelationMatrix(CellsLoader M) {
		String[][] csv = new String[M.AFtsSet.getAftHash().size() + 1][CellsSet.getCapitalsName().size() + 1];
		AtomicInteger i = new AtomicInteger(1);
		
		csv[0][0]="";
		M.AFtsSet.forEach(a -> {
			csv[i.get()][0]=a.getLabel();
			AtomicInteger j = new AtomicInteger(1);
			CellsSet.getCapitalsName().forEach(capital -> {
				csv[0][j.get()]=capital;
				csv[i.get()][j.get()] = vectorGenerator(a, capital) + "";
				j.getAndIncrement();
			});
			
			i.getAndIncrement();
		});
		CsvTools.writeCSVfile(csv,Paths.getProjectPath()+"\\output\\capiAFT.csv");
	}

	 static double vectorGenerator(Manager a, String CapitalsName) {
		double[] aft = new double[CellsSet.getCellsSet().size()];
		double[] capitalData = new double[CellsSet.getCellsSet().size()];

		AtomicInteger i = new AtomicInteger();
		CellsSet.getCellsSet().forEach(c -> {
			if (c.getOwner() == a)
				aft[i.get()] = 1;
			else
				aft[i.get()] = 0;

			capitalData[i.getAndIncrement()] = c.getCapitals().get(CapitalsName);
		});
		
		double ret = chiSquareTest(aft, capitalData);
				System.out.println(a.getLabel()+" , "+CapitalsName+"->" + ret);
		return ret;// Math.max(ret,0);//
	}

	static double chiSquareTest(double[] capitalData, double[] aft) {

		// Calculate point-biserial correlation
		SpearmansCorrelation correlation = new SpearmansCorrelation ();// PearsonsCorrelation
		double correlationCoefficient = correlation.correlation(capitalData, aft);

		return correlationCoefficient;

	}

}
