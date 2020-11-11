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

package com.mark59.metrics.data.beans;

import java.math.BigDecimal;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class Sla {
	
	String		application;
	String		txnId;
	String		txnIdURLencoded;
	String		isTxnIgnored;	
	BigDecimal	sla90thResponse;
	long		slaPassCount;
	BigDecimal	slaPassCountVariancePercent;
	long		slaFailCount;
	BigDecimal	slaFailPercent;
	String		slaRefUrl;
	String 		comment;	
	String		slaOriginalTxnId;
	
	
	public Sla() {
		super();
	}

	
	public Sla(Sla copy) {
		this.application = copy.application;
		this.txnId = copy.txnId;
		this.txnIdURLencoded  = copy.txnIdURLencoded;
		this.isTxnIgnored = copy.isTxnIgnored;
		this.sla90thResponse = copy.sla90thResponse;
		this.slaFailCount = copy.slaFailCount;
		this.slaFailPercent = copy.slaFailPercent;
		this.slaOriginalTxnId = copy.slaOriginalTxnId;
		this.slaPassCount = copy.slaPassCount;
		this.slaPassCountVariancePercent = copy.slaPassCountVariancePercent;
		this.slaRefUrl = copy.slaRefUrl;
		this.comment = copy.comment;
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
	public long getSlaFailCount() {
		return slaFailCount;
	}
	public void setSlaFailCount(long slaFailCount) {
		this.slaFailCount = slaFailCount;
	}
	public BigDecimal getSlaFailPercent() {
		return slaFailPercent;
	}
	public void setSlaFailPercent(BigDecimal slaFailPercent) {
		this.slaFailPercent = slaFailPercent;
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
	public String getSlaOriginalTxnId() {
		return slaOriginalTxnId;
	}
	public void setSlaOriginalTxnId(String slaOriginalTxnId) {
		this.slaOriginalTxnId = slaOriginalTxnId;
	}

	

}
