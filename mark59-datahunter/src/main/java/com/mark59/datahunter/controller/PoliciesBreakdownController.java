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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.CountPoliciesBreakdown;
import com.mark59.datahunter.model.CountPoliciesBreakdownForm;
import com.mark59.datahunter.model.PolicySelectionCriteria;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class PoliciesBreakdownController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@RequestMapping("/policies_breakdown")
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
	
		
	@RequestMapping("/policies_breakdown_action")
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
			countPoliciesBreakdownFormList.add(countPoliciesBreakdownForm);
		}
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
	
}
