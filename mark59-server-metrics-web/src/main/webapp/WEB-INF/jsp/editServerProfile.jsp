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
  Date:    Australian Summer 2020
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>Server Metrics Via Web - Edit Server Profile</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
</head>

<body onload="enableOrdisableCreateCipherBtn('serverProfile.'); loadCommandListForSelected('serverProfile.')"> 

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

  <h1>Edit Server Profile</h1> 
  <p>&nbsp;</p> 
  
  <div>
    <form:form method="post" action="updateServerProfile?reqOperatingSystem=${map.reqOperatingSystem}" modelAttribute="serverProfileEditingForm" >

    <table>

     <tr><td>Server&nbsp;Profile&nbsp;:</td><td>${map.serverProfileEditingForm.serverProfile.serverProfileName}</td><td></td></tr>

     <tr>
      <td>Server&nbsp;:</td>
      <td><form:input path="serverProfile.server" size="64" maxlength="64"  height="20" /></td><td></td>
     </tr>
     <tr>
     <tr>
      <td>Alternative&nbsp;Server&nbsp;Id&nbsp;:</td>
      <td><form:input path="serverProfile.alternativeServerId" size="64" maxlength="64"  height="20"  /></td>
      <td style="font-size: 10px">for a '<b>localhost</b>' server, entering '<b>HOSTID</b>' here means txnIds<br>will use the id of the server running this application</td>
     </tr>
     
     <tr>
      <td>Username&nbsp;:</td>
      <td><form:input path="serverProfile.username" size="64" maxlength="64"  height="20" /></td><td></td>
     </tr>
     <tr>
      <td>Password&nbsp;:</td>
      <td><form:input path="serverProfile.password" size="64" maxlength="64"  height="20"  onkeyup="enableOrdisableCreateCipherBtn('serverProfile.')" /></td>
   	  <td><button type="button" id="createCipherBtn" onclick="createCipher('serverProfile.')">Create Cipher</button></td>
     </tr>
     <tr>
      <td>Password&nbsp;Cipher&nbsp;:</td>
      <td><form:input path="serverProfile.passwordCipher" size="64" maxlength="64"  height="20" /></td><td></td>
     </tr>
     <tr>
      <td>Operating&nbsp;System&nbsp;:</td>
      <td><form:select path="serverProfile.operatingSystem"  items="${map.operatingSystems}" value="${map.serverProfile.operatingSystem}" onchange="populateOsDefaults('serverProfile.')" /></td><td></td>
     </tr>
     <tr>
      <td>Connection&nbsp;Port&nbsp;:</td>
      <td><form:input path="serverProfile.connectionPort" size="10"  height="20"  type="number" min="0" max="2147483647"  /></td><td></td>
     </tr>
     <tr>
      <td>Connection&nbsp;Timeout&nbsp;:</td>
      <td><form:input path="serverProfile.connectionTimeout" size="10" height="20"  type="number" min="0" max="2147483647"  /></td><td></td>
     </tr>   
    <tr>
      <td>Comment&nbsp;:</td>
      <td colspan="2"><form:input path="serverProfile.comment"  size="100" maxlength="128"  height="20"  /></td>
     </tr>     
     
     <tr><td><br></td><td></td><td></td></tr>
     <tr> 
      <td>Commands</td>
      <td colspan="2">
 		<table>    
        <c:forEach items="${serverProfileEditingForm.commandSelectors}" var="commandSelector"  varStatus="status"   >
     		<tr id="commandSelectors${status.index}">
     		   <td><form:checkbox path="commandSelectors[${status.index}].commandChecked" /></td>     		   
     		   <td><a href="editCommand?&reqCommandName=${commandSelector.commandName}">${commandSelector.commandName}</a></td>
    		   <td><form:hidden path="commandSelectors[${status.index}].commandName" /></td>
    		   <td style="font-size: 10px">&nbsp;&nbsp;(${commandSelector.executor})</td>
    		   <td><form:hidden path="commandSelectors[${status.index}].executor" /></td>      		   
     		</tr>
        </c:forEach>
        </table> 
      <td> 
     </tr> 
     <tr><td><br></td><td></td><td></td></tr>
         
     <tr>
      <td></td>
      <td><input type="submit" value="Save" /></td><td></td>
     </tr>

     <tr>
      <td colspan="3"><a href="serverProfileList?reqOperatingSystem=${map.reqOperatingSystem}">Cancel</a></td>
     </tr>

    </table>
     
    <form:hidden path="serverProfile.serverProfileName" />
     
   </form:form>
  </div>

</div>


</body>
</html>