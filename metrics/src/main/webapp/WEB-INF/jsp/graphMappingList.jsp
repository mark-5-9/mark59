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
<!DOCTYPE html>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>Mark59 - Graph Mapping Administration</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>
 
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content">    
  
  <h1>Graph Mapping Administration</h1>     

  <p><a href="registerGraphMapping">New Graph</a></p>

  <table class=metricsTable >
   <tr>
    <th></th>
    <th></th>   
    <th>Order</th>
    <th>Graph</th>
    <th>Data Type</th>
    <th>Value Derivation</th>    
    <th>UOM Description</th>
    <th>Range Bar Sql</th>
    <th>Range Bar Legend</th>
    <th>Comment</th>
   </tr>
   
   <c:forEach var="graphMapping" items="${graphMappingList}">
   
   	<c:url value="editGraphMapping" var="editUrl">
  		<c:param name="graph" value="${graphMapping.graph}" />
  		<c:param name="txnType" value="${graphMapping.txnType}" />  		
  		<c:param name="valueDerivation" value="${graphMapping.valueDerivation}" />  	
   		<c:param name="barRangeSql" value="${graphMapping.barRangeSql}" />  	
	</c:url>
   
    <tr>
     <td><a href="${editUrl}" title="Edit"><img src="images/edit.png"/></a></td>
     <td><a href="deleteGraphMapping?graph=${graphMapping.graph}" onclick="return confirm('Are you sure (graph=${graphMapping.graph})?');" title="Delete"><img src="images/delete.png"/></a></td>  
     <td>${graphMapping.listOrder}</td>
     <td>${graphMapping.graph}</td>
     <td>${graphMapping.txnType}</td>
     <td class="tdnowrap">${graphMapping.valueDerivation}</td>     
     <td>${graphMapping.uomDescription}</td>
     <td>${graphMapping.barRangeSql}</td>
     <td>${graphMapping.barRangeLegend}</td>
     <td>${graphMapping.comment}</td>
    </tr>
   </c:forEach>
  </table>
  
  </div>

</body>
</html>
