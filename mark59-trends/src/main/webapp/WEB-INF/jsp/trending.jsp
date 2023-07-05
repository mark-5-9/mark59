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
  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" 	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ page session="false" %>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="X-UA-Compatible" content="IE=9" />  
<title>3D Trend Analysis - Mark59</title>
<link rel="shortcut icon"  href="favicon.png" />
<script type="text/javascript" src="javascript/asyncTrending.js"></script>
<script type="text/javascript" src="visgraph3d/dist/vis.js"></script>
<script type="text/javascript" src="visgraph3d/visGraphLoad.js"></script>
<script type="text/javascript" src="visgraph3d/csv2array.js"></script>
<link type="text/css" rel="stylesheet" href="visgraph3d/visGraph.css" >
</head>	

<body onload="
	showHideElementIfCheckboxTicked('useRawRunSQL1','runTimeSelectionSQL'); 
 	showHideElementIfCheckboxTicked('useRawSQL1','transactionIdsSQL');
 	setElementReadonlyIfCheckboxTicked('manuallySelectRuns1','chosenRuns');
 	setElementReadonlyIfCheckboxTicked('manuallySelectTxns1','chosenTxns' );
 	buildHomePageLink();
 	buildTitleHomePageLink();
 	buildSlaDatabaseLink();
 	buildMetricSlaDatabaseLink();
 	buildRunsListLink(); 	 
 	trendingBuildPageLink();  
 	buildEventMappingLink(); 
 	buildGraphMappingLink();
	showHideElement('comparetab');
	load();">  

<form:form modelAttribute="trendingForm">

<table  id="graphSelectorTable">
	<tr>
	  <td style="white-space:nowrap"><a class=maroontitle id="titleHomePageLink" href="see_titleHomePageLink_JS">Trend Analysis</a></td>
  	  <td style="white-space:nowrap"><form:select path="appListSelector" items="${appListSelectors}"  class="smallborder" dir="rtl"  onChange="asyncPopulationOfApplicationList()" /></td>
	  <td style="white-space:nowrap; padding-top: 5px">applications:</td>
 	  <td style="white-space:nowrap"><form:select path="application" items="${applications}"  onChange="resetSelectionFields()" /></td>
 	  <td style="white-space:nowrap">graph:  <form:select path="graph" items="${graphs}" />	</td>
 	  <td style="white-space:nowrap; text-align:right">latest<br>runs</td>		 		
 	  <td style="white-space:nowrap">:</td>
 	  <td ><form:input path="maxRun"  maxlength="5" size="2"  onChange="clearChosenRuns('maxRun')" /></td>	   	  
 	  <td style="white-space:nowrap; text-align:right">latest<br>baselines</td>
 	  <td style="white-space:nowrap">:</td>
  	  <td style="white-space:nowrap"><form:input path="maxBaselineRun"   maxlength="5" size="2"  onChange="clearChosenRuns('maxBaselineRun')" /></td>	
 	  <td style="white-space:nowrap; text-align:right">only top<br>txn values</td>
 	  <td style="white-space:nowrap">:</td>
  	  <td style="white-space:nowrap"><form:input path="nthRankedTxn"   	maxlength="3" size="1" /></td>
	  <td style="white-space:nowrap" id=cdpDrowdown > 
	   		<table style="width:100%; border: 0">
	   		  <tr>
			 	<td class="novertpadding">show/hide<br>CDP txns</td> 	
				<td class="novertpadding">:</td>
				<td class="novertpadding"><form:select path="showCdpOption" items="${showCdpOptions}"/></td>
	          </tr>
	        </table>   
  	  </td>
 	  <td style="white-space:nowrap; text-align:right">display txn<br>names at point</td>
 	  <td style="white-space:nowrap"><form:checkbox path="displayPointText"  onclick="graph3d.redraw();" /></td>
  	  <td style="white-space:nowrap; text-align:right">range<br>bars</td>	  
 	  <td style="white-space:nowrap"><form:checkbox path="displayBarRanges"  onclick="graph3d.redraw();" /></td>
	  <td width="80%"></td>  
      <td> <input type="button" value="Draw"  onclick="trendingBuildPageLink();trendingLinkGetNewPage();" id="submitDrawLink" class="customButton" > </td>  
	  <td> <input type="button" value="Table" onclick="showHideElement('comparetab');draw();" id="showCompareBtb" class="customButton" ></td> 
	  <td> <input type="button" value="Reset" onclick="draw();" id="resetGraph" class="customButton" ></td>  
	  <td width="2%"></td>  
	</tr>
</table> 


<table style="width:100%;">
  <tr>
    <td>
    	<table style="width:100%;">
    		<tr>
    			<td width="100%">
				    <div id=graphdiv></div>
    			</td>
    			<td>
    				<div id=comparetab style="display:none; overflow-y: auto; height:882px;" ></div>    				
    			</td>
    		</tr>		      
    	</table>
    </td>
  </tr>

 <tr>
	<td><h3>Quick Links &nbsp; &nbsp; &nbsp; (links open in a new tab)</h3></td> 
 </tr>	
 <tr>
    <td>	
    <a id="homePageLink" 			href="see_buildHomePageLink_JS"       target="_blank">Home</a> &nbsp; &nbsp; &nbsp;    
    <a id="dashboardLink" 			href="dashboard?reqAppListSelector=Active" target="_blank">Application Dashboard </a> &nbsp; &nbsp; &nbsp;
    <a id="slaDatabaseLink" 		href="see_buildSlaDatabaseLink_JS"    target="_blank">Transactional SLAs</a> &nbsp; &nbsp; &nbsp;
    <a id="slaMetricDatabaseLink" 	href="see_buildMetricSlaDatabaseLink" target="_blank">Metric SLAs</a> &nbsp; &nbsp; &nbsp;
    <a id="runsListLink" 			href="see_buildRunsLIst_JS" 		  target="_blank">Runs List</a> &nbsp; &nbsp; &nbsp;
    <a id="eventMappingLink"  		href="see_buildEventMappingLink_JS"   target="_blank">Event Mapping Admin</a> &nbsp; &nbsp; &nbsp;
    <a id="graphMappingLink" 		href="see_buildGraphMappingLink_JS"   target="_blank">Graph Mapping Admin</a> &nbsp; &nbsp; &nbsp;
    <input type="button" value="+ Advanced Filters" onclick="showHideElement('AdvFilters');" id="showAdvFilters" class="customButton" style="float: right; margin-left: 5px;" > &nbsp; &nbsp; &nbsp;
    <input type="button" value="+ Debug"            onclick="showHideElement('debugData');"  id="showDebugData"  class="customButton" style="float: right;" > 
   </td>	
 </tr>


 <tr> 
	<td style="width:100%;">
	<div id="AdvFilters" style="display: none;">
	<table style="width:100%;">
	
	 <tr>
		<td><h3>Transaction Display Filters  &nbsp; &nbsp; &nbsp; -  &nbsp; &nbsp; &nbsp; there is minimal validation on the filters, use with care!</h3></td>  
	 <tr>

	 <tr>
	   <td> 
	   		<table style="width:100%; text-align:left; border: 0">
	   		  <tr>	          
				<td></td>
				<td colspan=4 style="font-size: 10px" >note: do NOT include the '(CDP)' tag in these 'SQL like' fields. The tag is informational and not part of the transaction name</td>
	          </tr>	          
	   		  <tr>
			 	<td width="4%" style="white-space: nowrap">select txn (SQL like) : </td> 	
				<td width="45%"><form:textarea path="sqlSelectLike" class="textarea" style="width:98%;" /></td>
				<td width="4%" style="white-space: nowrap">exclude txn ( NOT like ) : </td>
				<td width="45%"><form:textarea path="sqlSelectNotLike" class="textarea" style="width:98%;" /></td>
	 			<td width="2%"></td>  
	          </tr>
	        </table> 
	   </td>	
	 </tr> 

	 <tr>
	   <td> 
	   		<table style="width:100%; text-align:left; border: 0">
	   		  <tr>	          
				<td colspan=2></td>
	   			<td colspan=4 style="font-size: 10px" >note any '(CDP)' tags are information only and simply ignored for manual transaction selection</td> 
	          </tr>	          
	   		  <tr>
				<td width="8%"><div  style='vertical-align:centre;'>Manually Select Txns: </div></td>
	   	    	<td width="1%"><form:checkbox path="manuallySelectTxns"  onclick="setElementReadonlyIfCheckboxTicked('manuallySelectTxns1','chosenTxns' )"  /></td>  
	   			<td width="80%"><form:textarea  path="chosenTxns" class="textarea" style="width:97%;" /></td> 
	   			<td width="13%"><div style='vertical-align:centre;'>or use raw 'Txn' SQL [Experimental]:</div></td>
	 			<td width="1%"><form:checkbox path="useRawSQL"  onclick="showHideElementIfCheckboxTicked('useRawSQL1','transactionIdsSQL');"  /> </td>  
	 			<td width="1%"></td>  
	          </tr>
	        </table> 
	   </td>	
	 </tr> 

	 <tr>
	   <td>select transactions SQL:   
	   <form:textarea  path="transactionIdsSQL" class="textarea" style="width:95%;height:150px" /> 
	   </td>	
	 </tr>

	 <tr>
		<td><h3>Run Display Filters</h3></td> 
	 <tr>

	 <tr>
	   <td> 
	   		<table style="width:100%; text-align:left; border: 0">
	   		  <tr>	          
				<td></td>
				<td colspan=4 style="font-size: 10px" >note that no date formatting is required (only the numerics in the date are needed)</td>
	          </tr>	          
	   		  <tr>
			 	<td width="4%" style="white-space: nowrap">select run date-time (SQL like):</td> 	
				<td width="45%"><form:textarea path="sqlSelectRunLike" class="textarea" style="width:98%;" /></td>
				<td width="4%" style="white-space: nowrap">exclude run date-time ( NOT like ):</td>
				<td width="45%"><form:textarea path="sqlSelectRunNotLike" class="textarea" style="width:98%;" /></td>
	 			<td width="2%"></td>  
	          </tr>
	        </table> 
	   </td>	
	 </tr> 
	 
	 <tr>
	   <td>
	   		<table style="width:100%; text-align:left; border: 0">
	   		  <tr>
				<td width="8%"><div  style='vertical-align:centre;'>Manually Select Runs: </div></td>
	   	    	<td width="1%"><form:checkbox path="manuallySelectRuns"  onclick="setElementReadonlyIfCheckboxTicked('manuallySelectRuns1','chosenRuns' )"  /></td>  
	      
	   			<td width="80%"><form:textarea  path="chosenRuns" class="textarea" style="width:97%;" /></td> 
	   
	   			<td width="13%"><div style='vertical-align:centre;'>or use raw 'run' SQL [Experimental]:</div></td>
	 			<td width="1%"><form:checkbox path="useRawRunSQL" onclick="showHideElementIfCheckboxTicked('useRawRunSQL1','runTimeSelectionSQL')"  /></td>  
	 			<td width="1%"></td>  
	          </tr>
	        </table> 
	   </td>	
	 </tr>
	
	 <tr>
	   <td>select runs SQL:   
	   <form:textarea  path="runTimeSelectionSQL" class="textarea" style="width:95%;height:150px" /> 
	   </td>	
	 </tr>
	
	</table>
	</div>
	</td>
 </tr>


 <tr> 
   <td > 
   <div id="debugData" style="display: none;">
     <h3>Debug Data :  Axis and Transaction Data (display purposes only) </h3>
     
      <table>
         <tr> 
           <td width="15%"><b>Version: 5.4</b> </td> <td width="85%"></td> 	
         </tr> 

         <tr> 
           <td width="15%">Dates (X axis) </td> 
           <td width="85%"> 
           	<textarea id='runDatesToGraphId' style="width:100%;height:50px" >${runDatesToGraphId}</textarea> 
           </td> 	
         </tr> 
      
         <tr> 
           <td width="15%">Run Short Descriptions (for use in graphic)   </td> 
           <td width="85%"> 
	           	<textarea id='labelRunShortDescriptionsId'  style="width:100%;height:50px" >${labelRunShortDescriptionsId}</textarea> 	
           </td> 	
         </tr> 	       
        
         <tr> 
           <td width="15%">Run Descriptions (for use in Comparison Table)   </td> 
           <td width="85%"> 
	           	<textarea id='labelRunDescriptionsId'  style="width:100%;height:50px" >${labelRunDescriptionsId}</textarea> 	
           </td> 	
         </tr> 	       
     
         <tr> 
           <td width="15%">Transaction Id Keys (internal use only)</td> 
           <td width="85%"> 
           	<textarea id='txnsToGraphId' style="width:100%;height:50px" >${txnsToGraphId}</textarea> 
           </td> 	
         </tr> 	

         <tr> 
           <td width="15%">Failed Transaction Ids (any SLA related to txn type)</td> 
           <td width="85%"> 
           	<textarea id='trxnIdsWithAnyFailedSlaId' style="width:100%;height:50px" >${trxnIdsWithAnyFailedSlaId}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Failed Sla for 90th Percentile Transaction Ids</td> 
           <td width="85%"> 
           	<textarea id='trxnIdsWithFailedSla90thResponseId' style="width:100%;height:50px" >${trxnIdsWithFailedSla90thResponseId}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Failed Sla for 95th Percentile Transaction Ids</td> 
           <td width="85%"> 
           	<textarea id='trxnIdsWithFailedSla95thResponseId' style="width:100%;height:50px" >${trxnIdsWithFailedSla95thResponseId}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Failed Sla for 99th Percentile Transaction Ids</td> 
           <td width="85%"> 
           	<textarea id='trxnIdsWithFailedSla99thResponseId' style="width:100%;height:50px" >${trxnIdsWithFailedSla99thResponseId}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Failed Sla for Txn FAIL Percent Transaction Ids</td> 
           <td width="85%"> 
           	<textarea id='trxnIdsWithFailedSlaFailPercentId' style="width:100%;height:50px" >${trxnIdsWithFailedSlaFailPercentId}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Failed Sla FAIL Count Transaction Ids</td> 
           <td width="85%"> 
           	<textarea id='trxnIdsWithFailedSlaFailCount' style="width:100%;height:50px" >${trxnIdsWithFailedSlaFailCount}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Failed Sla PASS Count Transaction Ids</td> 
           <td width="85%"> 
           	<textarea id='trxnIdsWithFailedSlaPassCount' style="width:100%;height:50px" >${trxnIdsWithFailedSlaPassCount}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Transactions which have an SLA set but do not exist in the results</td> 
           <td width="85%"> 
           	<textarea id='missingTransactionsId' style="width:100%;height:50px" >${missingTransactionsId}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Transactions which have had the 'Ignore in Graph' flag set in the SLA Transaction table</td> 
           <td width="85%"> 
           	<textarea id='ignoredTransactionsId' style="width:100%;height:50px" >${ignoredTransactionsId}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Transactions which have had their SLAs set to disabled in the SLA Transaction table</td> 
           <td width="85%"> 
           	<textarea id='disabledSlasId' style="width:100%;height:50px" >${disabledSlasId}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Fails SLA Metrics for the Graphed Metric Measure (Metric Type + Field Derivation)</td> 
           <td width="85%"> 
           	<textarea id='trxnIdsWithFailedSlaForThisMetricMeasure' style="width:100%;height:50px" >${trxnIdsWithFailedSlaForThisMetricMeasure}</textarea> 
           </td> 	
         </tr> 	

	     <tr> 
           <td width="15%">Transaction Bar Range Array</td> 
           <td width="85%"> 
           	<textarea id='trxnIdsRangeBarId' style="width:100%;height:50px" >${trxnIdsRangeBarId}</textarea> 
           </td> 	
         </tr> 
        	
         <tr> 
           <td width="15%">csvTextarea (data used by vis graphics)</td> 
           <td width="85%"> 
            <div id="csv">
                <textarea id='csvTextarea' style="width:100%;height:50px" >${csvTextarea}</textarea> 
            </div>
           </td> 	
         </tr>

      </table> 
     
   </div>
   </td> 
 </tr>    


 <tr style="display: none;" >
    <td>
      <h2>Options</h2>

      <table>
        <tr><th>Option</th><th>Value</th> </tr>
        <tr><td>width</td><td><input type="text" id="width" value="100%" /><span class="info">for example "500px" or "100%"</span></td> </tr>
        <tr><td>height</td><td><input type="text" id="height" value="100%" /><span class="info">for example "500px" or "100%"</span></td> </tr>
        <tr> <td>style</td>
          <td>
          	<select id="style">
                        <option value="bar" >bar
                        <option value="bar-color" >bar-color
                        <option value="bar-size" >bar-size
                        <option value="dot" >dot
                        <option value="dot-color" >dot-color
                        <option value="dot-size" >dot-size
                        <option value="dot-line" >dot-line
                        <option value="line" >line
                        <option value="grid" >grid
                        <option value="surface" selected>surface
         	 </select>
        </tr>
        <tr><td>showAnimationControls</td>  <td><input type="checkbox" id="showAnimationControls" checked /></td>     </tr>
        <tr><td>showGrid</td>               <td><input type="checkbox" id="showGrid" checked /></td>        </tr>
        <tr><td>showXAxis</td>              <td><input type="checkbox" id="showXAxis" checked /></td>        </tr>
        <tr><td>showYAxis</td>              <td><input type="checkbox" id="showYAxis" checked /></td>        </tr>
        <tr><td>showZAxis</td>              <td><input type="checkbox" id="showZAxis" checked /></td>        </tr>
        <tr><td>showPerspective</td>        <td><input type="checkbox" id="showPerspective" checked /></td>        </tr>
        <tr><td>showLegend</td>             <td><input type="checkbox" id="showLegend" checked /></td>        </tr>
        <tr><td>showShadow</td>             <td><input type="checkbox" id="showShadow" /></td>        </tr>
        <tr><td>keepAspectRatio</td>        <td><input type="checkbox" id="keepAspectRatio" /></td>         </tr>
        <tr><td>verticalRatio</td>          <td><input type="text" id="verticalRatio" value="0.5" /> <span class="info">a value between 0.1 and 1.0</span></td>   </tr>
        <tr><td>animationInterval</td>      <td><input type="text" id="animationInterval" value="1000" /> <span class="info">in milliseconds</span></td>         </tr>
        <tr><td>animationPreload</td>       <td><input type="checkbox" id="animationPreload" /></td>         </tr>
        <tr><td>animationAutoStart</td>     <td><input type="checkbox" id="animationAutoStart" /></td>        </tr>
        <tr><td>xCenter</td>				<td><input type="text" id="xCenter" value="45%" /></td></tr>   
        <tr><td>yCenter</td>				<td><input type="text" id="yCenter" value="40%" /></td></tr>

        <tr><td>xMin</td>					<td><input type="text" id="xMin" /></td></tr>
        <tr><td>xMax</td>					<td><input type="text" id="xMax" /></td></tr>
        <tr><td>xStep</td>					<td><input type="text" id="xStep" value="1" /></td></tr>         

        <tr><td>yMin</td>					<td><input type="text" id="yMin" /></td></tr>
        <tr><td>yMax</td>					<td><input type="text" id="yMax" /></td></tr>
        <tr><td>yStep</td>					<td><input type="text" id="yStep" value="1" /></td></tr>         

        <tr><td>zMin</td>					<td><input type="text" id="zMin" /></td></tr>
        <tr><td>zMax</td>					<td><input type="text" id="zMax" /></td></tr>
        <tr><td>zStep</td>					<td><input type="text" id="zStep" /></td></tr>

        <tr><td>valueMin</td>				<td><input type="text" id="valueMin" /></td></tr>
        <tr><td>valueMax</td>				<td><input type="text" id="valueMax" /></td></tr>

         <tr><td>xBarWidth</td>				<td><input type="text" id="xBarWidth" /></td></tr>
         <tr><td>yBarWidth</td>				<td><input type="text" id="yBarWidth" /></td></tr>

         <tr><td>xLabel</td>				<td><input type="text" id="xLabel" value="x"/></td></tr>
         <tr><td>yLabel</td>				<td><input type="text" id="yLabel" value="y"/></td></tr>
         <tr><td>zLabel</td>				<td><input type="text" id="zLabel" value="z"/></td></tr>
         <tr><td>filterLabel</td>			<td><input type="text" id="filterLabel" value="time"/></td></tr>
         <tr><td>legendLabel</td>			<td><input type="text" id="legendLabel" value="value"/></td></tr>
      </table>
    </td>
 </tr>

</table>


<table>
    <tr style="display: none;" >
        <td style="white-space: nowrap">
            <input type="radio" name="datatype" id="datatypeCsv" onclick="selectDataType();" checked value="csv">Csv
        </td>
    </tr>
</table>

</form:form>

<table>
    <tr style="display: none;" ><td><a id="trendingLink" href="see_trendingBuildPageLink_JS" >Page URL</a>
   	<tr><td><input type="hidden" name="datatype" id="datatypeDatasource" onclick="selectDataType();"  value="datasource"></td></tr>
   	<tr><td><div id="datasource"><input type="hidden" id="datasourceText"></div></td></tr>
   	<tr><td><input type="hidden" id="showHistoricData" value="no" /></td></tr>
   	<tr><td><input type="hidden" id="barRangeLegendId" value="${barRangeLegendId}" /></td></tr>
   	<tr><td><input type="hidden" id="cdpTxnsCountId"   value="${cdpTxnsCount}" /></td></tr>
   	<tr><td><input type="hidden" id="txnTypedId" 	   value="${txnTypedId}" /></td></tr>
</table>
 	
</body>

</html>