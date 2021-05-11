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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.mark59.metrics.data.beans.Transaction;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class TransactionExtractor implements ResultSetExtractor<Transaction> {

	 public Transaction extractData(ResultSet resultSet) throws SQLException,  DataAccessException {
	  
	  Transaction transaction = new Transaction();
	  
	  transaction.setApplication(resultSet.getString(1));
	  transaction.setRunTime(resultSet.getString(2));
	  transaction.setTxnId(resultSet.getString(3));
	  transaction.setTxnType(resultSet.getString(4));
	  transaction.setTxnMinimum(resultSet.getBigDecimal(5));
	  transaction.setTxnAverage(resultSet.getBigDecimal(6));
	  transaction.setTxnMaximum(resultSet.getBigDecimal(7));
	  transaction.setTxnStdDeviation(resultSet.getBigDecimal(8));
	  transaction.setTxn90th(resultSet.getBigDecimal(9));
	  transaction.setTxn95th(resultSet.getBigDecimal(10));
	  transaction.setTxn99th(resultSet.getBigDecimal(11));
	  transaction.setTxnPass(resultSet.getLong(12));
	  transaction.setTxnFail(resultSet.getLong(13));
	  transaction.setTxnStop(resultSet.getLong(14));
	  transaction.setTxnFirst(resultSet.getBigDecimal(15));
	  transaction.setTxnLast(resultSet.getBigDecimal(16));
	  transaction.setTxnSum(resultSet.getBigDecimal(17));
	  
	  return transaction;
	 }
}
