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

	function reloadSlaBulkLoadPage() {
		var applicationSelector = document.getElementById("application").value;
		var host =  window.location.host; 
		reloadSlaBulkLoadPageUrl = "http://" + host + "/metrics/asyncReloadSlaBulkLoadPage?reqApp=" + applicationSelector;   
		ajaxRequestRegisterApplicationListChange(reloadSlaBulkLoadPageUrl);
	}

	function ajaxRequestRegisterApplicationListChange(reloadSlaBulkLoadPageUrl) {
		httpRequest = new XMLHttpRequest();
		if (!httpRequest) {
			alert('Giving up :( Cannot create an XMLHTTP instance');
			return false;
		}
		httpRequest.onreadystatechange = getAjaxResponseReloadSlaBulkLoadPageUrl
		httpRequest.open('GET', reloadSlaBulkLoadPageUrl);
		httpRequest.send();
	} 

	function getAjaxResponseReloadSlaBulkLoadPageUrl() {
		if (httpRequest.readyState === XMLHttpRequest.DONE) {
			if (httpRequest.status === 200) {
				document.getElementById("slaRefUrl").value = httpRequest.responseText;				
				enableOrdisableApplyRefUrlOption()
				enableOrdisableSlaBulkLoadPageSubmitBtn();
			} else {
				alert('There was a problem with the request: '  + httpRequest.responseText );
			}
		}
	}
	
	function enableOrdisableApplyRefUrlOption() {
		if (isEmpty(document.getElementById("slaRefUrl").value)) {
			document.getElementById("applyRefUrlOption").disabled = true;
		} else {
			document.getElementById("applyRefUrlOption").disabled = false;
		}
	}
	
	function enableOrdisableSlaBulkLoadPageSubmitBtn() {
		var applicationSelector = document.getElementById("application").value;
		if (isEmpty(applicationSelector)) {
			document.getElementById("submit").disabled = true;
		} else {
			document.getElementById("submit").disabled = false;
		}
	}
	
	
	
	function isEmpty(str) {
		return str === null || str === ""
	}
	
	function clearInnerHTML(id){
		document.getElementById(id).innerHTML = "";
	}
	
	function hideElement(id){
		document.getElementById(id).style.display = 'none';
	}
	
	function showElement(id){
		document.getElementById(id).style.display = 'block';
	}
	
	
		