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
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>
<html>
<head>
<title>Copy Event Mapping Entry</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>

<body>
  
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

 <h1>Copy Metric Event Mapping Entry </h1>

<p>&nbsp;</p>

  <div>
   <form:form method="post" action="insertEventMapping?reqMetricSource=${map.reqMetricSource}" modelAttribute="eventMapping">
    <table >
      <tr>
      <td>Metric Source :</td>
      <td><form:select path="metricSource"  items="${map.metricSources}" /></td>
     </tr>
      <tr>
      <td>Match When Like (SQL format) :</td>
      <td><form:input path="matchWhenLike" size="140" height="20" maxlength="150"  /></td>
     </tr>
     <tr>
      <td>Map to Metric Transaction Type :</td>
      <td><form:select path="txnType"  items="${map.metricTypes}" /></td>
     </tr>     
     <tr>
      <td>Target Name Left Boundary :</td>
      <td><form:input path="targetNameLB"  size="100" height="20" maxlength="126" /></td>
     </tr>
     <tr>
      <td>Target Name Right Boundary :</td>
      <td><form:input path="targetNameRB"  size="100" height="20" maxlength="126" /></td>
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
      <td><form:input path="comment"  size="140" height="20"  maxlength="140"  /></td>     
     </tr>  
     <tr>
      <td> </td>
      <td><input type="submit" value="Save" /></td>
     </tr>
     <tr>
      
      <td colspan="2"><a href="eventMappingList?reqMetricSource=${map.eventMapping.metricSource}">Cancel</a></td>
     </tr>
    </table>

   </form:form>
  </div>

</div>

</body>
</html>