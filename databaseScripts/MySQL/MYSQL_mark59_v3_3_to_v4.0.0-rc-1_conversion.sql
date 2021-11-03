
-- *************************************************************************************
-- **
-- **   the database changes for the 4.0.0-rc1 release implement the capture of  
-- **   CDP (Chrome DevTools Protocol) transactions from JMeter Selenium scripts,   
-- **   using the new CDP Network capabilities built into Selenium 4.  
-- **
-- *************************************************************************************


SET SQL_SAFE_UPDATES = 0;

ALTER TABLE `metricsdb`.`TRANSACTION` 
ADD COLUMN `IS_CDP_TXN` CHAR(1) NOT NULL DEFAULT 'N' AFTER `TXN_TYPE`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`APPLICATION`, `RUN_TIME`, `TXN_ID`, `TXN_TYPE`, `IS_CDP_TXN`);
;

ALTER TABLE `metricsdb`.`SLA` 
ADD COLUMN `IS_CDP_TXN` CHAR(1) NOT NULL DEFAULT 'N' AFTER `TXN_ID`,
DROP PRIMARY KEY,
ADD PRIMARY KEY (`TXN_ID`, `IS_CDP_TXN`, `APPLICATION`);
;

ALTER TABLE `metricsdb`.`TESTTRANSACTIONS` 
ADD COLUMN `IS_CDP_TXN` CHAR(1) NOT NULL DEFAULT 'N' AFTER `TXN_TYPE`;


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


DELETE FROM metricsdb.SLA where APPLICATION = 'DataHunter'; 

-- run ALL the "INSERT INTO `SLA` VALUES... " statements in MYSQLmetricsDataBaseCreation.sql 

DELETE FROM metricsdb.TRANSACTION where APPLICATION = 'DataHunter'; 
DELETE FROM metricsdb.TRANSACTION where APPLICATION = 'DataHunterDistributed'; 

-- run ALL the "INSERT INTO `TRANSACTION` VALUES ('DataHunter',... " statements in MYSQLmetricsDataBaseCreation.sql 
-- run ALL the "INSERT INTO `TRANSACTION` VALUES ('DataHunterDistributed',... " statements in MYSQLmetricsDataBaseCreation.sql 

 
DELETE FROM mark59servermetricswebdb.COMMANDS where COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute'; 
DELETE FROM mark59servermetricswebdb.COMMANDS where COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute_LINUX'; 
 
INSERT INTO mark59servermetricswebdb.COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','WMIC_WINDOWS','process call create \'cmd.exe /c \r\n echo Running Directly From Server Metrics Web (cmd DataHunterSeleniumDeployAndExecute) & \r\n echo  SERVER_METRICS_WEB_BASE_DIR: %SERVER_METRICS_WEB_BASE_DIR% & \r\n cd /D %SERVER_METRICS_WEB_BASE_DIR% &\r\n cd ..\\dataHunterPerformanceTestSamples & \r\n DEL C:\\apache-jmeter\\bin\\mark59.properties & COPY .\\mark59.properties C:\\apache-jmeter\\bin &\r\n DEL C:\\apache-jmeter\\bin\\chromedriver.exe  & COPY .\\chromedriver.exe  C:\\apache-jmeter\\bin &\r\n DEL C:\\apache-jmeter\\lib\\ext\\mark59-server-metrics.jar &\r\n COPY ..\\mark59-server-metrics\\target\\mark59-server-metrics.jar  C:\\apache-jmeter\\lib\\ext & \r\n DEL C:\\apache-jmeter\\lib\\ext\\dataHunterPerformanceTestSamples.jar & \r\n COPY .\\target\\dataHunterPerformanceTestSamples.jar  C:\\apache-jmeter\\lib\\ext & \r\n RMDIR /S /Q C:\\apache-jmeter\\lib\\ext\\dataHunterPerformanceTestSamples-dependencies &\r\n MKDIR C:\\apache-jmeter\\lib\\ext\\dataHunterPerformanceTestSamples-dependencies &\r\n COPY .\\target\\dataHunterPerformanceTestSamples-dependencies  C:\\apache-jmeter\\lib\\ext\\dataHunterPerformanceTestSamples-dependencies &\r\n\r\n mkdir C:\\Mark59_Runs &\r\n mkdir C:\\Mark59_Runs\\Jmeter_Results &\r\n mkdir C:\\Mark59_Runs\\Jmeter_Results\\DataHunter &\r\n\r\n set path=%path%;C:\\Windows\\System32;C:\\windows\\system32\\wbem & \r\n cd /D C:\\apache-jmeter\\bin &\r\n\r\n jmeter -n -X -f -t %SERVER_METRICS_WEB_BASE_DIR%\\..\\dataHunterPerformanceTestSamples\\test-plans\\DataHunterSeleniumTestPlan.jmx -l C:\\Mark59_Runs\\Jmeter_Results\\DataHunter\\DataHunterTestResults.csv -JForceTxnFailPercent=0 -JDataHunterUrl=http://localhost:8081/dataHunter -JStartCdpListeners=false &\r\n\r\n PAUSE\r\n\'\r\n','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in dataHunterPerformanceTestSamples ','[]');
INSERT INTO mark59servermetricswebdb.COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','SSH_LINIX_UNIX','echo This script runs the JMeter deploy in the background, then opens a terminal for JMeter execution.\r\necho starting from $PWD;\r\n\r\n{   # try  \r\n\r\n    cd ../dataHunterPerformanceTestSamples && \r\n    DH_TEST_SAMPLES_DIR=$(pwd) && \r\n    echo dataHunterPerformanceTestSamples base dir is $DH_TEST_SAMPLES_DIR &&\r\n\r\n    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&\r\n    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && \r\n    cp ../mark59-server-metrics/target/mark59-server-metrics.jar  ~/apache-jmeter/lib/ext/mark59-server-metrics.jar && \r\n    cp ./target/dataHunterPerformanceTestSamples.jar  ~/apache-jmeter/lib/ext/dataHunterPerformanceTestSamples.jar && \r\n    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&\r\n    rm -rf ~/apache-jmeter/lib/ext/dataHunterPerformanceTestSamples-dependencies &&\r\n    cp -r ./target/dataHunterPerformanceTestSamples-dependencies ~/apache-jmeter/lib/ext/dataHunterPerformanceTestSamples-dependencies &&\r\n \r\n    gnome-terminal -- sh -c \"~/apache-jmeter/bin/jmeter -n -X -f  -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false; exec bash\"\r\n\r\n} || { # catch \r\n    echo Deploy was unsuccessful! \r\n}','Y','refer bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh','[]');
 