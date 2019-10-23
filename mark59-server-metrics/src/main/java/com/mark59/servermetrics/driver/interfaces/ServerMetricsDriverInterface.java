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

package com.mark59.servermetrics.driver.interfaces;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
* @author Michael Cohen
* Written: Australian Winter 2019 
*/
public interface ServerMetricsDriverInterface <C> {
	
	public static final String HOSTID = "HOSTID";
	
	public void init(C config);

	public static String obtainReportedServerId(String server, String alternateServerId) {
		String reportedServerId = server;
		if ( HOSTID.equals(alternateServerId) ) {
			if ( System.getProperty("os.name", "unknown").toLowerCase().contains("win")){
				reportedServerId = System.getenv("COMPUTERNAME");
			} else { 
				reportedServerId = System.getenv("HOSTNAME");
			}

		} else if (StringUtils.isNotBlank(alternateServerId) ) {
			reportedServerId = alternateServerId;
		}
		return reportedServerId; 
	}
	
	
	public Map<String, Long> getCpuMetrics();
	
	public Map<String, Long> getMemoryMetrics();
	
	public Map<String, Long> getSystemInfo();
	
	public void quit();
	
}
