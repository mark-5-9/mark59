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

package com.mark59.metrics.data.beans;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class Run {
	
	String		application;
	String		runTime;
	String		runReference;
	String		period;
	String		duration;	
	String		baselineRun;
	String		comment;	
	

	public Run() {
	}
	
	public Run(Run run) {
		this.application = run.application;
		this.runTime = run.runTime;
		this.runReference = run.runReference;
		this.period = run.period;
		this.duration = run.duration;
		this.baselineRun = run.baselineRun;
		this.comment = run.comment;
	}


	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getRunTime() {
		return runTime;
	}
	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}
	public String getRunReference() {
		return runReference;
	}
	public void setRunReference(String runReference) {
		this.runReference = runReference;
	}
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getBaselineRun() {
		return baselineRun;
	}
	public void setBaselineRun(String baselineRun) {
		this.baselineRun = baselineRun;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	
}
