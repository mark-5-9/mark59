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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.metrics.application.AppConstants;
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
	
	public final static String lT_SMALLEST_VALUE_POSSIBLE_ON_TRANSACTON_TABLE = "-99999999999999999999";
	
	@Autowired  
	private DataSource dataSource;


	@Autowired
	GraphMappingDAO graphMappingDAO; 	
	
	
	@Override
	public void insert(Transaction transaction) {
		String sql = "INSERT INTO transaction "
				+ "(APPLICATION, RUN_TIME, TXN_ID, TXN_TYPE, "
				+ "TXN_MINIMUM, TXN_AVERAGE, TXN_MAXIMUM, TXN_STD_DEVIATION, TXN_90TH, TXN_PASS, TXN_FAIL, TXN_STOP, TXN_FIRST, TXN_LAST, TXN_SUM  )"
				+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
//		System.out.println("TransactionDAOjdbcTemplateImpl insert [" + transaction.toString() + "]"   );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] { transaction.getApplication(), transaction.getRunTime(), 
				transaction.getTxnId(), transaction.getTxnType(),
				transaction.getTxnMinimum(), transaction.getTxnAverage(), transaction.getTxnMaximum(),
				transaction.getTxnStdDeviation(), transaction.getTxn90th(),
				transaction.getTxnPass(), transaction.getTxnFail(), transaction.getTxnStop(), 
				transaction.getTxnFirst(), transaction.getTxnLast(), transaction.getTxnSum() });
	}
	
	
	public Transaction getTransaction(String application, String txnType, String runTime, String txnId) {

		List<Transaction> transactionList = new ArrayList<Transaction>();

		String sql = "SELECT * FROM transaction WHERE APPLICATION = '" + application + "' AND " +
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
	public Object getTransactionValue(String application, String txnType, String runTime, String txnId, String transactionField) {

		List<Object> transactionValues =  new ArrayList<Object>();  
		String sql = "SELECT " + transactionField + " FROM transaction " +
										  	   "WHERE APPLICATION = '" + application + "' AND " +
				                                        "RUN_TIME = '" + runTime + "' AND " +
				                                        "TXN_TYPE = '" + txnType + "' AND " +
				                                          "TXN_ID = '" + txnId + "' ";
		
//		System.out.println("TransactionDAOjdbcTemplateImpl.getTransactionValue sql: " + sql  );

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
		String sql = "delete from transaction where APPLICATION='" + application + "' "
												+ "and RUN_TIME='" + runTime + "'";
//		System.out.println("performing : " + sql );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}
	
	@Override
	public void deleteAllForApplication(String application) {
		String sql = "delete from transaction where APPLICATION='" + application + "' ";
		System.out.println("delete all transactions for application : " + sql );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

	
	@Override
	@SuppressWarnings("rawtypes")
	public List<String> determineTransactionIdsToGraph(String transactionIdsSQL){

		List<String> transactionIds = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(transactionIdsSQL);
		
		for (Map row : rows) {
			transactionIds.add( (String)row.get("TXN_ID") );
//			System.out.println("populating TXN_ID for trending page : " + row.get("TXN_ID")  ) ;
		}	
		return  transactionIds;
	}


	@Override
	public Collection<Transaction> findTransactions(String application, String txnType, String runTime, String transactionIdsSQL){

		List<String> transactionIds = determineTransactionIdsToGraph(transactionIdsSQL);
		
		Collection<Transaction> transactions = new ArrayList<Transaction>();
		Transaction transaction = new Transaction(); 
		for (String txnId : transactionIds) {
			transaction = getTransaction(application, AppConstants.MAPPED_DATA_TYPES.TRANSACTION.name(), runTime, txnId);
			if (transaction != null) {
				transactions.add(transaction);
			}
		}		
		return  transactions;
	}
	
	
	
	@Override
	public String transactionIdsSQL(String application, String graph, String sqlSelectLike, String sqlSelectNotLike, boolean manuallySelectTxns , String chosenTxns, String nthRankedTxn, String chosenRuns, boolean useRawSQL, String rawTransactionIdsSQL) {
		
		String sql;
		
		if (useRawSQL){
			
			sql = rawTransactionIdsSQL; 
			
		} else {
			
			GraphMapping graphMapping = graphMappingDAO.findGraphMapping(graph);

			String fieldNameOrDerivationContainingDatapointValues = transactionDBfieldContainingDatapointValues(graphMapping);
			
//			Select the most recent run that has data for the txn type we are going to plot 
			
			String sqlRunTime = " ( SELECT MAX(RUN_TIME)"
					+ " FROM TRANSACTION WHERE APPLICATION =  '" + application + "' "
					+ "AND TXN_TYPE =  '" + graphMapping.getTxnType() + "' "				
					+ "AND RUN_TIME in ( " + chosenRuns +  " )"   				
					+ " ) ";			
			
			Integer nthRankedTxnInt;
			try {
				nthRankedTxnInt = Integer.parseInt(nthRankedTxn);
			} catch (NumberFormatException e) {
				nthRankedTxnInt = Integer.MAX_VALUE;		
			}
			if ( nthRankedTxnInt <= 0 ) {
				nthRankedTxnInt = Integer.MAX_VALUE;		
			}
			
			String rankerViewSql = "SELECT TXN_ID, ( " + fieldNameOrDerivationContainingDatapointValues + " ) as rankedValue" 
					+ ",  COALESCE (NTH_VALUE(" + fieldNameOrDerivationContainingDatapointValues + ", " + nthRankedTxnInt + ") OVER(w) , " + lT_SMALLEST_VALUE_POSSIBLE_ON_TRANSACTON_TABLE + " )  as 'RANKER' "
					+ " FROM TRANSACTION WHERE APPLICATION =  '" + application + "' "
					+ "AND TXN_TYPE =  '" + graphMapping.getTxnType() + "' "
					+ "AND RUN_TIME = " + sqlRunTime; 
			
			if (! manuallySelectTxns ){
				rankerViewSql = rankerViewSql
					+ constructSqlTxnIdsLikeNotLike(sqlSelectLike, sqlSelectNotLike )
					+ "  AND TXN_ID NOT IN ( " + SlaUtilities.listOfIgnoredTransactionsSQL(application) + " ) " ; 

			} else {
				rankerViewSql = rankerViewSql + constructSqlIn(chosenTxns);
			}
			
			rankerViewSql = rankerViewSql +  " WINDOW w AS (ORDER BY " + fieldNameOrDerivationContainingDatapointValues + " DESC ) ";
			
			sql = "SELECT TXN_ID, rankedValue, RANKER  FROM ( " + rankerViewSql + " ) AS rankerView " 
					+ " WHERE rankedValue >= RANKER "
					+ " ORDER BY 1 ASC";
		}
//		System.out.println("TransactionDAOjdbcTemplateImpl.transactionIdsSQL sql = " + sql );
		return sql;
	}

	

	@Override
	public List<Datapoint> findDatapointsToGraph(String application, String graph, String chosenRuns, List<String> transactionIdsList) {
		
//		System.out.println("** in findDatapointsToGraph for : " + application ) ;

		List<Datapoint> datapoints =  new ArrayList<Datapoint>();  
		
		if ( StringUtils.isEmpty(chosenRuns)){ 
			return datapoints;     
		}
		
		Object datapointMetric = null;
		
		//String transactionIdsSQL =  transactionIdsSQL(application, txnType, sqlSelectLike, sqlSelectNotLike, manuallySelectTxns, chosenTxns, chosenRuns, useRawSQL, rawTransactionIdsSQL );
		
		if ( transactionIdsList.size() == 0 ){
			return datapoints;
		}
				
		String sqlTxnCommaSepList = " ";
		for (String txnId : transactionIdsList) {
			sqlTxnCommaSepList = sqlTxnCommaSepList + "'" + txnId +  "',";
		}
		sqlTxnCommaSepList = sqlTxnCommaSepList.substring(0, sqlTxnCommaSepList.length()-1);  //remove final trailing comma
		String sqlAndTnxName = " AND TXN_ID in ( " + sqlTxnCommaSepList  + " ) ";

		String sqlAndRunTime = " AND RUN_TIME in ( " +  chosenRuns  + " ) ";
		String orderBy =  " ORDER BY 1 DESC, 2 ASC";
		
		GraphMapping graphMapping = graphMappingDAO.findGraphMapping(graph);
		String fieldNameOrDerivationContainingDatapointValues = transactionDBfieldContainingDatapointValues(graphMapping);
		
		String sql = "SELECT RUN_TIME, TXN_ID, " + fieldNameOrDerivationContainingDatapointValues + " FROM TRANSACTION WHERE APPLICATION = '" + application + "' AND TXN_TYPE = '" + graphMapping.getTxnType() + "'"  
					+ sqlAndRunTime 
					+ sqlAndTnxName 
					+ orderBy;

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
			datapointMetric = row.get(fieldNameOrDerivationContainingDatapointValues);
			
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


	private String transactionDBfieldContainingDatapointValues(GraphMapping graphMapping) {
		String fieldNameOrDerivationContainingDatapointValues = AppConstants.getValueDerivatonToSourceFieldMap().get( graphMapping.getValueDerivation() );
	
		if ( fieldNameOrDerivationContainingDatapointValues == null){
			// no mapping - we therefore assume a direct name translation from the derivation entry 
			fieldNameOrDerivationContainingDatapointValues =  graphMapping.getValueDerivation();
		}
		return fieldNameOrDerivationContainingDatapointValues;
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
