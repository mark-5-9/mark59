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
<title>Mark59 - Add New Graph</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>

</head>
<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 

 <h1>Add new Graph</h1> 

<p>&nbsp;</p>

  <div>

   <c:if test="${param.reqErr != ''}">
		<p style="color:red"><b>${param.reqErr}</b></p> 
  </c:if>  
  
   <form:form method="post" action="insertGraphMapping" modelAttribute="graphMapping">
    <table>
     <tr>
      <td>Dropdown list order:</td>
      <td><form:input path="listOrder" size="8" maxlength="8" /></td>
      <td></td>
     </tr>
      <tr>
      <td>Graph name:</td>
      <td><form:input path="graph"  size="64" maxlength="64"/></td>
      <td></td>
     </tr>
     
     <tr>
      <td>Transaction Type: </td>
      <td><form:select path="txnType"  items="${map.transactionTypes}" value="${map.graphMapping.txnType}" />
      <td></td>     
     </tr>
     
     <tr>
      <td>Value Derivation:</td>
      <td colspan=2 style="font-size: 12px" ><br>
      		<b>predefined:</b> Minimum, Maximum, Average, StdDeviation, 90th, Pass, Fail, Stop, First, Last, Sum  <br>
          	<b>available within sql:</b> TXN_MINIMUM, TXN_AVERAGE, TXN_MAXIMUM, TXN_STD_DEVIATION, TXN_90TH, TXN_PASS, TXN_FAIL, TXN_STOP<br>
          	<b>sample sql computation: </b>COALESCE( 100*TXN_FAIL/(TXN_PASS+TXN_FAIL) , -1)
      </td>          
     </tr>
 
     <tr>
      <td></td>
      <td colspan=2 ><form:input path="valueDerivation"  size="64"  maxlength="64"  /></td>
     </tr>
     
     <tr>
      <td>UOM Description:</td>
      <td><form:input path="uomDescription" size="64"  maxlength="64" /></td>
      <td></td>     
     </tr> 
  
     <tr>
      <td><br></td>
      <td style="font-size: 12px"><b>Format:</b> select ... as txn_id,..as bar_min,...as bar_max ..   <b>Available MySQL vars:</b> @runTime @application</td>
      <td></td>
     </tr>  
     
     <tr>
      <td>Range Bar Sql :</td>
      <td><form:textarea path="barRangeSql"  style="width:100%;height:150px" value="xxxx" maxlength="2000"  />
      </td>
     </tr> 
     
     <tr>
      <td><br></td>
      <td style="font-size: 12px">Legend is required if using Range Bars on this Graph</td>
      <td></td>
     </tr>       
     <tr>
      <td>Range Bar Legend :</td>
      <td><form:input path="barRangeLegend" value="${map.graphMapping.barRangeLegend}"  size="64"  maxlength="64"  />
      </td>
     </tr> 
         
     <tr>
      <td>Comment :</td>
      <td><form:input path="comment"  size="100" maxlength="126"  /></td>
      <td></td>     
     </tr>

     <tr>
      <td>�</td>
      <td><input type="submit" value="Save" /></td>
     </tr>
     <tr>
      
      <td colspan="2"><a href="graphMappingList">Cancel</a></td>
     </tr>
    </table>
   </form:form>
  </div>


</div>

</body>
</html>
