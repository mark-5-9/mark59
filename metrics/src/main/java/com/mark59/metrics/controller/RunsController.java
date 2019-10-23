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

package com.mark59.metrics.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.run.dao.RunDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@Controller
public class RunsController {
	
	@Autowired
	RunDAO  runDAO; 	

	@RequestMapping("/runsList")
	public ModelAndView runsList(@RequestParam(required=false) String reqApp) {
		List<String> applicationList = populateApplicationDropdown();
		if (reqApp == null  && applicationList.size() > 1  ){
			// when no application request parameter has been sent, take the first application 
			reqApp = (String)applicationList.get(1);
		}
		List<Run> runsList = new ArrayList<Run>(); 
		runsList = runDAO.findRuns(reqApp) ;
		
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("runsList",runsList);
		map.put("reqApp",reqApp);		
		map.put("applications",applicationList);
		return new ModelAndView("runsList", "map", map);
	}	
		
	@RequestMapping("/editRun")
	public String editRun(@RequestParam String reqApp, @RequestParam String runTime, @ModelAttribute Run run, Model model) {

		run = runDAO.findRun(reqApp, runTime);
		model.addAttribute("run", run);
		
		List<String> baselineRunYesNo = populateBaselineActiveYesNoDropdown();	
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("baselineRunYesNo",baselineRunYesNo);
		map.put("reqApp",reqApp);			
		model.addAttribute("map", map);
		return "editRun"; 		
	}

	
	@RequestMapping("/updateRun")
	public ModelAndView updateRun(@ModelAttribute Run run) {
//		System.out.println("@ updateRun : app=" + run.getApplication() + ", time="     + run.getRunTime()  + ", file=" + run.getRunReference() 
//				                 + ",  period=" + run.getPeriod()      + ", duration=" + run.getDuration() + ", baseline=" + run.getBaselineRun()
//				                 + " ,comment=" + run.getComment()  );
		runDAO.updateRun(run);
		return new ModelAndView("redirect:/runsList?reqApp=" + run.getApplication() ) ;
	}

	
	@RequestMapping("/deleteRun")
	public String deleteRun(@RequestParam String reqApp, @RequestParam String runTime) {
		System.out.println("deleting run for application: " + reqApp  + ", runtime: " +  runTime);
		runDAO.deleteRun(reqApp, runTime ) ;	
		return "redirect:/runsList?reqApp=" + reqApp;
	}

	private List<String> populateApplicationDropdown() {
		List<String> applicationList = new ArrayList<String>();
		applicationList = runDAO.findApplications();
		applicationList.add(0, "");
		return applicationList;
	}		
	
	
	private List<String> populateBaselineActiveYesNoDropdown( ) {
		List<String> activeYesNo =  new ArrayList<String>();
		activeYesNo.add("Y");
		activeYesNo.add("N");
		return activeYesNo;
	}		
	
	
	
}
