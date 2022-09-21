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

package com.mark59.metrics.drivers;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.mark59.core.utils.SimpleAES;
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

	
	private static final String WMIC_COMMAND_REMOTE_FORMAT = "WMIC /user:{0} /password:{1} /node:{2} {3}";
	private static final String WMIC_COMMAND_LOCAL_FORMAT  = "WMIC /node:localhost {0}";
	
	
	private static final String CHALLENGE_REPLACE_REGEX = "(^.*password:).*?(\\s.*$)";
	private static final String CHALLENGE_REPLACE_VALUE = "$1********$2";

	public static final String WMIC_DIR = MetricsUtils.wmicExecutableDirectory();
	
	
	private final ServerProfile serverProfile;
	

	public CommandDriverWinWmicImpl(ServerProfile serverProfile) {
		this.serverProfile = serverProfile;
	}
	
	
	/**
	 * Executes the DOS command via WMIC returning the response  
	 * @param command  command
	 * @return CommandDriverResponse
	 */
	@Override
	public CommandDriverResponse executeCommand(Command command) {
		LOG.debug("executeCommand :" + command);
		String actualPassword; 
		String cipherUsedLog = " (local execution)";
		CommandDriverResponse commandDriverResponse;
		String runtimeCommand;
		String runtimeCommandForLog;
		
		if ("localhost".equalsIgnoreCase(serverProfile.getServer())) {
			runtimeCommand = WMIC_DIR + MessageFormat.format(WMIC_COMMAND_LOCAL_FORMAT, command.getCommand().replaceAll("\\R", " "));
			if (System.getProperty(MetricsConstants.METRICS_BASE_DIR) != null ) {
				runtimeCommand = runtimeCommand
						.replace("%"+MetricsConstants.METRICS_BASE_DIR+"%", System.getProperty(MetricsConstants.METRICS_BASE_DIR));
			}
			runtimeCommandForLog = runtimeCommand;

		} else {
			
			if (StringUtils.isBlank(serverProfile.getPasswordCipher())){
				actualPassword = serverProfile.getPassword();
				cipherUsedLog = " user " + serverProfile.getUsername() + " (not enciphered)"; 
			} else {
				cipherUsedLog = " (pwd chipher used)" ;
				try {
					actualPassword = SimpleAES.decrypt(serverProfile.getPasswordCipher());
				} catch (Exception e) {
					commandDriverResponse = new CommandDriverResponse();
					commandDriverResponse.setRawCommandResponseLines(new ArrayList<>());
					commandDriverResponse.setCommandLog("<br>" + cipherUsedLog + "<br>pwd decryption error: " + e.getMessage() + "<br>");			
					commandDriverResponse.setCommandFailure(true);
					return commandDriverResponse;
				}

			}
			runtimeCommand = WMIC_DIR + MessageFormat.format(WMIC_COMMAND_REMOTE_FORMAT, serverProfile.getUsername(), actualPassword, serverProfile.getServer(), command.getCommand().replaceAll("\\R", " ") );
			runtimeCommandForLog = runtimeCommand.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE).replaceAll("\\R", "") + "</font>";
		}

		commandDriverResponse = CommandDriver.executeRuntimeCommand(runtimeCommand, command.getIngoreStderr(),CommandExecutorDatatypes.WMIC_WINDOWS);
		
		commandDriverResponse.setCommandLog(
				CommandDriver.logExecution(cipherUsedLog, runtimeCommandForLog, command.getIngoreStderr(), commandDriverResponse.getRawCommandResponseLines()));

		return commandDriverResponse;
	}

}
