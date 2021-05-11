## Mark59 Documention, Guides, Downloads and More ..

<p>Available at the https://www.mark59.com website

## Releases 

<p>Release 3.2.(.0) <br>
  This was a major release 
  <ul>
    <li>Add Emulation Network Conditions (latency and byte rates)</li>
    <li>Improve validatoin for SLA data entry</li>
    <li>DataHunter sample includes a 'once only' run method (out-of-date driver warning)</li>	
    <li>A Run can now be 'ignored' on trending graphic</li>	
    <li>DataHunter 'policies' now named 'items' on pages</li>   
    <li>Trending - show table by default</li>
    <li>Include computation and graphics for 95 and 99th percentiles</li>	
    <li>Groovy scripting for mark59-server-metrics-web (required db changes)</li>	
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
    <li>Default port for metrics (Trend Analysis) changed from 8080 to 8083</li>
    <li>Sample projects updated, docker-compose files added to allow for the Jenkins Docker Sample </li>
  </ul>  


<p>Release 3.0.(.0) <br>
  This was a major release 
  <ul>
    <li>New project: mark59-server-metrics-web.   Significant upgrade of server metric capture in mark59.</li>
    <li>Project rename:  dataHunterPVTest to dataHunterPerformanceTestSamples</li>
    <li>Rename MySQL database pvmetrics to metricsdb (naming consistency across projects)</li>    
    <li>All project can use the h2, MySQL and Postgres database (enable quick start-up for demo and learning)</li>    
    <li>Align bat files to new download structure (a single zip file with all projects)</li>
    <li>Using OpenCSV for csv reads/writes (some edge case issues found using exists methods)</li>   
    <li>To JMeter_Java 5.3 , multiple dependency jar updates (confirmed working to chromedrver 85)</li>
    <li>Display mark59 build info on JMeter Java Request panels</li>	
    <li>Multiple small changes and code clean-up</li>	
  </ul>  

<p>Release 2.3 - Was just a 'dry run' for 3.0 Release 3.0  
  
<p>Significant changes release 2.2(.0)
  <ul>
    <li>Transactions reported instead of being lost if Chrome crashes during a Selenium test.</li>
    <li>"Functional" DataHunter selenium test and related DSL removed from the dataHunterPVTest project, and placed in its own project on the mark59-extras repo.</li>
    <li>Basic sample script added to dataHunterPVTest (DataHunterBasicSampleScript).</li>
    <li>Many jar dependecies updated (including Spring and H2) ** </li> 
    <li>DataHunter and Metric war files can now be deployed to a Tomcat server (as well as executing as a SpringBoot app).</li>
    <li>add a"reference" table to datahunterdb creation ddl (just a convenience table - not referenced by DataHunter)</li> 
  </ul>

<p>** as a consequence of the H2 update, if you have an existing H2 store, you may get an error like " Unsupported type 17 .... Unable to read the page...".  Delete the H2 store to fix (will be called test.mv.db, ususally within the User or current directory folder) 


<p>Previous Release 2.1(.1)
  <ul>
    <li>Improve Event Selection Ordering<br>
    <li>Fix Transaction Type to a Metric Type Mapping
  </ul>
