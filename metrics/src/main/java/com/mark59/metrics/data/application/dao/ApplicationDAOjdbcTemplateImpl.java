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

package com.mark59.metrics.data.application.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.Application;

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
		jdbcTemplate.update(sql,new Object[] {	application.getApplication(), application.getActive(), application.getComment() });
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
		jdbcTemplate.update(sql,new Object[] {application.getActive(), application.getComment(), application.getApplication()});
	}	
	

	@Override
	public List<Application> findApplications() {
		return findApplications("");
	}
	
	
	@Override
	@SuppressWarnings("rawtypes")
	public List<Application> findApplications(String appListSelector) {

		String sqlWhereClause = "";
		if (AppConstantsMetrics.ACTIVE.equals(appListSelector)) {
			sqlWhereClause = " where ACTIVE = 'Y' ";
		}
		
		String sql = "SELECT APPLICATION, ACTIVE, COMMENT FROM APPLICATIONS " + sqlWhereClause + "order by APPLICATION ";
		
		List<Application> applications  = new ArrayList<Application>();
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


	@Override
	public Application findApplication(String applicationId) {

		String sql = "select APPLICATION, ACTIVE, COMMENT from APPLICATIONS where APPLICATION = :applicationId " ;
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("applicationId", applicationId);	

		Application application = new Application();
//		System.out.println("runsDao findRuns : " + runsListSelectionSQL +  UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
		if (rows.isEmpty()){
			System.out.println("application " + applicationId + " does not exist on application table " );
			application.setApplication("");
		} else {		
			Map<String, Object> row = rows.get(0);
			application.setApplication((String)row.get("APPLICATION"));
			application.setActive((String)row.get("ACTIVE"));
			application.setComment((String)row.get("COMMENT"));
		}
		return  application;
	}
	
}
