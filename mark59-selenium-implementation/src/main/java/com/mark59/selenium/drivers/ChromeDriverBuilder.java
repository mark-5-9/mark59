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

package com.mark59.selenium.drivers;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;

/**
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class ChromeDriverBuilder extends SeleniumDriverBuilder<ChromeOptions> {

	private static final Logger LOG = Logger.getLogger(ChromeDriverBuilder.class);

	public ChromeDriverBuilder() {
		// https://stackoverflow.com/questions/18702533/how-to-execute-selenium-chrome-webdriver-in-silent-mode
		serviceBuilder = new ChromeDriverService.Builder().withSilent(true);
		options = new ChromeOptions();
		
		// renderer issues around Chrome 73, adding options which can assist 
		// https://stackoverflow.com/questions/48450594/selenium-timed-out-receiving-message-from-renderer
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-gpu-sandbox");	
		
		//workaround for Chrome 76 ?
		// https://stackoverflow.com/questions/56558361/driver-manage-logs-getloglogtype-browser-no-longer-working-in-chromedriver-v/56596616#56596616
		// https://stackoverflow.com/questions/56507652/selenium-chrome-cant-see-browser-logs-invalidargumentexception
		options.setExperimentalOption("w3c", false);
//		options.setExperimentalOption("w3c", true);
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setHeadless(boolean isHeadless) {
		options.setHeadless(isHeadless);
		return (T) this;
	}


	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setWriteBrowserLogfile(boolean isWriteBrowserLogFile) {
		LOG.debug("Note: Browser Logfile not implemented for Chrome ");
		return (T) this;
	}	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setVerbosePerformanceLoggingLogging(boolean isVerbose) {
		LoggingPreferences logPreferences = new LoggingPreferences();
		logPreferences.enable(LogType.PERFORMANCE, Level.INFO);
		options.setCapability(CapabilityType.LOGGING_PREFS, logPreferences);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setDriverExecutable(Path driverPath) {
		serviceBuilder.usingDriverExecutable(driverPath.toFile());
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setAlternateBrowser(Path browserExecutablePath) {
		options.setBinary(browserExecutablePath.toFile());
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setPageLoadStrategy(PageLoadStrategy strategy) {
		options.setPageLoadStrategy(strategy);
		return (T) this;
	}

	@Override
	public <T extends SeleniumDriverBuilder<?>> T setPageLoadStrategyNone() {
		return setPageLoadStrategy(PageLoadStrategy.NONE);
	}

	@Override
	public <T extends SeleniumDriverBuilder<?>> T setPageLoadStrategyNormal() {
		return setPageLoadStrategy(PageLoadStrategy.NORMAL);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setProxy(Proxy proxy) {
		options.setProxy(proxy);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setAdditionalOptions(List<String> arguments) {
		options.addArguments(arguments);
		return (T) this;
	}
	
	
	/* 
	 * Creates the Selenium Chrome driver, returning it in a 'wrapper'.
	 * 
	 * Note that the ChromeDriverService used is per instance of the driver. Experimentation showed this does not appear to be 
	 * particularly inefficient (especially for longer running scripts).  Using a shared ChromDriverService also caused
	 * test failures, as the ChromeDriverService becomes a single point of failure. 
	 */
	@Override
	public SeleniumDriverWrapper build(Map<String, String> arguments) { 
		ChromeDriver driver = null;
		
		if (LOG.isDebugEnabled()) LOG.debug("chrome options : " + Arrays.toString(options.asMap().entrySet().toArray()));			

		try {
			driver = new ChromeDriver((ChromeDriverService) serviceBuilder.build(), options);
			driver.manage().window().setSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
			
			if (LOG.isDebugEnabled()) {
				Capabilities caps = driver.getCapabilities();
				LOG.debug("  Browser Name and Version : " + caps.getBrowserName() + " " + caps.getVersion());
				@SuppressWarnings("unchecked")
				Map<String, String> chromeReturnedCapsMap = (Map<String, String>) caps.getCapability("chrome");
				LOG.debug("  Chrome Driver Version    : " + chromeReturnedCapsMap.get("chromedriverVersion"));
				LOG.debug("  Chrome Driver Temp Dir   : " + chromeReturnedCapsMap.get("userDataDir"));
			}

		} catch (Exception e) {
			LOG.error("An error has occured during the creation of the ChromeDriver : "  + e.getMessage() );	
			e.printStackTrace();
			if (driver != null) {driver.quit();};
			throw new RuntimeException("An error has occured during the creation of the ChromeDriver - throwing Runtime exception");
		}
		return new ChromeDriverWrapper(driver);
	}



}
