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

package com.mark59.datahunter.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;
import com.mark59.datahunter.model.UploadFile;

@Controller
public class UploadFileController implements HandlerExceptionResolver  {

	@Autowired
	PoliciesDAO policiesDAO;	
	
	
	@RequestMapping("/upload")
	public ModelAndView showUpload(UploadFile uploadFile, Model model) {
		createDropdownAttributes(model);		
		return new ModelAndView("upload");
	}


	@PostMapping("/upload_action")
	public ModelAndView fileUpload(@RequestParam("file") MultipartFile file,@ModelAttribute UploadFile uploadFile, 
			Model model,RedirectAttributes redirectAttributes,HttpServletRequest httpServletRequest) {

//		System.out.println("upload_action uploadFile: " + uploadFile  );
		DataHunterUtils.expireSession(httpServletRequest, 1200); 

		if (file.isEmpty()) {
			createDropdownAttributes(model);
			model.addAttribute("validationerror", "oops, file is empty or not yet chosen");	
			return new ModelAndView("/upload", "model", model);	
		}
		if (uploadFile.getApplication().trim().isEmpty()) {
			createDropdownAttributes(model);
			model.addAttribute("validationerror", "oops, the Application cannot be blank");	
			return new ModelAndView("/upload", "model", model);	
		}

		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setApplication(uploadFile.getApplication());
		policySelectionCriteria.setLifecycle(uploadFile.getLifecycle());
		policySelectionCriteria.setUseability(null);  // select for any use state
		policySelectionCriteria.setSelectClause(" count(*)  as counter ");
		policySelectionCriteria.setSelectOrder(DataHunterConstants.SELECT_UNORDERED);
		
		int lineCount=0; int rowsInserted=0; int rowsUpdated=0; 

		try {
			String line;
			InputStream is = file.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			while ((line = br.readLine()) != null) {

				lineCount++;
				policySelectionCriteria.setIdentifier(line.replace("'", "''"));    // generic database char escaping
//				System.out.println("line: " + line + ", uploadFile" + uploadFile );

				if (DataHunterConstants.UPDATE_USEABILITY_ON_EXISTING_ENTRIES.equals(uploadFile.getUpdateOrBypassExisting())){
					
					if (policyAlreadyExsits(policySelectionCriteria)) {
						rowsUpdated = rowsUpdated + updatePolicyUseState(policySelectionCriteria, uploadFile.getUseability());
					} else {
						addNewPolicy(policySelectionCriteria, uploadFile.getUseability() );
						rowsInserted++;
					}
					
				} else {  // LEAVE_EXISTING_ENTRIES_UNCHANGED
					
					if ( ! policyAlreadyExsits(policySelectionCriteria)) {
						addNewPolicy(policySelectionCriteria, uploadFile.getUseability());
						rowsInserted++;
					}
				}
			}

		} catch (Exception e) {
			System.err.println(e.getMessage());
			model.addAttribute("filename", file.getOriginalFilename());
			model.addAttribute("sql", "n/a");
			model.addAttribute("sqlResult", e.getMessage());
			model.addAttribute("rowsAffected", "error occured around line " + lineCount);
			return new ModelAndView("/upload_action", "model", model);
		}

		model.addAttribute("filename", file.getOriginalFilename());
		model.addAttribute("sql", "(multiple)");
		model.addAttribute("sqlResult", "PASS");
		model.addAttribute("rowsAffected", (rowsInserted+rowsUpdated) + 
				" (" + rowsInserted + " inserts, " + rowsUpdated + " updates)");	

		if (rowsInserted+rowsUpdated == 0 ){
			model.addAttribute("sqlResultText", "sql execution OK, but no rows where affected.");
		} else {
			model.addAttribute("sqlResultText", "sql execution OK");
		}
		return new ModelAndView("/upload_action", "model", model);		
	}


	private boolean policyAlreadyExsits(PolicySelectionCriteria policySelectionCriteria) {
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPolicySql(policySelectionCriteria);
		int policyFound = policiesDAO.runCountSql(sqlWithParms);
		return ( policyFound > 0 );
	}

	
	private int addNewPolicy(PolicySelectionCriteria policySelectionCriteria, String useability) {
		Policies policies = new Policies();
		policies.setApplication(policySelectionCriteria.getApplication());
		policies.setIdentifier(policySelectionCriteria.getIdentifier());
		policies.setLifecycle(policySelectionCriteria.getLifecycle());
		policies.setUseability(useability);
		policies.setOtherdata("");
		policies.setEpochtime(System.currentTimeMillis());
		SqlWithParms sqlWithParms = policiesDAO.constructInsertDataSql(policies);
		return policiesDAO.runDatabaseUpdateSql(sqlWithParms);
	}

	
	private int updatePolicyUseState(PolicySelectionCriteria policySelectionCriteria, String useability) {
		UpdateUseStateAndEpochTime updateUse = new UpdateUseStateAndEpochTime(); 
		updateUse.setApplication(policySelectionCriteria.getApplication());
		updateUse.setIdentifier(policySelectionCriteria.getIdentifier());
		updateUse.setLifecycle(policySelectionCriteria.getLifecycle());
		updateUse.setUseability(null);  // any 'from usability'
		updateUse.setToUseability(useability);
		updateUse.setToEpochTime(System.currentTimeMillis());
		SqlWithParms sqlWithParms = policiesDAO.constructUpdatePoliciesUseStateSql(updateUse);
		return policiesDAO.runDatabaseUpdateSql(sqlWithParms);
	}


	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		ModelAndView modelAndView = new ModelAndView("/upload");
		List<String> usabilityList = new ArrayList<String>(DataHunterConstants.USEABILITY_LIST);
		modelAndView.getModel().put("Useabilities",usabilityList);
		List<String> updateOrBypass = new ArrayList<String>(DataHunterConstants.UPDATE_OR_BYPASS);
		modelAndView.getModel().put("updateOrBypass",updateOrBypass);
		modelAndView.getModel().put("validationerror", ex.getMessage());	
		modelAndView.getModel().put("uploadFile", new UploadFile());
		modelAndView.addObject("uploadFile", new UploadFile());
		return modelAndView;
	}
	

	private void createDropdownAttributes(Model model) {
		List<String> usabilityList = new ArrayList<String>(DataHunterConstants.USEABILITY_LIST);
		model.addAttribute("Useabilities",usabilityList);	
		List<String> updateOrBypass = new ArrayList<String>(DataHunterConstants.UPDATE_OR_BYPASS);
		model.addAttribute("updateOrBypass",updateOrBypass);
	}

}	
