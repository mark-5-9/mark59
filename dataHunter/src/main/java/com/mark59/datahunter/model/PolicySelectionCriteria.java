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

package com.mark59.datahunter.model;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class PolicySelectionCriteria {

	String	selectClause;
	String	application;
	String	applicationStartsWithOrEquals;	
	String	identifier;
	String	lifecycle;
	String	useability;	
	String	selectOrder;	
	
	public PolicySelectionCriteria() {
		
	}
	
	
	public String getSelectClause() {
		return selectClause;
	}
	
	
	public void setSelectClause(String selectClause) {
		this.selectClause = selectClause;
	}

	public String getApplication() {
		return application;
	}


	public void setApplication(String application) {
		this.application = application;
	}

	
	
	public String getApplicationStartsWithOrEquals() {
		return applicationStartsWithOrEquals;
	}


	public void setApplicationStartsWithOrEquals(String applicationStartsWithOrEquals) {
		this.applicationStartsWithOrEquals = applicationStartsWithOrEquals;
	}


	public String getIdentifier() {
		return identifier;
	}


	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}


	public String getLifecycle() {
		return lifecycle;
	}


	public void setLifecycle(String lifecycle) {
		this.lifecycle = lifecycle;
	}


	public String getUseability() {
		return useability;
	}


	public void setUseability(String useability) {
		this.useability = useability;
	}
	
	


	public String getSelectOrder() {
		return selectOrder;
	}


	public void setSelectOrder(String selectOrder) {
		this.selectOrder = selectOrder;
	}


	@Override
    public String toString() {
        return   "[application= "+ application + 
        		", aStartOrEqr= "+ applicationStartsWithOrEquals + 
        		", identifier= "+ identifier +         		
        		", lifecycle= "+ lifecycle + 
        		", useability= "+ useability + 
        		", selectOrder= "+ selectOrder +         		
        		"]";
	}
		
}
