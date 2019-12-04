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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>DataHunter Home</title>
<link rel="shortcut icon"  href="favicon.png" />
<style>
  body { font-size: 18px; color: black; font-family: Calibri; }
  table.metricsTable  { width: 100%; border-collapse: collapse; }
  table.metricsTable th { font-size: 18px; color: white;   background-color: purple; border: 1px solid #9344BB; padding: 3px 7px 2px 7px; text-align: left; }
  table.metricsTable td { font-size: 15px; color: #000000; background-color: white;  border: 1px solid #9344BB; padding: 3px 7px 2px 7px; }
  a {  color: blue; }
  ol.nomarker {list-style-type: none;}  
  
  img.center {  display: block;  margin-left: auto;  margin-right: auto;}
}
  
</style>
</head>


<body style="background-color:white; ">

<br><br><br>
<h1 style="text-align:center;" >The Data Hunter</h1>
<h3 style="text-align:center;" >Designed for Performance and Volume Testing</h3>
<br>

		 
<table>
	<tr><td valign="top"><img src="icons/add.png" height="40" width="40" /></td>	  	<td><a href="http://${urltoContext}/add_policy${urlAppReqParm}">add_policy</a></td></tr>
	<tr><td valign="top"><img src="icons/count.jpg" height="40" width="40" /></td>	  	<td><a href="http://${urltoContext}/count_policies${urlAppReqParm}">count_policies</a></td></tr>
	<tr><td valign="top"><img src="icons/count.jpg" height="40" width="40" /> </td>	  	<td><a href="http://${urltoContext}/count_policies_breakdown${urlAppReqParm}">count_policies_breakdown</a></td></tr>
	<tr><td valign="top"><img src="icons/policy.png" height="40" width="40" /></td>	  	<td><a href="http://${urltoContext}/print_policy${urlAppReqParm}">print_policy</a></tr>
	<tr><td valign="top"><img src="icons/policies.png" height="40" width="40" /></td> 	<td><a href="http://${urltoContext}/print_selected_policies${urlAppReqParm}">print_selected_policies</a></td></tr>
	<tr><td valign="top"><img src="icons/delete.png" height="40" width="40" /> </td>  	<td><a href="http://${urltoContext}/delete_policy${urlAppReqParm}">delete_policy</a></td></tr>
	<tr><td valign="top"><img src="icons/remove.jpg" height="40" width="40" /></td>	  	<td><a href="http://${urltoContext}/delete_multiple_policies${urlAppReqParm}">delete_multiple_policies</a></td></tr>
	<tr><td valign="top"><img src="icons/use_next.jpg" height="40" width="40" /></td> 	<td><a href="http://${urltoContext}/next_policy${urlAppReqParm}${urlUseReqParmName}use">use_next_policy</a></td></tr>
	<tr><td valign="top"><img src="icons/use_next.jpg" height="40" width="40" /></td> 	<td><a href="http://${urltoContext}/next_policy${urlAppReqParm}${urlUseReqParmName}lookup">lookup_next_policy</a></td></tr>
	<tr><td valign="top"><img src="icons/update.png" height="40" width="40" /></td>	  	<td><a href="http://${urltoContext}/update_policies_use_state${urlAppReqParm}">Update Policy(s) Time and Use State</a></td></tr>
	<tr><td valign="top"><img src="icons/asynchronous.jpg" height="40" width="40"/></td><td><a href="http://${urltoContext}/async_message_analyzer${urlAppReqParm}">Asynchronous Message Analyzer</a></td></tr>
	<tr><td valign="top"><img src="icons/h2-logo.png" height="40" width="40"/></td>     <td><a href="http://${urltoContext}/h2-console">H2 Database Console (for H2 only)</a></td></tr>
</table>

<br>
<br>
		<p>Version: 2.1.0  -  For User Guides and More: https://mark59.com</p>  
</body>


</html>