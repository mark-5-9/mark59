REM   --------------------------------------------------------------------------------------------------------------
REM   |  This bat starts the three Mark59 Web Applications:   datahunter
REM   |                                                       trends
REM   |                                                       metrics 
REM   |
REM   |  using a 'H2' database.  This database is built/started automatically, so a database build is not needed  
REM   |   
REM   |  Assuming you are using (default) server.port values for the applications, home page URLs  will be:    
REM   |  
REM   |     http://localhost:8081/mark59-datahunter/
REM   |     http://localhost:8083/mark59-trends/
REM   |     http://localhost:8085/mark59-metrics/
REM   |  
REM   --------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

SET "DATABASE=H2"

ECHO Starting the DataHunter Spring Boot Application  
CD ../mark59-datahunter
START StartDataHunterFromTarget.bat

ECHO Starting the Metrics Application  
CD ../mark59-metrics
START StartMetricsFromTarget.bat

rem SET "DATABASE=H2TCPSERVER"
ECHO Starting the Trends Web Application  
CD ../mark59-trends
START StartTrendsFromTarget.bat
