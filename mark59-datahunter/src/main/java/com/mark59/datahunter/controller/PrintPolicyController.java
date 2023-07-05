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
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
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
		

	@RequestMapping("/print_policy")
	public String printPolicyUrl(@RequestParam(required=false) String application,@ModelAttribute PolicySelectionCriteria policySelectionCriteria, Model model  ) { 
//		System.out.println("/print_policy");
		return "/print_policy";				
	}
	
		
	@RequestMapping("/print_policy_action")
	public ModelAndView printPolicyAction(@ModelAttribute PolicySelectionCriteria policySelectionCriteria, HttpServletRequest httpServletRequest) {
		DataHunterUtils.expireSession(httpServletRequest);
		
		policySelectionCriteria.setSelectClause(PoliciesDAO.SELECT_POLICY_COLUMNS);
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPolicySql(policySelectionCriteria);
		
		List<Policies> policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);

		//System.out.println("printPolicyAction" + policySelectionCriteria +  "policies count=" + policiesList.size() );

		ModelMap modelMap = new ModelMap();
		modelMap.addAttribute("sql", sqlWithParms);
		modelMap.addAttribute("rowsAffected", policiesList.size());
		
		if (policiesList.size() == 1 ){
			modelMap.addAttribute("sqlResult", "PASS");			
			modelMap.addAttribute("sqlResultText", "sql execution OK");
			modelMap.addAttribute("policies", policiesList.get(0));
			return new ModelAndView("/print_policy_action", "modelMap", modelMap);
			
		} else if (policiesList.size() == 0){
			modelMap.addAttribute("sqlResult", "FAIL");			
			modelMap.addAttribute("sqlResultText", "No rows matching the selection.");
			return new ModelAndView("/print_policy_action_error", "modelMap", modelMap);
			
		} else {
			modelMap.addAttribute("sqlResult", "FAIL");		
			modelMap.addAttribute("sqlResultText", "sql execution : Error.  1 row should of been affected, but sql result indicates " + policiesList.size() + " rows affected?" );
			return new ModelAndView("/print_policy_action_error", "modelMap", modelMap);			
		}

	}
	
}
