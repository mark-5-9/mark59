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
<!DOCTYPE html>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<title>Mark59 - Application Dashboard</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

<script>

function resendDashboardURL(){
		var reqAppListSelector = document.getElementById("appListSelector").value;
		window.location.href = 	"./dashboard?reqAppListSelector=" + encodeURIComponent(reqAppListSelector);
	}	
	
	function setAppListSelectorInDropdown(reqAppListSelector){    
	    var element = document.getElementById('appListSelector');
	    element.value = reqAppListSelector;
	}
	
</script>
</head>

<body onload="setAppListSelectorInDropdown('${map.reqAppListSelector}')">

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
  
 <h1>Applications</h1>     

  <table class="metricsTable">
   <tr>
    <th></th>
    <th></th>
    <th><b>x</b>*</th>   
    <th>Application</th>
    <th>SLA<br>summary</th>    
    <th>Active?<br> 
    	<form:select id='appListSelector' path="map" items="${map.appListSelectorList}"  onChange="resendDashboardURL()" />      	
    </th>
    <th>Elapsed Time <br>since last run</th>
    <th>Transactional<br>SLAs</th>
    <th>Metric<br>SLAs</th>
    <th>Comment</th>
   </tr>
   <c:forEach var="app" items="${map.dashboardList}">
    <tr>
     <td><a href="copyApplication?&reqApp=${app.application}&reqAppListSelector=${map.reqAppListSelector}" title="Copy"><img src="images/copy.png"/></a></td>  
     <td><a href="editApplication?reqApp=${app.application}&reqAppListSelector=${map.reqAppListSelector}" title="Edit"><img src="images/edit.png"/></a></td>
     <td>
       	<c:if test = "${app.active == 'N'}">
     		<a href="deleteApplication?reqApp=${app.application}" onclick="return confirm('Are you sure (application=${app.application})?');" title="Delete"><img src="images/delete.png"/></a>
     	</c:if>				
     </td>  
     <td><a href="trending?reqApp=${app.application}" target="_blank">${app.application}</a></td>
     <td><img src="images/${app.slaSummaryIcon}.png" style="width:25px;height:25px;"/></td>    
     <td>${app.active} </td>
     <td>${app.sinceLastRun} </td>
     <td><img src="images/${app.slaTransactionResultIcon}.png" style="width:15px;height:15px;"/></td>    
     <td><img src="images/${app.slaMetricsResultIcon}.png" style="width:15px;height:15px;"/></td>    
     <td>${app.comment}</td>
    </tr>
   </c:forEach>
  </table>
  
  <p style="font-size: 12px"><b>*</b> You must 'deactivate' an application before you are permitted to delete it.</p> 
  

  <input type="hidden" id="passedReqAppListSelector" value="${parmsMap.reqAppListSelector}" />

</div>

</body>
</html>
