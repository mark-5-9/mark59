REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   |  Use this file to copy Maven build artifacts from target folders in mark59-scripting-samples and mark59-metrics projects,
REM   |  into a JMeter instance at C:\apache-jmeter  
REM   | 
REM   |  Note that mark59serverprofiles.xlsx is NOT copied. To run the '..usingExcel' testplan, copy it manually to the JMeter bin directory.
REM   | 
REM   |  If you are only interesting in UI scripts using Playwright (and not Selenium), doesnt matter if the chromedriver is missing.
REM   | 
REM   |  Sample Usage.
REM   |  ------------
REM   |  Assumes your target Jmeter instance is at C:\apache-jmeter
REM   | 
REM   |  -  open up a Dos command prompt and cd to the directory holding this bat file. 
REM   |  -  to execute type:  DeployDataHunterTestArtifactsToJmeter.bat                       ( or just double-click on this file ) 
REM   |  
REM   -------------------------------------------------------------------------------------------------------------------------------------------------

MODE con:cols=180 lines=120

cd  ..\mark59-scripting-samples   
DEL C:\apache-jmeter\bin\mark59.properties
DEL C:\apache-jmeter\bin\chromedriver.exe
DEL C:\apache-jmeter\lib\ext\mark59-metrics-api.jar
DEL C:\apache-jmeter\lib\ext\mark59-scripting-samples.jar

RMDIR /S /Q "C:\apache-jmeter\lib\ext\mark59-scripting-samples-dependencies"
MKDIR "C:\apache-jmeter\lib\ext\mark59-scripting-samples-dependencies"

COPY .\mark59.properties C:\apache-jmeter\bin
COPY .\chromedriver.exe  C:\apache-jmeter\bin
COPY ..\mark59-metrics-api\target\mark59-metrics-api.jar  C:\apache-jmeter\lib\ext
COPY .\target\mark59-scripting-samples.jar  C:\apache-jmeter\lib\ext
COPY .\target\mark59-scripting-samples-dependencies  C:\apache-jmeter\lib\ext\mark59-scripting-samples-dependencies

PAUSE