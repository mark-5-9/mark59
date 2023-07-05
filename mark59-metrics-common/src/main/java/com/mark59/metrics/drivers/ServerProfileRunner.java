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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.SafeSleep;
import com.mark59.metrics.data.beans.Command;
import com.mark59.metrics.data.beans.CommandParserLink;
import com.mark59.metrics.data.beans.CommandResponseParser;
import com.mark59.metrics.data.beans.ServerCommandLink;
import com.mark59.metrics.data.beans.ServerProfile;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.metrics.data.commands.dao.CommandsDAO;
import com.mark59.metrics.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.metrics.pojos.CommandDriverResponse;
import com.mark59.metrics.pojos.ParsedCommandResponse;
import com.mark59.metrics.pojos.ParsedMetric;
import com.mark59.metrics.pojos.WebServerMetricsResponsePojo;
import com.mark59.metrics.utils.MetricsConstants;
import com.mark59.metrics.utils.MetricsConstants.CommandExecutorDatatypes;
import com.mark59.metrics.utils.MetricsUtils;

/**
 * Invokes the commands to be processed for a given Server Profile 
 * (Groovy script or to run on the profile's target server).  
 * 
 * <p>This class runs within the Mark59 'metrics' web application when invoked via the Web APIs or directly using a
 * 'Run Profile' option in the application, but runs locally (eg on the server running JMeter) when invoked via the Excel
 *  spreadsheet. Refer to ServerMetricsCaptureViaExcel in the mark59-metrics-api project.  
 * 
 * @author Philip Webb
 * <br>Written: Australian Autumn 2020 
 */
public class ServerProfileRunner {

	private static final Logger LOG = LogManager.getLogger(ServerProfileRunner.class);	

	private static final String indent = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
	private static int commandCount;
	private static int commandFailureCount;
	private static int parsingSuccessCount;
	private static int parsingFailureCount;
		
	/**
	 * Builds and returns a response object holding the results of executing a server profile.  Invoked via the 
	 * metrics API, and also used directly in the Metrics Web application to run and test outside of the API. 
	 * 
	 * <p>When running via the Metrics web application, logging for command and parser failures is sent to the 
	 * application log (log4j).  To prevent excessive logging error message are abbreviated (currently to 2000 chars
	 * for command failures and 1200 chars for parser failures).	
	 * 
	 * <p>Logging is turned to a minimum for command and parser failures when running locally via the Excel spreadsheet
	 * to prevent excess logging of failed commands and parsers in the JMeter log and console.
	 * 
	 * @param reqServerProfileName server profile
	 * @param reqTestMode if set, (eg when executing a profile via the web app UI), returns a more detailed formatted
	 *  response, including logged command details ('password' fields masked), and a 'testModeResult' summary.
	 * @param serverProfilesDAO  serverProfilesDAO
	 * @param serverCommandLinksDAO  serverCommandLinksDAO
	 * @param commandsDAO  commandsDAO
	 * @param commandParserLinksDAO  commandParserLinksDAO
	 * @param commandResponseParsersDAO  commandResponseParsersDAO
	 * @param runningViaWeb 'false' will shorten log4j WARN messages to a minimum for Command and Parser failures
	 * 
	 * @return WebServerMetricsResponsePojo  response
	 */
	public static WebServerMetricsResponsePojo commandsResponse(String reqServerProfileName, String reqTestMode,
			ServerProfilesDAO serverProfilesDAO, ServerCommandLinksDAO serverCommandLinksDAO, CommandsDAO commandsDAO,
			CommandParserLinksDAO commandParserLinksDAO, CommandResponseParsersDAO commandResponseParsersDAO,
			boolean runningViaWeb) {
		
		LOG.debug("ServerProfileRunner.commandsResponse profile =" + reqServerProfileName);
		boolean testMode = Mark59Utils.resolvesToTrue(reqTestMode);

		WebServerMetricsResponsePojo response = new WebServerMetricsResponsePojo();
		response.setServerProfileName(reqServerProfileName);
		response.setParsedCommandResponses(new ArrayList<>());
		response.setLogLines("");
		List<String> logLines = new ArrayList<>();
		commandCount = 0;		
		commandFailureCount = 0;		
		parsingSuccessCount = 0;
		parsingFailureCount = 0;	
		Instant serverProfileStarts = Instant.now();
		
		try {
	
			ServerProfile serverProfile = serverProfilesDAO.findServerProfile(reqServerProfileName);
			
			if (serverProfile == null ) {
				response.setServerProfileName(reqServerProfileName); 
				response.setFailMsg(MetricsConstants.SERVER_PROFILE_NOT_FOUND + " (" + reqServerProfileName + ")" ); 
				return response;
			}

			response.setServerProfileName(reqServerProfileName);
			response.setServer(serverProfile.getServer());
			response.setAlternativeServerId(serverProfile.getAlternativeServerId());
			response.setReportedServerId(CommandDriver.obtainReportedServerId(
					serverProfile.getServer(),serverProfile.getAlternativeServerId()));
			response.setFailMsg("");
			
			Map<String,String> cmdParms = null; 
			if (!CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equalsIgnoreCase(serverProfile.getExecutor())){
				cmdParms = createNonGroovyPredefinedParms(serverProfile, response);
			}
			
			List<ParsedCommandResponse> parsedCommandResponses = new ArrayList<>();
			List<ServerCommandLink> serverCommandLinks = serverCommandLinksDAO.findServerCommandLinksForServerProfile(
					serverProfile.getServerProfileName());  
			
			// execute each command linked to the server profile
			
			for (ServerCommandLink serverCommandLink : serverCommandLinks) {
				commandCount++;
				
				Command command = commandsDAO.findCommand(serverCommandLink.getCommandName());
				CommandDriver driver =  CommandDriver.init(command.getExecutor(), serverProfile);
				ShortPauseBetweenOScommands(commandCount); 
				
				CommandDriverResponse commandDriverResponse = driver.executeCommand(command, cmdParms, testMode);
				
				testModeLog(logLines,"<b><a href=./editCommand?&reqCommandName=" + command.getCommandName() + ">"
						+ command.getCommandName() + "</a></b>");
				
				ParsedCommandResponse parsedCommandResponse = new ParsedCommandResponse(); 
				parsedCommandResponse.setCommandName(command.getCommandName());
				parsedCommandResponse.setCommandFailure(commandDriverResponse.isCommandFailure());  				

				if (commandDriverResponse.isCommandFailure()){      
					
					commandFailureCount++;
					
					String failureMsg = "<br>Command " + command.getCommandName() 
						+ ", on Server Profile " + reqServerProfileName + " has failed." 
						+ "<br>" + commandDriverResponse.getCommandLog() + ".";
					
					testModeLog(logLines, failureMsg);
					testModeLog(logLines, "<br><font color='red'><b>Execution has errored. </b></font><br><br>");
					
					parsedCommandResponse.setParsedMetrics(new ArrayList<ParsedMetric>() );
					parsedCommandResponse.setCommandResponse(failureMsg.replace("<br>", "\n"));
					parsedCommandResponses.add(parsedCommandResponse);
					
					if (runningViaWeb) {
						if (StringUtils.contains(failureMsg, "Response :") && failureMsg.length() > 2000 ){  
							// caters for long 'invoked commands', to ensure at least part of the response is output
							String failnl = failureMsg.replace("<br>", "\n").replace("&nbsp;", " ");
							String cmdInvoked  = StringUtils.abbreviate(StringUtils.substringBefore(failnl, "Response :"), 1000);
							String cmdResponse = StringUtils.substringAfter(failnl, "Response :");
							LOG.warn(StringUtils.abbreviate(cmdInvoked + "\nResponse : " + cmdResponse, 2000));
						} else {
							LOG.warn(StringUtils.abbreviate(failureMsg.replace("<br>", "\n"), 2000));
						}
					} else { // excel
						LOG.warn("Cmd " + command.getCommandName() + ", Profile " + reqServerProfileName + " failed");
					}
				} else if  (CommandExecutorDatatypes.GROOVY_SCRIPT.getExecutorText().equalsIgnoreCase(command.getExecutor())){
					// Groovy script command responses don't need to invoke a 'Parser'. 
					// The metrics just need to be copied from the 'driver' command response to the response parsed metrics list    
					
					for (ParsedMetric parsedMetric : commandDriverResponse.getParsedMetrics() ) {	
						if ( parsedMetric.getSuccess())	{				
							parsingSuccessCount++;
						} else {
							parsingFailureCount++;
						}
					}
					parsedCommandResponse.setParsedMetrics(commandDriverResponse.getParsedMetrics()  );
					parsedCommandResponse.setCommandResponse(commandDriverResponse.getCommandLog());
					
					testModeLog(logLines, commandDriverResponse.getCommandLog());
					testModeLog(logLines, logParsedMetrics(parsedCommandResponse.getParsedMetrics()));
			
					parsedCommandResponses.add(parsedCommandResponse);

				} else {      // invoke Parsers on a successful non-Groovy command response 
				
					testModeLog(logLines, commandDriverResponse.getCommandLog());
	
					String commandResponseAsString = MetricsUtils.createMultiLineLiteral(commandDriverResponse.getRawCommandResponseLines());
					parsedCommandResponse.setCommandResponse(commandResponseAsString);
					List<ParsedMetric> parsedMetrics = new ArrayList<>(); 
					List<CommandParserLink> commandParserLinks = commandParserLinksDAO.findCommandParserLinksForCommand(command.getCommandName());

					for (CommandParserLink commandParserLink : commandParserLinks) {
					
						CommandResponseParser commandResponseParser = commandResponseParsersDAO.findCommandResponseParser(
								commandParserLink.getParserName()); 

						ParsedMetric parsedMetric = new ParsedMetric(); 		
						parsedMetric.setLabel(Mark59Utils.constructCandidateTxnIdforMetric(
								commandResponseParser.getMetricTxnType(),
								response.getReportedServerId(),
								commandResponseParser.getMetricNameSuffix()));				
						parsedMetric.setDataType(commandResponseParser.getMetricTxnType());	
					
						parsedMetric = RunParser(commandResponseParser, commandResponseAsString, parsedMetric );

						testModeLog(logLines, indent + "<b><a href=./viewCommandResponseParser?&reqParserName=" 
								+ commandParserLink.getParserName() + ">" + commandParserLink.getParserName()+ "</a></b> parser");
						testModeLog(logLines, logParsedMetric(parsedMetric)); 
						
						if (!parsedMetric.getSuccess()) {
							if (runningViaWeb) {
								LOG.warn(StringUtils.abbreviate("Parser Fails for profile " + reqServerProfileName + ", command " 
										+ command.getCommandName() + "\ndetails: " +  parsedMetric.getParseFailMsg(), 1200)); 
							} else { // excel
								LOG.warn("Parser Fails for profile " + reqServerProfileName + ", command " + command.getCommandName());
							}
						}						
						parsedMetrics.add(parsedMetric);
					}
					testModeLog(logLines, "<br>"); 
					parsedCommandResponse.setParsedMetrics(parsedMetrics);
					parsedCommandResponses.add(parsedCommandResponse);
				}
			}
	
			response.setParsedCommandResponses(parsedCommandResponses);
			response.setLogLines(String.join("", logLines));			
			response.setTestModeResult(summariseResponse(testMode, serverProfileStarts, Instant.now()));
		
		} catch (Exception e) {
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			String failureMsg = "Error: Unexpected Failure attempting to execute server profile. \n" +
								"reqServerProfileName : " + reqServerProfileName + "\n" + e.getMessage() + "\n" + stackTrace.toString();
			response.setFailMsg(failureMsg);
			if (testMode) {
				response.setLogLines(response.getLogLines() + failureMsg.replaceAll("\\R", "<br>"));
				response.setTestModeResult("<font color='red'>Error: Unexpected Failure attempting to execute server profile."
						+ "<br>Server Profile : " + reqServerProfileName + "<br>Error Message : " + e.getMessage());
			}
			LOG.warn(failureMsg);
			LOG.debug("    loglines : " + response.getLogLines());
		}
        LOG.debug("<< response : " + response);
		return response;
	}


	/**
	 * As a non-Groovy profile can have multiple commands, and each commands has the same parameters passed, 
	 * the parameter list is built at profile level and passed to each of the command(s).
	 * <p>The pre-defined parameters for the non-Groovy profiles are also created here. Note password is only 
	 * available in PowerShell (even then the recommendation is to create a SecureString parameter instead).
	 *     
	 * @param serverProfile  a (non-Groovy) serverProfile
	 * @param response WebServerMetricsResponsePojo
	 * @return map of command parameters (including pre-defined parametera).
	 */
	private static Map<String, String> createNonGroovyPredefinedParms(ServerProfile serverProfile, WebServerMetricsResponsePojo response) {
		Map<String,String> cmdParms = serverProfile.getParameters()==null ? new HashMap<>() : serverProfile.getParameters();
		if (System.getProperty(MetricsConstants.METRICS_BASE_DIR) != null){
			cmdParms.put(MetricsConstants.METRICS_BASE_DIR, System.getProperty(MetricsConstants.METRICS_BASE_DIR));
		}
		cmdParms.put(MetricsConstants.PROFILE_NAME, serverProfile.getServerProfileName());
		cmdParms.put(MetricsConstants.PROFILE_SERVER, serverProfile.getServer());
		cmdParms.put(MetricsConstants.PROFILE_USERNAME, serverProfile.getUsername());
		
		if (CommandExecutorDatatypes.POWERSHELL_WINDOWS.getExecutorText().equals(serverProfile.getExecutor())){  
			cmdParms.put(MetricsConstants.PROFILE_PASSWORD, MetricsUtils.actualPwd(serverProfile));
		}
		return cmdParms;
	}

	
	/**
	 * A short 100ms pause between Nix and Win commands could assist command stability under load. 
	 * Not relevant for Groovy Server Profiles as they only run a single command.
	 * @param commandCount the current command count
	 **/
	private static void ShortPauseBetweenOScommands(int commandCount) {
		if (commandCount > 1) {
			SafeSleep.sleep(100L);
		}
	}


	private static ParsedMetric RunParser(CommandResponseParser commandResponseParser,	String commandResponseAsString, ParsedMetric parsedMetric) {
		try {
			Object groovyScriptResult = MetricsUtils.runGroovyScript(commandResponseParser.getScript(), commandResponseAsString);

			if (isaParsableDouble(groovyScriptResult.toString())) {
				
				try {
					Double metricResult = Double.parseDouble(groovyScriptResult.toString());
					parsingSuccessCount++;
					parsedMetric.setSuccess(true);		
					parsedMetric.setResult(metricResult);	
					
				} catch (Exception pe) { // should never happen
					parsingFailureCount++;
					parsedMetric.setSuccess(false);	
					parsedMetric.setResult(null);	
					parsedMetric.setParseFailMsg(
							"\nError : " + commandResponseParser.getParserName() + " Script parsing failure\n" + 
							"Script parsing failure (for an passed numeric result) : [" + groovyScriptResult + "]." +  
							"\nParser : " + commandResponseParser.getParserName() +
							"\nCommand Response was : " + "\n" + commandResponseAsString +  "\n" +
							"\nError Msg : " + pe.getMessage());
				}
			
			} else {

				parsingFailureCount++;
				parsedMetric.setSuccess(false);
				parsedMetric.setResult(null);
				parsedMetric.setParseFailMsg(
						"\nError : " + commandResponseParser.getParserName() + " Script parsing failure\n" +
						"Error : Script parsing failure.  Neither a valid numeric or null returned : [" + groovyScriptResult + "]." +
						"\nParser : " + commandResponseParser.getParserName() +
						"\nCommand Response was : " + "\n" + commandResponseAsString);
			}
			
		} catch (Exception e) {
			parsingFailureCount++;
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			parsedMetric.setSuccess(false);	
			parsedMetric.setResult(null);
			parsedMetric.setParseFailMsg(
					"\nError: " + commandResponseParser.getParserName()	+ " parser failed to processes command response.\n" + 
					"\nCommand Response was : " + "\n" 	+ commandResponseAsString + "\n" + e.getMessage() + "\n" + stackTrace.toString());						
		}
		return parsedMetric;
	}
			

	private static List<String> logParsedMetrics(List<ParsedMetric> parsedMetrics) {
		List<String> logLines = new ArrayList<>();
			
		for (ParsedMetric parsedMetric  : parsedMetrics) {
			logLines.add(indent + "Txn label : " +  parsedMetric.getLabel());   
			logLines.add(indent + "Value     : " +  parsedMetric.getResult());
			String txnPassedFormatted = "<font color='green'><b>true</b></font><br>";
			if (!parsedMetric.getSuccess()) { 
				txnPassedFormatted = "<font color='red'>false</font><br>";
			}
			logLines.add(indent + "Success :  " +  txnPassedFormatted);
		}
		return logLines;
	}

	
	private static List<String> logParsedMetric(ParsedMetric parsedMetric) {
		List<String> logLines = new ArrayList<>();

		logLines.add(indent + "Txn label : " +  parsedMetric.getLabel());   
		logLines.add(indent + "Value     : " +  parsedMetric.getResult());

		String txnPassedFormatted = "<font color='green'><b>true</b></font><br>";
		if (!parsedMetric.getSuccess()) { 
			txnPassedFormatted = "<font color='red'>false</font><br>";
			logLines.add(indent + parsedMetric.getParseFailMsg().replaceAll("\n", "<br>") );
		} 
		logLines.add(indent + "Success :  " +  txnPassedFormatted);
		return logLines;
	}


	private static String summariseResponse(boolean testMode, Instant serverProfileStarts, Instant serverProfileEnds) {
		String testModeResult= "";
		
		if (testMode){
			String elapsedSec = " [" + new BigDecimal(Duration.between(serverProfileStarts, serverProfileEnds)
					.toMillis()).setScale(2, RoundingMode.HALF_UP)
					.divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP)	+ "s]";

			if (parsingSuccessCount == 0) {
				testModeResult = "<font color='red'> You have not received any metrics back!  "
						+ "Please check your commands (" + commandFailureCount + " failures recorded), "
						+ "connectivity and other settings." + elapsedSec + "</font>";
			} else if (parsingFailureCount > 0 || commandFailureCount > 0) {
				testModeResult = "<font color='orange'> " + parsingFailureCount + " out of "
						+ (parsingSuccessCount + parsingFailureCount) + " command response parser(s) have failed, "
						+ commandFailureCount + " command(s) failed." + elapsedSec + "</font>";
			} else {
				testModeResult = "<font color='green'> You have received metrics results!  "
						+ "Please check the values are as you expect." + elapsedSec + "</font>";
			}
		}
		return testModeResult;
	}

	
	private static void testModeLog(List<String> logLines, String string) {
		logLines.add(string);
	}
	
	private static void testModeLog(List<String> logLines, List<String> stringList) {
		logLines.addAll(stringList);
	}

	
	private static boolean isaParsableDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException ignored) {}
		return false;
	}	
	
}
