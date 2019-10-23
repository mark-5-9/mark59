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

package com.mark59.datahunter.controller;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.Utils;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class DeletePolicyController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@RequestMapping("/delete_policy")
	public String deletePolicyUrl(@RequestParam(required=false) String application, @ModelAttribute PolicySelectionCriteria policySelectionCriteria, Model model  ) { 
//		System.out.println("/delete_policy");
		return "/delete_policy";				
	}
	
		
	@RequestMapping("/delete_policy_action")
	public ModelAndView deletePolicyAction(@ModelAttribute PolicySelectionCriteria policySelectionCriteria, Model model, HttpServletRequest httpServletRequest) {
		Utils.expireSession(httpServletRequest);
		
		String sql = policiesDAO.constructDeletePoliciesSql(policySelectionCriteria);
		int rowsAffected = 0;
		
		model.addAttribute("sql", sql);
		
		try {
			
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sql);
			model.addAttribute("rowsAffected", rowsAffected);	
			
		} catch (Exception e) {
			model.addAttribute("sqlResult", "FAIL");
			model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
			model.addAttribute("rowsAffected", -1);				
			return new ModelAndView("/delete_policy_action", "model", model);	
		}	
		
		if (rowsAffected == 1 ){
			model.addAttribute("sqlResult", "PASS");			
			model.addAttribute("sqlResultText", "sql execution OK");
		} else if (rowsAffected == 0 ){
			model.addAttribute("sqlResult", "PASS");			
			model.addAttribute("sqlResultText", "no rows matching the selection - so nothing was deleted from the database");
		} else {
			model.addAttribute("sqlResult", "PASS");		
			model.addAttribute("sqlResultText", "sql execution OK: Note that mulitple rows ( "  + rowsAffected + " ) have been affected (deleted)" );
		}
		return new ModelAndView("/delete_policy_action", "model", model);
	}
	
}
