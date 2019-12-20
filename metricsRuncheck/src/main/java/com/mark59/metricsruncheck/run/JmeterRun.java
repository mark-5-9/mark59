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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.mark59.metrics.application.AppConstants;
import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.EventMapping;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.TestTransaction;
import com.mark59.metrics.data.beans.Transaction;
	
/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class JmeterRun extends PerformanceTest  {

	private static final String PARENT ="PARENT";
	// this value is based on a value in mark59 core enum OutputDatatypes - any update to the core version may need to be reflected here.	

	private static final String IGNORE ="IGNORE";
	
	private Map<String,String> optimizedTxnTypeLookup = new HashMap<String, String>();;
	private Map<String,EventMapping> txnIdToEventMappingLookup = new HashMap<String, EventMapping>();
	
	private int fieldPostimeStamp;
	private int fieldPoselapsed;	
	private int fieldPoslabel;	
	private int fieldPosdataType;	
	private int fieldPossuccess;	

	
	public JmeterRun(ApplicationContext context, String application, String inputdirectory, String runReference, String excludestart, String captureperiod) {
		
		super(context,application, runReference);
		
		//clean up before  
		testTransactionsDAO.deleteAllForApplication(run.getApplication());
		loadTestTransactionAllDataFromJmeterFiles(run.getApplication(), inputdirectory);
		
		DateRangeBean dateRangeBean = getRunDateRangeUsingTestTransactionalData(run.getApplication());
		run = new Run( calculateAndSetRunTimesUsingEpochStartAndEnd(run, dateRangeBean));
		runDAO.deleteRun(run.getApplication(), run.getRunTime());
		runDAO.insertRun(run);
				
		applyTimingRangeFilters(excludestart, captureperiod, dateRangeBean);

		transactionDAO.deleteAllForRun(run.getApplication(), run.getRunTime());				
		storeTransactionSummaries(run);

		
		storeSystemMetricSummaries(run);
		
		//clean up after (leave commented out to keep the last load of an application on the testTransactions table)
		//testTransactionsDAO.deleteAllForApplication(run.getApplication());				
		
	}



	private void loadTestTransactionAllDataFromJmeterFiles(String application, String inputdirectory) {
		int sampleCount = 0;
		
		for (File jmeterResultsFile : new File(inputdirectory).listFiles()){  
		
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

		} else if ( firstLineOfFile.length() > 28 && StringUtils.countMatches(firstLineOfFile, ",") > 14  && firstLineOfFile.indexOf(",") == 13   ){  //assuming a headerless CSV file in default layout	
			return loadCSVFile(jmeterResultsFile, false, application);
			
		} else {
			System.out.println("   Warning : " + jmeterResultsFile.getName() + " bypassed - not in expected Jmeter results format. (Does not start with regex 'timeStamp.elapsed' (csv) or '<' (xml))" );
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
	    			
	    		} else { 
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
		System.out.println("\n   " + inputXmlFileName.getName() + " processing completed  at " +  new Date(endLoadms) + " :" );
	    System.out.println("        " + lineCount + " file lines processed" );
	    System.out.println("        " + samplesCreated + " transaction samples loaded" );
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
			testTransaction.setRunTime(AppConstants.RUN_TIME_YET_TO_BE_CALCULATED);	
			testTransactionList.add(testTransaction);
			samplesCreatedForLine = 1;
		}
		return samplesCreatedForLine;
	}		
	

	private TestTransaction extractTransactionFromJmeterXMLfile(String jmeterFileLine) {
		TestTransaction testTransaction = new TestTransaction();
		testTransaction.setTxnId(StringUtils.substringBetween(jmeterFileLine, " lb=\"", "\""));
		
		String sampleLineDataType = StringUtils.substringBetween(jmeterFileLine, " dt=\"", "\"");
		if ( StringUtils.isBlank( sampleLineDataType )){ 
			sampleLineDataType = AppConstants.MAPPED_DATA_TYPES.TRANSACTION.name();   
		}
		
		testTransaction.setTxnType( determineTransactionType(testTransaction.getTxnId(), AppConstants.JMETER, sampleLineDataType));
			
		// The response time ("t=") holds the value to be reported for all sample types, for transactions (ie, not data samples) it assumed to be milliseconds.
		//  TODO: perfmon stats?		
		
		String txnResultMsStr = StringUtils.substringBetween(jmeterFileLine, " t=\"", "\"");
		BigDecimal txnResultMsBigD = new BigDecimal(txnResultMsStr);
		if ( AppConstants.MAPPED_DATA_TYPES.TRANSACTION.name().equals(testTransaction.getTxnType())) {
			testTransaction.setTxnResult( txnResultMsBigD.divide(AppConstants.THOUSAND, 3, BigDecimal.ROUND_HALF_UP)  );			
		} else {
			try {
				testTransaction.setTxnResult( determineDatapointValue(txnResultMsBigD, testTransaction.getTxnType()));		
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
	 * 	CSV files are treated in a similarly to XML.  The main difference is that results with sub-results are non easily detectable using
	 *  the file structure and labels produced via mark59, so the dataType setting of "MAIN" is used to detect result lines produced by
	 *  the mark59 framework which are (normally) expected to have sub-results. 
	 * 
	 * @param inputCsvFileName
	 * @param application
	 * @return
	 * @throws IOException
	 */
	private int loadCSVFile(File inputCsvFileName, boolean hasHeader, String application) throws IOException {
		
		int samplesCreated=0; 
		BufferedReader csvReader = new BufferedReader(new FileReader(inputCsvFileName));
		int lineCount = 0; 
		String csvDelimiter= ",";
		
		long startLoadms = System.currentTimeMillis(); 
		System.out.println("\n\nProcessing CSV formatted Jmeter Results File " + inputCsvFileName.getName() + " at " + new Date(startLoadms));					
		
		if (hasHeader) { 
			String csvHeader = csvReader.readLine();
			csvDelimiter = StringUtils.substringBetween(csvHeader, "timeStamp", "elapsed");
			
			List<String> csvHeaderFieldsList =  Arrays.asList(csvHeader.trim().split(Pattern.quote(csvDelimiter)));  
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
		
	    for (String csvDataLine; (csvDataLine = csvReader.readLine()) != null; ) {
	    	
	    	long approxFieldCount =  StringUtils.countMatches(csvDataLine, csvDelimiter);

			if  ( !(  (csvDataLine.trim().length() < 17 )  || approxFieldCount < 4 ||  (csvDataLine.trim().indexOf(",") != 13) )){
				//would be too short,  not enough fields or first field cannot be a time stamp .. so bypass 		
		    	//format all csv files the same way (the header is always the same) ...
		    	
		    	String[] csvDataLineFields =  csvDataLine.trim().split(Pattern.quote(csvDelimiter));  
	    		String transactionNameLabel = csvDataLineFields[fieldPoslabel];
	    		String inputDatatype 		= csvDataLineFields[fieldPosdataType];

	    		if (!transactionNameLabel.startsWith(IGNORE) &&
	    			!inputDatatype.equals(PARENT) ){
	    			addCsvSampleToTestTransactionList(testTransactionList, csvDataLine, csvDelimiter, application);
					samplesCreated++;
		    	} 	    		
	    	} 

			lineCountProgressDisplay(lineCount);
	    	lineCount++;
	    	
	    	if ( (samplesCreated % 100 ) == 0 ){
	    		testTransactionsDAO.insertMultiple(testTransactionList);
	    		testTransactionList.clear();
	    	}

	    } // end for loop
	    
		testTransactionsDAO.insertMultiple(testTransactionList);
		testTransactionList.clear();	    
	    
		long endLoadms = System.currentTimeMillis(); 	    
		System.out.println("\n   " + inputCsvFileName.getName() + " processing completed  at " +  new Date(endLoadms) + " :" );
	    System.out.println("        " + lineCount + " file lines processed" );
	    System.out.println("        " + samplesCreated + " transaction samples loaded" );
		System.out.println("        took " +  (endLoadms - startLoadms)/1000 + " secs" );	    
		System.out.println();
	    
	    csvReader.close();
		return samplesCreated;
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

	private void addCsvSampleToTestTransactionList(List<TestTransaction> testTransactionList, String csvDataLine, String csvDelimiter, String application) {
		TestTransaction testTransaction = extractTransactionFromJmeterCSVsample(csvDataLine, csvDelimiter);
		testTransaction.setApplication(application);
		testTransaction.setRunTime(AppConstants.RUN_TIME_YET_TO_BE_CALCULATED);
		testTransactionList.add(testTransaction);		
	}

	private TestTransaction extractTransactionFromJmeterCSVsample(String csvDataLine, String csvDelimiter) {
		TestTransaction testTransaction = new TestTransaction();
		
		String[] csvDataLineFields =  csvDataLine.trim().split(Pattern.quote(csvDelimiter));  

		testTransaction.setTxnId(csvDataLineFields[fieldPoslabel]);
		
		String sampleLineDataType =  AppConstants.MAPPED_DATA_TYPES.TRANSACTION.name();   // most commonly the data type will be a TRANSACTION (dt='')
		if ( ! StringUtils.isBlank(csvDataLineFields[fieldPosdataType])){                 // blank means its a TRANSACTION 
			sampleLineDataType = csvDataLineFields[fieldPosdataType];
		}
		
		testTransaction.setTxnType( determineTransactionType(testTransaction.getTxnId(), AppConstants.JMETER, sampleLineDataType));
			
//		The response time ("elapsed" column) holds the value to be reported for all sample types, but  : 
//			is assumed to be milliseconds for all timed transaction samples 
//			is assumed to be multiplied by 1000 for raw Perfmon captured metrics (will not be marked by the DATAPOINT indicator in the return code, but assumed to be handed by Event Mapping lookup(TODO: !)
//			.. CI tool database holds transaction values in seconds so we divide by 1000 (response times back to seconds)  		
		
		BigDecimal txnResultMsBigD = new BigDecimal(csvDataLineFields[fieldPoselapsed]);
		testTransaction.setTxnResult( txnResultMsBigD.divide(AppConstants.THOUSAND, 3, BigDecimal.ROUND_HALF_UP)  );			
	
		if ( AppConstants.MAPPED_DATA_TYPES.TRANSACTION.name().equals(testTransaction.getTxnType())) {
			testTransaction.setTxnResult( txnResultMsBigD.divide(AppConstants.THOUSAND, 3, BigDecimal.ROUND_HALF_UP)  );			
		} else {
			try {
				testTransaction.setTxnResult( determineDatapointValue(txnResultMsBigD, testTransaction.getTxnType()));
			} catch (Exception e) {
				invalidDatapointMessageAndFail(csvDataLine, e);
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
		System.out.println("           The line in issue:  ");
		System.out.println("           " + jmeterFileLine);	
		e.printStackTrace();
		throw new RuntimeException("  " + e.getClass());
	}
	

	//TODO: PERFMON
	
	private String determineTransactionType(String txnId, String performanceTool, String sourceDataType) {
		
		String txnType = null;
		String metricSource = performanceTool + "_" + sourceDataType;
		
		String txnId_MetricSource_Key = txnId + "-" + metricSource; 
			
		if (optimizedTxnTypeLookup.get(txnId_MetricSource_Key) != null ){
			
			//As we could be processing large files, this is just using a Map of type by transaction ids (labels), for ids that have already have a lookup on the eventMapping table.  
			// Done to minimise sql calls.  Each different label id / data type in the jmeter file just gets one lookup to see if it has a match on Event Mapping table .....
			
			txnType = optimizedTxnTypeLookup.get(txnId_MetricSource_Key);
			
		} else {
			
			txnType = AppConstants.MAPPED_DATA_TYPES.TRANSACTION.name();
			EventMapping eventMapping = eventMappingDAO.findAnEventForTxnIdAndSource(txnId, metricSource);
			
			if ( eventMapping != null ) {
				// so this not a standard transaction - store eventMapping for later use 
				txnType = eventMapping.getTxnType();
				txnIdToEventMappingLookup.put(txnId, eventMapping);
			}
			optimizedTxnTypeLookup.put(txnId_MetricSource_Key, txnType);
		}
		return txnType;
	}

	
	private String extractMetricSourceType(String sampleLineDataType) throws Exception{
	    for (String keySourceByTool : AppConstants.getToolDataTypeToSourceValueMap().keySet()) {
	    	if ( keySourceByTool.startsWith(AppConstants.JMETER ) ) {
	    		if ( sampleLineDataType.startsWith( AppConstants.getToolDataTypeToSourceValueMap().get(keySourceByTool))){
	    			return AppConstants.getToolDataTypeToSourceValueMap().get(keySourceByTool);
	    		}
	    	}
	    }
	    throw new Exception("ERORR unexpected datatype present :  " +  sampleLineDataType    ) ;
	}	

	/**
	 * Calculate the datapoint value, using the multiplier passed in the jmeter datatype ("dt") field.
	 * 
	 *  eg dt=DATAPOINT_1000  - the loaded value is the passed numeric / 1000 
	 *  DATAPOINT, DATAPOINT_1 will use the value as passed.   
	 */
	private BigDecimal determineDatapointValue(BigDecimal txnResultMsBigD, String sampleLineDataType) throws Exception {
//		System.out.println("  at determineDatapointValue :  " +  txnResultMsBigD + " : " + sampleLineDataType );
		
		BigDecimal valueMultiplier = new BigDecimal(1L);
		String passedMultipler =   sampleLineDataType.replace(extractMetricSourceType(sampleLineDataType) , "" ).replace("_", "") ;    // eg 'CPU_UTIL_1000' should return 1000,  'CPU_UTIL' returns empty string
		
		if (StringUtils.isNotBlank(passedMultipler)) {
			valueMultiplier = new BigDecimal(passedMultipler);
		}

		return txnResultMsBigD.divide(valueMultiplier, 3, BigDecimal.ROUND_HALF_UP) ;
	}

		
	/**
	 * Once all transaction and metrics data has been stored for the run, work out the start and end time for the run. 
	 * Start/end times are taken lowest and highest transaction epoch time for the application (assumes data for only 1 run in table!) 
	 * Note this is actually an approximation, as any time difference between the timestamp and the time to take the sample is not considered, 
	 * nor is any running time before/after the first/last sample.      
	 */
	private DateRangeBean getRunDateRangeUsingTestTransactionalData(String application){
		Long runStartTime = testTransactionsDAO.getEarliestTimestamp(application);
		Long runEndTime   = testTransactionsDAO.getLatestTimestamp(application);
		return new DateRangeBean(runStartTime, runEndTime);
	}

	
	private void storeSystemMetricSummaries(Run run) {

		List<TestTransaction> dataSampleTxnkeys = testTransactionsDAO.getUniqueListOfSystemMetricNamesByType(run.getApplication()); 
		
		for (TestTransaction dataSampleKey : dataSampleTxnkeys) {

			EventMapping eventMapping = txnIdToEventMappingLookup.get(dataSampleKey.getTxnId());
			assert(eventMapping != null);
			
			Transaction eventTransaction = testTransactionsDAO.extractEventSummaryStats(run.getApplication(), dataSampleKey.getTxnType(), dataSampleKey.getTxnId(), eventMapping);
			eventTransaction.setRunTime(run.getRunTime());
			transactionDAO.insert(eventTransaction);
		} 
	}

}