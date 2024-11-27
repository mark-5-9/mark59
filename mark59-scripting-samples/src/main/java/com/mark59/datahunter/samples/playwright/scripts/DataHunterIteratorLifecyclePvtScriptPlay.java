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
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.SafeSleep;
import com.mark59.datahunter.samples.dsl.helpers.DslConstants;
import com.mark59.scripting.KeepBrowserOpen;
import com.mark59.scripting.ScriptingConstants;
import com.mark59.scripting.playwright.JmeterFunctionsForPlaywrightScripts;
import com.mark59.scripting.playwright.PlaywrightIteratorAbstractJavaSamplerClient;
import com.microsoft.playwright.Page;

/**
 * Similar test to {@link DataHunterLifecyclePvtScriptPlay}, except this test iterates via the  
 * {@link #iteratePlaywrightTest(JavaSamplerContext, JmeterFunctionsForPlaywrightScripts, Page)  method.
 * 
 * <p>Note the addition of 'initiate' and 'finalize' methods, and also the additional test parameters required to control iteration timing.
 * 
 * <p>An example of {@link #userActionsOnScriptFailure(JavaSamplerContext, JmeterFunctionsForPlaywrightScripts, Page) has been 
 * included in this script.
 * 
 * @author Philip Webb
 * Written: Australian Summer 2023/24
 * 
 * @see PlaywrightIteratorAbstractJavaSamplerClient
 * @see DataHunterLifecyclePvtScriptPlay
 */
public class DataHunterIteratorLifecyclePvtScriptPlay  extends PlaywrightIteratorAbstractJavaSamplerClient {

	private static final Logger LOG = LogManager.getLogger(DataHunterIteratorLifecyclePvtScriptPlay.class);	
	
	String lifecycle;
	String dataHunterUrl;
	String application;
	String user = "default_user";
	int forceTxnFailPercent = 0;

	DataHunterLocatorsPlay dhpage;
	
	/**
	 *  Construct the parameter map seen in the JMeter Java Request panel.  These values can be overridden in that panel.
	 *  <p>For example, it would be usual to have <code>HEADLESS_MODE</code> set to <code>false</code> here, so you can run the script 
	 *  in the IDE and see the browser, but the override the <code>HEADLESS_MODE</code> value to <code>true</code> in the JMeter test plan.
	 *  <p>Similarly for <code>PRINT_RESULTS_SUMMARY</code>.  You may want to see the results when running in the IDE, but set it to
	 *  <code>false</code> (which is also the default) when executing in JMeter, to minimize logging.       
	 */
	//@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<>();
		
		// iteration parameters
		jmeterAdditionalParameters.put(ITERATE_FOR_PERIOD_IN_SECS, 						"25");
		jmeterAdditionalParameters.put(ITERATE_FOR_NUMBER_OF_TIMES,  					 "0");
		jmeterAdditionalParameters.put(ITERATION_PACING_IN_SECS,  						"10");
		jmeterAdditionalParameters.put(STOP_THREAD_AFTER_TEST_START_IN_SECS,  			 "0");
		jmeterAdditionalParameters.put(STOP_THREAD_ON_FAILURE,		    String.valueOf(false));			
		
		// user defined parameters
		jmeterAdditionalParameters.put("DATAHUNTER_URL", "http://localhost:8081/mark59-datahunter");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST");
		jmeterAdditionalParameters.put("FORCE_TXN_FAIL_PERCENT", "0");
		jmeterAdditionalParameters.put("USER", user);				

		// some optional playwright settings (defaults apply)
		jmeterAdditionalParameters.put(ScriptingConstants.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE, "");
		jmeterAdditionalParameters.put(ScriptingConstants.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(ScriptingConstants.PLAYWRIGHT_DEFAULT_TIMEOUT, "");
		jmeterAdditionalParameters.put(ScriptingConstants.PLAYWRIGHT_VIEWPORT_SIZE, "");		
		
		// optional logging settings (defaults apply) 
//		jmeterAdditionalParameters.put(JmeterFunctionsForPlaywrightScripts.LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS,Mark59LogLevels.DEFAULT.getName());
//		jmeterAdditionalParameters.put(JmeterFunctionsForPlaywrightScripts.LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());
//		jmeterAdditionalParameters.put(JmeterFunctionsForPlaywrightScripts.LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS,Mark59LogLevels.DEFAULT.getName());
//		jmeterAdditionalParameters.put(JmeterFunctionsForPlaywrightScripts.LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());
//		jmeterAdditionalParameters.put(JmeterFunctionsForPlaywrightScripts.LOG_PERF_LOG_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());		
//
//		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_BUFFERED_LOGS,	String.valueOf(true));
//		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_SCREENSHOT, 		String.valueOf(true));
//		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_PAGE_SOURCE, 		String.valueOf(true));
//		jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_STACK_TRACE,		String.valueOf(true));
		
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(true));		
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(false));		

		// optional miscellaneous settings (defaults apply) 
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");			
		jmeterAdditionalParameters.put(ScriptingConstants.EMULATE_NETWORK_CONDITIONS, "");			
	
		return jmeterAdditionalParameters;			
	}


	@Override
	protected void initiatePlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page page){

// 	 	These log settings can be used to override log4j based defaults and transaction log-related additionalTestParameters 		
		//	jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
		//	jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
		//	jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
		//	jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
		//	// you need to use jm.writeBufferedArtifacts to output BUFFERed data (see end of this method)		
		//	jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);
	
		lifecycle = "thread_" + Thread.currentThread().getName().replace(" ", "_").replace(".", "_");
//		System.out.println("Thread " + lifecycle + " is running with LOG level " + LOG.getLevel());
        
		dataHunterUrl       = context.getParameter("DATAHUNTER_URL");
		application         = context.getParameter("DATAHUNTER_APPLICATION_ID");
		forceTxnFailPercent = Integer.parseInt(context.getParameter("FORCE_TXN_FAIL_PERCENT").trim());
		user         		= context.getParameter("USER");

		dhpage = new DataHunterLocatorsPlay(page); 

// 		select and delete any existing policies for this application/thread combination
		jm.startTransaction("DH_lifecycle_0001_loadInitialPage");
		page.navigate(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application, dhpage.domContentLoaded);
		dhpage.lifecycle().fill(lifecycle);
		dhpage.submitBtn().click();
		dhpage.backLink().click(dhpage.waitUntilClickable);
		jm.endTransaction("DH_lifecycle_0001_loadInitialPage");	

		jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies");
		page.onDialog(dialog -> dialog.accept());
		dhpage.manangeMultipleItems_deleteSelectedItemsLink().click();
		waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);
		jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies");	
	}
		
	/**
	 * Iterate over a typical DataHunter lifecycle 
	 */
	@Override
	protected void iteratePlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page playwrightPage) {
		
//		add one policy 
		dhpage.navAddItemLink().click(dhpage.andsleep); 
		dhpage.identifier().fill("TESTID_ITER");   
		dhpage.lifecycle().fill(lifecycle);
		dhpage.useabilityList().selectOption(DslConstants.UNUSED);
		dhpage.otherdata().fill(user);		
		dhpage.epochtime().fill(Long.toString(System.currentTimeMillis()));
		// jm.writeScreenshot("add_policy TESTID_ITER");
		
		jm.startTransaction("DH_lifecycle_0200_addPolicy");
		SafeSleep.sleep(200);  // Mocking a 200 ms txn delay
		dhpage.submitBtn().click();
		dhpage.backLink().click(dhpage.waitUntilClickable);
		waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);						 
		jm.endTransaction("DH_lifecycle_0200_addPolicy");

//		dummy transaction just to test transaction failure behavior
		jm.startTransaction("DH_lifecycle_0299_sometimes_I_fail");
		int randomNum_1_to_100 = ThreadLocalRandom.current().nextInt(1, 101);
		if ( randomNum_1_to_100 >= forceTxnFailPercent ) {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail", Outcome.PASS);
		} else {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail", Outcome.FAIL);
		}
		
		//-_navigation.countItemsLink().click();
		dhpage.navCountItemsLink().click();
		dhpage.lifecycle().clear();
		dhpage.useabilityList().selectOption(DslConstants.UNUSED);

		jm.startTransaction("DH_lifecycle_0300_countUnusedPolicies");
		dhpage.submitBtn().click();
		waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);
		jm.endTransaction("DH_lifecycle_0300_countUnusedPolicies");
		
		long countPolicies = Long.parseLong(dhpage.rowsAffected().innerText());
		LOG.debug( "countPolicies : " + countPolicies); 
		jm.userDataPoint(application + "_Total_Unused_Policy_Count", countPolicies);

//	 	count breakdown - count for unused DATAHUNTER_PV_TEST policies  (by lifecycle) 	
		dhpage.navItemsBreakdownLink().click();	
		dhpage.applicationStartsWithOrEqualsList().selectOption(DslConstants.EQUALS);
		dhpage.useabilityList().selectOption(DslConstants.UNUSED);

		jm.startTransaction("DH_lifecycle_0400_countUnusedPoliciesCurrentThread");		
		dhpage.submitBtn().click();
		waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);		
		jm.endTransaction("DH_lifecycle_0400_countUnusedPoliciesCurrentThread");				
		
		// direct access to required row-column table element by computing the id:
		int countUsedPoliciesCurrentThread = dhpage.countItemsBreakdown_count(application, lifecycle, DslConstants.UNUSED); 
		LOG.debug( "countUsedPoliciesCurrentThread : " + countUsedPoliciesCurrentThread); 
		jm.userDataPoint(application + "_This_Thread_Unused_Policy_Count", countUsedPoliciesCurrentThread);	

//		use next policy
		dhpage.navUseNextItemLink().click();
		dhpage.lifecycle().fill(lifecycle);
		dhpage.useabilityList().selectOption(DslConstants.UNUSED);
		dhpage.selectOrderList().selectOption(DslConstants.SELECT_MOST_RECENTLY_ADDED);
		
		jm.startTransaction("DH_lifecycle_0500_useNextPolicy");		
		dhpage.submitBtn().click();
		waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);			
		jm.endTransaction("DH_lifecycle_0500_useNextPolicy");	
		
		if (LOG.isDebugEnabled() ) {LOG.debug("useNextPolicy: " + application + "-" + lifecycle + " : " + dhpage.identifier() );	}
	
// 		delete multiple policies (test cleanup - a duplicate of the initial delete policies transactions)
		
		jm.startTransaction("DH_lifecycle_0099_gotoDeleteMultiplePoliciesUrl");	
		dhpage.page.navigate(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application, dhpage.domContentLoaded);
		dhpage.lifecycle().fill(lifecycle);
		dhpage.submitBtn().click();
		dhpage.backLink().click(dhpage.waitUntilClickable);			
		jm.endTransaction("DH_lifecycle_0099_gotoDeleteMultiplePoliciesUrl");	
		
		jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies");		
		dhpage.manangeMultipleItems_deleteSelectedItemsLink().click();
		waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);
		jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies");
		
//		jm.writeBufferedArtifacts();
	}

	
	/**
	 *  Finalize here just does another data clean-up (typically this method could be used for application logoff)
	 */
	@Override
	protected void finalizePlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page playwrightPage){
		dhpage.page.navigate(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application, dhpage.domContentLoaded);
		dhpage.lifecycle().fill(lifecycle);
		dhpage.submitBtn().click();
		dhpage.backLink().click(dhpage.waitUntilClickable);

		jm.startTransaction("DH_lifecycle_9999_finalize_deleteMultiplePolicies");	
		// note you only need to a given dialog (onDialog..) once.  
		dhpage.manangeMultipleItems_deleteSelectedItemsLink().click();
		waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);
		jm.endTransaction("DH_lifecycle_9999_finalize_deleteMultiplePolicies");	
	};	
	
	
	/**
	 *  Just as a demo, create some transaction and remove any already created items to avoid duplicates (in a real test 
	 *  you may want go to a logout page/option).
	 *  <p>As you can see, even in this simple script attempting re-start logic can get quite complex, and is likely to have
	 *  some fragility.  
	 */
	@Override
	protected void userActionsOnScriptFailure(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page playwrightPage) {
		// just as a demo, create some transaction and go to some random page (that is different to the page the simulated crash occurred
		jm.startTransaction("DH_lifecycle_9998_userActionsOnScriptFailure");
		System.out.println("  -- page title at userActionsOnScriptFailure is " + dhpage.page.title() + " --");
		jm.endTransaction("DH_lifecycle_9998_userActionsOnScriptFailure");
		
		System.out.println("  -- attempt to recover (for when attempting more iters - clear up database)");
		SafeSleep.sleep(3000); 
		
		jm.startTransaction("DH_lifecycle_9998_onFail_clearUpPolicies");
		dhpage.page.navigate(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application, dhpage.domContentLoaded);
		dhpage.lifecycle().fill(lifecycle);  // this thread
		dhpage.submitBtn().click();
		dhpage.backLink().click(dhpage.waitUntilClickable);
		dhpage.manangeMultipleItems_deleteSelectedItemsLink().click();
		waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);
		jm.endTransaction("DH_lifecycle_9998_onFail_clearUpPolicies");	
	}	
	
	
	private void waitForSqlResultsTextOnActionPageAndCheckOk(DataHunterLocatorsPlay dhpage) {
		String sqlResultText = dhpage.sqlResult().innerText();	
		if (sqlResultText==null || !sqlResultText.contains("PASS")) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " +
					dhpage.formatResultsMessage(dhpage.getClass().getName()));
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
		DataHunterIteratorLifecyclePvtScriptPlay thisTest = new DataHunterIteratorLifecyclePvtScriptPlay();

		//1: single
		thisTest.runUiTest(KeepBrowserOpen.ONFAILURE);
		
		//2: multi-thread  (a. with and b. without KeepBrowserOpen option) 
//		thisTest.runMultiThreadedUiTest(2, 500);
//		thisTest.runMultiThreadedUiTest(2, 2000, KeepBrowserOpen.ONFAILURE);   

		//3: multi-thread with parms
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(ScriptingConstants.HEADLESS_MODE,	  java.util.Arrays.asList( "true"        , "false"    , "true"     , "false"));	
//		//  (a. with and b. without KeepBrowserOpen option)
//		thisTest.runMultiThreadedUiTest(4, 2000, threadParameters);
//		thisTest.runMultiThreadedUiTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE);	
		
		//4: multi-thread with parms, each thread iterating, optional summary printout and/or CSV file in JMeter format. See JavaDocs for details. 
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(ScriptingConstants.HEADLESS_MODE, 	  java.util.Arrays.asList( "true"        , "false"    , "true"     , "false"));	
//		thisTest.runMultiThreadedUiTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE, 3, 1500, true, new File("C:/Mark59_Runs/csvSample.csv"));
	}
	
}
