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
package com.mark59.datahunter.model;


/**
 * This class is specifically designed for the DataHunter UI.
 * <p>CountPoliciesBreakdown (the class it extends) is used within 
 * DataHunter Rest API, and a database results object.    
 * 
 * @author Philip Webb
 * Written: Australian Spring 2023
 */
public class CountPoliciesBreakdownForm extends CountPoliciesBreakdown  {

	String holeStats; 	
	String lookupParmsUrl; 	
	
	public CountPoliciesBreakdownForm() {
	}

	public String getHoleStats() {
		return holeStats;
	}

	public void setHoleStats(String holeStats) {
		this.holeStats = holeStats;
	}

	public String getLookupParmsUrl() {
		return lookupParmsUrl;
	}

	public void setLookupParmsUrl(String lookupParmsUrl) {
		this.lookupParmsUrl = lookupParmsUrl;
	}

	@Override
    public String toString() {
        return  super.toString() + 
        		", holeStats="+ holeStats +         		
        		", lookupParmsUrl="+ lookupParmsUrl +         		
        		"]";
	}
}
