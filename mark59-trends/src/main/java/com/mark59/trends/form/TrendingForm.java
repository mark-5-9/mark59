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

package com.mark59.trends.form;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class TrendingForm {
	
	private String  application;
	private String  graph;
	private String  showCdpOption;
	private String  sqlSelectLike;
	private String  sqlSelectNotLike;
	private boolean manuallySelectTxns;
	private String  chosenTxns;	
	private boolean useRawSQL;
	private String  transactionIdsSQL;
	private String  nthRankedTxn;
	private String  sqlSelectRunLike;
	private String  sqlSelectRunNotLike;	
	private boolean manuallySelectRuns;
	private String  chosenRuns;
	private boolean useRawRunSQL;	
	private String  runTimeSelectionSQL;
	private String  maxRun;
	private String  maxBaselineRun;
	private String  appListSelector;
	private boolean displayPointText;
	private boolean displayBarRanges;

	
	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public String getShowCdpOption() {
		return showCdpOption;
	}

	public void setShowCdpOption(String showCdpOption) {
		this.showCdpOption = showCdpOption;
	}

	public String getSqlSelectLike() {
		return sqlSelectLike;
	}

	public void setSqlSelectLike(String sqlSelectLike) {
		this.sqlSelectLike = sqlSelectLike;
	}

	public String getSqlSelectNotLike() {
		return sqlSelectNotLike;
	}

	public void setSqlSelectNotLike(String sqlSelectNotLike) {
		this.sqlSelectNotLike = sqlSelectNotLike;
	}

	public boolean isManuallySelectTxns() {
		return manuallySelectTxns;
	}

	public void setManuallySelectTxns(boolean manuallySelectTxns) {
		this.manuallySelectTxns = manuallySelectTxns;
	}

	public String getChosenTxns() {
		return chosenTxns;
	}

	public void setChosenTxns(String chosenTxns) {
		this.chosenTxns = chosenTxns;
	}

	public boolean isUseRawSQL() {
		return useRawSQL;
	}

	public void setUseRawSQL(boolean useRawSQL) {
		this.useRawSQL = useRawSQL;
	}

	public String getTransactionIdsSQL() {
		return transactionIdsSQL;
	}

	public void setTransactionIdsSQL(String transactionIdsSQL) {
		this.transactionIdsSQL = transactionIdsSQL;
	}

	public String getNthRankedTxn() {
		return nthRankedTxn;
	}

	public void setNthRankedTxn(String nthRankedTxn) {
		this.nthRankedTxn = nthRankedTxn;
	}

	public String getSqlSelectRunLike() {
		return sqlSelectRunLike;
	}

	public void setSqlSelectRunLike(String sqlSelectRunLike) {
		this.sqlSelectRunLike = sqlSelectRunLike;
	}

	public String getSqlSelectRunNotLike() {
		return sqlSelectRunNotLike;
	}

	public void setSqlSelectRunNotLike(String sqlSelectRunNotLike) {
		this.sqlSelectRunNotLike = sqlSelectRunNotLike;
	}

	public boolean isManuallySelectRuns() {
		return manuallySelectRuns;
	}

	public void setManuallySelectRuns(boolean manuallySelectRuns) {
		this.manuallySelectRuns = manuallySelectRuns;
	}

	public String getChosenRuns() {
		return chosenRuns;
	}

	public void setChosenRuns(String chosenRuns) {
		this.chosenRuns = chosenRuns;
	}

	public boolean isUseRawRunSQL() {
		return useRawRunSQL;
	}

	public void setUseRawRunSQL(boolean useRawRunSQL) {
		this.useRawRunSQL = useRawRunSQL;
	}

	public String getRunTimeSelectionSQL() {
		return runTimeSelectionSQL;
	}

	public void setRunTimeSelectionSQL(String runTimeSelectionSQL) {
		this.runTimeSelectionSQL = runTimeSelectionSQL;
	}

	public String getMaxRun() {
		return maxRun;
	}

	public void setMaxRun(String maxRun) {
		this.maxRun = maxRun;
	}

	public String getMaxBaselineRun() {
		return maxBaselineRun;
	}

	public void setMaxBaselineRun(String maxBaselineRun) {
		this.maxBaselineRun = maxBaselineRun;
	}

	public String getAppListSelector() {
		return appListSelector;
	}

	public void setAppListSelector(String appListSelector) {
		this.appListSelector = appListSelector;
	}

	public boolean isDisplayPointText() {
		return displayPointText;
	}

	public void setDisplayPointText(boolean displayPointText) {
		this.displayPointText = displayPointText;
	}

	public boolean isDisplayBarRanges() {
		return displayBarRanges;
	}

	public void setDisplayBarRanges(boolean displayBarRanges) {
		this.displayBarRanges = displayBarRanges;
	}
	
	@Override
	public String toString(){
		return "TrendingForm: "
				+ " application="+application 
				+ " metric="+graph 
				+ " showCdpOption="+showCdpOption 
				+ " sqlSelectLike="+sqlSelectLike 
				+ " sqlSelectNotLike="+sqlSelectNotLike 
				+ " manuallySelectTxns="+manuallySelectTxns 
				+ " chosenTxns="+chosenTxns 
				+ " useRawSQL="+useRawSQL 
				+ " transactionIdsSQL="+transactionIdsSQL 
				+ " nthRankedTxn="+nthRankedTxn
				+ " sqlSelectRunLike="+sqlSelectRunLike 
				+ " sqlSelectRunNotLike="+sqlSelectRunNotLike 				
				+ " manuallySelectRuns="+manuallySelectRuns 
				+ " chosenRuns="+chosenRuns 
				+ " useRawRunSQL="+useRawRunSQL 
				+ " runTimeSelectionSQL="+runTimeSelectionSQL 
				+ " maxRun="+maxRun 
				+ " maxBaselineRun="+maxBaselineRun 
				+ " appListSelector="+appListSelector
				+ " displayPointText="+displayPointText 
				+ " displayBarRanges="+displayBarRanges
				;
	}

}
