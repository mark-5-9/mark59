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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.Datapoint;
import com.mark59.metrics.data.beans.GraphMapping;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.data.graphMapping.dao.GraphMappingDAO;
import com.mark59.metrics.sla.SlaUtilities;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class TransactionDAOjdbcTemplateImpl implements TransactionDAO 
{
	
	@Autowired  
	private DataSource dataSource;

	@Autowired
	GraphMappingDAO graphMappingDAO; 	
	
	
	@Override
	public void insert(Transaction transaction) {
		String sql = "INSERT INTO TRANSACTION "
				+ "(APPLICATION, RUN_TIME, TXN_ID, TXN_TYPE, "
				+ "TXN_MINIMUM, TXN_AVERAGE, TXN_MEDIAN, TXN_MAXIMUM, TXN_STD_DEVIATION, TXN_90TH, TXN_95TH, TXN_99TH, "
				+ "TXN_PASS, TXN_FAIL, TXN_STOP, TXN_FIRST, TXN_LAST, TXN_SUM, TXN_DELAY)"
				+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
//		System.out.println("TransactionDAOjdbcTemplateImpl insert [" + transaction.toString() + "]"   );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] { transaction.getApplication(), transaction.getRunTime(), 
				transaction.getTxnId(), transaction.getTxnType(),
				transaction.getTxnMinimum(), transaction.getTxnAverage(), transaction.getTxnMedian(), transaction.getTxnMaximum(),
				transaction.getTxnStdDeviation(), transaction.getTxn90th(), transaction.getTxn95th(), transaction.getTxn99th(),
				transaction.getTxnPass(), transaction.getTxnFail(), transaction.getTxnStop(), 
				transaction.getTxnFirst(), transaction.getTxnLast(), transaction.getTxnSum(), transaction.getTxnDelay() });
	}
	
	
	public Transaction getTransaction(String application, String txnType, String runTime, String txnId) {

		List<Transaction> transactionList = new ArrayList<Transaction>();

		String sql = "SELECT * FROM TRANSACTION WHERE APPLICATION = '" + application + "' AND " +
				                                        "RUN_TIME = '" + runTime + "' AND " +
				                                        "TXN_TYPE = '" + txnType + "' AND " +
				                                          "TXN_ID = '" + txnId + "' ";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		transactionList = jdbcTemplate.query(sql, new TransactionRowMapper());
		
		if (transactionList.isEmpty() )
			return null;
		else
			return transactionList.get(0);		
	}		
	
	
	@Override
	public List<Transaction> getUniqueListOfTransactionsByType(String application) {
		// bit of a hack using the 'transaction' bean (should be a new form bean really...)
		List<Transaction> transactionKeyList = new ArrayList<Transaction>();
		String sql = "SELECT DISTINCT TXN_ID, TXN_TYPE, MAX(RUN_TIME) AS MAX_RUN_TIME, COUNT(*) AS TXN_COUNT "
					+ "FROM TRANSACTION "
					+ "WHERE APPLICATION = '" + application + "' "
					+ "GROUP BY TXN_ID, TXN_TYPE "
					+ "ORDER BY 2 DESC, 1 ASC "; 
				
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> row : rows) {
			Transaction transactionKey = new Transaction();
			transactionKey.setApplication(application); 
			transactionKey.setTxnId((String)row.get("TXN_ID")); 
			transactionKey.setTxnType((String)row.get("TXN_TYPE")); 
			transactionKey.setRunTime((String)row.get("MAX_RUN_TIME")); 
			transactionKey.setTxnPass((Long)row.get("TXN_COUNT"));   // big hack 
			try {
				transactionKey.setTxnIdURLencoded(URLEncoder.encode(transactionKey.getTxnId(), "UTF-8")) ;
			} catch (UnsupportedEncodingException e) {	e.printStackTrace();	}	  
			transactionKeyList.add(transactionKey);
		}	
		return transactionKeyList;		
	}

	
	/**
	 *  Used to check to see if any runs contain BOTH transactions 
	 */
	@Override	
	public long countRunsContainsBothTxnIds(String application, String txnType, String txnId1, String txnId2 ){
		Long rowCount;
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String sql =  "SELECT COUNT(DISTINCT R.RUN_TIME) FROM RUNS R, TRANSACTION T "    
				   + " WHERE R.APPLICATION = '" + application + "' " 
				   + " AND T.TXN_TYPE = '" + txnType + "'" 
				   + " AND R.APPLICATION = T.APPLICATION AND R.RUN_TIME = T.RUN_TIME "   
				   + " AND R.RUN_TIME IN ( SELECT RUN_TIME FROM TRANSACTION  WHERE APPLICATION = '" + application + "' " 
				   														+ " AND TXN_TYPE = '" + txnType + "'" 
				   														+ " AND TXN_ID = '" + txnId1 + "') " 
				   + " AND R.RUN_TIME IN ( SELECT RUN_TIME FROM TRANSACTION  WHERE APPLICATION = '" + application + "' " 
				   														+ " AND TXN_TYPE = '" + txnType + "'" 
				   														+ " AND TXN_ID = '" + txnId2 + "') "; 
		rowCount = Long.valueOf(jdbcTemplate.queryForObject(sql, String.class));
//		System.out.println("countRunsContainsBothTxnIds sql = " + sql + "\n - rowCount = " + rowCount );
		return rowCount;
	}	
	
	
	/**
	 *  Validation need to be done before the rename (see {@link #countRunsContainsBothTxnIds(String, String, String, String)}. 
	 */
	@Override
	public void renameTransactions(String application, String txnType, String fromTxnId, String toTxnId) {
		String sql = "UPDATE TRANSACTION SET TXN_ID = '" + toTxnId + "'"  
					+ " WHERE APPLICATION='" + application + "'"  
					+ "   AND TXN_TYPE='" + txnType + "'"
					+ "   AND TXN_ID='" + fromTxnId + "'";
//		System.out.println("TransactionDAOjdbcTemplateImpl.renameTransactions : " + sql );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);	
	}
	

	@Override
	public Object getTransactionValue(String application, String txnType, String runTime, String txnId, String transactionField) {

		List<Object> transactionValues =  new ArrayList<Object>();  
		String sql = "SELECT " + transactionField + " FROM TRANSACTION " +
										  	   "WHERE APPLICATION = '" + application + "' AND " +
				                                        "RUN_TIME = '" + runTime + "' AND " +
				                                        "TXN_TYPE = '" + txnType + "' AND " +
				                                          "TXN_ID = '" + txnId + "' ";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		for (Map<String, Object> row : rows) {
			transactionValues.add( row.get(transactionField) );
//			System.out.println("populating " + transactionField + " for getTransactionValue " + transactionValues.get(  transactionValues.size() - 1 )   ) ;
		}	
		
		if (transactionValues.isEmpty() ){
//			System.out.println("TransactionDAOjdbcTemplateImpl : no txn row for [" + application + " : " + txnType + " : "   + runTime + " : " + txnId + " - "   + transactionField + "]" ) ;
			return null;
		} else {
//			System.out.println("TransactionDAOjdbcTemplateImpl : [" + application + " : " + txnType + " : " + runTime + " : " + txnId + " - " + transactionField + " returns " + transactionValues.get(0) + "]" ) ;			
			return transactionValues.get(0);
		}	
	}
	
	
	@Override
	public void deleteAllForRun(Run run) {
		deleteAllForRun(run.getApplication(), run.getRunTime());
	}
	
	@Override
	public void deleteAllForRun(String application,  String runTime) {
		String sql = "delete from TRANSACTION where APPLICATION='" + application + "' "
												+ "and RUN_TIME='" + runTime + "'";
//		System.out.println("performing : " + sql );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}
	
	@Override
	public void deleteAllForApplication(String application) {
		String sql = "delete from TRANSACTION where APPLICATION='" + application + "' ";
		System.out.println("delete all transactions for application : " + sql );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

	
	
	/**
	 *  The returned list of Transactions is ordered by the ranking of the target values
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public List<Transaction> returnListOfSelectedTransactions(String transactionIdsSQL, String nthRankedTxn){
		
		Map<Transaction, BigDecimal> transactionListOrderedByValue = new LinkedHashMap<Transaction, BigDecimal>();
		BigDecimal rankedValue = null;
				
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(transactionIdsSQL);
		
		for (Map row : rows) {
			Transaction transaction = getTransaction((String)row.get("APPLICATION"),
					                                 (String)row.get("TXN_TYPE"),
					                                 (String)row.get("RUN_TIME"),
					                                 (String)row.get("TXN_ID"));
		
			if ( row.get("rankedValue").getClass().toString().contains("BigDecimal")  ){
				rankedValue = ((BigDecimal)row.get("rankedValue"));
			} else { // assume 'long'
				rankedValue =  new BigDecimal((Long)row.get("rankedValue"));   
			}	
			transactionListOrderedByValue.put(transaction, rankedValue);
		}
		
		List<Transaction> selectedTransactions = selectTopNthRankedTransactionsByValue(transactionListOrderedByValue, nthRankedTxn);  
		return selectedTransactions;
	}
	 
	 
	/**
	 * Note: The supplied database installs (DDL) will put the lists is in UTF-8 order (h2 default, MySQL utf8mb4_0900_bin collation)
	 * This should be pretty close to the ordering used by Java, but to avoid any  potential for issues for odd non-standard chars or
	 * a different collation being chosen on a database, the returned list is re-ordered in the natural order of Java sorting.
	 * 
	 * This is Important as this list is supplied to the graph datapoints creation routine where a string compareTo is done.  The datapoints
	 * would be out of sequence and the graph could fail if the compareTo did not work properly because the list was out of natural Java order.  
	 */
	@Override
	public List<String> returnListOfSelectedTransactionIds(String transactionIdsSQL, String nthRankedTxn){
		List<Transaction> selectedTransactions = returnListOfSelectedTransactions(transactionIdsSQL, nthRankedTxn);
		List<String> orderedSelectedTransactionsIds = new ArrayList<String>();
		for (Transaction transaction : selectedTransactions) {
			orderedSelectedTransactionsIds.add(transaction.getTxnId());
		}
		Collections.sort(orderedSelectedTransactionsIds);
		return orderedSelectedTransactionsIds;
	}


	private List<Transaction> selectTopNthRankedTransactionsByValue(Map<Transaction, BigDecimal> transactionIdsOrderedByValue, String nthRankedTxn) {
		//System.out.println("** transactionIdsOrderedByValue map " +  Mark59Utils.prettyPrintMap(transactionIdsOrderedByValue));		
		List<Transaction> selectedTransactions = new ArrayList<Transaction>();
		Integer nthRankedTxnInt = convertRankingStrToInt(nthRankedTxn);
		
		boolean nthRankedTxnNotReached = true;
		BigDecimal nthRankedValue = null;  ;
		int listPosition = 0;
		Iterator<Entry<Transaction, BigDecimal>>  valueOrderedIterator = transactionIdsOrderedByValue.entrySet().iterator();
		
		while ( valueOrderedIterator.hasNext()  &&  nthRankedTxnNotReached  ) {
			Entry<Transaction, BigDecimal> orderedEntry = valueOrderedIterator.next();
			listPosition++;
			if (listPosition <= nthRankedTxnInt ) {
				selectedTransactions.add(orderedEntry.getKey());
			}
			if (listPosition == nthRankedTxnInt ) {
				nthRankedValue = orderedEntry.getValue();
			}
			if (listPosition > nthRankedTxnInt ) {
				if (orderedEntry.getValue().equals(nthRankedValue) ) {  // equal values at Nth included
					selectedTransactions.add(orderedEntry.getKey());
				} else {
					nthRankedTxnNotReached = false;
				}
			}				
		}
		return selectedTransactions;
	}
	

	private Integer convertRankingStrToInt(String nthRankedTxn) {
		if (nthRankedTxn == null) return Integer.MAX_VALUE;
		if (AppConstantsMetrics.ALL.equalsIgnoreCase(nthRankedTxn)) return Integer.MAX_VALUE;
		try {
			Integer nthRankedTxnInt = Integer.parseInt(nthRankedTxn);
			if (nthRankedTxnInt <= 0)  return Integer.MAX_VALUE; 
			return nthRankedTxnInt;
		} catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}

	
	@Override
	public String transactionIdsSQL(String application, String graph, String sqlSelectLike, String sqlSelectNotLike, boolean manuallySelectTxns , String chosenTxns, String chosenRuns, boolean useRawSQL, String rawTransactionIdsSQL) {
		
		String sql;
		
		if (useRawSQL){
			
			sql = rawTransactionIdsSQL; 
			
		} else {
			
			GraphMapping graphMapping = graphMappingDAO.findGraphMapping(graph);

			sql = "SELECT APPLICATION, RUN_TIME, TXN_ID, TXN_TYPE, ( " + transactionDBColNameOrDerivationForRequestedValues(graphMapping) + " ) as rankedValue "
				+ "FROM TRANSACTION WHERE APPLICATION = '" + application + "' "
				+ "  AND TXN_TYPE =  '" + graphMapping.getTxnType() + "' "
				+ "  AND RUN_TIME = " + " ( SELECT MAX(RUN_TIME)"
										+ " FROM TRANSACTION WHERE APPLICATION = '" + application + "' "
										+ "  AND TXN_TYPE =  '" + graphMapping.getTxnType() + "' "				
										+ "  AND RUN_TIME in ( '" + chosenRuns.replaceAll(",", "','") + "' ) "										
										
										+ ") "; 
			if (! manuallySelectTxns ){
				sql = sql
					+ constructSqlTxnIdsLikeNotLike(sqlSelectLike, sqlSelectNotLike )
					+ "  AND TXN_ID NOT IN ( " + SlaUtilities.listOfIgnoredTransactionsSQL(application) + " ) " ; 

			} else {
				sql = sql + constructSqlIn(chosenTxns);
			}
			sql =  sql + " ORDER BY 2 DESC";
		}
		// System.out.println("TransactionDAOjdbcTemplateImpl.transactionIdsSQL sql = " + sql );
		return sql;
	}

	

	@Override
	public List<Datapoint> findDatapointsToGraph(String application, String graph, String chosenRuns, List<String> orderedTxnsToGraphIdList) {

		// System.out.println("** in findDatapointsToGraph for : " + application ) ;
		List<Datapoint> datapoints =  new ArrayList<Datapoint>();  
		Object datapointMetric = null;
		
		if ( StringUtils.isEmpty(chosenRuns) || orderedTxnsToGraphIdList.size() == 0  ){ 
			return datapoints;     
		}
				
		String sqlTxnCommaSepList = " ";
		for (String txnId : orderedTxnsToGraphIdList) {
			sqlTxnCommaSepList = sqlTxnCommaSepList + "'" + txnId +  "',";
		}
		sqlTxnCommaSepList = sqlTxnCommaSepList.substring(0, sqlTxnCommaSepList.length()-1);  //remove final trailing comma
		
		GraphMapping graphMapping = graphMappingDAO.findGraphMapping(graph);
		
		// runs from most recent back, txn_ids case sensitive (actually utf8 if using suggested db collation) order
	
		String sql = "SELECT RUN_TIME, TXN_ID, " + transactionDBColNameOrDerivationForRequestedValues(graphMapping) + " AS VALUE_TO_PLOT " 
				    + " FROM TRANSACTION WHERE APPLICATION = '" + application + "' AND TXN_TYPE = '" + graphMapping.getTxnType() + "'"  
					+ " AND RUN_TIME in ( '" + chosenRuns.replaceAll(",", "','") + "' ) "
					+ " AND TXN_ID   in ( " + sqlTxnCommaSepList  + " ) " 
					+ " ORDER BY 1 DESC, 2 ASC";   

//		System.out.println("********************************************************** "  );
//		System.out.println("TransactionDAOjdbcTemplateImpl:findDatapointsToGraph sql : " + sql  );
//		System.out.println("********************************************************** "  );		
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		
		int numRunsBeingGraphed = 0;   // eg, for initial request..
		if (chosenRuns != ""  ){
			numRunsBeingGraphed = StringUtils.countMatches(chosenRuns, ",") + 1;  
		} else {
			System.out.println(" ****** no chosen runs passed to findDatapointsToGraph!!!!!   Application was " + application);
		}
		
		int runTimeCount = 0;
		String runTime = "";
		String prevRunTime = "";

		for (int i = 0; i < rows.size()  &&  runTimeCount <= numRunsBeingGraphed ; i++) {
			Map<String, Object> row = rows.get(i);
			
			Datapoint datapoint = new Datapoint();
			runTime = (String)row.get("RUN_TIME");
			datapoint.setRunTime(runTime);
			datapoint.setTxnId((String)row.get("TXN_ID"));
			datapointMetric = row.get("VALUE_TO_PLOT");
			
			if ( datapointMetric.getClass().toString().contains("BigDecimal")  ){
				datapoint.setValue((BigDecimal)datapointMetric);
			} else { // assume 'long'
				datapoint.setValue(new BigDecimal((Long)datapointMetric));   
			}	
				
			if ( !runTime.equals(prevRunTime) ){
				runTimeCount++;
				prevRunTime = runTime;
			}
		
			if ( runTimeCount <= numRunsBeingGraphed){
				datapoints.add(datapoint);
			}
		}
		return datapoints;
	}


	private String transactionDBColNameOrDerivationForRequestedValues(GraphMapping graphMapping) {
		String transactionDBColNameOrDerivationForRequestedValues = 
				AppConstantsMetrics.getValueDerivatonToSourceFieldMap().get(graphMapping.getValueDerivation());
	
		if ( transactionDBColNameOrDerivationForRequestedValues == null){
			// no mapping - we therefore assume a direct name translation from the derivation entry 
			transactionDBColNameOrDerivationForRequestedValues =  graphMapping.getValueDerivation();
		}
		return transactionDBColNameOrDerivationForRequestedValues;
	}


	private String constructSqlTxnIdsLikeNotLike(String sqlSelectLike, String sqlSelectNotLike) {
		String sqlAnd = "";
		if (sqlSelectLike != "%" ){
			sqlAnd =" AND TXN_ID LIKE '%" +  sqlSelectLike + "%' ";  
		}
		if (sqlSelectNotLike != "" ){
			sqlAnd = sqlAnd + " AND NOT (TXN_ID LIKE '%" +  sqlSelectNotLike + "%' ) ";  
		}
		return sqlAnd;
	}


	private String constructSqlIn(String chosenTxnsList) {
		String chosenTxnsListWithQuotes =  chosenTxnsList.replaceAll("," , "','");
		String sqlIn =" AND TXN_ID IN ( '" +  chosenTxnsListWithQuotes + "' ) ";  
		return sqlIn;
	}

	
}
