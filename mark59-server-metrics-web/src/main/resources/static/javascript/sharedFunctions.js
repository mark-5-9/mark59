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
	
	
	function formatJSONkeyValues(json){
		var formatedMetric = 
			'<table class="nb" width="100%"><tr><th>Transaction</th><th>Value</th><th>Pass/Fail</th></tr>'

		for (var key in json) {
		    if (json.hasOwnProperty(key)) {
		    	
		    	var passOrFail = "<font color='red'><b>Fail</b></font>"
		    	if (json[key].txnPassed == 'Y'  ){
		    		passOrFail = "<font color='green'>Pass</font>"
		    	}
		    		
		        formatedMetric = formatedMetric + 
		        '<tr>' +
		           '<td>' + json[key].candidateTxnId + '</td>' +
		           '<td>' + json[key].parsedCommandResponse + '</td>' +		           
		           '<td>' + passOrFail + '</td>' +		           
		        '</tr>'   
		    }
		}
		return formatedMetric + '</table>';
	}
	
	
	function testCommandResponseParser() {
		var scriptNameId = document.getElementById('scriptName').innerHTML
		var xhttp = new XMLHttpRequest();
		xhttp.onreadystatechange = function() {
			if (this.readyState == 4 && this.status == 200) {
				populateTestCommandResponseParserResult(this.responseText)
			} 
		};
		xhttp.open("GET", encodeURI("api/testCommandResponseParser?scriptName="	+ scriptNameId ), true);
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
		selectedOS = document.getElementById(idprefix + 'operatingSystem').value;
		if (selectedOS == 'WINDOWS') {
			document.getElementById(idprefix + 'connectionPort').value = '';
			document.getElementById(idprefix + 'connectionTimeout').value = ''
		} else {
			document.getElementById(idprefix + 'connectionPort').value = '22';
			document.getElementById(idprefix + 'connectionTimeout').value = '60000'
		}
		loadCommandListForSelected(idprefix)
	}
		
	
	function loadCommandListForSelected(idprefix) {
		selectedOS = document.getElementById(idprefix + 'operatingSystem').value;
		var i=0;		
		while ( document.getElementById('commandSelectors' + i + '.executor') ) {
			
			if (selectedOS == 'WINDOWS') {
				if (document.getElementById('commandSelectors' + i + '.executor').value == "SSH_LINIX_UNIX" ){
					document.getElementById('commandSelectors' + i + '.commandChecked1').checked = false;	
					hideElement('commandSelectors' + i) ;	
				} else {
					showElement('commandSelectors' + i) ;
				}
			} else {  // LINUX or UNIX
				if (document.getElementById('commandSelectors' + i + '.executor').value == "WMIC_WINDOWS" ){
					document.getElementById('commandSelectors' + i + '.commandChecked1').checked = false;	
					hideElement('commandSelectors' + i) ;	
				} else {
					showElement('commandSelectors' + i) ;
				}				
			};
			i ++;
		};
	}
	
	
	function displayWinOnlyPredefinedVars(){
		var selectedExecutorValue = document.getElementById("command.executor").value;
		var winOnlyPredefinedVars = document.getElementById("winOnlyPredefinedVars");
		if (selectedExecutorValue == "WMIC_WINDOWS" ) {
			winOnlyPredefinedVars.style.display = 'block';
		} else {
			winOnlyPredefinedVars.style.display = 'none';
		}
	}
	
	
	function buildApiLink(){
		//eg:  http://mysever:8085/mark59-server-metrics-web/api/metric?reqServerProfileName=localhost_WINDOWS
		var host =  window.location.host; 	
		url="http://" + host + "/mark59-server-metrics-web/api/metric?reqServerProfileName=" + document.getElementById("serverProfile").innerText
		document.getElementById('apiLInk').href = url;  	
	}	
		