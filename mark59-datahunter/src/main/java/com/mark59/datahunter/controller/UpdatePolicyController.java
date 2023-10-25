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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class UpdatePolicyController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@RequestMapping("/update_policy")
	public ModelAndView updatePolicyUrl(@RequestParam(required=false) String application,
			@RequestParam(required=false) String identifier,@RequestParam(required=false) String lifecycle,
			@ModelAttribute PolicySelectionCriteria policySelectionCriteria, Model model){ 
		Map<String, Object> map = new HashMap<>();
		map.put("policySelectionCriteria",policySelectionCriteria);		
		map.put("application", application);
		return new ModelAndView("update_policy", "map", map);		
	}
	

	@RequestMapping("/update_policy_data")
	public ModelAndView addPolicyUrl(@RequestParam(required=false) String application,
			@RequestParam(required=false) String identifier,@RequestParam(required=false) String lifecycle,			
			@ModelAttribute	PolicySelectionCriteria policySelectionCriteria, Model model){ 
		List<String> usabilityList = new ArrayList<String>(DataHunterConstants.USEABILITY_LIST);

		policySelectionCriteria.setSelectClause(PoliciesDAO.SELECT_POLICY_COLUMNS);
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPolicySql(policySelectionCriteria);
		List<Policies> policiesList = new ArrayList<Policies>();
		
		try {
			policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);
		} catch (Exception e) {
			model.addAttribute("sqlResult", "FAIL");
			model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
			return new ModelAndView("/update_policy_action", "model", model);	
		}
		
		Policies policies = new Policies(); 
		if (policiesList.size() == 1 ){
			policies = policiesList.get(0);
		} else {
			Map<String, Object> map = new HashMap<>();
			map.put("policySelectionCriteria", policySelectionCriteria);		
			map.put("reqErr", "oops, that Item does not exist");			
			model.addAttribute("map", map);	
			return new ModelAndView("update_policy", "map", map);
		}

		model.addAttribute("navUrParms", createNavUrlParms(policies));			
		model.addAttribute("policies", policies);		
		model.addAttribute("Useabilities", usabilityList);
		return new ModelAndView("/update_policy_data", "model", model);			
	}
		
	
	@RequestMapping("/update_policy_action")
	public ModelAndView updatePolicyAction(@ModelAttribute Policies policies, Model model, HttpServletRequest httpServletRequest ) {
		DataHunterUtils.expireSession(httpServletRequest); 
		
		if (policies.getEpochtime() == null){
			policies.setEpochtime(System.currentTimeMillis());
		}
		
		SqlWithParms sqlWithParms = policiesDAO.constructUpdatePoliciesSql(policies);
		model.addAttribute("sql", sqlWithParms);
		model.addAttribute("policies", policies);
		model.addAttribute("navUrParms", createNavUrlParms(policies));	
		
		int rowsAffected = 0;
		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			model.addAttribute("sqlResult", "FAIL");
			model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
			return new ModelAndView("/update_policy_action", "model", model);	
		}	
		
		model.addAttribute("rowsAffected", rowsAffected);
		
		if (rowsAffected == 1 ){
			model.addAttribute("sqlResult", "PASS");			
			model.addAttribute("sqlResultText", "sql execution OK");
		} else {
			model.addAttribute("sqlResult", "FAIL");		
			model.addAttribute("sqlResultText", "sql execution : Error.  1 row should of been affected, but sql result indicates " + rowsAffected + " rows affected?" );
		}
		return new ModelAndView("/update_policy_action", "model", model);
	}
	
	
	private String createNavUrlParms(Policies policies) {
		String navUrParms = "application=" + DataHunterUtils.encode(policies.getApplication())
		+ "&identifier=" + DataHunterUtils.encode(policies.getIdentifier()) 
		+ "&lifecycle="  + DataHunterUtils.encode(policies.getLifecycle())		
		+ "&useability=" + DataHunterUtils.encode(policies.getUseability())		
		+ "&otherdata="  + DataHunterUtils.encode(policies.getOtherdata())		
		+ "&epochtime=";
		
		if (policies.getEpochtime()!=null) {
			navUrParms +=  String.valueOf(policies.getEpochtime());
		}
		return navUrParms;
	}
}
