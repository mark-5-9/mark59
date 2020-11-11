#!/bin/sh
#   --------------------------------------------------------------------------------------------------------------
#   |  This bat starts the three Mark59 Web Applications:   DataHunter
#   |                                                       Metrics (Trend Analysis)
#   |                                                       Mark59 Server Metrics Web 
#   |
#   |  using a 'MySQL' database.  The MySQL database build scripts must be run first.  
#   |   
#   |  Assuming you are using (default) server.port values for the applications, home page URLs  will be:    
#   |  
#   |     http://localhost:8081/dataHunter/
#   |     http://localhost:8083/metrics/
#   |     http://localhost:8085/mark59-server-metrics-web/
#   |  
#   --------------------------------------------------------------------------------------------------------------

# cd ~/gitrepo/mark59/mark59-wip/bin
echo starting from $PWD

DATABASE=MYSQL
export DATABASE
echo Database is set to "$DATABASE"

cd ..
echo Starting the DataHunter Spring Boot Application
gnome-terminal --working-directory=$PWD/dataHunter -- ./StartDataHunterFromTarget.sh  

echo Starting the Metrics Trend Analysis Web Application  
gnome-terminal --working-directory=$PWD/metrics -- ./StartMetricsTrendAnalysisFromTarget.sh

echo Starting the mark59-server-metrics-web Application 
gnome-terminal --working-directory=$PWD/mark59-server-metrics-web -- ./StartMark59ServerMetricsWebFromTarget.sh 

# $SHELL
