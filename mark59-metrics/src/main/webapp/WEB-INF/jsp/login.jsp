<!-- Copyright 2019 Mark59.com
 
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
  Date:    Australian Summer 2020
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%-- <%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>   --%>
<!DOCTYPE html>
<html>
<head>
<title>Server Metrics Via Web - Login</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />

<style>
   @font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }
   body {  margin: 0; font-family: "Lato", sans-serif;   display:table-cell;   vertical-align:middle;}
   div.content { margin-left: 190px;  padding: 20px; margin-bottom: 20px; }
   .canterbury {font-family: "Canterbury", Verdana, Tahoma;	font-size: 40px; text-align: center;	color: maroon;	padding: 10px;	border-bottom: solid 1px maroon;}
   h1 {  color: black;  font-family: arial, verdana, helvetica;  text-align: left;  font-size: 20px;}
  table.metricsTable  { width: 100%; border-collapse: collapse; }
  table.metricsTable td { font-size: 12px; color: #000000; background-color: white;  border: 1px solid #f1f1f1; padding: 4px; }
  .textarea { width: 35px; height: 15px; min-height: 10px; font-family: Arial, sans-serif; font-size: 12px; color: #444; padding: 5px;}
</style>
</head>
<body>

 <div class="content"> 

 <div class="canterbury">Mark59</div> 
 <p><b>Server Metrics Web Application Login</b></p>

 <form method="post" action="loginAction">
		
		<table>
			<c:if test="${map.reqErr != ''}">
	  			<tr>
        			<td></td><td>
						<p style="color:red"><b>${map.reqErr}</b></p> 
        			</td><td></td>
	  			</tr>
			</c:if>   
			<tr>
				<td>Username&nbsp;:</td>
				<td><input type="text" id="username" name="username" /></td>
				<td></td>
			</tr>
			<tr>
				<td>Password&nbsp;:</td>
				<td><input type="password" id="password" name="password" /></td>
				<td></td>
			</tr>

        	<tr>
        		<td></td>
        		<td><button type="submit">Log in</button></td>
        		<td></td>
  			</tr>

		</table>

 </form>
  </div>
</body>
</html>