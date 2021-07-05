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

package com.mark59.core.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Cohen
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class Mark59Constants {
	
	public static final String MARK59_VERSION  = "3.3";	
		
	public static final String TRUE = "TRUE";
	public static final String FALSE = "FALSE";
	public static final String DEFAULT_DRIVER = "CHROME";
	
	
	
	public static final int DEFAULT_BROWSER_WIDTH  = 1920;
	public static final int DEFAULT_BROWSER_HEIGHT = 1080;
	public static final String DEFAULT_BROWSER_DIMENSIONS ="1920,1080";
    
	public static final String REFERENCE  = "__Mark59.com____";

    public static final String H2     		= "h2";
    public static final String MYSQL  		= "mysql";
    public static final String PG     		= "pg";
    public static final String H2MEM  		= "h2mem";
    public static final String H2TCPCLIENT	= "h2tcpclient";
    
    /**
     * Defines an enumeration of values used in the Mark59 framework that are used in to populate the JMeter
     *  results file data type (the 'dt' column in a csv formatted file) 
     * 
     * <p>The current list of types, and their corresponding text values (as would appear in a JMeter csv Results file) are:<br>
	 * <table summary="">
  	 *	<tr><td>Mark59 JMeterFileDatatypes<br>enumeration<br></td><td> --&gt; </td><td>JMeter file 'dt' value</td></tr>
  	 *	<tr><td>CPU_UTIL  </td><td> --&gt; </td><td>CPU_UTIL   </td></tr>
  	 *	<tr><td>MEMORY    </td><td> --&gt; </td><td>MEMORY     </td></tr>
  	 *	<tr><td>DATAPOINT </td><td> --&gt; </td><td>DATAPOINT  </td></tr>
  	 *	<tr><td>TRANSACTION</td><td> --&gt; </td><td>'' (blank)</td></tr>
  	 *	<tr><td>PARENT</td><td> --&gt; </td><td>PARENT</td></tr>
  	 * </table>
  	 * 
  	 * <p>Note that 'PARENT' is intended for internal use only.  
     */
    public static enum JMeterFileDatatypes {

    	DATAPOINT ("DATAPOINT", true), CPU_UTIL ("CPU_UTIL", true),	MEMORY ("MEMORY", true), TRANSACTION ("", false), PARENT ("PARENT", false);
    	
    	private final String datatypeText;
    	private final boolean metricDataType;
    	
    	private JMeterFileDatatypes(String datatypeText, boolean metricDataType) {
    		this.datatypeText = datatypeText;
    		this.metricDataType = metricDataType;
    	}
    	public String getDatatypeText() {
    		return datatypeText;
    	}
		public boolean isMetricDataType() {
			return this.metricDataType;
		}    	
		public static List<String> listOfJMeterFileDatatypes() {
			List<String> listOfJMeterFileDatatypes = new ArrayList<String>();
			for (JMeterFileDatatypes jMeterFileDatatype : JMeterFileDatatypes.values()) {
				listOfJMeterFileDatatypes.add(jMeterFileDatatype.name());
			}
			return listOfJMeterFileDatatypes;
		}
		public static List<String> listOfMetricJMeterFileDatatypes() {
			List<String> listOfMetricJMeterFileDatatypes = new ArrayList<String>();
			for (Mark59Constants.JMeterFileDatatypes jMeterFileDatatype : JMeterFileDatatypes.values()) {
				if (jMeterFileDatatype.isMetricDataType()) {
					listOfMetricJMeterFileDatatypes.add(jMeterFileDatatype.name());
				}
			}
			return listOfMetricJMeterFileDatatypes;
		}
    }
    
    /**
     * Defines an enumeration for transaction data-type values used in the Mark59 framework that are stored 
     * in the database (TXN_TYPE on the transaction table ) 
     */
    public static enum DatabaseTxnTypes { 

    	DATAPOINT(true), CPU_UTIL(true), MEMORY(true), TRANSACTION(false);
		
    	private final boolean metricTxnType;   
		private DatabaseTxnTypes(boolean metricTxnType) {
			this.metricTxnType = metricTxnType;  
		}
		public boolean isMetricTxnType() {
			return this.metricTxnType;
		}
		public static List<String> listOfDatabaseTxnTypes(){
			List<String> listOfDatabaseTxnTypes = new ArrayList<String>();
			for (DatabaseTxnTypes databaseTxnType : DatabaseTxnTypes.values()) {
				listOfDatabaseTxnTypes.add(databaseTxnType.name());
			}
			return listOfDatabaseTxnTypes;
		}
		public static List<String> listOfMetricDatabaseTxnTypes() {
			List<String> listOfMetricDatabaseTxnTypes = new ArrayList<String>();
			for (DatabaseTxnTypes databaseTxntypes : DatabaseTxnTypes.values()) {
				if (databaseTxntypes.isMetricTxnType()) {
					listOfMetricDatabaseTxnTypes.add(databaseTxntypes.name());
				}
			}
			return listOfMetricDatabaseTxnTypes;
		}
	}


	private Mark59Constants() {
	}    
}
