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

package com.mark59.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.interfaces.DriverFunctions;
import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.core.utils.Mark59LogLevels;
import com.mark59.core.utils.Mark59LoggingConfig;
import com.mark59.core.utils.StaticCounter;


/**
 * Implements the JmeterFunctions interface, with methods that can be called throughout the life cycle of the test in order 
 * to handle behavior around logging, transaction recording and timing.
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
 * <p>The class works by creating JMeter 'sub-results', one per recorded transaction, which are attached to a main SampleResult.
 * At the end of the script the sub-results ({@link #tearDown()} are printed (at LOG info level).  
 * 
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019  
 */
public class JmeterFunctionsImpl implements JmeterFunctions {

	private static final Logger LOG = LogManager.getLogger(JmeterFunctionsImpl.class);
	
	/**
	 * Intention is that this string is used as a JMeter Parameter to flag if Transaction Results Summary should be logged 
	 * (jmeter.log). The default expected would be to not log the summary when executing from JMeter.  Eg, in the
	 * 'additionalTestParameters' method of a Mark59 selenium script:
	 * 
	 * <p><code>jmeterAdditionalParameters.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(false));</code>	
	 * 
	 * <p>When running from the IDE you may want the default to be to print the Results Summary. This is done for Mark59
	 * selenium scripts, and can be done for custom implementations using this field. For an example, see the 
	 * mark59-datahunter-samples project, class DataHunterLifecyclePvtScriptUsingRestApiClient.    
	 * <p>Also see {@link #isPrintResultsSummary}     
	 */
	public static final String LOG_RESULTS_SUMMARY = "LOG_RESULTS_SUMMARY";
	
	/**
	 * The same as {@link #LOG_RESULTS_SUMMARY}, but output goes to console. 
	 */
	public static final String PRINT_RESULTS_SUMMARY = "PRINT_RESULTS_SUMMARY";	
	
	
	/**
	 * see {@link Mark59LoggingConfig}
	 */	
	protected static Mark59LoggingConfig loggingConfig;	

	/**
	 * the JMeter main sample result for the script
	 */
	protected SampleResult mainResult = new SampleResult();
	
	/**
	 * The executed and in-flight transactions
	 * Intended as an internal key tracking mechanism to prevent multiple transactions with the
	 * same name being started and running concurrently within the one script    
	 */
	protected Map<String, SampleResult> transactionMap = new ConcurrentHashMap<>();
	
	/**
	 * holds most Recent Transaction Started  (used in the sample scripts by the DevTools DSL)
	 */
	protected String mostRecentTransactionStarted;
	
	/**
	 * holds current thread Name 
	 */
	protected String threadName;

	/**
	 * used to force the outcome of test script to Failed
	 */
	protected boolean isForcedFail;

	/**
	 * Used to flag if Transaction Results Summary should be logged. 
	 * The default is not to log. Also see {@link #LOG_RESULTS_SUMMARY}     
	 */
	protected boolean isLogResultsSummary = false;	

	/**
	 * Used to flag if Transaction Results Summary should be printed. 
	 * The default is not to print. Also see {@link #PRINT_RESULTS_SUMMARY}     
	 */
	protected boolean isPrintResultsSummary = false;
	
	/**
	 * map of captured logs as a byte array 
	 * (key would usually be expected to be filename for most implementations)
	 */
	protected Map<String, byte[]> bufferedArtifacts = new HashMap<>();
	
	/**
	 * the 'constant' bit of all mark59 log names for a particular script 
	 */
	protected String leadingPartOfLogNames;

	
	/**
	 * On instantiation will use mark59.properties to setup Mark59 logging configuration (directory naming),
	 * if not already done.
	 * 
	 * <p>Also see {@link #JmeterFunctionsImpl(JavaSamplerContext, boolean) } 
	 * (which is invoked with isMark59PropertyConfigurationRequired set to true). 
	 * 
	 * @param context the JMeter JavaSamplerContext
	 */
	public JmeterFunctionsImpl(JavaSamplerContext context) {
		this(context, true);
	}

	
	/**
	 * On instantiation will start the 'main' JMeter sampler timer (to which 'sub-results' can be added to using the methods of this class) 
	 * 
	 * <p>Some implementations will not require any information from mark59.properties. That is, they will not log to a 'Mark59 log' directory,
	 * do not need the location of a Selenium driver location, the location of a profiles excel sheet, or any other property that may be set
	 * in mark59.properties.  For example, the metrics capture via web Java Request does not rely on mark59.properties at all.
	 * 
	 * <p>For such implementations, this constructor should be used, with 'isMark59PropertyConfigurationRequired' set as false.
	 * This will stop INFO/WARNIMG messages about mark59.properties not being set in a JMeter test. 
	 * 
	 * @param context the JMeter JavaSamplerContext
	 * @param isMark59PropertyConfigurationRequired determines if mark59 properties will be accessed using this implementation 
	 */
	public JmeterFunctionsImpl(JavaSamplerContext context, boolean isMark59PropertyConfigurationRequired) {
		threadName =Thread.currentThread().getName();
		
		if (isMark59PropertyConfigurationRequired){
			loggingConfig = Mark59LoggingConfig.getInstance();
			leadingPartOfLogNames = formLeadingPartOfLogNames(loggingConfig.getLogNamesFormat(), context);
		}
		
		logResultSummary(false);
		if (context!=null && String.valueOf(true).equalsIgnoreCase(context.getParameter(JmeterFunctionsImpl.LOG_RESULTS_SUMMARY, String.valueOf(false)))) {
			logResultSummary(true);
		}
		
		printResultSummary(false);
		if (context!=null && String.valueOf(true).equalsIgnoreCase(context.getParameter(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY,String.valueOf(false)))){
			printResultSummary(true);
		}	

		mainResult.sampleStart();
	}
	
	
	/**
	 * The leading components a for log name of a given Selenium script are constant, so can be set during 
	 * creation of this JMeter functions class for a script   
	 * 
	 * @param logNamesFormat  from the Mark59 property "mark59.logname.format"
	 * @param context the JMeter JavaSamplerContext
	 */
	private String formLeadingPartOfLogNames(String logNamesFormat, JavaSamplerContext context) {
		String leadingPartOfLogNames = null;
		
		if (loggingConfig.getLogDirectory() != null) {
			leadingPartOfLogNames = loggingConfig.getLogDirectory().getPath() + File.separator;
		
			if (logNamesFormat.contains(Mark59Constants.THREAD_NAME)){
				leadingPartOfLogNames += threadName + "_"; 	
			}
			if (logNamesFormat.contains(Mark59Constants.THREAD_GROUP)){
				if (context.getJMeterContext() != null  && context.getJMeterContext().getThreadGroup() != null){
					leadingPartOfLogNames += context.getJMeterContext().getThreadGroup().getName() + "_" ;
				} else {
					leadingPartOfLogNames += "noTG_"; 
				}
			}	
			if (logNamesFormat.contains(Mark59Constants.SAMPLER)){
				if (context.getJMeterContext() != null  && context.getJMeterContext().getCurrentSampler() != null){
					leadingPartOfLogNames += context.getJMeterContext().getCurrentSampler().getName() + "_" ;
				} else {
					leadingPartOfLogNames += "noSampler_"; 
				}
			}
		
		}
		return StringUtils.removeEnd(leadingPartOfLogNames, "_");
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
					"Could not find a transactionn to end matching the passed label : "	+ transactionLabel);

		if (StringUtils.isBlank(responseCode)) 
			responseCode = result.getOutcomeResponseCode();
		
		SampleResult subResult = transactionMap.get(transactionLabel);
		subResult.sampleEnd();
		subResult.setSuccessful(result.isOutcomeSuccess());
		subResult.setResponseMessage(result.getOutcomeText());
		subResult.setResponseCode(responseCode);     // 200 | -1 | responseCode (passed string)
		subResult.setSampleLabel(transactionLabel);
		mainResult.addSubResult(subResult, false);   // 'false' prevents strange indexed named transactions (from Jmeter 5.0)		

		transactionMap.remove(transactionLabel);
		return subResult;
	}

	
	/**
	 * Ends an existing transaction (SampleResult), stopping the running timer.
	 * 
	 * <p>Functions as per {@link #endTransaction(String, Outcome, String)}, but does <b>NOT</b> add this transaction into
	 * the main results.  That is, the transaction will not appear in the reported results.       
	 * 
	 * @param transactionLabel transactionLabel label for the transaction
	 * @param result the success or failure state of the transaction
	 * @param responseCode response message (useful for error transactions) 
	 * @return the JMeter subresult for this transaction - which includes the transaction time (getTime)
	 */
	public SampleResult endTransactionTimingButDontRecordResult(String transactionLabel, Outcome result, String responseCode) {
		if (StringUtils.isBlank(transactionLabel))
			throw new IllegalArgumentException("transactionLabel cannot be null or empty");

		if (!transactionMap.containsKey(transactionLabel))
			throw new NoSuchElementException(
					"Could not find a transactionn to end matching the passed label : "	+ transactionLabel);

		if (StringUtils.isBlank(responseCode)) 
			responseCode = result.getOutcomeResponseCode();
		
		// timing is captured and returned but NOT added to the main Result
		SampleResult subResult = transactionMap.get(transactionLabel);
		subResult.sampleEnd();
		subResult.setSuccessful(result.isOutcomeSuccess());
		subResult.setResponseMessage(result.getOutcomeText());
		subResult.setResponseCode(responseCode);     // 200 | -1 | responseCode (passed string)
		subResult.setSampleLabel(transactionLabel);
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
	public SampleResult setTransaction(String transactionLabel, JMeterFileDatatypes jMeterFileDatatypes, long transactionTime, boolean success, String responseCode){
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
		if (LOG.isDebugEnabled())LOG.debug(" userDatatypeEntry [" + dataPointName + ":" + dataPointValue + ":" + jMeterFileDatatypes.getDatatypeText() + "]");
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
	
	
	private SampleResult createSubResult(String dataPointName, long dataPointValue, Outcome result, JMeterFileDatatypes jmeterFileDatatypes, String responseCode){
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
	 * Rename a transaction that has already completed within a running script.  If the same transaction
	 * name is used multiple times, all occurrences of the transaction name will be renamed. 
	 * 
	 * @param fromTxnName existing name ('label' in JMeter terminology) of the transaction
	 * @param toTxnName  what the transaction is to be renamed as
	 * 
	 * @see JmeterFunctionsImpl#renameTransactionsPrefixedBy(String, String)	
	 * @see JmeterFunctionsImpl#renameTransactions(Predicate, Function)	
	 */
	public void renameTransaction(String fromTxnName, String toTxnName) {
		renameTransactions(
				sampleResult -> sampleResult.getSampleLabel().equals(fromTxnName)
			 ,  sampleResult -> {return toTxnName;});
	}
	
	
	/**
	 * Rename all transactions that are prefixed with a given string, with a new prefix, for transactions 
	 * that have already completed within a running script.
	 * 
	 * <p>As CDP transactions are also re-named, this method can be useful when renaming a transaction 
	 * that has a related set of CDP transactions associated with it (obviously depending on your naming 
	 * conventions). 
	 * 
	 * @param origPrefix  prefix of the existing transactions name ('label' in JMeter terminology)
	 * @param newPrefixRegex replacement prefix (can be a Regex expression) 
	 * 
	 * @see JmeterFunctionsImpl#renameTransaction(String, String)	
	 * @see JmeterFunctionsImpl#renameTransactions(Predicate, Function)	
	 */
	public void renameTransactionsPrefixedBy(String origPrefix, String newPrefixRegex) {
		renameTransactions(
				sampleResult -> sampleResult.getSampleLabel().startsWith(origPrefix)
			 ,  sampleResult -> {
					return RegExUtils.replaceFirst(sampleResult.getSampleLabel(), origPrefix, newPrefixRegex);
			});
	};	
	
	
	/**
	 * Rename all transactions that satisfy the Predicate condition passed, for transactions 
	 * that have already completed within a running script.  
	 * 
	 * <p>The new name for a transaction is defined by the Function passed to the method.
	 * 
	 * <p>For examples of creating Predicates and Functions passed to this method, refer to 'See Also'  below  
	 * 
	 * @param transactionSelection Predicate function to select SampleResult(s) transaction names 
	 * ('labels' in JMeter terminology) to be renamed
	 * @param transactionRename  Function returning the new transaction ('label') name for a selected SampleResults
	 * 
	 * @see JmeterFunctionsImpl#renameTransaction(String, String)	
	 * @see JmeterFunctionsImpl#renameTransactionsPrefixedBy(String, String)      
	 */
	public void renameTransactions(Predicate<SampleResult> transactionSelection, Function<SampleResult, String> transactionRename){
		SampleResult[] subresults =  mainResult.getSubResults();

		for (int i = 0; i < subresults.length; i++) {
			SampleResult sampleResult = subresults[i];
			
			if (transactionSelection.test(sampleResult) ) {
				sampleResult.setSampleLabel(transactionRename.apply(sampleResult) );
			}
		} 
	}
	
	
	/**
	 * Delete a transaction that has already completed within a running script.  If the same transaction
	 * name is used multiple times, all occurrences of the transaction name will be deleted.
	 * 
	 * <p>Note: Due to the way the underlying SampleResults JMeter class works, this method actually
	 * saves, removes and then rebuilds the 'sub-results' transactions list, excluding transactions to be deleted.
	 * The effect is that this method isn't guaranteed thread-safe, so we suggest performing a deletion
	 * when you are unlikely to clash with another thread (ie CDP transactions).  For example, at the
	 * end of a script if possible.      
	 * 
	 * @param transactionName  name ('label' in JMeter terminology) of the transaction to be deleted
	 * 
	 * @see JmeterFunctionsImpl#deleteTransactionsPrefixedBy(String)
	 * @see JmeterFunctionsImpl#deleteTransactions(Predicate) 	
	 */
	public void deleteTransaction(String transactionName){
		deleteTransactions(	sampleResult -> sampleResult.getSampleLabel().equals(transactionName));
	}
	
	
	/**
	 * Delete all transactions that are prefixed with a given string for transactions that have already completed
	 * within a running script.
	 * 
	 * <p>As CDP transactions are also deleted, this method can be useful when deleting a transaction 
	 * that has a related set of CDP transactions associated with it (that you also want deleted, and 
	 * obviously depends on your naming conventions). 
	 * 
	 * <p>Note: Due to the way the underlying SampleResults JMeter class works, this method actually
	 * saves, removes and then rebuilds the 'sub-results' transactions list, excluding transactions to be deleted.
	 * The effect is that this method isn't guaranteed thread-safe, so we suggest performing a deletion
	 * when you are unlikely to clash with another thread (ie CDP transactions).  For example, at the
	 * end of a script if possible.  
	 *  
	 * @param transactionPrefix prefix of the existing transactions name ('label' in JMeter terminology) to be deleted
	 * 
	 * @see JmeterFunctionsImpl#deleteTransaction(String)
	 * @see JmeterFunctionsImpl#deleteTransactions(Predicate) 
	 */
	public void deleteTransactionsPrefixedBy(String transactionPrefix) {
		deleteTransactions(	sampleResult -> sampleResult.getSampleLabel().startsWith(transactionPrefix));
	};	
	

	/**
	 * Delete all transactions that satisfy the Predicate condition passed, for transactions 
	 * that have already completed within a running script.  
	 * 
	 * <p>For examples of creating Predicates passed to this method, refer to 'See Also'  below   
	 * 
	 * <p>Note: Due to the way the underlying SampleResults JMeter class works, this method actually
	 * saves, removes and then rebuilds the 'sub-results' transactions list, excluding transactions to be deleted.
	 * The effect is that this method isn't guaranteed thread-safe, so we suggest performing a deletion
	 * when you are unlikely to clash with another thread (ie CDP transactions).  For example, at the
	 * end of a script if possible. 
	 * 
	 * @param transactionSelection  Predicate function to select SampleResult(s) transaction names 
	 * ('labels' in JMeter terminology) to be deleted
	 * 
	 * @see JmeterFunctionsImpl#deleteTransaction(String)
	 * @see JmeterFunctionsImpl#deleteTransactionsPrefixedBy(String)
	 */
	public synchronized void deleteTransactions(Predicate<SampleResult> transactionSelection){
		SampleResult[] subresults =  mainResult.getSubResults();

		// clear immediately so at least you only have a small chance of loosing (CDP) transactions
		mainResult.removeSubResults();  

		for (int i = 0; i < subresults.length; i++) {
			SampleResult sampleResult = subresults[i];
			if (!transactionSelection.test(sampleResult) ) {
				mainResult.addSubResult(sampleResult, false);				
			} 
		}
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
	 * This implementation excludes SET transactions and DataPoints.
	 * 
	 * @return mostRecentTransactionStarted (txnId)
	 */
	@Override
	public String getMostRecentTransactionStarted() {
		return mostRecentTransactionStarted;
	}
	

	/**
	 * Called upon completion of the test run.
	 * 
	 * <p>Traverses the internal created transactions Map, looking for any transactions that had been
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
		
		if (isLogResultsSummary) {
			logThreadTransactionResults();
		}
		
		if (isPrintResultsSummary) {
			printThreadTransactionResults();
		}		
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
	 * Called to set the main result of a test to a failed state, regardless of the state of the sub results attached to the main result.
	 * <p>Normally, a main result would only fail if at least one of it's sub results was a fail.</p>
	 */
	@Override
	public void failTest() {
		isForcedFail = true;
	}

	
	/**
	 * Used to flag if Transaction Results Summary should be logged.<br> 
	 * See {@link #LOG_RESULTS_SUMMARY} 
	 * @param isLogResultsSummary flag if Transaction Results Summary should be logged
	 */
	public void logResultSummary(boolean isLogResultsSummary) {
		this.isLogResultsSummary = isLogResultsSummary;
	}
	
	
	/**
	 * Used to flag if Transaction Results Summary should be printed.<br>
	 * See {@link #PRINT_RESULTS_SUMMARY} 
	 * @param isPrintResultsSummary flag if Transaction Results Summary should be printed
	 */
	public void printResultSummary(boolean isPrintResultsSummary) {
		this.isPrintResultsSummary = isPrintResultsSummary;
	}
		
	
	/**
	 * @return a map of the buffered logs (keyed by name) 
	 */
	@Override	
	public Map<String, byte[]> getBufferedLogs() {
		return bufferedArtifacts;
	}

	
	/**
	 * Writes all buffered screenshots/logs to disk (eg, all transaction-level logging performed using
	 * a Mark59LogLevels of "BUFFER")
	 * @see Mark59LogLevels
	 */
	@Override		
	public void writeBufferedArtifacts() {
		LOG.debug("Writing " + bufferedArtifacts.size() + " buffered logs to disk");

		for (Entry<String, byte[]> bufferedArtifact : bufferedArtifacts.entrySet()) {
			writeBytesToDisk(bufferedArtifact.getKey(), bufferedArtifact.getValue());
		}
		bufferedArtifacts.clear();
	}
	

	@Override
	public void writeStackTrace(String stackTraceName, Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();
		if (loggingConfig.getLogDirectory() != null) {
			writeLog(stackTraceName, "txt", stackTrace.getBytes());
		} else {
			System.out.println("Attempt to write a Exception Stack Trace, but mark59 logging is not enabled: " + e.getMessage());
		}
	}
	
	
	/**
	 * Capture and immediately output a 'screenshot' log. Use with caution in a 
	 * Performance and Volume test as misuse of this method may produce many more screenshots
	 * than intended. 
	 * <p>Instead, you could use {@link #bufferScreenshot(String)} and {@link #writeBufferedArtifacts()}.
	 * <p>Can be implemented by extending this class and combining with a 
	 * {@link DriverFunctions} implementation that is capable of taking logs/screenshots    
	 * 
	 * @param imageName filename to use for the screenshot
	 */
	@Override	
	public void writeScreenshot(String imageName){
		System.out.println("writeScreenshot not implemented!" );
	} 

	
	/**
	 * Stores a 'screenshot' log in memory, ready to be written to file later.
	 * <p>Can be implemented by extending this class and combining with a 
	 * {@link DriverFunctions} implementation that is capable of taking logs/screenshots     
	 */
	@Override	
	public void bufferScreenshot(String imageName){
		System.out.println("bufferScreenshot not implemented!" );
	} 


	/**
	 * Save the byte[] to the specified file name, and will create the parent directory if missing 
	 * (ie initial directory creation)
	 * 
	 * <p>Generally meant to be used within Mark59 to write pre-defined log types 
	 * (eg Selenium screenshots, Chromium performance Logs, Exception stack traces), but can be invoked from 
	 * a user-written script to immediately write data to a Mark59 log. 
	 * 
	 * <p>Sample usage from a script:
	 * <p><code>jm.writeLog("kilroy", "txt", "Kilroy was here".getBytes());</code>
	 * 
	 * @param mark59LogName last part of the log filename (excluding extension)  
	 * @param mark59LogNameSuffix suffix of the log filename (eg 'txt', 'jpg')  
	 * @param mark59LogBytes  data to be written to log
	 */
	@Override	
	public void writeLog(String mark59LogName, String mark59LogNameSuffix, byte[] mark59LogBytes) {
		writeBytesToDisk((buildFullyQualifiedLogName(mark59LogName, mark59LogNameSuffix)), mark59LogBytes);
	}

	/**
	 * Save a byte[] with a specified log name and suffix, ready to be written to file later. 
	 * 
	 * <p>Generally meant to be used within Mark59 to buffer pre-defined log types 
	 * (eg Selenium screenshots, Chromium performance Logs), but can be invoked from 
	 * a user-written script.
	 * 
	 * <p>Sample usage from a script:
	 * <p><code>jm.bufferLog("kilroybuffer", "txt", "Kilroy was buffered here".getBytes());</code>
	 * 
	 * @see #writeBufferedArtifacts()
	 * 
	 * @param mark59LogName last part of the log filename (excluding extension)  
	 * @param mark59LogNameSuffix suffix of the log filename (eg 'txt', 'jpg')  
	 * @param mark59LogBytes the log data 
	 */
	@Override	
	public void bufferLog(String mark59LogName, String mark59LogNameSuffix, byte[] mark59LogBytes) {
		if (loggingConfig.getLogDirectory() != null) {
			bufferedArtifacts.put(buildFullyQualifiedLogName(mark59LogName,mark59LogNameSuffix), mark59LogBytes);
		}		
	}


	/**
	 * Puts everything together to form a full mark59 log name
	 * @param imageName  last part of logname
	 * @param suffix logname suffix (eg .txt)
	 * @return a string representing the full path of the log 
	 */
	private String buildFullyQualifiedLogName(String imageName, String suffix) {
		if (loggingConfig.getLogDirectory() == null) {
			return null;
		}

		String fullLogname = leadingPartOfLogNames;
		
		if (loggingConfig.getLogNamesFormat().contains(Mark59Constants.LABEL)) {
			if (StringUtils.isNotBlank(mostRecentTransactionStarted)){
				fullLogname +=  "_" + mostRecentTransactionStarted;
			} else {
				fullLogname += "_noTxn";
			}
		}
		
		// include Log Counter in log file name, and increment counter ready for next image
		fullLogname +=  "_" + String.format("%04d", StaticCounter.readCount(Mark59Constants.LOG_COUNTER));
		StaticCounter.incrementCount(Mark59Constants.LOG_COUNTER);			

		return fullLogname + "_" + imageName +"." + suffix;
	}

	
	private void writeBytesToDisk(String fullyQualifiedMark59LogName, byte[] mark59LogBytes) {
		if (loggingConfig.getLogDirectory() == null) {
			return;
		}		
		
		LOG.info(MessageFormat.format("Writing image to disk: {0}", fullyQualifiedMark59LogName));
		System.out.println("[" + Thread.currentThread().getName() + "]  Writing image to disk:" + fullyQualifiedMark59LogName);
		
		File fullyQualifiedMark59LogFile = new File(fullyQualifiedMark59LogName);
		
		//create the parent directory if missing (ie initial directory creation)
		new File(fullyQualifiedMark59LogFile.getParent()).mkdirs();
		
		if (mark59LogBytes == null ) {
			mark59LogBytes = "(null)".getBytes();
		}
		
		try (OutputStream stream = new FileOutputStream(fullyQualifiedMark59LogFile)){
			stream.write(mark59LogBytes);
			
		} catch (IOException e) {
			LOG.error("Caught " + e.getClass().getName() + " with message: " + e.getMessage());
		}
	}
	
	
	/**
	 * <p>Intended for internal testing purposes.
	 * 
	 * <p>Fetches the SampleResult from the transactionMap that matches the supplied label.
	 * If it fails to find a SampleResult it either means no such label had been added to the transactionMap, 
	 * or the SampleResult has already been finalized and added to the main result.</p>
	 * 
	 * @param label the transaction label for the SampleResult to locate.
	 * @return SampleResult belonging to the supplied label.
	 */
	public SampleResult getSampleResultWithLabel(String label) {
		//	System.out.println( ">> transactionMap");
		//	System.out.println( Mark59Utils.prettyPrintMap(transactionMap)  );
		//	System.out.println( "<< transactionMap");
		return transactionMap.get(label);
	}
	
	
	/**
	 * <p>Intended for internal testing purposes.
	 * 
	 *  <p>Searches the main result for all instances of the supplied label, collating the SampleResults into a List 
	 * and returning all of them.
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
		LOG.info(String.format("%-40s%-10s%-70s%-20s%-20s", "Thread", "#", "txn name", "Resp Message", "resp time"));

		for (int i = 0; i < sampleResult.length; i++) {
			SampleResult subSR = sampleResult[i];
			
			if (StringUtils.isBlank(subSR.getDataType())) {
				LOG.info(String.format("%-40s%-10s%-70s%-20s%-20s", threadName, i, subSR.getSampleLabel(),
						subSR.getResponseMessage(), subSR.getTime()));				
			} else {
				LOG.info(String.format("%-40s%-10s%-70s%-20s%-20s", threadName, i, subSR.getSampleLabel(),
						subSR.getResponseMessage() + " (" + subSR.getDataType() + ")" , subSR.getTime()));
			}
		}
		LOG.info("");
	}
	
	
	private void printThreadTransactionResults() {
		SampleResult[] sampleResult = mainResult.getSubResults();
		System.out.println("");
		System.out.println(Thread.currentThread().getName() + " result  (" + mainResult.getResponseMessage() + ")"   ) ; 
		System.out.println(String.format("%-40s%-10s%-70s%-20s%-20s", "Thread", "#", "txn name", "Resp Message", "resp time"));

		for (int i = 0; i < sampleResult.length; i++) {
			SampleResult subSR = sampleResult[i];
			
			if (StringUtils.isBlank(subSR.getDataType())) {
				System.out.println(String.format("%-40s%-10s%-70s%-20s%-20s", threadName, i, subSR.getSampleLabel(),
						subSR.getResponseMessage(), subSR.getTime()));				
			} else {
				System.out.println(String.format("%-40s%-10s%-70s%-20s%-20s", threadName, i, subSR.getSampleLabel(),
						subSR.getResponseMessage() + " (" + subSR.getDataType() + ")" , subSR.getTime()));
			}
		}
		System.out.println("");
	}
	
}
