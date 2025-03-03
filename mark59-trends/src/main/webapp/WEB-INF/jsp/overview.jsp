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
  Date:    Australian Winter 2019
  -->
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<title>Trends Overview</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
  
<h1>Trends Overview</h1>     

<div><img style="max-width: 100%;" src="images/mark59_banner_thin.png"/></div>

<p>The documentation below is an extract from the Mark59 User Guide available at <a href="https://mark59.com">The Mark59 Website</a>, 
in particular the 'Trends Web Application' and 'Trends Database Load' chapters should be referred to for further information.

<p>The Trends Web Application allows you to view and analyze test result trends over time.
The following is a brief summary of each of the options shown in the left hand side navigation menu. 
 
<h3>Application Dashboard</h3>

<p>The current status of applications under test at a glance. The SLA states refer to the last test for the application.
Note that the status is computed dynamically when you enter the dashboard (it may start to take several seconds to do this as your 
application list grows), so will reflect changes you have made to the SLA tables since last run. 'Active' or 'All' applications can
be displayed.
<p>You can copy (or copy/delete to rename) an application name from here.  Note to delete an application you need to first deactivate it
(edit > set Active flag to 'N').

<h3>Trend Analysis</h3>

<p>The Graphical Display and Table data that is the core component on the application. Hopefully the function of each graph should 
mostly make sense, although there are several things worth commenting on. 

<p>The range bars (and Range Bar Legend in the bottom right of the graph canvas) will depend on the graph being displayed. If you want to 
know exactly what the Range Bar is for a graph, you can see the SQL used in the 'Graph Mapping Admin' page.

<p>A transaction breaching a SLA will be displayed on the graphic with a red exclamation mark (!) beside it.  In the table data, 
the transaction name will be in red. However, the metric quantity is only shown in red if that is the quantity that caused the 
failure. 

<p>Where a transaction name exists on the SLA tables and has SLA set for it, but the transaction doesn't appear in the results 
for the last displayed run, it will appear on a table called 'Missing Transactions List' .

<p>A similar table, the 'Missing Metrics List', will display on the particular graph relevant to a metric that has had a metric 
SLA set, but the metric was not captured during the run. 

<p>Transactions that you don't want to see displayed on the graph can be removed in the 'SLA Metrics' option. The 'Ignored Transactions List' 
displays below the main transaction table as a reminder.

<p>Both metric and transactional SLA can be made inactive by setting the 'Active' flag to 'N' in their respective SLA Maintenance
Options. Inactive SLAs will be shown on a 'Disabled' metrics/transactions Table.

<h3>Run List</h3>

<p>A straightforward page providing functionality to delete an application run, or to edit it. The most important aspect of the 
edit function is the ability to mark a run as 'Baseline'. This allows selection of the run as a baseline on the Trend Analysis Graph. 
The latest baseline can also be used to automatically create transactional SLAs with defaulted value (next option).

<h3>SLA Transactions</h3>

<p>Maintenance of transaction-based SLAs for an application, also bulk creation of SLA data for an entire application, and ability to copy/delete 
an entire application's SLA data. We suggest you play around with the SLA data as uploaded from the supplied sample for DataHunter if you want to 
get a feel for the screens. Any changed SLA are applied to the Trend Analysis page graphic as soon as you redraw the graph, so you can see what 
constitutes passed/failed/ignored transactions as you make changes.

<h3>SLA Metrics</h3>

<p>Maintenance of metric based SLAs for an application - DATAPOINT, CPU_UTIL and MEMORY statistics.  As for transactional SLAs, any changes are applied 
to the Trend Analysis page graphic as soon as you redraw the graph, so you can see what constitutes passed or failed SLAs as you make changes.		


<h3>Rename Transactions</h3>

<p>A simple facility to help with the situation where you want to rename a transaction in a script, but keep the history of the transaction available 
in the Trend Analysis graphic.

<p>The main page includes the names of all transactions for the application. To help with context it also lists the last time the transaction appeared 
in a test, and how many tests it appears in. 

<p>Two forms of renames are allowed: when you want to rename a transaction to something not used before, or when you want to 'merge' one transaction 
with another.  You get a warning when you do a 'merge'... because you can't just undo it.  However, You cannot rename a transaction to a name of a 
transaction that appears in the same test, nor can you change the translation type of the transactions (a special case exists for 'CDP' transactions,
please refer to the User Guide).
		

<h3>Event Mapping Admin</h3>

<p>This page provides a mechanism to match groups of similarly named metric transactions to a metric type. This functionality is more relevant 
to LoadRunner, but could be useful in JMeter too - probably to map from an input 'metric source' of TRANSACTION to the metric data types of CPU_UTIL 
or MEMORY (eg when you are capturing metrics via some 3rd party tool that cannot set the datatype of the transaction).

<h3>Graph Mapping Admin</h3>		

Is an administrative function that we do not expect most users to be using often, if at all. It provides the ability to add further graph types into
Trend Analysis. Please refer to the User Guide for more detail, including a worked example.
	
<br>
<br>
<p>Version: 6.3  Please see our User Guide at <a href="https://mark59.com" target="_blank">mark59.com</a></p>  
	
</div>
</body>
</html>