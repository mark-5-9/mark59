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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.AsyncMessageAnalyzerRequest;
import com.mark59.datahunter.model.AsyncMessageAnalyzerResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class AsyncMessageAnalyzerController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@GetMapping("/async_message_analyzer")
	public String asyncMessageAnalyzerUrl(@RequestParam(required=false) String application, AsyncMessageAnalyzerRequest asyncMessageAnalyzerRequest, Model model) { 
		List<String> applicationOperators = new ArrayList<>(DataHunterConstants.APPLICATION_OPERATORS);
		model.addAttribute("applicationOperators",applicationOperators);
		List<String> usabilityList = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		
		usabilityList.add(0,"UNPAIRED");		
		usabilityList.add(1,"");		
		model.addAttribute("Useabilities",usabilityList);	
		
		List<String> usabilityListTo = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		usabilityListTo.add(0,"");
		model.addAttribute("usabilityListTo",usabilityListTo);
		
		return "/async_message_analyzer";
	}
	
		
	@PostMapping("/async_message_analyzer_action")
	public ModelAndView asyncMessageAnalyzerUrlAction(@ModelAttribute AsyncMessageAnalyzerRequest asyncMessageAnalyzerRequest, Model model, HttpServletRequest httpServletRequest) {
		
		SqlWithParms analyzerSqlWithParms = policiesDAO.constructAsyncMessageAnalyzerSql(asyncMessageAnalyzerRequest);
		List<AsyncMessageAnalyzerResult> asyncMessageAnalyzerResultList = policiesDAO.runAsyncMessageAnalyzerSql(analyzerSqlWithParms);
		int rowsAffected = asyncMessageAnalyzerResultList.size();

		String navUrParms = "application=" + DataHunterUtils.encode(asyncMessageAnalyzerRequest.getApplication())
			+ "&applicationStartsWithOrEquals=" + DataHunterUtils.encode(asyncMessageAnalyzerRequest.getApplicationStartsWithOrEquals()) 
			+ "&identifier="   + DataHunterUtils.encode(asyncMessageAnalyzerRequest.getIdentifier()) 
			+ "&useability="   + DataHunterUtils.encode(asyncMessageAnalyzerRequest.getUseability())
			+ "&toUseability=" + DataHunterUtils.encode(asyncMessageAnalyzerRequest.getToUseability());

		model.addAttribute("navUrParms", navUrParms);			
		
		if (rowsAffected == 0 ){
			model.addAttribute("asyncMessageAnalyzerResultList", asyncMessageAnalyzerResultList);		
			model.addAttribute("sql", analyzerSqlWithParms);
			model.addAttribute("sqlResult", "PASS");
			model.addAttribute("rowsAffected", rowsAffected);
			model.addAttribute("sqlResultText", "sql execution OK, but no rows matched the selection criteria.");
			return new ModelAndView("/async_message_analyzer_action", "model", model);
		}
		
		if ( ! DataHunterUtils.isEmpty(asyncMessageAnalyzerRequest.getToUseability())){
			try {
				asyncMessageAnalyzerResultList = policiesDAO.updateMultiplePoliciesUseState(asyncMessageAnalyzerResultList, asyncMessageAnalyzerRequest.getToUseability());
			} catch (Exception e) {
				model.addAttribute("asyncMessageAnalyzerResultList", asyncMessageAnalyzerResultList);						
				model.addAttribute("sql", "not provided (failure of change of useability may indicate point of failure)");
				model.addAttribute("sqlResult", "FAIL");
				model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
				model.addAttribute("rowsAffected", rowsAffected);	
				return new ModelAndView("/async_message_analyzer_action", "model", model);					
			}
		}
		
		model.addAttribute("asyncMessageAnalyzerResultList", asyncMessageAnalyzerResultList);		
		model.addAttribute("sql", analyzerSqlWithParms);
		model.addAttribute("sqlResult", "PASS");
		model.addAttribute("rowsAffected", rowsAffected);	
		model.addAttribute("sqlResultText", "sql execution OK");

		DataHunterUtils.expireSession(httpServletRequest);
		
		return new ModelAndView("/async_message_analyzer_action", "model", model);
	}
	
}
