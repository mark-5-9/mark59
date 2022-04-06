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

package com.mark59.metrics.application;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mark59.core.utils.Mark59Constants;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class AppConstantsMetrics {
	
	public static final String MARK59_TRENDING_VERSION = "4.2";		
	
	public static final String TXN_90TH_GRAPH = "TXN_90TH";
	public static final String TXN_95TH_GRAPH = "TXN_95TH";
	public static final String TXN_99TH_GRAPH = "TXN_99TH";

	public static final String SHOW_HIDE_CDP = "HIDE_CDP";
	public static final String SHOW_SHOW_CDP = "SHOW_CDP";
	public static final String SHOW_ONLY_CDP = "ONLY_CDP";
	
	public static final int MAX_NUM_RUNS_DISPLAYABLE_ON_HTML_TABLE  = 10;
	public static final int DEFAULT_NUM_RUNS_DISPLAYED       		= 10;  
	public static final int DEFAULT_NUM_BASELINE_RUNS_DISPLAYED 	= 1;  	
	
	public static final String DEFAULT_10	= "10";
	public static final String DEFAULT_01	= "1";	
	public static final String ALL		 	= "All";
	public static final BigDecimal THOUSAND = new BigDecimal(1000);  
	
	public static final String ACTIVE  		= "Active";
	
	public static final String CDP_TAG 		= " (CDP)";
	
	public static final String LOADRUNNER 	= "Loadrunner";
	public static final String JMETER		= "Jmeter";	
	public static final String GATLING		= "Gatling";	
	
	
	public static final String RUN_TIME_YET_TO_BE_CALCULATED	= "000000000000";
	public static final String NO_ARGUMENT_PASSED 	 		 	= "No argument passed.";
	
	public static final String JMETER_IGNORED_TXNS		= "IGNORE";  
	public static final String TXN_STOPPPED_STATUS	 	= "Stop";
	
	public static final String APPLY_TO_NEW_SLAS_ONLY 	= "New SLAs only";  	
	public static final String APPLY_TO_ALL_SLAS	  	= "All SLAs";  	
	
	
	public static final String METRIC_SOURCE_LOADRUNNER_DATAPOINT_METER	= LOADRUNNER + "_DataPoint";   //maps to lr access db table DataPoint_meter
	public static final String METRIC_SOURCE_LOADRUNNER_MONITOR_METER	= LOADRUNNER + "_SiteScope";   //maps to lr access db table Monitor_meter
	public static final String METRIC_SOURCE_JMETER_DATAPOINT	  		= JMETER + "_" + Mark59Constants.DatabaseTxnTypes.DATAPOINT.name(); 	
	public static final String METRIC_SOURCE_JMETER_CPU      	  		= JMETER + "_" + Mark59Constants.DatabaseTxnTypes.CPU_UTIL.name(); 	
	public static final String METRIC_SOURCE_JMETER_MEMORY     	  		= JMETER + "_" + Mark59Constants.DatabaseTxnTypes.MEMORY.name(); 	
	public static final String METRIC_SOURCE_JMETER_TRANSACTION	  		= JMETER + "_" + Mark59Constants.DatabaseTxnTypes.TRANSACTION.name();
	// there is no concept of datatype for Gatling, so all incoming simulation log entries will be TRANSACTIONs
	public static final String METRIC_SOURCE_GATLING_TRANSACTION		= GATLING + "_" + Mark59Constants.DatabaseTxnTypes.TRANSACTION.name(); 
		

	// bit of a stretch here for a constants - unmodifiable map for value lookups
	
	private static final Map<String, String> TOOL_DATATYPES_TO_SOURCE_VALUE_MAP = createDataTypeLookupMap();	
    private static Map<String, String> createDataTypeLookupMap() {
        Map<String, String> datatypevals = new HashMap<>();
        datatypevals.put(METRIC_SOURCE_LOADRUNNER_DATAPOINT_METER,	"DataPoint_meter");
        datatypevals.put(METRIC_SOURCE_LOADRUNNER_MONITOR_METER,    "Monitor_meter");
       return Collections.unmodifiableMap(datatypevals);
    }
	public static Map<String, String> getToolDataTypeToSourceValueMap() {
		return TOOL_DATATYPES_TO_SOURCE_VALUE_MAP;
	}

	
	private static final Map<String, String> VALUE_DERIVATON_TO_SOURCE_FIELD_MAP = createTxnFieldsMap();	
    private static Map<String, String> createTxnFieldsMap() {
        Map<String, String> result = new HashMap<>();
        result.put("Minimum",        "TXN_MINIMUM");
        result.put("Average",        "TXN_AVERAGE");
        result.put("Median",         "TXN_MEDIAN");
        result.put("Maximum",        "TXN_MAXIMUM");
        result.put("StdDeviation",   "TXN_STD_DEVIATION");
        result.put("90th",           "TXN_90TH");
        result.put("95th",           "TXN_95TH");
        result.put("99th",           "TXN_99TH");
        result.put("Pass",           "TXN_PASS");
        result.put("Fail",           "TXN_FAIL");
        result.put("Stop",           "TXN_STOP");
        result.put("First",          "TXN_FIRST");
        result.put("Last",           "TXN_LAST");
        result.put("Sum",            "TXN_SUM");
        result.put("Delay",          "TXN_DELAY");
        result.put("PercentOver90",  "TXN_90TH");
        return Collections.unmodifiableMap(result);
    }
	public static Map<String, String> getValueDerivatonToSourceFieldMap() {
		return VALUE_DERIVATON_TO_SOURCE_FIELD_MAP;
	}
	
	public static final List<String>  DIRECT_VALUE_DERIVATONS =  
			Arrays.asList("Minimum","Average","Median","Maximum","StdDeviation","90th","95th","99th","Pass","Fail","Stop","First","Last","Sum","Delay","PercentOver90");  
	
}
