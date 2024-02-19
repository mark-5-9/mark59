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

package com.mark59.datahunter.samples.dsl.datahunterSpecificPages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.mark59.dsl.samples.seleniumDSL.pageElements.Link;
import com.mark59.dsl.samples.seleniumDSL.pageElements.PageTextElement;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class AddPolicyActionPage  extends _GenericDataHunterActionPage {
	

	public AddPolicyActionPage( WebDriver driver) {
		super(driver);
	}
		
	public PageTextElement application() {
		return new PageTextElement(driver, By.id("application"));
	}
	public PageTextElement identifier() {
		return new PageTextElement(driver, By.id("identifier"));
	}
	public PageTextElement lifecycle() {
		return new PageTextElement(driver, By.id("lifecycle"));
	}
	public PageTextElement useability() {
		return new PageTextElement(driver, By.id("useability"));
	}
	public PageTextElement otherdata() {
		return new PageTextElement(driver, By.id("otherdata"));
	}
	public PageTextElement epochtime() {
		return new PageTextElement(driver, By.id("epochtime"));
	}
	public Link backLink() {
		return new Link(driver, "Back");
	}
}
