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
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.InvalidElementStateException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.FluentWait;

/**
 * Helper factory to centralise and standardise the ignored exceptions of the
 * returned FluentWait object
 * 
 * @see org.openqa.selenium.support.ui.FluentWait
 * @author Michael Cohen
 * Written: Australian Winter 2019  
 */
public class FluentWaitFactory {

	public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);
	public static final Duration DEFAULT_POLLING = Duration.ofMillis(200);
	
	/**
	 * A list of polling interval times (milliseconds) intended to be used in the custom class {@link FluentWaitVariablePolling},
	 * which iterates over the list to set intervals between the condition test. Once the last entry on the list is reached, 
	 * all subsequent intervals will be set to that value.   
	 */
	public static final List<Long> DEFAULT_VARIABLE_POLLING = Arrays.asList(10L, 100L, 250L, 400L, 600L, 600L, 800L);


	private FluentWaitFactory() {

	}

	
	/**
	 * Returns a new FluentWait object pre-configured to ignore the following
	 * exceptions:<br>
	 * 
	 * <ul>
	 * <li>StaleElementReferenceException</li>
	 * <li>NoSuchElementException</li>
	 * <li>ElementClickInterceptedException</li>
	 * <li>ElementNotInteractableException</li>
	 * <li>InvalidElementStateException</li>
	 * <li>WebDriverException</li>
	 * </ul>
	 * 
	 * @param driver webdriver
	 * @param timeout secs
	 * @param pollingFrequency a Duration
	 * @return a fluent wait
	 */
	public static FluentWait<WebDriver> getFluentWait(WebDriver driver, Duration timeout, Duration pollingFrequency) {
		return new FluentWait<>(driver).withTimeout(timeout).pollingEvery(pollingFrequency)
				.ignoring(StaleElementReferenceException.class)
				.ignoring(NoSuchElementException.class)
				.ignoring(ElementClickInterceptedException.class)
				.ignoring(ElementNotInteractableException.class)
				.ignoring(InvalidElementStateException.class)
				.ignoring(WebDriverException.class);
	}

	
	/**
	 * Returns a new FluentWait object pre-configured with a polling Duration
	 * defined in FluentWaitFactory.DEFAULT_POLLING, and to ignore the following
	 * exceptions:<br>
	 * 
	 * <ul>
	 * <li>StaleElementReferenceException</li>
	 * <li>NoSuchElementException</li>
	 * <li>ElementClickInterceptedException</li>
	 * <li>ElementNotInteractableException</li>
	 * <li>InvalidElementStateException</li>
	 * <li>WebDriverException</li>
	 * </ul>
	 * 
	 * @param driver webdriver
	 * @param timeout secs
	 * @return a fluent wait
	 */
	public static FluentWait<WebDriver> getFluentWait(WebDriver driver, Duration timeout) {
		return getFluentWait(driver, timeout, DEFAULT_POLLING);
	}

	
	/**
	 * Returns a new FluentWait object pre-configured with a polling Duration
	 * defined in FluentWaitFactory.DEFAULT_POLLING, and a timeout Duration defined
	 * in FluentWaitFactory.DEFAULT_TIMEOUT, and to ignore the following
	 * exceptions:<br>
	 * 
	 * <ul>
	 * <li>StaleElementReferenceException</li>
	 * <li>NoSuchElementException</li>
	 * <li>ElementClickInterceptedException</li>
	 * <li>ElementNotInteractableException</li>
	 * <li>InvalidElementStateException</li>
	 * <li>WebDriverException</li>
	 * </ul>
	 * 
	 * @param driver webdriver
	 * @return a fluent wait
	 */
	public static FluentWait<WebDriver> getFluentWait(WebDriver driver) {
		return getFluentWait(driver, DEFAULT_TIMEOUT, DEFAULT_POLLING);
	}
	
	
	/**
	 * Returns a new FluentWaitVariablePolling object pre-configured to ignore the following
	 * exceptions:<br>
	 * 
	 * <ul>
	 * <li>StaleElementReferenceException</li>
	 * <li>NoSuchElementException</li>
	 * <li>ElementClickInterceptedException</li>
	 * <li>ElementNotInteractableException</li>
	 * <li>InvalidElementStateException</li>
	 * <li>WebDriverException</li>
	 * </ul>
	 * 
	 * @param driver webdriver
	 * @param timeout secs
	 * @param pollingFreqsMs a list of polling frequencies (milliseconds)
	 * @return a FluentWaitVariablePolling
	 */
	public static FluentWaitVariablePolling<WebDriver> getFluentWaitVariablePolling(WebDriver driver, Duration timeout,
			List<Long> pollingFreqsMs) {
		return new FluentWaitVariablePolling<>(driver).withTimeout(timeout).withPollingFrequencies(pollingFreqsMs)
				.ignoring(StaleElementReferenceException.class)
				.ignoring(NoSuchElementException.class)
				.ignoring(ElementClickInterceptedException.class)
				.ignoring(ElementNotInteractableException.class)
				.ignoring(InvalidElementStateException.class)
				.ignoring(WebDriverException.class);
	}
	
	/**
	 * Returns a new FluentWaitVariablePolling object pre-configured with a polling frequencies
	 * defined in {@link #DEFAULT_VARIABLE_POLLING}, and to ignore the following
	 * exceptions:<br>
	 * 
	 * <ul>
	 * <li>StaleElementReferenceException</li>
	 * <li>NoSuchElementException</li>
	 * <li>ElementClickInterceptedException</li>
	 * <li>ElementNotInteractableException</li>
	 * <li>InvalidElementStateException</li>
	 * <li>WebDriverException</li>
	 * </ul>
	 * 
	 * @param driver webdriver
	 * @param timeout secs
	 * @return a FluentWaitVariablePolling
	 */
	public static FluentWaitVariablePolling<WebDriver> getFluentWaitVariablePolling(WebDriver driver, Duration timeout) {
		return getFluentWaitVariablePolling(driver, timeout, DEFAULT_VARIABLE_POLLING);
	}

	
	/**
	 * Returns a new FluentWaitVariablePolling object pre-configured with a polling frequencies
	 * defined in {@link #DEFAULT_VARIABLE_POLLING}, and a timeout Duration defined
	 * in {@link #DEFAULT_TIMEOUT}, and to ignore the following
	 * exceptions:<br>
	 * 
	 * <ul>
	 * <li>StaleElementReferenceException</li>
	 * <li>NoSuchElementException</li>
	 * <li>ElementClickInterceptedException</li>
	 * <li>ElementNotInteractableException</li>
	 * <li>InvalidElementStateException</li>
	 * <li>WebDriverException</li>
	 * </ul>
	 * 
	 * @param driver webdriver
	 * @return a FluentWaitVariablePolling
	 */
	public static FluentWaitVariablePolling<WebDriver> getFluentWaitVariablePolling(WebDriver driver) {
		return getFluentWaitVariablePolling(driver, DEFAULT_TIMEOUT, DEFAULT_VARIABLE_POLLING);
	}

	
	/**
	 * Returns a new FluentWait object witch does NOT ignore exceptions. Intended for use when debugging.   
	 * The standard {@link #getFluentWait(WebDriver)} method in this class ignores
	 * a set of exceptions, which has the effect that you can't tell what actually caused an action to fail - you just get a 
	 * timeout exceptions at the end of the polling .  This method will fail on any exception, so it allows the 
	 * failures to be thrown and captured. Note that timeout and polling are expected to generally be passed as zero (no exceptions are going
	 * to be ignored, so re-polling is not going to happen) 
	 *     
	 * @see Elemental#runUsingFluentWaitInDebugMode(org.openqa.selenium.support.ui.ExpectedCondition)
	 * 
	 * @param driver webdriver
	 * @param timeout secs
	 * @param pollingFrequency a Duration
	 * @return a fluent wait
	 */
	public static FluentWait<WebDriver> getFluentWaitDebugMode(WebDriver driver, Duration timeout, Duration pollingFrequency) {
		return new FluentWait<>(driver).withTimeout(timeout).pollingEvery(pollingFrequency);
	}

}
