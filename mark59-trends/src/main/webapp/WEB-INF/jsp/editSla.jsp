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
<title>Mark59 - Edit Transactional SLA</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>

<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
 
 <h1>Edit Transactional SLA</h1>

<p>&nbsp;</p>
 
  <div>
   <form:form method="post" action="updateSla?reqApp=${map.reqApp}" modelAttribute="sla">
    <table>
     <tr><td>Application :</td><td>${map.sla.application}</td></tr>
     <tr><td>Transaction :</td><td>${map.sla.txnId}</td></tr>
     <tr><td>CDP Txn     :</td><td>${map.sla.isCdpTxn}</td></tr>
  
     <tr>
      <td>Ignore Txn on Graphs?</td>
      <td><form:select path="isTxnIgnored" items="${map.isTxnIgnoredYesNo}" value="${map.sla.isTxnIgnored}" /></td>
     </tr>     
     <tr>
      <td><br></td>
      <td style="font-size: 12px"><br>Enter <b>-1</b> for values you do not want to set an SLA against.</td>     
     </tr>       
     <tr>
      <td>90thResponse:</td>
      <td><form:input path="sla90thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$" value="${map.sla.sla90thResponse}" /></td>
     </tr>  
     <tr>
      <td>95thResponse:</td>
      <td><form:input path="sla95thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$" value="${map.sla.sla95thResponse}" /></td>
     </tr>
     <tr>
      <td>99thResponse:</td>
      <td><form:input path="sla99thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$" value="${map.sla.sla99thResponse}" /></td>
     </tr> 
     <tr>
      <td>Pass Count:</td>
      <td><form:input path="slaPassCount" type="text" pattern="-?\d*" value="${map.sla.slaPassCount}" /></td>
     </tr>
      <tr>
      <td>Pass Count Variance %:</td>
      <td><form:input path="slaPassCountVariancePercent" type="text" pattern="^-?\d*\.{0,1}\d+$" value="${map.sla.slaPassCountVariancePercent}" /></td>
     </tr>  
     <tr>
      <td>Fail Count:</td>
      <td><form:input path="slaFailCount" type="text" pattern="-?\d*"/></td>
     </tr>  
     <tr>
      <td>Fail Percent:</td>
      <td><form:input path="slaFailPercent" type="text" pattern="^-?\d*\.{0,1}\d+$" value="${map.sla.slaFailPercent}" /></td>
     </tr>
     <tr>
      <td>Is Active :</td>
      <td><form:select path="isActive" value="${map.metricSla.isActive}"  items="${map.isActiveYesNo}" /></td>
     </tr>    
     <tr>
      <td><br></td>
      <td style="font-size: 12px"><br>Non-SLA related fields (defaults are 0's).</td>     
     </tr>        
     <tr>
      <td>Txn delay:</td>
      <td><form:input path="txnDelay" type="text" pattern="^-?\d*\.{0,1}\d+$" value="${map.sla.txnDelay}" /></td>     
     </tr>       
     <tr>
      <td>Xtra num:</td>
      <td><form:input path="xtraNum" type="text" pattern="^-?\d*\.{0,1}\d+$" value="${map.sla.xtraNum}" /></td>     
     </tr>
     <tr>
      <td>Xtra int:</td>
      <td><form:input path="xtraInt" type="text" pattern="-?\d*" value="${map.sla.xtraInt}" /></td>      
     </tr>      
     <tr>
      <td><br>Reference:</td>
      <td><br><form:textarea path="slaRefUrl" maxlength="1000" style="width:100%;height:70px" value="${map.sla.slaRefUrl}" /></td>
     </tr>
     <tr>
      <td>Comment :</td>
      <td><form:input path="comment" size="80" maxlength="126" value="${map.sla.comment}"  /></td>     
     </tr>    
            
     <tr>
      <td><br></td>
      <td><br><input type="submit" value="Save" /></td>
     </tr>
     
     <tr>
       <td colspan="2"><a href="slaList?reqApp=${map.reqApp}">Cancel</a></td>
     </tr>     
    </table>
    
    <form:hidden path="application"      value="${map.sla.application}" />
    <form:hidden path="txnId"            value="${map.sla.txnId}" />
    <form:hidden path="isCdpTxn"         value="${map.sla.isCdpTxn}" />
    <form:hidden path="slaOriginalTxnId" value="${map.sla.slaOriginalTxnId}" />
   </form:form>
  </div>

</div>

</body>
</html>