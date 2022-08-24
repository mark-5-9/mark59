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

package com.mark59.metrics.controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mark59.core.utils.Mark59Constants;
import com.mark59.core.utils.Mark59Utils;
import com.mark59.core.utils.SimpleAES;
import com.mark59.metrics.data.beans.CommandResponseParser;
import com.mark59.metrics.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.metrics.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.metrics.data.commands.dao.CommandsDAO;
import com.mark59.metrics.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.metrics.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.metrics.drivers.ServerProfileRunner;
import com.mark59.metrics.pojos.TestCommandParserResponsePojo;
import com.mark59.metrics.utils.ServerMetricsWebUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controls API calls from the server metrics web application, and from any JMeter Java Sampler implementation using a direct API call
 * to obtain metrics from a server (see ServerMetricsCaptureViaWeb).
 *   
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */

@RestController
@RequestMapping("/api")
public class ServerMetricRestController {
	
	private static final Logger LOG = LogManager.getLogger(ServerMetricRestController.class);	
	
	@Autowired
	ServerProfilesDAO serverProfilesDAO;	
	
	@Autowired
	ServerCommandLinksDAO serverCommandLinksDAO;
	
	@Autowired
	CommandsDAO commandsDAO;	
	
	@Autowired
	CommandParserLinksDAO commandParserLinksDAO;
	
	@Autowired
	CommandResponseParsersDAO commandResponseParsersDAO; 

		
	
	/**
	 *  Invoke Server Profile execution
	 *  <p>Calls a functions which controls and executed commands on the target servers, and returns the formatted response.
	 *  <p>Will be used by implementation(s) of JMeter Java Samplers designed to cater for 
	 *  metrics capture directly via this API call (refer to com.mark59.metrics.api.ServerMetricsCaptureViaWeb).
	 *  <p> For example using profile localhost_HOSTID, and from default setting localhost, the url used would be <br>
	 *  http://localhost:8085/mark59-metrics/api/metric?reqServerProfileName=localhost_HOSTID
	 *  
	 * @param reqServerProfileName  profile name
	 * @param reqTestMode whether running as a 'test' (eg directly from the web application UI)
	 * @return org.springframework.http.ResponseEntity (Json format)
	 */
	@GetMapping(path =  "/metric")
	public ResponseEntity<Object> apiMetric(@RequestParam String reqServerProfileName, @RequestParam(required=false) String reqTestMode){
	
		return ResponseEntity.ok(ServerProfileRunner.commandsResponse(reqServerProfileName, reqTestMode, 
				serverProfilesDAO, serverCommandLinksDAO, commandsDAO, commandParserLinksDAO, commandResponseParsersDAO, true));	
	}


		
	@GetMapping(path =  "/cipher")
	public ResponseEntity<Object> cipher(@RequestParam(required=false) String pwd) {
		// System.out.println("cipher called pwd : [" + pwd +"]");
		LOG.debug("cipher called pwd : [" + pwd +"]");
		String encrypted = SimpleAES.encrypt(pwd, Mark59Constants.REFERENCE );
		if (encrypted == null ) {
			encrypted = "Oops. Something went wrong attempting to encrypt this password (" + pwd + ")";
		}
		// System.out.println("     cipher response is : [" + encrypted +"]");
		LOG.debug("     cipher response is : [" + encrypted +"]");
        return ResponseEntity.ok(encrypted);
	}

	
	@GetMapping(path =  "/testCommandResponseParser")
	public ResponseEntity<Object> testCommandResponseParser(@RequestParam(required=false) String parserName){
		
		// System.out.println("ServerMetricRestController.testCommandResponseParser script : " + parserName );
		TestCommandParserResponsePojo testResponse = new TestCommandParserResponsePojo();
		CommandResponseParser commandResponseParser = commandResponseParsersDAO.findCommandResponseParser(parserName); 
			
		String candidateTxnId = Mark59Utils.constructCandidateTxnIdforMetric(
				commandResponseParser.getMetricTxnType(), "{SERVER}", commandResponseParser.getMetricNameSuffix()) ;
		testResponse.setCandidateTxnId(candidateTxnId);
		
		Object groovyScriptResult;
		try {
			groovyScriptResult = ServerMetricsWebUtils.runGroovyScript(commandResponseParser.getScript(), commandResponseParser.getSampleCommandResponse());
		} catch (Exception e) {
			testResponse.setSummary("<font color='red'>Script failure</font>");
			StringWriter outError = new StringWriter();
			e.printStackTrace(new PrintWriter(outError));
			String stackTraceStr = outError.toString().replaceAll("\\r\\n|\\r|\\n", "<br>");
			testResponse.setParserResult(e.getMessage() + "<br><br>" + stackTraceStr);
			return ResponseEntity.ok(testResponse);
		}
		
		try {
			double doubleVal = Double.parseDouble(groovyScriptResult.toString());
			if (doubleVal==0) {
				testResponse.setSummary("<font color='darkorange'> The parser has returned 0.  This is valid, but is it what you expected? <br>"
						+ " Maybe try a Sample Response that returns a non-zero positive value.. </font>");				
			} else if (doubleVal < 0) {
				testResponse.setSummary("<font color='darkorange'> The parser has returned negative value, which will be ignored.  Is it what you expected? <br>"
						+ " Maybe try a Sample Response that returns a non-zero positive value.. </font>");				
			} else { 
				testResponse.setSummary("<font color='green'> The parser has returned a valid number.  Please check the value is as you expect.. </font>");
			}
		} catch (Exception e) {
			testResponse.setSummary("<font color='red'> The parser has not returned a usable numeric value, please edit and retest ...</font>" );
		}
		
		if (groovyScriptResult != null) {
			testResponse.setParserResult(groovyScriptResult.toString() );
		} else {
			testResponse.setParserResult("<font color='red'>null</font>");
		}
        return ResponseEntity.ok(testResponse);
	}

	
}
