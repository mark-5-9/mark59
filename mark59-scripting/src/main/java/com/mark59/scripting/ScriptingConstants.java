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

package com.mark59.scripting;

import com.mark59.core.utils.PropertiesKeys;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Proxy;

/**
 * @author Philip Webb
 * Written: Australian Sumer 2023/24
 */
public class ScriptingConstants {

	/**
	 * "HEADLESS_MODE" (default is 'true')
	 */
	public static final String HEADLESS_MODE = "HEADLESS_MODE";	

	
	/**
	 * "PLAYWRIGHT_ENV_VAR_PWDEBUG" - A value of "1" enables Playwright tests to run in debug mode from the 
	 * start of execution (The <b>PWDEBUG</b> playwright environment variable is set).  
	 * <p>Intended for use when running from and IDE. Use <code>page.pause();</code> to break at a 
	 * given point. See the Playwright documentation for more information. 
	 */
	public static final String PLAYWRIGHT_ENV_VAR_PWDEBUG = "PLAYWRIGHT_ENV_VAR_PWDEBUG";		
	
	
	/**
	 * "OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE"
	 * - sets the browser executable path (eg to a Chrome Beta or Chromium instance).
	 * <p>Will over-ride the mark59 property <code>mark59.browser.executable</code> in mark59.properties (if set). 
	 * <p>If neither this JMeter argument or <code>mark59.browser.executable</code> property 
	 * are set, the default installation of the expected browser is assumed.  In most cases Selenium will find
	 * the default installation, however, for Playwright a default Chrome install location for the O/S is simply 
	 * set by Mark59 - so for Playwright we advise the executable path be explicitly set, either by setting 
	 * the mark59 property or supplying this argument in the script.  Argument value must be non-blank to be 
	 * considered set.
	 * 
	 * @see PropertiesKeys#MARK59_PROP_BROWSER_EXECUTABLE  
	 * @see #DEFAULT_CHROME_PATH_LINUX
	 * @see #DEFAULT_CHROME_PATH_MAC
	 * @see #DEFAULT_CHROME_PATH_WIN
	 */
	public static final String OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE = "OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE";

	/**
	 * NO LONGER IN USE - PLEASE CHANGE TO {@link #OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE}
	 */
	@Deprecated
	public static final String BROWSER_EXECUTABLE = "BROWSER_EXECUTABLE";
	
	
	/**
	 * <p>"ADDITIONAL_OPTIONS" - Caters for the direct setting of any additional driver options from the JMeter
	 *  additional parameters.  Intended for use with the Chrome Driver (for Selenium and Playwright).</p>
	 * 
	 * <p>The input string needs to be a comma delimited list for multiple options.  
	 * For example, to set a proxy pac url and activate the disable extensions option, the "ADDITIONAL OPTIONS" 
	 * parameter for the SeleniumAbstractJavaSamplerClient based test script can be entered as : 
	 * <br><br> <b> --proxy-pac-url=http://myawesomecompany.corp/proxy.pac,--disable-extensions</b> </p>
	 * 
	 * <p>Note that a proxy override can be set using the "PROXY" parameters provided by Selenium and 
	 * Playwright (different structures), just shown here as an example.<br>  
	 *  
	 * <p>Another example: to run Chrome in incognito mode and have DevTools open with the browser, you can set the
	 *  "ADDITIONAL OPTIONS" parameter as : 
	 * <br><br> <b>--incognito,--auto-open-devtools-for-tabs</b> </p>
	 * 
	 * <p>At the time of writing the best sources for the list of available options are:<br>
	 *  <ul>
	 *   <li><a href="https://peter.sh/experiments/chromium-command-line-switches">
	 *   			  https://peter.sh/experiments/chromium-command-line-switches</a></li> 
	 *   <li><a href="https://chromium.googlesource.com/chromium/src/+/master/chrome/common/chrome_switches.cc">
	 *   			  https://chromium.googlesource.com/chromium/src/+/master/chrome/common/chrome_switches.cc</a></li> 
	 *  </ul>
	 */
	public static final String ADDITIONAL_OPTIONS = "ADDITIONAL_OPTIONS";	
	
	
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
	public static final String EMULATE_NETWORK_CONDITIONS = "EMULATE_NETWORK_CONDITIONS";	
	
	
	/**
	 * "PLAYWRIGHT_DEVTOOLS" - "true" / "false" 
	 *  @see BrowserType.LaunchOptions#setDevtools(boolean)
	 */
	public static final String PLAYWRIGHT_OPEN_DEVTOOLS = "PLAYWRIGHT_OPEN_DEVTOOLS";	

	
	/**
	 * "PLAYWRIGHT_DOWNLOADS_PATH" - Downloads directory path 
	 *  @see BrowserType.LaunchOptions#setDownloadsPath(java.nio.file.Path)
	 */
	public static final String PLAYWRIGHT_DOWNLOADS_PATH = "PLAYWRIGHT_DOWNLOADS_PATH";	
	
	
	/**
	 * "PLAYWRIGHT_PROXY_SERVER" - Required entry if a proxy is to be set)
	 * <p>Proxy server to be used for all requests. HTTP and SOCKS proxies are supported, 
	 * for example {@code http://myproxy.com:3128} or {@code socks5://myproxy.com:3128}. 
	 * Short form {@code myproxy.com:3128} is considered an HTTP proxy.
	 * <p>Note Bypass, Username, Password are optional and set separately
	 * @see #PLAYWRIGHT_PROXY_BYPASS
	 * @see #PLAYWRIGHT_PROXY_USERNAME
	 * @see #PLAYWRIGHT_PROXY_PASSWORD
	 * @see Proxy#server
	 * @see BrowserType.LaunchOptions#setProxy(com.microsoft.playwright.options.Proxy)
	 */
	public static final String PLAYWRIGHT_PROXY_SERVER = "PLAYWRIGHT_PROXY_SERVER";	

	/**
	 * "PLAYWRIGHT_PROXY_PYPASS" - Optional comma-separated domains to bypass proxy, for example 
	 * {@code ".com, chromium.org, .domain.com"}.
	 * @see Proxy#bypass
	 * @see BrowserType.LaunchOptions#setProxy(com.microsoft.playwright.options.Proxy)
	 */
	public static final String PLAYWRIGHT_PROXY_BYPASS = "PLAYWRIGHT_PROXY_BYPASS";	

	/**
	 * "PLAYWRIGHT_PROXY_PYPASS" - Optional username to use if HTTP proxy requires authentication.
	 * @see Proxy#username
	 * @see BrowserType.LaunchOptions#setProxy(com.microsoft.playwright.options.Proxy)
	 */
	public static final String PLAYWRIGHT_PROXY_USERNAME = "PLAYWRIGHT_PROXY_USERNAME";	

	/**
	 * "PLAYWRIGHT_PROXY_PYPASS" - Optional username to use if HTTP proxy requires authentication.
	 * @see Proxy#password
	 * @see BrowserType.LaunchOptions#setProxy(com.microsoft.playwright.options.Proxy)
	 */
	public static final String PLAYWRIGHT_PROXY_PASSWORD = "PLAYWRIGHT_PROXY_PASSWORD";	
	
	
	/**
	 * "PLAYWRIGHT_SLOW_MO" - Slow down Playwigtht ops (in msecs) 
	 *  @see BrowserType.LaunchOptions#setSlowMo(double)
	 */
	public static final String PLAYWRIGHT_SLOW_MO = "PLAYWRIGHT_SLOW_MO";	
	
	
	/**
	 * "PLAYWRIGHT_TIMEOUT_BROWSER_INIT" - Max browser startup in msec (default 30000) 
	 *  @see BrowserType.LaunchOptions#setTimeout(double)
	 */
	public static final String PLAYWRIGHT_TIMEOUT_BROWSER_INIT = "PLAYWRIGHT_TIMEOUT_BROWSER_INIT";	
	
	
	/**
	 * "PLAYWRIGHT_HAR_FILE_CREATION" - If 'true', a .har file will be created during script execution.
	 * Meant for debugging purposes, obviously use with caution within a performance test.
	 * The .har file will be written by mark59 to the mark59 log directory (as per mark59.properties) 
	 * @see #PLAYWRIGHT_HAR_URL_FILTER 
	 * @see Browser.NewContextOptions#recordHarPath
	 */
	public static final String PLAYWRIGHT_HAR_FILE_CREATION = "PLAYWRIGHT_HAR_FILE_CREATION";	
	
	
	/**
	 * "PLAYWRIGHT_HAR_URL_FILTER" - Invokes Playwright's browserContext.setRecordHarUrlFilter()
	 * method, when a .har file is to be created (optional).  For example <code>"**&#47;add_policy**"</code>
	 * will only caputure urls containing <code>&#47;add_policy</code>
	 * @see #PLAYWRIGHT_HAR_FILE_CREATION 
	 * @see Browser.NewContextOptions#setRecordHarUrlFilter(java.util.regex.Pattern)
	 */
	public static final String PLAYWRIGHT_HAR_URL_FILTER = "PLAYWRIGHT_HAR_URL_FILTER";	
	
	
	/**
	 * "PLAYWRIGHT_TRACES_DIR" - If specified, traces are saved into this directory path 
	 *  @see BrowserType.LaunchOptions#setTracesDir(java.nio.file.Path)
	 */
	public static final String PLAYWRIGHT_TRACES_DIR = "PLAYWRIGHT_TRACES_DIR";	
		
	
	/**
	 * "PLAYWRIGHT_DEFAULT_TIMEOUT" -This setting will change the default maximum time for all
	 *  the methods accepting timeout option.  Value in milliseconds.  
	 *  @see Page#setDefaultTimeout(double)
	 */
	public static final String PLAYWRIGHT_DEFAULT_TIMEOUT = "PLAYWRIGHT_DEFAULT_TIMEOUT";	
	
	
	/**
	 * "PLAYWRIGHT_VIEWPORT_SIZE" -Page viewport size.  Two comma delimited integers 
	 * representing width by height.  eg:
	 *  <pre>{@code  "640,480" }</pre>
	 *  @see Page#setViewportSize(int, int)
	 */
	public static final String PLAYWRIGHT_VIEWPORT_SIZE = "PLAYWRIGHT_VIEWPORT_SIZE";	

	
	/** Assumed path for a default location of Chrome on WINDOWS (for Playwright scripts)
	 * <br><code>"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";</code> */
	public static final String DEFAULT_CHROME_PATH_WIN = 
			"C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";

	/** Assumed path for a default location of Chrome on MAC (for Playwright scripts)
	 * <br><code>"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";</code> */
	public static final String DEFAULT_CHROME_PATH_MAC = 
			"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
	
	/** Assumed path for a default location of Chrome on NIX (for Playwright scripts)
	 * <br><code>"/opt/google/chrome/google-chrome";</code> */	
	public static final String DEFAULT_CHROME_PATH_LINUX = "/opt/google/chrome/google-chrome";
}
