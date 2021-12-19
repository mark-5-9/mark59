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
<!DOCTYPE html>
<html>
<head>
<title>Mark59 - Overview</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

	<div class="content"> 
	  
		<h1>Overview</h1>     
		
		<div><img src="images/mark59_banner.jpg"/></div>
		
		<p>The Mark59 Performance and Volume Testing Analysis System allows you to view and analyse and test result trends over time.</p>
	
		<p>The system is a collection of the following components that can be launched from the left hand side menu:</p>  
		
		<ul>
			<li>Application Dashboard</li>
			<li>Trend Analysis</li>
			<li>Run List</li>
			<li>SLA Transactions</li>
			<li>SLA Metrics</li>
			<li>Rename Transactions</li>
			<li>Event Mapping Admin</li>
			<li>Graph Mapping Admin</li>
		</ul>
	
		<br>
		<br>
		<p>Version: 4.0.1.   Please see our User Guide and more at <a href="https://mark59.com" target="_blank">mark59.com</a></p>  
	
	</div>
</body>
</html>