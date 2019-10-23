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

package com.mark59.metrics.sla;

import java.math.BigDecimal;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SlaTransactionResult {
	
	private String txnId;
	private boolean passedAllSlas;
	private boolean foundSLAforTxnId;
	
	private boolean passed90thResponse;
	private BigDecimal txn90thResponse;
	private BigDecimal sla90thResponse;
	
	private boolean passedFailPercent;
	private double txnFailurePercent;
	private BigDecimal slaFailurePercent;
	
	private boolean passedPassCount;
	private long txnPassCount;
	private long slaPassCount;	
	private BigDecimal	slaPassCountVariancePercent;
		
	
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public boolean isPassedAllSlas() {
		return passedAllSlas;
	}
	public void setPassedAllSlas(boolean passedAllSlas) {
		this.passedAllSlas = passedAllSlas;
	}
	
	public boolean isFoundSLAforTxnId() {
		return foundSLAforTxnId;
	}
	public void setFoundSLAforTxnId(boolean foundSLAforTxnId) {
		this.foundSLAforTxnId = foundSLAforTxnId;
	}
	
	public boolean isPassed90thResponse() {
		return passed90thResponse;
	}
	public void setPassed90thResponse(boolean passed90thResponse) {
		this.passed90thResponse = passed90thResponse;
	}
	public boolean isPassedFailPercent() {
		return passedFailPercent;
	}
	public void setPassedFailPercent(boolean passedFailPercent) {
		this.passedFailPercent = passedFailPercent;
	}
	public BigDecimal getTxn90thResponse() {
		return txn90thResponse;
	}
	public void setTxn90thResponse(BigDecimal txn90thResponse) {
		this.txn90thResponse = txn90thResponse;
	}
	public BigDecimal getSla90thResponse() {
		return sla90thResponse;
	}
	public void setSla90thResponse(BigDecimal sla90thResponse) {
		this.sla90thResponse = sla90thResponse;
	}
	public double getTxnFailurePercent() {
		return txnFailurePercent;
	}
	public void setTxnFailurePercent(double txnFailurePercent) {
		this.txnFailurePercent = txnFailurePercent;
	}
	public BigDecimal getSlaFailurePercent() {
		return slaFailurePercent;
	}
	public void setSlaFailurePercent(BigDecimal slaFailurePercent) {
		this.slaFailurePercent = slaFailurePercent;
	}

	public boolean isPassedPassCount() {
		return passedPassCount;
	}
	public void setPassedPassCount(boolean passedPassCount) {
		this.passedPassCount = passedPassCount;
	}
	public long getTxnPassCount() {
		return txnPassCount;
	}
	public void setTxnPassCount(long txnPassCount) {
		this.txnPassCount = txnPassCount;
	}
	public long getSlaPassCount() {
		return slaPassCount;
	}
	public void setSlaPassCount(long slaPassCount) {
		this.slaPassCount = slaPassCount;
	}
	public BigDecimal getSlaPassCountVariancePercent() {
		return slaPassCountVariancePercent;
	}
	public void setSlaPassCountVariancePercent(BigDecimal slaPassCountVariancePercent) {
		this.slaPassCountVariancePercent = slaPassCountVariancePercent;
	}


	

}
