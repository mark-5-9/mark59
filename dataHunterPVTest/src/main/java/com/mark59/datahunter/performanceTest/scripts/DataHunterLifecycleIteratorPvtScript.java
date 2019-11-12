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

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;

import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
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
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages._GenericDatatHunterActionPage;
import com.mark59.selenium.corejmeterimpl.JmeterFunctionsForSeleniumScripts;
import com.mark59.selenium.corejmeterimpl.KeepBrowserOpen;
import com.mark59.selenium.corejmeterimpl.SeleniumIteratorAbstractJavaSamplerClient;
import com.mark59.selenium.drivers.SeleniumDriverFactory;

//import com.mark59.selenium.corejmeterimpl.Mark59LogLevels;

/**
 * Similar test to DataHunterLifecyclePvtScript, except this test iterates via the  
 * the {@link #iterateSeleniumTest(JavaSamplerContext, JmeterFunctionsForSeleniumScripts, WebDriver)} method.
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
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<String, String>();
		
		jmeterAdditionalParameters.put(ITERATE_FOR_PERIOD_IN_SECS, 						"25");
		jmeterAdditionalParameters.put(ITERATE_FOR_NUMBER_OF_TIMES,  					 "0");
		jmeterAdditionalParameters.put(ITERATION_PACING_IN_SECS,  						"10");
		jmeterAdditionalParameters.put(STOP_THREAD_AFTER_TEST_START_IN_SECS,  			 "0");
		
		jmeterAdditionalParameters.put("DATAHUNTER_URL_HOST_PORT",	"http://localhost:8081");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_PV_TEST");
		jmeterAdditionalParameters.put("FORCE_TXN_FAIL_PERCENT", 	"20");
		jmeterAdditionalParameters.put("USER", 	user);
		jmeterAdditionalParameters.put("DRIVER", "CHROME");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NORMAL.toString());
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PROXY, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, "");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, 	String.valueOf(false));
		jmeterAdditionalParameters.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		return jmeterAdditionalParameters;			
	}
	
	
	/**
	 *  Initiate does a data clean-up (typically could also be an application logon)
	 */
	@Override
	protected void initiateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver) {
	
//		jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
//		jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);		
//		jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );
//		jm.logPerformanceLogAtEndOfTransactions(Mark59LogLevels.WRITE);
//		jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);		

		lifecycle 	= "thread_" + Thread.currentThread().getName(); ;
//		System.out.println("Thread " + lifecycle + " is running with LOG level " + LOG.getLevel());

		dataHunterUrl 		= context.getParameter("DATAHUNTER_URL_HOST_PORT");
		application 		= context.getParameter("DATAHUNTER_APPLICATION_ID");
		forceTxnFailPercent = new Integer(context.getParameter("FORCE_TXN_FAIL_PERCENT").trim());
		user 				= context.getParameter("USER");

// 		delete any existing policies for this application/thread combination
		jm.startTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		jm.endTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");	
		
		DeleteMultiplePoliciesPage deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPage(driver); 
		assertTrue("check init get url failed!", deleteMultiplePoliciesPage.doesPageContainText("Delete Multiple Policies Matching Selection Criteria" ));		
		deleteMultiplePoliciesPage.lifecycle().type(lifecycle);

		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit().submit();
		checkSqlOk(new DeleteMultiplePoliciesActionPage(driver));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
	}
	

	/**
	 * Iterate over a typical DataHunter lifecycle 
	 */
	@Override
	protected void iterateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {

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
			
			jm.startTransaction("DH-lifecycle-0200-addPolicy");
			addPolicyPage.submit().submit();	
			AddPolicyActionPage addPolicyActionPage = new AddPolicyActionPage(driver);			
			checkSqlOk(addPolicyActionPage);
			jm.endTransaction("DH-lifecycle-0200-addPolicy");
			
			addPolicyActionPage.backLink().click().waitUntilClickable( addPolicyPage.submit() );  // waitUntilClickable(..) isn't necessary here, just to show usage
		} 
		
		if (Thread.currentThread().getName().equals("THREAD NAME I WANT TO SIMULATE A FAILURE ON")){  // e.g. "main" for single thread test
			System.out.println("SIMULATING FAILURE ON THREAD " + Thread.currentThread().getName());
			throw new RuntimeException(" -- simulate failure on " + Thread.currentThread().getName() +" -- ");
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
		countPoliciesPage.useability().selectByVisibleText(TestConstants.UNUSED).thenSleep();   // thenSleep() isn't necessary here, just to show usage

		jm.startTransaction("DH-lifecycle-0300-countUnusedPolicies");
		countPoliciesPage.submit().submit();
		CountPoliciesActionPage countPoliciesActionPage = new CountPoliciesActionPage(driver);	
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
		
		jm.startTransaction("DH-lifecycle-0400-countUnusedPoliciesCurrentThread");		
		countPoliciesBreakdownPage.submit().submit();
		CountPoliciesBreakdownActionPage countPoliciesBreakdownActionPage = new CountPoliciesBreakdownActionPage(driver);	
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
		
		jm.startTransaction("DH-lifecycle-0500-useNextPolicy");		
		nextPolicyPage.submit().submit();
		NextPolicyActionPage nextPolicyActionPage = new NextPolicyActionPage(driver);		
		checkSqlOk(nextPolicyActionPage);			
		jm.endTransaction("DH-lifecycle-0500-useNextPolicy");	
		
		if (LOG.isDebugEnabled() ) {LOG.debug("useNextPolicy: " + application + "-" + lifecycle + " : " + nextPolicyActionPage.identifier() );	}
		
// 		delete multiple policies (test cleanup - a duplicate of the initial delete policies transactions)
		DeleteMultiplePoliciesPage deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPage(driver); 
		jm.startTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");		
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		jm.endTransaction("DH-lifecycle-0001-gotoDeleteMultiplePoliciesUrl");	
		deleteMultiplePoliciesPage.lifecycle().type(lifecycle);
		jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit().submit();
		checkSqlOk(new DeleteMultiplePoliciesActionPage(driver));
		jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies");	
		
//		jm.writeBufferedArtifacts();
	}

	
	/**
	 *  Finalize here just does another data clean-up (typically could be an application logoff)
	 */
	@Override
	protected void finalizeSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,	WebDriver driver) {
		driver.get(dataHunterUrl + TestConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + application);
		DeleteMultiplePoliciesPage deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPage(driver);
		deleteMultiplePoliciesPage.lifecycle().type(lifecycle);
		jm.startTransaction("DH-lifecycle-9999-finalize-deleteMultiplePolicies");		
		deleteMultiplePoliciesPage.submit().submit();
		checkSqlOk(new DeleteMultiplePoliciesActionPage(driver));
		jm.endTransaction("DH-lifecycle-9999-finalize-deleteMultiplePolicies");	
	}

	
	/**
	 *  Just as a demo, create some transaction and go the home page (in a real test you may want go to a logout page/option).
	 *  Will be triggered in this script, for example, if the 'assertTrue' returns 'false' (or any exception is thrown) . 	
	 */
	@Override
	protected void userActionsOnScriptFailure(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,	WebDriver driver) {
		// just as a demo, create some transaction and go to some random page (that is different to the page the simulated crash occurred
		jm.startTransaction("DH-lifecycle-9998-userActionsOnScriptFailure");
		driver.get(dataHunterUrl + "/dataHunter");	
		System.out.println("   -- page at userActionsOnScriptFailure has been changed to " + driver.getTitle() + " --");
		jm.endTransaction("DH-lifecycle-9998-userActionsOnScriptFailure");
	}
	

	private void checkSqlOk(_GenericDatatHunterActionPage _genericDatatHunterActionPage) {
		String sqlResultText = _genericDatatHunterActionPage.sqlResult().getText();
		if ( !"PASS".equals(sqlResultText) ) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " + _genericDatatHunterActionPage.formatResultsMessage(_genericDatatHunterActionPage.getClass().getName()));   
		}
	}
	
	
	/**
	 * A main method to assist with script testing outside Jmeter.  The samples below demonstrate three ways of running the script: <br><br>
	 * 1.  Run a simple single instance, without extra thread-based parameterization (KeepBrowserOpen enumeration is optionally available).<br>
	 * 2.  Run multiple instances of the script, without extra thread-based parameterization <br> 
	 * 3.  Run multiple instances of the script, with extra thread-based parameterization, represented as a map with parameter name as key, and values for each instance to be executed<br>  
	 * 
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args) throws InterruptedException{
		Log4jConfigurationHelper.init(Level.INFO) ;

		DataHunterLifecycleIteratorPvtScript thisTest = new DataHunterLifecycleIteratorPvtScript();

		//1: single
		thisTest.runSeleniumTest(KeepBrowserOpen.ONFAILURE);
		
		//2: multi-thread
//		thisTest.runMultiThreadedSeleniumTest(2, 2000);

		//3: multi-thread with parms
//		Map<String, java.util.List<String>>threadParameters = new java.util.LinkedHashMap<String,java.util.List<String>>();
//		threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));
//		threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "false"));		
//		thisTest.runMultiThreadedSeleniumTest(4, 2000, threadParameters);
	}
		
}
