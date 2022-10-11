/*
  Copyright 2019 Insurance Australia Group Limited
 
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

	function isEmptyOrSpaces(str) {
		return str === null || str.match(/^ *$/) !== null;
	}

	function populatePasswordCipher(pwdCipher, idprefix) {
		var passwordCipherId = document.getElementById(idprefix + 'passwordCipher');
		passwordCipherId.value = pwdCipher;
		document.getElementById(idprefix + 'password').value = '';
		document.getElementById("createCipherBtn").disabled = true;
	}

	function enableOrdisableCreateCipherBtn(idprefix) {
		if (isEmptyOrSpaces(document.getElementById(idprefix + 'password').value)) {
			document.getElementById("createCipherBtn").disabled = true;
		} else {
			document.getElementById("createCipherBtn").disabled = false;
		}
	}

	
	function testConnection() {

		document.getElementById('testConnectionTestModeResult').innerHTML = "Processing your request ..."	;
		hideElement('responseTable');	
			
		var serverProfile = document.getElementById('serverProfile').innerHTML
		
		var xhttp = new XMLHttpRequest();
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4 && this.status == 200) {
				populateTestConnectionResult(this.responseText)
			}
		};
		xhttp.open("GET", encodeURI("api/metric?reqServerProfileName="	+ serverProfile  + "&reqTestMode=true" ), true);
		xhttp.send();
	}

	
	function populateTestConnectionResult(testConnectionResponse) {
		showElement('responseTable');
		var jsonResp = JSON.parse(testConnectionResponse);
		document.getElementById('testConnectionTestModeResult').innerHTML = jsonResp.testModeResult;
		document.getElementById('testConnectionServerProfile').innerHTML =  jsonResp.serverProfileName;  
		document.getElementById('testConnectionCommandResponses').innerHTML =  formatJSONkeyValues(jsonResp.parsedCommandResponses);
		document.getElementById('testConnectionLogLines').innerHTML =  jsonResp.logLines;
	}
	
	
	function formatJSONkeyValues(parsedCommandResponses){
		var formatedMetric = 
			'<table class="nb" width="100%"><tr><th>Transaction</th><th>Value</th><th>Pass/Fail</th><th></th><th></th></tr>'

		Object.entries(parsedCommandResponses).forEach(([key, parsedCommandResponse]) => {
//		    alert('key=' + key + ' ,commandName=' + parsedCommandResponse.commandName + ' ,parsedMetrics=' + parsedCommandResponse.parsedMetrics )
		    
		    const parsedMetrics = parsedCommandResponse.parsedMetrics
			
			Object.entries(parsedMetrics).forEach(([key, metric]) => {
//			    alert('key=' + key + ', label=' + metric.label + ', result=' + metric.result + ', success=' + metric.success + ", dt=" + metric.dataType )
				
		    	var passOrFail = "<font color='red'><b>Fail</b></font>"
		    	if (metric.success == true ){
		    		passOrFail = "<font color='green'>Pass</font>"
		    	}
		    	var dataType = ""
		    	if (metric.dataType != null ){
		    		dataType = metric.dataType
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
	
	
	function populateOsDefaults(idprefix) {
		selectedExecutor = document.getElementById(idprefix + 'executor').value;
		if (selectedExecutor == 'SSH_LINUX_UNIX') {
			document.getElementById(idprefix + 'connectionPort').value = '22';
			document.getElementById(idprefix + 'connectionTimeout').value = '60000'
		} else {
			document.getElementById(idprefix + 'connectionPort').value = '';
			document.getElementById(idprefix + 'connectionTimeout').value = ''
		}
	}
		
	
	function visibilyForCommandExecutor(){
		var executor = document.getElementById("command.executor").value;
		var winOnlyPredefinedVars = document.getElementById("winOnlyPredefinedVars");
		var groovyPredefinedVars  = document.getElementById("groovyPredefinedVars");
		var paramNamesRow = document.getElementById("paramNamesRow");
		var responseParsersRow = document.getElementById("responseParsersRow");

		winOnlyPredefinedVars.style.display = 'none';
		groovyPredefinedVars.style.display  = 'none';
		paramNamesRow.style.display         = 'none';
		responseParsersRow.style.display    = 'none';
				
		if (executor == "GROOVY_SCRIPT"){
			groovyPredefinedVars.style.display  = 'block';
			paramNamesRow.style.display = 'table-row';
		} else if (executor == "WMIC_WINDOWS" || executor == "SSH_LINUX_UNIX"){
			responseParsersRow.style.display    = 'table-row';
			if (executor == "WMIC_WINDOWS" ) {
				winOnlyPredefinedVars.style.display = 'block';
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
	
	
	function buildApiLink(){
		var host =  window.location.host; 	
		document.getElementById('apiLInk').href = "http://" + host + "/mark59-metrics/api/metric?reqServerProfileName=" + encodeURIComponent(document.getElementById("serverProfile").innerText);  	
	}
		