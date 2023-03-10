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

import java.util.List;

import com.mark59.trends.data.beans.Run;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface RunDAO 
{
	void insertRun(Run run);

	void updateRun(Run run);

	void deleteRun(String application, String runTime);

	void deleteAllForApplication(String application);
	
	Run findRun(String application, String runTime);

	List<Run> findRuns(String application);
		
	List<String> findApplications();
	
	List<String> findApplications(String appListSelector);
		
	String findLastRunDate(String application);
	
	List<String> findRunDates(String application);

	List<String> findApplicationsWithBaselines();

	Run findLastBaselineRun(String application);

	String determineRunDatesToGraph(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike, boolean isManuallySelectRuns, String chosenRuns, boolean isUseRawRunSQL, String runTimeSelectionSQL, String maxRun, String maxBaselineRun);
	
	String runsSQL(String application, String sqlSelectRunLike, String reqSqlSelectRunNotLike, boolean useRawRunSQL, String rawRunTimeSelectionSQL);


}