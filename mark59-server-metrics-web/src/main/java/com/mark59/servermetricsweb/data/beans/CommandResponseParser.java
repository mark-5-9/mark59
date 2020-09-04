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

package com.mark59.servermetricsweb.data.beans;

/**
 * @author Philip Webb
 * Written: Australian Spring 2020
 */
public class CommandResponseParser {

	String scriptName;
	String metricTxnType;
	String metricNameSuffix;
	String script;
	String comment;
	String sampleCommandResponse;
	
	
	public CommandResponseParser() {
	}


	public String getScriptName() {
		return scriptName;
	}


	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}


	public String getMetricTxnType() {
		return metricTxnType;
	}


	public void setMetricTxnType(String metricTxnType) {
		this.metricTxnType = metricTxnType;
	}


	public String getMetricNameSuffix() {
		return metricNameSuffix;
	}


	public void setMetricNameSuffix(String metricNameSuffix) {
		this.metricNameSuffix = metricNameSuffix;
	}


	public String getScript() {
		return script;
	}


	public void setScript(String script) {
		this.script = script;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}

	
	public String getSampleCommandResponse() {
		return sampleCommandResponse;
	}


	public void setSampleCommandResponse(String sampleCommandResponse) {
		this.sampleCommandResponse = sampleCommandResponse;
	}


	@Override
    public String toString() {
        return   "[scriptName="+ scriptName + 
        		", metricTxnType="+ metricTxnType + 
        		", metricNameSuffix="+ metricNameSuffix + 
        		", script="+ script.trim() + 
        		"]";
	}
		
}
