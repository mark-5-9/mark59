package com.mark59.metrics.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.interfaces.JmeterFunctions;
import com.mark59.core.utils.Mark59Constants.JMeterFileDatatypes;
import com.mark59.metrics.api.utils.AppConstantsServerMetrics;
import com.mark59.metrics.pojos.ParsedCommandResponse;
import com.mark59.metrics.pojos.ParsedMetric;
import com.mark59.metrics.pojos.WebServerMetricsResponsePojo;
import com.mark59.metrics.utils.AppConstantsServerMetricsWeb;

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
		if ( response.getFailMsg().startsWith(AppConstantsServerMetricsWeb.SERVER_PROFILE_NOT_FOUND)){
			throw new RuntimeException("Error : " + response.getFailMsg());
		}
	}
		
	
	/**
	 * Create JMeter transactions for each parsed metric of the command.  Also
	 *  controls logging of errors.  Error message format will depend on the passed 
	 *  'PRINT_ERROR_MESSAGES' JMeter (context) parameter. 
	 * 
	 * @param context JavaSamplerContext
	 * @param jm JmeterFunctions
	 * @param reqServerProfileName the Server Profile 
	 * @param parsedCommandResponse command response being processed
	 */
	public static void createJMeterTxnsUsingCommandResponse(JavaSamplerContext context, JmeterFunctions jm,
			String reqServerProfileName, ParsedCommandResponse parsedCommandResponse) {
		
		if (parsedCommandResponse.isCommandFailure()){  
			String cmdFailureMsg = parsedCommandResponse.getCommandResponse();

			if (FULL.equalsIgnoreCase(context.getParameter(AppConstantsServerMetrics.PRINT_ERROR_MESSAGES))) {
				System.out.println(cmdFailureMsg);
				LOG.warn(cmdFailureMsg );
			} else if (!NO.equalsIgnoreCase(context.getParameter(AppConstantsServerMetrics.PRINT_ERROR_MESSAGES))) { 
				// defaults to 'short' action
				System.out.println(StringUtils.abbreviate(cmdFailureMsg, 2000));
				LOG.warn(StringUtils.abbreviate(cmdFailureMsg, 2000));
			}

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
					
					if (FULL.equalsIgnoreCase(context.getParameter(AppConstantsServerMetrics.PRINT_ERROR_MESSAGES))){
						System.out.println(metricFailsMsg);
						LOG.warn(metricFailsMsg);
					} else if (!NO.equalsIgnoreCase(context.getParameter(AppConstantsServerMetrics.PRINT_ERROR_MESSAGES))){ 
						// defaults to 'short' action
						System.out.println(StringUtils.abbreviate(metricFailsMsg, 1200));
						LOG.warn(StringUtils.abbreviate(metricFailsMsg, 1200));
					}
				}	
			}
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
		String errorMsg = "Error: Unexpected Failure during" + source + " execution.\n" + e.getMessage() + "\n" + stackTrace.toString();
		LOG.error(errorMsg);
		System.out.println(errorMsg);
		if (response != null) {
			String erroredServerProfleMsg = "        occurred using server profile :" + response.getServerProfileName();	
			LOG.error(erroredServerProfleMsg);	
			System.out.println(erroredServerProfleMsg);	
		}
	}
	
}
