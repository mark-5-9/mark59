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

package com.mark59.datahunter.functionalTest.dsl.pages;

import org.openqa.selenium.WebDriver;

import com.mark59.datahunter.functionalTest.dsl.pageElements.DropdownList;
import com.mark59.datahunter.functionalTest.dsl.pageElements.InputTextElement;
import com.mark59.datahunter.functionalTest.dsl.pageElements.Link;
import com.mark59.datahunter.functionalTest.dsl.pageElements.SubmitBtn;
import com.mark59.seleniumDSL.core._GenericPage;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class AddPolicyPage extends _GenericPage  {
	
	public  InputTextElement application;
	public  InputTextElement identifier;	
	public  InputTextElement lifecycle;		
	public  DropdownList     useability;	
	public  InputTextElement otherdata;
	public  InputTextElement epochtime;			
	public  SubmitBtn submit;
	public  Link homePageLink;
	
	public  boolean waitConditionsDebugMode;


	public AddPolicyPage(WebDriver driver) {
		this(driver, false);
	}
	
	public AddPolicyPage( WebDriver driver, boolean waitConditionsDebugMode ) {
		super(driver);
		application = new InputTextElement(driver, 	"application");
		identifier  = new InputTextElement(driver, 	"identifier");
		lifecycle   = new InputTextElement(driver, 	"lifecycle");
		useability  = new DropdownList(driver, 		"useability");    
		otherdata   = new InputTextElement(driver, 	"otherdata");
		epochtime	= new InputTextElement(driver, 	"epochtime");				
		submit   	= new SubmitBtn(driver, "submit");	
		homePageLink= new Link(driver, "Home Page");
		
		this.waitConditionsDebugMode = waitConditionsDebugMode; 
	}

	
	
	
}
