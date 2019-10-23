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

import com.mark59.datahunter.functionalTest.dsl.pageElements.PageTextElement;
import com.mark59.seleniumDSL.core._GenericPage;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class SuperDataHunterActionPage extends  _GenericPage {
	
	public PageTextElement sql;
	public PageTextElement sqlResult;
	public PageTextElement rowsAffected;	
	public PageTextElement sqlResultText;	
	
	public SuperDataHunterActionPage(WebDriver driver) {
		super(driver);
		sql 			= new PageTextElement(driver, By.id("sql"));
		sqlResult		= new PageTextElement(driver, By.id("sqlResult")); 		
		rowsAffected	= new PageTextElement(driver, By.id("rowsAffected")); 
		sqlResultText	= new PageTextElement(driver, By.id("sqlResultText")); 		
	}
	

	public String formatResultsMessage(String tag){
		return  "DataHunter " +  sqlResult.getText() + " at " + tag + ": "
				+ ", SQL statement [" + sql.getText() + "]"
				+ ", rows affected [" + rowsAffected.getText() + "]"				
				+ ", details [" + sqlResultText.getText() + "]";		
	}

	
}
