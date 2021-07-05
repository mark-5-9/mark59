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
import com.mark59.metrics.data.beans.MetricSla;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAO;
import com.mark59.metrics.form.CopyApplicationForm;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@Controller
public class MetricSlaController {
	
	@Autowired
	MetricSlaDAO metricSlaDAO; 

	
	@RequestMapping("/metricSlaList")
	public ModelAndView getMetricSlaList(@RequestParam(required=false) String reqApp) {
		List<String> applicationList = populateApplicationDropdown();
		if (StringUtils.isBlank(reqApp)  && applicationList.size() > 1  ){
			// when no application request parameter has been sent, take the first application 
			reqApp = (String)applicationList.get(1);
		}		
		
		List<MetricSla> metricSlaList = new ArrayList<MetricSla>(); 
		if (StringUtils.isBlank(reqApp) ){
			metricSlaList = metricSlaDAO.getMetricSlaList();
		} else {
			metricSlaList = metricSlaDAO.getMetricSlaList(reqApp);			
		}

		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("metricSlaList",metricSlaList);
		map.put("reqApp",reqApp);
		map.put("applications",applicationList);
		return new ModelAndView("metricSlaList", "map", map);
	}
	
	
	
	@RequestMapping("/registerMetricSla")
	public String registerMetricSla(@RequestParam(required=false) String reqApp, @RequestParam(required=false) String reqErr, @ModelAttribute MetricSla metricSla, Model model) { 
		List<String> derivations = populateDerivationsDropdown();
		List<String>metricTypes = Mark59Constants.DatabaseTxnTypes.listOfMetricDatabaseTxnTypes();
		List<String> isActiveYesNo = populateIsActiveYesNoDropdown();
		
		model.addAttribute("derivations",derivations);		
		model.addAttribute("metricTypes",metricTypes);	
		model.addAttribute("isActiveYesNo", isActiveYesNo);		
		model.addAttribute("reqApp", reqApp);	
		return "registerMetricSla";
	}
	

	@RequestMapping("/insertMetricSla")
	public String insertData(@RequestParam(required=false) String reqApp, @RequestParam(required=false) String reqErr,  @ModelAttribute MetricSla metricSla) {
		MetricSla	existingMetricSla = new MetricSla();
		if (metricSla != null)
			existingMetricSla = metricSlaDAO.getMetricSla(metricSla.getApplication(),metricSla.getMetricName(), metricSla.getMetricTxnType(), metricSla.getValueDerivation());   
		
		if (existingMetricSla == null ){  //not trying to add something already there, so go ahead..
			metricSlaDAO.insertData(metricSla);
			return "redirect:/metricSlaList?reqApp=" + reqApp;
		} else {
			return "redirect:/registerMetricSla?reqApp=" + reqApp + "&reqErr=Oh, that metric for " + metricSla.getMetricName() + " AlreadyExists";
		}
	}
	
	
	@RequestMapping("/copyMetricSla")
	public String copyMetricSla(@RequestParam String metricName,@RequestParam String metricTxnType, @RequestParam String valueDerivation, @RequestParam(required=false) String reqApp,  
			@ModelAttribute MetricSla metricSla, Model model) {
		metricSla = metricSlaDAO.getMetricSla(reqApp, metricName, metricTxnType,valueDerivation);   
		metricSla.setOriginalMetricName(metricSla.getMetricName());
		model.addAttribute("metricSla", metricSla);
		
		List<String> derivations = populateDerivationsDropdown();
		List<String>metricTypes = Mark59Constants.DatabaseTxnTypes.listOfMetricDatabaseTxnTypes();
		List<String> isActiveYesNo = populateIsActiveYesNoDropdown();
		
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("derivations",derivations);
		map.put("metricTypes",metricTypes);	
		map.put("isActiveYesNo",isActiveYesNo);			
		map.put("reqApp",reqApp);
		map.put("metricName", metricName);	
		model.addAttribute("map", map);
		return "copyMetricSla";
	}
	
	
	@RequestMapping("/editMetricSla")
	public String editMetricSla(@RequestParam String metricName,@RequestParam String metricTxnType, @RequestParam String valueDerivation, @RequestParam(required=false) String reqApp,  
			@ModelAttribute MetricSla metricSla, Model model) {
		metricSla = metricSlaDAO.getMetricSla(reqApp, metricName, metricTxnType,valueDerivation);   
		metricSla.setOriginalMetricName(metricSla.getMetricName());
		model.addAttribute("metricSla", metricSla);
		
		List<String> derivations = populateDerivationsDropdown();
		List<String>metricTypes = Mark59Constants.DatabaseTxnTypes.listOfMetricDatabaseTxnTypes();
		List<String> isActiveYesNo = populateIsActiveYesNoDropdown();
		
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("metricSla",metricSla);
		map.put("derivations",derivations);
		map.put("metricTypes",metricTypes);	
		map.put("isActiveYesNo",isActiveYesNo);			
		map.put("reqApp",reqApp);
		map.put("metricName", metricName);	
		model.addAttribute("map", map);
		return "editMetricSla";
	}

	
	@RequestMapping("/copyApplicationMetricSla") 
	public Object copyApplicationMetricSla(@RequestParam(required=false) String reqApp,  @ModelAttribute CopyApplicationForm copyApplicationForm ) {
//		System.out.println("@ copyApplicationSla : reqApp=" + copyApplicationForm.getReqApp() +	", ReqToApp=" + copyApplicationForm.getReqToApp()  );

		copyApplicationForm.setReqApp(reqApp);
		copyApplicationForm.setValidForm("N");

		if ( StringUtils.isNotEmpty( copyApplicationForm.getReqToApp())) { 
			copyApplicationForm.setValidForm("Y");
			
			//do the copy
			List<MetricSla> slaList = new ArrayList<MetricSla>();
			slaList = metricSlaDAO.getMetricSlaList(reqApp);
			for (MetricSla origMetricSla : slaList) {
				MetricSla copyMetricSla = new MetricSla(origMetricSla);
				copyMetricSla.setApplication(copyApplicationForm.getReqToApp());
				metricSlaDAO.updateData(copyMetricSla);				
			}
					
			return "redirect:/metricSlaList?reqApp=" + copyApplicationForm.getReqToApp();
		} else {
			return new ModelAndView("copyApplicationMetricSla", "copyApplicationForm" , copyApplicationForm  );
		}	
	}

	@RequestMapping("/updateApplicationMetricSla")	
	public String updateApplicationMetricSla(@RequestParam(required=false) String reqApp, @ModelAttribute CopyApplicationForm copyApplicationForm) {
//		System.out.println("@ updateApplicationMetricSla : reqApp=" + copyApplicationForm.getReqApp() + ", ReqToApp=" + copyApplicationForm.getReqToApp()  );
		return "redirect:/metricSlaList?reqApp=" + reqApp  ;
	}	
	
	
		
	@RequestMapping("/updateMetricSla")
	public String updateMetricSla(@RequestParam(required=false) String reqApp, @ModelAttribute MetricSla metricSla) {
//		System.out.println("@ updateSla : reqApp=" + reqApp + ", app=" + sla.getApplication() + ",  origTxn=" + sla.getSlaOriginalTxnId() + ", txnId=" + sla.getTxnId() + " ,90th=" + sla.getSla90thResponse()   );
		metricSlaDAO.updateData(metricSla);
		return "redirect:/metricSlaList?reqApp=" + reqApp  ;
	}

	

	@RequestMapping("/deleteApplicationMetricSla")
	public String deleteApplicationSla(@RequestParam String reqApp) {
//		System.out.println("deleting all slas for application " + reqApp );
		metricSlaDAO.deleteAllSlasForApplication(reqApp);
		return "redirect:/metricSlaList?reqApp=";
	}
	

	@RequestMapping("/deleteMetricSla")
	public String deleteSla(@RequestParam String metricName,@RequestParam String metricTxnType, @RequestParam String valueDerivation, @RequestParam(required=false) String reqApp) {
//		System.out.println("deleting sla for  app = " + reqApp + ", txnId = " + txnId);
		metricSlaDAO.deleteData(reqApp, metricName, metricTxnType,valueDerivation);
		return "redirect:/metricSlaList?reqApp=" + reqApp;
	}
	

	private List<String> populateApplicationDropdown() {
		List<String> applicationList = new ArrayList<String>();
		applicationList = metricSlaDAO.findApplications();
		applicationList.add(0, "");
		return applicationList;
	}		
	

	private List<String> populateDerivationsDropdown() {
		List<String> derivationsList = AppConstantsMetrics.DIRECT_VALUE_DERIVATONS;
		return derivationsList;
	}	
	
	private List<String> populateIsActiveYesNoDropdown( ) {
		List<String> isActiveYesNo =  new ArrayList<String>();
		isActiveYesNo.add("Y");
		isActiveYesNo.add("N");
		return isActiveYesNo;
	}		
	
	
	
}
