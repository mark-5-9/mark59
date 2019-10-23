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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.mark59.datahunter.functionalTest.dsl.pageElements.Link;
import com.mark59.datahunter.functionalTest.dsl.pageElements.PageTextElement;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class NextPolicyActionPage extends SuperDataHunterActionPage {
	
	public PageTextElement application;	
	public PageTextElement identifier;	
	public PageTextElement lifecycle;	
	public PageTextElement useability;
	public PageTextElement otherdata;	
	public PageTextElement created;	
	public PageTextElement updated;		
	public PageTextElement epochtime;
	
	public Link backLink;

	
	public NextPolicyActionPage( WebDriver driver) {
		super(driver);
		
		application		= new PageTextElement(driver, By.id("application")); 				
		identifier		= new PageTextElement(driver, By.id("identifier")); 			
		lifecycle		= new PageTextElement(driver, By.id("lifecycle")); 			
		useability		= new PageTextElement(driver, By.id("useability")); 	
		otherdata		= new PageTextElement(driver, By.id("otherdata")); 	
		created			= new PageTextElement(driver, By.id("created")); 		
		updated			= new PageTextElement(driver, By.id("updated")); 				
		epochtime		= new PageTextElement(driver, By.id("epochtime")); 			
		
		backLink 		= new  Link(driver, "Back");
	}
	
			

}
