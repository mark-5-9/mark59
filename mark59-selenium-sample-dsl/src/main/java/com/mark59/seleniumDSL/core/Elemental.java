package com.mark59.seleniumDSL.core;

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

import java.time.Duration;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * Useful class that implements a number of commonly used interactions with Selenium WebElement objects, with a particular
 * focus on 'wait' conditions 
 *
 * @author Philip Webb
 * @author Michael Cohen
 * Written: Australian Spring 2019
 */
public class Elemental {

	private static final Logger LOG = Logger.getLogger(Elemental.class);	
	
	protected WebDriver driver;
	protected By by;
	protected Duration timeout;
	protected Duration pollingFrequency;
	protected boolean waitCondionsDebugMode;

	
	public Elemental(WebDriver driver, By by, Duration timeout, Duration pollingFrequency) {
		this(driver, by, timeout, pollingFrequency, false); 
	}

	public Elemental(WebDriver driver, By by, Duration timeout, Duration pollingFrequency, boolean waitCondionsDebugMode) {
		this.driver = driver;
		this.by = by;
		this.timeout = timeout;
		this.pollingFrequency = pollingFrequency;
		this.waitCondionsDebugMode = waitCondionsDebugMode;
	}

	
	public void waitUntilCondition(ExpectedCondition<?> condition) {
		
		if (!waitCondionsDebugMode) {
			FluentWaitFactory.getFluentWait(driver, timeout, pollingFrequency).until(condition);
		} else {
			runWaitUntilConditonsDebugMode(condition);  
		}
	}
	
	
	protected WebElement waitForAndFindElement() {
		return waitUntilConditionReturnsWebElement(ExpectedConditions.elementToBeClickable(by));
	}

	protected List<WebElement> waitForAndFindElements() {
		return waitUntilConditionReturnsWebElements(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
	}
	
	
	
	protected Elemental click() {
		waitForAndFindElement().click();
		return this;
	}
	
	protected Elemental waitUntilClickable() {
		waitUntilCondition(ExpectedConditions.elementToBeClickable(by));
		return this;
	}
	
	
	protected Elemental waitUntilClickable(Elemental elemental) {
		waitUntilCondition(ExpectedConditions.elementToBeClickable(elemental.getBy()));
		return  this;
	}
	
	
	protected Elemental waitUntilStale() {
		return waitUntilStale(this);
	}
	

	protected Elemental waitUntilStale(Elemental elemental) {
		waitUntilCondition(ExpectedConditions.stalenessOf(driver.findElement(elemental.getBy())));
		return this;
	}
	
	

	protected Elemental waitUntilTextPresentInElement(Elemental elemental, String expectedText) {
		waitUntilCondition(ExpectedConditions.textToBePresentInElementLocated(elemental.getBy() , expectedText));
		return this;
	}


	
	protected WebElement waitUntilConditionReturnsWebElement(ExpectedCondition<WebElement> condition) {
		
		if (!waitCondionsDebugMode) {
			return FluentWaitFactory.getFluentWait(driver, timeout, pollingFrequency).until(condition);
		} else {
			return runWaitUntilConditonsDebugMode(condition);  
		}

	}
	
	
	protected List<WebElement> waitUntilConditionReturnsWebElements(ExpectedCondition<List<WebElement>> condition) {
		return FluentWaitFactory.getFluentWait(driver, timeout, pollingFrequency).until(condition);
	}
	
	
	
	private WebElement runWaitUntilConditonsDebugMode(ExpectedCondition<?> condition) {
		
		int webdriverTimeOutInSeconds = new Long( pollingFrequency.getSeconds() ).intValue() + 1;          //will generally get set to 1 
		int numberOfAttempts = new Long( timeout.getSeconds() / webdriverTimeOutInSeconds ).intValue();  
		
		for (int i = 1; i <= numberOfAttempts; i++ ) {
			try {
				if (i>1){ 
					LOG.debug( "re-attempt (try #" + i + " of " + numberOfAttempts + ") timeout : " + webdriverTimeOutInSeconds + "s" );
				}	
				WebDriverWait webDriverWait = new WebDriverWait(driver, webdriverTimeOutInSeconds);
				Object returnedFromWaitForCondition =   webDriverWait.until(condition);
				
				if (returnedFromWaitForCondition instanceof WebElement) {
					return (WebElement) returnedFromWaitForCondition;
				}
				return null;

			} catch (Exception e) {
				LOG.debug(" condition for " + condition.getClass() + " not met : " + e.getMessage() );
			}
		}  
		throw new RuntimeException("** Exhausted all attempts - forcing a failure. ");
	}

	public Elemental thenSleep() {
		thenSleep(1000);
		return this;
	}
	
	public Elemental thenSleep(long sleepDuration){
		SafeSleep.sleep(sleepDuration);
		return this;
	}
	
	public By getBy() {
		return by;
	}

}
