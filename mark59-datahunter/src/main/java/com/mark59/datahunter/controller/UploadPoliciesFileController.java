package com.mark59.datahunter.controller;

import java.io.BufferedReader;
import java.io.IOException;
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
import com.mark59.datahunter.model.UploadPoliciesFile;
import com.opencsv.CSVParser;


@Controller
public class UploadPoliciesFileController {

	
	@Autowired
	PoliciesDAO policiesDAO;
	
	public static final int MAX_ERRORS_REPORTED = 50; 
	public static final int NUM_OF_EXPECTED_COLS = 6; 
	
	
	@RequestMapping("/upload_policies")
	public ModelAndView uploadPolicies(@ModelAttribute UploadPoliciesFile uploadPoliciesFile, Model model) {
		createDropdownAttributes(model);		
		return new ModelAndView("upload_policies");
	}

	
	@PostMapping("/upload_policies_action")	
	public ModelAndView uploadPoliciesAction(@ModelAttribute UploadPoliciesFile uploadPoliciesFile, Model model, 
			@RequestParam("file") MultipartFile file){
	
		String navUrParms = "updateOrBypassExisting=" + DataHunterUtils.encode(uploadPoliciesFile.getUpdateOrBypassExisting())	;	
		model.addAttribute("navUrParms", navUrParms);			
		
		int lineCount=1; int rowsInserted=0; int rowsUpdated=0; int rowsBypassed=0; int errorLines=0;  BufferedReader br = null;
		String errorLinesTxt = "";
		Policies policy = new Policies();
		SqlWithParms sqlWithParms = new SqlWithParms();
		String line = "";
		String policyValidationMsg = "";

		try {
			br = new BufferedReader(new InputStreamReader(file.getInputStream()));
			
			validateHeader(br.readLine());			
			
			while ((line = br.readLine()) != null) {
				lineCount++;

				if (!DataHunterUtils.isEmpty(line.replaceAll("\\s", ""))){
					policyValidationMsg = validLineOfPolicyData(line, errorLines, lineCount);

					if ( DataHunterConstants.OK.equals(policyValidationMsg)){
						policy = createPolicy(line);

						if (DataHunterConstants.UPDATE_EXISTING_ITEMS.equals(uploadPoliciesFile.getUpdateOrBypassExisting())){
							
							if (policyAlreadyExists(policy)) {
								sqlWithParms = policiesDAO.constructUpdatePoliciesSql(policy);
								policiesDAO.runDatabaseUpdateSql(sqlWithParms);
								rowsUpdated++;
							} else {
								sqlWithParms = policiesDAO.constructInsertDataSql(policy);
								policiesDAO.runDatabaseUpdateSql(sqlWithParms);
								rowsInserted++;
							}
							
						} else {  // LEAVE_EXISTING_ITEMS_UNCHANGED
							
							if (policyAlreadyExists(policy)) {
								rowsBypassed++;
							} else {
								sqlWithParms = policiesDAO.constructInsertDataSql(policy);
								policiesDAO.runDatabaseUpdateSql(sqlWithParms);
								rowsInserted++;
							}
						}
						
					} else { // invalid line
						errorLinesTxt += policyValidationMsg;
						errorLines++;
					}
				} // blank line	
			} //end-while
			br.close();
		
		} catch (Exception e) {
			System.err.println(e.getMessage());
			model.addAttribute("filename", file.getOriginalFilename());
			model.addAttribute("sql", sqlWithParms.getSql());
			model.addAttribute("sqlResult", "<b>FATAL ERROR - LOAD ABORTED DURING EXECUTION</b> "
					+ "<br>" + e.getMessage()
					+ "<br>Error occured processing line " + lineCount + " [" + line + "]"
					+ "<br>" + sqlWithParms.getSql() + "<br>" + sqlWithParms.getSqlparameters());
			model.addAttribute("rowsAffected", "At point of failure: " + (rowsInserted+rowsUpdated) + 
					" (" + rowsInserted + " inserts, " + rowsUpdated + " updates, " + rowsBypassed + " bypassed)." 
					+ " Also " + errorLines + " invalid data lines found" );	
			try {br.close();} catch (Exception e1){System.err.println(e1.getMessage());}
			return new ModelAndView("/upload_policies_action", "model", model);
		}
		
		model.addAttribute("filename", file.getOriginalFilename());
		model.addAttribute("sql", "(multiple)");
		model.addAttribute("sqlResult", "PASS");
		model.addAttribute("rowsAffected", (rowsInserted+rowsUpdated) + 
				" (" + rowsInserted + " inserts, " + rowsUpdated + " updates, " + rowsBypassed + " bypassed)." 
				+ " Also " + errorLines + " invalid data lines found" );	

		if (rowsInserted+rowsUpdated == 0 ){
			model.addAttribute("sqlResultText", "sql execution OK, but no rows where affected.");
		} else {
			model.addAttribute("sqlResultText", "sql execution OK");
		}
		if (errorLines == 0) {
			model.addAttribute("errorLinesTxt", "0 (no lines rejected during load)");
		} else {
			model.addAttribute("errorLinesTxt", errorLinesTxt);
		}
				
		return new ModelAndView("/upload_policies_action", "model", model);		
	}
	

	private void validateHeader(String headerLine) {
		if (headerLine == null) {
			throw new RuntimeException("File Unselected or Empty!. <br>(A file with a header of format " 
					+ "'"+DataHunterConstants.CSV_DOWNLOAD_HEADER_TEXT+"' is expected)");
		}
		if (!DataHunterConstants.CSV_DOWNLOAD_HEADER_TEXT.equalsIgnoreCase(headerLine.replaceAll("\\s", "")) &&
				!DataHunterConstants.CSV_DOWNLOAD_HEADER_QUOTES.equalsIgnoreCase(headerLine.replaceAll("\\s", ""))){	
			throw new RuntimeException("Invalid Header. <br>A file header of format " 
					+ "'"+DataHunterConstants.CSV_DOWNLOAD_HEADER_TEXT+"' is expected,<br>"
					+ "but the first line of the file was " + headerLine);			
		}
	}
	
	
	public String validLineOfPolicyData(String line, int errorLines, int lineCount){
		String[] lineCols = {};
		try {
			lineCols = new CSVParser().parseLine(line);
		} catch (IOException e) {
			return formatLineLoadErrorMsg(e.getMessage(), line, errorLines, lineCount);
		}
		if (NUM_OF_EXPECTED_COLS != lineCols.length ) {
			return formatLineLoadErrorMsg("expected "+NUM_OF_EXPECTED_COLS+" columns, but found "+lineCols.length, 
					line, errorLines, lineCount);
		}
		if (DataHunterConstants.CSV_DOWNLOAD_BLANK_LINE.equals(line.replaceAll("\\s", ""))) {
			return formatLineLoadErrorMsg("A line of all blank values is not considered valid", 
					line, errorLines, lineCount);
		}
		if (StringUtils.isBlank(lineCols[0])) {
			return formatLineLoadErrorMsg("Loading a blank 'APPLICATION' is not considered valid",
					line, errorLines, lineCount);
		}
		if (!DataHunterConstants.USEABILITY_LIST.contains(lineCols[3].trim())) {
			return formatLineLoadErrorMsg("The 'USEABILITY' value must be one of " + DataHunterConstants.USEABILITY_LIST, 
					line, errorLines, lineCount);
		}
		if (StringUtils.isNotBlank(lineCols[5]) && !StringUtils.isNumeric((lineCols[5].trim()))){
			return formatLineLoadErrorMsg("The 'EPOCHTIME' value must blank or a numeric", line, errorLines, lineCount);

		}
		return DataHunterConstants.OK;
	}

	
	/**
	 * line format assumed: "APPLICATION","IDENTIFIER","LIFECYCLE","USEABILITY","OTHERDATA","EPOCHTIME"
	 * @param line (data line from upload file) 
	 * @return policy 
	 * @throws IOException
	 */
	public Policies createPolicy(String line) throws IOException {
		String[] lineCols = new CSVParser().parseLine(line);
		Policies policy = new Policies();
		
		// policySelectionCriteria.setIdentifier(line.replace("'", "''"));    // generic database char escaping???????
		
		policy.setApplication(lineCols[0].trim());	
		policy.setIdentifier(lineCols[1].trim());
		policy.setLifecycle(lineCols[2].trim());
		policy.setUseability(lineCols[3].trim());
		policy.setOtherdata(lineCols[4]);
		
		if (StringUtils.isNumeric(lineCols[5].trim())){
			policy.setEpochtime(Long.valueOf(lineCols[5].trim()));
		} else {
			policy.setEpochtime(System.currentTimeMillis());
		}
		return policy;
	}
	
	
	private String formatLineLoadErrorMsg(String errorMsg, String line, int errorLines, int lineCount) {
		String formattedErrorLine = "";
		if (errorLines == MAX_ERRORS_REPORTED){
			return "<br><b>... more than " + MAX_ERRORS_REPORTED + " invalid lines of data found</b>";
		}
		if (errorLines == 0){
			formattedErrorLine = "<b>Summary of Invalid Data Lines</b><br>";
		}		
		if (errorLines < MAX_ERRORS_REPORTED) {
			formattedErrorLine += "<br>line " + String.format("%07d", lineCount) + " [" + line + "] " + errorMsg; 
		}
		return formattedErrorLine;
	}


	private boolean policyAlreadyExists(Policies policy) {
		PolicySelectionCriteria policySelectionCriteria = new PolicySelectionCriteria();
		policySelectionCriteria.setSelectClause(PoliciesDAO.SELECT_POLICY_COUNTS);
		policySelectionCriteria.setSelectOrder(DataHunterConstants.SELECT_UNORDERED);		
		policySelectionCriteria.setApplication(policy.getApplication());
		policySelectionCriteria.setIdentifier(policy.getIdentifier());
		policySelectionCriteria.setLifecycle(policy.getLifecycle());
		SqlWithParms sqlWithParms = policiesDAO.constructSelectPolicySql(policySelectionCriteria);
		int policyFound = 0;
		try {
			policyFound = policiesDAO.runCountSql(sqlWithParms);
		} catch (Exception e) {
			throw new RuntimeException("<b>FATAL DATABASE ERROR - LOAD ABORTED DURING EXECUTION<B> "
				+ "<br>" + sqlWithParms.getSql() + "<br>" + sqlWithParms.getSqlparameters()
				+ "<br> policy data : " + policy + "<br>" + e.getMessage());
		}	
		return ( policyFound > 0 );
	}
	    

	private void createDropdownAttributes(Model model) {
		List<String> updateOrBypass = new ArrayList<>(DataHunterConstants.UPDATE_OR_BYPASS_POLICIES);
		model.addAttribute("updateOrBypass",updateOrBypass);
	}

}
