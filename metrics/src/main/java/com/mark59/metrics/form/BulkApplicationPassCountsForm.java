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

package com.mark59.metrics.form;

import java.math.BigDecimal;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class BulkApplicationPassCountsForm {

		String		application;
		String		isTxnIgnored;
		BigDecimal	sla90thResponse;
		boolean		sla90thFromBaseline;
		BigDecimal	sla95thResponse;
		boolean		sla95thFromBaseline;		
		BigDecimal	sla99thResponse;
		boolean		sla99thFromBaseline;		
		Long		slaPassCount;
		BigDecimal	slaPassCountVariancePercent;
		Long		slaFailCount;
		BigDecimal	slaFailPercent;
		BigDecimal	txnDelay;	
		BigDecimal	xtraNum;
		Long		xtraInt;		
		String		slaRefUrl;
		String		slaOriginalTxnId;
		String      applyRefUrlOption;
				
		
		public String getApplication() {
			return application;
		}
		public void setApplication(String application) {
			this.application = application;
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
		public boolean isSla90thFromBaseline() {
			return sla90thFromBaseline;
		}
		public void setSla90thFromBaseline(boolean sla90thFromBaseline) {
			this.sla90thFromBaseline = sla90thFromBaseline;
		}
		public BigDecimal getSla95thResponse() {
			return sla95thResponse;
		}
		public void setSla95thResponse(BigDecimal sla95thResponse) {
			this.sla95thResponse = sla95thResponse;
		}
		public boolean isSla95thFromBaseline() {
			return sla95thFromBaseline;
		}
		public void setSla95thFromBaseline(boolean sla95thFromBaseline) {
			this.sla95thFromBaseline = sla95thFromBaseline;
		}
		public BigDecimal getSla99thResponse() {
			return sla99thResponse;
		}
		public void setSla99thResponse(BigDecimal sla99thResponse) {
			this.sla99thResponse = sla99thResponse;
		}
		public boolean isSla99thFromBaseline() {
			return sla99thFromBaseline;
		}
		public void setSla99thFromBaseline(boolean sla99thFromBaseline) {
			this.sla99thFromBaseline = sla99thFromBaseline;
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
		public void setSlaPassCountVariancePercent(
				BigDecimal slaPassCountVariancePercent) {
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
		public String getSlaOriginalTxnId() {
			return slaOriginalTxnId;
		}
		public void setSlaOriginalTxnId(String slaOriginalTxnId) {
			this.slaOriginalTxnId = slaOriginalTxnId;
		}
		public String getApplyRefUrlOption() {
			return applyRefUrlOption;
		}
		public void setApplyRefUrlOption(String applyRefUrlOption) {
			this.applyRefUrlOption = applyRefUrlOption;
		}
	
		@Override
		public String toString() {
			String prettyPrint = "application="+application
					+ ", isTxnIgnored="+isTxnIgnored		
					+ ", sla90thResponse="+sla90thResponse		
					+ ", sla90thFromBaseline="+sla90thFromBaseline		
					+ ", sla95thResponse="+sla95thResponse		
					+ ", sla95thFromBaseline="+sla95thFromBaseline		
					+ ", sla99thResponse="+sla99thResponse		
					+ ", sla99thFromBaseline="+sla99thFromBaseline		
					+ ", slaPassCountVariancePercent="+slaPassCountVariancePercent		
					+ ", slaFailCount="+slaFailCount		
					+ ", slaFailPercent="+slaFailPercent		
					+ ", isTxnIgnored="+isTxnIgnored		
					+ ", txnDelay="+txnDelay		
					+ ", xtraNum="+xtraNum		
					+ ", xtraInt="+xtraInt		
					+ ", slaRefUrl="+slaRefUrl		
					+ ", slaOriginalTxnId="+slaOriginalTxnId		
					+ ", applyRefUrlOption="+applyRefUrlOption		
					;
			return prettyPrint;
		}
		
}
