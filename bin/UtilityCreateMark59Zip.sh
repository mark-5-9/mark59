#!/bin/sh
#   --------------------------------------------------------------------------------------------------------------
#   |  An adminstration tool to create the file structure for inclusion in the download Mark59 zip file. 
#   | 
#   |  Files are derived from source git repo plus the built executables in target folders, and any other artefacts
#   |  required for execution (eg bat and shell files).
#   | 
#   |  Source is also included for mark59-datahunter-samples and mark59-dsl-samples projects (the  . 
#   |  target folder is excluded for mark59-dsl-samples projects - so it contains source only)     
#   |  
#   |  mark59-core, mark59-datahunter-api, mark59-selenium-implementation are excluded as they are accessed
#   |  or directly downloaded via Maven Central.
#   |   
#   --------------------------------------------------------------------------------------------------------------
# SOURCE_DIR=~/gitrepo/mark-5-9/mark59-wip/
# DEST_DIR=~/mark59-x.y"

SOURCE_DIR=~/gitrepo/mark59-wip/
DEST_DIR=~/mark59-5.6/ 

rm -rf ${DEST_DIR}
mkdir -p ${DEST_DIR}
cd ~/gitrepo/mark59-wip/

## All projects except the sample projects where you want source code copied 

rsync -av -m --exclude '.*' --exclude '*.yml' --exclude '*/src' --exclude '*/webapp' --exclude '*/test' --exclude '*/WEB-INF' --exclude '*/classes' --exclude '*/test-classes' --exclude 'TESTDATA' --exclude '*/maven*' --exclude 'archive-tmp' --exclude '*/m2e-wtp*' --exclude '*/surefire*' --exclude '*/bin'  --exclude '*.log' --exclude '*.png' --exclude '*.gif' --exclude '*.original' --exclude '*.java' --exclude 'dockerfile' --exclude 'UtilityCreateMark59Zip.*' --exclude 'pom.xml'  --exclude 'mark59-datahunter.jar' --exclude 'mark59-metrics.jar' --exclude 'mark59-trends.jar' --exclude 'mark59-datahunter-samples' --exclude 'mark59-dsl-samples' --exclude 'mark59-core' --exclude 'mark59-datahunter-api' --exclude 'mark59-metrics-common' --exclude 'mark59-selenium-implementation' "${SOURCE_DIR}" "${DEST_DIR}"

## This rsync allows source code and arfefacts to be copied. Used for the sample projects mark59-datahunter-samples and mark59-dsl-samples (source only) projects. 

rsync -a -m --exclude '.*' --exclude '*.yml' --exclude '*/classes' --exclude '*/test-classes' --exclude '*/maven*' --exclude 'archive-tmp' --exclude '*/m2e-wtp*' --exclude '*/surefire*' --exclude '*.log' --exclude '*.original' --exclude '/pom.xml' --exclude 'bin' --exclude 'databaseScripts'  --exclude 'mark59-dsl-samples/target' --exclude 'mark59-core' --exclude 'mark59-datahunter' --exclude 'mark59-datahunter-api' --exclude 'mark59-metrics' --exclude 'mark59-metrics-api' --exclude 'mark59-metrics-common' --exclude 'mark59-results-splitter' --exclude 'mark59-selenium-implementation' --exclude 'mark59-trends' --exclude 'mark59-trends-load' "${SOURCE_DIR}" "${DEST_DIR}"

$SHELL
