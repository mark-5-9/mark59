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

package com.mark59.trends.slaIcons;


import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.mark59.trends.application.AppConstantsTrends;
import com.mark59.trends.data.beans.Transaction;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAO;
import com.mark59.trends.data.run.dao.RunDAO;
import com.mark59.trends.data.sla.dao.SlaDAO;
import com.mark59.trends.data.transaction.dao.TransactionDAO;
import com.mark59.trends.slaMetrics.MetricSlaChecker;
import com.mark59.trends.slaMetrics.MetricSlaResult;
import com.mark59.trends.slaTransactions.SlaChecker;
import com.mark59.trends.slaTransactions.SlaTransactionResult;


/**
 * @author Philip Webb
 * Written: Australian Spring 2025  
 */
public class SlaIconColourCodes implements SlaIconColourCodesInterface {

	@Autowired
	RunDAO  runDAO; 	
		
	@Autowired
	SlaDAO  slaDAO; 	
	
	@Autowired	
	MetricSlaDAO metricSlaDAO;
	
	@Autowired
	TransactionDAO transactionDAO; 	

	
	public String slaIconColourCodesForRun(String reqApp, String reqRunTime){  
		String slaResultColours =  reqApp+","+reqRunTime+",unknown,unknown,unknown";
		try {
			if (StringUtils.isAllBlank(reqRunTime)){
				reqRunTime = runDAO.findLastRunDate(reqApp);
				slaResultColours =  reqApp+","+reqRunTime+",unknown,unknown,unknown";
			}
			String slaTransactionIcon = computeSlaTransactionResultIconColour(reqApp,reqRunTime);
			String slaMetricsIcon = computeMetricSlasResultIconColour(reqApp,reqRunTime);
			String slaSummaryIcon = computeSlaSummaryIconColour(slaTransactionIcon,slaMetricsIcon);
			slaResultColours = reqApp+","+reqRunTime+","+slaSummaryIcon+","+slaTransactionIcon+","+slaMetricsIcon;
		} catch (Exception e){
			System.out.println(reqApp + ":" + reqRunTime
					+ " failed to load sla results to the dashboard - is it valid? (" + slaResultColours + ")");
		}
		// System.out.println("<< via slaIconColoursForRun : " + reqApp + " : "+ slaResultColours);
		return slaResultColours;
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
	
}
