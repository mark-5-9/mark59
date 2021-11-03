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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.WebDriver;

import com.mark59.core.utils.IpUtilities;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.selenium.drivers.SeleniumDriverFactory;


/**
 * Selenium flavoured extension of the JMeter Java Sampler AbstractJavaSamplerClient.  This class extends Mark59 class SeleniumAbstractJavaSamplerClient, which itself extends the base 
 * implementation of the JMeter Java Request class AbstractJavaSamplerClient. 
 * 
 * <p>The extra functionality provided over SeleniumAbstractJavaSamplerClient allows for an 'initial' action (eg, a logon), iterate (eg, repeatedly perform an 
 * application workflow of some kind), and at the end - which can be determined by a count of iterations and/or a time settings - perform a 'finalize' action (eg logoff).
 * 
 * The parameters from Mark59 class SeleniumAbstractJavaSamplerClient are available, plus these additions to control pacing and flow:
 * <ul> 
 * <li> <b>ITERATE_FOR_PERIOD_IN_SECS.</b> at the end of each iteration, a check is made to see if the time in seconds the script has been iterating has reached this.
 *   If so, the finalize is executed and the script completed.  Must be a non-zero numeric to be active.
 * <li> <b>ITERATE_FOR_NUMBER_OF_TIMES.</b> at the end of each iteration, a check is made to see if the number of iterations the script has performed has reached this value.
 *  If so, the finalize is executed and the script completed.  Must be a non-zero numeric to be active.
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
 * @see SeleniumAbstractJavaSamplerClient
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public abstract class SeleniumIteratorAbstractJavaSamplerClient  extends  SeleniumAbstractJavaSamplerClient {

	/** log4J class logger */
	public static final Logger LOG = LogManager.getLogger(SeleniumIteratorAbstractJavaSamplerClient.class);

	/**
	 * At the end of each iteration, a check is made to see if the time in seconds the script has been iterating has reached this.
	 * If so, the finalize is executed and the script completed. Must be a non-zero numeric to be active.
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 */
	public static final String ITERATE_FOR_PERIOD_IN_SECS 			= "ITERATE_FOR_PERIOD_IN_SECS";
	
	/**
	 * At the end of each iteration, a check is made to see if the number of iterations the script has performed has reached this value.
	 * If so, the finalize is executed and the script completed. Must be a non-zero numeric to be active.
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 */
	public static final String ITERATE_FOR_NUMBER_OF_TIMES			= "ITERATE_FOR_NUMBER_OF_TIMES";

	/**
	 * The target length of time of each iteration. A thread delay calculated at the end of the iteration forces the iteration to the iteration pacing time.  
	 * Must be a non-zero numeric to be active.
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 */	
	public static final String ITERATION_PACING_IN_SECS 			= "ITERATION_PACING_IN_SECS";	
	
	/**
	 * At the start of each iteration, a check is made to see if the time in seconds since the JMeter test started exceeds this value.
	 * If so, the finalize is executed and the and the script completed. The check is also made at script start-up, so when the script (re)starts and this 
	 * condition has been met the thread will be stopped immediately.  Must be a non-zero numeric to be active.
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 */	
	public static final String STOP_THREAD_AFTER_TEST_START_IN_SECS	= "STOP_THREAD_AFTER_TEST_START_IN_SECS";	
	
	/**
	 * By default the script thread will re-start on failure (timers permitting).  This flag can be set to <b>true</b> to force the thread to stop
	 * for the rest of the test.
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 */	
	public static final String STOP_THREAD_ON_FAILURE 				= "STOP_THREAD_ON_FAILURE";

	
	private static final Map<String,String> defaultIterArgumentsMap;	
	static {
		Map<String,String> staticIterMap = new LinkedHashMap<String,String>();

		staticIterMap.put("______________________ interation settings: _____________________", "" );		
		staticIterMap.put(ITERATE_FOR_PERIOD_IN_SECS, 						"0");
		staticIterMap.put(ITERATE_FOR_NUMBER_OF_TIMES,  					"1");
		staticIterMap.put(ITERATION_PACING_IN_SECS,  						"0");
		staticIterMap.put(STOP_THREAD_AFTER_TEST_START_IN_SECS,				"0");
		staticIterMap.put(STOP_THREAD_ON_FAILURE,		  String.valueOf(false));
		staticIterMap.putAll(defaultArgumentsMap);
		
		defaultIterArgumentsMap = Collections.unmodifiableMap(staticIterMap);		
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
		return Mark59Utils.mergeMapWithAnOverrideMap(defaultIterArgumentsMap, additionalTestParameters());
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
		
		Long jMeterTestStartMs = 0L;
		if (context.getJMeterVariables() != null) {
			jMeterTestStartMs = convertToLong("TESTSTART.MS", context.getJMeterVariables().get("TESTSTART.MS"),0L);
		} else {
			LOG.debug("JMeterVariables do not exist (probably executing outside JMeter - so any STOP_THREAD_AFTER_TEST_START_IN_SECS condition is not checked");
		}
		Long stopThreadAfterTestStartMs = convertToLong(STOP_THREAD_AFTER_TEST_START_IN_SECS, context.getParameter(STOP_THREAD_AFTER_TEST_START_IN_SECS),0L) * 1000;			
		
		if (isStopThreadAfterTestStartMsConditionMet(jMeterTestStartMs, stopThreadAfterTestStartMs)){
			LOG.info("Thread Group " + tgName + " is stopping ('STOP_THREAD_AFTER_TEST_START_IN_SECS' has been reached)" );
			if (tg!=null) tg.stop();
			return null;	
		}		
		
		Map<String,String> jmeterRuntimeArgumentsMap = convertJmeterArgumentsToMap(context);

		try {
			seleniumDriverWrapper = new SeleniumDriverFactory().makeDriverWrapper(jmeterRuntimeArgumentsMap) ;
		} catch (Exception e) {
			LOG.error("ERROR : " + this.getClass() + ". Fatal error has occured for Thread Group " + tgName
					+ " while attempting to initiate the selenium Driver. The Thread is stopping !" );
			LOG.error(e.getMessage());
			e.printStackTrace();			
			if (tg!=null) tg.stop();
			return null;
		}
		
		driver =  (WebDriver)seleniumDriverWrapper.getDriverPackage() ;
		jm = new JmeterFunctionsForSeleniumScripts(Thread.currentThread().getName(), seleniumDriverWrapper, jmeterRuntimeArgumentsMap);   	
				
		try {
			LOG.debug(">> initiateSeleniumTest");			
			initiateSeleniumTest(context, jm, driver);
			LOG.debug("<< finished initiateSeleniumTest" );

			Long scriptStartTimeMs 		 	= System.currentTimeMillis(); 			
			Long iterateForPeriodMs 	 	= convertToLong(ITERATE_FOR_PERIOD_IN_SECS, context.getParameter(ITERATE_FOR_PERIOD_IN_SECS),0L) * 1000;
			Integer iterateNumberOfTimes 	= convertToInteger(context.getParameter(ITERATE_FOR_NUMBER_OF_TIMES));
			Long iterationPacingMs       	= convertToLong(ITERATION_PACING_IN_SECS, context.getParameter(ITERATION_PACING_IN_SECS),0L) * 1000;
			long scriptIterationStartTimeMs;
			long delay = 0;
			
			if (LOG.isDebugEnabled()) LOG.debug(Thread.currentThread().getName() + ": tgName = " + tgName + ", scriptStartTimeMs = " + scriptStartTimeMs 
					+ ", iteratePeriodMs = " + iterateForPeriodMs + ", iterateNumberOfTimes = " + iterateNumberOfTimes );
		
			if (iterateForPeriodMs==0 && iterateNumberOfTimes==0 && stopThreadAfterTestStartMs==0 ) {
				LOG.info("Thread Group " + tgName + " is stopping (none of ITERATE_FOR_PERIOD_IN_SECS or ITERATE_FOR_NUMBER_OF_TIMES or "
						+ "STOP_THREAD_AFTER_TEST_START_IN_SECS have been set to a valid non-zero value)" );
				if (tg!=null) tg.stop();
				return null;
			}
			int i=0;
			
			while ( ! isAnyIterateEndConditionMet(tgName, scriptStartTimeMs, iterateForPeriodMs, iterateNumberOfTimes, i, jMeterTestStartMs,  stopThreadAfterTestStartMs)) {
				i++;
				LOG.debug(">> iterateSeleniumTest (" + i + ")");
				scriptIterationStartTimeMs =  System.currentTimeMillis();
				
				iterateSeleniumTest(context, jm, driver);
				
				if (iterationPacingMs > 0) {
					delay =	iterationPacingMs + scriptIterationStartTimeMs - System.currentTimeMillis();
				    if (delay < 0){
				         LOG.info("  script execution time exceeded pacing by  : " + (0-delay) + " ms."  );
				         delay = 0;
				    }
				}
		        LOG.debug("<<  iterateSeleniumTest - script execution sleeping for : " + delay + " ms."  );
			    Thread.sleep(delay);
			}
			
			LOG.debug(">> running finalizeSeleniumTest ");			
			finalizeSeleniumTest(context, jm, driver);
			LOG.debug("<< finished finalizeSeleniumTest" );			
						
			jm.tearDown();

		} catch (Exception | AssertionError e) {

			scriptExceptionHandling(context, e);	

			if ("true".equalsIgnoreCase(context.getParameter(STOP_THREAD_ON_FAILURE))){
				LOG.info("Thread Group " + tgName + " is stopping (script failure, and STOP_THREAD_ON_FAILURE is set to true)" );
				context.getJMeterContext().getThreadGroup().stop();
			}
			
		} finally {
			if (! this.getKeepBrowserOpen().equals(KeepBrowserOpen.ALWAYS )     ) { 
				seleniumDriverWrapper.driverDispose();
			}
		}
		return jm.getMainResult();
	}


	private Long convertToLong(String parameterName, String parameter, Long returnedValueForInvalidParameter ) {
		Long convertedLong = returnedValueForInvalidParameter;
		if (parameter!= null) parameter = parameter.trim();
		if (NumberUtils.isCreatable(parameter)){
			convertedLong = Long.valueOf(parameter.trim());
		} else {
			LOG.debug(returnedValueForInvalidParameter + " is being assumed for the parameter '" + parameterName + "'" );
		}
		return convertedLong;
	}

	private Integer convertToInteger(String parameter) {
		Integer convertedInt = 0;
		if (parameter!= null) parameter = parameter.trim();		
		if (StringUtils.isNumeric(parameter.trim())){
			convertedInt = Integer.valueOf(parameter);
		}
		return convertedInt;
	}

	private boolean isAnyIterateEndConditionMet(String tgName, Long scriptStartTimeMs, Long iterateForPeriodMs, 
			Integer iterateNumberOfTimes, Integer alreadyIterated, Long jMeterTestStartMs, Long stopThreadAfterTestStartMs) {
		
		if ( iterateNumberOfTimes > 0 && alreadyIterated >= iterateNumberOfTimes ){
			return true;
		}
		if ( iterateForPeriodMs > 0 &&  System.currentTimeMillis() > scriptStartTimeMs + iterateForPeriodMs ){
			return true;
		}
		if (isStopThreadAfterTestStartMsConditionMet(jMeterTestStartMs, stopThreadAfterTestStartMs)) {
			LOG.info("Thread Group " + tgName + " will be stopped on any further Thread Loops ('STOP_THREAD_AFTER_TEST_START_IN_SECS' has been reached)" );
			return true;
		};		
		return false;
	}


	/**
	 * @param jMeterTestStartMs  obtained from JMeter variable "TESTSTART.MS"
	 * @param stopThreadAfterTestStartMs how long to run the thread 
	 * @return a boolean (is the condition met?)
	 */
	private boolean isStopThreadAfterTestStartMsConditionMet(Long jMeterTestStartMs, Long stopThreadAfterTestStartMs) {
		if ( jMeterTestStartMs > 0 && stopThreadAfterTestStartMs > 0 &&  System.currentTimeMillis() > jMeterTestStartMs + stopThreadAfterTestStartMs ){
			return true;
		} else {
			return false;
		}
	}



	/**
	 * no implementation for a SeleniumIteratorAbstractJavaSamplerClient test
	 */
	@Override
	protected void runSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver) {}
	
	
	/**
	 * @param context JavaSamplerContext
	 * @param jm JmeterFunctionsForSeleniumScripts
	 * @param driver WebDriver
	 */
	protected abstract void initiateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver);
	
	/**
	 * @param context JavaSamplerContext
	 * @param jm JmeterFunctionsForSeleniumScripts
	 * @param driver WebDriver
	 */
	protected abstract void iterateSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver);
	
	/**
	 * @param context JavaSamplerContext
	 * @param jm JmeterFunctionsForSeleniumScripts
	 * @param driver WebDriver
	 */
	protected abstract void finalizeSeleniumTest(JavaSamplerContext context, JmeterFunctionsForSeleniumScripts jm, WebDriver driver);	

}
