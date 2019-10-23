REM   Place this bat file in the same folder as the Metrics Spring Boot metrics.war file 
MODE con:cols=180 lines=60

ECHO Starting the Metrics Spring Boot Application with standard mysql port, on server port 8080 ....
  
java -jar metrics.war  --mysql.server=localhost --mysql.port=3306  --server.port=8080 --spring.profiles.active=mysql

PAUSE