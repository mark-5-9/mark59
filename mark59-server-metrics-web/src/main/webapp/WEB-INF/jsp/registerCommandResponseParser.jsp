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
<title>Server Metrics Via Web - Add New Command Response Parser</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
</head>

<body> 

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

  <h1>Add New Command Response Parser</h1> 
 
 <p>&nbsp;</p> 
  
  <div>
    
   <c:if test="${map.reqErr != ''}">
		<p style="color:red"><b>${map.reqErr}</b></p> 
   </c:if>      
 
   <form:form method="post" action="insertCommandResponseParser?reqMetricTxnType=${map.reqMetricTxnType}" modelAttribute="commandResponseParser" >
 
    <table >
     
     <tr>
      <td width="9%">Script&nbsp;Name</td><td>:</td>
      <td width="90%"><form:input path="scriptName" size="64" maxlength="64"  height="20" /></td>
     </tr>    
     <tr>
      <td width="9%">Metric&nbsp;Type</td><td>:</td>
      <td width="90%"><form:select path="metricTxnType"  items="${map.metricTxnTypes}" /></td>
     </tr>
     <tr>
      <td width="9%">Metric&nbsp;Name&nbsp;Suffix</td><td>:</td>
      <td width="90%"><form:input path="metricNameSuffix" size="64" maxlength="64"  height="20" /></td>
     </tr>
     
     <tr><td width="9%"></td><td></td><td width="90%" style="color:grey;font-size:small;">Available variable: <b>commandResponse</b></td></tr>
     
     <tr>
      <td width="9%">Script</td><td> </td>
      <td width="90%"><form:textarea path="script"  style="width:100%;height:200px" maxlength="4000" /></td>
     </tr>
     <tr>
      <td width="9%">Comment</td><td> </td>
      <td width="90%"><form:textarea path="comment" style="width:100%;height:20px"  maxlength="1000" /></td>
     </tr>
 
     <tr>
      <td width="9%">Sample&nbsp;Response</td><td> </td>
      <td width="90%"><form:textarea path="sampleCommandResponse" style="width:100%;height:50px"  maxlength="1000" /></td>
     </tr>
 
     <tr>
      <td width="9%"> </td><td> </td>
      <td width="90%"><input type="submit" value="Save" /></td>
     </tr>

     <tr>
      <td colspan="3"><a href="commandResponseParserList?reqMetricTxnType=${map.reqMetricTxnType}">Cancel</a></td>
     </tr>

    </table>
     
   </form:form>
  </div>

</div>


</body>
</html>
