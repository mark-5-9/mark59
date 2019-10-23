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

package com.mark59.metrics.sla;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SlaUtilities {

	
	public static String listOfIgnoredTransactionsSQL(String graphApplication) {
		
		return " SELECT TXN_ID FROM SLA WHERE SLA_APPLICATION_KEY = '" + graphApplication + "' AND IS_TXN_IGNORED = 'Y' ";   
	}	
	
	
	/*
	 * Get the test application 'default SLA', and apply that if a transaction does not have an explicit SLA - which is assumed to be "-", then the application-id followed by "-DEFAULT-SLA-". 
	 * eg if the application is named  "APPUNDERTEST", the 'default SLA' txn Id will be "-APPUNDERTEST-DEFAULT-SLA-   
	 */
	public static String deriveDefaultSLAtransactionId(String graphApplication) {
		String defaultSlaTxnId = "-" + graphApplication + "-DEFAULT-SLA-";
//		System.out.println( "SlaUtilitiesdefaultSlaAppId: SLA Generic txn KEY being used for this run is " +  defaultSlaAppId );

		return defaultSlaTxnId;
	}	

	
	
	
	
}
