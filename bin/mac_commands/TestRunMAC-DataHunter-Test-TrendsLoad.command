#!/usr/bin/env bash
#   -------------------------------------------------------------------------------------------------------------------------------------------------
#   | Load DataHunter Test Results to Mark59 Trends database.
#   |
#   |
#   |  Alternative to running this .command  
#   |	   - login  to the server-metrics-web application  "http://localhost:8085/mark59-metrics" 
#   |	   - run the DemoMAC-DataHunterTestTrendsLoad profile. 
#   |
#   |  JMeter input results file expected at ~/Mark59_Runs/Jmeter_Results/DataHunter/ 
#   |
#   |  Loaded run can be seen at http://localhost:8083/mark59-trends/trending?reqApp=DataHunter    (assuming default setup)
#   |
#   |  *** YOU NEED TO SELECT WHICH DATABASE TO LOAD RESULTS TO BEFORE EXECUTION ***
#   |
#   -------------------------------------------------------------------------------------------------------------------------------------------------
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo Convenience script which can be invoked directly from Finder
echo script dir is $SCRIPT_DIR
cd $SCRIPT_DIR


DATABASE=H2
# DATABASE=MYSQL
# DATABASE=POSTGRES

# -- special purpose values
# DATABASE=H2TCPCLIENT
# DATABASE=H2MEM

cd ../../mark59-trends-load;
sh LoadDataHunterResultsIntoTrends.sh "${DATABASE}";

$SHELL
