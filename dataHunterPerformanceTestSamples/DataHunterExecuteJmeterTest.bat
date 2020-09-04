REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  Execute DataHunterSeleniumTestPlan in non-gui mode 
REM   | 
REM   |  Sample Usage.
REM   |  ------------
REM   |  Assumes you have placed your target Jmeter instance is at C:\apache-jmeter,
REM   |                          you have deployed artifacts to the Jmeter instance (see DeployDataHunterTestArtifactsToJmeter.bat) 
REM   |                          Java 8+ is installed 
REM   |                          A local instance of DataHunter is running on (default) port 8081 
REM   | 
REM   |  -  open up a Dos command prompt and cd to the directory holding this bat file.
REM   |  -  to execute the DataHunter (Selenium) JMeter test, type : DataHunterExecuteJmeterTest.bat   ( or just double-click on this file )   
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
@echo off
MODE con:cols=180 lines=500

 mkdir C:\Mark59_Runs
 mkdir C:\Mark59_Runs\Jmeter_Results
 mkdir C:\Mark59_Runs\Jmeter_Results\DataHunter

set path=%path%;C:\Windows\System32;C:\windows\system32\wbem

C:\apache-jmeter\bin\jmeter -n -X -f -t ./test-plans/DataHunterSeleniumTestPlan.jmx -l C:\Mark59_Runs\Jmeter_Results\DataHunter\DataHunterTestResults.csv -j C:\apache-jmeter\bin\jmeter.log  -JForceTxnFailPercent=0 -JDataHunterUrlHostPort=http://localhost:8081
PAUSE