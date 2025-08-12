REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  Execute DataHunterSeleniumTestPlan in non-gui mode 
REM   | 
REM   |  Sample Usage.
REM   |  ------------
REM   |  Assumes you have placed your target Jmeter instance is at C:\apache-jmeter,
REM   |                          you have deployed artifacts to the Jmeter instance (see DeployDataHunterTestArtifactsToJmeter.bat) 
REM   |                          Java 11+ is installed 
REM   |                          A local instance of DataHunter is running on (default) port 8081 
REM   | 
REM   |  -  open up a Dos command prompt and cd to the directory holding this bat file.
REM   |  -  to execute the DataHunter (Selenium) JMeter test, type : DataHunterExecuteJmeterSeleniumTest.bat   ( or just double-click on this file )   
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
@echo off
MODE con:cols=180 lines=500

rem SET "StartCdpListeners=true"
rem SET "StartCdpListeners=false"

ECHO StartCdpListeners has been set to %StartCdpListeners% 
ECHO ForceException has been set to %ForceException% 

mkdir C:\Mark59_Runs
mkdir C:\Mark59_Runs\Jmeter_Results
mkdir C:\Mark59_Runs\Jmeter_Results\DataHunter

set path=%path%;C:\Windows\System32;C:\windows\system32\wbem

C:\apache-jmeter\bin\jmeter -n -X -f -t ./test-plans/DataHunterSeleniumTestPlan.jmx -l C:\Mark59_Runs\Jmeter_Results\DataHunter\DataHunterTestResults.csv -j C:\apache-jmeter\bin\jmeter.log -JDataHunterUrl=http://localhost:8081/mark59-datahunter -JForceTxnFailPercent=0 -JStartCdpListeners=%StartCdpListeners% -JForceException=%ForceException%
PAUSE