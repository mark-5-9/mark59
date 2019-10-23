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

package com.mark59.servermetrics.driver;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mark59.servermetrics.driver.config.NewRelicServerMetricsDriverConfig;
import com.mark59.servermetrics.driver.interfaces.ServerMetricsDriverInterface;

/**
* @author Philip Webb    
* @author Michael Cohen
* Written: Australian Winter 2019   
*/
public class NewRelicServerMetricsDriver implements ServerMetricsDriverInterface<NewRelicServerMetricsDriverConfig> {

	private static final Logger LOG = Logger.getLogger(NewRelicServerMetricsDriver.class);

	private String newRelicApiUrl;
	private String newRelicApiAppId;
	private String newRelicXapiKey;
	private String proxyServer;
	private String proxyPort;
	
	private String url; 
	private JSONObject jsonResponse;
	private Proxy proxy;
	
	@Override
	public void init(NewRelicServerMetricsDriverConfig config) {
		
		this.newRelicApiUrl = config.getNewRelicApiUrl();    // "http://api.newrelic.com/v2/applications/";
		this.newRelicApiAppId = config.getNewRelicApiAppId();   
		this.newRelicXapiKey = config.getNewRelicXapiKey();
		this.proxyServer = config.getProxyServer();
		this.proxyPort = config.getProxyPort();
		
		proxy = null;
		if ( StringUtils.isNotBlank(proxyServer + proxyPort) ) {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyServer, new Integer(proxyPort)));
		}
	}

	public Map<String, Long> getCpuMetrics() {
		Map<String, Long> metrics = new HashMap<>();
		
		LOG.debug(" new relic getCpuMetrics Request : " + newRelicApiUrl + newRelicApiAppId + "/instances.json");	
		
		jsonResponse = invokeNewRelicApi(newRelicApiUrl + newRelicApiAppId + "/instances.json");
		
		LOG.debug(" new relic getCpuMetrics Response : " + jsonResponse.toMap() );	

		String urlDateRangeParmStr = buildUrlDateRangeParm();
		JSONArray application_instances = jsonResponse.getJSONArray("application_instances");

		for (int i = 0; i < application_instances.length(); i++) {
			JSONObject application_instance = (JSONObject) application_instances.get(i);
			Integer instanceId = (Integer) application_instance.get("id");
			metrics.put( "CPU_" + instanceId, instanceCpuUtilization(instanceId, urlDateRangeParmStr));
		}
		return metrics;
	}
	
	
	public Map<String, Long> getMemoryMetrics() {
		Map<String, Long> metrics = new HashMap<>();
		
		jsonResponse = invokeNewRelicApi(newRelicApiUrl + newRelicApiAppId + "/instances.json");
		
		LOG.debug(" new relic getMemoryMetrics Response : " + jsonResponse.toMap() );
		
		String urlDateRangeParmStr = buildUrlDateRangeParm();
		JSONArray application_instances = jsonResponse.getJSONArray("application_instances");

		for (int i = 0; i < application_instances.length(); i++) {
			JSONObject application_instance = (JSONObject) application_instances.get(i);
			Integer instanceId = (Integer) application_instance.get("id");
			metrics.put( "MEMORY_"   + instanceId, instanceCpuMemory(instanceId, urlDateRangeParmStr));
		}
		return metrics;

	}


	private String buildUrlDateRangeParm() {

		ZonedDateTime utcTimeNow = ZonedDateTime.now(ZoneOffset.UTC);
		String toHour 		= String.format("%02d", utcTimeNow.getHour());
		String toMinute 	= String.format("%02d", utcTimeNow.getMinute());
		
		ZonedDateTime utcMinus1Min 	= utcTimeNow.minusMinutes(1);
		String fromHour 	= String.format("%02d", utcMinus1Min.getHour());
		String fromMinute 	= String.format("%02d", utcMinus1Min.getMinute());

		String fromDate = utcMinus1Min.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String toDate 	= utcTimeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));		
		
		return "&from=" + fromDate + "T" + fromHour + "%3A" + fromMinute + "%3A00%2B00%3A00"
			   + "&to=" + toDate   + "T" + toHour   + "%3A" + toMinute   + "%3A00%2B00%3A00";
	}


	private Long instanceCpuUtilization(Integer instanceId, String urlDateRangeParmStr){
		jsonResponse = invokeNewRelicApi(newRelicApiUrl + newRelicApiAppId + "/instances/" + instanceId +  "/metrics/data.json?names%5B%5D=CPU%2FUser%2FUtilization" + urlDateRangeParmStr);
		Double percentCpuUserUtilization = -1.0;
		try {
			percentCpuUserUtilization = 10.0 * (Double)((JSONObject)((JSONObject)jsonResponse.getJSONObject("metric_data").getJSONArray("metrics").get(0)).getJSONArray("timeslices").get(0)).getJSONObject("values").get("percent") ;
		} catch (Exception e) {
			LOG.warn("Attempt to get CPU Utilizaiton from New Relic for instanceId : " + instanceId + " has failed (set to -1)");
		}
		return percentCpuUserUtilization.longValue();
	}
	

	private Long instanceCpuMemory(Integer instanceId, String urlDateRangeParmStr){
		jsonResponse = invokeNewRelicApi(newRelicApiUrl + newRelicApiAppId  + "/instances/" + instanceId + "/metrics/data.json?names%5B%5D=Memory%2FPhysical" + urlDateRangeParmStr);
		Double totalUusedMbMemory = -1.0;
		try {
			totalUusedMbMemory =  0.1 *  (Integer)((JSONObject)((JSONObject)jsonResponse.getJSONObject("metric_data").getJSONArray("metrics").get(0)).getJSONArray("timeslices").get(0)).getJSONObject("values").get("total_used_mb") ;
		} catch (Exception e) {
			LOG.warn("Attempt to get Memory data from New Relic for instanceId : " + instanceId + " has failed (set to -1)");
		}
		return totalUusedMbMemory.longValue();
	}

	
		
	private JSONObject invokeNewRelicApi(String url){
		try {
			HttpURLConnection conn;
			
			if (proxy != null) {
				conn = (HttpURLConnection)new URL(url).openConnection(proxy);
			} else {
				conn = (HttpURLConnection)new URL(url).openConnection();
			}
			conn.setRequestMethod("GET");
			conn.addRequestProperty("X-Api-Key", newRelicXapiKey);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
	
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new RuntimeException("rc:" + conn.getResponseCode() + "from " + url);
			}
			return convertStreamToString(conn.getInputStream());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("throwing RuntimeException for url: " + url + "\n response of  : " + jsonResponse + "\n message: "+ e.getMessage()  );
		}	
	}


	@Override
	public void quit() {
	}
		
	
	@Override
	public Map<String, Long> getSystemInfo() {
		LOG.warn("getSystemInfo() not yet implemented for New Relic API");
		return null;
	}	
	
	
	
	@SuppressWarnings("resource")
	private JSONObject convertStreamToString(java.io.InputStream is) {
		String isStr = null;
		try {
			Scanner s = new Scanner(is).useDelimiter("\\A");
			isStr = s.hasNext() ? s.next() : "";
			JSONObject jsonObject = new JSONObject(isStr);
			return jsonObject;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("convertStreamToString RuntimeException for url: " + url
					+ "\n last response of  : " + isStr + "\n message: " + e.getMessage());
		}
	}
	
	
	private static void printMap(Map<String, Long> map ) {
		for (String key : map.keySet()) {
			Long value = map.get(key);
			System.out.println("   " + key + " : " + value);
		}
	}
	
// please put the actual values into the DriverConfig before running !! 
	public static void main(String[] args) {		
		System.out.println("> NewRelicServerMetricsDriver");
		NewRelicServerMetricsDriver newRelicServerMetricsDriver = new NewRelicServerMetricsDriver();
		NewRelicServerMetricsDriverConfig nrsmc = new NewRelicServerMetricsDriverConfig( 
				"http://api.newrelic.com/v2/applications/", "--appid--", "--XapiKey---", "--proxy--", "--proxyport--");
		newRelicServerMetricsDriver.init(nrsmc);
		Map<String, Long> metrics = newRelicServerMetricsDriver.getCpuMetrics();
		System.out.println("- cpu -------------------------" );
		printMap(metrics);
		metrics = newRelicServerMetricsDriver.getMemoryMetrics();
		System.out.println("- memory ----------------------" );
		printMap(metrics);
		System.out.println("< NewRelicServerMetricsDriver");
	}

}
