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
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class UpdatePoliciesUseStateController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@RequestMapping("/update_policies_use_state")
	public String updatePoliciesUseStatetateUrl(@RequestParam(required=false) String application, @ModelAttribute UpdateUseStateAndEpochTime updateUseStateAndEpochTime, Model model  ) { 
//		System.out.println("/update_policies_use_state");
		List<String> usabilityListFrom = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		usabilityListFrom.add(0,"");
		model.addAttribute("usabilityListFrom",usabilityListFrom);
		List<String> usabilityListTo = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		model.addAttribute("usabilityListTo",usabilityListTo);		
		return "/update_policies_use_state";				
	}
	
		
	@RequestMapping("/update_policies_use_state_action")
	public ModelAndView updatePoliciesUseStatetateUrlAction(@ModelAttribute UpdateUseStateAndEpochTime updateUseStateAndEpochTime,  Model model, HttpServletRequest httpServletRequest) {
		DataHunterUtils.expireSession(httpServletRequest);

		SqlWithParms sqlWithParms = policiesDAO.constructUpdatePoliciesUseStateSql(updateUseStateAndEpochTime);
		model.addAttribute("sql", sqlWithParms);

		
		String navUrParms = "application=" + DataHunterUtils.encode(updateUseStateAndEpochTime.getApplication())
			+ "&identifier=" + DataHunterUtils.encode(updateUseStateAndEpochTime.getIdentifier()) 
			+ "&lifecycle=" + DataHunterUtils.encode(updateUseStateAndEpochTime.getLifecycle()) 
			+ "&useability=" + DataHunterUtils.encode(updateUseStateAndEpochTime.getUseability())
			+ "&toUseability="  + DataHunterUtils.encode(updateUseStateAndEpochTime.getToUseability())
			+ "&toEpochTime=";
		
		if (updateUseStateAndEpochTime.getToEpochTime()!=null) {
			navUrParms +=  String.valueOf(updateUseStateAndEpochTime.getToEpochTime());
		} 
		model.addAttribute("navUrParms", navUrParms);		

		int rowsAffected;
		try {
			rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			model.addAttribute("sqlResult", "FAIL");
			model.addAttribute("sqlResultText", "sql exception caught: " + e.getMessage() );
			return new ModelAndView("/update_policies_use_state_action", "model", model);	
		}	
			
		model.addAttribute("rowsAffected", rowsAffected);
		
		if (rowsAffected == 0 ){
			model.addAttribute("sqlResult", "PASS");	
			model.addAttribute("sqlResultText", "sql execution OK, but no rows matched the selection criteria.  Nothing was updated on the database");
		} else if (rowsAffected == 1 ){  
			model.addAttribute("sqlResult", "PASS");	
			model.addAttribute("sqlResultText", "sql execution OK." );
		} else {
			model.addAttribute("sqlResult", "PASS");	
			model.addAttribute("sqlResultText", "sql execution OK.  Note muliple rows where updated by this query" );
		}	
		return new ModelAndView("/update_policies_use_state_action", "model", model);
	}
	
}
