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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.ReusableIndexedUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerResult;
import com.mark59.datahunter.model.CountPoliciesBreakdown;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.PolicySelectionFilter;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;
import com.mark59.datahunter.pojo.ValidReuseIxPojo;


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
	public SqlWithParms constructSelectPolicySql(Policies policies){
		PolicySelectionCriteria policySelect = new PolicySelectionCriteria();
		policySelect.setSelectClause(PoliciesDAO.SELECT_POLICY_COLUMNS);
		policySelect.setApplication(policies.getApplication());
		policySelect.setIdentifier(policies.getIdentifier());
		policySelect.setLifecycle(policies.getLifecycle());
		return constructSelectPolicySql(policySelect);
	}
	
    
	@Override	
	public SqlWithParms constructSelectPolicySql(PolicySelectionCriteria policySelect){
		trimKeys(policySelect);
		
		String sql = "";
		if (PoliciesDAO.SELECT_POLICY_COUNTS.equals(policySelect.getSelectClause())){
			sql = "SELECT " + PoliciesDAO.SELECT_POLICY_COUNTS + " FROM POLICIES ";
		} else {
			sql = "SELECT " + PoliciesDAO.SELECT_POLICY_COLUMNS + " FROM POLICIES ";
		}
		sql += "WHERE APPLICATION = :application "
				+ "  AND IDENTIFIER = :identifier "
				+ "  AND LIFECYCLE = :lifecycle ";

		if (StringUtils.isBlank(policySelect.getLifecycle())) {
			policySelect.setLifecycle(""); 
		}   	
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", policySelect.getApplication())
				.addValue("identifier", policySelect.getIdentifier())
				.addValue("lifecycle", policySelect.getLifecycle()); 
	
		return new SqlWithParms(sql,sqlparameters);
	}
	

	@Override	
	public SqlWithParms constructSelectPoliciesFilterSql(PolicySelectionFilter policySelectionFilter){
		return constructSelectPoliciesFilterSql(policySelectionFilter, true); 
	}
	
	@Override	
	public SqlWithParms constructSelectPoliciesFilterSql(PolicySelectionFilter policySelectionFilter, boolean applyLimit){
		trimKeys(policySelectionFilter);
		
		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelectionFilter);
		
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters()
				.addValue("application", policySelectionFilter.getApplication());

		String sql = "SELECT " + PoliciesDAO.SELECT_POLICY_COLUMNS + " FROM POLICIES WHERE APPLICATION = :application " + sqlWithParms.getSql();	
		
		sql = otherDataAndDatesSelector(policySelectionFilter, sqlparameters, sql);
		sql = orderBySelector(policySelectionFilter, sql);
		if (applyLimit) {
			sql = limitSelector(policySelectionFilter, sql);
		}
		return new SqlWithParms(sql,sqlparameters);
	}

	
	@Override	
	public SqlWithParms constructSelectNextPolicySql(PolicySelectionCriteria policySelect){
		trimKeys(policySelect);
		
		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelect);
		
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters()
				.addValue("application", policySelect.getApplication());

		String sql = "SELECT " + PoliciesDAO.SELECT_POLICY_COLUMNS + " FROM POLICIES WHERE APPLICATION = :application " + sqlWithParms.getSql();
		
		ValidReuseIxPojo validReuseIx = ReusableIndexedUtils.validateReusableIndexed(policySelect, this);

		if (validReuseIx.getPolicyReusableIndexed()){  // the special  'Reusable Indexed' case
			if (!validReuseIx.getValidatedOk()) {
				throw new RuntimeException(validReuseIx.getErrorMsg());				
			}
			if (DataHunterConstants.SELECT_MOST_RECENTLY_ADDED.equals(policySelect.getSelectOrder())){
				sql += " AND IDENTIFIER <> '" + DataHunterConstants.INDEXED_ROW_COUNT + "' ORDER BY IDENTIFIER DESC LIMIT 1 ";
			} else if (DataHunterConstants.SELECT_OLDEST_ENTRY.equals(policySelect.getSelectOrder())){
				sql += " AND IDENTIFIER <> '" + DataHunterConstants.INDEXED_ROW_COUNT + "' ORDER BY IDENTIFIER ASC LIMIT 1 ";
			} else if (DataHunterConstants.SELECT_RANDOM_ENTRY.equals(policySelect.getSelectOrder())){
				int randInRange = ThreadLocalRandom.current().nextInt(1, Integer.parseInt(validReuseIx.getIxPolicy().getOtherdata())+1);
				String randIdentifer = StringUtils.leftPad(String.valueOf(randInRange), 10, "0");
				sqlparameters = sqlWithParms.getSqlparameters().addValue("identifier", randIdentifer);
				// mark this as a special case of a random lookup on 'reusable Indexed' data.
				sqlparameters = sqlWithParms.getSqlparameters().addValue(DataHunterConstants.REUSEABLE_INDEXED_RAND, String.valueOf(true));
				sql += " AND IDENTIFIER = :identifier ";
			} else {
				throw new RuntimeException("error - invalid Select Order (Reusable Indexed data) : " + policySelect.getSelectOrder());
			}
			
		} else { // not a special 'Reusable Indexed' case

			if (DataHunterConstants.SELECT_MOST_RECENTLY_ADDED.equals(policySelect.getSelectOrder())){
				sql += " ORDER BY CREATED DESC, EPOCHTIME DESC, IDENTIFIER DESC LIMIT 1 ";		// Epoch time and Id are just tie-breakers
			} else if (DataHunterConstants.SELECT_OLDEST_ENTRY.equals(policySelect.getSelectOrder())){
				sql += " ORDER BY CREATED ASC, EPOCHTIME ASC, IDENTIFIER ASC LIMIT 1 ";			// Epoch time and Id are just tie-breakers
			} else if (DataHunterConstants.SELECT_RANDOM_ENTRY.equals(policySelect.getSelectOrder())){
				sql += " ORDER BY RAND() LIMIT 1 ";	
			} else {
				throw new RuntimeException("error - invalid Select Order : " + policySelect.getSelectOrder());
			}
		}
		return new SqlWithParms(sql,sqlparameters);
	}
	
	
	@Override	
	public SqlWithParms constructCountPoliciesSql(PolicySelectionCriteria policySelect){
		trimKeys(policySelect);
		
		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelect);
		
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters()
				.addValue("application", policySelect.getApplication());

		String sql = "SELECT " + PoliciesDAO.SELECT_POLICY_COUNTS + " FROM POLICIES WHERE APPLICATION = :application " + sqlWithParms.getSql(); 

		return new SqlWithParms(sql,sqlparameters);
	}

	
	@Override	
	public SqlWithParms constructCountPoliciesBreakdownSql(PolicySelectionCriteria policySelect){
		String applicationStripStart = "%";
		if (policySelect.getApplication() !=null ) {
			applicationStripStart = StringUtils.stripStart(policySelect.getApplication(), null) + "%";
		}
		trimKeys(policySelect);
		
		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelect);
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters();

		String sql = "SELECT DISTINCT APPLICATION, LIFECYCLE, USEABILITY, COUNT(*) AS ROWCOUNT FROM POLICIES WHERE " ;
				
		if ( DataHunterConstants.STARTS_WITH.equals(policySelect.getApplicationStartsWithOrEquals()) ){
			// allows for embedded space within Application name, but still remove any leading whitespace 
			sqlparameters.addValue("applicationLike", applicationStripStart);
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
		String applicationStripStart = "%";
		if (policySelect.getApplication() !=null ) {
			applicationStripStart = StringUtils.stripStart(policySelect.getApplication(), null) + "%";
		}
		trimKeys(policySelect);
		
		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelect);
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters();		
		
		String sql = "SELECT APPLICATION, IDENTIFIER, USEABILITY,  "
				+ "MIN(EPOCHTIME) AS STARTTM, "
				+ "MAX(EPOCHTIME) AS ENDTM, "
				+ "MAX(EPOCHTIME) - MIN(EPOCHTIME) AS DIFFERENCETM "
				+ "FROM POLICIES WHERE ";
		
		if ( DataHunterConstants.STARTS_WITH.equals(policySelect.getApplicationStartsWithOrEquals()) ){
			sqlparameters.addValue("applicationLike", applicationStripStart);
			sql += " APPLICATION LIKE :applicationLike ";
		} else {
			sqlparameters.addValue("application", policySelect.getApplication());
			sql += " APPLICATION = :application ";
		}

		if (StringUtils.isNotBlank(policySelect.getIdentifier())){  
			sqlparameters.addValue("identifier", policySelect.getIdentifier());
			sql += " AND IDENTIFIER = :identifier ";
		} 
		if (StringUtils.isNotBlank(policySelect.getUseability())){  
			sqlparameters.addValue("useability", policySelect.getUseability());
			sql += " AND USEABILITY = :useability ";
		} 		
		sql += " GROUP BY APPLICATION, IDENTIFIER, USEABILITY HAVING COUNT(*) > 1 ORDER BY APPLICATION DESC, IDENTIFIER DESC" ;
		
		return new SqlWithParms(sql,sqlparameters);		
	}

	
	@Override	
	public SqlWithParms constructDeleteMultiplePoliciesSql(PolicySelectionFilter policySelectionFilter){
		trimKeys(policySelectionFilter);
		
		SqlWithParms sqlWithParms = lifecycleAndUseabiltySelector(policySelectionFilter);
				
		MapSqlParameterSource sqlparameters = sqlWithParms.getSqlparameters()
				.addValue("application", policySelectionFilter.getApplication());

		String sql = "DELETE FROM POLICIES WHERE APPLICATION = :application " + sqlWithParms.getSql();
		
		sql = otherDataAndDatesSelector(policySelectionFilter, sqlparameters, sql);
		
		return new SqlWithParms(sql,sqlparameters);
	}

	
	private SqlWithParms lifecycleAndUseabiltySelector(PolicySelectionCriteria policySelect) {
		String sql = "";
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource();

		if (StringUtils.isBlank(policySelect.getLifecycle()) && StringUtils.isBlank(policySelect.getUseability())){  
			// do nothing, sql done
		} else if (StringUtils.isBlank(policySelect.getUseability())) { 	// only lifecycle has a value
			sqlparameters.addValue("lifecycle", policySelect.getLifecycle());
			sql += " AND LIFECYCLE = :lifecycle ";	
		} else if (StringUtils.isBlank(policySelect.getLifecycle())) {  	// only usability has a value
			sqlparameters.addValue("useability", policySelect.getUseability().trim());
			sql += " AND USEABILITY = :useability ";
		} else {																// both have a value set
			sqlparameters.addValue("lifecycle", policySelect.getLifecycle()); 
			sqlparameters.addValue("useability", policySelect.getUseability().trim());			
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
	public Stream<Policies> runStreamPolicieSql(SqlWithParms sqlWithParms) {
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		Stream<Policies> PolicyStream = jdbcTemplate.queryForStream (
				sqlWithParms.getSql(), 
				sqlWithParms.getSqlparameters(),
				new PoliciesRowMapper());		
		return PolicyStream;
	}
	
	
				
	@Override
	public SqlWithParms constructInsertDataSql(Policies policies) {
		trimKeys(policies);
		if (policies.getEpochtime() == null){
			policies.setEpochtime(System.currentTimeMillis());
		}

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
	public SqlWithParms countReusableIndexedIdsInExpectedRange(PolicySelectionCriteria policySelect, int ixCount){
		trimKeys(policySelect);
		String highid = StringUtils.leftPad(String.valueOf(ixCount), 10, "0");
		
		String sql = " SELECT " + PoliciesDAO.SELECT_POLICY_COUNTS + " FROM POLICIES "
				+ " WHERE APPLICATION = :application "
				+ "  AND LIFECYCLE = :lifecycle "
				+ "  AND USEABILITY = 'REUSABLE'"
				+ "  AND IDENTIFIER BETWEEN '0000000001' AND :highid "
				+ "  AND CHAR_LENGTH(IDENTIFIER) = 10 ";
		if (DataHunterConstants.PG.equalsIgnoreCase(currentDatabaseProfile)){				
			sql = sql + " AND NOT IDENTIFIER ~ '[^0-9]' ";
		} else {	
			sql = sql + " AND NOT IDENTIFIER REGEXP '[^0-9]' ";
		}
		
		if (StringUtils.isBlank(policySelect.getLifecycle())) {
			policySelect.setLifecycle(""); 
		}   	
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", policySelect.getApplication())
				.addValue("lifecycle", policySelect.getLifecycle())
				.addValue("highid", highid); 
	
		return new SqlWithParms(sql,sqlparameters);
	}


	@Override	
	public SqlWithParms countNonReusableIdsForReusableIndexedData(String application, String lifecycle){
		
		String sql = " SELECT " + PoliciesDAO.SELECT_POLICY_COUNTS + " FROM POLICIES "
				+ " WHERE APPLICATION = :application "
				+ "  AND LIFECYCLE = :lifecycle "
				+ "  AND USEABILITY != 'REUSABLE'";
	
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("lifecycle", lifecycle);

		return new SqlWithParms(sql,sqlparameters);
	}



	@Override
	public SqlWithParms constructCollectDataOutOfExpectedIxRangeSql(String application, String lifecycle, int policyCount) {
		String highid = StringUtils.leftPad(String.valueOf(policyCount), 10, "0");
		//System.out.println("highid:"+highid);
		
		String sql = " SELECT " + PoliciesDAO.SELECT_POLICY_COLUMNS + " FROM POLICIES "
				+ " WHERE APPLICATION = :application "
				+ "  AND LIFECYCLE = :lifecycle "
				+ "  AND IDENTIFIER != '0000000000_IX' "  
				+ "  AND (IDENTIFIER < '0000000001' "
				+ "      OR IDENTIFIER > :highid "
				+ "      OR CHAR_LENGTH(IDENTIFIER) != 10 " ;          // belt and braces check
		if (DataHunterConstants.PG.equalsIgnoreCase(currentDatabaseProfile)){				
			sql = sql + " OR IDENTIFIER ~ '[^0-9]' ) ";
		} else {	
			sql = sql + " OR IDENTIFIER REGEXP '[^0-9]' ) ";
		}

		if (StringUtils.isBlank(lifecycle)){
			lifecycle = ""; 
		}   	
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("lifecycle", lifecycle)
				.addValue("highid", highid); 
	
		return new SqlWithParms(sql,sqlparameters);
	}

	
	@Override
	public void insertMultiple(List<Policies> policiesList) {
		
		boolean yetToAddFirstRowToSqlStatement = true;
		StringBuilder sqlSb = new StringBuilder();
		
		sqlSb.append("INSERT INTO POLICIES "
				+ "(APPLICATION,IDENTIFIER,LIFECYCLE,USEABILITY,OTHERDATA,CREATED,UPDATED,EPOCHTIME)"
				+ " VALUES (?,?,?,?,?,CURRENT_TIMESTAMP(6),CURRENT_TIMESTAMP(6),?)");
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		ArrayList<Object> sqlBindParms = new ArrayList<>();
		
		for (Policies policies : policiesList) {
			trimKeys(policies);
			policies.setEpochtime(System.currentTimeMillis());
			
			sqlBindParms.add(policies.getApplication());
			sqlBindParms.add(policies.getIdentifier());
			sqlBindParms.add(policies.getLifecycle());
			sqlBindParms.add(policies.getUseability());
			sqlBindParms.add(policies.getOtherdata());
			sqlBindParms.add(policies.getEpochtime());
			
			if (yetToAddFirstRowToSqlStatement) {
				yetToAddFirstRowToSqlStatement = false;
			} else {
				sqlSb.append( ",(?,?,?,?,?,CURRENT_TIMESTAMP(6),CURRENT_TIMESTAMP(6),?)" );
			}

		} //end for
				
		if (!yetToAddFirstRowToSqlStatement) {
			try {
				jdbcTemplate.update(sqlSb.toString() , sqlBindParms.toArray());			
			} catch (Exception e) {
				e.printStackTrace();
				String sqlBindParmsToArray="";
		        for(int i = 0; i < sqlBindParms.size(); i++) {
		        	sqlBindParmsToArray += (" - " + (i+1)  + " : " + sqlBindParms.get(i));
		        }
				throw new RuntimeException("RuntimeException at PoliciesDAOjdbcTemplateImpl.insertMultiple"
						+ "<br><br> message=" + e.getMessage()
						+ "<br><br> sql=" + sqlSb.toString() 
						+ "<br><br> sqlBindParms=" + sqlBindParmsToArray);
			}
		}
	}
	
	
	@Override
	public SqlWithParms constructDeletePoliciesSql(PolicySelectionCriteria policySelectionCriteria) {
		trimKeys(policySelectionCriteria);
		
		String sql = "DELETE FROM POLICIES WHERE APPLICATION = :application AND IDENTIFIER = :identifier AND LIFECYCLE = :lifecycle  ";
				
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", policySelectionCriteria.getApplication())
				.addValue("identifier",  policySelectionCriteria.getIdentifier())
				.addValue("lifecycle",   policySelectionCriteria.getLifecycle());
		
		return new SqlWithParms(sql,sqlparameters);		
	}	

	
	@Override
	public SqlWithParms constructUpdatePoliciesSql(Policies policies) {
		trimKeys(policies);

		if (policies.getEpochtime() == null){ // just in case
			policies.setEpochtime(System.currentTimeMillis());
		}
		
		String sql = "UPDATE POLICIES SET USEABILITY = :useability, "
				+ "OTHERDATA = :otherdata, "				
				+ "EPOCHTIME = :epochtime, "				
				+ "UPDATED = CURRENT_TIMESTAMP(6) "
				+ "WHERE APPLICATION = :application "
				+ "AND IDENTIFIER = :identifier "
				+ "AND LIFECYCLE = :lifecycle ";
				
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", policies.getApplication())
				.addValue("identifier",  policies.getIdentifier())
				.addValue("lifecycle",   policies.getLifecycle())
				.addValue("useability",  policies.getUseability())
				.addValue("otherdata",   policies.getOtherdata())
				.addValue("epochtime",	 policies.getEpochtime());

		return new SqlWithParms(sql,sqlparameters);		
	}	
	
	
	@Override
	public SqlWithParms constructUpdatePolicyToUsedSql(Policies nextPolicy) {
		trimKeys(nextPolicy);
		UpdateUseStateAndEpochTime updateUse = new UpdateUseStateAndEpochTime();
		updateUse.setApplication(nextPolicy.getApplication());
		updateUse.setIdentifier(nextPolicy.getIdentifier());
		updateUse.setLifecycle(nextPolicy.getLifecycle());			
		updateUse.setUseability(nextPolicy.getUseability());
		updateUse.setToUseability(DataHunterConstants.USED);
		updateUse.setToEpochTime(nextPolicy.getEpochtime());

		return constructUpdatePoliciesUseStateSql(updateUse);
	}

	
	@Override
	public SqlWithParms constructUpdatePoliciesUseStateSql(UpdateUseStateAndEpochTime updateUse) {
		trimKeys(updateUse);
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("toUseability", updateUse.getToUseability())
				.addValue("application", updateUse.getApplication());
				
		String sql = "UPDATE POLICIES SET USEABILITY = :toUseability, UPDATED = CURRENT_TIMESTAMP(6) "; 		

		if (updateUse.getToEpochTime() !=  null ){
			sqlparameters.addValue("epochTime", updateUse.getToEpochTime());
			sql += ", EPOCHTIME = :epochTime "; 
		}
		sql += " WHERE APPLICATION = :application ";

		if (StringUtils.isNotBlank(updateUse.getLifecycle())) {   
			sqlparameters.addValue("lifecycle", updateUse.getLifecycle());
			sql += " AND LIFECYCLE = :lifecycle ";
		}
		if (StringUtils.isNotBlank(updateUse.getIdentifier())) {  
			sqlparameters.addValue("identifier", updateUse.getIdentifier());
			sql += " AND IDENTIFIER = :identifier ";
		}
		if (StringUtils.isNotBlank(updateUse.getUseability())) {   
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
		
		if (toUseability != null){
			toUseability = toUseability.trim();
		}
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
				String sqlBindParmsToArray="";
		        for(int i = 0; i < sqlBindParms.size(); i++) {
		        	sqlBindParmsToArray += (" - " + (i+1)  + " : " + sqlBindParms.get(i));
		        }
				throw new RuntimeException("RuntimeException at runUpdateMultiplePoliciesUseState, sql=" + sqlSb.toString() +
						", sqlBindParms.toArray()=" + sqlBindParmsToArray);
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

	
	private String otherDataAndDatesSelector(PolicySelectionFilter policySelectionFilter,
			MapSqlParameterSource sqlparameters, String sql) {
		
		if (policySelectionFilter.isIdentifierLikeSelected()   ){
			sqlparameters.addValue("identifierLike", policySelectionFilter.getIdentifierLike());
			sql += " AND IDENTIFIER LIKE :identifierLike ";			
		}
		
		if (policySelectionFilter.isIdentifierListSelected()){
			sqlparameters.addValue("identifierList", DataHunterUtils.commaDelimStringToStringSet(policySelectionFilter.getIdentifierList()));
			sql += " AND IDENTIFIER in ( :identifierList ) ";			
		}

		if (policySelectionFilter.isOtherdataSelected()){
			sqlparameters.addValue("otherdata", policySelectionFilter.getOtherdata() );
			sql += " AND OTHERDATA LIKE :otherdata ";			
		}
		if (policySelectionFilter.isCreatedSelected()){
			sqlparameters.addValue("createdFrom", policySelectionFilter.getCreatedFrom());
			sqlparameters.addValue("createdTo"  , policySelectionFilter.getCreatedTo());
			
			if (DataHunterConstants.PG.equalsIgnoreCase(currentDatabaseProfile)){
				sql += " AND CREATED BETWEEN TO_TIMESTAMP( :createdFrom, 'YYYY-MM-DD HH24:MI:SS.US') AND TO_TIMESTAMP( :createdTo , 'YYYY-MM-DD HH24:MI:SS.US')";
			} else {
				sql += " AND CREATED BETWEEN :createdFrom AND :createdTo ";			
			}
		}
		if (policySelectionFilter.isUpdatedSelected()){
			sqlparameters.addValue("updatedFrom", policySelectionFilter.getUpdatedFrom());
			sqlparameters.addValue("updatedTo"  , policySelectionFilter.getUpdatedTo());
			if (DataHunterConstants.PG.equalsIgnoreCase(currentDatabaseProfile)){
				sql += " AND UPDATED BETWEEN TO_TIMESTAMP( :updatedFrom, 'YYYY-MM-DD HH24:MI:SS.US') AND TO_TIMESTAMP( :updatedTo , 'YYYY-MM-DD HH24:MI:SS.US')";			
			} else {
				sql += " AND UPDATED BETWEEN :updatedFrom AND :updatedTo ";			
			}
		}
		if (policySelectionFilter.isEpochtimeSelected()){
			sqlparameters.addValue("epochtimeFrom", policySelectionFilter.getEpochtimeFrom());
			sqlparameters.addValue("epochtimeTo"  , policySelectionFilter.getEpochtimeTo());
			if (DataHunterConstants.PG.equalsIgnoreCase(currentDatabaseProfile)){
				sql += " AND EPOCHTIME BETWEEN cast( :epochtimeFrom as bigint) AND cast( :epochtimeTo as bigint) ";			
			} else {
				sql += " AND EPOCHTIME BETWEEN :epochtimeFrom AND :epochtimeTo ";			
			}
		}
		return sql;
	};	
	

	private String orderBySelector(PolicySelectionFilter policySelectionFilter, String sql) {
		if (StringUtils.isBlank(policySelectionFilter.getSelectOrder())) {  
			sql += " ORDER BY APPLICATION, IDENTIFIER, LIFECYCLE ";
		} else if (DataHunterConstants.KEY.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.ASC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, IDENTIFIER, LIFECYCLE ";
		} else if (DataHunterConstants.KEY.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.DESC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, IDENTIFIER DESC, LIFECYCLE DESC ";
		} else if (DataHunterConstants.USEABILTY_KEY.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.ASC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, USEABILITY, IDENTIFIER, LIFECYCLE ";
		} else if (DataHunterConstants.USEABILTY_KEY.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.DESC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, USEABILITY DESC, IDENTIFIER DESC, LIFECYCLE DESC ";
		} else if (DataHunterConstants.OTHERDATA.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.ASC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, OTHERDATA ";
		} else if (DataHunterConstants.OTHERDATA.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.DESC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, OTHERDATA DESC ";
		} else if (DataHunterConstants.CREATED.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.ASC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, CREATED ";
		} else if (DataHunterConstants.CREATED.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.DESC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, CREATED DESC ";
		}  else if (DataHunterConstants.UPDATED.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.ASC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, UPDATED ";
		} else if (DataHunterConstants.UPDATED.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.DESC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, UPDATED DESC ";
		} else if (DataHunterConstants.EPOCHTIME.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.ASC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, EPOCHTIME ";
		} else if (DataHunterConstants.EPOCHTIME.equals(policySelectionFilter.getSelectOrder()) && DataHunterConstants.DESC.equals(policySelectionFilter.getOrderDirection())){
			sql += " ORDER BY APPLICATION, EPOCHTIME DESC ";
		} else {
			sql += " ORDER BY APPLICATION, IDENTIFIER, LIFECYCLE ";			
		}
		return sql;
	}


	private String limitSelector(PolicySelectionFilter policySelectionFilter, String sql) {
		int limit = 100;
		if (StringUtils.isNumeric(policySelectionFilter.getLimit())){
			limit = Integer.valueOf(policySelectionFilter.getLimit());
			if (limit > 1000) {
				limit = 1000;
			}
		}
		sql+= " LIMIT " + String.valueOf(limit);
		return sql;
	}
	
	
	private void trimKeys(PolicySelectionCriteria policySelect) {
		if (policySelect.getApplication() != null) {
			policySelect.setApplication(policySelect.getApplication().trim());
		}
		if (policySelect.getIdentifier() != null) {
			policySelect.setIdentifier(policySelect.getIdentifier().trim());
		}
		if (policySelect.getLifecycle() != null) {
			policySelect.setLifecycle(policySelect.getLifecycle().trim());
		}
	}
	
	private void trimKeys(UpdateUseStateAndEpochTime updateUse) {
		if (updateUse.getApplication() != null) {
			updateUse.setApplication(updateUse.getApplication().trim());
		}
		if (updateUse.getIdentifier() != null) {
			updateUse.setIdentifier(updateUse.getIdentifier().trim());
		}
		if (updateUse.getLifecycle() != null) {
			updateUse.setLifecycle(updateUse.getLifecycle().trim());
		}
		if (updateUse.getToUseability() != null) {
			updateUse.setToUseability(updateUse.getToUseability().trim());
		}
	}
	
	private void trimKeys(Policies policies) {
		if (policies.getApplication() != null) {
			policies.setApplication(policies.getApplication().trim());
		}
		if (policies.getIdentifier() != null) {
			policies.setIdentifier(policies.getIdentifier().trim());
		}
		if (policies.getLifecycle() != null) {
			policies.setLifecycle(policies.getLifecycle().trim());
		}
	}
	
}
