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

package com.mark59.metrics.data.testTransactions.dao;

import java.util.List;

import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.EventMapping;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.TestTransaction;
import com.mark59.metrics.data.beans.Transaction;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface TestTransactionsDAO 
{
	
	public void insert(TestTransaction testTransaction);

	public void insertMultiple(List<TestTransaction> testTransactionList);

	public void deleteAllForRun(String application, String runTime);
	
	public void deleteAllForApplication(String application);	
	
	public void updateRunTime(String application, String originalRunTime, String newRunTime);

	public List<TestTransaction> getUniqueListOfSystemMetricNamesByType(String application);	
	
//	public List<String> getTransactionIds(Run run, String txnType);

	public Long getEarliestTimestamp(String applicationn);	
	
	public Long getLatestTimestamp(String applicationn);
	
	public List<Transaction> extractTransactionResponsesSummary(String application, String txnType_dataOrTxnSample);

	public Transaction extractEventSummaryStats(String application, String txnType, String dataSampleLablel, EventMapping EventMapping);

	public int filterByTime(Run run, DateRangeBean filteredDateRangeBean);


	
}