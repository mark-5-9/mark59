#!/bin/sh
#   --------------------------------------------------------------------------------------------------------------
#   |  An adminstration tool to create the file structure for inclusion in the download Mark59 zip file. 
#   | 
#   |  Files are derived from source git repo plus the built executables in target folders --exclude  and any other artefacts
#   |  required for execution (eg bat and shell files).
#   | 
#   |  Source is also included for dataHunterPerformanceTestSamples and mark59-selenium-sample-dsl projects --exclude 
#   |  as they are the Selenium projects to be used as samples for scripting (I gave up on the bizarre curly bracket expansion).  
#   |   
#   |  When a new version of mark59-core and mark59-selenium-implementation is being used that is yet to be uploaded to     
#   |  Maven central those maven projects must also be incldued.   
#   --------------------------------------------------------------------------------------------------------------
# SOURCE_DIR=~/gitrepo/mark-5-9/mark59-wip/
# DEST_DIR=~/mark59-3.0_PRE_RELEASE"

SOURCE_DIR=~/gitrepo/mark59-wip/
DEST_DIR=~/mark59-3.0_PRE_RELEASE/ 

rm -rf ${DEST_DIR}
mkdir -p ${DEST_DIR}
cd ~/gitrepo/mark59-wip/

rsync -av -m --exclude '.*' --exclude '*/src' --exclude '*/webapp' --exclude '*/test' --exclude '*/WEB-INF' --exclude '*/classes' --exclude '*/test-classes' --exclude 'TESTDATA' --exclude '*/maven*' --exclude '*/m2e-wtp*' --exclude '*/surefire*' --exclude '*.log' --exclude '*.png' --exclude '*.gif' --exclude '*.original' --exclude '*.java' --exclude 'UtilityCreateMark59Zip.*' --exclude 'pom.xml' --exclude 'mark59-server-metrics-web.jar' --exclude 'metrics.jar' --exclude 'dataHunterPerformanceTestSamples' --exclude 'mark59-selenium-sample-dsl' --exclude 'mark59-core' --exclude 'mark59-selenium-implementation' "${SOURCE_DIR}" "${DEST_DIR}"

# add/remove "--exclude 'mark59-core' --exclude 'mark59-selenium-implementation'" for full releases (versions have been added in Maven Central) / remove when versions not in Maven Central  

rsync -a -m --exclude '.*' --exclude '*/classes' --exclude '*/test-classes' --exclude '*/maven*' --exclude '*/m2e-wtp*' --exclude '*/surefire*' --exclude '*.log' --exclude '*.original' --exclude '/pom.xml' --exclude 'bin' --exclude 'databaseScripts' --exclude 'dataHunter' --exclude 'mark59-server-metrics' --exclude 'mark59-server-metrics-web' --exclude 'metrics' --exclude 'metricsRuncheck' --exclude 'resultFilesConverter' "${SOURCE_DIR}" "${DEST_DIR}"

$SHELL
