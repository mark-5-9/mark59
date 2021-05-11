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
<script type="text/javascript" src="javascript/asyncBulkAppplicationPassCounts.js"></script>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
</head>

<body onload="enableOrdisableApplyRefUrlOption();enableOrdisableSlaBulkLoadPageSubmitBtn()" >

 <%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

 <h1>Bulk Load Transaction Pass Counts for New and Existing Application SLAs</h1> 

<p>&nbsp;</p>
  
   <div>
    
   <c:if test="${param.reqErr != ''}">
		<p id=errMsg style="color:red"><b>${param.reqErr}</b></p> 
  </c:if>  


   <form:form method="post" action="insertOrUpdateApplicationPassCounts" modelAttribute="bulkApplicationPassCountsForm">
    <table>

     <tr>
      <td>Application :</td>
      <td><form:select path="application" items="${map.applications}" onchange="clearInnerHTML('errMsg');reloadSlaBulkLoadPage()" /></td>
     </tr>
     <tr>
      <td style="font-size: 12px">(with baselines only)</td>   
       <td></td>  
     </tr>
 
     <tr>
      <td><br><br><b>DEFAULT VALUES</b><br><br></td>
      <td style="font-size: 12px"><br><br>
      	Will be applied to new Application SLAs only.<br>
      	Enter <b>-1</b> for values you do not want to set an SLA against.
      </td>     
     </tr>
  
     <tr>
      <td>Ignore Txn on Graphs? :</td>
      <td><form:input path="isTxnIgnored"  value="N"  /></td>     
     </tr>
     <tr>
      <td>90th percentile  :</td>
      <td><form:input path="sla90thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$" value="-1.0" /></td>     
     </tr>
     <tr>
      <td>95th percentile  :</td>
      <td><form:input path="sla95thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$" value="-1.0" /></td>     
     </tr> 
     <tr>
      <td>99th percentile  :</td>
      <td><form:input path="sla99thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$" value="-1.0" /></td>     
     </tr>    
     <tr>
      <td>Pass count variance % :</td>
      <td><form:input path="slaPassCountVariancePercent" type="text" pattern="^-?\d*\.{0,1}\d+$"  value="10.0" /></td>     
     </tr>     
     <tr>
      <td>Fail count :</td>
      <td><form:input path="slaFailCount" type="text" pattern="-?\d*" value="-1"/></td>     
     </tr>
     <tr>
      <td>Fail percent :</td>
      <td><form:input path="slaFailPercent" type="text" pattern="^-?\d*\.{0,1}\d+$"  value="2.0" /></td>     
     </tr>  
     <tr>
      <td>Xtra num :</td>
      <td><form:input path="xtraNum" type="text" pattern="^-?\d*\.{0,1}\d+$"  value="0.0" /></td>     
     </tr>
      
     <tr>
      <td><br><br><b>SLA REFERENCE</b><br><br></td>
      <td style="font-size: 12px"><br><br>If entered, will be applied to new or all Application SLAs (for transactions that exist on the baseline), as selected.<br><br></td>     
     </tr>
         
     <tr>
      <td>Reference :</td>
      <td><form:textarea path="slaRefUrl" maxlength="1000" style="width:100%;height:50px" onkeyup="enableOrdisableApplyRefUrlOption()" /></td> 
     </tr>
     <tr>
      <td>Apply to :</td>
      <td><form:select path="applyRefUrlOption" items="${map.applyRefUrlOptions}"  /></td>     
     </tr>

     <tr>
      <td> </td>
      <td><br><input id=submit type="submit" value="Load" /></td>
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
