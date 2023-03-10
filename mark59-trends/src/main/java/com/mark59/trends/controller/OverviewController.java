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

package com.mark59.trends.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@Controller
public class OverviewController {
	
	@RequestMapping(value = "/",  method = RequestMethod.GET)
	public String empty(Model model, HttpServletRequest request ) {
		return "overview";
	}	

	@RequestMapping(value = "/overview",  method = RequestMethod.GET)
	public String overview(Model model, HttpServletRequest request ) {
		return "overview";
	}		
	
	@RequestMapping(value = "/welcome",  method = RequestMethod.GET)
	public String welcome(Model model, HttpServletRequest request ) {
		return "overview";
	}		

	@RequestMapping(value = "/home",  method = RequestMethod.GET)
	public String home(Model model, HttpServletRequest request ) {
		return "overview";
	}		

}
