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
import com.mark59.datahunter.application.ReusableIndexedUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.pojo.ValidReuseIxPojo;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class AddPolicyController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@RequestMapping("/add_policy")
	public String addPolicyUrl(@RequestParam(required=false) String application,@ModelAttribute Policies policies, Model model) {
		
		ValidReuseIxPojo validReuseIx = ReusableIndexedUtils.validateReusableIndexed(policies, policiesDAO);
		if (validReuseIx.getPolicyReusableIndexed()){
			if (validReuseIx.getValidatedOk()) {
				int newCount = validReuseIx.getCurrentIxCount() + 1;
				validReuseIx.getIxPolicy().setOtherdata(String.valueOf(newCount)); 		
				policies.setIdentifier(StringUtils.leftPad(String.valueOf(newCount), 10, "0"));
			} else {
				policies.setIdentifier("?");
			}
		} 
		
		List<String> usabilityList = new ArrayList<String>(DataHunterConstants.USEABILITY_LIST);
		model.addAttribute("Useabilities",usabilityList);
		return "/add_policy";				
	}
	
		
	@RequestMapping("/add_policy_action")
	public ModelAndView addPolicyAction(@ModelAttribute Policies policies, Model model, HttpServletRequest httpServletRequest ) {

		DataHunterUtils.expireSession(httpServletRequest); 
		
		if (policies.getEpochtime() == null){
			policies.setEpochtime(System.currentTimeMillis());
		}

		String navUrParms = "application=" + DataHunterUtils.encode(policies.getApplication())
			+ "&identifier=" + DataHunterUtils.encode(policies.getIdentifier()) 
			+ "&lifecycle="  + DataHunterUtils.encode(policies.getLifecycle()) 
			+ "&useability=" + DataHunterUtils.encode(policies.getUseability());
		model.addAttribute("navUrParms", navUrParms);		
				
		ValidReuseIxPojo validReuseIx = ReusableIndexedUtils.validateReusableIndexed(policies, policiesDAO);
		
		if (validReuseIx.getPolicyReusableIndexed()){
			if (validReuseIx.getValidatedOk()) {
				int newCount = validReuseIx.getCurrentIxCount() + 1;
				validReuseIx.getIxPolicy().setOtherdata(String.valueOf(newCount)); 		
				SqlWithParms sqlWithParmsIx = policiesDAO.constructUpdatePoliciesSql(validReuseIx.getIxPolicy());
				
				try {
					System.out.println("update ix : " + validReuseIx.getIxPolicy());
					policiesDAO.runDatabaseUpdateSql(sqlWithParmsIx);
				} catch (Exception e) {
					model.addAttribute("sqlResult", "FAIL");
					model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
					model.addAttribute("rowsAffected", 0);
					return new ModelAndView("/add_policy_action", "model", model);	
				}	
				
				policies.setIdentifier(StringUtils.leftPad(String.valueOf(newCount), 10, "0"));
				
			} else { // invalid
				model.addAttribute("sqlResult", "N/A");
				model.addAttribute("sqlResultText", "validation error: "  + validReuseIx.getErrorMsg() );
				model.addAttribute("rowsAffected", 0);
				return new ModelAndView("/add_policy_action", "model", model);	
			}
		} 
		
		SqlWithParms sqlWithParms = policiesDAO.constructInsertDataSql(policies);
		
		model.addAttribute("sql", sqlWithParms);
		int rowsAffected = 0;
		try {
			rowsAffected = rowsAffected + policiesDAO.runDatabaseUpdateSql(sqlWithParms);
		} catch (Exception e) {
			model.addAttribute("sqlResult", "FAIL");
			model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
			model.addAttribute("rowsAffected", 0);
			return new ModelAndView("/add_policy_action", "model", model);	
		}	
		
		model.addAttribute("sql", sqlWithParms);
		model.addAttribute("rowsAffected", rowsAffected);
		
		if (rowsAffected == 1 ){
			model.addAttribute("sqlResult", "PASS");			
			model.addAttribute("sqlResultText", "sql execution OK");
		} else {
			model.addAttribute("sqlResult", "FAIL");		
			model.addAttribute("sqlResultText", "sql execution : Error.  1 row should of been affected, but sql result indicates "+rowsAffected+" rows affected?" );
		}
		return new ModelAndView("/add_policy_action", "model", model);
	}
	
}
