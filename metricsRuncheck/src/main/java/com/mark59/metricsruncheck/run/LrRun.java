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

import java.util.List;

import org.springframework.context.ApplicationContext;

import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metricsruncheck.accessdb.LrRunAccessDatabase;
	
/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class LrRun extends PerformanceTest  {
	
	public LrRun(ApplicationContext context, String application, String inputAccessDbFileNmae, String runReference, String excludestart, String captureperiod,
			String keeprawresults, String timeZone) {
		super(context, application, runReference);
		
		LrRunAccessDatabase lrRundb = new LrRunAccessDatabase(inputAccessDbFileNmae);
		System.out.println("Processing Loadrunner access DB file " + inputAccessDbFileNmae);
		
		testTransactionsDAO.deleteAllForRun(run.getApplication(), AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED);
		
		DateRangeBean dateRangeBean = lrRundb.getRunDateRangeUsingLoadrunnerAccessDB(timeZone);
		lrRundb.loadTestTransactionForTransactionsOnlyFromLoadrunnAccessDB(run.getApplication(), testTransactionsDAO, dateRangeBean.getRunStartTime());  

		run = new Run( calculateAndSetRunTimesUsingEpochStartAndEnd(run, dateRangeBean));
		runDAO.deleteRun(run.getApplication(), run.getRunTime());
		runDAO.insertRun(run);

		DateRangeBean filteredDateRangeBean = applyTimeRangeFiltersToTestTransactions(excludestart, captureperiod, dateRangeBean);
		if (filteredDateRangeBean.isFilterApplied()) {
			System.out.println("   Note that for Loadrunner results the 'transactions removed by filter' count applies to transactions only, "
					+ " - however time filter is also applied to system metrics (Monitor_meter and  DataPoint_meter mdb tables)");
		}
		
		transactionDAO.deleteAllForRun(run.getApplication(), run.getRunTime());				
		storeTransactionSummaries(run);

		// for Loadrunner, metric data is not placed in the testTransactions table,
		// it is summarized directly from the LR Access DB tables and inserted directly onto the transaction table 
		
		storeMetricTransactionSummariesFromLoadrunnerAccessDB(run, lrRundb, dateRangeBean, filteredDateRangeBean);
		
		endOfRunCleanupTestTransactions(keeprawresults);
	}


	private void storeMetricTransactionSummariesFromLoadrunnerAccessDB(Run run, LrRunAccessDatabase lrRundb, DateRangeBean dateRangeBean, DateRangeBean filteredDateRangeBean) {

		List<Transaction> eventTransactions = lrRundb.extractSystemMetricEventsFromMDB(run, eventMappingDAO, dateRangeBean, filteredDateRangeBean);      	
      	for (Transaction eventTransaction : eventTransactions ) {
      		try {
      			transactionDAO.insert(eventTransaction);
      		} catch ( org.springframework.dao.DuplicateKeyException e ) {
      			System.out.println("\n\nError :  Whoa!  This can happen if you try to match the same transaction name to two different LoadRunner events.\n"
      					+ "Review the Event Map Table (printed above) and your matching criteria to see if this is the issue.\n\n"
      					+ "The attempted transaction was : " + eventTransaction + "\n\n" );
      			throw new RuntimeException(e); 
      		}
		}
	}

}
