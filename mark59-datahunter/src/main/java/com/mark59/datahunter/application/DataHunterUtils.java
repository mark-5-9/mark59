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

package com.mark59.datahunter.application;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;



public class DataHunterUtils  {

	public static void expireSession(HttpServletRequest httpServletRequest) {
		if (httpServletRequest != null){
			expireSession( httpServletRequest, 10);
		}
	}
	
	public static void expireSession(HttpServletRequest httpServletRequest, int intervalinSecs) {
		HttpSession httpSession = httpServletRequest.getSession(false); 
		if (httpSession != null){
			httpSession.setMaxInactiveInterval(intervalinSecs);
		}
	}

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
	 * Convenience method to print out a Map  
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
			prettyOut+= "<br> (no sql paramters) " ;
	    }
	    return prettyOut;
	}
	
	
	/**
	 * returns the given list with empty values and white spaces trimmed off
	 * 
	 * <p>EG: ", ,,cat 1, ,, mat 2, , ,," returns "cat 1,mat 2"
	 * 
	 * @param commaDelimitedString a list of comma delimited strings (can be a single value)
	 * @return the commaDelimitedString but with whitespace stripped from the start and end of every string 
	 */
	public static String commaDelimStringTrimAll(String commaDelimitedString) {
		String trimmedStrings = "";
		// note when an empty string is passed to the split, it creates a empty first element ... 
		if (StringUtils.isNotBlank(commaDelimitedString)){
			String[] strippedStringAry = StringUtils.stripAll(StringUtils.split(commaDelimitedString, ","));
			for (String strippedString : strippedStringAry){
				if (StringUtils.isNotBlank(strippedString)){
					trimmedStrings = trimmedStrings + strippedString + ",";
				}
			}
			trimmedStrings = StringUtils.stripEnd(trimmedStrings, ",");
		}	
		return trimmedStrings;
	}
	
	
	/**
	 * @param commaDelimitedString string with comma(",") being used as a field delimiter 
	 * @return the Set of split strings 
	 */
	public static Set<String> commaDelimStringToStringSet(String commaDelimitedString) {
		List<String> listOfStrings = new ArrayList<>();
		if ( StringUtils.isNotBlank(commaDelimitedString)){
			listOfStrings =  Arrays.asList(StringUtils.stripAll(StringUtils.split(commaDelimitedString, ",")));
		}
		return new HashSet<>(listOfStrings);
	}
	
	
	/**
	 * @param uri Parameter to encode
	 * @return encoded parameter 
	 */
	public static String encode(String uriParm) {
		try {
			return URLEncoder.encode(nullToEmpty(uriParm), StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("UnsupportedEncodingException using url : " + uriParm );
		}
	}
	
	
	private static String nullToEmpty(String str) {
		return null == str ? "" : str;
	}
	
}