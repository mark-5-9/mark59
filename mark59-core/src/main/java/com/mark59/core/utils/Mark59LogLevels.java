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

package com.mark59.core.utils;

import com.mark59.core.JmeterFunctionsImpl;
import com.mark59.core.interfaces.JmeterFunctions;

/**
 * Used to set actions when writing the transaction-level logs in the Mark59 framework.
 * 
 * <p>This enumeration is used in the mark59-selenium-implementation of {@link JmeterFunctionsImpl}, and should be used
 * for any further {@link JmeterFunctions} implementations that require buffering/writing of logs.   
 * 
 *  <b>WRITE</b> will force output of the log type being set to the Mark59 logging directory (as set by the property "mark59.log.directory").<p>
 *  <b>BUFFER</b> will keep the log data for the log type being set in memory, and will only be printed should the script fail, 
 *  or explicitly requested to by written via a  writeBufferedArtifacts() call (refer below),  otherwise the log data is cleared at the end of the script<p>
 *  <b>OFF</b> will switch off logging for the log type    
 *    
 *  <p><b>DEFAULT</b> uses these setting :
 *  <p> At Log4J Trace Level:
 *  <ul>
 *  <li> 	write Screenshots At Start and End OfTransactions
 *  <li> 	write Page Source At Start and End OfTransactions	    
 *	<li>	write Performance Log At End Of Transactions	
 *  </ul>
 *  <p> At Log4j Debug Level:
 *  <ul>
 *  <li>	write Screenshots End Of Transactions
 *  <li> 	write Page Source End Of Transactions	    
 *	<li>	write PerformanceLog At End Of Transactions	
 *  </ul>
 *  <p> At Log4j Info Level and above transaction-level logging is switched off (although exceptions are written).
 *
 * @see JmeterFunctions
 * @see JmeterFunctionsImpl
 * @see PropertiesKeys
 * 
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public enum Mark59LogLevels {
	
	/** DEFAULT	-  depends on log4j level (see class description) */
	DEFAULT ("default"),
	/** BUFFER	-  will keep the log data for the log type being set in memory  (see class description)  */
	BUFFER ("buffer"),
	/** WRITE	- force output of the log type being set to the Mark59 logging directory  (see class description)  */
	WRITE  ("write"),
	/** OFF	- switch off logging for the log type  (see class description)  */
	OFF    ("off");  
	
	private final String mark59LogLevelString;
	
	Mark59LogLevels(String mark59LogLevelString) {
		this.mark59LogLevelString = mark59LogLevelString;
	}
	
	/**
	 * @return the string representation of a Mark59LogLevels enum
	 */
	public String getName() {
		return mark59LogLevelString;
	}
	
	
	/**
	 * @param mark59LogLevelString the string representation of a Mark59LogLevels enum
	 * @return a mark59LogLevels enum 
	 */
	public static Mark59LogLevels fromString(String mark59LogLevelString) {
		if (mark59LogLevelString == null)
			return null;
		
		for (Mark59LogLevels mark59LogLevels : Mark59LogLevels.values()) {
			if (mark59LogLevels.mark59LogLevelString.equalsIgnoreCase(mark59LogLevelString.trim())) {
				return mark59LogLevels;
			}
		}
		return null;
	}
	
}

