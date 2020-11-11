REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  Load DataHunter Test Results to Mark59 Metrics (Trend Analysis) database.
REM   | 
REM   |  This bat assumes - the metricsRuncheck.jar file exists in the ./target directory (relative to this file) 
REM   |                   - when using a MySQL or Postgres database, the metricsdb database exists locally (using defaults)
REM   |
REM   |  Notes : the use of double quotes in a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank here). 
REM   |          the current time is use as the reference..
REM   |
REM   |  To directly execute :
REM   |  ---------------------
REM   |  You need to un-rem whichever SET DATABASE you wish to user first  
REM   |  (re-rem all the SET DATABASE line afterwards if you intend to run this bat via the mark59 /bin directory bat later)
REM   |
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
@echo off
MODE con:cols=180 lines=500
rem SET "DATABASE=H2"
rem SET "DATABASE=MYSQL"
rem SET "DATABASE=POSTGRES"

rem -- special purpose values
rem SET "DATABASE=H2MEM"
rem SET "DATABASE=H2TCPCLIENT"

ECHO The database has been set to %DATABASE%

IF "%DATABASE%" == "" (
	rem Using H2  Starting metricsRuncheck batch for a dataHunter load (defaults taken on parameters) 
	ECHO No database set, so assuming H2	
	java -jar ./target/metricsRuncheck.jar  -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d h2   	
)

IF "%DATABASE%" == "H2" (
	rem Using H2  Starting metricsRuncheck batch for a dataHunter load (defaults taken on parameters) 
	java -jar ./target/metricsRuncheck.jar  -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d h2   	
)

IF "%DATABASE%" == "H2TCPCLIENT" (
	rem Using H2 using a tcp connection to start  metricsRuncheck batch for a dataHunter load (defaults taken on parameters. (Note for docker use -h metrics) 
	java -jar ./target/metricsRuncheck.jar  -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d h2tcpclient -h localhost  -p 9092   	
)

IF "%DATABASE%" == "H2MEM" (
	rem Using H2 in memory  Starting metricsRuncheck batch for a dataHunter load (defaults taken on parameters) 
	java -jar ./target/metricsRuncheck.jar  -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d h2mem   	
)

IF "%DATABASE%" == "MYSQL" (
	rem using MySQL:  Starting metricsRuncheck batch with some parameters provided (defaults taken on other parameters. ) 
	java -jar ./target/metricsRuncheck.jar -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d mysql -h localhost  -p 3306 -s metricsdb -q "?allowPublicKeyRetrieval=true&useSSL=false" -t JMETER  -r "%date% %time%"
)

IF "%DATABASE%"=="POSTGRES" (
	rem using Postgress:  Starting metricsRuncheck batch with some parameters provided (defaults taken on other parameters. ) 
	java -jar ./target/metricsRuncheck.jar -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d pg -h localhost  -p 5432 -s metricsdb -q "?sslmode=disable" -t JMETER  -r "%date% %time%"
)

PAUSE