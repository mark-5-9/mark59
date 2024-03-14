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

package com.mark59.scripting.selenium;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59LogLevels;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.scripting.KeepBrowserOpen;
import com.mark59.scripting.ScriptingConstants;
import com.mark59.scripting.UiAbstractJavaSamplerClient;
import com.mark59.scripting.interfaces.JmeterFunctionsUi;
import com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory;
import com.mark59.scripting.selenium.interfaces.DriverFunctionsSelenium;



/**
 * A Selenium Webdriver enabled extension of the JMeter Java Sampler class {@link org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient}.  
 * This is core class of the Mark59 Selenium implementation, and should be extended when creating 
 * a JMeter-ready Selenium script. 
 * 
 * <p>Implementation of abstract method {@link #runSeleniumTest(JavaSamplerContext, JmeterFunctionsForSeleniumScripts, WebDriver)} 
 * should contain the test, with parameterisation handled by {@link #additionalTestParameters()}.  
 * See the 'DataHunter' samples provided for implementation details. 
 *      
 * <p>Includes a number of standard parameters expected for a Selenium WebDriver.</p>
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
 * @see IpUtilities#RESTRICT_TO_ONLY_RUN_ON_IPS_LIST
 * @see JmeterFunctionsImpl#LOG_RESULTS_SUMMARY
 * @see JmeterFunctionsImpl#PRINT_RESULTS_SUMMARY 
 * @see JmeterFunctionsForSeleniumScripts
 * @see #scriptExceptionHandling(JavaSamplerContext, Map, Throwable)
 *
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public abstract class SeleniumAbstractJavaSamplerClient extends UiAbstractJavaSamplerClient {
	
	static {  // block CDP websocket closed warnings in JMeter   
		org.apache.logging.log4j.core.config.Configurator.setLevel("org.asynchttpclient.netty.handler", Level.ERROR);
		org.apache.logging.log4j.core.config.Configurator.setLevel("org.openqa.selenium.remote.http", Level.ERROR);
		
		String logConfig = "handlers = java.util.logging.ConsoleHandler\n" + 
				".level = WARNING\n" +
				"java.util.logging.ConsoleHandler.level = WARNING\n" +
				"org.openqa.selenium.remote.http.level = SEVERE\n" +
				"org.asynchttpclient.netty.handler.level = SEVERE\n";
		try {
			java.util.logging.LogManager.getLogManager().readConfiguration(new java.io.ByteArrayInputStream(logConfig.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException e) {
			System.err.println("Failed to configure override java.util.logging : " + logConfig + "\nError : " + e.getMessage());
		}
	}

	/** log4J class logger */
	public static final Logger LOG = LogManager.getLogger(SeleniumAbstractJavaSamplerClient.class);	

	/**  the mark59 JmeterFunctionsForSeleniumScripts for the test  */		
	protected JmeterFunctionsForSeleniumScripts jm;
	/**  the Selenium driver 'Wrapper' for the test, with additional functions around logging and exception handling */	
	protected DriverFunctionsSelenium<WebDriver> mark59SeleniumDriver; 
	/**  the Selenium Web Driver for the test  */
	protected WebDriver driver;

	
	/** Hold default arguments for implementations of this class */
	private static final Map<String, String> seleniumDefaultArgumentsMap; 
	static {
		Map<String, String> staticMap = buildBaseSeleniumStaticArgsMap();	
		seleniumDefaultArgumentsMap = Collections.unmodifiableMap(staticMap);
	}
	
	/**
	 * @return default arguments for implementations of this class
	 */
	protected static Map<String, String> buildBaseSeleniumStaticArgsMap() {
		Map<String,String> staticMap = new LinkedHashMap<>();
		
		staticMap.put("______________________ driver settings: ________________________", "Refer Mark59 User Guide : http://mark59.com");	
		staticMap.put(SeleniumDriverFactory.DRIVER,						Mark59Constants.DEFAULT_DRIVER);
		staticMap.put(SeleniumDriverFactory.HEADLESS_MODE, 				String.valueOf(true));
		staticMap.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, 		PageLoadStrategy.NORMAL.toString());
		staticMap.put(SeleniumDriverFactory.BROWSER_DIMENSIONS, 		Mark59Constants.DEFAULT_BROWSER_DIMENSIONS);
		staticMap.put(SeleniumDriverFactory.PROXY, 						"");
		staticMap.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, 		"");				
		staticMap.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, String.valueOf(false));		
		staticMap.put(SeleniumDriverFactory.UNHANDLED_PROMPT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE.toString());
		staticMap.put(ScriptingConstants.OVERRIDE_PROPERTY_MARK59_BROWSER_EXECUTABLE, "");
		
		
		staticMap.put("______________________ logging settings: _______________________", "Expected values: 'default', 'buffer', 'write' or 'off' ");		
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS,	Mark59LogLevels.DEFAULT.getName() );
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS,	Mark59LogLevels.DEFAULT.getName());
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_PERF_LOG_AT_END_OF_TRANSACTIONS, 		Mark59LogLevels.DEFAULT.getName());

		staticMap.put(ON_EXCEPTION_WRITE_BUFFERED_LOGS, String.valueOf(true));
		staticMap.put(ON_EXCEPTION_WRITE_SCREENSHOT, 	String.valueOf(true));
		staticMap.put(ON_EXCEPTION_WRITE_PAGE_SOURCE, 	String.valueOf(true));
		staticMap.put(ON_EXCEPTION_WRITE_PERF_LOG, 		String.valueOf(true));
		staticMap.put(ON_EXCEPTION_WRITE_STACK_TRACE, 	String.valueOf(true));
		
		staticMap.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(false));	   
		staticMap.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(false));	   
		
		staticMap.put("______________________ miscellaneous: __________________________", "");				
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		staticMap.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");		
		
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
		return Mark59Utils.mergeMapWithAnOverrideMap(seleniumDefaultArgumentsMap, additionalTestParameters());
	}
	

	/**
	 * Used to define user defined arguments for the test, or override arguments default values.
	 * <p>Internally the values are used to build a Map of parameters that will be available throughout
	 * 'Mark59' for whatever customization is required for your test, or for the Webdriver implementation.</p>
	 * <p>Please see link(s) below for more detail.  
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
	 * @see IpUtilities#RESTRICT_TO_ONLY_RUN_ON_IPS_LIST 
	 * @see JmeterFunctionsImpl#LOG_RESULTS_SUMMARY
	 * @see JmeterFunctionsImpl#PRINT_RESULTS_SUMMARY 
	 * @see JmeterFunctionsForSeleniumScripts
	 * @see #scriptExceptionHandling(JavaSamplerContext, Map, Throwable)
	 * 
	 * @return the updated map of JMeter arguments with any required changes
	 */
	protected abstract Map<String, String> additionalTestParameters();
		

	/**
	 *  Execute a Selenium script using the arguments passed from JMeter or defaults, and handle exceptions.
	 * 
	 *  <p>Note the use of the catch on AssertionError, as this is NOT an Exception but an Error, and therefore needs
	 *  to be explicitly caught. 
	 *  
	 *  <p>Refer to the scriptExceptionHandling JavaDoc (the 'see' link below) for more information
	 *  
	 * @see #scriptExceptionHandling(JavaSamplerContext, Map, Throwable)
	 */
	@Override
	public JmeterFunctionsUi UiScriptExecutionAndExceptionsHandling(JavaSamplerContext context, Map<String,String> jmeterRuntimeArgumentsMap, String tgName ) {

		try {
			mark59SeleniumDriver = new SeleniumDriverFactory().makeMark59SeleniumDriver(jmeterRuntimeArgumentsMap) ;
		} catch (Exception e) {
			LOG.error("ERROR : " + this.getClass() + ". Fatal error has occurred for Thread Group " + tgName
					+ " while attempting to initiate the selenium Driver!" );
			LOG.error(e.getMessage());
			e.printStackTrace();			
			return null;
		}

		driver = mark59SeleniumDriver.getDriver();
		jm = new JmeterFunctionsForSeleniumScripts(context, mark59SeleniumDriver, jmeterRuntimeArgumentsMap);   	
		
		try {
			
			LOG.debug(">> running test ");			
			
			runSeleniumTest(context, jm, driver);
			
			jm.tearDown();
		
			LOG.debug("<< finished test" );

		} catch (Exception | AssertionError e) {

			scriptExceptionHandling(context, jmeterRuntimeArgumentsMap, e);
		
		} finally {
			if (! keepBrowserOpen.equals(KeepBrowserOpen.ALWAYS) ){ 
				mark59SeleniumDriver.driverDispose();
			}
		}
		return jm;
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
	 * <li>{@link UiAbstractJavaSamplerClient#ON_EXCEPTION_WRITE_SCREENSHOT} - screenshot(s) when exception occurred</li>
	 * <li>{@link UiAbstractJavaSamplerClient#ON_EXCEPTION_WRITE_PAGE_SOURCE} - page source(s) when exception occurred</li>
	 * <li>{@link UiAbstractJavaSamplerClient#ON_EXCEPTION_WRITE_STACK_TRACE} - Exception stack trace</li>
	 * <li>{@link UiAbstractJavaSamplerClient#ON_EXCEPTION_WRITE_PERF_LOG} - Chromium Performance Log, unwritten or unbuffered records when exception occurred</li>
	 * </ul>
	 *    
	 * <p>For example, to suppress buffered logs being output when a script fails, in additionalTestParameters:<br><br>
	 * <code>jmeterAdditionalParameters.put(ON_EXCEPTION_WRITE_BUFFERED_LOGS, String.valueOf(false));</code>      
	 *    
	 * @see #userActionsOnScriptFailure(JavaSamplerContext, JmeterFunctionsForSeleniumScripts, WebDriver)
	 * @param context the current JavaSamplerContext  
	 * @param jmeterRuntimeArgumentsMap  map of JMeter (Java Request) parameters 
	 * @param e can be an exception or Assertion error
	 */
	protected void scriptExceptionHandling(JavaSamplerContext context, Map<String, String> jmeterRuntimeArgumentsMap, Throwable e) {
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
			if (Boolean.parseBoolean(context.getParameter(ON_EXCEPTION_WRITE_PERF_LOG))){	
				jm.writeDriverPerfLogs(lastTxnStarted + "_EXCEPTION_PERFLOG");
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
			userActionsOnScriptFailure(context, jm, driver); 
		} catch (Exception errorHandlingException) {
			LOG.error("["+ thread + "]  ERROR : " + this.getClass() + ".  An exception occurred during scriptExceptionHandling (userActionsOnScriptFailure) "
					+  errorHandlingException.getClass().getName() +  " thrown",  errorHandlingException);
			errorHandlingException.printStackTrace();
		}
		
		jm.failTest();
		jm.tearDown();
		
		if (keepBrowserOpen.equals(KeepBrowserOpen.ONFAILURE)){
			// force browser to stay open
			keepBrowserOpen = KeepBrowserOpen.ALWAYS;   
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
	 * @param jm the current JmeterFunctionsForSeleniumScripts  
	 * @param driver object array - often expected to be (or include) the current WebDriver
	 */
	protected void userActionsOnScriptFailure(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm,	WebDriver driver) {};	
	
	
	/**
	 * Method to be implemented containing the actual test steps. 
	 * 
	 * @param context the current JavaSamplerContext
	 * @param jm the current JmeterFunctionsForSeleniumScripts  
	 * @param driver the current WebDriver
	 */
	protected abstract void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver);
	
	
	/**
	 * @deprecated Please replace with {@link #runUiTest()}
	 * @return {@link SampleResult}
	 */
	@Deprecated
	public SampleResult runSeleniumTest() {
		return runUiTest(KeepBrowserOpen.ONFAILURE, true);
	}
	
	/**
	 * @deprecated Please replace with {@link #runUiTest(KeepBrowserOpen)}
	 * @param keepBrowserOpen  KeepBrowserOpen (NEVER, ONFAILURE, ALWAYS) 
	 * @return SampleResult  - runSeleniumTest with LogResultsSummary set as true
	 */
	@Deprecated	
	public SampleResult runSeleniumTest(KeepBrowserOpen keepBrowserOpen ) {
		return runUiTest(keepBrowserOpen, true);
	}

	/**
	 * @deprecated Please replace with {@link #runUiTest(KeepBrowserOpen, boolean)}
	 * @param keepBrowserOpen see {@link KeepBrowserOpen}
	 * @param isLogResultsSummary see {@link JmeterFunctionsImpl#LOG_RESULTS_SUMMARY}
	 * @return {@link SampleResult}
	 */
	@Deprecated
	public SampleResult runSeleniumTest(KeepBrowserOpen keepBrowserOpen, boolean isLogResultsSummary) {
		return runUiTest(keepBrowserOpen,isLogResultsSummary);
	}

	/**
	 * @deprecated Please replace with {@link #runMultiThreadedUiTest(int, int)}
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs time between start of each thread in milliseconds
	 */
	@Deprecated
	public void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs) {
		runMultiThreadedUiTest(numberOfThreads, threadStartGapMs);
	}
	
	/**
	 * @deprecated Please replace with {@link #runMultiThreadedUiTest(int, int, KeepBrowserOpen)}
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs time between start of each thread in milliseconds
	 * @param keepBrowserOpen  see KeepBrowserOpen
	 */
	@Deprecated	
	public void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, KeepBrowserOpen keepBrowserOpen) {
		runMultiThreadedUiTest(numberOfThreads, threadStartGapMs, keepBrowserOpen);
		
	}
	
	/**
	 * @deprecated Please replace with {@link #runMultiThreadedUiTest(int, int, Map)}
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs  time between start of each thread in milliseconds
	 * @param threadParameters  parameter key and list of values to be passed to each thread (needs to be at least as many entries as number of threads) 
	 */
	@Deprecated	
	public void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters) {
		runMultiThreadedUiTest(numberOfThreads, threadStartGapMs, threadParameters);
	}	
	
	/**
	 * @deprecated Please replace with {@link #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen)}
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs  time between start of each thread in milliseconds
	 * @param threadParameters  parameter key and list of values to be passed to each thread (needs to be at least as many entries as number of threads) 
	 * @param keepBrowserOpen  see KeepBrowserOpen	 
	 */
	@Deprecated		
	public void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters, KeepBrowserOpen keepBrowserOpen) {
		runMultiThreadedUiTest(numberOfThreads, threadStartGapMs, threadParameters, keepBrowserOpen);
	}
	
	/**
	 * @deprecated Please replace with {@link #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File)}
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs  time between start of each thread in milliseconds
	 * @param threadParameters  parameter key and list of values to be passed to each thread (override existing or create new parameters). 
	 * Needs to be at least as many entries as number of threads, or set to null when no extra parameterization required
	 * @param keepBrowserOpen  see KeepBrowserOpen	 
	 * @param iterateEachThreadCount  number of times to iterate each of the threads	 
	 * @param iteratePacingGapMs  gap between script iterations in milliseconds	
	 * @param printResultsSummary <code>true</code> to print the summary report, <code>false</code> to not.	  
	 * @param jmeterResultsFile  output file name.  Set to <code>null</code> if a file is not required.    
	 */
	@Deprecated		
	public void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters, KeepBrowserOpen keepBrowserOpen,
			int iterateEachThreadCount, int iteratePacingGapMs, boolean printResultsSummary, File jmeterResultsFile) {
		runMultiThreadedUiTest(numberOfThreads, threadStartGapMs, threadParameters, keepBrowserOpen,
				iterateEachThreadCount, iteratePacingGapMs, printResultsSummary,jmeterResultsFile);
	}

}
