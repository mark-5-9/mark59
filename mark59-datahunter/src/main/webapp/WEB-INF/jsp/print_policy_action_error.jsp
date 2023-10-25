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
<!DOCTYPE>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<title>Display Item Error</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content">

 <h1>Display Item - Lookup Error !</h1>   		 
  
 <table >
     <tr><td>application</td><td>:</td><td>${policySelectionCriteria.application}</td></tr>
     <tr><td>identifier</td> <td>:</td><td>${policySelectionCriteria.identifier}</td></tr>          
     <tr><td>lifecycle</td>  <td>:</td><td>${policySelectionCriteria.lifecycle}</td></tr>
 </table>

 <br><br>statement executed : ${model.sql}
 <br><br> result: [${model.sqlResult}]  &nbsp;&nbsp; ${model.sqlResultText}<br>
 
 <br><a href='print_policy?${model.navUrParms}'>Back</a> 
</div>
</body>
</html>
