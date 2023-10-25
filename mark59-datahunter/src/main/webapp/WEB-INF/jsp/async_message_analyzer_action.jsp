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
<!DOCTYPE html PUBLIC>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<html>
<head>
<title>Async Msg Analyzer</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>
<body>
<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />
<div class="content">

 <h1>Asynchronous Message Analyzer - Results</h1>  
 <span> 
 <b>Matching Criteria :</b> ${asyncMessageaAnalyzerRequest.applicationStartsWithOrEquals} "${asyncMessageaAnalyzerRequest.application}",
 	&nbsp; Id = "${asyncMessageaAnalyzerRequest.identifier}",&nbsp; Useability = "${asyncMessageaAnalyzerRequest.useability}" )
 </span>       
 <br><br>
 <table id=asyncMessageaAnalyzerTable class=metricsTable >
   <tr>
    <th>application</th>
    <th>identifier</th>
    <th>useability</th>    
    <th>start</th>
    <th>end</th>     
    <th>difference (msecs)</th>        
    <th style="display:none;"></th>
   </tr>
   <c:forEach var="asyncMessageaAnalyzerResult" items="${model.asyncMessageaAnalyzerResultList}">
    <tr>
     <td id=application>${asyncMessageaAnalyzerResult.application}</td>
     <td id=identifier>${asyncMessageaAnalyzerResult.identifier}</td>
     <td id=useability>${asyncMessageaAnalyzerResult.useability}</td>
     <td id=starttm>${asyncMessageaAnalyzerResult.starttm}</td>
     <td id=endtm>${asyncMessageaAnalyzerResult.endtm}</td>
     <td id=${asyncMessageaAnalyzerResult.application}_${asyncMessageaAnalyzerResultidentifier}_${asyncMessageaAnalyzerResult.useability}_differencetm>${asyncMessageaAnalyzerResult.differencetm}</td>
	 <td style="display:none;" id=differencetm>${asyncMessageaAnalyzerResult.differencetm}</td>
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
 
 <br><a href='async_message_analyzer?${model.navUrParms}'>Back</a>
</div>  
</body>
</html>
