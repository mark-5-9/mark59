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

package com.mark59.trends.application;

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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.mark59.core.utils.Mark59Utils;
import com.mark59.trends.data.beans.EventMapping;
import com.mark59.trends.data.beans.Transaction;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class UtilsTrends  {


	public static String stringListToCommaDelimString(List<String> listOfStrings) {
		StringBuilder commaDelimitedsb = new StringBuilder();
		boolean firstTimeThruNoComma = true;
		for (String str : listOfStrings) {
			if (!firstTimeThruNoComma ){ 
				commaDelimitedsb.append(",");
			}
			firstTimeThruNoComma = false;
			commaDelimitedsb.append(str); 
		}
		return commaDelimitedsb.toString();
	}


	public static List<String> commaDelimStringToStringList(String commaDelimitedString) {
		List<String> listOfStrings = new ArrayList<>();
		if (StringUtils.isNotBlank(commaDelimitedString)){
			listOfStrings =  Arrays.asList(StringUtils.stripAll(StringUtils.split(commaDelimitedString, ",")));
		} 
		return listOfStrings;
	}
	
	
	public static String[] commaDelimStringToSortedStringArray(String commaDelimitedString, Comparator<Object> comparator) {
		// when an empty string is passed to the split, it creates a empty first element ... not what we want .. 
		if (StringUtils.isNotBlank(commaDelimitedString)){
			String[] strings = StringUtils.stripAll(StringUtils.split(commaDelimitedString, ","));
			Arrays.sort(strings, comparator);
			return strings;
		} else {
			return ArrayUtils.EMPTY_STRING_ARRAY;
		}
	}
	


	public static String deriveEventTxnIdUsingEventMappingBoundaryRules(String sourceTxnId,	EventMapping eventMapping) {
		String txnId;
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
	
	

	public static String decodeBase64urlParam(String base64urlEncodedString) {
//		System.out.println("at UtilsMetrics.decodeBase64urlParam :" + base64urlEncodedString );
		byte[] decodedByte;
	
		try {
			decodedByte = Base64.getDecoder().decode(base64urlEncodedString);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("An attempt to decode a base64 (and uri encoded) parameter has failed for : " + base64urlEncodedString )	;	
		}
		String urlEncodedString = new String(decodedByte);
		
		String decodedString;
		try {
			decodedString = java.net.URLDecoder.decode(urlEncodedString, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("An attempt to decode a uri encoded parameter has failed for : " + urlEncodedString  )	;	
		}
		return decodedString;
	}
  
	
	public static String encodeBase64urlParam(String plainString) {
//		System.out.println("at UtilsMetrics.encodeBase64urlParam :" + plainString );
		String URLencodedString;
		try {
			URLencodedString = java.net.URLEncoder.encode(plainString, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException("An attempt to decode a uri encoded parameter has failed for : " + plainString  )	;	
		}

		byte[] base64urlEncodeByte;
		try {
			base64urlEncodeByte = Base64.getEncoder().encode(URLencodedString.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("An attempt to encode base64 (and uri encoded) parameter has failed for : " + plainString )	;	
		}

		return new String(base64urlEncodeByte);
	}

	 
	/**
	 * 'tag' selected transactions, as they will be named on the trend analysis graphs.  That, is a A CDP transaction
	 * will be suffixed with a CDP tag.  This method does not invoke a database call.
	 * 
	 * <p>Assumes the transactions have already been selected for graphing
	 *  (EG using  TransactionDAO.returnListOfTransactionsToGraph)
	 * 
	 * <p>Note: The supplied database installs (DDL) will put the lists is in UTF-8 order (h2 default, MySQL utf8mb4_0900_bin collation)
	 * This should be pretty close to the ordering used by Java, but to avoid any  potential for issues for odd non-standard chars or
	 * a different collation being chosen on a database, and to cater for the tagged CDP entry on transactions 
	 * the returned list is re-ordered in the natural order of Java sorting using the tagged transactions names.
	 * 
	 * <p>This is Important as this list is supplied to the graph datapoints creation routine where a string compareTo is done.  The datapoints
	 * would be out of sequence and the graph could fail if the compareTo did not work properly because the list was out of natural Java order.  
	 */
	public static List<Transaction> returnOrderedListOfTransactionsToGraphTagged(List<Transaction> listOfTransactionsToGraph){

		List<Transaction> listOfTransactionsToGraphTagged = new ArrayList<>();
		for (Transaction transaction : listOfTransactionsToGraph) {
			Transaction graphedTransaction = new Transaction(transaction);
			if ("Y".equalsIgnoreCase(transaction.getIsCdpTxn())){
				graphedTransaction.setTxnId(transaction.getTxnId() + AppConstantsMetrics.CDP_TAG);  
				// txnIdURLencoded may be needed to be set in future 
			}
			listOfTransactionsToGraphTagged.add(graphedTransaction);
		} 

		// sort the transaction list using the transaction names to be graphed 
		listOfTransactionsToGraphTagged.sort(Comparator.comparing(Transaction::getTxnId));
		return listOfTransactionsToGraphTagged;
	}


	/**
	 * Extract transaction names from a list of transactions. 
	 * 
	 * @param transactions transactions list
	 * @return listOfTxnIdsd (in natural order)
	 */
	public static List<String> returnListOfTxnIdsdFromListOfTransactions(List<Transaction> transactions){
		// alt:	transactions.stream().map(transaction -> transaction.getTxnId()).collect(Collectors.toList());
		List<String> listOfTxnIds = new ArrayList<>();
		for (Transaction transaction : transactions) {
			listOfTxnIds.add(transaction.getTxnId());
		}
		return listOfTxnIds;
	}
	


	/**
	 * Filter a list of Transactions by CDP, returning a list of txnids 
	 * 
	 * @param transactions  transactions list
	 * @param isCdpTxnFilter CDP filter
	 * @return listOfTxnIds
	 */
	public static List<String> returnFilteredListOfTransactionNamesToGraph(List<Transaction> transactions, String isCdpTxnFilter){
		// alt:	transactions.stream().map(transaction -> transaction.getTxnId()).collect(Collectors.toList());
		List<String> listOfTxnIds = new ArrayList<>();
		for (Transaction transaction : transactions) {
			if (isCdpTxnFilter.equalsIgnoreCase(transaction.getIsCdpTxn() )) {
				listOfTxnIds.add(transaction.getTxnId());
			}
		}
		return listOfTxnIds;
	}
	

	public static String removeCdpTags(String cdpTaggedTransactionString) {
		if (StringUtils.isNotBlank(cdpTaggedTransactionString)) {
			cdpTaggedTransactionString = cdpTaggedTransactionString.replace(AppConstantsMetrics.CDP_TAG, ""); 
		}
		return cdpTaggedTransactionString;
	}

	/**
	 * @param sqlparameters  the parameters used in a NamedParameterJdbcTemplate 
	 * @return formatted string representation of the parameter map
	 */
	public static String prettyPrintParms(MapSqlParameterSource sqlparameters) {
	    return Mark59Utils.prettyPrintMap(sqlparameters.getValues());	
	}
	
}