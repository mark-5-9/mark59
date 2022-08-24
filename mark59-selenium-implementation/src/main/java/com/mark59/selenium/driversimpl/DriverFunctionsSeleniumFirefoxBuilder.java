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

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.service.DriverService;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59LoggingConfig;
import com.mark59.selenium.interfaces.DriverFunctionsSelenium;
import com.mark59.selenium.interfaces.DriverFunctionsSeleniumBuilder;

/**
 * @author Michael Cohen
 * Written: Australian Winter 2019  
 */
public class DriverFunctionsSeleniumFirefoxBuilder implements DriverFunctionsSeleniumBuilder<FirefoxOptions> {

	private static final Logger LOG = LogManager.getLogger(DriverFunctionsSeleniumFirefoxBuilder.class);
	
	private int width  = Mark59Constants.DEFAULT_BROWSER_WIDTH;
	private int height = Mark59Constants.DEFAULT_BROWSER_HEIGHT;

	private DriverService.Builder<GeckoDriverService, GeckoDriverService.Builder> serviceBuilder;
	private FirefoxOptions options;	
	

	/**
	 * creates the GeckoDriverService Builder and FirefoxOptions object to be built
	 */
	public DriverFunctionsSeleniumFirefoxBuilder() {
		serviceBuilder = new GeckoDriverService.Builder();
		options = new FirefoxOptions();
	}

	
	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setDriverExecutable(Path driverPath) {
		serviceBuilder.usingDriverExecutable(driverPath.toFile());
		return this;
	}

	
	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setHeadless(boolean isHeadless) {
		options.setHeadless(isHeadless);
		return this;
	}

		
	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setPageLoadStrategy(PageLoadStrategy strategy) {
		options.setPageLoadStrategy(strategy);
		return this;
	}
	
	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setPageLoadStrategyNone() {
		return setPageLoadStrategy(PageLoadStrategy.NONE);
	}

	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setPageLoadStrategyNormal() {
		return setPageLoadStrategy(PageLoadStrategy.NORMAL);
	}

	
	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setSize(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}


	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setProxy(Proxy proxy) {
		options.setProxy(proxy);
		return this;
	}
	

	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setUnhandledPromptBehaviour(UnexpectedAlertBehaviour behaviour) {
		options.setUnhandledPromptBehaviour(behaviour);
		return this;
	}
		
	
	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setAdditionalOptions(List<String> arguments) {
		LOG.debug("Note: setAdditionalOptions for Firefox not currenlty supported (options will be ignored)");
		return this;
	}
	
	
	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setWriteBrowserLogfile(boolean isWriteBrowserLogFile) {
		String logsDirectory = Mark59LoggingConfig.getInstance().getLogDirectory().getName();
		
		if (isWriteBrowserLogFile && StringUtils.isNotBlank(logsDirectory)) {
			String firefoxBrowserLog =  logsDirectory + File.separator + Thread.currentThread().getName() + "_FirefoxBrowserLogfile.txt";
			LOG.info("Note: FireFox driver logging directed to " + firefoxBrowserLog);
			File firefoxBrowserLogFile = new File(firefoxBrowserLog);
			new File(firefoxBrowserLogFile.getParent()).mkdirs();
			System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, firefoxBrowserLog);
		} else {
			LOG.debug("Note: FireFox driver logging is being suppressed.");
			System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
		}	
		return this;
	}
	
	
	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setAlternateBrowser(Path browserExecutablePath) {
		options.setBinary(browserExecutablePath);
		return this;
	}

	
	@Override
	public DriverFunctionsSeleniumBuilder<FirefoxOptions> setVerbosePerformanceLoggingLogging(boolean isVerbose) {
		LOG.debug("Note: FireFox driver does not support Performance Logging");
		return this;
	}
	
	
	@Override
	public DriverFunctionsSelenium<FirefoxDriver> build(Map<String, String> arguments) {

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
				
		return new DriverFunctionsSeleniumFirefox(driver);
	}

}
