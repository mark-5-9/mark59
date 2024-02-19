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

package com.mark59.datahunter.samples.selenium.scripts;

import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.EQUALS;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.REUSABLE;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.SELECT_MOST_RECENTLY_ADDED;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.SELECT_OLDEST_ENTRY;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.UNUSED;
import static com.mark59.datahunter.samples.dsl.helpers.DslConstants.USED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.SafeSleep;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.AddPolicyActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.AddPolicyPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesBreakdownActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesBreakdownPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.CountPoliciesPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.DeletePolicyActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.DeletePolicyPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.MultiplePoliciesActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.MultiplePoliciesPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.NextPolicyActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.NextPolicyPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.PrintPolicyActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.PrintPolicyPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.UpdatePoliciesUseStateActionPage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages.UpdatePoliciesUseStatePage;
import com.mark59.datahunter.samples.dsl.datahunterSpecificPages._GenericDataHunterActionPage;
import com.mark59.datahunter.samples.dsl.helpers.DslConstants;
import com.mark59.datahunter.samples.dsl.helpers.Policy;
import com.mark59.datahunter.samples.dsl.helpers.PolicySelectionCriteria;
import com.mark59.datahunter.samples.dsl.helpers.UpdateUseState;
import com.mark59.dsl.samples.seleniumDSL.pageElements.HtmlTableRow;
import com.mark59.scripting.KeepBrowserOpen;
import com.mark59.scripting.selenium.JmeterFunctionsForSeleniumScripts;
import com.mark59.scripting.selenium.SeleniumAbstractJavaSamplerClient;
import com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory;

/**
 * A script to check run thru and check each item action on the DataHunter pages
 * 
 * <p>Useful to get ideas on how to call DataHunter when running a script for another application.  However
 * if possible use DataHunter Rest Api calls to invoke dataHunter functionality from a script.
 * 
 * @see SeleniumAbstractJavaSamplerClient
 * @author Philip Webb
 * Written: Australian Spring 2021
 */
public class DataHunterRegressionScript  extends SeleniumAbstractJavaSamplerClient {

	private static final Logger LOG = LogManager.getLogger(DataHunterRegressionScript.class);	
	
	
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
	
	
	class DslPageFunctions  implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String dataHunterUrl;
			
		public DslPageFunctions(String dataHunterUrl) {
		    this.dataHunterUrl = dataHunterUrl;
		}

		public Policy addAnItem(Policy policy, WebDriver driver) {
			driver.get(dataHunterUrl + DslConstants.ADD_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + policy.getApplication());
			AddPolicyPage addPolicyPage = new AddPolicyPage(driver);
			AddPolicyActionPage addPolicyActionPage = new AddPolicyActionPage(driver);
			addPolicyPage.identifier().type(policy.getIdentifier());
			addPolicyPage.lifecycle().type(policy.getLifecycle());
			addPolicyPage.useability().selectByVisibleText(policy.getUseability());
			addPolicyPage.otherdata().type(policy.getOtherdata());
			if (StringUtils.isNotBlank(policy.getEpochtime())){
				addPolicyPage.epochtime().type(policy.getEpochtime());
			}
			addPolicyPage.submit().submit();
			waitForSqlResultsTextOnActionPageAndCheckOk(addPolicyActionPage);
			confirmAddPolicyActionPageOk(policy, addPolicyActionPage);
			return policy;
		}

		public Integer countItems(PolicySelectionCriteria selectionCriteria, WebDriver driver, Long expectedCount) {
			driver.get(dataHunterUrl + DslConstants.COUNT_POLICIES_URL_PATH + DslConstants.URL_PARM_APPLICATION + selectionCriteria.getApplication());
			CountPoliciesPage countPoliciesPage = new CountPoliciesPage(driver); 
			CountPoliciesActionPage countPoliciesActionPage = new CountPoliciesActionPage(driver);
			if (StringUtils.isNotBlank(selectionCriteria.getLifecycle())){
				countPoliciesPage.lifecycle().type(selectionCriteria.getLifecycle());		
			}
			if (StringUtils.isNotBlank(selectionCriteria.getUseability())){
				countPoliciesPage.useability().selectByVisibleText(selectionCriteria.getUseability());				
			}
			countPoliciesPage.submit().submit();
			
			waitForSqlResultsTextOnActionPageAndCheckOk(countPoliciesActionPage);
			if (expectedCount != null) {
				confirmCountPoliciesActionPageOk(expectedCount, countPoliciesActionPage);
				assertEquals(Long.parseLong(countPoliciesActionPage.rowsAffected().getText()), expectedCount.longValue()  );
			}
			return Integer.parseInt(countPoliciesActionPage.rowsAffected().getText());
		}

		public Integer countItemsBreakdown(PolicySelectionCriteria selectionCriteria, WebDriver driver, PolicySelectionCriteria anExpectedRow, Long itsExpectedCount ) {
			int count = 0;
			driver.get(dataHunterUrl + DslConstants.COUNT_POLICIES_BREAKDOWN_URL_PATH  + DslConstants.URL_PARM_APPLICATION + selectionCriteria.getApplication());
			CountPoliciesBreakdownPage countPoliciesBreakdownPage = new CountPoliciesBreakdownPage(driver); 
			CountPoliciesBreakdownActionPage countPoliciesBreakdownActionPage = new CountPoliciesBreakdownActionPage(driver);
			
			countPoliciesBreakdownPage.applicationStartsWithOrEquals().selectByVisibleText(selectionCriteria.getApplicationStartsWithOrEquals());		
			if (StringUtils.isNotBlank(selectionCriteria.getLifecycle())){
				countPoliciesBreakdownPage.lifecycle().type(selectionCriteria.getLifecycle());		
			}
			if (StringUtils.isNotBlank(selectionCriteria.getUseability())){
				countPoliciesBreakdownPage.useability().selectByVisibleText(selectionCriteria.getUseability());				
			}
			countPoliciesBreakdownPage.submit().submit();
			
			waitForSqlResultsTextOnActionPageAndCheckOk(countPoliciesBreakdownActionPage);
			if (anExpectedRow != null) {
				count = countPoliciesBreakdownActionPage.getCountForBreakdown(
						anExpectedRow.getApplication(), anExpectedRow.getLifecycle(), anExpectedRow.getUseability());
			}
			if (anExpectedRow != null && itsExpectedCount != null) {
				confirmCountPoliciesActionPageOk(anExpectedRow, itsExpectedCount, countPoliciesBreakdownActionPage);
				assertEquals(Long.valueOf(count), itsExpectedCount);
			}
			return count;
		}
			
		public Policy printAnItem(PolicySelectionCriteria policySelection, WebDriver driver, Policy expectedPolicy) {
			driver.get(dataHunterUrl + DslConstants.PRINT_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelection.getApplication());
			PrintPolicyPage printPolicyPage = new PrintPolicyPage(driver);
			PrintPolicyActionPage printPolicyActionPage = new PrintPolicyActionPage(driver);
			printPolicyPage.identifier().type(policySelection.getIdentifier());
			if (StringUtils.isNotBlank(policySelection.getLifecycle())){
				printPolicyPage.lifecycle().type(policySelection.getLifecycle());
			}
			printPolicyPage.submit().submit();
			waitForSqlResultsTextOnActionPageAndCheckOk(printPolicyActionPage);
			Policy retrievedPolicy = new Policy();
			if (Integer.parseInt(printPolicyActionPage.rowsAffected().getText()) > 0 ) { 
				retrievedPolicy.setApplication(printPolicyActionPage.application().getText());
				retrievedPolicy.setIdentifier (printPolicyActionPage.identifier().getText());
				retrievedPolicy.setLifecycle  (printPolicyActionPage.lifecycle().getText());
				retrievedPolicy.setUseability (printPolicyActionPage.useability().getText());				
				retrievedPolicy.setOtherdata  (printPolicyActionPage.otherdata().getText());
				retrievedPolicy.setCreated    (printPolicyActionPage.created().getText());
				retrievedPolicy.setUpdated    (printPolicyActionPage.updated().getText());
				retrievedPolicy.setEpochtime  (printPolicyActionPage.epochtime().getText());	
			}
			if (expectedPolicy != null) {
				confirmPrintPolicyActionPageOk(expectedPolicy, printPolicyActionPage, retrievedPolicy);		
			}
			return retrievedPolicy;
		}		

		/**
		 * Intended as a convenience method only.   Please consider the potential size of the response table before using
		 * in a performance or functional test scenario
		 */
		public Map<String, Policy> printSelectedItems(PolicySelectionCriteria policySelectionCriteria, WebDriver driver, List<Policy> expectedPolicies) {
			driver.get(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelectionCriteria.getApplication());
			MultiplePoliciesPage  printSelectedPoliciesPage = new MultiplePoliciesPage(driver); 
			MultiplePoliciesActionPage  printSelectedPoliciesActionPage = new MultiplePoliciesActionPage(driver);
			HashMap<String, Policy> policiesMap = new HashMap<>();
			printSelectedPoliciesPage.lifecycle().type(policySelectionCriteria.getLifecycle());
			printSelectedPoliciesPage.useability().selectByVisibleText("");
			printSelectedPoliciesPage.submit().submit();
			waitForSqlResultsTextOnActionPageAndCheckOk(printSelectedPoliciesActionPage);
			
			List<HtmlTableRow> htmlTableRows = printSelectedPoliciesActionPage.printSelectedPoliciesTable().getHtmlTableRows();

			for (HtmlTableRow htmlTableRow : htmlTableRows ) {
				List<WebElement> cols = htmlTableRow.getWebElementsForRow(10);
				String policyKey =	 cols.get(2).getText() + ":" +  cols.get(3).getText() + ":" + cols.get(4).getText();	
				Policy policy = new Policy();
				policy.setApplication(cols.get(2).getText());
				policy.setIdentifier (cols.get(3).getText());
				policy.setLifecycle  (cols.get(4).getText());
				policy.setUseability (cols.get(5).getText());				
				policy.setOtherdata  (cols.get(6).getText());
				policy.setCreated    (cols.get(7).getText());
				policy.setUpdated    (cols.get(8).getText());
				policy.setEpochtime  (cols.get(9).getText());
				policiesMap.put(policyKey, policy);
			}
			if (expectedPolicies != null) {
				confirmPrintSelectedPoliciesActionPageOk(expectedPolicies, policiesMap);		
			}					
			return policiesMap;
		}
		
		public Integer deleteAnItem(PolicySelectionCriteria policySelection, WebDriver driver, Long expectedCount) {
			driver.get(dataHunterUrl + DslConstants.DELETE_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelection.getApplication());
			DeletePolicyPage deletePolicyPage = new DeletePolicyPage(driver); 
			DeletePolicyActionPage deletePolicyActionPage = new DeletePolicyActionPage(driver); 
			deletePolicyPage.identifier().type(policySelection.getIdentifier());
			if (StringUtils.isNotBlank(policySelection.getLifecycle())){
				deletePolicyPage.lifecycle().type(policySelection.getLifecycle());
			}  
			deletePolicyPage.submit().submit();
			
			waitForSqlResultsTextOnActionPageAndCheckOk(deletePolicyActionPage);
			if (expectedCount != null) {
				confirmDeletePolicyActionPageOk(expectedCount, deletePolicyActionPage);		
			}
			return Integer.parseInt(deletePolicyActionPage.rowsAffected().getText());		
		}

		public Integer deleteMultipleItems(PolicySelectionCriteria policySelection, WebDriver driver, Long expectedCount) {
			
			driver.get(dataHunterUrl + DslConstants.SELECT_MULTIPLE_POLICIES_URL_PATH + "?application=" + policySelection.getApplication());
			MultiplePoliciesPage multiplePoliciesPage = new MultiplePoliciesPage(driver); 
			MultiplePoliciesActionPage multiplePoliciesActionPage = new MultiplePoliciesActionPage(driver);	
			
			if (StringUtils.isNotBlank(policySelection.getLifecycle())){
				multiplePoliciesPage.lifecycle().type(policySelection.getLifecycle());
			}
			multiplePoliciesPage.submit().submit().waitUntilClickable( multiplePoliciesActionPage.backLink() );				
			multiplePoliciesActionPage.multipleDeleteLink().click().waitUntilAlertisPresent().acceptAlert();
			waitForSqlResultsTextOnActionPageAndCheckOk(multiplePoliciesActionPage);
			if (expectedCount != null) {		
				confirmDeleteMultiplePoliciesPage(expectedCount, multiplePoliciesActionPage);
			}
			return Integer.parseInt(multiplePoliciesActionPage.rowsAffected().getText().replaceAll("[^\\d]", ""));
		}
			
		public Policy lookupNextItem(PolicySelectionCriteria policySelection, WebDriver driver, Policy expectedPolicy) {
			return  nextPolicy(DslConstants.LOOKUP, policySelection, driver, expectedPolicy);
		}	
		
		public Policy useNextItem(PolicySelectionCriteria policySelection, WebDriver driver, Policy expectedPolicy) {
			return  nextPolicy(DslConstants.USE, policySelection, driver, expectedPolicy);
		}
		
		private Policy nextPolicy(String useOrLookup, PolicySelectionCriteria policySelection, WebDriver driver, Policy expectedPolicy) {
			driver.get(dataHunterUrl + DslConstants.NEXT_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelection.getApplication() + DslConstants.URL_PARM_USE_OR_LOOKUP + useOrLookup);
			NextPolicyPage nextPolicyPage = new NextPolicyPage(driver); 
			NextPolicyActionPage nextPolicyActionPage = new NextPolicyActionPage(driver); 
			nextPolicyPage.lifecycle().type(policySelection.getLifecycle());
			nextPolicyPage.useability().selectByVisibleText(policySelection.getUseability());
			nextPolicyPage.selectOrder().selectByVisibleText(policySelection.getSelectOrder());
			nextPolicyPage.submit().submit();

			waitForSqlResultsTextOnActionPageAndCheckOk(nextPolicyActionPage);
			
			Policy nextPolicy = new Policy();
			nextPolicy.setApplication(nextPolicyActionPage.application().getText());
			nextPolicy.setIdentifier(nextPolicyActionPage.identifier().getText());
			nextPolicy.setLifecycle(nextPolicyActionPage.lifecycle().getText());
			nextPolicy.setUseability(nextPolicyActionPage.useability().getText());			
			nextPolicy.setOtherdata(nextPolicyActionPage.otherdata().getText());
			nextPolicy.setCreated(nextPolicyActionPage.created().getText());
			nextPolicy.setUpdated(nextPolicyActionPage.updated().getText());
			nextPolicy.setEpochtime(nextPolicyActionPage.epochtime().getText());
			if (expectedPolicy != null) {
				confirmNextPolicyActionPageOk(expectedPolicy, nextPolicyActionPage);		
			}
			return nextPolicy;
		}
		
		public Integer updateItemsUseState(UpdateUseState updateUseStateAndEpochTime, WebDriver driver, Long expectedCount) {
			driver.get(dataHunterUrl + DslConstants.UPDATE_POLICIES_USE_STATE_URL_PATH + DslConstants.URL_PARM_APPLICATION + updateUseStateAndEpochTime.getApplication());
			UpdatePoliciesUseStatePage updatePoliciesUseStatePage = new UpdatePoliciesUseStatePage(driver); 
			UpdatePoliciesUseStateActionPage updatePoliciesUseStateActionPage = new UpdatePoliciesUseStateActionPage(driver);

			if (StringUtils.isNotBlank(updateUseStateAndEpochTime.getIdentifier())){
				updatePoliciesUseStatePage.identifier().type(updateUseStateAndEpochTime.getIdentifier());
			}
			updatePoliciesUseStatePage.useability().selectByVisibleText(updateUseStateAndEpochTime.getUseability());	
			updatePoliciesUseStatePage.toUseability().selectByVisibleText(updateUseStateAndEpochTime.getToUseability());
			if (updateUseStateAndEpochTime.getToEpochTime() != null ){
				updatePoliciesUseStatePage.epochtime().type(updateUseStateAndEpochTime.getToEpochTime().toString());
			}
			updatePoliciesUseStatePage.submit().submit();
			waitForSqlResultsTextOnActionPageAndCheckOk(updatePoliciesUseStateActionPage);
			if (expectedCount != null) {
				confirmUpdatePoliciesUseStateActionPage(expectedCount, updatePoliciesUseStateActionPage);		
			}		
			return Integer.parseInt(updatePoliciesUseStateActionPage.rowsAffected().getText());
		}
		
		private void waitForSqlResultsTextOnActionPageAndCheckOk(_GenericDataHunterActionPage _genericDataHunterActionPage) {
			String sqlResultText = _genericDataHunterActionPage.sqlResult().getText();
			if (sqlResultText==null || !sqlResultText.contains("PASS")) {
				throw new RuntimeException("SQL issue (" + sqlResultText + ") : " +
							_genericDataHunterActionPage.formatResultsMessage(_genericDataHunterActionPage.getClass().getName()));
			}
		}
		
		private void confirmAddPolicyActionPageOk(Policy policy, AddPolicyActionPage addPolicyActionPage){
			assertEquals(policy.getApplication().trim(), addPolicyActionPage.application().getText());
			assertEquals(policy.getIdentifier().trim(), addPolicyActionPage.identifier().getText());
			assertEquals(policy.getLifecycle().trim(), addPolicyActionPage.lifecycle().getText());
			assertEquals(policy.getUseability(), addPolicyActionPage.useability().getText());
			assertEquals(policy.getOtherdata(), addPolicyActionPage.otherdata().getText());
			assertTrue(StringUtils.isNumeric(addPolicyActionPage.epochtime().getText()));
		}

		private void confirmCountPoliciesActionPageOk(long expectedCount, CountPoliciesActionPage countPoliciesActionPage) {
			assertEquals(expectedCount, Long.parseLong(countPoliciesActionPage.prettyCount().getText()));
			assertEquals(expectedCount, Long.parseLong(countPoliciesActionPage.rowsAffected().getText()));
		}
		
		private void confirmCountPoliciesActionPageOk(PolicySelectionCriteria anExpectedRow, long itsExpectedCount,	
				CountPoliciesBreakdownActionPage countPoliciesBreakdownActionPage) {
			long actualCount = countPoliciesBreakdownActionPage.getCountForBreakdown(
					anExpectedRow.getApplication(), anExpectedRow.getLifecycle(), anExpectedRow.getUseability());
			assertEquals(itsExpectedCount, actualCount);
		}
		
		private void confirmPrintPolicyActionPageOk(Policy expectedPolicy, PrintPolicyActionPage printPolicyActionPage, Policy retrievedPolicy) {
			assertEquals(expectedPolicy.getApplication().trim(), printPolicyActionPage.application().getText());
			assertEquals(expectedPolicy.getIdentifier().trim(), printPolicyActionPage.identifier().getText());
			assertEquals(expectedPolicy.getLifecycle().trim(), printPolicyActionPage.lifecycle().getText());
			assertEquals(expectedPolicy.getUseability(), printPolicyActionPage.useability().getText());
			assertEquals(expectedPolicy.getOtherdata(), printPolicyActionPage.otherdata().getText());
			assertTrue(StringUtils.isNumeric(printPolicyActionPage.epochtime().getText()));
			assertEquals(1, Long.parseLong(printPolicyActionPage.rowsAffected().getText()));

			assertEquals(expectedPolicy.getApplication().trim(), retrievedPolicy.getApplication());
			assertEquals(expectedPolicy.getIdentifier().trim(), retrievedPolicy.getIdentifier());
			assertEquals(expectedPolicy.getLifecycle().trim(), retrievedPolicy.getLifecycle());
			assertEquals(expectedPolicy.getUseability(), retrievedPolicy.getUseability());
			assertEquals(expectedPolicy.getOtherdata(), retrievedPolicy.getOtherdata());
		}
		
		private void confirmPrintSelectedPoliciesActionPageOk(List<Policy> expectedPolicies, HashMap<String, Policy> policiesMap) {
			for (Policy expectedPolicy : expectedPolicies) {
				Policy actualPolicyRow = policiesMap.get(expectedPolicy.getApplication().trim() + ":"
						+ expectedPolicy.getIdentifier().trim() + ":" + expectedPolicy.getLifecycle().trim());
				assertNotNull(expectedPolicy + " - not found", actualPolicyRow);
				assertEquals(expectedPolicy.getUseability(), actualPolicyRow.getUseability());
				assertEquals(expectedPolicy.getOtherdata(), actualPolicyRow.getOtherdata());
			}
		}
		
		private void confirmDeletePolicyActionPageOk(long expectedCount, DeletePolicyActionPage deletePolicyActionPage) {
			assertEquals(expectedCount, Long.parseLong(deletePolicyActionPage.rowsAffected().getText()));
		}
		
		private void confirmDeleteMultiplePoliciesPage(long expectedCount, MultiplePoliciesActionPage multiplePoliciesActionPage) {
			assertEquals(expectedCount, Long.parseLong(multiplePoliciesActionPage.rowsAffected().getText().replaceAll("[^\\d]", "")));
		}
		
		private void confirmNextPolicyActionPageOk(Policy expectedPolicy, NextPolicyActionPage nextPolicyActionPage) {
			assertEquals(expectedPolicy.getApplication().trim(), nextPolicyActionPage.application().getText());
			assertEquals(expectedPolicy.getIdentifier().trim(), nextPolicyActionPage.identifier().getText());
			assertEquals(expectedPolicy.getLifecycle().trim(), nextPolicyActionPage.lifecycle().getText());
			assertEquals(expectedPolicy.getUseability(), nextPolicyActionPage.useability().getText());
			assertEquals(expectedPolicy.getOtherdata(), nextPolicyActionPage.otherdata().getText());
			assertEquals(expectedPolicy.getIdentifier().trim(), nextPolicyActionPage.prettyidentifier().getText());
		}
		
		private void confirmUpdatePoliciesUseStateActionPage(long expectedCount, UpdatePoliciesUseStateActionPage updatePoliciesUseStateActionPage) {
			assertEquals(expectedCount, Long.parseLong(updatePoliciesUseStateActionPage.rowsAffected().getText()));
		}
		
	}
	
	
	
	/**
	 * A main method to assist with script testing outside JMeter.  
	 * For logging details see @Log4jConfigurationHelper 
	 */
	public static void main(String[] args){
		Log4jConfigurationHelper.init(Level.INFO) ;
		DataHunterRegressionScript thisTest = new DataHunterRegressionScript();
		thisTest.runUiTest(KeepBrowserOpen.ONFAILURE);
	}
		
}
