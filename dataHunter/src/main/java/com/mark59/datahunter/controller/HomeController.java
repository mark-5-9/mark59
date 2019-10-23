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

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */

@Controller
public class HomeController {
	
	@RequestMapping(value = "/",  method = RequestMethod.GET)
	public String welcome(Model model, HttpServletRequest request ) {

		String urltoContext = request.getServerName() + ":" + Integer.toString(request.getServerPort()) + request.getContextPath() ; 
//		System.out.println("urltoContext:  [" + urltoContext + "]" );     
		
		model.addAttribute("urltoContext", urltoContext );
		
//		System.out.println(">>>> Mome Controller request parameters");
//		Enumeration<String> params = request.getParameterNames(); 
//		while(params.hasMoreElements()){
//		 String paramName = params.nextElement();
//		 System.out.println("Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
//		}
//		System.out.println("<<<< ");		
		
		
		String application = request.getParameter("application");
		
		if (!StringUtils.isEmpty(application)){
			model.addAttribute("urlAppReqParm", "?application=" + application );
			model.addAttribute("urlUseReqParmName", "&pUseOrLookup=");			
		} else { 	
			model.addAttribute("urlUseReqParmName", "?pUseOrLookup=");		
		}	
		
		return "welcome";
	}	
	
}
