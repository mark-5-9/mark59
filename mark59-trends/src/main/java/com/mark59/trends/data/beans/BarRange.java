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

package com.mark59.trends.data.beans;

import java.math.BigDecimal;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class BarRange {

	String		txnId;	
	BigDecimal	barMin;
	BigDecimal	barMax;	
	
	public BarRange() {
	}

	public BarRange(String txnId, BigDecimal barMin, BigDecimal	barMax) {
		this.txnId  = txnId;
		this.barMin = barMin;
		this.barMax = barMax;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public BigDecimal getBarMin() {
		return barMin;
	}

	public void setBarMin(BigDecimal barMin) {
		this.barMin = barMin;
	}

	public BigDecimal getBarMax() {
		return barMax;
	}

	public void setBarMax(BigDecimal barMax) {
		this.barMax = barMax;
	}

		
}
