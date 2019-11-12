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


/**
 * Useful class that implements a number of commonly used interactions with Selenium WebElement objects
 *
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class Elemental {

	private static final Logger LOG = Logger.getLogger(Elemental.class);	
	
	protected WebDriver driver;
	protected By by;
	protected Duration timeout;
	protected Duration pollingFrequency;

	
	public Elemental(WebDriver driver, By by, Duration timeout, Duration pollingFrequency) {
		this.driver = driver;
		this.by = by;
		this.timeout = timeout;
		this.pollingFrequency = pollingFrequency;
	}

	
	public void waitUntilCondition(ExpectedCondition<?> condition) {
		
		if (!LOG.isDebugEnabled() ) {			
			FluentWaitFactory.getFluentWait(driver, timeout, pollingFrequency).until(condition);
		} else {
			runUsingFluentWaitInDebugMode(condition);  
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
		
		if (!LOG.isDebugEnabled()) {	
			return FluentWaitFactory.getFluentWait(driver, timeout, pollingFrequency).until(condition);
		} else {
			return runUsingFluentWaitInDebugMode(condition);  
		}

	}
	
	
	protected List<WebElement> waitUntilConditionReturnsWebElements(ExpectedCondition<List<WebElement>> condition) {
		return FluentWaitFactory.getFluentWait(driver, timeout, pollingFrequency).until(condition);
	}
	
	
	
	/**
	 * Intended to be used for debugging. Approximately simulates the standard fluent wait obtained from the {@link FluentWaitFactory}, providing debug messaging.
	 * 
	 * <p>It invokes a 'debug' fluent wait, which has no 'ignoring' conditions attached.  Therefore on any failure the fluent wait will throw an exception and 
	 * return. The precise reason of the failure of the failure can then be reported, as opposed to the standard fluent wait method (most exception conditions
	 * get "ignored", so you are not able to see the reason(s) for re-polling until the duration is exceeded and timeout exception and stack trace printed). 
	 * 
	 * <p>Re-tries are controlled directly via a sleep call, instead of by the polling frequency used in the standard fluent wait method.  To avoid excessive re-try output,
	 * the sleep between re-tries is set to the polling frequency plus one second.  For example, if the polling frequency has been set to 200ms, the re-try wait will be 1200ms. 
	 *  
	 * <p>It divides the total time allowed (duration), by the sleep wait time to calculate the total number of retries that will be attempted, so the call gets roughly
	 * as much time to succeed as it would if not in debug mode.
	 * 
	 * @see  FluentWaitFactory#getFluentWaitDebugMode(WebDriver, Duration, Duration) 
	 * @param condition
	 * @return WebElement or null
	 */
	protected WebElement runUsingFluentWaitInDebugMode(ExpectedCondition<?> condition) {
		
		long debugPollingFrequency = pollingFrequency.toMillis() + 1000L;                                                //will generally get set to a bit over 1 seconds 
		int numberOfAttempts = new Long( timeout.getSeconds() * 1000L / debugPollingFrequency ).intValue();  
		
		for (int i = 1; i <= numberOfAttempts; i++ ) {
			try {
			
				if (i>1){ 
					LOG.debug(" re-attempt (try #" + i + " of " + numberOfAttempts + "), retrying every " + debugPollingFrequency + "ms."   );
				}
				
				Object returnedFromWaitForCondition = FluentWaitFactory.getFluentWaitDebugMode(driver,Duration.ofSeconds(0), Duration.ofSeconds(0)).until(condition);
				
				if (returnedFromWaitForCondition instanceof WebElement) {
					return (WebElement) returnedFromWaitForCondition;
				}
				return null;

			} catch (Exception e) {
				LOG.debug(" condition for " + condition.getClass() + " not met : " + e.getMessage().replaceAll("\\R+", " ") );
				SafeSleep.sleep(debugPollingFrequency);    // on failure, force a wait for re-tries 
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
