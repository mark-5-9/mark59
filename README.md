## Mark59 Documention, Guides, Downloads and More ..

<p>Available at the https://mark59.com/ website

## Version 

<p>Central Repository versioning at 2.2<br>
All other projects at 2.2.0

<p>Significant changes for latest release 2.2(.0)
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


<p>All projects here at 2.2 or 2.2.0
