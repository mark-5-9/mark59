REM   -------------------------------------------------------------------------------------------------------------------------------------------------
REM   | Generate the JMeter reports for a DataHunter Selenium Test.
REM   |
REM   |  Alternative to running this .bat 
REM   |		 - login  to the server-metrics-web application  "http://localhost:8085/mark59-metrics" 
REM   |		 - run the DemoWIN-DataHunter-Selenium-GenJmeterReport profile. 
REM   |
REM   |  There are are no database considerations when running JMeter report generation.
REM   |
REM   |  JMeter input results file expected at C:\Mark59_Runs\Jmeter_Results\DataHunter 
REM   |
REM   |  Output generated to C:\Mark59_Runs\Jmeter_Reports\DataHunter 
REM   |
REM   -------------------------------------------------------------------------------------------------------------------------------------------------
CD /D "%~dp0"

cd ../mark59-results-splitter
CALL CreateDataHunterJmeterReports.bat
		