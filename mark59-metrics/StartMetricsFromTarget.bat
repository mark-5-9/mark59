REM   ---------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  This bat assumes - the mark59-metrics.war file exists in the ./target directory (relative to this file) 
REM   |		              - when using a MySQL or Postgress database 'mark59metricsdb' database exists locally (using defaults)  
REM   |
REM   |  Note the use of double quotes in a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank blank here). 
REM   |
REM   |  Setting a 'logging.level.root' Environment Variable to 'ERROR' will prevent application warning messages appearing (command and parser failures).
REM   |  Setting a 'logging.level.com.mark59.' Environment Variable to 'DEBUG' will ouput debug level application  messages.
REM   |
REM   ---------------------------------------------------------------------------------------------------------------------------------------------------
@echo off
MODE con:cols=180 lines=500
rem SET "DATABASE=MYSQL"

SET "logging.level.root=INFO"
rem SET "logging.level.root=ERROR"
rem SET "logging.level.com.mark59.=DEBUG"

ECHO The database has been set to %DATABASE%
ECHO 'logging.level.root' set to %logging.level.root%
ECHO 'logging.level.com.mark59.' set to %logging.level.com.mark59.%

IF [%DATABASE%] == [] (
	ECHO 'DATABASE' variable not set, assuming H2 
	rem Using H2  Starting mark59-metrics  (default application server port) 
	java -Djdk.util.jar.enableMultiRelease=false -jar ./target/mark59-metrics.war --spring.profiles.active=h2 --port=8085
)

IF "%DATABASE%" == "H2" (
	rem Using H2  Starting mark59-metrics  (default application server port) 
	java -Djdk.util.jar.enableMultiRelease=false -jar ./target/mark59-metrics.war --spring.profiles.active=h2 --port=8085 
)


rem -- This is how to start a H2 instance with Basic Authentication switched on for Metrics API calls --  
rem -- Review MetricsUtilsTest unit test case in mark59-metrics project for an example of Basic Authentication token creation.  --  
rem
rem IF "%DATABASE%" == "H2" (
rem 	ECHO Basic Authentication has been activated !! 
rem 	java -Djdk.util.jar.enableMultiRelease=false -jar ./target/mark59-metrics.war --spring.profiles.active=h2 --port=8085 --mark59metricsapiauth=true --mark59metricsapiuser=myuser --mark59metricsapipass=mypass 
rem )


IF "%DATABASE%" == "H2MEM" (
	rem Using H2 in memory  Starting mark59-metrics  (default application server port) 
	java -jar -Djdk.util.jar.enableMultiRelease=false ./target/mark59-metrics.war --spring.profiles.active=h2mem --port=8085 
)

IF "%DATABASE%" == "MYSQL" (
	rem Using MySQL + server info with override user/ pass / show-on-console option 
	java -jar -Djdk.util.jar.enableMultiRelease=false ./target/mark59-metrics.war --spring.profiles.active=mysql --port=8085  --mysql.server=localhost --mysql.port=3306  --mysql.schema=mark59metricsdb --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin --mark59metricsid=admin --mark59metricspasswrd=mark59 --mark59metricshide=false
)

rem -- another MySQL example --  
rem Using MySQL  Starting mark59-metrics.  Providing DB connection and server information (using default values) ie as above, but using default app user/pass  
rem java -jar -Djdk.util.jar.enableMultiRelease=false ./target/mark59-metrics.war --spring.profiles.active=mysql --port=8085  --mysql.server=localhost --mysql.port=3306  --mysql.schema=mark59metricsdb --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin

IF "%DATABASE%"=="POSTGRES" (
	rem Using Postgress + server info with override user/ pass / show-on-console option 
	java -jar -Djdk.util.jar.enableMultiRelease=false ./target/mark59-metrics.war --spring.profiles.active=pg ---port=8085  --pg.server=localhost --pg.port=5432  --pg.database=mark59metricsdb --pg.xtra.url.parms=" " --pg.username=admin --pg.password=admin --mark59metricsid=admin --mark59metricspasswrd=mark59 --mark59metricshide=false
)

PAUSE
