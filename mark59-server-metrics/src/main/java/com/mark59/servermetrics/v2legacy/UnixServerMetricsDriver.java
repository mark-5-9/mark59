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

package com.mark59.servermetrics.v2legacy;


import static com.mark59.servermetrics.v2legacy.AppConstantsServerMetricsV2Legacy.LINUX;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mark59.core.utils.SimpleAES;

/**
* @author Philip Webb    
* @author Michael Cohen
* Written: Australian Winter 2019   
*/
@Deprecated
public class UnixServerMetricsDriver implements ServerMetricsDriverInterface<ServerMetricsDriverConfig>{

	private static final int ONETHOUSAND_MILLIS = 1000;

	private static final Logger LOG = Logger.getLogger(UnixServerMetricsDriver.class);

	private static final String LINUX_CPU_METRICS_COMMAND = "mpstat 1 1";
	private static final String UNIX_CPU_METRICS_COMMAND = "lparstat 5 1";

	private static final String DECIMAL_FORMAT = "\\d*\\.?\\d+";

	// This is ripped from a shell script we use internally to monitor our unix VMs
	
	private static final String UNIX_MEMORY_METRICS_COMMAND = "vmstat=$(vmstat -v);"
			+ "let total_pages=$(print \"$vmstat\" | grep 'memory pages' | awk '{print $1}');"
			+ "let pinned_pages=$(print \"$vmstat\" | grep 'pinned pages' | awk '{print $1}');"
			+ "let pinned_percent=$(( $(print \"scale=4; $pinned_pages / $total_pages \" | bc) * 100 ));"
			+ "let numperm_pages=$(print \"$vmstat\" | grep 'file pages' | awk '{print $1}');"
			+ "let numperm_percent=$(print \"$vmstat\" | grep 'numperm percentage' | awk '{print $1}');"
			+ "pgsp_utils=$(lsps -a | tail +2 | awk '{print $5}');"
			+ "let pgsp_num=$(print \"$pgsp_utils\" | wc -l | tr -d ' ');" + "let pgsp_util_sum=0;"
			+ "for pgsp_util in $pgsp_utils;" + "do let pgsp_util_sum=$(( $pgsp_util_sum + $pgsp_util ));" + "done;"
			+ "pgsp_aggregate_util=$(( $pgsp_util_sum / $pgsp_num ));"
			+ "print \"${pinned_percent},${numperm_percent},${pgsp_aggregate_util}\";";

	private static final String LINUX_MEMORY_METRICS_COMMAND = "free -m 1 1";

	private JSch jschSSHChannel;
	private String strUserName;
	private String strConnectionIP;
	private Integer intConnectionPort;
	private String strPassword;
	private String strPasswordCipher;
	private String alternateServerId;
	private Session sesConnection;
	private Integer intTimeOut;
	private String operatingSystem;

	private String reportedServerId;	
	private Boolean cipherUsed;
	private String commandLog;	
	
	
	public void init(ServerMetricsDriverConfig config) {
		this.strConnectionIP = config.getServer();
		this.strUserName = config.getUser();
		this.strPassword = config.getPassword();
		this.strPasswordCipher = config.getPasswordCipher();
		this.alternateServerId = config.getAlternateServerId();
		this.intConnectionPort = config.getConnectionPort();
		this.intTimeOut = config.getConnectionTimeOut();
		this.operatingSystem = config.getOperatingSystem();

		reportedServerId = ServerMetricsDriverInterface.obtainReportedServerId(strConnectionIP, alternateServerId);
		commandLog = "";
		cipherUsed = false; 
		if (StringUtils.isNotBlank(strPasswordCipher)) {
			cipherUsed = true; 			
		} 			
		jschSSHChannel = new JSch();
		if (LOG.isDebugEnabled()) LOG.debug("UnixServerMetricsDriver : config= " + config.toString() + " : " + "reportedServerId= " + reportedServerId  ) ;
	}

	
	public Map<String, Long> getCpuMetrics() {
		Map<String, Long> metrics = new HashMap<>();
		String commandResult;

		if (LINUX.equalsIgnoreCase(operatingSystem)) {
			commandResult = executeCommand(LINUX_CPU_METRICS_COMMAND).get(0);
		} else { // the default is Unix
			commandResult = executeCommand(UNIX_CPU_METRICS_COMMAND).get(0);;
		}

		if (StringUtils.isNotBlank(commandResult)) {
			String cpuIdle = extract("%idle", DECIMAL_FORMAT, commandResult);
			// Unix/Linux returns 'server idle', so invert for cpu util 
			metrics.put("CPU_" + reportedServerId, Math.round(100.0 - Double.parseDouble(cpuIdle)));
		}
		LOG.debug(strConnectionIP + " CPU command result :" + commandResult);
		return metrics;
	}

	public Map<String, Long> getMemoryMetrics() {
		Map<String, Long> metrics = new HashMap<>();
		String commandResult;

		if (LINUX.equalsIgnoreCase(operatingSystem)) {
			commandResult = executeCommand(LINUX_MEMORY_METRICS_COMMAND).get(0);;
			
			if (StringUtils.isNotBlank(commandResult)) {
				String memorytotal = memextract("total", "Mem:", commandResult);
				String memoryused = memextract("used", "Mem:", commandResult);
				String memoryfree = memextract("free", "Mem:", commandResult);

				metrics.put("Memory_totalG_" + reportedServerId, Math.round(Double.parseDouble(memorytotal)/1000  ));
				metrics.put("Memory_usedG_" + reportedServerId,  Math.round(Double.parseDouble(memoryused)/1000));
				metrics.put("Memory_freeG_" + reportedServerId,  Math.round(Double.parseDouble(memoryfree)/1000));
			}
		} else { // the default is Unix
			commandResult = executeCommand(UNIX_MEMORY_METRICS_COMMAND).get(0);;
			if (StringUtils.isNotBlank(commandResult)) {
				String[] memoryResults = commandResult.split(",");

				String dataMemoryKey = "Memory_";
				
				metrics.put(dataMemoryKey + reportedServerId + "_pinned_percent", 	  Long.parseLong(memoryResults[0].trim()));
				metrics.put(dataMemoryKey + reportedServerId + "_numperm_percent", 	  Long.parseLong(memoryResults[1].trim()));
				metrics.put(dataMemoryKey + reportedServerId + "_pgsp_aggregate_util", Long.parseLong(memoryResults[2].trim()));
			}
		}
		LOG.debug(strConnectionIP + " Memory command result :" + commandResult);
		return metrics;
	}
	
	
	@Override
	public String getCommandLog() {
		return commandLog;
	}	
	

	public List<String> executeCommand(String command) {
		commandLog+= "<br><br>Command:<br><font face='Courier'>" + command + "</font><br>Chiper Used? : " + cipherUsed + ", Response :<br><font face='Courier'>";
		List<String> commandResult = new ArrayList<>();
		connect();
		commandResult.add(sendCommand(command));
		
		String[] commandResultSplit = commandResult.get(0).replace(" ", "&nbsp;").split("\\r\\n|\\n|\\r") ;
		for (String commandReplyLine : commandResultSplit) {
			commandLog += "    " +commandReplyLine + "<br>";
		}
		commandLog+= "</font><br>";

		close();
		return commandResult;
	}

	private String connect() {
		String errorMessage = null;
		try {
			sesConnection = jschSSHChannel.getSession(strUserName, strConnectionIP, intConnectionPort );
			
			if (StringUtils.isBlank(strPasswordCipher)) {
				sesConnection.setPassword(strPassword);
			} else {
				sesConnection.setPassword(SimpleAES.decrypt(strPasswordCipher));
			}
			sesConnection.setConfig("StrictHostKeyChecking", "no");
			sesConnection.connect(intTimeOut);
		} catch (JSchException jschX) {
			errorMessage = jschX.getMessage();
			LOG.warn(errorMessage);
		}
		return errorMessage;
	}

	private String sendCommand(String command) {

		LOG.debug("Attempting to execute command : " + command);

		StringBuilder outputBuffer = new StringBuilder();

		try {
			Channel channel = sesConnection.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			InputStream commandOutput = channel.getInputStream();
			channel.connect();
			int readByte = commandOutput.read();

			while (commandOutput.available() > 0) {
				while (readByte != 0xffffffff) {
					outputBuffer.append((char) readByte);
					readByte = commandOutput.read();
				}
				sleepSilently(ONETHOUSAND_MILLIS);
			}
			channel.disconnect();
		} catch (JSchException | IOException e) {
			LOG.warn(e.getMessage());
			return null;
		}
		LOG.debug("command " + command + " : " + outputBuffer.toString());
		return outputBuffer.toString();
	}

	private void sleepSilently(int millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception ee) {
			LOG.info(ee);
		}
	}

	private void close() {
		if(sesConnection.isConnected())
			sesConnection.disconnect();
	}

	/**
	 * This works on data in a simple column format (eg unix lparstat and linux
	 * mpstat cpu) It will return the first matching value it finds in the column
	 * requested.
	 * 
	 * <p><b>Linux CPU sample</b>
	 * 
	 * <p>In the Linux sample below: <code>extract( "%idle", DECIMAL_FORMAT, {command response} )</code> 
	 *  returns "64.15" 

	 * <p>Command:  mpstat 1 1
	 * 
	 * <p>Linux Response: 
	 * <p><code>
	 *	Linux 3.10.0-1062.9.1.el7.x86_64 (linux01.mycorp.corp)  19/02/20  _x86_64_ (5 CPU)<br>
	 *	<br>
	 *	08:36:17     CPU    %usr   %nice    %sys %iowait    %irq   %soft  %steal  %guest  %gnice   %idle<br>
	 *	08:36:18     all   25.87    0.00    5.70    0.20    0.00    4.07    0.00    0.00    0.00   64.15<br>
	 *	Average:     all   25.87    0.00    5.70    0.20    0.00    4.07    0.00    0.00    0.00   64.15<br>
	 * </code>
	 * 
	 * <p><b>UNIX CPU sample</b>
	 *  
	 * <p>In the UNIX LPARSTSAT sample below <code>extract( "%idle", DECIMAL_FORMAT, {command response} )</code>
	 *  returns "68.6"
	 *  
	 * <p>Comand: lparstat 5 1
	 * 
	 * <p>UNIX Response: 
	 * <p><code>
	 *  System configuration: type=Shared mode=Uncapped smt=4 lcpu=8 mem=34816MB psize=39 ent=0.35<br>
	 * <br>
	 *  %user %sys   %wait %idle  physc %entc lbusy  app   vcsw  phint %nsp  %utcyc <br>
	 *  ----- ----- ------ ------ ----- ----- ------ ---   ----- ----- ----- ------<br>
	 *  17.1  14.3   0.0   68.6   0.20  55.8  3.3    25.08 622   0     101   1.38<br>
	 * </code> 
	 */
	private String extract(String targetColumnName, String targetmetricFormat, String commandResult) {
		String extractedMetric = "-1";
		int colNumberOfTargetColumnName = -1;
		String[] commandResultLine = commandResult.trim().split("\\r\\n|\\n|\\r");

		for (int i = 0; i < commandResultLine.length && "-1".equals(extractedMetric); i++) {

			String[] wordsOnThiscommandResultsLine = commandResultLine[i].trim().split("\\s+");

			if (colNumberOfTargetColumnName > -1
					&& wordsOnThiscommandResultsLine[colNumberOfTargetColumnName].matches(targetmetricFormat)) {
				extractedMetric = wordsOnThiscommandResultsLine[colNumberOfTargetColumnName];
			}

			if (colNumberOfTargetColumnName == -1) { // column name not yet found, so see if it is on this line ...
				colNumberOfTargetColumnName = ArrayUtils.indexOf(wordsOnThiscommandResultsLine, targetColumnName);
			}
		}
		return extractedMetric;
	}

	/**
	 * This works on data in a "tabular" format - Linux memory stats are returned
	 * this was as per sample below. It will return the value for the metric column
	 * requested, on the line metric requested.
	 * 
	 * <p>So in the sample below <code>memextract( "free", "Mem:", {command response} )</code> returns "315"
	 * 
	 * <p>Linux Command: "free -m 1 1"  
	 * 
	 * <p>Linux Response:
	 * <p><code>
	 *        total    used   free  shared  buff/cache   available<br> 
	 * Mem:   35036   31916    315     239       2804         2238 <br> 
	 * Swap:  34815   17344  17471<br> 
	 * </code>
	 * 
	 */
	private String memextract(String targetColumnName, String targetRowName, String commandResult) {

		String extractedMetric = "-1";

		if (StringUtils.isNotBlank(commandResult)) {

			String wordsOnThisResultLine = commandResult.replace("\n", " ").replace("\r", " ");
			ArrayList<String> cmdResultLine = new ArrayList<>(
					Arrays.asList(wordsOnThisResultLine.trim().split("\\s+")));

			if (cmdResultLine.contains(targetColumnName)) {
				extractedMetric = cmdResultLine
						.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);
			}
		}
		return extractedMetric;
	}

	@Override
	public void quit() {
		close();
	}
	
	@Override
	public Map<String, Long> getSystemInfo() {
		LOG.warn("getSystemInfo() not yet implemented for LINUX/UNIX systems");
		return null;
	}

//	public static void main(String[] args) {		
//		LOG.info("Beginning UnixServerMetricsDriver!");
//		
//		ServerMetricsDriverConfig config = new ServerMetricsDriverConfig("server", "user", "clearcasepass", "aespass" ); 	
//		UnixServerMetricsDriver unixServerMetricsDriver = new UnixServerMetricsDriver();
//		unixServerMetricsDriver.init(config);
//		unixServerMetricsDriver.operatingSystem = LINUX;
//		LOG.info("UnixServerMetricsDriver - cpu ");
//		Map<String, Long> metrics = unixServerMetricsDriver.getCpuMetrics();
//		LOG.info(Arrays.asList(metrics));
//		LOG.info("--------------------------------------------------------------");
//		LOG.info("UnixServerMetricsDriver - memory ");
//		metrics = unixServerMetricsDriver.getMemoryMetrics();
//		LOG.info(Arrays.asList(metrics));
//		LOG.info("Ending UnixServerMetricsDriver!");
//	}

}
