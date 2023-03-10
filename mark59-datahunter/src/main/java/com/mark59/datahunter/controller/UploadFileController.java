package com.mark59.datahunter.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;
import com.mark59.datahunter.model.UploadFile;


@Controller
public class UploadFileController {

	
	@Autowired
	PoliciesDAO policiesDAO;	
	
	
	@RequestMapping("/upload")
	public ModelAndView showUpload(@ModelAttribute UploadFile uploadFile, Model model) {
		createDropdownAttributes(model);		
		return new ModelAndView("upload");
	}


	@PostMapping("/upload_action")	
	public ModelAndView fileUpload(@ModelAttribute UploadFile uploadFile, Model model, @RequestParam("file") MultipartFile file){
		// System.out.println("uploadFile: " + uploadFile);

		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setSelectClause(" count(*)  as counter ");
		policySelectionCriteria.setSelectOrder(DataHunterConstants.SELECT_UNORDERED);
		policySelectionCriteria.setApplication(uploadFile.getApplication().trim());
		policySelectionCriteria.setLifecycle(uploadFile.getLifecycle().trim());
		policySelectionCriteria.setUseability(uploadFile.getUseability());
		
		int lineCount=0; int rowsInserted=0; int rowsUpdated=0; BufferedReader br = null;

		try {
			String line;
			br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			
			while ((line = br.readLine()) != null) {
				lineCount++;
				System.out.println("  <" + lineCount + "> " + line  );;
				policySelectionCriteria.setIdentifier(line.replace("'", "''"));    // generic database char escaping
	
				if (DataHunterConstants.UPDATE_USEABILITY_ON_EXISTING_ENTRIES.equals(uploadFile.getUpdateOrBypassExisting())){
					
					if (policyAlreadyExsits(policySelectionCriteria)) {
						rowsUpdated = rowsUpdated + updatePolicyUseState(policySelectionCriteria, uploadFile.getUseability());
					} else {
						addNewPolicy(policySelectionCriteria, uploadFile.getUseability() );
						rowsInserted++;
					}
					
				} else {  // LEAVE_USEABILITY_ON_EXISTING_ENTRIES_UNCHANGED
					
					if ( ! policyAlreadyExsits(policySelectionCriteria)) {
						addNewPolicy(policySelectionCriteria, uploadFile.getUseability());
						rowsInserted++;
					}
				}
			} //end-while
			br.close();
		
		} catch (Exception e) {
			System.err.println(e.getMessage());
			model.addAttribute("filename", file.getOriginalFilename());
			model.addAttribute("sql", "n/a");
			model.addAttribute("sqlResult", e.getMessage());
			model.addAttribute("rowsAffected", "error occured around line " + lineCount);
			try {br.close();} catch (Exception e1){System.err.println(e1.getMessage());}
			return new ModelAndView("/upload_action", "model", model);
		}
		
		model.addAttribute("filename", file.getOriginalFilename());
		model.addAttribute("sql", "(multiple)");
		model.addAttribute("sqlResult", "PASS");
		model.addAttribute("rowsAffected", (rowsInserted+rowsUpdated) + 
				" (" + rowsInserted + " inserts, " + rowsUpdated + " updates)");	

		if (rowsInserted+rowsUpdated == 0 ){
			if (StringUtils.isBlank(file.getOriginalFilename()) ) {
				model.addAttribute("sqlResultText", "sql execution OK, but no rows where affected."
						+ "<br>Did you forget to select the file?");
			} else { 
				model.addAttribute("sqlResultText", "sql execution OK, but no rows where affected.");
			}
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
	
	private void createDropdownAttributes(Model model) {
		List<String> usabilityList = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		model.addAttribute("Useabilities",usabilityList);	
		List<String> updateOrBypass = new ArrayList<>(DataHunterConstants.UPDATE_OR_BYPASS);
		model.addAttribute("updateOrBypass",updateOrBypass);
	}

}
