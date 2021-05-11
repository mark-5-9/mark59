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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.application.UtilsMetrics;
import com.mark59.metrics.data.beans.DateRangeBean;
import com.mark59.metrics.data.beans.EventMapping;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.TestTransaction;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.data.graphMapping.dao.GraphMappingDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class TestTransactionsDAOjdbcTemplateImpl implements TestTransactionsDAO 
{
	
	@Autowired  
	private DataSource dataSource;

    @Autowired
    private String currentDatabaseProfile;
	
	@Autowired
	GraphMappingDAO graphMappingDAO; 	
	
	
	@Override
	public void insert(TestTransaction testTransaction) {
		String sql = "";
		
		if (testTransaction.getTxnId().startsWith( AppConstantsMetrics.JMETER_IGNORED_TXNS )){
//			System.out.println("TestTransactionsDAOjdbcTemplateImpl  : " +  testTransaction.getTxnId() + " has been ignored!" );
		} else {
		
			sql = "INSERT INTO TESTTRANSACTIONS "
					+ "(APPLICATION, RUN_TIME, TXN_ID, TXN_TYPE, "
					+ "TXN_RESULT, TXN_PASSED, TXN_EPOCH_TIME )"
					+ " VALUES ("
					+  "'" + testTransaction.getApplication() + "',"
					+  "'" + testTransaction.getRunTime() + "',"
					+  "'" + testTransaction.getTxnId() + "',"
					+  "'" + testTransaction.getTxnType() + "',"
					       + testTransaction.getTxnResult() + ","
					+  "'" + testTransaction.getTxnPassed() + "',"
					+  "'" + testTransaction.getTxnEpochTime() + "')";

			//System.out.println("TestTransactionsDAOjdbcTemplateImpl insert testtransaction : " + testTransaction.toString() );
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			
			try {
				jdbcTemplate.update(sql);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("   TestTransaction used in SQL Bind Parameter Array : " + testTransaction );
				System.out.println("   sql: " + sql );				
				throw new RuntimeException();
			}
		}
	}
	
		
	@Override
	public void insertMultiple(List<TestTransaction> testTransactionList) {
		
		boolean yetToAddFirstRowToSqlStatement = true;
		StringBuilder sqlSb = new StringBuilder();
		
		sqlSb.append("INSERT INTO TESTTRANSACTIONS "
				+ "(APPLICATION, RUN_TIME, TXN_ID, TXN_TYPE, "
				+ "TXN_RESULT, TXN_PASSED, TXN_EPOCH_TIME )"
				+ " VALUES (?,?,?,?,?,?,?)");
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		ArrayList<Object> sqlBindParms = new ArrayList<Object>();		
		
		for (TestTransaction testTransaction : testTransactionList) {
		
			if (testTransaction.getTxnId().startsWith( AppConstantsMetrics.JMETER_IGNORED_TXNS )){
	//			System.out.println("TestTransactionsDAOjdbcTemplateImpl  : " +  testTransaction.getTxnId() + " has been ignored!" );
			} else {
				sqlBindParms.add(testTransaction.getApplication());
				sqlBindParms.add(testTransaction.getRunTime());
				sqlBindParms.add(testTransaction.getTxnId());
				sqlBindParms.add(testTransaction.getTxnType());
				sqlBindParms.add(testTransaction.getTxnResult());
				sqlBindParms.add(testTransaction.getTxnPassed());
				sqlBindParms.add(testTransaction.getTxnEpochTime());
				
				if (yetToAddFirstRowToSqlStatement) {
					yetToAddFirstRowToSqlStatement = false;
				} else {
					sqlSb.append( ",(?,?,?,?,?,?,?)" );
				}
			}
		} //end for
				
		if (!yetToAddFirstRowToSqlStatement) {
			
			try {
				jdbcTemplate.update(sqlSb.toString() , sqlBindParms.toArray());			

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("   TestTransactions used in SQL Bind Parameter Array : ");
		        for(int i = 0; i < testTransactionList.size(); i++) {
		            System.out.println("  " + (i+1)  + " : " + testTransactionList.get(i));
		        }
				System.out.println("   ------------------------");
				throw new RuntimeException();
			}
			
		}
	}
	
	
	@Override
	public void deleteAllForRun(Run run) {
		String sql = "delete from TESTTRANSACTIONS where APPLICATION='" + run.getApplication() + "' "
												     + "and RUN_TIME='" + run.getRunTime() + "'";
//		System.out.println("performing TestTransactionsDAOjdbcTemplateImpl.deleteAllForRun : " + sql );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}
		
	
	@Override
	public void deleteAllForApplication(String application) {
		String sql = "delete from TESTTRANSACTIONS where APPLICATION='" + application + "'";
//		System.out.println("TestTransactionsDAOjdbcTemplateImpl.deleteAllForApplication : " + sql );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

		

	@Override
	public void updateRunTime(String application, String originalRunTime, String newRunTime) {
		String sql = "update TESTTRANSACTIONS "
					+ " set RUN_TIME='" + newRunTime + "'"  
					+ " where RUN_TIME='" + originalRunTime + "'"  
					+ "   and APPLICATION='" + application + "'";
//		System.out.println("TestTransactionsDAOjdbcTemplateImpl.updateRunTime : " + sql );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}
	
	
	@Override
	public List<TestTransaction> getUniqueListOfSystemMetricNamesByType(String application) {
		List<TestTransaction> dataSampleTxnkeys  = new ArrayList<TestTransaction>();
		
		String sql = "select distinct TXN_ID, TXN_TYPE  from TESTTRANSACTIONS "
					+ "where APPLICATION = '"  + application + "' "
					+ "  and TXN_TYPE <> 'TRANSACTION' "
					+   "and RUN_TIME = '" + AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED + "' ";					

//		System.out.println("TestTransactionsDAOjdbcTemplateImpl.getUniqueListOfSystemMetricNamesByType : " + sql );				
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> row : rows) {
			TestTransaction dataSampleKey = new TestTransaction();
			dataSampleKey.setApplication(application); 
			dataSampleKey.setTxnId((String)row.get("TXN_ID")); 
			dataSampleKey.setTxnType((String)row.get("TXN_TYPE")); 
			dataSampleTxnkeys.add(dataSampleKey);
//			System.out.println("populating for TestTransactionsDAOjdbcTemplateImpl.getUniqueListOfSystemMetricNamesByType : " + row.get("TXN_ID") + ":" + row.get("TXN_TYPE") ) ;
		}	
		return dataSampleTxnkeys;
	}	
	

	@Override
	public Long getEarliestTimestamp(String applicationn) {
		String sql = "select min(TXN_EPOCH_TIME) from TESTTRANSACTIONS "
				+ " where APPLICATION = '" + applicationn + "' "
				+ "  and     RUN_TIME = '" + AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED + "' ";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	    String earliestTimestamp = (String) jdbcTemplate.queryForObject(sql, String.class);
	    
	    if ( ! StringUtils.isNumeric(earliestTimestamp)) {
	    	throw new RuntimeException(" A valid date range for the test was not found (possibly no transactions in output dataset?)."
	    			+ "  Aborting run." + "(found run start time of " + earliestTimestamp + ")" );
	    }
	    return Long.valueOf(earliestTimestamp);
	}
	
	@Override
	public Long getLatestTimestamp(String applicationn) {
		String sql = "select max(TXN_EPOCH_TIME) from TESTTRANSACTIONS "
				+ " where APPLICATION = '" + applicationn + "' "
				+ "  and     RUN_TIME = '" + AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED + "' ";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	    String latestTimestamp = (String) jdbcTemplate.queryForObject(sql, String.class);

	    if ( ! StringUtils.isNumeric(latestTimestamp)) {
	    	throw new RuntimeException(" A valid date range for the test was not found (possibly no transactions in output dataset?)."
	    			+ "  Aborting run." + "(found run end time of " + latestTimestamp + ")" );
	    }
	    return Long.valueOf (latestTimestamp);
	}

	/* 
	 * Extracts all data for an application data type (transaction or data sample).
	 * 
	 * NOTE: When this method is called currently assumed the run being processed will have a  
	 * run-time of AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED (zeros) on TESTTRANSACTIONS 
	 */
	@Override
	public List<Transaction> extractTransactionResponsesSummary(String application, String txnType) {
		// for MySQL : http://rpbouman.blogspot.com/2008/07/calculating-nth-percentile-in-mysql.html
		//   - BUT "90/100 * COUNT(*) + 0.5" INSTEAD OF "90/100 * COUNT(*) + 1" to match Loadrunner results (which seems to be correct)
		//     may have something to do with the way integer rounding is implemented in MySql   
		// https://stackoverflow.com/questions/2567000/mysql-and-group-concat-maximum-length
		// https://dev.mysql.com/doc/refman/8.0/en/server-system-variables.html#sysvar_group_concat_max_len
		// Note that transaction statistics are only calculated for Passed transactions
		
		List<Transaction> transactions = new ArrayList<Transaction> ();     
		long startLoadms = System.currentTimeMillis(); 
		System.out.println("Collation of test transactional data starts at " + new Date(startLoadms));
		
		String dbDependentStdDevAnd90th = null;
		String dbDependent95th = null;
		String dbDependent99th = null;
		if (Mark59Constants.MYSQL.equals(currentDatabaseProfile)){ 
			dbDependentStdDevAnd90th = " std(TXN_RESULT) txnStdDeviation, " +
					          " SUBSTRING_INDEX(SUBSTRING_INDEX( GROUP_CONCAT(TXN_RESULT ORDER BY TXN_RESULT SEPARATOR ','), ',', CEILING(90/100 * COUNT(*) ) ), ',', -1) as txn90th, " ;
			dbDependent95th = " SUBSTRING_INDEX(SUBSTRING_INDEX( GROUP_CONCAT(TXN_RESULT ORDER BY TXN_RESULT SEPARATOR ','), ',', CEILING(95/100 * COUNT(*) ) ), ',', -1) as txn95th, " ;
			dbDependent99th = " SUBSTRING_INDEX(SUBSTRING_INDEX( GROUP_CONCAT(TXN_RESULT ORDER BY TXN_RESULT SEPARATOR ','), ',', CEILING(99/100 * COUNT(*) ) ), ',', -1) as txn99th, " ;			
		} else {
			dbDependentStdDevAnd90th = 	" STDDEV_POP (TXN_RESULT) txnStdDeviation,"
				              + " PERCENTILE_DISC(0.90) WITHIN GROUP (ORDER BY TXN_RESULT ) as txn90th, " ;
			dbDependent95th = 	" PERCENTILE_DISC(0.95) WITHIN GROUP (ORDER BY TXN_RESULT ) as txn95th, " ;
			dbDependent99th = 	" PERCENTILE_DISC(0.99) WITHIN GROUP (ORDER BY TXN_RESULT ) as txn99th, " ;
		}
		
		String sql = 
				"select APPLICATION, RUN_TIME,TXN_ID, " + 
				"       sum(txnMinimum) txnMinimum, " + 
				"       sum(txnAverage) txnAverage, " + 
				"       sum(txnMaximum) txnMaximum, " + 
				"       sum(txnStdDeviation) txnStdDeviation, " + 
				"       sum(txn90th) txn90th, " + 
				"       sum(txn95th) txn95th, " + 
				"       sum(txn99th) txn99th, " + 
				"       sum(txnPass) txnPass," + 
				"       sum(txnFail) txnFail, " + 
				"       sum(txnStop) txnStop "+ 
				"from ( " +
					"select APPLICATION, RUN_TIME,TXN_ID, " +
					 " min(TXN_RESULT) txnMinimum, " +
					 " avg(TXN_RESULT) txnAverage, " +
					 " max(TXN_RESULT) txnMaximum, " +
					 dbDependentStdDevAnd90th +
					 dbDependent95th +
					 dbDependent99th +
					 " sum(case when TXN_PASSED = 'Y'  then 1 else 0 end) txnPass, " +
					 " 0 txnFail, " +
					 " 0 txnStop " +				
					 " from TESTTRANSACTIONS " +					 
					 " where APPLICATION = '" + application + "' " +
					 "   and RUN_TIME = '" + AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED + "' " +
			   		 "   and TXN_TYPE = '" + txnType + "' " +
			   		 "   and TXN_PASSED = 'Y' " + 		   		
			   		 " group by APPLICATION, RUN_TIME,TXN_ID" +
			   		 
			   		 " union " + 
			   		 
			   		"select APPLICATION, RUN_TIME,TXN_ID, " + 
			   		 " 0 txnMinimum, " + 
			   		 " 0 txnAverage, " + 
			   		 " 0 txnMaximum, " + 
			   		 " 0 txnStdDeviation, " + 
			   		 " 0 txn90th, " + 
			   		 " 0 txn95th, " + 
			   		 " 0 txn99th, " + 
			   		 " 0 txnPass, " + 
					 " sum(case when TXN_PASSED = 'N'  then 1 else 0 end) txnFail, " +
					 " sum(case when TXN_PASSED = '" + AppConstantsMetrics.TXN_STOPPPED_STATUS +  "' then 1 else 0 end) txnStop " +	
			   		 " from TESTTRANSACTIONS " + 
					 " where APPLICATION = '" + application + "' " +
					 "   and RUN_TIME = '" + AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED + "' " +			   		 
			   		 "   and TXN_TYPE = '" + txnType + "' " +
			   		 "   and TXN_PASSED != 'Y' " + 		   		
			   		 " group by APPLICATION, RUN_TIME,TXN_ID" +
			   		 
			   	") as txnSummary " + 
			   	"  group by APPLICATION, RUN_TIME, TXN_ID "	 ;
		
//		System.out.println("*** extractTransactionResponsesSummary sql : " + sql);
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		if (Mark59Constants.MYSQL.equals(currentDatabaseProfile)){
			jdbcTemplate.update("SET GLOBAL group_concat_max_len = 18446744073709551615");		
		}
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		for (Map<String, Object> row : rows) {
			Transaction transaction = new Transaction();
			transaction.setApplication(application);
			transaction.setRunTime((String)row.get("RUN_TIME"));
			transaction.setTxnId((String)row.get("TXN_ID"));
			transaction.setTxnType(Mark59Constants.DatabaseTxnTypes.TRANSACTION.name());			
			transaction.setTxnMinimum((BigDecimal)row.get("txnMinimum"));
			transaction.setTxnAverage((BigDecimal)row.get("txnAverage"));
			transaction.setTxnMaximum((BigDecimal)row.get("txnMaximum"));
			
			if (Mark59Constants.PG.equals(currentDatabaseProfile)){
				transaction.setTxnStdDeviation((BigDecimal)row.get("txnStdDeviation"));				
			} else {
				transaction.setTxnStdDeviation(new BigDecimal((Double)row.get("txnStdDeviation")));
			}

			if (Mark59Constants.MYSQL.equals(currentDatabaseProfile)){ 
				transaction.setTxn90th(new BigDecimal((Double)row.get("txn90th")));
				transaction.setTxn95th(new BigDecimal((Double)row.get("txn95th")));
				transaction.setTxn99th(new BigDecimal((Double)row.get("txn99th")));
			} else {
				transaction.setTxn90th((BigDecimal)row.get("txn90th"));
				transaction.setTxn95th((BigDecimal)row.get("txn95th"));
				transaction.setTxn99th((BigDecimal)row.get("txn99th"));
			}
			
			transaction.setTxnPass(Long.valueOf( ((BigDecimal)row.get("txnPass")).longValue() ));
			transaction.setTxnFail(Long.valueOf( ((BigDecimal)row.get("txnFail")).longValue() ));
			transaction.setTxnStop(Long.valueOf( ((BigDecimal)row.get("txnStop")).longValue() ));		
      		transaction.setTxnFirst(new BigDecimal(-1.0));
      		transaction.setTxnLast(new BigDecimal(-1.0));
      		transaction.setTxnSum(new BigDecimal(-1.0));
      		transactions.add(transaction);
//			System.out.println("populating extractTransactionStats: " + row.get("TXN_ID")  ) ;
		}
		
		long endLoadms = System.currentTimeMillis(); 
		System.out.println("Collation of test transactional data completed " + new Date(endLoadms) +  ". Took " + (endLoadms -startLoadms)/1000 + " secs\n" );		
		return transactions;
	}



	@Override
	public Transaction extractEventSummaryStats(String application, String txnType, String dataSampleLablel, EventMapping eventMapping) {
		Run run = new Run();
		run.setApplication(application);
		run.setRunTime(AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED );
		return extractEventSummaryStats(run, txnType, dataSampleLablel, eventMapping);
	}	
	

	/*
	 * used to finds and computes 'metric' transactions for the run being processed.
	 * At the point this method is all called it is assumed the run being processed will have  
	 * run-time of AppConstantsMetrics.RUN_TIME_YET_TO_BE_CALCULATED (zeros) on TESTTRANSACTIONS 
	 */
	private Transaction extractEventSummaryStats(Run run, String txnType, String dataSampleLablel, EventMapping eventMapping) {
		
//		System.out.println("extractEventSummaryStats txnType: " + txnType + "\n  lablel:" + dataSampleLablel + "\n  eventMapping: " + eventMapping  );
		
		String sql = "select TXN_RESULT from TESTTRANSACTIONS "
				+ " where APPLICATION = '" + run.getApplication() + "' "
				+ "   and RUN_TIME = '" + run.getRunTime() + "'"
				+ "   and TXN_ID = '" + dataSampleLablel + "'"		   		
		   		+ "   and TXN_TYPE = '" + txnType + "' "
		   		+ " order by TXN_EPOCH_TIME ";
		   				
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		
		BigDecimal value = new BigDecimal(0.000000).setScale(6, RoundingMode.DOWN);
		BigDecimal totalOfValues = new BigDecimal(0.000000).setScale(6, RoundingMode.HALF_UP );
		
		Long countPointsAtBottleneckThreshold = Long.valueOf(0);
		Long count = Long.valueOf(0);	
		
		BigDecimal txnMinimum =  new BigDecimal(-1.0).setScale(3, RoundingMode.HALF_UP);
		BigDecimal txnMaximum =  new BigDecimal(-1.0).setScale(3, RoundingMode.HALF_UP);				

		BigDecimal txnFirst =  new BigDecimal(-1.0).setScale(3, RoundingMode.HALF_UP);		
		BigDecimal txnLast  =  new BigDecimal(-1.0).setScale(3, RoundingMode.HALF_UP);		
		boolean firstTimeThru = true;	
		
		BigDecimal ninteyPercent = new BigDecimal(90.0);
		
		
		for (Map<String, Object> row : rows) {
			value =  (BigDecimal)row.get("TXN_RESULT");  //.setScale(3, RoundingMode.HALF_UP) ;
			
			if (firstTimeThru) {
				txnMinimum = value;
				txnMaximum = value;
				txnFirst = value;
				txnLast = value;
				firstTimeThru = false;
			}
	
			txnLast = value;

			if ( value.compareTo(txnMinimum) < 0  ){			
				txnMinimum = value.setScale(3, RoundingMode.HALF_UP);
			}
			if ( value.compareTo(txnMinimum) > 0  ){				
				txnMaximum = value.setScale(3, RoundingMode.HALF_UP);
			}			
			
			// if the metric value is a %idle, we need to invert it to turn it into a %utilisation ..
			if (eventMapping.getIsInvertedPercentage().equals("Y") ){
				value = value.subtract( new BigDecimal(100.0)).negate();
			}
			
			//calculation of server utilisation average takes the total of all points captured, then divides that by number of points (also may be useful for certain Datapoints) 
			totalOfValues =  totalOfValues.add(value);

			//calculation of time spent over 90% of metric (or <10% inverted percentage like idle).  Takes the number of points at >90%  and uses total number of point to get % 'bottlenecked'
			if (eventMapping.getIsPercentage().equals("Y") &&  value.compareTo(ninteyPercent) > 0 ){
				countPointsAtBottleneckThreshold = countPointsAtBottleneckThreshold + 1;
			}
	
			count = count +1;
//			System.out.println("value = " + value + ",  count = " + count  );			
			
		}
		
		//form the transaction id for the system metric by removing from the JMeter file label any unwanted characters using the event mapping left and right boundaries.
		
		String eventTxnId = UtilsMetrics.deriveEventTxnIdUsingEventMappingBoundaryRules(dataSampleLablel, eventMapping);
		
				
		//average
		BigDecimal tnxAverage =   totalOfValues.divide(new BigDecimal(count), 3, RoundingMode.HALF_UP  );
		
		DecimalFormat df = new DecimalFormat("#.00");
		
		//time above 90% threshold
		Double percentSpendAtBottleneckThreshold = ( countPointsAtBottleneckThreshold / count.doubleValue()  ) * 100.0;
		String percentSpendAtBottleneckThresholdStr =   df.format(percentSpendAtBottleneckThreshold);
				
//		System.out.println("extractTransactionMetricsForServerStats: " + eventTxnId + " " + eventMapping.getTxnType()  
//				+ "  eventAve = " + tnxAverage + " util% = " + tnxAverage +	", 90th% threshold = " + percentSpendAtBottleneckThresholdStr + ", count was " + count );	
		
		Transaction metricTransaction = new Transaction();
		metricTransaction.setApplication(run.getApplication());
		metricTransaction.setRunTime(run.getRunTime()); 
		metricTransaction.setTxnId(eventTxnId); 
		metricTransaction.setTxnType(eventMapping.getTxnType());   //DATAPOINT, CPU_UTIL, MEMORY .. 
		metricTransaction.setTxnAverage(tnxAverage); 
		metricTransaction.setTxnMinimum(txnMinimum); 		      		
		metricTransaction.setTxnMaximum(txnMaximum);
		metricTransaction.setTxnStdDeviation(new BigDecimal(-1.0));

		metricTransaction.setTxn90th(new BigDecimal(-1.0));
		if (eventMapping.getIsPercentage().equals("Y")){
			metricTransaction.setTxn90th(new BigDecimal(percentSpendAtBottleneckThresholdStr));
		}
		metricTransaction.setTxn95th(new BigDecimal(-1.0));
		metricTransaction.setTxn99th(new BigDecimal(-1.0));
		metricTransaction.setTxnPass(count);
		metricTransaction.setTxnFail(Long.valueOf(-1).longValue() );
		metricTransaction.setTxnStop(Long.valueOf(-1).longValue() );
		
		metricTransaction.setTxnFirst(txnFirst);
		metricTransaction.setTxnLast(txnLast);
		metricTransaction.setTxnSum(totalOfValues);		

		return metricTransaction;
	}


	@Override
	public int filterByTime(Run run, DateRangeBean filteredDateRangeBean) {
		
		String sql = "delete from TESTTRANSACTIONS where APPLICATION = '" + run.getApplication() + "' "
									+ "  and TXN_EPOCH_TIME not between " + filteredDateRangeBean.getRunStartTime() 
															   + "  and " + filteredDateRangeBean.getRunEndTime();

//		System.out.println("performing TestTransactionsDAOjdbcTemplateImpl.filterByTime : " + sql );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		int rowsAffected = 	jdbcTemplate.update(sql);
		return rowsAffected;
	}


}
