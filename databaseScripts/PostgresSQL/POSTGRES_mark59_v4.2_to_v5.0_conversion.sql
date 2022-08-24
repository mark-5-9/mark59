
-- *************************************************************************************
-- **
-- **   from 4.2.x to 5.0   
-- **
-- **
-- **   RENAME OF DATABASES
-- **   ------------------
-- **   See suggestion below for sql to do the rename: 
-- **
-- **       datahunterdb               to mark59datahunterdb 
-- **       mark59servermetricswebdb   to mark59metricsdb
-- **       metricsdb                  to mark59trendsdb 
-- **
-- *************************************************************************************


-- *************************************************************************************
-- database rename  - DO A BACKUP FIRST !! -
--  (In pgamin, 'Disconnect Database...'s, and run the ALTER command from 'postgres' dbe) 
-- *************************************************************************************
ALTER DATABASE datahunterdb RENAME TO mark59datahunterdb;
ALTER DATABASE mark59servermetricswebdb RENAME TO mark59metricsdb;
ALTER DATABASE metricsdb RENAME TO mark59trendsdb;


-- *************************************************************************************
-- table changes mark59metricsdb (ex mark59servermetricswebdb))
-- *************************************************************************************
ALTER TABLE COMMANDPARSERLINKS RENAME SCRIPT_NAME TO PARSER_NAME;
ALTER TABLE COMMANDRESPONSEPARSERS RENAME SCRIPT_NAME TO PARSER_NAME;

-- *********************************************************************************************
-- 'SSH_LINIX_UNIX' changed to 'SSH_LINUX_UNIX' as a Command EXECUTOR option in mark59metricsdb  
-- *********************************************************************************************
UPDATE SERVERPROFILES SET EXECUTOR = 'SSH_LINUX_UNIX' WHERE EXECUTOR = 'SSH_LINIX_UNIX'; 
UPDATE COMMANDS SET EXECUTOR = 'SSH_LINUX_UNIX' WHERE EXECUTOR = 'SSH_LINIX_UNIX';  

-- *************************************************************************************
--  Row updates for the mark59metricsdb (ex mark59servermetricswebdb) tables
--  --------------------------------------------------------------------
--  Due to code and name changes the following changes may be needed depending on your requirements.
--  The changes assume you want/have the sample Profiles as originally provided in 4.2 / 4.2.1, and have followed similar
--   patterns  when creating additional Profiles.  Please review before execution.      
--
-- as the RunCheck program is now called TrendsLoad, and directory /metrics is renamed to /mark59-trends-load... 
-- *************************************************************************************

DELETE FROM SERVERPROFILES WHERE SERVER_PROFILE_NAME = 'DemoLINUX-DataHunterSeleniumRunCheck';
DELETE FROM SERVERPROFILES WHERE SERVER_PROFILE_NAME = 'DemoWIN-DataHunterSeleniumRunCheck';
INSERT INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterSeleniumTrendsLoad','SSH_LINUX_UNIX','localhost','','','','','22','60000','Loads Trend Analysis (PG database).  See:<br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','');
INSERT INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumTrendsLoad','WMIC_WINDOWS','localhost','','','','','','','Loads Trend Analysis (PG database).  See:<br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','');

DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumRunCheck';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumRunCheck_LINUX';
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad','WMIC_WINDOWS','process call create ''cmd.exe /c 
 echo Load DataHunter Test Results into Mark59 Trends Analysis PG database. & 
 cd /D  %METRICS_BASE_DIR% & 
 cd ../mark59-trends-load &  
 
 java -jar ./target/mark59-trends-load.jar -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d pg &
 PAUSE
''
','N','','');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad_LINUX','SSH_LINUX_UNIX','echo This script runs mark59-trends-load,to load results from a DataHunter test run into the Metrics Trends Graph.
echo starting from $PWD;

{   # try  

    cd ../mark59-trends-load/target &&
    gnome-terminal -- sh -c "java -jar mark59-trends-load.jar -a DataHunter -i ~/Mark59_Runs/Jmeter_Results/DataHunter -d pg; exec bash"

} || { # catch 
    echo attempt to execute mark59-trends-load has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Selenium-metricsTrendsLoad.sh','');

DELETE FROM SERVERCOMMANDLINKS WHERE SERVER_PROFILE_NAME = 'DemoLINUX-DataHunterSeleniumRunCheck';
DELETE FROM SERVERCOMMANDLINKS WHERE SERVER_PROFILE_NAME = 'DemoWIN-DataHunterSeleniumRunCheck';
INSERT INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterSeleniumTrendsLoad','DataHunterSeleniumTrendsLoad_LINUX');
INSERT INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterSeleniumTrendsLoad','DataHunterSeleniumTrendsLoad');

DELETE FROM COMMANDPARSERLINKS WHERE COMMAND_NAME = 'DataHunterSeleniumRunCheck';
DELETE FROM COMMANDPARSERLINKS WHERE COMMAND_NAME = 'DataHunterSeleniumRunCheck_LINUX';
INSERT INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumTrendsLoad','Return1');
INSERT INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumTrendsLoad_LINUX','Return1');

-- as directory structures/jars names of mark59 have changed ( dataHunterPerformanceTestSamples to mark59-datahunter-samples, mark59-server-metrics to mark59-metrics-api, resultFilesConverter to mark59-results-splitter )   

DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute_LINUX';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumGenJmeterReport';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumGenJmeterReport_LINUX';
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','WMIC_WINDOWS','process call create ''cmd.exe /c 
 echo Running Directly From Server Metrics Web (cmd DataHunterSeleniumDeployAndExecute) & 
 echo  METRICS_BASE_DIR: %METRICS_BASE_DIR% & 
 cd /D %METRICS_BASE_DIR% &  
 cd ..\mark59-datahunter-samples & 
 DEL C:\apache-jmeter\bin\mark59.properties & COPY .\mark59.properties C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\bin\chromedriver.exe  & COPY .\chromedriver.exe  C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\lib\ext\mark59-metrics-api.jar &
 COPY ..\mark59-metrics-api\target\mark59-metrics-api.jar  C:\apache-jmeter\lib\ext & 
 DEL C:\apache-jmeter\lib\ext\mark59-datahunter-samples.jar & 
 COPY .\target\mark59-datahunter-samples.jar  C:\apache-jmeter\lib\ext &
 RMDIR /S /Q C:\apache-jmeter\lib\ext\mark59-datahunter-samples-dependencies &
 MKDIR C:\apache-jmeter\lib\ext\mark59-datahunter-samples-dependencies &
 COPY .\target\mark59-datahunter-samples-dependencies  C:\apache-jmeter\lib\ext\mark59-datahunter-samples-dependencies &

 mkdir C:\Mark59_Runs &
 mkdir C:\Mark59_Runs\Jmeter_Results &
 mkdir C:\Mark59_Runs\Jmeter_Results\DataHunter &

 set path=%path%;C:\Windows\System32;C:\windows\system32\wbem & 
 cd /D C:\apache-jmeter\bin &

 echo Starting JMeter DataHunter test ... &  

 jmeter -n -X -f -t %METRICS_BASE_DIR%\..\mark59-datahunter-samples\test-plans\DataHunterSeleniumTestPlan.jmx -l C:\Mark59_Runs\Jmeter_Results\DataHunter\DataHunterTestResults.csv -JForceTxnFailPercent=0 -JDataHunterUrl=http://localhost:8081/mark59-datahunter -JStartCdpListeners=false &
 PAUSE
''
','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in mark59-datahunter-samples ','');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','SSH_LINUX_UNIX','echo This script runs the JMeter deploy in the background, then opens a terminal for JMeter execution.
echo starting from $PWD;

{   # try  

    cd ../mark59-datahunter-samples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo mark59-datahunter-samples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-datahunter-samples.jar  ~/apache-jmeter/lib/ext/mark59-datahunter-samples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-datahunter-samples-dependencies &&
    cp -r ./target/mark59-datahunter-samples-dependencies ~/apache-jmeter/lib/ext/mark59-datahunter-samples-dependencies &&
 
    gnome-terminal -- sh -c "~/apache-jmeter/bin/jmeter -n -X -f -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false; exec bash"

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh','');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport','WMIC_WINDOWS','process call create ''cmd.exe /c 
 cd /D %METRICS_BASE_DIR% & 
 cd../mark59-results-splitter & 
 CreateDataHunterJmeterReports.bat''
','N','','');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport_LINUX','SSH_LINUX_UNIX','echo This script creates a set of JMeter reports from a DataHunter test run.
echo starting from $PWD;

{   # try  

    cd ../mark59-results-splitter
    gnome-terminal -- sh -c "./CreateDataHunterJmeterReports.sh; exec bash"

} || { # catch 
    echo attempt to generate JMeter Reports has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Selenium-GenJmeterReport.sh','');

-- as mark59 package names have changed  (com.mark59.servermetricsweb.XX to com.mark59.metrics.XX, which can affect Grovy scripts) 

UPDATE COMMANDS SET COMMAND = REPLACE(COMMAND,'com.mark59.servermetricsweb', 'com.mark59.metrics');
