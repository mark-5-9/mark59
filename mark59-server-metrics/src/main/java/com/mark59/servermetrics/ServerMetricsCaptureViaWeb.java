
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.servermetrics.utils.AppConstantsServerMetrics;
import com.mark59.servermetricsweb.controller.ServerMetricRestController;
import com.mark59.servermetricsweb.pojos.ParsedCommandResponse;
import com.mark59.servermetricsweb.pojos.ParsedMetric;
import com.mark59.servermetricsweb.pojos.WebServerMetricsResponsePojo;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb;
import com.mark59.servermetricsweb.utils.ServerMetricsWebUtils;

/**
 * @author Philip Webb
 * Written: Australian Autumn 2020 
 * 
 * This is the initiating class for web-mark59-server-metrics (metrics capture via API) 
 * 
 * @see ServerMetricRestController
 * 
 */
public class ServerMetricsCaptureViaWeb  extends AbstractJavaSamplerClient { 

	private static final Logger LOG = LogManager.getLogger(ServerMetricsCaptureViaWeb.class);
	
	public static final String MARK59_SERVER_METRICS_WEB_URL 			= "MARK59_SERVER_METRICS_WEB_URL";
	public static final String DEFAULT_MARK59_SERVER_METRICS_WEB_URL 	= "http://localhost:8085/mark59-server-metrics-web";

	public static final String SERVER_PROFILE_NAME 	= "SERVER_PROFILE_NAME";
	
	
	protected String thread = Thread.currentThread().getName();
	protected String tgName = null; 
	protected AbstractThreadGroup tg = null;
	
	
	
	private static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<String,String>();
	
		staticMap.put(MARK59_SERVER_METRICS_WEB_URL, DEFAULT_MARK59_SERVER_METRICS_WEB_URL );
		staticMap.put(SERVER_PROFILE_NAME, "" );

		staticMap.put("______________________ miscellaneous: ____________________", "");				
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");	
		
		staticMap.put("______________________ notes: _________________________________", "");	
		staticMap.put("__","- please replace the default url with your actual.");	
		staticMap.put("_", "- server profile of 'localhost' only reports metrics of the mark59-server-metrics-web machine!");	
		staticMap.put("-", "   use actual machine name or (better) via Excel 'localhost..' entry instead (see Mark59 User Guide)");	
		staticMap.put(".", "");		
		staticMap.put("build information: ", "mark59-server-metrics version " + AppConstantsServerMetrics.MARK59_SERVER_METRICS_VERSION);			
		
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
		
		if ( context.getJMeterContext() != null  && context.getJMeterContext().getThreadGroup() != null ) {
			tg     = context.getJMeterContext().getThreadGroup();
			tgName = tg.getName();
		}
		
		if (IpUtilities.localIPisNotOnListOfIPaddresses(context.getParameter(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST))){ 
			LOG.info("Thread Group " + tgName + " is stopping (not on 'Restrict to IP List')" );
			if (tg!=null) tg.stop();
			return null;
		}
		
		JmeterFunctions jm = new JmeterFunctionsImpl(Thread.currentThread().getName());
		BufferedReader in = null;
		Integer repsonseCode = null;
		WebServerMetricsResponsePojo response = null;
		String webServiceUrl = null;
		
		try {
			
			String reqServerProfileName = context.getParameter(SERVER_PROFILE_NAME); 
			
			webServiceUrl = context.getParameter(MARK59_SERVER_METRICS_WEB_URL) 	+ "/api/metric?reqServerProfileName=" + reqServerProfileName;
			LOG.debug("webServiceUrl : " + webServiceUrl);
			
			URL url = new URL(webServiceUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			repsonseCode = con.getResponseCode();
			
			in = new BufferedReader( new InputStreamReader(con.getInputStream()));
			String respLine;
			StringBuffer jsonResponseStr = new StringBuffer();
			while ((respLine = in.readLine()) != null) {
				jsonResponseStr.append(respLine);
			}
			in.close();

			response = new ObjectMapper().readValue(jsonResponseStr.toString(), WebServerMetricsResponsePojo.class );

			if ( response == null || response.getServerProfileName()  == null){
				throw new RuntimeException("Error : null repsonse or a null server profile name returned is an Unexpected Response!");
			}
			if ( response.getFailMsg().startsWith(AppConstantsServerMetricsWeb.SERVER_PROFILE_NOT_FOUND)){
				throw new RuntimeException("Error : " + response.getFailMsg());
			}

			for (ParsedCommandResponse parsedCommandResponse : response.getParsedCommandResponses()) {
				
				for (ParsedMetric parsedMetric  : parsedCommandResponse.getParsedMetrics()) {
					
					if (parsedMetric.getSuccess()) {
						jm.userDatatypeEntry(parsedMetric.getLabel(), parsedMetric.getResult().longValue(), JMeterFileDatatypes.valueOf(parsedMetric.getDataType()));
					} else {
						String metricFailsMsg = "Warning : Failed metric response for txn : " + parsedMetric.getLabel() + " (at: " + System.currentTimeMillis() + ")"  ; 
						System.out.println(metricFailsMsg);
						LOG.warn(metricFailsMsg +  "\n     command response : " + parsedCommandResponse.getCommandResponse() + "\n   api response msg  : " +  response.getFailMsg());
					};
				}
			}
			
		} catch (Exception | AssertionError e) {
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			String errorMsg = "Error: Unexpected Failure calling the Server Metrics Service at " + webServiceUrl + " message : \n" + e.getMessage() + "\n" + stackTrace.toString();
			LOG.error(errorMsg);
			System.out.println(errorMsg);
			if (response != null) {
				String erroredServerProfleMsg = "        occurred using server profile :" + response.getServerProfileName();	
				LOG.error(erroredServerProfleMsg);	
				System.out.println(erroredServerProfleMsg);	
			}
			LOG.debug("        last response-code from mark59-server-metrics-web was " + repsonseCode);
			LOG.debug("        last response from mark59-server-metrics-web was  \n" + response );
			if (in != null){try {in.close();} catch (IOException e1) {}};
		} finally {
			jm.tearDown();
		}

		return jm.getMainResult();
	}
	
	
	
	public static void main(String[] args) {
		// expects server metrics web to be running on url and have profile(s) localhost_WINDOWS / localhost_LINUX ( or properly set SCRIPT profile)
		Log4jConfigurationHelper.init(Level.INFO);
		ServerMetricsCaptureViaWeb ostest = new ServerMetricsCaptureViaWeb();
		additionalTestParametersMap.put(MARK59_SERVER_METRICS_WEB_URL, "http://localhost:8085/mark59-server-metrics-web");	
		additionalTestParametersMap.put(SERVER_PROFILE_NAME, "localhost_" + ServerMetricsWebUtils.obtainOperatingSystemForLocalhost());			
		JavaSamplerContext context = new JavaSamplerContext( ostest.getDefaultParameters()  );
		ostest.setupTest(context);
		ostest.runTest(context);
		
		ServerMetricsCaptureViaWeb groovyscripttest = new ServerMetricsCaptureViaWeb();
		additionalTestParametersMap.put(MARK59_SERVER_METRICS_WEB_URL, "http://localhost:8085/mark59-server-metrics-web");	
//		additionalTestParametersMap.put(SERVER_PROFILE_NAME, "NewRelicTestProfile");			
		additionalTestParametersMap.put(SERVER_PROFILE_NAME, "SimpleScriptSampleRunner");			
		JavaSamplerContext groovyscriptcontext = new JavaSamplerContext( groovyscripttest.getDefaultParameters()  );
		groovyscripttest.setupTest(groovyscriptcontext);
		groovyscripttest.runTest(groovyscriptcontext);	
	}
		

}