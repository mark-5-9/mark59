REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   | Load DataHunter Test Results to Mark59 Metrics (Trend Analysis) database.
REM   |
REM   |
REM   |  Alternative to running this .bat  ** H2 DATABASE ONLY **
REM   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-server-metrics-web" 
REM   |		 - run the DemoWIN-DataHunterSeleniumRunCheck profile. 
REM   |
REM   |  JMeter input results file expected at C:\Mark59_Runs\Jmeter_Results\DataHunter 
REM   |
REM   |  Loaded run can be seen at http://localhost:8083/metrics/trending?reqApp=DataHunter    (assuming default setup)
REM   |
REM   |  *** YOU NEED TO SELECT WHICH DATABASE TO LOAD RESULTS TO BEFORE EXECUTION ***
REM   |
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

echo  "YOU NEED TO SELECT WHICH DATABASE TO LOAD RESULTS TO BEFORE EXECUTION (DEFAULT IS H2) "; 

SET "DATABASE=H2"
rem SET "DATABASE=MYSQL"
rem SET "DATABASE=POSTGRES"

rem -- special purpose values
rem SET "DATABASE=H2TCPCLIENT"
rem SET "DATABASE=H2MEM"

ECHO Starting the Metrics (Trend Analysis) runCheck Load Results program  
CD ../metricsRuncheck
START LoadDataHunterResultsIntoMetricsTrendAnalysis.bat
