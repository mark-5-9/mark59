<%@ page import="com.mark59.datahunter.application.DataHunterUtils"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>

<%
	String path = request.getServletPath().toLowerCase();
	String reqUrlParms = "";
	String reqApp = request.getParameter("application");
	
	String urlUseReqParmName = "?pUseOrLookup=";
	String urlUseReqParmValue = request.getParameter("pUseOrLookup");
	
	// System.out.println("path [" + path +  "]");
	// System.out.println(">>>> Home Controller request parameters");
	// java.util.Enumeration<String> params = request.getParameterNames(); 
	// while(params.hasMoreElements()){
	//  String paramName = params.nextElement();
	//  System.out.println("Parameter Name ["+paramName+"], Value ["+request.getParameter(paramName)+"]");
	// }
	// System.out.println("<<<< ");	

	if (!DataHunterUtils.isEmpty(reqApp)){
		reqUrlParms =  "?application=" + DataHunterUtils.encode(reqApp);
		String applicationStartsWithOrEquals  = request.getParameter("applicationStartsWithOrEquals");
		String identifier = request.getParameter("identifier");
		String lifecycle  = request.getParameter("lifecycle");
		String useability = request.getParameter("useability");
		if (!DataHunterUtils.isEmpty(applicationStartsWithOrEquals)){
			reqUrlParms += "&applicationStartsWithOrEquals=" + applicationStartsWithOrEquals;
		}
		if (!DataHunterUtils.isEmpty(identifier)){
			reqUrlParms += "&identifier=" + DataHunterUtils.encode(identifier);
		}
		if (!DataHunterUtils.isEmpty(lifecycle)){
			reqUrlParms += "&lifecycle=" + DataHunterUtils.encode(lifecycle);
		}
		if (!DataHunterUtils.isEmpty(useability)){
			reqUrlParms += "&useability=" + DataHunterUtils.encode(useability);
		}
		urlUseReqParmName = "&pUseOrLookup=";			
	}

%>

<div class="sidebar">
<div><a class=canterbury href="/mark59-metrics">Mark59</a></div>
<div class="canterburysmall">DataHunter</div> 

  <a <% if (path.contains("overview")){ %> class="active" <% } %> href="overview<%= reqUrlParms %>">Overview</a> 
  <a <% if (path.contains("menu")){ %> class="active" <% } %> href="menu<%= reqUrlParms %>">Main Menu</a> 
  <a <% if (path.contains("policies_breakdown")){ %> class="active" <% } %> href="policies_breakdown<%= reqUrlParms %>">Items Breakdown</a>
  <a <% if (path.contains("select_multiple_policies")){ %> class="active" <% } %> href="select_multiple_policies<%= reqUrlParms %>">Manage&nbsp;Multiple&nbsp;Items</a>
  <a <% if (path.contains("add_policy")){ %> class="active" <% } %> href="add_policy<%= reqUrlParms %>">Add Item</a>
  <a <% if (path.contains("count_policies")){ %> class="active" <% } %> href="count_policies<%= reqUrlParms %>">Count Items</a>
  <a <% if (path.contains("print_policy")){ %> class="active" <% } %> href="print_policy<%= reqUrlParms %>">Display Item</a>
  <a <% if (path.contains("delete_policy")){ %> class="active" <% } %> href="delete_policy<%= reqUrlParms %>">Delete Item</a>
  <a <% if (path.contains("next") && urlUseReqParmValue.contains("use"))   { %> class="active" <% } %> href="next_policy<%= reqUrlParms+urlUseReqParmName+"use" %>">Use Next Item</a>
  <a <% if (path.contains("next") && urlUseReqParmValue.contains("lookup")){ %> class="active" <% } %> href="next_policy<%= reqUrlParms+urlUseReqParmName+"lookup" %>">Lookup Next Item</a>
  <a <% if (path.contains("update_policy")){ %> class="active" <% } %> href="update_policy<%= reqUrlParms %>">Update Item</a>  
  <a <% if (path.contains("update_policies_use_state")){ %> class="active" <% } %> href="update_policies_use_state<%= reqUrlParms %>">Update Use States</a>
  <a <% if (path.contains("async_message_analyzer")){ %> class="active" <% } %> href="async_message_analyzer<%= reqUrlParms %>">Async Msg Analyzer</a>
  <a <% if (path.contains("upload_ids")){ %> class="active" <% } %> href="upload_ids<%= reqUrlParms %>">Upload Ids File</a>
  <a <% if (path.contains("upload_policie")){ %> class="active" <% } %> href="upload_policies<%= reqUrlParms %>">Upload Items File</a>
  
  <c:if test="${currentDatabaseProfile == 'h2'}">
      <a <% if (path.contains("h2-console")) { %> class="active" <% } %> href="h2-console">H2 Console URL: jdbc:h2:~/hunter</a>
  </c:if>       
  <c:if test="${currentDatabaseProfile == 'h2mem'}">
      <a <% if (path.contains("h2-console")) { %> class="active" <% } %> href="h2-console">H2 Console URL: jdbc:h2:mem:huntermem</a>
  </c:if>       
</div>
