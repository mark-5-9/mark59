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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import com.mark59.metrics.utils.AppConstantsServerMetricsWeb.CommandExecutorDatatypes;


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

		String actualPassword = serverProfile.getPassword();
		String cipherUsedLog = " user " + serverProfile.getUsername() + " (" + actualPassword + " )"; 
		if (StringUtils.isNotBlank(serverProfile.getPasswordCipher())){
			actualPassword = SimpleAES.decrypt(serverProfile.getPasswordCipher());
			cipherUsedLog = "  user " + serverProfile.getUsername() + " (pwd chipher used)" ;
		} 			
		if ("localhost".equalsIgnoreCase(serverProfile.getServer())) {
			cipherUsedLog = " (local execution)";
		} 
		
		String IgnoreStdErrLog = "";
		if(Mark59Utils.resolvesToTrue(command.getIngoreStderr())){
			IgnoreStdErrLog = ". StdErr to be ignored. ";
		}

		String commandLog = cipherUsedLog + IgnoreStdErrLog + " :<br><font face='Courier'>" + command.getCommand().replaceAll("\\R", "<br>") + "</font>";

		CommandDriverResponse commandDriverResponse;
		if ("localhost".equalsIgnoreCase(serverProfile.getServer())) {
			commandDriverResponse = CommandDriver.executeRuntimeCommand(command.getCommand().replaceAll("\\R", "\n")  , command.getIngoreStderr(), CommandExecutorDatatypes.SSH_LINUX_UNIX );
		} else {
			commandDriverResponse = connect(serverProfile, actualPassword);
			if (sesConnection != null) {                                                                      
				commandDriverResponse = executeRemoteNixSystemCommand(command.getCommand().replaceAll("\\R", "\n")  , command.getIngoreStderr());
			} 
		}
		
		commandLog += "<br>Response :<br><font face='Courier'>" 
				+ commandDriverResponse.getCommandLog()
				+ String.join("<br>", commandDriverResponse.getRawCommandResponseLines()).replace(" ", "&nbsp;") + "</font><br>";
		
		commandDriverResponse.setCommandLog(commandLog);
		return commandDriverResponse;
	}

	
	
	private CommandDriverResponse connect(ServerProfile serverProfile, String actualPassword) {
		CommandDriverResponse commandDriverResponse = new CommandDriverResponse();
		try {
			sesConnection = new JSch().getSession(serverProfile.getUsername(), serverProfile.getServer(), Integer.parseInt(serverProfile.getConnectionPort()));
			sesConnection.setPassword(actualPassword);
			sesConnection.setConfig("StrictHostKeyChecking", "no");
			sesConnection.connect(Integer.parseInt(serverProfile.getConnectionTimeout()));
		} catch (JSchException jschX) {
			try {sesConnection.disconnect();} catch (Exception ignored){}
			commandDriverResponse.setRawCommandResponseLines(new ArrayList<>());
			commandDriverResponse.setCommandLog("<br>A failure has occured attempting to connect : " + jschX.getMessage() + "<br>");			
			commandDriverResponse.setCommandFailure(true);
			sesConnection = null;
			LOG.warn("A failure has occured attempting to connect. Server profile : " + serverProfile );
		}
		return commandDriverResponse;
	}	

	

	private CommandDriverResponse executeRemoteNixSystemCommand(String runtimeCommand, String ingoreStderr) {
		LOG.debug( "executeRemoteNixSystemCommand : " + runtimeCommand );
		
		CommandDriverResponse commandDriverResponse = new CommandDriverResponse();   
		commandDriverResponse.setCommandFailure(false);
		List<String> rawCommandResponseLines = new ArrayList<>();
		String commandLog = "";
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
			
			rawCommandResponseLines.addAll( readCommandInputStream(commandResponseStream) );
			
			channelExec.getSession().disconnect();
			channelExec.disconnect();
			
		} catch (Exception e) {
			try {Objects.requireNonNull(channelExec).getSession().disconnect();channelExec.disconnect();} catch (Exception ignored){}
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			commandLog += "<br>A faiure has occured attempting to execute the command : " + e.getMessage() + "<br>" + stackTrace.toString() + "<br>";			
			LOG.debug("Command failure : " + runtimeCommand + ":\n" + e.getMessage() + stackTrace.toString());
		}

		commandDriverResponse.setRawCommandResponseLines(rawCommandResponseLines);
		commandDriverResponse.setCommandLog(commandLog);
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