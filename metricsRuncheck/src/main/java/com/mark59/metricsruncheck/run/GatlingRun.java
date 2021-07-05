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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.TestTransaction;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
	
/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class GatlingRun extends PerformanceTest  {
	
	private static final String GATLING_FORMAT_UNKNOWN ="UNKNOWN";
	private static final String GATLING_VER_lATEST_FORMAT ="3.4-3.6";         
	private static final String GATLING_VER_3_3_FORMAT ="3.3";
	
	private static final String RUN = "RUN";
	private static final String REQUEST = "REQUEST";
	private static final String KO = "KO";

	private int fieldPosTxnId;	
	private int fieldPosTimeStampStart;
	private int fieldPosTimeStampEnd;
	private int fieldPosSuccess;	
	private int fieldPosRequestErrorMsg;
	
	public GatlingRun(ApplicationContext context, String application, String inputdirectory, String runReference, String excludestart, String captureperiod, 
			String keeprawresults, String ignoredErrors, String simulationLog, String simlogCustom) {
		
		super(context,application, runReference);
		testTransactionsDAO.deleteAllForRun(run.getApplication(), AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);
		
		loadTestTransactionDataFromGatlingSimulationLog(run.getApplication(), inputdirectory, ignoredErrors, simulationLog, simlogCustom);
		
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


	private void loadTestTransactionDataFromGatlingSimulationLog(String application, String inputdirectory, String ignoredErrors, String simulationLog, String simlogCustom) {
		int sampleCount = 0;
		
		try {
			File simulationLogFile = new File(inputdirectory + "/" + simulationLog);
			sampleCount = loadTestTransactionDataFromGatlingSimulationLogFile(simulationLogFile, application, ignoredErrors, simlogCustom);
		} catch (IOException e) {
			System.out.println( "Error : problem with processing Gatling simulation log file  " + inputdirectory + "/" + simulationLog );
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new RuntimeException(e.getMessage());
		}
	    System.out.println("____________________________________" );
	    System.out.println(sampleCount + " Total samples written" );
	    System.out.println(" " );	    
	}

	
	/**
	 * A validly name named Gatling simulation log file is expected to be passed, now need determine its version and extract results 
	 */
	private int loadTestTransactionDataFromGatlingSimulationLogFile(File simulationLogFile, String application, String ignoredErrors, String simlogCustom) throws IOException {

		List<TestTransaction> testTransactionList = new ArrayList<TestTransaction>();
		long startLoadms = System.currentTimeMillis(); 
		System.out.println("\n\nProcessing Gatling Simulation Log File " + simulationLogFile.getName() + " at " + new Date(startLoadms));					
		int lineCount = 0; 
		int samplesCreated=0;
		
		CSVParser csvParser = new CSVParserBuilder().withIgnoreLeadingWhiteSpace(true).withIgnoreQuotations(true).withSeparator('\t').build();
		CSVReader csvReader = new CSVReaderBuilder(new FileReader(simulationLogFile)).withCSVParser(csvParser).build();
		
		String[] csvDataLineFields = csvReadNextLine(csvReader, simulationLogFile);
		
		if (csvDataLineFields == null) {
			System.out.println("   Warning : " + simulationLogFile.getName() + " is empty!" );
			return 0;
		} 
		
		String gatlingFormat = GATLING_FORMAT_UNKNOWN;
		
		if (StringUtils.isNotBlank(simlogCustom)) {
			System.out.println("\n  A custom 'REQUEST' field layout has been requested for this Gatling file load !" );
			System.out.println("\n  The 'RUN' (verion info) line will be bypassed, and the first REQUEST start time in the simulation log will be used as the test start time.\n" );				
		
		} else {
			
			boolean stillLookingForRUN = true; 
			while ( csvDataLineFields != null && stillLookingForRUN ){
				if (RUN.equals(csvDataLineFields[0].trim())){
					String gatlingVersion = csvDataLineFields[5].trim();
					System.out.println("Gatling version: " + gatlingVersion);
					if (StringUtils.isBlank(gatlingVersion)) {
						System.out.println("\n  Info :  The version of Gatling being used could not be determined ! ");
						System.out.println("\n  Proceeding on assuption the format is compatable with Gatling version " + GATLING_VER_lATEST_FORMAT);
						System.out.println("\n  If the field positions for 'REQUEST' are incompatable the 'simlogcustoM' (m) parameter may be of assistance.\n" );
						gatlingFormat = GATLING_VER_lATEST_FORMAT;	
					} else if ( gatlingVersion.startsWith("3.3")) {
						gatlingFormat = GATLING_VER_3_3_FORMAT;	
					} else if ( gatlingVersion.startsWith("3.4") || gatlingVersion.startsWith("3.5")  || gatlingVersion.startsWith("3.6") ){
						gatlingFormat = GATLING_VER_lATEST_FORMAT;					
					} else {
						System.out.println("\n  Info :  The version of Gatling being used (" + gatlingFormat + ") has not been catered for ! ");
						System.out.println("\n  Proceeding on assuption the format is compatable with Gatling version " + GATLING_VER_lATEST_FORMAT);						
						System.out.println("\n  If the field positions for 'REQUEST' are incompatable the 'simlogcustoM' (m) parameter may be of assistance.\n" );
						gatlingFormat = GATLING_VER_lATEST_FORMAT;	
					}
					stillLookingForRUN = false;
				}
				lineCount++;
				csvDataLineFields = csvReadNextLine(csvReader, simulationLogFile);
			}
		}
		
		if (GATLING_VER_lATEST_FORMAT.equals(gatlingFormat)){
			fieldPosTxnId = 2;	
			fieldPosTimeStampStart = 3;
			fieldPosTimeStampEnd = 4;
			fieldPosSuccess = 5;	
			fieldPosRequestErrorMsg = 6;
		} else if (GATLING_VER_3_3_FORMAT.equals(gatlingFormat)){
			fieldPosTxnId = 3;	
			fieldPosTimeStampStart = 4;
			fieldPosTimeStampEnd = 5;
			fieldPosSuccess = 6;	
			fieldPosRequestErrorMsg = 7;
		} else if (StringUtils.isNotBlank(simlogCustom)) {
			List<String> mPos = Mark59Utils.commaDelimStringToStringList(simlogCustom);
			fieldPosTxnId            = Integer.parseInt(mPos.get(0));	
			fieldPosTimeStampStart   = Integer.parseInt(mPos.get(1));	
			fieldPosTimeStampEnd     = Integer.parseInt(mPos.get(2));	
			fieldPosSuccess          = Integer.parseInt(mPos.get(3));		
			fieldPosRequestErrorMsg  = Integer.parseInt(mPos.get(4));				
		} else {
			throw new RuntimeException("Logic Error finding Gatling format " + gatlingFormat);
		}
		
		List<String> ignoredErrorsList = Mark59Utils.pipeDelimStringToStringList(ignoredErrors);
		
		while ( csvDataLineFields != null ) {
		
			if (REQUEST.equals(csvDataLineFields[0].trim())) {
    			addSampleToTestTransactionList(testTransactionList, csvDataLineFields, application, ignoredErrorsList);
				samplesCreated++;
			}

			lineCountProgressDisplay(lineCount);
			lineCount++;
			csvDataLineFields = csvReadNextLine(csvReader, simulationLogFile);
			
			if ( (samplesCreated % 100 ) == 0 ){
				testTransactionsDAO.insertMultiple(testTransactionList);
				testTransactionList.clear();
			}
			
		} // end for loop
		
		testTransactionsDAO.insertMultiple(testTransactionList);
		testTransactionList.clear();	    
		
		long endLoadms = System.currentTimeMillis(); 	    
		System.out.println("\n   " + simulationLogFile.getName() + "  file uploaded at " +  new Date(endLoadms) + " :" );
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

	
	private void lineCountProgressDisplay(int lineCount) {
		if ( (lineCount % 1000 )   == 0 ){	System.out.print("^");};
		if ( (lineCount % 100000 ) == 0 ){	System.out.println();};
	}
	
	
	private void addSampleToTestTransactionList(List<TestTransaction> testTransactionList, String[] csvDataLineFields, String application, List<String> ignoredErrorsList){
		TestTransaction testTransaction = extractTransactionFromGatlingLine(csvDataLineFields, ignoredErrorsList);
		testTransaction.setApplication(application);
		testTransaction.setRunTime(AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);
		testTransactionList.add(testTransaction);		
	}

	
	private TestTransaction extractTransactionFromGatlingLine(String[] csvDataLineFields, List<String> ignoredErrorsList) {

		TestTransaction testTransaction = new TestTransaction();
		testTransaction.setTxnId(csvDataLineFields[fieldPosTxnId]);
		
		// not sure if there is much point doing this transform for Gatling as most tests just capture http response times and are always TRANSACTIONS, but just for completeness.. 
		testTransaction.setTxnType(eventMappingTxnTypeTransform(testTransaction.getTxnId(), AppConstantsMetrics.GATLING, Mark59Constants.DatabaseTxnTypes.TRANSACTION.name()));

		BigDecimal elapsedTime = new BigDecimal(Long.parseLong(csvDataLineFields[fieldPosTimeStampEnd]) - Long.parseLong(csvDataLineFields[fieldPosTimeStampStart])); 	
		testTransaction.setTxnResult(elapsedTime.divide(AppConstantsMetrics.THOUSAND, 3, RoundingMode.HALF_UP));
	
		testTransaction.setTxnPassed("Y");
		if (KO.equalsIgnoreCase(csvDataLineFields[fieldPosSuccess]) && !errorToBeIgnored(csvDataLineFields[fieldPosRequestErrorMsg], ignoredErrorsList)){
			testTransaction.setTxnPassed("N");
		}
		testTransaction.setTxnEpochTime(csvDataLineFields[fieldPosTimeStampStart]);
		return testTransaction;
	}


}