REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  This bat assumes - the mark59-trends.war file exists in the ./target directory (relative to this file) 
REM   |                   - when using a mySQL database, the mark59trendsdb database exists locally (using defaults)
REM   |
REM   |  Alternative to running this .bat (H2 database ONLY)
REM   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-trends" 
REM   |		 - run the DemoWIN-DataHunterSeleniumTrendsLoad profile. 
REM   |
REM   |  Note the use of double quotes n a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank blank here). 
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
@echo off
MODE con:cols=180 lines=500
rem SET "DATABASE=MYSQL"


ECHO The database has been set to %DATABASE%

IF [%DATABASE%] == [] (
	ECHO 'DATABASE' variable not set, assuming H2 
	rem Using H2  Starting Trend Analysis. default application server port 
	java -Djdk.util.jar.enableMultiRelease=false -jar ./target/mark59-trends.war --spring.profiles.active=h2 --port=8083 
)

IF "%DATABASE%" == "H2" (
	rem Using H2  Starting Trend Analysis. default application server port 
	java -Djdk.util.jar.enableMultiRelease=false -jar ./target/mark59-trends.war --spring.profiles.active=h2 --port=8083 
)

IF "%DATABASE%" == "H2TCPSERVER" (
	rem Using H2  Starting Trend Analysis.  (default application server port, db TCP server started on default port) 
	java -jar -Djdk.util.jar.enableMultiRelease=false ./target/mark59-trends.war --spring.profiles.active=h2tcpserver --port=8083 --h2.port=9092 
)

IF "%DATABASE%" == "H2MEM" (
	rem Using H2 in memory  Starting Trend Analysis. default application server port 
	java -jar -Djdk.util.jar.enableMultiRelease=false ./target/mark59-trends.war --spring.profiles.active=h2mem --port=8083 
)

IF "%DATABASE%" == "MYSQL" (
	rem Using MySQL.  Starting Trend Analysis. Providing DB connection and server information (using default values)  
	java -jar -Djdk.util.jar.enableMultiRelease=false ./target/mark59-trends.war --spring.profiles.active=mysql --port=8083  --mysql.server=localhost --mysql.port=3306  --mysql.schema=mark59trendsdb  --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin
)

IF "%DATABASE%"=="POSTGRES" (
	rem Using Postgres   Starting Trend Analysis. Providing DB connection and server information (using postres default values) 
	java -jar -Djdk.util.jar.enableMultiRelease=false ./target/mark59-trends.war --spring.profiles.active=pg ---port=8083  --pg.server=localhost --pg.port=5432  --pg.database=mark59trendsdb --pg.xtra.url.parms="?sslmode=disable" --pg.username=admin --pg.password=admin
)

PAUSE
