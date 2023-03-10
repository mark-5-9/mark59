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
 * 
 * Describes a 3d point, indented to indicate a single point on the Trend analysis graph
 * run by txnID on the x-y plane, the value on the z axis.   
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class Datapoint {

	String		runTime;
	String		txnId;
	BigDecimal	value;
	
	public Datapoint() {
	}
	
	public Datapoint(String runTime, String txnId, BigDecimal value) {
		super();
		this.runTime = runTime;
		this.txnId = txnId;
		this.value = value;
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
	
	public BigDecimal getValue() {
		return value;
	}
	
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	

	
	@Override
    public String toString() {
        return "[runTime= "+ runTime + ", txnId= "+ txnId + ", value= "+ value  +"]";
	}

	
		
}
