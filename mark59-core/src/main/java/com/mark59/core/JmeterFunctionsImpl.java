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

package com.mark59.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;



/**
 * Implements the JmeterFunctions interface, with methods that can be called throughout the life cycle of the test in order to handle 
 * behavior around transaction recording and timing.
 * 
 * <p>Typical usage from a scripting perspective is to time a transaction.  For example:
 * <pre><code>
 * jm.startTransaction("DH_lifecycle_0100_deleteMultiplePolicies");		
 * deleteMultiplePoliciesPage.submit().submit();
 * jm.endTransaction("DH_lifecycle_0100_deleteMultiplePolicies")
 * </code></pre>
 * 
 * <p>where '<code>jm</code>' is this class, or an extension of this class (eg SeleniumAbstractJavaSamplerClient)
 * 
 * <p>The class works by creating JMeter 'sub-results', one per recored transaction, which are attached to a main SampleResult.  
 * At the end of the script the sub-results ({@link #tearDown()} are printed (at LOG info level).  
 * 
 * 
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019  
 */
public class JmeterFunctionsImpl implements JmeterFunctions {

	private static final Logger LOG = LogManager.getLogger(JmeterFunctionsImpl.class);
	
	/**
	 * the JMeter main sample result for the script
	 */
	protected SampleResult mainResult = new SampleResult();
	/**
	 * the executed and in-flight transactions   
	 */
	protected Map<String, SampleResult> transactionMap = new ConcurrentHashMap<>();
	/**
	 * holds most Recent Transaction Started  (used in the sample script by the DevTools DSL)
	 */
	protected String mostRecentTransactionStarted;
	/**
	 * holds thread Name
	 */
	protected String threadName;

	/**
	 * used to the the result of a test as a failure
	 */
	protected boolean isForcedFail;
	
	/**
	 * @param threadName  thread name (eg, obtained via<code>Thread.currentThread().getName()</code> )
	 */
	public JmeterFunctionsImpl(String threadName) {
		this.threadName = threadName;
		mainResult.sampleStart();
	}

	/**
	 * Called upon completion of the test run.
	 * 
	 * <p>Traverses the internal created transactionMap, looking for any transactions that had been
	 * started but not completed. If incomplete transactions are encountered then they are ended and flagged as
	 * "failed".</p>
	 * 
	 * <p>If a test execution contains one or more failed transactions, the entire script
	 * run is flagged as a failed test.</p>
	 * 
	 * <p>Once any outstanding transactions are completed, the SampleResult object is
	 * finalised (its status is set as PASS or FAIL).</p>
	 * 
	 * <p>
	 * this.tearDown() is called as part of the framework, so an end user should not need to call this method
	 * themselves unless they're using a custom implementation of AbstractJmeterTestRunner.runTest(JavaSamplerContext)
	 * </p>
	 */
	@Override
	public void tearDown() {	
		
		for (Entry<String, SampleResult> subResultEntry : transactionMap.entrySet()) {
			if (StringUtils.isBlank(subResultEntry.getValue().getResponseMessage())){
				endTransaction(subResultEntry.getValue().getSampleLabel(), Outcome.FAIL);
			}
		}

		if (allSamplesPassed() && !isForcedFail){
			tearDownMainResult(Outcome.PASS);
		} else {
			tearDownMainResult(Outcome.FAIL);
		}
		
		logThreadTransactionResults();
	}
	
	
	private boolean allSamplesPassed() {
		return Arrays.stream(mainResult.getSubResults()).allMatch(SampleResult::isSuccessful);
	}

	
	/**
	 * Completes the test main transaction - expected to be invoked at end of test script run.
	 * Note from JMeter 5.0 a call to set the end time of the main transaction is called as each  
	 * sub-result ends, so a call to the sampleEnd() method only needs to be made if no subResult 
	 * has already set the main transaction end time 
	 * 
	 * A data type of 'PARENT' is used to indicate this is a main result (normally expected to have sub-results)
	 * produced using the mark59 framework is set.  This is useful to to separate results from sub-results, particularly
	 * for JMeter result files in CSV format, as a CSV has a flat structure.  
	 * 
	 */
	private void tearDownMainResult(Outcome outcome) {
		if (mainResult.getEndTime() == 0) {
			mainResult.sampleEnd(); // stop stopwatch
		}
		mainResult.setSuccessful(outcome.isOutcomeSuccess());
		mainResult.setResponseMessage(outcome.getOutcomeText());
		mainResult.setResponseCode(outcome.getOutcomeResponseCode()); // 200 code
		
		mainResult.setDataType(JMeterFileDatatypes.PARENT.getDatatypeText() );
	}
	

	/**
	 * Starts timing a transaction.  Note you cannot start a transaction using the same name as one already running 
	 * in a script (controlled using an internally created transactionMap holding a key of running transaction names)
	 * and starts timing the transaction. 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @throws IllegalArgumentException if the transaction name supplied is an illegal value (null or empty) or already in use.
	 */
	@Override
	public void startTransaction(String transactionLabel) {
		startTransaction(transactionLabel, JMeterFileDatatypes.TRANSACTION);
	}
	
	
	
	/**
	 * Starts timing a transaction.  Note you cannot start a transaction using the same name as one already running 
	 * in a script (controlled using an internally created transactionMap holding a key of running transaction names)
	 * and starts timing the transaction. 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param jMeterFileDatatypes a {@link JMeterFileDatatypes} (it's text value will be written in the data type field of the JMeter results file)
	 * @throws IllegalArgumentException if the transaction name supplied is an illegal value (null or empty) or already in use.
	 */
	@Override
	public void startTransaction(String transactionLabel, JMeterFileDatatypes jMeterFileDatatypes) {
		if (StringUtils.isBlank(transactionLabel)) {
			throw new IllegalArgumentException("transactionLabel cannot be null or empty");
		}
		if (transactionMap.containsKey(transactionLabel)) {
			throw new IllegalArgumentException("Error -  a transaction using the passed transaction name appears to be currently"
					+ " in use (running) in this script : " + transactionLabel);
		}
		SampleResult sampleResult = new SampleResult();
		sampleResult.setSampleLabel(transactionLabel);
		sampleResult.setDataType(jMeterFileDatatypes.getDatatypeText());   // eg "CDP" for DevTools, blank for standard txn.	
		transactionMap.put(transactionLabel, sampleResult);
		mostRecentTransactionStarted = transactionLabel;
		sampleResult.sampleStart();
	}


	/**
	 * Ends an existing transaction (SampleResult), stopping the running timer.
	 * 
	 * <p>When the transaction is added to the main result (with a status of a 'passed')</p>
	 * <p>Elapsed time is recorded in milliseconds, the standard for JMeter</p>
	 * <p>Once a transaction is ended it is added to the main SampleResult object that will ultimately be returned upon script completion.<br>
	 * <p>Once a transaction is ended, an internal key entry for the transaction ('label' in JMeter terminology) is cleared, freeing the
	 *  transaction name to be re-used if desired.</p>
	 * 
	 * @param transactionLabel label for the transaction
	 * @throws IllegalArgumentException if the transactionLabel supplied is an illegal value (null or empty)
	 * @throws NoSuchElementException   if the transactionLabel doesn't exist in the  transactionMap
	 * @return the JMeter subresult for this transaction - which includes the transaction time (getTime)	 * 
	 */
	@Override
	public SampleResult endTransaction(String transactionLabel) {
		return endTransaction(transactionLabel, Outcome.PASS, null);
	}

	
	
	/**
	 * Ends an existing transaction (SampleResult), stopping the running timer.
	 * 
	 * <p>When the transaction is added to the main result, it will be given a success state based on the Outcome passed in</p>
	 * <p>Elapsed time is recorded in milliseconds, the standard for JMeter</p>
	 * <p>Once a transaction is ended it is added to the main SampleResult object that will ultimately be returned upon test completion.<br>
	 * <p>Once the transaction is ended, an internal key entry for the transaction ('label' in JMeter terminology) is cleared, freeing the
	 *  transaction name to be re-used if desired.</p>
	 * 
	 * @param transactionLabel label for the transaction
	 * @param result the success or failure state of the transaction
	 * 
	 * @throws IllegalArgumentException if the transactionLabel supplied is an illegal value (null or empty)
	 * @throws NoSuchElementException   if the transactionLabel doesn't exist in the  transactionMap
	 * @return the JMeter sub-result for this transaction (which includes the transaction time)
	 */
	public SampleResult endTransaction(String transactionLabel, Outcome result) {
		return endTransaction(transactionLabel, result, null);
	}
	
		
	/**
	 * Ends an existing transaction (SampleResult), stopping the running timer.
	 * 
	 * <p>When the transaction is added to the main result, it will be given a success state based on the Outcome passed in</p>
	 * <p>Elapsed time is recorded in milliseconds, the standard for JMeter</p>
	 * <p>Once a transaction is ended it is added to the main SampleResult object that will ultimately be returned upon test completion.<br>
	 * <p>Once a transaction is ended, an internal key entry for the transaction ('label' in JMeter terminology) is cleared, freeing the
	 *  transaction name to be re-used if desired.</p>
	 * 
	 * <p>Allows for a response message (which can be printed in a JMeter report for errored transactions).  This  will 
	 * default to "200" or  "-1" for passed/failed transaction if a blank or null message is passed.
	 * 
	 * @param transactionLabel label for the transaction
	 * @param result the success or failure state of the transaction
	 * @param responseCode response message (useful for error transactions) 
	 * 
	 * @throws IllegalArgumentException if the transactionLabel supplied is an illegal value (null or empty)
	 * @throws NoSuchElementException   if the transactionLabel doesn't exist in the  transactionMap
	 * @return the JMeter subresult for this transaction - which includes the transaction time (getTime)
	 */
	public SampleResult endTransaction(String transactionLabel, Outcome result, String responseCode) {
		if (StringUtils.isBlank(transactionLabel))
			throw new IllegalArgumentException("transactionLabel cannot be null or empty");

		if (!transactionMap.containsKey(transactionLabel))
			throw new NoSuchElementException(
					"could not find SampleResult in transactionMap as it does not contain a key matching the expected value : "
							+ transactionLabel);

		if (StringUtils.isBlank(responseCode)) 
			responseCode = result.getOutcomeResponseCode();
		
		SampleResult subResult = transactionMap.get(transactionLabel);
		subResult.sampleEnd();
		subResult.setSuccessful(result.isOutcomeSuccess());
		subResult.setResponseMessage(result.getOutcomeText());
		subResult.setResponseCode(responseCode);     // 200 | -1 | responseCode (passed string)
		subResult.setSampleLabel(transactionLabel);
		mainResult.addSubResult(subResult, false);   // prevents strange indexed named transactions (from Jmeter 5.0)		

		transactionMap.remove(transactionLabel);
		return subResult;
	}

	
	/**
	 * Adds a new SubResult to the main result, bypassing the transactionMap used by the timing methods: 
	 * this.startTransaction(String); this.endTransaction(String).
	 * 
	 * <p>The new SubResult must be given both a transactionLabel and a transactionTime, transactionTime
	 * is expected to be in milliseconds.
	 * 
	 * @param transactionLabel label for the transaction
	 * @param transactionTime time taken for the transaction. Expects Milliseconds.
	 * 
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 * @return SampleResult
	 */
	@Override
	public SampleResult setTransaction(String transactionLabel, long transactionTime){
		return setTransaction(transactionLabel, transactionTime, true);
	}
	
	
	/**
	 * Adds a new SubResult directly to the main result, without the need to use timing methods 
	 * startTransaction(txnName) and endTransaction(txnName).
	 * 
	 * <p>When the transaction is added to the main result, it will be given a success state based on the Outcome passed in</p>
	 * 
	 * <p>The new SubResult must be given both a transactionLabel and a transactionTime, transactionTime 
	 * is expected to be in milliseconds.
	 * 
	 * @param transactionLabel label for the transaction
	 * @param transactionTime time taken for the transaction. Expects Milliseconds.
	 * 
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 * @return SampleResult
	 */
	@Override
	public SampleResult setTransaction(String transactionLabel, long transactionTime, boolean success) {
		return createSubResult(transactionLabel, transactionTime, success ? Outcome.PASS : Outcome.FAIL, JMeterFileDatatypes.TRANSACTION, null );		
	}
	

	/**
	 * Adds a new SubResult directly to the main result, without the need to use timing methods 
	 * startTransaction(txnName) and endTransaction(txnName).
	 * 
	 * <p>When the transaction is added to the main result, it will be given a success state based on the Outcome passed in</p>
	 * 
	 * <p>The new SubResult must be given both a transactionLabel and a transactionTime, transactionTime 
	 * is expected to be in milliseconds.
	 * 
	 * <p>Allows for a response message (which can be printed in a JMeter report for errored transactions).  This  will 
	 * default to "200" / "-1" for passed/failed transaction if a blank or null message is passed.
	 * 
	 * @param transactionLabel label for the transaction
	 * @param transactionTime time taken for the transaction
	 * @param success  the success (true) or failure (false) state of the transaction
	 * @param responseCode response message (useful for error transactions)
	 *   
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 * @return SampleResult
	 */
	@Override
	public SampleResult setTransaction(String transactionLabel, long transactionTime, boolean success, String responseCode) {
		return createSubResult(transactionLabel, transactionTime, success ? Outcome.PASS : Outcome.FAIL, JMeterFileDatatypes.TRANSACTION, responseCode );		
	}
	
	/**
	 *  As per {@link #setTransaction(String, long, boolean, String)} but with the additional option of setting the data type
	 * field of the JMeter results file
	 * 
	 * @param transactionLabel label for the transaction
	 * @param jMeterFileDatatypes  a {@link JMeterFileDatatypes} (it's text value will be written in the data type field of the JMeter results file)
	 * @param transactionTime time taken for the transaction (ms)
	 * @param success success state of the transaction
	 * @param responseCode response message (useful for error transactions)
	 * @return SampleResult
	 */
	@Override
	public SampleResult setTransaction(String transactionLabel, JMeterFileDatatypes jMeterFileDatatypes, long transactionTime, boolean success, String responseCode) {
		return createSubResult(transactionLabel, transactionTime, success ? Outcome.PASS : Outcome.FAIL, jMeterFileDatatypes, responseCode );		
	}
	
	
	
	/**
	 * Similar to this.{@link #userDataPoint(String, long)}, but instead of just being able to create a JMeter sub-result of
	 * data type DATAPOINT, you can specify the type from the list of allowed types used by Mark59 as defined by
	 * {@link Mark59Constants.JMeterFileDatatypes} 
	 *  
	 * <p>For example 
	 * <pre><code>
	 * jm.userDatatypeEntry("MyServersCPUUtilizationPercent", 40L, Mark59Constants.JMeterFileDatatypes.CPU_UTIL );	
	 * </code></pre>
	 * @return SampleResult
	 */ 
	@Override
	public SampleResult userDatatypeEntry(String dataPointName, long dataPointValue,  JMeterFileDatatypes jMeterFileDatatypes) {
		if (LOG.isDebugEnabled()) LOG.debug(" userDatatypeEntry Name:Value [ " + dataPointName + ":" + dataPointValue + ":" +  jMeterFileDatatypes.getDatatypeText() + "]");
		return createSubResult(dataPointName, dataPointValue, Outcome.PASS, jMeterFileDatatypes, null);		
	}
	
	
	/**
	 * Adds a new SubResult to the main result reflecting a non-timing related metric - a 'DATAPOINT'.
	 * <p>A DATAPOINT must be given both a dataPointName and a dataPointValue, the value being
	 *  any arbitrary long value.</p>
	 * 
	 * @param dataPointName label for the DATAPOINT
	 * @param dataPointValue an arbitrary non-timing metric
	 * 
	 * @throws IllegalArgumentException if the dataPointName is null or empty
	 * @return SampleResult
	 */
	@Override
	public SampleResult userDataPoint(String dataPointName, long dataPointValue ) {
		if (LOG.isDebugEnabled()) LOG.debug(" userDataPoint Name:Value [ " + dataPointName + ":" + dataPointValue + "]");
		return createSubResult(dataPointName, dataPointValue, Outcome.PASS, JMeterFileDatatypes.DATAPOINT, null);
	}
	
	
	private SampleResult createSubResult(String dataPointName, long dataPointValue, Outcome result, JMeterFileDatatypes jmeterFileDatatypes, String responseCode) {
		if (StringUtils.isBlank(dataPointName))
			throw new IllegalArgumentException("dataPointName cannot be null or empty");
		if (StringUtils.isBlank(responseCode)) 
			responseCode = result.getOutcomeResponseCode();
		
		SampleResult subResult = new SampleResult(); 
		subResult.setSuccessful(result.isOutcomeSuccess());             // true | false
		subResult.setResponseCode(responseCode);                        // 200 | -1 | responseCode (passed string)
		subResult.setResponseMessage(result.getOutcomeText());          // PASS | FAIL
		subResult.setDataType(jmeterFileDatatypes.getDatatypeText() );	
		subResult.setSampleLabel(dataPointName);
		subResult.sampleStart();    
		subResult.setEndTime(subResult.getStartTime() + dataPointValue );	
		mainResult.addSubResult(subResult, false);
		return subResult;
	}			
	
	
	
	
	
	/**
	 *  Return results from running the test.  Specifically for the Mark59 implementation,
	 *  it can be used to access the transaction results in a running script by 
	 *  getting the subResults list.
	 */
	@Override
	public SampleResult getMainResult() {
		return mainResult;
	}
	
	
	/**
	 * Returns the transaction id of the last (most recent) transaction started. 
	 * Excludes SET transactions and DataPoints.
	 * 
	 * @return mostRecentTransactionStarted (txnId)
	 */
	public String getMostRecentTransactionStarted() {
		return mostRecentTransactionStarted;
	}
	
	
	/**
	 * Fetches the SampleResult from the transactionMap that matches the supplied label.
	 * The transactionMap is primarily intended as an internal key tracking mechanism to prevent multiple 
	 * transactions with the same name being started and running concurrently within the one script  
	 * 
	 * <p>If it fails to find a SampleResult it either means no such label had been added to the transactionMap, 
	 * or the SampleResult has already been finalized and added to the main result.</p>
	 * 
	 * @param label the transaction label for the SampleResult to locate.
	 * @return SampleResult belonging to the supplied label.
	 */
	public SampleResult getSampleResultWithLabel(String label) {
		
		
		System.out.println( ">> transactionMap");
		System.out.println( Mark59Utils.prettyPrintMap(transactionMap)  );
		System.out.println( "<< transactionMap");
		
		return transactionMap.get(label);
	}
	
	/**
	 * Searches the main result for all instances of the supplied label, collating the SampleResults into a List and returning all of them.
	 * Note: primary purpose is as a helper method for junit testing. 
	 *  
	 * @param label the transaction label for the SampleResults to locate.
	 * @return  a list of sample results
	 */
	public List<SampleResult> getSampleResultFromMainResultWithLabel(String label) {
		return Arrays.stream(mainResult.getSubResults())
				.filter(sr -> sr.getSampleLabel().equals(label))
				.collect(Collectors.toList());
	}

	
	private void logThreadTransactionResults() {
		SampleResult[] sampleResult = mainResult.getSubResults();
		LOG.info("");
		LOG.info(Thread.currentThread().getName() + " result  (" + mainResult.getResponseMessage() + ")"   ) ; 
		LOG.info(String.format("%-40s%-10s%-60s%-20s%-20s", "Thread", "#", "txn name", "Resp Message", "resp time"));

		for (int i = 0; i < sampleResult.length; i++) {
			SampleResult subSR = sampleResult[i];
			
			if (StringUtils.isBlank(subSR.getDataType())) {
				LOG.info(String.format("%-40s%-10s%-60s%-20s%-20s", threadName, i, subSR.getSampleLabel(),
						subSR.getResponseMessage(), subSR.getTime()));				
			} else {
				LOG.info(String.format("%-40s%-10s%-60s%-20s%-20s", threadName, i, subSR.getSampleLabel(),
						subSR.getResponseMessage() + " (" + subSR.getDataType() + ")" , subSR.getTime()));
			}
		}
		LOG.info("");
	}

	/**
	 * Called to set the main result of a test to a failed state, regardless of the state of the sub results attached to the main result.
	 * <p>Normally, a main result would only fail if at least one of it's sub results was a fail.</p>
	 */
	@Override
	public void failTest() {
		isForcedFail = true;
	}
}
