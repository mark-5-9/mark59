<!-- Copyright 2019 Mark59.com
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License. 
  
  Author:  Philip Webb
  Date:    Australian Summer 2020
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>Server Metrics Via Web - Command Response Parsers List</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

<script>
	
	function resendCommandResponseParserListMetricTxnType(){
		var selectedreqMetricTxnType = document.getElementById('metricTxnType').value;		
		window.location.href = 	"./commandResponseParserList?&reqMetricTxnType=" + encodeURIComponent(selectedreqMetricTxnType);
	}	

	function setSelectedMetricTxnTypeInDropdown(reqMetricTxnType){    
	    var element = document.getElementById('metricTxnType');
	    element.value = reqMetricTxnType;
	}	
	
</script>
</head>

<body onload="setSelectedMetricTxnTypeInDropdown('${parmsMap.reqMetricTxnType}') ">
              
              
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content">              

 <h1>Command  Response Parsers List</h1>     

 <p><a href="registerCommandResponseParser?&reqMetricTxnType=${parmsMap.reqMetricTxnType}">Add Command Parser</a></p>


  <table class="metricsTable" >
   <tr>
    <th></th>
    <th></th>
    <th></th>
    <th>Parser Name</th>
    <th>Metric Type<br><form:select id='metricTxnType' path="parmsMap" items="${parmsMap.metricTxnTypes}"  onChange="resendCommandResponseParserListMetricTxnType()" /></th>
    <th>Metric Name Suffix</th>
    <th>Script</th>        
    <th>Comment</th>
   </tr>
   <c:forEach var="commandResponseParser" items="${parmsMap.commandResponseParserList}">
    <tr>
     <td><a href="copyCommandResponseParser?&reqParserName=${commandResponseParser.parserName}&reqMetricTxnType=${parmsMap.reqMetricTxnType}" title="Copy"><img src="images/copy.png"/></a></td> 
     <td><a href="editCommandResponseParser?&reqParserName=${commandResponseParser.parserName}&reqMetricTxnType=${parmsMap.reqMetricTxnType}" title="Edit"><img src="images/edit.png"/></a></td>
     <td><a href="deleteCommandResponseParser?&reqParserName=${commandResponseParser.parserName}&reqMetricTxnType=${parmsMap.reqMetricTxnType}" onclick="return confirm('Are you sure (server : ${commandResponseParser.parserName})?');" title="Delete"><img src="images/delete.png"/></a></td>
     <td><a href="viewCommandResponseParser?&reqParserName=${commandResponseParser.parserName}&reqMetricTxnType=${parmsMap.reqMetricTxnType}">${commandResponseParser.parserName}</a></td>     
     <td>${commandResponseParser.metricTxnType}</td>
     <td>${commandResponseParser.metricNameSuffix}</td>
     <td>${commandResponseParser.script}</td>
     <td>${commandResponseParser.comment}</td>
    </tr>
   </c:forEach>
  </table>
  
  </div>

</body>
</html>
