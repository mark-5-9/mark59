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
<title>Mark59 - Transactional SLAs</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

<script>

	function resendSlaListURL(){

		var selectedApplication = document.getElementById("application").value;
		window.location.href = 	"./slaList?reqApp=" + selectedApplication;
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

 <h1>Transaction SLA Reference Maintenance</h1>     
  
 <div align="right">
   <p><b>SLA Bulk Edit Functions:</b></p>
   <p>
   <c:if test="${map.reqApp != null && map.reqApp != ''}"><a href="copyApplicationSla?reqApp=${map.reqApp}">[ Copy All ${map.reqApp} ]</a>&nbsp;&nbsp;</c:if>
   <c:if test="${map.reqApp != null && map.reqApp != ''}"><a href="deleteApplicationSla?reqApp=${map.reqApp}"  onclick="return confirm('Are you sure (applicaton=${map.reqApp})?');" >[ Delete All ${map.reqApp} ]</a>&nbsp;&nbsp;</c:if>
   <a href="bulkApplicationPassCounts?reqApp=${map.reqApp}">[ Bulk Load Transaction Pass Counts ]</a>
   </p>	  
</div>   
 
 <p><a href="registerSla?reqApp=${map.reqApp}">New Sla</a></p>

  <table class="metricsTable">
   <tr>
    <th></th>
    <th></th>
    <th></th>
    <th>Transaction</th>
    <th>CDP<br>Txn</th>    
    <th>Application<br><form:select id='application' path="map" items="${map.applications}"  onChange="resendSlaListURL()" /></th>
    <th>Txn Ignored<br>on Graphs</th>    
    <th>90th Res<br>Time</th>
    <th>95th Res<br>Time</th>
    <th>99th Res<br>Time</th>
    <th>Txn Pass Count</th>
    <th>Txn Pass<br>Count Variance %</th>    
    <th>Txn Fail Count</th>
    <th>Txn Fail %</th>
    <th>Txn<br>delay</th>    
    <th>Xtra<br>num</th>
    <th>Xtra<br>int</th>    
    <th>Reference</th>
    <th>Comment</th>
    <th>Active</th>    
   </tr>
   <c:forEach var="sla" items="${map.slaList}">
    <tr>
     <td><a href="copySla?reqTxnId=${sla.txnIdURLencoded}&reqIsCdpTxn=${sla.isCdpTxn}&reqApp=${sla.application}"   title="Copy"><img src="images/copy.png"/></a></td>  
     <td><a href="editSla?reqTxnId=${sla.txnIdURLencoded}&reqIsCdpTxn=${sla.isCdpTxn}&reqApp=${sla.application}"   title="Edit"><img src="images/edit.png"/></a></td>
     <td><a href="deleteSla?reqTxnId=${sla.txnIdURLencoded}&reqIsCdpTxn=${sla.isCdpTxn}&reqApp=${sla.application}"
     							 onclick="return confirm('Are you sure (transaction=${sla.txnId}), CDP txn=${sla.isCdpTxn} ?');" title=deleteLink><img src="images/delete.png"/></a></td>
     <td>${sla.txnId}</td>
     <td>${sla.isCdpTxn}</td>
     <td>${sla.application}</td>
     <td>${sla.isTxnIgnored }</td>     
     <td>${sla.sla90thResponse}</td>
     <td>${sla.sla95thResponse}</td>
     <td>${sla.sla99thResponse}</td>
     <td>${sla.slaPassCount}</td>
     <td>${sla.slaPassCountVariancePercent}</td>
     <td>${sla.slaFailCount}</td>     
     <td>${sla.slaFailPercent}</td>
     <td>${sla.txnDelay}</td>        
     <td>${sla.xtraNum}</td>     
     <td>${sla.xtraInt}</td>     
     <td>${sla.slaRefUrl}</td>
     <td>${sla.comment}</td>   
     <td>${sla.isActive}</td>       
    </tr>
   </c:forEach>
  </table>
  <input type="hidden" id="passedReqApp" value="${map.reqApp}" />
    
  </div>

</body>
</html>
