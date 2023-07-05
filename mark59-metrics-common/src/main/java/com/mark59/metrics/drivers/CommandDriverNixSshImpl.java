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
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mark59.core.utils.Mark59Utils;
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
public class CommandDriverNixSshImpl implements CommandDriver {

	private static final Logger LOG = LogManager.getLogger(CommandDriverNixSshImpl.class);	

	private final ServerProfile serverProfile;
	private Session sesConnection = null;

	public CommandDriverNixSshImpl(ServerProfile serverProfile) {
		this.serverProfile = serverProfile;
	}
	
	
	/**
	 * Executes a Nix command locally, or on the requested server via SSH using the JSch library  
	 * 
	 * <p><b>Jsch Connections configuration notes:</b>
	 * <ul>
	 * <li>Default PreferredAuthentications is "<code>publickey,keyboard-interactive,password</code>". 
	 * <li>If serverprofile password is set to <code>KERBEROS</code>, PreferredAuthentications will be set to 
	 * "<code>gssapi-with-mic</code>", as "<code>Kerberos/GSSAPI</code>" authentication (the default), as 
	 * can hang if Kerberos is installed on client. Actual password can still be stored in the Password Cipher field.  
	 * See <br>https://stackoverflow.com/questions/10881981/sftp-connection-through-java-asking-for-weird-authentication.
	 * <li>A parameter named <b>SSH_PREFERRED_AUTHENTICATIONS</b> can be passed to explicitly set 
	 * PreferredAuthentications. 
	 * <li>A parameter named <b>SSH_KNOWN_HOSTS</b> can be passed to setIdentity (filename)   
	 * <li>A parameter named <b>SSH_IDENTITY</b> can be used to set a private key location (filename), See
	 * https://www.svlada.com/ssh-public-key-authentication/   
	 * </ul>
	 * @param command Command
	 * @param cmdParms parameters (for the server profile in use)
	 * @return CommandDriverResponse
	 */
	@Override
	public CommandDriverResponse executeCommand(Command command, Map<String, String> cmdParms, boolean testMode) {
		LOG.debug("executeCommand :" + command);
		CommandDriverResponse commandDriverResponse;
		StringSubstitutor parmSubstitutor = new StringSubstitutor(cmdParms);		
		
		String runtimeCommand = parmSubstitutor.replace(command.getCommand()).replaceAll("\\R", "\n");
		
		String runtimeCommandForLog = parmSubstitutor.replace(command.getCommand()
				.replace(MetricsConstants.PROFILE_PASSWORD_VAR, "********")).replaceAll("\\R", "\n");	
		
		if ("localhost".equalsIgnoreCase(serverProfile.getServer())) {
			commandDriverResponse = CommandDriver.executeRuntimeCommand(runtimeCommand, command.getIngoreStderr(),
					CommandExecutorDatatypes.SSH_LINUX_UNIX);
		
		} else { // remote connection
					
			try {
				connect(serverProfile, cmdParms);
			} catch (Exception e) {
				commandDriverResponse = new CommandDriverResponse();
				commandDriverResponse.setRawCommandResponseLines(new ArrayList<>());
				commandDriverResponse.setCommandLog("<br>Connection failure: " + e.getMessage() + "<br>");			
				commandDriverResponse.setCommandFailure(true);
				return commandDriverResponse;
			}
			
			commandDriverResponse = executeRemoteNixSystemCommand(runtimeCommand, command.getIngoreStderr());
		}
		
		commandDriverResponse.setCommandLog(CommandDriver.logExecution(runtimeCommandForLog, command.getIngoreStderr(),
				commandDriverResponse.getRawCommandResponseLines(), testMode));
		return commandDriverResponse;		
	}

	
	/**
	 * @param serverProfile serverProfile
	 * @param cmdParms the command parms (using the 'experimental' nix connection ones here)y    
	 * @return a description of the connection (for debug purposes) 
	 */
	private void connect(ServerProfile serverProfile,  Map<String, String> cmdParms) {
		LOG.debug( "CommandDriverNixSshImpl connect, serverProfile : " + serverProfile);
		String connDesc = " Remote Execution "; 

		JSch jsch = new JSch();
		try {
			String preferredAuthentications = "publickey,keyboard-interactive,password";
			if (MetricsConstants.KERBEROS.equals(serverProfile.getPassword())){
				preferredAuthentications = "gssapi-with-mic";				
			} else if (StringUtils.isNotEmpty(cmdParms.get(MetricsConstants.SSH_PREFERRED_AUTHENTICATIONS))) {
				preferredAuthentications = cmdParms.get(MetricsConstants.SSH_PREFERRED_AUTHENTICATIONS); 
			}
			connDesc +=  "<br>&nbsp;&nbsp;, PreferredAuthentications=" + preferredAuthentications;

			if (StringUtils.isNotEmpty(cmdParms.get(MetricsConstants.SSH_KNOWN_HOSTS))){
				String knownHosts = cmdParms.get(MetricsConstants.SSH_KNOWN_HOSTS);
				connDesc +=  "<br>&nbsp;&nbsp;, KnownHosts =" + knownHosts;
				jsch.setKnownHosts(knownHosts);
			};

			if (StringUtils.isNotEmpty(cmdParms.get(MetricsConstants.SSH_IDENTITY)) 
					&& StringUtils.isEmpty(cmdParms.get(MetricsConstants.SSH_PASSPHRASE))){
				String identity = cmdParms.get(MetricsConstants.SSH_IDENTITY);
				connDesc += " <br>&nbsp;&nbsp;, Identity=" + identity
						+ " <br>&nbsp;&nbsp;&nbsp;&nbsp; (note: don't set a password in the serverProfile for a prv key connection."
						+ " No passphase was entered - is that right? )";
				jsch.addIdentity(identity);
			};
			
			if (StringUtils.isNotEmpty(cmdParms.get(MetricsConstants.SSH_IDENTITY)) 
					&& StringUtils.isNotEmpty(cmdParms.get(MetricsConstants.SSH_PASSPHRASE))){
				String identity = cmdParms.get(MetricsConstants.SSH_IDENTITY);
				String passphrase = cmdParms.get(MetricsConstants.SSH_PASSPHRASE);				
				connDesc += " <br>&nbsp;&nbsp;, Identity=" + identity
						+ " <br>&nbsp;&nbsp;&nbsp;&nbsp; (note: don't set a password in the serverProfile for a prv key connection.";
				jsch.addIdentity (identity, passphrase);
			};
			
			sesConnection = jsch.getSession(serverProfile.getUsername(), serverProfile.getServer(), Integer.parseInt(serverProfile.getConnectionPort()));
			
			String actualPwd = MetricsUtils.actualPwd(serverProfile);
			if (StringUtils.isNotEmpty(actualPwd)) {
				sesConnection.setPassword(actualPwd);
				connDesc +=  "<br>&nbsp;&nbsp, Password set (from serverProfile details)";
			} else {
				connDesc +=  "<br>&nbsp;&nbsp, connection password not set";
			}
			Properties connectConfig = new java.util.Properties();
			connectConfig.put("StrictHostKeyChecking", "no");
			connectConfig.put("PreferredAuthentications", preferredAuthentications);
			sesConnection.setConfig(connectConfig);
			sesConnection.connect(Integer.parseInt(serverProfile.getConnectionTimeout()));
		} catch (JSchException jschX) {
			try {sesConnection.disconnect();} catch (Exception ignored){}
			throw new RuntimeException(jschX.getMessage() + "<br><br>Connection Details : " +  connDesc);
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
			try {
				Objects.requireNonNull(channelExec).getSession().disconnect();
				channelExec.disconnect();
			} catch (Exception ignored) {}
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