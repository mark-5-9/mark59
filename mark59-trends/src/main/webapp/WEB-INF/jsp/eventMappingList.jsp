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
  Date:    Australian Winter 2019
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>Mark59 - Metrics Event Mapping Reference List</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

<script>

	function resendEventMappingListPerformanceTools()
	{
		var selectedreqPerformanceTool = document.getElementById("performanceTool").value;
		window.location.href = 	"./eventMappingList?reqPerformanceTool=" + selectedreqPerformanceTool + "&reqMetricSource=";
	}	
	
	function resendEventMappingListMetricSources()
	{
		var selectedreqMetricSource = document.getElementById("metricSource").value;		
		window.location.href = 	"./eventMappingList?reqPerformanceTool=&reqMetricSource=" + selectedreqMetricSource;
	}	

	function setSelectedperformanceToolDropdown(reqPerformanceTool)
	{    
	    var element = document.getElementById('performanceTool');
	    element.value = reqPerformanceTool;
	}	

	function setSelectedmetricSourceInDropdown(reqMetricSource)
	{    
	    var element = document.getElementById('metricSource');
	    element.value = reqMetricSource;
	}	
	
	
</script>
</head>

<body onload="setSelectedmetricSourceInDropdown('${parmsMap.reqMetricSource}');
              setSelectedperformanceToolDropdown('${parmsMap.reqPerformanceTool}') ">
              
              
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content">              

 <h1>Metrics Event Mapping Reference</h1>     

 <p><a href="registerEventMapping?reqMetricSource=${parmsMap.reqMetricSource}">New Metrics Event Mapping</a></p>


  <table class="metricsTable" >
   <tr>
    <th></th>
    <th></th>
    <th></th>
    <th>Metric Source<br><form:select id='metricSource' path="parmsMap" items="${parmsMap.metricSources}"  onChange="resendEventMappingListMetricSources()" /></th>
    <th>Match When<br>Source Name Like</th>
    <th>Mapped to<br>Metric Data Type</th>        
    <th>Target Name LB</th>
    <th>Target Name RB</th>    
    <th>Is % ?</th>
    <th>Is Inverted % ?</th>
    <th>Tool<br><form:select id='performanceTool' path="parmsMap" items="${parmsMap.performanceTools}"  onChange="resendEventMappingListPerformanceTools()" /></th>      
    <th>Comment</th>
   </tr>
   <c:forEach var="eventMapping" items="${parmsMap.eventMappingList}">
    <tr>
     <td><a href="copyEventMapping?&txnType=${eventMapping.txnType}&metricSource=${eventMapping.metricSource}&matchWhenLike=${eventMapping.matchWhenLikeURLencoded}&reqMetricSource=${parmsMap.reqMetricSource}" title="Copy"><img src="images/copy.png"/></a></td> 
     <td><a href="editEventMapping?&txnType=${eventMapping.txnType}&metricSource=${eventMapping.metricSource}&matchWhenLike=${eventMapping.matchWhenLikeURLencoded}&reqMetricSource=${parmsMap.reqMetricSource}" title="Edit"><img src="images/edit.png"/></a></td>
     <td><a href="deleteEventMapping?&txnType=${eventMapping.txnType}&metricSource=${eventMapping.metricSource}&matchWhenLike=${eventMapping.matchWhenLikeURLencoded}&reqMetricSource=${parmsMap.reqMetricSource}" onclick="return confirm('Are you sure (match string ${eventMapping.matchWhenLike})?');" title="Delete"><img src="images/delete.png"/></a></td>
     <td>${eventMapping.metricSource}</td>     
     <td>${eventMapping.matchWhenLike}</td>
     <td>${eventMapping.txnType}</td>
     <td>${eventMapping.targetNameLB}</td>
     <td>${eventMapping.targetNameRB}</td>
     <td>${eventMapping.isPercentage}</td>
     <td>${eventMapping.isInvertedPercentage}</td>
     <td>${eventMapping.performanceTool}</td>     
     <td>${eventMapping.comment}</td>
    </tr>
   </c:forEach>
  </table>

<input type="hidden" id="passedReqApp" value="${parmsMap.reqApp}" />
</div>

</body>
</html>
