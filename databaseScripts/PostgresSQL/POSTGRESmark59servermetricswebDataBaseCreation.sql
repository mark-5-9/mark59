
-- >  comment/uncomment as required
 
CREATE USER admin SUPERUSER PASSWORD 'admin';

-- DROP DATABASE mark59servermetricswebdb;
CREATE DATABASE mark59servermetricswebdb WITH ENCODING='UTF8' OWNER=admin TEMPLATE=template0 LC_COLLATE='C' LC_CTYPE='C';
 
DROP TABLE IF EXISTS  SERVERPROFILES;
DROP TABLE IF EXISTS  COMMANDS;
DROP TABLE IF EXISTS  SERVERCOMMANDLINKS;
DROP TABLE IF EXISTS  COMMANDRESPONSEPARSERS;
DROP TABLE IF EXISTS  COMMANDPARSERLINKS;

-- <

--   The utf8/C ecoding/collation is more in line with other mark59 database options (and how Java/JS sorts work). 
--   if you use the pgAdmin tool to load data, remember to hit the 'commit' icon to save the changes! 


CREATE TABLE IF NOT EXISTS SERVERPROFILES  (
   SERVER_PROFILE_NAME  varchar(64) NOT NULL,
   SERVER  varchar(64) NOT NULL,
   ALTERNATE_SERVER_ID  varchar(64) DEFAULT '',
   USERNAME  varchar(64) DEFAULT '',
   PASSWORD  varchar(64) DEFAULT '',
   PASSWORD_CIPHER  varchar(64) DEFAULT '',
   OPERATING_SYSTEM  varchar(8) NOT NULL,
   CONNECTION_PORT  varchar(8) DEFAULT '',
   CONNECTION_TIMEOUT  varchar(8) DEFAULT '',
   COMMENT  varchar(128) DEFAULT NULL,
  PRIMARY KEY ( SERVER_PROFILE_NAME )
); 


CREATE TABLE IF NOT EXISTS COMMANDS  (
   COMMAND_NAME  varchar(64) NOT NULL,
   EXECUTOR  varchar(32) NOT NULL,
   COMMAND  varchar(4096) NOT NULL,
   IGNORE_STDERR  varchar(1) DEFAULT NULL,
   COMMENT  varchar(128) DEFAULT NULL,
  PRIMARY KEY ( COMMAND_NAME )
); 


CREATE TABLE IF NOT EXISTS SERVERCOMMANDLINKS  (
   SERVER_PROFILE_NAME  varchar(64) NOT NULL,
   COMMAND_NAME  varchar(64) NOT NULL,
  PRIMARY KEY ( SERVER_PROFILE_NAME , COMMAND_NAME )
);


CREATE TABLE IF NOT EXISTS COMMANDRESPONSEPARSERS  (
   SCRIPT_NAME  varchar(64) NOT NULL,
   METRIC_TXN_TYPE  varchar(64) NOT NULL,
   METRIC_NAME_SUFFIX  varchar(64) NOT NULL,
   SCRIPT  varchar(4096) NOT NULL,
   COMMENT  varchar(1024) NOT NULL,
   SAMPLE_COMMAND_RESPONSE  varchar(1024) NOT NULL,
  PRIMARY KEY ( SCRIPT_NAME )
); 


CREATE TABLE IF NOT EXISTS COMMANDPARSERLINKS  (
   COMMAND_NAME  varchar(64) NOT NULL,
   SCRIPT_NAME  varchar(64) NOT NULL,
  PRIMARY KEY ( COMMAND_NAME , SCRIPT_NAME )
); 


INSERT INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterSeleniumDeployAndExecute','localhost','','','','','LINUX','22','60000','');
INSERT INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterSeleniumGenJmeterReport','localhost','','','','','LINUX','22','60000','Reports generated at   ~/Mark59_Runs/Jmeter_Reports/DataHunter/   <br>(open each index.html)   ');
INSERT INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterSeleniumRunCheck','localhost','','','','','LINUX','22','60000','Loads Trend Analysis (H2 database).  See:<br>http://localhost:8080/metrics/trending?reqApp=DataHunter');
INSERT INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumDeployAndExecute','localhost','','','','','WINDOWS','','','');
INSERT INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumGenJmeterReport','localhost','','','','','WINDOWS','','','Hint - in browser open this URL and go to each index.html:  file:///C:/Mark59_Runs/Jmeter_Reports/DataHunter/');
INSERT INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumRunCheck','localhost','','','','','WINDOWS','','','Loads Trend Analysis (H2 database).  See:<br>http://localhost:8080/metrics/trending?reqApp=DataHunter');
INSERT INTO SERVERPROFILES VALUES ('localhost_LINUX','localhost','','','','','LINUX','22','60000','');
INSERT INTO SERVERPROFILES VALUES ('localhost_WINDOWS','localhost','','','','','WINDOWS','','','');
INSERT INTO SERVERPROFILES VALUES ('localhost_WINDOWS_HOSTID','localhost','HOSTID','','','','WINDOWS','','','HOSTID will be subed <br> with computername  ');
INSERT INTO SERVERPROFILES VALUES ('remoteLinuxServer','LinuxServerName','','userid','encryptMe','','LINUX','22','60000','');
INSERT INTO SERVERPROFILES VALUES ('remoteUnixVM','UnixVMName','','userid','encryptMe','','UNIX','22','60000','');
INSERT INTO SERVERPROFILES VALUES ('remoteWinServer','WinServerName','','userid','encryptMe','','WINDOWS','','','');


INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','WMIC_WINDOWS','process call create ''cmd.exe /c 
 echo Running Directly From Server Metrics Web (cmd DataHunterSeleniumDeployAndExecute) & 
 echo  SERVER_METRICS_WEB_BASE_DIR: %SERVER_METRICS_WEB_BASE_DIR% & 
 cd /D %SERVER_METRICS_WEB_BASE_DIR% &  
 cd ..\dataHunterPerformanceTestSamples & 
 DEL C:\apache-jmeter\bin\mark59.properties & COPY .\mark59.properties C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\bin\chromedriver.exe  & COPY .\chromedriver.exe  C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\lib\ext\mark59-server-metrics.jar &
 COPY ..\mark59-server-metrics\target\mark59-server-metrics.jar  C:\apache-jmeter\lib\ext & 
 DEL C:\apache-jmeter\lib\ext\dataHunterPerformanceTestSamples.jar & 
 COPY .\target\dataHunterPerformanceTestSamples.jar  C:\apache-jmeter\lib\ext & 

 mkdir C:\Mark59_Runs &
 mkdir C:\Mark59_Runs\Jmeter_Results &
 mkdir C:\Mark59_Runs\Jmeter_Results\DataHunter &

 set path=%path%;C:\Windows\System32;C:\windows\system32\wbem & 
 cd /D C:\apache-jmeter\bin &

 echo Starting JMeter DataHunter test ... &  

 jmeter -n -X -f 
     -t %SERVER_METRICS_WEB_BASE_DIR%\..\dataHunterPerformanceTestSamples\test-plans\DataHunterSeleniumTestPlan.jmx 
     -l C:\Mark59_Runs\Jmeter_Results\DataHunter\DataHunterTestResults.csv 
     -JForceTxnFailPercent=0 
     -JDataHunterUrlHostPort=http://localhost:8081 &  

 PAUSE
''
','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in dataHunterPerformanceTestSamples ');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','SSH_LINIX_UNIX','echo This script runs the JMeter deploy in the background, then opens a terminal for JMeter execution.
echo starting from $PWD;

{   # try  

    cd ../dataHunterPerformanceTestSamples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo dataHunterPerformanceTestSamples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-server-metrics/target/mark59-server-metrics.jar  ~/apache-jmeter/lib/ext/mark59-server-metrics.jar && 
    cp ./target/dataHunterPerformanceTestSamples.jar  ~/apache-jmeter/lib/ext/dataHunterPerformanceTestSamples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter && 
 
    gnome-terminal -- sh -c "~/apache-jmeter/bin/jmeter -n -X -f  -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0; exec bash"

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport','WMIC_WINDOWS','process call create ''cmd.exe /c 
 cd /D %SERVER_METRICS_WEB_BASE_DIR% & 
 cd../resultFilesConverter & 
 CreateDataHunterJmeterReports.bat''
','N','');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport_LINUX','SSH_LINIX_UNIX','echo This script creates a set of JMeter reports from a DataHunter test run.
echo starting from $PWD;

{   # try  

    cd ../resultFilesConverter
    gnome-terminal -- sh -c "./CreateDataHunterJmeterReports.sh; exec bash"

} || { # catch 
    echo attempt to generate JMeter Reports has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Selenium-GenJmeterReport.sh');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumRunCheck','WMIC_WINDOWS','process call create ''cmd.exe /c 
 echo Load DataHunter Test Results into  Mark59 Metrics (Trend Analysis) h2 database. & 
 cd /D  %SERVER_METRICS_WEB_BASE_DIR% & 
 cd ../metricsRuncheck &  
 
 java -jar ./target/metricsRuncheck.jar -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d h2 &
 PAUSE
''
','N','');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumRunCheck_LINUX','SSH_LINIX_UNIX','echo This script runs metricsRuncheck,to load results from a DataHunter test run into the Metrics Trend Analysis Graph.
echo starting from $PWD;

{   # try  

    cd ../metricsRuncheck/target &&
    gnome-terminal -- sh -c "java -jar metricsRuncheck.jar -a DataHunter -i ~/Mark59_Runs/Jmeter_Results/DataHunter -d h2; exec bash"

} || { # catch 
    echo attempt to execute metricsRuncheck has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Selenium-metricsRunCheck.sh');
INSERT INTO COMMANDS VALUES ('FreePhysicalMemory','WMIC_WINDOWS','OS get FreePhysicalMemory','N','');
INSERT INTO COMMANDS VALUES ('FreeVirtualMemory','WMIC_WINDOWS','OS get FreeVirtualMemory','N','');
INSERT INTO COMMANDS VALUES ('LINUX_free_m_1_1','SSH_LINIX_UNIX','free -m 1 1','N','linux memory');
INSERT INTO COMMANDS VALUES ('LINUX_mpstat_1_1','SSH_LINIX_UNIX','mpstat 1 1','N','');
INSERT INTO COMMANDS VALUES ('UNIX_lparstat_5_1','SSH_LINIX_UNIX','lparstat 5 1','N','');
INSERT INTO COMMANDS VALUES ('UNIX_Memory_Script','SSH_LINIX_UNIX','vmstat=$(vmstat -v); 
let total_pages=$(print "$vmstat" | grep ''memory pages'' | awk ''{print $1}''); 
let pinned_pages=$(print "$vmstat" | grep ''pinned pages'' | awk ''{print $1}''); 
let pinned_percent=$(( $(print "scale=4; $pinned_pages / $total_pages " | bc) * 100 )); 
let numperm_pages=$(print "$vmstat" | grep ''file pages'' | awk ''{print $1}''); 
let numperm_percent=$(print "$vmstat" | grep ''numperm percentage'' | awk ''{print $1}''); 
pgsp_utils=$(lsps -a | tail +2 | awk ''{print $5}''); 
let pgsp_num=$(print "$pgsp_utils" | wc -l | tr -d '' ''); 
let pgsp_util_sum=0; 
for pgsp_util in $pgsp_utils; do let pgsp_util_sum=$(( $pgsp_util_sum + $pgsp_util )); done; 
pgsp_aggregate_util=$(( $pgsp_util_sum / $pgsp_num )); 
print "${pinned_percent},${numperm_percent},${pgsp_aggregate_util}"','N','');
INSERT INTO COMMANDS VALUES ('UNIX_VM_Memory','SSH_LINIX_UNIX','vmstat=$(vmstat -v); 
let total_pages=$(print "$vmstat" | grep ''memory pages'' | awk ''{print $1}''); 
let pinned_pages=$(print "$vmstat" | grep ''pinned pages'' | awk ''{print $1}''); 
let pinned_percent=$(( $(print "scale=4; $pinned_pages / $total_pages " | bc) * 100 )); 
let numperm_pages=$(print "$vmstat" | grep ''file pages'' | awk ''{print $1}''); 
let numperm_percent=$(print "$vmstat" | grep ''numperm percentage'' | awk ''{print $1}''); 
pgsp_utils=$(lsps -a | tail +2 | awk ''{print $5}''); 
let pgsp_num=$(print "$pgsp_utils" | wc -l | tr -d '' ''); 
let pgsp_util_sum=0; 
for pgsp_util in $pgsp_utils; do let pgsp_util_sum=$(( $pgsp_util_sum + $pgsp_util )); done; 
pgsp_aggregate_util=$(( $pgsp_util_sum / $pgsp_num )); 
print "${pinned_percent},${numperm_percent},${pgsp_aggregate_util}"','N','');
INSERT INTO COMMANDS VALUES ('WinCpuCmd','WMIC_WINDOWS','cpu get loadpercentage','N','');


INSERT INTO COMMANDRESPONSEPARSERS VALUES ('LINUX_Memory_freeG','MEMORY','freeG','import org.apache.commons.lang3.StringUtils;
// ---
String targetColumnName= "free";              
String targetRowName= "Mem:";  
// ---
String extractedMetric = "-3";

if (StringUtils.isNotBlank(commandResponse)) {
    String wordsOnThisResultLine = commandResponse.replace("\\n", " ").replace("\\r", " ");
    ArrayList<String> cmdResultLine = new ArrayList<>(
         Arrays.asList(wordsOnThisResultLine.trim().split("\\s+")));

    if (cmdResultLine.contains(targetColumnName)) {
	extractedMetric = cmdResultLine
		.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);
    }
}
return Math.round(Double.parseDouble(extractedMetric) / 1000 );
','','              total        used        free      shared  buff/cache   available
Mem:          28798       14043         561        1412       14392       12953
Swap:             0           0           0
');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('LINUX_Memory_totalG','MEMORY','totalG','import org.apache.commons.lang3.StringUtils;
// ---
String targetColumnName= "total";              
String targetRowName= "Mem:";  
// ---
String extractedMetric = "-3";

if (StringUtils.isNotBlank(commandResponse)) {
    String wordsOnThisResultLine = commandResponse.replace("\\n", " ").replace("\\r", " ");
    ArrayList<String> cmdResultLine = new ArrayList<>(
         Arrays.asList(wordsOnThisResultLine.trim().split("\\s+")));

    if (cmdResultLine.contains(targetColumnName)) {
	extractedMetric = cmdResultLine
		.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);
    }
}
return Math.round(Double.parseDouble(extractedMetric) / 1000 );
','','              total        used        free      shared  buff/cache   available
Mem:          28798       14043         361        1412       14392       12953
Swap:             0           0           0
');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('LINUX_Memory_usedG','MEMORY','usedG','import org.apache.commons.lang3.StringUtils;
// ---
String targetColumnName= "used";              
String targetRowName= "Mem:";  
// ---
String extractedMetric = "-3";

if (StringUtils.isNotBlank(commandResponse)) {
    String wordsOnThisResultLine = commandResponse.replace("\\n", " ").replace("\\r", " ");
    ArrayList<String> cmdResultLine = new ArrayList<>(
         Arrays.asList(wordsOnThisResultLine.trim().split("\\s+")));

    if (cmdResultLine.contains(targetColumnName)) {
	extractedMetric = cmdResultLine
		.get(cmdResultLine.indexOf(targetRowName) + cmdResultLine.indexOf(targetColumnName) + 1);
    }
}
return Math.round(Double.parseDouble(extractedMetric) / 1000 );
','','              total        used        free      shared  buff/cache   available
Mem:          28798       14043         361        1412       14392       12953
Swap:             0           0           0
');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreePhysicalG','MEMORY','FreePhysicalG','Math.round(Double.parseDouble(commandResponse.replaceAll("[^\\d.]", "")) / 1000000 )','','FreePhysicalG
22510400');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreeVirtualG','MEMORY','FreeVirtualG','Math.round(Double.parseDouble(commandResponse.replaceAll("[^\\d.]", "")) / 1000000 )','','FreeVirtualMemory
22510400');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Nix_CPU_Idle','CPU_UTIL','IDLE','import org.apache.commons.lang3.ArrayUtils;
// ---
String targetColumnName = "%idle"              
String targetmetricFormat = "\\d*\\.?\\d+"   // a decimal format  
// ---
String extractedMetric = "-1";
int colNumberOfTargetColumnName = -1;
String[] commandResultLine = commandResponse.trim().split("\\r\\n|\\n|\\r");

for (int i = 0; i < commandResultLine.length && "-1".equals(extractedMetric); i++) {

    String[] wordsOnThiscommandResultsLine = commandResultLine[i].trim().split("\\s+");

    if (colNumberOfTargetColumnName > -1
  	&& wordsOnThiscommandResultsLine[colNumberOfTargetColumnName].matches(targetmetricFormat)) {
	extractedMetric = wordsOnThiscommandResultsLine[colNumberOfTargetColumnName];
    }
    if (colNumberOfTargetColumnName == -1) { // column name not yet found, so see if it is on this line ...
	colNumberOfTargetColumnName = ArrayUtils.indexOf(wordsOnThiscommandResultsLine, targetColumnName);
    }
}
return extractedMetric;
','This works on data in a simple column format (eg unix lparstat and linux mpstat cpu). It will return the first matching value it finds in the column requested.','System configuration: type=Shared mode=Uncapped smt=4 lcpu=4 mem=47104MB psize=60 ent=0.50 

%user  %sys  %wait  %idle physc %entc  lbusy   app  vcsw phint  %nsp  %utcyc
----- ----- ------ ------ ----- ----- ------   --- ----- ----- -----  ------
 11.3  15.0    0.0   73.7  0.22  44.5    6.1 45.26   919     0   101   1.39 ');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Nix_CPU_UTIL','CPU_UTIL','','import org.apache.commons.lang3.ArrayUtils;
import java.math.RoundingMode;
// 
String targetColumnName = "%idle";
String targetmetricFormat = "\\d*\\.?\\d+"; // a decimal format
// 
String notFound = "-1";
String extractedMetric = notFound;
int colNumberOfTargetColumnName = -1;
String[] commandResultLine = commandResponse.trim().split("\\r\\n|\\n|\\r");

for (int i = 0; i < commandResultLine.length && notFound.equals(extractedMetric); i++) {

	String[] wordsOnThiscommandResultsLine = commandResultLine[i].trim().split("\\s+");

	if (colNumberOfTargetColumnName > -1
			&& wordsOnThiscommandResultsLine[colNumberOfTargetColumnName].matches(targetmetricFormat)) {
		extractedMetric = wordsOnThiscommandResultsLine[colNumberOfTargetColumnName];
	}
	if (colNumberOfTargetColumnName == -1) { // column name not yet found, so see if it is on this line ...
		colNumberOfTargetColumnName = ArrayUtils.indexOf(wordsOnThiscommandResultsLine, targetColumnName);
	}
}

if (notFound.equals(extractedMetric))return notFound;
String cpuUtil = notFound;
try {  cpuUtil = new BigDecimal(100).subtract(new BigDecimal(extractedMetric)).setScale(1, RoundingMode.HALF_UP).toPlainString();} catch (Exception e) {}
return cpuUtil;','This works on data in a simple column format (eg unix lparstat and linux mpstat cpu). It will return the first matching value it finds in the column requested.','System configuration: type=Shared mode=Uncapped smt=4 lcpu=4 mem=47104MB psize=60 ent=0.50 

%user  %sys  %wait  %idle physc %entc  lbusy   app  vcsw phint  %nsp  %utcyc
----- ----- ------ ------ ----- ----- ------   --- ----- ----- -----  ------
 11.3  15.0    0.0   73.7  0.22  44.5    6.1 45.26   919     0   101   1.39 ');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Return1','DATAPOINT','','return 1','','any rand junk');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('UNIX_Memory_numperm_percent','MEMORY','numperm_percent','commandResponse.split(",")[1].trim()','','1,35,4');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('UNIX_Memory_pgsp_aggregate_util','MEMORY','pgsp_aggregate_util','commandResponse.split(",")[2].trim()','','1,35,4');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('UNIX_Memory_pinned_percent','MEMORY','pinned_percent','commandResponse.split(",")[0].trim()','','1,35,4');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('WicnCpu','CPU_UTIL','','java.util.regex.Matcher m = java.util.regex.Pattern.compile("-?[0-9]+").matcher(commandResponse);
Integer sum = 0; 
int count = 0; 
while (m.find()){ 
    sum += Integer.parseInt(m.group()); 
    count++;
}; 
if (count==0) 
    return 0 ; 
else 
    return sum/count;','comment','LoadPercentage
21');


INSERT INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterSeleniumDeployAndExecute','DataHunterSeleniumDeployAndExecute_LINUX');
INSERT INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterSeleniumGenJmeterReport','DataHunterSeleniumGenJmeterReport_LINUX');
INSERT INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterSeleniumRunCheck','DataHunterSeleniumRunCheck_LINUX');
INSERT INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterSeleniumDeployAndExecute','DataHunterSeleniumDeployAndExecute');
INSERT INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterSeleniumGenJmeterReport','DataHunterSeleniumGenJmeterReport');
INSERT INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterSeleniumRunCheck','DataHunterSeleniumRunCheck');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_LINUX','LINUX_free_m_1_1');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_LINUX','LINUX_mpstat_1_1');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS','FreePhysicalMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS','FreeVirtualMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS','WinCpuCmd');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','FreePhysicalMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','FreeVirtualMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WinCpuCmd');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteLinuxServer','LINUX_free_m_1_1');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteLinuxServer','LINUX_mpstat_1_1');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteUnixVM','UNIX_lparstat_5_1');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteUnixVM','UNIX_Memory_Script');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer','FreePhysicalMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer','FreeVirtualMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer','WinCpuCmd');


INSERT INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumDeployAndExecute','Return1');
INSERT INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','Return1');
INSERT INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumGenJmeterReport','Return1');
INSERT INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumGenJmeterReport_LINUX','Return1');
INSERT INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumRunCheck','Return1');
INSERT INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumRunCheck_LINUX','Return1');
INSERT INTO COMMANDPARSERLINKS VALUES ('FreePhysicalMemory','Memory_FreePhysicalG');
INSERT INTO COMMANDPARSERLINKS VALUES ('FreeVirtualMemory','Memory_FreeVirtualG');
INSERT INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1','LINUX_Memory_freeG');
INSERT INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1','LINUX_Memory_totalG');
INSERT INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1','LINUX_Memory_usedG');
INSERT INTO COMMANDPARSERLINKS VALUES ('LINUX_mpstat_1_1','Nix_CPU_UTIL');
INSERT INTO COMMANDPARSERLINKS VALUES ('UNIX_lparstat_5_1','Nix_CPU_UTIL');
INSERT INTO COMMANDPARSERLINKS VALUES ('UNIX_Memory_Script','UNIX_Memory_numperm_percent');
INSERT INTO COMMANDPARSERLINKS VALUES ('UNIX_Memory_Script','UNIX_Memory_pgsp_aggregate_util');
INSERT INTO COMMANDPARSERLINKS VALUES ('UNIX_Memory_Script','UNIX_Memory_pinned_percent');
INSERT INTO COMMANDPARSERLINKS VALUES ('UNIX_VM_Memory','UNIX_Memory_numperm_percent');
INSERT INTO COMMANDPARSERLINKS VALUES ('UNIX_VM_Memory','UNIX_Memory_pgsp_aggregate_util');
INSERT INTO COMMANDPARSERLINKS VALUES ('UNIX_VM_Memory','UNIX_Memory_pinned_percent');
INSERT INTO COMMANDPARSERLINKS VALUES ('WinCpuCmd','WicnCpu');
