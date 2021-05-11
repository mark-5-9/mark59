<!-- Copyright 2019 Insurance Australia Group Limited
 
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
  Date:    Australian Winter 2019
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>Application Runs List</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

<script>

	function resendRunsListURL(){

		var selectedApplication = document.getElementById("application").value;
		//alert("selectedApplication=<" + selectedApplication + ">"); 
		window.location.href = 	"./runsList?reqApp=" + selectedApplication;
	}	
	
	function setSelectedAppInDropdown(requestApp)
	{    
	    var element = document.getElementById('application');
	    element.value = requestApp;
	}	

</script>
</head>

<body onload="setSelectedAppInDropdown('${map.reqApp}')">

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
  
 <h1>Runs List</h1>     

  <table class="metricsTable">
   <tr>
    <th></th>
    <th></th>   
    <th>Application<br><form:select id='application' path="map" items="${map.applications}" onchange="resendRunsListURL()" /></th>
    <th>Run Start </th>
    <th>Run Ignored<br>on Graphs</th>   
    <th>Reference</th>
    <th>Run Period</th> 
    <th>Duration (min)</th>    
    <th>Baseline?</th>
    <th>Comment</th>
   </tr>
   <c:forEach var="runs" items="${map.runsList}">
    <tr>
     <td><a href="editRun?reqApp=${runs.application}&runTime=${runs.runTime}" title="Edit"><img src="images/edit.png"/></a></td>
     <td><a href="deleteRun?reqApp=${runs.application}&runTime=${runs.runTime}" onclick="return confirm('Are you sure (runtime=${runs.runTime})?');" title="Delete"><img src="images/delete.png"/></a></td>  
     <td>${runs.application}</td>
     <td>${runs.runTime}</td>
     <td>${runs.isRunIgnored }</td> 
     <td>${runs.runReference}</td>     
     <td class="tdnowrap">${runs.period}</td>
     <td>${runs.duration}</td>     
     <td>${runs.baselineRun}</td>
     <td>${runs.comment}</td>
    </tr>
   </c:forEach>
  </table>

</div>
</body>
</html>
