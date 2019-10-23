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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * IP checks and functions.
 * @author Philip Webb 
 * Written: Australian Winter 2019
 */
public class IpUtilities {
	
	private static final Logger LOG = Logger.getLogger(IpUtilities.class);
	
	public static final String RESTRICT_TO_ONLY_RUN_ON_IPS_LIST = "Restrict_To_Only_Run_On_IPs_List";	

	/**
	 * Designed for use in distributed tests, where a script is only to be executed on given slave(s).  
	 * 
	 * <p>For example, may be helpful where an application administrative functional should only by run on one server
	 * (eg, only a single logon allowed) or for a low-volume function that only needs to run on a few slaves. 
	 * May negate the necessity to use a "If Controller", and/or using -G parameter values is such situations.   
	 * 
	 * <p>Input parameter is expected to be a comma delimited IP address list.  The mark59 framework passes the parameter "Only_Run_on_Slave_IPs_List"
	 * to this method.  If it is not blank, then it is assumed to contain a comma delimited list of IP Addresses.
	 * In that case, the intent is that a script will only execute if it is running on a machine whose IP is in he list
	 * 
	 * <p>Eg: If Only_Run_on_Slave_IPs_List="111.222.333.04,111.222.333.05" then slave with IP 111.222.333.05 will run the script, 
	 * but slave 111.222.333.06 won't   
	 *  
	 * @param commaDelimListOfIPaddresses eg "111.222.333.04,111.222.333.05"
	 * @return localIPnotOnOnlyRunOnSlaveIPsList 
	 */
	public static boolean localIPisNotOnListOfIPaddresses(String commaDelimListOfIPaddresses) {
		boolean isLocalIPisNotOnListOfIPaddresses = false;

		if (StringUtils.isNotBlank(commaDelimListOfIPaddresses)) {
			String localHostIP = getLocalHostIP();
			
			// similar idea as jmeter RemoteStart.getRemoteHosts() 
            StringTokenizer st = new StringTokenizer(commaDelimListOfIPaddresses, ",");
            List<String> listOfIPaddresses = new ArrayList<String>();
            while (st.hasMoreElements()) {
            	listOfIPaddresses.add((String) st.nextElement());
            }
			
			if (!listOfIPaddresses.contains(localHostIP)){
				isLocalIPisNotOnListOfIPaddresses = true;
				LOG.debug("listOfIPaddresses: " + commaDelimListOfIPaddresses + ", localHostIP: " + localHostIP + " not matched to list" );
			}
		}
		return isLocalIPisNotOnListOfIPaddresses;
	}

	public static String getLocalHostIP() {
		String localHostIP = null; 
		try {
			// this should give the same as MeterUtils.getLocalHostIP()
			localHostIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage());
		}
		return localHostIP;
	}	

	
}
