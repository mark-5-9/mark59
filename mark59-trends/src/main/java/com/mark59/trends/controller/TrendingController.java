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
import org.springframework.web.bind.annotation.*;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.trends.application.AppConstantsTrends;
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
import com.mark59.trends.slaIcons.SlaIconColourCodesInterface;
import com.mark59.trends.slaMetrics.MetricSlaChecker;
import com.mark59.trends.slaMetrics.MetricSlaResult;
import com.mark59.trends.slaMetrics.SlaResultTypeEnum;
import com.mark59.trends.slaTransactions.SlaChecker;
import com.mark59.trends.slaTransactions.SlaTransactionResult;

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
	@Autowired
	SlaIconColourCodesInterface slaIconColourCodes;


	@GetMapping("/trending")
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
			reqAppListSelector = AppConstantsTrends.ACTIVE;
			List<String> applications = runDAO.findApplications(reqAppListSelector);

			if (applications.isEmpty()) { // if no active apps, try all apps
				reqAppListSelector = AppConstantsTrends.ALL;
				applications = runDAO.findApplications(reqAppListSelector);

				if (applications.isEmpty()) { // no apps at all
					throw new RuntimeException("Whoa !!  No Applications found " );
				}
			}

			// At this point, applications list is guaranteed to have at least one item
			reqApp = applications.get(0);
		}
		trendingForm.setApplication(reqApp);

		trendingForm.setAppListSelector(AppConstantsTrends.ACTIVE );
		if ( UtilsTrends.defaultIfNull(reqAppListSelector,AppConstantsTrends.ACTIVE).equals(AppConstantsTrends.ALL)){
			trendingForm.setAppListSelector(AppConstantsTrends.ALL);
		}
		if ( ! "Y".equals( applicationDAO.findApplication(reqApp).getActive() )){
			trendingForm.setAppListSelector(AppConstantsTrends.ALL);
		}

		trendingForm.setGraph(UtilsTrends.defaultIfNull(reqGraph, AppConstantsTrends.TXN_90TH_GRAPH)); 	// when no metric, assume txn90th
		GraphMapping graphMapping = graphMappingDAO.findGraphMapping(trendingForm.getGraph());

		trendingForm.setShowCdpOption(UtilsTrends.defaultIfNull(reqShowCdpOption, AppConstantsTrends.SHOW_HIDE_CDP));

		trendingForm.setSqlSelectLike(sanitizeSqlLikePattern(UtilsTrends.defaultIfNull(reqSqlSelectLike, "%"),"%"));
		trendingForm.setSqlSelectNotLike(sanitizeSqlLikePattern(UtilsTrends.defaultIfNull(reqSqlSelectNotLike, ""), ""));

		trendingForm.setManuallySelectTxns(false);
		if ( UtilsTrends.defaultIfNull(reqManuallySelectTxns,"false").equals("true")){
			trendingForm.setManuallySelectTxns(true);
		}

		trendingForm.setChosenTxns(UtilsTrends.defaultIfBlank(reqChosenTxns,""));

		trendingForm.setSqlSelectRunLike(sanitizeSqlLikePattern(UtilsTrends.defaultIfNull(reqSqlSelectRunLike, "%"), "%"));
		trendingForm.setSqlSelectRunNotLike(sanitizeSqlLikePattern(UtilsTrends.defaultIfNull(reqSqlSelectRunNotLike, ""), ""));

//		trendingForm.setSqlSelectRunLike(reqSqlSelectRunLike);
//		trendingForm.setSqlSelectRunNotLike(reqSqlSelectRunNotLike);

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
			String decodedSql = UtilsTrends.decodeBase64urlParam(reqRunTimeSelectionSQL);
			trendingForm.setRunTimeSelectionSQL(validateRawSql(decodedSql));
		}

		trendingForm.setUseRawSQL(false);
		if ( UtilsTrends.defaultIfNull(reqUseRawSQL,"false").equals("true")){
			trendingForm.setUseRawSQL(true);
		}

		trendingForm.setTransactionIdsSQL("");
		if (! UtilsTrends.defaultIfBlank(reqTransactionIdsSQL ,"").equals("")) {
			String decodedSql = UtilsTrends.decodeBase64urlParam(reqTransactionIdsSQL);
			trendingForm.setTransactionIdsSQL(validateRawSql(decodedSql));
		}

		trendingForm.setMaxRun(UtilsTrends.defaultIfBlank(reqMaxRun, AppConstantsTrends.DEFAULT_10));
		trendingForm.setMaxBaselineRun(UtilsTrends.defaultIfBlank(reqMaxBaselineRun, AppConstantsTrends.DEFAULT_01));

		trendingForm.setNthRankedTxn(UtilsTrends.defaultIfBlank(reqNthRankedTxn, AppConstantsTrends.ALL));

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

			String txnsToGraphId = transactionIdsToCommaDelimString(listOfTransactionNamesToGraphTagged);
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


			String slaIconColoursId = slaIconColourCodes.slaIconColourCodesForRun(trendingForm.getApplication(), latestRunTime);
			model.addAttribute("slaIconColoursId", slaIconColoursId );



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


	/**
	 * POST method is deprecated - all trending requests should use GET with URL parameters.
	 * This allows users to copy/paste URLs for sharing trending analysis results.
	 *
	 * @deprecated Use GET /trending with URL parameters instead
	 * @return HTTP 405 Method Not Allowed
	 */
	@Deprecated
	@PostMapping("/trending")
	@ResponseStatus(code = org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED,
	                reason = "POST method is not supported. Use GET /trending with URL parameters instead.")
	public void processTrendingForm(@RequestParam(required = false) String reqApp,
			@ModelAttribute TrendingForm trendingForm, Model model, HttpServletRequest request) {
		// Method intentionally empty - annotation handles the response
	}


	@GetMapping("/trendingAsyncPopulateApplicationList" )
	public @ResponseBody String trendingAsyncPopulateApplicationList(@RequestParam(required=false) String reqAppListSelector ) {
		List<String> applicationList = populateApplicationDropdown(reqAppListSelector);
		return  UtilsTrends.stringListToCommaDelimString(applicationList);
	}


	/**
	 * labelRunShortDescriptionsId id used by the graphic to populate the run labels
	 * If there is a link in the description, it will just grab the link text.
	 *
	 * @param application  application id (as displayed in the graphic)
	 * @param runDatesToGraphId  list of runs graphed
	 * @return labelRunShortDescriptionsId
	 */
	private String populateLabelRunShortDescriptionsId(String application, String runDatesToGraphId) {

		List<String> runDatesToGraph = UtilsTrends.commaDelimStringToStringList(runDatesToGraphId);
		List<String> runShortDescriptionsList = new ArrayList<>();
		String runReferenceLinkText;
		String runComment;

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

			runComment = StringUtils.substringBetween(run.getComment(), "'>", "</a");
			if (runComment == null) {
				runComment = run.getComment();
			}
			if (StringUtils.isNotBlank(runComment)) {
				if (runComment.length() > 20) {
					runDescriptionsb.append(" ").append(runComment.replace(',', ' '), 0, 20);
				} else {
					runDescriptionsb.append(" ").append(runComment.replace(',', ' '));
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
				if (run.getComment().length() > 20 && !UtilsTrends.stringContainsHtmlTags(run.getComment())  ) {
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

		model.addAttribute("trxnIdsWithAnyFailedSlaId", transactionIdsToCommaDelimString(trxnIdsWithAnyFailedSla)  );
		model.addAttribute("trxnIdsWithFailedSla90thResponseId", transactionIdsToCommaDelimString(trxnIdsWithFailedSla90thResponse) );
		model.addAttribute("trxnIdsWithFailedSla95thResponseId", transactionIdsToCommaDelimString(trxnIdsWithFailedSla95thResponse) );
		model.addAttribute("trxnIdsWithFailedSla99thResponseId", transactionIdsToCommaDelimString(trxnIdsWithFailedSla99thResponse) );
		model.addAttribute("trxnIdsWithFailedSlaFailPercentId", transactionIdsToCommaDelimString(trxnIdsWithFailedSlaFailPercent) );
		model.addAttribute("trxnIdsWithFailedSlaFailCount", transactionIdsToCommaDelimString(trxnIdsWithFailedSlaFailCount) );
		model.addAttribute("trxnIdsWithFailedSlaPassCount", transactionIdsToCommaDelimString(trxnIdsWithFailedSlaPassCount) );
		model.addAttribute("missingTransactionsId", transactionIdsToCommaDelimString(cdpTaggedMissingTransactions) );
	}


	private void populateIgnoredTransactionsList(String application, Model model){
		List<String> cdpTaggedIgnoredTransactions = slaDAO.getListOfIgnoredTransactionsAddingCdpTags(application);
		model.addAttribute("ignoredTransactionsId", transactionIdsToCommaDelimString(cdpTaggedIgnoredTransactions) );
	}


	private void populateDisabledSlasList(String application, Model model){
		List<String> cdpTaggedDisabledSlas = slaDAO.getListOfDisabledSlasAddingCdpTags(application);
		model.addAttribute("disabledSlasId", transactionIdsToCommaDelimString(cdpTaggedDisabledSlas) );
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
		model.addAttribute("trxnIdsWithAnyFailedSlaId",					transactionIdsToCommaDelimString(trxnIdsWithFailedSla));
		model.addAttribute("trxnIdsWithFailedSlaForThisMetricMeasure", 	transactionIdsToCommaDelimString(trxnIdsWithFailedSlaForThisMetricMeasure));
		model.addAttribute("missingTransactionsId", transactionIdsToCommaDelimString(missingSlaTransactions) );
	}


	private void populateDisabledMetricSlasList(String application, Model model, GraphMapping graphMapping){
		List<String> disabledMetricSlaNames= new ArrayList<>();
		String metricTxnType = graphMapping.getTxnType();
		String graphValueDerivation = graphMapping.getValueDerivation();
		List<MetricSla> disabledMetricSlas = metricSlaDAO.getDisabledMetricSlas(application, metricTxnType, graphValueDerivation);
		for (MetricSla metricSla : disabledMetricSlas) {
			disabledMetricSlaNames.add(metricSla.getMetricName());
		}
		model.addAttribute("disabledSlasId", transactionIdsToCommaDelimString(disabledMetricSlaNames));
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
		showCdpOptionsList.add(AppConstantsTrends.SHOW_HIDE_CDP);
		showCdpOptionsList.add(AppConstantsTrends.SHOW_SHOW_CDP);
		showCdpOptionsList.add(AppConstantsTrends.SHOW_ONLY_CDP);
		return showCdpOptionsList;
	}


	/**
	 * Converts a list of transaction IDs to a comma-delimited string.
	 * Replaces any commas in transaction IDs with dashes to prevent parsing issues
	 * when the comma-delimited string is passed to trending.jsp.
	 *
	 * @param transactionIds list of transaction IDs (may contain commas)
	 * @return comma-delimited string with sanitized transaction IDs (commas replaced with dashes)
	 */
	private String transactionIdsToCommaDelimString(List<String> transactionIds) {
		List<String> sanitized = transactionIds.stream()
			.map(id -> id.replace(',', '-'))
			.collect(java.util.stream.Collectors.toList());
		return UtilsTrends.stringListToCommaDelimString(sanitized);
	}


	/**
	 * Sanitizes SQL LIKE pattern input to prevent SQL injection attacks.
	 * Removes dangerous SQL characters and validates against allowed characters.
	 *
	 * <p>Implementation details:</p>
	 * <ul>
	 *   <li>Null/empty handling - returns "%" (wildcard for all)</li>
	 *   <li>Dangerous character removal - removes single quotes, double quotes, backslashes, and SQL comment syntax (--, /*, * /)</li>
	 *   <li>SQL keyword removal (case-insensitive) - removes OR, AND, UNION, SELECT, DROP, INSERT, UPDATE, DELETE, EXEC</li>
	 *   <li>SQL operator removal - removes parentheses, equals, less than, greater than</li>
	 *   <li>Whitelist validation - only allows: a-z, A-Z, 0-9, %, _, spaces, dots, and hyphens (all other characters are removed)</li>
	 *   <li>Empty string fallback - returns "%" if sanitized result is empty</li>
	 * </ul>
	 *
	 * @param input the user-provided SQL LIKE pattern
	 * @return sanitized pattern safe for SQL LIKE clause, or "%" if input is null/empty
	 */
	private String sanitizeSqlLikePattern(String input, String whenSanitizedEmpty) {
		// Handle null or empty input
		if ( input.isEmpty() || "%".equals(input.trim()) ) {
			return input;
		}
		String sanitized = input.trim();

		// Remove dangerous SQL characters that could be used for injection
		sanitized = sanitized.replaceAll("[';\"\\\\]", "");     // Remove quotes and backslashes
		sanitized = sanitized.replaceAll("--", "");              // Remove SQL comment syntax
		sanitized = sanitized.replaceAll("/\\*", "");            // Remove SQL block comment start
		sanitized = sanitized.replaceAll("\\*/", "");            // Remove SQL block comment end
		sanitized = sanitized.replaceAll("\\bOR\\b", "");        // Remove OR keyword
		sanitized = sanitized.replaceAll("\\bAND\\b", "");       // Remove AND keyword
		sanitized = sanitized.replaceAll("\\bUNION\\b", "");     // Remove UNION keyword
		sanitized = sanitized.replaceAll("\\bSELECT\\b", "");    // Remove SELECT keyword
		sanitized = sanitized.replaceAll("\\bDROP\\b", "");      // Remove DROP keyword
		sanitized = sanitized.replaceAll("\\bINSERT\\b", "");    // Remove INSERT keyword
		sanitized = sanitized.replaceAll("\\bUPDATE\\b", "");    // Remove UPDATE keyword
		sanitized = sanitized.replaceAll("\\bDELETE\\b", "");    // Remove DELETE keyword
		sanitized = sanitized.replaceAll("\\bEXEC\\b", "");      // Remove EXEC keyword
		sanitized = sanitized.replaceAll("[()=<>]", "");         // Remove SQL operators

		// Only allow alphanumeric characters, SQL LIKE wildcards (% and _), spaces, hyphens, and dots
		// Remove any remaining characters that don't match the allowed pattern
		sanitized = sanitized.replaceAll("[^a-zA-Z0-9_%\\s.\\-]", "");

		// If after sanitization the string is empty, default to wildcard
		if (sanitized.isEmpty()) {
			return whenSanitizedEmpty;
		}
		return sanitized;
	}


	/**
	 * Validates raw SQL input to prevent SQL injection attacks.
	 * Only allows safe SELECT statements without dangerous operations.
	 *
	 * <p>Validation rules:</p>
	 * <ul>
	 *   <li>Null/empty input returns empty string</li>
	 *   <li>Must start with SELECT (case-insensitive, after whitespace)</li>
	 *   <li>Rejects SQL keywords: INSERT, UPDATE, DELETE, DROP, TRUNCATE, ALTER, CREATE, EXEC, EXECUTE</li>
	 *   <li>Rejects SQL comment syntax: double-dash, slash-star, star-slash</li>
	 *   <li>Rejects dangerous characters: semicolons (statement separators)</li>
	 *   <li>Rejects UNION and UNION ALL to prevent query chaining</li>
	 *   <li>Rejects INTO keyword (SELECT INTO operations)</li>
	 *   <li>Rejects dangerous database functions for MySQL, PostgreSQL, and H2:
	 *       <ul>
	 *         <li>MySQL: LOAD_FILE, INTO OUTFILE, INTO DUMPFILE, LOAD DATA</li>
	 *         <li>PostgreSQL: PG_READ_FILE, PG_LS_DIR, PG_SLEEP, COPY, LO_IMPORT, LO_EXPORT, PG_EXECUTE_SERVER_PROGRAM</li>
	 *         <li>H2: FILE_READ, FILE_WRITE, CSVREAD, CSVWRITE, LINK_SCHEMA, RUNSCRIPT</li>
	 *         <li>Common: SYSTEM, SHELL, EXECUTE, EVAL</li>
	 *       </ul>
	 *   </li>
	 * </ul>
	 *
	 * @param rawSql the user-provided raw SQL statement
	 * @return validated SQL if safe, original SQL otherwise (to be rejected by DB layer)
	 * @throws IllegalArgumentException if SQL contains dangerous patterns
	 */
	private String validateRawSql(String rawSql) {
		// Handle null or empty input
		if (rawSql == null || rawSql.trim().isEmpty()) {
			return "";
		}

		String sql = rawSql.trim();
		String sqlUpper = sql.toUpperCase();

		// Must be a SELECT statement
		if (!sqlUpper.startsWith("SELECT")) {
			throw new IllegalArgumentException("Only SELECT statements are allowed. SQL must start with SELECT.");
		}

		// Check for dangerous SQL keywords (data modification)
		String[] dangerousKeywords = {
			"INSERT", "UPDATE", "DELETE", "DROP", "TRUNCATE",
			"ALTER", "CREATE", "EXEC", "EXECUTE"
		};

		for (String keyword : dangerousKeywords) {
			if (sqlUpper.matches(".*\\b" + keyword + "\\b.*")) {
				throw new IllegalArgumentException("SQL contains forbidden keyword: " + keyword);
			}
		}

		// Check for SQL comment syntax (could hide malicious code)
		if (sql.contains("--") || sql.contains("/*") || sql.contains("*/")) {
			throw new IllegalArgumentException("SQL comments are not allowed (-- or /* */)");
		}

		// Check for semicolons (statement separators - could chain commands)
		if (sql.contains(";")) {
			throw new IllegalArgumentException("Multiple SQL statements are not allowed (semicolon detected)");
		}

		// Check for UNION (could be used to chain queries and extract data)
		// Using indexOf to avoid ReDoS vulnerability from regex backtracking
		if (sqlUpper.contains(" UNION ") || sqlUpper.contains("\tUNION\t") ||
		    sqlUpper.contains("\tUNION ") || sqlUpper.contains(" UNION\t")) {
			throw new IllegalArgumentException("UNION queries are not allowed");
		}

		// Check for SELECT INTO (could create tables)
		// Using indexOf to avoid ReDoS vulnerability from regex backtracking
		if (sqlUpper.contains(" INTO ") || sqlUpper.contains("\tINTO\t") || sqlUpper.contains("\tINTO ") || sqlUpper.contains(" INTO\t")) {
			// Verify it's actually "SELECT ... INTO" pattern by checking SELECT appears before INTO
			int selectPos = sqlUpper.indexOf("SELECT");
			int intoPos = Math.min(
				sqlUpper.indexOf(" INTO ") != -1 ? sqlUpper.indexOf(" INTO ") : Integer.MAX_VALUE,
				Math.min(
					sqlUpper.indexOf("\tINTO\t") != -1 ? sqlUpper.indexOf("\tINTO\t") : Integer.MAX_VALUE,
					Math.min(
						sqlUpper.indexOf("\tINTO ") != -1 ? sqlUpper.indexOf("\tINTO ") : Integer.MAX_VALUE,
						sqlUpper.indexOf(" INTO\t") != -1 ? sqlUpper.indexOf(" INTO\t") : Integer.MAX_VALUE
					)
				)
			);
			if (selectPos != -1 && intoPos != Integer.MAX_VALUE && selectPos < intoPos) {
				throw new IllegalArgumentException("SELECT INTO is not allowed");
			}
		}

		// Check for dangerous database functions/procedures across MySQL, H2, and PostgreSQL
		String[] dangerousFunctions = {
			// MySQL dangerous functions
			"LOAD_FILE", "LOAD DATA",

			// PostgreSQL dangerous functions
			"PG_READ_FILE", "PG_LS_DIR", "PG_SLEEP", "COPY",
			"LO_IMPORT", "LO_EXPORT", "PG_EXECUTE_SERVER_PROGRAM",

			// H2 dangerous functions (can execute Java code or file operations)
			"FILE_READ", "FILE_WRITE", "CSVREAD", "CSVWRITE",
			"LINK_SCHEMA", "RUNSCRIPT",

			// Common across databases - system/admin functions
			"SYSTEM", "SHELL", "EVAL"
		};

		for (String func : dangerousFunctions) {
			// Use word boundary to match function names, not just substring occurrences
			if (sqlUpper.matches(".*\\b" + func + "\\b.*")) {
				throw new IllegalArgumentException("SQL contains forbidden function/procedure: " + func);
			}
		}

		// Special check for INTO OUTFILE and INTO DUMPFILE (multi-word patterns)
		if (sqlUpper.contains("INTO OUTFILE") || sqlUpper.contains("INTO DUMPFILE")) {
			throw new IllegalArgumentException("SQL contains forbidden file operation: INTO OUTFILE/DUMPFILE");
		}

		// If all checks pass, return the original SQL
		return rawSql;
	}

}
