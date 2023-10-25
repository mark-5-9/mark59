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

package com.mark59.datahunter.api.application;

import java.util.Arrays;
import java.util.List;

public class DataHunterConstants {

	public static final String MARK59_VERSION_DATAHUNTER 	= "5.6";  
	
	public static final String UNSELECTED 					= "";  
	
	public static final String REUSABLE	= "REUSABLE";  
	public static final String UNPAIRED	= "UNPAIRED"; 
	public static final String UNUSED	= "UNUSED";  
	public static final String USED		= "USED"; 
	
	/**
	 * REUSABLE, UNPAIRED, UNUSED, USED
	 */	
	public static final List<String>  USEABILITY_LIST = Arrays.asList(REUSABLE, UNPAIRED, UNUSED, USED);  

	
	/* Policy Selection Ordering  */
	
	public static final String SELECT_MOST_RECENTLY_ADDED 	= "SELECT_MOST_RECENTLY_ADDED";  
	public static final String SELECT_OLDEST_ENTRY    		= "SELECT_OLDEST_ENTRY"; 
	public static final String SELECT_RANDOM_ENTRY			= "SELECT_RANDOM_ENTRY";  
	public static final String SELECT_UNORDERED				= "SELECT_UNORDERED";  

	/**
	 * SELECT_MOST_RECENTLY_ADDED, SELECT_OLDEST_ENTRY, SELECT_RANDOM_ENTRY
	 */
	public static final List<String>  GET_NEXT_POLICY_SELECTOR =  
			Arrays.asList( SELECT_MOST_RECENTLY_ADDED, SELECT_OLDEST_ENTRY, SELECT_RANDOM_ENTRY );  

	
	/* Filtered Select Ordering  */
	
	public static final String KEY	 		 = "ID_LIFECYCLE";  	
	public static final String USEABILTY_KEY = "USEABILTY_ID_LIFECYCLE";  	
	public static final String OTHERDATA	 = "OTHERDATA";  	
	public static final String CREATED		 = "CREATED";  	
	public static final String UPDATED		 = "UPDATED";  	
	public static final String EPOCHTIME	 = "EPOCHTIME"; 
	
	/**
	 * ID_LIFECYCLE, USEABILTY_ID_LIFECYCLE, OTHERDATA, CREATED, UPDATED, EPOCHTIME  
	 */
	public static final List<String> FILTERED_SELECT_ORDER_LIST = 	
			Arrays.asList( KEY, USEABILTY_KEY, OTHERDATA, CREATED, UPDATED, EPOCHTIME );	
	
	public static final String ASC  = "ASCENDING";  	
	public static final String DESC = "DESCENDING";
	
	/**
	 * ASCENDING, DESCENDING  
	 */
	public static final List<String>  ORDER_DIRECTION_LIST = Arrays.asList( ASC, DESC);
	
	
	public static final String EQUALS		= "EQUALS";  
	public static final String STARTS_WITH	= "STARTS_WITH";  	
	public static final List<String>  APPLICATION_OPERATORS = Arrays.asList( EQUALS, STARTS_WITH);

	public static final String UPDATE_USEABILITY_ON_EXISTING_ENTRIES 			= "UPDATE_USEABILITY_ON_EXISTING_ENTRIES";  
	public static final String LEAVE_USEABILITY_ON_EXISTING_ENTRIES_UNCHANGED	= "LEAVE_USEABILITY_ON_EXISTING_ENTRIES_UNCHANGED";  
	
	public static final List<String>  UPDATE_OR_BYPASS      =  
			Arrays.asList( UPDATE_USEABILITY_ON_EXISTING_ENTRIES, LEAVE_USEABILITY_ON_EXISTING_ENTRIES_UNCHANGED);
	
	
	public static final String USE	  = "USE";  
	public static final String LOOKUP = "LOOKUP"; 

    /**
     * All H2 application database property files to start with 'h2' 
     */
    public static final String H2 = "h2";

}
