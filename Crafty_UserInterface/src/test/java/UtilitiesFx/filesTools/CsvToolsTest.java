package UtilitiesFx.filesTools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


class CsvToolsTest {
	List<File> list;

	@BeforeEach
	void setUp() throws Exception {
		 list = CsvTools.detectFiles("C:\\Users\\byari-m\\Downloads");
		
	}

	@Test
	void test() {
		System.out.println(list.size());
	//	mockedPathTools.when(() ->  System.out.println(Mockito.eq("\\production\\")+" -- "+ any()));
	}

}
