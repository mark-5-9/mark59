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
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;

import com.mark59.core.DriverWrapper;
import com.mark59.core.factories.DriverWrapperFactory;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.PropertiesKeys;
import com.mark59.core.utils.PropertiesReader;
import com.mark59.core.utils.ScreenshotLoggingHelper;

/**
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SeleniumDriverFactory implements DriverWrapperFactory {

	private static final Logger LOG = LogManager.getLogger(SeleniumDriverFactory.class);

	public static final String DRIVER = "DRIVER";
	public static final String HEADLESS_MODE = "HEADLESS_MODE";
	public static final String BROWSER_EXECUTABLE = "BROWSER_EXECUTABLE";
	public static final String PAGE_LOAD_STRATEGY = "PAGE_LOAD_STRATEGY";
	public static final String WRITE_FFOX_BROWSER_LOGFILE = "WRITE_FFOX_BROWSER_LOGFILE";
	public static final String PROXY = "PROXY";
	public static final String BROWSER_DIMENSIONS = "BROWSER_DIMENSIONS";
	public static final String ADDITIONAL_OPTIONS = "ADDITIONAL_OPTIONS";
	
	private static final String CHROME = "CHROME";
	private static final String FIREFOX = "FIREFOX";
	

	PropertiesReader pr = null;

	public SeleniumDriverFactory() {
		// https://stackoverflow.com/questions/52975287/selenium-chromedriver-disable-logging-or-redirect-it-java 
		java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.WARNING);
		
		try {
			pr = PropertiesReader.getInstance();
		} catch (IOException e) {
			LOG.fatal("Failed to load properties file");
			e.printStackTrace();			
			System.exit(1);
		}
		
		try {
			ScreenshotLoggingHelper.initialiseDirectory(pr);
		} catch (IOException e) {
			LOG.fatal("Failed on invoke of ScreenshotLoggingHelper.initialiseDirectory");
			e.printStackTrace();			
			System.exit(1);
		}	

		
	}

	
	/**
	 *  Controls the matching of parameters from an implementation of SeleniumAbstractJavaSamplerClient.
	 *  These are the parameters that are either required as part of the framework by default, or can be
	 *  entered / overridden by a script       
	 *  
	 *  <p>The following parameters are catered for here:
	 *  <br><b>SeleniumDriverFactory.DRIVER</b>&emsp;("DRIVER") - required (must be 'CHROME' or 'FIREFOX')  
	 *  <br><b>SeleniumDriverFactory.HEADLESS_MODE</b>&emsp;("HEADLESS_MODE") - default true  
	 *  <br><b>SeleniumDriverFactory.PAGE_LOAD_STRATEGY</b>&emsp;("PAGE_LOAD_STRATEGY") - PageLoadStrategy.NONE / PageLoadStrategy.NORMAL
	 *  <br><b>SeleniumDriverFactory.BROWSER_DIMENSIONS</b>&emsp;("BROWSER_DIMENSIONS") - sets the browser size (eg "800,600") default is 1920 x 1080  
	 *  <br><b>SeleniumDriverFactory.PROXY</b>&emsp;("PROXY") - set the proxy  
	 *  <br><b>SeleniumDriverFactory.ADDITIONAL_OPTIONS</b>&emsp;("ADDITIONAL_OPTIONS") - allows for the setting of any of the many additional driver options  
	 *  <br><b>SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE</b>&emsp;("WRITE_FFOX_BROWSER_LOGFILE") -
	 *  Only implemented for Firefox - primary purpose is to redirect gekodriver's copious error logging off the console..
	 *  <br><b>SeleniumDriverFactory.BROWSER_EXECUTABLE</b>&emsp;("BROWSER_EXECUTABLE") - Set an alternate browser executable (eg to a Chrome Beta or Chromium instance) 
	 *    	       
	 *  Please refer to the SeleniumAbstractJavaSamplerClient javaDocs for more details 
	 *    
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends DriverWrapper<?>> T makeDriverWrapper(Map<String, String> arguments) {
		LOG.debug("SeleniumDriverFactory : executing makeDriverWrapper using : "  +  Arrays.asList(arguments) );
			
		if (arguments.isEmpty())
			throw new IllegalArgumentException("No arguments supplied for driver construction");

		if (!arguments.containsKey(DRIVER))
			throw new IllegalArgumentException("No driver defined in arguments supplied for driver construction");

		SeleniumDriverBuilder<?> builder = getDriverBuilderOfType(arguments.get(DRIVER));

		
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
			String browserDimArray[] = arguments.get(BROWSER_DIMENSIONS).trim().split("\\s*,\\s*");
			
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
	

		// Set additional option arguments
		if (arguments.containsKey(ADDITIONAL_OPTIONS ) 
				&& StringUtils.isNotBlank(arguments.get(ADDITIONAL_OPTIONS))){
			//convert the comma delimited input string to a list of strings .. 
			java.util.List<java.lang.String> argumentsList = Arrays.asList(arguments.get(ADDITIONAL_OPTIONS).split("\\s*,\\s*"));
			builder.setAdditionalOptions(argumentsList);
		}

		
		//  Only implemented for Firefox - primary purpose is to redirect gekodriver's copious error logging off the console.. 	
		if (arguments.containsKey(WRITE_FFOX_BROWSER_LOGFILE)) 
			builder.setWriteBrowserLogfile(Boolean.parseBoolean(arguments.get(WRITE_FFOX_BROWSER_LOGFILE)));
		else	
			builder.setWriteBrowserLogfile(false);
		
		
		// Set an alternate browser executable. If not set will use the default installation.
		if (arguments.containsKey(BROWSER_EXECUTABLE) && StringUtils.isNotBlank(arguments.get(BROWSER_EXECUTABLE)))
			builder.setAlternateBrowser(new File(arguments.get(BROWSER_EXECUTABLE)).toPath());
		
				
		//Note the Performance Log is currently only available for the Chrome Driver  
		//TODO:  Maybe allow script argument to turn on or off more detailed driver performance logging (output seemed similar in test cases either way)
		builder.setVerbosePerformanceLoggingLogging(false);
		
		return (T) builder.build(arguments);
	}

	
	
	private void setProxy(Map<String, String> arguments, SeleniumDriverBuilder<?> builder) {
		java.util.List<java.lang.String> proxyArgumentsList = Arrays.asList(arguments.get(PROXY).split("\\s*,\\s*"));
		
		Map<String, Object> rawMap = new TreeMap<String, Object>();
		
		for (String proxyArgumentString : proxyArgumentsList) {
			String[] proxyArgumentArray = proxyArgumentString.split("=");
			if (proxyArgumentArray.length != 2) { 
			     throw new IllegalArgumentException("Unexpected PROXY argument - expected a key-value pair delimited by '=' symbol but got : [" + proxyArgumentString + "]."   );
			}
			rawMap.put(proxyArgumentArray[0] , (String)proxyArgumentArray[1]);
			LOG.debug("proxy setting : [" + proxyArgumentArray[0] + "=" + proxyArgumentArray[1] + "]" );			
		} 
		
		Proxy proxy = new Proxy(rawMap);
		builder.setProxy(proxy);
	}

	
	/**
	 * <p>Sets the selenium driver to be used for the test.  In a selenium script, the driverType parameter is set via the
	 * '<b>DRIVER</b>' parameter.  Current options are '<b>CHROME</b> or '<b>FIREFOX</b>'.</p>    
	 * <p>This method uses the driverType to do a properties lookup to get the driver path (usually set in mark59.properties)</p>
	 * 
	 * @param driverType
	 */
	private SeleniumDriverBuilder<?> getDriverBuilderOfType(String driverType) {
		SeleniumDriverBuilder<?> builder = null;

		String seleniumDriverPath = null;
		if (CHROME.equalsIgnoreCase(driverType)) {
			seleniumDriverPath = pr.getProperty(PropertiesKeys.MARK59_PROP_DRIVER_CHROME);
		} else if (FIREFOX.equalsIgnoreCase(driverType)) {
			seleniumDriverPath = pr.getProperty(PropertiesKeys.MARK59_PROP_DRIVER_FIREFOX );
		} else {
			throw new IllegalArgumentException("No known driver for " + driverType);
		}
		
		if (seleniumDriverPath == null) {
			throw new RuntimeException("No selenium driver path property set for " + driverType ); 
		}
		
		
		if (CHROME.equalsIgnoreCase(driverType)) {
			builder = new ChromeDriverBuilder();
		} else if (FIREFOX.equalsIgnoreCase(driverType)) {
			builder = new FireFoxDriverBuilder();
		} else {
			throw new IllegalArgumentException("No known driver for " + driverType);
		}

		builder.setDriverExecutable(new File(seleniumDriverPath).toPath());

		return builder;
	}

}
