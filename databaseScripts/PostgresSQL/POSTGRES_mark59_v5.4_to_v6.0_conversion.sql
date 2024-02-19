
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

DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute_LINUX';

DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumGenJmeterReport';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumGenJmeterReport_LINUX';

DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumTrendsLoad';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumTrendsLoad_LINUX';


INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','POWERSHELL_WINDOWS','Start-Process -FilePath ''${METRICS_BASE_DIR}\..\bin\TestRunWIN-DataHunter-Selenium-DeployAndExecute.bat''','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in mark59-scripting-samples ','[]');

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','SSH_LINUX_UNIX','echo This script runs the JMeter deploy in the background, then opens a terminal for JMeter execution.
echo starting from $PWD;

{   # try  

    cd ../mark59-scripting-samples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo mark59-scripting-samples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-scripting-samples.jar  ~/apache-jmeter/lib/ext/mark59-scripting-samples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
    cp -r ./target/mark59-scripting-samples-dependencies ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
 
    gnome-terminal -- sh -c "~/apache-jmeter/bin/jmeter -n -X -f  -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false; exec bash"

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh','[]');

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\..\mark59-results-splitter;
Start-Process -FilePath ''.\CreateDataHunterJmeterReports.bat''
','N','','[]');

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport_LINUX','SSH_LINUX_UNIX','echo This script creates a set of JMeter reports from a DataHunter test run.
echo starting from $PWD;

{   # try  

    cd ../mark59-results-splitter
    gnome-terminal -- sh -c "./CreateDataHunterJmeterReports.sh; exec bash"

} || { # catch 
    echo attempt to generate JMeter Reports has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Test-GenJmeterReport.sh',NULL);

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\..\bin;
Start-Process -FilePath ''.\TestRunWIN-DataHunter-Test-TrendsLoad.bat'' -ArgumentList ''${DATABASE}''
','N','','["DATABASE"]');

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad_LINUX','SSH_LINUX_UNIX','echo This script runs mark59-trends-load,to load results from a DataHunter test run into the Metrics Trends Graph.
echo starting from $PWD;

{   # try  

    cd ../mark59-trends-load/target &&
    gnome-terminal -- sh -c "java -jar mark59-trends-load.jar -a DataHunter -i ~/Mark59_Runs/Jmeter_Results/DataHunter -d pg; exec bash"

} || { # catch 
    echo attempt to execute mark59-trends-load has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Test-TrendsLoad.sh',NULL);
