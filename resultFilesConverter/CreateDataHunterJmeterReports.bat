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
REM   |  Load DataHunter Test Results into the Mark59 Analysis MySql database - output to  C:\Jmeter_Reports\DataHunter
REM   | 
REM   |  Sample Usage.
REM   |  ------------
REM   |  Assumes you have placed your mark59 repo at C:\gitrepo\mark59,  
REM   |                          jmeter results (the input for this job)  in C:\Jmeter_Results\DataHunter\ 
REM   |                          Jmeter at C:\apache-jmeter
REM   |                          Java is on your %path% 
REM   |                          Maven build has created target/resultFilesConverter.jar 
REM   | 
REM   |  -  open up a Dos command prompt an cd to this projects root:
REM   | 
REM   |     C:
REM   |     cd C:\gitrepo\mark59\resultFilesConverter  
REM   | 
REM   |  -  to generate reports:
REM   | 
REM   |     CreateDataHunterJmeterReports.bat       
REM   |  
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
MODE con:cols=200 lines=100

C:
if exist C:\Jmeter_Results\DataHunter\MERGED\ RD /S /Q C:\Jmeter_Results\DataHunter\MERGED	  
mkdir C:\Jmeter_Results\DataHunter\MERGED\   

java -jar ./target/resultFilesConverter.jar -iC:\Jmeter_Results\DataHunter -fDataHunterTestResults_converted.csv -mSplitByDataType -eNo -xTrue 

if exist C:\Jmeter_Reports\DataHunter\ RD /Q /S C:\Jmeter_Reports\DataHunter  

mkdir C:\Jmeter_Reports\DataHunter\DataHunter
mkdir C:\Jmeter_Reports\DataHunter\DataHunter_CPU_UTIL
mkdir C:\Jmeter_Reports\DataHunter\DataHunter_DATAPOINT 
mkdir C:\Jmeter_Reports\DataHunter\DataHunter_MEMORY
REM mkdir C:\Jmeter_Reports\DataHunter\DataHunter_METRICS (only needed for combined metrics generation)

cd ./sampleJmeterReportsGeneration
cmd /c  report.bat
cmd /c  report_CPU_UTIL.bat
cmd /c  report_DATAPOINT.bat
cmd /c  report_MEMORY.bat
REM cmd /c  report_METRICS.bat

PAUSE