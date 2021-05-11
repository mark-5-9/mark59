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

package com.mark59.servermetrics.v3legacy;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class NewRelicServerMetricsDriverConfig {

	private String newRelicApiUrl;
	private String newRelicApiAppId;
	private String newRelicXapiKey;
	private String proxyServer;
	private String proxyPort;	
	
	
	public NewRelicServerMetricsDriverConfig(String newRelicApiUrl, String newRelicApiAppId, String newRelicXapiKey, String proxyServer, String proxyPort) {
		this.newRelicApiUrl   = newRelicApiUrl;
		this.newRelicApiAppId = newRelicApiAppId;
		this.newRelicXapiKey  = newRelicXapiKey;
		this.proxyServer = proxyServer;
		this.proxyPort   = proxyPort;
	}
	
	
	public String getNewRelicApiUrl() {
		return newRelicApiUrl;
	}
		
	public String getNewRelicApiAppId() {
		return newRelicApiAppId;
	}
		
	public String getNewRelicXapiKey() {
		return newRelicXapiKey;
	}

	public String getProxyServer() {
		return proxyServer;
	}
		
	public String getProxyPort() {
		return proxyPort;
	}
			
}

