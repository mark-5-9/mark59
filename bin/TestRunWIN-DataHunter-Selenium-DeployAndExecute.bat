REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   | Deploy Artifacts and Run JMeter DataHunter Selenium Test.
REM   |
REM   | NOTE - you may need to ensure the chromedriver.exe file at root of dataHunterPerformanceTestSamples project is compatible with your Chrome version
REM   |        (see Mark59 user guide for details).  
REM   |      - mark59serverprofiles.xlsx is not copied. Before you run the '..usingExcel' testplan, copy it manually to the JMeter bin directory
REM   |        (not necessary before running this bat file as it runs the DataHunterSeleniumTestPlan which doesn't use the spreadsheet).
REM   |
REM   |  An instance of JMeter is expected at C:\apache-jmeter
REM   |
REM   |  Alternative to running this .bat 
REM   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-server-metrics-web" 
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

REM copied from DeployDataHunterTestArtifactsToJmeter.bat in the dataHunterPerformanceTestSamples project ..

cd  ..\dataHunterPerformanceTestSamples   

DEL C:\apache-jmeter\bin\mark59.properties
DEL C:\apache-jmeter\bin\chromedriver.exe
DEL C:\apache-jmeter\lib\ext\dataHunterPerformanceTestSamples.jar
DEL C:\apache-jmeter\lib\ext\mark59-server-metrics.jar

COPY .\mark59.properties C:\apache-jmeter\bin
COPY .\chromedriver.exe  C:\apache-jmeter\bin
COPY .\target\dataHunterPerformanceTestSamples.jar  C:\apache-jmeter\lib\ext
COPY ..\mark59-server-metrics\target\mark59-server-metrics.jar  C:\apache-jmeter\lib\ext

REM PAUSE
REM execute JMeter in non-GUI mode ..

CALL DataHunterExecuteJmeterTest.bat

PAUSE
