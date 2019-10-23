REM   Copyright 2019 Insurance Australia Group Limited
REM 
REM   Licensed under the Apache License, Version 2.0 (the "License");
REM   you may not use this file except in compliance with the License.
REM   You may obtain a copy of the License at
REM 
REM     http://www.apache.org/licenses/LICENSE-2.0
REM  
REM   Unless required by applicable law or agreed to in writing, software
REM   distributed under the License is distributed on an "AS IS" BASIS,
REM   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM   See the License for the specific language governing permissions and
REM   limitations under the License.
REM 
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
REM   |     cd C:\gitrepo\mark59\dataHunter  
REM   | 
REM   |  -  then to start dataHunter using this bat file, type:
REM   | 
REM   |     DataHunterStartFromMavenTarget.bat       
REM   |  
REM   |  -  Assuming your are using server.port of 8081, from localhost dataHunter home page URL will be    
REM   |  
REM   |     http://localhost:8081/dataHunter/
REM   |  
REM   |  -  Standard defaults are used for the H2 database console login.  Driver Class:org.h2.Driver JDBC URL:jdbc:h2:~/test User:sa Password:(blank)
REM   |  
REM   -------------------------------------------------------------------------------------------------------------------------------------------------

MODE con:cols=180 lines=60
ECHO Starting the dataHunter Spring Boot Application 

REM for using a h2 (in-memory) database:

java -jar ./target/dataHunter.war --server.port=8081 --spring.profiles.active=h2

REM for using a mqsql database:
REM java -jar ./target/dataHunter.war --mysql.server=localhost --mysql.port=3306  --server.port=8081 --spring.profiles.active=mysql

PAUSE