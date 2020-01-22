REM   Copyright 2019 Insurance Australia Group Limited
REM 
REM   Licensed under the Apache License, Version 2.0 (the "License");
REM   you may not use this file except in compliance with the License.
REM   You may obtain a copy of the License at
REM 
REM     http://www.apache.org/licenses/LICENSE-2.0
REM  
REM   Unless required by applicable law or agreed to in writing, software
REM   distributed under the License is distributed on an "AS IS" BASIS,
REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM   See the License for the specific language governing permissions and
REM   limitations under the License.
REM 
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  Execute DataHunterSeleniumApiTestPlan in non-gui mode 
REM   | 
REM   |  Sample Usage.
REM   |  ------------
REM   |  Assumes you have placed your mark59 repo at C:\gitrepo\mark59,  
REM   |                          your target Jmeter instance is at C:\apache-jmeter,
REM   |                          you have deployed artifacts to the Jmeter instance (see DataHunterDeployFromMavenTargetToJmeterInstance.bat) 
REM   |                          Java is on your %path% 
REM   | 
REM   |  -  open up a Dos command prompt an cd to this projects root:
REM   | 
REM   |     C:
REM   |     cd C:\gitrepo\mark59\dataHunterPVTest  
REM   | 
REM   |  -  to execute the Jmeter test:
REM   | 
REM   |     DataHunterDeployFromMavenTargetToJmeterInstance.bat       
REM   |  
REM   -------------------------------------------------------------------------------------------------------------------------------------------------

MODE con:cols=180 lines=60

C:
if exist C:\Jmeter_Results\DataHunter\ RD /Q /S C:\Jmeter_Results\DataHunter  
mkdir C:\Jmeter_Results\DataHunter

set path=%path%;C:\Windows\System32;C:\windows\system32\wbem

cd C:\apache-jmeter\bin  

jmeter -n -X -f -t C:\gitrepo\mark59\test-plans\DataHunterSeleniumApiTestPlan.jmx  -l C:\Jmeter_Results\DataHunter\  -JForceTxnFailPercent=0 -JDataHunterUrlHostPort=http://localhost:8081

PAUSE