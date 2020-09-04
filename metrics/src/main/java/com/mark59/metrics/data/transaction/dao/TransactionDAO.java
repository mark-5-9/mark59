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

package com.mark59.metrics.data.transaction.dao;

import java.util.List;

import com.mark59.metrics.data.beans.Datapoint;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.Transaction;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface TransactionDAO 
{
	public void insert(Transaction transaction);

	public void deleteAllForRun(Run run);

	public void deleteAllForRun(String application, String runTime);
	
	public void deleteAllForApplication(String application);
	
	public String transactionIdsSQL(String application, String graph, String sqlSelectLike, String sqlSelectNotLike, boolean manuallySelectTxns , String chosenTxns, String chosenRuns, boolean useRawSQL, String rawTransactionIdsSQL );

	public List<Transaction> returnListOfSelectedTransactions  (String transactionIdsSQL, String nthRankedTxn);	
	
	public List<String>      returnListOfSelectedTransactionIds(String transactionIdsSQL, String nthRankedTxn);	
	
	public Transaction getTransaction(String application, String txnType, String runTime, String txnId);	

	public List<Datapoint> findDatapointsToGraph(String application, String graph, String chosenRuns, List<String> orderedTxnsToGraphIdList);
	
	public Object getTransactionValue(String application, String txnType, String runTime, String txnId, String transactionField);




	
}