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
 * Written: Australian Spring 2023
 */
public class PolicySelectionFilter extends PolicySelectionCriteria {

	boolean otherdataSelected;	
	String otherdata;
	boolean createdSelected;		
	String createdFrom;
	String createdTo;
	boolean updatedSelected;		
	String updatedFrom;
	String updatedTo;	
	boolean epochtimeSelected; 
	String epochtimeFrom; 
	String epochtimeTo;
	String orderDirection; 	
	String limit; 
	
	public PolicySelectionFilter() {
	}

	public boolean isOtherdataSelected() {
		return otherdataSelected;
	}

	public void setOtherdataSelected(boolean otherdataSelected) {
		this.otherdataSelected = otherdataSelected;
	}

	public String getOtherdata() {
		return otherdata;
	}

	public void setOtherdata(String otherdata) {
		this.otherdata = otherdata;
	}

	public boolean isCreatedSelected() {
		return createdSelected;
	}

	public void setCreatedSelected(boolean createdSelected) {
		this.createdSelected = createdSelected;
	}

	public String getCreatedFrom() {
		return createdFrom;
	}

	public void setCreatedFrom(String createdFrom) {
		this.createdFrom = createdFrom;
	}

	public String getCreatedTo() {
		return createdTo;
	}

	public void setCreatedTo(String createdTo) {
		this.createdTo = createdTo;
	}

	public boolean isUpdatedSelected() {
		return updatedSelected;
	}

	public void setUpdatedSelected(boolean updatedSelected) {
		this.updatedSelected = updatedSelected;
	}

	public String getUpdatedFrom() {
		return updatedFrom;
	}

	public void setUpdatedFrom(String updatedFrom) {
		this.updatedFrom = updatedFrom;
	}

	public String getUpdatedTo() {
		return updatedTo;
	}

	public void setUpdatedTo(String updatedTo) {
		this.updatedTo = updatedTo;
	}

	public boolean isEpochtimeSelected() {
		return epochtimeSelected;
	}

	public void setEpochtimeSelected(boolean epochtimeSelected) {
		this.epochtimeSelected = epochtimeSelected;
	}

	public String getEpochtimeFrom() {
		return epochtimeFrom;
	}

	public void setEpochtimeFrom(String epochtimeFrom) {
		this.epochtimeFrom = epochtimeFrom;
	}

	public String getEpochtimeTo() {
		return epochtimeTo;
	}

	public void setEpochtimeTo(String epochtimeTo) {
		this.epochtimeTo = epochtimeTo;
	}
	
	public String getOrderDirection() {
		return orderDirection;
	}

	public void setOrderDirection(String orderDirection) {
		this.orderDirection = orderDirection;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}


	@Override
    public String toString() {
        return  super.toString() + 
        		", otherdataSelected="+ otherdataSelected +  
        		", otherdata="+ otherdata +  
        		", createdSelected="+ createdSelected +  
        		", createdFrom="+ createdFrom +  
        		", createdTo="+ createdTo +  
        		", updatedSelected="+ updatedSelected +  
        		", updatedFrom="+ updatedFrom +  
        		", updatedTo="+ updatedTo +  
        		", epochtimeSelected="+ epochtimeSelected +  
        		", epochtimeFrom="+ epochtimeFrom +  
        		", epochtimeTo="+ epochtimeTo +  
        		", orderDirection="+ orderDirection +  
        		", limit="+ limit +  
        		"]";
	}
		
}
