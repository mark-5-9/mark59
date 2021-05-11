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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.metrics.application.AppConstantsMetrics;
import com.mark59.metrics.data.beans.Run;
import com.mark59.metrics.data.beans.Sla;
import com.mark59.metrics.data.run.dao.RunDAO;
import com.mark59.metrics.data.sla.dao.SlaDAO;
import com.mark59.metrics.form.BulkApplicationPassCountsForm;
import com.mark59.metrics.form.CopyApplicationForm;

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
	
	
	@RequestMapping("/slaList")
	public ModelAndView getSlaList(@RequestParam(required=false) String reqApp) {
		List<String> applicationList = populateSlaApplicationDropdown();
		if (reqApp == null  && applicationList.size() > 1  ){
			// when no application request parameter has been sent, take the first application 
			reqApp = (String)applicationList.get(1);
		}
		
		List<Sla> slaList = new ArrayList<Sla>(); 
		if (StringUtils.isBlank(reqApp) ){
			slaList = slaDao.getSlaList();
		} else {
			slaList = slaDao.getSlaList(reqApp);			
		}
		
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("slaList",slaList);
		map.put("reqApp",reqApp);
		map.put("applications",applicationList);
		return new ModelAndView("slaList", "map", map);
	}

	
	
	@RequestMapping("/viewSlaList")
	public ModelAndView viewSlaList(@RequestParam(required=false) String reqApp) {
		List<Sla> slaList = new ArrayList<Sla>(); 
		if (reqApp == null){
			slaList = slaDao.getSlaList();				
		} else {
			slaList = slaDao.getSlaList(reqApp);			
		}
		return new ModelAndView("viewSlaList", "slaList", slaList);
	}	
	
	
	@RequestMapping("/registerSla")
	public String registerSla(@RequestParam(required=false) String reqApp, @RequestParam(required=false) String reqErr, @ModelAttribute Sla sla, Model model) {  
		//Map<String, Object> map = new HashMap<String, Object>(); 
		List<String> isTxnIgnoredYesNo = populateIsTxnIgnoredYesNoDropdown();	
		model.addAttribute("isTxnIgnoredYesNo", isTxnIgnoredYesNo);		
		model.addAttribute("reqApp", reqApp);		
		return "registerSla";  	
	}
	

	@RequestMapping("/insertSla")
	public String insertData(@RequestParam(required=false) String reqApp, @RequestParam(required=false) String reqErr,  @ModelAttribute Sla sla) {
		Sla	existingSla = new Sla();
		if (sla != null)
			existingSla = slaDao.getSla(sla.getApplication(), sla.getTxnId()); 
		
		if (existingSla == null ){  //not trying to add something already there, so go ahead..
			slaDao.insertData(sla);
			return "redirect:/slaList?reqApp=" + reqApp;
		} else {
			return "redirect:/registerSla?reqApp=" + reqApp + "&reqErr=Oh, transaction " + sla.getTxnId() + " AlreadyExists";
		}
	}
	
	
	@RequestMapping("/bulkApplicationPassCounts")
	public ModelAndView bulkApplicationPassCounts(@RequestParam(required=false) String reqErr, @RequestParam(required=false) String reqApp,  
			@ModelAttribute BulkApplicationPassCountsForm bulkApplicationPassCountsForm) { 
		
		bulkApplicationPassCountsForm.setApplication(reqApp);
		bulkApplicationPassCountsForm.setSlaRefUrl(referenceOfLastBaselineRun(reqApp));
		
		List<String> applicationList = populateApplicationsWithBaselinesDropdown();
		List<String> applyRefUrlOptions = populateApplyRefUrlDropdown();	

		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("applications",applicationList);		
		map.put("applyRefUrlOptions",applyRefUrlOptions);		
		return new ModelAndView("bulkApplicationPassCounts", "map", map);  			
	}

	
	@RequestMapping("/asyncReloadSlaBulkLoadPage" )	
	public @ResponseBody String asyncReloadSlaBulkLoadPage(@RequestParam(required=false) String reqApp ) {  
		return  referenceOfLastBaselineRun(reqApp);
	}
	
	
	@RequestMapping("/insertOrUpdateApplicationPassCounts")
	public String insertOrUdateApplicationPassCounts(@RequestParam(required=false) String reqErr, 
			@ModelAttribute BulkApplicationPassCountsForm bulkApplicationPassCountsForm) {
	
		Sla slaKeywithDefaultValues = new Sla();
		slaKeywithDefaultValues.setApplication(bulkApplicationPassCountsForm.getApplication() );
		slaKeywithDefaultValues.setIsTxnIgnored(bulkApplicationPassCountsForm.getIsTxnIgnored()  );		
		slaKeywithDefaultValues.setSla90thResponse(bulkApplicationPassCountsForm.getSla90thResponse()  );
		slaKeywithDefaultValues.setSla95thResponse(bulkApplicationPassCountsForm.getSla95thResponse()  );
		slaKeywithDefaultValues.setSla99thResponse(bulkApplicationPassCountsForm.getSla99thResponse()  );
		slaKeywithDefaultValues.setSlaFailCount(bulkApplicationPassCountsForm.getSlaFailCount() );
		slaKeywithDefaultValues.setSlaFailPercent(bulkApplicationPassCountsForm.getSlaFailPercent() );
		slaKeywithDefaultValues.setXtraNum(bulkApplicationPassCountsForm.getXtraNum());
		slaKeywithDefaultValues.setSlaPassCountVariancePercent(bulkApplicationPassCountsForm.getSlaPassCountVariancePercent() );
		slaKeywithDefaultValues.setSlaRefUrl(bulkApplicationPassCountsForm.getSlaRefUrl());
		slaKeywithDefaultValues.setComment("");
		
		int rowcont = slaDao.bulkInsertOrUpdateApplication( bulkApplicationPassCountsForm.getApplication(), 
																slaKeywithDefaultValues,
																bulkApplicationPassCountsForm.getApplyRefUrlOption());

		if (rowcont == 0 ){  //nothing was found to upload..
			return "redirect:/bulkApplicationPassCounts?reqErr=No Data.  Check that application '" + bulkApplicationPassCountsForm.getApplication() + "' has had a baseline set..."
					+ "&reqApp=" + bulkApplicationPassCountsForm.getApplication();
		} else { // OK
			return "redirect:/slaList?reqApp=" + bulkApplicationPassCountsForm.getApplication();	
		}		
	}
	

	
	@RequestMapping("/copySla")
	public String copySla(@RequestParam String reqTxnId, @RequestParam(required=false) String reqApp,  @ModelAttribute Sla sla, Model model) {
		sla = slaDao.getSla(reqApp, reqTxnId);
		sla.setTxnId(sla.getTxnId() + "_CPY" );
		model.addAttribute("sla", sla);

		Map<String, Object> map = new HashMap<String, Object>(); 
		List<String> isTxnIgnoredYesNo = populateIsTxnIgnoredYesNoDropdown();	
		map.put("isTxnIgnoredYesNo",isTxnIgnoredYesNo);				
		map.put("reqApp",reqApp);
		map.put("reqTxnId",reqTxnId);
		model.addAttribute("map", map);		
		return "editSla";
	}
	
	
	@RequestMapping("/editSla")
	public String editSla(@RequestParam String reqTxnId, @RequestParam(required=false) String reqApp, @ModelAttribute Sla sla, Model model) {
//		System.out.println("SlaController:editSla : txnId=" + txnId + ", reqApp=" + reqApp  );		
		sla = slaDao.getSla(reqApp, reqTxnId);
		sla.setSlaOriginalTxnId(sla.getTxnId());
		model.addAttribute("sla", sla);		
		
		Map<String, Object> map = new HashMap<String, Object>(); 
		List<String> isTxnIgnoredYesNo = populateIsTxnIgnoredYesNoDropdown();	
		map.put("isTxnIgnoredYesNo",isTxnIgnoredYesNo);		
		map.put("reqApp",reqApp);
		map.put("reqTxnId",reqTxnId);
		model.addAttribute("map", map);
		return "editSla"; 
	}

	
	@RequestMapping("/copyApplicationSla") 
	public Object copyApplicationSla(@RequestParam(required=false) String reqApp,  @ModelAttribute CopyApplicationForm copyApplicationForm ) {
//		System.out.println("@ copyApplicationSla : reqApp=" + copyApplicationForm.getReqApp() +	", ReqToApp=" + copyApplicationForm.getReqToApp()  );

		copyApplicationForm.setReqApp(reqApp);
		copyApplicationForm.setValidForm("N");

		if ( StringUtils.isNotEmpty( copyApplicationForm.getReqToApp())) { 
			copyApplicationForm.setValidForm("Y");
			
			//do the copy
			List<Sla> slaList = new ArrayList<Sla>();
			slaList = slaDao.getSlaList(reqApp);
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

	@RequestMapping("/updateApplicationSla")	
	public String updateApplicationSla(@RequestParam(required=false) String reqApp, @ModelAttribute CopyApplicationForm copyApplicationForm) {
//		System.out.println("@ updateApplicationSla : reqApp=" + copyApplicationForm.getReqApp() + ", ReqToApp=" + copyApplicationForm.getReqToApp()  );
		return "redirect:/slaList?reqApp=" + reqApp  ;
	}	
	
	
	
	@RequestMapping("/updateSla")
	public String updateSla(@RequestParam(required=false) String reqApp, @ModelAttribute Sla sla) {
//		System.out.println("@ updateSla : reqApp=" + reqApp + ", app=" + sla.getApplication() + ",  origTxn=" + sla.getSlaOriginalTxnId() + ", txnId=" + sla.getTxnId() + " ,90th=" + sla.getSla90thResponse()   );
		slaDao.updateData(sla);
		return "redirect:/slaList?reqApp=" + reqApp  ;
	}


	

	@RequestMapping("/deleteApplicationSla")
	public String deleteApplicationSla(@RequestParam String reqApp) {
//		System.out.println("deleting all slas for application " + reqApp );
		slaDao.deleteAllSlasForApplication(reqApp);
		return "redirect:/slaList?reqApp=";
	}
	
	

	@RequestMapping("/deleteSla")
	public String deleteSla(@RequestParam String reqTxnId, @RequestParam(required=false) String reqApp) {
//		System.out.println("deleting sla for  app = " + reqApp + ", txnId = " + txnId);
		slaDao.deleteData(reqApp, reqTxnId);
		return "redirect:/slaList?reqApp=" + reqApp;
	}
	
	
	private List<String> populateSlaApplicationDropdown() {
		List<String> applicationList = new ArrayList<String>();
		applicationList = slaDao.findApplications();
		applicationList.add(0, "");
		return applicationList;
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

	
	private List<String> populateIsTxnIgnoredYesNoDropdown( ) {
		List<String> isTxnIgnoredYesNo =  new ArrayList<String>();
		isTxnIgnoredYesNo.add("N");
		isTxnIgnoredYesNo.add("Y");
		return isTxnIgnoredYesNo;
	}		
	
	private List<String> populateApplyRefUrlDropdown( ) {
		List<String> applyRefUrlOptions =  new ArrayList<String>();
		applyRefUrlOptions.add(AppConstantsMetrics.APPLY_TO_NEW_SLAS_ONLY);
		applyRefUrlOptions.add(AppConstantsMetrics.APPLY_TO_ALL_SLAS);
		return applyRefUrlOptions;
	}		
	
	
	
}
