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

package com.mark59.servermetricsweb.pojos;

import java.util.Arrays;
import java.util.List;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020
 * 
 */
public class WebServerMetricsResponsePojo {

	private String serverProfileName;
	private String server;
	private String alternativeServerId;
	private String reportedServerId;
	private List<ParsedCommandResponse> parsedCommandResponses;
	private String logLines;
	private String testModeResult;
	private String failMsg;	

	
	public WebServerMetricsResponsePojo() {
	}

	
	public String getServerProfileName() {
		return serverProfileName;
	}

	public void setServerProfileName(String serverProfileName) {
		this.serverProfileName = serverProfileName;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getAlternativeServerId() {
		return alternativeServerId;
	}

	public void setAlternativeServerId(String alternativeServerId) {
		this.alternativeServerId = alternativeServerId;
	}

	public String getReportedServerId() {
		return reportedServerId;
	}

	public void setReportedServerId(String reportedServerId) {
		this.reportedServerId = reportedServerId;
	}

	public List<ParsedCommandResponse> getParsedCommandResponses() {
		return parsedCommandResponses;
	}

	public void setParsedCommandResponses(List<ParsedCommandResponse> parsedCommandResponses) {
		this.parsedCommandResponses = parsedCommandResponses;
	}

	public String getLogLines() {
		return logLines;
	}

	public void setLogLines(String logLines) {
		this.logLines = logLines;
	}

	public String getTestModeResult() {
		return testModeResult;
	}

	public void setTestModeResult(String testModeResult) {
		this.testModeResult = testModeResult;
	}

	public String getFailMsg() {
		return failMsg;
	}

	public void setFailMsg(String failMsg) {
		this.failMsg = failMsg;
	}


	@Override
    public String toString() {
		String parsedCommandResponsesStr = " NULL ";
		if (parsedCommandResponses!=null) {
			parsedCommandResponsesStr= Arrays.toString(parsedCommandResponses.toArray());
		}
		
        return   "[serverProfileName" + serverProfileName
        		+ ", server="+ server
        		+ ", alternativeServerId="+ alternativeServerId
        		+ ", parsedCommandResponses = "+ parsedCommandResponsesStr  
        		+ ", logLines = "+ logLines  
        		+ ", testModeResult  = "+ testModeResult
        		+ ", failMsg="+ failMsg   
        		+ "]";
	}
		
}
