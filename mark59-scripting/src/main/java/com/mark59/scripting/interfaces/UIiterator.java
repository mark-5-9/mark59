package com.mark59.scripting.interfaces;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.Logger;

import com.mark59.scripting.playwright.PlaywrightIteratorAbstractJavaSamplerClient;
import com.mark59.scripting.selenium.SeleniumIteratorAbstractJavaSamplerClient;

/**
 * Capture common elements for UI iterator implementations.  
 *  
 * @see #ITERATE_FOR_PERIOD_IN_SECS
 * @see #ITERATE_FOR_NUMBER_OF_TIMES
 * @see #ITERATION_PACING_IN_SECS
 * @see #STOP_THREAD_AFTER_TEST_START_IN_SECS  
 * @see #STOP_THREAD_ON_FAILURE  
 * @see SeleniumIteratorAbstractJavaSamplerClient
 * @see PlaywrightIteratorAbstractJavaSamplerClient
 * 
 * @author Philip Webb    
 * Written: Australian Summer 2023/24  
 */
public interface UIiterator  {

	/**
	 * At the end of each iteration, a check is made to see if the time in seconds the script has been iterating has reached this.
	 * If so, the finalize is executed and the script completed. Must be a non-zero numeric to be active.
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 * @see PlaywrightIteratorAbstractJavaSamplerClient
	 */
	public static final String ITERATE_FOR_PERIOD_IN_SECS 			= "ITERATE_FOR_PERIOD_IN_SECS";
	
	/**
	 * At the end of each iteration, a check is made to see if the number of iterations the script has performed has reached this value.
	 * If so, the finalize is executed and the script completed. Must be a non-zero numeric to be active. Current implementations set
	 * a default value of 1, so it needs to be over-ridden with 0 (or a non-numeric like a space) to deactivate.  
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 * @see PlaywrightIteratorAbstractJavaSamplerClient
	 */
	public static final String ITERATE_FOR_NUMBER_OF_TIMES			= "ITERATE_FOR_NUMBER_OF_TIMES";

	/**
	 * The target length of time of each iteration. A thread delay calculated at the end of the iteration forces the iteration to the iteration pacing time.  
	 * Must be a non-zero numeric to be active.
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 * @see PlaywrightIteratorAbstractJavaSamplerClient 
	 */	
	public static final String ITERATION_PACING_IN_SECS 			= "ITERATION_PACING_IN_SECS";	
	
	/**
	 * At the start of each iteration, a check is made to see if the time in seconds since the JMeter test started exceeds this value.
	 * If so, the finalize is executed and the and the script completed. The check is also made at script start-up, so when the script (re)starts and this 
	 * condition has been met the thread will be stopped immediately.  Must be a non-zero numeric to be active.
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 * @see PlaywrightIteratorAbstractJavaSamplerClient 
	 */	
	public static final String STOP_THREAD_AFTER_TEST_START_IN_SECS	= "STOP_THREAD_AFTER_TEST_START_IN_SECS";	
	
	/**
	 * By default the script thread will re-start on failure (timers permitting).  This flag can be set to <b>true</b> to force the thread to stop
	 * for the rest of the test.
	 * <p>This is quite an extreme action - perhaps you could consider the making use  
	 * @see SeleniumIteratorAbstractJavaSamplerClient
	 * @see PlaywrightIteratorAbstractJavaSamplerClient 
	 */	
	public static final String STOP_THREAD_ON_FAILURE 				= "STOP_THREAD_ON_FAILURE";


	/**
	 * Convert parameter to a Long, defaulting to 0L (zero Long)
	 * @param parameterName passed for debug purposes only
	 * @param parameter the string to be converted
	 * @param LOG Logger
	 * @return Long set to 0L if a null or non-number passed   
	 */
	public default Long convertToLong(String parameterName, String parameter, Logger LOG ) {
		long convertedLong = 0L;
		if (parameter!= null) parameter = parameter.trim();
		if (NumberUtils.isCreatable(parameter)){
			convertedLong = Long.parseLong(parameter.trim());
		} else {
			LOG.debug("0L is being assumed for the parameter '" + parameterName + "'" );
		}
		return convertedLong;
	}

	
	/**
	 * Convert a String to Integer, with a default of 0
	 * @param parameter String to convert into Integer 
	 * @return integer or 0 if null or not numeric 
	 */
	public default Integer convertToInteger(String parameter) {
		int convertedInt = 0;
		if (parameter!= null){
			if (StringUtils.isNumeric(parameter.trim())){
				convertedInt = Integer.parseInt(parameter.trim());
			}
		}
		return convertedInt;
	}
	

	/**
	 * @param tgName  thread group
	 * @param scriptStartTimeMs  start time (derived from current time at script start)
	 * @param iterateForPeriodMs iteration period
	 * @param iterateNumberOfTimes target iteration count
	 * @param alreadyIterated current iteration count
	 * @param jMeterTestStartMs JMeter test start time (refer JMeter variable TESTSTART.MS)
	 * @param stopThreadAfterTestStartMs flag for total test time reached 
	 * @param forceStop if this boolean set to true, return true (end condition met) 
	 * @param LOG Logger
	 * @return flag if conditions to stop iterations met
	 */
	public default boolean isAnyIterateEndConditionMet(String tgName, Long scriptStartTimeMs, Long iterateForPeriodMs, Integer iterateNumberOfTimes, 
			Integer alreadyIterated, Long jMeterTestStartMs, Long stopThreadAfterTestStartMs, boolean forceStop, Logger LOG ) {
		
		if (forceStop){
			if (LOG.isDebugEnabled()) LOG.debug(Thread.currentThread().getName() + ": tgName = " + tgName + " inter End Cond : \n | force stop set");
			return true;
		}		
		if ( iterateNumberOfTimes > 0 && alreadyIterated >= iterateNumberOfTimes ){
			if (LOG.isDebugEnabled()) LOG.debug(Thread.currentThread().getName() + ": tgName = " + tgName + " inter End Cond : " 
					+ "\n | alreadyIterated = " + alreadyIterated + " >=  iterNumberOfTimes = " + iterateNumberOfTimes );
			return true;
		}
		if ( iterateForPeriodMs > 0 &&  System.currentTimeMillis() > (scriptStartTimeMs+iterateForPeriodMs) ){
			if (LOG.isDebugEnabled()) LOG.debug(Thread.currentThread().getName() + ": tgName = " + tgName + " inter End Cond -" 
					+ "\n | System.currentTimeMillis() = " + System.currentTimeMillis() 
					+ " > scriptStartTimeMs+iterPeriodMs = "+scriptStartTimeMs+"+"+iterateForPeriodMs+"="+(scriptStartTimeMs+iterateForPeriodMs));
			return true;
		}
		if (isStopThreadAfterTestStartMsConditionMet(jMeterTestStartMs, stopThreadAfterTestStartMs)) {
			LOG.info("Thread Group " + tgName + " iterations stop ('STOP_THREAD_AFTER_TEST_START_IN_SECS' has been reached)" );
			return true;
		}
		return false;
	}


	/**
	 * flag if conditions to stop iterations met
	 * 
	 * @param jMeterTestStartMs  obtained from JMeter variable "TESTSTART.MS"
	 * @param stopThreadAfterTestStartMs how long to run the thread 
	 * @return a boolean (is the condition met?)
	 */
	public default boolean isStopThreadAfterTestStartMsConditionMet(Long jMeterTestStartMs, Long stopThreadAfterTestStartMs) {
		return jMeterTestStartMs > 0 && stopThreadAfterTestStartMs > 0 && System.currentTimeMillis() > jMeterTestStartMs + stopThreadAfterTestStartMs;
	}

}
