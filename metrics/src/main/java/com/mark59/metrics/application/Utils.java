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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.mark59.metrics.data.beans.EventMapping;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class Utils  {

	
	
	public static String stringListToCommaDelimString(List<String> listOfStrings) {
		StringBuilder commaDelimitedsb = new StringBuilder();
		boolean firstTimeThruNoComma = true;
		for (String runDate : listOfStrings) {
			if (!firstTimeThruNoComma ){ 
				commaDelimitedsb.append(",");
			}
			firstTimeThruNoComma = false;
			commaDelimitedsb.append(runDate); 
		}
		return commaDelimitedsb.toString();
	}


	public static List<String> commaDelimStringToStringList(String commaDelimitedString) {
		List<String> listOfStrings = new ArrayList<String>();
		
		// when an empty string is passed to the split, it creates a empty first element ... not what we want .. 
		if ( ! (commaDelimitedString == null || commaDelimitedString.isEmpty() )){
			listOfStrings = Arrays.asList(commaDelimitedString.split("\\s*,\\s*"));
		} 
		return listOfStrings;
	}
	
	
	public static String[] commaDelimStringToSortedStringArray(String commaDelimitedString, Comparator<Object> comparator) {
		
		// when an empty string is passed to the split, it creates a empty first element ... not what we want .. 
		if ( ! (commaDelimitedString == null || commaDelimitedString.isEmpty() )){
			String[] strings = commaDelimitedString.split("\\s*,\\s*");
			Arrays.sort(strings, comparator);
			return strings;
		} else {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
	}
	


	public static String deriveEventTxnIdUsingEventMappingBoundaryRules(String sourceTxnId,	EventMapping eventMapping) {
		String txnId = null;
		if ( StringUtils.isBlank(eventMapping.getTargetNameLB()) && StringUtils.isBlank(eventMapping.getTargetNameRB()) ){
			txnId =  sourceTxnId;		
		} else	if ( StringUtils.isBlank(eventMapping.getTargetNameLB())){
			txnId =  StringUtils.substringBefore(sourceTxnId, eventMapping.getTargetNameRB());
		} else if ( StringUtils.isBlank(eventMapping.getTargetNameRB())){
			txnId =  StringUtils.substringAfterLast(sourceTxnId, eventMapping.getTargetNameLB());
		} else {
			txnId =  StringUtils.substringBetween(sourceTxnId, eventMapping.getTargetNameLB(), eventMapping.getTargetNameRB() );
//			System.out.println(" StringUtils.substringBetween('" + eventName + "', '" + eventMapping.getTargetNameLB() + "', '" + eventMapping.getTargetNameRB() + "' ) = " +  txnName     );
		}
//		System.out.println("deriveEventTxnNameUsingEventMappingBoundaryRules : using eventName  " + eventName + ", got txnName=" + txnName + "." );
		return txnId;
	}	

	// https://stackoverflow.com/questions/10664434/escaping-special-characters-in-java-regular-expressions
	
	public static String escapeSpecialRegexChars(String str) {
	    return Pattern.compile("[{}()\\[\\].+*?^$\\\\|]").matcher(str).replaceAll("\\\\$0");
	}	
	
	
	public static String defaultIfNull(String value, String defaultValue ) {
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}	

	public static String defaultIfBlank(String value, String defaultValue ) {
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		} else {
			return value;
		}
	}	
	
	public static List<String> listMappedDataTypes() {
		List<String> listOfMappedDataTypes = new ArrayList<String>();
		for (AppConstants.MAPPED_DATA_TYPES txnTypeEnum : (AppConstants.MAPPED_DATA_TYPES.values() )) {
			listOfMappedDataTypes.add(txnTypeEnum.name() );
		}
		return listOfMappedDataTypes;
	}		

	public static List<String> listMappedMetricDataTypes() {
		List<String> listOfMappedMetricDataTypes = new ArrayList<String>();
		for (AppConstants.MAPPED_DATA_TYPES txnTypeEnum : (AppConstants.MAPPED_DATA_TYPES.values() )) {
			if ( txnTypeEnum.isMetricDataType()){
				listOfMappedMetricDataTypes.add(txnTypeEnum.name());
			}
		}
		return listOfMappedMetricDataTypes;
	}	

	

	public static String decodeBase64urlParam(String base64urlEncodedString) {
//		System.out.println("at Utils.decodeBase64urlParam :" + base64urlEncodedString );
		byte[] decodedByte = null;
	
		try {
			decodedByte = Base64.getDecoder().decode(base64urlEncodedString);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("An attempt to decode a base64 (and uri encoded) parameter has failed for : " + base64urlEncodedString )	;	
		}
		String urlEncodedString = new String(decodedByte);
		
		String decodedString = "URL encoding decoding failure!!";
		try {
			decodedString = java.net.URLDecoder.decode(urlEncodedString, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("An attempt to decode a uri encoded parameter has failed for : " + urlEncodedString  )	;	
		}
		return decodedString;
	}
  
	
	
}