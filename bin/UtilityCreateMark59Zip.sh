#!/bin/sh
#   --------------------------------------------------------------------------------------------------------------
#   |  An adminstration tool to create the file structure for inclusion in the download Mark59 zip file. 
#   | 
#   |  Files are derived from the Mark59 git repo, plus the Maven built executables in /target folders.
#   | 
#   |  Source is also included for mark59-scripting-samples and mark59-scripting-sample-dsl projects. 
#   |  The target folder is excluded for mark59-scripting-sample-dsl projects - so it contains source only.
#   |  Selenium webdrivers are excluded from mark59-scripting-samples - to be added back for the Quick Start.      
#   |  
#   |  mark59-core, mark59-datahunter-api, mark59-scripting are excluded as they are accessed via Central
#   |  (or souce obtained from the Mark59 git repo).
#   |   
#   --------------------------------------------------------------------------------------------------------------
# SOURCE_DIR=~/gitrepo/mark-5-9/mark59-wip/
# DEST_DIR=~/mark59-x.y"

SOURCE_DIR=~/gitrepo/mark59-wip/
DEST_DIR=~/mark59-6.4/ 

rm -rf ${DEST_DIR}
mkdir -p ${DEST_DIR}
cd ~/gitrepo/mark59-wip/

## All required projects except the sample projects (where you want source code copied - they are copied by the next rsync command) 

rsync -av -m --exclude '.*' --exclude '*.yml' --exclude '*/src' --exclude '*/webapp' --exclude '*/test' --exclude '*/WEB-INF' --exclude '*/classes' --exclude '*/test-classes' --exclude 'TESTDATA' --exclude '*/maven*' --exclude 'archive-tmp' --exclude '*/m2e-wtp*' --exclude '*/surefire*' --exclude '*/bin'  --exclude '*.log' --exclude '*.png' --exclude '*.gif' --exclude '*.original' --exclude '*.java' --exclude 'dockerfile' --exclude 'UtilityCreateMark59Zip.*' --exclude 'pom.xml'  --exclude 'mark59-datahunter.jar' --exclude 'mark59-metrics.jar' --exclude 'mark59-trends.jar' --exclude 'mark59-scripting-samples' --exclude 'mark59-scripting-sample-dsl' --exclude 'mark59-core' --exclude 'mark59-datahunter-api' --exclude 'mark59-metrics-common' --exclude 'mark59-scripting' "${SOURCE_DIR}" "${DEST_DIR}"

## This rsync allows source code and arfefacts to be copied. Used for the sample projects mark59-scripting-samples and mark59-scripting-sample-dsl (source only) projects. 

rsync -a -m --exclude '.*' --exclude '*.yml' --exclude '*/classes' --exclude '*/test-classes' --exclude '*/maven*' --exclude 'archive-tmp' --exclude '*/m2e-wtp*' --exclude '*/surefire*' --exclude '*.log' --exclude '*.original' --exclude '/pom.xml' --exclude 'bin' --exclude 'databaseScripts'  --exclude 'mark59-scripting-sample-dsl/target' --exclude 'mark59-core' --exclude 'mark59-datahunter' --exclude 'mark59-datahunter-api' --exclude 'mark59-metrics' --exclude 'mark59-metrics-api' --exclude 'mark59-metrics-common' --exclude 'mark59-results-splitter' --exclude 'mark59-scripting' --exclude 'mark59-trends' --exclude 'mark59-trends-load' --exclude 'chromedriver*'  "${SOURCE_DIR}" "${DEST_DIR}"

$SHELL
