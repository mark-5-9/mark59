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
<title>Mark59 - Add New SLA</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

</head>
<body>

 <%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

 <h1>Bulk Load Transaction Pass Counts for a New or Existing Application SLAs</h1> 

<p>&nbsp;</p>
  
   <div>
    
   <c:if test="${param.reqErr != ''}">
		<p style="color:red"><b>${param.reqErr}</b></p> 
  </c:if>  
    

   <form:form method="post" action="insertOrUdateApplicationPassCounts" modelAttribute="bulkApplicationPassCountsForm">
    <table>

     <tr>
      <td>Application Graph : <br><i> - must have a baseline ! </i></td>
      <td><form:select path="graphApplication" items="${map.applications}"  /></td>
     </tr>

     <tr>
      <td><br><br><b>DEFAULT VALUES</b><br><br></td>
      <td><br><br><i>(will be applied to new transactions only)</i><br><br></td>     
     </tr>
 
     <tr>
      <td>Ignore Txn on Graphs? :</td>
      <td><form:input path="isTxnIgnored"  value="N"  /></td>     
     </tr>
     <tr>
      <td>90th percentile  :</td>
      <td><form:input path="sla90thResponse"  value="-1"  /></td>     
     </tr>
     <tr>
      <td>Pass count variance % :</td>
      <td><form:input path="slaPassCountVariancePercent"  value="10.0" /></td>     
     </tr>     
     <tr>
      <td>Fail count :</td>
      <td><form:input path="slaFailCount"  value="-1" /></td>     
     </tr>
     <tr>
      <td>Fail percent :</td>
      <td><form:input path="slaFailPercent"  value="2.0" /></td>     
     </tr>     
      <tr>
      <td>Ref URL :</td>
      <td><form:input path="slaRefUrl"  value="" size="50"  /></td>     
     </tr>

     <tr>
      <td> </td>
      <td><br><input type="submit" value="Load" /></td>
     </tr>
     
     <tr>
      <td colspan="2"><a href="slaList?reqApp=${reqApp}">Cancel</a></td>
     </tr>
    </table>
   </form:form>
  </div>

</div>

</body>
</html>
