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

package com.mark59.datahunter.performanceTest.scripts;


import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.selenium.corejmeterimpl.JmeterFunctionsForSeleniumScripts;
import com.mark59.selenium.corejmeterimpl.KeepBrowserOpen;
import com.mark59.selenium.corejmeterimpl.SeleniumAbstractJavaSamplerClient;
import com.mark59.selenium.drivers.SeleniumDriverFactory;

//import com.mark59.selenium.corejmeterimpl.Mark59LogLevels;

/**
 * This selenium test provides a basic example of the Mark59 framework usage. It contains no 'DSL' classes etc, so that you can see the basics within the script.
 * <p>This type of scripting would be appropriate when our are knocking out a 'quick and dirty' script for a simple application. It will quickly become too 
 * limited, cumbersome and difficult to maintain for more complex application tests, where DSL style scripts should be used.   
 * 
 * <p>This script performs a sub-set of the actions in performed by DataHunterLifecyclePvtScript:
 * <ul>
 * <li>deletes 'DataHunter' rows for application 'DATAHUNTER_PV_TEST_BASIC', which have a 'lifecycle' based on the thread name) </li>
 * <li>adds a single Policy for application 'DATAHUNTER_PV_TEST_BASIC' </li>
 * <li>creates a DataPoint </li>   
 * </ul> 
 *  
 * @see SeleniumAbstractJavaSamplerClient
 * 
 * @author Philip Webb
 * Written: Australian Summer 2019/20
 * 
 * 
 */
public class DataHunterBasicSampleScript  extends SeleniumAbstractJavaSamplerClient {

	private static final Logger LOG = LogManager.getLogger(DataHunterBasicSampleScript.class);	

	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<String, String>();
		jmeterAdditionalParameters.put("DATAHUNTER_URL_HOST_PORT",	"http://localhost:8081");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST_BASIC");
		jmeterAdditionalParameters.put("USER", 	 "default_user");		
		jmeterAdditionalParameters.put("DRIVER", "CHROME");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NORMAL.toString());
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, 	String.valueOf(false));
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");				
		return jmeterAdditionalParameters;			
	}
	

	@Override
	protected void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {
		
//		jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
//		jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
//		jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
//		jm.logPerformanceLogAtEndOfTransactions(Mark59LogLevels.WRITE);
		// you need to use jm.writeBufferedArtifacts to output BUFFERed data (see end of this method)  		
//		jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);		

		String thread = Thread.currentThread().getName();
		String lifecycle = "thread_" + thread;
//		System.out.println("Thread " + thread + " is running with LOG level " + LOG.getLevel());

		String dataHunterUrl 	= context.getParameter("DATAHUNTER_URL_HOST_PORT");
		String application 		= context.getParameter("DATAHUNTER_APPLICATION_ID");
		String user 			= context.getParameter("USER");

// 		delete any existing policies for this application/thread combination
		
		jm.startTransaction("DH-basic-0001-gotoDeleteMultiplePoliciesUrl");
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		jm.endTransaction("DH-basic-0001-gotoDeleteMultiplePoliciesUrl");	
		
		driver.findElement(By.id("lifecycle")).sendKeys(lifecycle);  ; 
		
		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");
		driver.findElement(By.id("submit")).submit();
		checkSqlOk(driver.findElement(By.id("sqlResult")));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
	
//		add a policy 		
		driver.get(dataHunterUrl + TestConstants.ADD_POLICY_URL_PATH + "?application=" + application);

		driver.findElement(By.id("identifier")).sendKeys("DH-BASIC-POLICY"); 		
		driver.findElement(By.id("lifecycle")).sendKeys(lifecycle); 		
		
		Select dropdown = new Select(driver.findElement(By.id("useability")));
		dropdown.selectByVisibleText(TestConstants.UNUSED);

		driver.findElement(By.id("otherdata")).sendKeys(user); 
		driver.findElement(By.id("epochtime")).sendKeys(new String(Long.toString(System.currentTimeMillis()))); 
//		jm.writeScreenshot("add_policy_DH-BASIC-POLICY");
		
		jm.startTransaction("DH-lifecycle-0200-addPolicy");
		driver.findElement(By.id("submit")).submit();
		checkSqlOk(driver.findElement(By.id("sqlResult")));
		jm.endTransaction("DH-lifecycle-0200-addPolicy");
		
//		set a Data Point		
		Long rowsAffected = Long.valueOf(driver.findElement(By.id("rowsAffected")).getText());
		LOG.debug( "rowsAffected : " + rowsAffected); 
		jm.userDataPoint(application + "_PolicyRowsAffected", rowsAffected);    // (expected to be always 1 for this action)		

		driver.findElement(By.linkText("Back")).click();
		
//		jm.writeBufferedArtifacts();
	}

	

	private void checkSqlOk(WebElement sqlResultWebElement) {
		String sqlResultText = sqlResultWebElement.getText();
		if ( !"PASS".equals(sqlResultText) ) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ")");   
		}
	}
	

	
	/**
	 * A main method to assist with script testing outside JMeter.  The samples below demonstrate three ways of running the script: <br><br>
	 * 1.  Run a simple single instance, without extra thread-based parameterization (KeepBrowserOpen enumeration is optionally available).<br>
	 * 2.  Run multiple instances of the script, without extra thread-based parameterization <br> 
	 * 3.  Run multiple instances of the script, with extra thread-based parameterization, represented as a map with parameter name as key,
	 *     and values for each instance to be executed<br>  
	 * 
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args) throws InterruptedException{
		Log4jConfigurationHelper.init(Level.INFO) ;
		DataHunterBasicSampleScript thisTest = new DataHunterBasicSampleScript();

		//1: single
		thisTest.runSeleniumTest(KeepBrowserOpen.ONFAILURE);
		
		//2: multi-thread
//		thisTest.runMultiThreadedSeleniumTest(2, 2000);

		//3: multi-thread with parms
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "true"));		
//		thisTest.runMultiThreadedSeleniumTest(4, 2000, threadParameters);
	}

		
}
