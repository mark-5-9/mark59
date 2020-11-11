/*
 *  Copyright 2019 Insurance Australia Group Limited
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

package com.mark59.converterTest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.mark59.converter.ResultFilesConverter;

import junit.framework.TestCase;

/**
 * Simple unit test JmterResultsConverter.
 * @author Philip Webb
 * Written: Australian Winter 2019  
 * 
 * Hint: the assert is pretty basic, just use git (or whatever source control) to look for changes in the output files... 
 */
public class ResultFilesConverterTest     extends TestCase
{
	int actualSamplesCount=0;
	
    public void testJmterResultsOneFileMetricsFileConverterTest() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException
    {
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED/jmterResultsFileConvertedToCSV.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/MERGED"));
    	Files.createDirectory(Paths.get("./TESTDATA/MERGED"));

		String[] args = { "-i./TESTDATA",
    						"-fjmterResultsFileConvertedToCSV.csv",
    						"-m" +  ResultFilesConverter.METRICS_FILE_NO,	
    						"-e" +  ResultFilesConverter.ERROR_TXNS_NO, 
    						"-x" +  "True"};
    	
		ResultFilesConverter resultFilesConverter = new ResultFilesConverter(); 
		resultFilesConverter.parseArguments(args);    
		resultFilesConverter.clearOutputDirectory();     
		actualSamplesCount=resultFilesConverter.convert();
        assertEquals(122, actualSamplesCount);
        
    	Files.delete(Paths.get("./TESTDATA/MERGED/jmterResultsFileConvertedToCSV.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED"));    	
    }
  	
	
    public void testJmterResultsMetricsFileConverterTest() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException
    {
    	Files.deleteIfExists(Paths.get("./TESTDATA/SEPARATE_METRICS/jmterResultsFileConvertedToCSV_METRICS.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/SEPARATE_METRICS/jmterResultsFileConvertedToCSV.csv"));
    	Files.deleteIfExists(Paths.get("./TESTDATA/SEPARATE_METRICS"));
    	Files.createDirectory(Paths.get("./TESTDATA/SEPARATE_METRICS"));
    	
		String[] args = { "-i./TESTDATA",
							"-o./TESTDATA/SEPARATE_METRICS",
    						"-fjmterResultsFileConvertedToCSV.csv",
    						"-m" +  ResultFilesConverter.METRICS_FILE_CREATE_METRICS_REPORT,	
    						"-e" +  ResultFilesConverter.ERROR_TXNS_RENAME,
    						"-x" +  "False"};
    	
		ResultFilesConverter resultFilesConverter = new ResultFilesConverter(); 
		resultFilesConverter.parseArguments(args);    
		resultFilesConverter.clearOutputDirectory();     
		actualSamplesCount=resultFilesConverter.convert();
        assertEquals(145, actualSamplesCount);
        
    	Files.delete(Paths.get("./TESTDATA/SEPARATE_METRICS/jmterResultsFileConvertedToCSV_METRICS.csv"));
    	Files.delete(Paths.get("./TESTDATA/SEPARATE_METRICS/jmterResultsFileConvertedToCSV.csv"));
    	Files.delete(Paths.get("./TESTDATA/SEPARATE_METRICS"));
    }
    
    
    public void testJmterResultsSplitByDatatypeConverterTest() throws FileNotFoundException, IOException, ParserConfigurationException, SAXException
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
    						"-m" +  ResultFilesConverter.METRICS_FILE_SPLIT_BY_DATATYPE,	
    						"-e" +  ResultFilesConverter.ERROR_TXNS_RENAME };
    	
		ResultFilesConverter resultFilesConverter = new ResultFilesConverter(); 
		resultFilesConverter.parseArguments(args);    
		resultFilesConverter.clearOutputDirectory();     
		actualSamplesCount=resultFilesConverter.convert();
        assertEquals(122, actualSamplesCount);
        
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_CPU_UTIL.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_DATAPOINT.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV_MEMORY.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE/jmterResultsFileConvertedToCSV.csv"));
    	Files.delete(Paths.get("./TESTDATA/MERGED_SPLIT_BY_DATAYPE"));
    }
    
}
