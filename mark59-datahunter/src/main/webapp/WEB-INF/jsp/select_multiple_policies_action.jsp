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
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<html>
<head>
<title>Manage Multiple Items</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
<script type="text/javascript" src="javascript/sharedFunctions.js"></script>
<script type="text/javascript">

function buildRefreshLink() {
	document.getElementById('RefreshLink').innerHTML="Refresh Table";
	refreshLinkUrl = "select_multiple_policies_action" + urlParmeters();  
	document.getElementById('RefreshLink').href = refreshLinkUrl;
}

function buildMultipleDeleteLink() {
	document.getElementById('MultipleDeleteLink').innerHTML="Delete Selected Items";
	downloadLinkUrl = "delete_multiple_selected_policies" + urlParmeters();
	document.getElementById('MultipleDeleteLink').href = downloadLinkUrl;
}

function buildDownloadLink() {
	document.getElementById('DownloadLink').innerHTML="Download Selected Items";
	downloadLinkUrl = "download_selected_policies" + urlParmeters();  
	document.getElementById('DownloadLink').href = downloadLinkUrl;
}

function buildBackLink() {
	document.getElementById('BackLink').innerHTML="Back";
	backLinkUrl = "select_multiple_policies" + urlParmeters();  
	document.getElementById('BackLink').href = backLinkUrl;
}

function buildAddItemLink() {
	document.getElementById('AddPolicyLink').innerHTML="Add Item";
	addItemLinkUrl = "add_policy" 
		+ "?application=" + encodeURIComponent(document.getElementById("application").value)
		+ "&lifecycle="	+ encodeURIComponent(document.getElementById("lifecycle").value)
		+ "&useability=" + encodeURIComponent(document.getElementById("useability").value);
	document.getElementById('AddPolicyLink').href = addItemLinkUrl;
}


function urlParmeters() {
	// window.alert("identifierSelected = " + document.getElementById("identifierSelected").value );
	
	return "?application=" 	+ encodeURIComponent(document.getElementById("application").value)
	+ "&lifecycle=" 		+ encodeURIComponent(document.getElementById("lifecycle").value)
	+ "&useability=" 		+ encodeURIComponent(document.getElementById("useability").value)
	+ "&identifierLikeSelected="+ encodeURIComponent(document.getElementById("identifierLikeSelected").value)
	+ "&identifierLike=" 	+ encodeURIComponent(document.getElementById("identifierLike").value)	
	+ "&identifierListSelected="+ encodeURIComponent(document.getElementById("identifierListSelected").value)
	+ "&identifierList=" 	+ encodeURIComponent(document.getElementById("identifierList").value)		
	+ "&otherdataSelected=" + encodeURIComponent(document.getElementById("otherdataSelected").value)
	+ "&otherdata=" 		+ encodeURIComponent(document.getElementById("otherdata").value)
	+ "&createdSelected=" 	+ encodeURIComponent(document.getElementById("createdSelected").value)
	+ "&createdFrom=" 		+ encodeURIComponent(document.getElementById("createdFrom").value)
	+ "&createdTo=" 		+ encodeURIComponent(document.getElementById("createdTo").value)
	+ "&updatedSelected=" 	+ encodeURIComponent(document.getElementById("updatedSelected").value)
	+ "&updatedFrom=" 		+ encodeURIComponent(document.getElementById("updatedFrom").value)
	+ "&updatedTo=" 		+ encodeURIComponent(document.getElementById("updatedTo").value)
	+ "&epochtimeSelected=" + encodeURIComponent(document.getElementById("epochtimeSelected").value)
	+ "&epochtimeFrom=" 	+ encodeURIComponent(document.getElementById("epochtimeFrom").value)
	+ "&epochtimeTo=" 		+ encodeURIComponent(document.getElementById("epochtimeTo").value)
	+ "&selectOrder=" 		+ encodeURIComponent(document.getElementById("selectOrder").value)
	+ "&orderDirection=" 	+ encodeURIComponent(document.getElementById("orderDirection").value)
	+ "&limit=" 			+ encodeURIComponent(document.getElementById("limit").value);
}

</script>
</head>

<body onload="buildRefreshLink();buildAddItemLink();buildMultipleDeleteLink();buildDownloadLink();buildBackLink();">  
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content"> 


 <h1>Items With  : &nbsp;Application = ${policySelectionFilter.application}, Lifecycle = ${policySelectionFilter.lifecycle}, 
 	Useability = ${policySelectionFilter.useability}</h1>
 	 
 <span class="tip"><br>See the sql statement for further filters applied &nbsp;( ${model.rowsAffected} rows )</span>

 
 <p><a id="RefreshLink" href="see_buildRefreshLink_JS">Refresh Table</a>&nbsp;&nbsp;&nbsp;&nbsp;
    <a id="AddPolicyLink" href="see_buildAddItemLink_JS">Add Item</a>&nbsp;&nbsp;&nbsp;&nbsp;
    <a id="MultipleDeleteLink" href="see_buildMultipleDeleteLink_JS"
        onclick="return confirm('Are you sure? All matching items will be deleted, LIMIT does not apply!');">Delete Selected Items</a>&nbsp;&nbsp;&nbsp;&nbsp;
    <a id="DownloadLink" href="see_buildDownloadLink_JS">Download Selected Items</a>
 </p>
 
 <table id=printSelectedPoliciesTable class=metricsTable >
   <tr>
    <th></th>
    <th></th>   
    <th>application</th>
    <th>identifier</th>
    <th>lifecycle</th>
    <th>useability</th>    
    <th>otherdata</th>
    <th>created</th>
    <th>updated</th>
    <th>epochtime</th>    
   </tr>
   <c:forEach var="policiesForm" items="${model.policiesFormList}">
    <tr>
     <td><a href="update_policy_data?${policiesForm.policyKeyAsEncodedUrlParameters}" title="Update"><img src="icons/edit.png"/></a></td>
     <td><a href="delete_policy?${policiesForm.policyKeyAsEncodedUrlParameters}" title=Delete><img src="icons/delete.png"/></a></td>
     <td>${policiesForm.application}</td>
     <td>${policiesForm.identifier}</td>
     <td>${policiesForm.lifecycle}</td>
     <td>${policiesForm.useability}</td>
     <td>${policiesForm.otherdata}</td>
     <td>${policiesForm.created}</td>
     <td>${policiesForm.updated}</td>
     <td>${policiesForm.epochtime}</td>
    </tr>
   </c:forEach>
 </table>

 <br><br>
 <table class="tip">
     <tr><td>sql&nbsp;statement</td><td>{</td><td id=sql>${model.sql}</td></tr> 
     <tr><td>result</td>			<td>:</td><td id=sqlResult>${model.sqlResult}</td></tr>
     <tr><td>rows&nbsp;affected</td><td>:</td><td id=rowsAffected>${model.rowsAffected}</td></tr>
     <tr><td>details</td>			<td>:</td><td id=sqlResultText>${model.sqlResultText}</td></tr>     
 </table>

 <input type="hidden" id="application" 				value="${policySelectionFilter.application}" />
 <input type="hidden" id="lifecycle" 				value="${policySelectionFilter.lifecycle}" />
 <input type="hidden" id="useability" 				value="${policySelectionFilter.useability}" />
 <input type="hidden" id="selectOrder" 				value="${policySelectionFilter.selectOrder}" />
 <input type="hidden" id="identifierLikeSelected" 	value="${policySelectionFilter.identifierLikeSelected}" />
 <input type="hidden" id="identifierLike" 			value="${policySelectionFilter.identifierLike}" />
 <input type="hidden" id="identifierListSelected" 	value="${policySelectionFilter.identifierListSelected}" />
 <input type="hidden" id="identifierList" 			value="${policySelectionFilter.identifierList}" />
 <input type="hidden" id="otherdataSelected" 		value="${policySelectionFilter.otherdataSelected}" />
 <input type="hidden" id="otherdata" 				value="${policySelectionFilter.otherdata}" />
 <input type="hidden" id="createdSelected" 			value="${policySelectionFilter.createdSelected}" />
 <input type="hidden" id="createdFrom" 				value="${policySelectionFilter.createdFrom}" />
 <input type="hidden" id="createdTo" 				value="${policySelectionFilter.createdTo}" />
 <input type="hidden" id="updatedSelected" 			value="${policySelectionFilter.updatedSelected}" />
 <input type="hidden" id="updatedFrom" 				value="${policySelectionFilter.updatedFrom}" />
 <input type="hidden" id="updatedTo" 				value="${policySelectionFilter.updatedTo}" />
 <input type="hidden" id="epochtimeSelected" 		value="${policySelectionFilter.epochtimeSelected}" />
 <input type="hidden" id="epochtimeFrom" 			value="${policySelectionFilter.epochtimeFrom}" />
 <input type="hidden" id="epochtimeTo" 				value="${policySelectionFilter.epochtimeTo}" />
 <input type="hidden" id="orderDirection" 			value="${policySelectionFilter.orderDirection}" />
 <input type="hidden" id="limit" 					value="${policySelectionFilter.limit}" />

 <br><a id="BackLink" href="see_buildBackLink_JS" >Back</a>  
</div> 
</body>
</html>
