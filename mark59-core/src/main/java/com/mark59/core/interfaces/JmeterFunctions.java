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

package com.mark59.core.interfaces;

import org.apache.jmeter.samplers.SampleResult;

import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;

/**
 * Defines methods that can be called throughout the lifecycle of the test in order to handle behavior around recording timings and other metrics.
 * 
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019  
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
	 * Behaviours to execute at the end of test, such as terminating transations that were started but not ended.
	 * <p>Specific behaviours vary based on the particular implementation of Tester.</p>
	 */
	void tearDown();
	
	
	/**
	 * Marks the test as failed.
	 */
	void failTest();
	
	
	/**
	 * Return results from running the test
	 * @return org.apache.jmeter.samplers.SampleResult
	 */	
	SampleResult getMainResult();

}
