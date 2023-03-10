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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.SimpleAES;
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
public class CommandDriverNixSshImpl implements CommandDriver {

	private static final Logger LOG = LogManager.getLogger(CommandDriverNixSshImpl.class);	

	private final ServerProfile serverProfile;
	private Session sesConnection = null;

	public CommandDriverNixSshImpl(ServerProfile serverProfile) {
		this.serverProfile = serverProfile;
	}
	
	
	/**
	 * Executes the command on the requested server via SSHn 
	 * @param command Command
	 * @return CommandDriverResponse
	 */
	@Override
	public CommandDriverResponse executeCommand(Command command) {
		LOG.debug("executeCommand :" + command);
		String actualPassword; 
		String cipherUsedLog = " (local execution)";		
		CommandDriverResponse commandDriverResponse;
		String runtimeCommand = command.getCommand().replaceAll("\\R", "\n");

		if ("localhost".equalsIgnoreCase(serverProfile.getServer())) {
			commandDriverResponse = CommandDriver.executeRuntimeCommand(runtimeCommand, command.getIngoreStderr(), CommandExecutorDatatypes.SSH_LINUX_UNIX );
		
		} else {
			
			if (StringUtils.isBlank(serverProfile.getPasswordCipher())){
				actualPassword = serverProfile.getPassword();
				if (MetricsConstants.KERBEROS.equals(actualPassword)){
					cipherUsedLog = " (connection via Kerberos)"; 
				} else {
					cipherUsedLog = " user " + serverProfile.getUsername() + " (not enciphered)"; 
				}
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

			try {
				connect(serverProfile, actualPassword);
			} catch (Exception e) {
				commandDriverResponse = new CommandDriverResponse();
				commandDriverResponse.setRawCommandResponseLines(new ArrayList<>());
				commandDriverResponse.setCommandLog("<br>" + cipherUsedLog + "<br>Connection failure: " + e.getMessage() + "<br>");			
				commandDriverResponse.setCommandFailure(true);
				return commandDriverResponse;
			}
			
			commandDriverResponse = executeRemoteNixSystemCommand(runtimeCommand, command.getIngoreStderr());
		}
		
		commandDriverResponse.setCommandLog(
				CommandDriver.logExecution(cipherUsedLog, runtimeCommand, command.getIngoreStderr(), commandDriverResponse.getRawCommandResponseLines()));
		
		//System.out.println("CommandLog at end:" + commandDriverResponse.getCommandLog()  );
		
		return commandDriverResponse;
	}

	
	/**
	 * The PreferredAuthentication configuration, removing Kerberos authentication (the default), as  
	 * Kerberos can hang the client if it is installed. 
	 * see  https://stackoverflow.com/questions/10881981/sftp-connection-through-java-asking-for-weird-authentication.
	 * 
	 * @param serverProfile the server profile
	 * @param actualPassword the actual password
	 */
	private void connect(ServerProfile serverProfile, String actualPassword) {
		try {
			sesConnection = new JSch().getSession(serverProfile.getUsername(), serverProfile.getServer(), Integer.parseInt(serverProfile.getConnectionPort()));
			sesConnection.setPassword(actualPassword);
			Properties connectConfig = new java.util.Properties();
			connectConfig.put("StrictHostKeyChecking", "no");
			if (MetricsConstants.KERBEROS.equals(actualPassword)){
				connectConfig.put("PreferredAuthentications", "gssapi-with-mic");				
			} else {
				connectConfig.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
			}
			sesConnection.setConfig(connectConfig);
			sesConnection.connect(Integer.parseInt(serverProfile.getConnectionTimeout()));
		} catch (JSchException jschX) {
			try {sesConnection.disconnect();} catch (Exception ignored){}
			throw new RuntimeException(jschX.getMessage());
		}
	}	

	
	private CommandDriverResponse executeRemoteNixSystemCommand(String runtimeCommand, String ingoreStderr) {
		LOG.debug( "executeRemoteNixSystemCommand : " + runtimeCommand );
		
		CommandDriverResponse commandDriverResponse = new CommandDriverResponse();   
		commandDriverResponse.setCommandFailure(false);
		List<String> rawCommandResponseLines = new ArrayList<>();
		commandDriverResponse.setCommandLog("");
		ChannelExec channelExec = null;
		
		try {		
		
			channelExec = (ChannelExec) sesConnection.openChannel("exec");
			channelExec.setCommand(runtimeCommand);
			InputStream commandResponseStream = channelExec.getInputStream();
			InputStream commandErrStream      = channelExec.getErrStream();
	
			channelExec.connect();               // its actually command execution
							
			if ( ! Mark59Utils.resolvesToTrue(ingoreStderr)){
				rawCommandResponseLines = readCommandInputStream(commandErrStream);
				if (rawCommandResponseLines.size() > 0  ) {
					commandDriverResponse.setCommandFailure(true);
				}
			} 			
			
			rawCommandResponseLines.addAll(readCommandInputStream(commandResponseStream));
			
			channelExec.getSession().disconnect();
			channelExec.disconnect();
			
		} catch (Exception e) {
			try {Objects.requireNonNull(channelExec).getSession().disconnect();channelExec.disconnect();} catch (Exception ignored){}
			commandDriverResponse.setCommandFailure(true);	
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			rawCommandResponseLines.add("<br>Command Failure: " + e.getMessage() + "<br>" + stackTrace.toString() + "<br>");
		}

		commandDriverResponse.setRawCommandResponseLines(rawCommandResponseLines);
		LOG.debug("commandDriverResponse: " + commandDriverResponse);
		return commandDriverResponse;
	}

	
	/**
	 * @param commandStream   input stream holding the command
	 * @return rawCommandResponseLines  line(s) containing the command response
	 * @throws IOException   IOException
	 * @throws InterruptedException  InterruptedException
	 */
	private List<String> readCommandInputStream(InputStream commandStream)	throws IOException, InterruptedException {
		List<String> rawCommandResponseLines = new ArrayList<>();
		StringBuilder outputBuffer = new StringBuilder();
		int readByte = commandStream.read();

		while (commandStream.available() > 0) {
			while (readByte != 0xffffffff) {
				if ( (char)readByte == '\n' ){
					rawCommandResponseLines.add(outputBuffer.toString());
					outputBuffer = new StringBuilder();
				} else {
					outputBuffer.append((char) readByte);
				}
				readByte = commandStream.read();
			}
			Thread.sleep(1000);
		}
		if (StringUtils.isNotBlank(outputBuffer.toString())){
			rawCommandResponseLines.add(outputBuffer.toString());
		}
		return rawCommandResponseLines;
	}

}