
## Mark59 Documention, Guides, Downloads and More ..

<p>Available at the https://www.mark59.com website

## Releases 


<p>Release 5.3<br>

  <ul>  
	<li>Java 17 minimum version for the Web Applications and their APIs (excluding DataHunter API)</li>	
	<li>Option to add a Basic Authentication Header to the Metrics API call</li>
	<li>Selenium DSL - scrollIntoViewThenClick, scrollToCentreThenClick methods added </li>
	<li>Selenium DSL - demo New Tab handling and control </li>	
	<li>try/catch separated for browser close and quit (reduce potential of dead chromedriver processes</li>		
	<li>Selenium to 4.8.1 (chrome V110+), spring-boot to 3.0.2 </li>
	<li>sb 3.0.2 moved sfrom javax.servlet to jakarta.servlet - required multiple updates in web applications</li>
	<li>Selenium-Implementation - add --remote-allow-origins=* driver option (caters for Chrome 111)</li>		
	<li>Metrics - Removal of Spring Security dependency (using standard Java filter servlet)</li>	
	<li>DataHunter - Upload process simplified to standard Java (removal of commons and spring code)</li>		
	<li>Improve Metrics project JUnit testing, multiple small JavaDocs updates</li>	
  </ul>


<p>Release 5.2<br>

  <ul>  
	<li>DataHunter Rest - 'otherdata' field is now optional when adding an Item</li>	
	<li>Selenium to 4.5.0 (chrome v106+), spring-boot to 2.7.4</li>
	<li>Bug Fix: Spurious characters when using % sign in Trends Advanced selectors</li>
	<li>Bug Fix: Broken links in Trends when graph is empty</li>	
  </ul>


<p>Release 5.1<br>

  <ul>  
	<li>Metrics UI - Same-named Groovy parameters preserved on a Command change</li> 
	<li>Metrics UI - Limit on characters shown for a command on Command List change</li> 	
	<li>LOG_RESULTS_SUMMARY JMeter parameter added to metrics api (for consistency with other logging options)</li>
	<li>Metrics - default bypass of Kerberos for Nix (prevents JSch session stalling for an unconfigured implementations). 
	    See User Guide for details</li>
	<li>Metrics API was producing a 'no mark59.properties' warning if run in a JMeter test with no Mark59 Selenium implementations</li>
	<li>New mark59 property mark59.print.startup.console.messages.  'true' will output some basic config messages to console</li>
	<li>Metrics - Improved New Relic sample (now uses okhttp).  SQL 5.0 to 5.1 conversions included in Release</li>	
	<li>spring-boot to 2.7.3</li>
	<li>Bug Fix: Metrics discontinues Nix commands immediately on connection failure (was attempting to run a command after the failure)</li>
	<li>Bug Fix: An Encryption class in Metrics was not thread-safe (caused occasional dropout of Nix/Win connections during tests</li>
	<li>Bug Fix: Trends Load - invalid use of DUAL in PG database when loading Loadrunner mdb Session files</li>	
  </ul>

<p>Release 5.0<br>

  <ul>  
	<li>Change project, URL and database URL names.  This is intended to be once-off change :)
	  <ul>
	    <li>Web application Urls context path changes
	       	<ul>
				<li>/dataHunter                to /mark59-datahunter</li> 
				<li>/mark59-server-metrics-web to /mark59-metrics</li>
				<li>/metrics                   to /mark59-trends</li>     	
	    	</ul> 
		</li>
	    <li>Database Renames (review conversion files in the mark59 zip download databaseScripts folder) 
	    	<ul>
				<li>datahunterdb               to mark59datahunterdb</li> 
				<li>mark59servermetricswebdb   to mark59metricsdb</li>
				<li>metricsdb                  to mark59trendsdb</li>     	
	    	</ul> 
	    </li>
	    <li>mark59-metrics (ex mark59-server-metrics-web) properties renamed 
	    	<ul>
				<li>mark59servermetricswebuserid  to mark59metricsid</li> 
				<li>mark59servermetricswebpasswrd to mark59metricspasswrd</li>
				<li>mark59servermetricswebhide    to mark59metricshide</li>     	
	    	</ul> 
	    </li>
	  </ul>  
	</li>
	<li>New property mark59.log.directory renames mark59.screenshot.directory (flagged as redundant)</li>
	<li>New property mark59.logname.format : formatter for log names output to the log directory</li>
	<li>New property mark59.log.directory.suffix : log directory suffix (a local 'date' or 'datetime')</li>	
	<li>Mark59 logs types can be disabled on script exception</li>	
	<li>Filter for CDP markers on results splitter for Transactions Report</li>	
	<li>JMeter to 5.5, Selenium to 4.4.0 (chrome104+), H2 to 2.1.214, spring-boot to 2.7.2, groovy to 3.0.11</li>
	<li>Improved error handling and logging in the mark59-metrics application</li>
	<li>mark59metricsdb (ex mark59servermetricswebdb) database column name SCRIPT_NAME changed to PARSER_NAME 
		on tables COMMANDRESPONSEPARSERS and COMMANDPARSERLINKS<br>
		(review conversion files in the mark59 zip download databaseScripts folder for details)</li>
	<li>'SSH_LINIX_UNIX' changed to 'SSH_LINUX_UNIX' as a Command EXECUTOR option in mark59metricsdb</li>	
	<li>New script JMeter parameters PRINT_RESULTS_SUMMARY and LOG_RESULTS_SUMMARY flag if Transaction Results Summary 
		should be printed (to console)/logged during JMeter execution</li>	
	<li>New DataHunter API Samples: Direct API httprequest access in JMeter, and same logic repeated using the Java API Client:
		<ul>
			<li>DataHunterLifecyclePvtScriptUsingApiViaHttpRequestsTestPlan.jmx replaces DataHunterHttpTestPlan.jmx</li> 
			<li>New sample script DataHunterLifecyclePvtScriptUsingRestApiClient</li>
	    </ul> 
	</li>
	<li>DataHunter file upload changed from in-memory to streaming API, allowing for large file uploads</li>
	<li>UNHANDLED_PROMPT_BEHAVIOUR options added for selenium scripting</li>
	<li>Breaking Change : some internal class name changes may affect you if you have used a Groovy jsr223 as a Mark59
	 selenium script. <br>Please review DataHunterBasicSampleScriptJSR223Format in the mark59-datahunter-samples project </li>	
	<li>Improve Mac useability: 'Quick Start' Guide can be executed on Mac
  </ul>

<p>Release 4.2.1<br>
  <ul>
    <li>NOTE: this is an interim release to fix a bug, and updates the selenium 
    framework (for using chrome 102+)</li>		
    <li>Bug: Perflog was always empty, fixed</li>
    <li>Selenium to 4.2.1  (same name as release is a coincidence)</li>
  </ul>  
  
<p>Release 4.2<br>
  <ul>
    <li>New DataHunter REST Api project (minor datahuner DB changes made)</li>
    <li>Add Disabled flag for Transactional SLA (new column on metrics DB SLA table required)</li>
    <li>Option to ignore CDP transactions on SLA Bulk Update</li>		
    <li>Selenium to 4.1.3, SpringBoot to 2.6.6, H2 to 2.1.210</li>	  
    <li>Maven Plugins updated, compiler plugin expliticly declared <br>
		- align with spring boot versioning, reduce log4j vulerabilities on security scans (2.17.2) </li>	  
    <li>DataHunter Test Samples - Maven Dependency Plugin now excludes 'provided' dependencies 
    <li>Truncate txnIds greater than 128 chars when loading to Trend Analysis, rather than fail</li>	 	
    <li>Code cleanup (improve readability in IntelliJ)</li>	
    <li>Sample DSL: ElementNotVisibleException no longer available, replaced by
        ElementClickInterceptedException, ElementNotInteractableException,InvalidElementStateException</li>	
  </ul>  

<p>Release 4.1<br>
  <ul>
    <li>Fix for log4j exposure (JMeter to 5.4.3)</li>
    <li>Also SpringBoot to 2.6.2, H2 to 2.0.206</li>
    <li>Selenium to 4.1.1</li>
  </ul>  	

<p>Release 4.0.1<br>
  <ul>
    <li>Interim workaround for the log4j exposure (CVE-2021-44228 and CVE 2021-45046) <br>
	    - Details at the mark59.com website and in the User Guide (ch 4.)</li>	  
  </ul>  	

<p>Release 4.0.0<br>
  <ul>
    <li>Selenium dependency to 4.0.0</li>	  
    <li>New mark59.browser.executable property</li>	  
    <li>Legacy Server Metrics JMeter Java Requests removed</li>	
    <li>Metrics graphics available vars change to colon (:) prefix</li>	
    <li>Expanded script Main() option (csv file, summary, multiple iterations and threads)</li>		
    <li>JavaDocs and code scans cleanup</li>	
    <li>Version number to emphasize the relationship of this release to Selenium</li>
  </ul>  

<p>Release 4.0.0-rc-1<br>
  <ul>
    <li>DevTools DSL added (demonstates Selenium 4 capabilities)</li>
    <li>Dependency library is now required for Selenium projects (due to Selenium 4)</li>
    <li>Tidy up of demo (txns use underscores, historic data neatened, use of DevTools)</li>
    <li>Sla, Metric Sla, Events JSPs neatened</li>
    <li>New transactional datatype of 'CDP' (Chrome DevTool Protocol txns)</li>
    <li>New Dropdown In Trend Analysis to show/hide CDP txns</li>
    <li>Selenium's CdpVersionFinder to only print version warning message once</li>
    <li>Allow for SLA bulk copy of transaction percentiles</li>	
    <li>Generally improve range of start/end transaction methods</li>
    <li>Include new 'start DLS transaction' methods</li>	
    <li>Improve JavaDocs</li>	
    <li>Version number to emphasize the relationship of this release to Selenium</li>
  </ul>  

<p>Release 3.3.(0) <br>
  <ul>
    <li>Historical capture of Txn (Mocked) Delays and related Graphs</li>
    <li>Simplify sample DSL project</li>
    <li>More Set Transaction options</li>
    <li>SafeSleep moved to core</li>
    <li>Add ability to rename historical transactions in Trend Analysis</li>
    <li>Trend Analysis can now capture Gatlng results (using the simulation log)</li>
    <li>Capablity to ignore a test transaction failed status (Gatling and csv JMeter only)</li>	
    <li>Improve Links in Trend Analysis App</li>
    <li>Bug Fixes For : LoadRunner data load and Fail Count SLA</li>		
    <li>Median captured in Trend Analysis</li>	
    <li>Improve JavaDocs, and more JUnit tests</li>
  </ul>  
  
<p>Release 3.2.(0) <br>
  <ul>
    <li>Add Emulation Network Conditions (latency and byte rates)</li>
    <li>Improve validatoin for SLA data entry</li>
    <li>DataHunter sample includes a 'once only' run method (out-of-date driver warning)</li>	
    <li>A Run can now be 'ignored' on trending graphic</li>	
    <li>DataHunter 'policies' now named 'items' on pages</li>   
    <li>Trending - show table by default</li>
    <li>Include computation and graphics for 95 and 99th percentiles</li>	
    <li>Groovy scripting for mark59-metrics (required db changes)</li>	
    <li>Plugin dependenices updated to latest versions</li>	
	<li>Dependenices update (inc SpringBoot 2.4.5, JMeter 5.4.1)</li>	
    <li>Improve JavaDocs, fix typos etc</li>
  </ul>  

<p>Release to 3.1 <br>
  <ul>
    <li>minor release - please just use 3.2 </li>
  </ul>  

<p>Release to 3.0.(1) <br>
  <ul>
    <li>Default port for Trends Analysis changed from 8080 to 8083</li>
    <li>Sample projects updated, docker-compose files added to allow for the Jenkins Docker Sample </li>
  </ul>  

<p>Release 3.0.(.0) <br>
  <ul>
    <li>New project: mark59-metrics.   Significant upgrade of server metric capture in mark59.</li>
    <li>Project rename:  dataHunterPVTest to dataHunterPerformanceTestSamples</li>
    <li>Rename MySQL database pvmetrics to metricsdb (naming consistency across projects)</li>    
    <li>All project can use the h2, MySQL and Postgres database (enable quick start-up for demo and learning)</li>    
    <li>Align bat files to new download structure (a single zip file with all projects)</li>
    <li>Using OpenCSV for csv reads/writes (some edge case issues found using exists methods)</li>   
    <li>To JMeter_Java 5.3 , multiple dependency jar updates (confirmed working to chromedrver 85)</li>
    <li>Display mark59 build info on JMeter Java Request panels</li>	
    <li>Multiple small changes and code clean-up</li>	
  </ul>  

<p>Release 2.3.(0) 
  <ul>
    <li>Just a 'dry run' for Release 3.0
  </ul>

<p>Release 2.2.(0) <br>  
  <ul>
    <li>Transactions reported instead of being lost if Chrome crashes during a Selenium test.</li>
    <li>"Functional" DataHunter selenium test and related DSL removed from the dataHunterPVTest project, and placed in its own project on the mark59-extras repo.</li>
    <li>Basic sample script added to dataHunterPVTest (DataHunterBasicSampleScript).</li>
    <li>DataHunter and Metric war files can now be deployed to a Tomcat server (as well as executing as a SpringBoot app).</li>
    <li>add a"reference" table to datahunterdb creation ddl (just a convenience table - not referenced by DataHunter)</li> 
    <li>Many jar dependecies updated (including Spring and H2):  
	<br>As a consequence, if you have an existing H2 store, you may get an error like
	<br>" Unsupported type 17 .... Unable to read the page...". 
	<br>Delete the H2 store to fix (will be called test.mv.db, ususally within the User or current directory folder) </li>
  </ul>

<p>Release 2.1(.1)
  <ul>
    <li>Improve Event Selection Ordering<br>
    <li>Fix Transaction Type to a Metric Type Mapping
  </ul>
