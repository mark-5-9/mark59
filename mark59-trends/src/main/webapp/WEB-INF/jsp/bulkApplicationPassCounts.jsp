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
  Date:    Australian Winter 2019
  -->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<title>Mark59 - Bulk Load SLAs</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
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
      <td colspan=2><form:select path="application" items="${map.applications}" onchange="clearInnerHTML('errMsg');reloadSlaBulkLoadPage()" />
          <span style="font-size: 12px">&nbsp;&nbsp;&nbsp;(with baselines only)</span></td>
     </tr>
     <tr>
      <td>Include CDP Txns? :</td>
      <td colspan=2><form:select path="isIncludeCdpTxns" items="${map.isIncludeCdpTxnsYesNo}" /></td>
     </tr>
     
     <tr>
      <td><br><b>DEFAULT VALUES</b><br><br></td>
      <td style="font-size: 12px" width="10%"><br>Only applied to new SLAs.
      		<br>Enter <b>-1</b> for values you do not<br>want to set an SLA against.<br></td>
      <td style="font-size: 12px; padding-left: 20px;"><br>Check box(es) for the percentiles you <br>want copied from the baseline.
      		<br>Otherwise the value in the text box is used.</td>   
     </tr>
  
     <tr>
      <td>Ignore Txn on Graphs? :</td>
      <td colspan=2><form:select path="isTxnIgnored" items="${map.isTxnIgnoredYesNo}" /></td>      
     </tr>
     <tr>
      <td>90th percentile  :</td>
      <td><form:input path="sla90thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$" value="-1.0" /></td>     
      <td style="padding-left: 20px;">
      	<form:checkbox path="sla90thFromBaseline" onclick="resetElementAndReadonlyIfCheckboxTicked('sla90thFromBaseline1','sla90thResponse')" /></td>     
     </tr>
     <tr>
      <td>95th percentile  :</td>
      <td><form:input path="sla95thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$" value="-1.0" /></td> 
      <td style="padding-left: 20px;">
      	<form:checkbox path="sla95thFromBaseline" onclick="resetElementAndReadonlyIfCheckboxTicked('sla95thFromBaseline1','sla95thResponse')" /></td>     
     </tr> 
     <tr>
      <td>99th percentile  :</td>
      <td><form:input path="sla99thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$" value="-1.0" /></td>     
      <td style="padding-left: 20px;">
      	<form:checkbox path="sla99thFromBaseline" onclick="resetElementAndReadonlyIfCheckboxTicked('sla99thFromBaseline1','sla99thResponse')" /></td>     
     </tr>    
     <tr>
      <td>Pass count variance % :</td>
      <td colspan=2><form:input path="slaPassCountVariancePercent" type="text" pattern="^-?\d*\.{0,1}\d+$"  value="10.0" /></td>     
     </tr>     
     <tr>
      <td>Fail count :</td>
      <td colspan=2><form:input path="slaFailCount" type="text" pattern="-?\d*" value="-1"/></td>     
     </tr>
     <tr>
      <td>Fail percent :</td>
      <td colspan=2><form:input path="slaFailPercent" type="text" pattern="^-?\d*\.{0,1}\d+$"  value="2.0" /></td>     
     </tr>
     <tr>
      <td>Is Active :</td>
      <td><form:select path="isActive" items="${map.isActiveYesNo}" /></td>
     </tr> 
     <tr>
      <td></td>
      <td  colspan=2 style="font-size: 12px"><br>
      	Non SLA-related field default values (Only applied to new SLAs.) :
      </td>     
     </tr>     
     <tr>
      <td>Txn delay :</td>
      <td colspan=2><form:input path="txnDelay" type="text" pattern="^-?\d*\.{0,1}\d+$"  value="0.0" /></td>     
     </tr>       
     <tr>
      <td>Xtra num :</td>
      <td colspan=2><form:input path="xtraNum" type="text" pattern="^-?\d*\.{0,1}\d+$"  value="0.0" /></td>     
     </tr>
     <tr>
      <td>Xtra int :</td>
      <td colspan=2><form:input path="xtraInt" type="text" pattern="-?\d*" value="0"/></td>      
     </tr>      
      
     <tr>
      <td><br><br><b>SLA REFERENCE</b><br><br></td>
      <td colspan=2 style="font-size: 12px"><br><br>If entered, will be applied to new or all Application SLAs (for transactions that exist on the baseline), as selected.<br><br></td>     
     </tr>
         
     <tr>
      <td>Reference :</td>
      <td colspan=2><form:textarea path="slaRefUrl" maxlength="1000" style="width:100%;height:50px" onkeyup="enableOrdisableApplyRefUrlOption()" /></td> 
     </tr>
     <tr>
      <td>Apply to :</td>
      <td colspan=2><form:select path="applyRefUrlOption" items="${map.applyRefUrlOptions}"  /></td>     
     </tr>

     <tr>
      <td></td>
      <td colspan=2><br><input id=submit type="submit" value="Load" /></td>
     </tr>
      
     <tr>
      <td colspan="3"><a href="slaList?reqApp=${reqApp}">Cancel</a></td>
     </tr>
    </table>
   </form:form>
  </div>

</div>

</body>
</html>
