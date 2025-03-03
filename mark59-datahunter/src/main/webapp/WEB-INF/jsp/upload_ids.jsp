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
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Upload Ids File</title>
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


function usabilityForIndexedReuseableLoad(){
	var typeDd = document.getElementById("typeOfUpload");
	var selectedType = typeDd.value;
	var useDd = document.getElementById("useability");
	
	if (selectedType == "BULK_LOAD_AS_INDEXED_REUSABLE"){
		useDd.value = "REUSABLE";
		for (var i=0; i < useDd.options.length; i++) {
			if (useDd.options[i].value != "REUSABLE"){
				useDd.options[i].disabled = true;
			}
		}
	} else {
		for (var i=0; i < useDd.options.length; i++) {
			useDd.options[i].disabled = false;
		}
	}	
}

</script>
</head>
<body onload="buildHomeLink();usabilityForIndexedReuseableLoad()"> 
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content"> 

  <h1>Upload File of Identifiers</h1> 

  <form:form method="post" action="upload_ids_action" modelAttribute="uploadIdsFile"  enctype="multipart/form-data">
    
<p style="color:red"><b>${validationerror}</b></p> 
   
   <table>
    <tr>
     <td class="tip" colspan=3>Note that files with a large number of lines (over 100K) may take several minutes to load
      (the Bulk load types are faster).<br>
      Application MaxFileSize = 5GB, but you may hit other application or network capacity limits below this size<br><br></td> 
    </tr>     

    <tr>
     <td>Application(s)</td>
     <td>:</td>
     <td><form:input path="application" size="64" height="20" onchange="trimkey(this)" /></td>
    </tr>
    <tr>
     <td></td><td></td>
     <td class="tip">required</td>
    </tr>  
   
    <tr>
     <td>Lifecycle</td>
     <td>:</td>
     <td><form:input path="lifecycle" value="" size="64" height="20" onchange="trimkey(this)" /></td>
    </tr>
    <tr>
     <td></td><td></td>
     <td class="tip">optional.  Leaving empty creates items with a blank lifecycle 
     (Not recommended unless lifecycle is not going to be used at all within this Application)</td>
     
    </tr>

    <tr>
     <td>Useability</td>
     <td>:</td>
     <td><form:select path="useability" items="${Useabilities}" /></td>
    </tr>  
    <tr>
     <td></td><td></td>
     <td class="tip"></td>
    </tr>      
   
    <tr>
     <td>Type of Upload</td>
     <td>:</td>
     <td><form:select path="typeOfUpload" items="${TypeOfUploads}" onchange="usabilityForIndexedReuseableLoad()" /></td>
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