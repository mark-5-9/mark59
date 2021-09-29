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

package com.mark59.datahunter.performanceTest.dsl.helpers;

import java.util.Arrays;
import java.util.List;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019
 */
public class DslConstants {
	

	public static final String DEFAULT_DATAHUNTER_URL 				= "http://localhost:8081/dataHunter";	

	public static final String ADD_POLICY_URL_PATH      			= "/add_policy";
	public static final String COUNT_POLICIES_URL_PATH 				= "/count_policies";	
	public static final String COUNT_POLICIES_BREAKDOWN_URL_PATH	= "/count_policies_breakdown";		
	public static final String PRINT_POLICY_URL_PATH      			= "/print_policy";
	public static final String PRINT_SELECTED_POLICIES_URL_PATH		= "/print_selected_policies";
	public static final String DELETE_POLICY_URL_PATH				= "/delete_policy";	
	public static final String DELETE_MULTIPLE_POLICIES_URL_PATH	= "/delete_multiple_policies";
	public static final String NEXT_POLICY_URL_PATH					= "/next_policy";	
	public static final String UPDATE_POLICIES_USE_STATE_URL_PATH	= "/update_policies_use_state";		
	public static final String ASYNC_MESSAGE_ANALYZER_URL_PATH		= "/async_message_analyzer";
	
	public static final String USE									= "use";  
	public static final String LOOKUP								= "lookup";
	
	public static final String URL_PARM_APPLICATION 				= "?application=";		
	public static final String URL_PARM_USEABILITY 					= "&useability=";		
	public static final String URL_PARM_USE_OR_LOOKUP				= "&pUseOrLookup=";		
	
	public static final String UNSELECTED 							= "";  	
	public static final String REUSABLE 							= "REUSABLE";  
	public static final String UNPAIRED    							= "UNPAIRED"; 
	public static final String UNUSED								= "UNUSED";  
	public static final String USED									= "USED";  		
	
	public static final List<String>  USEABILITY_LIST =  	Arrays.asList(UNSELECTED, REUSABLE, UNPAIRED, UNUSED, USED);  

	
	public static final String STARTS_WITH 						= "STARTS_WITH";  	
	public static final String EQUALS 							= "EQUALS";  
	
	public static final List<String>  APPLICATION_STARTS_WITH_OR_EQUALS_LIST =  	Arrays.asList(STARTS_WITH, EQUALS);  
	
	
	public static final String SELECT_MOST_RECENTLY_ADDED 		= "SELECT_MOST_RECENTLY_ADDED";  
	public static final String SELECT_OLDEST_ENTRY    			= "SELECT_OLDEST_ENTRY"; 
	public static final String SELECT_RANDOM_ENTRY				= "SELECT_RANDOM_ENTRY"; 
		
	public static final List<String>  GET_NEXT_POLICY_SELECTOR = Arrays.asList( SELECT_MOST_RECENTLY_ADDED, SELECT_OLDEST_ENTRY, SELECT_RANDOM_ENTRY );  	
		
	public static final String SQL_RESULT_PASS					= "PASS"; 
	public static final String SQL_RESULT_FAIL					= "FAIL"; 	
}
