package utils.genaralTools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import model.Cell;

public class Utils {
	public static List<ConcurrentHashMap<String, Cell>> splitIntoSubsets(ConcurrentHashMap<String, Cell> cellsHash,
			int n) {
		// Create a list to hold the n subsets
		List<ConcurrentHashMap<String, Cell>> subsets = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			subsets.add(new ConcurrentHashMap<>());
		}

		// Distribute keys randomly across the n subsets
		cellsHash.keySet().parallelStream().forEach(key -> {
			int subsetIndex = ThreadLocalRandom.current().nextInt(n);
			subsets.get(subsetIndex).put(key, cellsHash.get(key));
		});
		return subsets;
	}
}
