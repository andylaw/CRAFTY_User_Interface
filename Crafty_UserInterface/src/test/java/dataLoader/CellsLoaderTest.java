package dataLoader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fxmlControllers.TabPaneController;
import model.Cell;
import model.CellsSet;

public class CellsLoaderTest {

	AFTsLoaderTest afTets = new AFTsLoaderTest();
	public CellsLoader M = new CellsLoader();
	Cell c = mock(Cell.class);

	@BeforeEach
	public void setUp() throws Exception {
		Paths.initialisation("C:\\Users\\byari-m\\Documents\\Data\\data_EUpaper_nocsv");
		M.loadCapitalsAndServiceList();
		afTets.setUp();
		M.loadMap();
		CellsSet.setCellsSet(M);
		
	}

//	@Test
//	void testFindBookById() {
//
//		Manager ow = mock(Manager.class);
//		when(c.getOwner()).thenReturn(ow);// Mock the behavior of c
//	//	verify(c).getOwner(); 
//		System.out.println(c + "-->" + c.getOwner());
//		verify(c).getOwner(); 
//		when(c.getOwner().getColor()).thenReturn(Color.RED);
////		verify(c).getOwner(); 
//		System.out.println("-->" + c.getOwner().getColor());
//	//	verify(c).getOwner(); 
//////		// Exercise
//		Color result = c.getOwner().getColor();
////		verify(c).getOwner(); 
//////		// Verify
//		assertEquals(Color.RED, result); // Assert that the result is what we mocked
////		// Optionally verify that c.getOwner was called
//		
//		
//		
//	}

	@Test
	void test() {
		// afTets.test();
		// fail("Not yet implemented");
	}

}
