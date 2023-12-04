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

package com.mark59.trends.data.run.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.trends.application.AppConstantsTrends;
import com.mark59.trends.application.UtilsTrends;
import com.mark59.trends.data.application.dao.ApplicationDAO;
import com.mark59.trends.data.beans.Application;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.transaction.dao.TransactionDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class RunDAOjdbcTemplateImpl implements RunDAO
{
	@Autowired  
	private DataSource dataSource;
	
	@Autowired
	ApplicationDAO applicationDAO; 	
	
	@Autowired
	TransactionDAO transactionDAO; 	
	
	
	@Override
	public void insertRun(Run run) {
		String sql = "INSERT INTO RUNS "
				+ "(APPLICATION, RUN_TIME, IS_RUN_IGNORED, RUN_REFERENCE, PERIOD, DURATION, BASELINE_RUN, COMMENT) VALUES (?,?,?,?,?,?,?,?)";
//		System.out.println("performing : " + sql + " vars: runTime " + run.getRunTime() + ", period " + run.getPeriod() );
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql, run.getApplication(), run.getRunTime(), run.getIsRunIgnored(), run.getRunReference(),
				run.getPeriod(), run.getDuration(), run.getBaselineRun(), run.getComment());

		if (StringUtils.isBlank( applicationDAO.findApplication(run.getApplication()).getApplication())) {
//			System.out.println("RunDAO insertRun creating an application table entry for " + run.getApplication() );
			Application application = new Application();
			application.setApplication(run.getApplication());
			application.setActive("Y");
			application.setComment("");
			applicationDAO.insertApplication(application);
		}
	}

		
	@Override
	public void updateRun(Run run) {

		String sql = "UPDATE RUNS set IS_RUN_IGNORED = ?, RUN_REFERENCE = ?, PERIOD = ?, DURATION = ?, BASELINE_RUN = ?, COMMENT = ? "
				+ "where  APPLICATION = ? and  RUN_TIME = ? ";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql,
				run.getIsRunIgnored(), run.getRunReference(), run.getPeriod(), run.getDuration(), run.getBaselineRun(),
				run.getComment(),run.getApplication(), run.getRunTime());
	}	
	
	
	@Override
	public void deleteRun(String application, String runTime) {
		
		transactionDAO.deleteAllForRun(application, runTime);
		
		String sql = "delete from RUNS where APPLICATION= :application and RUN_TIME= :runTime ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("runTime", runTime);		
		
//		System.out.println("runDao deleteRun : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
		
		applicationDAO.enforceRunCascadeDelete(); 		
	}	

	
	@Override
	public void deleteAllForApplication(String application) {
		
		transactionDAO.deleteAllForApplication(application);
		
		String sql = "delete from RUNS where APPLICATION = :application ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);		
		
//		System.out.println("runDao deleteAllForApplication : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
		
		applicationDAO.enforceRunCascadeDelete(); 		
	}	

	
	@Override
	public List<String> findApplications(){

		String sql = "SELECT distinct APPLICATION FROM RUNS order by APPLICATION ";
		
		List<String> applications = new ArrayList<>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> row : rows) {
			applications.add( (String)row.get("APPLICATION") );
//			System.out.println("RunDAO findApplications : " + row.get("APPLICATION")  ) ;
		}	
		return  applications;
	}
	
	
	@Override
	public List<String> findApplications(String appListSelector){

		if (AppConstantsTrends.ACTIVE.equals(appListSelector)){ 
			
			String sql = "SELECT distinct APPLICATION FROM RUNS where APPLICATION in (select APPLICATION from APPLICATIONS where ACTIVE = 'Y')"
					+ " order by APPLICATION " ;
	
			List<String> applications = new ArrayList<>();
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
			for (Map<String, Object> row : rows) {
				applications.add( (String)row.get("APPLICATION") );
//				System.out.println("populating application in appListSelector drop down list : " + row.get("APPLICATION")  ) ;
			}	
			return  applications;
		
		} else {

			return  findApplications();			
		}
	}
	
	
	@Override
	public List<String> findApplicationsWithBaselines() {

		String sql = "SELECT distinct APPLICATION FROM RUNS where BASELINE_RUN = 'Y' order by APPLICATION ";

		List<String> applications = new ArrayList<>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map<String, Object> row : rows) {
			applications.add( (String)row.get("APPLICATION") );
//			System.out.println("RunDAO findApplicationsWithBaselines : " + row.get("APPLICATION")  ) ;
		}	
		return  applications;

	}
	
	
	@Override
	public String findLastRunDate(String application) {
		List<String> runDates = findRunDates(application);
		if (runDates.isEmpty()) {
			return null; 
		} else {
			return runDates.get(0); 
		}
	}	
	

	/** 
	 * Will pick all runs for a given application - that have any transactions.  Runs marked as 'ignore run on graph' will 
	 * also be returned.<br>
	 * Note this does not mean every graph will have transactions for every run.  For example, a run may not of captured
	 * any Server statistics    
	 */
	@Override
	public List<String> findRunDates(String application){
		List<String> runDates = new ArrayList<>();

		String sql = runsSqlNamedParms("%","", false, "" );  
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);	

//		System.out.println(" findRunDates : " + sql +  UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
		for (Map<String, Object> row : rows) {
			runDates.add( (String)row.get("RUN_TIME") );
//			System.out.println("  RunDao findRunDates : " + row.get("RUN_TIME")  ) ;
		}	
		return  runDates;
	}


	@Override
	public Run findRun(String application, String runTime){
		
		String runSQL = "select APPLICATION, RUN_TIME, IS_RUN_IGNORED, RUN_REFERENCE, PERIOD, DURATION, BASELINE_RUN, COMMENT from RUNS " +
		                "   where APPLICATION = :application " +
		                "     and RUN_TIME    = :runTime" ;
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("runTime", runTime);	

//		System.out.println(" findRunDates : " + runSQL +  UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(runSQL, sqlparameters);
		
		if (rows.isEmpty()){
			System.out.println("no runs have been selected for " + application + " runTime " + runTime + " !! " );
			return null;
		}
		Map<String, Object> row = rows.get(0);
		
		Run run = new Run();
		run.setApplication((String)row.get("APPLICATION"));
		run.setRunTime((String)row.get("RUN_TIME"));
		run.setIsRunIgnored((String)row.get("IS_RUN_IGNORED"));
		run.setRunReference((String)row.get("RUN_REFERENCE"));
		run.setPeriod((String)row.get("PERIOD"));
		run.setDuration((String)row.get("DURATION"));		
		run.setBaselineRun((String)row.get("BASELINE_RUN"));
		run.setComment((String)row.get("COMMENT"));
		return  run;
	}

	

	@Override
	public List<Run> findRuns(String application){

		List<Run> runsList = new ArrayList<>();
		String runsListSelectionSQL        = "select APPLICATION, RUN_TIME, IS_RUN_IGNORED, RUN_REFERENCE, PERIOD, DURATION, BASELINE_RUN, COMMENT from RUNS ";
		String runsListSelectionSQLwithApp = "   where APPLICATION = :application order by RUN_TIME DESC ";  
		String runsListSelectionSQLnoApp   = "   order by APPLICATION  ASC, RUN_TIME DESC "; 		

		if ( StringUtils.isEmpty(application)) {
			runsListSelectionSQL = runsListSelectionSQL + runsListSelectionSQLnoApp;
		} else {
			runsListSelectionSQL = runsListSelectionSQL + runsListSelectionSQLwithApp;
		}

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);	

//		System.out.println("runsDao findRuns : " + runsListSelectionSQL +  UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(runsListSelectionSQL, sqlparameters);
		
		for (Map<String, Object> row : rows) {
			Run run = new Run();
			run.setApplication((String)row.get("APPLICATION"));
			run.setRunTime((String)row.get("RUN_TIME"));
			run.setIsRunIgnored((String)row.get("IS_RUN_IGNORED"));			
			run.setRunReference((String)row.get("RUN_REFERENCE"));
			run.setPeriod((String)row.get("PERIOD"));
			run.setDuration((String)row.get("DURATION"));				
			run.setBaselineRun((String)row.get("BASELINE_RUN"));
			run.setComment((String)row.get("COMMENT"));
			
			runsList.add(run);
			//System.out.println("values from runDAOjdbcTemplateImpl.run_times  : " + row.get("RUN_TIME")  ) ;
		}	
		return  runsList;
	}


	@Override
	public Run findLastBaselineRun(String application) {
		
		String runsListSelectionSQL = "select APPLICATION, RUN_TIME, IS_RUN_IGNORED, RUN_REFERENCE, PERIOD, DURATION, BASELINE_RUN, COMMENT"
									+ " from RUNS where APPLICATION = :application AND BASELINE_RUN = 'Y' order by RUN_TIME DESC      ";  
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);	

//		System.out.println("runsDao findLastBaselineRun : " + runsListSelectionSQL +  UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(runsListSelectionSQL, sqlparameters);
		
		if (rows.isEmpty()){
//			System.out.println("no baseline runs for " + application );
			return null;
		}
		Map<String, Object> row = rows.get(0);
		
		Run run = new Run();
		run.setApplication((String)row.get("APPLICATION"));
		run.setRunTime((String)row.get("RUN_TIME"));
		run.setIsRunIgnored((String)row.get("IS_RUN_IGNORED"));
		run.setRunReference((String)row.get("RUN_REFERENCE"));
		run.setPeriod((String)row.get("PERIOD"));
		run.setDuration((String)row.get("DURATION"));		
		run.setBaselineRun((String)row.get("BASELINE_RUN"));
		run.setComment((String)row.get("COMMENT"));
		return  run;
	}
	

	@Override
	public String determineRunDatesToGraph(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike, boolean isManuallySelectRuns, 
			String chosenRuns, boolean isUseRawRunSQL, String rawRunTimeSelectionSQL,  String maxRun, String maxBaselineRun){
		
		String runDatesToGraphList;
		
		if (isManuallySelectRuns){  //user may of typed in dates out-of-order   
			String[] sortedChosenRunTimesArray = UtilsTrends.commaDelimStringToSortedStringArray(chosenRuns, Collections.reverseOrder());  
			ArrayList<String> sortedChosenRunTimesList = new ArrayList<>(Arrays.asList(sortedChosenRunTimesArray));
			// dud run typed in - remove it ...
			sortedChosenRunTimesList.removeIf(runtime -> findRun(application, runtime) == null);
			runDatesToGraphList = UtilsTrends.stringListToCommaDelimString(sortedChosenRunTimesList);
			
		} else {

			List<String> runDates = findRunDatesWhenRunsNotManuallyChosen(application, sqlSelectRunLike, reqSqlSelectRunNotLike, 
					integerValueOfMaxRun(maxRun), integerValueOfMaxBaselineRun(maxBaselineRun), isUseRawRunSQL, rawRunTimeSelectionSQL );
			runDatesToGraphList = UtilsTrends.stringListToCommaDelimString(runDates);
		}
		return runDatesToGraphList;
	}

	
	private int integerValueOfMaxRun(String maxRunDropdownValue){
		if (maxRunDropdownValue == null)
			return 10;
			if (maxRunDropdownValue.equals(AppConstantsTrends.DEFAULT_10 ) )
			return 10;
		if (maxRunDropdownValue.equals(AppConstantsTrends.ALL ) )
			return 99;
		return Integer.parseInt(maxRunDropdownValue);
	}


	private int integerValueOfMaxBaselineRun(String maxBaselineRunDropdownValue){
		if (maxBaselineRunDropdownValue == null )
			return 1;
		if (maxBaselineRunDropdownValue.equals(AppConstantsTrends.DEFAULT_01 ) )
			return 1;		
		if (maxBaselineRunDropdownValue.equals(AppConstantsTrends.ALL ) )
			return 99;
		return Integer.parseInt(maxBaselineRunDropdownValue);
	}


	private List<String> findRunDatesWhenRunsNotManuallyChosen(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike, 
			int numRunsDisplayed, int numBaselineRunsDisplayed, boolean useRawRunSQL, String rawRunTimeSelectionSQL ) {
		// pick up to the maximum requested number of most recent runs + most recent baseline (don't include ignore on graph runs) 			
		
		List<String> runTimes = new ArrayList<>();

		String sql = runsSqlNamedParms(sqlSelectRunLike, reqSqlSelectRunNotLike, useRawRunSQL, rawRunTimeSelectionSQL);
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("sqlSelectRunLike", "%" + sqlSelectRunLike.replace(".","").replace("T","").replace(":","").trim() + "%" )
				.addValue("reqSqlSelectRunNotLike", "%" + reqSqlSelectRunNotLike.replace(".","").replace("T","").replace(":","").trim() + "%" );

//		System.out.println(" findRunDatesWhenRunsNotManuallyChosen : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);
		
		boolean gotAllRequestRecentRuns     = false; 
		boolean gotAllRequestedBaselineRuns = false; 
		
		int numRunsFound = 0;
		int numBaseLineRunsFound = 0;
		
		for (int i = 0;  (i < rows.size())  &&  !(gotAllRequestRecentRuns && gotAllRequestedBaselineRuns);  i++) {
		
			Map<String, Object> row = rows.get(i);
			
			if ( ! "Y".equalsIgnoreCase((String)row.get("IS_RUN_IGNORED"))){
			
				if ( "Y".equalsIgnoreCase((String)row.get("BASELINE_RUN"))  &&  numBaseLineRunsFound < numBaselineRunsDisplayed ){
					
					numBaseLineRunsFound++;
					runTimes.add( (String)row.get("RUN_TIME") );
//					System.out.println("populating run_times for trending page (a baseline found): " + row.get("RUN_TIME")  ) ;	
					if (numBaseLineRunsFound >= numBaselineRunsDisplayed ){
						gotAllRequestedBaselineRuns = true;
					}
					
				} else {  // populate the recent runs. May include a baseline as a run (past the requested number of baselines)..  	
					
					if ( numRunsFound < numRunsDisplayed ){
						numRunsFound++;
						runTimes.add( (String)row.get("RUN_TIME") );
//						System.out.println("populating run_times for trending page (a std run found): " + row.get("RUN_TIME")  ) ;	
						if (numRunsFound >= numRunsDisplayed ){
							gotAllRequestRecentRuns = true;
						}					
					}	
					
				} //if  BASELINE_RUN
			} // if ! IS_RUN_IGNORED
		} //for rows
		
		return  runTimes;
	}

	
	/** 
	 * Pick all runs for a given application, that have any transactions, and that also satisfy any 'Like' and
	 * 'not Like' condition (runs marked as 'ignore run on graph' can be included in the results)<br> 
	 * Note this does not mean every graph will have transactions for every run.  For example, a run may not of captured
	 * any Server statistics 
	 */
	@Override
	public String runsSQL(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike, 
			boolean useRawRunSQL, String rawRunTimeSelectionSQL){	
	
		String sql = runsSqlNamedParms(sqlSelectRunLike, reqSqlSelectRunNotLike, useRawRunSQL, rawRunTimeSelectionSQL);

		sql = sql.replace(":application", "'" + application + "' ")
				 .replace(":sqlSelectRunLike", "'%" +  sqlSelectRunLike.replace(".","").replace("T","").replace(":","").trim() + "%'" )
				 .replace(":reqSqlSelectRunNotLike", "'%" + reqSqlSelectRunNotLike.replace(".","").replace("T","").replace(":","").trim() + "%'" );

//		System.out.println("RunAOjdbcTemplateImpl.getRunTimeSelectionSQL 'raw'sql : \n" + sql );
		return sql;
	}
	
	
	private String runsSqlNamedParms(String sqlSelectRunLike, String reqSqlSelectRunNotLike, boolean useRawRunSQL, String rawRunTimeSelectionSQL){	
		String sql;
		if (useRawRunSQL){
			sql = rawRunTimeSelectionSQL; 
		
		} else {
			sql = "select distinct r.RUN_TIME, r.BASELINE_RUN, r.IS_RUN_IGNORED from "
				 + " RUNS r, "
				 + " TRANSACTION t "
				 + "   where r.APPLICATION = :application "  
				 + "     and r.APPLICATION = t.APPLICATION "
				 + "     and r.RUN_TIME    = t.RUN_TIME ";
			
			if (!"%".equals(sqlSelectRunLike) ){
				sql = sql + " AND r.RUN_TIME LIKE :sqlSelectRunLike ";  
			}
			if (StringUtils.isNotBlank(reqSqlSelectRunNotLike)){
				sql = sql  + " AND NOT ( r.RUN_TIME LIKE :reqSqlSelectRunNotLike ) ";  
			}		
			sql = sql  + "   order by r.RUN_TIME DESC "; 
		}	
		return  sql;
	}	
	
}
