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

package com.mark59.core.utils;

/**
 * The current list of properties used by the Mark59 framework are defined here.
 * <p>
 * <b>mark59.screenshot.directory</b> : deprecated - renamed as mark59.log.directory<br>
 * <b>mark59.log.directory</b> : value defines the directory where transaction-level/error logging will occur<br>
 * <b>mark59.logname.format</b> : formatter for log names output to the log directory<br>
 * <b>mark59.log.directory.suffix</b> : log directory suffix, may be a local 'date' or 'datetime'<br>
 * <b>mark59.selenium.driver.path.chrome</b> : location of the chrome driver executable<br>
 * <b>mark59.selenium.driver.path.firefox</b> :  location of the geokodriver executable <br>
 * <b>mark59.server.profiles.excel.file.path</b> :  location of a excel file to be used for server metrics capture<br> 
 * <b>mark59.browser.executable</b> :  location of an alternate browser executable  
 * </p>
 * 
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class PropertiesKeys {

	private PropertiesKeys() {
	}
	
	/**
	 *  mark59.screenshot.directory.  Deprecated - renamed as mark59.log.directory
	 */
	@Deprecated
	public static final String MARK59_PROP_SCREENSHOT_DIRECTORY	= "mark59.screenshot.directory";
	
	/**
	 *  mark59.log.directory
	 */
	public static final String MARK59_PROP_LOG_DIRECTORY  		= "mark59.log.directory";

	/**
	 *  mark59.log.directory.suffix.  Valid values as listed :  {@link Mark59Constants#DATE} or {@link Mark59Constants#DATE_TIME} 	
	 */
	public static final String MARK59_PROP_LOG_DIRECTORY_SUFFIX = "mark59.log.directory.suffix";


	/**
	 *  Formatter for log names output to the log directory. A comma delimited list of values as described {@link Mark59Constants#LOGNAME_FORMAT_OPTIONS}	
	 *  <p>Default is <code>"ThreadName,Label"</code>
	 */
	public static final String MARK59_PROP_LOGNAME_FORMAT		= "mark59.logname.format";
	
	
	/**
	 * mark59.selenium.driver.path.chrome
	 */
	public static final String MARK59_PROP_DRIVER_CHROME		= "mark59.selenium.driver.path.chrome";

	/**
	 * mark59.selenium.driver.path.firefox
	 */
	public static final String MARK59_PROP_DRIVER_FIREFOX		= "mark59.selenium.driver.path.firefox";

	/**
	 * mark59.server.profiles.excel.file.path
	 */
	public static final String MARK59_PROP_SERVER_PROFILES_EXCEL_FILE_PATH	= "mark59.server.profiles.excel.file.path";

	/**
	 * mark59.browser.executable  (this property can be over-ridden at script level by setting the "BROWSER_EXECUTABLE" argument) 
	 */
	public static final String MARK59_PROP_BROWSER_EXECUTABLE	= "mark59.browser.executable";
	
	
	/**
	 * mark59.print.startup.console.messages. Set to 'true' to print Mark59 console message output on start-up (default is false) 
	 */
	public static final String MARK59_PRINT_STARTUP_CONSOLE_MESSAGES = "mark59.print.startup.console.messages";
	
	
	/**
	 * list of valid Mark59 property keys 
	 */
	public static final String[] MARK59_PROPERTY_KEYS =  { 
								MARK59_PROP_SCREENSHOT_DIRECTORY,
								MARK59_PROP_LOG_DIRECTORY,
								MARK59_PROP_LOG_DIRECTORY_SUFFIX,
								MARK59_PROP_LOGNAME_FORMAT,
								MARK59_PROP_DRIVER_CHROME, 
								MARK59_PROP_DRIVER_FIREFOX,
								MARK59_PROP_SERVER_PROFILES_EXCEL_FILE_PATH,
								MARK59_PROP_BROWSER_EXECUTABLE,
								MARK59_PRINT_STARTUP_CONSOLE_MESSAGES};
}
