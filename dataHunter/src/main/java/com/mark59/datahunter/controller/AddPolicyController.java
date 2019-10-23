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

import com.mark59.datahunter.application.AppConstants;
import com.mark59.datahunter.application.Utils;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class AddPolicyController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@RequestMapping("/add_policy")
	public String addPolicyUrl(@RequestParam(required=false) String application,@ModelAttribute Policies policies, Model model  ) { 
		List<String> usabilityList = new ArrayList<String>(AppConstants.USEABILITY_LIST);
		model.addAttribute("Useabilities",usabilityList);
		return "/add_policy";				
	}
	
		
	@RequestMapping("/add_policy_action")
	public ModelAndView addPolicyAction(@ModelAttribute Policies policies, Model model, HttpServletRequest httpServletRequest ) {

		Utils.expireSession(httpServletRequest); 
		
		if (policies.getEpochtime() == null){
			policies.setEpochtime(System.currentTimeMillis());
		}
		
		String sql = policiesDAO.constructInsertDataSql(policies);
		int rowsAffected = 0;
		
		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sql);
		} catch (Exception e) {
			model.addAttribute("sqlResult", "FAIL");
			model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
			return new ModelAndView("/add_policy_action", "model", model);	
		}	
		
		model.addAttribute("sql", sql);
		model.addAttribute("rowsAffected", rowsAffected);
		
		if (rowsAffected == 1 ){
			model.addAttribute("sqlResult", "PASS");			
			model.addAttribute("sqlResultText", "sql execution OK");
		} else {
			model.addAttribute("sqlResult", "FAIL");		
			model.addAttribute("sqlResultText", "sql execution : Error.  1 row should of been affected, but sql result indicates " + rowsAffected + " rows affected?" );
		}
		return new ModelAndView("/add_policy_action", "model", model);
	}
	
}
