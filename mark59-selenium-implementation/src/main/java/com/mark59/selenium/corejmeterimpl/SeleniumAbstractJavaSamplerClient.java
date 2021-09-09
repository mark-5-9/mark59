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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.selenium.drivers.SeleniumDriverFactory;
import com.mark59.selenium.drivers.SeleniumDriverWrapper;



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
 * @see SeleniumDriverFactory#getDriverBuilderOfType  
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
	
	static {  // block websocket closed warnings in JMeter   
		org.apache.logging.log4j.core.config.Configurator.setLevel("org.asynchttpclient.netty.handler", Level.ERROR);
		org.apache.logging.log4j.core.config.Configurator.setLevel("org.openqa.selenium.remote.http", Level.ERROR);
		
		String logConfig = "handlers= java.util.logging.ConsoleHandler\n" + ".level= WARNING\n";
		logConfig += "java.util.logging.ConsoleHandler.level = WARNING\n";
		logConfig += "org.openqa.selenium.remote.http.level = SEVERE\n";
		logConfig += "org.asynchttpclient.netty.handler.level = SEVERE\n";

		try {
			java.util.logging.LogManager.getLogManager().readConfiguration(new java.io.ByteArrayInputStream(logConfig.getBytes("UTF-8")));
		} catch (IOException e) {
			System.err.println("Failed to configure override java.util.logging : " + logConfig + "\nError : " + e.getMessage());
		}
	}
	
	public static Logger LOG = LogManager.getLogger(SeleniumAbstractJavaSamplerClient.class);	

	protected Arguments jmeterArguments = new Arguments();
	protected JmeterFunctionsForSeleniumScripts jm;	
	protected SeleniumDriverWrapper seleniumDriverWrapper; 
	protected WebDriver driver;
	protected String thread = Thread.currentThread().getName();
	protected String tgName = null; 
	protected AbstractThreadGroup tg = null;
	
	private KeepBrowserOpen keepBrowserOpen = KeepBrowserOpen.NEVER;

	protected static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<String,String>();
		
		staticMap.put("______________________ driver settings: ________________________", "Refer Mark59 User Guide : http://mark59.com");	
		staticMap.put(SeleniumDriverFactory.DRIVER,						Mark59Constants.DEFAULT_DRIVER);
		staticMap.put(SeleniumDriverFactory.HEADLESS_MODE, 				String.valueOf(true));
		staticMap.put(SeleniumDriverFactory.PAGE_LOAD_STRATEGY, 		PageLoadStrategy.NORMAL.toString());
		staticMap.put(SeleniumDriverFactory.BROWSER_DIMENSIONS, 		Mark59Constants.DEFAULT_BROWSER_DIMENSIONS);
		staticMap.put(SeleniumDriverFactory.PROXY, 						"");
		staticMap.put(SeleniumDriverFactory.ADDITIONAL_OPTIONS, 		"");				
		staticMap.put(SeleniumDriverFactory.WRITE_FFOX_BROWSER_LOGFILE, String.valueOf(false));		
		
		staticMap.put("______________________ logging settings: _______________________", "Expected values: 'default', 'buffer', 'write' or 'off' ");		
		staticMap.put(SeleniumDriverWrapper.LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS,	SeleniumDriverWrapper.DEFAULT);
		staticMap.put(SeleniumDriverWrapper.LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS, 	SeleniumDriverWrapper.DEFAULT);
		staticMap.put(SeleniumDriverWrapper.LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS,	SeleniumDriverWrapper.DEFAULT);
		staticMap.put(SeleniumDriverWrapper.LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS, 	SeleniumDriverWrapper.DEFAULT);
		staticMap.put(SeleniumDriverWrapper.LOG_PERF_LOG_AT_END_OF_TRANSACTIONS, 		SeleniumDriverWrapper.DEFAULT);
		
		staticMap.put("______________________ miscellaneous: __________________________", "");				
		staticMap.put(IpUtilities.RESTRICT_TO_ONLY_RUN_ON_IPS_LIST, "");
		staticMap.put(SeleniumDriverFactory.EMULATE_NETWORK_CONDITIONS, "");		
		
		staticMap.put("___________________"       , "");			
		staticMap.put("script build information: ", "using mark59-selenium-implementation version " + Mark59Constants.MARK59_VERSION);	
		
		defaultArgumentsMap = Collections.unmodifiableMap(staticMap);
	}
	
	
	
	
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
	 * @see SeleniumDriverFactory#getDriverBuilderOfType  
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
	 *  Note the use of the catch on  AssertionError - as this is NOT an Exception but an Error, and therefore need to be explicitly caught. 
	 */
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		if (LOG.isDebugEnabled()) LOG.debug(this.getClass().getName() +  " : exectuing runTest" );

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

		driver =  (WebDriver)seleniumDriverWrapper.getDriverPackage() ;
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
		
	};

		
	
	/**
	 * Method to be implemented containing the actual test steps. 
	 * 
	 * @param context the current JavaSamplerContext
	 * @param jm the current JmeterFunctionsForSeleniumScripts  
	 * @param driver the current WebDriver
	 */
	protected abstract void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver);

	
	
	protected Map<String,String> convertJmeterArgumentsToMap(JavaSamplerContext context) {
		Map<String, String> jmeterArgumentsAsMap = new HashMap<>();
		
		for (Iterator<String> iterator = context.getParameterNamesIterator(); iterator.hasNext();) {
			String paramKey = (String) iterator.next();
			jmeterArgumentsAsMap.put(paramKey, context.getParameter(paramKey) );
		}
		
		if (LOG.isDebugEnabled()) LOG.debug("context parameters at convert... :  "  + Arrays.toString(jmeterArgumentsAsMap.entrySet().toArray()));
		return jmeterArgumentsAsMap;
	}

	
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
	 * @see com.mark59.selenium.corejmeterimpl.KeepBrowserOpen
	 * @see Log4jConfigurationHelper
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * @see #runMultiThreadedSeleniumTest(int, int, Map)
	 * @param keepBrowserOpen  see KeepBrowserOpen
	 * @return {@link SampleResult} 
	 */
	public SampleResult runSeleniumTest(KeepBrowserOpen keepBrowserOpen ) {
		mockJmeterProperties();
		JavaSamplerContext context = new JavaSamplerContext( getDefaultParameters()  );
		
		this.keepBrowserOpen = keepBrowserOpen;
		if (String.valueOf(true).equalsIgnoreCase(context.getParameter(SeleniumDriverFactory.HEADLESS_MODE))) {
			this.keepBrowserOpen = KeepBrowserOpen.NEVER;
		};
		LOG.debug("keepBrowserOpen is set to "+ this.keepBrowserOpen);
		
		setupTest(context);
		SampleResult sampleResult  = runTest(context);
		return sampleResult;
	}


	/**
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * @see #runMultiThreadedSeleniumTest(int, int, Map)	  
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
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int, Map)	 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs time between start of each thread in milliseconds
	 */
	protected void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs) {
		runMultiThreadedSeleniumTest(numberOfThreads, threadStartGapMs, new HashMap<String,List<String>>(), KeepBrowserOpen.NEVER);
	}

	/**
	 * Convenience method to directly execute multiple script threads (rather than needing to use JMeter).  
	 * For example: <br><br>
	 * <code>thisTest.runMultiThreadedSeleniumTest(2, 2000, KeepBrowserOpen.ONFAILURE);</code>
	 * 
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int, Map)	 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs time between start of each thread in milliseconds
	 * @param keepBrowserOpen  see KeepBrowserOpen
	 */
	protected void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, KeepBrowserOpen keepBrowserOpen) {
		runMultiThreadedSeleniumTest(numberOfThreads, threadStartGapMs, new HashMap<String,List<String>>(), keepBrowserOpen);
		
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
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs  time between start of each thread in milliseconds
	 * @param threadParameters  parameter key and list of values to be passed to each thread (needs to be at least as many entries as number of threads) 
	 */
	protected void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters) {
		runMultiThreadedSeleniumTest(numberOfThreads, threadStartGapMs, threadParameters, KeepBrowserOpen.NEVER);
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
	 * @see #runSeleniumTest(KeepBrowserOpen)
	 * @see #runMultiThreadedSeleniumTest(int, int)
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs  time between start of each thread in milliseconds
	 * @param threadParameters  parameter key and list of values to be passed to each thread (needs to be at least as many entries as number of threads) 
	 * @param keepBrowserOpen  see KeepBrowserOpen	 
	 */
	protected void runMultiThreadedSeleniumTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters, KeepBrowserOpen keepBrowserOpen) {
		mockJmeterProperties();
		Map<String, String> thisThreadParameters = new LinkedHashMap<String,String>();
		
		
		for (int i = 1; i <= numberOfThreads; i++) {

			for (Entry<String, List<String>> entry : threadParameters.entrySet()) {
				if ( entry.getValue().size() >= i) {
					thisThreadParameters.put(entry.getKey() , entry.getValue().get(i-1));
				}
			}	
			if (!thisThreadParameters.isEmpty()){
				LOG.info(" Thread Override Parameters for thread " + String.format("%03d", i) + " : " +  Arrays.toString(thisThreadParameters.entrySet().toArray()));
			}
			
			new Thread(new SeleniumTestThread(this.getClass(), thisThreadParameters, keepBrowserOpen), String.format("%03d", i)).start();
			
			if (i<numberOfThreads) {
				try { Thread.sleep(threadStartGapMs);} catch (InterruptedException e){e.printStackTrace();}
			}
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

		private Class<? extends SeleniumAbstractJavaSamplerClient> testClass;
		private Map<String, String> thisThreadParametersOverride;  
		private KeepBrowserOpen keepBrowserOpen;  

		
		public SeleniumTestThread(Class<? extends SeleniumAbstractJavaSamplerClient> testClass, Map<String, String> thisThreadParametersOverride, KeepBrowserOpen keepBrowserOpen) {
			this.testClass = testClass;
			this.thisThreadParametersOverride = thisThreadParametersOverride;
			this.keepBrowserOpen = keepBrowserOpen;
		}

		/**
		 *
		 */
		public void run() {
			SeleniumAbstractJavaSamplerClient testInstance = null;
			try {
				testInstance = testClass.getDeclaredConstructor().newInstance();
			} catch (Exception e) {	e.printStackTrace(); System.out.println(" Error " + e.getMessage()  ); } 
			
			Arguments thisThreadParameterAuguments = Mark59Utils.mergeMapWithAnOverrideMap(getDefaultParameters().getArgumentsAsMap(), thisThreadParametersOverride);
			
			JavaSamplerContext context = new JavaSamplerContext( thisThreadParameterAuguments  );
			
			if (String.valueOf(true).equalsIgnoreCase(context.getParameter(SeleniumDriverFactory.HEADLESS_MODE))) {
				this.keepBrowserOpen = KeepBrowserOpen.NEVER;
			};
			
			testInstance.setKeepBrowserOpen(keepBrowserOpen);
			testInstance.setupTest(context);
			testInstance.runTest(context);
		}
	}
	
}
