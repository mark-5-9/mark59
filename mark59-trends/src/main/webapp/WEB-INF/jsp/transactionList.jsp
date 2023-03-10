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
<title>Mark59 - Transaction Rename Tool</title>

<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>



<script>

	function resendTransactionListURL(){
		var selectedApplication = document.getElementById("application").value;
		window.location.href = 	"./transactionList?reqApp=" + selectedApplication;
	}	
	
	function setSelectedAppInDropdown(requestApp){    
	    var element = document.getElementById('application');
	    element.value = requestApp;
	}	

</script>
</head>

<body onload="setSelectedAppInDropdown('${map.reqApp}')">

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

<h1>Transaction Renaming Tool</h1>

  <table class="metricsTable">
   <tr>
    <th></th>
    <th>Application<br><form:select id='application' path="map" items="${map.applications}"  onChange="resendTransactionListURL()" /></th>
    <th>Txn Id</th>    
    <th>CDP<br>Txn</th>    
    <th>Txn Type</th>
    <th>Last Run<br> to Include Txn</th>
    <th>Number of Runs<br>Using Txn</th>
   </tr>

   <c:forEach var="transaction" items="${map.transactionList}">
    <tr>
     <td><a href="transactionRenameDataEntry?reqTxnId=${transaction.txnIdURLencoded}&reqIsCdpTxn=${transaction.isCdpTxn}&reqTxnType=${transaction.txnType}&reqApp=${transaction.application}" 
     		title="Edit(Rename)"><img src="images/edit.png"/></a></td>
     <td>${transaction.application}</td>
     <td>${transaction.txnId}</td>
     <td>${transaction.isCdpTxn}</td>
     <td>${transaction.txnType}</td>     
     <td>${transaction.runTime}</td>     
     <td>${transaction.txnPass}</td>     
    </tr>
   </c:forEach>

  </table>
  <input type="hidden" id="passedReqApp" value="${map.reqApp}" />
 </div>
</body>
</html>
