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

package com.mark59.selenium.driversimpl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import com.mark59.selenium.interfaces.DriverFunctionsSelenium;

/**
 * 
 * Chrom(ium) implementation of {@link DriverFunctionsSelenium}  
 * 
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class DriverFunctionsSeleniumChrome extends DriverFunctionsSelenium<ChromeDriver>  {
	
	private static final Logger LOG = LogManager.getLogger(DriverFunctionsSeleniumChrome.class);

	WebDriver webDriver;

	
	/**
	 * @param webDriver the WebDriver to package
	 */
	public DriverFunctionsSeleniumChrome(WebDriver webDriver) {
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
		if (!this.getDriver().manage().logs().getAvailableLogTypes().contains(LogType.PERFORMANCE))
			return null;

		List<LogEntry> logs = this.getDriver().manage().logs().get(LogType.PERFORMANCE).getAll();

		StringBuilder allEntriesLogBuilder = new StringBuilder();
		for (LogEntry entry : logs) {
			allEntriesLogBuilder.append(entry.toString()).append("\n");
		}
		String allEntriesLog = allEntriesLogBuilder.toString();
		
		return StringUtils.isNotBlank(allEntriesLog) ? allEntriesLog.getBytes() : null;
	}

	
	@Override
	public void clearDriverPerfLogs() {
		if (!this.getDriver().manage().logs().getAvailableLogTypes().contains(LogType.PERFORMANCE))
			return;

		this.getDriver().manage().logs().get(LogType.PERFORMANCE).getAll();
	}

	
	/**
	 * Doing a close() before quit() appears to help chromeDriver cleanup its temp directories
	 * https://stackoverflow.com/questions/43289035/chromedriver-not-deleting-scoped-dir-in-temp-folder-after-test-is-complete/
	 * <p>The close and quit are 'try'ed separately, as we've observed failure on <code>driver.close</code> when multiple
	 * session targetIds(windows/tabs/cdp sessions) have been activated - and we always want the 'quit' to execute..
	 */
	@Override
	public void driverDispose() {

		try {
			getDriver().close();
		} catch (Exception e) {
			LOG.warn("Failure on Chromedriver close : " + e.getClass() + " : " + e.getMessage());
			if (LOG.isDebugEnabled()){
				e.printStackTrace();				
			}
		}
			
		try {
			this.getDriver().quit();
		} catch (Exception e) {
			LOG.warn("Failure on Chromedriver quit : " + e.getClass() + " : " + e.getMessage());
			if (LOG.isDebugEnabled()){
				e.printStackTrace();				
			}
		}
	}
	
}
