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
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<title>Count Items</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content">

 <h1>Items Count For Selection Criteria:</h1> 

 <table>
   <tr>
    <td>Application </td><td>:</td>
    <td>${policySelectionCriteria.application}</td>
   </tr>
   <tr>
    <td>Lifecycle </td><td>:</td>
    <td>${policySelectionCriteria.lifecycle}</td>
   </tr>       
   <tr>
    <td>Useability </td><td>:</td>
    <td>${policySelectionCriteria.useability}</td>
   </tr> 
 </table>

 <br><br>
 <div style='background-color:YELLOW;text-align:center;font-family:arial;color:brown;font-size:120px;' id=prettyCount>${model.rowsAffected}</div>

 <br><br>
 <table class="tip">
     <tr><td>sql&nbsp;statement</td><td>{</td><td id=sql>${model.sql}</td></tr> 
     <tr><td>result</td>			<td>:</td><td id=sqlResult>${model.sqlResult}</td></tr>
     <tr><td>rows&nbsp;affected</td><td>:</td><td id=rowsAffected>${model.rowsAffected}</td></tr>
     <tr><td>details</td>			<td>:</td><td id=sqlResultText>${model.sqlResultText}</td></tr>     
 </table>
  
 <br><a href='count_policies?${model.navUrParms}'>Back</a>
</div>  
</body>
</html>
