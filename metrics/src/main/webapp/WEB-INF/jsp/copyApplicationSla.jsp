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
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<!DOCTYPE html>
<html>
<head>
<title>Copy SLAs by Application</title>
<link rel="shortcut icon"  href="favicon.png" />
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/style.css" rel="stylesheet" type="text/css" />
<style>@font-face { font-family: "Canterbury";  src: url("fonts/Canterbury.ttf"); }</style>
</head>

<body>

<%-- Include navigation element --%>
<jsp:include page="include/navigation.jsp" />

<div class="content"> 
 
 <h1>Copy SLAs by Application</h1>

<p>&nbsp;</p>
  
  <div>
   <form:form method="post" action="copyApplicationSla?reqApp=${copySlaApplicationForm.reqApp}"  modelAttribute="copySlaApplicationForm">
    <table>
     <tr>
      <td>Copy all rows from :</td><td><b> ${copySlaApplicationForm.reqApp}</b> </td>
     </tr>
     
     <tr>
      <td>&nbsp;</td>
      <td></td>
     </tr>      
     
     <tr>
      <td>To application Id</td>
      <td><form:input path="reqToApp" value="${copySlaApplicationForm.reqToApp}"  size="100" />
      </td>
     </tr> 
 
      <tr>
      <td>&nbsp;</td>
      <td></td>
     </tr>   
  
      
     <tr>
      <td> </td>
      <td><input type="submit" value="Copy All" />
      </td>
     </tr>
     
     <tr>
      
      <td colspan="2"><a href="slaList?reqApp=${copySlaApplicationForm.reqApp}">Cancel</a></td>
     </tr>     
     
    </table>
   </form:form>
  </div>

</div>

</body>
</html>