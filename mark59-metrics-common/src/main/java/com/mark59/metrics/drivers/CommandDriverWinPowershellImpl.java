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


/**
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019   
 */
public class CommandDriverWinPowershellImpl implements CommandDriver {
	
	private static final String POWERSHELL_COMMAND  = "powershell -command ";

	public CommandDriverWinPowershellImpl(ServerProfile serverProfile) {
	}
	
	/**
	 * Executes and logs a POWERSHELL command, returning the response.  
	 * @param command  PowerShell command
	 * @return CommandDriverResponse
	 */
	@Override
	public CommandDriverResponse executeCommand(Command command, Map<String, String> cmdParms, boolean testMode) {
		LOG.debug("ps executeCommand :" + command + ", cmdParms=" + cmdParms );
		CommandDriverResponse commandDriverResponse;
		StringSubstitutor parmSubstitutor = new StringSubstitutor(cmdParms);
		
		String runtimeCommand = POWERSHELL_COMMAND + "\"" + parmSubstitutor.replace(command.getCommand()).replaceAll("\\R", "") + "\"";
		
		String runtimeCommandForLog = POWERSHELL_COMMAND + "\"" + parmSubstitutor
				.replace(command.getCommand().replace(MetricsConstants.PROFILE_PASSWORD_VAR, "********")).replaceAll("\\R", " ") + "\"";
			
		commandDriverResponse = CommandDriver.executeRuntimeCommand(runtimeCommand, command.getIngoreStderr(),CommandExecutorDatatypes.POWERSHELL_WINDOWS);
		commandDriverResponse.setCommandLog(CommandDriver.logExecution(runtimeCommandForLog, command.getIngoreStderr(),
				commandDriverResponse.getRawCommandResponseLines(), testMode));
		return commandDriverResponse;
	}

}
