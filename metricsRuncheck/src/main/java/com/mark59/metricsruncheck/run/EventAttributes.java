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

package com.mark59.metricsruncheck.run;

import com.mark59.metrics.data.beans.EventMapping;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class EventAttributes {

	Integer			eventId;
	String			txnId;
	EventMapping	eventMapping;

	
	public EventAttributes() {
	}
	
	public EventAttributes(Integer eventId, String txnId, EventMapping eventMapping ) {
		this.eventId = eventId;
		this.txnId = txnId;
		this.eventMapping = eventMapping;
	}
	

	public Integer getEventId() {
		return eventId;
	}
	
	public void setEventId(Integer eventId) {
		this.eventId = eventId;
	}

	
	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public EventMapping getEventMapping() {
		return eventMapping;
	}

	public void setEventMapping(EventMapping eventMapping) {
		this.eventMapping = eventMapping;
	}

	
	@Override
    public String toString() {
        return "[EventAttributes txnId= " + txnId +
        		", EVENT eventId= " + eventId + 
        		", txnType= " +	eventMapping.getTxnType() + 
        		", matchWhenLike= "+ eventMapping.getMatchWhenLike() +
        		", isPercentage= "+ eventMapping.getIsPercentage() +
        		", isInvertedPercentage= "+ eventMapping.getIsInvertedPercentage() +
        		", targetNameLB= "+ eventMapping.getTargetNameLB() +
        		", targetNameRB= "+ eventMapping.getTargetNameRB()  +"]";
	}

	
		
}
