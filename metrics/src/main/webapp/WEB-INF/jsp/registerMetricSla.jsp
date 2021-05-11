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
<title>Mark59 - Add New Metric SLA</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>

<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
 
 <h1>Add New Metric SLA</h1>
 
 <p>&nbsp;</p> 

  <div>
    
   <c:if test="${param.reqErr != ''}">
		<p style="color:red"><b>${param.reqErr}</b></p> 
  </c:if>  
    

   <form:form method="post" action="insertMetricSla?reqApp=${reqApp}" modelAttribute="metricSla">
    <table>
     <tr>
      <td>Application :</td>
      <td><form:input path="application"  value="${reqApp}" size="32" maxlength="32" /></td>
     </tr>
      <tr>
      <td>Metric Name :</td>
      <td><form:input path="metricName" size="80" maxlength="126"/></td>
     </tr>
      <tr>
      <td>Metric Type :</td>
      <td><form:select path="metricTxnType"  items="${metricTypes}" /></td>
     </tr>
     <tr>
      <td>Value Derivation :</td>
      <td><form:select path="valueDerivation"  items="${derivations}" /></td>
     </tr>
     <tr>
      <td>Min Value :</td>
      <td><form:input path="slaMin" type="text" pattern="^-?\d*\.{0,1}\d+$" value="0.0" /></td>     
     </tr>     
     <tr>
      <td>Max Value :</td>
      <td><form:input path="slaMax" type="text" pattern="^-?\d*\.{0,1}\d+$" value="0.0" /></td>     
     </tr>
     <tr>
      <td>Is Active :</td>
      <td><form:select path="isActive"  items="${isActiveYesNo}" /></td>
     </tr>       
     <tr>
      <td>Comment :</td>
      <td><form:input path="comment"  value="" size="80" maxlength="126" /></td>     
     </tr>    

     <tr>
      <td> </td>
      <td><input type="submit" value="Save" /></td>
     </tr>
 
     <tr>
      <td colspan="2"><a href="metricSlaList?reqApp=${reqApp}">Cancel</a></td>
     </tr>
    </table>
   </form:form>
  </div>

</div>

</body>
</html>
