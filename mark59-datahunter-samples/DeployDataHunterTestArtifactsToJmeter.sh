#   |------------------------------------------------------------------------------------------------------------------------------------------------
#   |  Use this file to copy Maven build artifacts from target folders in mark59-datahunter-samples and mark59-metrics projects,
#   |  into the target JMeter instance at ~/apache-jmeter  
#   | 
#   |  Note that mark59serverprofiles.xlsx is NOT copied. To run the '..usingExcel' testplan, copy it manually to the JMeter bin directory.
#   | 
#   |  Sample Usage.
#   |  ------------
#   |  Assumes your target Jmeter instance is at ~/apache-jmeter
#   | 
#   |  -  open up a command window and cd to the directory holding this .sh file. 
#   |  -  to execute type:  ./DeployDataHunterTestArtifactsToJmeter.sh       
#   |  
#   |  - the script is just the 'deploy' portion of the shell script bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh 
#   |-------------------------------------------------------------------------------------------------------------------------------------------------

echo This script runs the JMeter deploy for the DataHutner sample test.
echo starting from $PWD;

{   # try  

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-metrics-api/target/mark59-metrics-api  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-datahunter-samples.jar  ~/apache-jmeter/lib/ext/mark59-datahunter-samples.jar &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-datahunter-samples-dependencies &&
    cp -r ./target/mark59-datahunter-samples-dependencies ~/apache-jmeter/lib/ext/mark59-datahunter-samples-dependencies &&
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter 

} || { # catch 

    echo Deploy was unsuccessful! 
}
