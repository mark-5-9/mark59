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

package com.mark59.trends.data.beans;

import java.math.BigDecimal;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class Sla {
	
	String		application;
	String		txnId;
	String		txnIdURLencoded;
	String		isCdpTxn;
	String		isTxnIgnored;	
	BigDecimal	sla90thResponse;
	BigDecimal	sla95thResponse;
	BigDecimal	sla99thResponse;
	Long		slaPassCount;
	BigDecimal	slaPassCountVariancePercent;
	Long		slaFailCount;
	BigDecimal	slaFailPercent;
	BigDecimal	txnDelay;	
	BigDecimal	xtraNum;
	Long		xtraInt;	
	String		slaRefUrl;
	String 		comment;
	String		isActive;		
	String		slaOriginalTxnId;
	
	
	public Sla() {
		super();
	}

	
	public Sla(Sla copy) {
		this.application = copy.application;
		this.txnId = copy.txnId;
		this.txnIdURLencoded  = copy.txnIdURLencoded;
		this.isCdpTxn = copy.isCdpTxn;
		this.isTxnIgnored = copy.isTxnIgnored;
		this.sla90thResponse = copy.sla90thResponse;
		this.sla95thResponse = copy.sla95thResponse;
		this.sla99thResponse = copy.sla99thResponse;
		this.slaPassCount = copy.slaPassCount;
		this.slaPassCountVariancePercent = copy.slaPassCountVariancePercent;
		this.slaFailCount = copy.slaFailCount;
		this.slaFailPercent = copy.slaFailPercent;
		this.txnDelay = copy.txnDelay;
		this.xtraNum = copy.xtraNum;
		this.xtraInt = copy.xtraInt;
		this.slaRefUrl = copy.slaRefUrl;
		this.comment = copy.comment;
		this.isActive = copy.isActive;
		this.slaOriginalTxnId = copy.slaOriginalTxnId;
	}


	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getTxnIdURLencoded() {
		return txnIdURLencoded;
	}
	public void setTxnIdURLencoded(String txnIdURLencoded) {
		this.txnIdURLencoded = txnIdURLencoded;
	}
	public String getIsCdpTxn() {
		return isCdpTxn;
	}
	public void setIsCdpTxn(String isCdpTxn) {
		this.isCdpTxn = isCdpTxn;
	}
	public String getIsTxnIgnored() {
		return isTxnIgnored;
	}
	public void setIsTxnIgnored(String isTxnIgnored) {
		this.isTxnIgnored = isTxnIgnored;
	}
	public BigDecimal getSla90thResponse() {
		return sla90thResponse;
	}
	public void setSla90thResponse(BigDecimal sla90thResponse) {
		this.sla90thResponse = sla90thResponse;
	}
	public BigDecimal getSla95thResponse() {
		return sla95thResponse;
	}
	public void setSla95thResponse(BigDecimal sla95thResponse) {
		this.sla95thResponse = sla95thResponse;
	}
	public BigDecimal getSla99thResponse() {
		return sla99thResponse;
	}
	public void setSla99thResponse(BigDecimal sla99thResponse) {
		this.sla99thResponse = sla99thResponse;
	}
	public Long getSlaPassCount() {
		return slaPassCount;
	}
	public void setSlaPassCount(Long slaPassCount) {
		this.slaPassCount = slaPassCount;
	}
	public BigDecimal getSlaPassCountVariancePercent() {
		return slaPassCountVariancePercent;
	}
	public void setSlaPassCountVariancePercent(BigDecimal slaPassCountVariancePercent) {
		this.slaPassCountVariancePercent = slaPassCountVariancePercent;
	}	
	public Long getSlaFailCount() {
		return slaFailCount;
	}
	public void setSlaFailCount(Long slaFailCount) {
		this.slaFailCount = slaFailCount;
	}
	public BigDecimal getSlaFailPercent() {
		return slaFailPercent;
	}
	public void setSlaFailPercent(BigDecimal slaFailPercent) {
		this.slaFailPercent = slaFailPercent;
	}
	public BigDecimal getTxnDelay() {
		return txnDelay;
	}
	public void setTxnDelay(BigDecimal txnDelay) {
		this.txnDelay = txnDelay;
	}
	public BigDecimal getXtraNum() {
		return xtraNum;
	}
	public void setXtraNum(BigDecimal xtraNum) {
		this.xtraNum = xtraNum;
	}
	public Long getXtraInt() {
		return xtraInt;
	}
	public void setXtraInt(Long xtraInt) {
		this.xtraInt = xtraInt;
	}
	public String getSlaRefUrl() {
		return slaRefUrl;
	}
	public void setSlaRefUrl(String slaRefUrl) {
		this.slaRefUrl = slaRefUrl;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}
	public String getSlaOriginalTxnId() {
		return slaOriginalTxnId;
	}
	public void setSlaOriginalTxnId(String slaOriginalTxnId) {
		this.slaOriginalTxnId = slaOriginalTxnId;
	}

}
