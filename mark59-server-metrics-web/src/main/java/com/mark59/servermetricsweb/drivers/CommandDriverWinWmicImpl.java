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

package com.mark59.servermetricsweb.drivers;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.SimpleAES;
import com.mark59.servermetricsweb.data.beans.Command;
import com.mark59.servermetricsweb.data.beans.ServerProfile;
import com.mark59.servermetricsweb.pojos.CommandDriverResponse;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb;
import com.mark59.servermetricsweb.utils.ServerMetricsWebUtils;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb.CommandExecutorDatatypes;


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

	public static final String WMIC_DIR = ServerMetricsWebUtils.wmicExecutableDirectory();
	
	
	private ServerProfile serverProfile;
	

	public CommandDriverWinWmicImpl(ServerProfile serverProfile) {
		this.serverProfile = serverProfile;
	}
	
	
	/**
	 * Executes the DOS command via WMIC returning the response  
	 * @param command
	 * @return CommandDriverResponse
	 */
	@Override
	public CommandDriverResponse executeCommand(Command command) {
		LOG.debug("executeCommand :" + command);
		
		String actualPassword = serverProfile.getPassword();
		String runtimeCommand = "";
		String runtimeCommandLog = "";
		
		String cipherUsedLog = " (no pwd chipher)"; 
		if (StringUtils.isNotBlank(serverProfile.getPasswordCipher())){
			actualPassword = SimpleAES.decrypt(serverProfile.getPasswordCipher());
			cipherUsedLog = " (pwd chipher used)" ;
		} 			
		
		if ("localhost".equalsIgnoreCase(serverProfile.getServer())) {
			runtimeCommand = WMIC_DIR + MessageFormat.format(WMIC_COMMAND_LOCAL_FORMAT, command.getCommand().replaceAll("\\R", " "));
			if (System.getProperty(AppConstantsServerMetricsWeb.SERVER_METRICS_WEB_BASE_DIR) != null ) {
				runtimeCommand = runtimeCommand
						.replace("%"+AppConstantsServerMetricsWeb.SERVER_METRICS_WEB_BASE_DIR+"%", System.getProperty(AppConstantsServerMetricsWeb.SERVER_METRICS_WEB_BASE_DIR));
			}
					
			runtimeCommandLog = " :<br><font face='Courier'>" + runtimeCommand.replaceAll("\\R", " ")  + "</font>";
			cipherUsedLog = " (local execution)";
		} else {
			runtimeCommand = WMIC_DIR + MessageFormat.format(WMIC_COMMAND_REMOTE_FORMAT, serverProfile.getUsername(), actualPassword, serverProfile.getServer(), command.getCommand().replaceAll("\\R", " ") );
			runtimeCommandLog = ": <br><font face='Courier'>" + runtimeCommand.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE).replaceAll("\\R", "") + "</font>";
		}

		String IgnoreStdErrLog = "";
		if(Mark59Utils.resovesToTrue(command.getIngoreStderr())){
			IgnoreStdErrLog = ". StdErr to be ignored. ";
		}

		String commandLog = cipherUsedLog + IgnoreStdErrLog + runtimeCommandLog; 
		
		CommandDriverResponse commandDriverResponse = CommandDriver.executeRuntimeCommand(runtimeCommand, command.getIngoreStderr(),CommandExecutorDatatypes.WMIC_WINDOWS);
		
		commandLog += "<br>Response :<br><font face='Courier'>" 
					+ commandDriverResponse.getCommandLog()
					+ String.join("<br>", commandDriverResponse.getRawCommandResponseLines()) + "</font><br>";

		commandDriverResponse.setCommandLog(commandLog);
		return commandDriverResponse;
	}

}
