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

package com.mark59.metrics.data.sla.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.Sla;
import com.mark59.metrics.sla.SlaUtilities;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SlaDAOjdbcImpl implements SlaDAO {

	@Autowired
	private DataSource dataSource;

	
	public void insertData(Sla sla) {
		/* transaction name and id the same thing for now.. */
		String sql = "INSERT INTO SLA "
				+ "(TXN_ID, APPLICATION, IS_TXN_IGNORED, SLA_90TH_RESPONSE, SLA_95TH_RESPONSE, SLA_99TH_RESPONSE, "
				+ "SLA_PASS_COUNT, SLA_PASS_COUNT_VARIANCE_PERCENT, SLA_FAIL_COUNT, SLA_FAIL_PERCENT, "
				+ "TXN_DELAY, XTRA_NUM, XTRA_INT, SLA_REF_URL, COMMENT) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		sla = nullsToDefaultValues(sla);

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql,
				new Object[] { sla.getTxnId(), sla.getApplication(), sla.getIsTxnIgnored(), sla.getSla90thResponse(), 
						sla.getSla95thResponse(), sla.getSla99thResponse(),	sla.getSlaPassCount(),sla.getSlaPassCountVariancePercent(),
						sla.getSlaFailCount(), sla.getSlaFailPercent(), 
						sla.getTxnDelay(), sla.getXtraNum(), sla.getXtraInt(), sla.getSlaRefUrl(), sla.getComment()});
	}



	@Override
	public int bulkInsertOrUpdateApplication(String application, Sla slaKeywithDefaultValues, String applyRefUrlOption) { 

		String sql =  "SELECT  DISTINCT TXN_ID, TXN_PASS, RUN_TIME FROM TRANSACTION TX WHERE"
				+ " TX.APPLICATION =  '" + application + "' AND"
				+ " TX.TXN_TYPE =  'TRANSACTION'  and"
				+ " TX.RUN_TIME = ( select max(RUN_TIME) from RUNS where RUNS.APPLICATION =  '" + application + "' AND RUNS.BASELINE_RUN = 'Y' )";

//		System.out.println("SlaDAOjdbcImpl.bulkInsertOrUpdateApplication sql: "  + sql );		
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		String passedSlaRefUrl =  slaKeywithDefaultValues.getSlaRefUrl();
		int rowCount = rows.size();
//		System.out.println("bulkInsertOrUpdateApplication : number of rows to process: "  + rowCount + ", application: " + application );
		
		for (Map<String, Object> row : rows) {
//			System.out.println("bulkInsertOrUpdateApplication: " + slaDefaults.getApplication() + ":" + (String)row.get("TXN_ID") + ":" + (Long)row.get("TXN_PASS") );
			Sla existingSla =  getSla(slaKeywithDefaultValues.getApplication(), (String)row.get("TXN_ID")); 
			
			if (existingSla == null ){  //sla application-transaction does not exist, so add it
				slaKeywithDefaultValues.setTxnId((String)row.get("TXN_ID"));
				slaKeywithDefaultValues.setSlaPassCount((Long)row.get("TXN_PASS"));
				slaKeywithDefaultValues.setSlaRefUrl(passedSlaRefUrl);	
				
				insertData(slaKeywithDefaultValues);
				
			} else {  // update the Pass Count, and reference if requested (removing any previous comment) for an existing transaction
				
				existingSla.setSlaPassCount((Long)row.get("TXN_PASS"));
				
				if ( AppConstantsMetrics.APPLY_TO_ALL_SLAS.equalsIgnoreCase(applyRefUrlOption) && StringUtils.isNotEmpty(passedSlaRefUrl)) {
					existingSla.setSlaRefUrl(passedSlaRefUrl);
				}
				if (existingSla.getSlaPassCount() == null){
					existingSla.setSlaPassCount(new Long(-1));
				}				
				sql = "UPDATE SLA set SLA_PASS_COUNT = ?, SLA_REF_URL = ? where APPLICATION=? and TXN_ID = ?";
				jdbcTemplate = new JdbcTemplate(dataSource);
				jdbcTemplate.update(sql, new Object[] { existingSla.getSlaPassCount(), existingSla.getSlaRefUrl(), existingSla.getApplication(), existingSla.getTxnId() });
			}
		}
		return rowCount;
	}	
		
	

	@Override
	public void deleteAllSlasForApplication(String application) {
		String sql = "delete from SLA where  APPLICATION='" + application + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);

	}
	
	@Override
	public void deleteData(String application, String txnId) {
		String sql = "delete from SLA where  APPLICATION='" + application + "' and TXN_ID='" + txnId + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);

	}

	/*
	 * delete/insert (i.e. rename) if transaction does not exist within the given application,  otherwise update the new values for the passed transaction name
	 */
	@Override
	public void updateData(Sla sla) {

		Sla existingSla =  getSla(sla.getApplication(), sla.getTxnId()); 
//		System.out.println("SlaDAOjdbcImpl.updateData: app=" + sla.getApplication() + ", TxnId=" + sla.getTxnId() + ",  existingSla? = " + existingSla);	
		
		if (existingSla == null ){  //a transaction 'rename'
			deleteData(sla.getApplication(), sla.getSlaOriginalTxnId());
			insertData(sla);
			
		} else {  // update values for an existing transaction
				
			sla = nullsToDefaultValues(sla);

			String sql = "UPDATE SLA set APPLICATION = ?, IS_TXN_IGNORED = ?,SLA_90TH_RESPONSE = ?, SLA_95TH_RESPONSE = ?, SLA_99TH_RESPONSE = ?, "
					+ "SLA_PASS_COUNT = ?, SLA_PASS_COUNT_VARIANCE_PERCENT = ?, SLA_FAIL_COUNT = ?, SLA_FAIL_PERCENT = ?, "
					+ "TXN_DELAY = ?, XTRA_NUM = ?, XTRA_INT = ?, SLA_REF_URL = ?, COMMENT = ? "
					+ "where APPLICATION=? and TXN_ID = ?";
			
			
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.update(sql, 
					new Object[] { sla.getApplication(), sla.getIsTxnIgnored(), sla.getSla90thResponse(),sla.getSla95thResponse(), sla.getSla99thResponse(),
							sla.getSlaPassCount(),sla.getSlaPassCountVariancePercent(),	sla.getSlaFailCount(), sla.getSlaFailPercent(), 
							sla.getTxnDelay(), sla.getXtraNum(), sla.getXtraInt(), sla.getSlaRefUrl(), sla.getComment(),
							sla.getApplication(), sla.getTxnId() });
		}
	}
	

	@Override
	public Sla getSla(String application, String txnId) { //  , String defaultSlaForApplicationKey) {
		List<Sla> slaList = new ArrayList<Sla>();
		String sql = "select * from SLA where APPLICATION='" + application + "' and TXN_ID='" + txnId + "'";
//		System.out.println("getSla sql = " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		slaList = jdbcTemplate.query(sql, new SlaRowMapper());
		
		if (slaList.isEmpty() )
			return null;			
		else
			return slaList.get(0);
	}
	
	
	
	@Override
	public List<Sla> getSlaList(String application) {
		List<Sla> slaList = new ArrayList<Sla>();
		String sql = "select * from SLA where APPLICATION='" + application	+ "' order by TXN_ID"   ;
//		System.out.println("getSla sql = " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		slaList = jdbcTemplate.query(sql, new SlaRowMapper());
		return slaList;
	}
	
	public List<Sla> getSlaList() {
		List<Sla> slaList = new ArrayList<Sla>();
		String sql = "select * from SLA order by APPLICATION, TXN_ID";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		slaList = jdbcTemplate.query(sql, new SlaRowMapper());
		return slaList;
	}


	@Override
	@SuppressWarnings("rawtypes")
	public List<String> findApplications() {
		String sql = "SELECT distinct APPLICATION FROM SLA order by APPLICATION";

		List<String> applications = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map row : rows) {
			applications.add( (String)row.get("APPLICATION") );
//			System.out.println("populating application in dropdown list : " + row.get("APPLICATION")  ) ;
		}	
		return  applications;
	}


	/*
	 *  Reports on transactions which appear on the SLA table, but do not exist in the run.
	 *  Also, at least one SLA must actually be set (a value other than -1 has been set against an SLA).
	 *  Note transactions marked as 'ignored on graphs' are also reported.    
	 */
	@Override
	@SuppressWarnings("rawtypes")	
	public List<String> getSlasWithMissingTxnsInThisRun(String application, String runTime) {
		
		String sql = "SELECT TXN_ID FROM SLA "
					+ " where APPLICATION='" + application	+ "' " 
					+ "  and TXN_ID not in ( "
					+ "     SELECT TXN_ID FROM TRANSACTION"
					+ "     where APPLICATION = '" + application + "' "
					+ "       and RUN_TIME = '" + runTime + "' " 
					+ "       and TXN_TYPE = 'TRANSACTION' ) " 					
					+ "  and TXN_ID != '-" + application + "-DEFAULT-SLA-' "
					+ "  and (not SLA_90TH_RESPONSE < 0.0 or not SLA_95TH_RESPONSE < 0 or not SLA_99TH_RESPONSE < 0 "
					+ "        or not SLA_PASS_COUNT < 0 or not SLA_FAIL_COUNT < 0 or not SLA_FAIL_PERCENT < 0.0 )"
					+ " order by TXN_ID";

//		System.out.println("getSlasWithMissingTxnsInThisRun sql : " + sql  );
		
		List<String> missingTransactions = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map row : rows) {
			missingTransactions.add( (String)row.get("TXN_ID") );
//			System.out.println("getSlasWithMissingTxnsInThisRun : txn  " + (String)row.get("TXN_ID") ) ;
		}	
		return  missingTransactions;
	}



	@Override
	public List<String> getListOfIgnoredTransactionsSQL(String application) {
		String sql = SlaUtilities.listOfIgnoredTransactionsSQL(application); 
		List<String> ignoredTransactions = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> row : rows) {
			ignoredTransactions.add( (String)row.get("TXN_ID") );
//			System.out.println("populating application in dropdown list : " + row.get("APPLICATION")  ) ;
		}	
		return  ignoredTransactions;
	}
	
	/*
	 *   To prevent null exceptions during SLA processing
	 */
	private Sla nullsToDefaultValues(Sla sla) {
		if (sla.getSla90thResponse() == null)
			sla.setSla90thResponse(new BigDecimal(-1.0));
		if (sla.getSla95thResponse() == null)
			sla.setSla95thResponse(new BigDecimal(-1.0));
		if (sla.getSla99thResponse() == null)
			sla.setSla99thResponse(new BigDecimal(-1.0));
		if (sla.getSlaPassCount() == null)
			sla.setSlaPassCount(new Long(-1));
		if (sla.getSlaPassCountVariancePercent() == null)
			sla.setSlaPassCountVariancePercent(new BigDecimal(10.0));
		if (sla.getSlaFailCount() == null)
			sla.setSlaFailCount(new Long(-1));
		if (sla.getSlaFailPercent() == null)
			sla.setSlaFailPercent(new BigDecimal(2.0));
		if (sla.getTxnDelay() == null)
			sla.setTxnDelay(new BigDecimal(0.0));		
		if (sla.getXtraNum() == null)
			sla.setXtraNum(new BigDecimal(0.0));
		if (sla.getXtraInt() == null)
			sla.setXtraInt(new Long(0));		
		return sla;
	}
	
}
