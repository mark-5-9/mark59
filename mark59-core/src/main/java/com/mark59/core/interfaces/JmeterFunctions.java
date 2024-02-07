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

package com.mark59.core.interfaces;

import java.util.Map;

import org.apache.jmeter.samplers.SampleResult;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;

/**
 * Defines methods that can be called throughout the lifecycle of the test in order to handle behavior around recording timings and other metrics.
 * 
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019  
 *
 * @see JmeterFunctionsImpl
 */
public interface JmeterFunctions {

	/**
	 * Starts timing a transaction.  Note you cannot start a transaction using the same name as one already running 
	 * in a script (controlled using an internally created transactionMap holding a key of running transaction names)
	 * and starts timing the transaction. 
	 * <p>Should be paired with a call an endTransaction method</p>
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @throws IllegalArgumentException if the transaction name supplied is an illegal value (null or empty) or already in use.
	 */
	void startTransaction(String transactionLabel);

	
	/**
	 * Starts timing a transaction.  Note you cannot start a transaction using the same name as one already running 
	 * in a script (controlled using an internally created transactionMap holding a key of running transaction names)
	 * and starts timing the transaction. 
	 * <p>Should be paired with a call an endTransaction method</p>
	 * @param transactionLabel ('label' in JMeter terminology) for the transaction
	 * @param jMeterFileDatatypes a {@link JMeterFileDatatypes} (it's text value will be written in the data type field of the JMeter results file)
	 * @throws IllegalArgumentException if the transaction name supplied is an illegal value (null or empty) or already in use.
	 */
	void startTransaction(String transactionLabel, JMeterFileDatatypes jMeterFileDatatypes);	
	
	
	/**
	 * Stop monitoring the transaction / End the timer
	 * <p>Ends an existing transaction with the supplied label name, stopping the timer</p>
	 * <p>Should be paired with a call to this.startTransaction(String).</p>
	 * 
	 * @param transactionID label for the transaction
	 * @return SampleResult
	 */
	SampleResult endTransaction(String transactionID);

	
	/**
	 * Set a transactions for a given time.
	 * 
	 * <p>This is independent of starting or stopping transactions, setting a specific value for the transaction duration.</p>
	 * 
	 * @param transactionLabel label for the transaction
	 * @param transactionTime time taken for the transaction (ms)
	 * @return SampleResult 
	 */
	SampleResult setTransaction(String transactionLabel, long transactionTime);
	
	
	/**
	 * Set a transactions for a given time (in milliseconds).  The transaction status of success (or fail) is passed. 
	 * <p>This is independent of starting or stopping transactions, setting a specific value for the transaction duration.</p>
	 * <p>Allows for setting whether the transaction was a success or failure</p>
	 * 
	 * @param transactionLabel label for the transaction
	 * @param transactionTime time taken for the transaction (ms)
	 * @param success success state of the transaction
	 * @return SampleResult
	 */
	SampleResult setTransaction(String transactionLabel, long transactionTime, boolean success);
	
	
	/**
	 * Set a transactions for a given time (in milliseconds).  The transaction status of success (or fail) is passed. 
	 * <p>This is independent of starting or stopping transactions, setting a specific value for the transaction duration.</p>
	 * <p>Allows for setting whether the transaction was a success or failure</p>
	 * <p>Allows for a response message (which is printed in a JMeter report for error transactions)</p>
	 * 
	 * @param transactionLabel label for the transaction
	 * @param transactionTime time taken for the transaction (ms)
	 * @param success success state of the transaction
	 * @param responseCode response message (useful for error transactions)
	 * @return SampleResult
	 */
	SampleResult setTransaction(String transactionLabel, long transactionTime, boolean success, String responseCode);


	/**
	 * As per {@link #setTransaction(String, long, boolean, String)}, but with the additional option of setting the data type
	 * field of the JMeter results file
	 * 
	 * @param transactionLabel label for the transaction
	 * @param jMeterFileDatatypes  a {@link JMeterFileDatatypes} (it's text value will be written in the data type field of the JMeter results file)
	 * @param transactionTime time taken for the transaction (ms)
	 * @param success success state of the transaction
	 * @param responseCode response message (useful for error transactions)
	 * @return SampleResult
	 */
	SampleResult setTransaction(String transactionLabel, JMeterFileDatatypes jMeterFileDatatypes, long transactionTime,
			boolean success, String responseCode);
	
	/**
	 * Add a single datapoint.
	 * <p>A datapoint reflects an arbitrary discrete value, other than time to complete a transaction.</p>
	 * 
	 * @param dataPointName label for the datapoint
	 * @param dataPointValue value for the datapoint
	 * @return SampleResult
	 */
	SampleResult userDataPoint(String dataPointName, long dataPointValue);
	
	
	/**
	 * Add a single result of a given datatype.
	 * 
	 * @param dataPointName label for the datapoint
	 * @param dataPointValue value for the datapoint
	 * @param jmeterFileDatatypes see data value as they appear on the JMeter results file
	 * @return SampleResult
	 * @see JMeterFileDatatypes
	 */
	SampleResult userDatatypeEntry(String dataPointName, long dataPointValue,  JMeterFileDatatypes jmeterFileDatatypes);

	
	/**
	 * Return results from running the test
	 * @return org.apache.jmeter.samplers.SampleResult
	 */	
	SampleResult getMainResult();


	
	/**
	 * Returns the transaction id of the last (most recent) transaction started. 
	 * <p>(The selenium implementation excludes SET transactions and DataPoints).
	 * 
	 * @return mostRecentTransactionStarted (txnId)
	 */
	String getMostRecentTransactionStarted();
	
	
	/**
	 * Behaviours to execute at the end of test, such as terminating transations that were started but not ended.
	 * <p>Specific behaviours vary based on the particular implementation of Tester.</p>
	 */
	void tearDown();
	
	
	/**
	 * Marks the test as failed.
	 */
	void failTest();
	
	
	
	/**
	 * @return the Map of the buffered screenshots
	 */
	Map<String, byte[]> getBufferedLogs();
	
	
	/**
	 * Writes all buffered screenshots/logs to disk (ie, all transaction-level logging
	 * performed using a Mark59LogLevels of "BUFFER")
	 * <p>Can be implemented by extending this class and combining with a 
	 * {@link DriverFunctions} implementation can is capable of taking logs/screenshots  
	 */
	void writeBufferedArtifacts();

	
	/**
	 * Capture and immediately write a stack track log for the passed Exception.
	 * <p>(There's no 'Buffer' option for exceptions as it's assumed the script will not continue)
	 * @param stackTraceName filename to use for the log (without suffix)
	 * @param e Throwable (Exception) being loggoed
	 */
	void writeStackTrace(String stackTraceName, Throwable e);
	

	/**
	 * Save the byte[] to the specified file name file.
	 * 
	 * <p>Implementations may also create the parent log directory if missing (ie initial directory creation)
	 * 
	 * <p>Generally meant to be used within mark59 to write pre-defined log types 
	 * (eg Selenium screenshots, Chromium performance Logs, Exception stack traces), but can be invoked from 
	 * a user-written script to immediately write data to a mark59 log. 
	 * 
	 * @param mark59LogName filename to use for the log (without suffix) 
	 * @param mark59LogNameSuffix suffix (eg 'txt', 'jpg') of the filename to use for the log 
	 * @param mark59LogBytes the log data 
	 * 
	 * @see JmeterFunctionsImpl
	 */
	void writeLog(String mark59LogName, String mark59LogNameSuffix, byte[] mark59LogBytes);
	
	
	/**
	 * Save a byte[] with a specified log name and suffix, ready to be written to file later. 
	 * 
	 * <p>Generally meant to be used within Mark59 to buffer pre-defined log types 
	 * (eg Selenium screenshots, Chromium performance Logs), but can be invoked from 
	 * a user-written script.
	 * 
	 * @param mark59LogName last part of the log filename (excluding extension)  
	 * @param mark59LogNameSuffix suffix of the log filename (eg 'txt', 'jpg')  
	 * @param mark59LogBytes  the log data 
	 * 
	 * @see JmeterFunctionsImpl
	 */
	void bufferLog(String mark59LogName, String mark59LogNameSuffix, byte[] mark59LogBytes);

}
