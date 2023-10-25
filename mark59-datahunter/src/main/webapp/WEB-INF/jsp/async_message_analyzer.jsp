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
<!DOCTYPE>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<title>Async Msg Analyzer</title>
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
		+ "&applicationStartsWithOrEquals=" + encodeURIComponent(document.getElementById("applicationStartsWithOrEquals").value)
		+ "&identifier=" + encodeURIComponent(document.getElementById("identifier").value)
		+ "&useability=" + encodeURIComponent(document.getElementById("toUseability").value);
	document.getElementById('HomeLink').href = homeLinkUrl;
}

</script>
</head>
<body onload="buildHomeLink();"> 
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content"> 

  <h1>Asynchronous Message Analyzer - Matching Criteria</h1>  
  <br>

  <form:form method="post" action="async_message_analyzer_action" modelAttribute="asyncMessageaAnalyzerRequest" >
   <table>
    <tr>
     <td>Application(s)</td>
     <td><form:select path="applicationStartsWithOrEquals" items="${applicationOperators}" /></td> 
     <td><form:input path="application" size="64" height="20" onchange="trimkey(this)" /></td>
    </tr>
    <tr><td></td><td></td>
     <td class="tip" colspan=2>required. Note that setting the application dropdown to 'STARTS_WITH'<br>
     and leaving a blank Application applies selection criteria across all Applications</td>
    </tr>     
    <tr>
     <td>Identifier</td>
     <td>:</td>
     <td><form:input path="identifier" value="" size="64" height="20" onchange="trimkey(this)" /></td>
    </tr>
    <tr>
     <td></td><td></td>
     <td class="tip">leave field blank to select all Identifiers within other criteria</td>
    </tr>     
    <tr>
     <td>Useability</td>
     <td>:</td>
     <td><form:select path="useability" items="${Useabilities}" value="${useability}" /></td>
    </tr> 
    <tr>
     <td class="tip" colspan=3 ><br></td>
    </tr>  
          
    <tr>
     <td>Update to Useability</td>
     <td>:</td>
     <td><form:select path="toUseability" items="${usabilityListTo}" /></td>
    </tr>
    <tr>
     <td></td><td></td>
     <td class="tip">leave dropdown unselected (blank) for no updates</td>
    </tr>  
    <tr>
     <td colspan="3"><br><br><input type="submit" value="submit"  id="submit" /></td>
    </tr>
   </table>
  </form:form>
  
  <br><a id="HomeLink" href="see_buildHomeLink_JS">Home Page</a>
</div>  
</body>
</html>
