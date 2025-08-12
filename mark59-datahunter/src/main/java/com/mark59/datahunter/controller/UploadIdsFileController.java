package com.mark59.datahunter.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.mark59.datahunter.application.DataHunterConstants;
import com.mark59.datahunter.application.DataHunterUtils;
import com.mark59.datahunter.application.SqlWithParms;
import com.mark59.datahunter.data.beans.Policies;
import com.mark59.datahunter.data.policies.dao.PoliciesDAO;
import com.mark59.datahunter.model.PolicySelectionCriteria;
import com.mark59.datahunter.model.PolicySelectionFilter;
import com.mark59.datahunter.model.UpdateUseStateAndEpochTime;
import com.mark59.datahunter.model.UploadIdsFile;


@Controller
public class UploadIdsFileController {

	@Autowired
	PoliciesDAO policiesDAO;	

	
	@GetMapping("/upload_ids")
	public ModelAndView uploadIds(@ModelAttribute UploadIdsFile uploadIdsFile, Model model) {
		createDropdownAttributes(model);		
		return new ModelAndView("upload_ids");
	}

	
	@PostMapping("/upload_ids_action")	
	public ModelAndView uploadIdsAction(@ModelAttribute UploadIdsFile uploadIdsFile, Model model, 
			@RequestParam MultipartFile file){
		
		Policies policies = new Policies();
		policies.setApplication(uploadIdsFile.getApplication().trim());
		policies.setLifecycle(uploadIdsFile.getLifecycle().trim());
		policies.setUseability(uploadIdsFile.getUseability());

		String navUrParms = "application=" + DataHunterUtils.encode(uploadIdsFile.getApplication().trim())
			+ "&lifecycle="    + DataHunterUtils.encode(uploadIdsFile.getLifecycle().trim()) 
			+ "&useability="   + DataHunterUtils.encode(uploadIdsFile.getUseability())
			+ "&typeOfUpload=" + DataHunterUtils.encode(uploadIdsFile.getTypeOfUpload());
		
		model.addAttribute("navUrParms", navUrParms);			
		
		String line = "?";
		int lineCount=0; int rowsInserted=0; int rowsUpdated=0; 
		int rowsDeleted=0; int indexedId=0; boolean firstTimeThru=true;
		List<Policies> policiesList = new ArrayList<Policies>();
		BufferedReader br = null; 

		try {
			br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			
			while ((line = br.readLine()) != null) {
				line = line.trim();
				lineCount++;
				if (!DataHunterUtils.isEmpty(line)){
					// System.out.println("  <"+lineCount+"> ["+line+"]" );
		
					if (DataHunterConstants.UPDATE_USEABILITY_ON_EXISTING_ITEMS.equals(uploadIdsFile.getTypeOfUpload())){
						
						policies.setIdentifier(line);
						policies.setOtherdata("");

						if (policyAlreadyExists(policies)) {
							rowsUpdated = rowsUpdated + updatePolicyUseState(policies);
						} else {
							addNewPolicy(policies);
							rowsInserted++;
						}
						
					} else if (DataHunterConstants.LEAVE_EXISTING_ITEMS_UNCHANGED.equals(uploadIdsFile.getTypeOfUpload())){

						policies.setIdentifier(line);
						
						if ( ! policyAlreadyExists(policies)) {
							policies.setOtherdata("");
							addNewPolicy(policies);
							rowsInserted++;
						}
					
					} else if (DataHunterConstants.BULK_LOAD.equals(uploadIdsFile.getTypeOfUpload())) {
						
						if (firstTimeThru) {  	// can only get here if at least one entry exists on the upload file 
							rowsDeleted = deleteExistingItems(policies);
							firstTimeThru = false;
						}
						
						policies.setIdentifier(line);
						policies.setOtherdata("");
						policiesList.add(new Policies(policies));
						rowsInserted++; 													// only get actually inserted every 100:
						
				    	if ( (rowsInserted % 100) == 0 ){
				    		policiesDAO.insertMultiple(policiesList);
				    		policiesList.clear();
				    	}

					} else if (DataHunterConstants.BULK_LOAD_AS_INDEXED_REUSABLE.equals(uploadIdsFile.getTypeOfUpload())) {
						
						if (firstTimeThru) { 	// can only get here if at least one entry must exist on the upload file
							rowsDeleted = deleteExistingItems(policies);
							policies.setIdentifier(DataHunterConstants.INDEXED_ROW_COUNT); // special "indexed" count row
							policies.setOtherdata("Error occurred during Ids data load");  // replaced by row count on completion
							addNewPolicy(policies);
							rowsInserted++;
							firstTimeThru = false;
						}
						
						indexedId++;
						policies.setIdentifier(StringUtils.leftPad(String.valueOf(indexedId), 10, "0"));  // "indexed" id 
						policies.setOtherdata(line); // placing actual file data into Otherdata
						policiesList.add(new Policies(policies));
						rowsInserted++;
						
				    	if ( (indexedId % 100) == 0 ){
				    		policiesDAO.insertMultiple(policiesList);
				    		policiesList.clear();
				    	}
				    	
					} else {
						throw new Exception("logic error - invalid action for load : " + uploadIdsFile.getTypeOfUpload());
					}
					
				}  // end-if (bypass empty line)	
			} //end-while
			
			if ( (DataHunterConstants.BULK_LOAD.equals(uploadIdsFile.getTypeOfUpload()) || 
				  DataHunterConstants.BULK_LOAD_AS_INDEXED_REUSABLE.equals(uploadIdsFile.getTypeOfUpload()))
					&& rowsInserted > 0){
				policiesDAO.insertMultiple(policiesList);
				policiesList.clear();
				if (DataHunterConstants.BULK_LOAD_AS_INDEXED_REUSABLE.equals(uploadIdsFile.getTypeOfUpload())){
					policiesDAO.updateIndexedRowCounter(policies, indexedId);
				}	
			}
			br.close();
		
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			model.addAttribute("filename", file.getOriginalFilename());
			model.addAttribute("sql", "n/a");
			model.addAttribute("sqlResult", e.getMessage());
			model.addAttribute("rowsAffected", "error occured around line " + lineCount + ", line : " + line);
			try {br.close();} catch (Exception e1){System.err.println(e1.getMessage());}
			return new ModelAndView("/upload_ids_action", "model", model);
		}
		
		model.addAttribute("filename", file.getOriginalFilename());
		model.addAttribute("sql", "(multiple)");
		model.addAttribute("sqlResult", "PASS");
		model.addAttribute("rowsAffected", (rowsInserted+rowsUpdated+rowsDeleted) + 
				" (" + rowsInserted + " inserts, " + rowsUpdated + " updates," + rowsDeleted + " deleted)");	

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
	

	private boolean policyAlreadyExists(Policies policies) {
		PolicySelectionCriteria psc = new PolicySelectionCriteria();
		psc.setSelectClause(PoliciesDAO.SELECT_POLICY_COUNTS);
		psc.setApplication(policies.getApplication());
		psc.setIdentifier(policies.getIdentifier());	
		psc.setLifecycle(policies.getLifecycle());		
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPolicySql(psc);
		int policyFound = policiesDAO.runCountSql(sqlWithParms);
		return ( policyFound > 0 );
	}

	private int updatePolicyUseState(Policies policies) {
		UpdateUseStateAndEpochTime updateUse = new UpdateUseStateAndEpochTime(); 
		updateUse.setApplication(policies.getApplication());
		updateUse.setIdentifier(policies.getIdentifier());
		updateUse.setLifecycle(policies.getLifecycle());
		updateUse.setUseability(null);  // any 'from usability'
		updateUse.setToUseability(policies.getUseability());
		updateUse.setToEpochTime(System.currentTimeMillis());
		SqlWithParms sqlWithParms = policiesDAO.constructUpdatePoliciesUseStateSql(updateUse);
		return policiesDAO.runDatabaseUpdateSql(sqlWithParms);
	}
	    
	private int addNewPolicy(Policies policies) {
		policies.setEpochtime(System.currentTimeMillis());
		SqlWithParms sqlWithParms = policiesDAO.constructInsertDataSql(policies);
		return policiesDAO.runDatabaseUpdateSql(sqlWithParms);
	}
	
	private int deleteExistingItems(Policies policies) {
		PolicySelectionFilter psf = new PolicySelectionFilter();
		psf.setApplication(policies.getApplication());
		psf.setLifecycle(policies.getLifecycle());
		SqlWithParms sqlWithParms = policiesDAO.constructDeleteMultiplePoliciesSql(psf);
		return policiesDAO.runDatabaseUpdateSql(sqlWithParms);
	}

	private void createDropdownAttributes(Model model) {
		List<String> usabilityList = new ArrayList<>(DataHunterConstants.USEABILITY_LIST);
		model.addAttribute("Useabilities",usabilityList);	
		List<String> typeOfUploadList = new ArrayList<>(DataHunterConstants.TYPE_OF_IDS_FILE_UPLOAD);
		model.addAttribute("TypeOfUploads",typeOfUploadList);
	}

}
