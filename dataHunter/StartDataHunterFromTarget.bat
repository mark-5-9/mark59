REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  This bat assumes - the dataHunter.war file exists in the ./target directory (relative to this file) 
REM   |                   - when using a MySQL or Postgres database, the datahunterdb database exists locally (using defaults)
REM   |
REM   |  Note the use of double quotes n a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank blank here). 
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
@echo off
MODE con:cols=180 lines=500
rem SET "DATABASE=MYSQL"

ECHO The database has been set to %DATABASE%

IF [%DATABASE%] == [] (
	ECHO 'DATABASE' variable not set, assuming H2 
	rem Using H2 Database.  Starting the DataHunter Web Application (default values) 
	java -jar ./target/dataHunter.war  --spring.profiles.active=h2 --port=8081
)

IF "%DATABASE%" == "H2" (
	rem Using H2 Database.  Starting the DataHunter Web Application (default values) 
	java -jar ./target/dataHunter.war  --spring.profiles.active=h2 --port=8081
)

IF "%DATABASE%" == "H2MEM" (
	rem Using H2 Database 'IN MEMORY' option.  Starting the DataHunter Web Application (default values) 
	java -jar ./target/dataHunter.war  --spring.profiles.active=h2mem --port=8081
)

IF "%DATABASE%" == "MYSQL" (
	rem Using MySQL. Starting DataHunter providing DB connection and server information (using default values) 
	java -jar ./target/dataHunter.war  --spring.profiles.active=mysql --port=8081 --mysql.server=localhost --mysql.port=3306 --mysql.schema=datahunterdb --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin
)

rem -- another MySQL example -- 
rem Using MySQL  Starting DataHunter defaults  (will default to MySQL database, application URL port of 8081) 
rem java -jar ./target/dataHunter.war 

IF "%DATABASE%"=="POSTGRES" (
	rem Using Postgres.  Starting DataHunter providing DB connection and server information (using postgres default values) 
	java -jar ./target/dataHunter.war --spring.profiles.active=pg ---port=8081  --pg.server=localhost --pg.port=5432  --pg.database=datahunterdb --pg.xtra.url.parms=" " --pg.username=admin --pg.password=admin
)

PAUSE