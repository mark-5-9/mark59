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
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerResult;
import com.mark59.datahunter.model.CountPoliciesBreakdown;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public interface PoliciesDAO 
{
	public String constructSelectPolicySql(PolicySelectionCriteria policySelect);

	public String constructSelectPoliciesSql(PolicySelectionCriteria policySelectionCriteria);		
	public String constructCountPoliciesBreakdownSql(PolicySelectionCriteria policySelectionCriteria);
	public String constructAsyncMessageaAnalyzerSql(PolicySelectionCriteria policySelectionCriteria);
	
	public int runCountSql(String sql);	
	public List<Policies> runSelectPolicieSql(String sql);		
	public List<CountPoliciesBreakdown> runCountPoliciesBreakdownSql(String sql);
	public List<AsyncMessageaAnalyzerResult> runAsyncMessageaAnalyzerSql(String sql);
	
	public String constructInsertDataSql(Policies policies);	
	public String constructDeletePoliciesSql(PolicySelectionCriteria policySelectionCriteria);
	public String constructDeleteMultiplePoliciesSql(PolicySelectionCriteria policySelectionCriteria);
	
	public String constructUpdatePoliciesUseStateSql(UpdateUseStateAndEpochTime updateUse);
	public String constructUpdatePolicyToUsedSql(Policies nextPolicy);

	public int runDatabaseUpdateSql(String sql);

	public void getLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString, int timeout);
	public void releaseLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString) throws SQLException;

	public void getLock(String lockResouceString, int timeout);
	public void releaseLock(String lockResouceString) throws SQLException;
}