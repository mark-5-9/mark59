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

package com.mark59.trends.sla;

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
	
	private boolean passed95thResponse;
	private BigDecimal txn95thResponse;
	private BigDecimal sla95thResponse;
	
	private boolean passed99thResponse;
	private BigDecimal txn99thResponse;
	private BigDecimal sla99thResponse;	
	
	private boolean passedFailPercent;
	private BigDecimal txnFailurePercent;
	private BigDecimal slaFailurePercent;
	
	private boolean passedFailCount;
	private long txnFailCount;
	private long slaFailCount;	
	
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
	public boolean isPassed95thResponse() {
		return passed95thResponse;
	}
	public void setPassed95thResponse(boolean passed95thResponse) {
		this.passed95thResponse = passed95thResponse;
	}
	public BigDecimal getTxn95thResponse() {
		return txn95thResponse;
	}
	public void setTxn95thResponse(BigDecimal txn95thResponse) {
		this.txn95thResponse = txn95thResponse;
	}
	public BigDecimal getSla95thResponse() {
		return sla95thResponse;
	}
	public void setSla95thResponse(BigDecimal sla95thResponse) {
		this.sla95thResponse = sla95thResponse;
	}
	public boolean isPassed99thResponse() {
		return passed99thResponse;
	}
	public void setPassed99thResponse(boolean passed99thResponse) {
		this.passed99thResponse = passed99thResponse;
	}
	public BigDecimal getTxn99thResponse() {
		return txn99thResponse;
	}
	public void setTxn99thResponse(BigDecimal txn99thResponse) {
		this.txn99thResponse = txn99thResponse;
	}
	public BigDecimal getSla99thResponse() {
		return sla99thResponse;
	}
	public void setSla99thResponse(BigDecimal sla99thResponse) {
		this.sla99thResponse = sla99thResponse;
	}
	public BigDecimal getTxnFailurePercent() {
		return txnFailurePercent;
	}
	public void setTxnFailurePercent(BigDecimal txnFailurePercent) {
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
	public boolean isPassedFailCount() {
		return passedFailCount;
	}
	public void setPassedFailCount(boolean passedFailCount) {
		this.passedFailCount = passedFailCount;
	}
	public long getTxnFailCount() {
		return txnFailCount;
	}
	public void setTxnFailCount(long txnFailCount) {
		this.txnFailCount = txnFailCount;
	}
	public long getSlaFailCount() {
		return slaFailCount;
	}
	public void setSlaFailCount(long slaFailCount) {
		this.slaFailCount = slaFailCount;
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

	@Override
	public String toString() {
		return "txnId : " + txnId
				+ ", passedAllSlas="+passedAllSlas
				+ ", foundSLAforTxnId="+foundSLAforTxnId
				+ ", passed90thResponse="+passed90thResponse
				+ ", txn90thResponse="+txn90thResponse
				+ ", sla90thResponse="+sla90thResponse
				+ ", passed95thResponse="+passed95thResponse
				+ ", txn95thResponse="+txn95thResponse
				+ ", sla95thResponse="+sla95thResponse
				+ ", passed99thResponse="+passed99thResponse
				+ ", txn99thResponse="+txn99thResponse
				+ ", sla99thResponse="+sla99thResponse
				+ ", passedFailPercent="+passedFailPercent
				+ ", txnFailurePercent="+txnFailurePercent
				+ ", slaFailurePercent="+slaFailurePercent
				+ ", passedPassCount="+passedPassCount
				+ ", txnPassCount="+txnPassCount
				+ ", slaPassCount="+slaPassCount
				+ ", slaPassCountVariancePercent="+slaPassCountVariancePercent;
	} 
}
