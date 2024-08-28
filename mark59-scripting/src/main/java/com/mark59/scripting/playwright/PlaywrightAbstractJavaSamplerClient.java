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

package com.mark59.scripting.playwright;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59LogLevels;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.PropertiesKeys;
import com.mark59.core.utils.PropertiesReader;
import com.mark59.scripting.KeepBrowserOpen;
import com.mark59.scripting.ScriptingConstants;
import com.mark59.scripting.UiAbstractJavaSamplerClient;
import com.mark59.scripting.interfaces.JmeterFunctionsUi;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.Proxy;


/**
 * A Playwright enabled extension of the JMeter Java Sampler class {@link org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient}.  
 * This is core class of the Mark59 Playwright implementation, and should be extended when creating 
 * a JMeter-ready Playwright script. 
 * 
 * <p>Implementation of abstract method {@link #runPlaywrightTest(JavaSamplerContext, JmeterFunctionsForPlaywrightScripts, Page)} 
 * should contain the test, with parameterisation handled by {@link #additionalTestParameters()}.  
 * See the 'DataHunter' samples provided for implementation details. 
 *      
 * <p>Includes a number of standard parameters for Playwright, logging and exception handling.</p>
 *
 * @see #additionalTestParameters() 
 * @see ScriptingConstants#HEADLESS_MODE 
 * @see ScriptingConstants#PLAYWRIGHT_ENV_VAR_PWDEBUG
 * @see ScriptingConstants#BROWSER_EXECUTABLE 
 * @see ScriptingConstants#ADDITIONAL_OPTIONS 
 * @see ScriptingConstants#EMULATE_NETWORK_CONDITIONS 
 * @see ScriptingConstants#PLAYWRIGHT_DOWNLOADS_PATH
 * @see ScriptingConstants#PLAYWRIGHT_OPEN_DEVTOOLS 
 * @see ScriptingConstants#PLAYWRIGHT_PROXY_SERVER
 * @see ScriptingConstants#PLAYWRIGHT_PROXY_BYPASS
 * @see ScriptingConstants#PLAYWRIGHT_PROXY_USERNAME
 * @see ScriptingConstants#PLAYWRIGHT_PROXY_PASSWORD
 * @see ScriptingConstants#PLAYWRIGHT_SLOW_MO
 * @see ScriptingConstants#PLAYWRIGHT_TIMEOUT_BROWSER_INIT
 * @see ScriptingConstants#PLAYWRIGHT_TRACES_DIR
 * @see ScriptingConstants#PLAYWRIGHT_DEFAULT_TIMEOUT
 * @see ScriptingConstants#PLAYWRIGHT_VIEWPORT_SIZE
 * @see IpUtilities#localIPisNotOnListOfIPaddresses(String)
 * @see IpUtilities#RESTRICT_TO_ONLY_RUN_ON_IPS_LIST 
 * @see JmeterFunctionsImpl#LOG_RESULTS_SUMMARY
 * @see JmeterFunctionsImpl#PRINT_RESULTS_SUMMARY
 * @see JmeterFunctionsForPlaywrightScripts
 * @see #scriptExceptionHandling(JavaSamplerContext, Map, Throwable)  
 * @see #makePlaywrightPage(Map)  
 *
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public abstract class PlaywrightAbstractJavaSamplerClient extends UiAbstractJavaSamplerClient {
	
	/** log4J class logger */
	public static final Logger LOG = LogManager.getLogger(PlaywrightAbstractJavaSamplerClient.class);	

	/**  the mark59 JmeterFunctionsForSeleniumScripts for the test */		
	protected JmeterFunctionsForPlaywrightScripts jm;
	
	//  Playwright Objects (used within Mark59) 
	
	/** The Playwright object used within Mark59 */
	protected Playwright playwright;
	/** The Playwright Browser object used within Mark59 */	
	protected Browser browser;
	/** The Playwright BrowserContext object used within Mark59 */		
	protected BrowserContext browserContext;
	/** The Playwright Page object used within Mark59 */	
	protected Page playwrightPage;
	
	
	/** Hold default arguments for implementations of this class */
	private static final Map<String, String> playwrightDefaultArgumentsMap; 
	static {
		Map<String, String> staticMap = buildBasePlaywrightStaticArgsMap();	
		playwrightDefaultArgumentsMap = Collections.unmodifiableMap(staticMap);
	}
	
	/**
	 * @return default arguments for implementations of this class
	 */
	protected static Map<String, String> buildBasePlaywrightStaticArgsMap() {
		Map<String,String> staticMap = new LinkedHashMap<>();
		
		staticMap.put("______________________ playwright settings: ________________________", "Refer Mark59 User Guide : http://mark59.com");	
		staticMap.put(ScriptingConstants.HEADLESS_MODE, 		 String.valueOf(true));
		staticMap.put(ScriptingConstants.ADDITIONAL_OPTIONS,	 "");
		staticMap.put(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE, "");
		
		staticMap.put(ScriptingConstants.PLAYWRIGHT_DEFAULT_TIMEOUT, "");	
		staticMap.put(ScriptingConstants.PLAYWRIGHT_VIEWPORT_SIZE, "");	
		staticMap.put(ScriptingConstants.PLAYWRIGHT_OPEN_DEVTOOLS, String.valueOf(false));
		staticMap.put(ScriptingConstants.PLAYWRIGHT_ENV_VAR_PWDEBUG, "");		
		staticMap.put(ScriptingConstants.PLAYWRIGHT_DOWNLOADS_PATH, "");		
		staticMap.put(ScriptingConstants.PLAYWRIGHT_SLOW_MO, "");	
		staticMap.put(ScriptingConstants.PLAYWRIGHT_TIMEOUT_BROWSER_INIT, "");	
		staticMap.put(ScriptingConstants.PLAYWRIGHT_TRACES_DIR, "");
		
		staticMap.put(ScriptingConstants.PLAYWRIGHT_PROXY_SERVER, "");		
		staticMap.put(ScriptingConstants.PLAYWRIGHT_PROXY_BYPASS, "");		
		staticMap.put(ScriptingConstants.PLAYWRIGHT_PROXY_USERNAME, "");		
		staticMap.put(ScriptingConstants.PLAYWRIGHT_PROXY_PASSWORD, "");	
		
		
		
		staticMap.put("______________________ logging settings: _______________________", "Expected values: 'default', 'buffer', 'write' or 'off' ");		
		staticMap.put(JmeterFunctionsForPlaywrightScripts.LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS,	Mark59LogLevels.DEFAULT.getName());
		staticMap.put(JmeterFunctionsForPlaywrightScripts.LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());
		staticMap.put(JmeterFunctionsForPlaywrightScripts.LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS,	Mark59LogLevels.DEFAULT.getName());
		staticMap.put(JmeterFunctionsForPlaywrightScripts.LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());

		staticMap.put(ON_EXCEPTION_WRITE_BUFFERED_LOGS, String.valueOf(true));
		staticMap.put(ON_EXCEPTION_WRITE_SCREENSHOT, 	String.valueOf(true));
		staticMap.put(ON_EXCEPTION_WRITE_PAGE_SOURCE, 	String.valueOf(true));
		staticMap.put(ON_EXCEPTION_WRITE_STACK_TRACE, 	String.valueOf(true));
		
		staticMap.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(false));	   
		staticMap.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(false));	   
		
		staticMap.put("______________________ miscellaneous: __________________________", "");				
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		staticMap.put(ScriptingConstants.EMULATE_NETWORK_CONDITIONS, "");	
		
		staticMap.put("___________________"       , "");			
		staticMap.put("script build information: ", "using mark59-scripting Version: " + Mark59Constants.MARK59_VERSION);
		return staticMap;
	}	

	
	/** 
	 * Creates the list of parameters with default values, as they would appear on the JMeter GUI for the JavaSampler being implemented.
	 * <p>An implementing class (the script extending this class) can add additional parameters (or override the standard defaults) 
	 * via the additionalTestParameters() method.    
	 * @see #additionalTestParameters()
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient
	 */
	@Override
	public Arguments getDefaultParameters() {
		return Mark59Utils.mergeMapWithAnOverrideMap(playwrightDefaultArgumentsMap, additionalTestParameters());
	}
	
	
	/**
	 * Used to define user defined arguments for the test, or override arguments default values.
	 * <p>Internally the values are used to build a Map of parameters that will be available throughout
	 * 'Mark59' for whatever customization is required for your test, or for Playwright configuration.</p>
	 * <p>Please see link(s) below for more detail.  
	 * 
	 * @see #additionalTestParameters() 
	 * @see ScriptingConstants#HEADLESS_MODE 
	 * @see ScriptingConstants#PLAYWRIGHT_ENV_VAR_PWDEBUG
	 * @see ScriptingConstants#BROWSER_EXECUTABLE 
	 * @see ScriptingConstants#ADDITIONAL_OPTIONS 
	 * @see ScriptingConstants#EMULATE_NETWORK_CONDITIONS 
	 * @see ScriptingConstants#PLAYWRIGHT_DOWNLOADS_PATH
	 * @see ScriptingConstants#PLAYWRIGHT_OPEN_DEVTOOLS 
	 * @see ScriptingConstants#PLAYWRIGHT_PROXY_SERVER
	 * @see ScriptingConstants#PLAYWRIGHT_PROXY_BYPASS
	 * @see ScriptingConstants#PLAYWRIGHT_PROXY_USERNAME
	 * @see ScriptingConstants#PLAYWRIGHT_PROXY_PASSWORD
	 * @see ScriptingConstants#PLAYWRIGHT_SLOW_MO
	 * @see ScriptingConstants#PLAYWRIGHT_TIMEOUT_BROWSER_INIT
	 * @see ScriptingConstants#PLAYWRIGHT_TRACES_DIR
	 * @see ScriptingConstants#PLAYWRIGHT_DEFAULT_TIMEOUT
	 * @see ScriptingConstants#PLAYWRIGHT_VIEWPORT_SIZE
	 * @see IpUtilities#localIPisNotOnListOfIPaddresses(String)
	 * @see IpUtilities#RESTRICT_TO_ONLY_RUN_ON_IPS_LIST 
	 * @see JmeterFunctionsImpl#LOG_RESULTS_SUMMARY
	 * @see JmeterFunctionsImpl#PRINT_RESULTS_SUMMARY
	 * @see JmeterFunctionsForPlaywrightScripts
	 * @see #scriptExceptionHandling(JavaSamplerContext, Map, Throwable)  
	 * @see #makePlaywrightPage(Map)  
	 * 
	 * @return the updated map of JMeter arguments with any required changes
	 */
	protected abstract Map<String, String> additionalTestParameters();
		

	/**
	 *  Execute a Playwright script using the arguments passed from JMeter or defaults, and handle exceptions.
	 * 
	 *  <p>Note the use of the catch on AssertionError, as this is NOT an Exception but an Error, and therefore needs
	 *  to be explicitly caught.
	 *  
	 *  <p>Refer to the scriptExceptionHandling (the 'see' link below) for more information
	 *  
	 *  @see #scriptExceptionHandling(JavaSamplerContext, Map, Throwable)
	 */
	@Override
	public JmeterFunctionsUi UiScriptExecutionAndExceptionsHandling(JavaSamplerContext context, Map<String,String> jmeterRuntimeArgumentsMap, 
			String tgName){
		
		try {
			playwrightPage = makePlaywrightPage(jmeterRuntimeArgumentsMap);   
		} catch (Exception e) {
			LOG.error("ERROR : " + this.getClass() + ". Fatal error has occurred for Thread Group " + tgName
					+ " while attempting to initiate playwright!" );
			LOG.error(e.getMessage());
			e.printStackTrace();			
			return null;
		}

		jm = new JmeterFunctionsForPlaywrightScripts(context, playwrightPage, jmeterRuntimeArgumentsMap);   	
		
		try {
			
			LOG.debug(">> running test ");			
			
			runPlaywrightTest(context, jm, playwrightPage);
		
			LOG.debug("<< finished test" );

		} catch (Exception | AssertionError e) {

			scriptExceptionHandling(context, jmeterRuntimeArgumentsMap, e);
		
		} finally {
			
			jm.tearDown();
			if (! keepBrowserOpen.equals(KeepBrowserOpen.ALWAYS)){ 
				driverDispose();
			}
		}
		return jm;
	}


	/**
	 * Creates the playwright objects, based on the JMeter arguments (and defaults for arguments not present)
	 * @param arguments JMeter arguments
	 * @return page - playwright page used by the framework when invoked by this class during script initiation 
	 */
	@SuppressWarnings("deprecation")
	protected Page makePlaywrightPage(Map<String, String> arguments) {

		Map<String,String> playwrightEnv = new HashMap<>();
		playwrightEnv.put("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD","1");	
		
		if ("1".equals(arguments.get(ScriptingConstants.PLAYWRIGHT_ENV_VAR_PWDEBUG))){
			playwrightEnv.put("PWDEBUG","1");	
		}

		playwright = Playwright.create(new Playwright.CreateOptions().setEnv(playwrightEnv)); 

		PropertiesReader pr = null;
		try {
			pr = PropertiesReader.getInstance();
		} catch (IOException e) {
			LOG.fatal("Failed to load properties file");
			e.printStackTrace();			
			System.exit(1);
		}
			
		LaunchOptions browserLaunchOptions = new BrowserType.LaunchOptions();
		
		// Set an alternate browser executable. If mark59.property contains mark59.browser.executable, that will be used
		// but can overridden by the OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE augment. 
		// If neither is present the a default location based on the o/s is guessed.
		
		Path browserPath = null;
		String pathMsg = "";
		
		if (StringUtils.isNotBlank(pr.getProperty(PropertiesKeys.MARK59_PROP_BROWSER_EXECUTABLE))) {
			browserPath = new File(pr.getProperty(PropertiesKeys.MARK59_PROP_BROWSER_EXECUTABLE)).toPath();
			pathMsg = "Playwright script uses prop to set browser: " + pr.getProperty(PropertiesKeys.MARK59_PROP_BROWSER_EXECUTABLE);
		}
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE))) {
			try {
				browserPath = new File(arguments.get(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE)).toPath();
				pathMsg = "Playwright script uses override arg to set browser: "
						+ arguments.get(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE);			
			} catch (Exception e) {
				throw new RuntimeException(" An invalid value for "
						+ "OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE JMeter argument was passed !" 
						+ " ["+arguments.get(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE)+"]");
			}
		}
		if (browserPath == null) {
			String localhostOs = Mark59Utils.obtainOperatingSystemForLocalhost();  
			if (Mark59Constants.OS.WINDOWS.getOsName().equals(localhostOs)){
				browserPath = new File(ScriptingConstants.DEFAULT_CHROME_PATH_WIN).toPath();
			} else if (Mark59Constants.OS.MAC.getOsName().equals(localhostOs)){  // LINUX  
				browserPath = new File(ScriptingConstants.DEFAULT_CHROME_PATH_MAC).toPath();	
			} else {  // LINUX by default  
				browserPath = new File(ScriptingConstants.DEFAULT_CHROME_PATH_LINUX).toPath();			
			} 
			pathMsg ="Playwright will use the o/s default as the browser executable:"
					+ " OS: " + localhostOs + ", Path: " + browserPath.toFile().getAbsolutePath();
		}
		browserLaunchOptions.setExecutablePath(browserPath);
		if (LOG.isDebugEnabled())
			LOG.debug(pathMsg); 
	
		
		// Create a warning for the now deprecated, unused BROWSER_EXECUTABLE argument		
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.BROWSER_EXECUTABLE))) {
			LOG.warn("'BROWSER_EXECUTABLE' JMeter argument is no longer in use! Please use 'OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE'");			
		}
		
		// Turn driver headless mode on or off. Default: ON
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.HEADLESS_MODE))){
			browserLaunchOptions.setHeadless(Boolean.parseBoolean(arguments.get(ScriptingConstants.HEADLESS_MODE)));
		}
		
		// Set additional option Arguments
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.ADDITIONAL_OPTIONS))){
			browserLaunchOptions.setArgs(Arrays.asList(StringUtils.split(
					arguments.get(ScriptingConstants.ADDITIONAL_OPTIONS), ",")));
		}

		// Set option to auto open Devtools for tabs
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_OPEN_DEVTOOLS))){
			browserLaunchOptions.setDevtools(Boolean.parseBoolean(arguments.get(ScriptingConstants.PLAYWRIGHT_OPEN_DEVTOOLS)));
		}
		
		// Set Downloads directory path 
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_DOWNLOADS_PATH))){
			browserLaunchOptions.setDownloadsPath(new File(arguments.get(ScriptingConstants.PLAYWRIGHT_DOWNLOADS_PATH)).toPath());
		}	
		
		// Set Downloads directory path 
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_DOWNLOADS_PATH))){
			browserLaunchOptions.setDownloadsPath(new File(arguments.get(ScriptingConstants.PLAYWRIGHT_DOWNLOADS_PATH)).toPath());
		}	
		
		// Set Proxy Server and additional optional Proxy parameters 
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_PROXY_SERVER))){
			Proxy proxy = new Proxy(arguments.get(ScriptingConstants.PLAYWRIGHT_PROXY_BYPASS));

			if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_PROXY_BYPASS))){
				proxy.setBypass(arguments.get(ScriptingConstants.PLAYWRIGHT_PROXY_BYPASS));
			}	
			if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_PROXY_USERNAME))){
				proxy.setUsername(arguments.get(ScriptingConstants.PLAYWRIGHT_PROXY_USERNAME));
			}	
			if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_PROXY_PASSWORD))){
				proxy.setPassword(arguments.get(ScriptingConstants.PLAYWRIGHT_PROXY_PASSWORD));
			}	
			browserLaunchOptions.setProxy(proxy);
		}	
		
		// Set Playwright slowed down (milliseconds)
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_SLOW_MO))){
			browserLaunchOptions.setSlowMo(Double.parseDouble(arguments.get(ScriptingConstants.PLAYWRIGHT_SLOW_MO)));
		}	
		
		// Set Maximum time in milliseconds to wait for the browser instance to start
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_TIMEOUT_BROWSER_INIT))){
			browserLaunchOptions.setTimeout(Double.parseDouble(arguments.get(ScriptingConstants.PLAYWRIGHT_TIMEOUT_BROWSER_INIT)));
		}	

		// If specified, traces are saved into this directory path
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_TRACES_DIR))){
			browserLaunchOptions.setTracesDir(new File(arguments.get(ScriptingConstants.PLAYWRIGHT_TRACES_DIR)).toPath());
		}	
		
		browser = playwright.chromium().launch(browserLaunchOptions);
		
		browserContext = browser.newContext(new Browser.NewContextOptions());
		
		playwrightPage = browserContext.newPage();

		//  default maximum time in milliseconds for timeout option
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_DEFAULT_TIMEOUT))){
			playwrightPage.setDefaultTimeout(Double.parseDouble(arguments.get(ScriptingConstants.PLAYWRIGHT_DEFAULT_TIMEOUT)));
		}	
		
		
		// Set browser dimensions
		if (StringUtils.isNotBlank(arguments.get(ScriptingConstants.PLAYWRIGHT_VIEWPORT_SIZE))) {	
			String[] browserDimArray = StringUtils.split(arguments.get(ScriptingConstants.PLAYWRIGHT_VIEWPORT_SIZE), ",");
			if ( browserDimArray.length == 2  && StringUtils.isNumeric(browserDimArray[0]) && StringUtils.isNumeric(browserDimArray[1])){
				int width  = Integer.parseInt(browserDimArray[0]);
				int height = Integer.parseInt(browserDimArray[1]);
				playwrightPage.setViewportSize(width, height);
			} else {
				LOG.warn("Browser dim " + arguments.get(ScriptingConstants.PLAYWRIGHT_VIEWPORT_SIZE) + " is not valid.  Option ignored.");
			}
		} 		
		
		// creates a Network.emulateNetworkConditions CDP session
		String emulateNetworkConditions = arguments.get(ScriptingConstants.EMULATE_NETWORK_CONDITIONS);
		if (StringUtils.isNotBlank(emulateNetworkConditions)){
			emulateNetworkConditionsCDPSession(browserContext, playwrightPage, emulateNetworkConditions);
		}

		return playwrightPage;
	}

	
	/**
	 * Invoked when a script Exception | AssertionError is caught.
	 * 
	 * <p>Logs and records this script execution as a failure.  By default all available mark59 logs are output for the point of failure, 
	 * including previously buffered logs.  
	 * 
	 * <p>Logs can be suppressed by setting a parameter in additionalTestParameters controlling it's output 
	 * to <code>false</code>: 
	 * <ul>
	 * <li>{@link UiAbstractJavaSamplerClient#ON_EXCEPTION_WRITE_BUFFERED_LOGS} -  log buffered (during the script)</li>
	 * <li>{@link UiAbstractJavaSamplerClient#ON_EXCEPTION_WRITE_SCREENSHOT} - screenshot(s) when exception occurred **</li>
	 * <li>{@link UiAbstractJavaSamplerClient#ON_EXCEPTION_WRITE_PAGE_SOURCE} - page source(s) when exception occurred **</li>
	 * <li>{@link UiAbstractJavaSamplerClient#ON_EXCEPTION_WRITE_STACK_TRACE} - Exception stack trace</li>
	 * </ul>
	 *    
	 * <p>For Playwright each open page on the {@link #browser} object will have its page source and screenshot captured, so there may be 
	 * multiple of each output on an exception. 
	 *    
	 * <p>For example, to suppress buffered logs being output when a script fails, in additionalTestParameters:<br><br>
	 * <code>jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_BUFFERED_LOGS, String.valueOf(false));</code>      
	 *    
	 * @see #userActionsOnScriptFailure(JavaSamplerContext, JmeterFunctionsForPlaywrightScripts, Page)
	 * @param context the current JavaSamplerContext  
	 * @param jmeterRuntimeArgumentsMap  map of JMeter (Java Request) parameters 
	 * @param e can be an exception or Assertion error
	 */
	protected void scriptExceptionHandling(JavaSamplerContext context, Map<String, String> jmeterRuntimeArgumentsMap, Throwable e) {

		jm.failInFlightTransactions();
		
		String thread = Thread.currentThread().getName();
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		
		System.err.println("["+ thread + "]  ERROR : " + this.getClass() + ". See Mark59 log directory for details. Stack trace: \n  " + sw.toString());
		LOG.error("["+ thread + "]  ERROR : " + this.getClass() + ". See Mark59 log directory for details. Stack trace: \n  " + sw.toString());

		String lastTxnStarted = jm.getMostRecentTransactionStarted();
		if (StringUtils.isBlank(lastTxnStarted)){
			lastTxnStarted =  "noTxn";
		} 
		
		try {
			
			if (Boolean.parseBoolean(context.getParameter(ON_EXCEPTION_WRITE_BUFFERED_LOGS))){
				jm.writeBufferedArtifacts();
			}
			if (Boolean.parseBoolean(context.getParameter(ON_EXCEPTION_WRITE_SCREENSHOT))){
				jm.writeScreenshot(lastTxnStarted + "_EXCEPTION");	
			}
			if (Boolean.parseBoolean(context.getParameter(ON_EXCEPTION_WRITE_PAGE_SOURCE))){	
				jm.writePageSource(lastTxnStarted + "_EXCEPTION" );
			}
			if (Boolean.parseBoolean(context.getParameter(ON_EXCEPTION_WRITE_STACK_TRACE))){	
				jm.writeStackTrace(lastTxnStarted + "_EXCEPTION_STACKTRACE", e);
			}
			
		} catch (Exception ex) {
			LOG.error("["+ thread + "]  ERROR : " + this.getClass() + ".  An exception occurred during scriptExceptionHandling (documentExceptionState) "
					+  ex.getClass().getName() +  " thrown",  e);
			ex.printStackTrace();
		}	
		
		try {
			userActionsOnScriptFailure(context, jm, playwrightPage); 
		} catch (Exception errorHandlingException) {
			LOG.error("["+ thread + "]  ERROR : " + this.getClass() + ".  An exception occurred during scriptExceptionHandling (userActionsOnScriptFailure) "
					+  errorHandlingException.getClass().getName() +  " thrown",  errorHandlingException);
			errorHandlingException.printStackTrace();
		}
		
		jm.failTest();
		
		if (keepBrowserOpen.equals(KeepBrowserOpen.ONFAILURE)){
			// force browser to stay open
			keepBrowserOpen = KeepBrowserOpen.ALWAYS;   
		}
	}

	
	/**
	 * Close playwright objects
	 */
	public void driverDispose() {
		try {
			playwrightPage.close();
			browserContext.close();
			browser.close();   // sometimes a 30s wait on running headed (Chrome WIN only)?
			playwright.close();   
		} catch (Exception e) {
			LOG.warn("Failure on attempt to close playwright objects : "+e.getClass()+" : "+e.getMessage());
			if (LOG.isDebugEnabled()) {
				e.printStackTrace();
			}
		}
	}	
	
	
	/**
	 * Intended to be an override in scripts where some user interactions is required when a script fails 
	 * (via the browser if still available, or other application interface such as an API call).
	 * <p>An example of such an interaction may be to force a user-id logout, where re-entry into the user is
	 * needed, and the user may be otherwise be left in an uncertain state.
	 * <p>If an exception occurs during execution the of this method, the exception is logged and the method simply exited
	 * (the original failure will still be handled by the Mark59 framework).  
	 *          
	 * @param context the current JavaSamplerContext
	 * @param jm the current JmeterFunctionsForPlaywrightScripts  
	 * @param playwrightPage the current playwrightPage
	 */
	protected void userActionsOnScriptFailure(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page playwrightPage) {};	
	
	
	/**
	 * Method to be implemented containing the actual test steps. 
	 * 
	 * @param context the current JavaSamplerContext
	 * @param jm the current JmeterFunctionsForPlaywrightScripts  
	 * @param playwrightPage the current playwrightPage
	 */
	protected abstract void runPlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page playwrightPage);
	
	
	/**
	 * Note: this should be consistent with the implementation of network conditions in DriverFunctionsSeleniumChromeBuilder
	 * 
	 * <p>Also, initial testing seemed to indicate CDP sessions are not as stable in Playwright compared to Selenium. 
	 * 
	 * @param browserContext browserContext
	 * @param page page
	 * @param emulateNetworkConditions  non-blank EMULATE_NETWORK_CONDITIONS argument
	 */
	private void emulateNetworkConditionsCDPSession(BrowserContext browserContext, Page page, String emulateNetworkConditions){
		List<String> emulateNetworkConditionsArray = Mark59Utils.commaDelimStringToStringList(emulateNetworkConditions);
		if (emulateNetworkConditionsArray.size() != 3 ) {
			LOG.warn("Invalid EMULATE_NETWORK_CONDITIONS passed (3 comma-delimited values required) and will be ignored : "+emulateNetworkConditions);
		} else if (	!StringUtils.isNumeric(emulateNetworkConditionsArray.get(0)) || 
					!StringUtils.isNumeric(emulateNetworkConditionsArray.get(1)) || 
					!StringUtils.isNumeric(emulateNetworkConditionsArray.get(2) )){
			LOG.warn("Invalid EMULATE_NETWORK_CONDITIONS passed (only integer values allowed) and will be ignored : "+emulateNetworkConditions);
		} else {
			CDPSession cdp = browserContext.newCDPSession(page) ;
			
			JsonObject cdpParms = new JsonObject();
			cdpParms.addProperty("offline", Boolean.parseBoolean("false"));
			cdpParms.addProperty("downloadThroughput",Integer.parseInt(emulateNetworkConditionsArray.get(0)) * 128); // kbps to bytes/sec(1024/8)
			cdpParms.addProperty("uploadThroughput",  Integer.parseInt(emulateNetworkConditionsArray.get(1)) * 128); // kbps to bytes/sec(1024/8)
			cdpParms.addProperty("latency",			  Integer.valueOf(emulateNetworkConditionsArray.get(2)));		 // msecs

			cdp.send("Network.emulateNetworkConditions", cdpParms );
			LOG.debug("  EMULATE_NETWORK_CONDITIONS triggered: " + emulateNetworkConditions);
			System.out.println("  EMULATE_NETWORK_CONDITIONS triggered: " + emulateNetworkConditions);
		}			
	}
	
}
