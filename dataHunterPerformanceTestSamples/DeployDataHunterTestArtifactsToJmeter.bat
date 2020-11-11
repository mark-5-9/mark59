REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  Use this file to copy Maven build artifacts from target folders in dataHunterPerformanceTestSamples and mark59-server-metrics projects,
REM   |  into a JMeter instance at C:\apache-jmeter  
REM   | 
REM   |  Note that mark59serverprofiles.xlsx is NOT copied. To run the '..usingExcel' testplan, copy it manually to the JMeter bin directory.
REM   | 
REM   |  Sample Usage.
REM   |  ------------
REM   |  Assumes your target Jmeter instance is at C:\apache-jmeter
REM   | 
REM   |  -  open up a Dos command prompt and cd to the directory holding this bat file. 
REM   |  -  to execute type:  DeployDataHunterTestArtifactsToJmeter.bat                       ( or just double-click on this file ) 
REM   |  
REM   -------------------------------------------------------------------------------------------------------------------------------------------------

MODE con:cols=180 lines=60

DEL C:\apache-jmeter\bin\mark59.properties
DEL C:\apache-jmeter\bin\chromedriver.exe
DEL C:\apache-jmeter\lib\ext\dataHunterPerformanceTestSamples.jar
DEL C:\apache-jmeter\lib\ext\mark59-server-metrics.jar

COPY .\mark59.properties C:\apache-jmeter\bin
COPY .\chromedriver.exe  C:\apache-jmeter\bin
COPY .\target\dataHunterPerformanceTestSamples.jar  C:\apache-jmeter\lib\ext
COPY ..\mark59-server-metrics\target\mark59-server-metrics.jar  C:\apache-jmeter\lib\ext

PAUSE