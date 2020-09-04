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
public class TestTransaction {
	
	
	String		application;
	String		runTime;
	String		txnId;
	String		txnType;
	BigDecimal	txnResult;
	String		txnPassed;
	String		txnEpochTime;
	

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getRunTime() {
		return runTime;
	}

	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public BigDecimal getTxnResult() {
		return txnResult;
	}

	public void setTxnResult(BigDecimal txnResult) {
		this.txnResult = txnResult;
	}

	public String getTxnPassed() {
		return txnPassed;
	}

	public void setTxnPassed(String txnPassed) {
		this.txnPassed = txnPassed;
	}

	public String getTxnEpochTime() {
		return txnEpochTime;
	}

	public void setTxnEpochTime(String txnEpochTime) {
		this.txnEpochTime = txnEpochTime;
	}

	
	@Override
	public String toString() {
		String prettyPrint = "transaction : " + application
				+ ", runTime="+runTime
				+ ", txnId="+txnId
				+ ", txnType="+txnType
				+ ", txnResult="+txnResult
				+ ", txnPassed="+txnPassed	
				+ ", txnEpochTime="+txnEpochTime						
				;
		return prettyPrint;
	} 
	
}
