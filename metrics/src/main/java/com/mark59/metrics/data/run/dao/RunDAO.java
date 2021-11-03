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

import java.util.List;

import com.mark59.metrics.data.beans.Run;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface RunDAO 
{
	public void insertRun(Run run);

	public void updateRun(Run run);

	public void deleteRun(String application, String runTime);

	public void deleteAllForApplication(String application);
	
	public Run findRun(String application, String runTime);

	public List<Run> findRuns(String application);	
		
	public List<String> findApplications();
	
	public List<String> findApplications(String appListSelector);
		
	public String findLastRunDate(String application); 
	
	public List<String> findRunDates(String application);

	public List<String> findApplicationsWithBaselines();

	public Run findLastBaselineRun(String application);

	public String determineRunDatesToGraph(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike, boolean isManuallySelectRuns, String chosenRuns, boolean isUseRawRunSQL, String runTimeSelectionSQL,  String maxRun, String maxBaselineRun);
	
	public String runsSQL(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike, boolean useRawRunSQL, String rawRunTimeSelectionSQL);


}