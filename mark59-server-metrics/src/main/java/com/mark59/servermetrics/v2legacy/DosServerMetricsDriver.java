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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.mark59.core.utils.SimpleAES;

/**
 * @author Philip Webb    
 * @author Michael Cohen
 * Written: Australian Winter 2019   
 */
@Deprecated
public class DosServerMetricsDriver implements ServerMetricsDriverInterface<ServerMetricsDriverConfig> {

	private static final Logger LOG = Logger.getLogger(DosServerMetricsDriver.class);
	
	private static final String WMIC_COMMAND_REMOTE = "wmic /user:{0} /password:{1} /node:{2} {3}";
	private static final String WMIC_COMMAND_LOCAL = "wmic /node:localhost {0}";
	private static final String WMIC_FREE_VIRTUAL_MEMORY = "OS get FreeVirtualMemory";
	private static final String WMIC_FREE_PHYSICAL_MEMORY = "OS get FreePhysicalMemory";
	private static final String WMIC_CPU_LOAD_PERCENTAGE = "cpu get loadpercentage";
	
	private static final String CHALLENGE_REPLACE_REGEX = "(^.*password:).*?(\\s.*$)";
	private static final String CHALLENGE_REPLACE_VALUE = "$1********$2";
	
	private static final int MEMORY_METRICS_DIVISOR = 1000000;

	private String user;
	private String password;
	private String passwordCipher ;
	private String actualPassword;
	private String server;
	
	private String reportedServerId;
	private Boolean cipherUsed;
	
	private String commandLog;

	@Override
	public void init(ServerMetricsDriverConfig config) {
		this.user = config.getUser();
		this.password = config.getPassword();
		this.passwordCipher = config.getPasswordCipher();		
		this.server = config.getServer();
		
		reportedServerId = ServerMetricsDriverInterface.obtainReportedServerId(server, config.getAlternateServerId());
		
		actualPassword = password; 
		cipherUsed = false; 
		commandLog = "";
		
		if (StringUtils.isNotBlank(passwordCipher)) {
			actualPassword = SimpleAES.decrypt(passwordCipher);
			cipherUsed = true; 			
		} 			
		if (LOG.isDebugEnabled()) LOG.debug("DosServerMetricsDriver : config= " + config.toString() + " : " + "reportedServerId= " + reportedServerId  ) ;		
	}

	
	@Override
	public Map<String, Long> getCpuMetrics() {
		Map<String, Long> metrics = new HashMap<>();

		String command = null;

		if (isLocalHost()) {
			command = MessageFormat.format(WMIC_COMMAND_LOCAL, WMIC_CPU_LOAD_PERCENTAGE);
		} else {
			command = MessageFormat.format(WMIC_COMMAND_REMOTE, user, actualPassword, server, WMIC_CPU_LOAD_PERCENTAGE);
		}	
		
		commandLog+= "<br><br>Command:<br><font face='Courier'>" + command.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE) + "</font><br>Chiper Used? : " + cipherUsed + ", Response :<br><font face='Courier'>";
		
		List<String> commandReply = executeCommand(command);
		
		for (String commandReplyLine : commandReply) {
			commandLog += "    " +commandReplyLine + "<br>";
		}
		commandLog+= "</font><br>";
		
		List<Double> rawWmiCpuStats = getDoublesFromList(commandReply);

		if ( rawWmiCpuStats.size() == 0  ) {
			LOG.warn("cpu caputure has failed : " + commandLog) ;			
			//if (LOG.isDebugEnabled()) LOG.debug("commandLog : " + commandLog);		
		} else {
			Double sumCpuValues = rawWmiCpuStats.stream()
					.mapToDouble(Double::doubleValue)
					.sum();
	
			Long metricValue = Math.round(sumCpuValues / rawWmiCpuStats.size());
	
			metrics.put("CPU_" + reportedServerId, metricValue);
		}
		return metrics;
	}

	
	@Override
	public Map<String, Long> getMemoryMetrics() {
		Map<String, Long> metrics = new HashMap<>();

		Double virtualMemoryMetric = getVirtualMemoryMetric();
		if (virtualMemoryMetric != null) {
			metrics.put("Memory_" + reportedServerId + "_FreeVirtualG",
					Math.round(virtualMemoryMetric / MEMORY_METRICS_DIVISOR));
		}
		
		Double physicalMemoryMetric = getPhysicalMemoryMetric();
		if (physicalMemoryMetric != null) {
			metrics.put("Memory_" + reportedServerId + "_FreePhysicalG",
					Math.round(physicalMemoryMetric / MEMORY_METRICS_DIVISOR));
		}

		if ( metrics.size() == 0  ) {
			LOG.warn("memory caputure has failed ( server " + reportedServerId + " )");
		}	
		
		return metrics;
	}
	
	@Override
	public String getCommandLog() {
		return commandLog;
	}	
	
	
	
	/**
	 * Executes the DOS command and then 
	 * @param command
	 * @return
	 */
	public List<String> executeCommand(String command) {

		LOG.debug(reportedServerId + " executing metrics command : " + command.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE));
		
		String line = "";
		List<String> outputLines = new ArrayList<>();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();

			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = reader.readLine()) != null) {
				if (line.length() > 0) {
					outputLines.add(line.trim());
				}
			}

		} catch (Exception e) {
			LOG.warn(e.getMessage());
		}
		
		outputLines.forEach(LOG::debug);
		
		return outputLines;
	}
	
	/**
	 * converts a List<String> into a List<Double>, dropping all non numeric Strings in the Process 
	 * @param stringList
	 * @return
	 */
	private List<Double> getDoublesFromList(List<String> stringList) {
		return stringList.stream()
				.filter(StringUtils::isNumeric)
				.map(Double::parseDouble)
				.collect(Collectors.toList());
	}

	/**
	 * Builds and executes a WMIC command to return the systems utilisation of PHYSICAL memory 
	 * @return
	 */
	private Double getPhysicalMemoryMetric() {
		return getMemoryMetric(false);
	}

	/**
	 * Builds and executes a WMIC command to return the systems utilisation of VIRTUAL memory 
	 * @return
	 */
	private Double getVirtualMemoryMetric() {
		return getMemoryMetric(true);
	}

	/**
	 * Generic method to build the WMIC memory command
	 * @param isVirtualMemory
	 * @return
	 */
	private Double getMemoryMetric(boolean isVirtualMemory) {
		String command = null;
		String memoryType = isVirtualMemory ? WMIC_FREE_VIRTUAL_MEMORY : WMIC_FREE_PHYSICAL_MEMORY;

		if (isLocalHost())
			command = MessageFormat.format(WMIC_COMMAND_LOCAL, memoryType);
		else
			command = MessageFormat.format(WMIC_COMMAND_REMOTE, user, actualPassword, server, memoryType);

		LOG.debug(command.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE));
		
		List<String> commandResult =  executeCommand(command);
		
		commandLog+= "<br><br>Command:<br><font face='Courier'>" + command.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE) + "</font><br>Chiper Used? : " + cipherUsed + ", Response :<br><font face='Courier'>";
		
		List<String> commandReply = executeCommand(command);
		
		for (String commandReplyLine : commandReply) {
			commandLog += "    " +commandReplyLine + "<br>";
		}
		commandLog+= "</font><br>";

		
		List<Double> commandResultDoubles = getDoublesFromList(commandResult);
		
		if (commandResultDoubles.size() == 0) {
			LOG.warn("memory caputure has failed ( " + command.replaceAll(CHALLENGE_REPLACE_REGEX, CHALLENGE_REPLACE_VALUE) + " )");
			//if (LOG.isDebugEnabled()) LOG.debug("commandLog : " + commandLog);		
			return null;
		} else {
			return commandResultDoubles.get(0);
		}
	}

	private boolean isLocalHost() {
		return "localhost".equalsIgnoreCase(server);
	}

	@Override
	public void quit() {
		// nothing to do
		
	}

	@Override
	public Map<String, Long> getSystemInfo() {
		LOG.warn("getSystemInfo() not yet implemented for DOS systems");
		return null;
	}

}
