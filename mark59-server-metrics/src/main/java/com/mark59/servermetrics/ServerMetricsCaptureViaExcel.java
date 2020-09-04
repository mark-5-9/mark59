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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.PropertiesKeys;
import com.mark59.core.utils.PropertiesReader;
import com.mark59.servermetrics.utils.AppConstantsServerMetrics;
import com.mark59.servermetricsweb.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.servermetricsweb.data.commandResponseParsers.dao.CommandResponseParsersDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.servermetricsweb.data.commandparserlinks.dao.CommandParserLinksDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.data.commands.dao.CommandsDAO;
import com.mark59.servermetricsweb.data.commands.dao.CommandsDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.servermetricsweb.data.servercommandlinks.dao.ServerCommandLinksDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.servermetricsweb.data.serverprofiles.dao.ServerProfilesDAOexcelWorkbookImpl;
import com.mark59.servermetricsweb.pojos.ParsedCommandResponse;
import com.mark59.servermetricsweb.pojos.WebServerMetricsResponsePojo;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb;
import com.mark59.servermetricsweb.utils.ServerMetricsWebUtils;
import com.mark59.servermetricsweb.utils.TargetServerCommandProcessor;


/**
 * This is the initiating class for server metrics capture using a (already downloaded) server metrics excel spreadsheet.
 * The location of the spreadsheet containing the server profile details is generally expected to set set via
 * the property 'mark59.server.profiles.excel.file.path' (in Mark59.properties).  An path override is also available
 * on the Java Request parameter list.    
 * 
 * <p>Using details provided on the spreadsheet, calls a functions which controls and executed commands on the target servers, 
 * and returns the formatted response.
 * 
 * @see TargetServerCommandProcessor
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2020 
 * 
 * 
 */
public class ServerMetricsCaptureViaExcel extends AbstractJavaSamplerClient { 

	private static final Logger LOG = LogManager.getLogger(ServerMetricsCaptureViaExcel.class);
	
	public static final String SERVER_PROFILE_NAME 	= "SERVER_PROFILE_NAME";
	public static final String OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH = "OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH";
	
	protected String thread = Thread.currentThread().getName();
	protected String tgName = null; 
	protected AbstractThreadGroup tg = null;

	
	private static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<String,String>();
		staticMap.put(SERVER_PROFILE_NAME, "localhost" );
		staticMap.put("______________________ miscellaneous: ____________________", "");
		staticMap.put(OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH, "");			
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");	
		staticMap.put("______________"       , "");			
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
		String testModeNo = "N";
		WebServerMetricsResponsePojo response = null;
		
		try {
			
			String parmServerProfileName = context.getParameter(SERVER_PROFILE_NAME); 

			String excelFilePath = context.getParameter(OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH);
			if (StringUtils.isAllBlank(excelFilePath)){
				excelFilePath =  PropertiesReader.getInstance().getProperty(PropertiesKeys.MARK59_PROP_SERVER_PROFILES_EXCEL_FILE_PATH);
			}
 	
			File excelFile = null;
        	try {
				excelFile = new File(excelFilePath);
			} catch (Exception e) {
				LOG.error("excel speadsheet " + excelFilePath + " file error. Msg : "  + e.getMessage());
				e.printStackTrace();
			}
        	LOG.debug("File excelFile path : full path  = " + excelFile.getPath() + " :"  + excelFile.getCanonicalPath() );
        	 
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
	        	
			response = TargetServerCommandProcessor.serverResponse(parmServerProfileName, testModeNo, serverProfilesDAO,
													serverCommandLinksDAO, commandsDAO, commandParserLinksDAO, commandResponseParsersDAO);
	 		workbook.close();

			if ( response == null || response.getServerProfileName()  == null){
				throw new RuntimeException("Error : null repsonse or a null server profile name returned is an Unexpected Response!");
			}
			if ( response.getFailMsg().startsWith(AppConstantsServerMetricsWeb.SERVER_PROFILE_NOT_FOUND)){
				throw new RuntimeException("Error : " + response.getFailMsg());
			}
	

			for (ParsedCommandResponse parsedCommandResponse : response.getParsedCommandResponses()) {
				
				if (Mark59Utils.resovesToTrue(parsedCommandResponse.getTxnPassed())){
					jm.userDatatypeEntry(
							parsedCommandResponse.getCandidateTxnId(), 
							(long)Math.round(Double.parseDouble(parsedCommandResponse.getParsedCommandResponse())),
							JMeterFileDatatypes.valueOf(parsedCommandResponse.getMetricTxnType()));
				} else {
					String metricFailsMsg = "Warning : Server Metrics (via Excel)  has recorded a failed metric response for txn : "
								+ parsedCommandResponse.getCandidateTxnId(); 
					LOG.warn(metricFailsMsg);
					System.out.println(metricFailsMsg);
					if (LOG.isDebugEnabled()){
						LOG.debug("command response : " + parsedCommandResponse.getCommandResponse());
						LOG.debug("parsed response : "  + parsedCommandResponse.getParsedCommandResponse());
					}
				};
				
			}
			
		} catch (Exception | AssertionError e) {
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			String errorMsg = "Error: Unexpected Failure during ServerMetricsCaptureViaExcel execution.\n" + e.getMessage() + "\n" + stackTrace.toString();
			LOG.error(errorMsg);
			System.out.println(errorMsg);
			if (response != null) {
				String erroredServerProfleMsg = "        occurred using server profile :" + response.getServerProfileName();	
				LOG.error(erroredServerProfleMsg);	
				System.out.println(erroredServerProfleMsg);	
			}
			LOG.debug("        last response from server was  \n" + response );
		} finally {
			jm.tearDown();
		}

		return jm.getMainResult();
	}
	
	
	
	public static void main(String[] args) { 
		Log4jConfigurationHelper.init(Level.INFO);
		//copy of test case..		 
		ServerMetricsCaptureViaExcel thistest = new ServerMetricsCaptureViaExcel();
		additionalTestParametersMap.put(OVERRIDE_PROPERTY_MARK59_SERVER_PROFILES_EXCEL_FILE_PATH,
				"./src/test/resources/simpleSheetWithLocalhostProfileForEachOs/mark59serverprofiles.xlsx");	
		additionalTestParametersMap.put(SERVER_PROFILE_NAME, "localhost_" + ServerMetricsWebUtils.obtainOperatingSystemForLocalhost());	
		JavaSamplerContext context = new JavaSamplerContext( thistest.getDefaultParameters()  );
		thistest.setupTest(context);
		thistest.runTest(context);
	}

}