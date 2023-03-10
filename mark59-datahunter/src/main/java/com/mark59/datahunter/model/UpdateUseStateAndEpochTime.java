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
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class UpdateUseStateAndEpochTime extends PolicySelectionCriteria   {

	String toUseability;	
	Long   toEpochTime;	
	
	public UpdateUseStateAndEpochTime() {
		
	}


	public String getToUseability() {
		return toUseability;
	}

	public void setToUseability(String toUseability) {
		this.toUseability = toUseability;
	}


	public Long getToEpochTime() {
		return toEpochTime;
	}

	public void setToEpochTime(Long toEpochTime) {
		this.toEpochTime = toEpochTime;
	}



	@Override
    public String toString() {
        return  super.toString() + 
        		", toUseability="+ toUseability +  
        		", toEpochTime="+ toEpochTime +               		
        		"]";
	}
		
		
}
