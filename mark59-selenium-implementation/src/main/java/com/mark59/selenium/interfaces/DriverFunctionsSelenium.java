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

package com.mark59.selenium.interfaces;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;

import com.mark59.core.interfaces.DriverFunctions;
import com.mark59.selenium.corejmeterimpl.JmeterFunctionsForSeleniumScripts;
import com.mark59.selenium.driversimpl.DriverFunctionsSeleniumChrome;
import com.mark59.selenium.driversimpl.DriverFunctionsSeleniumFirefox;

/**
 * Encapsulates the WebDriver to be used in a Mark59 script. Currently can be a Firefox or Chom(ium) driver.  
 * 
 * <p>The WebDriver is available via the {@link #getDriver()} method.  
 * 
 * <p>As well as the WebDriver itself, additional functionality around screenshots, logging and exception 
 * handling are included.  This class is intended as an internal class within the Mark59 framework, with its     
 * functionality accessible for a script via {@link JmeterFunctionsForSeleniumScripts}
 * 
 * <p>Note: Ending a Selenium connection is slightly different between FireFox and Chrom(ium), so 
 * {@link #driverDispose()} methods have been written in the implementation of those Mark59 drivers 
 * 
 * @see DriverFunctionsSeleniumChrome
 * @see DriverFunctionsSeleniumFirefox
 *  
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public abstract class DriverFunctionsSelenium<O extends WebDriver> implements DriverFunctions<WebDriver> {

	static final Logger LOG = LogManager.getLogger(DriverFunctionsSelenium.class);


	/**
	 * Used to return any Performance logs captured by the Driver.
	 * @return byte[] perflog data
	 */
	public abstract byte[] captureDriverPerfLogs();
	

	/**
	 *  Note Performance logs (only implemented in chrom(ium) are read destructive, 
	 *  so you do not need to use this method after a standard capture of perflogs)  
	 */
	public abstract void clearDriverPerfLogs();

	
	@Override
	public byte[] captureScreenshot() {
		
		byte[] screenshot;
		
		try {
			screenshot = Base64.decodeBase64(((TakesScreenshot) this.getDriver()).getScreenshotAs(OutputType.BASE64));
		} catch (Exception e) {
			LOG.debug("Screenshot failure ("  + e.getClass().getName() + ")  Message : " + e.getMessage()); 
			LOG.warn("Screenshot failure ("  + e.getClass().getName() + ")  Message  Message starts: " + StringUtils.abbreviate(e.getMessage(), 100)); 
			screenshot = ("Screenshot failure ("  + e.getClass().getName() + ")  Message : " + e.getMessage()).getBytes();
		} 
		return screenshot;
	}


	/**
	 * @return message string with url and page source
	 */
	public String captureCurrentUrlAndtHtmlPageSource() {
		String currentURL;
		try {
			currentURL = (this.getDriver()).getCurrentUrl();
		} catch (UnhandledAlertException e) {
			LOG.debug("UnhandledAlertException.  Message : " + e.getMessage() );
			LOG.warn("UnhandledAlertException thrown!  Message starts: " +  StringUtils.abbreviate(e.getMessage(), 100)); 
			currentURL = "URL is not availale.  An UnhandledAlertException Exception has been thrown : " + e.getMessage();
		}
		
		String pageSource;
		try {
			pageSource = (this.getDriver()).getPageSource();
		} catch (Exception e ) {
			LOG.debug("Page Source Not Available  ("  + e.getClass().getName() + ")  .  Message : " + e.getMessage()); 
			LOG.warn("Page Source Not Available  ("  + e.getClass().getName() + ")  .  Message starts: " + StringUtils.abbreviate(e.getMessage(), 100)); 
			pageSource = "Page Source Not Available.  Error message : " + e.getMessage();
		}

		return "<!--  Driver CurrentUrl : " + currentURL + " --> \n" +  pageSource;
	}
	
}

