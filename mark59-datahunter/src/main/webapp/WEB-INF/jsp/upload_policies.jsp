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
  Date:    Australian Spring 2023
  --%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Upload Items File</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
<script type="text/javascript">

function buildHomeLink() {
	document.getElementById('HomeLink').innerHTML="Home Page";
	homeLinkUrl = "/mark59-datahunter" 
	//	+ "?application="+ encodeURIComponent(document.getElementById("application").value);
	document.getElementById('HomeLink').href = homeLinkUrl;
}

</script>
</head>
<body onload="buildHomeLink();"> 
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content"> 

  <h1>Upload File of Items Data</h1>   
  
  <form:form method="post" action="upload_policies_action" modelAttribute="uploadPoliciesFile"  enctype="multipart/form-data">
    
  <p style="color:red"><b>${validationerror}</b></p> 
   
   <table>
    <tr>
     <td class="tip" colspan=3>Note that files with a large number of lines (over 100K) may take several minutes to load
      (the Bulk load is faster). <br>
      Application MaxFileSize = 5GB, but you may hit other application or network capacity limits below this size<br><br></td> 
    </tr>     

    <tr>
     <td>Type of Upload</td>
     <td>:</td>
     <td><form:select path="typeOfUpload" items="${TypeOfUploads}" /></td>
    </tr>         
    <tr>
     <td class="tip" colspan=3><br></td> 
    </tr> 
    
    <tr>
     <td colspan="3"><input type="file" name="file" /></td>  <%-- set via @RequestParam --%>
    </tr>   
    <tr>
     <td colspan="3"><br><br><input type="submit" value="submit" id="submit" onclick="hideSubmitBtn();" /></td>     
    </tr>
    <tr>
     <td colspan="3"><span id="loading" class="loading" style="display: none;" >Loading.</span></td>     
    </tr>
    
   </table>
  </form:form>
 
  <br><a id="HomeLink" href="see_buildHomeLink_JS">Home Page</a> 
</div>    
</body>
</html>