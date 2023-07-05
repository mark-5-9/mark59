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
<title>Server Metrics Via Web - View and Test Server Profile</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>

<style>
table { border-collapse: collapse;}
.cb { border: 0px }
.cb th { font-size: 14px; color: white; background-color: maroon; border: 1px solid maroon; text-align: left;}
.cb td { border: 1px solid maroon;}
.readonly { background-color: #f1f1f1; border: 0px solid white;}
.readonly:focus{border: 0 none #FFF; overflow: hidden; outline:none;}
table
.nb td {  background-color: white; border: 2px white solid; color: black; }
</style>

<script type="text/javascript">
function myFunction(myMessage) {
    alert(myMessage);
}
</script>


</head>

<body onload="hideElement('responseTable');" > 

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

  <h1>View and Test Server Profile</h1> 
  <p>&nbsp;</p> 
 
  <c:if test="${map.reqErr != ''}">
	<p style="color:red"><b>${map.reqErr}</b></p> 
  </c:if>   
  
  <div>

   <table>
     
     <tr><td>Server Profile</td>	<td>:</td><td id='serverProfile'>${map.serverProfileEditingForm.serverProfile.serverProfileName}</td></tr>
     <tr><td>Command(s)Executor</td><td>:</td><td>${map.serverProfileEditingForm.serverProfile.executor}</td></tr>
     
     <c:set var="commandexecutor" value="${serverProfileEditingForm.serverProfile.executor}" />

     <c:if test="${!commandexecutor.equals('GROOVY_SCRIPT')}">
	     <tr><td>Server</td>		    <td>:</td><td>${map.serverProfileEditingForm.serverProfile.server}</td></tr>
	     <tr><td>Alt Server Id</td>	    <td>:</td><td>${map.serverProfileEditingForm.serverProfile.alternativeServerId}</td></tr>
	     <tr><td>Username</td>		    <td>:</td><td>${map.serverProfileEditingForm.serverProfile.username}</td></tr>
	     <tr><td>Connection Port</td>   <td>:</td><td>${map.serverProfileEditingForm.serverProfile.connectionPort}</td></tr>
	     <tr><td>Connection Timeout</td><td>:</td><td>${map.serverProfileEditingForm.serverProfile.connectionTimeout}</td><tr>
     </c:if> 
      
     <tr><td>Comment</td>           <td>:</td><td>${map.serverProfileEditingForm.serverProfile.comment}</td><tr>
     <tr><td><br></td><td></td><td></td></tr>

     <tr> 
      <td>Parameters</td><td>:</td>
      <td>
 		<table>    
        <c:forEach items="${serverProfileEditingForm.commandParameters}" var="commandParameter"  varStatus="status" >
     		<tr id="commandParameters${status.index}" >
    		   <td>${commandParameter.paramName}${commandParameter.paramDuplicated}</td>
    		   <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
    		   <td><textarea class="readonly" readonly style="width:600px;height:16px" >${commandParameter.paramValue}</textarea></td>
     		</tr>
        </c:forEach>
        </table> 
      </td> 
     </tr> 
     <tr><td><br></td><td></td></tr>

     <c:if test="${!commandexecutor.equals('GROOVY_SCRIPT')}">
	     <tr> 
	      <td>Selected Commands</td><td>:</td>
	      <td>
	 		<table><tr>    
	        <c:forEach items="${serverProfileEditingForm.commandSelectors}" var="commandSelector"  varStatus="status"   >
	            <c:if test="${commandSelector.commandChecked}">
	    	       <td><a href="editCommand?&reqCommandName=${commandSelector.commandName}">${commandSelector.commandName}</a>&nbsp;</td>
	    	    </c:if>   
	        </c:forEach>
	        </tr></table> 
	      <td> 
	     </tr> 
     </c:if>  
 
     <c:if test="${commandexecutor.equals('GROOVY_SCRIPT')}">
	     <tr> 
	      <td>Command</td><td>:</td>
		  <td><a href="editCommand?&reqCommandName=${serverProfileEditingForm.selectedScriptCommandName}">${serverProfileEditingForm.selectedScriptCommandName}</a></td>
	     </tr> 
	     <tr><td><br></td><td></td></tr>
   	 </c:if>    
 
	<tr><td colspan="3"><td><br></tr>
    <tr>
      <td><button type="button" onclick="testConnection('true')">Run Profile</button></td><td></td><td id='testConnectionTestModeResult'></td>
    </tr>
    <tr><td colspan="3"><td><br></tr>
    
    <tr>
      <td></td><td></td>	
	  <td>	   
		<table id='responseTable' class="cb"> 
 		    <tr><th colspan="2">Response Details</th></tr>
			<tr><td>Server<br>Profile</td><td id='testConnectionServerProfile'></td></tr>
		    <tr><td>Txn(s)<br>Summary</td><td id='testConnectionCommandResponses'></td></tr>
		    <tr><td>Command<br>Log</td>	  <td id='testConnectionLogLines'></td></tr>
		 </table>	  	   
	  </td>
	</tr>

	<tr><td colspan="3"><td><br></tr>
    <tr>
      <td colspan="3">
        <a href="serverProfileList?reqExecutor=${map.reqExecutor}">Servers Profiles</a>&nbsp;&nbsp;
        <a href="editServerProfile?&reqServerProfileName=${map.serverProfileEditingForm.serverProfile.serverProfileName}&reqExecutor=${map.reqExecutor}">Edit Server Profile</a>&nbsp;&nbsp;
    	<a href="javascript:testConnection('false')" >API Link</a>     
      </td>
    </tr>
   </table>
     
  </div>
  <input type="hidden" id="apiAuthToken" value="${serverProfileEditingForm.apiAuthToken}" />

</div>
</body>
</html>
