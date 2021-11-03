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

package com.mark59.servermetricsweb.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class AppConstantsServerMetricsWeb {
	
	public static final String MARK59_SERVER_METRICS_VERSION = "4.0.0";	
	
	public static final String MARK59_SERVER_PROFILES_EXCEL_FILE = "mark59serverprofiles.xlsx";  

	public static final String SERVER_PROFILE_NOT_FOUND  = "SERVER_PROFILE_NOT_FOUND";	
	
	public static final String SERVER_METRICS_WEB_BASE_DIR 	= "SERVER_METRICS_WEB_BASE_DIR"; 
	
	
	public static enum OS {
		WINDOWS("WINDOWS"), LINUX("LINUX"), UNIX("UNIX"), UNKNOWN("UNKNOWN");

		private String osName;
		OS(String osName) {	this.osName = osName;}
		public String getOsName() {return osName;}
	}
	
	
	public static enum CommandExecutorDatatypes {
		WMIC_WINDOWS("WMIC_WINDOWS"), SSH_LINIX_UNIX("SSH_LINIX_UNIX"), GROOVY_SCRIPT("GROOVY_SCRIPT");

		private String executorText;
		CommandExecutorDatatypes(String executorText) {this.executorText = executorText;}
		public String getExecutorText() {return executorText;}
		
		public static List<String> listOfCommandExecutorDatatypes(){
			List<String> listOfCommandExecutorDatatypes = new ArrayList<String>();
			for (CommandExecutorDatatypes commandExecutorDatatypes : (CommandExecutorDatatypes.values())) {
				listOfCommandExecutorDatatypes.add(commandExecutorDatatypes.name());
			}
			return listOfCommandExecutorDatatypes;
		}
	}
	
	
	
    private AppConstantsServerMetricsWeb() {
    }
}
