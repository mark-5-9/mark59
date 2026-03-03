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
import java.util.ArrayList;
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
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.core.utils.Mark59LogLevels;
import com.mark59.core.utils.Mark59LoggingConfig;
import com.mark59.core.utils.PropertiesKeys;
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
 * At the end of the script the sub-results {@link #tearDown()} are printed (at LOG info level).
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
	 * 'additionalTestParameters' method of a Mark59 UI script:
	 *
	 * <p><code>jmeterAdditionalParameters.put(JmeterFunctionsImpl.PRINT_RESULTS_SUMMARY, String.valueOf(false));</code>
	 *
	 * <p>When running from the IDE you may want change from the default to print the Results Summary. This is done
	 * for Mark59 sample UI scripts (and could be done for custom implementations using this field).
	 *
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
	protected Mark59LoggingConfig loggingConfig;

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
	 * The JMeter context for this script execution
	 */
	protected JavaSamplerContext context;


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
	 * in mark59.properties.  For example, metrics capture via web Java Request does not rely on mark59.properties at all.
	 *
	 * <p>For such implementations, this constructor should be used with 'isMark59PropertyConfigurationRequired' set as false.
	 * This will stop INFO/WARNIMG messages about mark59.properties not being set in a JMeter test.
	 *
	 * @param context the JMeter JavaSamplerContext
	 * @param isMark59PropertyConfigurationRequired determines if mark59 properties will be accessed using this implementation
	 */
	public JmeterFunctionsImpl(JavaSamplerContext context, boolean isMark59PropertyConfigurationRequired) {
		this.context = context; // Store the context for later use
		threadName =Thread.currentThread().getName();

		if (isMark59PropertyConfigurationRequired){
			try {
				loggingConfig = Mark59LoggingConfig.getInstance();
				leadingPartOfLogNames = formLeadingPartOfLogNames(loggingConfig.getLogNamesFormat(), context);
			} catch (Exception e) {
				// Handle test scenarios where Mark59 configuration might not be properly initialized
				LOG.warn("Mark59 logging configuration not available, using fallback settings: " + e.getMessage());
				loggingConfig = null;
				leadingPartOfLogNames = null;
			}
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
	 * The leading components a for log name of a given script are constant, so can be set here
	 *
	 * @param logNamesFormat  from the Mark59 property "mark59.logname.format"
	 * @param context the JMeter JavaSamplerContext
	 */
	private String formLeadingPartOfLogNames(String logNamesFormat, JavaSamplerContext context) {
		// Validate input parameters
		if (logNamesFormat == null) {
			LOG.debug("formLeadingPartOfLogNames: Log names format is null, setting to " + Mark59Constants.THREAD_NAME);
			logNamesFormat = Mark59Constants.THREAD_NAME;
		}

		String leadingPartOfLogNames = null;

		if (loggingConfig != null && loggingConfig.getLogDirectory() != null) {
			leadingPartOfLogNames = loggingConfig.getLogDirectory().getPath() + File.separator;

			if (logNamesFormat.contains(Mark59Constants.THREAD_NAME)){
				String sanitizedThreadName = sanitizeForFilename(threadName, "defaultThread");
				leadingPartOfLogNames += sanitizedThreadName + "_";
			}
			if (logNamesFormat.contains(Mark59Constants.THREAD_GROUP)){
				if (context != null && context.getJMeterContext() != null && context.getJMeterContext().getThreadGroup() != null){
					String threadGroupName = context.getJMeterContext().getThreadGroup().getName();
					String sanitizedThreadGroupName = sanitizeForFilename(threadGroupName, "noTG");
					leadingPartOfLogNames += sanitizedThreadGroupName + "_" ;
				} else {
					leadingPartOfLogNames += "noTG_";
				}
			}
			if (logNamesFormat.contains(Mark59Constants.SAMPLER)){
				if (context != null && context.getJMeterContext() != null && context.getJMeterContext().getCurrentSampler() != null){
					String samplerName = context.getJMeterContext().getCurrentSampler().getName();
					String sanitizedSamplerName = sanitizeForFilename(samplerName, "noSampler");
					leadingPartOfLogNames += sanitizedSamplerName + "_" ;
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
	 * @return the JMeter subresult for this transaction - which includes the transaction time (getTime)
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
	 * @param transactionLabel label for the transaction
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
	 * @param success the success (true) or failure (false) state of the transaction
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
	 * 
	 * @param dataPointName label for the data point 
	 * @param dataPointValue the value of the data point
	 * @param jMeterFileDatatypes the data type for the JMeter results file
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
	 * @param transactionSelection  Predicate function to select SampleResult(s) transaction names
	 * ('labels' in JMeter terminology) to be deleted
	 *
	 * @see JmeterFunctionsImpl#deleteTransaction(String)
	 * @see JmeterFunctionsImpl#deleteTransactionsPrefixedBy(String)
	 */
	public synchronized void deleteTransactions(Predicate<SampleResult> transactionSelection){
		SampleResult[] originalResults = mainResult.getSubResults();

		// Build the filtered results list atomically before modifying main result
		List<SampleResult> filteredResults = new ArrayList<>();
		for (SampleResult sampleResult : originalResults) {
			if (!transactionSelection.test(sampleResult)) {
				filteredResults.add(sampleResult);
			}
		}

		// Perform atomic replacement: remove all then add filtered results
		mainResult.removeSubResults();
		for (SampleResult sampleResult : filteredResults) {
			mainResult.addSubResult(sampleResult, false);
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
	 * <p>This method implements comprehensive error handling to ensure that failures in individual
	 * teardown operations don't prevent the overall teardown process from completing. Critical
	 * operations like finalizing the main result are prioritized over optional operations like logging.</p>
	 *
	 * <p>
	 * this.tearDown() is called as part of the framework, so an end user should not need to call this method
	 * themselves unless they're using a custom implementation of AbstractJmeterTestRunner.runTest(JavaSamplerContext)
	 * </p>
	 */
	@Override
	public void tearDown() {
		boolean tearDownSuccessful = true;
		StringBuilder errorMessages = new StringBuilder();

		// Step 1: Handle in-flight transactions (critical - must complete)
		try {
			failInFlightTransactions();
		} catch (Exception e) {
			tearDownSuccessful = false;
			errorMessages.append("Failed to handle in-flight transactions: ").append(e.getMessage()).append("; ");
			LOG.error("Error during failInFlightTransactions in tearDown", e);
		}

		// Step 2: Determine and set main result outcome (critical - must complete)
		try {
			if (allSamplesPassed() && !isForcedFail) {
				tearDownMainResult(Outcome.PASS);
			} else {
				tearDownMainResult(Outcome.FAIL);
			}
		} catch (Exception e) {
			tearDownSuccessful = false;
			errorMessages.append("Failed to finalize main result: ").append(e.getMessage()).append("; ");
			LOG.error("Error during tearDownMainResult in tearDown", e);

			// Try emergency fallback to mark test as failed
			try {
				tearDownMainResult(Outcome.FAIL);
			} catch (Exception fallbackException) {
				LOG.error("Emergency fallback for main result also failed", fallbackException);
			}
		}

		// Step 3: Optional logging operations (non-critical - failures won't stop teardown)
		if (isLogResultsSummary) {
			try {
				logThreadTransactionResults();
			} catch (Exception e) {
				tearDownSuccessful = false;
				errorMessages.append("Failed to log transaction results: ").append(e.getMessage()).append("; ");
				LOG.warn("Error during logThreadTransactionResults in tearDown", e);
			}
		}

		if (isPrintResultsSummary) {
			try {
				printThreadTransactionResults();
			} catch (Exception e) {
				tearDownSuccessful = false;
				errorMessages.append("Failed to print transaction results: ").append(e.getMessage()).append("; ");
				LOG.warn("Error during printThreadTransactionResults in tearDown", e);
			}
		}

		// Step 4: Log overall teardown status
		if (!tearDownSuccessful) {
			String fullErrorMessage = "TearDown completed with errors: " + errorMessages.toString();
			LOG.warn(fullErrorMessage);
			System.err.println("[" + Thread.currentThread().getName() + "] " + fullErrorMessage);
		} else {
			LOG.debug("TearDown completed successfully for thread: " + Thread.currentThread().getName());
		}
	}
	/**
	 * <p>Traverses the internal created transactions Map, looking for any transactions that had been
	 * started but not completed.</p>
	 *
	 * @return List of In-Flight transaction names
	 */
	public List<String> returnInFlightTransactionNames() {

		List<String> inFlightTransactionsNames = new ArrayList<String>();
		for (Entry<String, SampleResult> subResultEntry : transactionMap.entrySet()) {
			if (StringUtils.isBlank(subResultEntry.getValue().getResponseMessage())){
				inFlightTransactionsNames.add(subResultEntry.getValue().getSampleLabel());
			}
		}
		return inFlightTransactionsNames;
	}



	/**
	 * <p>Traverses the internal created transactions Map, looking for any transactions that had been
	 * started but not completed. If incomplete transactions are encountered then they are ended and flagged as
	 * "failed".</p>
	 */
	public void failInFlightTransactions() {

		for (Entry<String, SampleResult> subResultEntry : transactionMap.entrySet()) {
			if (StringUtils.isBlank(subResultEntry.getValue().getResponseMessage())){
				endTransaction(subResultEntry.getValue().getSampleLabel(), Outcome.FAIL);
			}
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
	 * A data type of 'PARENT' is used to indicate this is a main result (normally expected to have
	 * sub-results)produced using the mark59 framework is set.  This is useful to to separate results
	 * from sub-results, particularly for JMeter result files in CSV format, as a CSV has a flat structure.
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
	 * This will immediately stop the JMeter ThreadGroup the script is running on (for the rest of the test).
	 * Context/TG may not be set if running outside JMeter (ie in an IDE), so check is made for null objects
	 * before call.
	 */
	@Override
	public void stopThreadGroup(JavaSamplerContext context) {
		if (context != null && context.getJMeterContext() != null
				&& context.getJMeterContext().getThreadGroup() != null) {
			AbstractThreadGroup tg = context.getJMeterContext().getThreadGroup();
			LOG.debug("Actioning request to stop TG " + tg.getThreadName());
			tg.stop();
		}
	}

	/**
	 * Called to set the main result of a test to a failed state, regardless of the state of the
	 * sub results attached to the main result.
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

		List<String> failedWrites = new ArrayList<>();

		for (Entry<String, byte[]> bufferedArtifact : bufferedArtifacts.entrySet()) {
			try {
				writeBytesToDisk(bufferedArtifact.getKey(), bufferedArtifact.getValue());
			} catch (Exception e) {
				String filename = bufferedArtifact.getKey();
				failedWrites.add(filename);
				LOG.error("Failed to write buffered artifact '" + filename + "': " + e.getMessage());
				// Continue with other files rather than failing completely
			}
		}

		bufferedArtifacts.clear();

		if (!failedWrites.isEmpty()) {
			String errorMsg = "Failed to write " + failedWrites.size() + " buffered artifacts: " + failedWrites;
			LOG.warn(errorMsg);
			// Don't throw exception here as this is called during cleanup - just log the failures
		}
	}


	@Override
	public void writeStackTrace(String stackTraceName, Throwable e) {
		if (e == null) {
			LOG.warn("Cannot write stack trace - exception is null: " + stackTraceName);
			System.out.println("Attempt to write a null Exception Stack Trace for: " + stackTraceName);
			return;
		}

		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();

		if (loggingConfig != null && loggingConfig.getLogDirectory() != null) {
			writeLog(stackTraceName, "txt", stackTrace.getBytes());
		} else {
			System.out.println("Attempt to write a Exception Stack Trace, but mark59 logging is not enabled: " + e.getMessage());
		}
	}


	/**
	 * Save the byte[] to the specified file name.  The precise formatting of the file name and the directory
	 * are obtained from properties that can be set in the mark59.properties file.
	 *
	 * <p>Note the parent directory is created if missing (ie initial log file in the directory).
	 *
	 * <p>Generally meant to be used within Mark59 to write pre-defined log types
	 * (eg UI screenshots, Chromium performance Logs, Exception stack traces), but can be invoked from
	 * a user-written script to immediately write data to a Mark59 log.
	 *
	 * <p>Sample usage from a script:
	 * <p><code>jm.writeLog("kilroy", "txt", "Kilroy was here".getBytes());</code>
	 *
	 * @see PropertiesKeys

	 * @param mark59LogName last part of the log filename (excluding extension)
	 * @param mark59LogNameSuffix suffix of the log filename (eg 'txt', 'jpg')
	 * @param mark59LogBytes  data to be written to log
	 * @throws RuntimeException if the log file cannot be written
	 */
	@Override
	public void writeLog(String mark59LogName, String mark59LogNameSuffix, byte[] mark59LogBytes) {
		try {
			String fullyQualifiedLogName = buildFullyQualifiedLogName(mark59LogName, mark59LogNameSuffix);
			writeBytesToDisk(fullyQualifiedLogName, mark59LogBytes);
		} catch (Exception e) {
			String errorMsg = "Failed to write log '" + mark59LogName + "." + mark59LogNameSuffix + "': " + e.getMessage();
			LOG.error(errorMsg);

			// For user-facing writeLog operations, we should fail the test to ensure critical logs aren't lost
			failTest();
			throw new RuntimeException(errorMsg, e);
		}
	}


	/**
	 * Save the byte[] to the fully specified file name
	 *
	 * <p>This method is <b>not expected to use used for logging except in special circumstances</b> when
	 * full control of the filename to be used is required.
	 * {@link #writeLog(String, String, byte[])} is the standard for creation of log files.
	 *
	 * <p>The most likely use case is when a log file name needs to be reserved, for use later or during a
	 * script.  In this case the usual location and formatting for a mark59 log file can be obtained
	 * using {@link #reserveFullyQualifiedLogName(String, String)}.
	 *
	 * @param fullyQualifiedMark59LogName log filename to be written (including extension)
	 * @param mark59LogBytes  data to be written to log
	 * @throws RuntimeException if the log file cannot be written
	 */
	@Override
	public void writeLog(String fullyQualifiedMark59LogName, byte[] mark59LogBytes) {
		try {
			writeBytesToDisk(fullyQualifiedMark59LogName, mark59LogBytes);
		} catch (Exception e) {
			String errorMsg = "Failed to write log '" + fullyQualifiedMark59LogName + "': " + e.getMessage();
			LOG.error(errorMsg);

			// For user-facing writeLog operations, we should fail the test to ensure critical logs aren't lost
			failTest();
			throw new RuntimeException(errorMsg, e);
		}
	}


	/**
	 * Save a byte[] with a specified log name and suffix, ready to be written to file later.
	 * The precise formatting of the file name and directory are obtained from properties
	 * that can be set in the mark59.properties file.
	 *
	 * <p>Generally meant to be used within Mark59 to buffer pre-defined log types
	 * (eg Selenium screenshots, Chromium performance Logs), but can be invoked from
	 * a user-written script.
	 *
	 * <p>Sample usage from a script:
	 * <p><code>jm.bufferLog("kilroybuffer", "text", "Kilroy was buffered here".getBytes());</code>
	 *
	 * @see #writeBufferedArtifacts()
	 * @see PropertiesKeys
	 *
	 * @param mark59LogName last part of the log filename (excluding extension)
	 * @param mark59LogNameSuffix suffix of the log filename (eg 'txt', 'jpg')
	 * @param mark59LogBytes the log data
	 */
	@Override
	public void bufferLog(String mark59LogName, String mark59LogNameSuffix, byte[] mark59LogBytes) {
		if (loggingConfig != null && loggingConfig.getLogDirectory() != null) {
			bufferedArtifacts.put(buildFullyQualifiedLogName(mark59LogName,mark59LogNameSuffix), mark59LogBytes);
		}
	}


	/**
	 * Puts everything together to form a full mark59 log name.  Calling this method directly will result
	 * in a log filename being 'reserved', so the file can be created later during script execution.
	 * <p>An example of this is .HAR files creation (filename needs to be set during Playwright page creation, but is not
	 * created until the BrowserContext.close() is invoked at the end of the script).
	 * <p>Note: programmatically it's just calling the private method {@link #buildFullyQualifiedLogName(String, String)}.
	 * this public method has been created more to help show the intent of the call (to get and reserve a
	 * log filename for later use).
	 *
	 * @param imageName  last part of logname
	 * @param suffix logname suffix (eg .txt)
	 * @return a string representing the full path of the log
	 */
	@Override
	public String reserveFullyQualifiedLogName(String imageName, String suffix) {
		return buildFullyQualifiedLogName(imageName, suffix);
	}


	/**
	 * Puts everything together to form a full mark59 log name.
	 *
	 * @param imageName  last part of logname
	 * @param suffix logname suffix (eg .txt)
	 * @param context the JMeter context for accessing thread group and sampler information
	 * @return a string representing the full path of the log
	 */
	private String buildFullyQualifiedLogName(String imageName, String suffix) {
		// Validate and sanitize inputs
		String sanitizedImageName = sanitizeForFilename(imageName, "defaultImage");
		String sanitizedSuffix = sanitizeForFilenameSuffix(suffix, "txt");

		// Where a  logging config is available, do not set a name
		if (loggingConfig == null || loggingConfig.getLogDirectoryPathName() == null) {
			return null;
		}

		String fullLogname = leadingPartOfLogNames;

		if (loggingConfig.getLogNamesFormat().contains(Mark59Constants.LABEL)) {
			if (StringUtils.isNotBlank(mostRecentTransactionStarted)){
				String sanitizedLabel = sanitizeForFilename(mostRecentTransactionStarted, "noTxn");
				fullLogname +=  "_" + sanitizedLabel;
			} else {
				fullLogname += "_noTxn";
			}
		}
		// include Log Counter in log file name, and atomically increment counter ready for next image
		fullLogname +=  "_" + String.format("%04d", StaticCounter.getNext(Mark59Constants.LOG_COUNTER));
		return fullLogname + "_" + sanitizedImageName +"." + sanitizedSuffix;
	}

	/**
	 * Sanitizes a string for safe use in filenames by removing or replacing invalid characters.
	 *
	 * @param input The input string to sanitize
	 * @param defaultValue Default value to use if input is null, empty, or becomes empty after sanitization
	 * @return A sanitized string safe for use in filenames
	 */
	private String sanitizeForFilename(String input, String defaultValue) {
		if (input == null || input.trim().isEmpty()) {
			return defaultValue;
		}

		// Remove or replace invalid filename characters and potentially dangerous patterns
		// Invalid characters: < > : " | ? * and control characters (0-31), DEL (127)
		// Also replace characters that could be used in injection attacks: ; ' ` -
		// Replace spaces to prevent command line injection and improve filename readability
		String sanitized = input.replaceAll("[<>:\"|?*;'`\\-\\s\\p{Cntrl}]", "_");

		// Replace path separators and dots with underscores to prevent directory traversal
		sanitized = sanitized.replace("/", "_").replace("\\", "_").replace(".", "_");

		// Remove leading/trailing spaces but keep underscores (they might represent sanitized malicious patterns)
		sanitized = sanitized.trim();

		// Limit length to prevent filesystem issues (255 is common limit, use 200 to be safe)
		if (sanitized.length() > 200) {
			sanitized = sanitized.substring(0, 200);
		}

		// If sanitization resulted in empty string, use default
		if (sanitized.isEmpty()) {
			return defaultValue;
		}

		return sanitized;
	}

	/**
	 * Sanitizes a file extension/suffix for safe use in filenames.
	 *
	 * @param suffix The file extension to sanitize
	 * @param defaultSuffix Default extension to use if input is invalid
	 * @return A sanitized file extension
	 */
	private String sanitizeForFilenameSuffix(String suffix, String defaultSuffix) {
		if (suffix == null || suffix.trim().isEmpty()) {
			return defaultSuffix;
		}

		// Remove any path separators and invalid characters from suffix
		String sanitized = suffix.replaceAll("[<>:\"|?*/\\\\\\p{Cntrl}]", "");

		// Remove leading/trailing spaces and dots
		sanitized = sanitized.trim().replaceAll("^[.\\s]+|[.\\s]+$", "");

		// Limit suffix length (typically 3-4 characters)
		if (sanitized.length() > 10) {
			sanitized = sanitized.substring(0, 10);
		}

		// If sanitization resulted in empty string, use default
		if (sanitized.isEmpty()) {
			return defaultSuffix;
		}

		return sanitized;
	}


	private void writeBytesToDisk(String fullyQualifiedMark59LogName, byte[] mark59LogBytes) {
		// Validate inputs first, regardless of logging configuration
		if (fullyQualifiedMark59LogName == null || fullyQualifiedMark59LogName.trim().isEmpty()) {
			String errorMsg = "Cannot write log file - filename is null or empty";
			LOG.error(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}

		if (loggingConfig == null || loggingConfig.getLogDirectory() == null) {
			String errorMsg = "Cannot write log file - logging not configured or log directory is null: " + fullyQualifiedMark59LogName;
			LOG.warn(errorMsg);
			throw new RuntimeException(errorMsg);
		}

		LOG.info(MessageFormat.format("Writing log to disk: {0}", fullyQualifiedMark59LogName));
		System.out.println("[" + Thread.currentThread().getName() + "]  Writing log to disk: " + fullyQualifiedMark59LogName);

		File fullyQualifiedMark59LogFile;
		try {
			fullyQualifiedMark59LogFile = new File(fullyQualifiedMark59LogName);
		} catch (Exception e) {
			String errorMsg = "Invalid log file path '" + fullyQualifiedMark59LogName + "': " + e.getMessage();
			LOG.error(errorMsg);
			throw new IllegalArgumentException(errorMsg, e);
		}

		// Create parent directory with error handling
		File parentDir = fullyQualifiedMark59LogFile.getParentFile();
		if (parentDir != null && !parentDir.exists()) {
			if (!parentDir.mkdirs()) {
				String errorMsg = "Failed to create log directory: " + parentDir.getAbsolutePath();
				LOG.error(errorMsg);
				throw new RuntimeException(errorMsg);
			}
		}

		if (mark59LogBytes == null) {
			mark59LogBytes = "(null)".getBytes();
		}

		// Write file with comprehensive error handling
		try (OutputStream stream = new FileOutputStream(fullyQualifiedMark59LogFile)) {
			stream.write(mark59LogBytes);
			stream.flush(); // Ensure data is written to disk
			LOG.debug("Successfully wrote " + mark59LogBytes.length + " bytes to: " + fullyQualifiedMark59LogName);
		} catch (SecurityException e) {
			String errorMsg = "Security error writing log file '" + fullyQualifiedMark59LogName + "': " + e.getMessage();
			LOG.error(errorMsg);
			throw new RuntimeException(errorMsg, e);
		} catch (IOException e) {
			String errorMsg = "I/O error writing log file '" + fullyQualifiedMark59LogName + "': " + e.getMessage();
			LOG.error(errorMsg);

			// Check for specific I/O issues
			if (e.getMessage() != null) {
				String msg = e.getMessage().toLowerCase();
				if (msg.contains("no space left") || msg.contains("disk full")) {
					errorMsg = "Disk full - cannot write log file: " + fullyQualifiedMark59LogName;
				} else if (msg.contains("permission denied") || msg.contains("access denied")) {
					errorMsg = "Permission denied writing log file: " + fullyQualifiedMark59LogName;
				} else if (msg.contains("file name too long")) {
					errorMsg = "File name too long: " + fullyQualifiedMark59LogName;
				}
			}

			// For critical I/O errors, we should fail the test to prevent silent data loss
			throw new RuntimeException(errorMsg, e);
		} catch (Exception e) {
			String errorMsg = "Unexpected error writing log file '" + fullyQualifiedMark59LogName + "': " + e.getMessage();
			LOG.error(errorMsg);
			throw new RuntimeException(errorMsg, e);
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
