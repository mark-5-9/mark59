#!/bin/sh
#   --------------------------------------------------------------------------------------------------------------
#   |  This bat starts the three Mark59 Web Applications:   DataHunter
#   |                                                       Trends Analysis
#   |                                                       Mark59 Server Metrics Web 
#   |
#   |  using a 'POSTGRES' database.  The POSTGRES database build scripts must be run first.  
#   |   
#   |  Assuming you are using (default) server.port values for the applications, home page URLs  will be:    
#   |  
#   |     http://localhost:8081/mark59-datahunter/
#   |     http://localhost:8083/mark59-trends/
#   |     http://localhost:8085/mark59-metrics/
#   |  
#   --------------------------------------------------------------------------------------------------------------

# cd ~/gitrepo/mark59/mark59-wip/bin
echo starting from $PWD

DATABASE=POSTGRES
export DATABASE
echo Database is set to "$DATABASE"

cd ..
echo Starting the DataHunter Spring Boot Application
gnome-terminal --working-directory=$PWD/mark59-datahunter -- ./StartDataHunterFromTarget.sh  

echo Starting the Metrics Trend Analysis Web Application  
gnome-terminal --working-directory=$PWD/mark59-trends -- ./StartTrendsFromTarget.sh

echo Starting the mark59-metrics Application 
gnome-terminal --working-directory=$PWD/mark59-metrics -- ./StartMetricsFromTarget.sh 

# $SHELL
