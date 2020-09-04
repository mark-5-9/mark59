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

package com.mark59.datahunter.application;

import java.util.Arrays;
import java.util.List;

public class DataHunterConstants {

	public static final String DATAHUNTER_VERSION 			= "3.0.0";  
	
	public static final String UNSELECTED 					= "";  
	
	public static final String REUSABLE 					= "REUSABLE";  
	public static final String UNPAIRED    					= "UNPAIRED"; 
	public static final String UNUSED						= "UNUSED";  
	public static final String USED							= "USED";  		
	
	public static final List<String>  USEABILITY_LIST =  
			Arrays.asList(REUSABLE, UNPAIRED, UNUSED, USED);  

	
	public static final String SELECT_MOST_RECENTLY_ADDED 	= "SELECT_MOST_RECENTLY_ADDED";  
	public static final String SELECT_OLDEST_ENTRY    		= "SELECT_OLDEST_ENTRY"; 
	public static final String SELECT_RANDOM_ENTRY			= "SELECT_RANDOM_ENTRY";  
	public static final String SELECT_UNORDERED				= "SELECT_UNORDERED";  
	
	public static final List<String>  GET_NEXT_POLICY_SELECTOR =  
			Arrays.asList( SELECT_MOST_RECENTLY_ADDED, SELECT_OLDEST_ENTRY, SELECT_RANDOM_ENTRY );  

	
	public static final String EQUALS						= "EQUALS";  
	public static final String STARTS_WITH					= "STARTS_WITH";  	
	
	public static final List<String>  APPLICATION_OPERATORS =  
			Arrays.asList( EQUALS, STARTS_WITH);

}
