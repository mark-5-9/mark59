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

import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.EQUALS;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.REUSABLE;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.SELECT_MOST_RECENTLY_ADDED;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.SELECT_OLDEST_ENTRY;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.UNUSED;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.USED;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.SafeSleep;
import com.mark59.datahunter.samples.dsl.helpers.DslPageFunctions;
import com.mark59.datahunter.samples.dsl.helpers.Policy;
import com.mark59.datahunter.samples.dsl.helpers.PolicySelectionCriteria;
import com.mark59.datahunter.samples.dsl.helpers.UpdateUseState;
import com.mark59.selenium.corejmeterimpl.JmeterFunctionsForSeleniumScripts;
import com.mark59.selenium.corejmeterimpl.KeepBrowserOpen;
import com.mark59.selenium.corejmeterimpl.SeleniumAbstractJavaSamplerClient;
import com.mark59.selenium.driversimpl.SeleniumDriverFactory;

/**
 * A script to check run thru and check each item action on the DataHunter pages (except the asyn processing - 
 * see the dataHunterFunctionalTest project in the mark59-xtras gitreop).
 * 
 * <p>Useful to get ideas on how to call DataHunter when running a script for another application.  However
 * if possible we suggest to use DataHunter Rest Api calls to invoke dataHunter functionality from a script.
 * 
 * @see SeleniumAbstractJavaSamplerClient
 * @author Philip Webb
 * Written: Australian Spring 2021
 */
public class DataHunterBasicRegressionScript  extends SeleniumAbstractJavaSamplerClient {

	private static final Logger LOG = LogManager.getLogger(DataHunterBasicRegressionScript.class);	
	
	
	@Override
	protected Map<String, String> additionalTestParameters() {
		Map<String, String> jmeterAdditionalParameters = new LinkedHashMap<>();
		jmeterAdditionalParameters.put("DATAHUNTER_URL", "http://localhost:8081/mark59-datahunter");
		jmeterAdditionalParameters.put("DATAHUNTER_APPLICATION_ID", "DATAHUNTER_REGRESSION");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.DRIVER, "CHROME");
		jmeterAdditionalParameters.put(SeleniumDriverFactory.HEADLESS_MODE, String.valueOf(false));
		jmeterAdditionalParameters.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, PageLoadStrategy.NONE.toString());
		jmeterAdditionalParameters.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(false));				
		return jmeterAdditionalParameters;			
	}
	

	@Override
	protected void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,  WebDriver driver) {
		LOG.info("DataHunterBasicRegressionScript starting");	 
		
		String dataHunterUrl = context.getParameter("DATAHUNTER_URL");
		String application	 = context.getParameter("DATAHUNTER_APPLICATION_ID");
		String someLifecycle = "some_state";
		String someOtherdata = "some_data_";
		DslPageFunctions dslPageFunctions = new DslPageFunctions(dataHunterUrl);
		
		Policy id1policy = new Policy(application	 , " id1 "	 , " "+someLifecycle, UNUSED, someOtherdata+"id1");SafeSleep.sleep(100);
		Policy id2policy = new Policy(application+" ", "  id2   ", someLifecycle+" ", UNUSED, someOtherdata+"id2");SafeSleep.sleep(100);
		Policy id3policy = new Policy(" "+application, "   id3 " , someLifecycle    , UNUSED, someOtherdata+"id3");SafeSleep.sleep(100);
		
		PolicySelectionCriteria wholeApp = new PolicySelectionCriteria(application, "", "", null);
		dslPageFunctions.deleteMultipleItems(wholeApp, driver, null);

		dslPageFunctions.addAnItem(id1policy, driver);
		dslPageFunctions.addAnItem(id2policy, driver);
		dslPageFunctions.addAnItem(id3policy, driver);
		
		PolicySelectionCriteria selectId2 = new PolicySelectionCriteria(application, "id2", someLifecycle);
		dslPageFunctions.printAnItem(selectId2, driver, id2policy);
		
		PolicySelectionCriteria selectOldestInitalState = new PolicySelectionCriteria(application, someLifecycle, UNUSED, SELECT_OLDEST_ENTRY);
		dslPageFunctions.lookupNextItem(selectOldestInitalState, driver, id1policy);
		
		PolicySelectionCriteria mostRecenttInitalState = new PolicySelectionCriteria(application, someLifecycle, UNUSED, SELECT_MOST_RECENTLY_ADDED);
		dslPageFunctions.useNextItem(mostRecenttInitalState, driver, id3policy);
		dslPageFunctions.useNextItem(mostRecenttInitalState, driver, id2policy);		
		
		PolicySelectionCriteria unusedInitalState = new PolicySelectionCriteria(application, someLifecycle, UNUSED, null);
		PolicySelectionCriteria usedInitalState   = new PolicySelectionCriteria(application, someLifecycle, USED, null);
		dslPageFunctions.countItems(unusedInitalState, driver, 1L);
		dslPageFunctions.countItems(usedInitalState, driver, 2L);
		dslPageFunctions.countItems(wholeApp, driver, 3L);

		PolicySelectionCriteria wholeAppBreakdown = new PolicySelectionCriteria(null, application, EQUALS, "", "", "", null);
		dslPageFunctions.countItemsBreakdown(wholeAppBreakdown, driver, unusedInitalState, 1L);
		dslPageFunctions.countItemsBreakdown(wholeAppBreakdown, driver, usedInitalState, 2L);		
		
		List<Policy> allPolicies = new ArrayList<>();
		allPolicies.add(new Policy(application, "id3",someLifecycle, USED, someOtherdata+"id3"));
		allPolicies.add(new Policy(application, "id2",someLifecycle, USED, someOtherdata+"id2"));
		allPolicies.add(id1policy); // still in its original state 
		dslPageFunctions.printSelectedItems(wholeApp, driver, allPolicies);
		
		dslPageFunctions.deleteAnItem(selectId2, driver, 1L);
		dslPageFunctions.countItems(usedInitalState, driver, 1L);
		dslPageFunctions.countItems(wholeApp, driver, 2L);
		
		dslPageFunctions.addAnItem(id2policy, driver);
		dslPageFunctions.countItems(unusedInitalState, driver, 2L);
		dslPageFunctions.countItems(wholeApp, driver, 3L);	
		
		UpdateUseState id3toReusbale = new UpdateUseState(application,"id3", USED, REUSABLE, null); 
		UpdateUseState unusedToUsed  = new UpdateUseState(application,"", UNUSED, USED, null); 		
		dslPageFunctions.updateItemsUseState(id3toReusbale, driver, 1L);
		dslPageFunctions.updateItemsUseState(unusedToUsed, driver, 2L);
		
		allPolicies = new ArrayList<>();
		allPolicies.add(new Policy(application, "id3",someLifecycle, REUSABLE, someOtherdata+"id3"));
		allPolicies.add(new Policy(application, "id2",someLifecycle, USED, someOtherdata+"id2"));
		allPolicies.add(new Policy(application, "id1",someLifecycle, USED, someOtherdata+"id1"));
		dslPageFunctions.printSelectedItems(wholeApp, driver, allPolicies);		
		
		dslPageFunctions.deleteMultipleItems(wholeApp, driver, 3L);
		
		Policy idPolicyNoLifecycle = new Policy(application, "idPolicyNoLifecycle","", UNUSED, someOtherdata+"id1");SafeSleep.sleep(100);
		dslPageFunctions.addAnItem(idPolicyNoLifecycle, driver);
		PolicySelectionCriteria noLifecycle = new PolicySelectionCriteria(application, "idPolicyNoLifecycle", "");
		dslPageFunctions.deleteAnItem(noLifecycle, driver, 1L);		
		
		LOG.info("DataHunterBasicRegressionScript ending");	
	}
	
	/**
	 * A main method to assist with script testing outside JMeter.  
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args){
		Log4jConfigurationHelper.init(Level.INFO) ;
		DataHunterBasicRegressionScript thisTest = new DataHunterBasicRegressionScript();
		thisTest.runSeleniumTest(KeepBrowserOpen.ONFAILURE);
	}
		
}
