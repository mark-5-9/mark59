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
<title>Mark59 - SLA Reference List</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

<script>

	function resendSlaListURL(){

		var selectedApplication = document.getElementById("application").value;
		//alert("selectedApplication=<" + selectedApplication + ">"); 
		window.location.href = 	"./slaList?reqApp=" + selectedApplication;
	}	
	
	
	function setSelectedAppInDropdown(requestApp)
	{    
	    var element = document.getElementById('application');
	    element.value = requestApp;
	}	

	//TODO: use this idea to set links when default set?
	function changeHrefLinks() {
		alert("start of linkjs");
	    var x = document.getElementsByName("editLink");
	    var i;
	    for (i = 0; i < x.length; i++) {
	        // if (x[i].type == "checkbox") {
	            x[i].href = "editSla?txnId=THEQUICKBROXFOX";
	        // }
	    }
	}
	
</script>
</head>

<body onload="setSelectedAppInDropdown('${parmsMap.reqApp}')">

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

 <h1>SLA Reference Maintenance</h1>     
  
 <div align="right">
   <p><b>SLA Bulk Edit Functions:</b></p>
   <input type="hidden" id="passedReqApp" value="${parmsMap.reqApp}" />
   <p>
   <c:if test="${parmsMap.reqApp != null && parmsMap.reqApp != ''}"><a href="copyApplicationSla?reqApp=${parmsMap.reqApp}">[ Copy All ${parmsMap.reqApp} ]</a>&nbsp;&nbsp;</c:if>
   <c:if test="${parmsMap.reqApp != null && parmsMap.reqApp != ''}"><a href="deleteApplicationSla?reqApp=${parmsMap.reqApp}"  onclick="return confirm('Are you sure (applicaton=${parmsMap.reqApp})?');" >[ Delete All ${parmsMap.reqApp} ]</a>&nbsp;&nbsp;</c:if>
   <a href="bulkApplicationPassCounts?reqApp=${parmsMap.reqApp}">[ Bulk Load Transaction Pass Counts ]</a>
   </p>	  

</div>   
 
 <p><a href="registerSla?reqApp=${parmsMap.reqApp}">New Sla</a></p>

  <table class="metricsTable">
   <tr>
    <th></th>
    <th></th>
    <th></th>
    <th>Transaction</th>
    <th>Application<br><form:select id='application' path="parmsMap" items="${parmsMap.applications}"  onChange="resendSlaListURL()" /></th>
    <th>Txn Ignored<br>on Graphs</th>    
    <th>90th Res Time</th>
    <th>Txn Pass Count</th>
    <th>Txn Pass<br>Count Variance %</th>    
    <th>Txn Fail Count</th>
    <th>Txn Fail %</th>
    <th>Reference URL</th>
   </tr>
   <c:forEach var="sla" items="${parmsMap.slaList}">
    <tr>
     <td><a href="copySla?reqTxnId=${sla.txnIdURLencoded}&reqApp=${sla.slaApplicationKey}"   title="Copy"><img src="images/copy.png"/></a></td>  
     <td><a href="editSla?reqTxnId=${sla.txnIdURLencoded}&reqApp=${sla.slaApplicationKey}"   title="Edit"><img src="images/edit.png"/></a></td>
     <td><a href="deleteSla?reqTxnId=${sla.txnIdURLencoded}&reqApp=${sla.slaApplicationKey}" onclick="return confirm('Are you sure (transaction=${sla.txnId})?');" title=deleteLink><img src="images/delete.png"/></a></td>
     <td>${sla.txnId}</td>
     <td>${sla.slaApplicationKey}</td>
     <td>${sla.isTxnIgnored }</td>     
     <td>${sla.sla90thResponse}</td>
     <td>${sla.slaPassCount}</td>
     <td>${sla.slaPassCountVariancePercent}</td>
     <td>${sla.slaFailCount}</td>     
     <td>${sla.slaFailPercent}</td>
     <td>${sla.slaRefUrl}</td>
    </tr>
   </c:forEach>
  </table>

  

  
  </div>

</body>
</html>
