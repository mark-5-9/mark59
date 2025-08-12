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

package com.mark59.metrics.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.mark59.metrics.PropertiesConfiguration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author Philip Webb Written: Australian Summer 2020
 */

@Controller
public class HomeController {
	
	@Autowired
	PropertiesConfiguration springBootConfiguration;

	
	@GetMapping("/login")
	public String loginUrl() {
		return "/login";
	}	
	
	
	@PostMapping("/loginAction")
   	public String loginAction(HttpServletRequest httpServletRequest, HttpServletResponse response, 
   			Model model){

		String id = springBootConfiguration.getMark59metricsid();
		String passwrd  = springBootConfiguration.getMark59metricspasswrd();
		
        String username = httpServletRequest.getParameter("username");
        String password = httpServletRequest.getParameter("password");		
 	
        HttpSession session = httpServletRequest.getSession(true);

        if (id.equals(username) && passwrd.equals(password)) {
            session.setAttribute("authState", "authOK");
            session.setMaxInactiveInterval(30*60);    //setting session to expire in 30 minutes
        	return "redirect:/serverProfileList";
        	
        } else {
        	session.setAttribute("authState", "invalid");
    		Map<String, Object> map = new HashMap<>();
    		map.put("reqErr", "<font color=red>Bad credentials.</font>");
      		model.addAttribute("map", map);	
        	return "login";
        }
	}
  
   
	@GetMapping("/logout")
    public String logoutPage (HttpServletRequest httpServletRequest, HttpServletResponse response) {
	     HttpSession existingSession = httpServletRequest.getSession(false);
	     if (existingSession != null) {
	    	 existingSession.setAttribute("authState", "invalid");
	     }
        return "login";
    }

   
	@GetMapping(value = "/")
	public String root(Model model, HttpServletRequest request) {
        return "login";
	}	
	
	
	@GetMapping(value = "/welcome")
	public String overview(Model model, HttpServletRequest request) {
        return "welcome";
	}		
	
}
