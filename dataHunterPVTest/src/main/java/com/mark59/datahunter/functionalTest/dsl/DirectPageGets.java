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

package com.mark59.datahunter.functionalTest.dsl;

import org.openqa.selenium.WebDriver;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class DirectPageGets {

	WebDriver driver;
	String dataHunterUrlHostPort; 
	String application;
	
	public DirectPageGets( WebDriver driver, String dataHunterUrlHostPort, String application ) {
		this.driver = driver;
		this.dataHunterUrlHostPort = dataHunterUrlHostPort;
		this.application = application;
	}

	public void gotoAddPolicyPage() {
		driver.get(dataHunterUrlHostPort + DslConstants.ADD_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + application);
	}
	
	public void gotoCountPoliciesBreakdownPage() {
		driver.get(dataHunterUrlHostPort + DslConstants.COUNT_POLICIES_BREAKDOWN_URL_PATH + DslConstants.URL_PARM_APPLICATION + application);
	}
	
	public void gotoCountPoliciesPage() {
		driver.get(dataHunterUrlHostPort + DslConstants.COUNT_POLICIES_URL_PATH + DslConstants.URL_PARM_APPLICATION + application);
	}
	
	public void gotoDeleteMultiplePoliciesPage() {
		driver.get(dataHunterUrlHostPort + DslConstants.DELETE_MULTIPLE_POLICIES_URL_PATH + DslConstants.URL_PARM_APPLICATION + application);
	}

	public void gotoDeletePolicyPage() {
		driver.get(dataHunterUrlHostPort + DslConstants.DELETE_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + application);
	}

	public void gotoNextPolicyPage(String useOrLookup) {
		driver.get(dataHunterUrlHostPort + DslConstants.NEXT_POLICY_URL_PATH + DslConstants.URL_PARM_APPLICATION + application + DslConstants.URL_PARM_USE_OR_LOOKUP + useOrLookup);
	}

	
}
