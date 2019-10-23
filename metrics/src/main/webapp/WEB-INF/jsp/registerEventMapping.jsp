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
  Date:    Australian Winter 2019
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>Add New Metric Event Mapping</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

<script type="text/javascript" >

function showHideMappingByTool() {
	var selectedMetricSource = document.getElementById('metricSource').value;
	if ( selectedMetricSource.startsWith('Loadrunner') ){
		document.getElementById('MatchJmeter').style.display = 'none';
		document.getElementById('MatchLoadrunner').style.display = 'table-row';		
	} else {
		document.getElementById('MatchJmeter').style.display = 'table-row';
		document.getElementById('MatchLoadrunner').style.display = 'none';		
 	}
}
		

function setMatchWhenLike() {
	var selectedMetricSource = document.getElementById('metricSource').value;
	if ( selectedMetricSource.startsWith('Loadrunner') ){
		document.getElementById("matchWhenLike").value = document.getElementById("lrEventType").value + ":" + document.getElementById("LikelrEventName").value; 	
	} else {
		document.getElementById("matchWhenLike").value = document.getElementById("jmeterMatchWhenLike").value; 
	}	
}

</script>
</head>

<body onload="showHideMappingByTool();" > 

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

  <h1>Add New Metric Event Mapping</h1> 
 
 <p>&nbsp;</p> 
  
  <div>
    
   <c:if test="${map.reqErr != ''}">
		<p style="color:red"><b>${map.reqErr}</b></p> 
   </c:if>      
 
   <form:form method="post" action="insertEventMapping?reqMetricSource=${map.reqMetricSource}" modelAttribute="eventMapping"  onsubmit="setMatchWhenLike()"  >
    <table >
      <tr>
      <td>Metric Source :</td>
      <td><form:select path="metricSource"  items="${map.metricSources}" value="${map.reqMetricSource}" onchange="showHideMappingByTool();"   /></td>
     </tr>

     <tr id=MatchJmeter>
      <td>Match When Jmeter Label Like : <br>(SQL format)<br></td>
      <td><input id="jmeterMatchWhenLike" name="jmeterMatchWhenLike" type="text"  value=""   size="140" height="20" /></td>
     </tr>
	 
 
     <tr id=MatchLoadrunner>
      <td style='white-space:nowrap;'>Loadrunner Event Match <br><i>[Event Type]</i> : <i>[Event Name Like]</i> : <br><br></td>
      <td style='white-space:nowrap;'>
      <input id="lrEventType" name="lrEventType" type="text"  value=""   size="30" height="20"  onchange="setMatchWhenLike()" /> : 
      <input id="LikelrEventName" name="LikelrEventName" type="text"  value=""   size="120" height="20" onchange="setMatchWhenLike()"  />
      </td>
     </tr>       


     <tr>
      <td>Map to Metric Transaction Type :</td>
      <td><form:select path="txnType"  items="${map.metricTypes}" value="${map.eventMapping.txnType}" /></td>
     </tr>
     <tr>
      <td>Target Name Left Boundary :</td>
      <td><form:input path="targetNameLB"  value=""  size="100" height="20" /></td>
     </tr>
     <tr>
      <td>Target Name Right Boundary :</td>
      <td><form:input path="targetNameRB"  value="" size="100" height="20"  /></td>
     </tr>
     <tr>
      <td>Is a Percentage Value? :</td>
      <td><form:select path="isPercentage"  items="${map.isPercentageYesNo}" /></td>
     </tr> 
     <tr>
      <td>Is Inverted Percentage :</td>
      <td><form:select path="isInvertedPercentage"  items="${map.isInvertedPercentageYesNo}" /></td>
     </tr> 
     
 	<%--  performanceTool field derived from the entered 'metricSource'  --%>
     
     <tr>
      <td>Comment :</td>
      <td><form:input path="comment"  value=""   size="140" height="20"  /></td>     
     </tr> 
       
     <tr>

      <td> </td>
      <td><input type="submit" value="Save" /></td>
     </tr>
     <tr>
      
      <td colspan="2"><a href="eventMappingList?reqMetricSource=${map.reqMetricSource}">Cancel</a></td>
     </tr>
     
     <tr><td><input type="hidden" id="matchWhenLike" name="matchWhenLike" value="setviajs" /></td></tr>

    </table>
     
   </form:form>
  </div>

</div>


</body>
</html>
