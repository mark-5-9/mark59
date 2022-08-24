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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.trends.application.AppConstantsMetrics;
import com.mark59.trends.data.beans.DateRangeBean;
import com.mark59.trends.data.beans.EventMapping;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.beans.Sla;
import com.mark59.trends.data.beans.TestTransaction;
import com.mark59.trends.data.beans.Transaction;
import com.mark59.trends.data.eventMapping.dao.EventMappingDAO;
import com.mark59.trends.data.run.dao.RunDAO;
import com.mark59.trends.data.sla.dao.SlaDAO;
import com.mark59.trends.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.trends.data.transaction.dao.TransactionDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class PerformanceTest {
	
	protected RunDAO runDAO; 
	protected TransactionDAO transactionDAO; 	
	protected TestTransactionsDAO testTransactionsDAO; 	
	protected EventMappingDAO eventMappingDAO; 
	protected SlaDAO slaAO; 

	protected Run run = new Run();
	
	private List<Transaction> transactionSummariesThisRun;
	private final List<Transaction> metricTransactionSummariesThisRun = new ArrayList<>();  // currently only used for testing

	private final Map<String,String> optimizedTxnTypeLookup = new HashMap<>();
	private final Map<String,EventMapping> txnIdToEventMappingLookup = new HashMap<>();

	
	public PerformanceTest(ApplicationContext context, String application, String runReferenceArg) {

		runDAO = (RunDAO)context.getBean("runDAO");
		transactionDAO = (TransactionDAO)context.getBean("transactionDAO");
		testTransactionsDAO = (TestTransactionsDAO)context.getBean("testTransactionsDAO");		
		eventMappingDAO = (EventMappingDAO)context.getBean("eventMappingDAO");	
		slaAO = (SlaDAO)context.getBean("slaDAO");	

		run.setApplication(application);
		run.setRunTime(AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED );
		run.setIsRunIgnored("N");
		run.setBaselineRun("N");
		run.setRunReference(runReferenceArg);
		run.setComment("");
	}


	protected Run calculateAndSetRunTimesUsingEpochStartAndEnd(Run run, DateRangeBean dateRangeBean) {
		
		Long runStartTime = dateRangeBean.getRunStartTime();
		Long runEndTime   = dateRangeBean.getRunEndTime();
		
		if (runStartTime != null){ //just is case of empty run
		
			Date runStartDate = new Date(runStartTime);
			DateFormat formatterMinutePrecision = new SimpleDateFormat("yyyyMMddHHmm");
			run.setRunTime(formatterMinutePrecision.format(runStartDate));
			
			Date runEndDate = new Date(runEndTime);
			long durationMs  = runEndDate.getTime() - runStartDate.getTime();
			long durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
			run.setDuration(Long.toString(durationInMinutes));
			
			// period (local time from/to format) is set really just to keep everything to the same format as a default Loadrunner report does
			
			DateFormat formatterSecPrecision = new SimpleDateFormat("yyyyMMddHHmmss");
			
			String period = formatterSecPrecision.format(runStartDate).substring(0,8) + "_"
				 		  + formatterSecPrecision.format(runStartDate).substring(8,14) + " - "
				 		  + formatterSecPrecision.format(runEndDate).substring(0,8) + "_"
						  + formatterSecPrecision.format(runEndDate).substring(8,14); 
			
			System.out.println("\nRun start time set as " + run.getRunTime() + "  [ " + runStartDate 
				+ ", Timestamp " + runStartTime + " ] with a duration of " + run.getDuration() + " minutes.");
			System.out.println("Run period of " + period );			
			System.out.println("Epoch Range (msec)  " + dateRangeBean.prettyPrint() + "\n"  );		
			
			run.setPeriod(period + "<br>" + dateRangeBean.prettyPrint());			
			
			// generate the run reference from the run start time, if a reference argument was not passed
			
			if (run.getRunReference().startsWith(AppConstantsMetrics.NO_ARGUMENT_PASSED)) {
				run.setRunReference(formatterSecPrecision.format(runStartDate).substring(0,8) + "_" + formatterSecPrecision.format(runStartDate).substring(8,14));
				System.out.println("Run reference has been set as  " + run.getRunReference());	
			}
		}
		return run;
	}


	protected List<Transaction> storeTransactionSummaries(Run run) {
		transactionSummariesThisRun = testTransactionsDAO.extractTransactionResponsesSummary(run.getApplication(), Mark59Constants.DatabaseTxnTypes.TRANSACTION.name() );  
      	for (Transaction transaction : transactionSummariesThisRun) {  // insert a row for each transaction captured 
      		transaction.setRunTime(run.getRunTime());
      		
      		Sla sla = slaAO.getSla(run.getApplication(), transaction.getTxnId(), transaction.getIsCdpTxn());
      		if (sla != null && sla.getTxnDelay() != null ){
      			transaction.setTxnDelay(sla.getTxnDelay());
      		}
      		transactionDAO.insert(transaction);
      	}		
		return transactionSummariesThisRun;
	}
	
	
	protected List<Transaction> storeMetricTransactionSummaries(Run run) {
		// Creates a list of the names of metric transactions for the run, with their types (bit of an abuse of the 'TestTransaction' bean)  
		List<TestTransaction> metricTypeAndTxnIds = testTransactionsDAO.getUniqueListOfSystemMetricTxnIdsByType(run.getApplication()); 
		
		for (TestTransaction metricTypeAndTxnId : metricTypeAndTxnIds) {

			EventMapping eventMapping = txnIdToEventMappingLookup.get(metricTypeAndTxnId.getTxnId());
			if (eventMapping == null) {
				throw new RuntimeException("ERROR : No event mapping found for " + metricTypeAndTxnId.getTxnId());
			}
			Transaction eventTransaction = testTransactionsDAO.extractEventSummaryStats(run.getApplication(), metricTypeAndTxnId.getTxnType(), metricTypeAndTxnId.getTxnId(), eventMapping);
			eventTransaction.setRunTime(run.getRunTime());
			transactionDAO.insert(eventTransaction);
			metricTransactionSummariesThisRun.add(eventTransaction);
		} 
		return metricTransactionSummariesThisRun; 
	}
	
	
	protected DateRangeBean applyTimeRangeFiltersToTestTransactions(String excludestart, String captureperiod, DateRangeBean dateRangeBean) {
		
		DateRangeBean filteredDateRangeBean = new DateRangeBean(dateRangeBean.getRunStartTime(), dateRangeBean.getRunEndTime(), false );
			
		Long excludestartMsecs = 0L;
		if (StringUtils.isNumeric(excludestart)) {  
			excludestartMsecs = TimeUnit.MINUTES.toMillis(Long.parseLong(excludestart));
		}

		if ( excludestartMsecs != 0  || !captureperiod.equalsIgnoreCase(AppConstantsMetrics.ALL) ){
			System.out.println();
			System.out.println( " Transaction results will be filtered by time for this run"  );
			System.out.print( " - only transactions " + excludestart + " mins from the start of the test ");
			
			long filterEpochTimeFromMsecs = dateRangeBean.getRunStartTime() + excludestartMsecs;
			Long filterEpochTimeToMsecs   = dateRangeBean.getRunEndTime();
			
			if (StringUtils.isNumeric(captureperiod)){
				filterEpochTimeToMsecs = filterEpochTimeFromMsecs + TimeUnit.MINUTES.toMillis(Long.parseLong(captureperiod));
				System.out.print( ", for the following " + captureperiod	+ " mins "); 
			} 
			
			System.out.println("will be inculded in the captured results");
			System.out.println();		
			
			filteredDateRangeBean.setRunStartTime(filterEpochTimeFromMsecs );
			filteredDateRangeBean.setRunEndTime(filterEpochTimeToMsecs);
			filteredDateRangeBean.setFilterApplied(true);

			int rowsAffected = testTransactionsDAO.filterByTime(run, filteredDateRangeBean);

			System.out.println("   " + rowsAffected + " transactions removed by filter. " + filteredDateRangeBean.prettyPrint());

			run.setPeriod(run.getPeriod() + " filter x:c ["  + excludestart + ":" + captureperiod + "]" );
			runDAO.updateRun(run);
		}
		return filteredDateRangeBean;
	}
	
	
	public boolean errorToBeIgnored(String errorMsg, List<String> ignoredErrorsList) {
		if (StringUtils.isBlank(errorMsg))
			return false;
		
		boolean isErrorToBeIgnored = false;
		for (String ignoredError : ignoredErrorsList) {
			if (errorMsg.startsWith(ignoredError)) {
				isErrorToBeIgnored = true;
				break;
			}
		}
		return isErrorToBeIgnored;
	}
	

	/**
	 * If an event mapping is found for the given transaction / tool / Database Data type (relating to a a sample line), 
	 * then the Database Data type for that mapping is returned (CDP flag is not taken into consideration)<br>
	 * If a event mapping for the sample line is not found, then it is taken to be a TRANSACTION<br>
	 * TODO: PERFMON - allow for transforms to a TRANSACTION in event mapping (catering for CDP would be needed)<br>
	 * 
	 * @param txnId txnId
	 * @param performanceTool performanceTool
	 * @param sampleLineRawDbDataType -will be a string value of enum Mark59Constants.DatabaseDatatypes (DATAPOINT, CPU_UTIL, MEMORY, TRANSACTION)
	 * @return txnType -  will be a string value of enum Mark59Constants.DatabaseDatatypes (DATAPOINT, CPU_UTIL, MEMORY, TRANSACTION)
	 */
	protected String eventMappingTxnTypeTransform(String txnId, String performanceTool, String sampleLineRawDbDataType) {
		
		String eventMappingTxnType;
		String metricSource = performanceTool + "_" + sampleLineRawDbDataType;   // (eg 'Jmeter_CPU_UTIL',  'Jmeter_TRANSACTION' ..)
		
		String txnId_MetricSource_Key = txnId + "-" + metricSource; 
			
		if (optimizedTxnTypeLookup.get(txnId_MetricSource_Key) != null ){
			
			// As we could be processing large files, a Map of type by transaction ids (labels) is held for ids that have already had a lookup on the eventMapping table.  
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
	 * Once all transaction and metrics data has been stored for the run, work out the start and end 
	 * time for the run.  Start/end times are taken lowest and highest transaction epoch time for the
	 * application run. 
	 *  
	 * The times are actually an approximation, as any time difference between the timestamp and the time
	 * to take the sample is not considered, nor is any running time before/after the first/last sample.
	 * 
	 * NOTE: When this method is called currently assumed the run being processed will have a  
	 * run-time of AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED (zeros) on TESTTRANSACTIONS 	  
	 * 
	 * @param application application
	 * @return DateRangeBean DateRangeBean
	 */
	protected DateRangeBean getRunDateRangeUsingTestTransactionalData(String application){
		Long runStartTime = testTransactionsDAO.getEarliestTimestamp(application);
		Long runEndTime   = testTransactionsDAO.getLatestTimestamp(application);
		return new DateRangeBean(runStartTime, runEndTime);
	}

	
	/**
	 * Save off testTransaction data if request, otherwise clean it up at the end of the run.
	 * @param keeprawresults keeprawresults
	 */
	protected void endOfRunCleanupTestTransactions(String keeprawresults) {
		if (String.valueOf(true).equalsIgnoreCase(keeprawresults)) {
			testTransactionsDAO.deleteAllForRun(run.getApplication(), run.getRunTime()); // clean up in case of re-run (the data already exists)
			testTransactionsDAO.updateRunTime(run.getApplication(), AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED, run.getRunTime());
		} else {
			testTransactionsDAO.deleteAllForRun(run.getApplication(), AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);
		}
	}
		
	
	public Run getRunSummary() {
		return run;
	}
	public List<Transaction> getTransactionSummariesThisRun() {
		return transactionSummariesThisRun;
	}
	public List<Transaction> getMetricTransactionSummariesThisRun() {
		return metricTransactionSummariesThisRun;
	}
}
