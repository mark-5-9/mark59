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

package com.mark59.datahunter.controller;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PoliciesForm;
import com.mark59.datahunter.model.PolicySelectionFilter;
import com.opencsv.CSVParser;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 * 
 * <p>Downloaded CSV columns: APPLICATION, IDENTIFIER, LIFECYCLE, USEABILITY, OTHERDATA, EPOCHTIME  
 * <p>references : https://technicalsand.com/streaming-data-spring-boot-restful-web-service/
 * 
 */
@Controller
public class ManageMultiplePoliciesController {
	
	@Autowired
	PoliciesDAO policiesDAO;	
		
	
	@RequestMapping (value = "/download_selected_policies")
	public ResponseEntity<StreamingResponseBody> streamSelectedDataAsFile(@ModelAttribute PolicySelectionFilter policySelectionFilter, Model model) {

		SqlWithParms sqlWithParms = policiesDAO.constructSelectPoliciesFilterSql(policySelectionFilter, false);
		List<Policies> policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);
		CSVParser csvParser = new CSVParser();
		
		StreamingResponseBody responseBody = response -> {
			response.write((csvParser.parseToLine(DataHunterConstants.CSV_DOWNLOAD_HEADER, true)+"\n").getBytes());
			response.flush();
			String csvItemLine;
			
			for (Policies policy : policiesList) {
				csvItemLine = csvParser.parseToLine(
						new String[]{policy.getApplication(), policy.getIdentifier(), policy.getLifecycle(),
								policy.getUseability(), policy.getOtherdata(), Long.toString(policy.getEpochtime())}, true);
				response.write((csvItemLine+"\n").getBytes());
				response.flush();
			}
		};
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DataHunterItems.csv")
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(responseBody);
	}
	
	
	@RequestMapping("/select_multiple_policies")
	public String printSelectedPoliciesUrl(@RequestParam(required=false) String application,@ModelAttribute PolicySelectionFilter policySelectionFilter, Model model){ 
		// System.out.println("/print_selected_policies : " + policySelectionFilter );

		if (DataHunterUtils.isEmpty(policySelectionFilter.getOtherdata())){
			policySelectionFilter.setOtherdata("%");
		}
		if (DataHunterUtils.isEmpty(policySelectionFilter.getCreatedFrom())){
			policySelectionFilter.setCreatedFrom("2001-01-01 01:01:10.000001");
		}
		if (DataHunterUtils.isEmpty(policySelectionFilter.getCreatedTo())){
			policySelectionFilter.setCreatedTo("2099-12-31 23:59:59.999999");
		}
		if (DataHunterUtils.isEmpty(policySelectionFilter.getUpdatedFrom())){
			policySelectionFilter.setUpdatedFrom("2001-01-01 01:01:10.000001");
		}
		if (DataHunterUtils.isEmpty(policySelectionFilter.getUpdatedTo())){
			policySelectionFilter.setUpdatedTo("2099-12-31 23:59:59.999999");
		}
		if (DataHunterUtils.isEmpty(policySelectionFilter.getEpochtimeFrom())){
			policySelectionFilter.setEpochtimeFrom("0");
		}
		if (DataHunterUtils.isEmpty(policySelectionFilter.getEpochtimeTo())){
			policySelectionFilter.setEpochtimeTo("4102444799999");
		}
		if (DataHunterUtils.isEmpty(policySelectionFilter.getLimit())){
			policySelectionFilter.setLimit("100");
		}
		
		List<String> usabilityList = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		List<String> selectOrderList = new ArrayList<>(DataHunterConstants.FILTERED_SELECT_ORDER_LIST);
		List<String> orderDirectionList = new ArrayList<>(DataHunterConstants.ORDER_DIRECTION_LIST);
		usabilityList.add(0,"");
		model.addAttribute("Useabilities",usabilityList);
		model.addAttribute("SelectOrders",selectOrderList);
		model.addAttribute("OrderDirections",orderDirectionList);
		return "/select_multiple_policies";				
	}
	
		
	@RequestMapping("/select_multiple_policies_action")
	public ModelAndView printSelectedPoliciesAction(@ModelAttribute PolicySelectionFilter policySelectionFilter, Model model, HttpServletRequest httpServletRequest) {

		SqlWithParms sqlWithParms = policiesDAO.constructSelectPoliciesFilterSql(policySelectionFilter);
		List<Policies> policiesList = new ArrayList<>();
		
		try {
			policiesList = policiesDAO.runSelectPolicieSql(sqlWithParms);

			List<PoliciesForm> policiesFormList = new ArrayList<PoliciesForm>();
	
			for (Policies policies : policiesList) {
				PoliciesForm policiesForm = new PoliciesForm();
				policiesForm.setApplication(policies.getApplication());
				policiesForm.setIdentifier(policies.getIdentifier());
				policiesForm.setLifecycle(policies.getLifecycle());
				policiesForm.setUseability(policies.getUseability());
				policiesForm.setOtherdata(policies.getOtherdata());
				policiesForm.setCreated(policies.getCreated());
				policiesForm.setUpdated(policies.getUpdated());
				policiesForm.setEpochtime(policies.getEpochtime());
				
				policiesForm.setPolicyKeyAsEncodedUrlParameters("application=" + DataHunterUtils.encode(policies.getApplication()) 
						+ "&identifier=" + DataHunterUtils.encode(policies.getIdentifier()) 
						+ "&lifecycle="  + DataHunterUtils.encode(policies.getLifecycle()));
				
				policiesFormList.add(policiesForm);
			}
			model.addAttribute("policiesFormList", policiesFormList);
			model.addAttribute("sql", sqlWithParms);
			model.addAttribute("rowsAffected", policiesList.size());
			model.addAttribute("sqlResult", "PASS");			
			model.addAttribute("sqlResultText", "sql execution OK");
		} catch (Exception e) {
			model.addAttribute("sql", sqlWithParms);
			model.addAttribute("rowsAffected", -1);			
			model.addAttribute("sqlResult", "FAIL");
			model.addAttribute("sqlResultText", "sql exception caught: " + e.getMessage()) ;
		}
		DataHunterUtils.expireSession(httpServletRequest);
		
		return new ModelAndView("/select_multiple_policies_action", "model", model);
	}
	
	
	@RequestMapping("/delete_multiple_selected_policies")
	public ModelAndView deleteMultipleSelected(@ModelAttribute PolicySelectionFilter policySelectionFilter, Model model, HttpServletRequest httpServletRequest) {

		SqlWithParms sqlWithParms = policiesDAO.constructDeleteMultiplePoliciesSql(policySelectionFilter);
		model.addAttribute("sql", "(delete) " + sqlWithParms);

		try {
			int rowsAffected = policiesDAO.runDatabaseUpdateSql(sqlWithParms);
			
			model.addAttribute("rowsAffected", "(delete) " + rowsAffected);
			model.addAttribute("sqlResult", "(delete) PASS");
	
			if (rowsAffected == 0 ){
				model.addAttribute("sqlResultText", "(delete) sql execution OK, but no rows matched the selection criteria.");
			} else {
				model.addAttribute("sqlResultText", "(delete) sql execution OK");
			}
		} catch (Exception e) {
			model.addAttribute("sqlResult", "FAIL");
			model.addAttribute("sqlResultText", "sql exception caught: " + e.getMessage());
		}

		DataHunterUtils.expireSession(httpServletRequest);
		return new ModelAndView("/select_multiple_policies_action", "model", model);
	}
	
}
