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

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.ScreenshotLoggingHelper;

/**
 * @author Michael Cohen
 * Written: Australian Winter 2019  
 */
public class FireFoxDriverBuilder extends SeleniumDriverBuilder<FirefoxOptions> {

	private static final Logger LOG = LogManager.getLogger(FireFoxDriverBuilder.class);
	
	private int width  = Mark59Constants.DEFAULT_BROWSER_WIDTH;
	private int height = Mark59Constants.DEFAULT_BROWSER_HEIGHT;

	/**
	 * creates the GeckoDriverService Builder and FirefoxOptions object to be built
	 */
	public FireFoxDriverBuilder() {
		serviceBuilder = new GeckoDriverService.Builder();
		options = new FirefoxOptions();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setDriverExecutable(Path driverPath) {
		serviceBuilder.usingDriverExecutable(driverPath.toFile());
		return (T) this;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setHeadless(boolean isHeadless) {
		options.setHeadless(isHeadless);
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
	public <T extends SeleniumDriverBuilder<?>> T setSize(int width, int height) {
		this.width = width;
		this.height = height;
		return (T) this;
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
		LOG.debug("Note: setAdditionalOptions for Firefox not currenlty supported (options will be ignored)");
		return (T) this;
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setWriteBrowserLogfile(boolean isWriteBrowserLogFile) {
		
		if ( isWriteBrowserLogFile && StringUtils.isNotBlank(ScreenshotLoggingHelper.getScreenshotDirectory())) {
			
			String firefoxBrowserLog =  ScreenshotLoggingHelper.getScreenshotDirectory() + "/" + Thread.currentThread().getName() + "_FirefoxBrowserLogfile.txt";
			LOG.info("Note: FireFox driver logging directed to " + firefoxBrowserLog);
			File firefoxBrowserLogFile = new File(firefoxBrowserLog);
			new File(firefoxBrowserLogFile.getParent()).mkdirs();
			System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, firefoxBrowserLog);
			
		} else {
			LOG.debug("Note: FireFox driver logging is being suppressed.");
			System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
		}	
		return (T) this;
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setAlternateBrowser(Path browserExecutablePath) {
		options.setBinary(browserExecutablePath);
		return (T) this;
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SeleniumDriverBuilder<?>> T setVerbosePerformanceLoggingLogging(boolean isVerbose) {
		LOG.debug("Note: FireFox driver does not support Performance Logging");
		return (T) this;
	}
	
	
	@Override
	public SeleniumDriverWrapper build(Map<String, String> arguments) {

		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("dom.disable_beforeunload", true);
		options.setProfile(profile);
		if (LOG.isDebugEnabled()) LOG.debug("Starting Firefox Driver with the following : " + options.toJson());
		
		FirefoxDriver driver = new FirefoxDriver((GeckoDriverService) serviceBuilder.build(), options);
		driver.manage().window().setSize(new Dimension(width, height));

		if (LOG.isDebugEnabled()) {
			Capabilities caps = driver.getCapabilities();
			LOG.debug("Firefox driver created. Browser Name+Version : " + caps.getBrowserName() + " " + caps.getBrowserVersion());
			LOG.debug("  geckodriver version  : " + caps.getCapability("moz:geckodriverVersion"));
			LOG.debug("  moz profile          : " + caps.getCapability("moz:profile"));
		}		
				
		return new FireFoxDriverWrapper(driver);
	}


}
