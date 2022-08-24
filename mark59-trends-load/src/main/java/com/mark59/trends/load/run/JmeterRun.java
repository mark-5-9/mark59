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

package com.mark59.trends.load.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.trends.application.AppConstantsMetrics;
import com.mark59.trends.data.beans.DateRangeBean;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.beans.TestTransaction;
import com.mark59.core.utils.Mark59Utils;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
	
/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class JmeterRun extends PerformanceTest  {

	private static final String IGNORE ="IGNORE";
	private static final int MAX_ALLOWED_TXN_ID_CHARS = 128;
	
	private int fieldPostimeStamp;
	private int fieldPoselapsed;	
	private int fieldPoslabel;	
	private int fieldPosdataType;	
	private int fieldPossuccess;	
	private int fieldPosfailureMessage;	

	
	public JmeterRun(ApplicationContext context, String application, String inputdirectory, String runReference, String excludestart, String captureperiod,
			String keeprawresults, String ignoredErrors) {
		
		super(context,application, runReference);
		testTransactionsDAO.deleteAllForRun(run.getApplication(), AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);
		
		loadTestTransactionAllDataFromJmeterFiles(run.getApplication(), inputdirectory, ignoredErrors );
		
		DateRangeBean dateRangeBean = getRunDateRangeUsingTestTransactionalData(run.getApplication());
		run = new Run( calculateAndSetRunTimesUsingEpochStartAndEnd(run, dateRangeBean));
		runDAO.deleteRun(run.getApplication(), run.getRunTime());
		runDAO.insertRun(run);
		
		applyTimeRangeFiltersToTestTransactions(excludestart, captureperiod, dateRangeBean);

		transactionDAO.deleteAllForRun(run.getApplication(), run.getRunTime());	
		
		storeTransactionSummaries(run);
		
		storeMetricTransactionSummaries(run);
		
		endOfRunCleanupTestTransactions(keeprawresults);
	}


	private void loadTestTransactionAllDataFromJmeterFiles(String application, String inputdirectory, String ignoredErrors) {
		int sampleCount = 0;
		File[] jmeterResultsDirFiles = new File(inputdirectory).listFiles();
		
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
					sampleCount = sampleCount + loadTestTransactionDataForaJmeterFile(jmeterResultsFile, application, ignoredErrors);
				} catch (IOException e) {
					System.out.println( "Error : problem with processing Jmeter results file transactions " + jmeterResultsFile.getName() );
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					throw new RuntimeException(e.getMessage());
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
	private int loadTestTransactionDataForaJmeterFile(File jmeterResultsFile, String application, String ignoredErrors) throws IOException {

		BufferedReader brOneLine = new BufferedReader(new FileReader(jmeterResultsFile));
		String firstLineOfFile = brOneLine.readLine();
		brOneLine.close();
		
		if (firstLineOfFile == null) {
			System.out.println("   Warning : " + jmeterResultsFile.getName() + " bypassed - empty file" );
			return 0;
		
		} else  if (firstLineOfFile.trim().startsWith("<")) {
			if (StringUtils.isNotBlank(ignoredErrors)){
				System.out.println("   Warning : " + " the -e ('ignoredErrors') runtime option is not implemented for XML files");
			}
			return loadXMLFile(jmeterResultsFile, application);
			
		} else if (firstLineOfFile.trim().startsWith("timeStamp") &&  firstLineOfFile.matches("timeStamp.elapsed.*")){	
			return loadCSVFile(jmeterResultsFile, true, application, ignoredErrors);

		} else if ( firstLineOfFile.length() > 28 && StringUtils.countMatches(firstLineOfFile, ",") > 14  && firstLineOfFile.indexOf(",") == 13   ){  
			System.out.println("   Info : " + " the file " + jmeterResultsFile.getName() + " appears to be headerless (default field positions assumed)");
			return loadCSVFile(jmeterResultsFile, false, application, ignoredErrors);
			
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
		List<TestTransaction> testTransactionList = new ArrayList<>();
		
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
		return jmeterXmlLineIsASample(trimmedJmeterFileLine) && trimmedJmeterFileLine.endsWith("/>");
	}
	
	private boolean jmeterXmlLineIsAnUnclosedSample(String trimmedJmeterFileLine) {
		return jmeterXmlLineIsASample(trimmedJmeterFileLine) && trimmedJmeterFileLine.endsWith("\">");
	}

	private boolean jmeterXmlLineIsASample(String trimmedJmeterFileLine) {
		return trimmedJmeterFileLine.startsWith("<sample") || trimmedJmeterFileLine.startsWith("<httpSample");
	}
	
	private boolean jmeterXmlLineIsAnEndSampleTag(String trimmedJmeterFileLine) {
		return trimmedJmeterFileLine.startsWith("</sample>") || trimmedJmeterFileLine.startsWith("</httpSample>");
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
		if ( (lineCount % 1000 )   == 0 ){	System.out.print("^");}
        if ( (lineCount % 100000 ) == 0 ){	System.out.println();}
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
		testTransaction.setTxnId(truncateOverlyLongIds(StringUtils.substringBetween(jmeterFileLine, " lb=\"", "\"")));

		String jmeterFileDatatype = StringUtils.substringBetween(jmeterFileLine, " dt=\"", "\"");
		String sampleLineRawDbTxnType = Mark59Utils.convertJMeterFileDatatypeToDbTxntype(jmeterFileDatatype);
		testTransaction.setTxnType( eventMappingTxnTypeTransform(testTransaction.getTxnId(), AppConstantsMetrics.JMETER, sampleLineRawDbTxnType));
		
		testTransaction.setIsCdpTxn("N"); 
		if (JMeterFileDatatypes.CDP.name().equals(jmeterFileDatatype)){
			testTransaction.setIsCdpTxn("Y"); 
		}
			
		//		The response time ("t=") holds the value to be reported for all sample types. Note: 
		//		- the taken to be milliseconds for all timed TRANSACTION samples in the Jmeter results file. The Trends Analysis database 
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
	 * @param inputCsvFileName inputCsvFileName
	 * @param application application
	 * @return line count
	 * @throws IOException IOException
	 */
	private int loadCSVFile(File inputCsvFileName, boolean hasHeader, String application, String ignoredErrors) throws IOException {
		
		int samplesCreated=0; 
		CSVReader csvReader = new CSVReader(new BufferedReader(new FileReader(inputCsvFileName)));
		int lineCount = 0; 
		
		long startLoadms = System.currentTimeMillis(); 
		System.out.println("\n\nProcessing CSV formatted Jmeter Results File " + inputCsvFileName.getName() + " at " + new Date(startLoadms));					
		
		if (hasHeader) { 
			List<String> csvHeaderFieldsList;
			try {
				csvHeaderFieldsList = Arrays.asList(csvReader.readNext());
			} catch (CsvValidationException e) {
				e.printStackTrace();
				csvReader.close();
				throw new RuntimeException("failed to process expected CVS header fields (line 1 of file) " + e.getMessage());
			}

			fieldPostimeStamp      = csvHeaderFieldsList.indexOf("timeStamp"); 
			fieldPoselapsed        = csvHeaderFieldsList.indexOf("elapsed"); 
			fieldPoslabel          = csvHeaderFieldsList.indexOf("label"); 
			fieldPosdataType       = csvHeaderFieldsList.indexOf("dataType"); 
			fieldPossuccess        = csvHeaderFieldsList.indexOf("success"); 
			fieldPosfailureMessage = csvHeaderFieldsList.indexOf("failureMessage"); 
			
			if (fieldPostimeStamp==-1 || fieldPoselapsed==-1 || fieldPoslabel==-1 || fieldPosdataType==-1 || fieldPossuccess==-1 ){
				System.out.println("\n   Severe Error.  Unexpected csv file header format, terminating run");
				System.out.println("   - the header is expected to contain at least these field names:  timeStamp, elapsed, label, dataType, success\n");
				csvReader.close();
				throw new RuntimeException("Error : Unexpected csv file header format for file " + inputCsvFileName.getName());
			}
			
		} else {
			setFieldPositionsAssumingTheDefaultCsvLayout();
		}

		List<TestTransaction> testTransactionList = new ArrayList<>();
		String[] csvDataLineFields = csvReadNextLine(csvReader, inputCsvFileName);
		
		// at this point, should be at the first line of data in the file
		if  ( csvDataLineFields != null  && !StringUtils.isNumeric(csvDataLineFields[fieldPostimeStamp]) ) {
			throw new RuntimeException("Error :  Only elapsed times in epoch (millisecond) format can be processed ! "
				+ "\nFirst data line of file " + inputCsvFileName + " contains elapsed value of " + csvDataLineFields[fieldPostimeStamp]);
		}
		
		List<String> ignoredErrorsList = Mark59Utils.pipeDelimStringToStringList(ignoredErrors);
		
	   	while ( csvDataLineFields != null ) {
	   		
	   		// if not enough fields or first field cannot be a time stamp, bypass 	
			if  ( ! (   csvDataLineFields.length < 5 || csvDataLineFields[0].length() == 12 )){
 
	    		String transactionNameLabel = csvDataLineFields[fieldPoslabel];
	    		String inputDatatype 		= csvDataLineFields[fieldPosdataType];

	    		if (!transactionNameLabel.startsWith(IGNORE) &&  !inputDatatype.equals(JMeterFileDatatypes.PARENT.getDatatypeText() )){
	    			addCsvSampleToTestTransactionList(testTransactionList, csvDataLineFields, application, ignoredErrorsList);
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
		String[] csvDataLineFields;
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
		fieldPosfailureMessage	= 8; 
	}

	private void addCsvSampleToTestTransactionList(List<TestTransaction> testTransactionList, String[] csvDataLineFields, String application, List<String> ignoredErrorsList) {
		TestTransaction testTransaction = extractTransactionFromJmeterCSVsample(csvDataLineFields, ignoredErrorsList);
		testTransaction.setApplication(application);
		testTransaction.setRunTime(AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);
		testTransactionList.add(testTransaction);		
	}

	private TestTransaction extractTransactionFromJmeterCSVsample(String[] csvDataLineFields, List<String> ignoredErrorsList) {
		TestTransaction testTransaction = new TestTransaction();

		testTransaction.setTxnId(truncateOverlyLongIds(csvDataLineFields[fieldPoslabel]));
		
		String jmeterFileDatatype = csvDataLineFields[fieldPosdataType];
		String sampleLineRawDbTxnType = Mark59Utils.convertJMeterFileDatatypeToDbTxntype(jmeterFileDatatype);
		testTransaction.setTxnType( eventMappingTxnTypeTransform(testTransaction.getTxnId(), AppConstantsMetrics.JMETER, sampleLineRawDbTxnType));
		
		testTransaction.setIsCdpTxn("N"); 
		if (JMeterFileDatatypes.CDP.name().equals(jmeterFileDatatype)){
			testTransaction.setIsCdpTxn("Y"); 
		}
				
//		The response time ("elapsed" column) holds the value to be reported for all sample types. Note: 
//			- the taken to be milliseconds for all timed TRANSACTION samples in the Jmeter results file. The Trends Analysis database 
//		      holds transaction values in seconds, so we divide by 1000 (response times back to seconds)  		
//			- TODO: is assumed to be multiplied by 1000 for raw Perfmon captured metrics will not be marked by the DATAPOINT indicator in the
//		      return code, but to be handed by Event Mapping lookup?
		
		BigDecimal txnResultMsBigD = new BigDecimal(csvDataLineFields[fieldPoselapsed]);
	
		if ( Mark59Constants.DatabaseTxnTypes.TRANSACTION.name().equals(testTransaction.getTxnType())) {
			testTransaction.setTxnResult( txnResultMsBigD.divide(AppConstantsMetrics.THOUSAND, 3, RoundingMode.HALF_UP)  );	
		} else {	
			try {
				testTransaction.setTxnResult( validateAndDetermineMetricValue(txnResultMsBigD, testTransaction.getTxnType()));
			} catch (Exception e) {
				invalidDatapointMessageAndFail(Arrays.toString(csvDataLineFields), e);
			}
		}		

		testTransaction.setTxnPassed("Y");
		if ("false".equalsIgnoreCase(csvDataLineFields[fieldPossuccess]) && !errorToBeIgnored(csvDataLineFields[fieldPosfailureMessage], ignoredErrorsList)){
			testTransaction.setTxnPassed("N");
		}		
				
		testTransaction.setTxnEpochTime(csvDataLineFields[fieldPostimeStamp]);
		return testTransaction;
	}

	
	private String truncateOverlyLongIds(String txnId) {
		if (txnId.length() > MAX_ALLOWED_TXN_ID_CHARS ) {
			txnId = txnId.substring(0, MAX_ALLOWED_TXN_ID_CHARS - 3) + "...";
		}
		return txnId;
	}
	
	
	private void invalidDatapointMessageAndFail(String jmeterFileLine, Exception e) {
		System.out.println("!! Error : looks like an invalid datapoint value or type has been entered. ");
		System.out.println("           Time (t) must be an integer.  Datatype (dt) must be a know datatype + optional multiplier.  eg DATAPOINT or CPU_1000 .. ");
		System.out.println("           The line (by field for csv) with the issue:  ");
		System.out.println("           " + jmeterFileLine);	
		e.printStackTrace();
		throw new RuntimeException("  " + e.getClass());
	}
	

	/**
	 * Calculate the datapoint value, using the multiplier passed in the jmeter datatype ("dt") field.
	 * 
	 *  eg dt=DATAPOINT_1000  - the value loaded to the database will become the passed numeric value / 1000 
	 *  DATAPOINT, DATAPOINT_1 will use the value as passed.   
	 */
	private BigDecimal validateAndDetermineMetricValue(BigDecimal txnResultMsBigD, String sampleLineDataType){
		
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
	    throw new RuntimeException("ERROR : unexpected datatype present :  " +  sampleLineDataType    ) ;
	}

}