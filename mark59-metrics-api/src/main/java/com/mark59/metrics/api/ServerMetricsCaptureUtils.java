package com.mark59.metrics.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.metrics.api.utils.MetricsApiConstants;
import com.mark59.metrics.drivers.ServerProfileRunner;
import com.mark59.metrics.pojos.ParsedCommandResponse;
import com.mark59.metrics.pojos.ParsedMetric;
import com.mark59.metrics.pojos.WebServerMetricsResponsePojo;
import com.mark59.metrics.utils.MetricsConstants;

public class ServerMetricsCaptureUtils {

	private static final Logger LOG = LogManager.getLogger(ServerMetricsCaptureUtils.class);
	
	public static final String FULL	= "full";
	public static final String NO	= "no";		
	
	
	/**
	 * throw a runtime exception for a duff Server Profile
	 * @param response WebServerMetricsResponsePojo
	 */
	public static void validateCommandsResponse(WebServerMetricsResponsePojo response) {
		if (response.getServerProfileName() == null){
			throw new RuntimeException("Error : null server profile name returned is an Unexpected Response!");
		}
		if ( response.getFailMsg().startsWith(MetricsConstants.SERVER_PROFILE_NOT_FOUND)){
			throw new RuntimeException("Error : " + response.getFailMsg());
		}
	}
		
	
	/**
	 * Create JMeter transactions for each parsed metric of the command.  
	 * <p>Also controls logging of errors by the API. 
	 * 
	 * @param context JavaSamplerContext
	 * @param jm JmeterFunctions
	 * @param reqServerProfileName the Server Profile 
	 * @param parsedCommandResponse command response being processed
	 * 
	 * @see #outputErrorMsg(JavaSamplerContext, String)
	 */
	public static void createJMeterTxnsUsingCommandResponse(JavaSamplerContext context, JmeterFunctions jm,
			String reqServerProfileName, ParsedCommandResponse parsedCommandResponse) {
		
		if (parsedCommandResponse.isCommandFailure()){  
			outputErrorMsg(context, parsedCommandResponse.getCommandResponse());

		} else {	
			
			for (ParsedMetric parsedMetric  : parsedCommandResponse.getParsedMetrics()) {
				
				if (parsedMetric.getSuccess()) {
					jm.userDatatypeEntry(
							parsedMetric.getLabel(), 
							parsedMetric.getResult().longValue(),
							JMeterFileDatatypes.valueOf(parsedMetric.getDataType()));
				} else {
					String metricFailsMsg = "Parser Fails for profile: " + reqServerProfileName
							+ " command: " + parsedCommandResponse.getCommandName()
							+ "\ndetails: "+ parsedMetric.getParseFailMsg();
					outputErrorMsg(context, metricFailsMsg);
				}	
			}
		}
	}


	/**
	 * <p>Error message format will depend on the 'PRINT_ERROR_MESSAGES' and 'LOG_ERROR_MESSAGES'
	 *  JMeter (context) parameter values:
	 *  <br><code>'short'</code> is the default (first 2000 chars are output), 
	 *  <br><code>'no'</code> for no output, and
	 *  <br><code>'full'</code> for the entire error message (stack trace) to print.  
	 * 
	 * <p>PRINT_ERROR_MESSAGES sends output to console, LOG_ERROR_MESSAGES to log4j
	 * 
	 * <p>** Please Note ** This is independent of any logging done by the 'metrics' application
	 * itself when running the profiles and building the command response (refer below) 
	 * 
	 * @param context JavaSamplerContext (holds the JMeter parameters)
	 * @param cmdFailureMsg the failure message
	 * 
	 * @see ServerProfileRunner#commandsResponse
	 * @see MetricsApiConstants#LOG_ERROR_MESSAGES 
	 * @see MetricsApiConstants#PRINT_ERROR_MESSAGES 
	 */
	public static void outputErrorMsg(JavaSamplerContext context, String cmdFailureMsg) {
		if (FULL.equalsIgnoreCase(context.getParameter(MetricsApiConstants.LOG_ERROR_MESSAGES))) {
			LOG.warn(cmdFailureMsg );
		} else if (!NO.equalsIgnoreCase(context.getParameter(MetricsApiConstants.LOG_ERROR_MESSAGES))) { 
			// default to 'short' action
			LOG.warn(StringUtils.abbreviate(cmdFailureMsg, 2000));
		}
		
		if (FULL.equalsIgnoreCase(context.getParameter(MetricsApiConstants.PRINT_ERROR_MESSAGES))) {
			System.out.println(cmdFailureMsg);
		} else if (!NO.equalsIgnoreCase(context.getParameter(MetricsApiConstants.PRINT_ERROR_MESSAGES))) { 
			System.out.println(StringUtils.abbreviate(cmdFailureMsg, 2000));
		}
	}


	/**
	 * Common code to handle serious exceptions during Server Metrics Capture
	 * 
	 * @param response WebServerMetricsResponsePojo
	 * @param e the unexpected exception (actually Throwable)
	 * @param source to identify the server metrics capture class executing
	 */
	public static void logUnexpectedException(WebServerMetricsResponsePojo response, Throwable e, String source) {
		StringWriter stackTrace = new StringWriter();
		e.printStackTrace(new PrintWriter(stackTrace));
		String errorMsg = "Error: Unexpected Failure during " + source + " execution.\n" + e.getMessage() + "\n" + stackTrace.toString();
		LOG.error(errorMsg);
		System.out.println(errorMsg);
		if (response != null) {
			String erroredServerProfleMsg = "        occurred using server profile :" + response.getServerProfileName();	
			LOG.error(erroredServerProfleMsg);	
			System.out.println(erroredServerProfleMsg);	
		}
	}
	
}
