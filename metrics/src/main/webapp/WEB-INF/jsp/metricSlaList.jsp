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
<title>Mark59 - Metric SLAs</title>

<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>



<script>

	function resendMetricSlaListURL(){

		var selectedApplication = document.getElementById("application").value;
		window.location.href = 	"./metricSlaList?reqApp=" + selectedApplication;
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

<h1>Metric SLA Reference Maintenance</h1>

<div align="right">
   <p><b>SLA Bulk Edit Functions:</b></p>
   <p>
   <c:if test="${map.reqApp != null && map.reqApp != ''}"><a href="copyApplicationMetricSla?reqApp=${map.reqApp}">[ Copy All ${map.reqApp} ]</a>&nbsp;&nbsp;</c:if>
   <c:if test="${map.reqApp != null && map.reqApp != ''}"><a href="deleteApplicationMetricSla?reqApp=${map.reqApp}"  onclick="return confirm('Are you sure (applicaton=${map.reqApp})?');" >[ Delete All ${map.reqApp} ]</a>&nbsp;&nbsp;</c:if>
   </p>	  
</div>  
  
<p><a href="registerMetricSla?reqApp=${map.reqApp}">New Sla</a></p>

  <table class="metricsTable">
   <tr>
    <th></th>
    <th></th>
    <th></th>
    <th>Application<br><form:select id='application' path="map" items="${map.applications}"  onChange="resendMetricSlaListURL()" /></th>
    <th>Metric Name</th>    
    <th>Metric Type</th>
    <th>Value Derivation</th>
    <th>Min Value</th>    
    <th>Max Value</th>
    <th>Active</th>
    <th>Comment</th>
   </tr>
   <c:forEach var="metricSla" items="${map.metricSlaList}">
    <tr>
     <td><a href="copyMetricSla?metricName=${metricSla.metricNameURLencoded}&metricTxnType=${metricSla.metricTxnType}&valueDerivation=${metricSla.valueDerivation}&reqApp=${metricSla.application}" title="Copy"><img src="images/copy.png"/></a></td> 
     <td><a href="editMetricSla?metricName=${metricSla.metricNameURLencoded}&metricTxnType=${metricSla.metricTxnType}&valueDerivation=${metricSla.valueDerivation}&reqApp=${metricSla.application}" title="Edit"><img src="images/edit.png"/></a></td>
     <td><a href="deleteMetricSla?metricName=${metricSla.metricNameURLencoded}&metricTxnType=${metricSla.metricTxnType}&valueDerivation=${metricSla.valueDerivation}&reqApp=${metricSla.application}" onclick="return confirm('Are you sure (name=${metricSla.metricName})?');" title="Delete"><img src="images/delete.png"/></a></td>
     <td>${metricSla.application}</td>
     <td>${metricSla.metricName}</td>     
     <td>${metricSla.metricTxnType}</td>
     <td>${metricSla.valueDerivation}</td>
     <td>${metricSla.slaMin}</td>
     <td>${metricSla.slaMax}</td>
     <td>${metricSla.isActive}</td>
     <td>${metricSla.comment}</td>
    </tr>
   </c:forEach>
  </table>
  <input type="hidden" id="passedReqApp" value="${map.reqApp}" />
  
  </div>

</body>
</html>
