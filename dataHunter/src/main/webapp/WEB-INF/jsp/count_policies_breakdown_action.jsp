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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>Policies BreakDown</title>
<link rel="shortcut icon"  href="favicon.png" />
<style>
  body { font-size: 20px; color: purple; font-family: Calibri; }
  table.metricsTable  { width: 100%; border-collapse: collapse; }
  table.metricsTable th { font-size: 18px; color: white;   background-color: purple; border: 1px solid #9344BB; padding: 3px 7px 2px 7px; text-align: left; }
  table.metricsTable td { font-size: 15px; color: #000000; background-color: white;  border: 1px solid #9344BB; padding: 3px 7px 2px 7px; }
</style>

</head>
<body>
  <br><br>
  <b>Policies Breakdowns Table</b>  &nbsp; &nbsp; &nbsp; (see the sql statement below for selection criteria) &nbsp; &nbsp; &nbsp;&nbsp;( ${model.rowsAffected} rows )
  <br><br>


  <table  class=metricsTable >
   <tr>
    <th>application</th>
    <th>lifecycle</th>
    <th>useability</th>    
    <th>count</th>
    <th style="display:none;"></th>
   </tr>
   <c:forEach var="countPoliciesBreakdown" items="${model.countPoliciesBreakdownList}">
    <tr>
     <td id=application>${countPoliciesBreakdown.application}</td>
     <td id=lifecycle>${countPoliciesBreakdown.lifecycle}</td>
     <td id=useability>${countPoliciesBreakdown.useability}</td>
     <td id=${countPoliciesBreakdown.application}_${countPoliciesBreakdown.lifecycle}_${countPoliciesBreakdown.useability}_count>${countPoliciesBreakdown.rowCount}</td>
	 <td style="display:none;" id=counter>${countPoliciesBreakdown.rowCount}</td>
    </tr>
   </c:forEach>
  </table>
 
 
<br><br>

 <table >
     <tr><td>sql statement</td>	<td>:</td><td id=sql>${model.sql}</td></tr> 
     <tr><td>result</td>		<td>:</td><td id=sqlResult>${model.sqlResult}</td></tr>
     <tr><td>rows affected</td>	<td>:</td><td id=rowsAffected>${model.rowsAffected}</td></tr>
     <tr><td>details</td>		<td>:</td><td id=sqlResultText>${model.sqlResultText}</td></tr>     
 </table>

 <br><br>
 
  <a href='count_policies_breakdown?application=${policySelectionCriteria.application}'>Back</a>
 
</body>
</html>
