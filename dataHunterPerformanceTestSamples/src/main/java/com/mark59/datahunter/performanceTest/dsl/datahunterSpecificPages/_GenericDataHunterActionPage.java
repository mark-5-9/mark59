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
import com.mark59.seleniumDSL.pageElements.PageTextElement;


/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class _GenericDataHunterActionPage extends  _GenericPage {
	
	public _GenericDataHunterActionPage(WebDriver driver) {
		super(driver);
	}

	public PageTextElement sql() {
		return new PageTextElement(driver, By.id("sql"));
	}
	public PageTextElement sqlResult() {
		return new PageTextElement(driver, By.id("sqlResult"));
	}
	public PageTextElement rowsAffected() {
		return new PageTextElement(driver, By.id("rowsAffected"));
	}
	public PageTextElement sqlResultText() {
		return new PageTextElement(driver, By.id("sqlResultText"));
	}


	public String formatResultsMessage(String tag){
		return  "DataHunter " +  sqlResultText().getText() + " at " + tag + ": "
				+ ", SQL statement [" + sql().getText() + "]"
				+ ", rows affected [" +  rowsAffected().getText() + "]"				
				+ ", details [" + sqlResultText().getText() + "]";		
	}

	
}
