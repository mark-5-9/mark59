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

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.mark59.core.utils.SafeSleep;


/**
 * Useful class that implements a number of commonly used interactions with Selenium WebElement objects.
 * Similar to Elemental, but uses a hand-crafted version of Selenium's FluentWait that allows for  
 * variation in polling intervals when checking if a condition (eg element is clickable) has been met.   
 *
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class ElementalUsingVariablePolling {
	
	protected WebDriver driver;
	protected By by;
	protected Duration timeout;
	protected List<Long> pollingFreqsMs;
	
	public ElementalUsingVariablePolling(WebDriver driver, By by, Duration timeout, List<Long> pollingFreqsMs) {
		this.driver = driver;
		this.by = by;
		this.timeout = timeout;
		this.pollingFreqsMs = pollingFreqsMs;
	}

	public ElementalUsingVariablePolling(WebDriver driver, By by, Duration timeout) {
		this.driver = driver;
		this.by = by;
		this.timeout = timeout;
		this.pollingFreqsMs = FluentWaitFactory.DEFAULT_VARIABLE_POLLING;
	}


	protected WebElement waitForAndFindElement() {
		return waitUntilConditionReturnsWebElement(ExpectedConditions.elementToBeClickable(by));
	}

	protected List<WebElement> waitForAndFindElements() {
		return waitUntilConditionReturnsWebElements(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
	}
		
	public ElementalUsingVariablePolling click() {
		WebElement webElement = waitForAndFindElement(); 
		if (webElement != null ) {
			webElement.click();
		}
		return this;
	}
	
	public ElementalUsingVariablePolling scrollIntoViewThenClick() {
		WebElement element = waitForAndFindElement();
	    JavascriptExecutor js = (JavascriptExecutor) driver;  
	    js.executeScript("arguments[0].scrollIntoView(true);", element);
	    element.click();
		return this;
	}
	
	public ElementalUsingVariablePolling scrollToCentreThenClick() {
		WebElement element = waitForAndFindElement();
	    JavascriptExecutor js = (JavascriptExecutor) driver;  
	    js.executeScript("arguments[0].scrollIntoView({block: \"center\"});", element);
	    element.click();
		return this;
	}
	
	public ElementalUsingVariablePolling waitUntilClickable() {
		waitUntilCondition(ExpectedConditions.elementToBeClickable(by));
		return this;
	}
	
	public ElementalUsingVariablePolling waitUntilClickable(ElementalUsingVariablePolling elemental) {
		waitUntilCondition(ExpectedConditions.elementToBeClickable(elemental.getBy()));
		return  this;
	}

	public ElementalUsingVariablePolling waitUntilStale() {
		return waitUntilStale(this);
	}

	public ElementalUsingVariablePolling waitUntilStale(ElementalUsingVariablePolling elemental) {
		waitUntilCondition(ExpectedConditions.stalenessOf(driver.findElement(elemental.getBy())));
		return this;
	}
	
	public ElementalUsingVariablePolling waitUntilTextPresentInElement(ElementalUsingVariablePolling elemental, String expectedText) {
		waitUntilCondition(ExpectedConditions.textToBePresentInElementLocated(elemental.getBy() , expectedText));
		return this;
	}
	
	public void waitUntilTextPresentInTitle(String expectedText) {
		waitUntilCondition(ExpectedConditions.titleIs(expectedText));  
	}

	public void waitUntilCondition(ExpectedCondition<?> condition) {
		FluentWaitFactory.getFluentWaitVariablePolling(driver, timeout, pollingFreqsMs).until(condition);
	}
	
	public WebElement waitUntilConditionReturnsWebElement(ExpectedCondition<WebElement> condition) {
		return FluentWaitFactory.getFluentWaitVariablePolling(driver, timeout, pollingFreqsMs).until(condition);
	}
	
	public List<WebElement> waitUntilConditionReturnsWebElements(ExpectedCondition<List<WebElement>> condition) {
		return FluentWaitFactory.getFluentWaitVariablePolling(driver, timeout, pollingFreqsMs).until(condition);
	}

	public ElementalUsingVariablePolling thenSleep() {
		thenSleep(1000);
		return this;
	}
	
	public ElementalUsingVariablePolling thenSleep(long sleepDuration){
		SafeSleep.sleep(sleepDuration);
		return this;
	}
	
	public By getBy() {
		return by;
	}

}
