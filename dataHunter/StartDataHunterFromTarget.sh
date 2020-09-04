#!/bin/sh
#   -------------------------------------------------------------------------------------------------------------------------------------------------
#   |  This Shell script assumes - the dataHunter.war file exists in the ./target directory (relative to this file) 
#   |                   - when using a MySQL or Postgres database, the datahunterdb database exists locally (using defaults)
#   |
#   |  Note the use of double quotes n a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank blank here). 
#   -------------------------------------------------------------------------------------------------------------------------------------------------
# DATABASE=H2|H2MEM|MYSQL|POSTGRES  

echo The database has been set to "$DATABASE"

#echo 'DATABASE' variable not set, assuming H2 
if [ "$DATABASE" = "" ]; then
	echo 'DATABASE' variable not set, assuming H2 
	# Using H2 Database.  Starting the DataHunter Web Application (default values) 
	java -jar ./target/dataHunter.war  --spring.profiles.active=h2 --port=8081
fi

if [ "$DATABASE" = "H2" ]; then
	# Using H2 Database.  Starting the DataHunter Web Application (default values) 
	java -jar ./target/dataHunter.war  --spring.profiles.active=h2 --port=8081
fi


if [ "$DATABASE" = "H2MEM" ]; then
	# Using H2 Database 'IN MEMORY' option.  Starting the DataHunter Web Application (default values) 
	java -jar ./target/dataHunter.war  --spring.profiles.active=h2mem --port=8081
fi



if [ "$DATABASE" = "MYSQL" ]; then
	# Using MySQL. Starting DataHunter providing DB connection and server information (using default values) 
	java -jar ./target/dataHunter.war  --spring.profiles.active=mysql --port=8081 --mysql.server=localhost --mysql.port=3306 --mysql.schema=datahunterdb --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin
fi

# -- another MySQL example -- 
# Using MySQL  Starting DataHunter defaults  (will default to MySQL database, application URL port of 8081) 
# java -jar ./target/dataHunter.war

if [ "$DATABASE" = "POSTGRES" ]; then
	# Using Postgres.  Starting DataHunter providing DB connection and server information (using postgres default values) 
	java -jar ./target/dataHunter.war --spring.profiles.active=pg ---port=8081  --pg.server=localhost --pg.port=5432  --pg.database=datahunterdb --pg.xtra.url.parms=" " --pg.username=admin --pg.password=admin
fi

# $SHELL
