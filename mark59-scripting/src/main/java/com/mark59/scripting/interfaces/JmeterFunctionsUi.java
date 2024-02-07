package com.mark59.scripting.interfaces;

import org.apache.jmeter.samplers.SampleResult;

import com.mark59.core.Outcome;
import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.core.utils.Mark59LogLevels;

/**
 * Extends the JmeterFunctions interface, to cover methods that can are considered specific to UI based scripts (Selenium and Playwright)
 * 
 * 
 * @author Philip Webb    
 * Written: Australian Summer 2023/24  
 */
public interface JmeterFunctionsUi extends JmeterFunctions {

	/**
	 * As per startTransaction(String), but will tag the transaction as a DevTools (CDP) transaction.
	 * <p>You can end the transaction with any of the <code>endTransaction</code> methods or {@link #endCdpTransaction(String)}
	 * (which is functionally equivalent to endTransaction(String)).  This is because the transaction is marked 
	 * as a DevTools (CDP) transaction when it is started by this method.   
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @throws IllegalArgumentException if the transaction name supplied is an illegal value (null or empty) or already in use.
	 */
	void startCdpTransaction(String transactionLabel);

	/**
	 * Identical to endTransaction(String).  Included as convenience to explicitly mark the end of DevTools (CDP) 
	 * transactions if you wish to do so. 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @throws IllegalArgumentException if the transactionLabel supplied is an illegal value (null or empty)
	 * @return the JMeter subresult for this transaction - which includes the transaction time (getTime) 
	 */
	SampleResult endCdpTransaction(String transactionLabel);

	/**
	 * As per startTransaction(String), but with the additional options specifying what type of transaction this is, and switching
	 * off all screenshot writing or buffering for the start of this transaction, regardless of the current {@link Mark59LogLevels} settings  
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param jMeterFileDatatypes a {@link JMeterFileDatatypes} (text value will be written in the data type field of the JMeter results file)
	 * @param includeInStartOfTransactionLogs boolean option to switch on/off logs for transaction starts (an override for this txn) 
	 */
	void startTransaction(String transactionLabel, JMeterFileDatatypes jMeterFileDatatypes,
			boolean includeInStartOfTransactionLogs);

	/**
	 * As per endTransaction(String), but also allows for overriding current logging config and forcing logging off for 
	 * this transaction (set includeInEndOfTransactionshots to false). 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param includeInEndOfTransactionLogs boolean option to switch on/off logs for transaction ends (an override for this txn) 
	 * @return the JMeter sub-result for this transaction (which includes the transaction time)
	 */
	SampleResult endTransaction(String transactionLabel, boolean includeInEndOfTransactionLogs);

	/**
	 * As per #endTransaction(String, Outcome, String), but also allows for overriding current logging config for this transaction 
	 * (set includeInEndOfTransactionLogs to false). 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param result transaction pass or fail as Outcome
	 * @param responseCode response code text
	 * @param includeInEndOfTransactionLogs boolean option to switch on/off logs for transaction ends (for this txn) 
	 * 
	 * @return SampleResult the JMeter sub-result for this transaction (which includes the transaction time)
	 */
	SampleResult endTransaction(String transactionLabel, Outcome result, String responseCode,
			boolean includeInEndOfTransactionLogs);

	/**
	 * As per setTransaction(String, long) but will tag the transaction as a DevTools (CDP) transaction 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime time taken for the transaction. Expects Milliseconds.
	 * 
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 * @return SampleResult
	 */
	SampleResult setCdpTransaction(String transactionLabel, long transactionTime);

	/**
	 * As per setTransaction(String, long, boolean) but will tag the transaction as a DevTools (CDP) transaction 
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime time taken for the transaction. Expects Milliseconds.
	 * @param success pass or fail transaction
	 * 
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 * @return SampleResult
	 */
	SampleResult setCdpTransaction(String transactionLabel, long transactionTime, boolean success);

	/**
	 * As per setTransaction(String, long, boolean), but also allows for forcing switch-off of logging for this
	 * transaction (includeInEndOfTransactionLogs set to false).  
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime transaction time (ms)
	 * @param success  pass or fail transaction
	 * @param includeInEndOfTransactionLogs boolean option to switch on/off logs for transaction ends (for this txn) 
	 * @return SampleResult
	 */
	SampleResult setTransaction(String transactionLabel, long transactionTime, boolean success,
			boolean includeInEndOfTransactionLogs);

	/**
	 * As per setTransaction(String, long, boolean, boolean), but will tag the transaction as a DevTools (CDP) transaction
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime transaction time (ms)
	 * @param success  pass or fail transaction
	 * @param includeInEndOfTransactionLogs boolean option to switch on/off logs for transaction ends (for this txn)  
	 * @return SampleResult
	 */
	SampleResult setCdpTransaction(String transactionLabel, long transactionTime, boolean success,
			boolean includeInEndOfTransactionLogs);

	/**
	 * As per setTransaction(String, long, boolean, String), but will tag the transaction as a DevTools (CDP) transaction
	 * 
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param transactionTime time taken for the transaction
	 * @param success  the success (true) or failure (false) state of the transaction
	 * @param responseCode response message (useful for error transactions)
	 *   
	 * @throws IllegalArgumentException if the transactionLabel is null or empty
	 * @return SampleResult
	 */
	SampleResult setCdpTransaction(String transactionLabel, long transactionTime, boolean success, String responseCode);

	/**
	 * As per setTransaction(String, long, boolean, String), but also allows for forcing disable of logging for this
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
	SampleResult setTransaction(String transactionLabel, JMeterFileDatatypes jMeterFileDatatypes, long transactionTime,
			boolean success, String responseCode, boolean includeInEndOfTransactionLogs);

	/**
	 * Screenshot logs to be written or buffered at the start transactions
	 * @param configLogLevel Used to set actions when writing logs.  
	 * @see Mark59LogLevels
	 */
	void logScreenshotsAtStartOfTransactions(Mark59LogLevels configLogLevel);

	/**
	 * Screenshot logs to be written or buffered at the end of transactions
	 * @param configLogLevel Used to set actions when writing logs.  
	 * @see Mark59LogLevels
	 */
	void logScreenshotsAtEndOfTransactions(Mark59LogLevels configLogLevel);

	/**
	 * PageSource to be written or buffered at the start of transactions
	 * @param configLogLevel Used to set actions when writing screenshots/logs  
	 * @see Mark59LogLevels 
	 */
	void logPageSourceAtStartOfTransactions(Mark59LogLevels configLogLevel);

	/**
	 * PageSource to be written or buffered at the end of transactions
	 * @param configLogLevel Used to set actions when writing logs  
	 * @see Mark59LogLevels
	 */
	void logPageSourceAtEndOfTransactions(Mark59LogLevels configLogLevel);

	/**
	 * PerformanceLog to be written or buffered at the end of transactions
	 * (currently implemented for selenium chromedriver only)
	 * @param configLogLevel Used to set actions when writing logs 
	 * @see Mark59LogLevels 
	 */
	void logPerformanceLogAtEndOfTransactions(Mark59LogLevels configLogLevel);

	/**
	 * A convenience method which can be used to control all transaction-level logging which occurs when a transaction ends.
	 * <p>For example, in a script
	 * <br><pre><code> jm.logAllLogsAtEndOfTransactions(Mark59LogLevels.BUFFER); </code></pre>
	 * <p>will switch all end of transaction logging output to be 'buffered'.  This would generally be considered the most common 
	 * method for debugging a troublesome script in the first instance.  Output will only occur if the transaction fails. 
	 * The above code fragment sets the debug flags to :
	 * <ul>  	
	 * <li>  	buffer Screenshots End Of Transactions
	 * <li>  	buffer Page Source End Of Transactions	    
	 * <li>		buffer PerformanceLog At End Of Transactions
	 * </ul> 
	 * 
	 * @param configLogLevel  Used to set actions when writing logs
	 * @see Mark59LogLevels 
	 */
	void logAllLogsAtEndOfTransactions(Mark59LogLevels configLogLevel);

	/**
	 * A convenience method which can be used to control transaction-level logging which occurs when a transaction starts.
	 * That is, this method sets the debug flags to control logging at :
	 * <ul>  	
	 * <li>  	Screenshots at start of transactions
	 * <li>  	Page Source at start of transactions	    
	 * </ul>  
	 * 
	 * @param configLogLevel  Used to set actions when writing logs
	 * @see Mark59LogLevels 
	 */
	void logAllLogsAtStartOfTransactions(Mark59LogLevels configLogLevel);

	/**
	 * Capture and immediately output a screenshot (.jpg) log. Use with caution in a Performance and Volume 
	 * test as misuse of this method may produce many more screenshots than intended. 
	 * <p>Instead, you could use {@link #bufferScreenshot(String)} and writeBufferedArtifacts()}.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')  
	 */
	void writeScreenshot(String imageName);

	/**
	 * Stores a screenshot (.jpg) log in memory, ready to be written to file later.  
	 * If you want to immediately write a screenshot to file, use {@link #writeScreenshot(String)}  instead.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.jpg')   
	 */
	void bufferScreenshot(String imageName);

	/**
	 * Capture and immediately output a page source (.html) log. Use with caution in a Performance and Volume 
	 * test as misuse of this method may produce many more screenshots than intended. 
	 * <p>Instead, you could use {@link #bufferPageSource(String)} and writeBufferedArtifacts().
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.html')
	 */
	void writePageSource(String imageName);

	/**
	 * Stores a page source (.html) log in memory, ready to be written to file later.  
	 * If you want to immediately write a screenshot to file, use {@link #writePageSource(String)} instead.
	 * @param imageName last part of the log filename (but excluding extension - which is set as '.html')   
	 */
	void bufferPageSource(String imageName);

	/**
	 * Note Performance logging is only implemented for selenium chrom(ium) drivers.
	 * @param textFileName last part of the log filename (but excluding extension - which is set as '.txt') 
	 */
	void writeDriverPerfLogs(String textFileName);

	/**
	 * Note Performance logging is only implemented for selenium chrom(ium) drivers
	 * @param textFileName last part of the log filename (excluding extension) 
	 */
	void bufferDriverPerfLogs(String textFileName);


}
