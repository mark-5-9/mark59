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

package com.mark59.datahunter.functionalTest.dsl.pageElements;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class DropdownList {

	private WebDriver driver;
	private By by;

	public DropdownList(WebDriver driver, String id) {
//		selectList = new Select(driver.findElement(By.id(id)));
		this.driver = driver;
		this.by = By.id(id);  
	}

	public DropdownList(WebDriver driver, By by) {
		this.driver = driver;
		this.by = by;
	}	
	
	
	public void selectByVisibleText(String visibleText) {
		Select selectList = new Select(driver.findElement(by));
		selectList.selectByVisibleText(visibleText);
		
		if ( ! visibleText.equals(getSelectedOptionValue(selectList))){ //it when wrong
			throw new RuntimeException( "selectByVisibleText expected " +  visibleText +  " but got " +  getSelectedOptionValue(selectList) + "!");
		}
	}
	
		
	public String getSelectedOptionValue(Select selectList){
		return selectList.getFirstSelectedOption().getAttribute("value");
	}
	
		
	public By getBy() {
		return by;
	}

	public void setBy(By by) {
		this.by = by;
	}	

}
