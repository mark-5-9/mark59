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
public interface PoliciesDAO 
{
	public SqlWithParms constructSelectPolicySql(PolicySelectionCriteria policySelect);

	public SqlWithParms constructSelectPoliciesSql(PolicySelectionCriteria policySelectionCriteria);		
	public SqlWithParms constructCountPoliciesBreakdownSql(PolicySelectionCriteria policySelectionCriteria);
	public SqlWithParms constructAsyncMessageaAnalyzerSql(PolicySelectionCriteria policySelectionCriteria);
	
	public int runCountSql(SqlWithParms sqlWithParms);	
	public List<Policies> runSelectPolicieSql(SqlWithParms sqlWithParms);		
	public List<CountPoliciesBreakdown> runCountPoliciesBreakdownSql(SqlWithParms sqlWithParms);
	public List<AsyncMessageaAnalyzerResult> runAsyncMessageaAnalyzerSql(SqlWithParms sqlWithParms);
	
	public SqlWithParms constructInsertDataSql(Policies policies);	
	public SqlWithParms constructDeletePoliciesSql(PolicySelectionCriteria policySelectionCriteria);
	public SqlWithParms constructDeleteMultiplePoliciesSql(PolicySelectionCriteria policySelectionCriteria);
	
	public SqlWithParms constructUpdatePoliciesUseStateSql(UpdateUseStateAndEpochTime updateUse);
	public SqlWithParms constructUpdatePolicyToUsedSql(Policies nextPolicy);

	public int runDatabaseUpdateSql(SqlWithParms sqlWithParms);

	public void getLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString, int timeout);
	public void releaseLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString) throws SQLException;

	public void getLock(String lockResouceString, int timeout);
	public void releaseLock(String lockResouceString) throws SQLException;
}