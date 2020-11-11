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

package com.mark59.metrics.data.run.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.application.UtilsMetrics;
import com.mark59.metrics.data.application.dao.ApplicationDAO;
import com.mark59.metrics.data.beans.Application;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;

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
				+ "(APPLICATION, RUN_TIME, LRS_FILENAME, PERIOD, DURATION, BASELINE_RUN, COMMENT) VALUES (?,?,?,?,?,?,?)";
//		System.out.println("performing : " + sql + " vars: runTime " + run.getRunTime() + ", period " + run.getPeriod() );
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,new Object[] { run.getApplication(), run.getRunTime(), run.getRunReference(), 
												run.getPeriod(),  run.getDuration(), run.getBaselineRun(),  run.getComment() });

		if (StringUtils.isBlank( applicationDAO.findApplication(run.getApplication()).getApplication())) {
			System.out.println("RunDAO insertRun creating an application table entry for " + run.getApplication() );
			Application application = new Application();
			application.setApplication(run.getApplication());
			application.setActive("Y");
			application.setComment("");
			applicationDAO.insertApplication(application);
		}
	}

	
	
	@Override
	public void updateRun(Run run) {

		String sql = "UPDATE RUNS set LRS_FILENAME = ? , PERIOD = ?, DURATION = ?, BASELINE_RUN = ?, COMMENT = ? "
				+ "where  APPLICATION = ? and  RUN_TIME = ? ";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);


		jdbcTemplate.update(sql,
				new Object[] {run.getRunReference(), run.getPeriod(),  run.getDuration(), run.getBaselineRun(),  run.getComment(),
				 		run.getApplication(), run.getRunTime(),});
	}	
	
	
	
	
	@Override
	public void deleteRun(String application, String runTime) {
		
		transactionDAO.deleteAllForRun(application, runTime);
		
		String sql = "delete from RUNS where APPLICATION='" + application	+ "' and RUN_TIME='" + runTime + "'";
		System.out.println("deleteRun sql: " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
		
		applicationDAO.enforceRunCascadeDelete(); 		
				
	}	

	
	@Override
	public void deleteMultiple(String applicationSelectionCriteria,	String runTimeSelectionCriteria) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		String sql = "delete from TRANSACTION where APPLICATION " + applicationSelectionCriteria	+ " and RUN_TIME " + runTimeSelectionCriteria;
		System.out.println("deleteMultiple performing : " + sql);
		jdbcTemplate.update(sql);		
		
		sql = "delete from RUNS where APPLICATION " + applicationSelectionCriteria	+ " and RUN_TIME " + runTimeSelectionCriteria;
		System.out.println("deleteMultiple performing : " + sql);
		jdbcTemplate.update(sql);

		applicationDAO.enforceRunCascadeDelete(); 	
	}
	
	
	@Override
	public void deleteAllForApplication(String application) {
		
		transactionDAO.deleteAllForApplication(application);
		
		String sql = "delete from RUNS where APPLICATION='" + application	+ "' ";
		System.out.println("delete all runs for : " + application);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
		
		applicationDAO.enforceRunCascadeDelete(); 		
	}	

	
	@Override
	@SuppressWarnings("rawtypes")
	public List<String> findApplications(){

		String sql = "SELECT distinct APPLICATION FROM RUNS order by APPLICATION ";
		
		List<String> applications = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map row : rows) {
			applications.add( (String)row.get("APPLICATION") );
//			System.out.println("RunDAO findApplications : " + row.get("APPLICATION")  ) ;
		}	
		return  applications;
	}
	

	
	@Override
	@SuppressWarnings("rawtypes")
	public List<String> findApplications(String appListSelector){

		if (AppConstantsMetrics.ACTIVE.equals(appListSelector)){ 
			
			String sql = "SELECT distinct APPLICATION FROM RUNS where APPLICATION in (select APPLICATION from APPLICATIONS where ACTIVE = 'Y')"
					+ " order by APPLICATION " ;
	
			List<String> applications = new ArrayList<String>();
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
			for (Map row : rows) {
				applications.add( (String)row.get("APPLICATION") );
//				System.out.println("populating application in appListSelector drop down list : " + row.get("APPLICATION")  ) ;
			}	
			return  applications;
		
		} else {

			return  findApplications();			
		}
	}
	
	

	@Override
	@SuppressWarnings("rawtypes")	
	public List<String> findApplicationsWithBaselines() {

		String sql = "SELECT distinct APPLICATION FROM RUNS where BASELINE_RUN = 'Y' order by APPLICATION ";

		List<String> applications = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map row : rows) {
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
	

	
	@Override
	@SuppressWarnings("rawtypes")
	public List<String> findRunDates(String application){
		
		List<String> runDates = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(getRunTimeSelectionSQL(application, "",""));
		
		for (Map row : rows) {
			runDates.add( (String)row.get("RUN_TIME") );
//			System.out.println("populating run_times for trending page : " + row.get("RUN_TIME")  ) ;
		}	
		return  runDates;
	}


	@Override
	@SuppressWarnings("rawtypes")
	public Run findRun(String application, String runTime){

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String runSQL = "select APPLICATION, RUN_TIME, LRS_FILENAME, PERIOD, DURATION, BASELINE_RUN, COMMENT from RUNS " +
		                "   where APPLICATION = '" + application + "'" +
		                "     and RUN_TIME    = '" + runTime + "'" ;
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(runSQL);
		
		if (rows.isEmpty()){
			System.out.println("no runs have been selected for " + application + " runTime " + runTime + " !! " );
			return null;
		}
		Map row = rows.get(0);
		
		Run runs = new Run();
		runs.setApplication((String)row.get("APPLICATION"));
		runs.setRunTime((String)row.get("RUN_TIME"));
		runs.setRunReference((String)row.get("LRS_FILENAME"));
		runs.setPeriod((String)row.get("PERIOD"));
		runs.setDuration((String)row.get("DURATION"));		
		runs.setBaselineRun((String)row.get("BASELINE_RUN"));
		runs.setComment((String)row.get("COMMENT"));
		return  runs;
	}

	

	@Override
	@SuppressWarnings("rawtypes")
	public List<Run> findRuns(String application){

		List<Run> runsList = new ArrayList<Run>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String runsListSelectionSQL        = "select APPLICATION, RUN_TIME, LRS_FILENAME, PERIOD, DURATION, BASELINE_RUN, COMMENT from RUNS ";
		String runsListSelectionSQLwithApp = "   where APPLICATION = '" + application + "' order by RUN_TIME DESC      ";  
		
		String runsListSelectionSQLnoApp   = "   order by APPLICATION  ASC, RUN_TIME DESC "; 		

		if ( StringUtils.isEmpty(application)) {
			runsListSelectionSQL = runsListSelectionSQL + runsListSelectionSQLnoApp;
		} else {
			runsListSelectionSQL = runsListSelectionSQL + runsListSelectionSQLwithApp;
		}

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(runsListSelectionSQL);
		
		for (Map row : rows) {
			Run runs = new Run();
			runs.setApplication((String)row.get("APPLICATION"));
			runs.setRunTime((String)row.get("RUN_TIME"));
			runs.setRunReference((String)row.get("LRS_FILENAME"));
			runs.setPeriod((String)row.get("PERIOD"));
			runs.setDuration((String)row.get("DURATION"));				
			runs.setBaselineRun((String)row.get("BASELINE_RUN"));
			runs.setComment((String)row.get("COMMENT"));
			
			runsList.add(runs);
			//System.out.println("values from runDAOjdbcTemplateImpl.run_times  : " + row.get("RUN_TIME")  ) ;
		}	
		return  runsList;
	}

	

	@Override
	@SuppressWarnings("rawtypes")	
	public Run findLastBaselineRun(String application) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String runsListSelectionSQL = "select APPLICATION, RUN_TIME, LRS_FILENAME, PERIOD, DURATION, BASELINE_RUN, COMMENT from RUNS "+
									  "  where APPLICATION = '" + application + "' AND BASELINE_RUN = 'Y' order by RUN_TIME DESC      ";  

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(runsListSelectionSQL);
		
		if (rows.isEmpty()){
//			System.out.println("no baseline runs for " + application );
			return null;
		}
		Map row = rows.get(0);
		
		Run run = new Run();
		run.setApplication((String)row.get("APPLICATION"));
		run.setRunTime((String)row.get("RUN_TIME"));
		run.setRunReference((String)row.get("LRS_FILENAME"));
		run.setPeriod((String)row.get("PERIOD"));
		run.setDuration((String)row.get("DURATION"));		
		run.setBaselineRun((String)row.get("BASELINE_RUN"));
		run.setComment((String)row.get("COMMENT"));
		return  run;
	}
	
	

	@Override
	public String determineRunDatesToGraph(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike, boolean isManuallySelectRuns, String chosenRuns, boolean isUseRawRunSQL, String runTimeSelectionSQL,  String maxRun, String maxBaselineRun){
		String runDatesToGraphList;
		if (isManuallySelectRuns){  //user may of typed in dates out-of-order   
			String[] sortedChosenRunTimesArray = UtilsMetrics.commaDelimStringToSortedStringArray(chosenRuns, Collections.reverseOrder());  
			ArrayList<String> sortedChosenRunTimesList = new ArrayList<String> (Arrays.asList(sortedChosenRunTimesArray));
			Iterator<String> it = sortedChosenRunTimesList.iterator();
			while (it.hasNext()) {
				String runtime = it.next();
				if ( findRun(application, runtime) == null ) {	// dud run typed in - remove it ... 
					it.remove();
				}
			}
			runDatesToGraphList = UtilsMetrics.stringListToCommaDelimString(sortedChosenRunTimesList);
		} else {
			List<String> runDates = findRunDatesWhenRunsNotManuallyChosen(application, sqlSelectRunLike, reqSqlSelectRunNotLike, integerValueOfMaxRun(maxRun), integerValueOfMaxBaselineRun(maxBaselineRun), isUseRawRunSQL, runTimeSelectionSQL );
			runDatesToGraphList = UtilsMetrics.stringListToCommaDelimString(runDates);
		}
		return runDatesToGraphList;
	}

	private int integerValueOfMaxRun(String maxRunDropdownValue){
		if (maxRunDropdownValue == null)
			return 10;
			if (maxRunDropdownValue.equals(AppConstantsMetrics.DEFAULT_10 ) )
			return 10;
		if (maxRunDropdownValue.equals(AppConstantsMetrics.ALL ) )
			return 99;
		return Integer.parseInt(maxRunDropdownValue);
	}; 
	
	private int integerValueOfMaxBaselineRun(String maxBaselineRunDropdownValue){
		if (maxBaselineRunDropdownValue == null )
			return 1;
		if (maxBaselineRunDropdownValue.equals(AppConstantsMetrics.DEFAULT_01 ) )
			return 1;		
		if (maxBaselineRunDropdownValue.equals(AppConstantsMetrics.ALL ) )
			return 99;
		return Integer.parseInt(maxBaselineRunDropdownValue);
	}; 
		
	
	private List<String> findRunDatesWhenRunsNotManuallyChosen(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike, int numRunsDisplayed, int numBaselineRunsDisplayed, boolean useRawRunSQL, String runTimeSelectionSQL ) {
		
		List<String> runTimes = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		if (useRawRunSQL){
			
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(runTimeSelectionSQL);
			
			for (int i = 0; i < rows.size()  &&  i < numRunsDisplayed ; i++) {
				Map<String, Object> row = rows.get(i);
				runTimes.add( (String)row.get("RUN_TIME") );
				// System.out.println("populating run_times for trending page : " + row.get("RUN_TIME")  ) ;			
			}
			
		} else { 
			
			// makeSure we pick up to the maximum requested number of most recent runs and most recent baselines 
			
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(getRunTimeSelectionSQL(application, sqlSelectRunLike, reqSqlSelectRunNotLike));
			
			boolean gotAllRequestRecentRuns     = false; 
			boolean gotAllRequestedBaselineRuns = false; 
			
			int numRunsFound = 0;
			int numBaseLineRunsFound = 0;
			
			for (int i = 0;  (i < rows.size())  &&  !(gotAllRequestRecentRuns && gotAllRequestedBaselineRuns);  i++) {
			
				Map<String, Object> row = rows.get(i);
				
				if ( "Y".equalsIgnoreCase((String)row.get("BASELINE_RUN"))  &&  numBaseLineRunsFound < numBaselineRunsDisplayed   ){
					
					numBaseLineRunsFound++;
					runTimes.add( (String)row.get("RUN_TIME") );
//					System.out.println("populating run_times for trending page (a baseline found): " + row.get("RUN_TIME")  ) ;	
					if (numBaseLineRunsFound >= numBaselineRunsDisplayed ){
						gotAllRequestedBaselineRuns = true;
					}
					
				} else {    // populate the recent runs (may include a baseline as a 'run, past the requested number of baselines, but not at the end of finding runs)..  	
					
					if ( numRunsFound < numRunsDisplayed ){
						numRunsFound++;
						runTimes.add( (String)row.get("RUN_TIME") );
//						System.out.println("populating run_times for trending page (a std run found): " + row.get("RUN_TIME")  ) ;	
						if (numRunsFound >= numRunsDisplayed ){
							gotAllRequestRecentRuns = true;
						}					
					}	
					
				} //else "Y".equalsIgnoreCase

			} //for
	
		}  //useRawRunSQL else
		
		return  runTimes;
	}

	
	/* 
	 * Pick all runs for a given application, that have any transactions).  Note this does not mean a particular graph
	 * will have transactions for every run.  For example, a run may not of captured any Server statistics 
	 */
	@Override
	public String getRunTimeSelectionSQL(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike){	
	
		String runTimeSelectionSQL = "select distinct r.RUN_TIME, r.BASELINE_RUN from "
				 + " RUNS r, "
				 + " TRANSACTION t "
				 + "   where r.APPLICATION = '" + application + "'"  
				 + "     and r.APPLICATION = t.APPLICATION "
				 + "     and r.RUN_TIME    = t.RUN_TIME "
			  	 + constructSqlRunTimeLikeNotLike("r.", sqlSelectRunLike, reqSqlSelectRunNotLike)
				 + "   order by r.RUN_TIME DESC "; 		

//		System.out.println("runDAOjdbcTemplateImpl:getRunTimeSelectionSQL: " + runTimeSelectionSQL) ;		
		return  runTimeSelectionSQL;
	}
	
	private String constructSqlRunTimeLikeNotLike(String runTableId, String sqlSelectRunLike, String reqSqlSelectRunNotLike) {
		String sqlAnd = "";
		if (sqlSelectRunLike != "%" ){
			sqlAnd =" AND " + runTableId + "RUN_TIME LIKE '%" +  sqlSelectRunLike.replace(".", "").replace("T","").replace(":","").trim() + "%' ";  
		}
		if (reqSqlSelectRunNotLike != "" ){
			sqlAnd = sqlAnd + " AND NOT (" + runTableId + "run_time LIKE '%" +  reqSqlSelectRunNotLike.replace(".", "").replace("T","").replace(":","").trim() + "%' ) ";  
		}
		return sqlAnd;
	}

	
}
