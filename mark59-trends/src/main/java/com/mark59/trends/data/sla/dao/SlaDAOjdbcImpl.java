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

package com.mark59.trends.data.sla.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.trends.application.AppConstantsMetrics;
import com.mark59.trends.data.beans.Sla;
import com.mark59.trends.form.BulkApplicationPassCountsForm;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SlaDAOjdbcImpl implements SlaDAO {

	@Autowired
	private DataSource dataSource;

	
	@Override
	public void insertData(Sla sla) {

		String sql = "INSERT INTO SLA "
				+ "(TXN_ID, IS_CDP_TXN, APPLICATION, IS_TXN_IGNORED, SLA_90TH_RESPONSE, SLA_95TH_RESPONSE, SLA_99TH_RESPONSE, "
				+ "SLA_PASS_COUNT, SLA_PASS_COUNT_VARIANCE_PERCENT, SLA_FAIL_COUNT, SLA_FAIL_PERCENT, "
				+ "TXN_DELAY, XTRA_NUM, XTRA_INT, SLA_REF_URL, COMMENT, IS_ACTIVE) "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		sla = nullsToDefaultValues(sla);

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql,
				sla.getTxnId(), sla.getIsCdpTxn(), sla.getApplication(), sla.getIsTxnIgnored(), sla.getSla90thResponse(),
				sla.getSla95thResponse(), sla.getSla99thResponse(), sla.getSlaPassCount(),sla.getSlaPassCountVariancePercent(),
				sla.getSlaFailCount(), sla.getSlaFailPercent(),
				sla.getTxnDelay(), sla.getXtraNum(), sla.getXtraInt(), sla.getSlaRefUrl(), sla.getComment(), sla.getIsActive());
	}


	@Override
	public int bulkInsertOrUpdateApplication(BulkApplicationPassCountsForm bulkApplication) { 
		String application = bulkApplication.getApplication();
		
		String sql = "SELECT DISTINCT TXN_ID, IS_CDP_TXN, TXN_90TH, TXN_95TH, TXN_99TH, TXN_PASS, RUN_TIME"
				+ " FROM TRANSACTION TX WHERE"
				+ " TX.APPLICATION = :application AND"
				+ " TX.TXN_TYPE =  :txnType AND"
				+ " TX.RUN_TIME = ( select max(RUN_TIME) from RUNS where RUNS.APPLICATION = :application AND RUNS.BASELINE_RUN = 'Y' )";
		
		if ("N".equals(bulkApplication.getIsIncludeCdpTxns())){
			sql = sql + " AND TX.IS_CDP_TXN = 'N' "; 
		}	
			
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("txnType", Mark59Constants.DatabaseTxnTypes.TRANSACTION.name());
		
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = namedJdbcTemplate.queryForList(sql, sqlparameters);
		
		String passedSlaRefUrl =  bulkApplication.getSlaRefUrl();
		int rowCount = rows.size();
		
		for (Map<String, Object> row : rows) {
//			System.out.println("bulkInsertOrUpdateApplication: " + application + ":" + (String)row.get("TXN_ID") + ":" + (Long)row.get("TXN_PASS") );
			Sla existingSla =  getSla(application, (String)row.get("TXN_ID"), (String)row.get("IS_CDP_TXN")); 
			
			if (existingSla == null ){  // sla doesn't exist for this transaction so add it
				
				Sla newSla = new Sla();
				newSla.setApplication(application);
				newSla.setTxnId((String)row.get("TXN_ID"));
				newSla.setIsCdpTxn((String)row.get("IS_CDP_TXN"));
				
				newSla.setIsTxnIgnored(bulkApplication.getIsTxnIgnored());
				
				if (bulkApplication.isSla90thFromBaseline()) {
					newSla.setSla90thResponse((BigDecimal)row.get("TXN_90TH"));
				} else {
					newSla.setSla90thResponse(bulkApplication.getSla90thResponse());
				}
				if (bulkApplication.isSla95thFromBaseline()) {
					newSla.setSla95thResponse((BigDecimal)row.get("TXN_95TH"));
				} else {
					newSla.setSla95thResponse(bulkApplication.getSla95thResponse());
				}
				if (bulkApplication.isSla99thFromBaseline()) {
					newSla.setSla99thResponse((BigDecimal)row.get("TXN_99TH"));
				} else {
					newSla.setSla99thResponse(bulkApplication.getSla99thResponse());
				}
				
				newSla.setSlaPassCount((Long)row.get("TXN_PASS"));
				
				newSla.setSlaPassCountVariancePercent(bulkApplication.getSlaPassCountVariancePercent());
				newSla.setSlaFailCount(bulkApplication.getSlaFailCount());
				newSla.setSlaFailPercent(bulkApplication.getSlaFailPercent());
				newSla.setTxnDelay(bulkApplication.getTxnDelay());
				newSla.setXtraNum(bulkApplication.getXtraNum());
				newSla.setXtraInt(bulkApplication.getXtraInt());
				newSla.setSlaRefUrl(passedSlaRefUrl);	
				newSla.setComment("");
				newSla.setIsActive(bulkApplication.getIsActive());
				
				insertData(newSla);
				
			} else {  // update the Pass Count, and reference if requested (removing any previous comment) for an existing transaction
				
				existingSla.setSlaPassCount((Long)row.get("TXN_PASS"));
				
				if (AppConstantsMetrics.APPLY_TO_ALL_SLAS.equalsIgnoreCase(bulkApplication.getApplyRefUrlOption())
						&& StringUtils.isNotEmpty(passedSlaRefUrl)) {
					existingSla.setSlaRefUrl(passedSlaRefUrl);
				}
				if (existingSla.getSlaPassCount() == null){
					existingSla.setSlaPassCount(-1L);
				}				
				sql = "UPDATE SLA set SLA_PASS_COUNT = ?, SLA_REF_URL = ? where APPLICATION=? and TXN_ID = ? and IS_CDP_TXN = ?";
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
				jdbcTemplate.update(sql, existingSla.getSlaPassCount(), existingSla.getSlaRefUrl(),
						existingSla.getApplication(), existingSla.getTxnId(), existingSla.getIsCdpTxn());
			}
		}
		return rowCount;
	}	
	

	@Override
	public void deleteAllSlasForApplication(String application) {
		
		String sql = "delete from SLA where  APPLICATION = :application ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);		
		
//		System.out.println("deleteAllSlasForApplication : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}
	
	
	@Override
	public void deleteData(String application, String txnId, String isCdpTxn) {
		
		String sql = "delete from SLA where APPLICATION = :application and TXN_ID= :txnId and IS_CDP_TXN= :isCdpTxn ";	
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)		
				.addValue("txnId", txnId)		
				.addValue("isCdpTxn", isCdpTxn);		
		
//		System.out.println(" SlaDao deleteData : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}

	
	/*
	 * delete/insert (i.e. rename) if transaction does not exist within the given application,  otherwise update the new values
	 * for the passed transaction name
	 */
	@Override
	public void updateData(Sla sla) {

		Sla existingSla =  getSla(sla.getApplication(), sla.getTxnId(), sla.getIsCdpTxn()); 
//		System.out.println("SlaDAOjdbcImpl.updateData: app="+sla.getApplication()+", TxnId="+sla.getTxnId()+", existingSla?="+existingSla);	
		
		if (existingSla == null ){  //a transaction 'rename'
			deleteData(sla.getApplication(), sla.getSlaOriginalTxnId(), sla.getIsCdpTxn());
			insertData(sla);
			
		} else {  // update values for an existing transaction

			sla = nullsToDefaultValues(sla);

			String sql = "UPDATE SLA set APPLICATION = ?, IS_TXN_IGNORED = ?,SLA_90TH_RESPONSE = ?, SLA_95TH_RESPONSE = ?, SLA_99TH_RESPONSE = ?, "
					+ "SLA_PASS_COUNT = ?, SLA_PASS_COUNT_VARIANCE_PERCENT = ?, SLA_FAIL_COUNT = ?, SLA_FAIL_PERCENT = ?, "
					+ "TXN_DELAY = ?, XTRA_NUM = ?, XTRA_INT = ?, SLA_REF_URL = ?, COMMENT = ?, IS_ACTIVE = ? "
					+ "where APPLICATION = ? and TXN_ID = ? and IS_CDP_TXN = ?";
			
			
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.update(sql,
					sla.getApplication(), sla.getIsTxnIgnored(), sla.getSla90thResponse(),sla.getSla95thResponse(), sla.getSla99thResponse(),
					sla.getSlaPassCount(),sla.getSlaPassCountVariancePercent(), sla.getSlaFailCount(), sla.getSlaFailPercent(),
					sla.getTxnDelay(), sla.getXtraNum(), sla.getXtraInt(), sla.getSlaRefUrl(), sla.getComment(), sla.getIsActive(),
					sla.getApplication(), sla.getTxnId(), sla.getIsCdpTxn());
		}
	}
	

	@Override
	public Sla getSla(String application, String txnId, String isCdpTxn) { 

		String sql = "select * from SLA where APPLICATION = :application and TXN_ID = :txnId and IS_CDP_TXN = :isCdpTxn ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)		
				.addValue("txnId", txnId)		
				.addValue("isCdpTxn", isCdpTxn);		
		
//		System.out.println(" getSla : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Sla> slaList = jdbcTemplate.query(sql, sqlparameters, new SlaRowMapper());	
		
		if (slaList.isEmpty() )
			return null;			
		else
			return slaList.get(0);
	}
	
	
	@Override
	public List<Sla> getSlaList(String application) {
		
		String sql = "select * from SLA where APPLICATION= :application order by TXN_ID"   ;
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);		
		
//		System.out.println(" getSlaList : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		return jdbcTemplate.query(sql, sqlparameters, new SlaRowMapper());
	}
	
	
	@Override
	public List<Sla> getSlaList() {
		String sql = "select * from SLA order by APPLICATION, TXN_ID";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return jdbcTemplate.query(sql, new SlaRowMapper());
	}


	@Override
	@SuppressWarnings("rawtypes")
	public List<String> findApplications() {
		
		String sql = "SELECT distinct APPLICATION FROM SLA order by APPLICATION";

		List<String> applications = new ArrayList<>();
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
	 *  Transactions marked as 'ignored on graphs' are also reported.    
	 *  Inactive SLA entries are not reported on.   
	 */
	@Override
	@SuppressWarnings("rawtypes")	
	public List<String> getSlasWithMissingTxnsInThisRunCdpTags(String application, String runTime) {
		
		String sql = "SELECT TXN_ID, IS_CDP_TXN FROM SLA S"
					+ " where APPLICATION = :application " 
					+ "  and IS_ACTIVE = 'Y' " 
					+ "  and TXN_ID not in ( "
					+ "     SELECT TXN_ID FROM TRANSACTION T"
					+ "     where APPLICATION = :application "
					+ "       and RUN_TIME = :runTime " 
					+ "       and TXN_TYPE = 'TRANSACTION' " 						
					+ "       and S.IS_CDP_TXN = T.IS_CDP_TXN) " 					
					+ "  and TXN_ID != :applicationDefaultSla "
					+ "  and (not SLA_90TH_RESPONSE < 0.0 or not SLA_95TH_RESPONSE < 0 or not SLA_99TH_RESPONSE < 0 "
					+ "        or not SLA_PASS_COUNT < 0 or not SLA_FAIL_COUNT < 0 or not SLA_FAIL_PERCENT < 0.0 )"
					+ " order by TXN_ID, IS_CDP_TXN";
		
		List<String> cdpTaggedMissingTransactions = new ArrayList<>();
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("runTime", runTime)
				.addValue("applicationDefaultSla", "-" + application + "-DEFAULT-SLA-' ");
		
//		System.out.println(" getSlasWithMissingTxnsInThisRunCdpTags : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);	
		
		for (Map row : rows) {
			String txnId =  (String)row.get("TXN_ID");
			if ("Y".equalsIgnoreCase((String)row.get("IS_CDP_TXN"))){
				cdpTaggedMissingTransactions.add(txnId + AppConstantsMetrics.CDP_TAG);				
			} else {
				cdpTaggedMissingTransactions.add(txnId);
			}
//			System.out.println("getSlasWithMissingTxnsInThisRun : txn:cdp " + (String)row.get("TXN_ID") + ":" + (String)row.get("IS_CDP_TXN"));
		}	
		return  cdpTaggedMissingTransactions;
	}


	@Override
	public List<String> getListOfIgnoredTransactionsAddingCdpTags(String application) {
		
		String sql =  "SELECT TXN_ID, IS_CDP_TXN FROM SLA "
					+ " WHERE APPLICATION = :application AND IS_TXN_IGNORED = 'Y' "
					+ " ORDER BY TXN_ID, IS_CDP_TXN";
		
		List<String> cdpTaggedIgnoredTransactions = new ArrayList<>();
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);
		
//		System.out.println(" getListOfIgnoredTransactionsCdpTags : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);	
		
		for (Map<String, Object> row : rows) {
			String txnId =  (String)row.get("TXN_ID");
			if ("Y".equalsIgnoreCase((String)row.get("IS_CDP_TXN"))){
				cdpTaggedIgnoredTransactions.add(txnId + AppConstantsMetrics.CDP_TAG);				
			} else {
				cdpTaggedIgnoredTransactions.add(txnId);
			}			
		}	
		return  cdpTaggedIgnoredTransactions;
	}
	
	
	@Override
	public List<String> getListOfDisabledSlasAddingCdpTags(String application) {
		
		String sql =  "SELECT TXN_ID, IS_CDP_TXN FROM SLA "
					+ " WHERE APPLICATION = :application AND IS_ACTIVE <> 'Y' "
					+ " ORDER BY TXN_ID, IS_CDP_TXN";
		
		List<String> cdpTaggedDisabledSlas = new ArrayList<>();
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);
		
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);	
		
		for (Map<String, Object> row : rows) {
			String txnId =  (String)row.get("TXN_ID");
			if ("Y".equalsIgnoreCase((String)row.get("IS_CDP_TXN"))){
				cdpTaggedDisabledSlas.add(txnId + AppConstantsMetrics.CDP_TAG);				
			} else {
				cdpTaggedDisabledSlas.add(txnId);
			}			
		}	
		return  cdpTaggedDisabledSlas;
	}
	
	
	/*
	 *   To prevent null exceptions during SLA processing
	 */
	private Sla nullsToDefaultValues(Sla sla) {
		if (sla.getSla90thResponse() == null)
			sla.setSla90thResponse(BigDecimal.valueOf(-1.0));
		if (sla.getSla95thResponse() == null)
			sla.setSla95thResponse(BigDecimal.valueOf(-1.0));
		if (sla.getSla99thResponse() == null)
			sla.setSla99thResponse(BigDecimal.valueOf(-1.0));
		if (sla.getSlaPassCount() == null)
			sla.setSlaPassCount(-1L);
		if (sla.getSlaPassCountVariancePercent() == null)
			sla.setSlaPassCountVariancePercent(new BigDecimal("10.0"));
		if (sla.getSlaFailCount() == null)
			sla.setSlaFailCount(-1L);
		if (sla.getSlaFailPercent() == null)
			sla.setSlaFailPercent(new BigDecimal("2.0"));
		if (sla.getTxnDelay() == null)
			sla.setTxnDelay(new BigDecimal("0.0"));
		if (sla.getXtraNum() == null)
			sla.setXtraNum(new BigDecimal("0.0"));
		if (sla.getXtraInt() == null)
			sla.setXtraInt(0L);		
		if (sla.getIsActive() == null)
			sla.setIsActive("Y");			
		return sla;
	}
	
}
