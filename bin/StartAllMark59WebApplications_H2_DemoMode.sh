#!/bin/sh
#   --------------------------------------------------------------------------------------------------------------
#   |  This Shell script starts the three Mark59 Web Applications:   DataHunter
#   |                                                                Metrics (Trend Analysis)
#   |                                                                Mark59 Server Metrics Web 
#   |
#   |  using a 'H2' database.  This database is built/started automatically, so a database build is not needed  
#   |   
#   |  Assuming you are using (default) server.port values for the applications, home page URLs  will be:    
#   |  
#   |     http://localhost:8081/dataHunter/
#   |     http://localhost:8080/metrics/
#   |     http://localhost:8085/mark59-server-metrics-web/
#   |  
#   --------------------------------------------------------------------------------------------------------------

# cd ~/gitrepo/mark59/mark59-wip/bin
echo starting from $PWD

DATABASE=H2
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

