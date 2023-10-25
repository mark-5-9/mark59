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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.Outcome;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.datahunter.api.application.DataHunterConstants;
import com.mark59.datahunter.api.data.beans.Policies;
import com.mark59.datahunter.api.model.AsyncMessageaAnalyzerResult;
import com.mark59.datahunter.api.model.CountPoliciesBreakdown;
import com.mark59.datahunter.api.model.DataHunterRestApiResponsePojo;
import com.mark59.datahunter.api.rest.DataHunterRestApiClient;
import com.mark59.datahunter.samples.dsl.helpers.DslConstants;
import com.mark59.selenium.corejmeterimpl.SeleniumAbstractJavaSamplerClient;


/**
 * This sample demonstrates the DataHunter API using the Java API Client {@link com.mark59.datahunter.api.rest.DataHunterRestApiClient}
 * 
 * <p>It is also an example of writing a generic Java class compatible with JMeter's 'Java Sampler', (ie, directly extends
 * AbstractJavaSamplerClient rather than using Mark59's SeleniumAbstractJavaSamplerClient class}, but does allow for timing of events via the
 * explicit creation of a (Mark59) JmeterFunctionsImpl instance.
 * 
 * <p>The first part of the runTest method replicates the functionality of the {@link DataHunterLifecyclePvtScript} sample script, 
 * to help show the relationship between the DataHunter HTML pages, and the corresponding functionality in the DataHunter API.
 * 
 * <p>Toward the end of the runTest method, code similar to that for DataHunterRestApiClientSampleUsage.asyncLifeCycleTestWithUseabilityUpdate() 
 * in the mark59-datahunter-api project been included, to given an example of how turn the results of the DataHunter Java Api async matching 
 * process into JMeter transactions.
 * 
 * <p>A more complete coverage of samples calls to the DataHunter Java API Client is available at 
 * {@link com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage} 
 * 
 * <p>The DataHunter API can also be called directly using http.  Please review test plan 
 * DataHunterLifecyclePvtScriptUsingApiViaHttpRequestsTestPlan.jmx  (the test-plans folder in this project).
 * Again, this replicates the basic functionality of the {@link DataHunterLifecyclePvtScript} sample script, to give a three way comparison
 * of the DataHunter HTML pages, the DataHunter Java API Client, and direct http invocation of the API.    
 *      
 * 
 * @see SeleniumAbstractJavaSamplerClient
 * @see DataHunterLifecyclePvtScript
 * @see com.mark59.datahunter.api.rest.DataHunterRestApiClient
 * @see com.mark59.datahunter.api.rest.samples.DataHunterRestApiClientSampleUsage
 *  
 * @author Philip Webb
 * Written: Australian Winter 2019
 * 
 */
public class DataHunterLifecyclePvtNoSeleniumUsesRestApi extends AbstractJavaSamplerClient    {

	private static final Logger LOG = LogManager.getLogger(DataHunterLifecyclePvtNoSeleniumUsesRestApi.class);	
	

	protected static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<>(); 
		staticMap.put("___________________"       , "");			
		staticMap.put("script build information: ", "using mark59-datahunter-api Version: " + Mark59Constants.MARK59_VERSION);				
		defaultArgumentsMap = Collections.unmodifiableMap(staticMap);
	}
	
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<>();
		// user defined parameters
		jmeterAdditionalParameters.put("DATAHUNTER_URL", "http://localhost:8081/mark59-datahunter");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID_CLIENT_API", "DATAHUNTER_PV_TEST_CLIENT_API");
		jmeterAdditionalParameters.put("FORCE_TXN_FAIL_PERCENT", "20");		
		jmeterAdditionalParameters.put("USER", "user");			
		jmeterAdditionalParameters.put("CLEAN_DATA_AT_SCRIPT_END", String.valueOf(true));
		// print to (JMeter) log:
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(true)); 
		// print to console:
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(false));
		return jmeterAdditionalParameters;			
	}	

	@Override
	public Arguments getDefaultParameters() {
		return Mark59Utils.mergeMapWithAnOverrideMap(defaultArgumentsMap, additionalTestParameters());
	}
	
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		
		JmeterFunctionsImpl jm = new JmeterFunctionsImpl(context);
		String lifecycle = "thread_" + Thread.currentThread().getName();
		
		/* replicating DataHunterLifecyclePvtScript - but using DataHunterRestApiClient calls instead of
		 * invoking the DataHunter UI via Selenium  */ 
		
		String dataHunterUrl         = context.getParameter("DATAHUNTER_URL");
		String applicationClientApi  = context.getParameter("DATAHUNTER_APPLICATION_ID_CLIENT_API");
		int forceTxnFailPercent      = Integer.parseInt(context.getParameter("FORCE_TXN_FAIL_PERCENT").trim());
		String user                  = context.getParameter("USER");
		Boolean cleanDataAtScriptEnd = Boolean.valueOf(context.getParameter("CLEAN_DATA_AT_SCRIPT_END"));
				
		DataHunterRestApiClient dhApiClient = new DataHunterRestApiClient(dataHunterUrl	);
		DataHunterRestApiResponsePojo response;
	
		jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies_clientApi");
		response = dhApiClient.deleteMultiplePolicies(applicationClientApi, null, null);
		jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies_clientApi");	
		confirmValidResponse(response, jm);

		LOG.info(jm.getMostRecentTransactionStarted() + " - number of rows deleted = " + response.getRowsAffected());
		
		Policies policy = new Policies();
		policy.setApplication(applicationClientApi);
		policy.setLifecycle(lifecycle);
		policy.setUseability(DslConstants.UNUSED);
		policy.setOtherdata(user);
		
		for (int i = 1; i <= 5; i++) {
			policy.setIdentifier("TESTAPIID" + i);
			policy.setEpochtime(System.currentTimeMillis());

			jm.startTransaction("DH_lifecycle_0200_addPolicy_clientApi");
			response = dhApiClient.addPolicy(policy);			
			jm.endTransaction("DH_lifecycle_0200_addPolicy_clientApi");			
			confirmValidResponse(response, jm);

			LOG.info(jm.getMostRecentTransactionStarted() + " - added id " + ((Policies)response.getPolicies().get(0)).getIdentifier());
		} 
	
//		just copied from DataHunterLifecyclePvtScript
		jm.startTransaction("DH_lifecycle_0299_sometimes_I_fail_clientApi");
		int randomNum_1_to_100 = ThreadLocalRandom.current().nextInt(1, 101);
		if ( randomNum_1_to_100 >= forceTxnFailPercent ) {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail_clientApi", Outcome.PASS);
		} else {
			jm.endTransaction("DH_lifecycle_0299_sometimes_I_fail_clientApi", Outcome.FAIL);
		}
		
		jm.startTransaction("DH_lifecycle_0300_countUnusedPolicies_clientApi");
		response = dhApiClient.countPolicies(applicationClientApi, null, DslConstants.UNUSED);
		jm.endTransaction("DH_lifecycle_0300_countUnusedPolicies_clientApi");		
		confirmValidResponse(response, jm);

		long countPolicies = response.getRowsAffected();
		LOG.info(applicationClientApi + "_Total_Unused_Policy_Count : " + countPolicies); 
		jm.userDataPoint(applicationClientApi + "_Total_Unused_Policy_Count", countPolicies);
		
//	 	count breakdown - counts for unused DATAHUNTER_PV_TEST_CLIENT_API policies (by lifecycle).
//		(Not a very realistic use, but keeps this transaction in line with the corresponding one in DataHunterLifecyclePvtScript).   		
		jm.startTransaction("DH_lifecycle_0400_countUnusedPoliciesCurrentThread_clientApi");		
		response = dhApiClient.policiesBreakdown(DslConstants.EQUALS, applicationClientApi, null, DslConstants.UNUSED);
		jm.endTransaction("DH_lifecycle_0400_countUnusedPoliciesCurrentThread_clientApi");
		confirmValidResponse(response, jm);
		
// 		go thru returned Policies to get the one for the current thread (lifecycle):			
		long countUsedPoliciesCurrentThread = 0;
		List<CountPoliciesBreakdown> countPoliciesBreakdowns = response.getCountPoliciesBreakdown();
		for (CountPoliciesBreakdown countPoliciesBreakdown : countPoliciesBreakdowns) {
			if (lifecycle.equals(countPoliciesBreakdown.getLifecycle())){
				countUsedPoliciesCurrentThread = countPoliciesBreakdown.getRowCount(); break;
			}
		}
		LOG.info(applicationClientApi + "_This_Thread_Unused_Policy_Count : " + countUsedPoliciesCurrentThread); 
		jm.userDataPoint(applicationClientApi + "_This_Thread_Unused_Policy_Count", countUsedPoliciesCurrentThread);	

//		use next policy
		jm.startTransaction("DH_lifecycle_0500_useNextPolicy_clientApi");		
		response = dhApiClient.useNextPolicy(applicationClientApi, lifecycle, DslConstants.UNUSED, DslConstants.SELECT_MOST_RECENTLY_ADDED);
		jm.endTransaction("DH_lifecycle_0500_useNextPolicy_clientApi");			
		confirmValidResponse(response, jm);
		
		if (response.getPolicies().isEmpty()) {
			LOG.info("useNextPolicy: NO POLICIES AVAILABLE");	
		} else {
			LOG.info("useNextPolicy: " + response.getPolicies());	
		}
		
		// 'printing' selected policies.
		jm.startTransaction("DH_lifecycle_0600_displaySelectedPolicies_clientApi");	
		response = dhApiClient.printSelectedPolicies(applicationClientApi, null, null);
		// demo how to extract a transaction time from with a running script 
		SampleResult sr_0600 = jm.endTransaction("DH_lifecycle_0600_displaySelectedPolicies_clientApi");
		confirmValidResponse(response, jm);
		
		LOG.info("Transaction " + sr_0600.getSampleLabel() + " ran at " + sr_0600.getTimeStamp() + " and took " + sr_0600.getTime() + " ms." );
		
		long used=0;
		long unused=0;
		for (int i = 0; i < response.getPolicies().size(); i++) {
			policy =  response.getPolicies().get(i);
			LOG.info("  policy " + (i+1) + " : " + policy );
			if (DslConstants.USED.equals(policy.getUseability())) used++;
			if (DslConstants.UNUSED.equals(policy.getUseability())) unused++;
		} 
		
		jm.userDataPoint("USED_count_html_demo",   used );				
		jm.userDataPoint("UNUSED_count_html_demo", unused );	
		LOG.info("Client API demo: USED=" + used + ", UNUSED=" + unused); 
		
		/* Now replicating the async processing example in: 
		 * 
		 * DataHunterRestApiClientSampleUsage.asyncLifeCycleTestWithUseabilityUpdate
		 * 
		 * from the 'mark59-datahunter-api' project, but additionally placing the matched 
		 * results into JMeter transactions.
		 * 
		 * Expected Output: you should see two transactions towards the end of the results list: 
		 * 
		 * TESTAPI_ASYNC_TOUSED_T99-testonly-02		PASS	2001	  
		 * TESTAPI_ASYNC_TOUSED_T99-testonly-01		PASS	1001
		 */ 

		dhApiClient.deleteMultiplePolicies("TESTAPI_ASYNC_TOUSED", null, null);
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-01", "FIRSTONE", "UNPAIRED", "", 1460613152000L));
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-01", "between",  "UNPAIRED", "", 1460613152009L));
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-01", "LASTONE",  "UNPAIRED", "", 1460613153001L));
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-02", "FIRSTONE", "UNPAIRED", "", 1460613153000L));
		dhApiClient.addPolicy( new Policies("TESTAPI_ASYNC_TOUSED", "T99-testonly-02", "LASTONE",  "UNPAIRED", "", 1460613155001L));
		response = dhApiClient.asyncMessageAnalyzer(DataHunterConstants.EQUALS,"TESTAPI_ASYNC_TOUSED", null, null, "USED");

		int i=0;
		List<AsyncMessageaAnalyzerResult>  asyncResults = response.getAsyncMessageaAnalyzerResults();
		System.out.println( "    asyncMessageAnalyzerPrintResults  (" + asyncResults.size() + ") - asyncLifeCycleTestWithUseabilityUpdate" );		
		System.out.println( "    -------------------------------- ");		
		for (AsyncMessageaAnalyzerResult asyncResult : asyncResults) {
			System.out.println("    " +  ++i + "   " + asyncResult);
		}
		
		for (AsyncMessageaAnalyzerResult  pairedAsyncTxn : asyncResults ) {
			// example of a typical transaction name you could set (and its response time)
			System.out.println("    Txn Name :  " + pairedAsyncTxn.getApplication() + "_" + pairedAsyncTxn.getIdentifier()
							+ "  Respsonse time (Assumed msecs) : " + pairedAsyncTxn.getDifferencetm());
			// now create some JMeter transactions (sub-results) using the paired DataHunter rows..
			jm.setTransaction(pairedAsyncTxn.getApplication() + "_" + pairedAsyncTxn.getIdentifier(), pairedAsyncTxn.getDifferencetm());
		}
		System.out.println( "    -------------------------------- ");	
		dhApiClient.deleteMultiplePolicies("TESTAPI_ASYNC_TOUSED", null, null);
		
		
		/* data cleanup */ 
		
		if (cleanDataAtScriptEnd) {
//	 		delete multiple policies (test cleanup - a duplicate of the initial delete policies transactions)
			jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies_clientApi");
			response = dhApiClient.deleteMultiplePolicies(applicationClientApi, null, null);
			jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies_clientApi");	
			confirmValidResponse(response, jm);
		}

		jm.tearDown();
		
		return jm.getMainResult();
	}

	
	private void confirmValidResponse(DataHunterRestApiResponsePojo response, JmeterFunctionsImpl jm) {
		response.getSuccess();
		if (!"TRUE".equalsIgnoreCase(response.getSuccess())) {
			throw new RuntimeException(
					"API call faiure! @ " + jm.getMostRecentTransactionStarted() + "\n" + response.getFailMsg());
		}
	}
	
	
	/**
	 * a main method to allow for execution of this JMeter/Mark59 compatible (but non-selenium) script directly in the IDE. 
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args) {
		Log4jConfigurationHelper.init(Level.INFO ) ;
		DataHunterLifecyclePvtNoSeleniumUsesRestApi thisTest = new DataHunterLifecyclePvtNoSeleniumUsesRestApi();
		Arguments jmeterParameters = thisTest.getDefaultParameters();
//		override default to force results summary to print to the console:		
//		jmeterParameters.removeArgument(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY);
//		jmeterParameters.addArgument(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(true));		
		thisTest.runTest(new JavaSamplerContext( jmeterParameters));
	}

}
