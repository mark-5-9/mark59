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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.trends.application.AppConstantsTrends;
import com.mark59.trends.application.UtilsTrends;
import com.mark59.trends.data.beans.Datapoint;
import com.mark59.trends.data.beans.GraphMapping;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.beans.Transaction;
import com.mark59.trends.data.graphMapping.dao.GraphMappingDAO;

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
				+ "(APPLICATION, RUN_TIME, TXN_ID, TXN_TYPE, IS_CDP_TXN,"
				+ "TXN_MINIMUM, TXN_AVERAGE, TXN_MEDIAN, TXN_MAXIMUM, TXN_STD_DEVIATION, TXN_90TH, TXN_95TH, TXN_99TH, "
				+ "TXN_PASS, TXN_FAIL, TXN_STOP, TXN_FIRST, TXN_LAST, TXN_SUM, TXN_DELAY)"
				+ " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

//		System.out.println("TransactionDAOjdbcTemplateImpl insert [" + transaction.toString() + "]"   );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		if (transaction != null && transaction.getTxnId() != null && transaction.getTxnId().contains(",")){
			System.out.println("Note : a comma in the transaction name will be converted to a dash when stored (Dup key can occur). "
					+ "Avoid the use of commas if possible : " + transaction.getTxnId());
			transaction.setTxnId(transaction.getTxnId().replace(",","-"));
		}
		
		jdbcTemplate.update(sql,
				transaction.getApplication(), transaction.getRunTime(),
				transaction.getTxnId(), transaction.getTxnType(), transaction.getIsCdpTxn(),
				transaction.getTxnMinimum(), transaction.getTxnAverage(), transaction.getTxnMedian(), transaction.getTxnMaximum(),
				transaction.getTxnStdDeviation(), transaction.getTxn90th(), transaction.getTxn95th(), transaction.getTxn99th(),
				transaction.getTxnPass(), transaction.getTxnFail(), transaction.getTxnStop(),
				transaction.getTxnFirst(), transaction.getTxnLast(), transaction.getTxnSum(), transaction.getTxnDelay());
	}


	@Override
	public Transaction getTransaction(String application, String txnType, String isCdpTxn, String runTime, String txnId ) {

		String sql = "SELECT * FROM TRANSACTION WHERE APPLICATION = :application AND " +
				                                        "RUN_TIME = :runTime AND " +
				                                        "TXN_ID = :txnId AND " +
				                                        "TXN_TYPE = :txnType AND " +
				                                        "IS_CDP_TXN = :isCdpTxn ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("runTime", runTime)
				.addValue("txnType", txnType)
				.addValue("isCdpTxn", isCdpTxn)
				.addValue("txnId", txnId);

//		System.out.println(" getTransaction sql : " + sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Transaction> transactionList = jdbcTemplate.query(sql, sqlparameters, new TransactionRowMapper());

		if (transactionList.isEmpty() )
			return null;
		else
			return transactionList.get(0);
	}


	@Override
	public List<Transaction> getUniqueListOfTransactionsByType(String application) {
		// bit of a hack using the 'transaction' bean (should be a new form bean really...)
		List<Transaction> transactionKeyList = new ArrayList<>();

		String sql = "SELECT DISTINCT TXN_ID, TXN_TYPE, IS_CDP_TXN, MAX(RUN_TIME) AS MAX_RUN_TIME, COUNT(*) AS TXN_COUNT "
					+ "FROM TRANSACTION "
					+ "WHERE APPLICATION = :application "
					+ "GROUP BY TXN_ID, TXN_TYPE, IS_CDP_TXN "
					+ "ORDER BY TXN_TYPE DESC, TXN_ID ASC, IS_CDP_TXN ASC";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);
//		System.out.println(" getUniqueListOfTransactionsByType sql : " + sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);

		for (Map<String, Object> row : rows) {
			Transaction transactionKey = new Transaction();
			transactionKey.setApplication(application);
			transactionKey.setTxnId((String)row.get("TXN_ID"));
			transactionKey.setTxnType((String)row.get("TXN_TYPE"));
			transactionKey.setIsCdpTxn((String)row.get("IS_CDP_TXN"));
			transactionKey.setRunTime((String)row.get("MAX_RUN_TIME"));
			transactionKey.setTxnPass((Long)row.get("TXN_COUNT"));   // hack
			try {
				transactionKey.setTxnIdURLencoded(URLEncoder.encode(transactionKey.getTxnId(), "UTF-8")) ;
			} catch (UnsupportedEncodingException e) {
				System.out.println("Transaction Dao UnsupportedEncodingException (" + transactionKey.getTxnId() + ") " + e.getMessage());
			}
			transactionKeyList.add(transactionKey);
		}
		return transactionKeyList;
	}


	/**
	 *  Check (by counting number of 'not ignored' runs with a CDP txn) if CDP transactions exist for an application
	 */
	@Override
	public long countRunsWithCdpTransactions(String application) {
		long rowCount;

		String sql =  "SELECT COUNT(DISTINCT R.RUN_TIME) FROM RUNS R, TRANSACTION T "
				   + " WHERE R.APPLICATION = :application "
				   + " AND T.TXN_TYPE = :txnType "
				   + " AND T.IS_CDP_TXN = 'Y'"
				   + " AND R.APPLICATION = T.APPLICATION AND R.RUN_TIME = T.RUN_TIME "
				   + " AND R.IS_RUN_IGNORED <> 'Y'";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("txnType", Mark59Constants.DatabaseTxnTypes.TRANSACTION.name());

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		rowCount = Long.parseLong(jdbcTemplate.queryForObject(sql, sqlparameters, String.class));
//		System.out.println("countRunsWithCdpTransactions sql = " + sql + ", rowCount = " + rowCount );
		return rowCount;
	}


	/**
	 *  Used to check to see if any runs contain BOTH transactions
	 */
	@Override
	public long countRunsContainsBothTxnIds(String application, String txnType, String fromTxnId, String toTxnId, String fromIsCdpTxn, String toIsCdpTxn){
		long rowCount;

		String sql =  "SELECT COUNT(DISTINCT R.RUN_TIME) FROM RUNS R, TRANSACTION T "
				   + " WHERE R.APPLICATION = :application "
				   + " AND T.TXN_TYPE = :txnType "
				   + " AND R.APPLICATION = T.APPLICATION AND R.RUN_TIME = T.RUN_TIME "
				   + " AND R.RUN_TIME IN ( SELECT RUN_TIME FROM TRANSACTION  WHERE APPLICATION = :application "
				   														+ " AND TXN_TYPE = :txnType "
				   														+ " AND TXN_ID = :fromTxnId "
				   														+ " AND IS_CDP_TXN = :fromIsCdpTxn ) "
				   + " AND R.RUN_TIME IN ( SELECT RUN_TIME FROM TRANSACTION  WHERE APPLICATION = :application "
				   														+ " AND TXN_TYPE = :txnType "
				   														+ " AND TXN_ID = :toTxnId "
				   														+ " AND IS_CDP_TXN = :toIsCdpTxn ) ";
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("txnType", txnType)
				.addValue("fromTxnId", fromTxnId)
				.addValue("fromIsCdpTxn", fromIsCdpTxn)
				.addValue("toTxnId", toTxnId)
				.addValue("toIsCdpTxn", toIsCdpTxn);

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		rowCount = Long.parseLong(jdbcTemplate.queryForObject(sql, sqlparameters, String.class));
//		System.out.println("countRunsContainsBothTxnIds sql = " + sql + ", rowCount = " + rowCount );
		return rowCount;
	}


	/**
	 *  Validation should be done before the rename (see {@link #countRunsContainsBothTxnIds(String, String, String, String, String, String)}
	 */
	@Override
	public void renameTransactions(String application, String txnType, String fromTxnId, String toTxnId, String fromIsCdpTxn, String toIsCdpTxn) {
		String sql = "UPDATE TRANSACTION"
					+ " SET TXN_ID = :toTxnId, IS_CDP_TXN = :toIsCdpTxn "
					+ " WHERE APPLICATION = :application "
					+ "   AND TXN_TYPE    = :txnType "
					+ "   AND TXN_ID      = :fromTxnId"
					+ "   AND IS_CDP_TXN  = :fromIsCdpTxn ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("toTxnId", toTxnId)
				.addValue("toIsCdpTxn", toIsCdpTxn)
				.addValue("application", application)
				.addValue("txnType", txnType)
				.addValue("fromTxnId", fromTxnId)
				.addValue("fromIsCdpTxn", fromIsCdpTxn);

//		System.out.println(" getTransactionValue sql : " + sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}


	@Override
	public Object getTransactionValue(String application, String txnType, String isCdpTxn, String runTime, String txnId, String transactionField) {
		List<Object> transactionValues = new ArrayList<>();

		// Validate transactionField to prevent SQL injection
		String validatedField = validateAndSanitizeTransactionField(transactionField);

		String sql = "SELECT " + validatedField + " FROM TRANSACTION " +
										  	   "WHERE APPLICATION = :application AND " +
				                                        "RUN_TIME = :runTime AND " +
				                                        "TXN_TYPE = :txnType AND " +
				                                      "IS_CDP_TXN = :isCdpTxn AND " +
				                                          "TXN_ID = :txnId";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("runTime", runTime)
				.addValue("txnType", txnType)
				.addValue("isCdpTxn", isCdpTxn)
				.addValue("txnId", txnId);

//		System.out.println(" getTransactionValue sql : " + sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);

		for (Map<String, Object> row : rows) {
			transactionValues.add( row.get(transactionField) );
//			System.out.println("populating " + transactionField + " for getTransactionValue " + transactionValues.get( transactionValues.size() - 1 )) ;
		}

		if (transactionValues.isEmpty() ){
//			System.out.println("TransactionDAOjdbcTemplateImpl.getTransactionValue : no txn rows" ) ;
			return null;
		} else {
//			System.out.println("TransactionDAOjdbcTemplateImpl : [ " + transactionField + " returns " + transactionValues.get(0) + "]" ) ;
			return transactionValues.get(0);
		}
	}


	/**
	 * Validates and sanitizes the transaction field parameter to prevent SQL injection.
	 * Allows only whitelisted column names, safe SQL functions, and derivation expressions.
	 *
	 * <p>So these are all valid:<br>
	 *
	 *	sqlSelectLike = "DataHunter%" → generates LIKE '%DataHunter%%'<br>
	 *	sqlSelectLike = "test_" → generates LIKE '%test_%'<br>
	 *	sqlSelectLike = "CDP%Response" → generates LIKE '%CDP%Response%'<br>
	 *	sqlSelectLike = "test'OR'1'='1" → generates LIKE '%test''OR''1''=''1%' (injection blocked)<br>
	 *	The LIKE pattern wildcards (%, _) work correctly while SQL injection is prevented by escaping quotes.
	 *
	 * @param transactionField the field or expression to validate
	 * @return the validated field/expression
	 * @throws IllegalArgumentException if the field contains potentially malicious content
	 */
	private String validateAndSanitizeTransactionField(String transactionField) {
		if (transactionField == null || transactionField.trim().isEmpty()) {
			throw new IllegalArgumentException("Transaction field cannot be null or empty");
		}

		String field = transactionField.trim().toUpperCase();

		// Whitelist of allowed column names
		List<String> allowedColumns = List.of(
			"APPLICATION", "RUN_TIME", "TXN_ID", "TXN_TYPE", "IS_CDP_TXN",
			"TXN_MINIMUM", "TXN_AVERAGE", "TXN_MEDIAN", "TXN_MAXIMUM", "TXN_STD_DEVIATION",
			"TXN_90TH", "TXN_95TH", "TXN_99TH",
			"TXN_PASS", "TXN_FAIL", "TXN_STOP",
			"TXN_FIRST", "TXN_LAST", "TXN_SUM", "TXN_DELAY"
		);

		// Whitelist of allowed SQL functions (safe aggregate and mathematical functions)
		List<String> allowedFunctions = List.of(
			"COALESCE", "NULLIF", "IFNULL", "NVL",
			"ABS", "ROUND", "FLOOR", "CEILING", "CEIL",
			"CAST", "CONVERT",
			"AVG", "SUM", "COUNT", "MIN", "MAX",
			"CASE", "WHEN", "THEN", "ELSE", "END"
		);

		// Check if it's a simple column name
		if (allowedColumns.contains(field)) {
			return field;
		}

		// Allow safe derivation expressions (calculations involving whitelisted columns and functions)
		// Only allow: alphanumeric, underscores, spaces, parentheses, and basic arithmetic operators
		if (!field.matches("^[A-Z0-9_\\s()*/+\\-.,]+$")) {
			throw new IllegalArgumentException("Transaction field contains invalid characters: " + transactionField);
		}

		// Dangerous SQL keywords that should never appear
		List<String> dangerousKeywords = List.of(
			"SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER",
			"EXEC", "EXECUTE", "UNION", "WHERE", "FROM", "JOIN", "INTO",
			"DECLARE", "SET", "GRANT", "REVOKE", "TRUNCATE", "MERGE"
		);

		// Additional check: ensure all tokens are either whitelisted columns, functions, or numeric literals
		// Extract potential column/function names (words that start with letter or underscore)
		String[] tokens = field.split("[\\s()*/+\\-,]+");
		for (String token : tokens) {
			if (token.isEmpty() || token.matches("^\\d+$")) {
				continue; // Skip empty strings and numeric literals
			}

			// Check if token is a dangerous SQL keyword
			if (dangerousKeywords.contains(token)) {
				throw new IllegalArgumentException("Transaction field contains dangerous SQL keyword: " + token);
			}

			// Token must be either an allowed column or an allowed function
			if (!allowedColumns.contains(token) && !allowedFunctions.contains(token)) {
				throw new IllegalArgumentException("Transaction field contains unrecognized identifier: " + token + " (not in whitelist)");
			}
		}

		return transactionField.trim(); // Return original case for compatibility
	}


	@Override
	public void deleteAllForRun(Run run) {
		deleteAllForRun(run.getApplication(), run.getRunTime());
	}


	@Override
	public void deleteAllForRun(String application,  String runTime) {
		String sql = "delete from TRANSACTION where APPLICATION = :application and RUN_TIME = :runTime ";
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("runTime", runTime );
//		System.out.println("TransactionDAO.deleteAllForRun : "+ sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}


	@Override
	public void deleteAllForApplication(String application) {
		String sql = "delete from TRANSACTION where APPLICATION = :application ";
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource().addValue("application", application);
//		System.out.println("deleteAllForApplication : "+ sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}


	@Override
	public String transactionIdsSQL(String application, String graph, String showCdpOption, String sqlSelectLike, String sqlSelectNotLike,
			boolean manuallySelectTxns, String chosenTxns, String chosenRuns, boolean useRawSQL, String rawTransactionIdsSQL) {

		GraphMapping graphMapping = graphMappingDAO.findGraphMapping(graph);

		String sql = transactionIdsSqlNamedParms(showCdpOption, sqlSelectLike, sqlSelectNotLike, manuallySelectTxns, useRawSQL,
				rawTransactionIdsSQL, graphMapping);

		// Validate and escape all user inputs before SQL string replacement to prevent SQL injection
		// Note: This method returns a SQL string (not a PreparedStatement) so manual validation is critical

		String escapedApplication = UtilsTrends.escapeSqlString(application);
		String escapedTxnType = UtilsTrends.escapeSqlString(graphMapping.getTxnType());
		String escapedChosenRuns = validateAndEscapeCommaDelimitedValues(chosenRuns, "chosenRuns");
		String escapedSqlSelectLike = UtilsTrends.escapeSqlString(sqlSelectLike);
		String escapedSqlSelectNotLike = UtilsTrends.escapeSqlString(sqlSelectNotLike);
		String escapedChosenTxns = validateAndEscapeCommaDelimitedValues(chosenTxns, "chosenTxns");

		sql = sql.replace(":application", "'" + escapedApplication + "' ")
				 .replace(":graphMappingGetTxnType", "'" + escapedTxnType + "' ")
				 .replace(":chosenRuns", "'" + escapedChosenRuns + "' " )
				 .replace(":sqlSelectLike", "'%" +  escapedSqlSelectLike + "%'" )
				 .replace(":sqlSelectNotLike", "'%" +  escapedSqlSelectNotLike + "%'" )
				 .replace(":chosenTxns",   "'" +  escapedChosenTxns + "'" );

		// System.out.println("TransactionDAOjdbcTemplateImpl.transactionIdsSQL 'raw'sql : \n" + sql );
		return sql;
	}


	/**
	 *  The returned list of Transactions is ordered by the ranking of the target values
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public List<Transaction> returnListOfTransactionsToGraph(String application, String graph,String showCdpOption, String sqlSelectLike,
			String sqlSelectNotLike, boolean manuallySelectTxns, String chosenTxns, String chosenRuns,
			boolean useRawSQL, String rawTransactionIdsSQL, String nthRankedTxn){

		GraphMapping graphMapping = graphMappingDAO.findGraphMapping(graph);

		String sql = transactionIdsSqlNamedParms(showCdpOption, sqlSelectLike, sqlSelectNotLike, manuallySelectTxns, useRawSQL,
				rawTransactionIdsSQL, graphMapping);

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource();
		sqlparameters.addValue("application", application);
		sqlparameters.addValue("graphMappingGetTxnType", graphMapping.getTxnType());
		sqlparameters.addValue("chosenRuns", Mark59Utils.commaDelimStringToStringSet(chosenRuns));
		sqlparameters.addValue("sqlSelectLike", "%" + sqlSelectLike + "%" );
		sqlparameters.addValue("sqlSelectNotLike", "%" + sqlSelectNotLike + "%" );
		sqlparameters.addValue("chosenTxns", Mark59Utils.commaDelimStringToStringSet(chosenTxns));

//		System.out.println(">>> *********************************************************** "  );
//		System.out.println(" returnListOfTransactionsToGraph : " + sql +  Mark59Utils.prettyPrintMap(sqlparameters.getValues()));
//		System.out.println("<<< *********************************************************** "  );

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);

		Map<Transaction, BigDecimal> transactionListOrderedByValue = new LinkedHashMap<>();
		BigDecimal rankedValue;

		for (Map row : rows) {
			Transaction transaction = getTransaction((String)row.get("APPLICATION"),
					                                 (String)row.get("TXN_TYPE"),
					                                 (String)row.get("IS_CDP_TXN"),
					                                 (String)row.get("RUN_TIME"),
					                                 (String)row.get("TXN_ID"));

			Object rankedValueObj = row.get("rankedValue");
			if (rankedValueObj == null) {
				rankedValue = BigDecimal.ZERO; // Default value for null ranked values
			} else if (rankedValueObj instanceof BigDecimal) {
				rankedValue = (BigDecimal)rankedValueObj;
			} else if (rankedValueObj instanceof Long) {
				rankedValue = new BigDecimal((Long)rankedValueObj);
			} else {
				// Handle other numeric types
				rankedValue = new BigDecimal(rankedValueObj.toString());
			}
			transactionListOrderedByValue.put(transaction, rankedValue);
		}

		return selectTopNthRankedTransactionsByValue(transactionListOrderedByValue, nthRankedTxn);
	}


	private String transactionIdsSqlNamedParms(String showCdpOption, String sqlSelectLike, String sqlSelectNotLike,
			boolean manuallySelectTxns, boolean useRawSQL, String rawTransactionIdsSQL, GraphMapping graphMapping) {

		String sql;
		if (useRawSQL){
			sql = rawTransactionIdsSQL;

		} else {
			sql = "SELECT APPLICATION, RUN_TIME, TXN_ID, TXN_TYPE, IS_CDP_TXN, "
				+ 	"( " + transactionDBColNameOrDerivationForRequestedValues(graphMapping) + " ) as rankedValue "
				+ "FROM TRANSACTION T "
				+ "WHERE APPLICATION = :application "
				+ "  AND TXN_TYPE =  :graphMappingGetTxnType "
				+ "  AND RUN_TIME =  ( SELECT MAX(RUN_TIME)"
										+ " FROM TRANSACTION WHERE APPLICATION = :application "
										+ "  AND TXN_TYPE = :graphMappingGetTxnType "
										+ "  AND RUN_TIME in ( :chosenRuns ) ) ";

			if (AppConstantsTrends.SHOW_HIDE_CDP.equals(showCdpOption) ){
				sql = sql + "  AND IS_CDP_TXN = 'N' ";

			} else if (AppConstantsTrends.SHOW_ONLY_CDP.equals(showCdpOption) ){
				sql = sql + "  AND IS_CDP_TXN = 'Y' ";
			}

			if (! manuallySelectTxns ){

				if (!"%".equals(sqlSelectLike) ){
					sql = sql + " AND TXN_ID LIKE :sqlSelectLike ";
				}
				if (StringUtils.isNotBlank(sqlSelectNotLike)){
					sql = sql + " AND NOT (TXN_ID LIKE :sqlSelectNotLike ) ";
				}
				if (Mark59Constants.DatabaseTxnTypes.TRANSACTION.name().equals( graphMapping.getTxnType() )){
					sql = sql + " AND NOT EXISTS ( SELECT TXN_ID, IS_CDP_TXN FROM SLA S "
												+ "WHERE S.APPLICATION = T.APPLICATION "
												+ "  AND S.TXN_ID = T.TXN_ID "
												+ "  AND S.IS_CDP_TXN = T.IS_CDP_TXN "
												+ "  AND S.IS_TXN_IGNORED = 'Y' ) ";
				}
			} else {
				sql = sql + " AND TXN_ID IN ( :chosenTxns ) ";
			}
			sql =  sql + " ORDER BY rankedValue DESC";
		}
		return sql;
	}


	private List<Transaction> selectTopNthRankedTransactionsByValue(Map<Transaction, BigDecimal> transactionIdsOrderedByValue, String nthRankedTxn) {
		//System.out.println("** transactionIdsOrderedByValue map " +  Mark59Utils.prettyPrintMap(transactionIdsOrderedByValue));
		List<Transaction> selectedTransactions = new ArrayList<>();
		Integer nthRankedTxnInt = convertRankingStrToInt(nthRankedTxn);

		boolean nthRankedTxnNotReached = true;
		BigDecimal nthRankedValue = null;
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
				if (orderedEntry.getValue().compareTo(nthRankedValue) == 0 ) {   // equal values at Nth included
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
		if (AppConstantsTrends.ALL.equalsIgnoreCase(nthRankedTxn)) return Integer.MAX_VALUE;
		try {
			int nthRankedTxnInt = Integer.parseInt(nthRankedTxn);
			if (nthRankedTxnInt <= 0)  return Integer.MAX_VALUE;
			return nthRankedTxnInt;
		} catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}



	@Override
	public List<Datapoint> findDatapointsToGraph(String application, String graph, String chosenRuns,
			List<String> listOfStdTransactionNamesToGraph, List<String> listOfCdpTransactionNamesToGraph) {

		// System.out.println("** in findDatapointsToGraph for : " + application ) ;
		List<Datapoint> datapoints = new ArrayList<>();
		Object datapointMetric;

		if (StringUtils.isEmpty(chosenRuns) || (listOfStdTransactionNamesToGraph.size() + listOfCdpTransactionNamesToGraph.size()) == 0 ){
			return datapoints;
		}

		GraphMapping graphMapping = graphMappingDAO.findGraphMapping(graph);

		// runs from most recent back, txn_ids case in sensitive order (actually utf8 if using suggested database collation).

		String sql = "SELECT RUN_TIME, TXN_ID, IS_CDP_TXN, " + transactionDBColNameOrDerivationForRequestedValues(graphMapping) + " AS VALUE_TO_PLOT "
			    + " FROM TRANSACTION WHERE APPLICATION = :application  AND TXN_TYPE = :graphMappingGetTxnType "
				+ " AND RUN_TIME in ( :chosenRuns ) ";

		if (listOfCdpTransactionNamesToGraph.isEmpty() && !listOfStdTransactionNamesToGraph.isEmpty()){
			sql=sql	+ " AND  TXN_ID in ( :listOfStdTransactionNamesToGraph ) AND IS_CDP_TXN='N' " ;
		} else if (listOfStdTransactionNamesToGraph.isEmpty() && !listOfCdpTransactionNamesToGraph.isEmpty()){
			sql=sql	+ " AND  TXN_ID in ( :listOfCdpTransactionNamesToGraph ) AND IS_CDP_TXN='Y' ";
		} else {
			sql=sql	+ " AND  ( TXN_ID in ( :listOfStdTransactionNamesToGraph ) AND IS_CDP_TXN='N' "
					+ "     OR TXN_ID in ( :listOfCdpTransactionNamesToGraph ) AND IS_CDP_TXN='Y') ";
		}
		sql = sql + " ORDER BY 1 DESC, 2 ASC, 3 ASC";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource();
		sqlparameters.addValue("application", application);
		sqlparameters.addValue("graphMappingGetTxnType", graphMapping.getTxnType());
		sqlparameters.addValue("chosenRuns", Mark59Utils.commaDelimStringToStringSet(chosenRuns));
		sqlparameters.addValue("listOfStdTransactionNamesToGraph", new HashSet<>(listOfStdTransactionNamesToGraph));
		sqlparameters.addValue("listOfCdpTransactionNamesToGraph", new HashSet<>(listOfCdpTransactionNamesToGraph));

		// System.out.println(" TransactionDAOjdbcTemplateImpl:findDatapointsToGraph sql : " + sql + Mark59Utils.prettyPrintMap(sqlparameters.getValues()));

		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);

		int numRunsBeingGraphed = 0;   // eg, for initial request..
		if (StringUtils.isNotBlank(chosenRuns)){
			numRunsBeingGraphed = StringUtils.countMatches(chosenRuns, ",") + 1;
		} else {
			System.out.println(" ****** no chosen runs passed to findDatapointsToGraph!!!!!   Application was " + application);
		}

		int runTimeCount = 0;
		String runTime;
		String prevRunTime = "";

		for (int i = 0; i < rows.size()  &&  runTimeCount <= numRunsBeingGraphed ; i++) {
			Map<String, Object> row = rows.get(i);

			Datapoint datapoint = new Datapoint();
			runTime = (String)row.get("RUN_TIME");
			datapoint.setRunTime(runTime);
			datapoint.setTxnId((String)row.get("TXN_ID"));

			if ("Y".equalsIgnoreCase((String)row.get("IS_CDP_TXN"))){
				datapoint.setTxnId(row.get("TXN_ID") + AppConstantsTrends.CDP_TAG);
			} else {
				datapoint.setTxnId((String)row.get("TXN_ID"));
			}

			datapointMetric = row.get("VALUE_TO_PLOT");

			if (datapointMetric == null) {
				datapoint.setValue(BigDecimal.ZERO); // Default value for null metrics
			} else if (datapointMetric instanceof BigDecimal) {
				datapoint.setValue((BigDecimal)datapointMetric);
			} else if (datapointMetric instanceof Long) {
				datapoint.setValue(new BigDecimal((Long)datapointMetric));
			} else {
				// Handle other numeric types
				datapoint.setValue(new BigDecimal(datapointMetric.toString()));
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
				AppConstantsTrends.getValueDerivationToSourceFieldMap().get(graphMapping.getValueDerivation());

		if ( transactionDBColNameOrDerivationForRequestedValues == null){
			// no mapping - we therefore assume a direct name translation from the derivation entry
			transactionDBColNameOrDerivationForRequestedValues =  graphMapping.getValueDerivation();
		}

		// Validate to prevent SQL injection before using in SQL concatenation
		return validateAndSanitizeTransactionField(transactionDBColNameOrDerivationForRequestedValues);
	}


	/**
	 * Validates and escapes a comma-delimited list of values for safe SQL IN clause usage.
	 * Each value is validated to contain only safe characters and then properly escaped.
	 *
	 * @param commaDelimitedValues comma-separated values
	 * @param fieldName name of the field for error messages
	 * @return SQL-safe quoted and comma-separated values
	 * @throws IllegalArgumentException if any value contains suspicious characters
	 */
	private String validateAndEscapeCommaDelimitedValues(String commaDelimitedValues, String fieldName) {
		if (StringUtils.isBlank(commaDelimitedValues)) {
			return "''";
		}

		String[] values = commaDelimitedValues.split(",");
		List<String> escapedValues = new ArrayList<>();

		for (String value : values) {
			String trimmed = value.trim();

			// Validate: only allow alphanumeric, spaces, underscores, hyphens, colons (for timestamps)
			if (!trimmed.matches("^[a-zA-Z0-9_\\s:\\-]+$")) {
				throw new IllegalArgumentException(fieldName + " contains invalid characters: " + trimmed);
			}

			// Escape and quote each value
			escapedValues.add(UtilsTrends.escapeSqlString(trimmed));
		}

		return String.join("','", escapedValues);
	}

}
