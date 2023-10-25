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
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;
import com.mark59.datahunter.model.UploadIdsFile;


@Controller
public class UploadIdsFileController {

	
	@Autowired
	PoliciesDAO policiesDAO;	
	
	
	@RequestMapping("/upload_ids")
	public ModelAndView uploadIds(@ModelAttribute UploadIdsFile uploadIdsFile, Model model) {
		createDropdownAttributes(model);		
		return new ModelAndView("upload_ids");
	}

	
	@PostMapping("/upload_ids_action")	
	public ModelAndView uploadIdsAction(@ModelAttribute UploadIdsFile uploadIdsFile, Model model, 
			@RequestParam("file") MultipartFile file){

		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setSelectClause(PoliciesDAO.SELECT_POLICY_COUNTS);
		policySelectionCriteria.setSelectOrder(DataHunterConstants.SELECT_UNORDERED);
		policySelectionCriteria.setApplication(uploadIdsFile.getApplication().trim());
		policySelectionCriteria.setLifecycle(uploadIdsFile.getLifecycle().trim());
		policySelectionCriteria.setUseability(uploadIdsFile.getUseability());
		
		String navUrParms = "application=" + DataHunterUtils.encode(uploadIdsFile.getApplication().trim())
			+ "&lifecycle="    + DataHunterUtils.encode(uploadIdsFile.getLifecycle().trim()) 
			+ "&useability="   + DataHunterUtils.encode(uploadIdsFile.getUseability())
			+ "&updateOrBypassExisting="  + DataHunterUtils.encode(uploadIdsFile.getUpdateOrBypassExisting());
		
		model.addAttribute("navUrParms", navUrParms);			
		
		int lineCount=0; int rowsInserted=0; int rowsUpdated=0; BufferedReader br = null;

		try {
			String line;
			br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			
			while ((line = br.readLine()) != null) {
				line = line.trim();
				lineCount++;
				if (!DataHunterUtils.isEmpty(line)){
					// System.out.println("  <"+lineCount+"> ["+line+"]" );
					
					policySelectionCriteria.setIdentifier(line);
		
					if (DataHunterConstants.UPDATE_USEABILITY_ON_EXISTING_ENTRIES.equals(uploadIdsFile.getUpdateOrBypassExisting())){
						
						if (policyAlreadyExists(policySelectionCriteria)) {
							rowsUpdated = rowsUpdated + updatePolicyUseState(policySelectionCriteria, uploadIdsFile.getUseability());
						} else {
							addNewPolicy(policySelectionCriteria, uploadIdsFile.getUseability() );
							rowsInserted++;
						}
						
					} else {  // LEAVE_USEABILITY_ON_EXISTING_ENTRIES_UNCHANGED
						
						if ( ! policyAlreadyExists(policySelectionCriteria)) {
							addNewPolicy(policySelectionCriteria, uploadIdsFile.getUseability());
							rowsInserted++;
						}
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
			return new ModelAndView("/upload_ids_action", "model", model);
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

		return new ModelAndView("/upload_ids_action", "model", model);		
	}

	
	private boolean policyAlreadyExists(PolicySelectionCriteria policySelectionCriteria) {
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
