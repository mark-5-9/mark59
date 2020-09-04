REM   --------------------------------------------------------------------------------------------------------------
REM   |  This bat starts the three Mark59 Web Applications:   DataHunter
REM   |                                                       Metrics (Trend Analysis)
REM   |                                                       Mark59 Server Metrics Web 
REM   |
REM   |  using a 'MySQL' database.  The MySQL database build scripts must be run first.  
REM   |   
REM   |  Assuming you are using (default) server.port values for the applications, home page URLs  will be:    
REM   |  
REM   |     http://localhost:8081/dataHunter/
REM   |     http://localhost:8080/metrics/
REM   |     http://localhost:8085/mark59-server-metrics-web/
REM   |  
REM   --------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

SET "DATABASE=MYSQL"

ECHO Starting the DataHunter Spring Boot Application  
CD ../dataHunter
START StartDataHunterFromTarget.bat

ECHO Starting the Metrics (Trend Analysis) Web Application  
CD ../metrics
START StartMetricsTrendAnalysisFromTarget.bat

ECHO Starting the mark59-server-metrics-web Application  
CD ../mark59-server-metrics-web
START StartMark59ServerMetricsWebFromTarget.bat
