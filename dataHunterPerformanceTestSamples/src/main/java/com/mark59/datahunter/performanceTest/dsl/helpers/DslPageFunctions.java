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

package com.mark59.datahunter.performanceTest.dsl.helpers;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.AddPolicyActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.AddPolicyPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesBreakdownActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesBreakdownPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.CountPoliciesPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.DeleteMultiplePoliciesActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.DeleteMultiplePoliciesPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.DeletePolicyActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.DeletePolicyPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.NextPolicyActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.NextPolicyPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.PrintPolicyActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.PrintPolicyPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.PrintSelectedPoliciesActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.PrintSelectedPoliciesPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.UpdatePoliciesUseStateActionPage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages.UpdatePoliciesUseStatePage;
import com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages._GenericDatatHunterActionPage;
import com.mark59.seleniumDSL.pageElements.HtmlTableRow;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class DslPageFunctions  implements Serializable {
	private static final long serialVersionUID = 1L;

	private String dataHunterUrl = DslConstants.DEFAULT_DATAHUNTER_URL; 
		
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
		Integer count = 0;
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
			assertEquals(new Long(count), itsExpectedCount);
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
		driver.get(dataHunterUrl + DslConstants.PRINT_SELECTED_POLICIES_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelectionCriteria.getApplication());
		PrintSelectedPoliciesPage  printSelectedPoliciesPage = new PrintSelectedPoliciesPage(driver); 
		PrintSelectedPoliciesActionPage  printSelectedPoliciesActionPage = new PrintSelectedPoliciesActionPage(driver);
		HashMap<String, Policy> policiesMap = new HashMap<String, Policy>(); 
		printSelectedPoliciesPage.lifecycle().type(policySelectionCriteria.getLifecycle());
		printSelectedPoliciesPage.useability().selectByVisibleText("");
		printSelectedPoliciesPage.submit().submit();
		waitForSqlResultsTextOnActionPageAndCheckOk(printSelectedPoliciesActionPage);
		
		List<HtmlTableRow> htmlTableRows = printSelectedPoliciesActionPage.printSelectedPoliciesTable().getHtmlTableRows();
		
		for (HtmlTableRow htmlTableRow : htmlTableRows ) {
			List<WebElement> cols = htmlTableRow.getWebElementsForRow(8);
			String policyKey =	 cols.get(0).getText() + ":" +  cols.get(1).getText() + ":" + cols.get(2).getText();	
			Policy policy = new Policy();
			policy.setApplication(cols.get(0).getText());
			policy.setIdentifier (cols.get(1).getText());
			policy.setLifecycle  (cols.get(2).getText());
			policy.setUseability (cols.get(3).getText());				
			policy.setOtherdata  (cols.get(4).getText());
			policy.setCreated    (cols.get(5).getText());
			policy.setUpdated    (cols.get(6).getText());
			policy.setEpochtime  (cols.get(7).getText());
			policiesMap.put(policyKey, policy);
		}
		if (expectedPolicies != null) {
			confirmPrintSelectedPoliciesActionPageOk(expectedPolicies, policiesMap);		
		}					
		return policiesMap;
	}

	
	public Integer deleteAnItem(PolicySelectionCriteria policySelectionCriteria, WebDriver driver, Long expectedCount) {
		driver.get(dataHunterUrl + DslConstants.DELETE_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelectionCriteria.getApplication());
		DeletePolicyPage deletePolicyPage = new DeletePolicyPage(driver); 
		DeletePolicyActionPage deletePolicyActionPage = new DeletePolicyActionPage(driver); 
		deletePolicyPage.identifier().type(policySelectionCriteria.getIdentifier());
		deletePolicyPage.submit().submit();
		
		waitForSqlResultsTextOnActionPageAndCheckOk(deletePolicyActionPage);
		if (expectedCount != null) {
			confirmDeletePolicyActionPageOk(expectedCount, deletePolicyActionPage);		
		}
		return Integer.parseInt(deletePolicyActionPage.rowsAffected().getText());		
	}


	public Integer deleteMultipleItems(PolicySelectionCriteria policySelection, WebDriver driver, Long expectedCount) {
		driver.get(dataHunterUrl + DslConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + "?application=" + policySelection.getApplication());
		DeleteMultiplePoliciesPage deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPage(driver); 
		DeleteMultiplePoliciesActionPage deleteMultiplePoliciesActionPage = new DeleteMultiplePoliciesActionPage(driver); 
		if (StringUtils.isNotBlank(policySelection.getLifecycle())){
			deleteMultiplePoliciesPage.lifecycle().type(policySelection.getLifecycle());
		}
		if (StringUtils.isNotBlank(policySelection.getUseability())){
			deleteMultiplePoliciesPage.useability().selectByVisibleText(policySelection.getUseability());
		}
		deleteMultiplePoliciesPage.submit().submit();
		waitForSqlResultsTextOnActionPageAndCheckOk(deleteMultiplePoliciesActionPage);
		if (expectedCount != null) {
			confirmDeleteMultiplePoliciesPage(expectedCount, deleteMultiplePoliciesActionPage);		
		}		
		return Integer.parseInt(deleteMultiplePoliciesActionPage.rowsAffected().getText());
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

	
	private void waitForSqlResultsTextOnActionPageAndCheckOk(_GenericDatatHunterActionPage _genericDatatHunterActionPage) {
		String sqlResultText = _genericDatatHunterActionPage.sqlResult().getText();
		if (!"PASS".equals(sqlResultText)) {
			throw new RuntimeException("SQL issue (" + sqlResultText + ") : " +
						_genericDatatHunterActionPage.formatResultsMessage(_genericDatatHunterActionPage.getClass().getName()));
		}
	}
	
	private void confirmAddPolicyActionPageOk(Policy policy, AddPolicyActionPage addPolicyActionPage){
		assertEquals(policy.getApplication(), addPolicyActionPage.application().getText());
		assertEquals(policy.getIdentifier(), addPolicyActionPage.identifier().getText());
		assertEquals(policy.getLifecycle(), addPolicyActionPage.lifecycle().getText());
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
		assertEquals(expectedPolicy.getApplication(), printPolicyActionPage.application().getText());
		assertEquals(expectedPolicy.getIdentifier(), printPolicyActionPage.identifier().getText());
		assertEquals(expectedPolicy.getLifecycle(), printPolicyActionPage.lifecycle().getText());
		assertEquals(expectedPolicy.getUseability(), printPolicyActionPage.useability().getText());
		assertEquals(expectedPolicy.getOtherdata(), printPolicyActionPage.otherdata().getText());
		assertTrue(StringUtils.isNumeric(printPolicyActionPage.epochtime().getText()));
		assertEquals(1, Long.parseLong(printPolicyActionPage.rowsAffected().getText()));

		assertEquals(expectedPolicy.getApplication(), retrievedPolicy.getApplication());
		assertEquals(expectedPolicy.getIdentifier(), retrievedPolicy.getIdentifier());
		assertEquals(expectedPolicy.getLifecycle(), retrievedPolicy.getLifecycle());
		assertEquals(expectedPolicy.getUseability(), retrievedPolicy.getUseability());
		assertEquals(expectedPolicy.getOtherdata(), retrievedPolicy.getOtherdata());
	}
	
	private void confirmPrintSelectedPoliciesActionPageOk(List<Policy> expectedPolicies, HashMap<String, Policy> policiesMap) {
		for (Policy expectedPolicy : expectedPolicies) {
			Policy actualPolicyRow =  policiesMap.get(expectedPolicy.getApplication()+":"+expectedPolicy.getIdentifier()+":"+expectedPolicy.getLifecycle());
			assertTrue( expectedPolicy + " - not found",   actualPolicyRow != null);
			assertEquals(expectedPolicy.getUseability(), actualPolicyRow.getUseability()); 
			assertEquals(expectedPolicy.getOtherdata(), actualPolicyRow.getOtherdata()); 
		} 
	}
	
	private void confirmDeletePolicyActionPageOk(long expectedCount, DeletePolicyActionPage deletePolicyActionPage) {
		assertEquals(expectedCount, Long.parseLong(deletePolicyActionPage.rowsAffected().getText()));
	}
	
	private void confirmDeleteMultiplePoliciesPage(long expectedCount, DeleteMultiplePoliciesActionPage deleteMultiplePoliciesActionPage) {
		assertEquals(expectedCount, Long.parseLong(deleteMultiplePoliciesActionPage.rowsAffected().getText()));
	}
	
	private void confirmNextPolicyActionPageOk(Policy expectedPolicy, NextPolicyActionPage nextPolicyActionPage) {
		assertEquals(expectedPolicy.getApplication(), nextPolicyActionPage.application().getText());
		assertEquals(expectedPolicy.getIdentifier(), nextPolicyActionPage.identifier().getText());
		assertEquals(expectedPolicy.getLifecycle(), nextPolicyActionPage.lifecycle().getText());
		assertEquals(expectedPolicy.getUseability(), nextPolicyActionPage.useability().getText());
		assertEquals(expectedPolicy.getOtherdata(), nextPolicyActionPage.otherdata().getText());
		assertEquals(expectedPolicy.getIdentifier(), nextPolicyActionPage.prettyidentifier().getText());
	}
	
	private void confirmUpdatePoliciesUseStateActionPage(long expectedCount, UpdatePoliciesUseStateActionPage updatePoliciesUseStateActionPage) {
		assertEquals(expectedCount, Long.parseLong(updatePoliciesUseStateActionPage.rowsAffected().getText()));
	}
	
}
