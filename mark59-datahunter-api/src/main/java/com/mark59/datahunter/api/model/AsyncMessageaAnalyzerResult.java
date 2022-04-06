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

package com.mark59.datahunter.api.model;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class AsyncMessageaAnalyzerResult extends PolicySelectionCriteria   {

	Long starttm;	
	Long endtm;	
	Long differencetm;
	
	public AsyncMessageaAnalyzerResult() {
		
	}


	public Long getStarttm() {
		return starttm;
	}


	public void setStarttm(Long starttm) {
		this.starttm = starttm;
	}


	public Long getEndtm() {
		return endtm;
	}


	public void setEndtm(Long endtm) {
		this.endtm = endtm;
	}


	public Long getDifferencetm() {
		return differencetm;
	}


	public void setDifferencetm(Long differencetm) {
		this.differencetm = differencetm;
	}


	/* (non-Javadoc)
	 * @see com.pnv.metrics.model.PolicySelectionCriteria#toString()
	 */
	@Override
    public String toString() {
        return  super.toString() + 
        		", starttm= "+ starttm +         	
        		", endtm= "+ endtm +        
        		", differencetm= "+ differencetm +                		
        		"]";
	}
		
		
}
