REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  This bat assumes - the metrics.war file exists in the ./target directory (relative to this file) 
REM   |                   - when using a mySQL database, the metricsdb database exists locally (using defaults)
REM   |
REM   |  Alternative to running this .bat (H2 database ONLY)
REM   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-server-metrics-web" 
REM   |		 - run the DemoWIN-DataHunter-Selenium-metricsRunCheck profile. 
REM   |
REM   |  Note the use of double quotes n a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank blank here). 
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
@echo off
MODE con:cols=180 lines=500
rem SET "DATABASE=MYSQL"


ECHO The database has been set to %DATABASE%

IF [%DATABASE%] == [] (
	ECHO 'DATABASE' variable not set, assuming H2 
	rem Using H2  Starting Metrics (Trend Analysis). default application server port 
	java -jar ./target/metrics.war --spring.profiles.active=h2 --port=8080 
)

IF "%DATABASE%" == "H2" (
	rem Using H2  Starting Metrics (Trend Analysis). default application server port 
	java -jar ./target/metrics.war --spring.profiles.active=h2 --port=8080 
)

IF "%DATABASE%" == "H2MEM" (
	rem Using H2 in memory  Starting Metrics (Trend Analysis). default application server port 
	java -jar ./target/metrics.war --spring.profiles.active=h2mem --port=8080 
)

IF "%DATABASE%" == "MYSQL" (
	rem Using MySQL. Starting the (Metrics Trend). Providing DB connection and server information (using default values)  
	java -jar ./target/metrics.war --spring.profiles.active=mysql --port=8080  --mysql.server=localhost --mysql.port=3306  --mysql.schema=metricsdb  --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin
)

IF "%DATABASE%"=="POSTGRES" (
	rem Using Postgres  Starting Metrics (Trend Analysis). Providing DB connection and server information (using postres default values) 
	java -jar ./target/metrics.war --spring.profiles.active=pg ---port=8080  --pg.server=localhost --pg.port=5432  --pg.database=metricsdb --pg.xtra.url.parms="?sslmode=disable" --pg.username=admin --pg.password=admin
)

PAUSE
