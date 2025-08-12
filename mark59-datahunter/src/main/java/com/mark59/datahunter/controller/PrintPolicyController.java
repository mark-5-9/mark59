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


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class PrintPolicyController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@GetMapping("/print_policy")
	public String printPolicyUrl(@RequestParam(required=false) String application,@ModelAttribute PolicySelectionCriteria policySelectionCriteria, Model model  ) { 
		return "/print_policy";				
	}
	
		
	@PostMapping("/print_policy_action")
	public ModelAndView printPolicyAction(@ModelAttribute PolicySelectionCriteria policySelectionCriteria,  Model model, HttpServletRequest httpServletRequest) {
		DataHunterUtils.expireSession(httpServletRequest);
		
		policySelectionCriteria.setSelectClause(PoliciesDAO.SELECT_POLICY_COLUMNS);
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPolicySql(policySelectionCriteria);
		List<Policies> policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);

		String navUrParms = "application=" + DataHunterUtils.encode(policySelectionCriteria.getApplication())
			+ "&identifier=" + DataHunterUtils.encode(policySelectionCriteria.getIdentifier()) 
			+ "&lifecycle="  + DataHunterUtils.encode(policySelectionCriteria.getLifecycle());

		model.addAttribute("navUrParms", navUrParms);			
		model.addAttribute("sql", sqlWithParms);
		model.addAttribute("rowsAffected", policiesList.size());
		
		if (policiesList.size() == 1 ){
			model.addAttribute("sqlResult", "PASS");			
			model.addAttribute("sqlResultText", "sql execution OK");
			model.addAttribute("policies", policiesList.get(0));
			return new ModelAndView("/print_policy_action", "model", model);
			
		} else if (policiesList.size() == 0){
			model.addAttribute("sqlResult", "FAIL");			
			model.addAttribute("sqlResultText", "No rows matching the selection.");
			return new ModelAndView("/print_policy_action_error", "model", model);
			
		} else {
			model.addAttribute("sqlResult", "FAIL");		
			model.addAttribute("sqlResultText", "sql execution : Error.  1 row should of been affected, but sql result indicates " + policiesList.size() + " rows affected?" );
			return new ModelAndView("/print_policy_action_error", "model", model);			
		}
	}
	
}
