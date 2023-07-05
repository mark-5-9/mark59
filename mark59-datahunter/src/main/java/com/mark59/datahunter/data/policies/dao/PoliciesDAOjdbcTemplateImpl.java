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

    @Autowired
    private String currentDatabaseProfile;
	
	
	@Override	
	public SqlWithParms constructSelectPolicySql(PolicySelectionCriteria policySelect){
	
		String sql = "";
		if (PoliciesDAO.SELECT_POLICY_COUNTS.equals(policySelect.getSelectClause())){
			sql = "SELECT " + PoliciesDAO.SELECT_POLICY_COUNTS + " FROM POLICIES ";
		} else {
			sql = "SELECT " + PoliciesDAO.SELECT_POLICY_COLUMNS + " FROM POLICIES ";
		}
		
		sql += "WHERE APPLICATION = :application "
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

		String sql = "";
		if (PoliciesDAO.SELECT_POLICY_COUNTS.equals(policySelect.getSelectClause())){
			sql = "SELECT " + PoliciesDAO.SELECT_POLICY_COUNTS + " FROM POLICIES ";
		} else {
			sql = "SELECT " + PoliciesDAO.SELECT_POLICY_COLUMNS + " FROM POLICIES ";
		}
		
		sql += "WHERE APPLICATION = :application " + sqlWithParms.getSql();;		
			
		if (DataHunterUtils.isEmpty(policySelect.getSelectOrder())) {   							//default ordering: most recently created first 
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
		// System.out.println("constructSelectPoliciesSql           sql: " + sql);
		// System.out.println("constructSelectPoliciesSql sqlparameters: " + sqlparameters);
	
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
		sql += " GROUP BY APPLICATION, LIFECYCLE, USEABILITY ";
		sql += " ORDER BY APPLICATION, LIFECYCLE, USEABILITY ";

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

		List<CountPoliciesBreakdown> countPoliciesBreakdownList = new ArrayList<>();
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

		List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList = new ArrayList<>();
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlWithParms.getSql(), sqlWithParms.getSqlparameters());
		
		for (Map<String, Object> row : rows) {
			AsyncMessageaAnalyzerResult asyncMessageaAnalyzerResult = new AsyncMessageaAnalyzerResult();
			asyncMessageaAnalyzerResult.setApplication((String)row.get("APPLICATION"));
			asyncMessageaAnalyzerResult.setIdentifier((String)row.get("IDENTIFIER"));
			asyncMessageaAnalyzerResult.setUseability((String)row.get("USEABILITY"));
			asyncMessageaAnalyzerResult.setStarttm((Long)row.get("STARTTM"));
			asyncMessageaAnalyzerResult.setEndtm((Long)row.get("ENDTM"));	
			asyncMessageaAnalyzerResult.setDifferencetm((Long)row.get("DIFFERENCETM"));			
			asyncMessageaAnalyzerResultList.add(asyncMessageaAnalyzerResult);
		}	
		return asyncMessageaAnalyzerResultList;
	}

	
	
	
	
	@Override	
	public int runCountSql(SqlWithParms sqlWithParms){
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		return Integer.parseInt(jdbcTemplate.queryForObject(sqlWithParms.getSql(), sqlWithParms.getSqlparameters(), String.class));
	}


	@Override
	public List<Policies> runSelectPolicieSql(SqlWithParms sqlWithParms) {

		List<Policies> policiesList = new ArrayList<>();
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
				+ "(:application, :identifier, :lifecycle, :useability, :otherdata, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6), :epochtime) ";
				
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

		String sql = "DELETE FROM POLICIES WHERE APPLICATION = :application AND IDENTIFIER = :identifier AND LIFECYCLE = :lifecycle  ";
				
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", policySelectionCriteria.getApplication())
				.addValue("identifier",  policySelectionCriteria.getIdentifier())
				.addValue("lifecycle",   policySelectionCriteria.getLifecycle());
		
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

		return constructUpdatePoliciesUseStateSql(updateUse);
	}

	
	@Override
	public SqlWithParms constructUpdatePoliciesUseStateSql(UpdateUseStateAndEpochTime updateUse) {
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("toUseability", updateUse.getToUseability())
				.addValue("application", updateUse.getApplication());
				
		String sql = "UPDATE POLICIES SET USEABILITY = :toUseability, UPDATED = CURRENT_TIMESTAMP(6) "; 		

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
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		return jdbcTemplate.update(sqlWithParms.getSql(), sqlWithParms.getSqlparameters());
	}
	
	
	/**
	 * Testing on the default database configurations indicated that multiple row updates in a single SQL statement were far more
	 *  effective for MYSQL and POSTGRES, but for H2 updating single rows multiple times was faster.     
	 */
	@Override
	public List<AsyncMessageaAnalyzerResult> updateMultiplePoliciesUseState(List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList,
			String toUseability) {
		if (currentDatabaseProfile.startsWith(DataHunterConstants.H2)){
			return updateMultiplePoliciesUseState(asyncMessageaAnalyzerResultList, toUseability, 1 );
		} else {
			return updateMultiplePoliciesUseState(asyncMessageaAnalyzerResultList, toUseability, 100 );			
		}
	}
	

	@Override
	public List<AsyncMessageaAnalyzerResult> updateMultiplePoliciesUseState(List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList, 
			String toUseability, int maxEntriesSqlUpdateStmt ){
		
		final String sqlBegins = "UPDATE POLICIES SET USEABILITY = ?, UPDATED = CURRENT_TIMESTAMP(6) WHERE ( ";
		
		boolean yetToAddFirstIdToSqlStatement = true;
		ArrayList<Object> sqlBindParms = new ArrayList<>();
		sqlBindParms.add(toUseability);
		StringBuilder sqlSb = new StringBuilder().append(sqlBegins);
		int i = 0;
		
		for (AsyncMessageaAnalyzerResult asyncMessageaAnalyzerResult : asyncMessageaAnalyzerResultList) {
			asyncMessageaAnalyzerResult.setUseability(toUseability);
			i++;
			if (yetToAddFirstIdToSqlStatement) {
				yetToAddFirstIdToSqlStatement = false;
				sqlSb.append( "APPLICATION=? AND IDENTIFIER=? " );
			} else {
				sqlSb.append( "OR APPLICATION=? AND IDENTIFIER=? " );
			}
			
			sqlBindParms.add(asyncMessageaAnalyzerResult.getApplication());
			sqlBindParms.add(asyncMessageaAnalyzerResult.getIdentifier());

	    	if ( (i % maxEntriesSqlUpdateStmt) == 0 ){
	    		sqlSb.append(" )");
	    		runUpdateMultiplePoliciesUseState(sqlSb, sqlBindParms, yetToAddFirstIdToSqlStatement);
	    		yetToAddFirstIdToSqlStatement = true;
	    		sqlBindParms = new ArrayList<>();
	    		sqlBindParms.add(toUseability);
	    		sqlSb = new StringBuilder().append(sqlBegins);
	    		i = 0;
	    	}
		}
		
		sqlSb.append(" )");
		runUpdateMultiplePoliciesUseState(sqlSb, sqlBindParms, yetToAddFirstIdToSqlStatement);
		return asyncMessageaAnalyzerResultList;
	}
		

	private int runUpdateMultiplePoliciesUseState(StringBuilder sqlSb, ArrayList<Object> sqlBindParms, boolean yetToAddFirstIdToSqlStatement) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		int rowsAffected = 0;
		
		if (!yetToAddFirstIdToSqlStatement) {
			try {
				rowsAffected = jdbcTemplate.update(sqlSb.toString() , sqlBindParms.toArray());			
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("   runUpdateMultiplePoliciesUseState sql: " + sqlSb.toString());
				System.err.println("   runUpdateMultiplePoliciesUseState sqlBindParms Array : ");
		        for(int i = 0; i < sqlBindParms.size(); i++) {
		            System.err.println("  " + (i+1)  + " : " + sqlBindParms.get(i));
		        }
				System.out.println("   ------------------------");
				throw new RuntimeException();
			}
		}
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
	

	private CountPoliciesBreakdown populateCountPoliciesBreakdownFromResultSet(Map<String, Object> row) {
		CountPoliciesBreakdown policiesSelectedCountsBreakdown = new CountPoliciesBreakdown();
		policiesSelectedCountsBreakdown.setApplication((String)row.get("APPLICATION"));
		policiesSelectedCountsBreakdown.setLifecycle((String)row.get("LIFECYCLE"));
		policiesSelectedCountsBreakdown.setUseability((String)row.get("USEABILITY"));		
		policiesSelectedCountsBreakdown.setRowCount((Long)row.get("ROWCOUNT"));
		return policiesSelectedCountsBreakdown;
	}
	

	
	@Override
	public void getLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString, int timeout) {
		
		String sql = "SELECT GET_LOCK('" +  lockResouceString + "', " + timeout + ")";
		
		Integer wasLockObtained = singleConnectionJdbcTemplate.queryForObject(sql, Integer.class);	
		if (wasLockObtained != null  &&  1 != wasLockObtained ){
			throw new RuntimeException("Failed to set lock for query " + sql + " [" + wasLockObtained + "]");
		}
//		System.out.println("sql lock : " + sql + "(" + lockResult + ")"  ); //String lockResult = ...
	}

	@Override
	public void releaseLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString) throws SQLException {
		String sql = "SELECT RELEASE_LOCK('" +  lockResouceString + "')";
		Integer lockWasReleased = singleConnectionJdbcTemplate.queryForObject(sql, Integer.class);	
		if (lockWasReleased != null  &&  1 != lockWasReleased ){
			throw new RuntimeException("Failed to set release lock for query " + sql + " [" + lockWasReleased + "]");
		}
		if (singleConnectionJdbcTemplate.getDataSource() != null
				&& singleConnectionJdbcTemplate.getDataSource().getConnection() != null) {
			singleConnectionJdbcTemplate.getDataSource().getConnection().close();
		}
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
		if (jdbcTemplate.getDataSource() != null && jdbcTemplate.getDataSource().getConnection() != null) {
			jdbcTemplate.getDataSource().getConnection().close();
		}		
//		System.out.println("sql lock : " + sql + "(" + lockResult + ")"  );
	}


}
