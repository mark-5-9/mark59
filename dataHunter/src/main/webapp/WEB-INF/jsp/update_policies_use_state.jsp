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
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>Update Policy Use State</title>
<link rel="shortcut icon"  href="favicon.png" />
<style>
  body { font-size: 20px; color: purple; font-family: Calibri; }
  table.metricsTable  { width: 100%; border-collapse: collapse; }
  table.metricsTable th { font-size: 18px; color: white;   background-color: purple; border: 1px solid #9344BB; padding: 3px 7px 2px 7px; text-align: left; }
  table.metricsTable td { font-size: 15px; color: #000000; background-color: white;  border: 1px solid #9344BB; padding: 3px 7px 2px 7px; }
</style>

</head>
<body>
 <center>
  
 <br><br><br>
 
 <b>Update Policy Use State and EpochTime Selection Criteria</b> 

 <br><br><br> 
 Change the Use State for the a policy identifier, or for Multiple policies in an Application (if an identifier not entered).
 <br>
 Epoch time can optionally be updated  
 <br> <br> <br> <br>
  <div>
   
   <form:form method="post" action="update_policies_use_state_action" modelAttribute="updateUseStateAndEpochTime">
    <table >
     <tr>
      <td>Application :</td>
      <td><form:input path="application"  size="64" height="20"  /></td>  <!-- can be set via @RequestParam -->
     </tr>
     <tr>
      <td>Identifier   :</td>
      <td><form:input path="identifier"  value="" size="64" height="20"  /></td>
     </tr>
     <tr>
      <td>From Useability   :</td>
      <td><form:select path="useability" items="${usabilityListFrom}"   /></td>
     </tr>
     
     <tr>
      <td>_____________________<br><br></td>
      <td></td>
     </tr>  
     
     <tr>
      <td>To Useability   :</td>
      <td><form:select path="toUseability" items="${usabilityListTo}"   /></td>
     </tr>
     <tr>
      <td>To Epoch Time (msecs) :</td>
      <td><form:input  path="toEpochTime" size="64" height="20"  /></td>
     </tr>  
            
     <tr>
      <td> </td>
      <td><br><br><input type="submit" value="submit"  id="submit" /></td>
     </tr>
     
    </table>
   </form:form>
  </div>
 </center>
 
 <br><br>
 <a href="/dataHunter?application=${updateUseStateAndEpochTime.application}">Home Page</a>
 
</body>
</html>
