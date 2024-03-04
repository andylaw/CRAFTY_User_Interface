package model;



import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import dataLoader.CellsLoaderTest;


class ModelRunnerTest {
	
	CellsLoaderTest loader= new CellsLoaderTest();
	ModelRunner R;
	
	@BeforeEach
	void setUp() throws Exception {
		loader.setUp();
		R = new ModelRunner(loader.M);
		R.calculeSystemSupply();
	}

	 @Test
	    void testCalculeSystemSupply() {
	        // Setup
	        // Assuming Cell is a class with methods getOwner() and prodactivity(String owner, String service)
	        Cell cell1 = mock(Cell.class);
	        Cell cell2 = mock(Cell.class);
	        Manager Owner1= mock(Manager.class);
	        Manager Owner2= mock(Manager.class);
	        
	        Mockito.when(cell1.getOwner()).thenReturn(Owner1);
	        Mockito.when(cell2.getOwner()).thenReturn(Owner2);
	        
	        String service1 = "Service1";
	        String service2 = "Service2";
	        Mockito.when(cell1.prodactivity(Owner1, service1)).thenReturn(10.0);
	        Mockito.when(cell1.prodactivity(Owner1, service2)).thenReturn(20.0);
	        Mockito.when(cell2.prodactivity(Owner2, service1)).thenReturn(15.0);
	        Mockito.when(cell2.prodactivity(Owner2, service2)).thenReturn(25.0);
	        
	     //   Mockito.when(CellsSet.getCellsSet()).thenReturn(new HashSet<>(Arrays.asList(cell1, cell2)));
	     //   Mockito.when(CellsSet.getServicesNames()).thenReturn(Arrays.asList(service1, service2));
	        
	   

	        // Act
	        R.calculeSystemSupply();
	        
	        // Assert
	        Map<String, Double> expectedSupply = new HashMap<>();
	        expectedSupply.put(service1, 25.0); // 10 from cell1 + 15 from cell2
	        expectedSupply.put(service2, 45.0); // 20 from cell1 + 25 from cell2
	        
//	        Mockito.assertThat(R.supply).containsExactlyInAnyOrderEntriesOf(expectedSupply);
	    }

}
