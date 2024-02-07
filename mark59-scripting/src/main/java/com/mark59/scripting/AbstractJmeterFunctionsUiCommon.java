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

import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.Outcome;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.scripting.interfaces.JmeterFunctionsUi;
import com.mark59.scripting.selenium.SeleniumAbstractJavaSamplerClient;
import com.mark59.core.utils.Mark59LogLevels;

/**
 * UI flavored extension of the Mark59 class {@link JmeterFunctionsImpl} ( whose primary purpose to to handle transaction results, 
 * implemented in Mark59 by the use of 'sub-results' within a {@link SampleResult} )    
 * 
 * <p>This class is designed to additionally handle Common UI (Selenium and Playwright) related functions within Mark59, 
 * in particular logging.  
 * 
 * <p>At instantiation, transaction level logging usage is set, based on the log4j level.  This can be over-ridden via JMeter parameters 
 * and/or directly calling the methods in this class from the script.  
 * Please refer to {@link #overrideTxnLoggingBehaviourUsingJmeterParameters(Map)}</p>
 * 
 * <p>Mark59 UI scripts can been provisioned to have transaction-level 'logging settings' available.<br>
 * 
 * <p>Current default outputs setting are :
 *  
 * <p> Trace Level:
 *  <ul>
 *  <li> 	write Screenshots At Start and End OfTransactions
 *  <li> 	write Page Source At Start and End OfTransactions	    
 *	<li>	write Performance Log At End Of Transactions **	
 *  </ul>
 *  <p> Debug Level:
 *  <ul>
 *  <li>	write Screenshots End Of Transactions
 *  <li> 	write Page Source End Of Transactions	    
 *	<li>	write PerformanceLog At End Of Transactions	
 * </ul>
 * 
 * <p>No screenshot/log transaction level at Info or above, except for exception handling, which occurs regardless of log4j level 
 * 
 * <p>** "Performance Log" function is only active in Selenium scripts
 * 
 * <p>For debugging a troublesome script during execution, note that 'buffered' logs can be set in script 
 *   (For example see {@link #logAllLogsAtEndOfTransactions(Mark59LogLevels)} )
 * 
 * <p>Finer grained control within a script can be achieved using methods to setting the individual logging flags.
 * 
 * <p>An example of transaction-level logging:  
 * <pre><code>
 * if (LOG.isInfoEnabled()) jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);
 * jm.startTransaction("Some-transaction-whose-screenshot-i-want-to-see-at-the-start-when-at-log4j-INFO-level");
 *       :
 * //OK, now just go back to the default behavior for the current Log4j level..  	 
 * if (LOG.isInfoEnabled()) jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.DEFAULT);
 * </code></pre>
 * 
 * @see Mark59LogLevels
 * @see JmeterFunctionsImpl
 * @see SeleniumAbstractJavaSamplerClient#scriptExceptionHandling(JavaSamplerContext, Map, Throwable)
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public abstract class AbstractJmeterFunctionsUiCommon extends JmeterFunctionsImpl implements JmeterFunctionsUi {
	
	/** log4J class logger */
	public static final Logger LOG = LogManager.getLogger(AbstractJmeterFunctionsUiCommon.class);

	/**	@see #logScreenshotsAtStartOfTransactions  */
	public static final String LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS = "Log_Screenshots_At_Start_Of_Transactions";
	/**	@see #logScreenshotsAtEndOfTransactions */
	public static final String LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS 	= "Log_Screenshots_At_End_Of_Transactions";
	/**	@see #logPageSourceAtStartOfTransactions   */
	public static final String LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS = "Log_Page_Source_At_Start_Of_Transactions";
	/**	@see #logPageSourceAtEndOfTransactions  */
	public static final String LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS	= "Log_Page_Source_At_End_Of_Transactions";
	/**	@see #logPerformanceLogAtEndOfTransactions  (Selenium only) */
	public static final String LOG_PERF_LOG_AT_END_OF_TRANSACTIONS 		= "Log_Perf_Log_At_End_Of_Transactions";


	private boolean writeScreenshotsAtStartOfTransactions = false;
	private boolean writeScreenshotsAtEndOfTransactions = false; 	
	private boolean bufferScreenshotsAtStartOfTransactions = false;
	private boolean bufferScreenshotsAtEndOfTransactions = false; 	
	private boolean writePageSourceAtStartOfTransactions = false;
	private boolean writePageSourceAtEndOfTransactions = false; 	
	private boolean bufferPageSourceAtStartOfTransactions = false;
	private boolean bufferPageSourceAtEndOfTransactions = false; 	
	private boolean writePerformanceLogAtEndOfTransactions = false; 		
	private boolean bufferPerformanceLogAtEndOfTransactions = false; 			

	/**
	 * @param context the JMeter JavaSamplerContext 
	 * @param jmeterRuntimeArgumentsMap used to override default state of Mark59 log output
	 */
	public AbstractJmeterFunctionsUiCommon(JavaSamplerContext context, Map<String, String> jmeterRuntimeArgumentsMap) {		
		super(context);
		setDefaultTxnLoggingBehaviourBasedOnLog4j();
		overrideTxnLoggingBehaviourUsingJmeterParameters(jmeterRuntimeArgumentsMap);
	}


	/**
	 *  Sets the default configuration of Mark59 logging at transaction level, based of the log4j log level. 
	 *  Please see {@link #overrideTxnLoggingBehaviourUsingJmeterParameters(Map)} for detailed settings. 
	 */
	private void setDefaultTxnLoggingBehaviourBasedOnLog4j() {
		logScreenshotsAtStartOfTransactions(Mark59LogLevels.DEFAULT);
		logScreenshotsAtEndOfTransactions(Mark59LogLevels.DEFAULT);
		logPageSourceAtStartOfTransactions(Mark59LogLevels.DEFAULT);
		logPageSourceAtEndOfTransactions(Mark59LogLevels.DEFAULT);
		logPerformanceLogAtEndOfTransactions(Mark59LogLevels.DEFAULT);
	}

	
	/**
	 *  Allows overriding of the default state of Mark59 logging using the JMeter Java Request Parameters panel for a script,
	 *  by setting values in the additionalTestParameters() method of a script (which can themselves be over-ridden for finer
	 *  grain control using jm.log..... methods).
	 *   
	 *  <p>The table below lists the names of the parameters.  The 'Value' needs to be one of the string values of the 
	 *  {@link  Mark59LogLevels} enumeration.  For example, to :
	 *  <ul>
	 *  <li>buffer all Screenshot JPGs at transaction start and end,</li>
	 *  <li>always write Html Page Source at transaction start,</li>
	 *  <li>never write Html Page Source at transaction end</li>
	 *  <li>leave the Performance Log using defaults (there's no effect adding this entry)</li>
	 *  </ul>
	 *  the entries in JMeter Java Request Parameters panel for a script would be:   
	 *  <p>
	 * <table>
	 * 	<tr><td>Name</td>									  <td>|</td><td>Value  </td><td></td></tr>
	 *	<tr><td>_________________________________________</td><td>|</td><td>________________________</td><td></td></tr>
  	 *	<tr><td>Log_Screenshots_At_Start_Of_Transactions</td> <td>|</td><td>buffer </td><td></td></tr>
  	 *	<tr><td>Log_Screenshots_At_End_Of_Transactions 	</td> <td>|</td><td>buffer </td><td></td></tr>
  	 *	<tr><td>Log_Page_Source_At_Start_Of_Transactions</td> <td>|</td><td>write  </td><td></td></tr>
  	 *	<tr><td>Log_Page_Source_At_End_Of_Transactions	</td> <td>|</td><td>off	   </td><td></td></tr>
  	 *	<tr><td>Log_Perf_Log_At_End_Of_Transactions **	</td> <td>|</td><td>default</td><td></td></tr>
  	 * </table>
  	 * 
  	 * <p>Note that you can override these (and default) settings for script transactional level logging at the start,
  	 *  or any other point, of the run{Selenium|Playwright}Test method in the script as well.  For example: <br><br>
  	 * <code>
  	 *		&emsp;protected void runSeleniumTest(JavaSamplerContext context, ....){ 
  	 *		<br><br>
  	 *		&emsp;&emsp;jm.logScreenshotsAtStartOfTransactions(Mark59LogLevels.WRITE);<br>
  	 *		&emsp;&emsp;jm.logScreenshotsAtEndOfTransactions(Mark59LogLevels.WRITE);<br>
	 *		&emsp;&emsp;jm.logPageSourceAtStartOfTransactions(Mark59LogLevels.WRITE);<br>	
	 *		&emsp;&emsp;jm.logPageSourceAtEndOfTransactions(Mark59LogLevels.WRITE );<br>
	 *		&emsp;&emsp;jm.logPerformanceLogAtEndOfTransactions(Mark59LogLevels.WRITE); *<br>
	 *		&emsp;&emsp;// you need to use jm.writeBufferedArtifacts to output BUFFERed data<br>		
	 *		&emsp;&emsp;jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER);<br>
  	 * </code>
 	 *
 	 * <p>  ** "Performance Log" function is only active in Selenium scripts
 	 *
 	 * @see #logScreenshotsAtStartOfTransactions
 	 * @see #logScreenshotsAtEndOfTransactions
 	 * @see #logPageSourceAtStartOfTransactions
 	 * @see #logPageSourceAtEndOfTransactions
 	 * @see #logPerformanceLogAtEndOfTransactions
 	 * @see #logAllLogsAtStartOfTransactions
 	 * @see #logAllLogsAtEndOfTransactions
 	 * @see #writeBufferedArtifacts
	 * @see Mark59LogLevels 
	 */
	private void overrideTxnLoggingBehaviourUsingJmeterParameters(Map<String,String> jmeterRuntimeArgumentsMap) {
		Mark59LogLevels logging;
		
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(LOG_SCREENSHOTS_AT_START_OF_TRANSACTIONS));
		if (logging != null) {
			logScreenshotsAtStartOfTransactions(logging);
		}
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(LOG_SCREENSHOTS_AT_END_OF_TRANSACTIONS));
		if (logging != null) { 
			logScreenshotsAtEndOfTransactions(logging);
		}
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(LOG_PAGE_SOURCE_AT_START_OF_TRANSACTIONS));
		if (logging != null) { 
			logPageSourceAtStartOfTransactions(logging);
		}
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(LOG_PAGE_SOURCE_AT_END_OF_TRANSACTIONS));
		if (logging != null) {
			logPageSourceAtEndOfTransactions(logging);
		}
		logging = Mark59LogLevels.fromString(jmeterRuntimeArgumentsMap.get(LOG_PERF_LOG_AT_END_OF_TRANSACTIONS));
		if (logging != null) {
			logPerformanceLogAtEndOfTransactions(logging);
		}
	}

	
	@Override
	public void startTransaction(String transactionLabel) {
		startTransaction(transactionLabel, JMeterFileDatatypes.TRANSACTION, true);	
	}
	
	
	/**
	 * As per {@link #startTransaction(String)}, but will tag the transaction as a DevTools (CDP) transaction.
	 * <p>You can end the transaction with any of the <code>endTransaction</code> methods or {@link #endCdpTransaction(String)}
	 * (which is functionally equivalent to {@link #endTransaction(String)}).  This is because the transaction is marked 
	 * as a DevTools (CDP) transaction when it is started by this method.   
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @throws IllegalArgumentException if the transaction name supplied is an illegal value (null or empty) or already in use.
	 */
	@Override	
	public void startCdpTransaction(String transactionLabel) {
		startTransaction(transactionLabel, JMeterFileDatatypes.CDP, true);
	}
	
	
	/**
	 * As per  {@link #startTransaction(String)}, but with the additional options specifying what type of transaction this is, and switching
	 * off all screenshot writing or buffering for the start of this transaction, regardless of the current {@link Mark59LogLevels} settings  
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param jMeterFileDatatypes a {@link JMeterFileDatatypes} (text value will be written in the data type field of the JMeter results file)
	 * @param includeInStartOfTransactionLogs boolean option to switch on/off logs for transaction starts (an override for this txn) 
	 */
	@Override	
	public void startTransaction(String transactionLabel, JMeterFileDatatypes jMeterFileDatatypes, boolean includeInStartOfTransactionLogs){
		if (includeInStartOfTransactionLogs) {
			if 	(bufferScreenshotsAtStartOfTransactions) {
				bufferScreenshot(transactionLabel + "_before" );
			}
			if 	(writeScreenshotsAtStartOfTransactions) {
				writeScreenshot(transactionLabel + "_before" );
			}
			if 	(bufferPageSourceAtStartOfTransactions) {
				bufferPageSource(transactionLabel + "_source_before" );
			}
			if 	(writePageSourceAtStartOfTransactions) {
				writePageSource(transactionLabel + "_source_before" );
			}
		}
		super.startTransaction(transactionLabel, jMeterFileDatatypes);
	}
	
	
	@Override
	public SampleResult endTransaction(String transactionLabel) {
		return endTransaction(transactionLabel, Outcome.PASS, null, true);
	}

	
	/**
	 * Identical to {@link #endTransaction(String)}.  Included as convenience to explicitly mark the end of DevTools (CDP) 
	 * transactions if you wish to do so. 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @throws IllegalArgumentException if the transactionLabel supplied is an illegal value (null or empty)
	 * @throws NoSuchElementException   if the transactionLabel doesn't exist in the  transactionMap
	 * @return the JMeter subresult for this transaction - which includes the transaction time (getTime) 
	 */
	@Override	
	public SampleResult endCdpTransaction(String transactionLabel) {
		return endTransaction(transactionLabel, Outcome.PASS, null);
	}
	
	
	/**
	 * As per {@link #endTransaction(String)}, but also allows for overriding current logging config and forcing logging off for 
	 * this transaction (set includeInEndOfTransactionshots to false). 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param includeInEndOfTransactionLogs boolean option to switch on/off logs for transaction ends (an override for this txn) 
	 * @return the JMeter sub-result for this transaction (which includes the transaction time)
	 */
	@Override	
	public SampleResult endTransaction(String transactionLabel, boolean includeInEndOfTransactionLogs) {
		return endTransaction(transactionLabel, Outcome.PASS, null, includeInEndOfTransactionLogs); 
	}
	
	
	@Override
	public SampleResult endTransaction(String transactionLabel, Outcome result) {
		return endTransaction(transactionLabel, result, null, true); 
	}
	
	
	/**
	 * As per {@link #endTransaction(String, Outcome)}, but also allows for overriding current logging config and forcing logging off for 
	 * this transaction (set includeInEndOfTransactionLogs to false). 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param result transaction pass or fail as Outcome
	 * @param includeInEndOfTransactionLogs boolean option to switch on/off logs for transaction ends (for this txn) 
	 * @return the JMeter sub-result for this transaction (which includes the transaction time)
	 */
	public SampleResult endTransaction(String transactionLabel, Outcome result, boolean includeInEndOfTransactionLogs) {
		return endTransaction(transactionLabel, result, null, includeInEndOfTransactionLogs);
	}
	
	
	@Override
	public SampleResult endTransaction(String transactionLabel, Outcome result, String responseCode) {
		return endTransaction(transactionLabel, result, responseCode, true);
	}
	
	
	/**
	 * As per {@link #endTransaction(String, Outcome, String)}, but also allows for overriding current logging config for this transaction 
	 * (set includeInEndOfTransactionLogs to false). 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param result transaction pass or fail as Outcome
	 * @param responseCode response code text
	 * @param includeInEndOfTransactionLogs boolean option to switch on/off logs for transaction ends (for this txn) 
	 * 
	 * @return SampleResult the JMeter sub-result for this transaction (which includes the transaction time)
	 */
	@Override	
	public SampleResult endTransaction(String transactionLabel, Outcome result, String responseCode, boolean includeInEndOfTransactionLogs){
		
		SampleResult sampleResult = super.endTransaction(transactionLabel, result, responseCode);

		String markIfailedTxnTag = "";
		if (result.getOutcomeText().equals(Outcome.FAIL.getOutcomeText())){
			markIfailedTxnTag = "_FAILED";
		}
		
		if (includeInEndOfTransactionLogs) {
			if 	(bufferScreenshotsAtEndOfTransactions) {
				bufferScreenshot(transactionLabel + markIfailedTxnTag + "_ends" );
			}
			if 	(writeScreenshotsAtEndOfTransactions) {
				writeScreenshot(transactionLabel + markIfailedTxnTag + "_ends" );
			}
			if 	(bufferPageSourceAtEndOfTransactions) {
				bufferPageSource(transactionLabel + markIfailedTxnTag + "_source_at_end" );
			}
			if 	(writePageSourceAtEndOfTransactions) {
				writePageSource(transactionLabel + markIfailedTxnTag + "_source_at_end" );
			}
			if 	(bufferPerformanceLogAtEndOfTransactions) {
				bufferDriverPerfLogs(transactionLabel + markIfailedTxnTag + "_perflog");
			}
			if 	(writePerformanceLogAtEndOfTransactions) {
				writeDriverPerfLogs(transactionLabel + markIfailedTxnTag + "_perflog");
			}			
		}
		return sampleResult;
	}


	@Override
	public SampleResult setTransaction(String transactionLabel, long transactionTime){
		return setTransaction(transactionLabel, transactionTime, true);
	}
	
	/**
	 * As per {@link #setTransaction(String, long)} but will tag the transaction as a DevTools (CDP) transaction 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime time taken for the transaction. Expects Milliseconds.
	 * 
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 * @return SampleResult
	 */
	@Override	
	public SampleResult setCdpTransaction(String transactionLabel, long transactionTime){
		return setCdpTransaction(transactionLabel, transactionTime, true);
	}
	
	
	
	@Override
	public SampleResult setTransaction(String transactionLabel, long transactionTime, boolean success) {
		return setTransaction(transactionLabel, JMeterFileDatatypes.TRANSACTION, transactionTime, success, null, true);
	}

	/**
	 * As per {@link #setTransaction(String, long, boolean)} but will tag the transaction as a DevTools (CDP) transaction 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime time taken for the transaction. Expects Milliseconds.
	 * @param success pass or fail transaction
	 * 
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 * @return SampleResult
	 */
	@Override	
	public SampleResult setCdpTransaction(String transactionLabel, long transactionTime, boolean success) {
		return setTransaction(transactionLabel, JMeterFileDatatypes.CDP, transactionTime, success, null, true );		
	}

	
	/**
	 * As per {@link #setTransaction(String, long, boolean)}, but also allows for forcing switch-off of logging for this
	 * transaction (includeInEndOfTransactionLogs set to false).  
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime transaction time (ms)
	 * @param success  pass or fail transaction
	 * @param includeInEndOfTransactionLogs boolean option to switch on/off logs for transaction ends (for this txn) 
	 * @return SampleResult
	 */
	@Override	
	public SampleResult setTransaction(String transactionLabel, long transactionTime, boolean success, boolean includeInEndOfTransactionLogs){
		return setTransaction(transactionLabel, JMeterFileDatatypes.TRANSACTION, transactionTime, success, null, includeInEndOfTransactionLogs);
	}
	
	/**
	 * As per {@link #setTransaction(String, long, boolean, boolean)}, but will tag the transaction as a DevTools (CDP) transaction
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime transaction time (ms)
	 * @param success  pass or fail transaction
	 * @param includeInEndOfTransactionLogs boolean option to switch on/off logs for transaction ends (for this txn)  
	 * @return SampleResult
	 */
	@Override	
	public SampleResult setCdpTransaction(String transactionLabel, long transactionTime, boolean success, boolean includeInEndOfTransactionLogs){
		return setTransaction(transactionLabel, JMeterFileDatatypes.CDP, transactionTime, success, null, includeInEndOfTransactionLogs);
	}
	
		
	@Override
	public SampleResult setTransaction(String transactionLabel, long transactionTime, boolean success, String responseCode) {
		return setTransaction(transactionLabel, JMeterFileDatatypes.TRANSACTION, transactionTime, success, responseCode, true);
	}

	/**
	 * As per {@link #setTransaction(String, long, boolean, String)}, but will tag the transaction as a DevTools (CDP) transaction
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime time taken for the transaction
	 * @param success  the success (true) or failure (false) state of the transaction
	 * @param responseCode response message (useful for error transactions)
	 *   
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 * @return SampleResult
	 */
	@Override
	public SampleResult setCdpTransaction(String transactionLabel, long transactionTime, boolean success, String responseCode) {
		return setTransaction(transactionLabel, JMeterFileDatatypes.CDP , transactionTime, success, responseCode, true);
	}

	
	/**
	 * As per {@link #setTransaction(String, long, boolean, String)}, but also allows for forcing disable of logging for this
	 * transaction (<code>includeInEndOfTransactionLogs</code> set to <code>false</code>), and also  additional option of setting the data type
	 * field of the JMeter results file
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param jMeterFileDatatypes  a {@link JMeterFileDatatypes} (text value will be written in the data type field of the JMeter results file) 
	 * @param transactionTime  transaction time (ms)
	 * @param success   pass or fail transaction
	 * @param responseCode  text response code
	 * @param includeInEndOfTransactionLogs boolean option to disable transaction end logs for this txn ('false' to disable) 
	 * @return SampleResult
	 */
	@Override
	public SampleResult setTransaction(String transactionLabel, JMeterFileDatatypes jMeterFileDatatypes,
			long transactionTime, boolean success, String responseCode, boolean includeInEndOfTransactionLogs){
		
		SampleResult sampleResult = super.setTransaction(transactionLabel, jMeterFileDatatypes, transactionTime, success, responseCode);
		
		if (includeInEndOfTransactionLogs) {
			if 	(bufferScreenshotsAtEndOfTransactions) {
				bufferScreenshot(transactionLabel + "_set");
			}
			if 	(writeScreenshotsAtEndOfTransactions) {
				writeScreenshot(transactionLabel + "_set");
			}
			if 	(bufferPageSourceAtEndOfTransactions) {
				bufferPageSource(transactionLabel + "_set");
			}
			if 	(writePageSourceAtEndOfTransactions) {
				writePageSource(transactionLabel + "_set");
			}
			if 	(bufferPerformanceLogAtEndOfTransactions) {
				bufferDriverPerfLogs(transactionLabel + "_perflog");
			}
			if 	(writePerformanceLogAtEndOfTransactions) {
				writeDriverPerfLogs(transactionLabel + "_perflog");
			}				
		}
		return sampleResult;
	}
	
	
	/**
	 * Screenshot logs to be written or buffered at the start transactions
	 * @param configLogLevel Used to set actions when writing logs
	 */
	@Override	
	public void logScreenshotsAtStartOfTransactions(Mark59LogLevels configLogLevel) {
		bufferScreenshotsAtStartOfTransactions = false;
		writeScreenshotsAtStartOfTransactions = false;
		
		if ( Mark59LogLevels.BUFFER.equals(configLogLevel)) {
			bufferScreenshotsAtStartOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(configLogLevel) ) {
			writeScreenshotsAtStartOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(configLogLevel) ) {
			
			if (LOG.isTraceEnabled() ) {
				writeScreenshotsAtStartOfTransactions = true;
			} 
		}
	}

	/**
	 * Screenshot logs to be written or buffered at the end of transactions
	 * @param configLogLevel Used to set actions when writing logs  
	 */
	@Override	
	public void logScreenshotsAtEndOfTransactions(Mark59LogLevels configLogLevel) {
		bufferScreenshotsAtEndOfTransactions = false; 
		writeScreenshotsAtEndOfTransactions = false;
		
		if ( Mark59LogLevels.BUFFER.equals(configLogLevel) ) {
			bufferScreenshotsAtEndOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(configLogLevel) ) {
			writeScreenshotsAtEndOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(configLogLevel) ) {

			if (LOG.isTraceEnabled() ) {
				writeScreenshotsAtEndOfTransactions = true;
			} else if (LOG.isDebugEnabled()  ) {
				writeScreenshotsAtEndOfTransactions = true;	
			}  
		}
	}
	
	/**
	 * PageSource to be written or buffered at the start of transactions
	 * @param configLogLevel Used to set actions when writing screenshots/logs  
	 */
	@Override	
	public void logPageSourceAtStartOfTransactions(Mark59LogLevels configLogLevel) {
		bufferPageSourceAtStartOfTransactions = false; 
		writePageSourceAtStartOfTransactions = false;	
		
		if ( Mark59LogLevels.BUFFER.equals(configLogLevel) ) {
			bufferPageSourceAtStartOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(configLogLevel) ) {
			writePageSourceAtStartOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(configLogLevel) ) {
			
			if (LOG.isTraceEnabled() ) {
				writePageSourceAtStartOfTransactions = true;
			} 
		}
	}

	/**
	 * PageSource to be written or buffered at the end of transactions
	 * @param configLogLevel Used to set actions when writing logs  
	 */
	@Override	
	public void logPageSourceAtEndOfTransactions(Mark59LogLevels configLogLevel){
		bufferPageSourceAtEndOfTransactions = false; 
		writePageSourceAtEndOfTransactions = false;
		
		if ( Mark59LogLevels.BUFFER.equals(configLogLevel) ) {
			bufferPageSourceAtEndOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(configLogLevel) ) {
			writePageSourceAtEndOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(configLogLevel) ) {

			if (LOG.isTraceEnabled() ) {
				writePageSourceAtEndOfTransactions = true;
			} else if (LOG.isDebugEnabled()  ) {
				writePageSourceAtEndOfTransactions = true;	
			} 
		}
	}
	
	/**
	 * PerformanceLog to be written or buffered at the end of transactions
	 * (currently implemented for selenium chromedriver only)
	 * @param configLogLevel Used to set actions when writing logs  
	 */
	@Override	
	public void logPerformanceLogAtEndOfTransactions(Mark59LogLevels configLogLevel){
		bufferPerformanceLogAtEndOfTransactions = false; 
		writePerformanceLogAtEndOfTransactions = false;
		
		if ( Mark59LogLevels.BUFFER.equals(configLogLevel) ) {
			bufferPerformanceLogAtEndOfTransactions = true; 
		} else if ( Mark59LogLevels.WRITE.equals(configLogLevel) ) {
			writePerformanceLogAtEndOfTransactions = true;
		} else if ( Mark59LogLevels.DEFAULT.equals(configLogLevel) ) {

			if (LOG.isTraceEnabled() ) {
				writePerformanceLogAtEndOfTransactions = true;
			} else if (LOG.isDebugEnabled()  ) {
				writePerformanceLogAtEndOfTransactions = true;	
			} 
		}
	}
	
	
	/**
	 * A convenience method which can be used to control all transaction-level logging which occurs when a transaction ends.
	 * <p>For example, in a script
	 * <br><pre><code> jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER); </code></pre>
	 * <p>will switch all end of transaction logging output to be 'buffered'.  This would generally be considered the most common
	 *  method for debugging a troublesome script in the first instance.   Output will only occur if the transaction fails. 
	 * The above code fragment sets the debug flags to :
	 * <ul>  	
	 * <li>  	buffer Screenshots End Of Transactions
	 * <li>  	buffer Page Source End Of Transactions	    
	 * <li>		buffer PerformanceLog At End Of Transactions
	 * </ul> 
	 * 
	 * @param configLogLevel see explanation above
	 */
	@Override	
	public void logAllLogsAtEndOfTransactions(Mark59LogLevels configLogLevel) {
		logScreenshotsAtEndOfTransactions(configLogLevel);
		logPageSourceAtEndOfTransactions(configLogLevel);
		logPerformanceLogAtEndOfTransactions(configLogLevel);
		
	}	

	
	/**
	 * A convenience method which can be used to control transaction-level logging which occurs when a transaction starts.
 	 * That is, this method sets the debug flags to control logging at :
	 * <ul>  	
	 * <li>  	Screenshots at start of transactions
	 * <li>  	Page Source at start of transactions	    
	 * </ul>  
	 * 
	 * 	@param configLogLevel see explanations above
	 */
	@Override	
	public void logAllLogsAtStartOfTransactions(Mark59LogLevels configLogLevel) {
		logScreenshotsAtStartOfTransactions(configLogLevel);
		logPageSourceAtStartOfTransactions(configLogLevel);
	}	

	
	/**
	 * Capture and immediately output a screenshot (.jpg) log. Use with caution in a Performance and Volume 
	 * test as misuse of this method may produce many more screenshots than intended. 
	 * <p>Instead, you could use {@link #bufferScreenshot(String)} and {@link #writeBufferedArtifacts()}.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')  
	 */
	@Override		
	public abstract void writeScreenshot(String imageName);

	
	/**
	 * Stores a screenshot (.jpg) log in memory, ready to be written to file later.  
	 * If you want to immediately write a screenshot to file, use {@link #writeScreenshot(String)}  instead.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')   
	 */
	@Override	
	public abstract void bufferScreenshot(String imageName); 

	
	/**
	 * Capture and immediately output a page source (.html) log. Use with caution in a Performance and Volume 
	 * test as misuse of this method may produce many more screenshots than intended. 
	 * <p>Instead, you could use {@link #bufferPageSource(String)} and writeBufferedArtifacts().
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.html')
	 */
	@Override		
	public abstract void writePageSource(String imageName); 

	
	/**
	 * Stores a page source (.html) log in memory, ready to be written to file later.  
	 * If you want to immediately write a screenshot to file, use {@link #writePageSource(String)} instead.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.html')   
	 */
	@Override		
	public abstract void bufferPageSource(String imageName); 


	/**
	 * Note Performance logging is only implemented for selenium chrom(ium) drivers.
	 * @param textFileName last part of the log filename (but excluding extension - which is set as '.txt') 
	 */
	@Override		
	public abstract void writeDriverPerfLogs(String textFileName);

	
	/**
	 * Note Performance logging is only implemented for selenium chrom(ium) drivers
	 * @param textFileName last part of the log filename (excluding extension) 
	 */
	@Override	
	public abstract void bufferDriverPerfLogs(String textFileName);
	
}

