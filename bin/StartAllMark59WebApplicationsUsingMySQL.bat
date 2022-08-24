REM   --------------------------------------------------------------------------------------------------------------
REM   |  This bat starts the three Mark59 Web Applications:   DataHunter
REM   |                                                       Trends Analysis
REM   |                                                       Mark59 Server Metrics Web 
REM   |
REM   |  using a 'MySQL' database.  The MySQL database build scripts must be run first.  
REM   |   
REM   |  Assuming you are using (default) server.port values for the applications, home page URLs  will be:    
REM   |  
REM   |     http://localhost:8081/mark59-datahunter/
REM   |     http://localhost:8083/mark59-trends/
REM   |     http://localhost:8085/mark59-metrics/
REM   |  
REM   --------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

SET "DATABASE=MYSQL"

ECHO Starting the DataHunter Spring Boot Application  
CD ../mark59-datahunter
START StartDataHunterFromTarget.bat

ECHO Starting the Trends Analysis Web Application  
CD ../mark59-trends
START StartTrendsFromTarget.bat

ECHO Starting the mark59-metrics Application  
CD ../mark59-metrics
START StartMetricsFromTarget.bat
