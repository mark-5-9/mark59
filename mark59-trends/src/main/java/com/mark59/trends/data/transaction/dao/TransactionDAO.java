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

package com.mark59.trends.data.transaction.dao;

import java.util.List;

import com.mark59.trends.data.beans.Datapoint;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.beans.Transaction;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface TransactionDAO 
{
	void insert(Transaction transaction);

	void deleteAllForRun(Run run);

	void deleteAllForRun(String application, String runTime);

	void deleteAllForApplication(String application);

	List<Transaction> getUniqueListOfTransactionsByType(String application);

	long countRunsWithCdpTransactions(String application);

	String transactionIdsSQL(String application, String graph, String showCdpOption, String sqlSelectLike,
                             String sqlSelectNotLike, boolean manuallySelectTxns, String chosenTxns, String chosenRuns,
                             boolean useRawSQL, String rawTransactionIdsSQL);

	List<Transaction> returnListOfTransactionsToGraph(String application, String graph, String showCdpOption, String sqlSelectLike,
                                                      String sqlSelectNotLike, boolean manuallySelectTxns, String chosenTxns, String chosenRuns,
                                                      boolean useRawSQL, String transactionIdsSQL, String nthRankedTxn);

	Transaction getTransaction(String application, String txnType, String runTime, String txnId,
                               String isCdpTxn);

	List<Datapoint> findDatapointsToGraph(String application, String graph, String chosenRuns,
                                          List<String> listOfStdTransactionNamesToGraph, List<String> listOfCdpTransactionNamesToGraph);

	Object getTransactionValue(String application, String txnType, String isCdpTxn, String runTime, String txnId,
                               String transactionField);

	long countRunsContainsBothTxnIds(String application, String txnType, String fromTxnId, String toTxnId,
                                     String fromIsCdpTxn, String toIsCdpTxn);

	void renameTransactions(String application, String txnType, String fromTxnId, String toTxnId,
                            String fromIsCdpTxn, String toIsCdpTxn);

	
}