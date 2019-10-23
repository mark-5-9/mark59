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

package com.mark59.servermetrics;

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
import com.mark59.core.OutputDatatypes;
import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.AppConstants;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.servermetrics.driver.DosServerMetricsDriver;
import com.mark59.servermetrics.driver.UnixServerMetricsDriver;
import com.mark59.servermetrics.driver.config.ServerMetricsDriverConfig;
import com.mark59.servermetrics.driver.interfaces.ServerMetricsDriverInterface;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
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
		staticMap.put("_______________ general settings: ________________________", "");	
		staticMap.put(MONITOR_CPU,				AppConstants.TRUE);
		staticMap.put(MONITOR_MEMORY, 			AppConstants.TRUE);
		staticMap.put(SYSTEM_INFO, 				AppConstants.FALSE);
		staticMap.put("________________ server configuration (user/pass blank for localhost,  'HOSTID' or put a text value in ALTERNATE SERVER ID to override reported server id) : ________", "");		
		staticMap.put(OPERATING_SYSTEM_KEY,		"UNIX|LINUX|WINDOWS" );
		staticMap.put(SERVER_KEY,				"" );
		staticMap.put(USERNAME_KEY, 			"");
		staticMap.put(PASSWORD_KEY,				"");
		staticMap.put(PASSWORD_CIPHER_KEY,		"");
		staticMap.put(ALTERNATE_SERVER_ID,		"");
		staticMap.put(CONNECTION_PORT_KEY,		"22");
		staticMap.put(CONNECTION_TIMEOUT_KEY, 	"60000");
		staticMap.put("______________________ miscellaneous: ____________________", "");				
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");	
		
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
													context.getParameter(ALTERNATE_SERVER_ID),													
													Integer.valueOf(context.getParameter(CONNECTION_PORT_KEY)),
													Integer.valueOf(context.getParameter(CONNECTION_TIMEOUT_KEY)),
													context.getParameter(OPERATING_SYSTEM_KEY));
		
		ServerMetricsDriverInterface<ServerMetricsDriverConfig> driver;
		
		if (ServerMetricsConstants.WINDOWS.equalsIgnoreCase(config.getOperatingSystem())){
			driver = new DosServerMetricsDriver();
		} else if (	ServerMetricsConstants.UNIX.equalsIgnoreCase(config.getOperatingSystem()) ||
					ServerMetricsConstants.LINUX.equalsIgnoreCase(config.getOperatingSystem()))	{
			driver = new UnixServerMetricsDriver();
		} else {
			throw new IllegalArgumentException("Driver for OS " + config.getOperatingSystem() + " is Undefined. Supported OS's are:  UNIX  LINUX  WINDOWS");
		}

		driver.init(config);

		JmeterFunctions jm = new JmeterFunctionsImpl(Thread.currentThread().getName());

		try {
			
			if (AppConstants.TRUE.equals(context.getParameter(MONITOR_CPU))) {
				Map<String, Long> cpuMetrics = driver.getCpuMetrics();
				for (Entry<String, Long> cpuMetric : cpuMetrics.entrySet()) {
					jm.userDatatypeEntry(cpuMetric.getKey(), cpuMetric.getValue(), OutputDatatypes.CPU_UTIL);
				}
			}
			
			if (AppConstants.TRUE.equals(context.getParameter(MONITOR_MEMORY))) {
				Map<String, Long> memoryMetrics = driver.getMemoryMetrics();
				for (Entry<String, Long> memoryMetric : memoryMetrics.entrySet()) {
					jm.userDatatypeEntry(memoryMetric.getKey(), memoryMetric.getValue(), OutputDatatypes.MEMORY);
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
	
	
	
	public static void main(String[] args) {
		Log4jConfigurationHelper.init(Level.DEBUG );
				
		ServerMetricsCapture thistest = new ServerMetricsCapture();

		additionalTestParametersMap.put(MONITOR_CPU,    AppConstants.TRUE);
		additionalTestParametersMap.put(MONITOR_MEMORY, AppConstants.TRUE);	
		additionalTestParametersMap.put(SERVER_KEY, 	"localhost");			
		
		String operatingSystem = System.getProperty("os.name", "unknown").toLowerCase();
		System.out.println("running on " + operatingSystem );
		
//		to test the local machine
		additionalTestParametersMap.put(ALTERNATE_SERVER_ID, ServerMetricsDriverInterface.HOSTID) ;	
//		additionalTestParametersMap.put(ALTERNATE_SERVER_ID, "CrazyNameForRabbits") ;	
		
		if ( operatingSystem.indexOf("win") >= 0 ) {
			additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	ServerMetricsConstants.WINDOWS);
		} else if ( operatingSystem.indexOf("linux") >= 0 ) {
			additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	ServerMetricsConstants.LINUX );
		} else if ( operatingSystem.indexOf("unix") >= 0 ) {
			additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	ServerMetricsConstants.UNIX);
		} else {
			additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"dont know the mapping for that os.name property");
		}
		
//		to test a remote linux machine
//		additionalTestParametersMap.put(SERVER_KEY, 			"server");
//		additionalTestParametersMap.put(USERNAME_KEY, 			"user");
//		additionalTestParametersMap.put(PASSWORD_KEY, 			"pass");
//		additionalTestParametersMap.put(PASSWORD_CIPHER_KEY, 	"passcipher");
//		additionalTestParametersMap.put(ALTERNATE_SERVER_ID, 	"");			
//		additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"LINUX");		
		
//		to test a remote unix machine
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
//		additionalTestParametersMap.put(PASSWORD_CIPHER_KEY, 	"passcipher");
//		additionalTestParametersMap.put(ALTERNATE_SERVER_ID, 	"");				
//		additionalTestParametersMap.put(OPERATING_SYSTEM_KEY, 	"WINDOWS");			


		JavaSamplerContext context = new JavaSamplerContext( thistest.getDefaultParameters()  );
		thistest.setupTest(context);
		thistest.runTest(context);
	}

}
