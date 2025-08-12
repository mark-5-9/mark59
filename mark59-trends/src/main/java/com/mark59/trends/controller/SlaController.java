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


import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.trends.application.AppConstantsTrends;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.beans.Sla;
import com.mark59.trends.data.run.dao.RunDAO;
import com.mark59.trends.data.sla.dao.SlaDAO;
import com.mark59.trends.form.BulkApplicationPassCountsForm;
import com.mark59.trends.form.CopyApplicationForm;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@Controller
public class SlaController {
	
	@Autowired
	SlaDAO slaDao; 

	@Autowired
	RunDAO runDAO; 
	
	
	@GetMapping("/slaList")
	public ModelAndView getSlaList(@RequestParam(required=false) String reqApp) {
		List<String> applicationList = populateSlaApplicationDropdown();
		if (StringUtils.isBlank(reqApp)  && applicationList.size() > 0  ){
			// when no application request parameter has been sent, take the first application 
			reqApp = applicationList.get(1);
		}		
		
		List<Sla> slaList;
		if (StringUtils.isBlank(reqApp) ){
			slaList = slaDao.getSlaList();
		} else {
			slaList = slaDao.getSlaList(reqApp);			
		}
		
		Map<String, Object> map = new HashMap<>();
		map.put("slaList",slaList);
		map.put("reqApp",reqApp);
		map.put("applications",applicationList);
		return new ModelAndView("slaList", "map", map);
	}
	
	
	@GetMapping("/viewSlaList")
	public ModelAndView viewSlaList(@RequestParam(required=false) String reqApp) {
		List<Sla> slaList;
		if (reqApp == null){
			slaList = slaDao.getSlaList();				
		} else {
			slaList = slaDao.getSlaList(reqApp);			
		}
		return new ModelAndView("viewSlaList", "slaList", slaList);
	}	
	
	
	@GetMapping("/registerSla")
	public ModelAndView registerSla(@RequestParam(required=false) String reqApp, @RequestParam(required=false) String reqErr, @ModelAttribute Sla sla, Model model) {
		Map<String, Object> map = createMapOfDropdowns();
		map.put("sla",sla);		
		map.put("reqApp", reqApp);	
		return new ModelAndView("registerSla", "map", map);
	}
	

	@PostMapping("/insertSla")
	public ModelAndView insertData(@RequestParam(required=false) String reqApp, @RequestParam(required=false) String reqErr,  @ModelAttribute Sla sla) {
		Sla	existingSla = new Sla();
		if (sla != null)
			existingSla = slaDao.getSla(sla.getApplication(), sla.getTxnId(), sla.getIsCdpTxn()); 
		
		if (existingSla == null ){  //not trying to add something already there, so go ahead..
			List<String> applicationList = populateSlaApplicationDropdown();
			slaDao.insertData(sla);
			if (StringUtils.isBlank(reqApp)  && applicationList.size() > 0  ){
				// when no application request parameter has been sent, take the first application 
				reqApp = applicationList.get(1);
			}		
			
			List<Sla> slaList;
			if (StringUtils.isBlank(reqApp) ){
				slaList = slaDao.getSlaList();
			} else {
				slaList = slaDao.getSlaList(reqApp);			
			}
			
			Map<String, Object> map = new HashMap<>();
			map.put("slaList",slaList);
			map.put("reqApp",reqApp);
			map.put("applications",applicationList);
			return new ModelAndView("slaList", "map", map);

		} else {
			Map<String, Object> map = createMapOfDropdowns();
			map.put("sla",sla);		
			map.put("reqApp", reqApp);	
			map.put("reqErr", "Oh, a sla for " + Objects.requireNonNull(sla).getTxnId() + " (CDP : "+sla.getIsCdpTxn()+") AlreadyExists");
			return new ModelAndView("registerSla", "map", map);			
		}
	}
	
	
	@GetMapping("/copySla")
	public String copySla(@RequestParam String reqTxnId, @RequestParam String reqIsCdpTxn, @RequestParam(required=false) String reqApp,  @ModelAttribute Sla sla, Model model) {
		sla = slaDao.getSla(reqApp, reqTxnId, reqIsCdpTxn);
		sla.setSlaOriginalTxnId(sla.getTxnId());
		model.addAttribute("sla", sla);

		Map<String, Object> map = createMapOfDropdowns();				
		map.put("sla",sla);		
		map.put("reqApp",reqApp);
		model.addAttribute("map", map);		
		return "copySla";
	}
	
	
	@GetMapping("/editSla")
	public String editSla(@RequestParam String reqTxnId, @RequestParam String reqIsCdpTxn, @RequestParam(required=false) String reqApp, @ModelAttribute Sla sla, Model model) {
		sla = slaDao.getSla(reqApp, reqTxnId, reqIsCdpTxn);
		sla.setSlaOriginalTxnId(sla.getTxnId());
		model.addAttribute("sla", sla);

		Map<String, Object> map = createMapOfDropdowns();				
		map.put("sla",sla);		
		map.put("reqApp",reqApp);
		model.addAttribute("map", map);		
		return "editSla"; 
	}
	
	
	@PostMapping("/updateSla")
	public String updateSla(@RequestParam(required=false) String reqApp, @ModelAttribute Sla sla) {
		slaDao.updateData(sla);
		return "redirect:/slaList?reqApp=" + reqApp  ;
	}


	@GetMapping("/deleteSla")
	public String deleteSla(@RequestParam String reqTxnId, @RequestParam String reqIsCdpTxn, @RequestParam(required=false) String reqApp) {
		slaDao.deleteData(reqApp, reqTxnId, reqIsCdpTxn);
		return "redirect:/slaList?reqApp=" + reqApp;
	}

	
	@GetMapping("/copyApplicationSla") 
	public Object copyApplicationSlaGet(@RequestParam(required=false) String reqApp,  @ModelAttribute CopyApplicationForm copyApplicationForm ) {
		return copyApplicationSla(reqApp, copyApplicationForm);	
	}

	@PostMapping("/copyApplicationSla") 
	public Object copyApplicationSlaPost(@RequestParam(required=false) String reqApp,  @ModelAttribute CopyApplicationForm copyApplicationForm ) {
		return copyApplicationSla(reqApp, copyApplicationForm);	
	}

	private Object copyApplicationSla(String reqApp, CopyApplicationForm copyApplicationForm) {
		copyApplicationForm.setReqApp(reqApp);
		copyApplicationForm.setValidForm("N");

		if ( StringUtils.isNotEmpty( copyApplicationForm.getReqToApp())) { 
			copyApplicationForm.setValidForm("Y");
			//do the copy
			List<Sla> slaList = slaDao.getSlaList(reqApp);
			for (Sla origSla : slaList) {
				Sla copySla = new Sla(origSla);
				copySla.setApplication(copyApplicationForm.getReqToApp());
				slaDao.updateData(copySla);				
			}
					
			return "redirect:/slaList?reqApp=" + copyApplicationForm.getReqToApp();
		} else {
			return new ModelAndView("copyApplicationSla", "copyApplicationForm" , copyApplicationForm  );
		}
	}

	
	@PostMapping("/updateApplicationSla")	
	public String updateApplicationSla(@RequestParam(required=false) String reqApp, @ModelAttribute CopyApplicationForm copyApplicationForm) {
//		System.out.println("@ updateApplicationSla : reqApp=" + copyApplicationForm.getReqApp() + ", ReqToApp=" + copyApplicationForm.getReqToApp()  );
		return "redirect:/slaList?reqApp=" + reqApp  ;
	}	
		
	
	@GetMapping("/deleteApplicationSla")
	public String deleteApplicationSla(@RequestParam String reqApp) {
		slaDao.deleteAllSlasForApplication(reqApp);
		return "redirect:/slaList?reqApp=";
	}
	
	
	@GetMapping("/bulkApplicationPassCounts")
	public ModelAndView bulkApplicationPassCounts(@RequestParam(required=false) String reqErr, @RequestParam(required=false) String reqApp,  
			@ModelAttribute BulkApplicationPassCountsForm bulkApplicationPassCountsForm) { 
		
		bulkApplicationPassCountsForm.setApplication(reqApp);
		bulkApplicationPassCountsForm.setSlaRefUrl(referenceOfLastBaselineRun(reqApp));
		
		List<String> applicationList = populateApplicationsWithBaselinesDropdown();
		List<String> isIncludeCdpTxnsYesNo = populateIsIncludeCdpTxnsYesNoDropdown();
		List<String> isTxnIgnoredYesNo = populateIsTxnIgnoredYesNoDropdown();
		List<String> isActiveYesNo   = populateIsActiveYesNoDropdown();		
		List<String> applyRefUrlOptions = populateApplyRefUrlDropdown();	
		
		Map<String, Object> map = new HashMap<>();
		map.put("applications",applicationList);
		map.put("isIncludeCdpTxnsYesNo", isIncludeCdpTxnsYesNo);
		map.put("isTxnIgnoredYesNo", isTxnIgnoredYesNo);
		map.put("isActiveYesNo",isActiveYesNo);			
		map.put("applyRefUrlOptions",applyRefUrlOptions);		
		return new ModelAndView("bulkApplicationPassCounts", "map", map);  			
	}

	
	@GetMapping("/asyncReloadSlaBulkLoadPage" )	
	public @ResponseBody String asyncReloadSlaBulkLoadPage(@RequestParam(required=false) String reqApp ) {  
		return  referenceOfLastBaselineRun(reqApp);
	}
	
	
	@PostMapping("/insertOrUpdateApplicationPassCounts")
	public String insertOrUdateApplicationPassCounts(@RequestParam(required=false) String reqErr, 
			@ModelAttribute BulkApplicationPassCountsForm bulkApplicationPassCountsForm) {
//		System.out.println("insertOrUdateApplicationPassCounts bulkApplicationPassCountsForm = " + bulkApplicationPassCountsForm );
		int rowcont = slaDao.bulkInsertOrUpdateApplication(bulkApplicationPassCountsForm);
		if (rowcont == 0 ){  //nothing was found to upload..
			return "redirect:/bulkApplicationPassCounts?reqErr=No Data.  Check that application '" + bulkApplicationPassCountsForm.getApplication() + "' has had a baseline set..."
					+ "&reqApp=" + bulkApplicationPassCountsForm.getApplication();
		} else { // OK
			return "redirect:/slaList?reqApp=" + bulkApplicationPassCountsForm.getApplication();	
		}		
	}
	
	
	private List<String> populateApplicationsWithBaselinesDropdown() {
		List<String> applicationList = runDAO.findApplicationsWithBaselines();
		applicationList.add(0, "");
		return applicationList;
	}		
	
	
	private String referenceOfLastBaselineRun(String reqApp) {
		String reference = "";
		if (StringUtils.isNotBlank(reqApp)){
			Run lastBaseLineRun = runDAO.findLastBaselineRun(reqApp);
			if (lastBaseLineRun != null) {
				reference = lastBaseLineRun.getRunReference();   
			}
		}	
		return reference;
	}		

	
	private List<String> populateApplyRefUrlDropdown( ) {
		List<String> applyRefUrlOptions = new ArrayList<>();
		applyRefUrlOptions.add(AppConstantsTrends.APPLY_TO_NEW_SLAS_ONLY);
		applyRefUrlOptions.add(AppConstantsTrends.APPLY_TO_ALL_SLAS);
		return applyRefUrlOptions;
	}		
	
	
	private Map<String, Object> createMapOfDropdowns() {
		Map<String, Object> map = new HashMap<>();
		List<String> applicationList   = populateSlaApplicationDropdown();		
		List<String> IsCdpTxnYesNo     = populateIsCdpTxnYesNoDropdown();	
		List<String> isTxnIgnoredYesNo = populateIsTxnIgnoredYesNoDropdown();	
		List<String> isActiveYesNo   = populateIsActiveYesNoDropdown();			
		map.put("applications",applicationList);		
		map.put("isTxnIgnoredYesNo", isTxnIgnoredYesNo);	
		map.put("IsCdpTxnYesNo",IsCdpTxnYesNo);	
		map.put("isActiveYesNo",isActiveYesNo);			
		return map;
	}	
	
	
	private List<String> populateSlaApplicationDropdown() {
		List<String> applicationList = slaDao.findApplications();
		applicationList.add(0, "");
		return applicationList;
	}		
	
	
	private List<String> populateIsIncludeCdpTxnsYesNoDropdown( ) {
		List<String> isIncludeCdpTxnsYesNo = new ArrayList<>();
		isIncludeCdpTxnsYesNo.add("N");
		isIncludeCdpTxnsYesNo.add("Y");
		return isIncludeCdpTxnsYesNo;
	}	

	
	private List<String> populateIsTxnIgnoredYesNoDropdown( ) {
		List<String> isTxnIgnoredYesNo = new ArrayList<>();
		isTxnIgnoredYesNo.add("N");
		isTxnIgnoredYesNo.add("Y");
		return isTxnIgnoredYesNo;
	}		
	
	
	private List<String> populateIsCdpTxnYesNoDropdown() {
		List<String> isCdpTxnYesNo = new ArrayList<>();
		isCdpTxnYesNo.add("N");
		isCdpTxnYesNo.add("Y");
		return isCdpTxnYesNo;
	}
		

	private List<String> populateIsActiveYesNoDropdown( ) {
		List<String> isActiveYesNo = new ArrayList<>();
		isActiveYesNo.add("Y");
		isActiveYesNo.add("N");
		return isActiveYesNo;
	}		

}
