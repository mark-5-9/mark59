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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.Outcome;
import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Log4jConfigurationHelper;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.SafeSleep;
import com.mark59.scripting.interfaces.JmeterFunctionsUi;
import com.mark59.scripting.playwright.PlaywrightAbstractJavaSamplerClient;
import com.mark59.scripting.selenium.SeleniumAbstractJavaSamplerClient;
//import com.mark59.scripting.selenium.driversimpl.SeleniumDriverFactory;

import jodd.util.CsvUtil;


/**
 * A Mark59 extension of the JMeter Java Sampler class {@link org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient}.  
 * This class contains core methods for the Mark59 implementation of UI scripting, to be extended to handle particular UI
 * implementations (Selenium,m Playwright).
 * 
 * <p>Those extended implementations of are expected to contain method(s) to be used with actual test scripts  
 * See the 'DataHunter' samples provided for implementation details. 
 *      
 * <p>Includes a few standard parameters, around core logging and reporting capabilities.</p>
 *
 * @see PlaywrightAbstractJavaSamplerClient   
 * @see SeleniumAbstractJavaSamplerClient   
 * @see IpUtilities#localIPisNotOnListOfIPaddresses(String)   
 * @see JmeterFunctionsUi
 * @see #UiScriptExecutionAndExceptionsHandling(JavaSamplerContext, Map, String)
 *
 * @author Philip Webb
 * Written: Australian Summer 2023/24  
 */
public abstract class UiAbstractJavaSamplerClient extends AbstractJavaSamplerClient {

	/** log4J class logger */
	public static final Logger LOG = LogManager.getLogger(UiAbstractJavaSamplerClient.class);	

	/**	@see UiAbstractJavaSamplerClient#UiScriptExecutionAndExceptionsHandling(JavaSamplerContext, Map, String)  */
	public static final String ON_EXCEPTION_WRITE_BUFFERED_LOGS	 		= "On_Exception_Write_Buffered_Logs";
	/**	@see UiAbstractJavaSamplerClient#UiScriptExecutionAndExceptionsHandling(JavaSamplerContext, Map, String)  */
	public static final String ON_EXCEPTION_WRITE_SCREENSHOT	 		= "On_Exception_Write_Screenshot";
	/**	@see UiAbstractJavaSamplerClient#UiScriptExecutionAndExceptionsHandling(JavaSamplerContext, Map, String)  */
	public static final String ON_EXCEPTION_WRITE_PAGE_SOURCE	 		= "On_Exception_Write_Page_Source";
	/**	@see UiAbstractJavaSamplerClient#UiScriptExecutionAndExceptionsHandling(JavaSamplerContext, Map, String)  */
	public static final String ON_EXCEPTION_WRITE_PERF_LOG		 		= "On_Exception_Write_Perf_Log";
	/**	@see UiAbstractJavaSamplerClient#UiScriptExecutionAndExceptionsHandling(JavaSamplerContext, Map, String)  */
	public static final String ON_EXCEPTION_WRITE_STACK_TRACE	 		= "On_Exception_Write_Stack_Trace";
	
	/**  generic meterFunctions (can be overridden by extending classes) */		
	protected JmeterFunctionsUi jm;

	/**
	 *  Generic arguments (expected to be overridden on implementing classes)
	 */
	protected static final Map<String,String> defaultArgumentsMap; 	
	static {
		Map<String,String> staticMap = new LinkedHashMap<>();

		staticMap.put(ON_EXCEPTION_WRITE_BUFFERED_LOGS, String.valueOf(true));
		staticMap.put(ON_EXCEPTION_WRITE_STACK_TRACE, 	String.valueOf(true));
		
		staticMap.put(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(false));	   
		staticMap.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(false));	   
		
		defaultArgumentsMap = Collections.unmodifiableMap(staticMap);
	}

	
	/** indicates to always close a browser on script completion */
	protected KeepBrowserOpen keepBrowserOpen = KeepBrowserOpen.NEVER;
	
	
	/**  used to output results table when running from a script Main() */
	protected static Map<String, List<Long>> resultsSummaryTable = new TreeMap<>();
	private static final int POS_0_NUM_SAMPLES  		= 0;	
	private static final int POS_1_NUM_FAIL  			= 1;	
	private static final int POS_2_SUM_RESPONSE_TIME 	= 2;	
	private static final int POS_3_RESPONSE_TIME_MIN 	= 3;	
	private static final int POS_4_RESPONSE_TIME_MAX	= 4;	
	
	
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
	 * <p>Internally the values are used to build a Map of parameters that will be available throughout
	 * 'Mark59' for whatever customization is required for your test (eg, for Selenium or Playwright implementation.</p>
	 * <p>Please see link(s) below for more detail.  
	 * 
	 * @see IpUtilities#localIPisNotOnListOfIPaddresses(String)
	 * @see JmeterFunctionsUi
	 * @see #UiScriptExecutionAndExceptionsHandling(JavaSamplerContext, Map, String)
	 * 
	 * @return the updated map of JMeter arguments with any required changes
	 */
	protected abstract Map<String, String> additionalTestParameters();
		

	/**
	 *  Setup for the execution of a Mark59 Ui test. 
	 */
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		if (LOG.isDebugEnabled()) LOG.debug(this.getClass().getName() +  " : executing runTest" );
		
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
		
		jm = UiScriptExecutionAndExceptionsHandling(context, jmeterRuntimeArgumentsMap, tgName);

		return jm.getMainResult();
	}


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
	 * Actions (closing browsers, logging, etc) to be taken when a script throws an exception
	 * @param context JMeter context
	 * @param jmeterRuntimeArgumentsMap JMeter arguments
	 * @param tgName current thread group name
	 * @return  a JmeterFunctionsUi implementation (used to populate script transaction results)
	 */
	protected abstract JmeterFunctionsUi UiScriptExecutionAndExceptionsHandling(JavaSamplerContext context,
			Map<String, String> jmeterRuntimeArgumentsMap, String tgName);
	

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
	 * Convenience method to a single-threaded script execution (ie from the IDE, rather than needing to use JMeter).
	 * Browser will stay open only on script failure.
	 * 
	 * <p>These RunUI.. methods can also be used in a JRS223 sampler in order to execute a script written directly in JMeter. 
	 * See the DataHunterLifecyclePvtScriptAsSingleJSR223 thread group in the the DataHunterSeleniumTestPlan.jmx test plan and
	 * com.mark59.datahunter.samples.scripts.jsr223format package in the mark59-datahunter-samples project for Selenium 
	 * examples.  
	 *  
	 * @see #runUiTest(KeepBrowserOpen)
	 * @see #runUiTest(KeepBrowserOpen, boolean)
	 * 
	 * @see #runMultiThreadedUiTest(int, int)
	 * @see #runMultiThreadedUiTest(int, int, Map) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @return {@link SampleResult}
	 */
	public SampleResult runUiTest() {
		return runUiTest(KeepBrowserOpen.ONFAILURE, true);
	}
	
	
	/**
	 * Convenience method to a single-threaded script execution (ie from the IDE, rather than needing to use JMeter).  
	 * <p>You can control if the browser closes at the end of the test. 
	 * <p>The results summary is always printed. 
	 * EG: <b>KeepBrowserOpen.ONFAILURE</b> will keep the browser open at test end if the test fails (unless running in headless mode). 
	 * 
	 * @see #runUiTest()
	 * @see #runUiTest(KeepBrowserOpen, boolean)
	 * 
	 * @see #runMultiThreadedUiTest(int, int)
	 * @see #runMultiThreadedUiTest(int, int, Map) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @see com.mark59.scripting.KeepBrowserOpen
	 * @see Log4jConfigurationHelper
	 * 
	 * @param keepBrowserOpen  KeepBrowserOpen (NEVER, ONFAILURE, ALWAYS) 
	 * @return SampleResult  - runSeleniumTest with LogResultsSummary set as true
	 */
	public SampleResult runUiTest(KeepBrowserOpen keepBrowserOpen ) {
		return runUiTest(keepBrowserOpen, true);
	}


	/**
	 * As per {@link #runUiTest(KeepBrowserOpen)}, but allowing an option for the results summary not to be printed 	 
	 *  
	 * @see #runUiTest()
	 * @see #runUiTest(KeepBrowserOpen)
	 * 
	 * @see #runMultiThreadedUiTest(int, int)
	 * @see #runMultiThreadedUiTest(int, int, Map) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @param keepBrowserOpen see {@link KeepBrowserOpen}
	 * @param isLogResultsSummary see {@link JmeterFunctionsImpl#LOG_RESULTS_SUMMARY}
	 * @return {@link SampleResult}
	 */
	public SampleResult runUiTest(KeepBrowserOpen keepBrowserOpen, boolean isLogResultsSummary) {
		mockJmeterProperties();
		Arguments jmeterParameters =  getDefaultParameters();
		JavaSamplerContext context = new JavaSamplerContext(jmeterParameters);
		
		this.keepBrowserOpen = keepBrowserOpen;
		if (String.valueOf(true).equalsIgnoreCase(context.getParameter(ScriptingConstants.HEADLESS_MODE))) {
			this.keepBrowserOpen = KeepBrowserOpen.NEVER;
		}
		LOG.debug("keepBrowserOpen is set to "+ this.keepBrowserOpen);
		
		setupTest(context);
		return runTest(context);
	}


	/**
	 * Convenience method to directly execute multiple script threads (ie from the IDE, rather than needing to use JMeter).  
	 * For example: <br><br>
	 * <code>thisTest.runMultiThreadedUiTest(2, 2000);</code>
	 * 
	 * @see #runMultiThreadedUiTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs time between start of each thread in milliseconds
	 */
	public void runMultiThreadedUiTest(int numberOfThreads, int threadStartGapMs) {
		runMultiThreadedUiTest(numberOfThreads, threadStartGapMs, new HashMap<>(), KeepBrowserOpen.NEVER, 1, 0, false, null);
	}

	
	/**
	 * Convenience method to directly execute multiple script threads (ie from the IDE, rather than needing to use JMeter).  
	 * For example: <br><br>
	 * <code>thisTest.runMultiThreadedUiTest(2, 2000, KeepBrowserOpen.ONFAILURE);</code>
	 * 
	 * @see #runMultiThreadedUiTest(int, int)
	 * @see #runMultiThreadedUiTest(int, int, Map) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs time between start of each thread in milliseconds
	 * @param keepBrowserOpen  see KeepBrowserOpen
	 */
	public void runMultiThreadedUiTest(int numberOfThreads, int threadStartGapMs, KeepBrowserOpen keepBrowserOpen) {
		runMultiThreadedUiTest(numberOfThreads, threadStartGapMs, new HashMap<>(), keepBrowserOpen, 1, 0, false, null);
	}
	
	
	/**
	 * Convenience method to directly execute multiple script threads (from the IDE rather than needing to use JMeter).  For example,
	 * if you want to user a user-defined parameter called "<code>USER</code>", and switch off headless mode for one of four threads running:  
	 * <br><br><code>
	 *  Map&lt;String, java.util.List&lt;String&gt;&gt;threadParameters = new java.util.LinkedHashMap&lt;String,java.util.List&lt;String&gt;&gt;();<br>
	 *	threadParameters.put("USER",                           java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));<br>
	 *	threadParameters.put(ScriptingConstants.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "true"));<br>		
	 *	thisTest.runMultiThreadedUiTest(4, 2000, threadParameters);
	 * </code>  
	 *  
	 * @see #runMultiThreadedUiTest(int, int)
	 * @see #runMultiThreadedUiTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs  time between start of each thread in milliseconds
	 * @param threadParameters  parameter key and list of values to be passed to each thread (needs to be at least as many entries as number of threads) 
	 */
	public void runMultiThreadedUiTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters) {
		runMultiThreadedUiTest(numberOfThreads, threadStartGapMs, threadParameters, KeepBrowserOpen.NEVER, 1, 0, false, null );
	}	
	
	
	/**
	 * Convenience method to directly execute multiple script threads (from the IDE rather than needing to use JMeter).  For example,
	 * if you want to user a user-defined parameter called "<code>USER</code>", and switch off headless mode for one of four threads running:  
	 * <br><br><code>
	 *  Map&lt;String, java.util.List&lt;String&gt;&gt;threadParameters = new java.util.LinkedHashMap&lt;String,java.util.List&lt;String&gt;&gt;();<br>
	 *	threadParameters.put("USER",                           java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));<br>
	 *	threadParameters.put(ScriptingConstants.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "true"));<br>		
	 *	thisTest.runMultiThreadedUiTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE);
	 * </code>  
	 *  
	 * @see #runMultiThreadedUiTest(int, int)
	 * @see #runMultiThreadedUiTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen, int, int, boolean, File) 
	 * 
	 * @param numberOfThreads number Of Java Threads
	 * @param threadStartGapMs  time between start of each thread in milliseconds
	 * @param threadParameters  parameter key and list of values to be passed to each thread (needs to be at least as many entries as number of threads) 
	 * @param keepBrowserOpen  see KeepBrowserOpen	 
	 */
	public void runMultiThreadedUiTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters, KeepBrowserOpen keepBrowserOpen) {
		runMultiThreadedUiTest(numberOfThreads, threadStartGapMs, threadParameters, keepBrowserOpen, 1, 0, false, null);
	}

	
	/**
	 * 'Full Monty' convenience method to directly execute multiple script threads (from the IDE). The threads can be set to run a given number of iterations, 
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
	 *	threadParameters.put("USER",                           java.util.Arrays.asList( "USER-MATTHEW", "USER-MARK", "USER-LUKE", "USER-JOHN"));<br>
	 *	threadParameters.put(ScriptingConstants.HEADLESS_MODE, java.util.Arrays.asList( "true"        , "false"    , "true"     , "true"));<br>
	 * </code>
	 * <p>Then, to run the 4 threads starting 2000ms apart, with each thread iterating the script 3 times having a 1500ms gap between each iteration, 
	 * and printing out a summary report and the CSV file to 'C:/Mark59_Runs/csvSample.csv' (Win machine) at the end : <br><br>
	 * <b><code>  		
	 * thisTest.runMultiThreadedUiTest(4, 2000, threadParameters, KeepBrowserOpen.ONFAILURE, 3, 1500, true, new File("C:/Mark59_Runs/csvSample.csv"));
	 * </code></b>
	 *  
	 * @see #runMultiThreadedUiTest(int, int)
	 * @see #runMultiThreadedUiTest(int, int, KeepBrowserOpen) 
	 * @see #runMultiThreadedUiTest(int, int, Map) 
	 * @see #runMultiThreadedUiTest(int, int, Map, KeepBrowserOpen) 
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
	public void runMultiThreadedUiTest(int numberOfThreads, int threadStartGapMs, Map<String, List<String>>threadParameters, KeepBrowserOpen keepBrowserOpen,
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

			Thread thread = new Thread(new UiTestThread(this.getClass(), thisThreadParameters, keepBrowserOpen, iterateEachThreadCount, iteratePacingGapMs,
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
	public void mockJmeterProperties() { 
		File f = new File("./jmeter.properties");
		if(f.exists() && !f.isDirectory()) { 
			LOG.debug("loading supplied jmeter.properties file");
			JMeterUtils.loadJMeterProperties("./jmeter.properties");   	
		}
	}
	
	
	/**
	 * Convenience inner class to enable multi-thread testing outside of JMeter
	 */
	public class UiTestThread implements Runnable {

		private final Class<? extends UiAbstractJavaSamplerClient> testClass;
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
		public UiTestThread(Class<? extends UiAbstractJavaSamplerClient> testClass,	Map<String, String> thisThreadParametersOverride, 
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
		 *  run a Ui Test Thread
		 */
		@Override
		public void run() {

			UiAbstractJavaSamplerClient testInstance = null;
			try {
				testInstance = testClass.getDeclaredConstructor().newInstance();
			} catch (Exception e) {	e.printStackTrace(); System.out.println(" Error " + e.getMessage()); } 
			
			Arguments thisThreadParameterAuguments = Mark59Utils.mergeMapWithAnOverrideMap(getDefaultParameters().getArgumentsAsMap(), thisThreadParametersOverride);
//			thisThreadParameterAuguments.removeArgument(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY);
//			thisThreadParameterAuguments.addArgument(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(true));
			JavaSamplerContext context = new JavaSamplerContext( thisThreadParameterAuguments  );
			
			if (String.valueOf(true).equalsIgnoreCase(context.getParameter(ScriptingConstants.HEADLESS_MODE))) {
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
