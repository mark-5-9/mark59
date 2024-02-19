
-- *************************************************************************************
-- **
-- **   from 5.4+ to 6.0   
-- **
-- **   THIS IS AN OPTIONAL UPDATE 
-- **   --------------------------------------
-- **   Only required to run the sample commands from the Quick Start.
-- **   (for the Win/Linux and Mac environments)
-- **   
-- **   Accounting for bat/sh file renames, and project renames 
-- **   (mark59-scripting-samples changed to mark59-scripting-samples)   
-- **
-- *************************************************************************************

SET SQL_SAFE_UPDATES = 0;

DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute_LINUX';

DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumGenJmeterReport';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumGenJmeterReport_LINUX';

DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumTrendsLoad';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumTrendsLoad_LINUX';


INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','POWERSHELL_WINDOWS','Start-Process -FilePath \'${METRICS_BASE_DIR}\\..\\bin\\TestRunWIN-DataHunter-Selenium-DeployAndExecute.bat\'','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in mark59-scripting-samples ','[]');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','SSH_LINUX_UNIX','echo This script runs the JMeter deploy in the background, then opens a terminal for JMeter execution.\r\necho starting from $PWD;\r\n\r\n{   # try  \r\n\r\n    cd ../mark59-scripting-samples && \r\n    DH_TEST_SAMPLES_DIR=$(pwd) && \r\n    echo mark59-scripting-samples base dir is $DH_TEST_SAMPLES_DIR &&\r\n\r\n    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&\r\n    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && \r\n    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && \r\n    cp ./target/mark59-scripting-samples.jar  ~/apache-jmeter/lib/ext/mark59-scripting-samples.jar && \r\n    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&\r\n    rm -rf ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&\r\n    cp -r ./target/mark59-scripting-samples-dependencies ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&\r\n \r\n    gnome-terminal -- sh -c \"~/apache-jmeter/bin/jmeter -n -X -f  -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false; exec bash\"\r\n\r\n} || { # catch \r\n    echo Deploy was unsuccessful! \r\n}','Y','refer bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh','[]');

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\\..\\mark59-results-splitter;\r\nStart-Process -FilePath \'.\\CreateDataHunterJmeterReports.bat\'\r\n','N','','[]');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport_LINUX','SSH_LINUX_UNIX','echo This script creates a set of JMeter reports from a DataHunter test run.\r\necho starting from $PWD;\r\n\r\n{   # try  \r\n\r\n    cd ../mark59-results-splitter\r\n    gnome-terminal -- sh -c \"./CreateDataHunterJmeterReports.sh; exec bash\"\r\n\r\n} || { # catch \r\n    echo attempt to generate JMeter Reports has failed! \r\n}\r\n','Y','refer bin/TestRunLINUX-DataHunter-Test-GenJmeterReport.sh',NULL);

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\\..\\bin;\r\nStart-Process -FilePath \'.\\TestRunWIN-DataHunter-Test-TrendsLoad.bat\' -ArgumentList \'${DATABASE}\'\r\n','N','','[\"DATABASE\"]');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad_LINUX','SSH_LINUX_UNIX','echo This script runs mark59-trends-load,to load results from a DataHunter test run into the Metrics Trends Graph.\r\necho starting from $PWD;\r\n\r\n{   # try  \r\n\r\n    cd ../mark59-trends-load/target &&\r\n    gnome-terminal -- sh -c \"java -jar mark59-trends-load.jar -a DataHunter -i ~/Mark59_Runs/Jmeter_Results/DataHunter -d mysql; exec bash\"\r\n\r\n} || { # catch \r\n    echo attempt to execute mark59-trends-load has failed! \r\n}\r\n','Y','refer bin/TestRunLINUX-DataHunter-Test-TrendsLoad.sh',NULL);

