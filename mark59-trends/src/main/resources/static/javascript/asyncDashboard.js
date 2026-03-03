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

var csv = null;
var csvArray = null;
var data = null;
var urlPopulateSlaResult = "init"


function asyncPopulationOfSlaResults() {
	
	var signalTxtElmt = document.getElementById("signal");
	var signalIxElmt = document.getElementById("signalix");	

	signalTxtElmt.addEventListener('input', (e) => {
		reqApp = 	e.target.value;	
		console.log('calling addEventListener: ', reqApp);
		urlPopulateSlaResult = "http://" + host + "/mark59-trends/api/slaIconColoursForRun?reqApp=" + reqApp;
		console.log('                     url: ', urlPopulateSlaResult  );
		ajaxRequestRegisterApplicationListChange(urlPopulateSlaResult);
	});
	
	var host =  window.location.host; 
	var appList = document.getElementById("dashboardAppList").value;
	console.log("dashboardAppList on initial load : " + appList );
	var appListAry = appList.split(",");
	
	signalIxElmt.value = 0;
	var firstApp = appListAry[signalIxElmt.value];
	signalTxtElmt.value=firstApp
	console.log("firstApp="+firstApp); 
	
	signalTxtElmt.dispatchEvent(new Event('input', { bubbles: true })); // trigger listener
}


function ajaxRequestRegisterApplicationListChange(urlPopulateSlaResult) {
	httpRequest = new XMLHttpRequest();

	if (!httpRequest) {
		alert('Giving up :( Cannot create an XMLHTTP instance');
		return false;
	}
	httpRequest.onreadystatechange = getAjaxResponseAndPopulateSlaResult
	httpRequest.open('GET', urlPopulateSlaResult);
	httpRequest.send();
} 


function getAjaxResponseAndPopulateSlaResult() {
	console.log("at getAjaxResponseAndPopulateSlaResult"); // + reqApp );
	
	if (httpRequest.readyState === XMLHttpRequest.DONE) {
		if (httpRequest.status === 200) {
			// response from controller:  reqApp+","+slaSummaryIcon+","+slaTransactionIcon+","+slaMetricsIcon;
			slaResultResponse =  httpRequest.responseText;
			console.log("request complete : slaResultResponse (js): " + slaResultResponse );			
			var appListAry = slaResultResponse.split(",");
			var reqApp = appListAry[0];
//			var reqRunTime = appListAry[1];
			var slaSummaryIcon = appListAry[2];
			var slaTransactionIcon = appListAry[3];
			var slaMetricsIcon = appListAry[4];
			
			var slaSummaryIconElmt = document.getElementById(reqApp+"slaSummaryIcon");
			slaSummaryIconElmt.src = "images/"+slaSummaryIcon+".png";
			var slaTransactionIconElmt = document.getElementById(reqApp+"slaTransactionIcon");
			slaTransactionIconElmt.src = "images/"+slaTransactionIcon+".png";
			var slaMetricsIconElmt = document.getElementById(reqApp+"slaMetricsIcon");
			slaMetricsIconElmt.src = "images/"+slaMetricsIcon+".png";							
			
			var signalIxElmt = document.getElementById("signalix");
			var signalTxtElmt = document.getElementById("signal");
			var appList = document.getElementById("dashboardAppList").value;
						
			var ix = Number(signalIxElmt.value);
			console.log("current ix : " + ix );
			ix = ix + 1;
			var appListAry = appList.split(",");
			signalIxElmt.value = ix;
			console.log("next ix : " + ix );	
			
			var appCount = appListAry.length;
			console.log("appList.length : " + appListAry.length );
			console.log("appCount : " + appCount );
			
			if ( ix < appCount ){
				signalTxtElmt.value = appListAry[ix];
				console.log('signalTxtElmt value changed to ' + signalTxtElmt.value); 
				signalTxtElmt.dispatchEvent(new Event('input', { bubbles: true })); // trigger listener
			} else {
				console.log("we are done !!!!!!!!!! " );
			}
		} else {
			alert('There was a problem with the request: '  + httpRequest.responseText );
		}
	} else {
		console.log('Not there yet: state: ' + httpRequest.readyState +" ,status:"+ httpRequest.status + " ,resp: "+ httpRequest.responseText );
	}
}


function clearApplicationDropdownOptions(applicationSelectBox) {
    var i;
    for (i = applicationSelectBox.options.length - 1 ; i >= 0 ; i--){
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
