REM   --------------------------------------------------------------------------------------------------------------
REM   |  This bat starts the three Mark59 Web Applications:   DataHunter
REM   |                                                       Trends Analysis
REM   |                                                       Mark59 Server Metrics Web 
REM   |
REM   |  Using a 'H2 in memory' database.  This database is built/started automatically, so a database build is not needed  
REM   |   
REM   |  This "H2 In Memory Option" is primarily designed for internal testing, although may be useful for datahunter
REM   |  when the data does not need to persist between tests.
REM   |   
REM   |  Home page URLs  will be:    
REM   |  
REM   |     http://localhost:8081/mark59-datahunter/
REM   |     http://localhost:8083/mark59-trends/
REM   |     http://localhost:8085/mark59-metrics/
REM   |  
REM   --------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

SET "DATABASE=H2MEM"

ECHO Starting the DataHunter Spring Boot Application  
CD ../mark59-datahunter
START StartDataHunterFromTarget.bat

ECHO Starting the Trends Analysis Web Application  
CD ../mark59-trends
START StartTrendsFromTarget.bat

ECHO Starting the mark59-metrics Application  
CD ../mark59-metrics
START StartMetricsFromTarget.bat
