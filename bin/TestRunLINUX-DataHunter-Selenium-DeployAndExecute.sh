#     -------------------------------------------------------------------------------------------------------------------------------------------------
#     | Deploy Artifacts and Run JMeter DataHunter Selenium Test.
#     |
#     | NOTE - you may need to ensure the chromedriver.exe file at root of dataHunterPerformanceTestSamples project is compatible with your Chrome version
#     |        (see Mark59 user guide for details).  
#     |      - mark59serverprofiles.xlsx is not copied. Before you run the '..usingExcel' testplan, copy it manually to the JMeter bin directory
#     |        (not necessary before running this bat file as it runs the DataHunterSeleniumTestPlan which doesn't use the spreadsheet).
#     |
#     |  An instance of JMeter is expected at C:\apache-jmeter
#     |
#     |  Alternative to running this .sh 
#     |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-server-metrics-web" 
#     |		 - run the DemoLINUX-DataHunter-Selenium-DeployAndExecute profile. 
#     |
#     |  The only database considerations are that when the test is running server metrics are obtained invoking the 'localhost_LINUX' profile from the running
#     |      server-metrics-web application (ie via whatever DB it is connected to), and also the datahunter application will be writing to its database.
#     |
#     |  logging at  ~/apache-jmeter/bin/jmeter.log
#     |  JMeter result file output to ~/Mark59_Runs/Jmeter_Results/DataHunter
#     |
#     -------------------------------------------------------------------------------------------------------------------------------------------------

echo This script runs the JMeter deploy in the background, then opens a terminal for JMeter execution.
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
 
    gnome-terminal -- sh -c "~/apache-jmeter/bin/jmeter -n -X -f  -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0; exec bash"

} || { # catch 
    echo Deploy was unsuccessful! 
}
