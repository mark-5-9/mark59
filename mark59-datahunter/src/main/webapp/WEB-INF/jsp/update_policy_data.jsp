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
<title>Update Item</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
</head>
<body>
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content"> 

  <h1>Update an Item - Data Entry</h1>   		 
  <span class="tip">Update an Item's Non-Key Data.</span>
  <br><br>
   
  <form:form method="post" action="update_policy_action" modelAttribute="policies">
  
   <table>
    <tr>
     <td class="tip" colspan=3 >Item Key<br></td>
    </tr>    
    <tr><td>application </td>	<td>:</td><td id=application>${policies.application}</td></tr>
    <tr><td>identifier </td>	<td>:</td><td id=identifier>${policies.identifier}</td></tr>
    <tr><td>lifecycle </td>	<td>:</td><td id=lifecycle>${policies.lifecycle}</td></tr>     

    <tr>
     <td class="tip" colspan=3 ><br>Item Data<br></td>
    </tr>    
    <tr>
     <td>Useability</td>
     <td>:</td>            
     <td><form:select path="useability" items="${Useabilities}" /></td>
    </tr>
    <tr>
     <td>Otherdata</td>
     <td>:</td>            
     <td><form:textarea path="otherdata" /></td>
    </tr>        
    <tr>
     <td>EpochTime (msecs)</td>
     <td>:</td>            
     <td><form:input path="epochtime" maxlength="18" size="13" type="text" pattern="-?\d*" 
     		onchange="trimkey(this)" /></td> 
    </tr>        
    <tr>
     <td colspan="3"><br><input type="submit" value="submit" id="submit" /></td>
    </tr>
   </table>
   
   <form:hidden path="application"	value="${policies.application}" />    
   <form:hidden path="identifier"	value="${policies.identifier}" />    
   <form:hidden path="lifecycle"	value="${policies.lifecycle}" />    
   
  </form:form>
 
  <br><a href='update_policy?${model.navUrParms}'>Back</a><br>
</div> 
</body>
</html>
