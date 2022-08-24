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
public class TransactionRenameForm {
	
	private String  application;
	private String  fromTxnId;
	private String  toTxnId;
	private String	fromIsCdpTxn;	
	private String	toIsCdpTxn;		
	private String  txnType;
	private String  passedValidation;
	private String  validationMsg;
	
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getFromTxnId() {
		return fromTxnId;
	}
	public void setFromTxnId(String fromTxnId) {
		this.fromTxnId = fromTxnId;
	}
	public String getToTxnId() {
		return toTxnId;
	}
	public void setToTxnId(String toTxnId) {
		this.toTxnId = toTxnId;
	}
	public String getFromIsCdpTxn() {
		return fromIsCdpTxn;
	}
	public void setFromIsCdpTxn(String fromIsCdpTxn) {
		this.fromIsCdpTxn = fromIsCdpTxn;
	}
	public String getToIsCdpTxn() {
		return toIsCdpTxn;
	}
	public void setToIsCdpTxn(String toIsCdpTxn) {
		this.toIsCdpTxn = toIsCdpTxn;
	}
	public String getTxnType() {
		return txnType;
	}
	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public String getPassedValidation() {
		return passedValidation;
	}
	public void setPassedValidation(String passedValidation) {
		this.passedValidation = passedValidation;
	}	
	public String getValidationMsg() {
		return validationMsg;
	}
	public void setValidationMsg(String validationMsg) {
		this.validationMsg = validationMsg;
	}

	@Override
	public String toString() {
		String prettyPrint = "application="+application
				+ ", fromTxnId="+fromTxnId
				+ ", toTxnId="+toTxnId
				+ ", fromIsCdpTxn="+fromIsCdpTxn				
				+ ", toIsCdpTxn="+toIsCdpTxn				
				+ ", txnType="+txnType
				+ ", passedValidation="+passedValidation
				+ ", validationMsg="+validationMsg
				;
		return prettyPrint;
	}

}
