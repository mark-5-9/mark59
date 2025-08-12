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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;

import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.PropertiesKeys;
import com.mark59.core.utils.PropertiesReader;
import com.mark59.scripting.ScriptingConstants;
import com.mark59.scripting.selenium.JmeterFunctionsForSeleniumScripts;
import com.mark59.scripting.selenium.SeleniumAbstractJavaSamplerClient;
import com.mark59.scripting.selenium.interfaces.DriverFunctionsSelenium;
import com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder;

/**
 * <p>Defines and controls the parameters used in the creation of a Selenium Webdriver suitable for 
 * use in a Mark59 script. In Mark59 currently invoked by {@link SeleniumAbstractJavaSamplerClient} )
 *
 * @see SeleniumDriverFactory#makeMark59SeleniumDriver(Map)
 * @see SeleniumDriverFactory#SeleniumDriverFactory()
 * @see com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory#HEADLESS_MODE 
 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setHeadless(boolean)  
 * @see com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory#PAGE_LOAD_STRATEGY 
 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setPageLoadStrategy(PageLoadStrategy) 
 * @see com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory#BROWSER_DIMENSIONS 
 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setSize(int width, int height) 
 * @see com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory#PROXY 
 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setProxy(org.openqa.selenium.Proxy) 
 * @see com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory#UNHANDLED_PROMPT_BEHAVIOUR 
 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setUnhandledPromptBehaviour(UnexpectedAlertBehaviour) 
 * @see com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory#ADDITIONAL_OPTIONS 
 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setAdditionalOptions(java.util.List) 
 * @see com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory#WRITE_FFOX_BROWSER_LOGFILE 
 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setWriteBrowserLogfile(boolean)
 * @see com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory#BROWSER_EXECUTABLE  
 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setAlternateBrowser(java.nio.file.Path) 
 * @see com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory#EMULATE_NETWORK_CONDITIONS 
 * @see IpUtilities#localIPisNotOnListOfIPaddresses(String)   
 * @see JmeterFunctionsForSeleniumScripts
 *
 * @author Michael Cohen 
 * @author Philip Webb
 * Written: Australian Winter 2019  
 * 
 */
public class SeleniumDriverFactory {	

	private static final Logger LOG = LogManager.getLogger(SeleniumDriverFactory.class);

	/**
	 *  "DRIVER"- 'CHROME' or 'FIREFOX'  
	 *  @see SeleniumDriverFactory#getDriverBuilderOfType  
	 */
	public static final String DRIVER = "DRIVER";
	
	/**
	 * "HEADLESS_MODE" - 'true' or 'false', default 'true' 
	 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setHeadless(boolean)   
	 */
	public static final String HEADLESS_MODE = ScriptingConstants.HEADLESS_MODE;
	
	/**
	 * NO LONGER IN USE - PLEASE CHANGE TO {@link ScriptingConstants#OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE}
	 */
	@Deprecated
	public static final String BROWSER_EXECUTABLE = ScriptingConstants.BROWSER_EXECUTABLE;
	
	/**
	 * "PAGE_LOAD_STRATEGY" - PageLoadStrategy.NONE ('NONE') / PageLoadStrategy.NORMAL ('NORMAL').
	 * Default is 'NORMAL'.
	 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setPageLoadStrategy(PageLoadStrategy)
	 */
	public static final String PAGE_LOAD_STRATEGY = "PAGE_LOAD_STRATEGY";
	
	/**
	 * "WRITE_FFOX_BROWSER_LOGFILE" - Only implemented for Firefox.  Primary purpose is to redirect 
	 * gekodriver's copious error logging off the console.  Set to 'true' or 'false', default is 'false'.
	 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setWriteBrowserLogfile(boolean)
	 */
	public static final String WRITE_FFOX_BROWSER_LOGFILE = "WRITE_FFOX_BROWSER_LOGFILE";
	
	/**
	 * "PROXY"- used to set the proxy (refer to the 'see also' below for format) 
	 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setProxy(org.openqa.selenium.Proxy) 
	 */
	public static final String PROXY = "PROXY";
	
	/**
	 * "UNHANDLED_PROMPT_BEHAVIOUR"- used to set the action for a UNHANDLE_PROMPT_BEHAVIOUR event in selenium.
	 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setUnhandledPromptBehaviour(org.openqa.selenium.UnexpectedAlertBehaviour)
	 */
	public static final String UNHANDLED_PROMPT_BEHAVIOUR = "UNHANDLED_PROMPT_BEHAVIOUR";
	
	/**
	 * "BROWSER_DIMENSIONS" - sets the browser size. Eg "1920,1080" (the default) means 1920 (w) x 1080 (h)
	 * @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setSize(int width, int height) 
	 */
	public static final String BROWSER_DIMENSIONS = "BROWSER_DIMENSIONS";
	
	/**
	 * "ADDITIONAL_OPTIONS" - a comma delimited list used to set of any of the many
	 *  additional driver options. Refer to the 'see also' below for details. 
	 *  @see com.mark59.scripting.selenium.interfaces.DriverFunctionsSeleniumBuilder#setAdditionalOptions(java.util.List)
	 */
	public static final String ADDITIONAL_OPTIONS = ScriptingConstants.ADDITIONAL_OPTIONS;	

	/**
	 * "EMULATE_NETWORK_CONDITIONS" - (Chrome only) allows for network throttling, with parameters for download speed, 
	 * upload speed, and latency.  Speeds are in kilobits per second (kb/s), and latency in milliseconds (ms).
	 * For instance, if you intend to emulate a connection download speed of of 12 Mbps (Megabits per second), 
	 * a typical low-end direct Internet connection in Australia, the download value to enter is 12288 (12 * 1024).
	 * <p>The three values to enter are comma-delimited, in the order : download speed, upload speed, and latency. So:
	 * <br><br><b>"12288,1024,10"</b>
	 * <br><br>represents a connection with 12Mbps download, 1Mbps upload, and 10ms latency      
	 *
	 * <p>Note that this throttling is achieved via the Chrome DevTools Protocol command `Network.emulateNetworkConditions`
	 * (<a href="https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-emulateNetworkConditions"> 
	 *           https://chromedevtools.github.io/devtools-protocol/tot/Network/#method-emulateNetworkConditions</a> ) 
	 * <p>The Mark59 framework internally converts the input kb/s speeds into bytes/sec, required by the
	 *  Network.emulateNetworkConditions command.               
	 */
	public static final String EMULATE_NETWORK_CONDITIONS = ScriptingConstants.EMULATE_NETWORK_CONDITIONS;
	
	private static final String CHROME = "CHROME";
	private static final String FIREFOX = "FIREFOX";
	
	private PropertiesReader pr;

	/**
	 *  Note that in the normal course of events for a Mark59 Selenium script, the (first) instantiation of this class will be  
	 *  when a the properties reader instance is created (a core class), and the mark59 properties set.
	 *  
	 *  @see SeleniumDriverFactory
	 *  
	 */
	public SeleniumDriverFactory() {
		// https://stackoverflow.com/questions/52975287/selenium-chromedriver-disable-logging-or-redirect-it-java
		// Note: May not be a general solution (loggers can be re-created - they are "WeakReferences"), but it seems to works here.
		java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.WARNING);
		
		try {
			pr = PropertiesReader.getInstance();
		} catch (IOException e) {
			LOG.fatal("Failed to load properties file");
			e.printStackTrace();			
			System.exit(1);
		}
	}

	
	/**
	 *  Controls the matching of driver related arguments from an implementation of {@link SeleniumAbstractJavaSamplerClient},
	 *  with the type and options used to create the Selenium driver.    
	 *  
	 *  <p>The following arguments are catered for here:
	 *  <br><b>SeleniumDriverFactory.DRIVER</b>&emsp;("DRIVER") ('CHROME' or 'FIREFOX') - defaults to 'CHROME'  
	 *  <br><b>SeleniumDriverFactory.HEADLESS_MODE</b>&emsp;("HEADLESS_MODE") - default true  
	 *  <br><b>SeleniumDriverFactory.PAGE_LOAD_STRATEGY</b>&emsp;("PAGE_LOAD_STRATEGY") - PageLoadStrategy.NONE / PageLoadStrategy.NORMAL
	 *  <br><b>SeleniumDriverFactory.BROWSER_DIMENSIONS</b>&emsp;("BROWSER_DIMENSIONS") - sets the browser size (eg "800,600") default is 1920 x 1080  
	 *  <br><b>SeleniumDriverFactory.PROXY</b>&emsp;("PROXY") - to set the proxy  
	 *  <br><b>SeleniumDriverFactory.UNHANDLED_PROMPT_BEHAVIOUR</b>&emsp;("UNHANDLED_PROMPT_BEHAVIOUR") - to set the UnexpectedAlertBehaviour (eg "ignore")  
	 *  <br><b>SeleniumDriverFactory.ADDITIONAL_OPTIONS</b>&emsp;("ADDITIONAL_OPTIONS") - allows for the setting of any of the many additional driver options  
	 *  <br><b>SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE</b>&emsp;("WRITE_FFOX_BROWSER_LOGFILE") -
	 *  Only implemented for Firefox - primary purpose is to redirect gekodriver's copious error logging off the console..
	 *  <br><b>SeleniumDriverFactory.BROWSER_EXECUTABLE</b>&emsp;("BROWSER_EXECUTABLE") - Set an alternate browser executable (eg to a Chrome Beta or Chromium instance) 
	 *    	       
	 *  Please refer to {@link SeleniumAbstractJavaSamplerClient} for more details 
	 *    
	 * @param <T> extends DriverFunctionsSelenium
	 * @param arguments  JMeter arguments
	 * @return DriverFunctionsSeleniumBuilder  Which contains a build method to implement a type of {@link DriverFunctionsSelenium}   
	 */
	@SuppressWarnings("unchecked")
	public <T extends DriverFunctionsSelenium<WebDriver>> T makeMark59SeleniumDriver(Map<String, String> arguments) {
		if (LOG.isDebugEnabled()){LOG.debug("SeleniumDriverFactory : makeDriverWrapper : " + Mark59Utils.prettyPrintMap(arguments));}
			
		if (arguments.isEmpty())
			throw new IllegalArgumentException("No arguments supplied for driver construction");

		if (!arguments.containsKey(DRIVER))
			throw new IllegalArgumentException("No driver defined in arguments supplied for driver construction");

		DriverFunctionsSeleniumBuilder<?> builder = getDriverBuilderOfType(arguments.get(DRIVER));
		
		// Set an alternate browser executable. If mark59.property contains mark59.browser.executable, that will be used
		// but can overridden by the BROWSER_EXECUTABLE augment. If neither is present the default installation is used.
		
		Path browserPath = null;
		String pathMsg = "";
		
		if (StringUtils.isNotBlank(pr.getProperty(PropertiesKeys.MARK59_PROP_BROWSER_EXECUTABLE))) {
			browserPath = new File(pr.getProperty(PropertiesKeys.MARK59_PROP_BROWSER_EXECUTABLE)).toPath();
			builder.setAlternateBrowser(browserPath);
			pathMsg = "Selenium script uses prop to set browser: " + pr.getProperty(PropertiesKeys.MARK59_PROP_BROWSER_EXECUTABLE);
		}
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE))) {
			try {
				browserPath = new File(arguments.get(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE)).toPath();
				builder.setAlternateBrowser(browserPath);
				pathMsg = "Selenium script uses override arg to set browser: : "
						+ arguments.get(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE);
			} catch (Exception e) {
				throw new RuntimeException(" An invalid value for "
						+ "OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE JMeter argument was passed !"
						+ " ["+arguments.get(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE)+"]");
			}
		} 
		if (browserPath == null) {
			pathMsg ="Selenium driver will use defaults to find browser.";
		}
		if (LOG.isDebugEnabled())
			LOG.debug(pathMsg);

		// throw error for the now deprecated unused BROWSER_EXECUTABLE argument		
		if (StringUtils.isNotBlank(arguments.get(BROWSER_EXECUTABLE))) {
			LOG.error("'BROWSER_EXECUTABLE' JMeter argument is no longer in use! Please use 'OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE'");
			throw new IllegalArgumentException("'OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE' has replaced 'BROWSER_EXECUTABLE'" );
		}
		
		// Turn driver headless mode on or off. Default: ON
		if (arguments.containsKey(HEADLESS_MODE))
			builder.setHeadless(Boolean.parseBoolean(arguments.get(HEADLESS_MODE)));
		else
			builder.setHeadless(true);
		

		// Set Page Load Strategy
		if (arguments.containsKey(PAGE_LOAD_STRATEGY))
			if (PageLoadStrategy.NONE.toString().equalsIgnoreCase(arguments.get(PAGE_LOAD_STRATEGY)))
				builder.setPageLoadStrategyNone();
			else if (PageLoadStrategy.NORMAL.toString().equalsIgnoreCase(arguments.get(PAGE_LOAD_STRATEGY)))
				builder.setPageLoadStrategyNormal();
		
		
		// Set browser dimensions
		if (arguments.containsKey(BROWSER_DIMENSIONS) && StringUtils.isNotBlank(arguments.get(BROWSER_DIMENSIONS))) {	
			String[] browserDimArray = StringUtils.split(arguments.get(BROWSER_DIMENSIONS), ",");
			
			if ( browserDimArray.length == 2  && StringUtils.isNumeric(browserDimArray[0]) && StringUtils.isNumeric(browserDimArray[1])){
				int width  = Integer.parseInt(browserDimArray[0]);
				int height = Integer.parseInt(browserDimArray[1]);
				builder.setSize(width, height); 
			} else {
				LOG.warn("Browser dim " + arguments.get(BROWSER_DIMENSIONS) + " is not valid - setting size to " + Mark59Constants.DEFAULT_BROWSER_DIMENSIONS); 
				builder.setSize(Mark59Constants.DEFAULT_BROWSER_WIDTH, Mark59Constants.DEFAULT_BROWSER_HEIGHT );
			}
		} else {
			LOG.debug("Browser size not passed - setting size to " + Mark59Constants.DEFAULT_BROWSER_DIMENSIONS );
			builder.setSize(Mark59Constants.DEFAULT_BROWSER_WIDTH, Mark59Constants.DEFAULT_BROWSER_HEIGHT );
		}

		
		// Set proxy settings		
		if (arguments.containsKey(PROXY) 
				&& StringUtils.isNotBlank(arguments.get(PROXY))) {
			setProxy(arguments, builder);
		}
	
		
		// Set UNHANDLED_PROMPT_BEHAVIOUR		
		if (arguments.containsKey(UNHANDLED_PROMPT_BEHAVIOUR)
				&& StringUtils.isNotBlank(arguments.get(UNHANDLED_PROMPT_BEHAVIOUR))) {
			UnexpectedAlertBehaviour behaviour = UnexpectedAlertBehaviour.fromString(arguments.get(UNHANDLED_PROMPT_BEHAVIOUR));
			if (behaviour != null) {
				builder.setUnhandledPromptBehaviour(behaviour);
			} else {
				builder.setUnhandledPromptBehaviour(UnexpectedAlertBehaviour.IGNORE);
			}
		}

		
		// Set additional option arguments
		if (arguments.containsKey(ADDITIONAL_OPTIONS ) 
				&& StringUtils.isNotBlank(arguments.get(ADDITIONAL_OPTIONS))){
			java.util.List<java.lang.String> argumentsList =  Arrays.asList(StringUtils.split(arguments.get(ADDITIONAL_OPTIONS), ","));
			builder.setAdditionalOptions(argumentsList);
		}

		
		//  Only implemented for Firefox - primary purpose is to redirect gekodriver's copious error logging off the console.. 	
		if (arguments.containsKey(WRITE_FFOX_BROWSER_LOGFILE)) 
			builder.setWriteBrowserLogfile(Boolean.parseBoolean(arguments.get(WRITE_FFOX_BROWSER_LOGFILE)));
		else	
			builder.setWriteBrowserLogfile(false);
		
				
		//Note the Performance Log is currently only available for the Chrome Driver  
		builder.setVerbosePerformanceLoggingLogging(false);
		
		return (T) builder.build(arguments);
	}

	
	
	private void setProxy(Map<String, String> arguments, DriverFunctionsSeleniumBuilder<?> builder) {

		String[] proxyArgumentsList = StringUtils.split(arguments.get(PROXY), ",");
		Map<String, Object> rawMap = new TreeMap<>();
		
		for (String proxyArgumentString : proxyArgumentsList) {
			String[] proxyArgumentArray = StringUtils.split(proxyArgumentString, "=");
			if (proxyArgumentArray.length != 2) {
				throw new IllegalArgumentException(
						"Unexpected PROXY argument - expected a key-value pair delimited by '=' symbol but got : ["
								+ proxyArgumentString + "].");
			}
			rawMap.put(proxyArgumentArray[0], proxyArgumentArray[1]);
			LOG.debug("proxy setting : [" + proxyArgumentArray[0] + "=" + proxyArgumentArray[1] + "]");
		}
		
		Proxy proxy = new Proxy(rawMap);
		builder.setProxy(proxy);
	}

	
	/**
	 * <p>Sets the selenium driver to be used for the test.  In a selenium script, the driverType parameter is set via the
	 * '<b>DRIVER</b>' parameter, for which for the current options are '<b>CHROME</b> or '<b>FIREFOX</b>', with CHROME as default (can be 
	 * used to drive either a Chrome or Chromium browser)</p>    
	 * <p>This method uses the driverType to do a properties lookup to get the driver path (usually set in mark59.properties)</p>
	 * 
	 * @param driverType currently Chrome or Firefox drivers allowed
	 */
	private DriverFunctionsSeleniumBuilder<?> getDriverBuilderOfType(String driverType) {
		DriverFunctionsSeleniumBuilder<?> builder;

		String seleniumDriverPath;
		if (CHROME.equalsIgnoreCase(driverType)) {
			seleniumDriverPath = pr.getProperty(PropertiesKeys.MARK59_PROP_DRIVER_CHROME);
		} else if (FIREFOX.equalsIgnoreCase(driverType)) {
			seleniumDriverPath = pr.getProperty(PropertiesKeys.MARK59_PROP_DRIVER_FIREFOX );
		} else {
			throw new IllegalArgumentException("No known driver for " + driverType);
		}
		
		if (seleniumDriverPath == null) {
			throw new RuntimeException("No selenium driver path property set for " + driverType +
				".\n (Please set " + PropertiesKeys.MARK59_PROP_DRIVER_CHROME + " or " + PropertiesKeys.MARK59_PROP_DRIVER_FIREFOX +
				" as appropriate, to the location of the Selenium driver (usually done in mark59.properties)." ); 
		}
			
		if (CHROME.equalsIgnoreCase(driverType)) {
			builder = new DriverFunctionsSeleniumChromeBuilder();
		} else if (FIREFOX.equalsIgnoreCase(driverType)) {
			builder = new DriverFunctionsSeleniumFirefoxBuilder();
		} else {
			throw new IllegalArgumentException("No known driver for " + driverType +  ".  (only CHROME or FIREFOX permitted)");
		}

		builder.setDriverExecutable(new File(seleniumDriverPath).toPath());
		return builder;
	}

}
