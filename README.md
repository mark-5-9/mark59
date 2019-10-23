# mark59

Mark59 is a Java-based framework that enables integration of Selenium scripts within the Jmeter Performance Test tool, also extending reporting capabilities.

## Early Release Notes

This initial release will present the DataHunter application.  This application is designed to handle data retention and re-use between and during a performance test. 

Samples of its use with Selenium within a performance test are planned for release shortly.    

## Terminologies

- DataHunter: The web-based application included in this git repository designed to handle performance test data.
- Selenium: Widely used Java-based browser automation tool.
- Jmeter: Leading Java-based performance test tool.
- Spring Boot: Java-based tooling which allows web-based applications to run without having to install a separate application server (such as Tomcat or WebSphere). 


## Key Concepts

- DataHunter can hold key-value pairs of data.
- Data can be held transiently (using a 'H2' database), or more permanently (using a MySQL database), depending on chosen implementation. 
- Both synchronous and asynchronous data life-cycles have been catered for.
- Page design has been kept minimal (no images or JavaScript on functional pages) as the application is designed for high volume usage in a performance test.
- DataHunter has been written as a Spring Boot application.  


## How it works

DataHunter is basically a set of simple data entry and usage screens.  As indicated above, the screens have deliberated kept minimal to maximise performance characteristics.

Here is a quick overview of DataHunter screen usage (labelled as per the main menu)

1. Home URL.

	The list of available pages.  This is the only page where images have been used (as this page is not expected to be hit during performance testing). 

2. add_policy.

	Key is Application, Identifier, Lifecycle.  We suggest generally 'Application' should identify a type of data, which can be keyed by 'Identifier'.  Lifecycle is an optional key element.
Useability can be REUSABLE (identifier can me selected multiple times), USED, UNUSED or  UNPAIRED (for asynchronous processing).
Other Data and Epoch Time can be optionally entered.

3. count_policies

	A unique count by Application, Lifecycle, Useability    

4. count_policies_breakdown_action

	A table of data pair counts (called 'policies' with the application). 

5. print_policy

	Displays data for a given policy

6. print_selected_policies

	Displays a list of polices matching the selection criteria.  Use this screen with caution ina performance test, as it can potentially bring back a large number of rows.   

7. delete_policy 

	Delete an Identifier for an Application

8. print_selected_policies

	Delete a set of polices matching the selection criteria.  NOTE with deletes no warnings or confirms are given.     

9. use_next_policy 

	Sets a policy to 'USED', as specified by the selection.   

10. lookup_next_policy 

	As per use_next_policy, but does not do an update (i.e., it lets you know what policy would be 'Used' next).   

11. update_policies_use_state

	Provides a mechanism to update a given Identifier (or an Application) with a chose Usage

12. async_message_analyzer

	Displays the state of asynchronous messages with the application. 

	- Early Release Note:  a detailed example of asynchronous message processing will be provided shorty.  

13. H2 Database Console 

	When using the H2 database, provides and SQL-style interface to the policy data. 


## Quickstart

1. The following assumes you will be building the application in the Eclipse IDE, and have downloaded the repo on your Windows machine to C:\gitrepo\mark59.  You'll need to compensate within these instructions if you have done otherwise.     

2. Building the dataHunter application.
    1. In Eclipse, Import Existing Maven Project, from C:\gitrepo\mark59.  dataHunter/pom.xml should be the pom you need to select.
    2. Before you do the Maven build, check your Maven settings.xml file is correct (only applies to a corporate site where a Maven 'proxy' such a Nexus may be in use).
	3. Maven build with goals: clean package 
	4. To start the application in 'default' mode, follow the steps outlined in the file DataHunterStartFromMavenTarget.bat  (on the project root)
	
