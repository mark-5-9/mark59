#   -------------------------------------------------------------------------------------------------------------------------------------------------
#   |  This shell script assumes - the metrics.war file exists in the ./target directory (relative to this file) 
#   |                   - when using a mySQL database, the metricsdb database exists locally (using defaults)
#   |
#   |  Note the use of double quotes in a few places, required to cater for the & (ampersand) char, or to enter a space (equates to a blank blank here). 
#   -------------------------------------------------------------------------------------------------------------------------------------------------
# DATABASE=H2|MYSQL|POSTGRES  

echo The database has been set to "$DATABASE"

if [ "$DATABASE" = "" ]; then
	echo 'DATABASE' variable not set, assuming H2 
	# Using H2  Starting Metrics (Trend Analysis). default application server port 
	java -jar ./target/metrics.war --spring.profiles.active=h2 --port=8083
fi

if [ "$DATABASE" = "H2" ]; then
	# Using H2  Starting Metrics (Trend Analysis). default application server port 
	java -jar ./target/metrics.war --spring.profiles.active=h2 --port=8083 
fi

if [ "$DATABASE" = "H2TCPSERVER" ]; then
	# Using H2  Starting mark59-server-metrics-web  (default application server port, db TCP server started on default port) 
	java -jar ./target/metrics.war --spring.profiles.active=h2tcpserver --port=8083 --h2.port=9092
fi

if [ "$DATABASE" = "MYSQL" ]; then
	# Using MySQL. Starting the (Metrics Trend). Providing DB connection and server information (using default values)  
	java -jar ./target/metrics.war --spring.profiles.active=mysql --port=8083  --mysql.server=localhost --mysql.port=3306  --mysql.schema=metricsdb  --mysql.xtra.url.parms="?allowPublicKeyRetrieval=true&useSSL=false" --mysql.username=admin --mysql.password=admin
fi

if [ "$DATABASE" = "POSTGRES" ]; then
	# Using Postgres  Starting Metrics (Trend Analysis). Providing DB connection and server information (using postres default values) 
	java -jar ./target/metrics.war --spring.profiles.active=pg ---port=8083  --pg.server=localhost --pg.port=5432  --pg.database=metricsdb --pg.xtra.url.parms="?sslmode=disable" --pg.username=admin --pg.password=admin
fi
