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
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.SafeSleep;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.AddPolicyActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.AddPolicyPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesBreakdownActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesBreakdownPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.MultiplePoliciesActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.MultiplePoliciesPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.NextPolicyActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.NextPolicyPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages._GenericDataHunterActionPage;
import com.mark59.datahunter.samples.dsl.helpers.DslConstants;
import com.mark59.selenium.corejmeterimpl.JmeterFunctionsForSeleniumScripts;
import com.mark59.selenium.corejmeterimpl.KeepBrowserOpen;
import com.mark59.selenium.corejmeterimpl.SeleniumIteratorAbstractJavaSamplerClient;
import com.mark59.selenium.driversimpl.SeleniumDriverFactory;

/**
 * Similar test to DataHunterLifecyclePvtScript, except this test iterates via the  
 * {@link #iterateSeleniumTest(JavaSamplerContext, JmeterFunctionsForSeleniumScripts, WebDriver)} method.
 * 
 * <p>Note the addition of 'initiate' and 'finalize' methods, and also the additional test parameters required to control iteration timing.
 * 
 * <p>An example of  {@link #userActionsOnScriptFailure(JavaSamplerContext, JmeterFunctionsForSeleniumScripts, WebDriver)} has been 
 * included in this script.
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019
 * 
 * @see SeleniumIteratorAbstractJavaSamplerClient
 * @see DataHunterLifecyclePvtScript
 */
public class DataHunterLifecycleIteratorPvtScript  extends SeleniumIteratorAbstractJavaSamplerClient {

	private static final Logger LOG = Logger.getLogger(DataHunterLifecycleIteratorPvtScript.class);	

	String lifecycle;
	String dataHunterUrl;
	String application;
	String user = "default_user";
	int forceTxnFailPercent = 0;
	
	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<>();
		
		jmeterAdditionalParameters.put(ITERATE_FOR_PERIOD_IN_SECS, 						"25");
		jmeterAdditionalParameters.put(ITERATE_FOR_NUMBER_OF_TIMES,  					 "0");
		jmeterAdditionalParameters.put(ITERATION_PACING_IN_SECS,  						"10");
		jmeterAdditionalParameters.put(STOP_THREAD_AFTER_TEST_START_IN_SECS,  			 "0");
		
		jmeterAdditionalParameters.put("DATAHUNTER_URL",			"http://localhost:8081/mark59-datahunter");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST");
		jmeterAdditionalParameters.put("FORCE_TXN_FAIL_PERCENT", 	"20");
		jmeterAdditionalParameters.put("USER", 	user);
		jmeterAdditionalParameters.put(SeleniumDriverFactory.DRIVER, Mark59Constants.CHROME);
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.BROWSER_DIMENSIONS, "900,900");		
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NORMAL.toString());
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, 	String.valueOf(false));
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");	
//		jmeterAdditionalParameters.put(SeleniumDriverFactory.BROWSER_EXECUTABLE, "C:/win_chrom_ium_install_path/chrome.exe");
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(true));				
		return jmeterAdditionalParameters;			
	}
	
	
	/**
	 *  Initiate does a data clean-up (typically could also be an application logon)
	 */
	@Override
	protected void initiateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver) {
	
//      // import com.mark59.selenium.corejmeterimpl.Mark59LogLevels;;		
//		jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
//		jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
//		jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
//		jm.logPerformanceLogAtEndOfTransactions(Mark59LogLevels.WRITE);
//		// you need to use jm.writeBufferedArtifacts to output BUFFERed data (see end of this method)		
//		jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);
		
		lifecycle 	= "thread_" + Thread.currentThread().getName().replace(" ", "_").replace(".", "_");
//		System.out.println("Thread " + lifecycle + " is running with LOG level " + LOG.getLevel());
		
		// Start browser to cater for initial launch time (for Firefox try "about:preferences") 
		driver.get("chrome://version/");
		SafeSleep.sleep(1000);

		dataHunterUrl 		= context.getParameter("DATAHUNTER_URL");
		application 		= context.getParameter("DATAHUNTER_APPLICATION_ID");
		forceTxnFailPercent = Integer.parseInt(context.getParameter("FORCE_TXN_FAIL_PERCENT").trim());
		user 				= context.getParameter("USER");

		MultiplePoliciesPage multiplePoliciesPage = new MultiplePoliciesPage(driver); 
		MultiplePoliciesActionPage multiplePoliciesActionPage = new MultiplePoliciesActionPage(driver);
		
// 		delete any existing policies for this application/thread combination
		jm.startTransaction("DH_lifecycle_0001_loadInitialPage");
		driver.get(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		multiplePoliciesPage.lifecycle().waitUntilClickable();
		multiplePoliciesPage.lifecycle().type(lifecycle);
		multiplePoliciesPage.submit().submit().waitUntilClickable( multiplePoliciesActionPage.backLink() );		
		jm.endTransaction("DH_lifecycle_0001_loadInitialPage");	

		jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies");		
		multiplePoliciesActionPage.multipleDeleteLink().click().waitUntilAlertisPresent().acceptAlert();
		waitForSqlResultsTextOnActionPageAndCheckOk(multiplePoliciesActionPage);
		jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies");	
	}
	

	/**
	 * Iterate over a typical DataHunter lifecycle 
	 */
	@Override
	protected void iterateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {

//		add one policy 		
		driver.get(dataHunterUrl + DslConstants.ADD_POLICY_URL_PATH + "?application=" + application);
		SafeSleep.sleep(1000);
		AddPolicyPage addPolicyPage = new AddPolicyPage(driver);
		
		addPolicyPage.identifier().type("TESTID_ITER");
		addPolicyPage.lifecycle().type(lifecycle);
		addPolicyPage.useability().selectByVisibleText(DslConstants.UNUSED) ;
		addPolicyPage.otherdata().type(user);		
		addPolicyPage.epochtime().type(Long.toString(System.currentTimeMillis()));
		//jm.writeScreenshot("add_policy_" + policy.getIdentifier());
		
		jm.startTransaction("DH_lifecycle_0200_addPolicy");
		SafeSleep.sleep(200);  // Mocking a 200 ms txn delay		
		addPolicyPage.submit().submit();	
		AddPolicyActionPage addPolicyActionPage = new AddPolicyActionPage(driver);			
		waitForSqlResultsTextOnActionPageAndCheckOk(addPolicyActionPage);
		jm.endTransaction("DH_lifecycle_0200_addPolicy");
		
		
//		dummy transaction just to test transaction failure behavior 		
		jm.startTransaction("DH_lifecycle_0299_sometimes_I_fail");
		int randomNum_1_to_100 = ThreadLocalRandom.current().nextInt(1, 101);
		if ( randomNum_1_to_100 >= forceTxnFailPercent ) {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail", Outcome.PASS);
		} else {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail", Outcome.FAIL);
		}
		
		driver.get(dataHunterUrl + DslConstants.COUNT_POLICIES_URL_PATH + "?application=" + application);
		CountPoliciesPage countPoliciesPage = new CountPoliciesPage(driver); 
		countPoliciesPage.useability().selectByVisibleText(DslConstants.UNUSED).thenSleep();   // thenSleep() isn't necessary here, just to show usage

		jm.startTransaction("DH_lifecycle_0300_countUnusedPolicies");
		countPoliciesPage.submit().submit();
		CountPoliciesActionPage countPoliciesActionPage = new CountPoliciesActionPage(driver);	
		waitForSqlResultsTextOnActionPageAndCheckOk(countPoliciesActionPage);
		jm.endTransaction("DH_lifecycle_0300_countUnusedPolicies");
		
		long countPolicies = Long.parseLong(countPoliciesActionPage.rowsAffected().getText());
		LOG.debug( "countPolicies : " + countPolicies); 
		jm.userDataPoint(application + "_Total_Unused_Policy_Count", countPolicies);
		
// 		count breakdown (count for unused DATAHUNTER_PV_TEST policies for this thread )
		driver.get(dataHunterUrl + DslConstants.COUNT_POLICIES_BREAKDOWN_URL_PATH + "?application=" + application);		
		CountPoliciesBreakdownPage countPoliciesBreakdownPage = new CountPoliciesBreakdownPage(driver);
		countPoliciesBreakdownPage.applicationStartsWithOrEquals().selectByVisibleText(DslConstants.EQUALS);
		countPoliciesBreakdownPage.useability().selectByVisibleText(DslConstants.UNUSED);
		
		jm.startTransaction("DH_lifecycle_0400_countUnusedPoliciesCurrentThread");		
		countPoliciesBreakdownPage.submit().submit();
		CountPoliciesBreakdownActionPage countPoliciesBreakdownActionPage = new CountPoliciesBreakdownActionPage(driver);	
		waitForSqlResultsTextOnActionPageAndCheckOk(countPoliciesBreakdownActionPage);		
		jm.endTransaction("DH_lifecycle_0400_countUnusedPoliciesCurrentThread");				
		
		// direct access to required row-column table element by computing the id:
		int countUsedPoliciesCurrentThread = countPoliciesBreakdownActionPage.getCountForBreakdown(application, lifecycle, DslConstants.UNUSED); 
		LOG.debug( "countUsedPoliciesCurrentThread : " + countUsedPoliciesCurrentThread); 
		jm.userDataPoint(application + "_This_Thread_Unused_Policy_Count", countUsedPoliciesCurrentThread);		
		
//		use next policy
		driver.get(dataHunterUrl + DslConstants.NEXT_POLICY_URL_PATH + "?application=" + application + "&pUseOrLookup=use");		
		NextPolicyPage nextPolicyPage = new NextPolicyPage(driver); 
		nextPolicyPage.lifecycle().type(lifecycle);
		nextPolicyPage.useability().selectByVisibleText(DslConstants.UNUSED);
		nextPolicyPage.selectOrder().selectByVisibleText(DslConstants.SELECT_MOST_RECENTLY_ADDED);
		
		jm.startTransaction("DH_lifecycle_0500_useNextPolicy");		
		nextPolicyPage.submit().submit();
		NextPolicyActionPage nextPolicyActionPage = new NextPolicyActionPage(driver);		
		waitForSqlResultsTextOnActionPageAndCheckOk(nextPolicyActionPage);			
		jm.endTransaction("DH_lifecycle_0500_useNextPolicy");	
		
		if (LOG.isDebugEnabled() ) {LOG.debug("useNextPolicy: " + application + "-" + lifecycle + " : " + nextPolicyActionPage.identifier() );	}
		
// 		delete multiple policies (test cleanup - a duplicate of the initial delete policies transactions)

		MultiplePoliciesPage multiplePoliciesPage = new MultiplePoliciesPage(driver); 
		MultiplePoliciesActionPage multiplePoliciesActionPage = new MultiplePoliciesActionPage(driver);

		jm.startTransaction("DH_lifecycle_0099_gotoDeleteMultiplePoliciesUrl");		
		driver.get(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);		
		multiplePoliciesPage.lifecycle().waitUntilClickable();
		multiplePoliciesPage.lifecycle().type(lifecycle);
		multiplePoliciesPage.submit().submit().waitUntilClickable( multiplePoliciesActionPage.backLink() );				
		jm.endTransaction("DH_lifecycle_0099_gotoDeleteMultiplePoliciesUrl");	

		jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies");		
		multiplePoliciesActionPage.multipleDeleteLink().click().waitUntilAlertisPresent().acceptAlert();
		waitForSqlResultsTextOnActionPageAndCheckOk(multiplePoliciesActionPage);
		jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies");
		
//		jm.writeBufferedArtifacts();
	}

	
	/**
	 *  Finalize here just does another data clean-up (typically this method could be used for application logoff)
	 */
	@Override
	protected void finalizeSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,	WebDriver driver) {
		driver.get(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);		
		MultiplePoliciesPage multiplePoliciesPage = new MultiplePoliciesPage(driver); 
		MultiplePoliciesActionPage multiplePoliciesActionPage = new MultiplePoliciesActionPage(driver);
		multiplePoliciesPage.lifecycle().waitUntilClickable();
		multiplePoliciesPage.lifecycle().type(lifecycle);
		multiplePoliciesPage.submit().submit().waitUntilClickable( multiplePoliciesActionPage.backLink() );				

		jm.startTransaction("DH_lifecycle_9999_finalize_deleteMultiplePolicies");		
		multiplePoliciesActionPage.multipleDeleteLink().click().waitUntilAlertisPresent().acceptAlert();
		waitForSqlResultsTextOnActionPageAndCheckOk(multiplePoliciesActionPage);
		jm.endTransaction("DH_lifecycle_9999_finalize_deleteMultiplePolicies");	
	}

	
	/**
	 *  Just as a demo, create some transaction and go the home page (in a real test you may want go to a logout page/option).
	 *  Will be triggered in this script, for example, if the 'assertTrue' returns 'false' (or any exception is thrown) . 	
	 */
	@Override
	protected void userActionsOnScriptFailure(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,	WebDriver driver) {
		// just as a demo, create some transaction and go to some random page (that is different to the page the simulated crash occurred
		jm.startTransaction("DH_lifecycle_9998_userActionsOnScriptFailure");
		System.out.println("  -- page title at userActionsOnScriptFailure is " + driver.getTitle() + " --");
		jm.endTransaction("DH_lifecycle_9998_userActionsOnScriptFailure");
		SafeSleep.sleep(30000); // stop failures quickly repeating 
		driver.get(dataHunterUrl + "/mark59-datahunter");	
	}
	
	
	/*
	 * At first glance this may seem not to have any 'wait for element' conditions.  However the 'getText()'
	 * method (indirectly) invokes a Fluent Wait condition 
	 */
	private void waitForSqlResultsTextOnActionPageAndCheckOk(_GenericDataHunterActionPage _genericDatatHunterActionPage) {
		String sqlResultText = _genericDatatHunterActionPage.sqlResult().getText();
		if (sqlResultText==null || !sqlResultText.contains("PASS")) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " +
						_genericDatatHunterActionPage.formatResultsMessage(_genericDatatHunterActionPage.getClass().getName()));
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
		DataHunterLifecycleIteratorPvtScript thisTest = new DataHunterLifecycleIteratorPvtScript();

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
