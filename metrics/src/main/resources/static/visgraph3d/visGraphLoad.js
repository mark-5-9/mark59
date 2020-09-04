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
  
  ########################################################################
  
  Attribution(s)
  --------------  
  The contents contained in this file use elements from the the vis.js product 
  (https://visjs.org/)
  
  The original contents did not contain an copyright note, however the vis product
  is dual licensed under both the Apache 2.0 license and the MIT License
  (https://github.com/almende/vis)    
*/

var csv = null;
var csvArray = null;
var data = null;
var graph3d = null;


function load() {
	
	window.addEventListener("resize", resizeGraph);
	
	// adjust some settings
	document.getElementById("style").value = "grid";
	document.getElementById("verticalRatio").value = "0.5";
	document.getElementById("filterLabel").value = "";
	document.getElementById("legendLabel").value = "";
	document.getElementById("showPerspective").checked = false;			

	draw()
}


/**
 * Redraw the graph with the entered data and options (set to assume csv data) 
 */
function draw() {

	data = getDataCsv();
	var options = getOptions();

	drawSummaryTable(data, "label");
	
	graph3d = new vis.Graph3d(document.getElementById('graphdiv'), data, options);
}



/**
 * Retrieve the datatable from the entered contents of the csv text
 * @param {boolean} [skipValue] | if true, the 4th element is a filter value
 * @return {vis.DataSet}
 */
function getDataCsv() {
	
  //parse the csv content into an javascript array
  csv = document.getElementById("csvTextarea").value;
  csvArray = csv2array(csv);
    
  // need to sort the array into run, txn order - runs with all transactions missing (z=-1) are currently put at the end of the list and so may be out of order. 
  // (the * 1 is intended to make the function order by numeric values (otherwise it will treat the index value as a string comparison)  
  
  csvArray.sort(function(a, b){
      var runIxa = a[0];
      var runIxb = b[0];

      if (isNaN(runIxa)){return -1;}   //leave the header row as the 1st one
      if (isNaN(runIxb)){return 1;}
      
      if (runIxa * 1 > runIxb * 1) {return 1;}
      if (runIxa * 1 < runIxb * 1) {return -1;} 	

      var txnIxa = a[1] * 1;
      var txnIxb = b[1] * 1;
      
      return  txnIxa - txnIxb
  });

  // naming the axis (comes from the first row vis csv graphic data) 
  document.getElementById("xLabel").value = csvArray[0][0];
  document.getElementById("yLabel").value = csvArray[0][1];
  document.getElementById("zLabel").value = csvArray[0][2];
  
  data = new vis.DataSet();

  var skipValue = false;
  if (document.getElementById("filterLabel").value != "" && document.getElementById("legendLabel").value == "") {
    skipValue = true;
  }

  // read all data
  for (var row = 1; row < csvArray.length; row++) {
    if (csvArray[row].length == 4 && skipValue == false) {
      data.add({x:parseFloat(csvArray[row][0]),
        y:parseFloat(csvArray[row][1]),
        z:parseFloat(csvArray[row][2]),
        style:parseFloat(csvArray[row][3])});
    }
    else if (csvArray[row].length == 4 && skipValue == true) {
      data.add({x:parseFloat(csvArray[row][0]),
        y:parseFloat(csvArray[row][1]),
        z:parseFloat(csvArray[row][2]),
        filter:parseFloat(csvArray[row][3])});
    }
    else if (csvArray[row].length == 5) {
      data.add({x:parseFloat(csvArray[row][0]),
        y:parseFloat(csvArray[row][1]),
        z:parseFloat(csvArray[row][2]),
        style:parseFloat(csvArray[row][3]),
        filter:parseFloat(csvArray[row][4])});
    }
    else {
      data.add({x:parseFloat(csvArray[row][0]),
        y:parseFloat(csvArray[row][1]),
        z:parseFloat(csvArray[row][2]),
        style:parseFloat(csvArray[row][2])});
    }
  }

  return data;
}

/**
 * remove leading and trailing spaces
 */
function trim(text) {
  while (text.length && text.charAt(0) == ' ')
    text = text.substr(1);

  while (text.length && text.charAt(text.length-1) == ' ')
    text = text.substr(0, text.length-1);

  return text;
}


/**
 * Retrieve a JSON object with all options
 */
function getOptions() {
	
//Customization: define additional properties (application and graph axis label arrays, etc) 	
	
  var selectedApplication = "optionselapp"; //document.getElementById("application").value;

  var labelRunShortDescriptionsId = document.getElementById("labelRunShortDescriptionsId").value; 
  var labelRunShortDescriptionsIdsArray = labelRunShortDescriptionsId.split(',');
     
  var labelRunDescriptions = document.getElementById("labelRunDescriptionsId").value; 
  var labelRunDescriptionsArray = labelRunDescriptions.split(',');

  var txnsToGraphText  = document.getElementById("txnsToGraphId").value;
  var txnsToGraphArray = txnsToGraphText.split(',');
  
  var trxnIdsWithAnyFailedSlaText   = document.getElementById("trxnIdsWithAnyFailedSlaId").value;
  var trxnIdsWithAnyFailedSlaArray 	= trxnIdsWithAnyFailedSlaText.split(',');  	

  var trxnIdsRangeBarCsv   = document.getElementById("trxnIdsRangeBarId").value;
  var trxnIdsRangeBarArray = csv2array(trxnIdsRangeBarCsv);
  
  var barRangeLegendText  = document.getElementById("barRangeLegendId").value;
  
  var options = {
    width:              document.getElementById("width").value,
    height:             document.getElementById("height").value,
    style:              document.getElementById("style").value,
    showAnimationControls: (document.getElementById("showAnimationControls").checked != false),
    showGrid:          (document.getElementById("showGrid").checked != false),
    showXAxis:         (document.getElementById("showXAxis").checked != false),
    showYAxis:         (document.getElementById("showYAxis").checked != false),
    showZAxis:         (document.getElementById("showZAxis").checked != false),
    showPerspective:   (document.getElementById("showPerspective").checked != false),
    showLegend:        (document.getElementById("showLegend").checked != false),
    showShadow:        (document.getElementById("showShadow").checked != false),
    keepAspectRatio:   (document.getElementById("keepAspectRatio").checked != false),
    verticalRatio:      Number(document.getElementById("verticalRatio").value) || undefined,
    animationInterval:  Number(document.getElementById("animationInterval").value) || undefined,
    xLabel:             document.getElementById("xLabel").value,
    yLabel:             document.getElementById("yLabel").value,
    zLabel:             document.getElementById("zLabel").value,
    filterLabel:        document.getElementById("filterLabel").value,
    legendLabel:        document.getElementById("legendLabel").value,
    animationPreload:  (document.getElementById("animationPreload").checked != false),
    animationAutoStart:(document.getElementById("animationAutoStart").checked != false),

    xCenter:           document.getElementById("xCenter").value,
    yCenter:           document.getElementById("yCenter").value,

    xMin:              Number(document.getElementById("xMin").value) || undefined,
    xMax:              Number(document.getElementById("xMax").value) || undefined,
    xStep:             Number(document.getElementById("xStep").value) || undefined,
    yMin:              Number(document.getElementById("yMin").value) || undefined,
    yMax:              Number(document.getElementById("yMax").value) || undefined,
    yStep:             Number(document.getElementById("yStep").value) || undefined,
    zMin:              Number(document.getElementById("zMin").value) || undefined,
    zMax:              Number(document.getElementById("zMax").value) || undefined,
    zStep:             Number(document.getElementById("zStep").value) || undefined,

    valueMin:          Number(document.getElementById("valueMin").value) || undefined,
    valueMax:          Number(document.getElementById("valueMax").value) || undefined,

    xBarWidth:         Number(document.getElementById("xBarWidth").value) || undefined,
    yBarWidth:         Number(document.getElementById("yBarWidth").value) || undefined,
    
    //Customization: adding application, axis names arrays, range bars ...
    
    displayPointText: (document.getElementById("displayPointText1").checked != false),
    displayBarRanges: (document.getElementById("displayBarRanges1").checked != false),
    
    application:       		selectedApplication,
    xAxisNames:        		labelRunShortDescriptionsIdsArray,
    labelRunDescriptions: 	labelRunDescriptionsArray,
    yAxisNames:        		txnsToGraphArray,
    trxnIdsWithFailedSla: 	trxnIdsWithAnyFailedSlaArray,
    trxnIdsRangeBar:  		trxnIdsRangeBarArray,
    barRangeLegend:			barRangeLegendText    
    
  };
  return options;
}


function drawSummaryTable(data, sortby){
	
	var runDatesToGraphText = document.getElementById("runDatesToGraphId").value; 
	var runDatesToGraphArray = runDatesToGraphText.split(',');

	if ( runDatesToGraphArray[0] == "" ){     	
		document.getElementById("comparetab").innerHTML =
			"<span style='color: red; font-weight: bold;'> No run data available! </span>  "  ;
		return false
	}
	
	
	var labelRunDescriptions = document.getElementById("labelRunDescriptionsId").value; 
	var labelRunDescriptionsArray = labelRunDescriptions.split(',');	
	
	var txnsToGraphText = document.getElementById("txnsToGraphId").value;
	var txnsToGraphArray = txnsToGraphText.split(',');
	// nasty side effect of split - empty string returns and array with one (unwanted) blank element.. 
	if (txnsToGraphArray[0] == "" ){     	
		txnsToGraphArray = [];
	}
	
	var trxnIdsWithAnyFailedSlaText  = document.getElementById("trxnIdsWithAnyFailedSlaId").value;
	var trxnIdsWithAnyFailedSlaArray = trxnIdsWithAnyFailedSlaText.split(',');  	
	
	var applicationText =  document.getElementById("application").value;
	var selectedGraph = document.getElementById("graph").value; 
	var txnType = document.getElementById("txnTypedId").value; 
	
	var trxnIdsForFailedSlaRelatingToThisGraphArray = []; 
	
	if ( selectedGraph == "TXN_90TH"  ){
		trxnIdsForFailedSlaRelatingToThisGraphArray = document.getElementById("trxnIdsWithFailedSla90thResponseId").value.split(',');
	}
	if ( selectedGraph == "TXN_FAIL" ||
		 selectedGraph == "TXN_FAIL_PERCENT"	){
		trxnIdsForFailedSlaRelatingToThisGraphArray = document.getElementById("trxnIdsWithFailedSlaFailPercentId").value.split(',');
	}
	if ( selectedGraph == "TXN_PASS"  ){
		trxnIdsForFailedSlaRelatingToThisGraphArray = document.getElementById("trxnIdsWithFailedSlaPassCount").value.split(',');
	}	
		
	if ( txnType != "TRANSACTION" ){ // assume we have a 'metric' txn type graph
		trxnIdsForFailedSlaRelatingToThisGraphArray = document.getElementById("trxnIdsWithFailedSlaForThisMetricMeasure").value.split(',');		
	}
	
	comparetabContent = "";	
	comparetabContent += "<br>Sort by : ";
	
	comparetabContent += '<input type="button" value="Txn Name" onclick="drawSummaryTable(data,&quot;label&quot;);" id="wwww">';
	comparetabContent += '<input type="button" value="' + selectedGraph + '" onclick="drawSummaryTable(data,&quot;metric&quot;);" id="zzz">';
	comparetabContent += '<input type="button" value="Difference" onclick="drawSummaryTable(data,&quot;seconds&quot;);" id="xxx">';
	comparetabContent += '<input type="button" value="Change(percent)" onclick="drawSummaryTable(data,&quot;percent&quot;);" id="yyy">';

	if ( document.getElementById("showHistoricData").value == 'no' ) {	
		comparetabContent += '<input type="button" value="More Runs.." onclick="showHistory(data);draw();" id="moreHistoryBtn">';
	}
	comparetabContent += '<br> ' ;
	comparetabContent += "<table id=comparetabId border='1' bordercolor='grey' ><tr>";
	comparetabContent += "<th  class='maxwidth'>" + applicationText + "<br>(" + selectedGraph +  ")</th>";
	
	comparetabContent += "<th>" + runDatesToGraphArray[0].substring(0,4)  + "." + runDatesToGraphArray[0].substring(4,6)+ "." + runDatesToGraphArray[0].substring(6,8);
	comparetabContent += "<br>" + runDatesToGraphArray[0].substring(8,10) + ":" + runDatesToGraphArray[0].substring(10,12);
	comparetabContent += "<br><br>" + labelRunDescriptionsArray[0] + "</th>";

	if ( runDatesToGraphArray[1] == undefined ){  
		comparetabContent += "<th> only single run available ! </th>";		
	} else {
		comparetabContent += "<th>" + runDatesToGraphArray[1].substring(0,4)  + "." + runDatesToGraphArray[1].substring(4,6)+ "." + runDatesToGraphArray[1].substring(6,8);
		comparetabContent += "<br>" + runDatesToGraphArray[1].substring(8,10) + ":" + runDatesToGraphArray[1].substring(10,12);	
		comparetabContent += "<br><br>" + labelRunDescriptionsArray[1] + "</th>";	
	}
	
	comparetabContent += "<th>Difference</th>";
	comparetabContent += "<th>Change %</th>";
	
	if ( document.getElementById("showHistoricData").value == 'yes' ) {	
		comparetabContent += "<th bgcolor='grey' ></th>"
		// could produce a weird html table if runDatesToGraphArray.length is out of wack with the number of runs expected in the csv data array.. 	
		for (var k = 2; k < runDatesToGraphArray.length && k < 7; k ++) { 
			comparetabContent += "<th>" + runDatesToGraphArray[k].substring(0,4)  + "." + runDatesToGraphArray[k].substring(4,6)+ "." + runDatesToGraphArray[k].substring(6,8);
			comparetabContent += "<br>" + runDatesToGraphArray[k].substring(8,10) + ":" + runDatesToGraphArray[k].substring(10,12);
			comparetabContent += "<br><br>" + labelRunDescriptionsArray[k] + "</th>";	;	
	  	}
	}
	
	comparetabContent += "</tr>";

	var sortedMetriscArray = [];

//  number of transactions to draw (should also be equal txnsToGraphArray.length, all being ok 
//  similarly , the number of run dates recorded on the graph will be runDatesToGraphArray.length)  	
	var dataTxnsRowCount     = txnsToGraphArray.length 
	var dataDatesRowCount    = runDatesToGraphArray.length
	var dataTotalPointsCount = dataDatesRowCount * dataTxnsRowCount ;  
	
	for (var i = 0; i < dataTxnsRowCount; i ++) {  		
		
		run0value = csvArray[ i + 1 ][2];
		
		if ( dataTxnsRowCount + i < dataTotalPointsCount  ){			

			run1value = csvArray[ dataTxnsRowCount + i + 1 ][2];			
			
		} else {
			run1value = " - " 
		}
		
		if (run0value < 0  || run1value < 0 ){ 
			
			if (run0value < 0) { run0value  = "n/a"; } ;
			if (run1value < 0) { run1value  = "n/a"; } ;
			
			diffSecs = "n/a";
			diffPcnt = "n/a";
	
		} else {
			
			run0numeric = run0value.replace(/\,/g,'')
			run1numeric = run1value.replace(/\,/g,'')
			
			diffSecs = parseFloat( run0numeric - run1numeric ).toFixed(3).replace('.000','') ; 
			diffPcnt = parseFloat( 100 * (run0numeric - run1numeric) / run1numeric  ).toFixed(0) ; 			
		}
		
		sortedMetriscArray[i] = {};
		sortedMetriscArray[i]["label"]     = txnsToGraphArray[i];
		sortedMetriscArray[i]["run0value"] = run0value;
		sortedMetriscArray[i]["run1value"] = run1value;
		sortedMetriscArray[i]["diffSecs"]  = diffSecs;
		sortedMetriscArray[i]["diffPcnt"]  = diffPcnt;
		
		if ( document.getElementById("showHistoricData").value == 'yes' ) {	
			
			for (var k = 2; k < dataDatesRowCount && k < 7; k ++) { 

				run1value = csvArray[ dataTxnsRowCount + i + 1 ][2];	
				
		 		if ( dataTxnsRowCount*k + i +1 <= dataTotalPointsCount  ){
		 			
		 			if (csvArray[ dataTxnsRowCount*k + i + 1 ][2] < 0){
		 				sortedMetriscArray[i]["run" + k + "value"] = "n/a"
		 			} else {
		 				sortedMetriscArray[i]["run" + k + "value"] = csvArray[ dataTxnsRowCount*k + i + 1 ][2]; 
		 			}
		 			
		 		} else {
		 			sortedMetriscArray[i]["run" + k + "value"] = "not found"
		 		}
				
			} //for k loop
		} // if showHistoricData
	}  // dataTxnsRowCount

	sortedMetriscArray.sort(function(a,b) {

		if (sortby == "label" ){
			// do not do a JavaScript sort on the label, leave transactions ids in the order as presented
			return 0;  

		} else if (sortby == "seconds" ) {
			var sorta =  a.diffSecs;
			var sortb =  b.diffSecs;
		} else if (sortby == "metric" ) {
			var sorta =  a.run0value;
			var sortb =  b.run0value;  			
		} else {
	  		var sorta =  a.diffPcnt;
	  		var sortb =  b.diffPcnt;
		}	
		
		if (  sorta == "n/a" && sortb == "n/a"  ){ return 0;}
		if (  sorta == "n/a" && sortb != "n/a"  ){ return 1;}
		if (  sortb == "n/a" && sorta != "n/a"  ){ return -1;}
		
		if (  sorta == "NaN" && sortb == "NaN"  ){ return 0;}
		if (  sorta == "NaN" && sortb != "NaN"  ){ return 1;}
		if (  sortb == "NaN" && sorta != "NaN"  ){ return -1;} 		
		
		return ( parseFloat(sortb) -  parseFloat(sorta)); 
		
	});


	for (var i = 0; i < sortedMetriscArray.length; i ++) {

		transactionName = sortedMetriscArray[i].label
		
		if (isInArray( transactionName, trxnIdsWithAnyFailedSlaArray)){
			comparetabContent += "<tr><td class=textred style='white-space:nowrap;'>" + transactionName + "</td>";
		} else {
			comparetabContent += "<tr><td style='white-space:nowrap;'>" + transactionName + "</td>"; 			
		};

		if (isInArray( transactionName, trxnIdsForFailedSlaRelatingToThisGraphArray)){
			comparetabContent += "<td class=textred>" + sortedMetriscArray[i].run0value + "</td>";
		} else {
			comparetabContent += "<td>" + sortedMetriscArray[i].run0value + "</td>"; 			
		};
		comparetabContent += "<td>" + sortedMetriscArray[i].run1value + "</td>";
		comparetabContent += "<td>" + sortedMetriscArray[i].diffSecs +  "</td>";
		comparetabContent += "<td>" + sortedMetriscArray[i].diffPcnt +  "</td>";
		
		
		if ( document.getElementById("showHistoricData").value == 'yes' ) {	 	
			comparetabContent += "<td bgcolor = 'grey' ></td>"
	 		if (runDatesToGraphArray.length > 2){ comparetabContent += "<td>" + sortedMetriscArray[i].run2value + "</td>";};		
	 		if (runDatesToGraphArray.length > 3){ comparetabContent += "<td>" + sortedMetriscArray[i].run3value + "</td>";};		
	 		if (runDatesToGraphArray.length > 4){ comparetabContent += "<td>" + sortedMetriscArray[i].run4value + "</td>";};		
	 		if (runDatesToGraphArray.length > 5){ comparetabContent += "<td>" + sortedMetriscArray[i].run5value + "</td>";};		
	 		if (runDatesToGraphArray.length > 6){ comparetabContent += "<td>" + sortedMetriscArray[i].run6value + "</td>";};		
		}
		
		comparetabContent += "</tr>";
	}

	comparetabContent += "</table>";
	comparetabContent += "<br>";
	
	drawMissingTransactionsTable(data,sortby,runDatesToGraphArray);
	
	drawIgnoredTransactionsTable(data,sortby);
	
	document.getElementById("comparetab").innerHTML = comparetabContent;
	
}





function drawMissingTransactionsTable(data,sortby,runDatesToGraphArray){
	
	var missingTransactionsIdText   = document.getElementById("missingTransactionsId").value;
	var trxnIdsWithAnyFailedSlaArray = missingTransactionsIdText.split(',');  
	var txnType = document.getElementById("txnTypedId").value; 

	if (trxnIdsWithAnyFailedSlaArray[0].length > 0){ 
		
		var txnType = document.getElementById("txnTypedId").value; 
		var host =  window.location.host; 	
		
		if ( txnType != "TRANSACTION" ){  // assume we are displaying a metrics graph
			slaUrl="http://" + host + "/metrics/metricSlaList?reqApp=" + document.getElementById("application").value 		
			slaUrlLink = "<a id=slaUrlLink href=" + slaUrl + " target='_blank'>Metric Sla Transaction Database Link</a>";
		} else {
			slaUrl="http://" + host + "/metrics/viewSlaList?reqApp=" + document.getElementById("application").value 		
			slaUrlLink = "<a id=slaUrlLink href=" + slaUrl + " target='_blank'>SLA Transaction Database Link</a>";
		}
			
		comparetabContent += "<br><br><br><br>";	
		comparetabContent += "<table id=comparetabId border='1' bordercolor='grey' ><tr>";
		
		if ( txnType != "TRANSACTION" ){  // assume we are displaying a metrics graph
			comparetabContent += "<th> Missing Metrics List <br>( txn has an SLA and should appear on this graph) <br>see: " + slaUrlLink ;		
		} else {
			comparetabContent += "<th> Missing Transactions List <br>(txn has a some SLA but it does not appear in results) <br>see: " + slaUrlLink;
		}
		
		comparetabContent += "<br>" + runDatesToGraphArray[0].substring(0,4)  + "." + runDatesToGraphArray[0].substring(4,6)+ "." + runDatesToGraphArray[0].substring(6,8);
		comparetabContent +=  "  "  + runDatesToGraphArray[0].substring(8,10) + ":" + runDatesToGraphArray[0].substring(10,12);
		comparetabContent += "</th><tr>";		
	
		

		for (var i = 0; i < trxnIdsWithAnyFailedSlaArray.length; i ++) {
	 		comparetabContent += "<tr><td  class=textred>" + trxnIdsWithAnyFailedSlaArray[i] + "</td></tr>";
	 	}	
		comparetabContent += "</table>";
		comparetabContent += "<br>"; 		
		
	}
	return comparetabContent;
}




function drawIgnoredTransactionsTable(data,sortby){
	
	var ignoredTransactionsIdText   = document.getElementById("ignoredTransactionsId").value;
	var ignoredTransactionsArray = ignoredTransactionsIdText.split(',');  

	if (ignoredTransactionsArray[0].length > 0){ 
		var host =  window.location.host; 	
		slaUrl="http://" + host + "/metrics/viewSlaList?reqApp=" + document.getElementById("application").value 		
		slaUrlLink = "<a id=slaUrlLink href=" + slaUrl + " target='_blank'>SLA Transaction Database Link</a>";
	
		comparetabContent += "<br><br><br><br>";	
		comparetabContent += "<table id=comparetabId border='1' bordercolor='grey' ><tr>";
		comparetabContent += "<th> Ignored Transactions List <br>&nbsp;&nbsp;&nbsp;(have been flaged to be ignored on transaction graphs" +
				"<br>&nbsp;&nbsp;&nbsp;see: " + slaUrlLink + "<br>&nbsp;&nbsp;&nbsp;note: can be manually added to graph)";

		comparetabContent += "</th><tr>";		

		for (var i = 0; i < ignoredTransactionsArray.length; i ++) {
	 		comparetabContent += "<tr><td>" + ignoredTransactionsArray[i] + "</td></tr>";
	 	}	
		comparetabContent += "</table>";
		comparetabContent += "<br>"; 		
	}
	return comparetabContent;
}





function showHistory(data){

	document.getElementById("showHistoricData").value = "yes";
	drawSummaryTable(data, "label");
}



function showHideElement(id){

	var showHideElement = document.getElementById(id);

	if(showHideElement.style.display == 'block') {
		showHideElement.style.display = 'none';
	} else {
	    showHideElement.style.display = 'block';
	}
}



function showHideElementIfCheckboxTicked(checkboxId, id){
	var checkbox        = document.getElementById(checkboxId);
	var showHideElement = document.getElementById(id);
	if(checkbox.checked) {
		showHideElement.style.display = 'block';
	} else {
	    showHideElement.style.display = 'none';
	}
}


function setElementReadonlyIfCheckboxTicked(checkboxId, id){
	var checkbox  = document.getElementById(checkboxId);
	var element   = document.getElementById(id);
	if(checkbox.checked) {
		element.readOnly = false;
		element.style.color = "Black";
	} else {
		element.readOnly = true;
		element.style.color = "LightGray";		
	}
}


function isInArray(value, array) {
	
	var foundInArray = false;
	var arrayLength = array.length;
	
	for (var i =0; i < arrayLength; i++ ){
		if (array[i] == value) {
			return true;
		}
	}
	return false;
	//window.alert ( "value is" + value + ", indexOf(value) is " + array.indexOf(value) );	
}


function clearChosenRuns() {
	document.getElementById("chosenRuns").value = "";
	document.getElementById("manuallySelectRuns1").checked = false;		
	// document.getElementById("useRawRunSQL1").checked = false; // -- deliberate (so you can choose the max runs of a given sql!!  
}


function resetSelectionFields() {
	// leaving graph the same (easier if some-one is doing a metric like-for-like compare across applications)   
	document.getElementById("sqlSelectLike").value = "%";
	document.getElementById("sqlSelectNotLike").value = "";
	document.getElementById("manuallySelectTxns1").checked = false;			
	document.getElementById("chosenTxns").value = "";	
	document.getElementById("useRawSQL1").checked = false;
	document.getElementById("nthRankedTxn").value = "All";
	document.getElementById("sqlSelectRunLike").value = "%";
	document.getElementById("sqlSelectRunNotLike").value = "";	
	document.getElementById("manuallySelectRuns1").checked = false;			
	document.getElementById("chosenRuns").value = "";
	document.getElementById("useRawRunSQL1").checked = false;
	document.getElementById("maxRun").value = "10";            // in line with DEFAULT_10 = "10";
	document.getElementById("maxBaselineRun").value = "1";     // in line with DEFAULT_01 = "1";
}


function trendingBuildPageLink() {

	document.getElementById('trendingLink').innerHTML="page url";
	
	var host =  window.location.host; 
	
	var reqSqlSelectLikeUrlParm = "";	
	if (document.getElementById("sqlSelectLike").value != "%" ){
		reqSqlSelectLikeUrlParm = "&reqSqlSelectLike=" 	+ encodeURIComponent(document.getElementById("sqlSelectLike").value)
	}
	
	var reqSqlSelectNotLikeUrlParm = "";	
	if (document.getElementById("sqlSelectNotLike").value != "" ){
		reqSqlSelectNotLikeUrlParm = "&reqSqlSelectNotLike=" 	+ encodeURIComponent(document.getElementById("sqlSelectNotLike").value)
	}

	var reqManuallySelectTxnsParm = "";
	if(document.getElementById("manuallySelectTxns1").checked) {
		reqManuallySelectTxnsParm =   "&reqManuallySelectTxns="  + document.getElementById("manuallySelectTxns1").checked
							 		+ "&reqChosenTxns="+ encodeURIComponent(document.getElementById("chosenTxns").value);
	} 
	
	var reqUseRawSQLLUrlParm = "";
	if(document.getElementById("useRawSQL1").checked) {
		reqUseRawSQLLUrlParm = "&reqUseRawSQL="        + document.getElementById("useRawSQL1").checked
		+ "&reqTransactionIdsSQL="+ btoa(encodeURIComponent(document.getElementById("transactionIdsSQL").value));
	} 
	
	var reqNthRankedTxnParm = "";
	if(document.getElementById("nthRankedTxn").value != "All"  ) {
		reqNthRankedTxnParm = "&reqNthRankedTxn=" + encodeURIComponent(document.getElementById("nthRankedTxn").value);		
	} 
	
	var reqSqlSelectRunLikeUrlParm = "";	
	if (document.getElementById("sqlSelectRunLike").value != "%" ){
		reqSqlSelectRunLikeUrlParm = "&reqSqlSelectRunLike=" 	+ encodeURIComponent(document.getElementById("sqlSelectRunLike").value)
	}
	
	var reqSqlSelectRunNotLikeUrlParm = "";	
	if (document.getElementById("sqlSelectRunNotLike").value != "" ){
		reqSqlSelectRunNotLikeUrlParm = "&reqSqlSelectRunNotLike=" 	+ encodeURIComponent(document.getElementById("sqlSelectRunNotLike").value)
	}
	
	var manuallySelectedRunsListUrlParm = "";
	if(document.getElementById("manuallySelectRuns1").checked) {
		manuallySelectedRunsListUrlParm = "&reqManuallySelectRuns=" + document.getElementById("manuallySelectRuns1").checked 
		+ "&reqChosenRuns="         + document.getElementById("chosenRuns").value;
	} 
	
	var reqUseRawRunSQLUrlParm = "";
	if(document.getElementById("useRawRunSQL1").checked) {
		reqUseRawRunSQLUrlParm = "&reqUseRawRunSQL=" 		+ document.getElementById("useRawRunSQL1").checked		
		+ "&reqRunTimeSelectionSQL="+ btoa(encodeURIComponent(document.getElementById("runTimeSelectionSQL").value));
	} 

	
	var reqAppListSelectorParm = "";
	if(document.getElementById("appListSelector").value == "All"  ) {
		reqAppListSelectorParm = "&reqAppListSelector="	+ document.getElementById("appListSelector").value 
	} 

	
	url="http://" + host + "/metrics/trending"  
		+ "?reqApp=" 				+ document.getElementById("application").value 		
		+ "&reqGraph=" 	    		+ document.getElementById("graph").value
		+ reqSqlSelectLikeUrlParm
		+ reqSqlSelectNotLikeUrlParm
		+ reqManuallySelectTxnsParm
		+ reqUseRawSQLLUrlParm
		+ reqNthRankedTxnParm
		+ reqSqlSelectRunLikeUrlParm
		+ reqSqlSelectRunNotLikeUrlParm
		+ manuallySelectedRunsListUrlParm
		+ reqUseRawRunSQLUrlParm
		+ reqAppListSelectorParm
		+ "&reqMaxRun=" 			+ document.getElementById("maxRun").value
		+ "&reqMaxBaselineRun=" 	+ document.getElementById("maxBaselineRun").value
		
	document.getElementById('trendingLink').href = url;
}



function buildHomePageink(){
	var host =  window.location.host; 	
	url="http://" + host + "/metrics?reqApp=" + document.getElementById("application").value
	document.getElementById('homePageLink').href = url;  	
}


function buildSlaDatabaseLink(){
	//eg: http://MYSERVER:8080/metrics/viewSlaList?reqApp=MYAPP
	var host =  window.location.host; 	
	url="http://" + host + "/metrics/slaList?reqApp=" + document.getElementById("application").value
	document.getElementById('slaDatabaseLink').href = url;  	
}

function buildMetricSlaDatabaseLink(){
	//eg: http://MYSERVER:8080/metrics/metricSlaList?reqApp=MYAPP
	var host =  window.location.host; 	
	url="http://" + host + "/metrics/metricSlaList?reqApp=" + document.getElementById("application").value
	document.getElementById('slaMetricDatabaseLink').href = url;  	
}

function buildRunsListLink(){
	//eg:http://MYSERVER:8080/metrics/runsList?reqApp=CI_MYAPP
	var host =  window.location.host; 	
	url="http://" + host + "/metrics/runsList?reqApp=" + document.getElementById("application").value
	document.getElementById('runsListLink').href = url;  	
}

function buildEventMappingLink(){
	var host =  window.location.host; 	
	url="http://" + host + "/metrics/eventMappingList"
	document.getElementById('eventMappingLink').href = url;  	
}

function buildGraphMappingLink(){
	var host =  window.location.host; 	
	url="http://" + host + "/metrics/graphMappingList"
	document.getElementById('graphMappingLink').href = url;  	
}


function trendingLinkGetNewPage() {
	document.getElementById('trendingLink').click();
}


function resizeGraph(){
	draw();	
}
