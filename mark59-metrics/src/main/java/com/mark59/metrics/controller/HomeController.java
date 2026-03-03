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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.mark59.metrics.PropertiesConfiguration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Handles authentication and navigation for the Mark59 Metrics application.
 * Provides login, logout, and welcome page functionality with session-based authentication.
 *
 * @author Philip Webb
 * Written: Australian Summer 2020
 */

@Controller
public class HomeController {

	private static final int SESSION_TIMEOUT_SECONDS = 30 * 60; // 30 minutes

	@Autowired
	PropertiesConfiguration springBootConfiguration;


	@GetMapping("/login")
	public String loginUrl() {
		return "login";
	}


	@PostMapping("/loginAction")
	public String loginAction(HttpServletRequest httpServletRequest, Model model){

		String id = springBootConfiguration.getMark59metricsid();
		String password = springBootConfiguration.getMark59metricspasswrd();

		String username = httpServletRequest.getParameter("username");
		String inputPassword = httpServletRequest.getParameter("password");

		HttpSession session = httpServletRequest.getSession(true);

		if (id.equals(username) && password.equals(inputPassword)) {
			session.setAttribute("authState", "authOK");
			session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
			return "redirect:/serverProfileList";

		} else {
			session.setAttribute("authState", "invalid");
			Map<String, Object> map = new HashMap<>();
			map.put("reqErr", "<span style='color:red'>Bad credentials.</span>");
			model.addAttribute("map", map);
			return "login";
		}
	}


	@GetMapping("/logout")
	public String logoutPage(HttpServletRequest httpServletRequest) {
		HttpSession existingSession = httpServletRequest.getSession(false);
		if (existingSession != null) {
			existingSession.invalidate();
		}
		return "login";
	}


	@GetMapping(value = "/")
	public String root() {
		return "login";
	}


	@GetMapping(value = "/welcome")
	public String overview() {
		return "welcome";
	}

}
