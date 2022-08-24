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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.metrics.data.beans.CommandResponseParser;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAO;


/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */

@Controller
public class CommandResponseParserController {
	
	@Autowired
	CommandResponseParsersDAO commandResponseParsersDAO; 

	@RequestMapping("/registerCommandResponseParser")
	public ModelAndView registerCommandResponseParser(@RequestParam(required=false) String reqMetricTxnType, @RequestParam(required=false) String reqErr, 
			@ModelAttribute CommandResponseParser commandResponseParser) { 
		Map<String, Object> map = createMapOfDropdowns();
		commandResponseParser.setMetricTxnType(reqMetricTxnType);
		map.put("reqMetricTxnType",reqMetricTxnType);
		return new ModelAndView("registerCommandResponseParser", "map", map);
	}
	

	@RequestMapping("/insertCommandResponseParser")
	public ModelAndView insertCommandResponseParser( @RequestParam(required=false) String reqMetricTxnType, @RequestParam(required=false) String reqErr,  
			@ModelAttribute CommandResponseParser commandResponseParser) {

		Map<String, Object> map = createMapOfDropdowns();			
		map.put("reqMetricTxnType",reqMetricTxnType);

		if (StringUtils.isEmpty(commandResponseParser.getParserName())) {
			map.put("commandResponseParser", commandResponseParser);		
			map.put("reqErr", "Parser Name id is required");			
			return new ModelAndView("registerCommandResponseParser", "map", map);
		}

		CommandResponseParser existingCommandResponseParser = commandResponseParsersDAO.findCommandResponseParser(commandResponseParser.getParserName());
		
		if (existingCommandResponseParser == null ){  //not trying to add something already there, so go ahead..

			commandResponseParsersDAO.insertCommandResponseParser(commandResponseParser);
			map.put("commandResponseParser", commandResponseParser);	
			return new ModelAndView("viewCommandResponseParser", "map", map);			
			
		} else {
		
			map.put("serverProfileName",existingCommandResponseParser);		
			map.put("reqErr","Oh, a listing for parser name  " + existingCommandResponseParser.getParserName() + " AlreadyExists");			
			return new ModelAndView("registerCommandResponseParser", "map", map);
		}
	}
	

	@RequestMapping("/commandResponseParserList")
	public ModelAndView commandResponseParserList(@RequestParam(required=false) String reqMetricTxnType) {

		List<CommandResponseParser> commandResponseParserList;

		if (!StringUtils.isEmpty(reqMetricTxnType)){
			commandResponseParserList = commandResponseParsersDAO.findCommandResponseParsers("METRIC_TXN_TYPE", reqMetricTxnType);	
		} else {
			commandResponseParserList = commandResponseParsersDAO.findCommandResponseParsers();	
		}
		
		List<String>metricTxnTypes = Mark59Constants.JMeterFileDatatypes.listOfMetricJMeterFileDatatypes();
		metricTxnTypes.add(0, "");	
		
		Map<String, Object> parmsMap = new HashMap<>();
		parmsMap.put("commandResponseParserList", commandResponseParserList);
		parmsMap.put("metricTxnTypes",metricTxnTypes);	
		parmsMap.put("reqMetricTxnType", reqMetricTxnType);
		return new ModelAndView("commandResponseParserList", "parmsMap", parmsMap);
	}

	@RequestMapping("/viewCommandResponseParser")
	public String viewCommandResponseParser(@RequestParam String reqParserName, @RequestParam(required=false) String reqMetricTxnType, Model model) {
		CommandResponseParser commandResponseParser = commandResponseParsersDAO.findCommandResponseParser(reqParserName); 
		model.addAttribute("commandResponseParser", commandResponseParser);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqMetricTxnType", reqMetricTxnType);
		map.put("commandResponseParser", commandResponseParser);
		model.addAttribute("map", map);	
		return "viewCommandResponseParser";
	}
	
	
	@RequestMapping("/copyCommandResponseParser")
	public String copyCommandResponseParser(@RequestParam String reqParserName, @RequestParam(required=false) String reqMetricTxnType, Model model) {
		CommandResponseParser commandResponseParser = commandResponseParsersDAO.findCommandResponseParser(reqParserName); 
		model.addAttribute("commandResponseParser", commandResponseParser);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqMetricTxnType", reqMetricTxnType);
		map.put("commandResponseParser", commandResponseParser);
		model.addAttribute("map", map);	
		return "copyCommandResponseParser";
	}
	
	
	@RequestMapping("/editCommandResponseParser")
	public String editCommandResponseParser(@RequestParam String reqParserName, @RequestParam(required=false) String reqMetricTxnType, Model model) {
		CommandResponseParser commandResponseParser = commandResponseParsersDAO.findCommandResponseParser(reqParserName); 
		model.addAttribute("commandResponseParser", commandResponseParser);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqMetricTxnType", reqMetricTxnType);
		
		// System.out.println( "commandResponseParser=" + commandResponseParser );
		
		map.put("commandResponseParser", commandResponseParser);
		model.addAttribute("map", map);	
		return "editCommandResponseParser";
	}

	
	@RequestMapping("/updateCommandResponseParser")
	public ModelAndView updateCommandResponseParser(@RequestParam(required=false) String reqMetricTxnType, @ModelAttribute CommandResponseParser commandResponseParser) {
		commandResponseParsersDAO.updateCommandResponseParser(commandResponseParser);
		Map<String, Object> map = createMapOfDropdowns();			
		map.put("reqMetricTxnType",reqMetricTxnType);
		map.put("commandResponseParser", commandResponseParser);	
		return new ModelAndView("viewCommandResponseParser", "map", map);		
	}


	@RequestMapping("/deleteCommandResponseParser")
	public String deleteCommandResponseParser(@RequestParam String reqParserName, @RequestParam String reqMetricTxnType) {
		commandResponseParsersDAO.deleteCommandResponseParser(reqParserName);
		return "redirect:/commandResponseParserList?reqMetricTxnType=" + reqMetricTxnType;
	}
	

	private Map<String, Object> createMapOfDropdowns() {
		Map<String, Object> map = new HashMap<>();
		List<String>metricTxnTypes = Mark59Constants.JMeterFileDatatypes.listOfMetricJMeterFileDatatypes();
		map.put("metricTxnTypes",metricTxnTypes);
		return map;
	}
	

}
