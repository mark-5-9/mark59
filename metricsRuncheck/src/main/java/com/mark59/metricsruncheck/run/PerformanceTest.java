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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAO;
import com.mark59.metrics.data.run.dao.RunDAO;
import com.mark59.metrics.data.testTransactions.dao.TestTransactionsDAO;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class PerformanceTest {
	
	protected RunDAO runDAO; 
	protected TransactionDAO transactionDAO; 	
	protected TestTransactionsDAO testTransactionsDAO; 	
	protected EventMappingDAO eventMappingDAO; 

	protected Run run = new Run();
	private List<Transaction> transactionSummariesThisRun;


	public PerformanceTest(ApplicationContext context, String application, String runReferenceArg) {

		runDAO = (RunDAO)context.getBean("runDAO");
		transactionDAO = (TransactionDAO)context.getBean("transactionDAO");
		testTransactionsDAO = (TestTransactionsDAO)context.getBean("testTransactionsDAO");		
		eventMappingDAO = (EventMappingDAO)context.getBean("eventMappingDAO");	
		
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
			
			Date runEndDate = new Date(Long.valueOf(runEndTime));
			long durationMs  = runEndDate.getTime() - runStartDate.getTime();
			Long durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
			run.setDuration(durationInMinutes.toString());
			
			//period is set really just to keep everything to the same format as a default Loadrunner report does
			
			DateFormat formatterSecPrecision = new SimpleDateFormat("yyyyMMddHHmmss");
			
			String period = formatterSecPrecision.format(runStartDate).substring(0,8) + "_"
				 		  + formatterSecPrecision.format(runStartDate).substring(8,14) + " - "
				 		  + formatterSecPrecision.format(runEndDate).substring(0,8) + "_"
						  + formatterSecPrecision.format(runEndDate).substring(8,14);	
			run.setPeriod(period);
			
			System.out.println("\nRun start time set as " + run.getRunTime() + "  [ " + runStartDate 
				+ ", Timestamp " + runStartTime + " ], with a duration of " + run.getDuration() + " mins,  period of " + run.getPeriod());
			System.out.println("Epoch Range (msec)  " + dateRangeBean.prettyPrint() + "\n"  );		
			
			// generate the run reference from the run start time, if a reference argument was not passed
			
			if (run.getRunReference().startsWith(AppConstantsMetrics.NO_ARGUMENT_PASSED)) {
				run.setRunReference(formatterSecPrecision.format(runStartDate).substring(0,8) + "_" + formatterSecPrecision.format(runStartDate).substring(8,14));
				System.out.println("Run reference has been set as  " + run.getRunReference());	
			}
		}
		return run;
	};	
	

	protected List<Transaction> storeTransactionSummaries(Run run) {
		transactionSummariesThisRun = testTransactionsDAO.extractTransactionResponsesSummary(run.getApplication(), Mark59Constants.DatabaseTxnTypes.TRANSACTION.name() );  
      	for (Transaction transaction : transactionSummariesThisRun) {  // insert a row for each transaction captured 
      		transaction.setRunTime(run.getRunTime());
      		transactionDAO.insert(transaction);
      	}		
		return transactionSummariesThisRun;
	}
	
	
	protected DateRangeBean applyTimingRangeFilters(String excludestart, String captureperiod, DateRangeBean dateRangeBean) {
		
		DateRangeBean filteredDateRangeBean = new DateRangeBean(dateRangeBean.getRunStartTime(), dateRangeBean.getRunEndTime(), false );
			
		Long excludestartMsecs = 0L;
		if (StringUtils.isNumeric(excludestart)) {  
			excludestartMsecs = TimeUnit.MINUTES.toMillis(Long.valueOf(excludestart)); 
		}

		if ( excludestartMsecs != 0  || !captureperiod.equalsIgnoreCase(AppConstantsMetrics.ALL) ){
			System.out.println();
			System.out.println( " Transaction results will be filtered by time for this run"  );
			System.out.print( " - only transactions " + excludestart + " mins from the start of the test ");
			
			Long filterEpochTimeFromMsecs = Long.valueOf(dateRangeBean.getRunStartTime()) + excludestartMsecs; 
			Long filterEpochTimeToMsecs   = Long.valueOf(dateRangeBean.getRunEndTime()); 
			
			if (StringUtils.isNumeric(captureperiod)){
				filterEpochTimeToMsecs = filterEpochTimeFromMsecs + TimeUnit.MINUTES.toMillis(Long.valueOf(captureperiod));
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
	

	public Run getRunSummary() {
		return run;
	}
	
	public List<Transaction> getTransactionSummariesThisRun() {
		return transactionSummariesThisRun;
	}
	
}
