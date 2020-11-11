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
<!DOCTYPE html>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>Mark59 - Sla Reference List</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

</head>
<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content">

<h1>Sla Reference List</h1>

  <table class="metricsTable">
   <tr>
    <th>Transaction</th>
    <th>Application</th>
    <th>Txn Ignored<br>on Graphs</th>        
    <th>90th Res Time</th>
    <th>Txn Pass Count</th>
    <th>Txn Pass<br>Count<br>Variance %</th>    
    <th>Txn Fail Count</th>
    <th>Txn Fail %</th>
    <th>Reference</th>
    <th>Comment</th>    
   </tr>
   <c:forEach var="sla" items="${slaList}">
    <tr>
     <td>${sla.txnId}</td>
     <td>${sla.application}</td>
     <td>${sla.isTxnIgnored }</td>       
     <td>${sla.sla90thResponse}</td>
     <td>${sla.slaPassCount}</td>
     <td>${sla.slaPassCountVariancePercent}</td>     
     <td>${sla.slaFailCount}</td>
     <td>${sla.slaFailPercent}</td>
     <td>${sla.slaRefUrl}</td>
     <td>${sla.comment}</td>        
    </tr>
   </c:forEach>
  </table>

 </div>
</body>
</html>
