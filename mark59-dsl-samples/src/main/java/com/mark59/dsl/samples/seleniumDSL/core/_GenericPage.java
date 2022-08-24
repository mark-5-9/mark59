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

package com.mark59.dsl.samples.seleniumDSL.core;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;


/**
 * @author Philip Webb
 * Written: Australian Spring 2019
 */
public class _GenericPage {
	
	protected WebDriver driver;
	protected int screenshotCounter = 0;
	
	public _GenericPage(WebDriver driver) {
		this.driver = driver;
	}
	
	public String getPageSource(){
		return driver.getPageSource();
	}

	public String getVisibleTextOnPage(){
		return driver.findElement(By.tagName("body")).getText();  
	}
	
	public boolean doesPageContainText(String text){
		String pageText = driver.findElement(By.tagName("body")).getText();
		return pageText.contains(text);
	}	
	
	public boolean doesPageHtmlContainString(String string){
		String pageSource = getPageSource();
		return pageSource.contains(string);
	}	
	
	public String getPageTitle(){
		return driver.getTitle() ;  
	}

	public void scrollBy(int pxRight, int pxDown){
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollBy( " +  pxRight + ", " + pxDown + ")", "");	
	}

}
