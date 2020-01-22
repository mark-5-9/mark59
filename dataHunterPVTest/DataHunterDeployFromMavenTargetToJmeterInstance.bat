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
REM   |  Use this file to copy artifacts from  Eclipse build target after Maven builds have been executed on the dataHunterPVtest and mark59-server-metrics projects,
REM   |  into the target Jmeter instance  
REM   | 
REM   |  Sample Usage.
REM   |  ------------
REM   |  Assumes you have placed your mark59 repo at C:\gitrepo\mark59,  and your target Jmeter instance is at C:\apache-jmeter.:
REM   | 
REM   |  -  open up a Dos command prompt an cd to this projects root:
REM   | 
REM   |     C:
REM   |     cd C:\gitrepo\mark59\dataHunterPVTest  
REM   | 
REM   |  -  to execute type:
REM   | 
REM   |     DataHunterDeployFromMavenTargetToJmeterInstance.bat       
REM   |  
REM   -------------------------------------------------------------------------------------------------------------------------------------------------

MODE con:cols=180 lines=60

DEL C:\apache-jmeter\bin\mark59.properties
DEL C:\apache-jmeter\bin\chromedriver.exe
DEL C:\apache-jmeter\lib\ext\dataHunterPVTest.jar
DEL C:\apache-jmeter\lib\ext\mark59-server-metrics.jar

COPY .\mark59.properties C:\apache-jmeter\bin
COPY .\chromedriver.exe  C:\apache-jmeter\bin
COPY .\target\dataHunterPVTest.jar  C:\apache-jmeter\lib\ext
COPY ..\mark59-server-metrics\target\mark59-server-metrics.jar  C:\apache-jmeter\lib\ext


PAUSE