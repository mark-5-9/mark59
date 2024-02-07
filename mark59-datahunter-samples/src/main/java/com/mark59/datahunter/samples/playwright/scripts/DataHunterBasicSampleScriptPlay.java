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

package com.mark59.datahunter.samples.playwright.scripts;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.SafeSleep;
import com.mark59.datahunter.api.application.DataHunterConstants;
import com.mark59.datahunter.samples.dsl.helpers.DslConstants;
import com.mark59.scripting.KeepBrowserOpen;
import com.mark59.scripting.ScriptingConstants;
import com.mark59.scripting.playwright.JmeterFunctionsForPlaywrightScripts;
import com.mark59.scripting.playwright.PlaywrightAbstractJavaSamplerClient;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 *  This script provides a basic example of Mark59 framework usage for Playwright. It contains no 'DSL' classes etc,
 *   so that you can see the basics within the script.
 *   
 * <p>This type of scripting would be appropriate when our are knocking out a 'quick and dirty' script for a simple application. It will quickly
 *  become too limited, cumbersome and difficult to maintain for more complex application tests, where DSL style scripts could be used 
 *  (even if its just a repository of all well-named locators for the all target html elements).   
 * 
 * <p>This script performs a sub-set of the actions in performed by  {@link DataHunterLifecyclePvtScriptPlay}:
 * <ul>
 * <li>deletes 'DataHunter' rows for application 'DATAHUNTER_PV_TEST_BASIC', which have a 'lifecycle' based on the thread name</li>
 * <li>adds a single Policy for application 'DATAHUNTER_PV_TEST_BASIC' </li>
 * <li>creates a DataPoint </li>   
 * </ul> 
 *  
 * <p>Only some of the parameters available are included in the {@link #additionalTestParameters()} method below.  
 *  
 * @see PlaywrightAbstractJavaSamplerClient
 * 
 * @author Philip Webb
 * Written: Australian Summer 2023/24
 */
public class DataHunterBasicSampleScriptPlay  extends PlaywrightAbstractJavaSamplerClient {

	private static final Logger LOG = LogManager.getLogger(DataHunterBasicSampleScriptPlay.class);	
	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<>();
		// user defined parameters
		jmeterAdditionalParameters.put("DATAHUNTER_URL",			"http://localhost:8081/mark59-datahunter");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST_BASIC");
		jmeterAdditionalParameters.put("USER", 	 "default_user");	
		// mark59 predefined parameters 
		jmeterAdditionalParameters.put(ScriptingConstants.HEADLESS_MODE, String.valueOf(false));  // default is true
		jmeterAdditionalParameters.put(ScriptingConstants.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_BUFFERED_LOGS,	String.valueOf(false));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_SCREENSHOT, 		String.valueOf(false));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_PAGE_SOURCE, 		String.valueOf(false));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_PERF_LOG,			String.valueOf(false));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_STACK_TRACE,		String.valueOf(false));	
		
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(true));		
		return jmeterAdditionalParameters;			
	}
	

	@Override
	protected void runPlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page page){
		
// 		These log settings can be used to override log4j based defaults and log-related additionalTestParameters 
//		jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
//		jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
//		jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
//		jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);

		String thread = Thread.currentThread().getName();
		String lifecycle = "thread_" + thread;
//		System.out.println("Thread " + thread + " is running with LOG level " + LOG.getLevel());
		
		SafeSleep.sleep(1000);
		
		String dataHunterUrl 	= context.getParameter("DATAHUNTER_URL");
		String application 		= context.getParameter("DATAHUNTER_APPLICATION_ID");
		String user 			= context.getParameter("USER");
		
		jm.writeLog("kilroy", "txt", "Kilroy was here".getBytes());
		jm.bufferLog("kilroybuffer", "txt", "Kilroy was buffered here".getBytes());			
		
// 		delete any existing policies for this application/thread combination
		jm.startTransaction("DH_lifecycle_0001_loadInitialPage");
		page.navigate(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		page.locator("id=lifecycle").fill(lifecycle);		
		page.locator("id=submit").click();
		jm.endTransaction("DH_lifecycle_0001_loadInitialPage");
		
		jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies");
		Locator deleteSelectedItemsLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Delete Selected Items")); 
		page.onDialog(dialog -> dialog.accept());
		deleteSelectedItemsLink.click();
		checkSqlOk(page.locator("id=sqlResult"));		
		jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies");	
	
//		add a policy 		
		page.locator("a#AddPolicyLink").click();  //the Add Item link within the page (not Nav bar)
		
		page.locator("id=identifier").fill("DH-BASIC-POLICY");			
		page.locator("id=lifecycle").fill(lifecycle);				
		page.locator("id=useability").selectOption(DataHunterConstants.UNUSED);
		page.locator("id=otherdata").fill(user);	
		page.locator("id=epochtime").fill(Long.toString(System.currentTimeMillis()));	
//		jm.writeScreenshot("add_policy_DH-BASIC-POLICY");
		
		jm.startTransaction("DH_lifecycle_0200_addPolicy");
		page.locator("id=submit").click();		
		checkSqlOk(page.locator("id=sqlResult"));
		jm.endTransaction("DH_lifecycle_0200_addPolicy");
		
//		set a Data Point		
		long rowsAffected = Long.parseLong(page.locator("id=rowsAffected").innerHTML());
		LOG.debug( "rowsAffected : " + rowsAffected); 
		jm.userDataPoint(application + "_PolicyRowsAffected", rowsAffected);    // (expected to be always 1 for this action)		

		page.locator("//a[text()='Back']").click();	
		
//		jm.writeBufferedArtifacts();
	}

	
	private void checkSqlOk(Locator sqlResultWebElement) {
		String sqlResultText = sqlResultWebElement.innerHTML();
		if (sqlResultText==null || !sqlResultText.contains("PASS")) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ")");   
		}
	}

	
	/**
	 * A main method to assist with script testing outside JMeter.  The samples below demonstrate three ways of running the script: <br><br>
	 * 1.  Run a simple single instance, without extra thread-based parameterization (KeepBrowserOpen enumeration is optionally available).<br>
	 * 2.  Run multiple instances of the script, without extra thread-based parameterization <br> 
	 * 3.  Run multiple instances of the script, with extra thread-based parameterization, represented as a map with parameter name as key,
	 *     and values for each instance to be executed<br>  
	 * 4.  As for 3, but allows for the threads to iterate, and optionally to print a summary and/or output a CSV file in JMeter format. 
	 *     See method {@link #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File)} JavaDocs for more..
	 *     
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args) {
		Log4jConfigurationHelper.init(Level.INFO) ;
		DataHunterBasicSampleScriptPlay thisTest = new DataHunterBasicSampleScriptPlay();

		//1: single
		thisTest.runUiTest(KeepBrowserOpen.ONFAILURE);
		
		
		//2: multi-thread  (a. with and b. without KeepBrowserOpen option) 
//		thisTest.runMultiThreadedUiTest(2, 500);
//		thisTest.runMultiThreadedUiTest(2, 2000, KeepBrowserOpen.ONFAILURE);   
  

		//3: multi-thread with parms
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "false"));	
//		//  (a. with and b. without KeepBrowserOpen option)
//		thisTest.runMultiThreadedUiTest(4, 2000, threadParameters);
//		thisTest.runMultiThreadedUiTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE);	
		
		
		//4: multi-thread with parms, each thread iterating, optional summary printout and/or CSV file in JMeter format. See JavaDocs for details. 
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "false"));	
//		thisTest.runMultiThreadedUiTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE, 3, 1500, true, new File("C:/Mark59_Runs/csvSample.csv"));
	}

		
}
