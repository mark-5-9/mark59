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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.trends.data.beans.GraphMapping;
import com.mark59.trends.data.graphMapping.dao.GraphMappingDAO;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@Controller
public class GraphMappingController {
	
	@Autowired
	GraphMappingDAO  graphMappingDAO; 	
	

	@RequestMapping("/graphMappingList")
	public ModelAndView runsList(@RequestParam(required=false) String graph) {
		List<GraphMapping> graphMappingList = graphMappingDAO.getGraphMappings() ;				
		return new ModelAndView("graphMappingList", "graphMappingList", graphMappingList);
	}	
	
	
	@RequestMapping("/registerGraphMapping")
	public ModelAndView registerSla(@RequestParam(required=false) String reqErr, @ModelAttribute GraphMapping graphMapping) { 
		List<String>transactionTypes = Mark59Constants.DatabaseTxnTypes.listOfDatabaseTxnTypes();  
		Map<String, Object> map = new HashMap<>();
		map.put("transactionTypes",transactionTypes);			
		return new ModelAndView("registerGraphMapping",  "map", map);  	
	}	
		
	
	@RequestMapping("/insertGraphMapping")
	public String insertData(@RequestParam(required=false) String reqErr,  @ModelAttribute GraphMapping graphMapping) {
		GraphMapping existingGraphMapping = new GraphMapping();
		if (graphMapping != null)
			existingGraphMapping = graphMappingDAO.findGraphMapping(graphMapping.getGraph() ); 
		
			if (existingGraphMapping == null ){  //not trying to add something already there, so go ahead..
				graphMappingDAO.inserttGraphMapping(graphMapping);
				return "redirect:/graphMappingList";
			} else {
				return "redirect:/registerGraphMapping?&reqErr=Oh, graph " + Objects.requireNonNull(graphMapping).getGraph()  + " AlreadyExists";
			}
	}	

	
	@RequestMapping("/editGraphMapping")
	public ModelAndView editGraphMapping(@RequestParam String graph,  @ModelAttribute GraphMapping graphMapping) {
		System.out.println("GraphMappingController:editGraphMapping : graph=" + graph  );		
		graphMapping = graphMappingDAO.findGraphMapping(graph);
		Map<String, Object> map = new HashMap<>();
		List<String>transactionTypes = Mark59Constants.DatabaseTxnTypes.listOfDatabaseTxnTypes();  
		map.put("graphMapping",graphMapping);
		map.put("transactionTypes",transactionTypes);
		return new ModelAndView("editGraphMapping", "map", map);
	}

	
	@RequestMapping("/updateGraphMapping")
	public String updateGraphMapping( @ModelAttribute GraphMapping graphMapping) {
		graphMappingDAO.updateGraphMapping(graphMapping);
		return "redirect:/graphMappingList";
	}

	
	@RequestMapping("/deleteGraphMapping")
	public String deleteGraphMapping(@RequestParam String graph) {
		System.out.println("GraphMappingController:deleteGraphMapping : graph=" + graph  );	
		graphMappingDAO.deleteGraphMapping(graph);
		return "redirect:/graphMappingList";
	}

}
