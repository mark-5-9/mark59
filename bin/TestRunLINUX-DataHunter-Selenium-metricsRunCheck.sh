#   -------------------------------------------------------------------------------------------------------------------------------------------------
#   | Load DataHunter Test Results to Mark59 Metrics (Trend Analysis) database.
#   |
#   |
#   |  Alternative to running this .sh   ** H2 DATABASE ONLY **
#   |	   - login  to the server-metrics-web application  "http://localhost:8085/mark59-server-metrics-web" 
#   |	   - run the DemoLINUX-DataHunterSeleniumRunCheck profile. 
#   |
#   |  JMeter input results file expected at ~/Mark59_Runs/Jmeter_Results/DataHunter/ 
#   |
#   |  Loaded run can be seen at http://localhost:8083/metrics/trending?reqApp=DataHunter    (assuming default setup)
#   |
#   |  *** YOU NEED TO SELECT WHICH DATABASE TO LOAD RESULTS TO BEFORE EXECUTION ***
#   |
#   -------------------------------------------------------------------------------------------------------------------------------------------------


echo  "YOU NEED TO SELECT WHICH DATABASE TO LOAD RESULTS TO BEFORE EXECUTION (DEFAULT IS H2) "; 

DATABASE=H2
# DATABASE=MYSQL
# DATABASE=POSTGRES

# -- special purpose values
# DATABASE=H2TCPCLIENT
# DATABASE=H2MEM

cd ../metricsRuncheck;
./LoadDataHunterResultsIntoMetricsTrendAnalysis.sh "${DATABASE}";

cd ../bin
$SHELL
