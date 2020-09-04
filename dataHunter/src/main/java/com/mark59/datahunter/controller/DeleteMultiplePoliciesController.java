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


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */

@Controller
public class DeleteMultiplePoliciesController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@RequestMapping("/delete_multiple_policies")
	public String deleteMultiplePoliciesUrl(@RequestParam(required=false) String application, @ModelAttribute PolicySelectionCriteria policySelectionCriteria, Model model  ) { 
//		System.out.println("/delete_multiple_policiess");
		List<String> usabilityList = new ArrayList<String>(DataHunterConstants.USEABILITY_LIST);
		usabilityList.add(0,"");
		model.addAttribute("Useabilities",usabilityList);
		return "/delete_multiple_policies";				
	}
	
		
	@RequestMapping("/delete_multiple_policies_action")
	public ModelAndView deleteMultiplePoliciesAction(@ModelAttribute PolicySelectionCriteria policySelectionCriteria, Model model, HttpServletRequest httpServletRequest) {

		String sql = policiesDAO.constructDeleteMultiplePoliciesSql(policySelectionCriteria);
		int rowsAffected = policiesDAO.runDatabaseUpdateSql(sql);

		model.addAttribute("sql", sql);
		model.addAttribute("sqlResult", "PASS");
		model.addAttribute("rowsAffected", rowsAffected);	

		if (rowsAffected == 0 ){
			model.addAttribute("sqlResultText", "sql execution OK, but no rows matched the selection criteria.");
		} else {
			model.addAttribute("sqlResultText", "sql execution OK");
		}
		DataHunterUtils.expireSession(httpServletRequest);
		
		return new ModelAndView("/delete_multiple_policies_action", "model", model);
	}
	
}
