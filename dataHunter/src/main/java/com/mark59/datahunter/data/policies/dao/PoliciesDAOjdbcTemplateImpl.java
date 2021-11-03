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

package com.mark59.datahunter.data.policies.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerResult;
import com.mark59.datahunter.model.CountPoliciesBreakdown;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class PoliciesDAOjdbcTemplateImpl implements PoliciesDAO 
{
	
	@Autowired  
	private DataSource dataSource;


	@Override	
	public SqlWithParms constructSelectPolicySql(PolicySelectionCriteria policySelect){
	
		String sql = "SELECT " + policySelect.getSelectClause() + " FROM POLICIES "
				+ "WHERE APPLICATION = :application "
				+ "  AND IDENTIFIER = :identifier "
				+ "  AND LIFECYCLE = :lifecycle ";

		if (DataHunterUtils.isEmpty(policySelect.getLifecycle())) {
			policySelect.setLifecycle(""); 
		}   	
				
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", policySelect.getApplication())
				.addValue("identifier", policySelect.getIdentifier())
				.addValue("lifecycle", policySelect.getLifecycle());  // not always used
	
		return new SqlWithParms(sql,sqlparameters);
	}
	

	@Override	
	public SqlWithParms constructSelectPoliciesSql(PolicySelectionCriteria policySelect){
		
		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelect);
		
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters()
				.addValue("application", policySelect.getApplication());

		String sql = "SELECT " + policySelect.getSelectClause() + " FROM POLICIES "
				+ "WHERE APPLICATION = :application "
				+ sqlWithParms.getSql();

		if (DataHunterUtils.isEmpty(policySelect.getSelectOrder())) {   							//default ordering: most recently created firstt 
			sql += " ORDER BY CREATED DESC ";
		} else if (DataHunterConstants.SELECT_UNORDERED.equals(policySelect.getSelectOrder())){ 	//eg when just selecting count(*)      
			sql += "";			
		} else if (DataHunterConstants.SELECT_MOST_RECENTLY_ADDED.equals(policySelect.getSelectOrder())){
			sql += " ORDER BY CREATED DESC, EPOCHTIME DESC, IDENTIFIER DESC LIMIT 1 ";				//with Epoch time and Id as a tie-breakers
		} else if (DataHunterConstants.SELECT_OLDEST_ENTRY.equals(policySelect.getSelectOrder())){
			sql += " ORDER BY CREATED ASC, EPOCHTIME ASC, IDENTIFIER ASC LIMIT 1 ";					//with Epoch time and Id as a tie-breakers
		} if (DataHunterConstants.SELECT_RANDOM_ENTRY.equals(policySelect.getSelectOrder())){
			sql += " ORDER BY RAND() LIMIT 1 ";	
		}
	
		return new SqlWithParms(sql,sqlparameters);
	}

	
	@Override	
	public SqlWithParms constructCountPoliciesBreakdownSql(PolicySelectionCriteria policySelect){

		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelect);
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters();

		String sql = "SELECT DISTINCT APPLICATION, LIFECYCLE, USEABILITY, COUNT(*) AS ROWCOUNT FROM POLICIES WHERE " ;
				
		if ( DataHunterConstants.STARTS_WITH.equals(policySelect.getApplicationStartsWithOrEquals()) ){
			sqlparameters.addValue("applicationLike", policySelect.getApplication() + "%"  );
			sql += " APPLICATION LIKE :applicationLike ";
		} else {
			sqlparameters.addValue("application", policySelect.getApplication());
			sql += " APPLICATION = :application ";
		}
		sql += sqlWithParms.getSql();
		sql += " GROUP BY APPLICATION, LIFECYCLE, USEABILITY";

		return new SqlWithParms(sql,sqlparameters);		
	}

		
	@Override
	public SqlWithParms constructAsyncMessageaAnalyzerSql(PolicySelectionCriteria policySelect) {

		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelect);
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters();		
		
		String sql = "SELECT APPLICATION, IDENTIFIER, USEABILITY,  "
				+ "MIN(EPOCHTIME) AS STARTTM, "
				+ "MAX(EPOCHTIME) AS ENDTM, "
				+ "MAX(EPOCHTIME) - MIN(EPOCHTIME) AS DIFFERENCETM "
				+ "FROM POLICIES WHERE ";
		
		if ( DataHunterConstants.STARTS_WITH.equals(policySelect.getApplicationStartsWithOrEquals()) ){
			sqlparameters.addValue("applicationLike", policySelect.getApplication() + "%"  );
			sql += " APPLICATION LIKE :applicationLike ";
		} else {
			sqlparameters.addValue("application", policySelect.getApplication());
			sql += " APPLICATION = :application ";
		}

		if ( ! DataHunterUtils.isEmpty(policySelect.getIdentifier())){  
			sqlparameters.addValue("identifier", policySelect.getIdentifier());
			sql += " AND IDENTIFIER = :identifier ";
		} 
		if ( ! DataHunterUtils.isEmpty(policySelect.getUseability())){  
			sqlparameters.addValue("useability", policySelect.getUseability());
			sql += " AND USEABILITY = :useability ";
		} 		
		sql += " GROUP BY APPLICATION, IDENTIFIER, USEABILITY HAVING COUNT(*) > 1 ORDER BY APPLICATION DESC, IDENTIFIER DESC" ;
		
		return new SqlWithParms(sql,sqlparameters);		
	}
	
	

	
	@Override	
	public SqlWithParms constructDeleteMultiplePoliciesSql(PolicySelectionCriteria policySelect){
		
		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelect);
				
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters()
				.addValue("application", policySelect.getApplication());

		String sql = "DELETE FROM POLICIES WHERE APPLICATION = :application "
				+ sqlWithParms.getSql();
		
		return new SqlWithParms(sql,sqlparameters);
	}

	private SqlWithParms lifecycleAndUseabiltySelector(PolicySelectionCriteria policySelect) {
		String sql = "";
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource();

		if ( DataHunterUtils.isEmpty(policySelect.getLifecycle()) && DataHunterUtils.isEmpty(policySelect.getUseability()) ){  
			// do nothing, sql done
		} else if (DataHunterUtils.isEmpty(policySelect.getUseability())) { 	// only lifecycle has a value
			sqlparameters.addValue("lifecycle", policySelect.getLifecycle());
			sql += " AND LIFECYCLE = :lifecycle ";	
		} else if (DataHunterUtils.isEmpty(policySelect.getLifecycle())) {   	// only usability has a value
			sqlparameters.addValue("useability", policySelect.getUseability());
			sql += " AND USEABILITY = :useability ";
		} else {																// both have a value se
			sqlparameters.addValue("lifecycle", policySelect.getLifecycle()); 
			sqlparameters.addValue("useability", policySelect.getUseability());			
			sql += " AND LIFECYCLE = :lifecycle ";
			sql += " AND USEABILITY = :useability ";
		}
		return new SqlWithParms(sql,sqlparameters);
	}

	
	@Override
	public List<CountPoliciesBreakdown> runCountPoliciesBreakdownSql(SqlWithParms sqlWithParms) {

		List<CountPoliciesBreakdown> countPoliciesBreakdownList = new ArrayList<CountPoliciesBreakdown>();
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlWithParms.getSql(), sqlWithParms.getSqlparameters());
		
		for (Map<String, Object> row : rows) {
			CountPoliciesBreakdown countPoliciesBreakdown = populateCountPoliciesBreakdownFromResultSet(row);
			countPoliciesBreakdownList.add(countPoliciesBreakdown);
		}	
		return countPoliciesBreakdownList;
	}

	
	@Override
	public List<AsyncMessageaAnalyzerResult> runAsyncMessageaAnalyzerSql(SqlWithParms sqlWithParms) {

		List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList = new ArrayList<AsyncMessageaAnalyzerResult>();
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlWithParms.getSql(), sqlWithParms.getSqlparameters());
		
		for (Map<String, Object> row : rows) {
			AsyncMessageaAnalyzerResult asyncMessageaAnalyzerResult = populateAsyncMessageaAnalyzerResultFromSqlResultRow(row);
			asyncMessageaAnalyzerResultList.add(asyncMessageaAnalyzerResult);
		}	
		return asyncMessageaAnalyzerResultList;
	}

	
	@Override	
	public int runCountSql(SqlWithParms sqlWithParms){
		Integer rowCount  = 0;
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		rowCount = Integer.valueOf(jdbcTemplate.queryForObject(sqlWithParms.getSql(), sqlWithParms.getSqlparameters(), String.class));
		return rowCount;
	};	
	

	@Override
	public List<Policies> runSelectPolicieSql(SqlWithParms sqlWithParms) {

		List<Policies> policiesList = new ArrayList<Policies>();
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlWithParms.getSql(), sqlWithParms.getSqlparameters());		
	
		for (Map<String, Object> row : rows) {
			Policies policies = populatePoliciesFromResultSet(row);
			policiesList.add(policies);
		}	
		return policiesList;
	}
	
				
	@Override
	public SqlWithParms constructInsertDataSql(Policies policies) {
		
		String sql = "INSERT INTO POLICIES "
				+ "( APPLICATION,  IDENTIFIER,  LIFECYCLE,  USEABILITY,  OTHERDATA, CREATED, UPDATED, EPOCHTIME) VALUES "
				+ "(:application, :identifier, :lifecycle, :useability, :otherdata, NOW(), CURRENT_TIMESTAMP, :epochtime) ";
				
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", policies.getApplication())
				.addValue("identifier",  policies.getIdentifier())
				.addValue("lifecycle",   policies.getLifecycle())
				.addValue("useability",  policies.getUseability())
				.addValue("otherdata",   policies.getOtherdata())
				.addValue("epochtime",   policies.getEpochtime());
		
		return new SqlWithParms(sql,sqlparameters);
	}

	
	@Override
	public SqlWithParms constructDeletePoliciesSql(PolicySelectionCriteria policySelectionCriteria) {

		String sql = "DELETE FROM POLICIES WHERE APPLICATION = :application AND IDENTIFIER = :identifier ";
				
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", policySelectionCriteria.getApplication())
				.addValue("identifier",  policySelectionCriteria.getIdentifier());
		
		return new SqlWithParms(sql,sqlparameters);		
	}	

	
	@Override
	public SqlWithParms constructUpdatePolicyToUsedSql(Policies nextPolicy) {
		
		UpdateUseStateAndEpochTime updateUse = new UpdateUseStateAndEpochTime();
		updateUse.setApplication(nextPolicy.getApplication());
		updateUse.setIdentifier(nextPolicy.getIdentifier() );
		updateUse.setLifecycle(nextPolicy.getLifecycle() );			
		updateUse.setUseability(nextPolicy.getUseability());
		updateUse.setToUseability(DataHunterConstants.USED);
		updateUse.setToEpochTime(nextPolicy.getEpochtime());

		SqlWithParms sqlWithParms = constructUpdatePoliciesUseStateSql(updateUse);
		return sqlWithParms;
	}

	
	@Override
	public SqlWithParms constructUpdatePoliciesUseStateSql(UpdateUseStateAndEpochTime updateUse) {
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("toUseability", updateUse.getToUseability())
				.addValue("application", updateUse.getApplication());
				
		String sql = "UPDATE POLICIES SET USEABILITY = :toUseability, UPDATED = CURRENT_TIMESTAMP "; 		

		if (updateUse.getToEpochTime() !=  null ){
			sqlparameters.addValue("epochTime", updateUse.getToEpochTime());
			sql += ", EPOCHTIME = :epochTime "; 
		}
		sql += " WHERE APPLICATION = :application ";

		if (! DataHunterUtils.isEmpty(updateUse.getLifecycle())) {   
			sqlparameters.addValue("lifecycle", updateUse.getLifecycle());
			sql += " AND LIFECYCLE = :lifecycle ";
		}
		if (! DataHunterUtils.isEmpty(updateUse.getIdentifier())) {  
			sqlparameters.addValue("identifier", updateUse.getIdentifier());
			sql += " AND IDENTIFIER = :identifier ";
		}
		if (! DataHunterUtils.isEmpty(updateUse.getUseability())) {   
			sqlparameters.addValue("useability", updateUse.getUseability());
			sql += " AND USEABILITY = :useability ";
		}

		return new SqlWithParms(sql,sqlparameters);
	}
	
	
	
	@Override
	public int runDatabaseUpdateSql(SqlWithParms sqlWithParms) {
//		System.out.println(" runDatabaseUpdateSql : " + sqlWithParms.getSql() + sqlWithParms.getSqlparameters());		
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		int rowsAffected = jdbcTemplate.update(sqlWithParms.getSql(), sqlWithParms.getSqlparameters());		
//		System.out.println("PoliciesDAO runDatabaseUpdateSql rowsAffected = " + rowsAffected    );
		return rowsAffected;		
	}
		

	

	private Policies populatePoliciesFromResultSet(Map<String, Object> row) {
		Policies policies = new Policies();
		policies.setApplication((String)row.get("APPLICATION"));
		policies.setIdentifier((String)row.get("IDENTIFIER"));
		policies.setLifecycle((String)row.get("LIFECYCLE"));
		policies.setUseability((String)row.get("USEABILITY"));
		policies.setOtherdata((String)row.get("OTHERDATA"));
		policies.setCreated((Timestamp)row.get("CREATED"));		
		policies.setUpdated((Timestamp)row.get("UPDATED"));			
		policies.setEpochtime((Long)row.get("EPOCHTIME"));		
		return policies;
	}
	
	/**
	 * Note values are returned with spaces converted to underscores (_).  This is because on the 
	 * CountPoliciesBreakdown results page, the values are used to form HTML ids on the page.   
	 */
	private CountPoliciesBreakdown populateCountPoliciesBreakdownFromResultSet(Map<String, Object> row) {
		CountPoliciesBreakdown policiesSelectedCountsBreakdown = new CountPoliciesBreakdown();
		policiesSelectedCountsBreakdown.setApplication(((String)row.get("APPLICATION")).replace(" ", "_"));
		policiesSelectedCountsBreakdown.setLifecycle(((String)row.get("LIFECYCLE")).replace(" ", "_"));
		policiesSelectedCountsBreakdown.setUseability(((String)row.get("USEABILITY")).replace(" ", "_"));
		policiesSelectedCountsBreakdown.setRowCount((Long)row.get("ROWCOUNT"));
		return policiesSelectedCountsBreakdown;
	}

	private AsyncMessageaAnalyzerResult populateAsyncMessageaAnalyzerResultFromSqlResultRow(Map<String, Object> row) {
		AsyncMessageaAnalyzerResult asyncMessageaAnalyzerResult = new AsyncMessageaAnalyzerResult();
		asyncMessageaAnalyzerResult.setApplication((String)row.get("APPLICATION"));
		asyncMessageaAnalyzerResult.setIdentifier((String)row.get("IDENTIFIER"));
		asyncMessageaAnalyzerResult.setUseability((String)row.get("USEABILITY"));
		asyncMessageaAnalyzerResult.setStarttm((Long)row.get("STARTTM"));
		asyncMessageaAnalyzerResult.setEndtm((Long)row.get("ENDTM"));	
		asyncMessageaAnalyzerResult.setDifferencetm((Long)row.get("DIFFERENCETM"));			
		return asyncMessageaAnalyzerResult;
	}


	@Override
	public void getLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString, int timeout) {
		
		String sql = "SELECT GET_LOCK('" +  lockResouceString + "', " + timeout + ")";
		
		Integer wasLockObtained = singleConnectionJdbcTemplate.queryForObject(sql, Integer.class);	
		if (1 != wasLockObtained ){
			throw new RuntimeException("Failed to set lock for query " + sql + " [" + wasLockObtained + "]");
		}
//		System.out.println("sql lock : " + sql + "(" + lockResult + ")"  ); //String lockResult = ...
	}

	@Override
	public void releaseLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString) throws SQLException {
		String sql = "SELECT RELEASE_LOCK('" +  lockResouceString + "')";
		Integer lockWasReleased = singleConnectionJdbcTemplate.queryForObject(sql, Integer.class);	
		if (1 != lockWasReleased ){
			throw new RuntimeException("Failed to set release lock for query " + sql + " [" + lockWasReleased + "]");
		}
		singleConnectionJdbcTemplate.getDataSource().getConnection().close();
		
//		System.out.println("sql lock : " + sql + "(" + lockResult + ")"  );
	}

	
	@Override
	public void getLock(String lockResouceString, int timeout) {
		
		String sql = "SELECT GET_LOCK('" +  lockResouceString + "', " + timeout + ")";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		Integer wasLockObtained = jdbcTemplate.queryForObject(sql, Integer.class);	
		if (1 != wasLockObtained ){
			throw new RuntimeException("Failed to set lock for query " + sql + " [" + wasLockObtained + "]");
		}
//		System.out.println("sql lock : " + sql + "(" + lockResult + ")"  ); //String lockResult = ...
	}

	@Override
	public void releaseLock(String lockResouceString) throws SQLException {
		String sql = "SELECT RELEASE_LOCK('" +  lockResouceString + "')";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);		
		Integer lockWasReleased = jdbcTemplate.queryForObject(sql, Integer.class);	
		if (1 != lockWasReleased ){
			throw new RuntimeException("Failed to set release lock for query " + sql + " [" + lockWasReleased + "]");
		}
		jdbcTemplate.getDataSource().getConnection().close();
		
//		System.out.println("sql lock : " + sql + "(" + lockResult + ")"  );
	}
	
}
