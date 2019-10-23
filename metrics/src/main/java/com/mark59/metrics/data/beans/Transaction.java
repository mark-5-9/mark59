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
public class Transaction {
		
	String		application;
	String		runTime;
	String		txnId;
	String		txnType;
	BigDecimal	txnMinimum;
	BigDecimal	txnAverage;
	BigDecimal	txnMaximum;
	BigDecimal	txnStdDeviation;
	BigDecimal	txn90th;
	long		txnPass;
	long		txnFail;
	long		txnStop;
	BigDecimal	txnFirst;	
	BigDecimal	txnLast;	
	BigDecimal	txnSum;	

	
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
	public BigDecimal getTxnMinimum() {
		return txnMinimum;
	}
	public void setTxnMinimum(BigDecimal txnMinimum) {
		this.txnMinimum = txnMinimum.setScale(3, BigDecimal.ROUND_HALF_UP);
	}
	public BigDecimal getTxnAverage() {
		return txnAverage;
	}
	public void setTxnAverage(BigDecimal txnAverage) {
		this.txnAverage = txnAverage.setScale(3, BigDecimal.ROUND_HALF_UP);
	}
	public BigDecimal getTxnMaximum() {
		return txnMaximum;
	}
	public void setTxnMaximum(BigDecimal txnMaximum) {
		this.txnMaximum = txnMaximum.setScale(3, BigDecimal.ROUND_HALF_UP);
	}
	public BigDecimal getTxnStdDeviation() {
		return txnStdDeviation;
	}
	public void setTxnStdDeviation(BigDecimal txnStdDeviation) {
		this.txnStdDeviation = txnStdDeviation.setScale(3, BigDecimal.ROUND_HALF_UP);
	}
	public BigDecimal getTxn90th() {
		return txn90th;
	}
	public void setTxn90th(BigDecimal txn90th) {
		this.txn90th = txn90th.setScale(3, BigDecimal.ROUND_HALF_UP);
	}
	public long getTxnPass() {
		return txnPass;
	}
	public void setTxnPass(long txnPass) {
		this.txnPass = txnPass;
	}
	public long getTxnFail() {
		return txnFail;
	}
	public void setTxnFail(long txnFail) {
		this.txnFail = txnFail;
	}
	public long getTxnStop() {
		return txnStop;
	}
	public void setTxnStop(long txnStop) {
		this.txnStop = txnStop;
	}
	
	public BigDecimal getTxnFirst() {
		return txnFirst;
	}
	public void setTxnFirst(BigDecimal txnFirst) {
		this.txnFirst = txnFirst.setScale(3, BigDecimal.ROUND_HALF_UP);
	}
	public BigDecimal getTxnLast() {
		return txnLast;
	}
	public void setTxnLast(BigDecimal txnLast) {
		this.txnLast = txnLast.setScale(3, BigDecimal.ROUND_HALF_UP);
	}
		
	public BigDecimal getTxnSum() {
		return txnSum;
	}
	public void setTxnSum(BigDecimal txnSum) {
		this.txnSum = txnSum.setScale(3, BigDecimal.ROUND_HALF_UP);
	}
	
	@Override
	public String toString() {
		String prettyPrint = "transaction : " + application
				+ ", runTime="+runTime
				+ ", txnId="+txnId
//				+ ", txnName="+txnName
				+ ", txnType="+txnType
				+ ", txnMinimum="+txnMinimum
				+ ", txnAverage="+txnAverage
				+ ", txnMaximum="+txnMaximum
				+ ", txn90th="+txn90th
				+ ", txnPass="+txnPass
				+ ", txnFail="+txnFail
				+ ", txnStop="+txnStop
				+ ", txnFirst="+txnFirst
				+ ", txnLast="+txnLast
				+ ", txnSum="+txnSum
				;
		return prettyPrint;
	} 
	
}
