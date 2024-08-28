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
<title>DataHunter Overview</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content"> 

<h1>DataHunter Overview</h1>

<div><img style="max-width: 100%;" src="images/mark59_banner_thin.png"/></div>   		 
	 
<p>The documentation below is an extract from the Mark59 User Guide available at <a href="https://mark59.com">The Mark59 Website</a>, 
in particular the 'DataHunter Web Application' and 'DataHunterAPI' chapters. This includes details of where to find of extensive
examples using the DataHunter UI and Rest API.  

<h3>Data Structure</h3>

<p>The DataHunter data has a key 'Application,Identifier,Lifecycle'.  For general use 'Application,Identifier' is expected to be 
sufficient and you can leave 'Lifecycle' blank. The exception is for Timing Asynchronous Processes where 'Lifecycle' is needed.</p>
<p>The 'Useability' field should always be set, to one of REUSABLE, UNPAIRED, UNUSED, USED</p>

<p>'Epochtime' can be  user-controlled - a numeric value can be entered or updated for an entry.  When a DataHunter entry is added 
without an epochtime being specified, it is defaulted to the current UTC epoch time in milliseconds.  Note however
Timing Asynchronous Processes uses epochtime to calculate the time between events.
<p>The timestamp fields 'created' and 'updated' are not directly available to the user for manipulation in either the UI for Rest API. 

<h3>Timing Asynchronous Processes</h3>

<p>DataHunter provides a technique to time event(s) which occur asynchronously (betwwen the the first and last event for an identifiable
 data key). More information and a detailed example available in the Mark59 User Guide.  Also see the Rest API description below. 

<h3>DataHunter File Uploads/Downloads</h3> 

<p>You can download 'Selected Items' from using a Download link on the 'Manage Multiple Items'.  All items satisfying the criteria are 
downloaded in a csv file, the 'limit' is not applied.
<p>The file downloads with a set column format,  a header is included in the downloaded file:
<br><pre>APPLICATION,IDENTIFIER,LIFECYCLE,USEABILITY,OTHERDATA,EPOCHTIME</pre>
These are all the columns editable by the user for a DataHunter record.
<p>This same file format is used to upload new or existing data into DataHunter as well, using the  'Upload Items File' option. 
Basic validation checks are done during upload, and invalid lines reported in the results page. Includes:
<ul>
<li>Valid header (as above) exists (otherwise upload immediately fails)</li>
<li>Line hs correct (6) column count</li>
<li>A line of blank values is considered invalid</li>
<li>A blank 'Application' name is considered invalid</li>
<li>EPOCHTIME must be blank or numeric (its value is the value is set to System.currentTimeMillis() if a blank is passed in the file)</li> 
</ul>

<p>Also, there is an 'Upload File of Identifiers' option to upload a text file containing one 'identifier' entry on each line of the file.  
The 'application', 'lifecycle' and 'useability' data being set as options on the page.
<p>There is no corresponding Rest API for these UI only functions.


<h3>DataHunter API</h3> 

<p>The table below is a summary of the available DataHunter API Client methods, the corresponding Rest API Http calls, and related UI function. 
JavaDocs for the Rest service is available in the mark59-datahunter project, class com.mark59.datahunter.controller.DataHunterRestController.  
JavaDocs for the API Client from the mark59-datahunter-api project, class com.mark59.datahunter.api.rest.DataHunterRestApiClient</p>
<p>Note the Rest API examaples in the table actually work (updates use an Application 'testrest').

<table  class=metricsTable >
 <tr>
	<th>DataHunter UI</th>
	<th><br>
	Java Client API (com.mark59.datahunter.api.rest.DataHunterRestApiClient)<br>
	__________________________________________________________________<br><br>
	Http Rest call    ( dataHunterUrl/api/...)<br><br>
	</th>
 </tr>
 
 <tr>
	<td>/policies_breakdown<br><br><br><b>Intems Breakdown</b></td>
	<td>
	<p>public DataHunterRestApiResponsePojo policiesBreakdown(String applicationStartsWithOrEquals, String application, String lifecycle, 
	String useability)<br>
	__________________________________________________________________<br>	
   
	<p><b>.../api/policiesBreakdown?applicationStartsWithOrEquals={applicationStartsWithOrEquals}&amp;lifecycle={lifecycle}
	&amp;useability={useability}</b>
	
	<p>Optional parameters: lifecycle,useability
	
	<p>Allowed values for applicationStartsWithOrEquals: EQUALS | STARTS_WITH
	
	<p>Example: breakdown count by key of all entries on the database:
	
	<p><a href='api/policiesBreakdown?applicationStartsWithOrEquals=STARTS_WITH&application='>
	            api/policiesBreakdown?applicationStartsWithOrEquals=STARTS_WITH&amp;application=</a>
	</td>
 </tr>
 
 <tr>
	<td>/select_multiple_policies<br><br><br><b>Manage&nbsp;Multiple&nbsp;Items</b></td>
	<td>
	<br><b>Basic Selection:</b><br><br>   

	<p>public DataHunterRestApiResponsePojo printSelectedPolicies(String application, String lifecycle, String useability)<br>
	__________________________________________________________________<br>	

	<p><b>.../api/printSelectedPolicies?application={application}&amp;lifecycle={lifecycle}&amp;useability={useability}</b>
	
	<p>Optional parameters: lifecycle,useability<br>(default 100 rows max printed)
	
	<p>Example: print all UNUSED entries for an application:
	
	<p><a href='api/printSelectedPolicies?application=testrest&useability=UNUSED'>
	            api/printSelectedPolicies?application=testrest&amp;useability=UNUSED</a>
	
	
	<br><br><br><b>Selection Using Additional Filters::</b><br><br>  	
	
	<p>public DataHunterRestApiResponsePojo printSelectedPolicies(PolicySelectionFilter policySelectionFilter)<br>
	__________________________________________________________________<br>		
	
	<p><b>.../api/printSelectedPolicies?
		application={application}&amp;lifecycle={lifecycle}&amp;useability={useability}<br>
		&amp;selectOrder={ID_LIFECYCLE|USEABILTY_ID_LIFECYCLE|OTHERDATA|CREATED|UPDATED|EPOCHTIME}<br>
		&amp;otherdataSelected={true|false}&amp;otherdata={otherdata}<br>
		&amp;createdSelected={true|false}&amp;createdFrom={createdFrom}&amp;createdTo={createdTo}<br> 
		&amp;updatedSelected={true|false}&amp;updatedFrom={updatedFrom}&amp;updatedTo={updatedTo}<br>
		&amp;epochtimeSelected={true|false}&amp;epochtimeFrom={epochtimeFrom}&amp;epochtimeTo={epochtimeTo)<br>
		&amp;orderDirection={ASCENDING|DESCENDING}&amp;limit={limit}</b>
	
	<p>Optional parameters: lifecycle,useability, all additional filters<br>Default limit 100, Max limit 1000 rows
	
	<p>Example: again printing UNUSED entries for an application, in 'otherdata' order, and with the additional filters except 
	'otherdata' being set:
	<br><i>Hint: If you Add the items in the Add Item example, you will see rows returned when you execute this example.</i>  

	<p><a href='api/printSelectedPolicies?application=testrest&useability=UNUSED&selectOrder=OTHERDATA&
		otherdataSelected=false&otherdata=&
		createdSelected=true&createdFrom=2023-01-01+15%3A59%3A59.469937&createdTo=2099-12-31+23%3A59%3A59.999999&
		updatedSelected=true&updatedFrom=2023-01-01+15%3A59%3A59.469937&updatedTo=2099-12-31+23%3A59%3A59.999999&
		epochtimeSelected=true&epochtimeFrom=66&epochtimeTo=4102444799999&
		orderDirection=DESCENDING&limit=250'>
			api/printSelectedPolicies?application=testrest&amp;useability=UNUSED&amp;selectOrder=OTHERDATA&amp;
			otherdataSelected=false&amp;otherdata=&amp;
			createdSelected=true&amp;createdFrom=2023-01-01+15%3A59%3A59.469937&amp;createdTo=2099-12-31+23%3A59%3A59.999999&amp;
			updatedSelected=true&amp;updatedFrom=2023-01-01+15%3A59%3A59.469937&amp;updatedTo=2099-12-31+23%3A59%3A59.999999&amp;
			epochtimeSelected=true&amp;epochtimeFrom=66&amp;epochtimeTo=4102444799999
			&amp;orderDirection=DESCENDING&amp;limit=250</a>
	</td>
 </tr>
 
 <tr>
	<td>/delete_multiple_selected_policies<br>
	<br><br><b>Delete&nbsp;Selected&nbsp;Items</b><br>(an option within Manage&nbsp;Multiple&nbsp;Items)
	</td>
	<td>
	<br><b>Basic Selection:</b><br><br>   

	<p>public DataHunterRestApiResponsePojo deleteMultiplePolicies(String application, String lifecycle, String useability)<br>
	__________________________________________________________________<br>	

 	<p><b>.../api/deleteMultiplePolicies?application={application}&amp;lifecycle={lifecycle}&amp;useability={useability}</b>
	
	<p>Optional parameters: lifecycle,useability<br>
	
	<p>Example: delete all entries for an application:
	
	<p><a href='api/deleteMultiplePolicies?application=testrest'>
	            api/deleteMultiplePolicies?application=testrest</a>
 
	<p>Example: delete all USED entries for an application:
	
	<p><a href='api/deleteMultiplePolicies?application=testrest&useability=USED'>
	            api/deleteMultiplePolicies?application=testrest&amp;useability=USED</a>
 
 	<br><br><br><b>Selection Using Additional Filters::</b><br><br> 
 	 	
	<p>public deleteMultiplePolicies printSelectedPolicies(PolicySelectionFilter policySelectionFilter)<br>
	__________________________________________________________________<br>		
 
	<p><b>.../api/deleteMultiplePolicies?
		application={application}&amp;lifecycle={lifecycle}&amp;useability={useability}<br>
		&amp;otherdataSelected={true|false}&amp;otherdata={otherdata}<br>
		&amp;createdSelected={true|false}&amp;createdFrom={createdFrom}&amp;createdTo={createdTo}<br> 
		&amp;updatedSelected={true|false}&amp;updatedFrom={updatedFrom}&amp;updatedTo={updatedTo}<br>
		&amp;epochtimeSelected={true|false}&amp;epochtimeFrom={epochtimeFrom}&amp;epochtimeTo={epochtimeTo)</b>

	<p>Note that the filters work as per printSelectedPolicies, with selectOrder, orderDirection and limit ignored.
	<br>So <b>ALL ROWS matching the selection are deleted.</b>
 
 	<p>Optional parameters: lifecycle, useability, all additional filters
	
	<p>Example: Delete UNUSED entries for an application, with the additional filters except 'otherdata' being set
	<br><i>Hint: If you Add the items in the Add Item example, you will see that rows are deleted when you execute this example</i>  
 
 	<p><a href='api/deleteMultiplePolicies?application=testrest&useability=UNUSED&
 		otherdataSelected=false&otherdata=&
 		createdSelected=true&createdFrom=2023-01-01+15%3A59%3A59.469937&createdTo=2099-12-31+23%3A59%3A59.999999&
 		updatedSelected=true&updatedFrom=2023-01-01+15%3A59%3A59.469937&updatedTo=2099-12-31+23%3A59%3A59.999999&
 		epochtimeSelected=true&epochtimeFrom=66&epochtimeTo=4102444799999'>
 			api/deleteMultiplePolicies?application=testrest&amp;useability=UNUSED&amp;
	 		otherdataSelected=false&amp;otherdata=&amp;
	 		createdSelected=true&amp;createdFrom=2023-01-01+15%3A59%3A59.469937&amp;createdTo=2099-12-31+23%3A59%3A59.999999&amp;
	 		updatedSelected=true&amp;updatedFrom=2023-01-01+15%3A59%3A59.469937&amp;updatedTo=2099-12-31+23%3A59%3A59.999999&amp;
	 		epochtimeSelected=true&amp;epochtimeFrom=66&amp;epochtimeTo=4102444799999</a>
	</td>
 </tr>
 
 <tr>
	<td>/add_policy<br><br><br><b>Add&nbsp;Item</b></td>	
	<td>
	<p>public DataHunterRestApiResponsePojo addPolicy(Policies policies)<br>
	__________________________________________________________________<br>	
   
	<p><b>...api/addPolicy?application={application}&amp;identifier={identifier&amp;lifecycle={lifecycle}&amp;useability={useability}
	&amp;otherdata={otherdata}&amp;epochtime={epochtime}</b>
	
	<p>Optional parameters: otherdata, epochtime
	<p>Examples:
	
	<p><a href='api/addPolicy?application=testrest&identifier=id1&lifecycle=somelc&useability=UNUSED&otherdata=other&epochtime=12345'>
				api/addPolicy?application=testrest&amp;identifier=id1&amp;lifecycle=somelc&amp;useability=UNUSED&amp;otherdata=other&amp;
				epochtime=12345</a>
	
	<p><a href='api/addPolicy?application=testrest&identifier=id2&lifecycle=somelc&useability=UNUSED&otherdata='>
				api/addPolicy?application=testrest&amp;identifier=id2&amp;lifecycle=somelc&amp;useability=UNUSED&amp;otherdata=</a>

	</td>
 </tr>
 
 <tr>
	<td>/count_policies<br><br><br><b>Count&nbsp;Items</b></td>
	<td>
	<p>public DataHunterRestApiResponsePojo countPolicies(String application, String lifecycle, String useability)<br>
	__________________________________________________________________<br>	
   
	<p><b>.../api/countPolicies?application={application}&amp;lifecycle={lifecycle}&amp;useability={useability}</b>
	
	<p>Optional parameters: lifecycle,useability
	
	<p>Example: count all entries for an application
	
	<p><a href='api/countPolicies?application=testrest'>
				api/countPolicies?application=testrest</a>
	</td>
 </tr>
 
 <tr>
	<td>/print_policy<br><br><br><b>Display&nbsp;Item</b></td>
	<td>
	<p>DataHunterRestApiResponsePojo printPolicy(String application, String identifier, String lifecycle)<br>and<br>
	DataHunterRestApiResponsePojo printPolicy(String application, String identifier) **<br>
	&nbsp;&nbsp;&nbsp;** means a blank lifecycle, NOT any lifecycle)<br>
	__________________________________________________________________<br>	
   
	<p><b>.../api/printPolicy?application={application}&amp;identifier={identifier}&amp;lifecycle={lifecycle}</b>
	
	<p>Optional parameters: lifecycle (means a blank lifecycle)
	<p>Example:
	
	<p><a href='api/printPolicy?application=testrest&identifier=id2&lifecycle=somelc'>
				api/printPolicy?application=testrest&amp;identifier=id2&amp;lifecycle=somelc</a>
	</td>
 </tr>

 <tr>
	<td>/delete_policy<br><br><br><b>Delete&nbsp;Item</b></td>
	<td>
	<p>public DataHunterRestApiResponsePojo deletePolicy(String application, String identifier, String lifecycle)<br>
	__________________________________________________________________<br>	

	<p><b>.../api/deletePolicy?application={application}&amp;identifier={identifier}&amp;lifecycle={lifecycle}</b>
	
	<p>Optional parameters: none
	
	<p>Example: add, then delete an item with a blank lifecycle
	
	<p><a href='api/addPolicy?application=testrest&identifier=id6&lifecycle=&useability=UNUSED&otherdata=blanklc'>
				api/addPolicy?application=testrest&amp;identifier=id6&amp;lifecycle=&amp;useability=UNUSED&amp;otherdata=blanklc</a>
	
	<p><a href='api/deletePolicy?application=testrest&identifier=id6&lifecycle='>
				api/deletePolicy?application=testrest&amp;identifier=id6&amp;lifecycle=</a>
	</td>
 </tr>

 <tr>
	<td>/next_policy?pUseOrLookup=use<br><br><br><b>Use&nbsp;Next&nbsp;Item</b></td>
	<td>
	<p>public DataHunterRestApiResponsePojo useNextPolicy(String application, String lifecycle,String useability, String selectOrder)<br>
	__________________________________________________________________<br>	

	<p><b>.../api/useNextPolicy?application={application}&amp;lifecycle={lifecycle}&amp;useability={useability}&amp;selectOrder={selectOrder}</b>
	
	<p>Optional parameters: lifecycle
	
	<p>selectOrder values: SELECT_MOST_RECENTLY_ADDED | SELECT_OLDEST_ENTRY | SELECT_RANDOM_ENTRY
	
	<p>Example: return oldest UNUSED entry for an application for any lifecycle<br> 
		&nbsp;&nbsp;&nbsp;(its Useability will be set to USED by the call) 

	<p><a href='api/useNextPolicy?application=testrest&useability=UNUSED&selectOrder=SELECT_OLDEST_ENTRY'>
				api/useNextPolicy?application=testrest&amp;useability=UNUSED&amp;selectOrder=SELECT_OLDEST_ENTRY</a>
	</td>
 </tr>

 <tr>
	<td>/next_policy?pUseOrLookup=lookup<br><br><br><b>Lookup&nbsp;Next&nbsp;Item</b></td>
	<td>
	<p>public DataHunterRestApiResponsePojo lookupNextPolicy(String application, String lifecycle,String useability, String selectOrder)<br>
	__________________________________________________________________<br>	

	<p><b>.../api/lookupNextPolicy?application={application}&amp;lifecycle={lifecycle}&amp;useability={useability}&amp;selectOrder={selectOrder}</b>
	
	<p>Optional parameters: lifecycle
	
	<p>selectOrder values: SELECT_MOST_RECENTLY_ADDED | SELECT_OLDEST_ENTRY |SELECT_RANDOM_ENTRY
	
	<p>Note: Exactly the same as useNextPolicy, except the Useability of the selected item is not updated
	</td>
 </tr>

 <tr>
	<td>/update_policy<br><br><br><b>Update&nbsp;Item</b></td>
	<td>
	<p>public DataHunterRestApiResponsePojo updatePolicy(Policies policies)<br>
	__________________________________________________________________<br>	

	<p><b>.../api/updatePolicy?application={application}&amp;identifier={identifier}&amp;lifecycle={lifecycle}&amp;useability={useability}
		&amp;otherdata={otherdata}&amp;epochtime={epochtime}</b>

	<p>Optional parameters: otherdata, epochtime

	<p>Notes :<br> 
	'otherdata' is set empty if blank (nothing) passed<br>
	'epochtime' a long value, or if blank or non-numeric will to set to System.currentTimeMillis()<br>
	
	<p>Example: Updating the 'id1' policy from the 'Add Item' example with new useability, otherdata and epochtime values 

	<p><a href='api/updatePolicy?application=testrest&identifier=id1&lifecycle=somelc&useability=UNUSED&otherdata=updated&epochtime=12346'>
				api/updatePolicy?application=testrest&amp;identifier=id1&amp;lifecycle=somelc&amp;useability=UNUSED&amp;
				otherdata=updated&amp;epochtime=12346</a>
	</td>
 </tr>

 <tr>
	<td>/update_policies_use_state<br><br><br><b>Update&nbsp;Use&nbsp;States</b></td>
	<td>
	<p>public DataHunterRestApiResponsePojo updatePoliciesUseState(String application, String identifier, String lifecycle, String useability, 
		String toUseability, String toEpochTime)<br>
	__________________________________________________________________<br>	

	<p><b>.../api/updatePoliciesUseState?application={application}&amp;identifier={&identifier}&amp;lifecycle={&lifecycle}&amp;
		useability={useability}&amp;toUseability={toUseability}&amp;toEpochTime=${toEpochTime}</b>

	<p>Optional parameters: identifier, lifecycle, useability, toEpochTime

	<p>Notes :<br> 
	'epochtime' is only updated if a numeric value is passed. Eg contains the System.currentTimeMillis() or another integer<br>
	
	<p>Example: set all USED entries for an application to UNUSED 

	<p><a href='api/updatePoliciesUseState?application=testrest&useability=USED&toUseability=UNUSED'>
				api/updatePoliciesUseState?application=testrest&amp;useability=USED&amp;toUseability=UNUSED</a>
	</td>
 </tr>

 <tr>
	<td>/async_message_analyzer<br><br><br><b>Async&nbsp;Msg&nbsp;Analyzer</b></td>
	<td>
	<p>public DataHunterRestApiResponsePojo asyncMessageAnalyzer(String applicationStartsWithOrEquals, String application, String identifier, 
	String useability, String toUseability<br>
	__________________________________________________________________<br>	

	<p><b>.../api/asyncMessageAnalyzer?applicationStartsWithOrEquals={applicationStartsWithOrEquals}&amp;application={application}&amp;
		identifier={identifier}&amp;useability={useability}&amp;toUseability={toUseability}</b>

	<p>Optional parameters: identifier, useability, toUseability
	
	<p>Notes :<br> 
	If 'useability' is not selected, only rows with the same useability will be paired<br>
	If 'toUseability' is not selected, no update occurs<br>	
	The calculated difference is based on epochtime<br>	

	<p>Values for applicationStartsWithOrEquals: EQUALS | STARTS_WITH
	
	<p>Example: perform a similar match as in the Timing Asynchronous Processes example from the Mark59 User Guide, but also update 
	the matched rows to USED.  First add 4 rows (2 pairs), then execute the Async Msg Analyzer.
	
	<p><a href='api/addPolicy?application=testrest&identifier=EventA-id1&lifecycle=begin&useability=UNPAIRED&epochtime=222'>
				api/addPolicy?application=testrest&amp;identifier=EventA-id1&amp;lifecycle=begin&amp;useability=UNPAIRED&amp;epochtime=222</a> 
	
	<p><a href='api/addPolicy?application=testrest&identifier=EventA-id1&lifecycle=end&useability=UNPAIRED&epochtime=333'>
				api/addPolicy?application=testrest&amp;identifier=EventA-id1&amp;lifecycle=end&amp;useability=UNPAIRED&amp;epochtime=333</a> 

	<p><a href='api/addPolicy?application=testrest&identifier=EventA-id2&lifecycle=go&useability=UNPAIRED&epochtime=44'>
				api/addPolicy?application=testrest&amp;identifier=EventA-id2&amp;lifecycle=go&amp;useability=UNPAIRED&amp;epochtime=44</a>
				 
	<p><a href='api/addPolicy?application=testrest&identifier=EventA-id2&lifecycle=stop&useability=UNPAIRED&epochtime=66'>
				api/addPolicy?application=testrest&amp;identifier=EventA-id2&amp;lifecycle=stop&amp;useability=UNPAIRED&amp;epochtime=66</a>				
				
	<p><a href='api/asyncMessageAnalyzer?applicationStartsWithOrEquals=EQUALS&application=testrest&useability=UNPAIRED&toUseability=USED'>
				api/asyncMessageAnalyzer?applicationStartsWithOrEquals=EQUALS&amp;application=testrest&amp;
				useability=UNPAIRED&amp;toUseability=USED</a>
	</td>
 </tr>
</table>

<br>
<p>Version: 6.1  Please see our User Guide at <a href="https://mark59.com" target="_blank">mark59.com</a></p>  
</div>
</body>
</html>