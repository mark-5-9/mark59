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
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.core.utils.SafeSleep;
import com.mark59.datahunter.samples.dsl.helpers.DslConstants;
import com.mark59.scripting.KeepBrowserOpen;
import com.mark59.scripting.ScriptingConstants;
import com.mark59.scripting.playwright.JmeterFunctionsForPlaywrightScripts;
import com.mark59.scripting.playwright.PlaywrightAbstractJavaSamplerClient;
import com.microsoft.playwright.Page;

/**
 * <p>This sample is meant to demonstrate how to structure a Mark59 playwright script (not how to use use DataHunter in a performance test -
 * see below). It uses a basic type of DSL (domain-specific language) which we suggest would be sufficient for most performance tests 
 * written using Playwright.
 *        
 * <p>The reason for a simple DSL structure in Playwright, compared to that provided in the Selenium examples, basically comes down to 
 * the fact Playwright implicitly provides <code>waitUntilClickable(..)</code> on retrieval of html elements, so there's not the need 
 * to explicitly control the waits (using custom FluentWaits) or a 'PAGE_LOAD_STRATEGY' as for Selenium (refer to the core 'Elemental' 
 * class in the DSL project). 
 * 
 * <p>In a performance test, DataHunter should be invoked using it's API. Review
 * {@link com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage}, and 
 * DataHunterLifecyclePvtScriptUsingApiViaHttpRequestsTestPlan.jmx (the test-plans folder in this project).  These samples replicate the basic 
 * functionality of this script, giving a three way comparison of the DataHunter UI usage, the DataHunter Java API Client, and direct http 
 * invocation of the API (actually 4 ways if you include the like-for=like Selenium script to this).     
 * 
 * @see PlaywrightAbstractJavaSamplerClient
 * 
 * @author Philip Webb
 * Written: Australian Summer 2023/24
 * 
 */
public class DataHunterLifecyclePvtScriptPlay  extends PlaywrightAbstractJavaSamplerClient {

	private static final Logger LOG = LogManager.getLogger(DataHunterLifecyclePvtScriptPlay.class);	
	
	
	/**
	 *  Construct the parameter map seen in the JMeter Java Request panel.  These values can be overridden in that panel.
	 *  <p>For example, it would be usual to have <code>HEADLESS_MODE</code> set to <code>false</code> here, so you can run the script 
	 *  in the IDE and see the browser, but the override the <code>HEADLESS_MODE</code> value to <code>true</code> in the JMeter test plan.
	 *  <p>Similarly for <code>PRINT_RESULTS_SUMMARY</code>.  You may want to see the results when running in the IDE, but set it to
	 *  <code>false</code> (which is also the default) when executing in JMeter, to minimize logging.       
	 */
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<>();
		
		// user defined parameters
		jmeterAdditionalParameters.put("DATAHUNTER_URL", "http://localhost:8081/mark59-datahunter");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST");
		jmeterAdditionalParameters.put("FORCE_TXN_FAIL_PERCENT", "20");
		jmeterAdditionalParameters.put("START_CDP_LISTENERS", String.valueOf(true));			
		jmeterAdditionalParameters.put("USER", "default_user");				

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
	protected void runPlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page page) {

// 	 	These log settings can be used to override log4j based defaults and transaction log-related additionalTestParameters 		
		//	jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
		//	jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
		//	jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
		//	jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
		//	// you need to use jm.writeBufferedArtifacts to output BUFFERed data (see end of this method)		
		//	jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);
		//	jm.writeLog("Kilroy","txt","Kilroy was here".getBytes());  //DIY log entry		
		//	jm.bufferLog("Kilroy","txt","Kilroy was buffered".getBytes());  //DIY buffered log entry				
		
		String lifecycle = "thread_" + Thread.currentThread().getName().replace(" ", "_").replace(".", "_");
//		System.out.println("Thread " + lifecycle + " is running with LOG level " + LOG.getLevel());
		
		SafeSleep.sleep(1000);
        
		String dataHunterUrl      = context.getParameter("DATAHUNTER_URL");
		String application        = context.getParameter("DATAHUNTER_APPLICATION_ID");
		int forceTxnFailPercent   = Integer.parseInt(context.getParameter("FORCE_TXN_FAIL_PERCENT").trim());
		boolean startNetListeners = Boolean.parseBoolean(context.getParameter("START_CDP_LISTENERS"));
		String user               = context.getParameter("USER");
				
		PrintSomeMsgOnceAtStartUp(dataHunterUrl, page);

		if (startNetListeners) {
			startNetworkListeners(jm, page);
		}

		DataHunterLocatorsPlay dhpage = new DataHunterLocatorsPlay(page); 

// 		select policies for this application/thread combination
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
	
//		add a set of policies 		
		dhpage.manangeMultipleItems_addItemLink().click(); 
		
		for (int i = 1; i <= 5; i++) {
			dhpage.identifier().fill("TESTID" + i);   
			dhpage.lifecycle().fill(lifecycle);
			dhpage.useabilityList().selectOption(DslConstants.UNUSED);
			dhpage.otherdata().fill(user);		
			dhpage.epochtime().fill(Long.toString(System.currentTimeMillis()));
//			jm.writeScreenshot("add_policy_TESTID" + i);
			
			jm.startTransaction("DH_lifecycle_0200_addPolicy");
			SafeSleep.sleep(200);  // Mocking a 200 ms txn delay
			dhpage.submitBtn().click();
			dhpage.backLink();
			waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);						 
			jm.endTransaction("DH_lifecycle_0200_addPolicy");
			
			dhpage.backLink().click(dhpage.andsleep);
		} 
	
//		dummy transaction just to test transaction failure behavior
		jm.startTransaction("DH_lifecycle_0299_sometimes_I_fail");
		int randomNum_1_to_100 = ThreadLocalRandom.current().nextInt(1, 101);
		if ( randomNum_1_to_100 >= forceTxnFailPercent ) {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail", Outcome.PASS);
		} else {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail", Outcome.FAIL);
		}
		
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
		
		//HTML table demo (force application as only parameter).
		page.navigate(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH  + "?application=" + application, dhpage.domContentLoaded);
		
		jm.startTransaction("DH_lifecycle_0600_displaySelectedPolicies");	
		dhpage.submitBtn().click();
		waitForSqlResultsTextOnActionPageAndCheckOk(dhpage);
		// demo how to extract a transaction time from with a running script 
		SampleResult sr_0600 = jm.endTransaction("DH_lifecycle_0600_displaySelectedPolicies");
		
		LOG.debug("Transaction " + sr_0600.getSampleLabel() + " ran at " + sr_0600.getTimeStamp() + " and took " + sr_0600.getTime() + " ms.");
		
		
		//TODO: simple html table use
/////////////////////////////////////////////////////////
		
//		long used=0;
//		long unused=0;
//		MultiplePoliciesActionPage printSelectedPoliciesActionPage = new MultiplePoliciesActionPage(driver);
//		HtmlTable printSelectedPoliciesTable = printSelectedPoliciesActionPage.printSelectedPoliciesTable();
//		for (HtmlTableRow tableRow : printSelectedPoliciesTable.getHtmlTableRows()) {
//			if (tableRow.getColumnNumberOfExpectedColumns(6, 10).getText().equals("USED"))   used++;
//			if (tableRow.getColumnNumberOfExpectedColumns(6, 10).getText().equals("UNUSED")) unused++;
//		}	
//		jm.userDataPoint("USED_count_html_demo",   used );				
//		jm.userDataPoint("UNUSED_count_html_demo", unused );	
		jm.userDataPoint("USED_count_html_demo",   5 );				
		jm.userDataPoint("UNUSED_count_html_demo", 9 );			
		
//		LOG.debug("HTML demo: USED=" + used + ", UNUSED=" + unused); 
		
		/////////////////////////////////////
		
// 		dele te multiple policies (test cleanup - a duplicate of the initial delete policies transactions)
		jm.startTransaction("DH_lifecycle_0099_gotoDeleteMultiplePoliciesUrl");	
		page.navigate(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + application, dhpage.domContentLoaded);
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

	
	private void startNetworkListeners(JmeterFunctionsImpl jm,Page page) {
        // page.onRequest(req -> { System.out.println( "ON_REQ Url: "+req.url()+", Type: "+req.resourceType()+", Method: "+req.method());});
        
        page.onResponse(res -> { 
			if ((res.request().url().contains("_action") || StringUtils.contains(jm.getMostRecentTransactionStarted(), "loadInitialPage"))
					&& "Document".equalsIgnoreCase(res.request().resourceType())
					&& jm.getMostRecentTransactionStarted() != null){
        		// System.out.println( "ON_RES Url: "+res.url()+" , Timing: " + res.request().timing().startTime);
				String urlAction = StringUtils.substringBeforeLast(StringUtils.substringAfter(res.url(), "mark59-datahunter/"), "?");
				String[] splitCurrTxn = StringUtils.split(jm.getMostRecentTransactionStarted(), "_", 4);
				String cdpTxnId = splitCurrTxn[0] + "_" + splitCurrTxn[1] + "_" + splitCurrTxn[2] + "__net_" + urlAction;
				jm.setTransaction(cdpTxnId, JMeterFileDatatypes.CDP, Double.valueOf(res.request().timing().responseStart).longValue(),true, "200");			
        	}; 
        });       
	}


	private void waitForSqlResultsTextOnActionPageAndCheckOk(DataHunterLocatorsPlay dhpage) {
		String sqlResultText = dhpage.sqlResult().innerText();	
		if (sqlResultText==null || !sqlResultText.contains("PASS")) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " +
					dhpage.formatResultsMessage(dhpage.getClass().getName()));
		}
	}

	
	private static synchronized void PrintSomeMsgOnceAtStartUp(String dataHunterUrl, Page page) {
		Properties sysprops = System.getProperties();
		if (!"true".equals(sysprops.get("printedOnce")) ) {	
			LOG.info(" Using DataHunter Url     : " + dataHunterUrl);
			LOG.info(" Browser Name and Version : " + page.context().browser().browserType().name() 
					+ " " + page.context().browser().version());
			sysprops.put("printedOnce", "true");
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
		DataHunterLifecyclePvtScriptPlay thisTest = new DataHunterLifecyclePvtScriptPlay();

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
