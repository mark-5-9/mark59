
-- *************************************************************************************
-- **
-- **   the database changes for the 4.0.0.beta-4 release implement the capture of  
-- **   CDP (Chrome DevTools Protocol) transactions from JMeter Selenium scripts,   
-- **   using the new CDP Network capabilities built into Selenium 4.  
-- **
-- **   This involves adding a new column called IS_CDP_TXN (which is indexed) to 
-- **   three tables.  Unfortunately Posgres does not allow the insertions of a 
-- **   new column in expect at the end of a table, so you will need to manually  
-- **   adjust any data, and reload it into the new table structures as defined in   
-- **   POSTGRESmetricsDataBaseCreation.sql
-- **
-- **   For information, below are the 4.0 changes for the MYSQL database.
-- **
-- *************************************************************************************

-- ALTER TABLE `metricsdb`.`TRANSACTION` 
-- ADD COLUMN `IS_CDP_TXN` CHAR(1) NOT NULL DEFAULT 'N' AFTER `TXN_TYPE`,
-- DROP PRIMARY KEY,
-- ADD PRIMARY KEY (`APPLICATION`, `RUN_TIME`, `TXN_ID`, `TXN_TYPE`, `IS_CDP_TXN`);
--
-- ALTER TABLE `metricsdb`.`SLA` 
-- ADD COLUMN `IS_CDP_TXN` CHAR(1) NOT NULL DEFAULT 'N' AFTER `TXN_ID`,
-- DROP PRIMARY KEY,
-- ADD PRIMARY KEY (`TXN_ID`, `IS_CDP_TXN`, `APPLICATION`);
--
-- ALTER TABLE `metricsdb`.`TESTTRANSACTIONS` 
-- ADD COLUMN `IS_CDP_TXN` CHAR(1) NOT NULL DEFAULT 'N' AFTER `TXN_TYPE`;




-- *************************************************************************************
-- **
-- **   OPTIONAL STATEMENTS  
-- **  
-- **   These statements simply adjust the 'DataHunter' application sample that existed   
-- **   in the 3.3 release, so that they match the 'DataHunter' sample supplied in the    
-- **   version 4 release.
-- **
-- **   You don't need to execute these rows if you have no intentional of running the 
-- **   DataHunter samples as described in the quick start guide.
-- **
-- **   However you may want to at least take note of the change made to the COMMANDS table 
-- **   in the mark59servermetricswebdb database - as that demos the extra steps needed
-- **   to transfer the Selenium script's dependencies library, which now must also be   
-- **   copied into the JMeter lib/ext directory, as well as the jar.
-- **
-- *************************************************************************************

-- connect to the metricsdb database and run:

DELETE FROM SLA where APPLICATION = 'DataHunter'; 

-- run ALL the "INSERT INTO `SLA` VALUES... " statements in POSTGRESmetricsDataBaseCreation.sql 

DELETE FROM TRANSACTION where APPLICATION = 'DataHunter'; 
DELETE FROM TRANSACTION where APPLICATION = 'DataHunterDistributed'; 

-- run ALL the "INSERT INTO TRANSACTION VALUES ('DataHunter',... " statements in POSTGRESmetricsDataBaseCreation.sql 
-- run ALL the "INSERT INTO TRANSACTION VALUES ('DataHunterDistributed',... " statements in POSTGRESmetricsDataBaseCreation.sql 



-- connect to mark59servermetricswebdb database and run:
 
DELETE FROM COMMANDS where COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute'; 
DELETE FROM COMMANDS where COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute_LINUX'; 
 
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','WMIC_WINDOWS','process call create ''cmd.exe /c 
 echo Running Directly From Server Metrics Web (cmd DataHunterSeleniumDeployAndExecute) & 
 echo  SERVER_METRICS_WEB_BASE_DIR: %SERVER_METRICS_WEB_BASE_DIR% & 
 cd /D %SERVER_METRICS_WEB_BASE_DIR% &  
 cd ..\dataHunterPerformanceTestSamples & 
 DEL C:\apache-jmeter\bin\mark59.properties & COPY .\mark59.properties C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\bin\chromedriver.exe  & COPY .\chromedriver.exe  C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\lib\ext\mark59-server-metrics.jar &
 COPY ..\mark59-server-metrics\target\mark59-server-metrics.jar  C:\apache-jmeter\lib\ext & 
 DEL C:\apache-jmeter\lib\ext\dataHunterPerformanceTestSamples.jar & 
 COPY .\target\dataHunterPerformanceTestSamples.jar  C:\apache-jmeter\lib\ext &
 RMDIR /S /Q C:\apache-jmeter\lib\ext\dataHunterPerformanceTestSamples-dependencies &
 MKDIR C:\apache-jmeter\lib\ext\dataHunterPerformanceTestSamples-dependencies &
 COPY .\target\dataHunterPerformanceTestSamples-dependencies  C:\apache-jmeter\lib\ext\dataHunterPerformanceTestSamples-dependencies &

 mkdir C:\Mark59_Runs &
 mkdir C:\Mark59_Runs\Jmeter_Results &
 mkdir C:\Mark59_Runs\Jmeter_Results\DataHunter &

 set path=%path%;C:\Windows\System32;C:\windows\system32\wbem & 
 cd /D C:\apache-jmeter\bin &

 echo Starting JMeter DataHunter test ... &  

 jmeter -n -X -f -t %SERVER_METRICS_WEB_BASE_DIR%\..\dataHunterPerformanceTestSamples\test-plans\DataHunterSeleniumTestPlan.jmx -l C:\Mark59_Runs\Jmeter_Results\DataHunter\DataHunterTestResults.csv -JForceTxnFailPercent=0 -JDataHunterUrl=http://localhost:8081/dataHunter -JStartCdpListeners=false &
 PAUSE
''
','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in dataHunterPerformanceTestSamples ','');

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','SSH_LINIX_UNIX','echo This script runs the JMeter deploy in the background, then opens a terminal for JMeter execution.
echo starting from $PWD;

{   # try  

    cd ../dataHunterPerformanceTestSamples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo dataHunterPerformanceTestSamples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-server-metrics/target/mark59-server-metrics.jar  ~/apache-jmeter/lib/ext/mark59-server-metrics.jar && 
    cp ./target/dataHunterPerformanceTestSamples.jar  ~/apache-jmeter/lib/ext/dataHunterPerformanceTestSamples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&
    rm -rf ~/apache-jmeter/lib/ext/dataHunterPerformanceTestSamples-dependencies &&
    cp -r ./target/dataHunterPerformanceTestSamples-dependencies ~/apache-jmeter/lib/ext/dataHunterPerformanceTestSamples-dependencies &&
 
    gnome-terminal -- sh -c "~/apache-jmeter/bin/jmeter -n -X -f -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false; exec bash"

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh','');
 