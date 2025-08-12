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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */

@Controller
public class HomeController {
	
	
	@GetMapping({"/", "menu"})
	public String menu(Model model, HttpServletRequest request ) {

		String urltoContext = request.getServerName() + ":" + request.getServerPort() + request.getContextPath() ;
//		System.out.println("urltoContext:  [" + urltoContext + "]" );     
		model.addAttribute("urltoContext", urltoContext );
	
		String reqApp = request.getParameter("application");

		if (!DataHunterUtils.isEmpty(reqApp)){
			String reqUrlParms =  "?application=" + DataHunterUtils.encode(reqApp);
			String applicationStartsWithOrEquals  = request.getParameter("applicationStartsWithOrEquals");
			String identifier = request.getParameter("identifier");
			String lifecycle  = request.getParameter("lifecycle");
			String useability = request.getParameter("useability");
			if (!DataHunterUtils.isEmpty(applicationStartsWithOrEquals)){
				reqUrlParms += "&applicationStartsWithOrEquals=" + applicationStartsWithOrEquals;
			}
			if (!DataHunterUtils.isEmpty(identifier)){
				reqUrlParms += "&identifier=" + DataHunterUtils.encode(identifier);
			}
			if (!DataHunterUtils.isEmpty(lifecycle)){
				reqUrlParms += "&lifecycle=" + DataHunterUtils.encode(lifecycle);
			}
			if (!DataHunterUtils.isEmpty(useability)){
				reqUrlParms += "&useability=" + DataHunterUtils.encode(useability);
			}
			model.addAttribute("reqUrlParms", reqUrlParms);
			model.addAttribute("urlUseReqParmName", "&pUseOrLookup=");			
		} else { 	
			model.addAttribute("urlUseReqParmName", "?pUseOrLookup=");		
		}	

//		System.out.println(">>>> Home Controller request parameters");
//		java.util.Enumeration<String> params = request.getParameterNames(); 
//		while(params.hasMoreElements()){
//		 String paramName = params.nextElement();
//		 System.out.println("Parameter Name ["+paramName+"], Value ["+request.getParameter(paramName)+"]");
//		}
//		System.out.println("<<<< ");		
		
		model.addAttribute("DATAHUNTER_VERSION", DataHunterConstants.MARK59_VERSION_DATAHUNTER);
		return "menu";
	}
	
	
	@GetMapping("/overview")
	public String overview(Model model, HttpServletRequest request) {
        return "overview";
	}		
	
}
