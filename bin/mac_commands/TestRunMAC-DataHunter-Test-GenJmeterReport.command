#!/usr/bin/env bash
#   -------------------------------------------------------------------------------------------------------------------------------------------------
#   | Generate the JMeter reports for a DataHunter Test.
#   |
#   |  Alternative to running this .command 
#   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-metrics" 
#   |		 - run the DemoMAC-DataHunterTestGenJmeterReport profile. 
#   |
#   |  There are are no database considerations when running JMeter report generation.
#   |
#   |  JMeter input results file expected at ~/Mark59_Runs/Jmeter_Results/DataHunter/
#   |
#   |  Output generated to ~/Mark59_Runs/Jmeter_Reports/DataHunter/ 
#   |
#   -------------------------------------------------------------------------------------------------------------------------------------------------
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo Convenience script which can be invoked directly from Finder
echo script dir is $SCRIPT_DIR
cd $SCRIPT_DIR

{   # try  

    cd ../../mark59-results-splitter
    sh CreateDataHunterJmeterReports.sh

} || { # catch 
    echo attempt to generate JMeter Reports has failed! 
}
$SHELL
