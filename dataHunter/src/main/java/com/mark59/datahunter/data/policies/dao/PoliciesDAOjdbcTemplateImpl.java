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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.datahunter.application.AppConstants;
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
		.append( " FROM policies WHERE application = '").append( policySelect.getApplication())
		.append( "' AND identifier = '").append( policySelect.getIdentifier()  ).append("' ");
		
		if (! StringUtils.isEmpty(policySelect.getLifecycle())) {   								//so only if lifecycle is entered (it is part of the full policy key)
			builder.append( " AND lifecycle = '").append(policySelect.getLifecycle()).append("' ");
		}	
		return builder.toString();
	}
	

	@Override	
	public String constructSelectPoliciesSql(PolicySelectionCriteria policySelect){
		
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ").append(policySelect.getSelectClause())
				.append( " FROM policies WHERE application = '").append( policySelect.getApplication()).append( "' ")
				.append(lifecycleAndUseabiltySelector(policySelect));
		
		if (StringUtils.isEmpty(policySelect.getSelectOrder())) {   						 			//default ordering: most recently created first on the list 
			builder.append(" ORDER BY created desc");
		} else if (AppConstants.SELECT_UNORDERED.equals(policySelect.getSelectOrder())){      			//eg when just selecting count(*)      
			builder.append("");			
		} else if (AppConstants.SELECT_MOST_RECENTLY_ADDED.equals(policySelect.getSelectOrder())){
			builder.append(" ORDER BY created desc, epochtime desc, identifier desc limit 1 ");			//with Epoch time and Id as a tie-breakers
		} else if (AppConstants.SELECT_OLDEST_ENTRY.equals(policySelect.getSelectOrder())){
			builder.append(" ORDER BY created asc, epochtime asc, identifier asc limit 1 ");			//with Epoch time and Id as a tie-breakers
		} if (AppConstants.SELECT_RANDOM_ENTRY.equals(policySelect.getSelectOrder())){
			builder.append(" ORDER BY RAND() limit 1 ");	
		}
			
		return builder.toString();
	}

	
	@Override	
	public String constructCountPoliciesBreakdownSql(PolicySelectionCriteria policySelect){

		StringBuilder builder = new StringBuilder();
		builder.append("select DISTINCT application, lifecycle, useability, COUNT(*) as rowCount from policies where ")
			.append(applicationSelectorDependingOnOperator(policySelect))
			.append(lifecycleAndUseabiltySelector(policySelect))				
			.append( " group by application, lifecycle, useability");
		
		return builder.toString();
	}

		
	@Override
	public String constructAsyncMessageaAnalyzerSql(PolicySelectionCriteria policySelect) {

		StringBuilder builder = new StringBuilder();
		builder.append("select application, identifier, useability,  min(epochtime) as starttm, max(epochtime) as endtm, max(epochtime) - min(epochtime) as differencetm from policies where ")
			.append(applicationSelectorDependingOnOperator(policySelect));

		if ( ! StringUtils.isEmpty(policySelect.getIdentifier())){  
			builder.append( " AND identifier = '").append(policySelect.getIdentifier()).append("' ");
		} 
		if ( ! StringUtils.isEmpty(policySelect.getUseability())){  
			builder.append( " AND useability = '").append(policySelect.getUseability()).append("' ");
		} 		
		builder.append( " group by application, identifier, useability having count(*) > 1 order by application desc, identifier desc");
		
		return builder.toString();
	}
	
	

	
	@Override	
	public String constructDeleteMultiplePoliciesSql(PolicySelectionCriteria policySelect){
		
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM policies WHERE application = '").append( policySelect.getApplication())	.append( "' ")
				.append(lifecycleAndUseabiltySelector(policySelect));

		return builder.toString();
	}
	
	
	private String  applicationSelectorDependingOnOperator(PolicySelectionCriteria policySelect) {
		String applicationSelection = " application =  '" + policySelect.getApplication() + "' ";
		if ( AppConstants.STARTS_WITH.equals(policySelect.getApplicationStartsWithOrEquals()) ){
			applicationSelection = " application like '"  + policySelect.getApplication() + "%' ";
		}
		return applicationSelection;
	}
	

	
	private String lifecycleAndUseabiltySelector(PolicySelectionCriteria policySelect) {
		StringBuilder builder = new StringBuilder();
		if ( StringUtils.isEmpty(policySelect.getLifecycle()) &&  StringUtils.isEmpty(policySelect.getUseability()) ){  
			// do nothing, sql done
		} else if (StringUtils.isEmpty(policySelect.getUseability())) {   								//so only lifecycle has a value
			builder.append( " AND lifecycle = '").append(policySelect.getLifecycle()).append("' ");
		} else if (StringUtils.isEmpty(policySelect.getLifecycle())) {   								//so only usability has a value
			builder.append(" AND useability = '").append(policySelect.getUseability()).append("' ");
		} else {																						//so both have a value set 
			builder.append(" AND lifecycle = '").append(policySelect.getLifecycle())
			.append("' AND useability = '").append(policySelect.getUseability()).append("'  ");	
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
		rowCount = new Integer(jdbcTemplate.queryForObject(sql, String.class));
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
		builder.append("INSERT INTO policies (application, identifier, lifecycle, useability, otherdata, created, updated, epochtime) VALUES ( '" )
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
		builder.append("DELETE FROM policies WHERE application = '").append(policySelectionCriteria.getApplication())
				.append("' and identifier = '").append(policySelectionCriteria.getIdentifier() ).append( "' ");

		return builder.toString();
	}	
	

	
	@Override
	public String  constructUpdatePolicyToUsedSql(Policies nextPolicy) {
		
		UpdateUseStateAndEpochTime updateUse = new UpdateUseStateAndEpochTime();
		updateUse.setApplication(nextPolicy.getApplication());
		updateUse.setIdentifier(nextPolicy.getIdentifier() );
		updateUse.setLifecycle(nextPolicy.getLifecycle() );			
		updateUse.setUseability(nextPolicy.getUseability());
		updateUse.setToUseability(AppConstants.USED);
		updateUse.setToEpochTime(nextPolicy.getEpochtime());

		String sql = constructUpdatePoliciesUseStateSql(updateUse);
		return sql;
	}

	


	
	@Override
	public String constructUpdatePoliciesUseStateSql(UpdateUseStateAndEpochTime updateUse) {
		StringBuilder builder = new StringBuilder();

		builder.append("UPDATE policies set useability = '").append(updateUse.getToUseability()).append("' ") 
			   .append(", updated = CURRENT_TIMESTAMP "); 

		if (updateUse.getToEpochTime() !=  null ){
			builder.append(", epochtime = ").append(updateUse.getToEpochTime()); 	
		}
		
		builder.append(" WHERE application = '").append(updateUse.getApplication()).append("' ");
		
		if (! StringUtils.isEmpty(updateUse.getLifecycle())) {   		 	
			builder.append(" and lifecycle = '").append(updateUse.getLifecycle()).append( "' ");
		}		
		
		if (! StringUtils.isEmpty(updateUse.getIdentifier())) {   		 	
			builder.append(" and identifier = '").append(updateUse.getIdentifier()).append( "' ");
		}		
		
		if (! StringUtils.isEmpty(updateUse.getUseability())) {   		 	
			builder.append(" and useability = '").append(updateUse.getUseability()).append( "' ");
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
		policies.setApplication((String)row.get("application"));
		policies.setIdentifier((String)row.get("identifier"));
		policies.setLifecycle((String)row.get("lifecycle"));
		policies.setUseability((String)row.get("useability"));
		policies.setOtherdata((String)row.get("otherdata"));
		policies.setCreated((Timestamp)row.get("created"));		
		policies.setUpdated((Timestamp)row.get("updated"));			
		policies.setEpochtime((Long)row.get("epochtime"));		
		return policies;
	}
	
	/**
	 * Note values are returned with spaces converted to underscores (_).  This is because on the 
	 * CountPoliciesBreakdown results page, the values are used to form HTML ids on the page.   
	 */
	private CountPoliciesBreakdown populateCountPoliciesBreakdownFromResultSet(Map<String, Object> row) {
		CountPoliciesBreakdown policiesSelectedCountsBreakdown = new CountPoliciesBreakdown();
		policiesSelectedCountsBreakdown.setApplication(((String)row.get("application")).replace(" ", "_"));
		policiesSelectedCountsBreakdown.setLifecycle(((String)row.get("lifecycle")).replace(" ", "_"));
		policiesSelectedCountsBreakdown.setUseability(((String)row.get("useability")).replace(" ", "_"));
		policiesSelectedCountsBreakdown.setRowCount((Long)row.get("rowCount"));
		return policiesSelectedCountsBreakdown;
	}

	private AsyncMessageaAnalyzerResult populateAsyncMessageaAnalyzerResultFromSqlResultRow(Map<String, Object> row) {
		AsyncMessageaAnalyzerResult asyncMessageaAnalyzerResult = new AsyncMessageaAnalyzerResult();
		asyncMessageaAnalyzerResult.setApplication((String)row.get("application"));
		asyncMessageaAnalyzerResult.setIdentifier((String)row.get("identifier"));
		asyncMessageaAnalyzerResult.setUseability((String)row.get("useability"));
		asyncMessageaAnalyzerResult.setStarttm((Long)row.get("starttm"));
		asyncMessageaAnalyzerResult.setEndtm((Long)row.get("endtm"));	
		asyncMessageaAnalyzerResult.setDifferencetm((Long)row.get("differencetm"));			
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
