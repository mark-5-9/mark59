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


import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.servlet.ModelAndView;

import com.mark59.trends.application.AppConstantsMetrics;
import com.mark59.trends.data.application.dao.ApplicationDAO;
import com.mark59.trends.data.beans.Application;
import com.mark59.trends.data.beans.Transaction;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAO;
import com.mark59.trends.data.run.dao.RunDAO;
import com.mark59.trends.data.sla.dao.SlaDAO;
import com.mark59.trends.data.transaction.dao.TransactionDAO;
import com.mark59.trends.form.ApplicationDashboardEntry;
import com.mark59.trends.metricSla.MetricSlaChecker;
import com.mark59.trends.metricSla.MetricSlaResult;
import com.mark59.trends.sla.SlaChecker;
import com.mark59.trends.sla.SlaTransactionResult;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@Controller
public class ApplicationController {

	@Autowired
	ApplicationDAO  applicationDAO; 		
		
	@Autowired
	RunDAO  runDAO; 	
		
	@Autowired
	SlaDAO  slaDAO; 	
	
	@Autowired	
	MetricSlaDAO metricSlaDAO;
	
	@Autowired
	TransactionDAO transactionDAO; 	
	
	
	@RequestMapping("/dashboard")
	public ModelAndView dashboard(@RequestParam(required=false) String reqAppListSelector) {

		List<Application> applicationList =  applicationDAO.findApplications(reqAppListSelector) ;
		List<ApplicationDashboardEntry> dashboardList = new ArrayList<>();

		for (Application app : applicationList) {
			ApplicationDashboardEntry dashboardEntry = new ApplicationDashboardEntry();
			String lastRunDateStr = runDAO.findLastRunDate(app.getApplication());
			dashboardEntry.setApplication(app.getApplication());
			dashboardEntry.setActive(app.getActive() );
			dashboardEntry.setComment(app.getComment());
			dashboardEntry.setSinceLastRun(calcTimeSinceLastRun(lastRunDateStr));
			
			String slaTransactionIcon = computeSlaTransactionResultIconColour(app.getApplication(),lastRunDateStr);
			dashboardEntry.setSlaTransactionResultIcon(slaTransactionIcon);
			
			String slaMetricsIcon = computeMetricSlasResultIconColour(app.getApplication(),lastRunDateStr);
			dashboardEntry.setSlaMetricsResultIcon(slaMetricsIcon);			
					
			String slaSummaryIcon = computeSlaSummaryIconColour(slaTransactionIcon,slaMetricsIcon);
			dashboardEntry.setSlaSummaryIcon(slaSummaryIcon); 
			
			dashboardList.add(dashboardEntry);
		}

		List<String> appListSelectorList = new ArrayList<>();
		appListSelectorList.add("Active");
		appListSelectorList.add("All");

		Map<String, Object> map = new HashMap<>();
		map.put("dashboardList",dashboardList);			
		map.put("reqAppListSelector",reqAppListSelector);	
		map.put("appListSelectorList",appListSelectorList);
		return new ModelAndView("dashboard", "map", map);
	}	


	@RequestMapping("/editApplication")
	public String editApplication(@RequestParam String applicationId, @RequestParam String reqAppListSelector, @ModelAttribute Application application, Model model) {
		
		application = applicationDAO.findApplication(applicationId); 
		model.addAttribute("application", application);
		
		Map<String, Object> map = new HashMap<>();
		List<String> activeYesNo = populateActiveYesNoDropdown();	
		map.put("activeYesNo",activeYesNo);
		map.put("applicationId",applicationId);
		map.put("reqAppListSelector",reqAppListSelector);
		model.addAttribute("map", map);
		return "editApplication"; 
	}
	

	@RequestMapping("/updateApplication")
	public ModelAndView updateApplication(@RequestParam String reqAppListSelector, @ModelAttribute Application application) {
		System.out.println("@ updateApplication : app=" + application.getApplication() ); 
		applicationDAO.updateApplication(application);
		return new ModelAndView("redirect:/dashboard?reqAppListSelector=" + reqAppListSelector ) ;
	}

	
	
	private String calcTimeSinceLastRun(String lastRunDateStr) {
		
		if (StringUtils.isBlank(lastRunDateStr)){
			return "n/a";
		} else {
			
			LocalDateTime lastRunDateTime = LocalDateTime.parse(lastRunDateStr, DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
			LocalDateTime now = LocalDateTime.now();
			
			long daysBetween = Duration.between(lastRunDateTime, now).toDays();
			
			if (daysBetween > 0 ) {
				return  daysBetween + " days";
			} else {
				long hoursBetween = Duration.between(lastRunDateTime, now).toHours();
				return  hoursBetween + " hours";
			}
		}
	}
	
	
	
	private String computeSlaTransactionResultIconColour(String application, String lastRunDateStr) {	
		String iconColour = "green";

		List<Transaction> transactions = transactionDAO.returnListOfTransactionsToGraph(application, AppConstantsMetrics.TXN_90TH_GRAPH, 
				AppConstantsMetrics.SHOW_SHOW_CDP,"%", "", false, "", lastRunDateStr, false, null, AppConstantsMetrics.ALL);
		
		List<SlaTransactionResult> slaTransactionResultList =  new SlaChecker().listCdpTaggedTransactionsWithFailedSlas(application, transactions, slaDAO);
		
		for (SlaTransactionResult slaTransactionResult : slaTransactionResultList) {
			if ( !slaTransactionResult.isPassedFailPercent()){
				return "red";
			}
			if ( !slaTransactionResult.isPassedPassCount()){
				return "red";
			}
			if ( !slaTransactionResult.isPassedAllSlas()){
				iconColour = "yellow";
			}			
		}
		
		List<String> cdpTaggedMissingTransactions  =  new SlaChecker().checkForMissingTransactionsWithDatabaseSLAs(application, lastRunDateStr, slaDAO  );
		if ( ! cdpTaggedMissingTransactions.isEmpty()){
			return "red";
		}

        return iconColour;
	}
	
	
	private String computeMetricSlasResultIconColour(String application, String lastRunDateStr) {	
		String iconColour = "green";
	
		List<MetricSlaResult> metricSlaResults = new MetricSlaChecker().listFailedMetricSLAs(application, lastRunDateStr, null, metricSlaDAO, transactionDAO);
		if ( ! metricSlaResults.isEmpty()){
			return "yellow";
		}
        return iconColour;
	}
	
	
	private String computeSlaSummaryIconColour(String slaTransactionIcon, String slaMetricsIcon) {
		String iconColour = "green";
		if (  "red".equalsIgnoreCase(slaTransactionIcon)  ||  "red".equalsIgnoreCase(slaMetricsIcon) ) {
			return "red";			
		}
		if (  "yellow".equalsIgnoreCase(slaTransactionIcon)  ||  "yellow".equalsIgnoreCase(slaMetricsIcon) ) {		
			return "yellow";			
		}
		return iconColour;
	}

	
	
	@RequestMapping("/deleteApplication")
	public String deleteApplication(@RequestParam String applicationId) {
		System.out.println("deleting all application data for: " + applicationId  );
		runDAO.deleteAllForApplication(applicationId);
		return "redirect:/dashboard?reqAppListSelector=All";
	}
	
	
	private List<String> populateActiveYesNoDropdown( ) {
		List<String> activeYesNo = new ArrayList<>();
		activeYesNo.add("Y");
		activeYesNo.add("N");
		return activeYesNo;
	}		

	
}
