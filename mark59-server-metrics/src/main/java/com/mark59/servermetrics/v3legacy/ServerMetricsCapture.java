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

import org.apache.commons.lang3.StringUtils;
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
import com.mark59.servermetrics.v2legacy.ServerMetricsDriverConfig;
import com.mark59.servermetrics.v2legacy.ServerMetricsDriverInterface;
import com.mark59.servermetrics.v2legacy.ServerMetricsUtilsV2Legacy;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019 
 * 
 * This is the initiating class for mark59-server-metrics
 * To be replaced by ServerMetricsCaptureViaExcel, ServerMetricsCaptureViaWeb
 * so deprecating
 */
@Deprecated
public class ServerMetricsCapture extends AbstractJavaSamplerClient { 

	private static final Logger LOG = Logger.getLogger(ServerMetricsCapture.class);
		
	public static final String SERVER_KEY 			  = "SERVER";
	public static final String USERNAME_KEY 		  = "USERNAME";
	public static final String PASSWORD_KEY 		  = "PASSWORD";
	public static final String PASSWORD_CIPHER_KEY	  = "PASSWORD_CIPHER";
	public static final String ALTERNATE_SERVER_ID	  = "ALTERNATE SERVER ID";
	public static final String MONITOR_CPU 			  = "MONITOR CPU";
	public static final String MONITOR_MEMORY		  = "MONITOR MEMORY";
	public static final String SYSTEM_INFO 			  = "SYSTEM INFO";
	public static final String CONNECTION_PORT_KEY 	  = "CONNECTION PORT";
	public static final String CONNECTION_TIMEOUT_KEY = "CONNECTION TIMEOUT";
	public static final String OPERATING_SYSTEM_KEY   = "OPERATING SYSTEM";

	
	private static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<String,String>();
		staticMap.put("__________________________________________________", "");	
		staticMap.put("_  THIS JAVA REQUEST IS LEGACY  - DO NOT USE            __", "");	
		staticMap.put("_________________________________________________", "");	
		staticMap.put("_______________ general settings: ________________________", "");	
		staticMap.put(MONITOR_CPU,				Mark59Constants.TRUE);
		staticMap.put(MONITOR_MEMORY, 			Mark59Constants.TRUE);
		staticMap.put(SYSTEM_INFO, 				Mark59Constants.FALSE);
		staticMap.put("________________ server configuration ____________________ ",
											      "THIS IS LEGACY AND WILL BE REMOVED IN FUTURE RELEASE.");
		staticMap.put(SERVER_KEY,				"" );
		staticMap.put(OPERATING_SYSTEM_KEY,		"UNIX|LINUX|WINDOWS" );
		staticMap.put(USERNAME_KEY, 			"");
		staticMap.put(PASSWORD_KEY,				"");
		staticMap.put(PASSWORD_CIPHER_KEY,		"");
		staticMap.put(ALTERNATE_SERVER_ID,		"");
		staticMap.put(CONNECTION_PORT_KEY,		"22");
		staticMap.put(CONNECTION_TIMEOUT_KEY, 	"60000");
		staticMap.put("______________________ miscellaneous: ____________________", "");				
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");	
		
		staticMap.put("______________"       , "");			
		staticMap.put("build information: ", "mark59-server-metrics V2.X LEGACY - USE WEB API OR EXCEL");			
		
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
		
		ServerMetricsDriverConfig config = new ServerMetricsDriverConfig(
													context.getParameter(SERVER_KEY),
													context.getParameter(USERNAME_KEY), 
													context.getParameter(PASSWORD_KEY),
													context.getParameter(PASSWORD_CIPHER_KEY),
													context.getParameter(OPERATING_SYSTEM_KEY),
													context.getParameter(ALTERNATE_SERVER_ID),													
													Integer.valueOf(context.getParameter(CONNECTION_PORT_KEY)),
													Integer.valueOf(context.getParameter(CONNECTION_TIMEOUT_KEY)));
		
		ServerMetricsDriverInterface<ServerMetricsDriverConfig> driver = ServerMetricsUtilsV2Legacy.createAndInitDriver(config) ;

		JmeterFunctions jm = new JmeterFunctionsImpl(Thread.currentThread().getName());

		try {
			
			if (StringUtils.isEmpty(context.getParameter(MONITOR_CPU))  ||  Mark59Utils.resovesToTrue(context.getParameter(MONITOR_CPU))){
				Map<String, Long> cpuMetrics = driver.getCpuMetrics();
				for (Entry<String, Long> cpuMetric : cpuMetrics.entrySet()) {
					jm.userDatatypeEntry(cpuMetric.getKey(), cpuMetric.getValue(), JMeterFileDatatypes.CPU_UTIL);
				}
			}
			
			if (StringUtils.isEmpty(context.getParameter(MONITOR_MEMORY))  ||  Mark59Utils.resovesToTrue(context.getParameter(MONITOR_MEMORY))){			
				Map<String, Long> memoryMetrics = driver.getMemoryMetrics();
				for (Entry<String, Long> memoryMetric : memoryMetrics.entrySet()) {
					jm.userDatatypeEntry(memoryMetric.getKey(), memoryMetric.getValue(), JMeterFileDatatypes.MEMORY);
				}
			}

		} catch (Exception | AssertionError e) {
			LOG.error("ERROR : " + this.getClass() + " exception on thread " + Thread.currentThread().getName(), e);
			jm.setTransaction("9999-ServerMetricsCapture-" + context.getParameter(SERVER_KEY), 0, false);
			jm.failTest();
			Exception x = new Exception(e);
			x.printStackTrace();

		} finally {
			jm.tearDown();
		}

		return jm.getMainResult();
	}
	
	
	
	public static void main(String[] args) {
		Log4jConfigurationHelper.init(Level.DEBUG );
				
		ServerMetricsCapture thistest = new ServerMetricsCapture();

		additionalTestParametersMap.put(MONITOR_CPU,    Mark59Constants.TRUE);
		additionalTestParametersMap.put(MONITOR_MEMORY, Mark59Constants.TRUE);	
		
		String operatingSystem = System.getProperty("os.name", "unknown").toLowerCase();
		System.out.println("running on " + operatingSystem );
		
//--    to test the local machine 
		additionalTestParametersMap.put(SERVER_KEY, 	"localhost");			
		additionalTestParametersMap.put(ALTERNATE_SERVER_ID, ServerMetricsDriverInterface.HOSTID) ;	
		
		if ( operatingSystem.indexOf("win") >= 0 ) {
			additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"WINDOWS");
		} else if ( operatingSystem.indexOf("linux") >= 0 ) {
			additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"LINUX" );
		} else if ( operatingSystem.indexOf("unix") >= 0 ) {
			additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"UNIX");
		} else {
			additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"dont know the mapping for that os.name property");
		}
		
		
//		additionalTestParametersMap.put(SERVER_KEY, 			"server");
//		additionalTestParametersMap.put(USERNAME_KEY, 			"user");
//		additionalTestParametersMap.put(PASSWORD_KEY, 			"pass");
//		additionalTestParametersMap.put(PASSWORD_CIPHER_KEY, 	"passcipher");
//		additionalTestParametersMap.put(ALTERNATE_SERVER_ID, 	"");			
//		additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"LINUX");		
		
//--	to test a remote unix machine
//		additionalTestParametersMap.put(SERVER_KEY, 			"server");
//		additionalTestParametersMap.put(USERNAME_KEY, 			"user");
//		additionalTestParametersMap.put(PASSWORD_KEY, 			"pass");
//		additionalTestParametersMap.put(PASSWORD_CIPHER_KEY, 	"passcipher");
//		additionalTestParametersMap.put(ALTERNATE_SERVER_ID, 	"");				
//		additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"UNIX");
		
		
//		to test a remote windows machine
//		additionalTestParametersMap.put(SERVER_KEY, 			"server");
//		additionalTestParametersMap.put(USERNAME_KEY, 			"user");
//		additionalTestParametersMap.put(PASSWORD_KEY, 			"pass");
////		additionalTestParametersMap.put(PASSWORD_CIPHER_KEY, 	"passcipher");
//		additionalTestParametersMap.put(ALTERNATE_SERVER_ID, 	"");				
//		additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"WINDOWS");			


		JavaSamplerContext context = new JavaSamplerContext( thistest.getDefaultParameters()  );
		thistest.setupTest(context);
		thistest.runTest(context);
	}

}
