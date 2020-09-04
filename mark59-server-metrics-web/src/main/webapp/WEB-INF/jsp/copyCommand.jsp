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
<title>Server Metrics Via Web - Copy Command</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
</head>

<body onload="displayWinOnlyPredefinedVars()"> 

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

  <h1>Copy Command</h1> 
  <p>&nbsp;</p> 
  
  <div>
    
   <c:if test="${map.reqErr != ''}">
		<p style="color:red"><b>${map.reqErr}</b></p> 
   </c:if>      
 
   <form:form method="post" action="insertCommand?reqExecutor=${map.reqExecutor}" modelAttribute="commandEditingForm" >
    <table >
    
     <tr>
      <td>Command&nbsp;Name&nbsp;:</td>
      <td><form:input path="command.commandName" value="" size="64" maxlength="64"  height="20" /></td>
     </tr>
   
     <tr>
      <td>Command&nbsp;Executor&nbsp;:</td>
      <td><form:select path="command.executor"  items="${map.commandExecutors}" value="${map.commandEditingForm.command.executor}"   onChange="displayWinOnlyPredefinedVars()"  /></td>      
     </tr>

     <tr>
      <td></td>
      <td id="winOnlyPredefinedVars" colspan=2 style="font-size: 9px" ><b>predefined (win only):&nbsp;&nbsp;</b>%SERVER_METRICS_WEB_BASE_DIR%</td>          
     </tr>

     <tr>
      <td>Command&nbsp;:</td>
      <td><form:textarea path="command.command" value="" style="width:100%;height:50px" maxlength="4000"  /></td>
     </tr>
   
     <tr>
      <td>Ignore&nbsp;StdErr?:</td>
      <td><form:select path="command.ingoreStderr"  items="${map.ingoreStderrYesNo}" value="${map.commandEditingForm.command.ingoreStderr}" /></td>      
     </tr>
     
     <tr>
      <td>Comment&nbsp;:</td>
      <td><form:input path="command.comment" size="64" maxlength="128"  height="20" /></td>
     </tr>
    
     <tr><td><br></td><td></td></tr>
     <tr> 
      <td>Response&nbsp;Parsers</td>
      <td>
 		<table>    
        <c:forEach items="${commandEditingForm.scriptSelectors}" var="scriptSelector"  varStatus="status"   >
     		<tr>
     		   <td><form:checkbox path="scriptSelectors[${status.index}].scriptChecked" /></td>     		   
     		   <td><a href="viewCommandResponseParser?&reqScriptName=${scriptSelector.scriptName}">${scriptSelector.scriptName}</a></td>
    		   <td><form:hidden path="scriptSelectors[${status.index}].scriptName" /></td>
     		</tr>
        </c:forEach>
        </table> 
      <td> 
     </tr> 
     <tr><td><br></td><td></td></tr>
 
     <tr>
      <td> </td>
      <td><input type="submit" value="Save" /></td>
     </tr>

     <tr>
      <td colspan="2"><a href="commandList?reqExecutor=${map.reqExecutor}">Cancel</a></td>
     </tr>

    </table>
     
   </form:form>
  </div>

</div>


</body>
</html>
