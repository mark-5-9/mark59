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

package com.mark59.datahunter.samples.dsl.datahunterSpecificPages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.mark59.dsl.samples.seleniumDSL.pageElements.Link;
import com.mark59.dsl.samples.seleniumDSL.pageElements.PageTextElement;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 * Tricker than appears as it needs to cater for ip-assigned distributed tests 
 */
public class CountPoliciesBreakdownActionPage extends _GenericDataHunterActionPage {
	// private static final Logger LOG = LogManager.getLogger(CountPoliciesBreakdownActionPage.class);	

	public CountPoliciesBreakdownActionPage( WebDriver driver) {
		super(driver);
	}
	
	public int getCountForBreakdown(String application, String lifecycle, String useability){
		String elementCountId = (application + "_" + lifecycle + "_" + useability + "_count").replace(" ", "_").replace(".", "_");
		// LOG.info("elementCountId (" +  Thread.currentThread().getName() + ") : " + elementCountId );
		PageTextElement countForBreakdownElement = new PageTextElement(driver, By.id(elementCountId));
		return Integer.parseInt(countForBreakdownElement.getText());
	}
	
	public Link backLink() {
		return new Link(driver, "Back");
	}
}
