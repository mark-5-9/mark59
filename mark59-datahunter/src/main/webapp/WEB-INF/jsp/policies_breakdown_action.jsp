<%-- Copyright 2019 Mark59.com
 
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
  --%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
<title>Items BreakDown</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content">
 
  <h1>Items Breakdown - Results</h1>   	
  <span class="tip">See the sql statement below for selection criteria) &nbsp;&nbsp;&nbsp;&nbsp;( ${model.rowsAffected} rows )
  <br><br></span>
  <table  class=metricsTable >
   <tr>
	<th width="20px"></th>   
    <th>application</th>
    <th>lifecycle</th>
    <th>useability</th>    
    <th>count</th>
    <th style="display:none;"></th>
   </tr>
   <c:forEach var="countPoliciesBreakdownForm" items="${model.countPoliciesBreakdownFormList}">
    <c:set var="app_id" value="${countPoliciesBreakdownForm.application.replaceAll('[^A-Za-z0-9-]', '_')}" /> 
    <c:set var="lcy_id" value="${countPoliciesBreakdownForm.lifecycle.replaceAll('[^A-Za-z0-9-]', '_')}" /> 
    <tr>
     <td><a href="select_multiple_policies_action?${countPoliciesBreakdownForm.lookupParmsUrl}" title="lookup">
     		<img src="icons/lookup.png"/></a></td>    
     <td id=application>${countPoliciesBreakdownForm.application}</td>
     <td id=lifecycle>${countPoliciesBreakdownForm.lifecycle}</td>
     <td id=useability>${countPoliciesBreakdownForm.useability}</td>
     <td id=${app_id}_${lcy_id}_${countPoliciesBreakdownForm.useability}_count>${countPoliciesBreakdownForm.rowCount}</td>
	 <td style="display:none;" id=counter>${countPoliciesBreakdownForm.rowCount}</td>
    </tr>
   </c:forEach>
  </table>
 
 <br><br>
 <table class="tip" >
     <tr><td>sql&nbsp;statement</td><td>{</td><td id=sql>${model.sql}</td></tr> 
     <tr><td>result</td>			<td>:</td><td id=sqlResult>${model.sqlResult}</td></tr>
     <tr><td>rows&nbsp;affected</td><td>:</td><td id=rowsAffected>${model.rowsAffected}</td></tr>
     <tr><td>details</td>			<td>:</td><td id=sqlResultText>${model.sqlResultText}</td></tr>     
 </table>

 <br><a href='policies_breakdown?${model.navUrParms}'>Back</a>
</div> 
</body>
</html>
