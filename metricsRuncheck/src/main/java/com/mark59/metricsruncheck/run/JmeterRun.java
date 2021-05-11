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

package com.mark59.metricsruncheck.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.EventMapping;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.TestTransaction;
import com.mark59.metrics.data.beans.Transaction;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
	
/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class JmeterRun extends PerformanceTest  {

	private static final String IGNORE ="IGNORE";
	
	private Map<String,String> optimizedTxnTypeLookup = new HashMap<String, String>();;
	private Map<String,EventMapping> txnIdToEventMappingLookup = new HashMap<String, EventMapping>();
	
	private int fieldPostimeStamp;
	private int fieldPoselapsed;	
	private int fieldPoslabel;	
	private int fieldPosdataType;	
	private int fieldPossuccess;	

	
	public JmeterRun(ApplicationContext context, String application, String inputdirectory, String runReference, String excludestart, String captureperiod, String keeprawresults) {
		
		super(context,application, runReference);
		
		//clean up before  
		testTransactionsDAO.deleteAllForRun(run);  // RUN_TIME_YET_TO_BE_CALCULATED
		
		loadTestTransactionAllDataFromJmeterFiles(run.getApplication(), inputdirectory);
		
		DateRangeBean dateRangeBean = getRunDateRangeUsingTestTransactionalData(run.getApplication());
		run = new Run( calculateAndSetRunTimesUsingEpochStartAndEnd(run, dateRangeBean));
		runDAO.deleteRun(run.getApplication(), run.getRunTime());
		runDAO.insertRun(run);

		applyTimingRangeFilters(excludestart, captureperiod, dateRangeBean);
		transactionDAO.deleteAllForRun(run.getApplication(), run.getRunTime());	
		
		storeTransactionSummaries(run);
		
		storeSystemMetricSummaries(run);
		
		if (String.valueOf(true).equalsIgnoreCase(keeprawresults)) {
			testTransactionsDAO.deleteAllForRun(run); // clean up in case of re-run (when the data already exists because this is a re-run)
			testTransactionsDAO.updateRunTime(run.getApplication(), AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED, run.getRunTime());
		}
	}


	private void loadTestTransactionAllDataFromJmeterFiles(String application, String inputdirectory) {
		int sampleCount = 0;
		File[] jmeterResultsDirFiles = new File(inputdirectory).listFiles();;
		
		if (jmeterResultsDirFiles == null){ 
			System.out.println("\n   Error : unable to access input directory '" + inputdirectory + "' files (is the directory missing or inaccesible?)\n");
			throw new RuntimeException("missing or inaccesible directory");
		}
		
		for (File jmeterResultsFile : jmeterResultsDirFiles){  
		
			if ( jmeterResultsFile.isFile() && (
					jmeterResultsFile.getName().toUpperCase().endsWith(".JTL") || 
					jmeterResultsFile.getName().toUpperCase().endsWith(".XML") || 
					jmeterResultsFile.getName().toUpperCase().endsWith(".CSV"))){
				try {
					sampleCount = sampleCount + loadTestTransactionDataForaJmeterFile(jmeterResultsFile, application);
				} catch (IOException e) {
					System.out.println( "Error: problem with processing Jmeter results file transactions " + jmeterResultsFile.getName() );
					e.printStackTrace();
				}
			} else {
				System.out.println("   " + jmeterResultsFile.getName() + " bypassed (only files in the input folder with a suffix of .xml, .csv or .jtl are processed)"  ); 
			}
		}
		
	    System.out.println("____________________________________" );
	    System.out.println(sampleCount + " Total samples written" );
	    System.out.println(" " );	    
	}



	/**
	 * A validly name named jmeter results file is expected to be passed for conversion, now need determine the data format 
	 */
	private int loadTestTransactionDataForaJmeterFile(File jmeterResultsFile, String application) throws IOException {

		BufferedReader brOneLine = new BufferedReader(new FileReader(jmeterResultsFile));
		String firstLineOfFile = brOneLine.readLine();
		brOneLine.close();
		
		if (firstLineOfFile == null) {
			System.out.println("   Warning : " + jmeterResultsFile.getName() + " bypassed - empty file" );
			return 0;
		
		} else  if (firstLineOfFile.trim().startsWith("<")) {
			return loadXMLFile(jmeterResultsFile, application);
			
		} else if (firstLineOfFile.trim().startsWith("timeStamp") &&  firstLineOfFile.matches("timeStamp.elapsed.*")){	
			return loadCSVFile(jmeterResultsFile, true, application);

		} else if ( firstLineOfFile.length() > 28 && StringUtils.countMatches(firstLineOfFile, ",") > 14  && firstLineOfFile.indexOf(",") == 13   ){  
			//assuming a headerless CSV file in default layout	
			return loadCSVFile(jmeterResultsFile, false, application);
			
		} else {
			System.out.println("   Warning : " + jmeterResultsFile.getName()
					+ " bypassed - not in expected Jmeter results format. (Does not start with regex 'timeStamp.elapsed' (csv) or '<' (xml))");
			return 0;
		}
	}	

	
	private int loadXMLFile(File inputXmlFileName, String application) throws IOException {

		long startLoadms = System.currentTimeMillis(); 
		System.out.println("\n\nProcessing Xml formatted Jmeter Results File " + inputXmlFileName.getName() + " at " + new Date(startLoadms));		

		int samplesCreated=0;
		BufferedReader xmlReader = new BufferedReader(new FileReader(inputXmlFileName));
		int lineCount = 0; 
		boolean isWithinASampleResult = false;
		String potentialSampleResultWithNoSubResults = null;
		List<TestTransaction> testTransactionList = new ArrayList<TestTransaction>();
		
	    for (String jmeterFileLine; (jmeterFileLine = xmlReader.readLine()) != null; ){
	    	jmeterFileLine = jmeterFileLine.trim();
	    	
	    	if ( jmeterXmlLineIsAClosedSample(jmeterFileLine) ) {
	    		samplesCreated = samplesCreated + addSampleToTestTransactionList(testTransactionList, jmeterFileLine, application);
    			potentialSampleResultWithNoSubResults = null;
    			
	    	} else if ( jmeterXmlLineIsAnUnclosedSample(jmeterFileLine) ){   
	    		
	    		if (isWithinASampleResult) {
		    		samplesCreated = samplesCreated + addSampleToTestTransactionList(testTransactionList, jmeterFileLine, application);
	    			potentialSampleResultWithNoSubResults = null;
	    			lineCount = readLinesToSubResultEndTag(xmlReader, lineCount);
	    			
	    		} else { // at a main (parent) result. Bypassed as parent results are not reported in Trend Analysis
	    			isWithinASampleResult = true;
	    			potentialSampleResultWithNoSubResults = jmeterFileLine;
	    		}
	    		
	    	} else if ( jmeterXmlLineIsAnEndSampleTag(jmeterFileLine) ){  
    			
	    		if (potentialSampleResultWithNoSubResults != null ){
		    		samplesCreated = samplesCreated + addSampleToTestTransactionList(testTransactionList, jmeterFileLine, application);
	    		}	
	    		isWithinASampleResult = false;
	    		potentialSampleResultWithNoSubResults = null;
	    	}

	    	lineCountProgressDisplay(lineCount);
	    	lineCount++;
	    	
	    	if ( (samplesCreated % 100 ) == 0 ){
	    		testTransactionsDAO.insertMultiple(testTransactionList);
	    		testTransactionList.clear();
	    	}
	    	
	    } 
	    
		testTransactionsDAO.insertMultiple(testTransactionList);
		testTransactionList.clear();	    
	    
		long endLoadms = System.currentTimeMillis(); 	    
		System.out.println("\n   " + inputXmlFileName.getName() + " file uploaded at " +  new Date(endLoadms) + " :" );
	    System.out.println("        " + lineCount + " file lines processed" );
	    System.out.println("        " + samplesCreated + " transaction samples created" );
		System.out.println("        took " +  (endLoadms -startLoadms)/1000 + " secs" );	    
		System.out.println();
	    
	    xmlReader.close();
	    return samplesCreated;
	}


	private boolean jmeterXmlLineIsAClosedSample(String trimmedJmeterFileLine) {
		boolean isUnclosedSample = jmeterXmlLineIsASample(trimmedJmeterFileLine) && trimmedJmeterFileLine.endsWith("/>") ?true:false;	
		return isUnclosedSample;
	}
	
	private boolean jmeterXmlLineIsAnUnclosedSample(String trimmedJmeterFileLine) {
		boolean isUnclosedSample = jmeterXmlLineIsASample(trimmedJmeterFileLine) && trimmedJmeterFileLine.endsWith("\">") ?true:false;	
		return isUnclosedSample;
	}

	private boolean jmeterXmlLineIsASample(String trimmedJmeterFileLine) {
		boolean isSample = trimmedJmeterFileLine.startsWith("<sample") || trimmedJmeterFileLine.startsWith("<httpSample") ?true:false;	
		return isSample;
	}
	
	private boolean jmeterXmlLineIsAnEndSampleTag(String trimmedJmeterFileLine) {
		boolean isSample = trimmedJmeterFileLine.startsWith("</sample>") || trimmedJmeterFileLine.startsWith("</httpSample>") ?true:false;	
		return isSample;
	}	

	private int readLinesToSubResultEndTag(BufferedReader xmlReader, int lineCount) throws IOException {
	    for (String jmeterFileLine; (jmeterFileLine = xmlReader.readLine()) != null; ){
	    	lineCountProgressDisplay(lineCount);
	    	lineCount++;
	    	jmeterFileLine = jmeterFileLine.trim();
	    	if (jmeterXmlLineIsAnEndSampleTag(jmeterFileLine)) {
	    		return lineCount;
	    	}			
	    }	
	    throw new RuntimeException("Read to end of file when looking for and sub-result end tag!!!"  );
	}

	private void lineCountProgressDisplay(int lineCount) {
		if ( (lineCount % 1000 )   == 0 ){	System.out.print("^");};
		if ( (lineCount % 100000 ) == 0 ){	System.out.println();};
	}
	
	
	private int addSampleToTestTransactionList(List<TestTransaction> testTransactionList, String jmeterFileLine, String application) {
		int samplesCreatedForLine = 0;
		
		TestTransaction testTransaction = extractTransactionFromJmeterXMLfile(jmeterFileLine);
		
		if (!testTransaction.getTxnId().startsWith(IGNORE)){
			testTransaction.setApplication(application);
			testTransaction.setRunTime(AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);	
			testTransactionList.add(testTransaction);
			samplesCreatedForLine = 1;
		}
		return samplesCreatedForLine;
	}		
	

	private TestTransaction extractTransactionFromJmeterXMLfile(String jmeterFileLine) {
		TestTransaction testTransaction = new TestTransaction();
		testTransaction.setTxnId(StringUtils.substringBetween(jmeterFileLine, " lb=\"", "\""));
		
		String sampleLineRawDbTxnType = Mark59Utils.convertJMeterFileDatatypeToDbTxntype(StringUtils.substringBetween(jmeterFileLine, " dt=\"", "\""));
		
		testTransaction.setTxnType( eventMappingTxnTypeTransform(testTransaction.getTxnId(), AppConstantsMetrics.JMETER, sampleLineRawDbTxnType));
			
		//		The response time ("t=") holds the value to be reported for all sample types. Note: 
		//		- the taken to be milliseconds for all timed TRANSACTION samples in the Jmeter results file. The metrics (trend analysis) database 
		//	      holds transaction values in seconds, so we divide by 1000 (response times back to seconds)  		
		//		- TODO: is assumed to be multiplied by 1000 for raw Perfmon captured metrics will not be marked by the DATAPOINT indicator in the
		//	      return code, but to be handed by Event Mapping lookup?		
		
		String txnResultMsStr = StringUtils.substringBetween(jmeterFileLine, " t=\"", "\"");
		BigDecimal txnResultMsBigD = new BigDecimal(txnResultMsStr);
		if ( Mark59Constants.DatabaseTxnTypes.TRANSACTION.name().equals(testTransaction.getTxnType())) {
			testTransaction.setTxnResult( txnResultMsBigD.divide(AppConstantsMetrics.THOUSAND, 3, RoundingMode.HALF_UP)  );			
		} else {
			try {
				testTransaction.setTxnResult( validateAndDetermineMetricValue(txnResultMsBigD, testTransaction.getTxnType()));		
			} catch (Exception e) {
				invalidDatapointMessageAndFail(jmeterFileLine, e);
			}
		}
				
		testTransaction.setTxnPassed("N");
		if ( "true".equalsIgnoreCase(StringUtils.substringBetween(jmeterFileLine, " s=\"", "\""))){
			testTransaction.setTxnPassed("Y");
		}
				
		testTransaction.setTxnEpochTime( StringUtils.substringBetween(jmeterFileLine, " ts=\"", "\"") );
		return testTransaction;
	}
	

	/**
	 * CSV files are treated in a similarly manner to XML.  The main difference is that due to the flat structure of CSV files, it is not  
	 * obvious which line relate to a stand-alone result, a parent result, or sub-results in within a parent.
	 * <p>In the Mark59 framework the data type field has been used and is set to "PARENT" for result lines which are (normally) expected 
	 * to have sub-results. 
	 * <p>PARENT transaction are not reported within Trend Analysis, so are bypassed here. 
	 * 
	 * @param inputCsvFileName
	 * @param application
	 * @return
	 * @throws IOException
	 */
	private int loadCSVFile(File inputCsvFileName, boolean hasHeader, String application) throws IOException {
		
		int samplesCreated=0; 
		CSVReader csvReader = new CSVReader(new BufferedReader(new FileReader(inputCsvFileName)));
		int lineCount = 0; 
		
		long startLoadms = System.currentTimeMillis(); 
		System.out.println("\n\nProcessing CSV formatted Jmeter Results File " + inputCsvFileName.getName() + " at " + new Date(startLoadms));					
		
		if (hasHeader) { 
			List<String> csvHeaderFieldsList = new ArrayList<String>();
			try {
				csvHeaderFieldsList = Arrays.asList(csvReader.readNext());
			} catch (CsvValidationException e) {
				e.printStackTrace();
				csvReader.close();
				throw new RuntimeException("failed to process expected CVS header fields (line 1 of file) " + e.getMessage());
			}

			fieldPostimeStamp = csvHeaderFieldsList.indexOf("timeStamp"); 
			fieldPoselapsed   = csvHeaderFieldsList.indexOf("elapsed"); 
			fieldPoslabel     = csvHeaderFieldsList.indexOf("label"); 
			fieldPosdataType  = csvHeaderFieldsList.indexOf("dataType"); 
			fieldPossuccess   = csvHeaderFieldsList.indexOf("success"); 
			
			if (fieldPostimeStamp==-1 || fieldPoselapsed==-1 || fieldPoslabel==-1 || fieldPosdataType==-1 || fieldPossuccess==-1 ){
				System.out.println("\n   Severe Error.  Unexpected csv file header format, terminating run");
				System.out.println("   - the header is expected to contain at least these field names:  timeStamp, elapsed, label, dataType, success\n");
				csvReader.close();
				throw new RuntimeException("Error : Unexpected csv file header format for file " + inputCsvFileName.getName());
			}
			
		} else {
			setFieldPositionsAssumingTheDefaultCsvLayout();
		}

		List<TestTransaction> testTransactionList = new ArrayList<TestTransaction>();
		String[] csvDataLineFields = csvReadNextLine(csvReader, inputCsvFileName);
		
	   	while ( csvDataLineFields != null ) {
	   		
	   		// if not enough fields or first field cannot be a time stamp, bypass 	
			if  ( ! (   csvDataLineFields.length < 5 || csvDataLineFields[0].length() == 12 )){
 
	    		String transactionNameLabel = csvDataLineFields[fieldPoslabel];
	    		String inputDatatype 		= csvDataLineFields[fieldPosdataType];

	    		if (!transactionNameLabel.startsWith(IGNORE) &&
	    			!inputDatatype.equals(JMeterFileDatatypes.PARENT.getDatatypeText() )){
	    			addCsvSampleToTestTransactionList(testTransactionList, csvDataLineFields, application);
					samplesCreated++;
		    	} 	    		
	    	} 

			lineCountProgressDisplay(lineCount);
	    	lineCount++;
	    	csvDataLineFields = csvReadNextLine(csvReader, inputCsvFileName);
	    	
	    	if ( (samplesCreated % 100 ) == 0 ){
	    		testTransactionsDAO.insertMultiple(testTransactionList);
	    		testTransactionList.clear();
	    	}

	    } // end for loop
	    
		testTransactionsDAO.insertMultiple(testTransactionList);
		testTransactionList.clear();	    
	    
		long endLoadms = System.currentTimeMillis(); 	    
		System.out.println("\n   " + inputCsvFileName.getName() + "  file uploaded at " +  new Date(endLoadms) + " :" );
	    System.out.println("        " + lineCount + " file lines processed" );
	    System.out.println("        " + samplesCreated + " transaction samples created" );
		System.out.println("        took " +  (endLoadms - startLoadms)/1000 + " secs" );	    
		System.out.println();
	    
	    csvReader.close();
		return samplesCreated;
	}


	private String[] csvReadNextLine( CSVReader csvReader, File inputCsvFileName) throws IOException {
		String[] csvDataLineFields = null;
		try {
			csvDataLineFields = csvReader.readNext();
		} catch (CsvValidationException e) {
			csvReader.close();
			System.out.println("Error :  Unexpected csv line format for file " + inputCsvFileName.getName() + 
					" Records count at time of failure : "  +  csvReader.getRecordsRead() + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return csvDataLineFields;
	}

	
	private void setFieldPositionsAssumingTheDefaultCsvLayout() {
		//timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect
		System.out.println("\n   This file is assumed to be a CSV file WITHOUT A HEADER.  Therefore the default Jmeter CSV field layout is assumed." );
		fieldPostimeStamp 		= 0; 
		fieldPoselapsed   		= 1; 
		fieldPoslabel     		= 2;
		fieldPosdataType  		= 6; 
		fieldPossuccess   		= 7; 
	}

	private void addCsvSampleToTestTransactionList(List<TestTransaction> testTransactionList, String[] csvDataLineFields, String application) {
		TestTransaction testTransaction = extractTransactionFromJmeterCSVsample(csvDataLineFields);
		testTransaction.setApplication(application);
		testTransaction.setRunTime(AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);
		testTransactionList.add(testTransaction);		
	}

	private TestTransaction extractTransactionFromJmeterCSVsample(String[] csvDataLineFields) {
		TestTransaction testTransaction = new TestTransaction();

		testTransaction.setTxnId(csvDataLineFields[fieldPoslabel]);
		
		String sampleLineRawDbTxnType = Mark59Utils.convertJMeterFileDatatypeToDbTxntype( csvDataLineFields[fieldPosdataType]  );
		
		testTransaction.setTxnType( eventMappingTxnTypeTransform(testTransaction.getTxnId(), AppConstantsMetrics.JMETER, sampleLineRawDbTxnType));
				
//		The response time ("elapsed" column) holds the value to be reported for all sample types. Note: 
//			- the taken to be milliseconds for all timed TRANSACTION samples in the Jmeter results file. The metrics (trend analysis) database 
//		      holds transaction values in seconds, so we divide by 1000 (response times back to seconds)  		
//			- TODO: is assumed to be multiplied by 1000 for raw Perfmon captured metrics will not be marked by the DATAPOINT indicator in the
//		      return code, but to be handed by Event Mapping lookup?
		
		BigDecimal txnResultMsBigD = new BigDecimal(csvDataLineFields[fieldPoselapsed]);
		testTransaction.setTxnResult( txnResultMsBigD.divide(AppConstantsMetrics.THOUSAND, 3, RoundingMode.HALF_UP)  );			
	
		if ( Mark59Constants.DatabaseTxnTypes.TRANSACTION.name().equals(testTransaction.getTxnType())) {
			testTransaction.setTxnResult( txnResultMsBigD.divide(AppConstantsMetrics.THOUSAND, 3, RoundingMode.HALF_UP)  );			
		} else {
			try {
				testTransaction.setTxnResult( validateAndDetermineMetricValue(txnResultMsBigD, testTransaction.getTxnType()));
			} catch (Exception e) {
				invalidDatapointMessageAndFail(Arrays.toString(csvDataLineFields), e);
			}
		}		
		
		testTransaction.setTxnPassed("N");
		if ( "true".equalsIgnoreCase(csvDataLineFields[fieldPossuccess])){
			testTransaction.setTxnPassed("Y");
		}
				
		testTransaction.setTxnEpochTime(csvDataLineFields[fieldPostimeStamp]);
		return testTransaction;
	}


	private void invalidDatapointMessageAndFail(String jmeterFileLine, Exception e) {
		System.out.println("!! Error : looks like an invalid datapoint value or type has been entered. ");
		System.out.println("           Time (t) must be an integer.  Datatype (dt) must be a know datatype + optional multiplier.  eg DATAPOINT or CPU_1000 .. ");
		System.out.println("           The line (by field for csv) uin issue:  ");
		System.out.println("           " + jmeterFileLine);	
		e.printStackTrace();
		throw new RuntimeException("  " + e.getClass());
	}
	
	

	
	/**
	 * If an event mapping is found for the given transaction / tool / Database Data type (relating to a a sample line), 
	 * then the Database Data type for that mapping is returned<br>
	 * If a event mapping for the sample line is not found, then it is taken to be a TRANSACTION<br>
	 * TODO: PERFMON<br>
	 * TODO: also allow for transforms to TRANSACTION in event mapping<br>
	 * @param txnId
	 * @param performanceTool
	 * @param sampleLineDbDataType -will be a string value of enum Mark59Constants.DatabaseDatatypes (DATAPOINT, CPU_UTIL, MEMORY, TRANSACTION)
	 * @return txnType -  will be a string value of enum Mark59Constants.DatabaseDatatypes (DATAPOINT, CPU_UTIL, MEMORY, TRANSACTION)
	 */
	private String eventMappingTxnTypeTransform(String txnId, String performanceTool, String sampleLineRawDbDataType) {
		
		String eventMappingTxnType = null;
		String metricSource = performanceTool + "_" + sampleLineRawDbDataType;   // (eg 'Jmeter_CPU_UTIL',  'Jmeter_TRANSACTION' ..)
		
		String txnId_MetricSource_Key = txnId + "-" + metricSource; 
			
		if (optimizedTxnTypeLookup.get(txnId_MetricSource_Key) != null ){
			
			//As we could be processing large files, a Map of type by transaction ids (labels) is held for ids that have already had a lookup on the eventMapping table.  
			// Done to minimise sql calls - each different label / data type in the jmeter file just gets one lookup to see if it has a match on Event Mapping table.
			
			eventMappingTxnType = optimizedTxnTypeLookup.get(txnId_MetricSource_Key);
			
		} else {
			
			eventMappingTxnType = Mark59Constants.DatabaseTxnTypes.TRANSACTION.name();   
			
			EventMapping eventMapping = eventMappingDAO.findAnEventForTxnIdAndSource(txnId, metricSource);
			
			if ( eventMapping != null ) {
				// this not a standard TRANSACTION (it's one of the metric types) - store eventMapping for later use 
				eventMappingTxnType = eventMapping.getTxnType();
				txnIdToEventMappingLookup.put(txnId, eventMapping);
			}
			optimizedTxnTypeLookup.put(txnId_MetricSource_Key, eventMappingTxnType);
		}
		return eventMappingTxnType;
	}


	/**
	 * Calculate the datapoint value, using the multiplier passed in the jmeter datatype ("dt") field.
	 * 
	 *  eg dt=DATAPOINT_1000  - the value loaded to the database will become the passed numeric value / 1000 
	 *  DATAPOINT, DATAPOINT_1 will use the value as passed.   
	 */
	private BigDecimal validateAndDetermineMetricValue(BigDecimal txnResultMsBigD, String sampleLineDataType) throws Exception {
		
		for ( String metricDataType : Mark59Constants.DatabaseTxnTypes.listOfMetricDatabaseTxnTypes()) {
			
			if (sampleLineDataType.startsWith(metricDataType)) {
				BigDecimal valueMultiplier = new BigDecimal(1L);
				
				String passedMultipler =   sampleLineDataType.replace(metricDataType, "" ).replace("_", "") ;  
				// eg 'CPU_UTIL_1000' should return 1000,  'CPU_UTIL' empty string
				
				if (StringUtils.isNotBlank(passedMultipler)) {
					valueMultiplier = new BigDecimal(passedMultipler);
				}

				return txnResultMsBigD.divide(valueMultiplier, 3, RoundingMode.HALF_UP) ;
			}
		}

	    throw new Exception("ERORR unexpected datatype present :  " +  sampleLineDataType    ) ;
	}

		
	/**
	 * Once all transaction and metrics data has been stored for the run, work out the start and end 
	 * time for the run.  Start/end times are taken lowest and highest transaction epoch time for the
	 * application run. 
	 *  
	 * The times are actually an approximation, as any time difference between the timestamp and the time
	 * to take the sample is not considered, nor is any running time before/after the first/last sample.
	 * 
	 * NOTE: When this method is called currently assumed the run being processed will have a  
	 * run-time of AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED (zeros) on TESTTRANSACTIONS 	  
	 */
	private DateRangeBean getRunDateRangeUsingTestTransactionalData(String application){
		Long runStartTime = testTransactionsDAO.getEarliestTimestamp(application);
		Long runEndTime   = testTransactionsDAO.getLatestTimestamp(application);
		return new DateRangeBean(runStartTime, runEndTime);
	}

	
	private void storeSystemMetricSummaries(Run run) {

		// Creates a list of the names of metric transactions for the run, with their types (bit of an abuse of the 'TestTransaction' bean)  
		List<TestTransaction> dataSampleTxnkeys = testTransactionsDAO.getUniqueListOfSystemMetricNamesByType(run.getApplication()); 
		
		for (TestTransaction dataSampleKey : dataSampleTxnkeys) {

			EventMapping eventMapping = txnIdToEventMappingLookup.get(dataSampleKey.getTxnId());
			if (eventMapping == null) {
				throw new RuntimeException("ERROR : No event mapping found for " + dataSampleKey.getTxnId());
			};
			Transaction eventTransaction = testTransactionsDAO.extractEventSummaryStats(run.getApplication(), dataSampleKey.getTxnType(), dataSampleKey.getTxnId(), eventMapping);
			eventTransaction.setRunTime(run.getRunTime());
			transactionDAO.insert(eventTransaction);
		} 
	}

}