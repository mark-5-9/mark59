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
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>
<html>
<head>
<title>Mark59 - Transaction Rename</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
</head>

<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
 
 <h1>Transaction Rename</h1>

<p>&nbsp;</p>
  
  <div>
   <form:form method="post" action="transactionRenameValidate" modelAttribute="transactionRenameForm">
    <table>
     <tr>
      <td>Application</td><td>:</td><td> ${transactionRenameForm.application} </td>
     </tr>
     <tr>
      <td>Transaction Type</td><td>:</td><td> ${transactionRenameForm.txnType} </td>
     </tr>
     
     <tr>
      <td>From Transaction Id</td><td>:</td><td> ${transactionRenameForm.fromTxnId} </td>
     </tr>       
     <tr>
      <td>To Transaction Id</td><td>:</td>
      <td><form:input path="toTxnId" value="${transactionRenameForm.toTxnId}" onchange="trimkey(this)" size="100" />
      </td>
     </tr> 
          
     <c:if test = "${transactionRenameForm.txnType == 'TRANSACTION'}">
	     <tr>
	      <td>From CDP txn</td><td>:</td><td> ${transactionRenameForm.fromIsCdpTxn} </td>
	     </tr>          
	     <tr>
	      <td>To CDP txn</td><td>:</td>
	      <td><form:select path="toIsCdpTxn" items="${map.isCdpTxnYesNo}" value="${transactionRenameForm.toIsCdpTxn}" /> </td>
	     </tr> 
     </c:if>	
     <c:if test = "${transactionRenameForm.txnType != 'TRANSACTION'}">
	     <tr><form:hidden path="toIsCdpTxn" value="${transactionRenameForm.fromIsCdpTxn}" /></tr> 
     </c:if>	              
     
     <tr>
      <td></td><td></td>
      <td><input type="submit" value="Validate" /></td>
     </tr>
     
     <tr>
      <td colspan="3"><a href="transactionList?reqApp=${transactionRenameForm.application}">Cancel</a></td>      
     </tr>     
     
    </table>
    <form:hidden path="application" />
    <form:hidden path="txnType" />
    <form:hidden path="fromTxnId" />
    <form:hidden path="fromIsCdpTxn" />    
   </form:form>
  </div>

</div>

</body>
</html>