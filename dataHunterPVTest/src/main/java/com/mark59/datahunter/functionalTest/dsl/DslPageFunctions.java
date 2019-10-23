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

package com.mark59.datahunter.functionalTest.dsl;


import java.io.Serializable;
import java.net.URL;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.mark59.datahunter.functionalTest.dsl.pageElements.HtmlTableRow;
import com.mark59.datahunter.functionalTest.dsl.pages.*;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class DslPageFunctions  implements Serializable {
	private static final long serialVersionUID = 1L;

	private String dataHunterUrlHostPort = DslConstants.DEFAULT_DATAHUNTER_URL_HOST_PORT; 

	public DslPageFunctions() {
	}

	public DslPageFunctions(String dataHunterUrlHostPort) {
		try {
			new URL(dataHunterUrlHostPort);
		} catch (Exception e) {
			throw new RuntimeException("Invalid url passed (" + dataHunterUrlHostPort + "). Please use format http://{server}:{port}");
		}
	    this.dataHunterUrlHostPort = dataHunterUrlHostPort;
	}



	public Policies addPolicy(Policies policy, WebDriver driver) {
		driver.get(dataHunterUrlHostPort + DslConstants.ADD_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + policy.getApplication());
		AddPolicyPage addPolicyPage = new AddPolicyPage(driver);
		addPolicyPage.identifier.type(policy.getIdentifier());
		addPolicyPage.lifecycle.type(policy.getLifecycle());
		addPolicyPage.useability.selectByVisibleText(policy.getUseability());
		addPolicyPage.otherdata.type(policy.getOtherdata());
		if (policy.getEpochtime() != null){
			addPolicyPage.epochtime.type(policy.getEpochtime().toString());
		}
		addPolicyPage.submit.submit();
		
		AddPolicyActionPage addPolicyActionPage = new AddPolicyActionPage(driver);
		if ( ! DslConstants.SQL_RESULT_PASS.equals(addPolicyActionPage.sqlResult.getText())){
			ThrowRuntimeExceptionWithMesage("Add Policy",  addPolicyActionPage, policy); 
		};
		policy.setEpochtime(new Long(addPolicyActionPage.epochtime.getText()));
		return policy;
	}

	
	public Integer countPolicies(PolicySelectionCriteria policySelectionCriteria, WebDriver driver) {
		Integer count = null;
		driver.get(dataHunterUrlHostPort + DslConstants.COUNT_POLICIES_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelectionCriteria.getApplication());
		CountPoliciesPage countPoliciesPage = new CountPoliciesPage(driver); 
		countPoliciesPage.lifecycle.type(policySelectionCriteria.getLifecycle());
		countPoliciesPage.useability.selectByVisibleText(policySelectionCriteria.getUseability());				
		countPoliciesPage.submit.submit();
		
		CountPoliciesActionPage countPoliciesActionPage = new CountPoliciesActionPage(driver);
		if ( ! DslConstants.SQL_RESULT_PASS.equals(countPoliciesActionPage.sqlResult.getText())){
			ThrowRuntimeExceptionWithMesage("CountPolicies",  countPoliciesActionPage, policySelectionCriteria); 
		};	
		try {
			count = new Integer(countPoliciesActionPage.rowsAffected.getText());
		} catch (Exception e){
			e.printStackTrace();
			ThrowRuntimeExceptionWithMesage("CountPolicies bad_count_value",  countPoliciesActionPage, policySelectionCriteria); 
		}
		return count;
	}
	
	
	public Integer countPoliciesForBreakdown(String application, String lifecycle, String useability, WebDriver driver) {
		Integer count = null;
		driver.get(dataHunterUrlHostPort + DslConstants.COUNT_POLICIES_BREAKDOWN_URL_PATH  + DslConstants.URL_PARM_APPLICATION + application);
		CountPoliciesBreakdownPage countPoliciesBreakdownPage = new CountPoliciesBreakdownPage(driver); 
		countPoliciesBreakdownPage.lifecycle.type(lifecycle);
		if (StringUtils.isNotBlank(useability) ) {
			countPoliciesBreakdownPage.useability.selectByVisibleText(useability);
		}
		countPoliciesBreakdownPage.submit.submit();
		
		CountPoliciesBreakdownActionPage countPoliciesBreakdownActionPage = new CountPoliciesBreakdownActionPage(driver);
		if ( ! DslConstants.SQL_RESULT_PASS.equals(countPoliciesBreakdownActionPage.sqlResult.getText())){
			ThrowRuntimeExceptionWithMesage("CountForBreakdown",  countPoliciesBreakdownActionPage); 
		};	
		try {
			count = countPoliciesBreakdownActionPage.getCountForBreakdown(application, lifecycle, useability);
		} catch (Exception e){
			e.printStackTrace();
			ThrowRuntimeExceptionWithMesage("CountForBreakdown bad_count_value",  countPoliciesBreakdownActionPage); 
		}
		return count;
	}
		
	public Integer deleteMultiplePolicies(PolicySelectionCriteria policySelectionCriteria, WebDriver driver) {
		Integer count = null;
		driver.get(dataHunterUrlHostPort + DslConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelectionCriteria.getApplication());
		DeleteMultiplePoliciesPage   deleteMultiplePoliciesPage = new DeleteMultiplePoliciesPage(driver); 
		deleteMultiplePoliciesPage.lifecycle.type(policySelectionCriteria.getLifecycle());
		if (StringUtils.isNotBlank(policySelectionCriteria.getUseability()) ) {
			deleteMultiplePoliciesPage.useability.selectByVisibleText(policySelectionCriteria.getUseability());				
		}
		deleteMultiplePoliciesPage.submit.submit();
		
		DeleteMultiplePoliciesActionPage deleteMultiplePoliciesActionPage = new DeleteMultiplePoliciesActionPage(driver);
		if ( ! DslConstants.SQL_RESULT_PASS.equals(deleteMultiplePoliciesActionPage.sqlResult.getText())){
			ThrowRuntimeExceptionWithMesage("deleteMultiplePolicies",  deleteMultiplePoliciesActionPage, policySelectionCriteria); 
		};	
		try {
			count = new Integer(deleteMultiplePoliciesActionPage.rowsAffected.getText());
		} catch (Exception e){
			e.printStackTrace();
			ThrowRuntimeExceptionWithMesage("deleteMultiplePolicies bad_count_value",  deleteMultiplePoliciesActionPage, policySelectionCriteria); 
		}
		return count;
	}

	
	public Integer deletePolicy(PolicySelectionCriteria policySelectionCriteria, WebDriver driver) {
		Integer count = null;
		driver.get(dataHunterUrlHostPort + DslConstants.DELETE_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelectionCriteria.getApplication());
		DeletePolicyPage  deletePolicyPage = new DeletePolicyPage(driver); 
		deletePolicyPage.identifier.type(policySelectionCriteria.getIdentifier());
		deletePolicyPage.submit.submit();
		
		DeletePolicyActionPage deletePolicyActionPage = new DeletePolicyActionPage(driver);
		if ( ! DslConstants.SQL_RESULT_PASS.equals(deletePolicyActionPage.sqlResult.getText())){
			ThrowRuntimeExceptionWithMesage("deletePolicy",  deletePolicyActionPage, policySelectionCriteria); 
		};	
		try {
			count = new Integer(deletePolicyActionPage.rowsAffected.getText());
		} catch (Exception e){
			e.printStackTrace();
			ThrowRuntimeExceptionWithMesage("deletePolicy bad_count_value",  deletePolicyActionPage, policySelectionCriteria); 
		}
		return count;
	}
	
	
	
	/**
	 * Intended as a convenience method only.   Please consider the potential size of the response table before using
	 * in a performance or functional test scenario
	 */
	public Map<String, Policies> printSelectedPolicies(PolicySelectionCriteria policySelectionCriteria, WebDriver driver) {
		HashMap<String, Policies> policiesMap = new HashMap<String, Policies>(); 
		driver.get(dataHunterUrlHostPort + DslConstants.PRINT_SELECTED_POLICIES_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelectionCriteria.getApplication());
		PrintSelectedPoliciesPage  printSelectedPoliciesPage = new PrintSelectedPoliciesPage(driver); 
		printSelectedPoliciesPage.lifecycle.type(policySelectionCriteria.getLifecycle());
		printSelectedPoliciesPage.useability.selectByVisibleText("");
		printSelectedPoliciesPage.submit.submit();
		
		PrintSelectedPoliciesActionPage  printSelectedPoliciesActionPage = new PrintSelectedPoliciesActionPage(driver);
		if ( ! DslConstants.SQL_RESULT_PASS.equals(printSelectedPoliciesActionPage.sqlResult.getText())){
			ThrowRuntimeExceptionWithMesage("printSelectedPolicies",  printSelectedPoliciesActionPage, policySelectionCriteria); 
		};
		
		try {
			List<HtmlTableRow> htmlTableRows = printSelectedPoliciesActionPage.printSelectedPoliciesTable.getHtmlTableRows();
			
			for (HtmlTableRow htmlTableRow : htmlTableRows ) {
				
				List<WebElement> cols = htmlTableRow.getElementsForRow(8);

				String policyKey =	 cols.get(0).getText() + ":" +  cols.get(1).getText() + ":" + cols.get(2).getText();	
				
				Policies policy = new Policies();
				policy.setApplication(cols.get(0).getText());
				policy.setIdentifier( cols.get(1).getText());
				policy.setLifecycle(  cols.get(2).getText());
				policy.setUseability( cols.get(3).getText());				
				policy.setOtherdata(  cols.get(4).getText());
				policy.setCreated(Timestamp.valueOf(cols.get(5).getText()));
				policy.setUpdated(Timestamp.valueOf(cols.get(6).getText()));
				policy.setEpochtime(new Long(cols.get(7).getText()));
				
				policiesMap.put(policyKey, policy);
			}
					
		} catch (Exception e){
			e.printStackTrace();
			ThrowRuntimeExceptionWithMesage("printSelectedPolicies",  printSelectedPoliciesActionPage, policySelectionCriteria); 
		}
		return policiesMap;
	}

	
	
	
	
		
	
	public Policies lookupNextPolicy(PolicySelectionCriteria policySelectionCriteria, WebDriver driver) {
		return  nextPolicy(DslConstants.LOOKUP, policySelectionCriteria, driver);
	}	
	
	public Policies useNextPolicy(PolicySelectionCriteria policySelectionCriteria, WebDriver driver) {
		return  nextPolicy(DslConstants.USE, policySelectionCriteria, driver);
	}
	
	private Policies nextPolicy(String useOrLookup, PolicySelectionCriteria policySelectionCriteria, WebDriver driver) {
		Policies policy = new Policies();
		driver.get(dataHunterUrlHostPort + DslConstants.NEXT_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelectionCriteria.getApplication() + DslConstants.URL_PARM_USE_OR_LOOKUP + useOrLookup);
		NextPolicyPage nextPolicyPage = new NextPolicyPage(driver); 
		nextPolicyPage.lifecycle.type(policySelectionCriteria.getLifecycle());
		nextPolicyPage.useability.selectByVisibleText(policySelectionCriteria.getUseability());
		nextPolicyPage.selectOrder.selectByVisibleText(policySelectionCriteria.getSelectOrder());
		nextPolicyPage.submit.submit();
		
		NextPolicyActionPage nextPolicyActionPage = new NextPolicyActionPage(driver);
		if ( ! DslConstants.SQL_RESULT_PASS.equals(nextPolicyActionPage.sqlResult.getText())){
			ThrowRuntimeExceptionWithMesage("NextPolicy",  nextPolicyActionPage, policySelectionCriteria); 
		};	
		try {
			policy.setApplication(nextPolicyActionPage.application.getText());
			policy.setIdentifier(nextPolicyActionPage.identifier.getText());
			policy.setLifecycle(nextPolicyActionPage.lifecycle.getText());
			policy.setUseability(nextPolicyActionPage.useability.getText());			
			policy.setOtherdata(nextPolicyActionPage.otherdata.getText());
			policy.setCreated(Timestamp.valueOf(nextPolicyActionPage.created.getText()));
			policy.setUpdated(Timestamp.valueOf(nextPolicyActionPage.updated.getText()));
			policy.setEpochtime(new Long(nextPolicyActionPage.epochtime.getText()));						
		} catch (Exception e){
			e.printStackTrace();
			ThrowRuntimeExceptionWithMesage("NextPolicy bad_policy_value",  nextPolicyActionPage, policySelectionCriteria); 
		}
		return policy;
	}
	

	
	public Integer updatePoliciesUseState(UpdateUseStateAndEpochTime updateUseStateAndEpochTime, WebDriver driver) {
		Integer count = null;
		driver.get(dataHunterUrlHostPort + DslConstants.UPDATE_POLICIES_USE_STATE_URL_PATH + DslConstants.URL_PARM_APPLICATION + updateUseStateAndEpochTime.getApplication());
		UpdatePoliciesUseStatePage updatePoliciesUseStatePage = new UpdatePoliciesUseStatePage(driver); 
		updatePoliciesUseStatePage.identifier.type(updateUseStateAndEpochTime.getIdentifier());
		updatePoliciesUseStatePage.useability.selectByVisibleText(updateUseStateAndEpochTime.getUseability());	
		updatePoliciesUseStatePage.toUseability.selectByVisibleText(updateUseStateAndEpochTime.getToUseability());
		if (updateUseStateAndEpochTime.getToEpochTime() != null ){
			updatePoliciesUseStatePage.toEpochTime.type(updateUseStateAndEpochTime.getToEpochTime().toString());
		}
		updatePoliciesUseStatePage.submit.submit();
		
		UpdatePoliciesUseStateActionPage updatePoliciesUseStateActionPage = new UpdatePoliciesUseStateActionPage(driver);
		if ( ! DslConstants.SQL_RESULT_PASS.equals(updatePoliciesUseStateActionPage.sqlResult.getText())){
			ThrowRuntimeExceptionWithMesage("deleteMultiplePolicies",  updatePoliciesUseStateActionPage, updateUseStateAndEpochTime); 
		};	
		try {
			count = new Integer(updatePoliciesUseStateActionPage.rowsAffected.getText());
		} catch (Exception e){
			e.printStackTrace();
			ThrowRuntimeExceptionWithMesage("deleteMultiplePolicies bad_count_value",  updatePoliciesUseStateActionPage, updateUseStateAndEpochTime); 
		}
		return count;
	}
	
	
	
	/**
	 * Consider the potential size of the response table when using in a performance or functional test scenario
	 */
	public Map<String, AsyncMessageaAnalyzerResult> asyncMessageAnalyzerPrintResults(PolicySelectionCriteria policySelectionCriteria, WebDriver driver) {
		String asyncMessageaAnalyzerResultKey = null;
		AsyncMessageaAnalyzerResult asyncMessageaAnalyzerResult = null; 
		HashMap<String, AsyncMessageaAnalyzerResult> asyncResultsMap = new HashMap<String, AsyncMessageaAnalyzerResult>();
		driver.get(dataHunterUrlHostPort + DslConstants.ASYNC_MESSAGE_ANALYZER_URL_PATH + DslConstants.URL_PARM_APPLICATION + policySelectionCriteria.getApplication());
		AsyncMessageAnalyzerPage asyncMessageAnalyzerPage = new AsyncMessageAnalyzerPage(driver); 
		asyncMessageAnalyzerPage.applicationStartsWithOrEquals.selectByVisibleText(policySelectionCriteria.getApplicationStartsWithOrEquals());		
		asyncMessageAnalyzerPage.identifier.type(policySelectionCriteria.getIdentifier());
		asyncMessageAnalyzerPage.useability.selectByVisibleText(policySelectionCriteria.getUseability());
		asyncMessageAnalyzerPage.submit.submit();
		
		AsyncMessageAnalyzerActionPage  asyncMessageAnalyzerActionPage = new AsyncMessageAnalyzerActionPage(driver);
		if ( ! DslConstants.SQL_RESULT_PASS.equals(asyncMessageAnalyzerActionPage.sqlResult.getText())){
			ThrowRuntimeExceptionWithMesage("printSelectedPolicies",  asyncMessageAnalyzerActionPage, policySelectionCriteria); 
		};
		
		try {
			List<HtmlTableRow> htmlTableRows = asyncMessageAnalyzerActionPage.asyncMessageaAnalyzerTable.getHtmlTableRows();
		
			for (HtmlTableRow htmlTableRow : htmlTableRows){
				
				List<WebElement> cols = htmlTableRow.getElementsForRow(7);

				asyncMessageaAnalyzerResultKey = cols.get(0).getText() + ":" + cols.get(1).getText();
				
				asyncMessageaAnalyzerResult  = new AsyncMessageaAnalyzerResult();
				asyncMessageaAnalyzerResult.setApplication(cols.get(0).getText());
				asyncMessageaAnalyzerResult.setIdentifier (cols.get(1).getText());
				asyncMessageaAnalyzerResult.setUseability (cols.get(2).getText());
				asyncMessageaAnalyzerResult.setStarttm     (new Long(cols.get(3).getText()));
				asyncMessageaAnalyzerResult.setEndtm       (new Long(cols.get(4).getText()));					
				asyncMessageaAnalyzerResult.setDifferencetm(new Long(cols.get(5).getText()));	
				
				asyncResultsMap.put(asyncMessageaAnalyzerResultKey, asyncMessageaAnalyzerResult);
			}
					
		} catch (Exception e){
			e.printStackTrace();
			ThrowRuntimeExceptionWithMesage("asyncMessageAnalyzerPrintResults",  asyncMessageAnalyzerActionPage, asyncMessageaAnalyzerResult); 
		}
		return asyncResultsMap;
	}


	

	
	
	
	private void ThrowRuntimeExceptionWithMesage(String tag, SuperDataHunterActionPage resultsTable ) {
		ThrowRuntimeExceptionWithMesage(tag, resultsTable, null); 
	}			
	
	private void ThrowRuntimeExceptionWithMesage(String tag, SuperDataHunterActionPage resultsTable, Object bean ) {
		String message =  resultsTable.formatResultsMessage(tag) ;       
		if (bean != null){
			message = message + formatBeanValuesMessage(bean); 
		}
		message = message +  " - HTML [ " + resultsTable.getPageSource() + " ] "; 
		System.out.println(message);
		throw new RuntimeException(message);
	}

	
	/**
	 * Convenience method to output name/value pairs for a given bean.  Primary usage is for formatting error message output.
	 */
	public String formatBeanValuesMessage(Object bean){
		BeanUtilsBean  beanUtils = new BeanUtilsBean();
		Map<String,String> beanPropertiesMap = new HashMap<String,String>(); 
		
		try {
			beanPropertiesMap = beanUtils.describe(bean);
		} catch (Exception e) {
			System.out.println("catastropic error within formatBeanValuesMessage !!");
			e.printStackTrace();
		}
		String message = " - ADDITONAL DATA [ " + bean.getClass() + " ]"; 
		
		for (Map.Entry<String, String> beanProperty : beanPropertiesMap.entrySet()) {
		    message = message + " : " + beanProperty.getKey() + "=" + beanProperty.getValue();
		}
		return message;
	}
	
	
			
}
