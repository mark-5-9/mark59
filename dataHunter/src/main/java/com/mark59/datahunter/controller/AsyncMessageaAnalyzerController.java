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

import java.time.Instant;
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
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerRequest;
import com.mark59.datahunter.model.AsyncMessageaAnalyzerResult;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
@Controller
public class AsyncMessageaAnalyzerController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		

	@RequestMapping("/async_message_analyzer")
	public String asyncMessageaAnalyzerUrl(@RequestParam(required=false) String application, AsyncMessageaAnalyzerRequest asyncMessageaAnalyzerRequest, Model model) { 
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
	
		
	@RequestMapping("/async_message_analyzer_action")
	public ModelAndView asyncMessageaAnalyzerUrlAction(@ModelAttribute AsyncMessageaAnalyzerRequest asyncMessageaAnalyzerRequest, Model model, HttpServletRequest httpServletRequest) {
		
		SqlWithParms analyzerSqlWithParms = policiesDAO.constructAsyncMessageaAnalyzerSql(asyncMessageaAnalyzerRequest);
		List<AsyncMessageaAnalyzerResult> asyncMessageaAnalyzerResultList = policiesDAO.runAsyncMessageaAnalyzerSql(analyzerSqlWithParms);
		int rowsAffected = asyncMessageaAnalyzerResultList.size();
		
		if (rowsAffected == 0 ){
			model.addAttribute("asyncMessageaAnalyzerResultList", asyncMessageaAnalyzerResultList);		
			model.addAttribute("sql", analyzerSqlWithParms);
			model.addAttribute("sqlResult", "PASS");
			model.addAttribute("rowsAffected", rowsAffected);
			model.addAttribute("sqlResultText", "sql execution OK, but no rows matched the selection criteria.");
			return new ModelAndView("/async_message_analyzer_action", "model", model);
		}
		
		long startepoch = Instant.now().toEpochMilli() ;
		System.out.println("## AsyncMessageaAnalyzerController start update at " +  startepoch) ;
		
		if ( ! DataHunterUtils.isEmpty(asyncMessageaAnalyzerRequest.getToUseability())){
			
//			UpdateUseStateAndEpochTime updateUse = new UpdateUseStateAndEpochTime();
//			updateUse.setUseability(asyncMessageaAnalyzerRequest.getUseability() );
//			updateUse.setToUseability(asyncMessageaAnalyzerRequest.getToUseability());
//			
//			for (AsyncMessageaAnalyzerResult asyncMessageaAnalyzerResult : asyncMessageaAnalyzerResultList) {
//				updateUse.setApplication(asyncMessageaAnalyzerResult.getApplication());
//				updateUse.setIdentifier(asyncMessageaAnalyzerResult.getIdentifier());
//				// lifecycle updated for all rows of the given id and so left blank (lifecycle is the part of the key that changes for each async event) 
//				updateUse.setLifecycle(""); 
//				SqlWithParms sqlWithParms = policiesDAO.constructUpdatePoliciesUseStateSql(updateUse);
//				try {
//					policiesDAO.runDatabaseUpdateSql(sqlWithParms);
//				} catch (Exception e) {
//					model.addAttribute("sql", sqlWithParms);
//					model.addAttribute("sqlResult", "FAIL");
//					model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
//					return new ModelAndView("/async_message_analyzer_action", "model", model);	
//				}
//				asyncMessageaAnalyzerResult.setUseability(asyncMessageaAnalyzerRequest.getToUseability());
//			} 
			
			try {
				asyncMessageaAnalyzerResultList = policiesDAO.updateMultiplePoliciesUseState(asyncMessageaAnalyzerResultList, asyncMessageaAnalyzerRequest.getToUseability());
			} catch (Exception e) {
				model.addAttribute("asyncMessageaAnalyzerResultList", asyncMessageaAnalyzerResultList);						
				model.addAttribute("sql", "not provided (failure of change of useability may indicate point of failure)");
				model.addAttribute("sqlResult", "FAIL");
				model.addAttribute("sqlResultText", "sql exception caught: "  + e.getMessage() );
				model.addAttribute("rowsAffected", rowsAffected);	
				return new ModelAndView("/async_message_analyzer_action", "model", model);					
			}
			
			
		}
		
		long endepoch = Instant.now().toEpochMilli() ;
		System.out.println("## AsyncMessageaAnalyzerController ends  update at " +  endepoch) ;
		long timetaken = endepoch - startepoch;
		System.out.println("## AsyncMessageaAnalyzerController " + asyncMessageaAnalyzerResultList.size() + " rows updated, taking " + timetaken + " ms");  
		
		model.addAttribute("asyncMessageaAnalyzerResultList", asyncMessageaAnalyzerResultList);		
		model.addAttribute("sql", analyzerSqlWithParms);
		model.addAttribute("sqlResult", "PASS");
		model.addAttribute("rowsAffected", rowsAffected);	
		model.addAttribute("sqlResultText", "sql execution OK");

		DataHunterUtils.expireSession(httpServletRequest);
		
		return new ModelAndView("/async_message_analyzer_action", "model", model);
	}
	
}
