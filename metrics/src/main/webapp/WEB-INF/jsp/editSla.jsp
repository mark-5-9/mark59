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
<title>Mark59 - Edit SLA Details</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>

<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
 
 <h1>Edit SLA Details</h1>

<p>&nbsp;</p>
 
  <div>
   <form:form method="post" action="updateSla?reqApp=${map.reqApp}" modelAttribute="sla">
    <table>
     <tr>
      <td>Transaction (original name):</td><td> ${map.reqTxnId} </td>
     </tr>
     <tr>
      <td>Transaction (new name)</td>
      <td><form:input path="txnId" size="100" />
      </td>
     </tr> 
     <tr>
      <td>Application</td>
      <td><form:input path="slaApplicationKey" />
      </td>
     </tr>
     
     <tr>
      <td>Ignore Txn on Graphs?</td>
      <td><form:select path="isTxnIgnored" items="${map.isTxnIgnoredYesNo}" />
      </td>
     </tr>     
           
     <tr>
      <td>90thResponse:</td>
      <td><form:input path="sla90thResponse" />
      </td>
     </tr>  
       
     <tr>
      <td>Pass Count:</td>
      <td><form:input path="slaPassCount"/>
      </td>
     </tr>
     
      <tr>
      <td>Pass Count Variance %:</td>
      <td><form:input path="slaPassCountVariancePercent" />
      </td>
     </tr>  
     <tr>    
       
     <tr>
      <td>Fail Count:</td>
      <td><form:input path="slaFailCount" />
      </td>
     </tr>  
     <tr>
      <td>Fail Percent:</td>
      <td><form:input path="slaFailPercent" />
      </td>
     </tr>       
     <tr>
      <td>Reference URL:</td>
      <td><form:input path="slaRefUrl" size="200" />
      </td>
     </tr>       
     <tr>
      <td> </td>
      <td><input type="submit" value="Save" />
      </td>
     </tr>
     <tr>
       <td colspan="2"><a href="slaList?reqApp=${map.reqApp}">Cancel</a></td>
     </tr>     
    </table>
    <form:hidden path="slaOriginalTxnId" />
   </form:form>
  </div>

</div>

</body>
</html>