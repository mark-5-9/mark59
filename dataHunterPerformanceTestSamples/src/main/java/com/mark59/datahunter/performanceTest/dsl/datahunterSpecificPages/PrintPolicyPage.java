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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.mark59.seleniumDSL.core._GenericPage;
import com.mark59.seleniumDSL.pageElements.InputTextElement;
import com.mark59.seleniumDSL.pageElements.SubmitBtn;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class PrintPolicyPage extends _GenericPage  {
	
	public PrintPolicyPage( WebDriver driver) {
		super(driver);
	}

	public InputTextElement application() {
		return new InputTextElement(driver, By.id("application"));
	};
	public InputTextElement identifier() {
		return new InputTextElement(driver, By.id("identifier"));
	};	
	public InputTextElement lifecycle() {
		return new InputTextElement(driver, By.id("lifecycle"));
	};
		
	public SubmitBtn submit() {
		return new SubmitBtn(driver, By.id("submit"));
	};		
	
}
