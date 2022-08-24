REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   | Deploy Artifacts and Run JMeter DataHunter Selenium Test.
REM   |
REM   | NOTE - you may need to ensure the chromedriver.exe file at root of mark59-datahunter-samples project is compatible with your Chrome version
REM   |        (see Mark59 user guide for details).  
REM   |      - mark59serverprofiles.xlsx is not copied. Before you run the '..usingExcel' testplan, copy it manually to the JMeter bin directory
REM   |        (not necessary before running this bat file as it runs the DataHunterSeleniumTestPlan which doesn't use the spreadsheet).
REM   |
REM   |  An instance of JMeter is expected at C:\apache-jmeter
REM   |
REM   |  Alternative to running this .bat 
REM   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-metrics" 
REM   |		 - run the DemoWIN-DataHunter-Selenium-DeployAndExecute profile. 
REM   |
REM   |  The only database considerations are that when the test is running server metrics are obtained invoking the 'localhost_WINDOWS' profile in the running
REM   |      server-metrics-web application (ie via whatever DB it is connected to), and also the datahunter application will be writing to its database.
REM   |
REM   |  logging at  C:\apache-jmeter\bin\jmeter.log
REM   |  JMeter result file output to C:\Mark59_Runs\Jmeter_Results\DataHunter
REM   |
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

rem use SET "StartCdpListeners=true" to allow the cdp listeners in the test script to execute 
SET "StartCdpListeners=false"
rem SET "StartCdpListeners=true"

REM copied from DeployDataHunterTestArtifactsToJmeter.bat in the mark59-datahunter-samples project ..
cd  ..\mark59-datahunter-samples   
DEL C:\apache-jmeter\bin\mark59.properties
DEL C:\apache-jmeter\bin\chromedriver.exe
DEL C:\apache-jmeter\lib\ext\mark59-metrics-api.jar
DEL C:\apache-jmeter\lib\ext\mark59-datahunter-samples.jar

RMDIR /S /Q "C:\apache-jmeter\lib\ext\mark59-datahunter-samples-dependencies"
MKDIR "C:\apache-jmeter\lib\ext\mark59-datahunter-samples-dependencies"

COPY .\mark59.properties C:\apache-jmeter\bin
COPY .\chromedriver.exe  C:\apache-jmeter\bin
COPY ..\mark59-metrics-api\target\mark59-metrics-api.jar  C:\apache-jmeter\lib\ext
COPY .\target\mark59-datahunter-samples.jar  C:\apache-jmeter\lib\ext
COPY .\target\mark59-datahunter-samples-dependencies  C:\apache-jmeter\lib\ext\mark59-datahunter-samples-dependencies

REM PAUSE
REM execute JMeter in non-GUI mode ..

CALL DataHunterExecuteJmeterTest.bat

PAUSE