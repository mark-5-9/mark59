
REM   Place this bat file in the same folder as the DataHunter Spring Boot war file 

MODE con:cols=180 lines=60
ECHO Starting the dataHunter Spring Boot Application 

REM for using a h2 (in-memory) database:

java -jar dataHunter.war --server.port=8081 --spring.profiles.active=h2

REM for using a mqsql database:
REM java -jar dataHunter.war --mysql.server=localhost --mysql.port=3306  --server.port=8081 --spring.profiles.active=mysql

PAUSE