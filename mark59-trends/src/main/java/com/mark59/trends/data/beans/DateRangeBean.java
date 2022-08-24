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

package com.mark59.trends.data.beans;
/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class DateRangeBean {

	Long		runStartTime;
	Long		runEndTime;
	boolean		filterApplied;		
	

	public DateRangeBean(Long runStartTime, Long runEndTime) {
		this.runStartTime = runStartTime;
		this.runEndTime = runEndTime;
		this.filterApplied = false; 
	}
	
	public DateRangeBean(Long runStartTime, Long runEndTime, boolean filterApplied) {
		this.runStartTime = runStartTime;
		this.runEndTime = runEndTime;
		this.filterApplied = filterApplied; 
	}

	public Long getRunStartTime() {
		return runStartTime;
	}
	public void setRunStartTime(Long runStartTime) {
		this.runStartTime = runStartTime;
	}
	public Long getRunEndTime() {
		return runEndTime;
	}
	public void setRunEndTime(Long runEndTime) {
		this.runEndTime = runEndTime;
	}

	public boolean isFilterApplied() {
		return filterApplied;
	}

	public void setFilterApplied(boolean filterApplied) {
		this.filterApplied = filterApplied;
	}

	public String prettyPrint() {
		return "[ " + runStartTime + " : " +  runEndTime + " ]";
	}
	
}
