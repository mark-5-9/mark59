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
<title>Delete a Policy</title>
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
 
 <b>Delete Policy</b> 

  <br><br><br> 

  <div>
 
   <form:form method="post" action="delete_policy_action" modelAttribute="policySelectionCriteria">
    <table >
     <tr>
      <td width="71">Application</td>
      <td width="6">:</td>      
      <td width="301"><form:input path="application" /></td>  
     </tr>
     <tr>
      <td width="71">Identifier</td>
      <td width="6">:</td>      
      <td width="301"><form:input path="identifier" /></td>
     </tr>
     <tr>
      <td colspan="3"><br><br><input type="submit" value="submit"  id="submit"  /></td>
     </tr>
    </table>
   </form:form>
   
  </div>
 </center>
 
 <br><br>
 <a href="/dataHunter?application=${policySelectionCriteria.application}">Home Page</a>
 
</body>
</html>
