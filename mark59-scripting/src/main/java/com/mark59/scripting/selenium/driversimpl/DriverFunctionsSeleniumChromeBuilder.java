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

package com.mark59.scripting.selenium.driversimpl;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.service.DriverService;

import com.google.common.collect.ImmutableMap;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.scripting.selenium.interfaces.DriverFunctionsSelenium;
import com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder;

/**
 * <p>Creates a chromium Selenium driver to be used in the scripts.
 * <p>Invocation of the {@link #build(Map)} method will creates the a Chrom(ium) Selenium driver.
 * A {@link ChromeDriverService.Builder} is created, and Selenium {@link ChromeOptions} (which in Mark59 have been
 *  set by the {@link SeleniumDriverFactory}) are used by the service builder during driver creation.
 * <p>Note: this should be consistent with the implementation of network conditions in PlaywrightAbstractJavaSamplerClient.
 * @see DriverFunctionsSeleniumBuilder
 * 
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class DriverFunctionsSeleniumChromeBuilder implements DriverFunctionsSeleniumBuilder<ChromeOptions> {

	private static final Logger LOG = LogManager.getLogger(DriverFunctionsSeleniumChromeBuilder.class);
	
	private int width  = Mark59Constants.DEFAULT_BROWSER_WIDTH;
	private int height = Mark59Constants.DEFAULT_BROWSER_HEIGHT;

	private DriverService.Builder<ChromeDriverService, ChromeDriverService.Builder> serviceBuilder;
	private ChromeOptions options;
	
	/**
	 * creates a ChromeDriverService Builder, and sets up ChromeOptions found necessary over time 
	 */
	public DriverFunctionsSeleniumChromeBuilder() {
		
		//https://stackoverflow.com/questions/52975287/selenium-chromedriver-disable-logging-or-redirect-it-java
		serviceBuilder = new ChromeDriverService.Builder().withSilent(true);
		options = new ChromeOptions();

		// renderer issues around Chrome 73, adding options which can assist 
		// https://stackoverflow.com/questions/48450594/selenium-timed-out-receiving-message-from-renderer
		// + linux issue: "unknown error: DevToolsActivePort file doesn't exist" 
		// https://stackoverflow.com/questions/50642308/webdriverexception-unknown-error-devtoolsactiveport-file-doesnt-exist-while-t
		// Chromium 111 required "--remote-allow-origins=*"  
		((ChromeOptions) options).addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage"); 		
		options.addArguments("--disable-gpu");
		options.addArguments("--disable-gpu-sandbox");	
		options.addArguments("--remote-allow-origins=*");	
	}

		
	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setDriverExecutable(Path driverPath) {
		serviceBuilder.usingDriverExecutable(driverPath.toFile());
		return this;
	}
	
	
	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setHeadless(boolean isHeadless) {
		if (isHeadless) {
			options.addArguments("--headless");
		}
		return this;
	}

	
	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setPageLoadStrategy(PageLoadStrategy strategy) {
		options.setPageLoadStrategy(strategy);
		return this;
	}

	
	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setPageLoadStrategyNone() {
		return setPageLoadStrategy(PageLoadStrategy.NONE);
	}

	
	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setPageLoadStrategyNormal() {
		return setPageLoadStrategy(PageLoadStrategy.NORMAL);
	}
		
	
	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setSize(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}
	

	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setProxy(Proxy proxy) {
		options.setProxy(proxy);
		return this;
	}
	

	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setUnhandledPromptBehaviour(UnexpectedAlertBehaviour behaviour) {
		options.setUnhandledPromptBehaviour(behaviour);
		return this;
	}

	
	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setAdditionalOptions(List<String> arguments) {
		options.addArguments(arguments);
		return this;
	}
	

	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setWriteBrowserLogfile(boolean isWriteBrowserLogFile) {
		LOG.debug("Note: Browser Logfile not implemented for Chrome ");
		return this;
	}	
	
	
	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setAlternateBrowser(Path browserExecutablePath) {
		options.setBinary(browserExecutablePath.toFile());
		return this;
	}

	
	@Override
	public DriverFunctionsSeleniumBuilder<ChromeOptions> setVerbosePerformanceLoggingLogging(boolean isVerbose) {
		LoggingPreferences logPreferences = new LoggingPreferences();
		logPreferences.enable(LogType.PERFORMANCE, Level.INFO);
		options.setCapability(ChromeOptions.LOGGING_PREFS, logPreferences);		// "goog:loggingPrefs"
		return this;
	}

	
	/** 
	 * Creates a Selenium WebDriver, 'wrapping' it as a class variable in a Mark59SeleniumDriver implementation.
	 * 
	 * Note that the ChromeDriverService used is per instance of the driver, rather than one service for entire JVM (ie, the entire 
	 * test run in JMeter). Experimentation showed this does not appear to be particularly inefficient (especially for longer running scripts).  
	 * Using a shared ChromeDriverService also caused test failures, as the ChromeDriverService becomes a single point of failure.
	 */
	@Override
	public DriverFunctionsSelenium<ChromeDriver> build(Map<String, String> arguments) { 
		ChromeDriver driver = null;
		if (LOG.isDebugEnabled()) LOG.debug("chrome options : " + Arrays.toString(options.asMap().entrySet().toArray()));	
		
		try {
			ChromeDriverService chromeDriverService = (ChromeDriverService)serviceBuilder.build(); 
			chromeDriverService.sendOutputTo(new OutputStream(){@Override public void write(int b){}});  // send to null		
			
			driver = new ChromeDriver(chromeDriverService, options);
			driver.manage().window().setSize(new Dimension(width, height));
			
			if (LOG.isDebugEnabled()) {
				Capabilities caps = driver.getCapabilities();
				LOG.debug("  Browser Name and Version : " + caps.getBrowserName() + " " + caps.getBrowserVersion());
				@SuppressWarnings("unchecked")
				Map<String, String> chromeReturnedCapsMap = (Map<String, String>) caps.getCapability("chrome");
				LOG.debug("  Chrome Driver Version    : " + chromeReturnedCapsMap.get("chromedriverVersion"));
				LOG.debug("  Chrome Driver Temp Dir   : " + chromeReturnedCapsMap.get("userDataDir"));
				LOG.debug("  >>  sysinfo for driver.executeCdpCommand(\"Browser.getVersion\" -- ");
				Map<String, Object> sysinfo = driver.executeCdpCommand("Browser.getVersion", new HashMap <String, Object>() );
				for (Map.Entry<String, Object> entry : sysinfo.entrySet()) {
					String key = entry.getKey();
					Object val = entry.getValue();
					LOG.debug("  key : " + key + ",  value = " + val) ;
				}
				LOG.debug("  <<  end SystemInfo.ProcessInfo -- ");
			}
		
			String emulateNetworkConditions = arguments.get(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS);
			
			if (StringUtils.isNotBlank(emulateNetworkConditions)) {
				List<String> emulateNetworkConditionsArray = Mark59Utils.commaDelimStringToStringList(emulateNetworkConditions);
				if (emulateNetworkConditionsArray.size() != 3 ) {
					LOG.warn("Invalid EMULATE_NETWORK_CONDITIONS passed (3 comma-delimited values required) and will be ignored : " + emulateNetworkConditions);
				} else if (	!StringUtils.isNumeric(emulateNetworkConditionsArray.get(0)) || 
							!StringUtils.isNumeric(emulateNetworkConditionsArray.get(1)) || 
							!StringUtils.isNumeric(emulateNetworkConditionsArray.get(2) )){
					LOG.warn("Invalid EMULATE_NETWORK_CONDITIONS passed (only integer values allowed) and will be ignored : " + emulateNetworkConditions);
				} else {
					Map<String, Object> map = new HashMap<>();
					map.put("offline", false);
					map.put("download_throughput", 	Integer.parseInt(emulateNetworkConditionsArray.get(0)) * 128); 	// kbps to bytes/sec (1024/8)
					map.put("upload_throughput",   	Integer.parseInt(emulateNetworkConditionsArray.get(1)) * 128); 	// kbps to bytes/sec (1024/8)
					map.put("latency", 				Integer.valueOf(emulateNetworkConditionsArray.get(2)));			// msecs

					CommandExecutor executor = driver.getCommandExecutor();
					executor.execute(new Command(driver.getSessionId(),
							"setNetworkConditions", ImmutableMap.of("network_conditions", ImmutableMap.copyOf(map))));
					LOG.debug("  EMULATE_NETWORK_CONDITIONS triggered   : " + emulateNetworkConditions);
				}
			}
			
		} catch (Exception e) {
			String thread =Thread.currentThread().getName();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			LOG.error("An error has occurred during the creation of the ChromeDriver : "  + e.getMessage() );	
			System.err.println("An error has occurred during the creation of the ChromeDriver : "  + e.getMessage() );				
			LOG.error(" ERROR : " + this.getClass() + ". Stack trace: \n  " + sw.toString());
			System.err.println("["+ thread + "]  ERROR : " + this.getClass() + ". Stack trace: \n  " + sw.toString());
			if (driver != null) {driver.quit();}
			throw new RuntimeException("An error has occurred during the creation of the ChromeDriver (throwing a RuntimeException" );
		}
		return new DriverFunctionsSeleniumChrome(driver);
	}

}
