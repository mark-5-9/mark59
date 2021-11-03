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


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class NextPolicyController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		
	@RequestMapping("/next_policy")
	public String lookupNextPolicyUrl(@RequestParam(required=false) String application, @RequestParam String pUseOrLookup,  @ModelAttribute PolicySelectionCriteria policySelectionCriteria, Model model  ) {
//		System.out.println("/lookupNextPolicyUrl called in mode " + pUseOrLookup );		
		List<String> getNextPolicySelector = new ArrayList<String>(DataHunterConstants.GET_NEXT_POLICY_SELECTOR);
		model.addAttribute("getNextPolicySelector",getNextPolicySelector);
		List<String> usabilityList = new ArrayList<String>(DataHunterConstants.USEABILITY_LIST);
		model.addAttribute("Useabilities",usabilityList);
		model.addAttribute("UseOrLookup", pUseOrLookup);
		return "/next_policy";
	}


	
	@RequestMapping("/{lookupOrUsePathVariable}_next_policy_action")
	public ModelAndView nextPolicyAction(@ModelAttribute PolicySelectionCriteria policySelectionCriteria, Model model, @PathVariable String lookupOrUsePathVariable, 
			 HttpServletRequest httpServletRequest) throws SQLException {
//		System.out.println( "lookupOrUsePathVariable :" + lookupOrUsePathVariable ); //System.out.println( "mapping :" + httpServletRequest.getServletPath() ); 

		DataHunterUtils.expireSession(httpServletRequest);
		List<Policies> policiesList = new ArrayList<Policies>();
		
		policySelectionCriteria.setSelectClause(" application, identifier, lifecycle, useability, otherdata, created, updated, epochtime ");
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPoliciesSql(policySelectionCriteria);
		model.addAttribute("sql", sqlWithParms);
		
		try {
			if (lookupOrUsePathVariable.equals("use")){
				synchronized (this){ policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms); }
			} else {
				policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);
			}
		} catch (Exception e) {
			model.addAttribute("sqlResult", "FAIL");
			model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
			return new ModelAndView("/next_policy_action", "model", model);		
		}
				
		int rowsAffected = policiesList.size();
		model.addAttribute("rowsAffected", rowsAffected);
		
		if (policiesList.size() == 0){
			model.addAttribute("sqlResult", "FAIL");			
			model.addAttribute("sqlResultText", "No rows matching the selection.  Possibly we have ran out of data for application:[" + policySelectionCriteria.getApplication() + "]" );
			return new ModelAndView("/next_policy_action", "model", model);
			
		} else if (policiesList.size() > 1){
			model.addAttribute("sqlResult", "FAIL");		
			model.addAttribute("sqlResultText", "sql execution : Error.  1 row should of been affected, but sql result indicates " + policiesList.size() + " rows affected?" );
			return new ModelAndView("/next_policy_action", "model", model);	
		}
		
		Policies nextPolicy = (Policies)policiesList.get(0);
		model.addAttribute("policies", nextPolicy);		
		
		if (lookupOrUsePathVariable.equals("use")){

			if (DataHunterConstants.REUSABLE.equalsIgnoreCase(nextPolicy.getUseability())){
				model.addAttribute("sqlResult", "PASS");
				model.addAttribute("sqlResultText", "sql execution OK.  However policy " + nextPolicy.getIdentifier() + " NOT updated to USED, as it is marked as REUSABLE");	
				return new ModelAndView("/next_policy_action", "model", model);
			} 			

			try {
				SqlWithParms updateSqlWithParms = policiesDAO.constructUpdatePolicyToUsedSql(nextPolicy);
				model.addAttribute("sql", "SELECT STMT : " + sqlWithParms + "<br>  UPDATE STMT : " + updateSqlWithParms + ".");
				
				int rowsUpdated = policiesDAO.runDatabaseUpdateSql(updateSqlWithParms);
				model.addAttribute("rowsAffected", rowsUpdated);
				
				if (rowsUpdated == 1){
					model.addAttribute("sqlResult",  "PASS");
					model.addAttribute("sqlResultText", "sql execution OK.");
				} else {
					model.addAttribute("sqlResult",  "FAIL");
					model.addAttribute("sqlResultText", "1 row should of been updated, but sql reurn count for update indicates " + rowsUpdated + " rows affected") ;					
				}				
				
			} catch (Exception e) {
				model.addAttribute("sqlResult", "FAIL");
				model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
			}
		
		} else {  																				//assume just a lookup
			model.addAttribute("sqlResult", "PASS");			
			model.addAttribute("sqlResultText", "sql execution OK (no update) ");		
		}
		
		return new ModelAndView("/next_policy_action", "model", model);
	}
	
}
