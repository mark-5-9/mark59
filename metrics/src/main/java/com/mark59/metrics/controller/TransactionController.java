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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.metrics.application.UtilsMetrics;
import com.mark59.metrics.data.beans.MetricSla;
import com.mark59.metrics.data.beans.Sla;
import com.mark59.metrics.data.beans.Transaction;
import com.mark59.metrics.data.metricSla.dao.MetricSlaDAO;
import com.mark59.metrics.data.run.dao.RunDAO;
import com.mark59.metrics.data.sla.dao.SlaDAO;
import com.mark59.metrics.data.transaction.dao.TransactionDAO;
import com.mark59.metrics.form.TransactionRenameForm;

/**
 * @author Philip Webb
 * Written: Australian Winter 2021  
 */

@Controller
public class TransactionController {
	
	@Autowired
	TransactionDAO transactionDAO; 
	@Autowired
	RunDAO runDAO; 
	@Autowired
	SlaDAO slaDAO; 	
	@Autowired
	MetricSlaDAO metricSlaDAO; 	
	
	
	@RequestMapping("/transactionList")
	public ModelAndView getTransactionList(@RequestParam(required=false) String reqApp) {
		List<String> applicationList = populateApplicationDropdown();
		if (StringUtils.isBlank(reqApp) && applicationList.size() > 1  ){
			// when no application request parameter has been sent, take the first application  
			reqApp = (String)applicationList.get(0);
		}		
		List<Transaction> transactionList = transactionDAO.getUniqueListOfTransactionsByType(reqApp);
		
		Map<String, Object> map = new HashMap<String, Object>(); 
		map.put("transactionList",transactionList);
		map.put("reqApp",reqApp);
		map.put("applications",applicationList);
		return new ModelAndView("transactionList", "map", map);
	}
	
	
	@RequestMapping("/transactionRenameDataEntry") 
	public Object renameTransactionEntry(@RequestParam String reqApp,  @RequestParam String reqTxnId, @RequestParam String reqTxnType,
			@ModelAttribute TransactionRenameForm transactionRenameForm  ) {
//		System.out.println("@ transactionRenameDataEntry : reqTxnId=" + reqTxnId + "reqTxnType=" + reqTxnType + ", Form=" + transactionRenameForm);
		transactionRenameForm.setApplication(reqApp); ;
		transactionRenameForm.setFromTxnId(reqTxnId);
		transactionRenameForm.setTxnType(reqTxnType);
		return new ModelAndView("transactionRenameDataEntry", "transactionRenameForm", transactionRenameForm);
	}

	
	@RequestMapping("/transactionRenameValidate") 
	public Object transactionRenameValidate(@ModelAttribute TransactionRenameForm transactionRenameForm){
//		System.out.println("@ transactionRenameValidate : Form=" + transactionRenameForm  );		
		transactionRenameForm.setPassedValidation("Y");
		
		if (StringUtils.isBlank(transactionRenameForm.getToTxnId())){
			transactionRenameForm.setPassedValidation("N");
			transactionRenameForm.setValidationMsg("<p style='color:red'>Blank transaction name not allowed</p>");
			return new ModelAndView("transactionRenameValidate", "transactionRenameForm" , transactionRenameForm  );
		}
		
		if (transactionRenameForm.getToTxnId().equals(transactionRenameForm.getFromTxnId())){
			transactionRenameForm.setPassedValidation("N");
			transactionRenameForm.setValidationMsg("<p style='color:red'>The transaction names must differ !</p>");
			return new ModelAndView("transactionRenameValidate", "transactionRenameForm" , transactionRenameForm  );
		}		
		
		long clashOfTxns = transactionDAO.countRunsContainsBothTxnIds(transactionRenameForm.getApplication(), 
																		transactionRenameForm.getTxnType(),
																		transactionRenameForm.getFromTxnId(),
																		transactionRenameForm.getToTxnId());
		if (clashOfTxns > 0 ){
			
			transactionRenameForm.setPassedValidation("N");
			String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

			String clashSql = "SELECT DISTINCT R.RUN_TIME, R.BASELINE_RUN, R.IS_RUN_IGNORED FROM RUNS R, TRANSACTION T "    
					   + " WHERE R.APPLICATION = '" + transactionRenameForm.getApplication() + "' " 
					   + " AND T.TXN_TYPE = '" + transactionRenameForm.getTxnType() + "'" 
					   + " AND R.APPLICATION = T.APPLICATION AND R.RUN_TIME = T.RUN_TIME "   
					   + " AND R.RUN_TIME IN ( SELECT RUN_TIME FROM TRANSACTION  WHERE APPLICATION = '" + transactionRenameForm.getApplication() + "' " 
					   														+ " AND TXN_TYPE = '" + transactionRenameForm.getTxnType() + "'" 
					   														+ " AND TXN_ID = '" + transactionRenameForm.getFromTxnId() + "') " 
					   + " AND R.RUN_TIME IN ( SELECT RUN_TIME FROM TRANSACTION  WHERE APPLICATION = '" + transactionRenameForm.getApplication() + "' " 
					   														+ " AND TXN_TYPE = '" + transactionRenameForm.getTxnType() + "'" 
					   														+ " AND TXN_ID = '" + transactionRenameForm.getToTxnId() + "') "; 			
			String encodedClashSql = UtilsMetrics.encodeBase64urlParam(clashSql);

			if (   Mark59Constants.DatabaseTxnTypes.TRANSACTION.name().equals(transactionRenameForm.getTxnType())){
				String clashLink = baseUrl + "/trending?reqApp=" + transactionRenameForm.getApplication() + 
						"&reqUseRawRunSQL=true&reqRunTimeSelectionSQL=" + encodedClashSql;
				transactionRenameForm.setValidationMsg("<p style='color:red'><b>Invalid Rename. There are run(s) containing both transaction names.</b></p>" +
						"<p>Please refer to the link below to examine these runs</p>" + 
						"<p><a href='" + clashLink + "'>Trend Analysis Graph for Runs with both Transaction Names</a></p>" ); 
			} else { 
				
				String graph = "CPU_UTIL";
				if  (Mark59Constants.DatabaseTxnTypes.MEMORY.name().equals(transactionRenameForm.getTxnType())){
					graph = "MEMORY";
				} else if  (Mark59Constants.DatabaseTxnTypes.DATAPOINT.name().equals(transactionRenameForm.getTxnType())){
					graph = "DATAPOINT_AVE";
				}  
				String clashLink = baseUrl + "/trending?reqApp=" + transactionRenameForm.getApplication() + "&reqGraph=" + graph +
						"&reqUseRawRunSQL=true&reqRunTimeSelectionSQL=" + encodedClashSql;
				transactionRenameForm.setValidationMsg("<p style='color:red'><b>Invalid Rename. There are run(s) containing both transaction names.</b></p>" +
						"<p>Please refer to the link below to examine these runs</p>" + 
						"<p>Note for metric transaction types the Graph names that delpoy with Mark59 are assumed to exist.</p>" + 
						"<p><a href='" + clashLink + "'>Trend Analysis Graph for Runs with both Transaction Names</a></p>" ); 			
			}
	
			
		} else {  // a valid rename
			
			String validationOkMsg = "<p>Please press the Rename button to rename the transaction.";
					
			if (Mark59Constants.DatabaseTxnTypes.TRANSACTION.name().equals(transactionRenameForm.getTxnType())){					
				validationOkMsg = validationOkMsg +
					"<p>If an SLA exists for the original transaction it will also be renamed, " +
					"unless an SLA with the new transaction name already exists.";
			} else {
				validationOkMsg = validationOkMsg +
					"<p>If metric SLA(s) exist for the original transaction / txn type, they will also be renamed, " +
					"unless any SLA with the new transaction name (for the same txn type) already exists.";				
			}
			
			long doesToTxnIdExist = transactionDAO.countRunsContainsBothTxnIds(transactionRenameForm.getApplication(), 
																				transactionRenameForm.getTxnType(),
																				transactionRenameForm.getToTxnId(),
																				transactionRenameForm.getToTxnId()); //repeated!
			if (doesToTxnIdExist > 0 ) {
				validationOkMsg = validationOkMsg +
				  	"<p>Some runs already contain transactions named " + transactionRenameForm.getToTxnId() + ".<br>" +
				  	"This means that this rename action may <b>not be reversible</b> (you are doing a merge of two transaction Ids)." + 
					"<p>Check everything is OK before you Rename !"; 
			}
			transactionRenameForm.setValidationMsg(validationOkMsg);
		}
		return new ModelAndView("transactionRenameValidate", "transactionRenameForm" , transactionRenameForm  );	
	}	

	
	@RequestMapping("/updateTransactionTables")
	public String updateTransactionTables(@ModelAttribute TransactionRenameForm transactionRenameForm){
		
		transactionDAO.renameTransactions(transactionRenameForm.getApplication(), 
											transactionRenameForm.getTxnType(),
											transactionRenameForm.getFromTxnId(),
											transactionRenameForm.getToTxnId());
		
		if (Mark59Constants.DatabaseTxnTypes.TRANSACTION.name().equals(transactionRenameForm.getTxnType())){
			
			Sla slaFromTxnId = slaDAO.getSla(transactionRenameForm.getApplication(), transactionRenameForm.getFromTxnId()); 
			Sla slaToTxnId   = slaDAO.getSla(transactionRenameForm.getApplication(), transactionRenameForm.getToTxnId()); 
			
			if (slaFromTxnId != null && slaToTxnId == null ){  // okay to rename the SLA 
				slaToTxnId = new Sla(slaFromTxnId);
				slaToTxnId.setTxnId(transactionRenameForm.getToTxnId());
				slaDAO.insertData(slaToTxnId);
				slaDAO.deleteData(transactionRenameForm.getApplication(), transactionRenameForm.getFromTxnId());
			}				
		
		} else { // a metric txn type	
			
			List<MetricSla> slaFromMetricTxnIds = metricSlaDAO.getMetricSlaList(transactionRenameForm.getApplication(), 
																				transactionRenameForm.getFromTxnId(),
																				transactionRenameForm.getTxnType());
			List<MetricSla> slaToMetricTxnIds   = metricSlaDAO.getMetricSlaList(transactionRenameForm.getApplication(), 
																				transactionRenameForm.getToTxnId(),
																				transactionRenameForm.getTxnType());
			if (slaFromMetricTxnIds.size() > 0 && slaToMetricTxnIds.size() == 0 ){  
				// ok to rename the SLA(s) - may be multiple 'Value Derivations'
				for (MetricSla metricSla : slaFromMetricTxnIds) {
					metricSla.setMetricName(transactionRenameForm.getToTxnId());
					metricSlaDAO.insertData(metricSla);
				}
				metricSlaDAO.deleteData(transactionRenameForm.getApplication(), transactionRenameForm.getFromTxnId(), transactionRenameForm.getTxnType());
			}	
		}
		
		return "redirect:/transactionList?reqApp=" + transactionRenameForm.getApplication()   ;
	}

	
	private List<String> populateApplicationDropdown() {
		List<String> applicationList = new ArrayList<String>();
		applicationList = runDAO.findApplications();
		return applicationList;
	}		
	
}
