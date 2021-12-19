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
  Date:    Australian Summer 2020
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<title>Server Metrics Via Web - Overview</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

	<div class="content"> 
	  
		<h1>Server Metrics Via Web - Overview</h1>     
		
		
		<p>Maintains a database of sever details, which can be used to enable connectivity from JMeter tests to servers for monitoring purposes during a performance test.</p>
	
		<p>This may be particularly useful when you are running a test in an environment where you do not have direct access to the application servers.</p>
		
		<p>For example, you may be running all your JMeter load generators in a cloud environment,
		 but security rules will not allow you do directly access non-Cloud servers you want to monitor.  
		 Then the Mark59 Server Metrics Via Web Application can act as a 'proxy' for you.</p>  
		

		<p>Version: 4.0.1   Please see our User Guide and more at <a href="https://mark59.com" target="_blank">mark59.com</a></p>  
	
	</div>
</body>
</html>