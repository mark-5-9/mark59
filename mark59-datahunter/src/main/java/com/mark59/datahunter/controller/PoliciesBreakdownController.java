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

package com.mark59.datahunter.controller;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.CountPoliciesBreakdown;
import com.mark59.datahunter.model.CountPoliciesBreakdownForm;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.pojo.ReindexResult;
import com.mark59.datahunter.pojo.ValidReuseIxPojo;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class PoliciesBreakdownController {
	
	@Autowired
	PoliciesDAO policiesDAO;	

	@GetMapping("/policies_breakdown")
	public String policiesBreakdownUrl(@RequestParam(required = false) String reqApplication,
			PolicySelectionCriteria policySelectionCriteria, Model model) {
//		System.out.println("/policies_breakdown");
		List<String> applicationOperators = new ArrayList<>(DataHunterConstants.APPLICATION_OPERATORS);
		model.addAttribute("applicationOperators",applicationOperators);
		List<String> usabilityList = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		usabilityList.add(0,"");
		model.addAttribute("Useabilities",usabilityList);		
		return "/policies_breakdown";
	}
	
	
	@GetMapping("/policies_breakdown_action")
	public ModelAndView policiesBreakdownActionGet(@RequestParam(required = false) String application,
			@RequestParam(required = false) String applicationStartsWithOrEquals,
			@RequestParam(required = false) String lifecycle,
			@RequestParam(required = false) String useability,
			Model model, HttpServletRequest httpServletRequest) {
		
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(application);
		policySelectionCriteria.setApplicationStartsWithOrEquals(applicationStartsWithOrEquals);
		policySelectionCriteria.setLifecycle(lifecycle);
		policySelectionCriteria.setUseability(useability);
		return policiesBreakdownAction(policySelectionCriteria, model, httpServletRequest);	
	}
		
	@PostMapping("/policies_breakdown_action")
	public ModelAndView policiesBreakdownAction(@ModelAttribute PolicySelectionCriteria policySelectionCriteria,
			Model model, HttpServletRequest httpServletRequest) {
		
		// this just aligns the Application value shown on the action page, with what is actually used in the sql
		if (DataHunterConstants.STARTS_WITH.equals(policySelectionCriteria.getApplicationStartsWithOrEquals())){
			// allows for embedded space within Application name, but still remove leading whitespace 
			policySelectionCriteria.setApplication(StringUtils.stripStart(policySelectionCriteria.getApplication(), null));
		} else { // 'EQUALS'
			policySelectionCriteria.setApplication(policySelectionCriteria.getApplication().trim()); 
		}

		String navUrParms = "application=" + DataHunterUtils.encode(policySelectionCriteria.getApplication())
			+ "&applicationStartsWithOrEquals=" + DataHunterUtils.encode(policySelectionCriteria.getApplicationStartsWithOrEquals()) 
			+ "&lifecycle="    + DataHunterUtils.encode(policySelectionCriteria.getLifecycle()) 
			+ "&useability="   + DataHunterUtils.encode(policySelectionCriteria.getUseability());

		model.addAttribute("navUrParms", navUrParms);			
		
		SqlWithParms sqlWithParms = policiesDAO.constructCountPoliciesBreakdownSql(policySelectionCriteria);
		List<CountPoliciesBreakdown> countPoliciesBreakdownList = policiesDAO.runCountPoliciesBreakdownSql(sqlWithParms);
		int rowsAffected = countPoliciesBreakdownList.size();
		
		List<CountPoliciesBreakdownForm > countPoliciesBreakdownFormList = new ArrayList<CountPoliciesBreakdownForm>();
		
		for (CountPoliciesBreakdown countPoliciesBreakdown : countPoliciesBreakdownList) {
			CountPoliciesBreakdownForm countPoliciesBreakdownForm = new CountPoliciesBreakdownForm(); 
			countPoliciesBreakdownForm.setApplicationStartsWithOrEquals(countPoliciesBreakdown.getApplicationStartsWithOrEquals());
			countPoliciesBreakdownForm.setApplication(countPoliciesBreakdown.getApplication());
			countPoliciesBreakdownForm.setLifecycle(countPoliciesBreakdown.getLifecycle());
			countPoliciesBreakdownForm.setUseability(countPoliciesBreakdown.getUseability());
			countPoliciesBreakdownForm.setRowCount(countPoliciesBreakdown.getRowCount());
			countPoliciesBreakdownForm.setLookupParmsUrl(
				"application="   + DataHunterUtils.encode(countPoliciesBreakdown.getApplication()) 
				+ "&lifecycle="  + DataHunterUtils.encode(countPoliciesBreakdown.getLifecycle())
				+ "&useability=" + DataHunterUtils.encode(countPoliciesBreakdown.getUseability()));
			
			countPoliciesBreakdownForm.setIsReusableIndexed("N");
			countPoliciesBreakdownForm.setHoleCount(0L);
			countPoliciesBreakdownForm.setHoleStats("");

			ValidReuseIxPojo validReuseIx = policiesDAO.validateReusableIndexed(countPoliciesBreakdown);
			if (validReuseIx.getPolicyReusableIndexed()){
				countPoliciesBreakdownForm.setIsReusableIndexed("Y");
				if (validReuseIx.getValidatedOk()) {
					if (countPoliciesBreakdown.getRowCount() <= 1 ){  // only the IX row itself exists 
						countPoliciesBreakdownForm.setHoleCount(0L);
						countPoliciesBreakdownForm.setHoleStats("na");
					} else {
						Long pcHoles = 0L; 
						sqlWithParms = policiesDAO.countValidIndexedIdsInExpectedRange(countPoliciesBreakdown, validReuseIx.getCurrentIxCount());
						validReuseIx.setValidIdsinRangeCount(policiesDAO.runCountSql(sqlWithParms));		
						countPoliciesBreakdown.setHoleCount(Long.valueOf(validReuseIx.getCurrentIxCount()) - validReuseIx.getValidIdsinRangeCount());						
						if (validReuseIx.getCurrentIxCount() > 0) {
							pcHoles = (countPoliciesBreakdown.getHoleCount()*100) / validReuseIx.getCurrentIxCount(); 
						}
						countPoliciesBreakdownForm.setHoleStats(countPoliciesBreakdown.getHoleCount() 
								+ " ("+pcHoles+"%), ix="+validReuseIx.getCurrentIxCount());
					}	
				} else { // invalid 
					countPoliciesBreakdownForm.setHoleCount(-1L);
					countPoliciesBreakdownForm.setHoleStats("?");					
				}
			} // reusable ix
			countPoliciesBreakdownFormList.add(countPoliciesBreakdownForm);
		} // for

		model.addAttribute("countPoliciesBreakdownFormList", countPoliciesBreakdownFormList);

		model.addAttribute("sql", sqlWithParms);
		model.addAttribute("sqlResult", "PASS");
		model.addAttribute("rowsAffected", rowsAffected);	

		if (rowsAffected == 0 ){
			model.addAttribute("sqlResultText", "sql execution OK, but no rows matched the selection criteria.");
		} else {
			model.addAttribute("sqlResultText", "sql execution OK");
		}
		DataHunterUtils.expireSession(httpServletRequest);
		
		return new ModelAndView("policies_breakdown_action", "model", model);
	}
	
	
	@GetMapping("/policies_breakdown_reindex")
	public ModelAndView policiesBreakdownReindex(@ModelAttribute PolicySelectionCriteria policySelectionCriteria,
			Model model, HttpServletRequest httpServletRequest) {
		DataHunterUtils.expireSession(httpServletRequest);

		String navUrParms = "application=" + DataHunterUtils.encode(policySelectionCriteria.getApplication())
			+ "&applicationStartsWithOrEquals="+DataHunterUtils.encode(policySelectionCriteria.getApplicationStartsWithOrEquals()) 
			+ "&lifecycle="  + DataHunterUtils.encode(policySelectionCriteria.getLifecycle()) 
			+ "&useability=" + DataHunterUtils.encode(policySelectionCriteria.getUseability());

		ReindexResult result = policiesDAO.reindexReusableIndexed(
				policySelectionCriteria.getApplication(),
				policySelectionCriteria.getLifecycle());

		model.addAttribute("navUrParms", navUrParms);			
		model.addAttribute("reindexResultSuccess",result.getSuccess());			
		model.addAttribute("reindexResultMessage",result.getMessage());			
		model.addAttribute("reindexResultRowsMoved",result.getRowsMoved());			
		model.addAttribute("reindexResulIxCount",result.getIxCount());			
		return new ModelAndView("policies_breakkown_reindex_action", "model", model);
	}
	
}
