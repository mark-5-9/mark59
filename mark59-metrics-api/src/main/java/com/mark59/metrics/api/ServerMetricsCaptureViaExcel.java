/*
 *  Copyright 2019 Mark59.com
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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.PropertiesKeys;
import com.mark59.core.utils.PropertiesReader;
import com.mark59.metrics.common.MetricsApiConstants;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAOexcelWorkbookImpl;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAOexcelWorkbookImpl;
import com.mark59.metrics.data.commands.dao.CommandsDAO;
import com.mark59.metrics.data.commands.dao.CommandsDAOexcelWorkbookImpl;
import com.mark59.metrics.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.metrics.data.servercommandlinks.dao.ServerCommandLinksDAOexcelWorkbookImpl;
import com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAOexcelWorkbookImpl;
import com.mark59.metrics.drivers.ServerProfileRunner;
import com.mark59.metrics.pojos.ParsedCommandResponse;
import com.mark59.metrics.pojos.WebServerMetricsResponsePojo;
import com.mark59.metrics.utils.MetricsConstants;

/**
 * 
 * <p>Intended for use as a Java Sampler in a JMeter test plan to capture metric data.
 *  
 * <p>It makes use of a  (previously downloaded) server metrics excel spreadsheet.  The spreadsheet can be 
 * downloaded from the Metrics web application (profiles page).
 * 
 * <p>The location of the spreadsheet containing the server profile details is generally expected to set set via
 * the property 'mark59.server.profiles.excel.file.path' (in mark59.properties).  An path override is also available
 * on the Java Request parameter list.    
 * 
 * <p>Using details provided on the spreadsheet, calls a functions which controls and executed commands on the target servers, 
 * and returns the formatted response.
 * 
 * @see ServerProfileRunner
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2020 
 */
public class ServerMetricsCaptureViaExcel extends AbstractJavaSamplerClient { 
	
//	static { 
//		// may be needed in future for poi : frequently outputting a WARN msg (https://bz.apache.org/bugzilla/show_bug.cgi?id=65326) 
//		org.apache.logging.log4j.core.config.Configurator.setLevel("org.apache.poi.util.XMLHelper", Level.ERROR);
//	}
	
	private static final Logger LOG = LogManager.getLogger(ServerMetricsCaptureViaExcel.class);
	
	public static final String SERVER_PROFILE_NAME 	= "SERVER_PROFILE_NAME";
	public static final String OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH = "OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH";	

	protected String tgName = null;
	protected AbstractThreadGroup tg = null;

	
	private static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<>();
		staticMap.put(SERVER_PROFILE_NAME, "localhost" );
		
		staticMap.put(".", "");	
		staticMap.put("_________________________ logging settings: _______________", "ERROR_MESSAGES values: 'short' (default), 'full', 'no'");
		staticMap.put(MetricsApiConstants.LOG_ERROR_MESSAGES, "short" );		
		staticMap.put(MetricsApiConstants.PRINT_ERROR_MESSAGES, "short" );
		staticMap.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(false));		
		staticMap.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(false));			
		staticMap.put("-", "");			
		staticMap.put("______________________ miscellaneous: ____________________", "");
		staticMap.put(OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH, "");
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");	
		staticMap.put("_", "");
		staticMap.put("___________________"       , "");			
		staticMap.put("build information: ", "mark59-metrics-api (via excel) Version: " + MetricsConstants.MARK59_VERSION_METRICS);			
		
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
		
		JmeterFunctions jm = new JmeterFunctionsImpl(context);
		String testModeNo = "N";
		WebServerMetricsResponsePojo response = null;
		
		try {
			
			String reqServerProfileName = context.getParameter(SERVER_PROFILE_NAME); 
			

			String excelFilePath = context.getParameter(OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH);
			if (StringUtils.isAllBlank(excelFilePath)){
				excelFilePath =  PropertiesReader.getInstance().getProperty(PropertiesKeys.MARK59_PROP_SERVER_PROFILES_EXCEL_FILE_PATH);
			}
 	
			File excelFile = null;
        	try {
				excelFile = new File(excelFilePath);
			} catch (Exception e) {
				LOG.error("excel speadsheet " + excelFilePath + " file error. Msg : "  + e.getMessage());
				System.out.println("excel speadsheet " + excelFilePath + " file error. Msg : "  + e.getMessage());
				e.printStackTrace();
			}
        	LOG.debug("File excelFile path: " + Objects.requireNonNull(excelFile).getPath()+":"+excelFile.getCanonicalPath() );
        	 
        	Workbook workbook = WorkbookFactory.create(excelFile, null, true);    // Factory class necessary to avoid excel file being 'touched' 
            
        	Sheet serverprofilesSheet 		  = workbook.getSheet("SERVERPROFILES");
        	Sheet servercommandlinksSheet	  = workbook.getSheet("SERVERCOMMANDLINKS");
        	Sheet commandsSheet 			  = workbook.getSheet("COMMANDS");
        	Sheet commandparserlinksSheet 	  = workbook.getSheet("COMMANDPARSERLINKS");
        	Sheet commandresponseparsersSheet = workbook.getSheet("COMMANDRESPONSEPARSERS");
        	
        	ServerProfilesDAO serverProfilesDAO 				= new ServerProfilesDAOexcelWorkbookImpl(serverprofilesSheet); 
        	ServerCommandLinksDAO serverCommandLinksDAO 		= new ServerCommandLinksDAOexcelWorkbookImpl(servercommandlinksSheet);    	
        	CommandsDAO commandsDAO 							= new CommandsDAOexcelWorkbookImpl(commandsSheet);     	
        	CommandParserLinksDAO commandParserLinksDAO 		= new CommandParserLinksDAOexcelWorkbookImpl(commandparserlinksSheet);
        	CommandResponseParsersDAO commandResponseParsersDAO = new CommandResponseParsersDAOexcelWorkbookImpl(commandresponseparsersSheet);
	        	
			response = ServerProfileRunner.commandsResponse(reqServerProfileName, testModeNo, serverProfilesDAO,
					serverCommandLinksDAO, commandsDAO, commandParserLinksDAO, commandResponseParsersDAO,
					MetricsApiConstants.RUNNING_VIA_EXCEL);
	 		workbook.close();

	 		ServerMetricsCaptureUtils.validateCommandsResponse(response);

			for (ParsedCommandResponse parsedCommandResponse : response.getParsedCommandResponses()) {
				ServerMetricsCaptureUtils.createJMeterTxnsUsingCommandResponse(context, jm, reqServerProfileName, parsedCommandResponse);				
			}
			
		} catch (Exception | AssertionError e) {
			ServerMetricsCaptureUtils.logUnexpectedException(response, e, "ServerMetricsCaptureViaExcel");
			LOG.debug("        last response from server was  \n" + response );
		} finally {
			jm.tearDown();	
		}

		return jm.getMainResult();
	}

	
	/**
	 * just to do some quick and dirty testing
	 * @param args
	 */
	public static void main(String[] args) { 
		Log4jConfigurationHelper.init(Level.INFO);
		org.apache.logging.log4j.core.config.Configurator.setLevel("org.apache.poi.util.XMLHelper", Level.ERROR);
		
		//from test cases..		 
		ServerMetricsCaptureViaExcel ostest = new ServerMetricsCaptureViaExcel();
		additionalTestParametersMap.put(OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH,
				"./src/test/resources/simpleXlsx/mark59serverprofiles.xlsx");	
		additionalTestParametersMap.put(SERVER_PROFILE_NAME, "localhost_" + Mark59Utils.obtainOperatingSystemForLocalhost());	
		additionalTestParametersMap.put(MetricsApiConstants.PRINT_ERROR_MESSAGES,"short");   // 'short' 'full' 'no'
		// to force Results summary to log:	
		Arguments jmeterParameters = ostest.getDefaultParameters();
		jmeterParameters.removeArgument(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY);
		jmeterParameters.addArgument(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(true));	
		// run 'localhost' test
		JavaSamplerContext context = new JavaSamplerContext( jmeterParameters );
		ostest.setupTest(context);
		ostest.runTest(context);

		ServerMetricsCaptureViaExcel groovyscripttest = new ServerMetricsCaptureViaExcel();
		additionalTestParametersMap.put(OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH,
				"./src/test/resources/simpleXlsx/mark59serverprofiles.xlsx");	
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