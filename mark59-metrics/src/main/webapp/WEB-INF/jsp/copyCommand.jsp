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
<title>Server Metrics Via Web - Copy Command</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
</head>

<body onload="sizeToFitText('command.command');sizeToFitText('paramNamesTextboxFormat');visibilyForCommandExecutor();"> 

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

  <h1>Copy Command</h1> 
  <p>&nbsp;</p> 
  
  <div>
    
   <c:if test="${map.reqErr != ''}">
		<p style="color:red"><b>${map.reqErr}</b></p> 
   </c:if>      
 
   <form:form method="post" action="insertCommand?reqExecutor=${map.reqExecutor}&fromAction=copy" modelAttribute="commandEditingForm" >
    <table >
    
     <tr>
      <td>Command&nbsp;Name&nbsp;:</td>
      <td width="99%"><form:input path="command.commandName" value="" size="64" maxlength="64" height="20" onchange="trimkey(this)" /></td>
     </tr>
   
    <tr>
      <td>Command&nbsp;Executor&nbsp;:</td>
      <td>${map.commandEditingForm.command.executor}</td>
     </tr>

     <tr>
      <td></td>
      <td id="predefinedVars" style="color:grey;font-size:small;"></td>          
     </tr>

     <tr>
      <td>Command&nbsp;:</td>
      <td><form:textarea path="command.command" spellcheck="false" /></td>
      
     </tr>
   
     <tr id=ignoreStdErrRow>
      <td>Ignore&nbsp;StdErr?:</td>
      <td><form:select path="command.ingoreStderr"  items="${map.ingoreStderrYesNo}" value="${map.commandEditingForm.command.ingoreStderr}" /></td>      
     </tr>
     <tr><td><br></td><td></td></tr>   
     
     <tr>
      <td>Comment&nbsp;:</td>
      <td><form:input path="command.comment"  size="100" maxlength="128"  height="20" /></td>
     </tr>
     <tr><td><br></td><td></td></tr>

     <tr>
      <td></td>
      <td id="specialParamNames" style="color:grey;font-size:small;"></td>          
     </tr>
         
     <tr id=paramNamesRow >
      <td>Parameter&nbsp;Names&nbsp;:</td>
      <td><form:textarea path="paramNamesTextboxFormat" spellcheck="false" maxlength="1000"/></td>
     </tr>
              
     <tr><td><br></td><td></td></tr>
     <tr id=responseParsersRow> 
      <td>Response&nbsp;Parsers</td>
      <td>
 		<table>    
        <c:forEach items="${commandEditingForm.parserSelectors}" var="parserSelector"  varStatus="status"   >
     		<tr>
     		   <td><form:checkbox path="parserSelectors[${status.index}].parserChecked" /></td>     		   
     		   <td><a href="viewCommandResponseParser?&reqParserName=${parserSelector.parserName}">${parserSelector.parserName}</a></td>
    		   <td><form:hidden path="parserSelectors[${status.index}].parserName" /></td>
     		</tr>
        </c:forEach>
        </table> 
      <td> 
     </tr> 
     <tr><td><br></td><td></td></tr>
  
     <tr>
      <td> </td>
      <td><button type="button" onclick="submitSaveCommand('exit')">Save and Exit</button>      
          <button type="button" onclick="submitSaveCommand('continue')">Save and Continue</button>
          <span style="font-size: 10px">&nbsp;&nbsp;&nbsp;Inactivity timeout: 30 min</span> 
      </td>
     </tr>

     <tr>
      <td colspan="2"><a href="commandList?reqExecutor=${map.reqExecutor}">Cancel</a></td>
     </tr>

    </table>
    
    <form:hidden path="command.executor" />    
    <form:hidden path="saveCmdAction" />        
     
   </form:form>
  </div>

</div>


</body>
</html>
