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
<title>Server Metrics Via Web - Monitored Servers Profiles</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script>
	
	function resendCommandListExecutor()
	{
		var selectedReqExecutor = document.getElementById("executor").value;		
		window.location.href = 	"./serverProfileList?reqExecutor=" + selectedReqExecutor;
	}	

	function setSelectedExecutorInDropdown(reqExecutor)
	{    
	    var element = document.getElementById('executor');
	    element.value = reqExecutor;
	}	

</script>
</head>

<body onload="setSelectedExecutorInDropdown('${parmsMap.reqExecutor}') ">
              
              
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content">              

 <h1>Monitored Server Profiles</h1>     

 <p><a href="registerServerProfile?reqExecutor=${parmsMap.reqExecutor}">Add Server Profile</a></p>

 <p><a href="downloadServerProfiles">Download Server Profiles</a></p>


  <table class="metricsTable" >
   <tr>
    <th></th>
    <th></th>
    <th></th>
    <th>Server Profile Name</th>    
    <th>Command(s) Executor<br><form:select id='executor' path="parmsMap" items="${parmsMap.commandExecutors}"  onChange="resendCommandListExecutor()" /></th>
    <th>Server</th>
    <th>Alternative Server Id</th>        
    <th>Username</th>
    <th>Port</th>
    <th>Timeout</th>
    <th>Comment</th> 
    <th>Commands</th>     
   </tr>
   <c:forEach var="serverProfileWithCommands" items="${parmsMap.serverProfileWithCommandLinksList}">
    <tr>
     <td><a href="copyServerProfile?&reqServerProfileName=${serverProfileWithCommands.serverProfile.serverProfileName}&reqExecutor=${parmsMap.reqExecutor}" title="Copy"><img src="images/copy.png"/></a></td> 
     <td><a href="editServerProfile?&reqServerProfileName=${serverProfileWithCommands.serverProfile.serverProfileName}&reqExecutor=${parmsMap.reqExecutor}" title="Edit"><img src="images/edit.png"/></a></td>
     <td><a href="deleteServerProfile?&reqServerProfileName=${serverProfileWithCommands.serverProfile.serverProfileName}&reqExecutor=${parmsMap.reqExecutor}" onclick="return confirm('Are you sure (server profile : ${serverProfileWithCommands.serverProfile.serverProfileName})?');" title="Delete"><img src="images/delete.png"/></a></td>
     <td><a href="viewServerProfile?&reqServerProfileName=${serverProfileWithCommands.serverProfile.serverProfileName}&reqExecutor=${parmsMap.reqExecutor}">${serverProfileWithCommands.serverProfile.serverProfileName}</a></td>
     <td>${serverProfileWithCommands.serverProfile.executor}</td>
     <td>${serverProfileWithCommands.serverProfile.server}</td>
     <td>${serverProfileWithCommands.serverProfile.alternativeServerId}</td>
     <td>${serverProfileWithCommands.serverProfile.username}</td>
     <td>${serverProfileWithCommands.serverProfile.connectionPort}</td>
     <td>${serverProfileWithCommands.serverProfile.connectionTimeout}</td>
     <td>${serverProfileWithCommands.serverProfile.comment}</td>
     
     <td>
        <c:forEach var="commandName" items="${serverProfileWithCommands.commandNames}">
			<a href="editCommand?&reqCommandName=${commandName}">${commandName}</a><br>
        </c:forEach>
     </td>       
    
    </tr>
    </c:forEach>
  </table>
  
  </div>

</body>
</html>
