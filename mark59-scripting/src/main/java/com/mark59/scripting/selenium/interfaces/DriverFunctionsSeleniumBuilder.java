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
package com.mark59.scripting.selenium.interfaces;

import java.nio.file.Path;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;

import com.mark59.scripting.selenium.SeleniumAbstractJavaSamplerClient;
import com.mark59.scripting.selenium.driversimpl.DriverFunctionsSeleniumChromeBuilder;
import com.mark59.scripting.selenium.driversimpl.DriverFunctionsSeleniumFirefoxBuilder;

/**
 * Base builder class for configuring Selenium web drivers.
 * 
 * <p>Specifies configurable capabilities that the SeleniumDriverFactory knows how to use.
 * <p>Eg, for chrome a 'service builder' is created, selenium 'options' are set, which are used
 * by the service during driver creation
 * 
 * @see DriverFunctionsSeleniumChromeBuilder  
 * @see DriverFunctionsSeleniumFirefoxBuilder
 *   
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public interface DriverFunctionsSeleniumBuilder<O extends MutableCapabilities>	{
	
	/**
	 * Sets the path to the WebDriver executable that is being configured.
	 * 
	 * @param driverPath driver path to the WebDriver executable
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?> setDriverExecutable(Path driverPath);
	
	/**
	 * Sets whether or not the Selenium WebDriver will start in headless mode,
	 * meaning that no GUI browser will be used.
	 * 
	 * <p>By default, headless mode will be set to 'true' by the factory. </p>
	 * 
	 * @param isHeadless indicates if the driver should start in headless mode
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?> setHeadless(boolean isHeadless);

	
	/**
	 * Sets the page load strategy to be used by the WebDriver
	 * 
	 * <p> Page load strategy determines when the WebDriver will consider a page "loaded" </p>
	 * 
	 * <p>
	 * Possible page load strategies are:
	 * <ul>
	 * <li>normal : WebDriver will wait for the page load event to fire</li>
	 * <li>eager : WebDriver will wait for DOMContentLoaded event to fire</li>
	 * <li>none : WebDriver will wait for the initial html load to complete</li>
	 * </ul>
	 * 
	 * <p>
	 * Not all load strategies are implemented for all WebDrivers.  Therefore when setting the <b>PAGE_LOAD_STRATEGY</b>
	 *  parameter for a selenium script, the values to use will be:
	 * <ul> 
	 * <li> PageLoadStrategy.<b>NORMAL</b>.toString()  -  this is the default, or
	 * <li> PageLoadStrategy.<b>NONE</b>.toString()
	 * </ul>   
	 * <p>
	 * It is recommended that the "normal" page load strategy be used in simple cases.
	 * For more advanced/difficult web pages,the "none" strategy will need to be paired with a check to verify a desired
	 * element has loaded (else it will likely return in just a few milliseconds).<br>
	 * </p>
	 * 
	 * @see PageLoadStrategy
	 * @param strategy type of wait strategy the WebDriver will adopt
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?> setPageLoadStrategy(PageLoadStrategy strategy);

	/**
	 * Sets the page load strategy used by the WebDriver to "none".
	 * @see #setPageLoadStrategy 
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?> setPageLoadStrategyNone();

	/**
	 * Sets the page load strategy used by the WebDriver to "normal". (current default)
	 * @see #setPageLoadStrategy
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?> setPageLoadStrategyNormal();
	
	
	/**
	 * <p>Caters for the direct setting of the browser size via script argument <b>BROWSER_DIMENSIONS</b>.</p>
	 * 
	 * The argument value needs to be two integers, comma separated, otherwise a default of 1920,1080 will be used.
	 *   
	 * <p>For example to set a browser width x height of 800 x 600 (which incidentally, is the Chrome Headless 'maximize' default value 
	 * being overridden with 1920,1080 in Mark59 ) you specify:  
	 * <br><br> <b>800,600</b></p>
	 * 
	 * <p>Note that <b>--start-maximized</b> is not available - as just noted you can use "800,600" if you want to achieve the same 
	 * effect in headless mode
	 * 
	 * @see org.openqa.selenium.chrome.ChromeOptions#addArguments(java.util.List)
	 * @see SeleniumAbstractJavaSamplerClient
	 * @see org.openqa.selenium.Dimension
	 * 
	 * @param width of the browser
	 * @param height of the browser
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?> setSize(int width, int height);
	
	
	/**
	 * <p>Overrides the proxy to be used by the selenium WebDriver, and can passed via the 'PROXY' parameter.  
	 * The proxy settings need to be formatted as a list of comma-delimited key-value pairs, 
	 * and are passed directly to the selenium's proxy class constructor (strings values only)</p>
	 * 
	 * <p>For example, defining a proxy pac url: 
	 * <br><b>&nbsp;&nbsp;&nbsp;&nbsp; proxyType=PAC,proxyAutoconfigUrl=http://myawesomecompany.corp/proxy.pac</b></p>
	 * 
	 * <p>Another example.  Defining a http / https(ssl) proxy via the proxy host name  
	 * <br><b>&nbsp;&nbsp;&nbsp;&nbsp; httpProxy=http://myawesomecompany.corp:8080,sslProxy=http://myawesomecompany.corp:8080</b><br><br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; Note: for Chrome at time of writing server user/password entry is not catered for with http and ssl proxy server setup.</p>
	 * 
	 * <p>Hint: To not use a proxy server and always make direct connections, use chrome option argument "--no-proxy-server" in the ADDITIONAL_OPTIONS
	 * parameter</p>   
	 * <p>Hint: To reset the value to blank in the Java Request PROXY option in Jmeter (ie, no proxy override), just type a space into the PROXY value field.</p>   
	 *    
	 * @param proxy comma delimited list of proxy settings for WebDriver
	 * 
	 * @see org.openqa.selenium.Proxy
	 * @see #setAdditionalOptions(java.util.List)
	 * @see SeleniumAbstractJavaSamplerClient
	 * @return this 
	 */
	DriverFunctionsSeleniumBuilder<?> setProxy(Proxy proxy);
	

	/**
	 * <p>Action to take for an unexpected alert. 
	 * 
	 * <p>From a selenium script, the only implemented JMeter parameter is '<b>UNHANDLED_PROMPT_BEHAVIOUR</b>'.  With values are should be 
	 * one of the text values (case ignored) of UnexpectedAlertBehaviour.  
	 * <p>If an invalid string value that does not represent a UnexpectedAlertBehaviour enum string value is passed as the argument, 
	 * UnexpectedAlertBehaviour.IGNORE is assumed.
	 *    
	 * <p>Selenium script example:<br><br>
	 * <code>jmeterAdditionalParameters.put(SeleniumDriverFactory.UNHANDLED_PROMPT_BEHAVIOUR , UnexpectedAlertBehaviour.IGNORE.toString());</code>   
	 *    
	 * @param behaviour an UnexpectedAlertBehaviourr
	 * 
	 * @see org.openqa.selenium.UnexpectedAlertBehaviour
	 * @see #setAdditionalOptions(java.util.List)
	 * @see SeleniumAbstractJavaSamplerClient
	 * @return this 
	 */
	DriverFunctionsSeleniumBuilder<?> setUnhandledPromptBehaviour(UnexpectedAlertBehaviour behaviour);
	
	
	/**
	 * <p>Caters for the direct setting of any additional driver options from the JMeter additional parameters.
	 * Intended for use with the Chrome Driver.</p>
	 * 
	 * <p>The input string needs to be a comma delimited list for multiple options.  
	 * For example, to set a proxy pac url and activate the disable extensions option, the "ADDITIONAL OPTIONS" parameter
	 * for the SeleniumAbstractJavaSamplerClient based test script can be entered as : 
	 * <br><br> <b> --proxy-pac-url=http://myawesomecompany.corp/proxy.pac,--disable-extensions</b> </p>
	 * 
	 * <p>(note that a proxy override can be set using the "PROXY" parameter, just shown here as an example)<br>  
	 *  
	 * <p>Another example: to run Chrome in incognito mode and have DevTools open with the browser, you can set the "ADDITIONAL OPTIONS"
	 *  parameter as : 
	 * <br><br> <b>--incognito,--auto-open-devtools-for-tabs</b> </p>
	 * 
	 * <p>At the time of writing the best sources for the list of available options are:<br>
	 *  <ul>
	 *   <li><a href="https://peter.sh/experiments/chromium-command-line-switches">
	 *   			  https://peter.sh/experiments/chromium-command-line-switches</a></li> 
	 *   <li><a href="https://chromium.googlesource.com/chromium/src/+/master/chrome/common/chrome_switches.cc">
	 *   			  https://chromium.googlesource.com/chromium/src/+/master/chrome/common/chrome_switches.cc</a></li> 
	 *  </ul>
	 * 
	 * @see org.openqa.selenium.chrome.ChromeOptions#addArguments(java.util.List)
	 * @see SeleniumAbstractJavaSamplerClient
	 * 
	 * @param arguments options
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?> setAdditionalOptions(java.util.List<java.lang.String> arguments);
	
	
	/**
	 * 
	 * <p>Only implemented for Firefox.  Primary purpose is to redirect gekodriver's copious error logging off the console..</p> 
	 * <p>From a selenium script, the only implemented JMeter parameter is '<b>WRITE_FFOX_BROWSER_LOGFILE</b>'.  Values are:
	 * <ul>
	 * <li><b>false</b> : suppresses Firefox error logging (the default)</li>
	 * <li><b>true</b> : depending on log setting, logs may to written to the 
	 * 'screenshot' log directory (for example as set via property mark59.screenshot.directory in the mark59.properties file).
	 * </ul>
	 * @param isWriteBrowserLogFile see above
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?> setWriteBrowserLogfile(boolean isWriteBrowserLogFile);


	/**
	 * Sets the path to an alternate version of the target browser, such as a Chromium browser or a  development 
	 * version of Chrome. 
	 * 
	 * <p>For example, if you have downloaded Chromium directly onto your C: drive on a Win machine, you would set 
	 * the "OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE" as : 
	 * <br><br><b>C:/Chromium/Application/chrome.exe</b> </p>
	 * 
	 * <p>This over-rides the mark59 property <code>mark59.browser.executable</code> (if set).
	 * <p>If neither the  "OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE" JMeter parameter or 
	 * <code>mark59.browser.executable</code> property are
	 * set, the default installation of the expected browser is assumed.      
	 * 
	 * @param browserExecutablePath executable path to the browser executable to be used (java.nio.file.Path)
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?> setAlternateBrowser(Path browserExecutablePath);
	
	
	/**
	 * Sets whether or not the Selenium WebDriver will have performance logging turned on.
	 * 
	 * <p> By default, WebDriver performance logging is turned off. </p>
	 * 
	 * @param isVerbose indicates if more detailed Webdriver performance logs are
	 *                  required, such as requests sent and received.
	 * @return this
	 */
	DriverFunctionsSeleniumBuilder<?>setVerbosePerformanceLoggingLogging(boolean isVerbose);
	
	
	/**
	 * Creates a Selenium web driver, returning it in a 'wrapper'.
	 * @param arguments an key value that my be need in the final creation of the selenium driver 
	 * @return a class which extends com.mark59.core.DriverWrapper (eg a Selenium driver wrapper)
	 * 
	 * @see com.mark59.core.interfaces.DriverFunctions
	 */
	DriverFunctionsSelenium<?> build(Map<String, String> arguments);

}
