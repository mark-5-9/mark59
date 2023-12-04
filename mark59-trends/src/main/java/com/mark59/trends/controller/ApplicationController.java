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

import com.mark59.trends.application.AppConstantsTrends;
import com.mark59.trends.data.application.dao.ApplicationDAO;
import com.mark59.trends.data.beans.Application;
import com.mark59.trends.data.beans.Transaction;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAO;
import com.mark59.trends.data.run.dao.RunDAO;
import com.mark59.trends.data.sla.dao.SlaDAO;
import com.mark59.trends.data.transaction.dao.TransactionDAO;
import com.mark59.trends.form.ApplicationDashboardEntry;
import com.mark59.trends.form.CopyApplicationForm;
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
			
			try {
		
				String slaTransactionIcon = computeSlaTransactionResultIconColour(app.getApplication(),lastRunDateStr);
				dashboardEntry.setSlaTransactionResultIcon(slaTransactionIcon);
				
				String slaMetricsIcon = computeMetricSlasResultIconColour(app.getApplication(),lastRunDateStr);
				dashboardEntry.setSlaMetricsResultIcon(slaMetricsIcon);			
						
				String slaSummaryIcon = computeSlaSummaryIconColour(slaTransactionIcon,slaMetricsIcon);
				dashboardEntry.setSlaSummaryIcon(slaSummaryIcon); 
			
			} catch (Exception e) {
				 System.out.println(app.getApplication() + " failed to load correctly on the dashboard - it is valid?");
			}
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
	public String editApplication(@RequestParam String reqApp, @RequestParam String reqAppListSelector, 
			@ModelAttribute Application application, Model model) {
		
		application = applicationDAO.findApplication(reqApp); 
		model.addAttribute("application", application);
		
		Map<String, Object> map = new HashMap<>();
		List<String> activeYesNo = populateActiveYesNoDropdown();	
		map.put("activeYesNo",activeYesNo);
		map.put("applicationId",reqApp);
		map.put("reqAppListSelector",reqAppListSelector);
		model.addAttribute("map", map);
		return "editApplication"; 
	}
	

	@RequestMapping("/copyApplication")
	public ModelAndView copyApplication(@RequestParam(required = false) String reqApp,
			@RequestParam(required = false) String reqAppListSelector, @RequestParam(required = false) String reqErr,
			@ModelAttribute CopyApplicationForm copyApplicationForm, Model model) {
		
		Map<String, Object> map = new HashMap<>();
		map.put("reqApp",reqApp);
		map.put("reqAppListSelector",reqAppListSelector);
		model.addAttribute("map", map);
		return new ModelAndView("copyApplication", "map", map);
	}
	
	
	@RequestMapping("/duplicateApplication") 
	public Object copyApplication(@RequestParam String reqApp, @RequestParam String reqAppListSelector,
			@ModelAttribute CopyApplicationForm copyApplicationForm ) {

		Map<String, Object> map = new HashMap<>();
		map.put("reqApp",reqApp);
		map.put("reqAppListSelector",reqAppListSelector);
		
		copyApplicationForm.setReqApp(reqApp);

		if ( StringUtils.isEmpty(copyApplicationForm.getReqToApp())) { 
			map.put("reqErr", "Please enter the new Application name");
			return new ModelAndView("copyApplication", "map", map);
		}
		
		if (StringUtils.containsWhitespace(copyApplicationForm.getReqToApp())){ 
			map.put("reqErr", "New Application Name cannot contain whitespace ");
			return new ModelAndView("copyApplication", "map", map);
		}
			
		if (!copyApplicationForm.getReqToApp().matches(AppConstantsTrends.ALLOWED_CHARS_APP_NAME) ){ 			
			map.put("reqErr", "New Application Name must contain alphanumerics, underlines, dashes and dots only");
			return new ModelAndView("copyApplication", "map", map);
		}

		Application existingFromApp = applicationDAO.findApplication(copyApplicationForm.getReqToApp());
		if (existingFromApp == null) {
			map.put("reqErr", "<b>Unexpected: " + copyApplicationForm.getReqApp() + " does not exist !</b>");
			return new ModelAndView("copyApplication", "map", map);
		}		
		
		Application existingToApp = applicationDAO.findApplication(copyApplicationForm.getReqToApp());
		if (StringUtils.isNotEmpty(existingToApp.getApplication())){
			map.put("reqErr","<b>"+copyApplicationForm.getReqToApp()+" already exists, delete it or chose another name</b>");
			return new ModelAndView("copyApplication", "map", map);
		}		
		
		try {
			applicationDAO.duplicateEntireApplication(reqApp, copyApplicationForm.getReqToApp());
		} catch (Exception e) {
			map.put("reqErr", "The attempt to copy Application '" + reqApp + "' to '" + copyApplicationForm.getReqToApp() + "' has failed."
					+ "<p>The database may be of been left in an invalid or partially completed state for Application " 
					+ copyApplicationForm.getReqToApp() + "." 
					+ "<p>Please review the error message below, the Trends application log if necessary, and if possible or after "
					+ "correction re-attempt the copy."
					+ "<p>Database tables that may be affected (in the order the copy is attempted) : APPLICATIONS, RUNS, TRANSACTION,"
					+ " SLA, METRICSLA."
					+ "<p> Error Msg : " + e.getMessage());
			System.out.println("duplicateEntireApplication: fromApp " + reqApp + ", toApp " + copyApplicationForm.getReqToApp() + " failed") ;
			e.printStackTrace();
			return new ModelAndView("copyApplication", "map", map);			
		}
		
		return "redirect:/editApplication?reqApp="+copyApplicationForm.getReqToApp()+"&reqAppListSelector=";
	}
	
	
	@RequestMapping("/updateApplication")
	public ModelAndView updateApplication(@RequestParam String reqAppListSelector, @ModelAttribute Application application){
		System.out.println("@ updateApplication : app=" + application.getApplication() ); 
		applicationDAO.updateApplication(application);
		return new ModelAndView("redirect:/dashboard?reqAppListSelector=" + reqAppListSelector ) ;
	}
	
	
	@RequestMapping("/deleteApplication")
	public String deleteApplication(@RequestParam String reqApp) {
		System.out.println("deleting all application data for: " + reqApp  );
		runDAO.deleteAllForApplication(reqApp);
		return "redirect:/dashboard?reqAppListSelector=All";
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

		List<Transaction> transactions = transactionDAO.returnListOfTransactionsToGraph(
				application, AppConstantsTrends.TXN_90TH_GRAPH,AppConstantsTrends.SHOW_SHOW_CDP,"%", "", false, "", 
				lastRunDateStr, false, null, AppConstantsTrends.ALL);
		
		List<SlaTransactionResult> slaTransactionResultList = new SlaChecker()
				.listCdpTaggedTransactionsWithFailedSlas(application, transactions, slaDAO);
		
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
		
		List<String> cdpTaggedMissingTransactions = new SlaChecker()
				.checkForMissingTransactionsWithDatabaseSLAs(application, lastRunDateStr, slaDAO);
		if ( ! cdpTaggedMissingTransactions.isEmpty()){
			return "red";
		}

        return iconColour;
	}
	
	
	private String computeMetricSlasResultIconColour(String application, String lastRunDateStr) {	
		String iconColour = "green";
	
		List<MetricSlaResult> metricSlaResults = new MetricSlaChecker().listFailedMetricSLAs(application,
				lastRunDateStr, null, metricSlaDAO, transactionDAO);
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

	
	private List<String> populateActiveYesNoDropdown( ) {
		List<String> activeYesNo = new ArrayList<>();
		activeYesNo.add("Y");
		activeYesNo.add("N");
		return activeYesNo;
	}		

	
}
