<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>

<%
String path = request.getServletPath().toLowerCase();
%>

<div class="sidebar">
<div class="canterbury">Mark59</div> 
<div class="canterburysmall">Server Metrics via Web</div> 

  <a <% if (path.contains("welcome")){ %> class="active" <% } %> href="/mark59-metrics">Overview</a> 
  <a <% if (path.contains("server")){ %> class="active" <% } %> href="serverProfileList">Server Profiles</a>
  <a <% if (path.contains("commandlist")){ %> class="active" <% } %> href="commandList">Commands</a>
  <a <% if (path.contains("parserlist")){ %> class="active" <% } %> href="commandResponseParserList">Response Parsers</a>
  
  <c:if test="${currentDatabaseProfile == 'h2'}">
      <a <% if (path.contains("h2-console")) { %> class="active" <% } %> href="h2-console">H2 Console URL: jdbc:h2:~/metrics</a>
  </c:if>       
 <c:if test="${currentDatabaseProfile == 'h2mem'}">
      <a <% if (path.contains("h2-console")) { %> class="active" <% } %> href="h2-console">H2 Console URL: jdbc:h2:mem:metricsmem</a>
  </c:if>       
  
  <a <% if (path.contains("logout")) { %> class="active" <% } %> href="logout">Logout</a>  
</div>