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
<title>Mark59 - Add New Transactional SLA</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>

<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
  
 <h1>Add New Transactional SLA</h1> 

<p>&nbsp;</p>

  <div>
    
   <c:if test="${param.reqErr != ''}">
		<p style="color:red"><b>${param.reqErr}</b></p> 
  </c:if>  
    

   <form:form method="post" action="insertSla?reqApp=${reqApp}" modelAttribute="sla">
    <table>
     <tr>
      <td>Application :</td>
      <td><form:input path="application"  value="${reqApp}" size="32" maxlength="32" /></td>
     </tr>
     <tr>
      <td>Transaction name :</td>
      <td><form:input path="txnId" size="80" maxlength="122" /></td>
     </tr>
     
     <tr>
      <td>Ignore Txn on Graphs?</td>
      <td><form:select path="isTxnIgnored" items="${isTxnIgnoredYesNo}" /></td>
     </tr>    
     <tr>
      <td><br></td>
      <td style="font-size: 12px"><br>Enter <b>-1</b> for values you do not want to set an SLA against.</td>     
     </tr>
     <tr>
      <td>90th percentile :</td>
      <td><form:input path="sla90thResponse" /></td>     
     </tr>
     <tr>
      <td>Pass count :</td>
      <td><form:input path="slaPassCount"  value="-1" /></td>     
     </tr>
     <tr>
      <td>Pass count variance % :</td>
      <td><form:input path="slaPassCountVariancePercent"  value="10.0" /></td>     
     </tr>     
     <tr>
      <td>Fail count :</td>
      <td><form:input path="slaFailCount"  value="-1" /></td>     
     </tr>
     <tr>
      <td>Fail percent :</td>
      <td><form:input path="slaFailPercent"  value="2.0" /></td>     
     </tr>     
      <tr>
      <td><br>Reference :</td>
      <td><br><form:textarea path="slaRefUrl" value="" maxlength="1000" style="width:100%;height:70px" /></td>     
     </tr>
     <tr>
      <td>Comment :</td>
      <td><form:input path="comment"  value="" size="80" maxlength="126" /></td>     
     </tr>    

     <tr>
      <td><br></td>
      <td><br><input type="submit" value="Save" /></td>
     </tr>
     
     <tr>
      <td colspan="2"><a href="slaList?reqApp=${reqApp}">Cancel</a></td>
     </tr>
    </table>
   </form:form>
  </div>


</div>

</body>
</html>
