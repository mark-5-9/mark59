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
import org.apache.log4j.Logger;

import com.mark59.core.interfaces.JmeterFunctions;



/**
 * Implements the JmeterFunctions interface, with methods that can be called throughout the lifecycle of the test in order to handle behavior around transaction recording and timing.
 * 
 * <p>For example, typical usage from a scripting perspective is to time an event(s).  For example:
 * <pre><code>
 * jm.startTransaction("DH-lifecycle-0100-deleteMultiplePolicies");		
 * deleteMultiplePoliciesPage.submit().submit();
 * jm.endTransaction("DH-lifecycle-0100-deleteMultiplePolicies")
 * </code></pre>
 * 
 * <p>where '<code>jm</code>' is this class, or an extension of this class (eg SeleniumAbstractJavaSamplerClient)
 * 
 * <p>The class works by holding transaction results in a map ('tansactionMap') of SampleResult (subResults of a 'main' SampleResult).  At the end of the 
 * script ({@link #tearDown()} the tansactionMap is processed and the results output.  
 * 
 * 
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019  
 */
public class JmeterFunctionsImpl implements JmeterFunctions {

	private static final Logger LOG = Logger.getLogger(JmeterFunctionsImpl.class);

	public static final String DATAPOINT = "DATAPOINT";  
	
	protected SampleResult mainResult = new SampleResult();
	protected Map<String, SampleResult> transactionMap = new ConcurrentHashMap<>();
	protected String threadName;

	protected boolean isForcedFail;
	
	public JmeterFunctionsImpl(String threadName) {
		this.threadName = threadName;
		mainResult.sampleStart();
	}

	/**
	 * Called upon completion of the test run.
	 * 
	 * <p>
	 * Traverses the transactionMap, looking for any transactions that had been
	 * started but not completed.<br>
	 * If incomplete transactions are encountered then they are ended and flagged as
	 * "failed".<br>
	 * If a test execution contains one or more failed transactions, the entire script
	 * run is flagged as a failed test.
	 * </p>
	 * 
	 * <p>
	 * Once any outstanding transactions are completed, the SampleResult object is
	 * finalised, ready for return.
	 * </p>
	 * 
	 * <p>
	 * this.tearDown() is called as part of the framework, so an end user ought not
	 * need to call teardown themselves unless they're using a custom implementation
	 * of AbstractJmeterTestRunner.runTest(JavaSamplerContext)
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
		return Arrays.asList(mainResult.getSubResults()).stream().noneMatch(sr->!sr.isSuccessful());
	}

	
	/**
	 * Completes the test main transaction - expected to be invoked at end of test script run.
	 * Note from Jmeter 5.0 a call to set the end time of the main transaction is called as each  
	 * sub-result ends, so a call to the sampleEnd() method only needs to be made if no subResult 
	 * has already set the main transaction end time 
	 * 
	 * A data type to indicate this is a main result (normally expected to have sub-results) produced
	 * using the mark59 framework is set (can be used to separate results from sub-results, particularly
	 * when output is in CSV format).  
	 * 
	 */
	private void tearDownMainResult(Outcome outcome) {
		if (mainResult.getEndTime() == 0) {
			mainResult.sampleEnd(); // stop stopwatch
		}
		mainResult.setSuccessful(outcome.isOutcomeSuccess());
		mainResult.setResponseMessage(outcome.getOutcomeText());
		mainResult.setResponseCode(outcome.getOutcomeResponseCode()); // 200 code
		
		mainResult.setDataType(OutputDatatypes.PARENT.getOutputDatatypeText() );
	}
	

	/**
	 * Adds a new SampleResult to transactionMap and starts the timer.
	 * 
	 * @param transactionLabel label for the transaction
	 * 
	 * @throws IllegalArgumentException if the transactionLabel supplied is an illegal value (null or empty) or already in use.
	 */
	@Override
	public void startTransaction(String transactionLabel) {
		if (StringUtils.isBlank(transactionLabel))
			throw new IllegalArgumentException("transactionLabel cannot be null or empty");

		if (transactionMap.containsKey(transactionLabel))
			throw new IllegalArgumentException(
					"could not add new SampleResult to transactionMap as it already contains a key matching the supplied value : "
							+ transactionLabel);

		SampleResult sampleResult = new SampleResult();
		sampleResult.setSampleLabel(transactionLabel);

		transactionMap.put(transactionLabel, sampleResult);

		sampleResult.sampleStart();
	}

	
	/**
	 * Ends an existing SampleResult, stopping the timer.
	 * 
	 * <p>
	 * Elapsed time is recorded in milliseconds.<br>
	 * Once a transaction is ended it is added to the main SampleResult object that
	 * will ultimately be returned upon test completion.<br>
	 * Upon adding the ended transaction to the main SampleResult object, it is
	 * removed from the transactionMap, freeing the label to be re-used if desired.
	 * </p>
	 * 
	 * @param transactionLabel label for the transaction
	 * 
	 * @throws IllegalArgumentException if the transactionLabel supplied is an
	 *                                  illegal value (null or empty)
	 * @throws NoSuchElementException   if the transactionLabel doesn't exist in the
	 *                                  transactionMap
	 */
	@Override
	public void endTransaction(String transactionLabel) {
		endTransaction(transactionLabel, Outcome.PASS);
	}
	
	
	/**
	 * Ends an existing SampleResult, stopping the timer.
	 * 
	 * <p>When the transaction is added to the main result, it will be given a success state based on the Outcome passed in</p>
	 * 
	 * <p>
	 * Elapsed time is recorded in milliseconds.<br>
	 * Once a transaction is ended it is added to the main SampleResult object that
	 * will ultimately be returned upon test completion.<br>
	 * Upon adding the ended transaction to the main SampleResult object, it is
	 * removed from the transactionMap, freeing the label to be re-used if desired.
	 * </p>
	 * 
	 * @param transactionLabel label for the transaction
	 * @param result the success or failure state of the transaction
	 * 
	 * @throws IllegalArgumentException if the transactionLabel supplied is an
	 *                                  illegal value (null or empty)
	 * @throws NoSuchElementException   if the transactionLabel doesn't exist in the
	 *                                  transactionMap
	 */
	public void endTransaction(String transactionLabel, Outcome result) {
		if (StringUtils.isBlank(transactionLabel))
			throw new IllegalArgumentException("transactionLabel cannot be null or empty");

		if (!transactionMap.containsKey(transactionLabel))
			throw new NoSuchElementException(
					"could not find SampleResult in transactionMap as it does not contain a key matching the expected value : "
							+ transactionLabel);

		SampleResult subResult = transactionMap.get(transactionLabel);
		subResult.sampleEnd();
		subResult.setSuccessful(result.isOutcomeSuccess());
		subResult.setResponseMessage(result.getOutcomeText());
		subResult.setResponseCode(result.getOutcomeResponseCode()); // 200 code
		subResult.setSampleLabel(transactionLabel);
		mainResult.addSubResult(subResult, false);   // prevents strange indexed named transactions (from Jmeter 5.0)		

		transactionMap.remove(transactionLabel);
	}
	
	/**
	 * Adds a new SubResult to the main result, bypassing the transactionMap used by the timing methods: this.startTransaction(String); this.endTransaction(String).
	 * 
	 * <p>The new SubResult must be given both a transactionLabel and a transactionTime<br>
	 * transactionTime is expected to be in milliseconds, as per the timing methods, but not enforced, so you can do with it as you will within the constraints of a single long</p>
	 * 
	 * @param transactionLabel label for the transaction
	 * @param transactionTime time taken for the transaction. Expects Milliseconds, but doesn't enforce.
	 * 
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 */
	@Override
	public void setTransaction(String transactionLabel, long transactionTime){
		setTransaction(transactionLabel, transactionTime, true);
	}
	
	/**
	 * Adds a new SubResult to the main result, bypassing the transactionMap used by the timing methods: this.startTransaction(String); this.endTransaction(String).
	 * 
	 * <p>When the transaction is added to the main result, it will be given a success state based on the Outcome passed in</p>
	 * 
	 * <p>The new SubResult must be given both a transactionLabel and a transactionTime<br>
	 * transactionTime is expected to be in milliseconds, as per the timing methods, but not enforced, so you can do with it as you will within the constraints of a single long</p>
	 * 
	 * @param transactionLabel label for the transaction
	 * @param transactionTime time taken for the transaction. Expects Milliseconds, but doesn't enforce.
	 * @param success the success or failure state of the transaction
	 * 
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 */
	@Override
	public void setTransaction(String transactionLabel, long transactionTime, boolean success) {
		createSubResult(transactionLabel, transactionTime, success ? Outcome.PASS : Outcome.FAIL, OutputDatatypes.TRANSACTION );		
	}


	
	@Override
	public void userDatatypeEntry(String dataPointName, long dataPointValue,  OutputDatatypes outputDatatype) {
		if (LOG.isDebugEnabled()) LOG.debug(" userDatatypeEntry Name:Value [ " + dataPointName + ":" + dataPointValue + ":" +  outputDatatype.getOutputDatatypeText() + "]");
		createSubResult(dataPointName, dataPointValue, Outcome.PASS, outputDatatype);		
	}
	
	
	/**
	 * Adds a new SubResult to the main result reflecting a non-timing related metric.
	 * 
	 * <p>The new SubResult must be given both a dataPointName and a dataPointValue.</p>
	 * 
	 * <p>dataPoint value is a non-timing value, used to record any arbitrary long value.</p>
	 * 
	 * @param dataPointName label for the datapoint
	 * @param dataPointValue an arbitrary non-timing metric
	 * 
	 * @throws IllegalArgumentException if the dataPointName is null or empty
	 */
	@Override
	public void userDataPoint(String dataPointName, long dataPointValue ) {
		if (LOG.isDebugEnabled()) LOG.debug(" userDataPoint Name:Value [ " + dataPointName + ":" + dataPointValue + "]");
		createSubResult(dataPointName, dataPointValue, Outcome.PASS, OutputDatatypes.DATAPOINT);
	}
	
	
	private void createSubResult(String dataPointName, long dataPointValue, Outcome result, OutputDatatypes outputDatatype) {
		if (StringUtils.isBlank(dataPointName))
			throw new IllegalArgumentException("dataPointName cannot be null or empty");
		
		SampleResult subResult = new SampleResult(); 
		subResult.setSuccessful(result.isOutcomeSuccess());
		subResult.setResponseMessage(result.getOutcomeText());		
		subResult.setDataType(outputDatatype.getOutputDatatypeText() );
		subResult.setSampleLabel(dataPointName);
		subResult.sampleStart();    
		subResult.setEndTime(subResult.getStartTime() + dataPointValue );	
		mainResult.addSubResult(subResult, false);
		
	}			
	
	@Override
	public SampleResult getMainResult() {
		return mainResult;
	}
	
	/**
	 * Fetches the SampleResult from the transactionMap that matches the supplied
	 * label.
	 * 
	 * <p>
	 * If it fails to find a SampleResult it either means no such label had been
	 * added to the transactionMap, or the SampleResult has already been finalised
	 * and added to the main result.
	 * </p>
	 * 
	 * @param label the transaction label for the SampleResult to locate.
	 * @return SampleResult belonging to the supplied label.
	 */
	public SampleResult getSampleResultWithLabel(String label) {
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
		return Arrays.asList(mainResult.getSubResults()).stream()
				.filter(sr -> sr.getSampleLabel().equals(label))
				.collect(Collectors.toList());
	}

	
	private void logThreadTransactionResults() {
		SampleResult[] sampleResut = mainResult.getSubResults();
		LOG.info(Thread.currentThread().getName() + " sub resuts list : " + sampleResut.length);
		LOG.info(String.format("%-40s%-10s%-60s%-20s%-20s%n", "Thread", "#", "txn name", "Resp Message", "resp time"));

		for (int i = 0; i < sampleResut.length; i++) {
			SampleResult subSR = sampleResut[i];
			LOG.info(String.format("%-40s%-10s%-60s%-20s%-20s%n", threadName, i, subSR.getSampleLabel(),
					subSR.getResponseMessage(), subSR.getTime()));
		}
	}

	/**
	 * Called to set the main result of a test to a failed state, regardless of the state of the sub results attached to the main result.
	 * 
	 * <p>Normally, a main result would only fail if at least one of it's sub results was a fail.</p>
	 */
	@Override
	public void failTest() {
		isForcedFail = true;
	}
}
