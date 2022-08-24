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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;
import com.mark59.datahunter.model.UploadFile;


/**
 * Upload a file of data into the DataHunter database.   
 * Written as a 'file stream' to enable large files to be processed,   
 * see https://www.baeldung.com/spring-apache-file-upload
 * 
 * @author Philip Webb 2020
 *
 */

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
	public ModelAndView fileUpload(Model model,HttpServletRequest httpServletRequest) throws FileUploadException, IOException {
		
		//System.out.println("at UploadFileController upload_action");
		DataHunterUtils.expireSession(httpServletRequest, 1200); 
		if (!ServletFileUpload.isMultipartContent(httpServletRequest)){
			throw new RuntimeException("Internal System error: Not a MultipartContent Request");
		};

		UploadFile uploadFile = new UploadFile();
		uploadFile.setUseability(DataHunterConstants.UNUSED);
		uploadFile.setUpdateOrBypassExisting(DataHunterConstants.UPDATE_USEABILITY_ON_EXISTING_ENTRIES);
		
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setSelectClause(" count(*)  as counter ");
		policySelectionCriteria.setSelectOrder(DataHunterConstants.SELECT_UNORDERED);
		policySelectionCriteria.setApplication("UNEXPECTEFDFIELDORDER");
		policySelectionCriteria.setUseability(DataHunterConstants.UNUSED);
		
		int lineCount=0; int rowsInserted=0; int rowsUpdated=0; 
		String filename = "not valid!";
	
		ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator iterStream = upload.getItemIterator(httpServletRequest);

		while (iterStream.hasNext()) {
			
			FileItemStream item = iterStream.next();
			String name = item.getFieldName();
			if (item.isFormField()) {
				String formFieldValue = Streams.asString(item.openStream());
				// System.out.println("Form field " + name	+ " with value " + formFieldValue + " detected.");
				
				if ("application".equals(name)) {
					uploadFile.setApplication(formFieldValue);
					policySelectionCriteria.setApplication(uploadFile.getApplication());
				} else if ("lifecycle".equals(name)) {
					uploadFile.setLifecycle(formFieldValue);
					policySelectionCriteria.setLifecycle(uploadFile.getLifecycle());
				} else if ("useability".equals(name)) {
					uploadFile.setUseability(formFieldValue);
					policySelectionCriteria.setUseability(formFieldValue);
				} else if ("updateOrBypassExisting".equals(name)) {
					uploadFile.setUpdateOrBypassExisting(formFieldValue);
				}
				
			} else {  // its the file.  Note there is an assumption the file will come after the form fields.
				
				// System.out.println("File field " + name + " with file name " + item.getName() + " detected.");
				filename = item.getName();
				BufferedReader br = null;

				try {
					String line;
					br = new BufferedReader(new InputStreamReader(item.openStream()));
					
					while ((line = br.readLine()) != null) {

						lineCount++;
						policySelectionCriteria.setIdentifier(line.replace("'", "''"));    // generic database char escaping
						// System.out.println("line: " + line + ", uploadFile" + uploadFile );

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
					} //end while
					br.close();

				} catch (Exception e) {
					System.err.println(e.getMessage());
					model.addAttribute("filename", filename);
					model.addAttribute("sql", "n/a");
					model.addAttribute("sqlResult", e.getMessage());
					model.addAttribute("rowsAffected", "error occured around line " + lineCount);
					try {br.close();} catch (Exception e1){System.err.println(e1.getMessage());}
					return new ModelAndView("/upload_action", "model", model);
				}

				model.addAttribute("filename", filename);
				model.addAttribute("sql", "(multiple)");
				model.addAttribute("sqlResult", "PASS");
				model.addAttribute("rowsAffected", (rowsInserted+rowsUpdated) + 
						" (" + rowsInserted + " inserts, " + rowsUpdated + " updates)");	

				if (rowsInserted+rowsUpdated == 0 ){
					if (StringUtils.isBlank(filename) ) {
						model.addAttribute("sqlResultText", "sql execution OK, but no rows where affected."
								+ "<br>Did you forget to select the file?");
					} else { 
						model.addAttribute("sqlResultText", "sql execution OK, but no rows where affected.");
					}
				} else {
					model.addAttribute("sqlResultText", "sql execution OK");
				}

			}		
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
		List<String> usabilityList = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		modelAndView.getModel().put("Useabilities",usabilityList);
		List<String> updateOrBypass = new ArrayList<>(DataHunterConstants.UPDATE_OR_BYPASS);
		modelAndView.getModel().put("updateOrBypass",updateOrBypass);
		modelAndView.getModel().put("validationerror", ex.getMessage());	
		modelAndView.getModel().put("uploadFile", new UploadFile());
		modelAndView.addObject("uploadFile", new UploadFile());
		return modelAndView;
	}
	

	private void createDropdownAttributes(Model model) {
		List<String> usabilityList = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		model.addAttribute("Useabilities",usabilityList);	
		List<String> updateOrBypass = new ArrayList<>(DataHunterConstants.UPDATE_OR_BYPASS);
		model.addAttribute("updateOrBypass",updateOrBypass);
	}

}	
