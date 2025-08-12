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
import java.util.stream.Stream;

import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerResult;
import com.mark59.datahunter.model.CountPoliciesBreakdown;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.PolicySelectionFilter;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;
import com.mark59.datahunter.pojo.ReindexResult;
import com.mark59.datahunter.pojo.ValidReuseIxPojo;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public interface PoliciesDAO 
{
	public final String SELECT_POLICY_COUNTS  = " count(*)  as counter ";
	public final String SELECT_POLICY_COLUMNS = " application, identifier, lifecycle, useability, otherdata, created, updated, epochtime ";
	
	SqlWithParms constructSelectPolicySql(PolicySelectionCriteria policySelect);
	SqlWithParms constructSelectPolicySql(Policies policies);

	SqlWithParms constructSelectPoliciesFilterSql(PolicySelectionFilter PolicySelectionFilter);
	SqlWithParms constructSelectPoliciesFilterSql(PolicySelectionFilter policySelectionFilter, boolean applyLimit);	
	SqlWithParms constructSelectNextPolicySql(PolicySelectionCriteria policySelect);
	SqlWithParms constructCountPoliciesSql(PolicySelectionCriteria policySelect);
	SqlWithParms constructCountPoliciesBreakdownSql(PolicySelectionCriteria policySelectionCriteria);
	SqlWithParms constructAsyncMessageaAnalyzerSql(PolicySelectionCriteria policySelectionCriteria);
	
	int runCountSql(SqlWithParms sqlWithParms);
	List<Policies> runSelectPolicieSql(SqlWithParms sqlWithParms);
	Stream<Policies> runStreamPolicieSql(SqlWithParms sqlWithParms);
	List<CountPoliciesBreakdown> runCountPoliciesBreakdownSql(SqlWithParms sqlWithParms);
	List<AsyncMessageaAnalyzerResult> runAsyncMessageaAnalyzerSql(SqlWithParms sqlWithParms);
	
	SqlWithParms constructInsertDataSql(Policies policies);
	
	SqlWithParms countValidIndexedIdsInExpectedRange(PolicySelectionCriteria policySelect, int ixCount);
	SqlWithParms countNonReusableIdsForReusableIndexedData(String application, String lifecycle);
	SqlWithParms constructCollectDataOutOfExpectedIxRangeSql(String application, String lifecycle, int policyCount);

	void insertMultiple(List<Policies> policiesList);
	SqlWithParms constructDeletePoliciesSql(PolicySelectionCriteria policySelectionCriteria);
	SqlWithParms constructDeleteMultiplePoliciesSql(PolicySelectionFilter policySelectionFilter);

	SqlWithParms constructUpdatePoliciesSql(Policies policies);
	SqlWithParms constructUpdatePolicyToUsedSql(Policies nextPolicy);
	SqlWithParms constructUpdatePoliciesUseStateSql(UpdateUseStateAndEpochTime updateUse);

	List<AsyncMessageaAnalyzerResult> updateMultiplePoliciesUseState(List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList, 
			String toUseability);
	List<AsyncMessageaAnalyzerResult> updateMultiplePoliciesUseState(List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList, 
			String toUseability, int maxEntriesSqlUpdateStmt);

	int runDatabaseUpdateSql(SqlWithParms sqlWithParms);

	void getLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString, int timeout);
	void releaseLock(JdbcTemplate singleConnectionJdbcTemplate, String lockResouceString) throws SQLException;

	void getLock(String lockResouceString, int timeout);
	void releaseLock(String lockResouceString) throws SQLException;
	
	int updateIndexedRowCounter(Policies policies, int indexedId);
	ReindexResult reindexReusableIndexed(String application, String lifecycle);
	ValidReuseIxPojo validateReusableIndexed(Policies policies);
	ValidReuseIxPojo validateReusableIndexed(PolicySelectionCriteria policySelect);


}