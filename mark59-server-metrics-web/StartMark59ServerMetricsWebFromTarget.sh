#!/bin/sh
#   -------------------------------------------------------------------------------------------------------------------------------------------------
#   |  This bat assumes - the mark59-server-metrics-web.war file exists in the ./target directory (relative to this file) 
#   |		              - when using a MySQL or Postgress database 'mark59servermetricswebdb' database exists locally (using defaults)  
#   |
#   |  Note the use of double quotes n a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank blank here). 
#   -------------------------------------------------------------------------------------------------------------------------------------------------
# DATABASE=H2|MYSQL|POSTGRES  


echo The database has been set to "$DATABASE"

if [ "$DATABASE" = "" ]; then
	echo 'DATABASE' variable not set, assuming H2 
	# Using H2 Starting mark59-server-metrics-web  (default application server port) 
	java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=h2 --port=8085 
fi

if [ "$DATABASE" = "H2" ]; then
	# Using H2  Starting mark59-server-metrics-web  (default application server port) 
	java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=h2 --port=8085 
fi

if [ "$DATABASE" = "H2MEM" ]; then
	# Using H2 in memory  Starting mark59-server-metrics-web  (default application server port) 
	java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=h2 --port=8085 
fi

if [ "$DATABASE" = "MYSQL" ]; then
	# Using MySQL + server info with override user/ pass / show-on-console option 
	java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=mysql --port=8085  --mysql.server=localhost --mysql.port=3306  --mysql.schema=mark59servermetricswebdb --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin --mark59servermetricswebuserid=admin --mark59servermetricswebpasswrd=mark59 --mark59servermetricswebhide=false
fi

# -- another MySQL example --  
# Using MySQL  Starting mark59-server-metrics-web.  Providing DB connection and server information (using default values) ie as above, but using default app user/pass  
# java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=mysql --port=8085  --mysql.server=localhost --mysql.port=3306  --mysql.schema=mark59servermetricswebdb --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin

if [ "$DATABASE" = "POSTGRES" ]; then
	# Using Postgress + server info with override user / pass / show-on-console option  
	java -jar ./target/mark59-server-metrics-web.war --spring.profiles.active=pg ---port=8085  --pg.server=localhost --pg.port=5432  --pg.database=mark59servermetricswebdb --pg.xtra.url.parms=" " --pg.username=admin --pg.password=admin --mark59servermetricswebuserid=admin --mark59servermetricswebpasswrd=mark59 --mark59servermetricswebhide=false
fi

# $SHELL

