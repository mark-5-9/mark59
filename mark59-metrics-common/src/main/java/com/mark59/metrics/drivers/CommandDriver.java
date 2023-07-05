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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.utils.Mark59Utils;
import com.mark59.metrics.data.beans.Command;
import com.mark59.metrics.data.beans.ServerProfile;
import com.mark59.metrics.pojos.CommandDriverResponse;
import com.mark59.metrics.utils.MetricsConstants.CommandExecutorDatatypes;
import com.mark59.metrics.utils.MetricsConstants.OS;
import com.mark59.metrics.utils.MetricsUtils;

/**
 * Controls the execution of Groovy based scripts or operating system specific commands (that may run locally
 * or on remote servers), expected to be purposed for extraction and reporting of server/application statistics.
 * 
 * <p><b>Security Considerations</b> Note the permitted o/s system commands and Groovy scripts are generic in
 * nature. We suggest reviewing the Security sections of the Metrics application in the Mark59 User Guide,and 
 * security guidelines at your installation, prior to implementation of mark59-metrics at your installation.     
 * 
 * @author Michael Cohen
 * @author Philip Web
 * <br>Written: Australian Winter 2019
 */
public interface CommandDriver {
	
	Logger LOG = LogManager.getLogger(CommandDriver.class);
	
	String HOSTID = "HOSTID";

	
	static String obtainReportedServerId(String server, String alternateServerId) {
		String reportedServerId = server;
		if ( "localhost".equalsIgnoreCase(server) && HOSTID.equals(alternateServerId) ) {

			if (OS.WINDOWS.getOsName().equals(MetricsUtils.obtainOperatingSystemForLocalhost())){				
				reportedServerId = System.getenv("COMPUTERNAME");
			} else { 
				reportedServerId = System.getenv("HOSTNAME");
			}
			if (StringUtils.isAllBlank(reportedServerId)){	  // use IP Host Name as a alternative .
				try {
					reportedServerId = InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					reportedServerId = "unknown";
				}
			}
			
		} else if (StringUtils.isNotEmpty(alternateServerId)) {
			reportedServerId = alternateServerId;
		}
		return reportedServerId; 
	}
	
	
	static CommandDriver init(String commandExecutor, ServerProfile serverProfile) {
		CommandDriver driver;
		if (CommandExecutorDatatypes.WMIC_WINDOWS.getExecutorText().equals(commandExecutor)) {
			driver = new CommandDriverWinWmicImpl(serverProfile);
		} else if (CommandExecutorDatatypes.POWERSHELL_WINDOWS.getExecutorText().equals(commandExecutor)){  
			driver = new CommandDriverWinPowershellImpl(serverProfile);			
		} else if (CommandExecutorDatatypes.SSH_LINUX_UNIX.getExecutorText().equals(commandExecutor)){  
			driver = new CommandDriverNixSshImpl(serverProfile);
		} else {  // GROOVY_SCRIPT
			driver = new CommandDriverGroovyScriptImpl(serverProfile);
		}
		return driver;
	}
	
	
	CommandDriverResponse executeCommand(Command command, Map<String, String> cmdParms, boolean testMode);

	
	static CommandDriverResponse executeRuntimeCommand(String runtimeCommand, String ingoreStderr,
			CommandExecutorDatatypes executorType) {
		LOG.debug("executeRuntimeCommand : " + runtimeCommand );
		CommandDriverResponse commandDriverResponse = new CommandDriverResponse();   
		commandDriverResponse.setCommandFailure(false);
		List<String> rawCommandResponseLines = new ArrayList<>();
		commandDriverResponse.setCommandLog("");
		String line;
				
		ProcessBuilder processBuilder = new ProcessBuilder();
		Process p = null;
		BufferedReader errors = null;
		BufferedReader reader = null;
		
		try {
			
			if (CommandExecutorDatatypes.POWERSHELL_WINDOWS.equals(executorType)
					|| CommandExecutorDatatypes.WMIC_WINDOWS.equals(executorType)) {
				processBuilder.command(new String[] { "cmd.exe", "/c", runtimeCommand });
			} else {// *nix
				processBuilder.command(new String[] { "sh", "-c", runtimeCommand });
			}		
			
			p = processBuilder.start();
			p.waitFor();
			
			errors = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = errors.readLine()) != null) {
				if (line.length() > 0  && !Mark59Utils.resolvesToTrue(ingoreStderr) ) {
					commandDriverResponse.setCommandFailure(true);
					rawCommandResponseLines.add("[ERR] " + line.trim());
				}
			} 			
			errors.close();

			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					rawCommandResponseLines.add(line.trim());
				}
			}
			reader.close();
			p.destroy();
			
		} catch (Exception e) {
			commandDriverResponse.setCommandFailure(true);			
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			rawCommandResponseLines.add("<br>Command Failure: " + e.getMessage() + "<br>" + stackTrace.toString() + "<br>");
			
			try {Objects.requireNonNull(errors).close();} catch (Exception ignored){}
			try {Objects.requireNonNull(reader).close();} catch (Exception ignored){}
			try {Objects.requireNonNull(p).destroy();} catch (Exception ignored){}
		}
		
		commandDriverResponse.setRawCommandResponseLines(rawCommandResponseLines);
		LOG.debug("commandDriverResponse: " + commandDriverResponse);
		return commandDriverResponse;
	}

	
	static String logExecution(String runtimeCommand, String ingoreStderr, List<String> rawCommandResponseLines,
			boolean testMode) {
		String IgnoreStdErrLog = "";
		if(Mark59Utils.resolvesToTrue(ingoreStderr)){
			IgnoreStdErrLog = " (StdErr to be ignored) ";
		}
		String invokedRuntimeCommandMsg = "";
		if (testMode){
			invokedRuntimeCommandMsg = "<br>Invoked Command :<br><font face='Courier'>" + runtimeCommand + "</font>";
		}
		return IgnoreStdErrLog + invokedRuntimeCommandMsg
				+ "<br>Response :<br><font face='Courier'>" 
				+ String.join("<br>", rawCommandResponseLines).replace(" ", "&nbsp;") 
				+ "</font><br>";
	}
	
}
