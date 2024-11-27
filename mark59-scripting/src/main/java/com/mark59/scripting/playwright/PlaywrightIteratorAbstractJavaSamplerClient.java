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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.scripting.KeepBrowserOpen;
import com.mark59.scripting.UiAbstractJavaSamplerClient;
import com.mark59.scripting.interfaces.UIiterator;
import com.microsoft.playwright.Page;


/**
 * Playwright flavoured extension of the JMeter Java Sampler AbstractJavaSamplerClient.  This class extends Mark59 class PlaywrightAbstractJavaSamplerClient, which itself extends the base 
 * implementation of the JMeter Java Request class AbstractJavaSamplerClient. 
 * 
 * <p>The extra functionality provided over PlaywrightAbstractJavaSamplerClient allows for an 'initial' action (eg, a logon), iterate (eg, repeatedly perform an 
 * application workflow of some kind), and at the end - which can be determined by a count of iterations and/or a time settings - perform a 'finalize' action (eg logoff).
 * 
 * The parameters from Mark59 class PlaywrightAbstractJavaSamplerClient are available, plus these additions to control pacing and flow:
 * <ul> 
 * <li> <b>ITERATE_FOR_PERIOD_IN_SECS.</b> at the end of each iteration, a check is made to see if the time in seconds the script has been iterating has reached this.
 *   If so, the finalize is executed and the script completed.  Must be a non-zero numeric to be active.
 * <li> <b>ITERATE_FOR_NUMBER_OF_TIMES.</b> at the end of each iteration, a check is made to see if the number of iterations the script has performed has reached this value.
 *  If so, the finalize is executed and the script completed.  Must be a non-zero numeric to be active. The default value is 1, so it needs to be over-ridden with 0
 *   (or a non-numeric like a space) to deactivate.  
 * <li> <b>STOP_THREAD_AFTER_TEST_START_IN_SECS.</b> at the start of each iteration, a check is made to see if the time in seconds since the JMeter test started exceeds this value.
 *  If so, the finalize is executed and the and the script completed. The check is also made at script start-up, so when the script (re)starts and this condition has been met the thread will
 *  be stopped immediately.  Must be a non-zero numeric to be active.   
 * <li> <b>ITERATION_PACING_IN_SECS.</b> The target length of time of each iteration. A thread delay calculated at the end of the iteration forces the iteration to the iteration pacing time.  
 *   Must be a non-zero numeric to be active.  
 * <li> <b>STOP_THREAD_ON_FAILURE.</b> by default the script thread will re-start on failure (timers permitting).  This flag can be set to <b>true</b> to force the thread to stop for the rest of the test.  
 * </ul>
 *     
 * <p>Note that if none of the conditions <b>ITERATE_FOR_PERIOD_IN_SECS</b> or <b>ITERATE_FOR_NUMBER_OF_TIMES</b> or <b>STOP_THREAD_AFTER_TEST_START_IN_SECS</b> are set, the thread will be stopped 
 * (a message giving the reason is logged). If multiple conditions are set, the iteration looping ends when the first condition is met. 
 *
 * <p>
 * <b>A simple example:</b><br>  
 *---------------------<br>
 *Say the settings are:<br>
 * ITERATE_FOR_PERIOD_IN_SECS =	25<br>
 * ITERATE_FOR_NUMBER_OF_TIMES = blank<br>
 * ITERATION_PACING_IN_SECS = 10<br> 
 * STOP_THREAD_AFTER_TEST_START_IN_SECS = 0<br>
 * STOP_THREAD_ON_FAILURE = false<br>
 * <p>
 *Then each iteration will take 10 seconds (unless the script execution goes over 10 secs - in which case the next iteration will start immediately provided iterations started no more than 25 secs ago).
 *After the 3rd iteration, the script would of been iterating for 30 seconds.  The total period of iteration is 25 secs, so the finalize will be performed and the script completes.
 *Assuming no other timers in the thread group, the script will then finish executing if the Thread Group Count or Scheduler conditions have been met, or otherwise restart again, even if a 
 *failure occurred during the script execution.    
 * <p>
 * <b>Another example:</b><br>  
 *---------------------<br>
 * ITERATE_FOR_PERIOD_IN_SECS =	25<br>
 * ITERATE_FOR_NUMBER_OF_TIMES = blank<br>
 * ITERATION_PACING_IN_SECS = 10<br> 
 * STOP_THREAD_AFTER_TEST_START_IN_SECS = <b>600</b><br>
 * STOP_THREAD_ON_FAILURE = false<br>
 * <p>
 *The same as the first example but the value STOP_THREAD_AFTER_TEST_START_IN_SECS will <b>additionally</b> be tested at the start of each iteration.  So, assuming the test is still going
 *at 600 seconds (eg, not already stopped by a shorter Scheduler Duration condition in the Thread Group setup), then when an iteration is due to start more than 600 seconds since the test
 *started (JMeter variable TESTSTART.MS) the script finalizes. The thread then stops if/when the script tries to restart.  Generally, the STOP_THREAD_AFTER_TEST_START_IN_SECS 
 *condition may be useful when you are using a thread that is iterating many times over a long period.  Then, if a thread failure occurs near the end of the test, the entire test is not 
 *unduly lengthened by the thread re-start.  
 *
 *<p>Note that keeping a single browser / thread open and iterating for an entire test means no intermediate results can be reported (all the data needs to be held in memory), 
 *so it is suggested this class should only be used when necessary.    
 *
 * @see UIiterator
 * @see PlaywrightAbstractJavaSamplerClient
 * @see UiAbstractJavaSamplerClient
 * 
 * @author Philip Webb
 * Written: Australian Summer 2023/24  
 */
public abstract class PlaywrightIteratorAbstractJavaSamplerClient extends PlaywrightAbstractJavaSamplerClient implements UIiterator {

	/** log4J class logger */
	public static final Logger LOG = LogManager.getLogger(PlaywrightIteratorAbstractJavaSamplerClient.class);

	
	/** Hold default arguments for implementations of this class */
	private static final Map<String, String> playwrightDefaultIteratorArgumentsMap; 
	static {
		Map<String,String> staticMap = new LinkedHashMap<>();

		staticMap.put("______________________ interation settings: _____________________", "" );
		staticMap.put(ITERATE_FOR_PERIOD_IN_SECS, 						"0");
		staticMap.put(ITERATE_FOR_NUMBER_OF_TIMES,  					"1");
		staticMap.put(ITERATION_PACING_IN_SECS,  						"0");
		staticMap.put(STOP_THREAD_AFTER_TEST_START_IN_SECS,				"0");
		staticMap.put(STOP_THREAD_ON_FAILURE,		  String.valueOf(false));		
		
		staticMap.putAll(buildBasePlaywrightStaticArgsMap());
		
		playwrightDefaultIteratorArgumentsMap = Collections.unmodifiableMap(staticMap);
	}

	
	/** 
	 * Creates the list of parameters with default values, as they would appear on the JMeter GUI for the JavaSampler being implemented.
	 * <p>A standard set of parameters are defined (defaultArgumentsMap and defaultIterArgumentsMap). Additionally,an implementing class 
	 * (the script extending this class) can add additional parameters (or override the standard defaults) via the additionalTestParameters() method.    
	 * 
	 * @see #additionalTestParameters()
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient
	 */
	@Override
	public Arguments getDefaultParameters() {
		return Mark59Utils.mergeMapWithAnOverrideMap(playwrightDefaultIteratorArgumentsMap, additionalTestParameters());
	}
		

	/**
	 * {@inheritDoc}
	 * 
	 *  Note the use of the catch on  AssertionError - as this is NOT an Exception but an Error, and therefore need to be explicitly caught. 
	 */
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		if (LOG.isDebugEnabled()) LOG.debug(this.getClass().getName() +  " : exectuing runTest (iterator)" );
		
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
		
		// JMeter 5.5 and 5.6 compatibility: JavaSamplerContext.getJMeterVariables() was changed from 'final' to 'static'
		// copying their common code to prevent 'static accessed' warning: "return JMeterContextService.getContext().getVariables();"
		
		Long jMeterTestStartMs = 0L;
		if (JMeterContextService.getContext().getVariables() != null) {
			jMeterTestStartMs = convertToLong("TESTSTART.MS", JMeterContextService.getContext().getVariables().get("TESTSTART.MS"), LOG);
		} else {
			LOG.debug("JMeterVariables do not exist (probably executing outside JMeter - so any STOP_THREAD_AFTER_TEST_START_IN_SECS condition is not checked");
		}
		Long stopThreadAfterTestStartMs = convertToLong(STOP_THREAD_AFTER_TEST_START_IN_SECS, context.getParameter(STOP_THREAD_AFTER_TEST_START_IN_SECS), LOG) * 1000;
		
		if (isStopThreadAfterTestStartMsConditionMet(jMeterTestStartMs, stopThreadAfterTestStartMs)){
			LOG.info("Thread Group " + tgName + " is stopping ('STOP_THREAD_AFTER_TEST_START_IN_SECS' has been reached)" );
			if (tg!=null) tg.stop();
			return null;	
		}		
		
		Map<String,String> jmeterRuntimeArgumentsMap = convertJmeterArgumentsToMap(context);

		jm = new JmeterFunctionsForPlaywrightScripts(context, jmeterRuntimeArgumentsMap);
		
		try {
			playwrightPage = makePlaywrightPage(jmeterRuntimeArgumentsMap);   
		} catch (Exception e) {
			LOG.error("ERROR : " + this.getClass() + ". Fatal error has occurred for Thread Group " + tgName
					+ " while attempting to initiate the selenium Driver. The Thread is stopping !" );
			LOG.error(e.getMessage());
			e.printStackTrace();			
			if (tg!=null) tg.stop();
			return null;
		}
		jm.setPage(playwrightPage);
		
		boolean forceStop = false;
 				
		try {
			LOG.debug(">> initiatePlaywrightTest");			
			initiatePlaywrightTest(context, jm, playwrightPage);
			LOG.debug("<< finished initiatePlaywrightTest" );

			Long scriptStartTimeMs 		 	= System.currentTimeMillis(); 			
			Long iterateForPeriodMs 	 	= convertToLong(ITERATE_FOR_PERIOD_IN_SECS, context.getParameter(ITERATE_FOR_PERIOD_IN_SECS), LOG) * 1000;
			Integer iterateNumberOfTimes 	= convertToInteger(context.getParameter(ITERATE_FOR_NUMBER_OF_TIMES));
			long iterationPacingMs       	= convertToLong(ITERATION_PACING_IN_SECS, context.getParameter(ITERATION_PACING_IN_SECS), LOG) * 1000;
			long scriptIterationStartTimeMs;
			long delay = 0;
			
			if (LOG.isDebugEnabled()) LOG.debug(Thread.currentThread().getName() + ": tgName = " + tgName + " scriptStartTimeMs = " + scriptStartTimeMs	
					+ "\n | iterPeriodMs = " + iterateForPeriodMs + "\n | iterNumberOfTimes = " + iterateNumberOfTimes 
					+ "\n | iterPacingMs = " + iterationPacingMs  + "\n | stopThreadAfterTestStartMs = " + stopThreadAfterTestStartMs );
		
			if (iterateForPeriodMs==0 && iterateNumberOfTimes==0 && stopThreadAfterTestStartMs==0 ) {
				LOG.info("Thread Group " + tgName + " is stopping (none of ITERATE_FOR_PERIOD_IN_SECS or ITERATE_FOR_NUMBER_OF_TIMES or "
						+ "STOP_THREAD_AFTER_TEST_START_IN_SECS have been set to a valid non-zero value)" );
				if (tg!=null) tg.stop();
				return null;
			}
			int i=0;
			
			while (!isAnyIterateEndConditionMet(tgName, scriptStartTimeMs, iterateForPeriodMs, iterateNumberOfTimes, i, jMeterTestStartMs,  stopThreadAfterTestStartMs, forceStop, LOG)){
				i++;
				LOG.debug(">> iteratePlaywrightTest (#" + i + ")");
				scriptIterationStartTimeMs =  System.currentTimeMillis();

				try {
					iteratePlaywrightTest(context, jm, playwrightPage);
					
					if (iterationPacingMs > 0) {
						delay =	iterationPacingMs + scriptIterationStartTimeMs - System.currentTimeMillis();
						if (delay < 0){
							LOG.info("  script execution time exceeded pacing by  : " + (-delay) + " ms."  );
							delay = 0;
						}
					}
					LOG.debug("<<  iteratePlaywrightTest - script execution sleeping for : " + delay + " ms."  );
					Thread.sleep(delay);

				} catch (Exception | AssertionError e) {

					scriptExceptionHandling(context, jmeterRuntimeArgumentsMap, e);	
					
					if ("true".equalsIgnoreCase(context.getParameter(STOP_THREAD_ON_FAILURE))){
						LOG.info("Thread Group " + tgName + " is stopping (script failure, and STOP_THREAD_ON_FAILURE is set to true)" );
						forceStop = true;
					}
				}
				
			} // end iteration loop
			
			LOG.debug(">> running finalizePlaywrightTest");			
			finalizePlaywrightTest(context, jm, playwrightPage);
			LOG.debug("<< finished finalizePlaywrightTest" );			

		} catch (Exception | AssertionError e) {

			scriptExceptionHandling(context, jmeterRuntimeArgumentsMap, e);	

			if ("true".equalsIgnoreCase(context.getParameter(STOP_THREAD_ON_FAILURE))){
				LOG.info("Thread Group " + tgName + " is stopping (script failure, and STOP_THREAD_ON_FAILURE is set to true)" );
				forceStop = true;
			}
			
		} finally {
			
			jm.tearDown();
			if (! this.getKeepBrowserOpen().equals(KeepBrowserOpen.ALWAYS)){ 
				driverDispose();
			}
			if (forceStop) {
				jm.stopThreadGroup(context);
			}
		}
		return jm.getMainResult();
	}


	/**
	 * no implementation for a PlaywrightIteratorAbstractJavaSamplerClient test
	 */
	@Override
	protected void runPlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page playwrightPage) {}

	/**
	 * @param context JavaSamplerContext
	 * @param jm JmeterFunctionsForPlaywrightScripts
	 * @param playwrightPage Playwright Page
	 */
	protected abstract void initiatePlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page playwrightPage);
	
	/**
	 * @param context JavaSamplerContext
	 * @param jm JmeterFunctionsForPlaywrightScripts
	 * @param playwrightPage Playwright Page
	 */
	protected abstract void iteratePlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page playwrightPage);
	
	/**
	 * @param context JavaSamplerContext
	 * @param jm JmeterFunctionsForPlaywrightScripts
	 * @param playwrightPage Playwright Page
	 */
	protected abstract void finalizePlaywrightTest(JavaSamplerContext context, JmeterFunctionsForPlaywrightScripts jm, Page playwrightPage);	

}
