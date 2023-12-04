/*
 *  Copyright 2019 Mark59.com
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.results.splitter.test;

import static org.junit.Assert.assertNotEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import com.mark59.results.splitter.ResultsSplitter;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import junit.framework.TestCase;

/**
 * Simple unit test ResultsSplitter.
 * @author Philip Webb
 * Written: Australian Winter 2019  
 * 
 * The asserts are pretty basic here (could be improved with file content comparisons?). 
 */
public class ResultsSplitterTest extends TestCase
{
	int actualSamplesCount=0;
	String outdest = "./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV";
	
    public void testJmterResultsOneFileMetricsFileSplitterTest() throws IOException, ParserConfigurationException, SAXException
    {
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED/jmterResultsFileConvertedToCSV.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED"));
    	Files.createDirectory(Paths.get("./TESTDATA/MERGED"));

		String[] args = { "-i./TESTDATA",
    						"-fjmterResultsFileConvertedToCSV.csv",
    						"-m" +  ResultsSplitter.METRICS_FILE_NO,	
    						"-e" +  ResultsSplitter.ERROR_TXNS_NO, 
    						"-x" +  "True"};
    	
		ResultsSplitter resultsSplitter = new ResultsSplitter(); 
		resultsSplitter.parseArguments(args);    
		resultsSplitter.clearOutputDirectory();     
		actualSamplesCount=resultsSplitter.convert();
        assertEquals(124, actualSamplesCount);
        
    	Files.delete(Paths.get("./TESTDATA/MERGED/jmterResultsFileConvertedToCSV.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED"));    	
    }
  	
	
    public void testJmterResultsMetricsFileSplitterTest() throws IOException, ParserConfigurationException, SAXException
    {
    	Files.deleteIfExists(Paths.get("./TESTDATA/SEPARATE_METRICS/jmterResultsFileConvertedToCSV_METRICS.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/SEPARATE_METRICS/jmterResultsFileConvertedToCSV.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/SEPARATE_METRICS"));
    	Files.createDirectory(Paths.get("./TESTDATA/SEPARATE_METRICS"));
    	
		String[] args = { "-i./TESTDATA",
							"-o./TESTDATA/SEPARATE_METRICS",
    						"-fjmterResultsFileConvertedToCSV.csv",
    						"-m" +  ResultsSplitter.METRICS_FILE_CREATE_METRICS_REPORT,	
    						"-e" +  ResultsSplitter.ERROR_TXNS_RENAME,
    						"-x" +  "False"};
    	
		ResultsSplitter resultsSplitter = new ResultsSplitter(); 
		resultsSplitter.parseArguments(args);    
		resultsSplitter.clearOutputDirectory();     
		actualSamplesCount=resultsSplitter.convert();
        assertEquals(147, actualSamplesCount);
        
    	Files.delete(Paths.get("./TESTDATA/SEPARATE_METRICS/jmterResultsFileConvertedToCSV_METRICS.csv"));
    	Files.delete(Paths.get("./TESTDATA/SEPARATE_METRICS/jmterResultsFileConvertedToCSV.csv"));
    	Files.delete(Paths.get("./TESTDATA/SEPARATE_METRICS"));
    }
    
    
    public void testJmterResultsSplitByDatatypeTest() throws IOException, ParserConfigurationException, SAXException
    {
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_CPU_UTIL.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_DATAPOINT.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_MEMORY.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    	Files.createDirectory(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    	
		String[] args = { "-i./TESTDATA",
							"-o./TESTDATA/MERGED_SPLIT_BY_DATAYPE",
    						"-fjmterResultsFileConvertedToCSV.csv",
    						"-m" +  ResultsSplitter.METRICS_FILE_SPLIT_BY_DATATYPE,	
    						"-e" +  ResultsSplitter.ERROR_TXNS_RENAME };
    	
		ResultsSplitter resultsSplitter = new ResultsSplitter(); 
		resultsSplitter.parseArguments(args);    
		resultsSplitter.clearOutputDirectory();     
		actualSamplesCount=resultsSplitter.convert();

		assertEquals("total", 124, actualSamplesCount);
        assertEquals("_CPU_UTIL", 9, linecount(outdest+"_CPU_UTIL.csv"));
        assertEquals("_DATAPOINT", 12, linecount(outdest+"_DATAPOINT.csv"));
        assertEquals("_MEMORY", 14, linecount(outdest+"_MEMORY.csv"));
        assertEquals("TXN", 95, linecount(outdest+".csv"));
      
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_CPU_UTIL.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_DATAPOINT.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_MEMORY.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    }
    
    
    public void testJmterResultsSplitByDatatypeTxnsAsONLYCDPTest() throws IOException, ParserConfigurationException, SAXException, CsvValidationException
    {
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_CPU_UTIL.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_DATAPOINT.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_MEMORY.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    	Files.createDirectory(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    	
		String[] args = { "-i./TESTDATA",
							"-o./TESTDATA/MERGED_SPLIT_BY_DATAYPE",
    						"-fjmterResultsFileConvertedToCSV.csv",
    						"-m" +  ResultsSplitter.METRICS_FILE_SPLIT_BY_DATATYPE,	
    						"-e" +  ResultsSplitter.ERROR_TXNS_RENAME,
							"-c" +  ResultsSplitter.ONLY_CDP};
    	
		ResultsSplitter resultsSplitter = new ResultsSplitter(); 
		resultsSplitter.parseArguments(args);    
		resultsSplitter.clearOutputDirectory();     
		actualSamplesCount=resultsSplitter.convert();
        
		assertEquals("total", 33, actualSamplesCount);  						// 8 + 9 + 13 + 2 = 32
        assertEquals("_CPU_UTIL", 9, linecount(outdest+"_CPU_UTIL.csv")); 		// 1 header =>  8 datas 
        assertEquals("_DATAPOINT", 12, linecount(outdest+"_DATAPOINT.csv")); 	// 1 header + 2 <lf> => 9 datas
        assertEquals("_MEMORY", 14, linecount(outdest+"_MEMORY.csv"));  		// 1 header =>  13 datas 
        assertEquals("TXN", 4, linecount(outdest+".csv"));        				// 1 header =>  2 datas 
        
        
        // all txn lines should be cdp
		FileReader fileReader = new FileReader(outdest+".csv");
		CSVReader csvReader = new CSVReader(new BufferedReader(fileReader));  
		String[] csvDataLineFields = csvReader.readNext();						// header
		List<String> csvHeaderFieldsList = Arrays.asList(csvDataLineFields);
		//System.out.println("csvHeaderFieldsList = " + csvHeaderFieldsList  );
		int fieldPoslabel     		= csvHeaderFieldsList.indexOf("label");
		int fieldPosdataType  		= csvHeaderFieldsList.indexOf("dataType");
		csvDataLineFields = csvReader.readNext();						// 1st data line
	   	while ( csvDataLineFields != null ) { 
	   		// System.out.println("label = " + csvDataLineFields[fieldPoslabel]);
	   		// System.out.println("type  = " + csvDataLineFields[fieldPosdataType]);
	   		assertEquals("label should end with (CDP)", " (CDP)", StringUtils.right(csvDataLineFields[fieldPoslabel], 6));
	   		assertEquals("should be CDP type", "CDP", csvDataLineFields[fieldPosdataType]);
	   		csvDataLineFields = csvReader.readNext();
	   	}
	   	csvReader.close();
        
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_CPU_UTIL.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_DATAPOINT.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_MEMORY.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    }

    
    public void testJmterResultsSplitByDatatypeTxnsAsHIDECDPTest() throws IOException, ParserConfigurationException, SAXException, CsvValidationException
    {
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_CPU_UTIL.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_DATAPOINT.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_MEMORY.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    	Files.createDirectory(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    	
		String[] args = { "-i./TESTDATA",
							"-o./TESTDATA/MERGED_SPLIT_BY_DATAYPE",
    						"-fjmterResultsFileConvertedToCSV.csv",
    						"-m" +  ResultsSplitter.METRICS_FILE_SPLIT_BY_DATATYPE,	
    						"-e" +  ResultsSplitter.ERROR_TXNS_RENAME,
							"-c" +  ResultsSplitter.HIDE_CDP};
    	
		ResultsSplitter resultsSplitter = new ResultsSplitter(); 
		resultsSplitter.parseArguments(args);    
		resultsSplitter.clearOutputDirectory();     
		actualSamplesCount=resultsSplitter.convert();
        
		assertEquals("total", 121, actualSamplesCount);  						
        assertEquals("_CPU_UTIL", 9, linecount(outdest+"_CPU_UTIL.csv")); 		 
        assertEquals("_DATAPOINT", 12, linecount(outdest+"_DATAPOINT.csv")); 	
        assertEquals("_MEMORY", 14, linecount(outdest+"_MEMORY.csv"));  		 
        assertEquals("TXN", 92, linecount(outdest+".csv"));
        
        // all txn lines should never be cdp
		FileReader fileReader = new FileReader(outdest+".csv");
		CSVReader csvReader = new CSVReader(new BufferedReader(fileReader));  
		String[] csvDataLineFields = csvReader.readNext();						// header
		List<String> csvHeaderFieldsList = Arrays.asList(csvDataLineFields);
		//System.out.println("csvHeaderFieldsList = " + csvHeaderFieldsList  );
		int fieldPoslabel     		= csvHeaderFieldsList.indexOf("label");
		int fieldPosdataType  		= csvHeaderFieldsList.indexOf("dataType");
		csvDataLineFields = csvReader.readNext();						// 1st data line
	   	while ( csvDataLineFields != null ) { 
	   		// System.out.println("label = " + csvDataLineFields[fieldPoslabel]);
	   		// System.out.println("type  = " + csvDataLineFields[fieldPosdataType]);
	   		assertNotEquals("label should not end with (CDP)", " (CDP)", StringUtils.right(csvDataLineFields[fieldPoslabel], 6));
	   		assertNotEquals("should be not be CDP type", "CDP", csvDataLineFields[fieldPosdataType]);
	   		csvDataLineFields = csvReader.readNext();
	   	}
	   	csvReader.close();
        
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_CPU_UTIL.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_DATAPOINT.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_MEMORY.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    }
   
 
    public void testJmterResultsSplitByDatatypeTxnsAsSHOWCDPTest() throws IOException, ParserConfigurationException, SAXException {
    	
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_CPU_UTIL.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_DATAPOINT.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_MEMORY.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    	Files.createDirectory(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    	
		String[] args = { "-i./TESTDATA",
							"-o./TESTDATA/MERGED_SPLIT_BY_DATAYPE",
    						"-fjmterResultsFileConvertedToCSV.csv",
    						"-m" +  ResultsSplitter.METRICS_FILE_SPLIT_BY_DATATYPE,	
    						"-e" +  ResultsSplitter.ERROR_TXNS_RENAME,
							"-c" +  ResultsSplitter.SHOW_CDP};
    	
		ResultsSplitter resultsSplitter = new ResultsSplitter(); 
		resultsSplitter.parseArguments(args);    
		resultsSplitter.clearOutputDirectory();     
		actualSamplesCount=resultsSplitter.convert();
        
		assertEquals("total", 124, actualSamplesCount);  						
        assertEquals("_CPU_UTIL", 9, linecount(outdest+"_CPU_UTIL.csv")); 		 
        assertEquals("_DATAPOINT", 12, linecount(outdest+"_DATAPOINT.csv")); 	
        assertEquals("_MEMORY", 14, linecount(outdest+"_MEMORY.csv"));  		 
        assertEquals("TXN", 95, linecount(outdest+".csv"));

        Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_CPU_UTIL.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_DATAPOINT.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_MEMORY.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    }
    
    
	public int linecount(String filename) {  // count includes header line
		int lineCount = 0;
		Scanner scanner;
		String line = "";
		try {
			scanner = new Scanner(new File(filename));
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				lineCount++;
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println(filename + " fails around line " + line);
			e.printStackTrace();
			return -1;
		}
		return lineCount;
	}    
    
    
}
