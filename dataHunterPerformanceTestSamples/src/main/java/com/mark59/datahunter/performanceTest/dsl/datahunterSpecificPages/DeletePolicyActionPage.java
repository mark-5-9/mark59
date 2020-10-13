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

package com.mark59.datahunter.performanceTest.dsl.datahunterSpecificPages;

import org.openqa.selenium.WebDriver;

import com.mark59.seleniumDSL.pageElements.Link;



/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class DeletePolicyActionPage extends _GenericDatatHunterActionPage {
	

	public DeletePolicyActionPage(WebDriver driver) {
		super(driver);
	}

	public Link backLink() {
		return new Link(driver, "Back");
	};		

}