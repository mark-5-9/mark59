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

package com.mark59.datahunter.api.application;

import java.util.Map;
import java.util.Map.Entry;


/**
 * General utilities for DataHunter:
 * 
 * @author Philip Webb
 * Written: Australian Autumn 2022
 *
 */
public class DataHunterUtils  {


	/**
	 * Check if a string is empty.  
	 * 
	 * @param s String being checked  
	 * @return  true if null or blank, otherwise true 
	 */
	public static boolean isEmpty(final String s) {
		return s == null || s.trim().isEmpty();
	}

	
	/**
	 * Convenience method to print out a Map  
	 * 
	 * @param <K> Map entry key 
	 * @param <V> Map entry value
	 * @param map map to pretty print 
	 * @return formatted string representation of the map
	 */
	public static <K,V> String prettyPrintMap (final Map<K,V> map) {
	    String prettyOut = "\n    ------------------------------- ";
	    
	    if (map != null && !map.isEmpty() ){
	    
			for (Entry<K,V> mapEntry: map.entrySet()) {
				prettyOut+= "\n   | " + mapEntry.getKey() + " | " + mapEntry.getValue() + " | " ;
			}
	    } else {
			prettyOut+= "\n   |        empty or null map     | " ;
	    }
	    return prettyOut+= "\n    ------------------------------- \n";	
	}

	
	/**
	 * Convenience method to print out a Map (HTTP formatted) 
	 * 
	 * @param <K> Map entry key 
	 * @param <V> Map entry value
	 * @param map map to pretty print 
	 * @return http (table) formatted representation of the map
	 */
	public static <K,V> String prettyHttpPrintMap (final Map<K,V> map) {
	    String prettyOut = "<br>";
	    
	    if (map != null && !map.isEmpty() ){
	    	prettyOut+= "<table>";
			for (Entry<K,V> mapEntry: map.entrySet()) {
				prettyOut+= "<tr><td>" + mapEntry.getKey() + "</td><td>:</td><td>" + mapEntry.getValue() + " </td><tr>" ;
			}
	    	prettyOut+= "</table>";		
	    } else {
			prettyOut+= "<br> (no sql parameters) " ;
	    }
	    return prettyOut;
	}
	
}