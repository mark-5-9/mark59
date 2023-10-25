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
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<html>
<head>
<title>DataHunter Home</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content"> 

<h1>The DataHunter - Main Menu</h1>   		 

<table>
	<tr><td valign="top"><img src="icons/information.png" height="30" width="30"/></td>		<td><a href="http://${urltoContext}/overview${reqUrlParms}">overview</a></td>
		<td class="tip">Summary Documentation. Includes DataHunter Rest API</td></tr>
	<tr><td valign="top"><img src="icons/count.jpg" height="30" width="30"/></td>	  		<td><a href="http://${urltoContext}/policies_breakdown${reqUrlParms}">count items breakdown</a></td>
		<td class="tip">Counts of all Items by Application (or Applications that 'StartWith'), Lifecycle and Useability</td></tr>
	<tr><td valign="top"><img src="icons/policies.png" height="30" width="30"/></td> 		<td><a href="http://${urltoContext}/select_multiple_policies${reqUrlParms}">manage multiple items</a></td>
		<td class="tip">View, Download and Delete Items within an Application (and further filters as needed)</td></tr>
	<tr><td valign="top"><img src="icons/add.png" height="30" width="30"/></td>				<td><a href="http://${urltoContext}/add_policy${reqUrlParms}">add an item</a></td></tr>
	<tr><td valign="top"><img src="icons/count.jpg" height="30" width="30"/></td>	  		<td><a href="http://${urltoContext}/count_policies${reqUrlParms}">count items</a></td>
		<td class="tip">Count of Item entries within an Application, and optionally Lifecycle and/or Useability</td></tr>
	<tr><td valign="top"><img src="icons/policy.png" height="30" width="30"/></td>	  		<td><a href="http://${urltoContext}/print_policy${reqUrlParms}">display an item</a></tr>
	<tr><td valign="top"><img src="icons/delete_sign.png" height="30" width="30"/></td>		<td><a href="http://${urltoContext}/delete_policy${reqUrlParms}">delete an item</a></td></tr>
	<tr><td valign="top"><img src="icons/use_next_item.png" height="30" width="30"/></td>	<td><a href="http://${urltoContext}/next_policy${reqUrlParms}${urlUseReqParmName}use">use next item</a></td>
		<td class="tip">Pick one Item within an Application, Useability, and optionally Lifecycle, and tag the item as USED</td></tr>
	<tr><td valign="top"><img src="icons/lookup_next_item.png" height="30" width="30"/></td><td><a href="http://${urltoContext}/next_policy${reqUrlParms}${urlUseReqParmName}lookup">lookup next item</a>
		<td class="tip">As for 'Use Next Item', but the Item is not updated</td></tr>
	<tr><td valign="top"><img src="icons/update_policy.png" height="30" width="30"/></td>	<td><a href="http://${urltoContext}/update_policy${reqUrlParms}">update an item</a></td>
		<td class="tip">Update the non-key fields for one Item</td></tr>
	<tr><td valign="top"><img src="icons/update_time_use.png" height=30 width="30"/></td>	<td><a href="http://${urltoContext}/update_policies_use_state${reqUrlParms}">update item(s) Use state</a></td>
		<td class="tip">Update Item entries Useabilty (also Epochtime), selected by Application, optionally by Id, Lifecycle, Useability.</td></tr>
	<tr><td valign="top"><img src="icons/asynchronous.jpg" height="30" width="30"/></td>	
		<td style="padding-right: 12px"><a href="http://${urltoContext}/async_message_analyzer${reqUrlParms}">asynchronous message analyzer</a></td>
		<td class="tip">Time differences betwenn asynchronous 'events' (see User Guide for more information)</td></tr>
	<tr><td valign="top"><img src="icons/upload.png" height="30" width="30"/></td>      	<td><a href="http://${urltoContext}/upload_ids${reqUrlParms}">upload identifiers file</a></td>
		<td class="tip">Upload a single-column file of 'Identifiers'</td></tr>	
	<tr><td valign="top"><img src="icons/upload.png" height="30" width="30"/></td>      	<td><a href="http://${urltoContext}/upload_policies${reqUrlParms}">upload items file</a></td>
		<td class="tip">Upload a csv format file of Items (same format as the Items download file in <a href="http://${urltoContext}/select_multiple_policies${reqUrlParms}">manage multiple items</a>)</td></tr>

	<c:if test="${currentDatabaseProfile == 'h2' }">
		<tr><td valign="top"><img src="icons/h2-logo.png" height="30" width="30"/></td> 	<td><a href="http://${urltoContext}/h2-console">H2 Database Console <br>URL: jdbc:h2:~/hunter</a></td></tr>
	</c:if>
	<c:if test="${currentDatabaseProfile == 'h2mem' }">
		<tr><td valign="top"><img src="icons/h2-logo.png" height="30" width="30"/></td> 	<td><a href="http://${urltoContext}/h2-console">H2 Database Console <br>URL: jdbc:h2:mem:huntermem</a></td></tr>
	</c:if>  	
</table>

<br>
<p>Version ${DATAHUNTER_VERSION} -  For User Guides and More: <a href=https://mark59.com>mark59.com</a> </p>  
</div>
</body>
</html>