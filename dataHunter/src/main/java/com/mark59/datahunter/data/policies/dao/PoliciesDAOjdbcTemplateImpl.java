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

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
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
	public String constructSelectPolicySql(PolicySelectionCriteria policySelect){
		
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ").append(policySelect.getSelectClause())
		.append( " FROM POLICIES WHERE APPLICATION = '").append( policySelect.getApplication())
		.append( "' AND IDENTIFIER = '").append( policySelect.getIdentifier()  ).append("' ");
		
		if (! DataHunterUtils.isEmpty(policySelect.getLifecycle())) {   								//so only if lifecycle is entered (it is part of the full policy key)
			builder.append( " AND LIFECYCLE = '").append(policySelect.getLifecycle()).append("' ");
		}	
		return builder.toString();
	}
	

	@Override	
	public String constructSelectPoliciesSql(PolicySelectionCriteria policySelect){
		
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ").append(policySelect.getSelectClause())
				.append( " FROM POLICIES WHERE APPLICATION = '").append( policySelect.getApplication()).append( "' ")
				.append(lifecycleAndUseabiltySelector(policySelect));
		
		if (DataHunterUtils.isEmpty(policySelect.getSelectOrder())) {   						 			//default ordering: most recently created first on the list 
			builder.append(" ORDER BY CREATED DESC ");
		} else if (DataHunterConstants.SELECT_UNORDERED.equals(policySelect.getSelectOrder())){      			//eg when just selecting count(*)      
			builder.append("");			
		} else if (DataHunterConstants.SELECT_MOST_RECENTLY_ADDED.equals(policySelect.getSelectOrder())){
			builder.append(" ORDER BY CREATED DESC, EPOCHTIME DESC, IDENTIFIER DESC LIMIT 1 ");			//with Epoch time and Id as a tie-breakers
		} else if (DataHunterConstants.SELECT_OLDEST_ENTRY.equals(policySelect.getSelectOrder())){
			builder.append(" ORDER BY CREATED ASC, EPOCHTIME ASC, IDENTIFIER ASC LIMIT 1 ");			//with Epoch time and Id as a tie-breakers
		} if (DataHunterConstants.SELECT_RANDOM_ENTRY.equals(policySelect.getSelectOrder())){
			builder.append(" ORDER BY RAND() LIMIT 1 ");	
		}
			
		return builder.toString();
	}

	
	@Override	
	public String constructCountPoliciesBreakdownSql(PolicySelectionCriteria policySelect){

		StringBuilder builder = new StringBuilder();
		builder.append("SELECT DISTINCT APPLICATION, LIFECYCLE, USEABILITY, COUNT(*) AS ROWCOUNT FROM POLICIES WHERE ")
			.append(applicationSelectorDependingOnOperator(policySelect))
			.append(lifecycleAndUseabiltySelector(policySelect))				
			.append( " GROUP BY APPLICATION, LIFECYCLE, USEABILITY");
		
		return builder.toString();
	}

		
	@Override
	public String constructAsyncMessageaAnalyzerSql(PolicySelectionCriteria policySelect) {

		StringBuilder builder = new StringBuilder();
		builder.append("SELECT APPLICATION, IDENTIFIER, USEABILITY,  MIN(EPOCHTIME) AS STARTTM, MAX(EPOCHTIME) AS ENDTM, MAX(EPOCHTIME) - MIN(EPOCHTIME) AS DIFFERENCETM FROM POLICIES WHERE ")
			.append(applicationSelectorDependingOnOperator(policySelect));

		if ( ! DataHunterUtils.isEmpty(policySelect.getIdentifier())){  
			builder.append( " AND IDENTIFIER = '").append(policySelect.getIdentifier()).append("' ");
		} 
		if ( ! DataHunterUtils.isEmpty(policySelect.getUseability())){  
			builder.append( " AND USEABILITY = '").append(policySelect.getUseability()).append("' ");
		} 		
		builder.append( " GROUP BY APPLICATION, IDENTIFIER, USEABILITY HAVING COUNT(*) > 1 ORDER BY APPLICATION DESC, IDENTIFIER DESC");
		
		return builder.toString();
	}
	
	

	
	@Override	
	public String constructDeleteMultiplePoliciesSql(PolicySelectionCriteria policySelect){
		
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM POLICIES WHERE APPLICATION = '").append( policySelect.getApplication())	.append( "' ")
				.append(lifecycleAndUseabiltySelector(policySelect));

		return builder.toString();
	}
	
	
	private String  applicationSelectorDependingOnOperator(PolicySelectionCriteria policySelect) {
		String applicationSelection = " APPLICATION =  '" + policySelect.getApplication() + "' ";
		if ( DataHunterConstants.STARTS_WITH.equals(policySelect.getApplicationStartsWithOrEquals()) ){
			applicationSelection = " APPLICATION LIKE '"  + policySelect.getApplication() + "%' ";
		}
		return applicationSelection;
	}
	

	
	private String lifecycleAndUseabiltySelector(PolicySelectionCriteria policySelect) {
		StringBuilder builder = new StringBuilder();
		if ( DataHunterUtils.isEmpty(policySelect.getLifecycle()) && DataHunterUtils.isEmpty(policySelect.getUseability()) ){  
			// do nothing, sql done
		} else if (DataHunterUtils.isEmpty(policySelect.getUseability())) {   								//so only lifecycle has a value
			builder.append( " AND LIFECYCLE = '").append(policySelect.getLifecycle()).append("' ");
		} else if (DataHunterUtils.isEmpty(policySelect.getLifecycle())) {   								//so only usability has a value
			builder.append(" AND USEABILITY = '").append(policySelect.getUseability()).append("' ");
		} else {																						//so both have a value set 
			builder.append(" AND LIFECYCLE = '").append(policySelect.getLifecycle())
			.append("' AND USEABILITY = '").append(policySelect.getUseability()).append("'  ");	
		}
		return builder.toString(); 
	}
	
	
	@Override
	public List<CountPoliciesBreakdown> runCountPoliciesBreakdownSql(String sql) {

		List<CountPoliciesBreakdown> countPoliciesBreakdownList = new ArrayList<CountPoliciesBreakdown>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		for (Map<String, Object> row : rows) {
			CountPoliciesBreakdown countPoliciesBreakdown = populateCountPoliciesBreakdownFromResultSet(row);
			countPoliciesBreakdownList.add(countPoliciesBreakdown);
		}	
		return countPoliciesBreakdownList;
	}

	
	@Override
	public List<AsyncMessageaAnalyzerResult> runAsyncMessageaAnalyzerSql(String sql) {

		List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList = new ArrayList<AsyncMessageaAnalyzerResult>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		for (Map<String, Object> row : rows) {
			AsyncMessageaAnalyzerResult asyncMessageaAnalyzerResult = populateAsyncMessageaAnalyzerResultFromSqlResultRow(row);
			asyncMessageaAnalyzerResultList.add(asyncMessageaAnalyzerResult);
		}	
		return asyncMessageaAnalyzerResultList;
	}

	
	
	@Override	
	public int runCountSql(String sql){
		Integer rowCount  = 0;
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		rowCount = Integer.valueOf(jdbcTemplate.queryForObject(sql, String.class));
		return rowCount;
		
	};	
	

	@Override
	public List<Policies> runSelectPolicieSql(String sql) {

		List<Policies> policiesList = new ArrayList<Policies>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		for (Map<String, Object> row : rows) {
			Policies policies = populatePoliciesFromResultSet(row);
			policiesList.add(policies);
		}	
		return policiesList;
	}
	
		
			
	@Override
	public String constructInsertDataSql(Policies policies) {
		
		StringBuilder builder = new StringBuilder();		
		builder.append("INSERT INTO POLICIES (APPLICATION, IDENTIFIER, LIFECYCLE, USEABILITY, OTHERDATA, CREATED, UPDATED, EPOCHTIME) VALUES ( '" )
		       .append(policies.getApplication()).append("', '")
		       .append(policies.getIdentifier()).append("', '")
		       .append(policies.getLifecycle()).append("', '")
		       .append(policies.getUseability()).append("', '")
		       .append(policies.getOtherdata()).append("', ")
		       .append("NOW(), " )								//created
		       .append("CURRENT_TIMESTAMP, ")					//updated		
		       .append(policies.getEpochtime())
		       .append(" )");

		return builder.toString();
	}

	
	@Override
	public String constructDeletePoliciesSql(PolicySelectionCriteria policySelectionCriteria) {
		
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM POLICIES WHERE APPLICATION = '").append(policySelectionCriteria.getApplication())
				.append("' AND IDENTIFIER = '").append(policySelectionCriteria.getIdentifier() ).append( "' ");

		return builder.toString();
	}	
	

	
	@Override
	public String  constructUpdatePolicyToUsedSql(Policies nextPolicy) {
		
		UpdateUseStateAndEpochTime updateUse = new UpdateUseStateAndEpochTime();
		updateUse.setApplication(nextPolicy.getApplication());
		updateUse.setIdentifier(nextPolicy.getIdentifier() );
		updateUse.setLifecycle(nextPolicy.getLifecycle() );			
		updateUse.setUseability(nextPolicy.getUseability());
		updateUse.setToUseability(DataHunterConstants.USED);
		updateUse.setToEpochTime(nextPolicy.getEpochtime());

		String sql = constructUpdatePoliciesUseStateSql(updateUse);
		return sql;
	}

	


	
	@Override
	public String constructUpdatePoliciesUseStateSql(UpdateUseStateAndEpochTime updateUse) {
		StringBuilder builder = new StringBuilder();

		builder.append("UPDATE POLICIES SET USEABILITY = '").append(updateUse.getToUseability()).append("' ") 
			   .append(", UPDATED = CURRENT_TIMESTAMP "); 

		if (updateUse.getToEpochTime() !=  null ){
			builder.append(", EPOCHTIME = ").append(updateUse.getToEpochTime()); 	
		}
		
		builder.append(" WHERE APPLICATION = '").append(updateUse.getApplication()).append("' ");
		
		if (! DataHunterUtils.isEmpty(updateUse.getLifecycle())) {   		 	
			builder.append(" AND LIFECYCLE = '").append(updateUse.getLifecycle()).append( "' ");
		}		
		
		if (! DataHunterUtils.isEmpty(updateUse.getIdentifier())) {   		 	
			builder.append(" AND IDENTIFIER = '").append(updateUse.getIdentifier()).append( "' ");
		}		
		
		if (! DataHunterUtils.isEmpty(updateUse.getUseability())) {   		 	
			builder.append(" AND USEABILITY = '").append(updateUse.getUseability()).append( "' ");
		}
		return builder.toString(); 
	}
	
	
	
	@Override
	public int runDatabaseUpdateSql(String sql) {
//		System.out.println("PoliciesDAO runDatabaseUpdateSql sql : " +  sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		int rowsAffected = jdbcTemplate.update(sql);
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
