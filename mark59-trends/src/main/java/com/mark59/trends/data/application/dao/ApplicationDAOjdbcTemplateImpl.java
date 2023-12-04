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

package com.mark59.trends.data.application.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.trends.application.AppConstantsTrends;
import com.mark59.trends.data.beans.Application;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

public class ApplicationDAOjdbcTemplateImpl implements ApplicationDAO 
{
	
	@Autowired  
	private DataSource dataSource;
	
	
	@Override
	public void insertApplication(Application  application) {

		String sql = "INSERT INTO APPLICATIONS (APPLICATION, ACTIVE, COMMENT) VALUES (?,?,?)";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql, application.getApplication(), application.getActive(), application.getComment());
	}

	
	@Override
	public void enforceRunCascadeDelete() {
		String sql = "DELETE FROM APPLICATIONS where APPLICATIONS.APPLICATION NOT IN (select distinct APPLICATION from RUNS) ";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}
		
	
	@Override
	public void updateApplication(Application application) {

		String sql = "UPDATE APPLICATIONS set ACTIVE = ? , COMMENT = ? where  APPLICATION = ?";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql, application.getActive(), application.getComment(), application.getApplication());
	}	
	
	
	/**
	 * Only Applications which have at least one Run are considered valid 
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public List<Application> findApplications(String appListSelector) {

		String sqlWhereClause = "";
		if (AppConstantsTrends.ACTIVE.equals(appListSelector)) {
			sqlWhereClause = " AND A.ACTIVE = 'Y' ";
		}
		
		String sql = "SELECT A.APPLICATION, A.ACTIVE, A.COMMENT FROM APPLICATIONS A, RUNS R "
				+ " WHERE A.APPLICATION = R.APPLICATION " + sqlWhereClause 
				+ " GROUP BY A.APPLICATION"
				+ " ORDER BY A.APPLICATION";
		
		List<Application> applications  = new ArrayList<>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map row : rows) {
			Application application = new Application();
			application.setApplication((String) row.get("APPLICATION"));
			application.setActive((String) row.get("ACTIVE"));
			application.setComment((String) row.get("COMMENT"));
			applications.add(application);
		}
		return applications;
	}


	/**
	 * Only Applications which have at least one Run are considered valid 
	 */
	@Override
	public Application findApplication(String applicationId) {

		String sql = "SELECT A.APPLICATION, A.ACTIVE, A.COMMENT FROM APPLICATIONS A, RUNS R"
				+ " WHERE A.APPLICATION = R.APPLICATION " 
				+ " AND A.APPLICATION = :applicationId " 
				+ " GROUP BY A.APPLICATION";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("applicationId", applicationId);	

		Application application = new Application();
//		System.out.println("runsDao findRuns : " + runsListSelectionSQL +  UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
		if (rows.isEmpty()){
			// System.out.println("application " + applicationId + " does not exist on application and runs tables " );
			application.setApplication("");
		} else {		
			Map<String, Object> row = rows.get(0);
			application.setApplication((String)row.get("APPLICATION"));
			application.setActive((String)row.get("ACTIVE"));
			application.setComment((String)row.get("COMMENT"));
		}
		return  application;
	}


	@Override
	public void duplicateEntireApplication(String fromApp, String toApp){
		System.out.println("duplicateEntireApplication: fromApp " + fromApp + ", toApp " + toApp);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		String sql = "DELETE FROM APPLICATIONS WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp);	
		
		sql = "INSERT INTO APPLICATIONS SELECT ?, ACTIVE, COMMENT FROM APPLICATIONS WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp, fromApp);
		
		sql = "DELETE FROM RUNS WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp);

		sql = "INSERT INTO RUNS SELECT ?, RUN_TIME, IS_RUN_IGNORED, RUN_REFERENCE, PERIOD, DURATION, BASELINE_RUN, COMMENT "
				+ "FROM RUNS WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp, fromApp);		
	
		sql = "DELETE FROM TRANSACTION WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp);

		sql = "INSERT INTO TRANSACTION SELECT ?, RUN_TIME, TXN_ID, TXN_TYPE, IS_CDP_TXN, "
				+ "TXN_MINIMUM, TXN_AVERAGE, TXN_MEDIAN, TXN_MAXIMUM, TXN_STD_DEVIATION, TXN_90TH, TXN_95TH, TXN_99TH, "
				+ "TXN_PASS, TXN_FAIL, TXN_STOP, TXN_FIRST, TXN_LAST, TXN_SUM, TXN_DELAY "
				+ "FROM TRANSACTION WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp, fromApp);		
		
		sql = "DELETE FROM SLA WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp);		
		
		sql = "INSERT INTO SLA "
				+ "SELECT TXN_ID, IS_CDP_TXN, ?, IS_TXN_IGNORED, SLA_90TH_RESPONSE, SLA_95TH_RESPONSE, SLA_99TH_RESPONSE, "
				+ "SLA_PASS_COUNT, SLA_PASS_COUNT_VARIANCE_PERCENT, SLA_FAIL_COUNT, SLA_FAIL_PERCENT, "
				+ "TXN_DELAY, XTRA_NUM, XTRA_INT, SLA_REF_URL, COMMENT, IS_ACTIVE "
				+ "FROM SLA WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp, fromApp);		
		
		sql = "DELETE FROM METRICSLA WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp);		
		
		sql = "INSERT INTO METRICSLA "
				+ "SELECT ?, METRIC_NAME, METRIC_TXN_TYPE, VALUE_DERIVATION, SLA_MIN, SLA_MAX, IS_ACTIVE, COMMENT "
				+ "FROM METRICSLA WHERE APPLICATION = ?";
		jdbcTemplate.update(sql, toApp, fromApp);
		
		System.out.println("duplicateEntireApplication: fromApp " + fromApp + ", toApp " + toApp + " completed") ;
	}
	
}
