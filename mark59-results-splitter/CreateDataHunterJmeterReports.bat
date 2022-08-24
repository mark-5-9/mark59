REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  Splits JMeter Mark59-framework produced csv results files by data-type (if requested)  
REM   |                    - written to process the datahunter JMeter test results file    
REM   |                    - csv file(s) output to  C:\Mark59_Runs\Jmeter_Results\DataHunter\MERGED
REM   | 
REM   |  It then runs JMeter report generation on the resultant files (one report per data-type/file) 
REM   |                    - JMeter reports to      C:\Mark59_Runs\Jmeter_Reports\DataHunter
REM   | 
REM   |  To directly execute :
REM   |  ---------------------
REM   |  This bat assumes  - mark59-results-splitter.jar exists in the ./target directory (relative to this file) 
REM   |                    - you have JMeter test results in C:\Mark59_Runs\Jmeter_Results\DataHunter\ 
REM   |                    - a Jmeter instance exists at C:\apache-jmeter
REM   | 
REM   |  -  to generate reports, type:
REM   |  -  open up a Dos command prompt and cd to the directory holding this bat file.
REM   |     CreateDataHunterJmeterReports.bat                                                ( or just double-click on this file ) 
REM   |  
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
MODE con:cols=200 lines=100

C:
if exist C:\Mark59_Runs\Jmeter_Results\DataHunter\MERGED\ RD /S /Q C:\Mark59_Runs\Jmeter_Results\DataHunter\MERGED	  
mkdir C:\Mark59_Runs\Jmeter_Results\DataHunter\MERGED\   

java -jar ./target/mark59-results-splitter.jar -iC:\Mark59_Runs\Jmeter_Results\DataHunter -fDataHunterTestResults_converted.csv -mSplitByDataType -eNo -xTrue 

if exist C:\Mark59_Runs\Jmeter_Reports\DataHunter\ RD /Q /S C:\Mark59_Runs\Jmeter_Reports\DataHunter  

mkdir C:\Mark59_Runs\Jmeter_Reports\DataHunter\DataHunter
mkdir C:\Mark59_Runs\Jmeter_Reports\DataHunter\DataHunter_CPU_UTIL
mkdir C:\Mark59_Runs\Jmeter_Reports\DataHunter\DataHunter_DATAPOINT 
mkdir C:\Mark59_Runs\Jmeter_Reports\DataHunter\DataHunter_MEMORY
REM mkdir C:\Mark59_Runs\Jmeter_Reports\DataHunter\DataHunter_METRICS (only needed for combined metrics generation)

cd ./sampleJmeterReportsGeneration
cmd /c  report.bat
cmd /c  report_CPU_UTIL.bat
cmd /c  report_DATAPOINT.bat
cmd /c  report_MEMORY.bat
REM cmd /c  report_METRICS.bat

PAUSE