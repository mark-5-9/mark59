REM   --------------------------------------------------------------------------------------------------------------
REM   |  This bat starts the three Mark59 Web Applications:   DataHunter
REM   |                                                       Metrics (Trend Analysis)
REM   |                                                       Mark59 Server Metrics Web 
REM   |
REM   |  Using a 'H2 in memory' database.  This database is built/started automatically, so a database build is not needed  
REM   |   
REM   |  This "H2 In Memory Option" is primarily designed for internal testing, although may be useful for datahunter
REM   |  when the data does not need to persist between tests.
REM   |   
REM   |  Home page URLs  will be:    
REM   |  
REM   |     http://localhost:8081/dataHunter/
REM   |     http://localhost:8080/metrics/
REM   |     http://localhost:8085/mark59-server-metrics-web/
REM   |  
REM   --------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

SET "DATABASE=H2MEM"

ECHO Starting the DataHunter Spring Boot Application  
CD ../dataHunter
START StartDataHunterFromTarget.bat

ECHO Starting the Metrics (Trend Analysis) Web Application  
CD ../metrics
START StartMetricsTrendAnalysisFromTarget.bat

ECHO Starting the mark59-server-metrics-web Application  
CD ../mark59-server-metrics-web
START StartMark59ServerMetricsWebFromTarget.bat
