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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<title>Update Items Use State</title>
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
		+ "&identifier=" + encodeURIComponent(document.getElementById("identifier").value)
		+ "&lifecycle="	 + encodeURIComponent(document.getElementById("lifecycle").value)
		+ "&useability=" + encodeURIComponent(document.getElementById("toUseability").value);
	document.getElementById('HomeLink').href = homeLinkUrl;
}

</script>
</head>
<body onload="buildHomeLink();"> 
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content"> 

  <h1>Update Items Use State and EpochTime - Selection Criteria</h1>   		 
   
  <form:form method="post" action="update_policies_use_state_action" modelAttribute="updateUseStateAndEpochTime">
   <table>
    <tr>
     <td class="tip" colspan=3 >Change the Use State for an Item or Items (when an identifier / lifecycle is not 
     entered) within an Application.<br>Epoch time can optionally be updated by entering a numeric value.<br></td>
    </tr>     
    <tr>
     <td>Application</td><td>:</td>
     <td><form:input path="application" size="64" height="20" onchange="trimkey(this)" /></td>
    </tr>
    <tr>
     <td>Identifier</td><td>:</td>
     <td><form:input path="identifier" value="" size="64" height="20" onchange="trimkey(this)" /></td>
    </tr>
    <tr>
     <td>Lifecycle</td><td>:</td>
     <td><form:input path="lifecycle" value="" size="64" height="20" onchange="trimkey(this)" /></td>
    </tr>     
    <tr>
     <td>From Useability</td><td>:</td>
     <td><form:select path="useability" items="${usabilityListFrom}"   /></td>
    </tr>
    <tr>
     <td>_____________________<br><br></td>
     <td></td>
    </tr>  
    <tr>
     <td>To Useability</td><td>:</td>
     <td><form:select path="toUseability" items="${usabilityListTo}"   /></td>
    </tr>
    <tr>
     <td>To Epoch Time (msecs)</td><td>:</td>
     <td><form:input path="toEpochTime" maxlength="18" size="13" height="20" type="text" pattern="-?\d*" 
     		onchange="trimkey(this)" /></td>
    </tr>  
    <tr>
     <td colspan="3"><br><input type="submit" value="submit" id="submit" /></td>
    </tr>
   </table>
   </form:form>
 
 <br><a id="HomeLink" href="see_buildHomeLink_JS">Home Page</a>  
</div> 
</body>
</html>
