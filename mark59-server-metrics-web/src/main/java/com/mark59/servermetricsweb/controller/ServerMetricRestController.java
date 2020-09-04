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

package com.mark59.servermetricsweb.controller;

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
import com.mark59.servermetricsweb.data.beans.CommandResponseParser;
import com.mark59.servermetricsweb.data.commandResponseParsers.dao.CommandResponseParsersDAO;
import com.mark59.servermetricsweb.data.commandparserlinks.dao.CommandParserLinksDAO;
import com.mark59.servermetricsweb.data.commands.dao.CommandsDAO;
import com.mark59.servermetricsweb.data.servercommandlinks.dao.ServerCommandLinksDAO;
import com.mark59.servermetricsweb.data.serverprofiles.dao.ServerProfilesDAO;
import com.mark59.servermetricsweb.pojos.TestCommandParserResponsePojo;
import com.mark59.servermetricsweb.utils.ServerMetricsWebUtils;
import com.mark59.servermetricsweb.utils.TargetServerCommandProcessor;

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
	 *  Service call to profile.
	 *  <p>Calls a functions which controls and executed commands on the target servers, and returns the formatted response.
	 *  <p>With the mark59 framework, will be called by implementation(s) of JMeter Java Samplers designed to cater for 
	 *  metrics capture directly via this API call (refer to See Also).
	 *  <p> For example using profile localhost_HOSTID, and from default setting localhost, the url used would be <br>
	 *  http://localhost:8085/mark59-server-metrics-web/api/metric?reqServerProfileName=localhost_HOSTID
	 *  
	 * @param reqServerProfileName
	 * @param reqTestMode
	 * @return org.springframework.http.ResponseEntity (Json format)
	 * @see ServerMetricsCaptureViaWeb
	 */
	@GetMapping(path =  "/metric")
	public ResponseEntity<Object> apiMetric(@RequestParam String reqServerProfileName, @RequestParam(required=false) String reqTestMode){
	
		return ResponseEntity.ok(TargetServerCommandProcessor.serverResponse(reqServerProfileName, reqTestMode, 
				serverProfilesDAO, serverCommandLinksDAO, commandsDAO, commandParserLinksDAO, commandResponseParsersDAO));	
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
	public ResponseEntity<Object> testCommandResponseParser(@RequestParam(required=false) String scriptName){
		
		// System.out.println("ServerMetricRestController.testCommandResponseParser script : " + scriptName );
		TestCommandParserResponsePojo testResponse = new TestCommandParserResponsePojo();
		CommandResponseParser commandResponseParser = commandResponseParsersDAO.findCommandResponseParser(scriptName); 
			
		String candidateTxnId = Mark59Utils.constructCandidateTxnIdforMetric(
				commandResponseParser.getMetricTxnType(), "{SERVER}", commandResponseParser.getMetricNameSuffix()) ;
		testResponse.setCandidateTxnId(candidateTxnId);
		
		Object groovyScriptResult = null;
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
			Double doubleVal = Double.parseDouble(groovyScriptResult.toString());
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
