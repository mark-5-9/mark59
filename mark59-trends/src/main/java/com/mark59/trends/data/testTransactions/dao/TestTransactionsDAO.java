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

package com.mark59.trends.data.testTransactions.dao;

import java.util.List;

import com.mark59.trends.data.beans.DateRangeBean;
import com.mark59.trends.data.beans.EventMapping;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.beans.TestTransaction;
import com.mark59.trends.data.beans.Transaction;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface TestTransactionsDAO 
{
	
//	public void insert(TestTransaction testTransaction);

	void insertMultiple(List<TestTransaction> testTransactionList);

	void deleteAllForRun(String application, String runTime);
	
	void deleteAllForApplication(String application);
	
	void updateRunTime(String application, String originalRunTime, String newRunTime);

	List<TestTransaction> getUniqueListOfSystemMetricTxnIdsByType(String application);

	Long getEarliestTimestamp(String applicationn);
	
	Long getLatestTimestamp(String applicationn);
	
	List<Transaction> extractTransactionResponsesSummary(String application, String txnType);

	Transaction extractEventSummaryStats(String application, String metricTxnType, String txnId, EventMapping eventMapping);

	int filterByTime(Run run, DateRangeBean filteredDateRangeBean);


	
}