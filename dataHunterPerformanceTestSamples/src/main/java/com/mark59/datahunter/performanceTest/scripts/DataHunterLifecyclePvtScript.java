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
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;

import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.AddPolicyActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.AddPolicyPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesBreakdownActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesBreakdownPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.DeleteMultiplePoliciesActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.DeleteMultiplePoliciesPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.NextPolicyActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.NextPolicyPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.PrintSelectedPoliciesActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.PrintSelectedPoliciesPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages._GenericDatatHunterActionPage;
import com.mark59.selenium.corejmeterimpl.JmeterFunctionsForSeleniumScripts;
import com.mark59.selenium.corejmeterimpl.KeepBrowserOpen;
import com.mark59.selenium.corejmeterimpl.SeleniumAbstractJavaSamplerClient;
import com.mark59.selenium.drivers.SeleniumDriverFactory;
import com.mark59.seleniumDSL.pageElements.HtmlTable;
import com.mark59.seleniumDSL.pageElements.HtmlTableRow;

//import com.mark59.selenium.corejmeterimpl.Mark59LogLevels;;

/**
 * This selenium test uses a style of DSL which we suggest would be suitable for most performance tests.<br><br>
 * For simple html pages (insignificant client-side javascript or ajax), a DSL not having element "wait until"s may suffice.
 * 
 * <p>Note the use of a <b>PAGE_LOAD_STRATEGY</b> of <b>NONE</b>.  This means you must control all page load timing in the script, usually
 * by waiting for an element on the next page to become available (for example by waiting for the first item on the page that you 
 * intend to click on becoming clickable).  
 * 
 * <p>**Note 1 and 2: the  waitUntilClickable(..) or thenSleep() methods are not necessary here, the simplicity of the pages don't require it.
 * Also, for Note 1, the <code>checkSqlOk</code> already has a wait built into it (<code>getText</code> for the SQL result, so that would give
 * you the correct timing). Included to demo what would be required in more difficult situations such as pages where you need to wait 
 * for async processes.
 * <br>
 * 
 * @see SeleniumAbstractJavaSamplerClient
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019
 * 
 * 
 */
public class DataHunterLifecyclePvtScript  extends SeleniumAbstractJavaSamplerClient {

	private static final Logger LOG = LogManager.getLogger(DataHunterLifecyclePvtScript.class);	

	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<String, String>();
		jmeterAdditionalParameters.put("DATAHUNTER_URL_HOST_PORT",	"http://localhost:8081");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST");
		jmeterAdditionalParameters.put("FORCE_TXN_FAIL_PERCENT", 	"20");
		jmeterAdditionalParameters.put("USER", 	 "default_user");		

		jmeterAdditionalParameters.put(SeleniumDriverFactory.DRIVER, "CHROME");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.BROWSER_DIMENSIONS, Mark59Constants.DEFAULT_BROWSER_DIMENSIONS);
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NONE.toString());
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, String.valueOf(false));
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");			
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
		int forceTxnFailPercent = Integer.valueOf(context.getParameter("FORCE_TXN_FAIL_PERCENT").trim());
		String user 			= context.getParameter("USER");

		DeleteMultiplePoliciesPage deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPage(driver); 

// 		delete any existing policies for this application/thread combination
		jm.startTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		deleteMultiplePoliciesPage.lifecycle().waitUntilClickable();
		jm.endTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");	
		
		deleteMultiplePoliciesPage.lifecycle().type(lifecycle);

		DeleteMultiplePoliciesActionPage deleteMultiplePoliciesActionPage = new DeleteMultiplePoliciesActionPage(driver);
		
		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit().submit().waitUntilClickable( deleteMultiplePoliciesActionPage.backLink() );   // ** note 1
		checkSqlOk(new DeleteMultiplePoliciesActionPage(driver));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
	
//		add a set of policies 		
		driver.get(dataHunterUrl + TestConstants.ADD_POLICY_URL_PATH + "?application=" + application);
		AddPolicyPage addPolicyPage = new AddPolicyPage(driver);
		
		for (int i = 1; i <= 5; i++) {
			addPolicyPage.identifier().type("TESTID" + i);
			addPolicyPage.lifecycle().type(lifecycle);
			addPolicyPage.useability().selectByVisibleText(TestConstants.UNUSED) ;
			addPolicyPage.otherdata().type(user);		
			addPolicyPage.epochtime().type(new String(Long.toString(System.currentTimeMillis())));
			//jm.writeScreenshot("add_policy_" + policy.getIdentifier());

			AddPolicyActionPage addPolicyActionPage = new AddPolicyActionPage(driver);			
			
			jm.startTransaction("DH-lifecycle-0200-addPolicy");
			addPolicyPage.submit().submit().waitUntilClickable( addPolicyActionPage.backLink() );   // ** note 1;	
			checkSqlOk(addPolicyActionPage);
			jm.endTransaction("DH-lifecycle-0200-addPolicy");
			
			addPolicyActionPage.backLink().click().waitUntilClickable( addPolicyPage.submit() ).thenSleep();;    // ** note 1 & note 2
		} 
	
//		dummy transaction just to test transaction failure behavior
		jm.startTransaction("DH-lifecycle-0299-sometimes-I-fail");
		int randomNum_1_to_100 = ThreadLocalRandom.current().nextInt(1, 101);
		if ( randomNum_1_to_100 >= forceTxnFailPercent ) {
			jm.endTransaction("DH-lifecycle-0299-sometimes-I-fail", Outcome.PASS);
		} else {
			jm.endTransaction("DH-lifecycle-0299-sometimes-I-fail", Outcome.FAIL);
		}
		
		driver.get(dataHunterUrl + TestConstants.COUNT_POLICIES_URL_PATH + "?application=" + application);
		CountPoliciesPage countPoliciesPage = new CountPoliciesPage(driver); 
		countPoliciesPage.useability().selectByVisibleText(TestConstants.UNUSED).thenSleep();   // ** note 2
		
		CountPoliciesActionPage countPoliciesActionPage = new CountPoliciesActionPage(driver);	

		jm.startTransaction("DH-lifecycle-0300-countUnusedPolicies");
		countPoliciesPage.submit().submit().waitUntilClickable( countPoliciesActionPage.backLink() );
		checkSqlOk(countPoliciesActionPage);
		jm.endTransaction("DH-lifecycle-0300-countUnusedPolicies");
		
		Long countPolicies = Long.valueOf( countPoliciesActionPage.rowsAffected().getText());
		LOG.debug( "countPolicies : " + countPolicies); 
		jm.userDataPoint(application + "_Total_Unused_Policy_Count", countPolicies);
		
// 		count breakdown (count for unused DATAHUNTER_PV_TEST policies for this thread )
		driver.get(dataHunterUrl + TestConstants.COUNT_POLICIES_BREAKDOWN_URL_PATH + "?application=" + application);		
		CountPoliciesBreakdownPage countPoliciesBreakdownPage = new CountPoliciesBreakdownPage(driver);
		countPoliciesBreakdownPage.applicationStartsWithOrEquals().selectByVisibleText(TestConstants.EQUALS);
		countPoliciesBreakdownPage.useability().selectByVisibleText(TestConstants.UNUSED);
		
		CountPoliciesBreakdownActionPage countPoliciesBreakdownActionPage = new CountPoliciesBreakdownActionPage(driver);	

		jm.startTransaction("DH-lifecycle-0400-countUnusedPoliciesCurrentThread");		
		countPoliciesBreakdownPage.submit().submit();
		checkSqlOk(countPoliciesBreakdownActionPage);		
		jm.endTransaction("DH-lifecycle-0400-countUnusedPoliciesCurrentThread");				
		
		// direct access to required row-column table element by computing the id:
		int countUsedPoliciesCurrentThread = countPoliciesBreakdownActionPage.getCountForBreakdown(application, lifecycle, TestConstants.UNUSED); 
		LOG.debug( "countUsedPoliciesCurrentThread : " + countUsedPoliciesCurrentThread); 
		jm.userDataPoint(application + "_This_Thread_Unused_Policy_Count", countUsedPoliciesCurrentThread);		
		
//		use next policy
		driver.get(dataHunterUrl + TestConstants.NEXT_POLICY_URL_PATH + "?application=" + application + "&pUseOrLookup=use");		
		NextPolicyPage nextPolicyPage = new NextPolicyPage(driver); 
		nextPolicyPage.lifecycle().type(lifecycle);
		nextPolicyPage.useability().selectByVisibleText(TestConstants.UNUSED);
		nextPolicyPage.selectOrder().selectByVisibleText(TestConstants.SELECT_MOST_RECENTLY_ADDED);

		NextPolicyActionPage nextPolicyActionPage = new NextPolicyActionPage(driver);		
		
		jm.startTransaction("DH-lifecycle-0500-useNextPolicy");		
		nextPolicyPage.submit().submit();
		checkSqlOk(nextPolicyActionPage);			
		jm.endTransaction("DH-lifecycle-0500-useNextPolicy");	
		
		if (LOG.isDebugEnabled() ) {LOG.debug("useNextPolicy: " + application + "-" + lifecycle + " : " + nextPolicyActionPage.identifier() );	}
		
		//HTML table demo.
		long used=0;
		long unused=0;
		
		driver.get(dataHunterUrl + TestConstants.PRINT_SELECTED_POLICIES_URL_PATH  + "?application=" + application);
		PrintSelectedPoliciesPage printSelectedPoliciesPage = new PrintSelectedPoliciesPage(driver);
		printSelectedPoliciesPage.submit().waitUntilClickable();

		PrintSelectedPoliciesActionPage printSelectedPoliciesActionPage = new PrintSelectedPoliciesActionPage(driver);
		
		jm.startTransaction("DH-lifecycle-0600-displaySelectedPolicies");		
		printSelectedPoliciesPage.submit().submit();
		checkSqlOk(printSelectedPoliciesActionPage);
		jm.endTransaction("DH-lifecycle-0600-displaySelectedPolicies");	
		
		HtmlTable printSelectedPoliciesTable = printSelectedPoliciesActionPage.printSelectedPoliciesTable();
		for (HtmlTableRow tableRow : printSelectedPoliciesTable.getHtmlTableRows()) {
			if (tableRow.getColumnNumberOfExpectedColumns(4, 8).getText().equals("USED"))   used++;
			if (tableRow.getColumnNumberOfExpectedColumns(4, 8).getText().equals("UNUSED")) unused++;
		}	
		jm.userDataPoint("USED-count-html-demo",   used );				
		jm.userDataPoint("UNUSED-count-html-demo", unused );	
		LOG.debug("HTML demo: USED=" + used + ", UNUSED=" + unused); 
		
// 		delete multiple policies (test cleanup - a duplicate of the initial delete policies transactions)
		jm.startTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");		
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		deleteMultiplePoliciesPage.lifecycle().waitUntilClickable();		
		jm.endTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");	
		
		deleteMultiplePoliciesPage.lifecycle().type(lifecycle);
		
		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit().submit();
		checkSqlOk(new DeleteMultiplePoliciesActionPage(driver));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
		
//		jm.writeBufferedArtifacts();
	}

	

	private void checkSqlOk(_GenericDatatHunterActionPage _genericDatatHunterActionPage) {
		String sqlResultText = _genericDatatHunterActionPage.sqlResult().getText() ;
		if ( !"PASS".equals(sqlResultText) ) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " + _genericDatatHunterActionPage.formatResultsMessage(_genericDatatHunterActionPage.getClass().getName()));   
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
		DataHunterLifecyclePvtScript thisTest = new DataHunterLifecyclePvtScript();

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
