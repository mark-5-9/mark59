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

package com.mark59.metrics.drivers;

import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.mark59.metrics.data.beans.Command;
import com.mark59.metrics.data.beans.ServerProfile;
import com.mark59.metrics.pojos.CommandDriverResponse;
import com.mark59.metrics.utils.MetricsConstants;
import com.mark59.metrics.utils.MetricsConstants.CommandExecutorDatatypes;
import com.mark59.metrics.utils.MetricsUtils;


/**
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019   
 */
public class CommandDriverWinWmicImpl implements CommandDriver {

	public static final String WMIC_DIR = MetricsUtils.wmicExecutableDirectory();
	
	private static final String WMIC_LOCAL_SWITCHES  = "WMIC /node:localhost ";
	
	private static final String WMIC_REMOTE_SWITCHES = "WMIC" 
		+ " /user: ${" + MetricsConstants.PROFILE_USERNAME + "}"
		+ " /password: <PWD_PLACEHOLDER>"
		+ " /node: ${" + MetricsConstants.PROFILE_SERVER + "} ";

	private final ServerProfile serverProfile;

	public CommandDriverWinWmicImpl(ServerProfile serverProfile) {
		this.serverProfile = serverProfile;
	}
	
	
	/**
	 * Executes and logs WMI Commands, returning the response  
	 * @param command  WMI Command
	 * @return CommandDriverResponse
	 */
	@Override
	public CommandDriverResponse executeCommand(Command command, Map<String, String> cmdParms, boolean testMode) {
		LOG.debug("wmic executeCommand :" + command + ", cmdParms=" + cmdParms );
		CommandDriverResponse commandDriverResponse;
		StringSubstitutor parmSubstitutor = new StringSubstitutor(cmdParms);		
		String runtimeCommand;
		String runtimeCommandForLog;
		
		if ("localhost".equalsIgnoreCase(serverProfile.getServer())) {
			runtimeCommand = WMIC_DIR + WMIC_LOCAL_SWITCHES + parmSubstitutor.replace(command.getCommand()).replaceAll("\\R", " ");
			runtimeCommandForLog = runtimeCommand;

		} else {
			
			runtimeCommand = WMIC_DIR
				+ parmSubstitutor
					.replace(WMIC_REMOTE_SWITCHES.replace("<PWD_PLACEHOLDER>",MetricsUtils.actualPwd(serverProfile)) + command.getCommand())
					.replaceAll("\\R", " ");
			
			runtimeCommandForLog = WMIC_DIR
				+ parmSubstitutor
					.replace(WMIC_REMOTE_SWITCHES.replace("<PWD_PLACEHOLDER>", "********") + command.getCommand())
					.replaceAll("\\R", " ");
		}

		commandDriverResponse = CommandDriver.executeRuntimeCommand(runtimeCommand, command.getIngoreStderr(),CommandExecutorDatatypes.WMIC_WINDOWS);
		commandDriverResponse.setCommandLog(CommandDriver.logExecution(runtimeCommandForLog, command.getIngoreStderr(),
				commandDriverResponse.getRawCommandResponseLines(), testMode));
		return commandDriverResponse;
	}

}
