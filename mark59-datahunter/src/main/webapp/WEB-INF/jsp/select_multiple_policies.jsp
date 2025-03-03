<%-- Copyright 2019 Mark59.com
 
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
  --%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<title>Manage Multiple Items</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
<script type="text/javascript">

function buildHomeLink() {
	document.getElementById('HomeLink').innerHTML="Home Page";
	homeLinkUrl = "/mark59-datahunter" 
		+ "?application="+ encodeURIComponent(document.getElementById("application").value)
		+ "&lifecycle="	 + encodeURIComponent(document.getElementById("lifecycle").value)
		+ "&useability=" + encodeURIComponent(document.getElementById("useability").value);
	document.getElementById('HomeLink').href = homeLinkUrl;
}

</script>
</head>
<body onload="buildHomeLink();"> 
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content"> 

  <h1>Manage Multiple Items - Selection Criteria</h1> 

  <form:form method="post" action="select_multiple_policies_action" modelAttribute="policySelectionFilter">
   <table >
    <tr>
     <td class="tip" colspan=3>Key Field Selection<br></td>
    </tr>  
    <tr>
     <td>Application</td><td>&nbsp;:&nbsp;</td>
     <td><form:input path="application" size="64" height="20" onchange="trimkey(this)" /></td>
    </tr>
    <tr>
     <td></td><td></td>
     <td class="tip">required</td>
    </tr>       
    <tr>
     <td>Lifecycle</td><td>&nbsp;:&nbsp;</td>
     <td><form:input path="lifecycle" size="64" height="20" onchange="trimkey(this)" /></td>
    </tr>
    <tr>
     <td></td><td></td>
     <td class="tip">leave field blank to select all Lifecycle values within other criteria</td>
    </tr>  
      
    <tr>
     <td class="tip" colspan=3 >Additional Filters<br></td>
    </tr>  
    <tr>
     <td>Useability</td><td>&nbsp;:&nbsp;</td>
     <td><form:select path="useability" items="${Useabilities}" value="${useability}" /></td>
    </tr>
    
    <tr>
     <td>Identifier Like</td><td>&nbsp;:&nbsp;</td>
     <td><form:checkbox path="identifierLikeSelected" />&nbsp;&nbsp;like&nbsp;<form:input path="identifierLike" size="64" height="20" /></td>
    </tr> 
    
    <tr>
     <td>Identifier List</td><td>&nbsp;:&nbsp;</td>
     <td><form:checkbox path="identifierListSelected" />&nbsp;&nbsp;in&nbsp;&nbsp;&nbsp;
     	 <form:textarea path="identifierList" style="width:82%;height:15px" /></td>
    </tr> 
      
    <tr>
     <td>Otherdata</td><td>&nbsp;:&nbsp;</td>
     <td><form:checkbox path="otherdataSelected" />&nbsp;&nbsp;like&nbsp;<form:input path="otherdata" size="64" height="20" /></td>
    </tr> 
    
    <tr>
     <td>Created</td><td>&nbsp;:&nbsp;</td>
     <td><form:checkbox path="createdSelected" />&nbsp;&nbsp;between&nbsp;
         <form:input path="createdFrom" maxlength="26" size="26" height="20" />&nbsp;and&nbsp;
         <form:input path="createdTo"   maxlength="26" size="26" height="20" /> </td>
    </tr>       

    <tr>
     <td>Updated</td><td>&nbsp;:&nbsp;</td>
     <td><form:checkbox path="updatedSelected" />&nbsp;&nbsp;between&nbsp;
         <form:input path="updatedFrom" maxlength="26" size="26" height="20" />&nbsp;and&nbsp;
         <form:input path="updatedTo"   maxlength="26" size="26" height="20" /> </td>
    </tr>       
    
    <tr>
     <td>Epoch Time</td><td>&nbsp;:&nbsp;</td>
     <td><form:checkbox path="epochtimeSelected" />&nbsp;&nbsp;between&nbsp;
         <form:input path="epochtimeFrom" type="text" pattern="-?\d*" maxlength="18" size="13" height="20" />&nbsp;and&nbsp;
         <form:input path="epochtimeTo"   type="text" pattern="-?\d*" maxlength="18" size="13" height="20" /> </td>
    </tr>       
     
    <tr>
     <td class="tip" colspan=3><br>List Order<br></td>
    </tr>  
    <tr>
     <td>Order By</td><td>&nbsp;:&nbsp;</td>
     <td><form:select path="selectOrder" items="${SelectOrders}" />&nbsp;&nbsp;&nbsp;
         <form:select path="orderDirection" items="${OrderDirections}" /></td>
    </tr>       
    
    <tr>
     <td colspan=3><br><br></td>
    </tr>    
    <tr>
     <td>Row Limit</td><td>&nbsp;:&nbsp;</td>
     <td><form:input path="limit" type="text" pattern="-?\d*" maxlength="4" size="4" onchange="trimkey(this)" /></td>      
    </tr>
    <tr>
     <td></td><td></td>
     <td class="tip" >maximum allowed limit is 1000</td>
    </tr>            
    
    <tr>
     <td colspan=3><br><input type="submit" value="submit" id="submit" /></td>
    </tr>
   </table>
  </form:form>

  <br><a id="HomeLink" href="see_buildHomeLink_JS">Home Page</a> 
</div>
</body>
</html>
