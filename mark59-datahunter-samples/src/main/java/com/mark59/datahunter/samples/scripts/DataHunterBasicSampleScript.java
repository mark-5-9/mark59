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

package com.mark59.datahunter.samples.scripts;


import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.SafeSleep;
import com.mark59.datahunter.samples.dsl.helpers.DslConstants;
import com.mark59.selenium.corejmeterimpl.JmeterFunctionsForSeleniumScripts;
import com.mark59.selenium.corejmeterimpl.KeepBrowserOpen;
import com.mark59.selenium.corejmeterimpl.SeleniumAbstractJavaSamplerClient;
import com.mark59.selenium.driversimpl.SeleniumDriverFactory;

/**
 * This selenium test provides a basic example of the Mark59 framework usage. It contains no 'DSL' classes etc, so that you can see the basics 
 * within the script.
 * <p>This type of scripting would be appropriate when our are knocking out a 'quick and dirty' script for a simple application. It will quickly
 *  become too limited, cumbersome and difficult to maintain for more complex application tests, where DSL style scripts should be used.   
 * 
 * <p>This script performs a sub-set of the actions in performed by  {@link DataHunterLifecyclePvtScript}:
 * <ul>
 * <li>deletes 'DataHunter' rows for application 'DATAHUNTER_PV_TEST_BASIC', which have a 'lifecycle' based on the thread name</li>
 * <li>adds a single Policy for application 'DATAHUNTER_PV_TEST_BASIC' </li>
 * <li>creates a DataPoint </li>   
 * </ul> 
 *  
 * <p>Only some of the parameters available are included in the {@link #additionalTestParameters()} method below.  
 * Refer to the {@link DataHunterLifecyclePvtScript} source code for a more complete example of the options available.     
 *  
 * @see SeleniumAbstractJavaSamplerClient
 * 
 * @author Philip Webb
 * Written: Australian Summer 2019/20
 */
public class DataHunterBasicSampleScript  extends SeleniumAbstractJavaSamplerClient {

	private static final Logger LOG = LogManager.getLogger(DataHunterBasicSampleScript.class);	

	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<>();
		// user defined parameters
		jmeterAdditionalParameters.put("DATAHUNTER_URL",			"http://localhost:8081/mark59-datahunter");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST_BASIC");
		jmeterAdditionalParameters.put("USER", 	 "default_user");	
		// mark59 defined parameters 
		jmeterAdditionalParameters.put(SeleniumDriverFactory.DRIVER, Mark59Constants.CHROME);   // FIREFOX
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NORMAL.toString());
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, 	String.valueOf(false));
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");	
		
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_BUFFERED_LOGS,	String.valueOf(false));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_SCREENSHOT, 		String.valueOf(false));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_PAGE_SOURCE, 		String.valueOf(false));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_PERF_LOG,			String.valueOf(false));
		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_STACK_TRACE,		String.valueOf(false));	
		
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(true));		
		return jmeterAdditionalParameters;			
	}
	

	@Override
	protected void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {
		
// 		These log settings can be used to override log4j based defaults and log-related additionalTestParameters 
//		jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
//		jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
//		jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
//		jm.logPerformanceLogAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);

		String thread = Thread.currentThread().getName();
		String lifecycle = "thread_" + thread;
//		System.out.println("Thread " + thread + " is running with LOG level " + LOG.getLevel());
		
		// Start browser to cater for initial launch time 
		driver.get("chrome://version/");
		SafeSleep.sleep(1000);
		
		String dataHunterUrl 	= context.getParameter("DATAHUNTER_URL");
		String application 		= context.getParameter("DATAHUNTER_APPLICATION_ID");
		String user 			= context.getParameter("USER");

// 		delete any existing policies for this application/thread combination
		jm.writeLog("kilroy", "txt", "Kilroy was here".getBytes());
		jm.startTransaction("DH_lifecycle_0001_loadInitialPage");
		jm.bufferLog("kilroybuffer", "txt", "Kilroy was buffered here".getBytes());		
		driver.get(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		driver.findElement(By.id("lifecycle")).sendKeys(lifecycle);
		driver.findElement(By.id("submit")).submit();	
		jm.endTransaction("DH_lifecycle_0001_loadInitialPage");
		
		jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies");
		driver.findElement(By.partialLinkText("Delete Selected Items")).click();
		Alert alert = driver.switchTo().alert();
		alert.accept();		
		checkSqlOk(driver.findElement(By.id("sqlResult")));
		jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies");	
	
//		add a policy 		
		driver.get(dataHunterUrl + DslConstants.ADD_POLICY_URL_PATH + "?application=" + application);

		driver.findElement(By.id("identifier")).sendKeys("DH-BASIC-POLICY"); 		
		driver.findElement(By.id("lifecycle")).sendKeys(lifecycle); 		
		
		Select dropdown = new Select(driver.findElement(By.id("useability")));
		dropdown.selectByVisibleText(DslConstants.UNUSED);

		driver.findElement(By.id("otherdata")).sendKeys(user); 
		driver.findElement(By.id("epochtime")).sendKeys(Long.toString(System.currentTimeMillis()));
//		jm.writeScreenshot("add_policy_DH-BASIC-POLICY");
		
		jm.startTransaction("DH_lifecycle_0200_addPolicy");
		driver.findElement(By.id("submit")).submit();
		checkSqlOk(driver.findElement(By.id("sqlResult")));
		jm.endTransaction("DH_lifecycle_0200_addPolicy");
		
//		set a Data Point		
		long rowsAffected = Long.parseLong(driver.findElement(By.id("rowsAffected")).getText());
		LOG.debug( "rowsAffected : " + rowsAffected); 
		jm.userDataPoint(application + "_PolicyRowsAffected", rowsAffected);    // (expected to be always 1 for this action)		

		driver.findElement(By.linkText("Back")).click();
		
//		jm.writeBufferedArtifacts();
	}
	

	private void checkSqlOk(WebElement sqlResultWebElement) {
		String sqlResultText = sqlResultWebElement.getText();
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
	 *     See method {@link #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File)} JavaDocs for more..
	 *     
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args) {
		Log4jConfigurationHelper.init(Level.INFO) ;
		DataHunterBasicSampleScript thisTest = new DataHunterBasicSampleScript();

		//1: single
		thisTest.runSeleniumTest(KeepBrowserOpen.ONFAILURE);
		
		
		//2: multi-thread  (a. with and b. without KeepBrowserOpen option) 
//		thisTest.runMultiThreadedSeleniumTest(2, 500);
//		thisTest.runMultiThreadedSeleniumTest(2, 2000, KeepBrowserOpen.ONFAILURE);   
  

		//3: multi-thread with parms
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "false"));	
//		//  (a. with and b. without KeepBrowserOpen option)
//		thisTest.runMultiThreadedSeleniumTest(4, 2000, threadParameters);
//		thisTest.runMultiThreadedSeleniumTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE);	
		
		
		//4: multi-thread with parms, each thread iterating, optional summary printout and/or CSV file in JMeter format. See JavaDocs for details. 
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "false"));	
//		thisTest.runMultiThreadedSeleniumTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE, 3, 1500, true, new File("C:/Mark59_Runs/csvSample.csv"));
	}
		
}
