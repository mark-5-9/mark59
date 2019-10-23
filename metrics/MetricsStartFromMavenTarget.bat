
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  Use this file to run the Eclipse build target after a Maven build has been (so the dataHunter war file exists in the /target directory ) 
REM   | 
REM   |  Sample Usage.
REM   |  ------------
REM   |  Assuming you have placed your mark59 repo at C:\gitrepo\mark59,  then:
REM   | 
REM   |  -  open up a Dos command prompt an cd to this projects root:
REM   | 
REM   |     C:
REM   |     cd C:\gitrepo\mark59\metrics  
REM   | 
REM   |  -  then to start dataHunter using this bat file, type:
REM   | 
REM   |     MetricsStartFromMavenTarget.bat       
REM   |  
REM   |  -  Assuming your are using server.port of 8080 from localhost metrics  home page URL will be    
REM   |  
REM   |     http://localhost:8080/metrics/
REM   |  
REM   -------------------------------------------------------------------------------------------------------------------------------------------------


MODE con:cols=180 lines=100
ECHO Starting the dataHunter Spring Boot Application 

REM  ECHO Starting the Metric Comparative Analysis Spring Boot Application (default mode), using Eclipse build artifacts  
REM  java -jar ./target/metrics.war

ECHO Starting the Metric Comparative Analysis Spring Boot Application with DB connection and server information using Eclipse build artifact 
java -jar ./target/metrics.war  --mysql.server=localhost --mysql.port=3306  --server.port=8080 --spring.profiles.active=mysql

PAUSE