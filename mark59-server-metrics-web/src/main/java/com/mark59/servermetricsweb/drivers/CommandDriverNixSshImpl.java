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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.SimpleAES;
import com.mark59.servermetricsweb.data.beans.Command;
import com.mark59.servermetricsweb.data.beans.ServerProfile;
import com.mark59.servermetricsweb.pojos.CommandDriverResponse;
import com.mark59.servermetricsweb.utils.AppConstantsServerMetricsWeb.CommandExecutorDatatypes;


/**
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019   
 */
public class CommandDriverNixSshImpl implements CommandDriver {

	private static final Logger LOG = LogManager.getLogger(CommandDriverNixSshImpl.class);	

	private ServerProfile serverProfile;
	private Session sesConnection = null;

	public CommandDriverNixSshImpl(ServerProfile serverProfile) {
		this.serverProfile = serverProfile;
	}
	
	
	/**
	 * Executes the command on the requested server via SSHn 
	 * @param command
	 * @return CommandDriverResponse
	 */
	public CommandDriverResponse executeCommand(Command command) {
		LOG.debug("executeCommand :" + command);

		CommandDriverResponse commandDriverResponse = new CommandDriverResponse();
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
		if(Mark59Utils.resovesToTrue(command.getIngoreStderr())){
			IgnoreStdErrLog = ". StdErr to be ignored. ";
		}

		String commandLog = cipherUsedLog + IgnoreStdErrLog + " :<br><font face='Courier'>" + command.getCommand().replaceAll("\\R", "<br>") + "</font>"; 
			
		if ("localhost".equalsIgnoreCase(serverProfile.getServer())) {
			commandDriverResponse = CommandDriver.executeRuntimeCommand(command.getCommand().replaceAll("\\R", "\n")  , command.getIngoreStderr(), CommandExecutorDatatypes.SSH_LINIX_UNIX );
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
			try {sesConnection.disconnect();} catch (Exception x){};			
			commandDriverResponse.setRawCommandResponseLines(new ArrayList<String>());
			commandDriverResponse.setCommandLog("<br>A failure has occured attempting to connect : " + jschX.getMessage() + "<br>");			
			commandDriverResponse.setCommandFailure(true);
			sesConnection = null;
			LOG.warn("A failure has occured attempting to connect : " + serverProfile  + " : " + jschX.getMessage());
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
							
			if ( ! Mark59Utils.resovesToTrue(ingoreStderr)){
				rawCommandResponseLines = readCommandInputStream(commandErrStream);
				if (rawCommandResponseLines.size() > 0  ) {
					commandDriverResponse.setCommandFailure(true);
				}
			} 			
			
			rawCommandResponseLines.addAll( readCommandInputStream(commandResponseStream) );
			
			channelExec.getSession().disconnect();
			channelExec.disconnect();
			
		} catch (Exception e) {
			try {channelExec.getSession().disconnect();channelExec.disconnect();} catch (Exception x){};
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			commandDriverResponse.setCommandLog("<br>A faiure has occured attempting to execute the command : " + e.getMessage() + "<br>" + stackTrace.toString() + "<br>");			
			LOG.warn("Command failure : " + runtimeCommand + ":\n" + e.getMessage() + stackTrace.toString());
		}

		commandDriverResponse.setRawCommandResponseLines(rawCommandResponseLines);
		commandDriverResponse.setCommandLog(commandLog);
		return commandDriverResponse;
	}

	
	/**
	 * @param rawCommandResponseLines
	 * @param commandStream
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private List<String> readCommandInputStream(InputStream commandStream)	throws IOException, InterruptedException {
		List<String> rawCommandResponseLines = new ArrayList<String>(); 
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
	
	
	
	
//	private static final int ONETHOUSAND_MILLIS = 1000;
//
//	private static final Logger LOG = Logger.getLogger(UnixServerMetricsDriver.class);
//
//	private static final String LINUX_CPU_METRICS_COMMAND = "mpstat 1 1";
//	private static final String UNIX_CPU_METRICS_COMMAND = "lparstat 5 1";
//
//	private static final String DECIMAL_FORMAT = "\\d*\\.?\\d+";
//
//	// This is ripped from a shell script we use internally to monitor our unix VMs
//	
//	private static final String UNIX_MEMORY_METRICS_COMMAND = "vmstat=$(vmstat -v);"
//			+ "let total_pages=$(print \"$vmstat\" | grep 'memory pages' | awk '{print $1}');"
//			+ "let pinned_pages=$(print \"$vmstat\" | grep 'pinned pages' | awk '{print $1}');"
//			+ "let pinned_percent=$(( $(print \"scale=4; $pinned_pages / $total_pages \" | bc) * 100 ));"
//			+ "let numperm_pages=$(print \"$vmstat\" | grep 'file pages' | awk '{print $1}');"
//			+ "let numperm_percent=$(print \"$vmstat\" | grep 'numperm percentage' | awk '{print $1}');"
//			+ "pgsp_utils=$(lsps -a | tail +2 | awk '{print $5}');"
//			+ "let pgsp_num=$(print \"$pgsp_utils\" | wc -l | tr -d ' ');" + "let pgsp_util_sum=0;"
//			+ "for pgsp_util in $pgsp_utils;" + "do let pgsp_util_sum=$(( $pgsp_util_sum + $pgsp_util ));" + "done;"
//			+ "pgsp_aggregate_util=$(( $pgsp_util_sum / $pgsp_num ));"
//			+ "print \"${pinned_percent},${numperm_percent},${pgsp_aggregate_util}\";";
//
//	private static final String LINUX_MEMORY_METRICS_COMMAND = "free -m 1 1";	
	
	

}
