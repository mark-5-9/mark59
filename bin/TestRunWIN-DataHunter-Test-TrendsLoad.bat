REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   | Load DataHunter Test Results to Mark59 Trends database.
REM   |
REM   |
REM   |  Alternative to running this .bat  ** H2 DATABASE ONLY **
REM   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-metrics" 
REM   |		 - run the DemoWIN-DataHunterTestTrendsLoad profile. 
REM   |
REM   |  JMeter input results file expected at C:\Mark59_Runs\Jmeter_Results\DataHunter 
REM   |
REM   |  Loaded run can be seen at http://localhost:8083/mark59-trends/trending?reqApp=DataHunter    (assuming default setup)
REM   |
REM   |  *** YOU NEED TO PASS OR SELECT WHICH DATABASE TO LOAD RESULTS TO (DEFAULT IS H2) ***
REM   |
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

echo  "YOU NEED TO PASS OR SELECT WHICH DATABASE TO LOAD RESULTS TO (DEFAULT IS H2) "; 

SET DATABASE=%1

rem SET "DATABASE=H2"
rem SET "DATABASE=MYSQL"
rem SET "DATABASE=POSTGRES"

rem -- special purpose values
rem SET "DATABASE=H2TCPCLIENT"
rem SET "DATABASE=H2MEM"

ECHO Starting the Trends Load Results program  
CD ../mark59-trends-load
START LoadDataHunterResultsIntoTrends.bat
