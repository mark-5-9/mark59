#   -------------------------------------------------------------------------------------------------------------------------------------------------
#   | Generate the JMeter reports for a DataHunter Test.
#   |
#   |  Alternative to running this .sh 
#   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-metrics" 
#   |		 - run the DemoLINUX-DDataHunterTestGenJmeterReport profile. 
#   |
#   |  There are are no database considerations when running JMeter report generation.
#   |
#   |  JMeter input results file expected at ~/Mark59_Runs/Jmeter_Results/DataHunter/
#   |
#   |  Output generated to ~/Mark59_Runs/Jmeter_Reports/DataHunter/ 
#   |
#   -------------------------------------------------------------------------------------------------------------------------------------------------
{   # try  

    cd ../mark59-results-splitter
    ./CreateDataHunterJmeterReports.sh

} || { # catch 
    echo attempt to generate JMeter Reports has failed! 
}
$SHELL
