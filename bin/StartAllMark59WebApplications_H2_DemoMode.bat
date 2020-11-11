REM   --------------------------------------------------------------------------------------------------------------
REM   |  This bat starts the three Mark59 Web Applications:   DataHunter
REM   |                                                       Metrics (Trend Analysis)
REM   |                                                       Mark59 Server Metrics Web 
REM   |
REM   |  using a 'H2' database.  This database is built/started automatically, so a database build is not needed  
REM   |   
REM   |  Assuming you are using (default) server.port values for the applications, home page URLs  will be:    
REM   |  
REM   |     http://localhost:8081/dataHunter/
REM   |     http://localhost:8083/metrics/
REM   |     http://localhost:8085/mark59-server-metrics-web/
REM   |  
REM   --------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

SET "DATABASE=H2"

ECHO Starting the DataHunter Spring Boot Application  
CD ../dataHunter
START StartDataHunterFromTarget.bat

ECHO Starting the mark59-server-metrics-web Application  
CD ../mark59-server-metrics-web
START StartMark59ServerMetricsWebFromTarget.bat

rem SET "DATABASE=H2TCPSERVER"
ECHO Starting the Metrics (Trend Analysis) Web Application  
CD ../metrics
START StartMetricsTrendAnalysisFromTarget.bat
