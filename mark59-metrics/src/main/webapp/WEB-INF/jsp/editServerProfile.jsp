<!-- Copyright 2019 Mark59.com
 
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
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
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

<body onload="enableOrdisableCreateCipherBtn('serverProfile.');visibilityForLocalhost();"> 

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

  <h1>Edit Server Profile</h1> 
  <p>&nbsp;</p> 
  
  <div>
    <form:form method="post" action="updateServerProfile?reqExecutor=${map.reqExecutor}" modelAttribute="serverProfileEditingForm" >

    <table>

     <tr><td>Server&nbsp;Profile&nbsp;:</td><td>${map.serverProfileEditingForm.serverProfile.serverProfileName}</td></tr>
     
     <tr><td>Command(s)&nbsp;Executor&nbsp;:</td><td>${map.serverProfileEditingForm.serverProfile.executor}</td></tr>
     
     <c:set var="commandexecutor" value="${serverProfileEditingForm.serverProfile.executor}" />
     
     <c:if test="${!commandexecutor.equals('GROOVY_SCRIPT')}">
	     <tr>
	      <td>Server&nbsp;:</td>
	      <td><form:input path="serverProfile.server" size="64" maxlength="64"  height="20" onchange="visibilityForLocalhost()" /></td>
	     </tr>
	     <tr>
	      <tr><td><br></td>
	      <td><span style="white-space: nowrap; font-size: 10px">for a '<b>localhost</b>' server, entering '<b>HOSTID</b>' here means txnIds will use the id of the server running this application</span></td>
	     </tr>     
	     <tr>
	      <td>Alternative&nbsp;Server&nbsp;Id&nbsp;:</td>
	      <td><form:input path="serverProfile.alternativeServerId" size="64" maxlength="64"  height="20" /></td>
	     </tr>
	     <tr id=usernameRow>
	      <td>Username&nbsp;:</td>
	      <td><form:input path="serverProfile.username" size="64" maxlength="64"  height="20" /></td>
	     </tr>
	     <tr id=passwordRow>
	      <td>Password&nbsp;:</td>
	      <td><form:input path="serverProfile.password" size="64" maxlength="64"  height="20"  onkeyup="enableOrdisableCreateCipherBtn('serverProfile.')"/>&nbsp;&nbsp;
	   	       <button type="button" id="createCipherBtn" onclick="createCipher('serverProfile.')">Create Cipher</button></td>
	     </tr>
	     <tr id=passwordCipherRow>
	      <td>Password&nbsp;Cipher&nbsp;:</td>
	      <td><form:input path="serverProfile.passwordCipher" size="64" maxlength="64"  height="20" /></td>
	     </tr>
	     <c:if test="${commandexecutor.equals('SSH_LINUX_UNIX')}">
		     <tr>
		      <td>Connection&nbsp;Port&nbsp;:</td>
		      <td><form:input path="serverProfile.connectionPort" size="10"  height="20"  type="number" min="0" max="2147483647"  /></td>
		     </tr>
		     <tr>
		      <td>Connection&nbsp;Timeout&nbsp;:</td>
		      <td><form:input path="serverProfile.connectionTimeout" size="10" height="20"  type="number" min="0" max="2147483647"  /></td>
		     </tr>
	     </c:if>  
     </c:if> 
      
     <tr>
      <td>Comment&nbsp;:</td>
      <td><form:input path="serverProfile.comment"  size="100" maxlength="128"  height="20"  /></td>
     </tr>     
 
     <tr><td><br></td><td></td></tr>
     <tr> 
	   <td>Parameters</td>
	   <td>
	 	 <table>    
	       <c:forEach items="${serverProfileEditingForm.commandParameters}" var="commandParameter"  varStatus="status" >
	   		 <tr id="commandParameters${status.index}">
	  		   <td>${commandParameter.paramName}${commandParameter.paramDuplicated}</td>
	  		   <td><form:hidden path="commandParameters[${status.index}].paramName" /></td> 
	  		   <td><form:hidden path="commandParameters[${status.index}].paramDuplicated" /></td> 
	   		   <td><form:input path="commandParameters[${status.index}].paramValue"  size="100" height="20"  /></td>     
	   		 </tr>
	       </c:forEach>
	     </table> 
	   </td> 
	 </tr> 
              
     <tr><td><br></td><td></td></tr>

     <c:if test="${!commandexecutor.equals('GROOVY_SCRIPT')}">
       <tr> 
         <td>Commands</td>
         <td>
 		  <table>    
          <c:forEach items="${serverProfileEditingForm.commandSelectors}" var="commandSelector"  varStatus="status"   >
 			<c:set var="thisrowexecutor" value="${commandSelector.executor}" />
 			<c:if test="${commandexecutor.equals(thisrowexecutor)}">  
     			<tr id="commandSelectors${status.index}">
	     		   <td><form:checkbox path="commandSelectors[${status.index}].commandChecked" onChange="resubmitToRefreshParm()" /></td>     		   
	     		   <td><a href="editCommand?&reqCommandName=${commandSelector.commandName}">${commandSelector.commandName}</a></td>
	    		   <td><form:hidden path="commandSelectors[${status.index}].commandName" /></td>
	    		   <td style="font-size: 10px">&nbsp;&nbsp;(${commandSelector.executor})</td>
	    		   <td><form:hidden path="commandSelectors[${status.index}].executor" /></td>   
     			</tr>
			</c:if>       		   
          </c:forEach>
          </table> 
         <td>          
       </tr> 
     </c:if>  

     <c:if test="${commandexecutor.equals('GROOVY_SCRIPT')}">
	   <tr> 
	     <td>Command</td>
		 <td><form:select path="selectedScriptCommandName"  items="${serverProfileEditingForm.commandNames}" 
		 					value="${serverProfileEditingForm.selectedScriptCommandName}" onChange="resubmitToRefreshParm()" /></td>
	   </tr> 
   	 </c:if>
   	     
     <tr><td><br></td><td></td></tr>
                   
     <tr>
      <td></td>
      <td><input type="submit" value="Save" /></td>
     </tr>

     <tr>
      <td colspan="2"><a href="serverProfileList?reqExecutor=${map.reqExecutor}">Cancel</a></td>
     </tr>

    </table>
     
    <form:hidden path="serverProfile.executor" />
    <form:hidden path="serverProfile.serverProfileName" />
    <form:hidden path="selectedScriptCommandNameChanged" />
	<form:hidden path="commandNames" />    

   </form:form>
  </div>

</div>


</body>
</html>