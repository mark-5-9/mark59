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

package com.mark59.selenium.driversimpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.mark59.selenium.interfaces.DriverFunctionsSelenium;

/**
 * Firefox implementation of a Mark59SeleniumDriver.  
 * 
 * @author Michael Cohen
 * Written: Australian Winter 2019  
 */
public class DriverFunctionsSeleniumFirefox extends DriverFunctionsSelenium<FirefoxDriver> {

	static final Logger LOG = LogManager.getLogger(DriverFunctionsSeleniumFirefox.class);	
	
	WebDriver webDriver;
	
	/**
	 * @param webDriver webDriver
	 */
	public DriverFunctionsSeleniumFirefox(WebDriver webDriver) {
		this.webDriver = webDriver;
	}
	
	
	@Override
	public WebDriver getDriver() {
		return webDriver;
	}
	
	
	@Override
	public String getDriverClass() {
		return this.getDriver().getClass().getName();  
	}
	
	@Override
	public byte[] captureDriverPerfLogs() {
		// FireFox doesn't support performance logs
		return null;
	}


	@Override
	public void clearDriverPerfLogs() {
		// FireFox doesn't support performance logs
	}
	
	/**
	 *  For a Firefox Selenium driver 'quit' will end the session.
	 *  Wrapping it in a try/catch, as historical gekodrivers used 'close'
	 *  (ie, just in case someone runs using an old gekodriver) 
	 */
	@Override
	public void driverDispose() {
		try {
			this.getDriver().quit();
		} catch (Exception e) {
			LOG.debug("attempting driver quit() : " + e.getMessage());
		}
	}
	
}