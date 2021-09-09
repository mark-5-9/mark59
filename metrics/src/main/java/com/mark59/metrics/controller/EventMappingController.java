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
import java.util.Arrays;
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
import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.EventMapping;
import com.mark59.metrics.data.eventMapping.dao.EventMappingDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@Controller
public class EventMappingController {
	
	@Autowired
	EventMappingDAO eventMappingDAO; 

	/**
	 * Note that for the selectors 'Metric Source' and 'Tool' simply wipe each other out (i.e. the are not additive ) 
	 */
	@RequestMapping("/eventMappingList")
	public ModelAndView eventMappingList(@RequestParam(required=false) String reqPerformanceTool, @RequestParam(required=false) String reqMetricSource) {
//		System.out.println("eventMappingList reqPerformanceTool=" + reqPerformanceTool + ",reqMetricSource=" + reqMetricSource  );

		// note 'Metric Source' has precedence  and will wipe out any 'tool' selector, as at this point the Metric Source value is unique and determines the tool   
		List<EventMapping> eventMappingList = new ArrayList<EventMapping>(); 

		if (StringUtils.isEmpty(reqPerformanceTool) && StringUtils.isEmpty(reqMetricSource) ){
			eventMappingList = eventMappingDAO.findEventMappings();
			
		} else if (!StringUtils.isEmpty(reqMetricSource)){
			eventMappingList = eventMappingDAO.findEventMappings("METRIC_SOURCE", reqMetricSource);	
			reqPerformanceTool = "";
			
		} else if ( !StringUtils.isEmpty(reqPerformanceTool)){
			eventMappingList = eventMappingDAO.findEventMappings("PERFORMANCE_TOOL", reqPerformanceTool);	
			reqMetricSource = "";
		}
			
		List<String>metricSources    = populateMetricSourceDropdown();
		metricSources.add(0, "");			
		
		List<String>performanceTools = populatePerformanceToolsDropdown();		
		performanceTools.add(0, "");
		
		Map<String, Object> parmsMap = new HashMap<String, Object>(); 
		parmsMap.put("eventMappingList",eventMappingList);
		parmsMap.put("metricSources",metricSources);
		parmsMap.put("reqMetricSource",reqMetricSource);
		parmsMap.put("performanceTools",performanceTools);			
		parmsMap.put("reqPerformanceTool",reqPerformanceTool);				
		return new ModelAndView("eventMappingList", "parmsMap", parmsMap);
	}

	
	@RequestMapping("/registerEventMapping")
	public ModelAndView registerEventMapping(@RequestParam(required=false) String reqMetricSource, @RequestParam(required=false) String reqErr, @ModelAttribute EventMapping eventMapping) { 
		Map<String, Object> map = createMapOfDropdowns();
		eventMapping.setMetricSource(reqMetricSource);
		map.put("eventMapping",eventMapping);		
		map.put("reqMetricSource",reqMetricSource);
		return new ModelAndView("registerEventMapping", "map", map);
	}
	

	@RequestMapping("/insertEventMapping")
	public ModelAndView insertData( @RequestParam(required=false) String reqMetricSource, @RequestParam(required=false) String reqErr,  @ModelAttribute EventMapping eventMapping) {
		EventMapping existingEventMapping = new EventMapping();
		if (eventMapping != null){
			existingEventMapping = eventMappingDAO.getEventMapping(eventMapping.getMetricSource(), eventMapping.getMatchWhenLike());
		}	
		
		if (existingEventMapping == null ){  //not trying to add something already there, so go ahead..
			eventMapping.setPerformanceTool(determinePerformanceTool(eventMapping.getMetricSource()));
			eventMappingDAO.insertData(eventMapping);

			Map<String, Object> parmsMap = new HashMap<String, Object>(); 
			
			List<EventMapping> eventMappingList = new ArrayList<EventMapping>(); 
			eventMappingList = eventMappingDAO.findEventMappings("METRIC_SOURCE", eventMapping.getMetricSource());	
			List<String>metricSources    = populateMetricSourceDropdown();
			metricSources.add(0, "");			
			List<String>performanceTools = populatePerformanceToolsDropdown();		
			performanceTools.add(0, "");
			
			parmsMap.put("eventMappingList",eventMappingList);
			parmsMap.put("metricSources",metricSources);
			parmsMap.put("reqMetricSource",eventMapping.getMetricSource());
			parmsMap.put("performanceTools",performanceTools);			
			parmsMap.put("reqPerformanceTool","");				
			
			return new ModelAndView("eventMappingList", "parmsMap", parmsMap);			
			
		} else {
			Map<String, Object> map = createMapOfDropdowns();			
			map.put("eventMapping",existingEventMapping);		
			map.put("reqMetricSource",reqMetricSource);
			map.put("reqErr","Oh, a mapping for a source of " + existingEventMapping.getMetricSource() + " which matches to  " + existingEventMapping.getMatchWhenLike() + " AlreadyExists");			
			return new ModelAndView("registerEventMapping", "map", map);
		}
	}
	
	
	@RequestMapping("/copyEventMapping")
	public String copyEventMapping(@RequestParam String txnType ,@RequestParam String metricSource, @RequestParam String matchWhenLike, @RequestParam(required=false) String reqMetricSource,  
			@ModelAttribute EventMapping eventMapping, Model model) {
		eventMapping = eventMappingDAO.getEventMapping(metricSource, matchWhenLike); 
		model.addAttribute("eventMapping", eventMapping);
		
		Map<String, Object> map = createMapOfDropdowns();
		map.put("reqMetricSource",reqMetricSource);
		map.put("eventMapping",eventMapping);
		model.addAttribute("map", map);	
		return "copyEventMapping";
	}
	
	
	@RequestMapping("/editEventMapping")
	public String editEventMapping(@RequestParam String txnType ,@RequestParam String metricSource, @RequestParam String matchWhenLike, @RequestParam(required=false) String reqMetricSource,  
			@ModelAttribute EventMapping eventMapping, Model model) {
		eventMapping = eventMappingDAO.getEventMapping(metricSource, matchWhenLike); 
		model.addAttribute("eventMapping", eventMapping);
		
		Map<String, Object> map = createMapOfDropdowns();  
		map.put("eventMapping",eventMapping);	
		map.put("reqMetricSource",reqMetricSource);
		model.addAttribute("map", map);	
		return "editEventMapping";
	}

	
	@RequestMapping("/updateEventMapping")
	public String updateEventMapping(@RequestParam(required=false) String reqMetricSource, @ModelAttribute EventMapping eventMapping) {
		eventMapping.setPerformanceTool(determinePerformanceTool(eventMapping.getMetricSource()));
		eventMappingDAO.updateData(eventMapping);
		return "redirect:/eventMappingList?reqMetricSource=" + reqMetricSource;
	}



	@RequestMapping("/deleteEventMapping")
	public String deleteEventMapping(@RequestParam String txnType ,@RequestParam String metricSource, @RequestParam String matchWhenLike, @RequestParam(required=false) String reqMetricSource) {
		eventMappingDAO.deleteData(txnType, metricSource, matchWhenLike);
		return "redirect:/eventMappingList?reqMetricSource=" + reqMetricSource;
	}
	

	private Map<String, Object> createMapOfDropdowns() {
		Map<String, Object> map = new HashMap<String, Object>(); 
		List<String>metricTypes		 = Mark59Constants.DatabaseTxnTypes.listOfMetricDatabaseTxnTypes();	
		List<String>performanceTools = populatePerformanceToolsDropdown();		
		List<String>metricSources	 = populateMetricSourceDropdown();
		map.put("metricTypes",metricTypes);		
		map.put("performanceTools",performanceTools);				
		map.put("metricSources",metricSources);
		map.put("isPercentageYesNo",populateYesNoDropdown());
		map.put("isInvertedPercentageYesNo",populateYesNoDropdown());
		return map;
	}
	
	
	
	private List<String> populatePerformanceToolsDropdown() {
		List<String> performanceToolsList =  new ArrayList<String>(Arrays.asList(AppConstantsMetrics.JMETER,
																		     	 AppConstantsMetrics.LOADRUNNER,
																		     	 AppConstantsMetrics.GATLING));
		return performanceToolsList;
	}	
	
	
	private List<String> populateMetricSourceDropdown() {
		List<String> metricSourceList = new ArrayList<String>(Arrays.asList(AppConstantsMetrics.METRIC_SOURCE_JMETER_CPU,
																			AppConstantsMetrics.METRIC_SOURCE_JMETER_MEMORY,
																			AppConstantsMetrics.METRIC_SOURCE_JMETER_DATAPOINT,
																			AppConstantsMetrics.METRIC_SOURCE_JMETER_TRANSACTION,																		  
																			AppConstantsMetrics.METRIC_SOURCE_LOADRUNNER_DATAPOINT_METER, 
																			AppConstantsMetrics.METRIC_SOURCE_LOADRUNNER_MONITOR_METER,
																			AppConstantsMetrics.METRIC_SOURCE_GATLING_TRANSACTION ));
		return metricSourceList;
	}	
	
	
	private List<String> populateYesNoDropdown( ) {
		List<String> yesNo =  new ArrayList<String>();
		yesNo.add("N");
		yesNo.add("Y");
		return yesNo;
	}		
	
	
	private String determinePerformanceTool(String metricSource) {
		String tool = AppConstantsMetrics.JMETER;
		if (metricSource.startsWith(AppConstantsMetrics.LOADRUNNER)){
			tool = AppConstantsMetrics.LOADRUNNER;
		} else if (metricSource.startsWith(AppConstantsMetrics.GATLING)){
			tool = AppConstantsMetrics.GATLING;
		}	
		return tool;
	}

}
