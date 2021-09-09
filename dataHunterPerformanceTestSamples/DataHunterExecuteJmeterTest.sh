#   |------------------------------------------------------------------------------------------------------------------------------------------------
#   |  Execute DataHunterSeleniumTestPlan in non-gui mode 
#   | 
#   |  Sample Usage.
#   |  ------------
#   |  Assumes you have placed your target Jmeter instance is at ~/apache-jmeter,
#   |                         you have deployed artifacts to the Jmeter instance (see DeployDataHunterTestArtifactsToJmeter.sh) 
#   |                          Java 8+ is installed 
#   |                          A local instance of DataHunter is running on (default) port 8081 
#   | 
#   |  -  open up a command window and cd to the directory holding this .sh file.
#   |  -  to execute the DataHunter (Selenium) JMeter test, type : ./DataHunterExecuteJmeterTest.sh 
#   |  
#   |  - the script is just the 'execute' portion of the shell script bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh 
#   |-------------------------------------------------------------------------------------------------------------------------------------------------
echo Opens a terminal for JMeter execution.
echo starting from $PWD;

{   # try  

    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter && 
 
    gnome-terminal -- sh -c "~/apache-jmeter/bin/jmeter -n -X -f  -t ./test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JDataHunterUrl=http://localhost:8081/dataHunter -JForceTxnFailPercent=0 -JStartCdpListeners=false; exec bash"

} || { # catch 
    echo A problem attempting to start the DataHunter JMeter test! 
}
