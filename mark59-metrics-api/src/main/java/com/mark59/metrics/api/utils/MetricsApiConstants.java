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

package com.mark59.metrics.api.utils;

import com.mark59.metrics.api.ServerMetricsCaptureUtils;
import com.mark59.metrics.api.ServerMetricsCaptureViaExcel;
import com.mark59.metrics.api.ServerMetricsCaptureViaWeb;
import com.mark59.metrics.drivers.ServerProfileRunner;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class MetricsApiConstants {

	/**
	 * Intention is that this string is used as a JMeter Parameter in a Java Request
	 * of one of the classes controlling metrics capture in a JMeter test, to determine 
	 * if (or what length) error message should be be logged via log4j (eg jmeter.log)
	 * when an attempt to retrieve a metric throws an error. 
	 * <p>The default currently set is a 'short' message will be logged. 
	 * <p>** Please Note ** This is independent of any logging done by the 'metrics' application
	 * itself when running the profiles and building the command response (refer below) 
	 * 
	 * @see ServerProfileRunner#commandsResponse
	 * @see ServerMetricsCaptureUtils#createJMeterTxnsUsingCommandResponse
	 * @see ServerMetricsCaptureViaWeb
	 * @see ServerMetricsCaptureViaExcel
	 */
	public static final String LOG_ERROR_MESSAGES	= "LOG_ERROR_MESSAGES";
	
	/**
	 * The same as {@link #LOG_ERROR_MESSAGES}, but output goes to console. 
	 */
	public static final String PRINT_ERROR_MESSAGES	= "PRINT_ERROR_MESSAGES";
	
	public static final boolean RUNNING_VIA_WEB = true;
	public static final boolean RUNNING_VIA_EXCEL = false;

}
