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
<title>Mark59 - Edit Transactional SLA</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>

<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
 
 <h1>Edit Transactional SLA</h1>

<p>&nbsp;</p>
 
  <div>
   <form:form method="post" action="updateSla?reqApp=${map.reqApp}" modelAttribute="sla">
    <table>
     <tr>
      <td>Transaction (original):</td><td> ${map.reqTxnId} </td>
     </tr>
     <tr>
      <td>Transaction (rename):</td>
      <td><form:input path="txnId" size="80" maxlength="126"  />
      </td>
     </tr> 
     <tr>
      <td>Application:</td>
      <td><form:input path="application" size="32" maxlength="32" />
      </td>
     </tr>
     
     <tr>
      <td>Ignore Txn on Graphs?</td>
      <td><form:select path="isTxnIgnored" items="${map.isTxnIgnoredYesNo}" /></td>
     </tr>     
     <tr>
      <td><br></td>
      <td style="font-size: 12px"><br>Enter <b>-1</b> for values you do not want to set an SLA against.</td>     
     </tr>       
     <tr>
      <td>90thResponse:</td>
      <td><form:input path="sla90thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$"/></td>
     </tr>  
     <tr>
      <td>95thResponse:</td>
      <td><form:input path="sla95thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$"/></td>
     </tr>
     <tr>
      <td>99thResponse:</td>
      <td><form:input path="sla99thResponse" type="text" pattern="^-?\d*\.{0,1}\d+$"/></td>
     </tr> 
     <tr>
      <td>Pass Count:</td>
      <td><form:input path="slaPassCount" type="text" pattern="-?\d*" /></td>
     </tr>
      <tr>
      <td>Pass Count Variance %:</td>
      <td><form:input path="slaPassCountVariancePercent" type="text" pattern="^-?\d*\.{0,1}\d+$"/></td>
     </tr>  
     <tr>
      <td>Fail Count:</td>
      <td><form:input path="slaFailCount" type="text" pattern="-?\d*"/></td>
     </tr>  
     <tr>
      <td>Fail Percent:</td>
      <td><form:input path="slaFailPercent" type="text" pattern="^-?\d*\.{0,1}\d+$"/></td>
     </tr>
     <tr>
      <td><br></td>
      <td style="font-size: 12px"><br>Non-SLA related fields (defaults are 0's).</td>     
     </tr>        
     <tr>
      <td>Txn delay:</td>
      <td><form:input path="txnDelay" type="text" pattern="^-?\d*\.{0,1}\d+$" /></td>     
     </tr>       
     <tr>
      <td>Xtra num:</td>
      <td><form:input path="xtraNum" type="text" pattern="^-?\d*\.{0,1}\d+$" /></td>     
     </tr>
     <tr>
      <td>Xtra int:</td>
      <td><form:input path="xtraInt" type="text" pattern="-?\d*" /></td>      
     </tr>      
     <tr>
      <td><br>Reference:</td>
      <td><br><form:textarea path="slaRefUrl" value="" maxlength="1000" style="width:100%;height:70px" /></td>
     </tr>
     <tr>
      <td>Comment :</td>
      <td><form:input path="comment"  value="" size="80" maxlength="126" /></td>     
     </tr>    
            
     <tr>
      <td><br></td>
      <td><br><input type="submit" value="Save" /></td>
     </tr>
     
     <tr>
       <td colspan="2"><a href="slaList?reqApp=${map.reqApp}">Cancel</a></td>
     </tr>     
    </table>
    <form:hidden path="slaOriginalTxnId" />
   </form:form>
  </div>

</div>

</body>
</html>