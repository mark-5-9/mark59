<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>

<%
String path = request.getServletPath().toLowerCase();
String reqApp = request.getParameter("reqApp");

String reqAppUrlParm = "";
if (reqApp != null && !reqApp.isEmpty()) { 
	reqAppUrlParm = "?reqApp=" + reqApp;
} 
%>

<div class="sidebar">
<div><a class=canterbury href="overview<%= reqAppUrlParm %>">Mark59</a></div>
<div class="canterburysmall">Trends</div> 

  <a <% if (path.contains("overview")){ %> class="active" <% } %> href="overview<%= reqAppUrlParm %>">Overview</a> 
  <a <% if (path.contains("dashboard")){ %> class="active" <% } %> href="dashboard?reqAppListSelector=Active">Application Dashboard</a> 
  <a href="trending<%= reqAppUrlParm %>">Trend Analysis</a>  
  <a <% if (path.contains("run")){ %> class="active" <% } %> href="runsList<%= reqAppUrlParm %>" >Run List</a> 
  <a <% if (path.contains("/slalist") || path.contains("copysla") || path.contains("registersla") || path.contains("editsla") || path.contains("deletesla") || path.contains("bulkapplication") || path.contains("applicationsla") )
                                       { %> class="active" <% } %> href="slaList<%= reqAppUrlParm %>">SLA Transactions</a> 
  <a <% if (path.contains("metricsla")){ %> class="active" <% } %> href="metricSlaList<%= reqAppUrlParm %>" >SLA Metrics</a>
  <a <% if (path.contains("transaction")){ %> class="active" <% } %> href="transactionList<%= reqAppUrlParm %>">Rename Transactions</a>
  <a <% if (path.contains("eventmapping")){ %> class="active" <% } %> href="eventMappingList">Event Mapping Admin</a>
  <a <% if (path.contains("graphmapping")){ %> class="active" <% } %> href="graphMappingList">Graph Mapping Admin</a>
 
  <c:if test="${currentDatabaseProfile == 'h2'}">  
      <a <% if (path.contains("h2-console")) { %> class="active" <% } %> href="h2-console">H2 Console URL: jdbc:h2:~/trends</a> 
  </c:if>
  <c:if test="${currentDatabaseProfile == 'h2mem'}">  
      <a <% if (path.contains("h2-console")) { %> class="active" <% } %> href="h2-console">H2 Console URL: jdbc:h2:mem:trendsmem</a> 
  </c:if>    
  
</div>