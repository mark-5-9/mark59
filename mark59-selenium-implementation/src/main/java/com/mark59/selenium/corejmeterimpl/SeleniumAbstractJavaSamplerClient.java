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

package com.mark59.selenium.corejmeterimpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;

import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.SafeSleep;
import com.mark59.selenium.drivers.SeleniumDriverFactory;
import com.mark59.selenium.drivers.SeleniumDriverWrapper;

import jodd.util.CsvUtil;



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
 * @see SeleniumDriverFactory#makeDriverWrapper(Map)
 * @see SeleniumDriverFactory#SeleniumDriverFactory()
 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#HEADLESS_MODE 
 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setHeadless(boolean)  
 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#PAGE_LOAD_STRATEGY 
 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setPageLoadStrategy(PageLoadStrategy) 
 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#BROWSER_DIMENSIONS 
 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setSize(int width, int height) 
 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#PROXY 
 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setProxy(org.openqa.selenium.Proxy) 
 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#ADDITIONAL_OPTIONS 
 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setAdditionalOptions(java.util.List) 
 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#WRITE_FFOX_BROWSER_LOGFILE 
 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setWriteBrowserLogfile(boolean)
 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#BROWSER_EXECUTABLE  
 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setAlternateBrowser(java.nio.file.Path) 
 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#EMULATE_NETWORK_CONDITIONS 
 * @see IpUtilities#localIPisNotOnListOfIPaddresses(String)   
 * @see JmeterFunctionsForSeleniumScripts
 *
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public abstract class SeleniumAbstractJavaSamplerClient extends AbstractJavaSamplerClient {
	
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
	/**  the Selenium driver 'Wrapper' for the test  */	
	protected SeleniumDriverWrapper seleniumDriverWrapper; 
	/**  the Selenium Web Driver for the test  */
	protected WebDriver driver;

	/**
	 *  Default arguments for Selenium scripts
	 */
	protected static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<>();
		
		staticMap.put("______________________ driver settings: ________________________", "Refer Mark59 User Guide : http://mark59.com");	
		staticMap.put(SeleniumDriverFactory.DRIVER,						Mark59Constants.DEFAULT_DRIVER);
		staticMap.put(SeleniumDriverFactory.HEADLESS_MODE, 				String.valueOf(true));
		staticMap.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, 		PageLoadStrategy.NORMAL.toString());
		staticMap.put(SeleniumDriverFactory.BROWSER_DIMENSIONS, 		Mark59Constants.DEFAULT_BROWSER_DIMENSIONS);
		staticMap.put(SeleniumDriverFactory.PROXY, 						"");
		staticMap.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, 		"");				
		staticMap.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, String.valueOf(false));		
		
		staticMap.put("______________________ logging settings: _______________________", "Expected values: 'default', 'buffer', 'write' or 'off' ");		
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS,	Mark59LogLevels.DEFAULT.getName() );
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS,	Mark59LogLevels.DEFAULT.getName());
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS, 	Mark59LogLevels.DEFAULT.getName());
		staticMap.put(JmeterFunctionsForSeleniumScripts.LOG_PERF_LOG_AT_END_OF_TRANSACTIONS, 		Mark59LogLevels.DEFAULT.getName());
		
		staticMap.put("______________________ miscellaneous: __________________________", "");				
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		staticMap.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");		
		
		staticMap.put("___________________"       , "");			
		staticMap.put("script build information: ", "using mark59-selenium-implementation version " + Mark59Constants.MARK59_VERSION);	
		
		defaultArgumentsMap = Collections.unmodifiableMap(staticMap);
	}
	
	
	/**  used to output results table when running from a script Main() */
	protected static Map<String, List<Long>> resultsSummaryTable = new TreeMap<>();
	private static final int POS_0_NUM_SAMPLES  		= 0;	
	private static final int POS_1_NUM_FAIL  			= 1;	
	private static final int POS_2_SUM_RESPONSE_TIME 	= 2;	
	private static final int POS_3_RESPONSE_TIME_MIN 	= 3;	
	private static final int POS_4_RESPONSE_TIME_MAX	= 4;	
	
	private KeepBrowserOpen keepBrowserOpen = KeepBrowserOpen.NEVER;
	
	/** 
	 * Creates the list of parameters with default values, as they would appear on the JMeter GUI for the JavaSampler being implemented.
	 * <p>A standard set of parameters are defined (defaultArgumentsMap). Additionally,an implementing class (the script extending this class) 
	 * can add additional parameters (or override the standard defaults) via the additionalTestParameters() method.    
	 * @see #additionalTestParameters()
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient
	 */
	@Override
	public Arguments getDefaultParameters() {
		return Mark59Utils.mergeMapWithAnOverrideMap(defaultArgumentsMap, additionalTestParameters());
	}


	@Override
	public void setupTest(JavaSamplerContext context) {
		super.setupTest(context);
	}

	
	/**
	 * Used to define required parameters for the test, or override their default values.
	 * <p>Internally the values are used to build a Map of parameters that will be available throughout the
	 *  'Mark59' framework for whatever customization is required for your test, or for the Webdriver implementation.</p>
	 * <p>Please see link(s) below for more detail.  
	 * 
	 * @see SeleniumDriverFactory#makeDriverWrapper(Map)
	 * @see SeleniumDriverFactory#SeleniumDriverFactory()
	 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#HEADLESS_MODE
	 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setHeadless(boolean)  
	 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#PAGE_LOAD_STRATEGY 
	 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setPageLoadStrategy(PageLoadStrategy) 
	 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#BROWSER_DIMENSIONS 
	 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setSize(int width, int height) 
	 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#PROXY 
	 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setProxy(org.openqa.selenium.Proxy) 
	 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#ADDITIONAL_OPTIONS 
	 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setAdditionalOptions(java.util.List) 
	 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#WRITE_FFOX_BROWSER_LOGFILE 
	 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setWriteBrowserLogfile(boolean)
	 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#BROWSER_EXECUTABLE  
	 * @see com.mark59.selenium.drivers.SeleniumDriverBuilder#setAlternateBrowser(java.nio.file.Path) 
	 * @see com.mark59.selenium.drivers.SeleniumDriverFactory#EMULATE_NETWORK_CONDITIONS 
	 * @see IpUtilities#localIPisNotOnListOfIPaddresses(String)   
	 * @see JmeterFunctionsForSeleniumScripts
	 * 
	 * @return the updated map of JMeter arguments with any required changes
	 */
	protected abstract Map<String, String> additionalTestParameters();
		

	/**
	 * {@inheritDoc}
	 * 
	 *  Note the use of the catch on AssertionError - as this is NOT an Exception but an Error, and therefore needs
	 *  to be explicitly caught. 
	 */
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		if (LOG.isDebugEnabled()) LOG.debug(this.getClass().getName() +  " : exectuing runTest" );
		
		AbstractThreadGroup tg = null;
		String tgName = null;

		if ( context.getJMeterContext() != null  && context.getJMeterContext().getThreadGroup() != null ) {
			tg     = context.getJMeterContext().getThreadGroup();
			tgName = tg.getName();
		}
		
		if (IpUtilities.localIPisNotOnListOfIPaddresses(context.getParameter(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST))){ 
			LOG.info("Thread Group " + tgName + " is stopping (not on 'Restrict to IP List')" );
			if (tg!=null) tg.stop();
			return null;
		}
	
		Map<String,String> jmeterRuntimeArgumentsMap = convertJmeterArgumentsToMap(context);

		try {
			seleniumDriverWrapper = new SeleniumDriverFactory().makeDriverWrapper(jmeterRuntimeArgumentsMap) ;
		} catch (Exception e) {
			LOG.error("ERROR : " + this.getClass() + ". Fatal error has occured for Thread Group " + tgName
					+ " while attempting to initiate the selenium Driver!" );
			LOG.error(e.getMessage());
			e.printStackTrace();			
			return null;
		}

		driver = seleniumDriverWrapper.getDriverPackage();
		jm = new JmeterFunctionsForSeleniumScripts(Thread.currentThread().getName(), seleniumDriverWrapper, jmeterRuntimeArgumentsMap);   	
		
		try {
			
			LOG.debug(">> running test ");			
			
			runSeleniumTest(context, jm, driver);
			
			jm.tearDown();
		
			LOG.debug("<< finished test" );

		} catch (Exception | AssertionError e) {

			scriptExceptionHandling(context, e);
		
		} finally {
			if (! keepBrowserOpen.equals(KeepBrowserOpen.ALWAYS )     ) { 
				seleniumDriverWrapper.driverDispose();
			}
		}
		return jm.getMainResult();
	}

	

	/**
	 * Log and record this script execution as a failure.
	 * 
	 * @see #userActionsOnScriptFailure(JavaSamplerContext, JmeterFunctionsForSeleniumScripts, WebDriver)
	 * @param context the current JavaSamplerContext  
	 * @param e can be an exception or Assertion error
	 */
	protected void scriptExceptionHandling(JavaSamplerContext context, Throwable e) {
		String thread = Thread.currentThread().getName();
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		
		System.err.println("["+ thread + "]  ERROR : " + this.getClass() + ". See screenshot directory for details. Stack trace: \n  " + sw.toString());
		LOG.error("["+ thread + "]  ERROR : " + this.getClass() + ". See screenshot directory for details. Stack trace: \n  " + sw.toString());

		try {
			seleniumDriverWrapper.documentExceptionState(new Exception(e));
		} catch (Exception ex) {
			LOG.error("["+ thread + "]  ERROR : " + this.getClass() + ".  An exception occured during scriptExceptionHandling (documentExceptionState) " 
					+  ex.getClass().getName() +  " thrown",  e);
			ex.printStackTrace();
		}	
		
		try {
			userActionsOnScriptFailure(context, jm, driver); 
		} catch (Exception errorHandlingException) {
			LOG.error("["+ thread + "]  ERROR : " + this.getClass() + ".  An exception occured during scriptExceptionHandling (userActionsOnScriptFailure) "
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
	 * @param driver the current WebDriver
	 */
	protected void userActionsOnScriptFailure(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver) {
		
	}


	/**
	 * Method to be implemented containing the actual test steps. 
	 * 
	 * @param context the current JavaSamplerContext
	 * @param jm the current JmeterFunctionsForSeleniumScripts  
	 * @param driver the current WebDriver
	 */
	protected abstract void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver);

	
	
	/**
	 * @param context the JavaSamplerContext (used by JMeter)
	 * @return a map of the script arguments (retrieved from the context 'parameters')
	 */
	protected Map<String,String> convertJmeterArgumentsToMap(JavaSamplerContext context) {
		Map<String, String> jmeterArgumentsAsMap = new HashMap<>();
		
		for (Iterator<String> iterator = context.getParameterNamesIterator(); iterator.hasNext();) {
			String paramKey = iterator.next();
			jmeterArgumentsAsMap.put(paramKey, context.getParameter(paramKey) );
		}
		
		if (LOG.isDebugEnabled()) LOG.debug("context parameters at convert... :  "  + Arrays.toString(jmeterArgumentsAsMap.entrySet().toArray()));
		return jmeterArgumentsAsMap;
	}

	
	/**
	 * @return the keepBrowserOpen value in use 
	 */
	public KeepBrowserOpen getKeepBrowserOpen() {
		return keepBrowserOpen;
	}

	/**
	 * By default a browser will close at the end of a JMeter iteration.  This can be changed via this setter:<br>
	 * <b>KeepBrowserOpen.ALWAYS</b>, <b>KeepBrowserOpen.ONFAILURE</b>, default is <b>KeepBrowserOpen.NEVER</b><br>
	 * Not expected to be used in the normal course of events (and pretty pointless if the browser is headless).   
	 * @param keepBrowserOpen NEVER,ONFAILURE,ALWAYS 
	 */
	public void setKeepBrowserOpen(KeepBrowserOpen keepBrowserOpen) {
		this.keepBrowserOpen = keepBrowserOpen;
	}

	
	/**
	 * Convenience method to a single-threaded script execution (rather than needing to use JMeter).  
	 * <p>It can also be used in a JRS223 sampler in order to execute a script written directly in JMeter. See  the 
	 * DataHunterLifecyclePvtScriptAsSingleJSR223 thread group in the the DataHunterSeleniumTestPlan.jmx test plan and
	 * com.mark59.datahunter.performanceTest.scripts.jsr223format package in the dataHunterPerformanceTestSamples project.    
	 * <p>See method runMultiThreadedSeleniumTest to executed a multi-thread test
	 * <p>You can control if the browser closes at the end of the test. 
	 * EG: <b>KeepBrowserOpen.ONFAILURE</b> will keep the browser open at test end if the test fails (unless running in headless mode). 
	 * 
	 * @see #runSeleniumTest()
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * @see #runMultiThreadedSeleniumTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @see com.mark59.selenium.corejmeterimpl.KeepBrowserOpen
	 * @see Log4jConfigurationHelper
	 * @param keepBrowserOpen  see KeepBrowserOpen
	 * @return {@link SampleResult} 
	 */
	public SampleResult runSeleniumTest(KeepBrowserOpen keepBrowserOpen ) {
		mockJmeterProperties();
		JavaSamplerContext context = new JavaSamplerContext( getDefaultParameters()  );
		
		this.keepBrowserOpen = keepBrowserOpen;
		if (String.valueOf(true).equalsIgnoreCase(context.getParameter(SeleniumDriverFactory.HEADLESS_MODE))) {
			this.keepBrowserOpen = KeepBrowserOpen.NEVER;
		}
		LOG.debug("keepBrowserOpen is set to "+ this.keepBrowserOpen);
		
		setupTest(context);
		return runTest(context);
	}


	/**
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * @see #runMultiThreadedSeleniumTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * @return {@link SampleResult}
	 */
	protected SampleResult runSeleniumTest() {
		return runSeleniumTest(KeepBrowserOpen.ONFAILURE);
	}

	/**
	 * Convenience method to directly execute multiple script threads (rather than needing to use JMeter).  
	 * For example: <br><br>
	 * <code>thisTest.runMultiThreadedSeleniumTest(2, 2000);</code>
	 * 
	 * @see #runSeleniumTest()
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs time between start of each thread in milliseconds
	 */
	protected void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs) {
		runMultiThreadedSeleniumTest(numberOfThreads, threadStartGapMs, new HashMap<>(), KeepBrowserOpen.NEVER, 1, 0, false, null);
	}

	/**
	 * Convenience method to directly execute multiple script threads (rather than needing to use JMeter).  
	 * For example: <br><br>
	 * <code>thisTest.runMultiThreadedSeleniumTest(2, 2000, KeepBrowserOpen.ONFAILURE);</code>
	 * 
	 * @see #runSeleniumTest()
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * @see #runMultiThreadedSeleniumTest(int, int, Map) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs time between start of each thread in milliseconds
	 * @param keepBrowserOpen  see KeepBrowserOpen
	 */
	protected void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, KeepBrowserOpen keepBrowserOpen) {
		runMultiThreadedSeleniumTest(numberOfThreads, threadStartGapMs, new HashMap<>(), keepBrowserOpen, 1, 0, false, null);
		
	}
	
	
	/**
	 * Convenience method to directly execute multiple script threads (rather than needing to use JMeter).  For example,
	 * if you want to user a user-defined parameter called "<code>USER</code>", and switch off headless mode for one of four threads running:  
	 * <br><br><code>
	 *  Map&lt;String, java.util.List&lt;String&gt;&gt;threadParameters = new java.util.LinkedHashMap&lt;String,java.util.List&lt;String&gt;&gt;();<br>
	 *	threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));<br>
	 *	threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "true"));<br>		
	 *	thisTest.runMultiThreadedSeleniumTest(4, 2000, threadParameters);
	 * </code>  
	 *  
	 * @see #runSeleniumTest()
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * @see #runMultiThreadedSeleniumTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs  time between start of each thread in milliseconds
	 * @param threadParameters  parameter key and list of values to be passed to each thread (needs to be at least as many entries as number of threads) 
	 */
	protected void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters) {
		runMultiThreadedSeleniumTest(numberOfThreads, threadStartGapMs, threadParameters, KeepBrowserOpen.NEVER, 1, 0, false, null );
	}	
	
	
	/**
	 * Convenience method to directly execute multiple script threads (rather than needing to use JMeter).  For example,
	 * if you want to user a user-defined parameter called "<code>USER</code>", and switch off headless mode for one of four threads running:  
	 * <br><br><code>
	 *  Map&lt;String, java.util.List&lt;String&gt;&gt;threadParameters = new java.util.LinkedHashMap&lt;String,java.util.List&lt;String&gt;&gt;();<br>
	 *	threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));<br>
	 *	threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "true"));<br>		
	 *	thisTest.runMultiThreadedSeleniumTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE);
	 * </code>  
	 *  
	 * @see #runSeleniumTest()
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * @see #runMultiThreadedSeleniumTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs  time between start of each thread in milliseconds
	 * @param threadParameters  parameter key and list of values to be passed to each thread (needs to be at least as many entries as number of threads) 
	 * @param keepBrowserOpen  see KeepBrowserOpen	 
	 */
	protected void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters, KeepBrowserOpen keepBrowserOpen) {
		runMultiThreadedSeleniumTest(numberOfThreads, threadStartGapMs, threadParameters, KeepBrowserOpen.NEVER, 1, 0, false, null);
	}

	
	
	/**
	 * 'Full Monty' convenience method to directly execute multiple script threads.  The threads can be set to run a given number of iterations, 
	 * with a timed gap between each iteration.   Additionally you can print a transactions summary table, and/or output a CSV file with the results
	 * of the run. 
	 * <p>As the CSV file is in JMeter format, you can use it to generate a JMeter report or load into Trend Analysis. Note the intention here is not 
	 * to try to replace a proper JMeter test, but you may find a low-volume use case where this is a useful trick.
	 * <p><b>Sample usage</b>         
	 * <p> For example,
	 * if you want to user a user-defined parameter called "<code>USER</code>", and switch off headless mode for one of four threads running, you need
	 * to create a map:  
	 * <br><br><code>
	 *  Map&lt;String, java.util.List&lt;String&gt;&gt;threadParameters = new java.util.LinkedHashMap&lt;String,java.util.List&lt;String&gt;&gt;();<br>
	 *	threadParameters.put("USER",                              java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));<br>
	 *	threadParameters.put(SeleniumDriverFactory.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "true"));<br>
	 * </code>
	 * <p>Then, to run the 4 threads starting 2000ms apart, with each thread iterating the script 3 times having a 1500ms gap between each iteration, 
	 * and printing out a summary report and the CSV file to 'C:/Mark59_Runs/csvSample.csv' (Win machine) at the end : <br><br>
	 * <b><code>  		
	 * thisTest.runMultiThreadedSeleniumTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE, 3, 1500, true, new File("C:/Mark59_Runs/csvSample.csv"));
	 * </code></b> 
	 * @see #runSeleniumTest()
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * @see #runMultiThreadedSeleniumTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map) 
	 * @see #runMultiThreadedSeleniumTest(int, int, Map, KeepBrowserOpen) 
	 * 
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
	protected void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters, KeepBrowserOpen keepBrowserOpen,
			int iterateEachThreadCount, int iteratePacingGapMs, boolean printResultsSummary, File jmeterResultsFile) {
		
		mockJmeterProperties();
		Thread[] threadAry = new Thread[numberOfThreads];
		PrintWriter csvPrintWriter = null;
		
		if (jmeterResultsFile != null ) {
			try {
				FileOutputStream jmeterResultsFOS = FileUtils.openOutputStream(jmeterResultsFile);
				csvPrintWriter = new PrintWriter(new OutputStreamWriter(jmeterResultsFOS));
				
				String csvLine = CsvUtil.toCsvString("timeStamp","elapsed","label","responseCode","responseMessage","threadName","dataType","success",
						"failureMessage","bytes","sentBytes", "grpThreads","allThreads","URL","Latency","Hostname","IdleTime","Connect");
				csvPrintWriter.println(csvLine);
				
			} catch (IOException e) {
				System.err.println(" Unable to open/create csv file " + jmeterResultsFile.getName() + " : " + e.getMessage() );
				e.printStackTrace();
			}
		}
		
		for (int i = 1; i <= numberOfThreads; i++) {
			
			Map<String, String> thisThreadParameters = new LinkedHashMap<>();

			if (threadParameters != null ) {  // null means no parameters passed
				for (Entry<String, List<String>> entry : threadParameters.entrySet()) {
					if ( entry.getValue().size() >= i) {
						thisThreadParameters.put(entry.getKey() , entry.getValue().get(i-1));
					}
				}	
			}
			
			if (!thisThreadParameters.isEmpty()){
				LOG.info(" Thread Override Parameters for thread " + String.format("%03d", i) +" : "+ Arrays.toString(thisThreadParameters.entrySet().toArray()));
			}

			Thread thread = new Thread(new SeleniumTestThread(this.getClass(), thisThreadParameters, keepBrowserOpen, iterateEachThreadCount, iteratePacingGapMs,
					printResultsSummary, csvPrintWriter), String.format("%03d", i));			
			
			thread.start();
			threadAry[i-1] = thread;
			
			if (i<numberOfThreads) {
				SafeSleep.sleep(threadStartGapMs);
			}
		}

		for(int i = 0; i < numberOfThreads; i++) {
			try {
				threadAry[i].join();
			} catch (InterruptedException e) {
				System.err.println("Interrupted thread join : " + e.getMessage()); e.printStackTrace();
			}
		} // all threads have completed when loop ends

		if (csvPrintWriter != null ) {
			csvPrintWriter.flush();
			csvPrintWriter.close();
		}
		if (printResultsSummary) {
			printResultsSummary(resultsSummaryTable);
		}
	}
	
	
	/**
	 * Sets JMeter properties if a jmeter.properties file is provided.  Only meant for use 
	 * during script testing (eg in an IDE).  Prevents log WARNings for non-existent properties.<p>
	 * If JMeter properties become relevant to a particular script for some reason, it is suggested the required 
	 * 'jmeter.properties' file be included in the root of the project.
	 */
	protected void mockJmeterProperties() { 
		File f = new File("./jmeter.properties");
		if(f.exists() && !f.isDirectory()) { 
			LOG.debug("loading supplied jmeter.properties file");
			JMeterUtils.loadJMeterProperties("./jmeter.properties");   	
		}
	}
	
	
	/**
	 * Convenience inner class to enable multi-thread testing outside of JMeter
	 */
	public class SeleniumTestThread implements Runnable {

		private final Class<? extends SeleniumAbstractJavaSamplerClient> testClass;
		private final Map<String, String> thisThreadParametersOverride;
		private KeepBrowserOpen keepBrowserOpen;
		private final int iterateEachThreadCount;
		private final int iteratePacingGapMs;
		private final boolean printResultsSummary;
		private final PrintWriter csvPrintWriter;
		
		/**
		 * @param testClass testClass
		 * @param thisThreadParametersOverride map of parm overrides
		 * @param keepBrowserOpen keep browser open enum
		 * @param iterateEachThreadCount count of thread iterations
		 * @param iteratePacingGapMs gap between iterations
		 * @param printResultsSummary choose to print summary
		 * @param csvPrintWriter print results to csv format file 
		 */
		public SeleniumTestThread(Class<? extends SeleniumAbstractJavaSamplerClient> testClass,	Map<String, String> thisThreadParametersOverride, 
				KeepBrowserOpen keepBrowserOpen, int iterateEachThreadCount, int iteratePacingGapMs, boolean printResultsSummary, PrintWriter csvPrintWriter) {
			this.testClass = testClass;
			this.thisThreadParametersOverride = thisThreadParametersOverride;
			this.keepBrowserOpen = keepBrowserOpen;
			this.iterateEachThreadCount = iterateEachThreadCount; 
			this.iteratePacingGapMs = iteratePacingGapMs;
			this.printResultsSummary = printResultsSummary; 
			this.csvPrintWriter = csvPrintWriter;
		}

		/**
		 *  run a SeleniumTestThread
		 */
		@Override
		public void run() {

			SeleniumAbstractJavaSamplerClient testInstance = null;
			try {
				testInstance = testClass.getDeclaredConstructor().newInstance();
			} catch (Exception e) {	e.printStackTrace(); System.out.println(" Error " + e.getMessage()); } 
			
			Arguments thisThreadParameterAuguments = Mark59Utils.mergeMapWithAnOverrideMap(getDefaultParameters().getArgumentsAsMap(), thisThreadParametersOverride);
			
			JavaSamplerContext context = new JavaSamplerContext( thisThreadParameterAuguments  );
			
			if (String.valueOf(true).equalsIgnoreCase(context.getParameter(SeleniumDriverFactory.HEADLESS_MODE))) {
				this.keepBrowserOpen = KeepBrowserOpen.NEVER;
			}

			assert testInstance != null;
			testInstance.setKeepBrowserOpen(keepBrowserOpen);
			testInstance.setupTest(context);
			
			for (int i = 1; i <= iterateEachThreadCount; i++) {
				SampleResult testInstanceSampleResult = testInstance.runTest(context);
				if (csvPrintWriter != null){
					writeTestInstanceSampleResult(testInstanceSampleResult, csvPrintWriter, Thread.currentThread().getName());
				}
				if (printResultsSummary){
					addResultsToSummaryTable(testInstanceSampleResult);
				}
				if (i<iterateEachThreadCount) {
					SafeSleep.sleep(iteratePacingGapMs);
				}
			}
		}

	}
	
	
	private synchronized static void writeTestInstanceSampleResult(SampleResult testInstanceSampleResult, PrintWriter csvPrintWriter, String originatingThread) {
		
		for (SampleResult subResult : testInstanceSampleResult.getSubResults()) {
			
			Boolean success = false; 
			if (Outcome.PASS.getOutcomeText().equalsIgnoreCase(subResult.getResponseMessage())){
				success = true; 
			}

			String csvLine = CsvUtil.toCsvString(String.valueOf(subResult.getTimeStamp()) , String.valueOf(subResult.getTime()),
					subResult.getSampleLabel(),	subResult.getResponseCode(),subResult.getResponseMessage(), "localthread_" + originatingThread, 
					subResult.getDataType(), String.valueOf(success), "", "0", "0", String.valueOf(subResult.getGroupThreads()),
					String.valueOf(subResult.getAllThreads()), "null", "0", "local", "0", "0" );
			
			csvPrintWriter.println(csvLine);
		}
		csvPrintWriter.flush();
	}

	
	private synchronized static void addResultsToSummaryTable(SampleResult testInstanceSampleResult) {
		List<Long> summaryTableTxnData; 
		
		for (SampleResult subResult : testInstanceSampleResult.getSubResults()) {

			String summaryTableTxn = subResult.getSampleLabel();
			
			if (StringUtils.isNotBlank(subResult.getDataType())){
				summaryTableTxn = summaryTableTxn + " (" + subResult.getDataType() + ")";
			}		
		
			summaryTableTxnData = resultsSummaryTable.get(summaryTableTxn);
			if (summaryTableTxnData == null) {
				summaryTableTxnData = Arrays.asList(0L, 0L, 0L, null, 0L);
			}
			
			if (Outcome.PASS.getOutcomeText().equalsIgnoreCase(subResult.getResponseMessage())){
				summaryTableTxnData.set(POS_0_NUM_SAMPLES,       summaryTableTxnData.get(POS_0_NUM_SAMPLES)+1 );
				summaryTableTxnData.set(POS_2_SUM_RESPONSE_TIME, summaryTableTxnData.get(POS_2_SUM_RESPONSE_TIME) + subResult.getTime());
				
				if  (summaryTableTxnData.get(POS_3_RESPONSE_TIME_MIN) == null || 
						subResult.getTime() < summaryTableTxnData.get(POS_3_RESPONSE_TIME_MIN)){
					summaryTableTxnData.set(POS_3_RESPONSE_TIME_MIN, subResult.getTime());
				}
				if  (subResult.getTime() > summaryTableTxnData.get(POS_4_RESPONSE_TIME_MAX)){
					summaryTableTxnData.set(POS_4_RESPONSE_TIME_MAX, subResult.getTime());
				}
			} else {
				summaryTableTxnData.set(POS_1_NUM_FAIL, summaryTableTxnData.get(POS_1_NUM_FAIL)+1);
			}
			
			resultsSummaryTable.put(summaryTableTxn, summaryTableTxnData);
		}
	}
	

	private static void printResultsSummary(Map<String, List<Long>> resultsSummaryTable) {
		
		LOG.info("\n\n\n"); 
		LOG.info(StringUtils.repeat(" ", 56) + "Results Summary Table");
		LOG.info(StringUtils.repeat(" ", 56) + "---------------------");
		LOG.info(""); 
		LOG.info(String.format("%-80s%-12s%-10s%-12s%-12s%-12s", "Transaction", "#Samples", "FAIL", "Average", "Min", "Max" ));
		LOG.info(String.format("%-80s%-12s%-10s%-12s%-12s%-12s", "-----------", "--------", "----", "-------", "---", "---" ));

		resultsSummaryTable.forEach((k, v) -> {
			if (k.length() < 76) {
				k = (k.length() % 2 == 0) ? k + "  " + StringUtils.repeat(" .", 38 - k.length()/2) : k + "  " + StringUtils.repeat(". ", 39 - (k.length()+1)/2 );
			}
			long average = (v.get(POS_0_NUM_SAMPLES) > 0) ? v.get(POS_2_SUM_RESPONSE_TIME) / v.get(POS_0_NUM_SAMPLES) : 0L;
			
			LOG.info(String.format("%-80s%-12s%-10s%-12s%-12s%-12s", k, 
					v.get(POS_0_NUM_SAMPLES),v.get(POS_1_NUM_FAIL),average,v.get(POS_3_RESPONSE_TIME_MIN),v.get(POS_4_RESPONSE_TIME_MAX)));
		});
		LOG.info(StringUtils.repeat("-", 132)); 		
		
		
		System.out.println("\n\n\n"); 
		System.out.println(StringUtils.repeat(" ", 56) + "Results Summary Table");
		System.out.println(StringUtils.repeat(" ", 56) + "---------------------");
		System.out.println();
		System.out.printf("%-80s%-12s%-10s%-12s%-12s%-12s%n", "Transaction", "#Samples", "FAIL", "Average", "Min", "Max" );
		System.out.printf("%-80s%-12s%-10s%-12s%-12s%-12s%n", "-----------", "--------", "----", "-------", "---", "---" );

		resultsSummaryTable.forEach((k, v) -> {
			if (k.length() < 76) {
				k = (k.length() % 2 == 0) ? k + "  " + StringUtils.repeat(" .", 38 - k.length()/2) : k + "  " + StringUtils.repeat(". ", 39 - (k.length()+1)/2 );
			}
			long average = (v.get(POS_0_NUM_SAMPLES) > 0) ? v.get(POS_2_SUM_RESPONSE_TIME) / v.get(POS_0_NUM_SAMPLES) : 0L;
			
			System.out.printf("%-80s%-12s%-10s%-12s%-12s%-12s%n", k,
					v.get(POS_0_NUM_SAMPLES),v.get(POS_1_NUM_FAIL),average,v.get(POS_3_RESPONSE_TIME_MIN),v.get(POS_4_RESPONSE_TIME_MAX));
		});
		System.out.println(StringUtils.repeat("-", 132));   // 132 because I'm a COBOL programmer really
	}

}
