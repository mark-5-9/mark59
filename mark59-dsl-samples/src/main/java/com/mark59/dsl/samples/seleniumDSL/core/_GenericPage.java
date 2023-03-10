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

package com.mark59.dsl.samples.seleniumDSL.core;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;


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
	
	/**
	 * Useful wait for a new tab be loaded and accessible in selenium.
	 * <p>Note: This method just uses the default setting for Timeout and Polling when building the Fluent Wait
	 * <p>Sample Usage:
	 * <p><code>
	 * 
	 *   driver.switchTo().newWindow(WindowType.TAB);<br>
	 *   // ( or some page action that causes the application to open a second tab) , then..<br>
     *   currentPage.waitUntilExpectedNumberOfWindowsToBe(2); <br>
     *   List<String> browserTabs = new ArrayList<String> (driver.getWindowHandles());<br>
     *   driver.switchTo().window(browserTabs.get(1));  <br>    
     *   newTabPage.someElementThatShouldBeClickable().waitUntilClickable();<br>
     *   ...
	 * </code>
	 *
	 * @param expectedNumberOfWindows
	 * @return _GenericPage
	 */
	public _GenericPage waitUntilExpectedNumberOfWindowsToBe(int expectedNumberOfWindows){
		FluentWaitFactory.getFluentWait(driver).until(ExpectedConditions.numberOfWindowsToBe(expectedNumberOfWindows));
		return this;
	}
	
}
