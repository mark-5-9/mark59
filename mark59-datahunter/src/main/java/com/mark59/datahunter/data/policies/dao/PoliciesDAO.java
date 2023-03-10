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
	SqlWithParms constructSelectPolicySql(PolicySelectionCriteria policySelect);

	SqlWithParms constructSelectPoliciesSql(PolicySelectionCriteria policySelectionCriteria);
	SqlWithParms constructCountPoliciesBreakdownSql(PolicySelectionCriteria policySelectionCriteria);
	SqlWithParms constructAsyncMessageaAnalyzerSql(PolicySelectionCriteria policySelectionCriteria);
	
	int runCountSql(SqlWithParms sqlWithParms);
	List<Policies> runSelectPolicieSql(SqlWithParms sqlWithParms);
	List<CountPoliciesBreakdown> runCountPoliciesBreakdownSql(SqlWithParms sqlWithParms);
	List<AsyncMessageaAnalyzerResult> runAsyncMessageaAnalyzerSql(SqlWithParms sqlWithParms);
	
	SqlWithParms constructInsertDataSql(Policies policies);
	SqlWithParms constructDeletePoliciesSql(PolicySelectionCriteria policySelectionCriteria);
	SqlWithParms constructDeleteMultiplePoliciesSql(PolicySelectionCriteria policySelectionCriteria);
	
	SqlWithParms constructUpdatePoliciesUseStateSql(UpdateUseStateAndEpochTime updateUse);
	SqlWithParms constructUpdatePolicyToUsedSql(Policies nextPolicy);

	List<AsyncMessageaAnalyzerResult> updateMultiplePoliciesUseState(List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList, 
			String toUseability);
	List<AsyncMessageaAnalyzerResult> updateMultiplePoliciesUseState(List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList, 
			String toUseability, int maxEntriesSqlUpdateStmt);

	int runDatabaseUpdateSql(SqlWithParms sqlWithParms);

	void getLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString, int timeout);
	void releaseLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString) throws SQLException;

	void getLock(String lockResouceString, int timeout);
	void releaseLock(String lockResouceString) throws SQLException;


}