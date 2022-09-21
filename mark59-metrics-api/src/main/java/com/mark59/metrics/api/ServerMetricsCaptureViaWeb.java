
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

package com.mark59.metrics.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.metrics.api.utils.MetricsApiConstants;
import com.mark59.metrics.controller.ServerMetricRestController;
import com.mark59.metrics.pojos.ParsedCommandResponse;
import com.mark59.metrics.pojos.WebServerMetricsResponsePojo;
import com.mark59.metrics.utils.MetricsConstants;
import com.mark59.metrics.utils.MetricsUtils;

/**
 * @author Philip Webb
 * Written: Australian Autumn 2020 
 * 
 * This is the initiating class for mark59-metrics-api (metrics capture via API).
 * 
 * <p>Intended for use as a Java Sampler in a JMeter test plan to capture metric data.
 * 
 * @see ServerMetricRestController
 * 
 */
public class ServerMetricsCaptureViaWeb  extends AbstractJavaSamplerClient { 

	private static final Logger LOG = LogManager.getLogger(ServerMetricsCaptureViaWeb.class);
	
	public static final String MARK59_METRICS_URL 			= "MARK59_METRICS_URL";
	public static final String DEFAULT_MARK59_METRICS_URL 	= "http://localhost:8085/mark59-metrics";

	public static final String SERVER_PROFILE_NAME 	= "SERVER_PROFILE_NAME";
	
	public static final String FULL	= "full";
	public static final String NO	= "no";

	protected String tgName = null;
	protected AbstractThreadGroup tg = null;
	
	
	
	private static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<>();
	
		staticMap.put(MARK59_METRICS_URL, DEFAULT_MARK59_METRICS_URL );
		staticMap.put(SERVER_PROFILE_NAME, "" );
		
		staticMap.put(".", "");	
		staticMap.put("_________________________ logging settings: _______________", "ERROR_MESSAGES values: 'short' (default), 'full', 'no'");
		staticMap.put(MetricsApiConstants.LOG_ERROR_MESSAGES, "short" );
		staticMap.put(MetricsApiConstants.PRINT_ERROR_MESSAGES, "short" );
		staticMap.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(false));		
		staticMap.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(false));			
		staticMap.put("..", "");			
		staticMap.put("_________________________ miscellaneous: __________________", "");
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");	
		staticMap.put("...", "");			
		staticMap.put("_________________________ notes: __________________________", "");	
		staticMap.put("__","- please replace the default url with your actual.");	
		staticMap.put("_", "- server profile of 'localhost' reports metrics for the machine hosting the 'mark59-metrics' application!");	
		staticMap.put("-",
				"   to capture injector metrics use the machine name or an Excel 'localhost..' entry instead (see Mark59 User Guide)");
		staticMap.put("___________________", "");		
		staticMap.put("build information: ", "mark59-metrics-api Version: " + MetricsConstants.MARK59_VERSION_METRICS);			
		
		defaultArgumentsMap = Collections.unmodifiableMap(staticMap);
	}
	
	private static final Map<String, String> additionalTestParametersMap = new HashMap<>();
	

	
	/** 
	 * Creates the list of parameters with default values, as they would appear on the Jmeter GUI for the JavaSampler being implemented.
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
		
		if ( context.getJMeterContext() != null  && context.getJMeterContext().getThreadGroup() != null ) {
			tg     = context.getJMeterContext().getThreadGroup();
			tgName = tg.getName();
		}
		
		if (IpUtilities.localIPisNotOnListOfIPaddresses(context.getParameter(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST))){ 
			LOG.info("Thread Group " + tgName + " is stopping (not on 'Restrict to IP List')" );
			if (tg!=null) tg.stop();
			return null;
		}
		
		JmeterFunctions jm = new JmeterFunctionsImpl(context, false);
		BufferedReader in = null;
		Integer repsonseCode = null;
		WebServerMetricsResponsePojo response = null;
		String webServiceUrl = null;

		try {
			
			String reqServerProfileName  = context.getParameter(SERVER_PROFILE_NAME); 
						
			webServiceUrl = context.getParameter(MARK59_METRICS_URL) 	+ "/api/metric?reqServerProfileName=" + reqServerProfileName;
			LOG.debug("webServiceUrl : " + webServiceUrl);
			
			URL url = new URL(webServiceUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			repsonseCode = con.getResponseCode();
			
			in = new BufferedReader( new InputStreamReader(con.getInputStream()));
			String respLine;
			StringBuilder jsonResponseStr = new StringBuilder();
			while ((respLine = in.readLine()) != null) {
				jsonResponseStr.append(respLine);
			}
			in.close();

			response = new ObjectMapper().readValue(jsonResponseStr.toString(), WebServerMetricsResponsePojo.class );

	 		ServerMetricsCaptureUtils.validateCommandsResponse(response);
	 		
			for (ParsedCommandResponse parsedCommandResponse : response.getParsedCommandResponses()) {
				ServerMetricsCaptureUtils.createJMeterTxnsUsingCommandResponse(context, jm, reqServerProfileName, parsedCommandResponse);				
			}
			
		} catch (Exception | AssertionError e) {
			ServerMetricsCaptureUtils.logUnexpectedException(response, e, "ServerMetricsCaptureViaWeb");
			LOG.debug("        last response-code from mark59-metrics was " + repsonseCode);
			LOG.debug("        last response from mark59-metrics was  \n" + response );
			if (in != null){try {in.close();} catch (IOException ignored) {}}
		} finally {
			jm.tearDown();
		}

		return jm.getMainResult();
	}
	
	
	
	/**
	 * Quick and dirty on the spot test.
	 * Expects server metrics web to be running on url and have profile(s) localhost_WINDOWS / localhost_LINUX 
	 * (or properly set SCRIPT profile) 
	 * @param args
	 */
	public static void main(String[] args) {
		Log4jConfigurationHelper.init(Level.INFO);
		ServerMetricsCaptureViaWeb ostest = new ServerMetricsCaptureViaWeb();
		additionalTestParametersMap.put(MARK59_METRICS_URL, "http://localhost:8085/mark59-metrics");	
		additionalTestParametersMap.put(SERVER_PROFILE_NAME, "localhost_" + MetricsUtils.obtainOperatingSystemForLocalhost());			
		// additionalTestParametersMap.put(SERVER_PROFILE_NAME, "remoteWinServer");   // 3 commands should fail			
		additionalTestParametersMap.put(MetricsApiConstants.PRINT_ERROR_MESSAGES,"short");   // 'short' 'full' 'no'			
		// to force Results summary to log:		
		Arguments jmeterParameters = ostest.getDefaultParameters();
		jmeterParameters.removeArgument(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY);
		jmeterParameters.addArgument(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(true));	
		// run 'localhost' test
		JavaSamplerContext context = new JavaSamplerContext( jmeterParameters );
		ostest.setupTest(context);
		ostest.runTest(context);
		
		
		ServerMetricsCaptureViaWeb groovyscripttest = new ServerMetricsCaptureViaWeb();
		additionalTestParametersMap.put(MARK59_METRICS_URL, "http://localhost:8085/mark59-metrics");	
		additionalTestParametersMap.put(SERVER_PROFILE_NAME, "SimpleScriptSampleRunner");		
		additionalTestParametersMap.put(MetricsApiConstants.PRINT_ERROR_MESSAGES,"short");   // 'short' 'full' 'no'	
		Arguments groovyscriptjmeterParameters = groovyscripttest.getDefaultParameters();
		groovyscriptjmeterParameters.removeArgument(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY);
		groovyscriptjmeterParameters.addArgument(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(true));	
		JavaSamplerContext groovyscriptcontext = new JavaSamplerContext(groovyscriptjmeterParameters);
		groovyscripttest.setupTest(groovyscriptcontext);
		groovyscripttest.runTest(groovyscriptcontext);	
	}
		

}