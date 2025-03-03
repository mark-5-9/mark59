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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<title>Display Item</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content">

 <h1>Reindexing Result</h1>   		 
  
 <table>
   <tr>
    <td>Application </td><td>:</td>
    <td id=application>${policySelectionCriteria.application}</td>
   </tr>
   <tr>
    <td>Lifecycle </td><td>:</td>
    <td id=lifecycle>${policySelectionCriteria.lifecycle}</td>
   </tr> 
   <tr>
    <td>Useability </td><td>:</td>
    <td id=useability>${policySelectionCriteria.useability}</td>
   </tr> 
  </table>
 
  <br><br>
  <table  class="tip">
      <tr><td>Success?</td><td>:</td><td id=reindexResultSuccess>${model.reindexResultSuccess}</td></tr>
      <tr><td>Message</td><td>:</td><td id=reindexResultMessage>${model.reindexResultMessage}</td></tr>
      <tr><td>Rows Moved</td><td>:</td><td id=reindexResultRowsMoved>${model.reindexResultRowsMoved}</td></tr>
      <tr><td>Index Row Count</td><td>:</td><td id=reindexResulIxCount>${model.reindexResulIxCount}</td></tr>
  </table>

  <br><br><a href='policies_breakdown_action?${model.navUrParms}'>Back</a>
</div>  
</body>
</html>
