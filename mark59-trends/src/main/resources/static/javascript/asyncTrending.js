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

var csv = null;
var csvArray = null;
var data = null;

function asyncPopulationOfApplicationList() {
	
	var appListSelector = document.getElementById("appListSelector").value;
	var host =  window.location.host; 

	urlPopulateApplicationList = "http://" + host + "/mark59-trends/trendingAsyncPopulateApplicationList?reqAppListSelector=" + appListSelector;    // + urlGraphicData;
	ajaxRequestRegisterApplicationListChange(urlPopulateApplicationList);
}



function ajaxRequestRegisterApplicationListChange(urlPopulateApplicationList) {
	httpRequest = new XMLHttpRequest();

	if (!httpRequest) {
		alert('Giving up :( Cannot create an XMLHTTP instance');
		return false;
	}
	httpRequest.onreadystatechange = getAjaxResponseAndPopulateAppList
	httpRequest.open('GET', urlPopulateApplicationList);
	httpRequest.send();
} 



function getAjaxResponseAndPopulateAppList() {
	if (httpRequest.readyState === XMLHttpRequest.DONE) {
		if (httpRequest.status === 200) {
		
			applicationSelectBox = document.getElementById("application");			
			clearApplicationDropdownOptions(applicationSelectBox);
			buildApplicationDropdownOptions(applicationSelectBox, httpRequest.responseText);
		
		} else {
			alert('There was a problem with the request: '  + httpRequest.responseText );
		}
	}
}


function clearApplicationDropdownOptions(applicationSelectBox) {
    var i;
    for (i = applicationSelectBox.options.length - 1 ; i >= 0 ; i--)
    {
    	applicationSelectBox.remove(i);
    }
}


function buildApplicationDropdownOptions(applicationSelectBox, optionsText) {

	optionsArray = optionsText.split(',');
	
	for (var i = 0; i < optionsArray.length; i++) {
	    var optionElement = document.createElement("option");
	    optionElement.textContent 	= optionsArray[i];
	    optionElement.value 		= optionsArray[i];
	    applicationSelectBox.appendChild(optionElement);
	}	
}


