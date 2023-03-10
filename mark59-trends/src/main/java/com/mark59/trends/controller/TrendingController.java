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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.trends.application.AppConstantsMetrics;
import com.mark59.trends.application.UtilsTrends;
import com.mark59.trends.data.application.dao.ApplicationDAO;
import com.mark59.trends.data.beans.BarRange;
import com.mark59.trends.data.beans.GraphMapping;
import com.mark59.trends.data.beans.MetricSla;
import com.mark59.trends.data.beans.Run;
import com.mark59.trends.data.beans.Transaction;
import com.mark59.trends.data.graphMapping.dao.GraphMappingDAO;
import com.mark59.trends.data.metricSla.dao.MetricSlaDAO;
import com.mark59.trends.data.run.dao.RunDAO;
import com.mark59.trends.data.sla.dao.SlaDAO;
import com.mark59.trends.data.transaction.dao.TransactionDAO;
import com.mark59.trends.form.TrendingForm;
import com.mark59.trends.graphic.data.VisGraphicDataProductionInterface;
import com.mark59.trends.metricSla.MetricSlaChecker;
import com.mark59.trends.metricSla.MetricSlaResult;
import com.mark59.trends.metricSla.SlaResultTypeEnum;
import com.mark59.trends.sla.SlaChecker;
import com.mark59.trends.sla.SlaTransactionResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */

@Controller
public class TrendingController {
	
	@Autowired
	ApplicationDAO applicationDAO; 
	@Autowired
	RunDAO runDAO; 	
	@Autowired
	TransactionDAO transactionDAO;
	@Autowired
	SlaDAO slaDAO; 	
	@Autowired	
	MetricSlaDAO metricSlaDAO;
	@Autowired
	GraphMappingDAO graphMappingDAO; 	
	@Autowired
	VisGraphicDataProductionInterface visGraphicDataProduction;

	 //TODO: increase and rationalize input validation (messages, prevent fails etc).   
	
	@RequestMapping(value="/trending",  method = RequestMethod.GET )
	public String loadTrendingPage( @RequestParam(required=false) String reqApp,
									@RequestParam(required=false) String reqGraph,	
									@RequestParam(required=false) String reqShowCdpOption,	
									@RequestParam(required=false) String reqSqlSelectLike,		
									@RequestParam(required=false) String reqSqlSelectNotLike,
									@RequestParam(required=false) String reqManuallySelectTxns,
									@RequestParam(required=false) String reqChosenTxns,	
									@RequestParam(required=false) String reqUseRawSQL,
									@RequestParam(required=false) String reqTransactionIdsSQL,
									@RequestParam(required=false) String reqNthRankedTxn,
									@RequestParam(required=false) String reqSqlSelectRunLike,		
									@RequestParam(required=false) String reqSqlSelectRunNotLike,
									@RequestParam(required=false) String reqManuallySelectRuns,
									@RequestParam(required=false) String reqChosenRuns,
									@RequestParam(required=false) String reqUseRawRunSQL,
									@RequestParam(required=false) String reqRunTimeSelectionSQL,									
									@RequestParam(required=false) String reqMaxRun,
									@RequestParam(required=false) String reqMaxBaselineRun,
									@RequestParam(required=false) String reqAppListSelector,
									@RequestParam(required=false) String reqDisplayPointText,
									@RequestParam(required=false) String reqDisplayBarRanges,
									Model model, HttpServletRequest request ) {

		TrendingForm trendingForm = new TrendingForm();
		
		if (reqApp == null ){
			// on initial entry, when no application request parameter has been sent, take the first "active" application 
			reqAppListSelector = AppConstantsMetrics.ACTIVE;
			if (runDAO.findApplications(reqAppListSelector).isEmpty()) { // if no active apps, just use any existing app
				reqAppListSelector = AppConstantsMetrics.ALL;
			}
			reqApp = runDAO.findApplications(reqAppListSelector).get(0);
			if (StringUtils.isBlank(reqApp)){ 
				throw new RuntimeException("Whoa !!  No Applications found " );
			}
		}
		trendingForm.setApplication(reqApp);
		
		trendingForm.setAppListSelector(AppConstantsMetrics.ACTIVE );
		if ( UtilsTrends.defaultIfNull(reqAppListSelector,AppConstantsMetrics.ACTIVE).equals(AppConstantsMetrics.ALL)){
			trendingForm.setAppListSelector(AppConstantsMetrics.ALL);
		}
		if ( ! "Y".equals( applicationDAO.findApplication(reqApp).getActive() )){
			trendingForm.setAppListSelector(AppConstantsMetrics.ALL);
		}

		trendingForm.setGraph(UtilsTrends.defaultIfNull(reqGraph, AppConstantsMetrics.TXN_90TH_GRAPH)); 	// when no metric, assume txn90th 
		GraphMapping graphMapping = graphMappingDAO.findGraphMapping(trendingForm.getGraph());
		
		trendingForm.setShowCdpOption(UtilsTrends.defaultIfNull(reqShowCdpOption, AppConstantsMetrics.SHOW_HIDE_CDP)); 		 
		
		trendingForm.setSqlSelectLike(UtilsTrends.defaultIfNull(reqSqlSelectLike, "%"));
		trendingForm.setSqlSelectNotLike(UtilsTrends.defaultIfNull(reqSqlSelectNotLike, ""));
			
		trendingForm.setManuallySelectTxns(false);
		if ( UtilsTrends.defaultIfNull(reqManuallySelectTxns,"false").equals("true")){
			trendingForm.setManuallySelectTxns(true);
		}
		
		trendingForm.setChosenTxns(UtilsTrends.defaultIfBlank(reqChosenTxns,""));		
		
		trendingForm.setSqlSelectRunLike(UtilsTrends.defaultIfNull(reqSqlSelectRunLike, "%"));
		trendingForm.setSqlSelectRunNotLike(UtilsTrends.defaultIfNull(reqSqlSelectRunNotLike, ""));		
		
		trendingForm.setManuallySelectRuns(false);
		if ( UtilsTrends.defaultIfNull(reqManuallySelectRuns,"false").equals("true")){
			trendingForm.setManuallySelectRuns(true);		
		}

		trendingForm.setChosenRuns(UtilsTrends.defaultIfBlank(reqChosenRuns,""));
				
		trendingForm.setDisplayPointText(true);
		if ( UtilsTrends.defaultIfNull(reqDisplayPointText,"true").equals("false")){
			trendingForm.setDisplayPointText(false);	
		}
		
		trendingForm.setDisplayBarRanges(true);
		if ( UtilsTrends.defaultIfNull(reqDisplayBarRanges,"true").equals("false")){
			trendingForm.setDisplayBarRanges(false);	
		}
				
		trendingForm.setUseRawRunSQL(false);
		if ( UtilsTrends.defaultIfNull(reqUseRawRunSQL,"false").equals("true")){
			trendingForm.setUseRawRunSQL(true);
		}
		
		trendingForm.setRunTimeSelectionSQL("");
		if (! UtilsTrends.defaultIfBlank(reqRunTimeSelectionSQL ,"").equals("")) {
			trendingForm.setRunTimeSelectionSQL(UtilsTrends.decodeBase64urlParam(reqRunTimeSelectionSQL));		
		}
		
		trendingForm.setUseRawSQL(false);
		if ( UtilsTrends.defaultIfNull(reqUseRawSQL,"false").equals("true")){
			trendingForm.setUseRawSQL(true);
		}
		
		trendingForm.setTransactionIdsSQL("");
		if (! UtilsTrends.defaultIfBlank(reqTransactionIdsSQL ,"").equals("")) {
			trendingForm.setTransactionIdsSQL(UtilsTrends.decodeBase64urlParam(reqTransactionIdsSQL));		
		}		
		
		trendingForm.setMaxRun(UtilsTrends.defaultIfBlank(reqMaxRun, AppConstantsMetrics.DEFAULT_10));
		trendingForm.setMaxBaselineRun(UtilsTrends.defaultIfBlank(reqMaxBaselineRun, AppConstantsMetrics.DEFAULT_01));
	
		trendingForm.setNthRankedTxn(UtilsTrends.defaultIfBlank(reqNthRankedTxn, AppConstantsMetrics.ALL));
		
		// System.out.println("TrendingController trendingForm : " + trendingForm  );
		
		trendingForm.setRunTimeSelectionSQL(runDAO.runsSQL(	trendingForm.getApplication(),
															trendingForm.getSqlSelectRunLike(),
															trendingForm.getSqlSelectRunNotLike(),				
															trendingForm.isUseRawRunSQL(), 
															trendingForm.getRunTimeSelectionSQL()));
		
		String runDatesToGraphId = runDAO.determineRunDatesToGraph( trendingForm.getApplication(),
																	trendingForm.getSqlSelectRunLike(),
																	trendingForm.getSqlSelectRunNotLike(),				
																	trendingForm.isManuallySelectRuns(),
																	trendingForm.getChosenRuns(),
																	trendingForm.isUseRawRunSQL(), 
																	trendingForm.getRunTimeSelectionSQL(),
																	trendingForm.getMaxRun(),	
																	trendingForm.getMaxBaselineRun());  
		model.addAttribute("runDatesToGraphId", runDatesToGraphId);
		
		if ( ! runDatesToGraphId.isEmpty()){ 
			
			trendingForm.setChosenRuns(runDatesToGraphId);

			String latestRunTime = UtilsTrends.commaDelimStringToStringList(runDatesToGraphId).get(0);

			String labelRunShortDescriptionsId = populateLabelRunShortDescriptionsId(trendingForm.getApplication(), runDatesToGraphId );
			model.addAttribute("labelRunShortDescriptionsId", labelRunShortDescriptionsId);
		
			String labelRunDescriptionsId = populateLabelRunDescriptionsId(trendingForm.getApplication(), runDatesToGraphId );
			model.addAttribute("labelRunDescriptionsId", labelRunDescriptionsId);


			trendingForm.setTransactionIdsSQL( transactionDAO.transactionIdsSQL(trendingForm.getApplication(), 
																				trendingForm.getGraph(), 
																				trendingForm.getShowCdpOption(), 
																				trendingForm.getSqlSelectLike(),
																				trendingForm.getSqlSelectNotLike(),
																				trendingForm.isManuallySelectTxns(),
																				UtilsTrends.removeCdpTags(trendingForm.getChosenTxns()), 
																				trendingForm.getChosenRuns(),
																				trendingForm.isUseRawSQL(), 
																				trendingForm.getTransactionIdsSQL())); // used when using raw SQL from form 
			
			List<Transaction> listOfTransactionsToGraph = transactionDAO.returnListOfTransactionsToGraph(
																				trendingForm.getApplication(), 
																				trendingForm.getGraph(), 
																				trendingForm.getShowCdpOption(), 
																				trendingForm.getSqlSelectLike(),
																				trendingForm.getSqlSelectNotLike(),
																				trendingForm.isManuallySelectTxns(),
																				UtilsTrends.removeCdpTags(trendingForm.getChosenTxns()), 
																				trendingForm.getChosenRuns(),
																				trendingForm.isUseRawSQL(), 
																				trendingForm.getTransactionIdsSQL(), 
																				trendingForm.getNthRankedTxn());
			
			List<Transaction> listOfTransactionsToGraphTagged = UtilsTrends.returnOrderedListOfTransactionsToGraphTagged(listOfTransactionsToGraph);
			
			List<String> listOfStdTransactionNamesToGraph    = UtilsTrends.returnFilteredListOfTransactionNamesToGraph(listOfTransactionsToGraph, "N");			
			List<String> listOfCdpTransactionNamesToGraph    = UtilsTrends.returnFilteredListOfTransactionNamesToGraph(listOfTransactionsToGraph, "Y");			
			List<String> listOfTransactionNamesToGraphTagged = UtilsTrends.returnListOfTxnIdsdFromListOfTransactions(listOfTransactionsToGraphTagged);
									
			String txnsToGraphId = UtilsTrends.stringListToCommaDelimString(listOfTransactionNamesToGraphTagged);
			model.addAttribute("txnsToGraphId", txnsToGraphId);
			trendingForm.setChosenTxns(txnsToGraphId);			
			
			if ( Mark59Constants.DatabaseTxnTypes.TRANSACTION.name().equals( graphMapping.getTxnType() )){
				populateFailedTransactionalSlaLists(trendingForm.getApplication(), latestRunTime, listOfTransactionsToGraph, model);
				populateIgnoredTransactionsList(trendingForm.getApplication(), model);	
				populateDisabledSlasList(trendingForm.getApplication(), model);	
				long cdpTxnsCount = transactionDAO.countRunsWithCdpTransactions(trendingForm.getApplication());
				model.addAttribute("cdpTxnsCount", String.valueOf(cdpTxnsCount));
			} else {
				populateFailedMetricSlaLists(trendingForm.getApplication(), latestRunTime, model, graphMapping);
				populateDisabledMetricSlasList(trendingForm.getApplication(), model, graphMapping);	
				model.addAttribute("cdpTxnsCount", "0");
			}
		
			String trxnIdsRangeBarId = populateRangeBarData(trendingForm.getApplication(), latestRunTime, graphMapping.getGraph());	
			model.addAttribute("trxnIdsRangeBarId", trxnIdsRangeBarId );
		
			String csvTextarea = visGraphicDataProduction.createDataPoints(trendingForm.getApplication(), graphMapping, runDatesToGraphId,
					listOfStdTransactionNamesToGraph, listOfCdpTransactionNamesToGraph, listOfTransactionNamesToGraphTagged ); 
			model.addAttribute("csvTextarea", csvTextarea );
		}	
		
		model.addAttribute(trendingForm);
		
		List<String> appListSelectorList = new ArrayList<>();
		appListSelectorList.add("Active");
		appListSelectorList.add("All");
		model.addAttribute("appListSelectors", appListSelectorList);
		
		List<String> applicationList = populateApplicationDropdown(trendingForm.getAppListSelector());
		model.addAttribute("applications", applicationList);
		
		List<String> graphsList = populateMetricsDropdown();
		model.addAttribute("graphs", graphsList);

		List<String> showCdpOptionsList = populateShowCdpOptionsDropdown();
		model.addAttribute("showCdpOptions", showCdpOptionsList);
		
		model.addAttribute("barRangeLegendId", graphMapping.getBarRangeLegend()) ; 		
		model.addAttribute("txnTypedId", 	   graphMapping.getTxnType()) ; 	

		return "trending";
	}

	
	@RequestMapping(value="/trending", method=RequestMethod.POST)
	public String processTrendingForm(@RequestParam(required = false) String reqApp,
			@ModelAttribute("trendingForm") TrendingForm trendingForm, Model model, HttpServletRequest request) {
		throw new RuntimeException("The POST method has been removed.  All requests are now by GET, using URL parms (to allow copy paste)" );
	}

	
	@RequestMapping("/trendingAsyncPopulateApplicationList" )	
	public @ResponseBody String trendingAsyncPopulateApplicationList(@RequestParam(required=false) String reqAppListSelector ) {  
		List<String> applicationList = populateApplicationDropdown(reqAppListSelector); 
		return  UtilsTrends.stringListToCommaDelimString(applicationList);  	
	}

	
	/**
	 * labelRunShortDescriptionsId id used by the graphic to populate the run labels 
	 * 
	 * @param application  application id (as displayed in the graphic)
	 * @param runDatesToGraphId  list of runs graphed
	 * @return labelRunShortDescriptionsId
	 */
	private String populateLabelRunShortDescriptionsId(String application, String runDatesToGraphId) {

		List<String> runDatesToGraph = UtilsTrends.commaDelimStringToStringList(runDatesToGraphId);
		List<String> runShortDescriptionsList = new ArrayList<>();
		String runReferenceLinkText;

		for (String runDate : runDatesToGraph) {
			Run run = runDAO.findRun(application, runDate);
			StringBuilder runDescriptionsb = new StringBuilder();

			runDescriptionsb.append(run.getRunTime(), 0, 4).append(".");
			runDescriptionsb.append(run.getRunTime(), 4, 6).append(".");
			runDescriptionsb.append(run.getRunTime(), 6, 8).append("T");
			runDescriptionsb.append(run.getRunTime(), 8, 10).append(":");
			runDescriptionsb.append(run.getRunTime(), 10, 12).append(" ");

			if ("Y".equalsIgnoreCase(run.getBaselineRun())) {
				runDescriptionsb.append(" BASELINE ");
			}

			runReferenceLinkText = StringUtils.substringBetween(run.getRunReference(), "'>", "</a");
			if (runReferenceLinkText == null) {
				runReferenceLinkText = run.getRunReference();
			}
			runDescriptionsb.append(runReferenceLinkText.replace(',', ' '));

			if (StringUtils.isNotBlank(run.getComment())) {
				if (run.getComment().length() > 20) {
					runDescriptionsb.append(" ").append(run.getComment().replace(',', ' '), 0, 20);
				} else {
					runDescriptionsb.append(" ").append(run.getComment().replace(',', ' '));
				}
			}
			runShortDescriptionsList.add(runDescriptionsb.toString());
		}
		String labelRunShortDescriptionsId = UtilsTrends.stringListToCommaDelimString(runShortDescriptionsList);
//		System.out.println("trendingController:populateLabelRunDescriptionsId: " + labelRunDescriptionsId );
		return labelRunShortDescriptionsId;
	}
	

	
	/**
	 * labelRunDescriptionsId is used for the run descriptions in the Comparison Table
	 * 
	 * @param application  applicaton id (as displayed on graph)
	 * @param runDatesToGraphId  list of runs being graphedt
	 * @return labelRunDescriptionsId
	 */
	private String populateLabelRunDescriptionsId(String application, String runDatesToGraphId) {

		List<String> runDatesToGraph = UtilsTrends.commaDelimStringToStringList(runDatesToGraphId);
		List<String> runDescriptionsList = new ArrayList<>();

		for (String runDate : runDatesToGraph) {
			Run run = runDAO.findRun(application, runDate);
			StringBuilder runDescriptionsb = new StringBuilder();

			runDescriptionsb.append(run.getRunReference().replace(',', ' '));

			if (StringUtils.isNotBlank(run.getComment())) {
				if (run.getComment().length() > 20) {
					runDescriptionsb.append("<br><br><div style='color:grey;'>").append(run.getComment().replace(',', ' '), 0, 20).append("..</div>");
				} else {
					runDescriptionsb.append("<br><br><div style='color:grey;'>").append(run.getComment().replace(',', ' ')).append("</div>");
				}
			}
			if ("Y".equalsIgnoreCase(run.getBaselineRun())) {
				runDescriptionsb.append("<br><div style='color:orange'>*baseline</div>");
			}
			runDescriptionsList.add(runDescriptionsb.toString());
		}
		String labelRunDescriptionsId = UtilsTrends.stringListToCommaDelimString(runDescriptionsList);
//		System.out.println("trendingController:populateLabelRunDescriptionsId: " + labelRunDescriptionsId );
		return labelRunDescriptionsId;
	}
	
	private String populateRangeBarData(String application, String latestRunTime, String graph) {
		String trxnIdsRangeBarId = "";
		List<BarRange> trxnIdsRangeBarData = graphMappingDAO.getTransactionRangeBarDataForGraph(application, latestRunTime, graph); 
		for (BarRange barRange : trxnIdsRangeBarData) {
			trxnIdsRangeBarId = trxnIdsRangeBarId + "\"" + barRange.getTxnId() + "\",\""
					+ barRange.getBarMin().toPlainString() + "\",\"" + barRange.getBarMax().toPlainString() + "\"\n";
		}
		return trxnIdsRangeBarId;
	}	
	

	private void populateFailedTransactionalSlaLists(String application, String latestRunTime, List<Transaction> selectedUntaggedTransactions, 
			Model model) {
 	
		List<SlaTransactionResult> cdpTaggedTransactionsWithFailedSlas =  
				new SlaChecker().listCdpTaggedTransactionsWithFailedSlas(application, selectedUntaggedTransactions, slaDAO);
		
		List<String> trxnIdsWithAnyFailedSla = new ArrayList<>();
		List<String> trxnIdsWithFailedSla90thResponse = new ArrayList<>();
		List<String> trxnIdsWithFailedSla95thResponse = new ArrayList<>();
		List<String> trxnIdsWithFailedSla99thResponse = new ArrayList<>();
		List<String> trxnIdsWithFailedSlaFailPercent = new ArrayList<>();
		List<String> trxnIdsWithFailedSlaFailCount = new ArrayList<>();
		List<String> trxnIdsWithFailedSlaPassCount = new ArrayList<>();

		for (SlaTransactionResult slaTransactionResult : cdpTaggedTransactionsWithFailedSlas) {
			if ( !slaTransactionResult.isPassedAllSlas()){
//				System.out.println("populateTrxnIdsWithFailedSlaId : sla fail for " + slaTransactionResult.getTxnId() );
				trxnIdsWithAnyFailedSla.add(slaTransactionResult.getTxnId());
			}
			if ( !slaTransactionResult.isPassed90thResponse()){
				trxnIdsWithFailedSla90thResponse.add(slaTransactionResult.getTxnId());
			}
			if ( !slaTransactionResult.isPassed95thResponse()){
				trxnIdsWithFailedSla95thResponse.add(slaTransactionResult.getTxnId());
			}
			if ( !slaTransactionResult.isPassed99thResponse()){
				trxnIdsWithFailedSla99thResponse.add(slaTransactionResult.getTxnId());
			}
			if ( !slaTransactionResult.isPassedFailPercent()){
				trxnIdsWithFailedSlaFailPercent.add(slaTransactionResult.getTxnId());
			}
			if ( !slaTransactionResult.isPassedFailCount()){
				trxnIdsWithFailedSlaFailCount.add(slaTransactionResult.getTxnId());
			}				
			if ( !slaTransactionResult.isPassedPassCount()){
				trxnIdsWithFailedSlaPassCount.add(slaTransactionResult.getTxnId());
			}			
		}
		
		List<String> cdpTaggedMissingTransactions = new SlaChecker().checkForMissingTransactionsWithDatabaseSLAs(application, latestRunTime, slaDAO  );
		trxnIdsWithAnyFailedSla.addAll(cdpTaggedMissingTransactions);
		
		model.addAttribute("trxnIdsWithAnyFailedSlaId", UtilsTrends.stringListToCommaDelimString(trxnIdsWithAnyFailedSla)  );
		model.addAttribute("trxnIdsWithFailedSla90thResponseId", UtilsTrends.stringListToCommaDelimString(trxnIdsWithFailedSla90thResponse) );	
		model.addAttribute("trxnIdsWithFailedSla95thResponseId", UtilsTrends.stringListToCommaDelimString(trxnIdsWithFailedSla95thResponse) );	
		model.addAttribute("trxnIdsWithFailedSla99thResponseId", UtilsTrends.stringListToCommaDelimString(trxnIdsWithFailedSla99thResponse) );	
		model.addAttribute("trxnIdsWithFailedSlaFailPercentId", UtilsTrends.stringListToCommaDelimString(trxnIdsWithFailedSlaFailPercent) );
		model.addAttribute("trxnIdsWithFailedSlaFailCount", UtilsTrends.stringListToCommaDelimString(trxnIdsWithFailedSlaFailCount) );
		model.addAttribute("trxnIdsWithFailedSlaPassCount", UtilsTrends.stringListToCommaDelimString(trxnIdsWithFailedSlaPassCount) );
		model.addAttribute("missingTransactionsId", UtilsTrends.stringListToCommaDelimString(cdpTaggedMissingTransactions) );				
	}	
	
	
	private void populateIgnoredTransactionsList(String application, Model model){
		List<String> cdpTaggedIgnoredTransactions = slaDAO.getListOfIgnoredTransactionsAddingCdpTags(application);
		model.addAttribute("ignoredTransactionsId", UtilsTrends.stringListToCommaDelimString(cdpTaggedIgnoredTransactions) );		
	}
	

	private void populateDisabledSlasList(String application, Model model){
		List<String> cdpTaggedDisabledSlas = slaDAO.getListOfDisabledSlasAddingCdpTags(application);
		model.addAttribute("disabledSlasId", UtilsTrends.stringListToCommaDelimString(cdpTaggedDisabledSlas) );		
	}
	
	
	private void populateFailedMetricSlaLists(String application, String latestRunTime, Model model, GraphMapping graphMapping){
	
		String metricTxnType = graphMapping.getTxnType(); 
				
		List<MetricSlaResult> metricSlaResults  = new MetricSlaChecker().listFailedMetricSLAs(application, latestRunTime, metricTxnType, metricSlaDAO, transactionDAO);

		List<String> trxnIdsWithFailedSla = new ArrayList<>();
		List<String> trxnIdsWithFailedSlaForThisMetricMeasure = new ArrayList<>();
		
		List<String> missingSlaTransactions = new ArrayList<>();
		
		/* if the graph being plotted is using the same transaction field (derivation) as the one the SLA failure if for, its name will be marked in red on the graph.  
		 * If the SLA is just for the same metric type, then the value only gets marked in red (just to indicate there is a problem with this value - but not for this graph) 
		 */
		String graphValueDerivation = graphMapping.getValueDerivation(); 
		
		for (MetricSlaResult metricSlaResult : metricSlaResults) {
			if ( ! metricSlaResult.getSlaResultType().equals(SlaResultTypeEnum.MISSING_SLA_TRANSACTION)){ 
				//System.out.println("FailedMetrics : sla fail for " + metricSlaResult.getTxnId() + ":"+metricSlaResult.getValueDerivation() + ":"+ graphValueDerivation );
				if (metricSlaResult.getValueDerivation().equalsIgnoreCase(graphValueDerivation)){
					trxnIdsWithFailedSla.add(metricSlaResult.getTxnId());
					trxnIdsWithFailedSlaForThisMetricMeasure.add(metricSlaResult.getTxnId());						
				} else {   
					assert metricTxnType.equalsIgnoreCase(metricSlaResult.getMetricTxnType());
					trxnIdsWithFailedSla.add(metricSlaResult.getTxnId());	
				}
		
			} else {   // Missing SLA (show only on graphs using the same Metric derivation as the Sla. eg derivation of 'Average' for a metric type of 'CPU_UTIL') 
				
				//System.out.println("MissingMetrics : sla  " + metricSlaResult.getTxnId() + ":"+metricSlaResult.getValueDerivation() + ":"+ graphValueDerivation );				
				if (metricSlaResult.getValueDerivation().equalsIgnoreCase(graphValueDerivation)){
					missingSlaTransactions.add(metricSlaResult.getTxnId());
					trxnIdsWithFailedSla.add(metricSlaResult.getTxnId());
				}
			}
		} 
		model.addAttribute("trxnIdsWithAnyFailedSlaId",					UtilsTrends.stringListToCommaDelimString(trxnIdsWithFailedSla));
		model.addAttribute("trxnIdsWithFailedSlaForThisMetricMeasure", 	UtilsTrends.stringListToCommaDelimString(trxnIdsWithFailedSlaForThisMetricMeasure));
		model.addAttribute("missingTransactionsId", UtilsTrends.stringListToCommaDelimString(missingSlaTransactions) );
	}


	private void populateDisabledMetricSlasList(String application, Model model, GraphMapping graphMapping){
		List<String> disabledMetricSlaNames= new ArrayList<>();
		String metricTxnType = graphMapping.getTxnType();
		String graphValueDerivation = graphMapping.getValueDerivation();
		List<MetricSla> disabledMetricSlas = metricSlaDAO.getDisabledMetricSlas(application, metricTxnType, graphValueDerivation);
		for (MetricSla metricSla : disabledMetricSlas) {
			disabledMetricSlaNames.add(metricSla.getMetricName());
		}
		model.addAttribute("disabledSlasId", UtilsTrends.stringListToCommaDelimString(disabledMetricSlaNames));
	}
	
	
	private List<String> populateApplicationDropdown(String appListSelector ) {
		return runDAO.findApplications(appListSelector);
	}		
	

	private List<String> populateMetricsDropdown() {
		List<String> metricsList = new ArrayList<>();
		List<GraphMapping> graphsList = graphMappingDAO.getGraphMappings();
		for (GraphMapping graphMapping : graphsList) {
			metricsList.add( graphMapping.getGraph() );
		}
		return metricsList;
	}

	
	private List<String> populateShowCdpOptionsDropdown(){
		List<String> showCdpOptionsList = new ArrayList<>();
		showCdpOptionsList.add(AppConstantsMetrics.SHOW_HIDE_CDP);
		showCdpOptionsList.add(AppConstantsMetrics.SHOW_SHOW_CDP);
		showCdpOptionsList.add(AppConstantsMetrics.SHOW_ONLY_CDP);
		return showCdpOptionsList;
	}		
	
}
