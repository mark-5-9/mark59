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
REM   |  Load DataHunter Test Results into the Mark59 Analysis MySql database
REM   | 
REM   |  Sample Usage.
REM   |  ------------
REM   |  Assumes you have placed your mark59 repo at C:\gitrepo\mark59,  
REM   |                          MySql database is setup locally (using defaults) 
REM   |                          results in C:\Jmeter_Results\DataHunter\, 
REM   |                          Java is on your %path% 
REM   |                          Maven build has created target/metricsRuncheck.jar 
REM   | 
REM   |  -  open up a Dos command prompt an cd to this projects root:
REM   | 
REM   |     C:
REM   |     cd C:\gitrepo\mark59\metricsRuncheck  
REM   | 
REM   |  -  to execute:
REM   | 
REM   |     LoadDataHunterTestResultsToMark59AnalysisDB.bat       
REM   |  
REM   -------------------------------------------------------------------------------------------------------------------------------------------------

MODE con:cols=180 lines=100

REM use the current time as the reference..

java -jar ./target/metricsRuncheck.jar -a DataHunter -i C:\Jmeter_Results\DataHunter -h localhost  -p 3306 -t JMETER   -r "%date% %time%"

PAUSE