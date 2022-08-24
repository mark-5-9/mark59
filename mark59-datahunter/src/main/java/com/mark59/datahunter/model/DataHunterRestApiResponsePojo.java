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

package com.mark59.datahunter.model;

import java.util.List;

import com.mark59.datahunter.data.beans.Policies;

/**
 * Used to provide the response back to the client for the DataHunter Rest API calls:
 * <p><code>policies</code> : contains the selected policy or policies (may contain debug information for requests 
 * not requiring policy data to be returned) 
 * <p><code>countPoliciesBreakdown</code> : is only designed to be populated when the using the countPoliciesBreakdown
 * operation is invoked 
 * <p><code>asyncMessageaAnalyzerResult</code> : is only designed to be populated when the using the asyncMessageAnalyzer
 * operation is invoked  
 * <p><code>success, rowsAffected, failMsg</code> : are populated in a similar manner to the values seen in the DataHunter
 * web application, aligning to most DataHunter action result pages with the values labeled 'result', 'rows affected' and
 * 'details'.  (<code>success</code> is set as 'true' or 'false', rather than using the values 'PASS' or 'FAIL' seen on 
 * the web pages)    
 * 
 * @author Philip Webb
 * Written: Australian Summer 2021/22
 */
public class DataHunterRestApiResponsePojo {

	private List<Policies> policies;
	private List<CountPoliciesBreakdown> countPoliciesBreakdown;
	private List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResults;
	private String success;
	private Integer rowsAffected;
	private String failMsg;	

	
	public DataHunterRestApiResponsePojo() {
	}


	public List<Policies> getPolicies() {
		return policies;
	}


	public void setPolicies(List<Policies> policies) {
		this.policies = policies;
	}
	
	public List<CountPoliciesBreakdown> getCountPoliciesBreakdown() {
		return countPoliciesBreakdown;
	}

	public void setCountPoliciesBreakdown(List<CountPoliciesBreakdown> countPoliciesBreakdown) {
		this.countPoliciesBreakdown = countPoliciesBreakdown;
	}

	public List<AsyncMessageaAnalyzerResult> getAsyncMessageaAnalyzerResults() {
		return asyncMessageaAnalyzerResults;
	}

	public void setAsyncMessageaAnalyzerResults(List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResults) {
		this.asyncMessageaAnalyzerResults = asyncMessageaAnalyzerResults;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public Integer getRowsAffected() {
		return rowsAffected;
	}

	public void setRowsAffected(Integer rowsAffected) {
		this.rowsAffected = rowsAffected;
	}

	public String getFailMsg() {
		return failMsg;
	}

	public void setFailMsg(String failMsg) {
		this.failMsg = failMsg;
	}

	@Override
    public String toString() {
       return   "[policies=" + policies
        		+ ", countPoliciesBreakdown="+ countPoliciesBreakdown
        		+ ", asyncMessageaAnalyzerResults="+ asyncMessageaAnalyzerResults
        		+ ", succes="+ success
        		+ ", rowsAffected="+ rowsAffected
        		+ ", failMsg="+ failMsg
        		+ "]";
	}
		
}
