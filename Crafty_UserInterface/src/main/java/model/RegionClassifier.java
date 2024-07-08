package model;

import java.util.concurrent.ConcurrentHashMap;

import dataLoader.CellsLoader;

public class RegionClassifier {


    public static ConcurrentHashMap<String, ConcurrentHashMap<String, Cell>> regions() {
        // Map to store region and cells belonging to that region
        ConcurrentHashMap<String, ConcurrentHashMap<String, Cell>> regionMap = new ConcurrentHashMap<>();

        // Using parallelStream to process entries in parallel
        CellsLoader.hashCell.entrySet().parallelStream().forEach(entry -> {
            String region = entry.getValue().getCurrentRegion();
            regionMap.computeIfAbsent(region, k -> new ConcurrentHashMap<>()).put(entry.getKey(), entry.getValue());
        });
        System.out.println(regionMap.keySet());
//        regionMap.forEach((name,hash)->{
//        	System.out.println(name+": "+hash.keySet());
//        	Color color = ColorsTools.RandomColor();
//        	hash.values().forEach(c->{
//        		CellsSet.pixelWriter.setColor(c.getX(), CellsSet.maxY - c.getY(), color);
//        	});
//        });
		return regionMap;

    }
}
