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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.core.utils.Mark59Utils;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
@Deprecated
public class NewRelicServerMetricsCapture extends AbstractJavaSamplerClient { 

	private static final Logger LOG = Logger.getLogger(NewRelicServerMetricsCapture.class);
	
	protected Arguments jmeterArguments = new Arguments();	
	protected Map<String, String> jmeterArgumentsMap = new LinkedHashMap<String,String>();
	
	public static final String MONITOR_CPU    = "MONITOR_CPU";
	public static final String MONITOR_MEMORY = "MONITOR_MEMORY";
	
	public static final String NEW_RELIC_API_URL    = "NEW_RELIC_API_URL";
	public static final String NEW_RELIC_API_APP_ID = "NEW_RELIC_API_APP_ID";	
	public static final String NEW_RELIC_XAPIKEY    = "NEW_RELIC_XAPIKEY";
	
	public static final String PROXY_SERVER = "PROXY SERVER";		
	public static final String PROXY_PORT   = "PROXY PORT";		

	private static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<String,String>();
		staticMap.put("__________________________________________________", "");	
		staticMap.put("_THIS JAVA REQUEST IS LEGACY - DO NOT USE -TO BE REMOVED IN THE NEXT RELEASE !!!!_", "");	
		staticMap.put("_________________________________________________", "");			
		staticMap.put("______________________ general settings: ________________________", "");	
		staticMap.put(MONITOR_CPU,				Mark59Constants.TRUE);
		staticMap.put(MONITOR_MEMORY, 			Mark59Constants.TRUE);
		staticMap.put("______________________ new relic configuration settings : ________",
	 											"THIS IS LEGACY AND IS PLANNED TO BE REMOVED IN THE NEXT RELEASE !!!!.");
		staticMap.put(NEW_RELIC_API_URL,		"https://api.newrelic.com/v2/applications/" );
		staticMap.put(NEW_RELIC_API_APP_ID, 	"");
		staticMap.put(NEW_RELIC_XAPIKEY,		"");
		staticMap.put(PROXY_SERVER, 			"");
		staticMap.put(PROXY_PORT, 				"");	
		staticMap.put("______________________ miscellaneous: ____________________________", "");				
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		staticMap.put("______________"       , "");			
		staticMap.put("build information: ", "mark59-server-metrics V2.X LEGACY - USE SUPPLIED ALTERNATIVE ");			
		defaultArgumentsMap = Collections.unmodifiableMap(staticMap);
	}
	
	private static Map<String, String> additionalTestParametersMap = new HashMap<String, String>();
	
	
	
	
	/** 
	 * Creates the list of parameters with default values, as they would appear on the Jmeter GUI for the JavaSampler being implemented.
	 * @see #additionalTestParameters()
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient
	 */
	@Override
	public Arguments getDefaultParameters() {
		return Mark59Utils.mergeMapWithAnOverrideMap(defaultArgumentsMap, additionalTestParametersMap);
	}

	
	@Override
	public void setupTest(JavaSamplerContext context) {
		super.setupTest(context);
	}	

	
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		
		if (IpUtilities.localIPisNotOnListOfIPaddresses(context.getParameter(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST))){ 
			return null;
		}
		
		NewRelicServerMetricsDriver driver = new NewRelicServerMetricsDriver();
		
		NewRelicServerMetricsDriverConfig config = new NewRelicServerMetricsDriverConfig(
															context.getParameter(NEW_RELIC_API_URL),
															context.getParameter(NEW_RELIC_API_APP_ID),
															context.getParameter(NEW_RELIC_XAPIKEY),
															context.getParameter(PROXY_SERVER),
															context.getParameter(PROXY_PORT));
		
		driver.init(config);		

		JmeterFunctions jm = new JmeterFunctionsImpl(Thread.currentThread().getName());

		try {
			
			if (Mark59Constants.TRUE.equals(context.getParameter(MONITOR_CPU))) {
				Map<String, Long> cpuMetrics = driver.getCpuMetrics();
				for (Entry<String, Long> cpuMetric : cpuMetrics.entrySet()) {
					jm.userDatatypeEntry(cpuMetric.getKey(), cpuMetric.getValue(), JMeterFileDatatypes.CPU_UTIL);
				}
			}
			
			if (Mark59Constants.TRUE.equals(context.getParameter(MONITOR_MEMORY))) {
				Map<String, Long> memoryMetrics = driver.getMemoryMetrics();
				for (Entry<String, Long> memoryMetric : memoryMetrics.entrySet()) {
					jm.userDatatypeEntry(memoryMetric.getKey(), memoryMetric.getValue(), JMeterFileDatatypes.MEMORY);
				}
			}

		} catch (Exception | AssertionError e) {
			LOG.error("ERROR : " + this.getClass() + " exception on thread " + Thread.currentThread().getName(), e);
			jm.failTest();
			Exception x = new Exception(e);
			x.printStackTrace();

		} finally {
			jm.tearDown();
		}

		return jm.getMainResult();
	}
	
	
	/**
	 * convenience test main
	 */
	public static void main(String[] args) {
		Log4jConfigurationHelper.init(Level.DEBUG );
		
		additionalTestParametersMap.put(MONITOR_CPU,    		Mark59Constants.TRUE);
		additionalTestParametersMap.put(MONITOR_MEMORY, 		Mark59Constants.TRUE);	
		additionalTestParametersMap.put("______________________ new relic configuration settings _______________________", "");		

	//---- fill in values ...		
		additionalTestParametersMap.put(NEW_RELIC_API_APP_ID, 	"newrelicappid");
		additionalTestParametersMap.put(NEW_RELIC_XAPIKEY, 		"newrelicxapikey");
		additionalTestParametersMap.put(PROXY_SERVER, 			"proxyurl");	
		additionalTestParametersMap.put(PROXY_PORT, 			"proxyport");		
	
		NewRelicServerMetricsCapture thistest = new NewRelicServerMetricsCapture();
		JavaSamplerContext context = new JavaSamplerContext( thistest.getDefaultParameters()  );
		thistest.setupTest(context);
		thistest.runTest(context);
	}	

}
