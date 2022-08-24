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

package com.mark59.metrics.pojos;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 */
public class TestCommandParserResponsePojo {

	private String candidateTxnId;
	private String parserResult;	
	private String summary;	

	public String getCandidateTxnId() {
		return candidateTxnId;
	}

	public void setCandidateTxnId(String candidateTxnId) {
		this.candidateTxnId = candidateTxnId;
	}

	public String getParserResult() {
		return parserResult;
	}

	public void setParserResult(String parserResult) {
		this.parserResult = parserResult;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}


	@Override
    public String toString() {
        return   "[candidateTxnId" + candidateTxnId
        		+ ", parserResult="+ parserResult   
        		+ ", summary="     + summary   
        		+ "]";
	}
		
}
