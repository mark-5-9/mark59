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
<title>Server Metrics Via Web - Commands List</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

<script>
	
	function resendCommandListExecutor(){
		var selectedReqExecutor = document.getElementById("executor").value;		
		window.location.href = 	"./commandList?reqExecutor=" + encodeURIComponent(selectedReqExecutor);
	}	

	function setSelectedExecutorInDropdown(reqExecutor){    
	    var element = document.getElementById('executor');
	    element.value = reqExecutor;
	}	
	
</script>
</head>

<body onload="setSelectedExecutorInDropdown('${parmsMap.reqExecutor}') ">
              
              
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content">              

 <h1>Commands List</h1>     

 <p><a href="registerCommand?reqExecutor=${parmsMap.reqExecutor}">Add Command</a></p>


  <table class="metricsTable" >
   <tr>
    <th></th>
    <th></th>
    <th></th>
    <th>Command Name</th>
    <th>Command Executor<br><form:select id='executor' path="parmsMap" items="${parmsMap.commandExecutors}"  onChange="resendCommandListExecutor()" /></th>
    <th>Command</th>
    <th>Ignore<br>StdErr?</th>
    <th>Comment</th> 
    <th>Response<br>Parsers</th>   
   </tr>
   <c:forEach var="commandWithParsers" items="${parmsMap.commandWithParsersList}">
    <tr>
     <td><a href="copyCommand?&reqCommandName=${commandWithParsers.command.commandName}&reqExecutor=${parmsMap.reqExecutor}" title="Copy"><img src="images/copy.png"/></a></td> 
     <td><a href="editCommand?&reqCommandName=${commandWithParsers.command.commandName}&reqExecutor=${parmsMap.reqExecutor}" title="Edit"><img src="images/edit.png"/></a></td>
     <td><a href="deleteCommand?reqCommandName=${commandWithParsers.command.commandName}&reqExecutor=${parmsMap.reqExecutor}" onclick="return confirm('Are you sure (commmand name : ${commandWithParsers.command.commandName})?');" title="Delete"><img src="images/delete.png"/></a></td>
     <td>${commandWithParsers.command.commandName}</td>     
     <td>${commandWithParsers.command.executor}</td>     
     <td class="small">${commandWithParsers.command.command}</td> 
     <td>${commandWithParsers.command.ingoreStderr}</td>   
     <td>${commandWithParsers.command.comment}</td>
     <td>
 		<table>    
        <c:forEach var="parserName" items="${commandWithParsers.parserNames}">
     		<tr><td><a href="viewCommandResponseParser?&reqParserName=${parserName}">${parserName}</a></td></tr>
        </c:forEach>
        </table> 
      </td>  
    </tr>
   </c:forEach>
  </table>
  
  </div>

</body>
</html>
