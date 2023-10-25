/*
  Copyright 2019 Mark59.com
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

*/


	function createCipher(idprefix) {
		var xhttp = new XMLHttpRequest();
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4 && this.status == 200) {
				populatePasswordCipher(this.responseText, idprefix)
			}
		};
		xhttp.open("GET", encodeURI("api/cipher?pwd="	+ document.getElementById(idprefix + 'password').value), true);
		xhttp.send();
	}


	function populatePasswordCipher(pwdCipher, idprefix) {
		var passwordCipherId = document.getElementById(idprefix + 'passwordCipher');
		passwordCipherId.value = pwdCipher;
		document.getElementById(idprefix + 'password').value = '';
		document.getElementById("createCipherBtn").disabled = true;
	}

	function enableOrdisableCreateCipherBtn(idprefix) {
		if (document.getElementById(idprefix + 'password') !== null){
			if (isEmpty(document.getElementById(idprefix + 'password').value)) {
				document.getElementById("createCipherBtn").disabled = true;
			} else {
				document.getElementById("createCipherBtn").disabled = false;
			}
		}
	}

	
	function testConnection(testMode) {
		if (testMode == 'true' ){ 
			document.getElementById('testConnectionTestModeResult').innerHTML = "Processing your request ..."	;
			hideElement('responseTable');	
		}	
			
		var serverProfile = document.getElementById('serverProfile').innerHTML
		
		var xhttp = new XMLHttpRequest();
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4 && this.status == 200) {
				if (testMode == 'true' ){ 
					populateTestConnectionResultFormatted(this.responseText)
				} else {
					populateTestConnectionResultRaw(this.responseText)
				}	
			}
		};
		xhttp.open("GET", encodeURI("api/metric?reqServerProfileName="	+ serverProfile  + "&reqTestMode=" + testMode ), true);
		//xhttp.setRequestHeader("Authorization", "Basic " + "c2FtcGxldXNlcjpzYW1wbGVwYXNz" );
		if (!isEmpty(document.getElementById('apiAuthToken').value)) { 
			xhttp.setRequestHeader("Authorization", "Basic " + document.getElementById('apiAuthToken').value);
		}	
		xhttp.send();
	}

	
	function populateTestConnectionResultFormatted(testConnectionResponse) {
		showElement('responseTable');
		var jsonResp = JSON.parse(testConnectionResponse);
		document.getElementById('testConnectionTestModeResult').innerHTML = jsonResp.testModeResult;
		document.getElementById('testConnectionServerProfile').innerHTML =  jsonResp.serverProfileName;  
		document.getElementById('testConnectionCommandResponses').innerHTML =  formatJSONkeyValues(jsonResp.parsedCommandResponses);
		document.getElementById('testConnectionLogLines').innerHTML =  jsonResp.logLines;
	}

	
	function populateTestConnectionResultRaw(connectionResponse) {
		var tab = window.open('about:blank', '_blank');
		tab.document.write("<!doctype html><html><body><textarea style='width:100%;height:500px;'>" + connectionResponse + "</textarea></body></html>" ); 
		tab.document.close();
	}
	
	
	function formatJSONkeyValues(parsedCommandResponses){
		var formatedMetric = 
			'<table class="nb" width="100%"><tr><th>Transaction</th><th>Value</th><th>Pass/Fail</th><th></th><th></th></tr>'

		Object.entries(parsedCommandResponses).forEach(([keyR, parsedCommandResponse]) => {
//		    alert('key=' + key + ' ,commandName=' + parsedCommandResponse.commandName + ' ,parsedMetrics=' + parsedCommandResponse.parsedMetrics )
		    
		    const parsedMetrics = parsedCommandResponse.parsedMetrics
			
			Object.entries(parsedMetrics).forEach(([keyM, metric]) => {
//			    alert('key=' + key + ', label=' + metric.label + ', result=' + metric.result + ', success=' + metric.success + ", dt=" + metric.dataType )
				
		    	var passOrFail = "<font color='red'><b>Fail</b></font>"
		    	if (metric.success == true ){
		    		passOrFail = "<font color='green'>Pass</font>"
		    	}
		    	var dataType = ""
		    	if (metric.dataType != null ){
		    		dataType = metric.dataType
		    	} else {
					dataType = '<font color="grey">Unexpected null for a datatype? ('+keyR+':'+keyM+')</font>'
				}				    
		        formatedMetric = formatedMetric + 
		        '<tr>' +
		           '<td nowrap style="width:1%">' + metric.label + '&nbsp;&nbsp;</td>' +
		           '<td nowrap style="width:1%">' + parseInt(metric.result) + '&nbsp;&nbsp;</td>' +		           
		           '<td nowrap style="width:1%">' + passOrFail + '&nbsp;&nbsp;</td>' +		           
		           '<td nowrap style="width:1%">' + dataType + '&nbsp;&nbsp;</td>' +		           
		           '<td></td>' 		           
		        '</tr>'  			    

			});		
		});		
		return formatedMetric + '</table>';
	}
	
	
	function testCommandResponseParser() {
		var parserNameId = document.getElementById('parserName').innerHTML
		var xhttp = new XMLHttpRequest();
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4 && this.status == 200) {
				populateTestCommandResponseParserResult(this.responseText)
			} 
		};
		xhttp.open("GET", encodeURI("api/testCommandResponseParser?parserName="	+ parserNameId ), true);
		xhttp.send();
	}

	
	function populateTestCommandResponseParserResult(testCommandResponseParserResponse) {
		showElement('testCommandResponseTable');
		var jsonResp = JSON.parse(testCommandResponseParserResponse);
		document.getElementById('testCommandResponseParserSummary').innerHTML = jsonResp.summary;
		document.getElementById('testCommandResponseParserCandidateTxnId').innerHTML = jsonResp.candidateTxnId;
		document.getElementById('testCommandResponseParserResult').innerHTML =  jsonResp.parserResult;
	}
	
	
	function hideElement(id){
		document.getElementById(id).style.display = 'none';
	}
	
	function showElement(id){
		document.getElementById(id).style.display = 'block';
	}
	
	
	function visibilityForLocalhost() {
		if (document.getElementById("serverProfile.server") !== null){
			var server = document.getElementById("serverProfile.server").value;
			var usernameRow = document.getElementById("usernameRow");
			var passwordRow = document.getElementById("passwordRow");
			var passwordCipherRow = document.getElementById("passwordCipherRow");
	
			usernameRow.style.display = 'table-row';
			passwordRow.style.display = 'table-row';
			passwordCipherRow.style.display = 'table-row';
	
			if (server == 'localhost') {
				usernameRow.style.display = 'none';
				passwordRow.style.display = 'none';
				passwordCipherRow.style.display = 'none';
				document.getElementById("serverProfile.username").value = ''
				document.getElementById("serverProfile.password").value = ''
				document.getElementById("serverProfile.passwordCipher").value = ''
			}
		}
	}
		
	
	function visibilyForCommandExecutor(){
		var executor = document.getElementById("command.executor").value;
		var predefinedVars = document.getElementById("predefinedVars");
		var ignoreStdErrRow = document.getElementById("ignoreStdErrRow");
		var specialParamNames = document.getElementById("specialParamNames");		
		var responseParsersRow = document.getElementById("responseParsersRow");

		// stdEr output is not relevent for Groovy scripts, so hide 
		ignoreStdErrRow.style.display = 'table-row';
		if (executor == "GROOVY_SCRIPT"){
			ignoreStdErrRow.style.display = 'none';
		} 
		
		// for *nix special parms can be defined for alternate ssh connection. 
		specialParamNames.innerHTML  = '';
		if (executor == "SSH_LINUX_UNIX"){
			specialParamNames.innerHTML  = 'SSH_IDENTITY, SSH_PASSPHRASE, SSH_KNOWN_HOSTS, SSH_PREFERRED_AUTHENTICATIONS ' + 
					'can be added for alternative connection types. See Overview or Mark59 User Guide.';
		} 
		
		// Parsers are not used for Groovy commandss 
		responseParsersRow.style.display = 'table-row';
		if (executor == "GROOVY_SCRIPT" ){
			responseParsersRow.style.display = 'none';
		} 

		// print pre-defined params available for each command type	
		if (isEmpty(executor)){
			predefinedVars.innerHTML  = '';
		} else if (executor == "GROOVY_SCRIPT"){
			predefinedVars.innerHTML  = '<b>predefined parameter:</b> serverProfile (eg serverProfile.serverProfileName). ' + 
										'<b>return:</b> an object of type ScriptResponse should be returned';
		} else if (executor == "WMIC_WINDOWS" || executor == "POWERSHELL_WINDOWS" || executor == "SSH_LINUX_UNIX"){
			predefinedVars.innerHTML  = '<b>predefined parameters:</b> ${PROFILE_NAME}, ${PROFILE_SERVER}, ${PROFILE_USERNAME}';
			if (executor == "POWERSHELL_WINDOWS" ) {
				predefinedVars.innerHTML += ', ${PROFILE_PASSWORD} (actual)'; 
			} 			
			if (executor == "WMIC_WINDOWS" || executor == "POWERSHELL_WINDOWS") {
				predefinedVars.innerHTML += ', ${METRICS_BASE_DIR}'; 
			} 			
		}
	}
	
	
	function sizeToFitText(id){
		var textarea = document.getElementById(id);
		textarea.style.width = "100%";
		textarea.style.height = "";
		textarea.style.height = textarea.scrollHeight + "px";
	}
	

	function resubmitToRefreshParm() {
		document.getElementById("selectedScriptCommandNameChanged").value = 'true';
		document.getElementById("serverProfileEditingForm").submit();
	}
	
	function submitSaveCommand(saveCmdAction) {
		document.getElementById("saveCmdAction").value = saveCmdAction;
		document.getElementById("commandEditingForm").submit();
	}
	
	
	
	function isEmpty(str) {
		return str === null || str === ""
	}


	function trimkey(key) {
		key.value = key.value.trim();
	}

		