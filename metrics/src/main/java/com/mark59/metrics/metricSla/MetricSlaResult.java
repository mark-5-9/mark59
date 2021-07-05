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

package com.mark59.metrics.metricSla;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class MetricSlaResult {
	
	private String txnId;
	private String metricTxnType;
	private String valueDerivation;
	private SlaResultTypeEnum slaResultType;
	private String messageText;		

	
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public MetricSlaResult(String txnId, String metricTxnType,	String valueDerivation, SlaResultTypeEnum slaResultType, String messageText  ) {
		super();
		this.txnId = txnId;
		this.metricTxnType = metricTxnType;
		this.valueDerivation = valueDerivation;
		this.slaResultType = slaResultType;
		this.messageText = messageText;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}
	public String getMetricTxnType() {
		return metricTxnType;
	}
	public void setMetricTxnType(String metricTxnType) {
		this.metricTxnType = metricTxnType;
	}
	public String getValueDerivation() {
		return valueDerivation;
	}
	public void setValueDerivation(String valueDerivation) {
		this.valueDerivation = valueDerivation;
	}
	public SlaResultTypeEnum getSlaResultType() {
		return slaResultType;
	}
	public void setSlaResultType(SlaResultTypeEnum slaResultType) {
		this.slaResultType = slaResultType;
	}

	@Override
	public String toString() {
		String prettyPrint = "txnId="+txnId
				+ ", metricTxnType="+metricTxnType
				+ ", valueDerivation="+valueDerivation
				+ ", slaResultType="+slaResultType.name()
				+ ", messageText="+messageText
				;
		return prettyPrint;
	} 
}
