-- DROP TABLE IF EXISTS  SERVERPROFILES;
-- DROP TABLE IF EXISTS  COMMANDS;
-- DROP TABLE IF EXISTS  SERVERCOMMANDLINKS;
-- DROP TABLE IF EXISTS  COMMANDRESPONSEPARSERS;
-- DROP TABLE IF EXISTS  COMMANDPARSERLINKS;

CREATE TABLE IF NOT EXISTS SERVERPROFILES  (
   SERVER_PROFILE_NAME  varchar(64)   NOT NULL,
   EXECUTOR             varchar(32)   NOT NULL,  
   SERVER               varchar(64)   NULL DEFAULT '',
   ALTERNATE_SERVER_ID  varchar(64)   DEFAULT '',
   USERNAME             varchar(64)   DEFAULT '',
   PASSWORD             varchar(64)   DEFAULT '',
   PASSWORD_CIPHER      varchar(64)   DEFAULT '',
   CONNECTION_PORT      varchar(8)    DEFAULT '',
   CONNECTION_TIMEOUT   varchar(8)    DEFAULT '',
   COMMENT              varchar(128)  DEFAULT NULL,
   PARAMETERS           varchar(2000) NULL DEFAULT NULL,
  PRIMARY KEY ( SERVER_PROFILE_NAME )
); 


CREATE TABLE IF NOT EXISTS COMMANDS  (
   COMMAND_NAME   varchar(64)   NOT NULL,
   EXECUTOR       varchar(32)   NOT NULL,
   COMMAND        varchar(8192) NOT NULL,
   IGNORE_STDERR  varchar(1)    DEFAULT NULL,
   COMMENT        varchar(128)  DEFAULT NULL,
   PARAM_NAMES    varchar(1000) NULL DEFAULT NULL,
  PRIMARY KEY ( COMMAND_NAME )
); 


CREATE TABLE IF NOT EXISTS SERVERCOMMANDLINKS  (
   SERVER_PROFILE_NAME  varchar(64) NOT NULL,
   COMMAND_NAME         varchar(64) NOT NULL,
  PRIMARY KEY ( SERVER_PROFILE_NAME , COMMAND_NAME )
);


CREATE TABLE IF NOT EXISTS COMMANDRESPONSEPARSERS  (
   PARSER_NAME              varchar(64) NOT NULL,
   METRIC_TXN_TYPE          varchar(64) NOT NULL,
   METRIC_NAME_SUFFIX       varchar(64) NOT NULL,
   SCRIPT                   varchar(4096) NOT NULL,
   COMMENT                  varchar(1024) NOT NULL,
   SAMPLE_COMMAND_RESPONSE  varchar(1024) NOT NULL,
  PRIMARY KEY ( PARSER_NAME )
); 


CREATE TABLE IF NOT EXISTS COMMANDPARSERLINKS  (
   COMMAND_NAME  varchar(64) NOT NULL,
   PARSER_NAME   varchar(64) NOT NULL,
  PRIMARY KEY ( COMMAND_NAME , PARSER_NAME )
); 



INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterSeleniumDeployAndExecute','SSH_LINUX_UNIX','localhost','','','','','22','60000','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterSeleniumGenJmeterReport','SSH_LINUX_UNIX','localhost','','','','','22','60000','Reports generated at   ~/Mark59_Runs/Jmeter_Reports/DataHunter/   <br>(open each index.html)   ','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterSeleniumTrendsLoad','SSH_LINUX_UNIX','localhost','','','','','22','60000','Loads Trend Analysis (H2 database).  See:<br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoMAC-DataHunterSeleniumDeployAndExecute','SSH_LINUX_UNIX','localhost','','','','','22','60000','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoMAC-DataHunterSeleniumGenJmeterReport','SSH_LINUX_UNIX','localhost','','','','','22','60000','Reports generated at   ~/Mark59_Runs/Jmeter_Reports/DataHunter/   <br>(open each index.html)   ','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoMAC-DataHunterSeleniumTrendsLoad','SSH_LINUX_UNIX','localhost','','','','','22','60000','Loads Trend Analysis (H2 database).  See:<br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumDeployAndExecute','WMIC_WINDOWS','localhost','','','','','','','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumGenJmeterReport','WMIC_WINDOWS','localhost','','','','','','','Hint - in browser open this URL and go to each index.html:  file:///C:/Mark59_Runs/Jmeter_Reports/DataHunter/','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumTrendsLoad','WMIC_WINDOWS','localhost','','','','','','','Loads Trend Analysis (H2 database).  See:<br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_LINUX','SSH_LINUX_UNIX','localhost','','','','','22','60000','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_WINDOWS','WMIC_WINDOWS','localhost','','','','','','','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_WINDOWS_HOSTID','WMIC_WINDOWS','localhost','HOSTID','','','','','','HOSTID will be subed <br> with computername','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteLinuxServer','SSH_LINUX_UNIX','LinuxServerName','','userid','encryptMe','','22','60000','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteUnixVM','SSH_LINUX_UNIX','UnixVMName','','userid','encryptMe','','22','60000','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteWinServer','WMIC_WINDOWS','WinServerName','','userid','encryptMe','','','','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('SimpleScriptSampleRunner', 'GROOVY_SCRIPT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'runs a supplied, basic groovy script sample', '{"parm1":"11","parm2":"55.7","parm3":"333"}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('NewRelicSampleProfile', 'GROOVY_SCRIPT', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'supplied sample New Relic API groovy script', '{"proxyPort":"proxyPort","newRelicXapiKey":"newRelicXapiKey","proxyServer":"proxyServer","newRelicApiAppId":"newRelicApiAppId"}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_MAC','SSH_LINUX_UNIX','localhost','','','','','22','60000','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_MAC_HOSTID','SSH_LINUX_UNIX','localhost','HOSTID','','','','22','60000','HOSTID will be subed <br> with computername','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_LINUX_HOSTID','SSH_LINUX_UNIX','localhost','HOSTID','','','','22','60000','HOSTID will be subed <br> with computername','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','WMIC_WINDOWS','process call create ''cmd.exe /c 
 echo Running Directly From Server Metrics Web (cmd DataHunterSeleniumDeployAndExecute) & 
 echo  METRICS_BASE_DIR: %METRICS_BASE_DIR% & 
 cd /D %METRICS_BASE_DIR% &  
 cd ..\mark59-datahunter-samples & 
 DEL C:\apache-jmeter\bin\mark59.properties & COPY .\mark59.properties C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\bin\chromedriver.exe  & COPY .\chromedriver.exe  C:\apache-jmeter\bin &
 DEL C:\apache-jmeter\lib\ext\mark59-metrics-api.jar &
 COPY ..\mark59-metrics-api\target\mark59-metrics-api.jar  C:\apache-jmeter\lib\ext & 
 DEL C:\apache-jmeter\lib\ext\mark59-datahunter-samples.jar & 
 COPY .\target\mark59-datahunter-samples.jar  C:\apache-jmeter\lib\ext &
 RMDIR /S /Q C:\apache-jmeter\lib\ext\mark59-datahunter-samples-dependencies &
 MKDIR C:\apache-jmeter\lib\ext\mark59-datahunter-samples-dependencies &
 COPY .\target\mark59-datahunter-samples-dependencies  C:\apache-jmeter\lib\ext\mark59-datahunter-samples-dependencies &

 mkdir C:\Mark59_Runs &
 mkdir C:\Mark59_Runs\Jmeter_Results &
 mkdir C:\Mark59_Runs\Jmeter_Results\DataHunter &

 set path=%path%;C:\Windows\System32;C:\windows\system32\wbem & 
 cd /D C:\apache-jmeter\bin &

 echo Starting JMeter DataHunter test ... &  

 jmeter -n -X -f -t %METRICS_BASE_DIR%\..\mark59-datahunter-samples\test-plans\DataHunterSeleniumTestPlan.jmx -l C:\Mark59_Runs\Jmeter_Results\DataHunter\DataHunterTestResults.csv -JForceTxnFailPercent=0 -JDataHunterUrl=http://localhost:8081/mark59-datahunter -JStartCdpListeners=false &
 PAUSE
''
','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in mark59-datahunter-samples ','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','SSH_LINUX_UNIX','echo This script runs the JMeter deploy in the background, then opens a terminal for JMeter execution.
echo starting from $PWD;

{   # try  

    cd ../mark59-datahunter-samples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo mark59-datahunter-samples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-datahunter-samples.jar  ~/apache-jmeter/lib/ext/mark59-datahunter-samples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-datahunter-samples-dependencies &&
    cp -r ./target/mark59-datahunter-samples-dependencies ~/apache-jmeter/lib/ext/mark59-datahunter-samples-dependencies &&
 
    gnome-terminal -- sh -c "cd ~/apache-jmeter/bin; ~/apache-jmeter/bin/jmeter -n -X -f -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false; exec bash"

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_MAC','SSH_LINUX_UNIX','echo This script runs the JMeter deploy and exeution in the background, and displays output on completion.
echo starting from $PWD;

{   # try  

    cd ../mark59-datahunter-samples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo mark59-datahunter-samples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-datahunter-samples.jar  ~/apache-jmeter/lib/ext/mark59-datahunter-samples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-datahunter-samples-dependencies &&
    cp -r ./target/mark59-datahunter-samples-dependencies ~/apache-jmeter/lib/ext/mark59-datahunter-samples-dependencies &&
    cd ~/apache-jmeter/bin && 
    echo Starting JMeter execution from $PWD && 
    ~/apache-jmeter/bin/jmeter -n -X -f -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport','WMIC_WINDOWS','process call create ''cmd.exe /c 
 cd /D %METRICS_BASE_DIR% & 
 cd../mark59-results-splitter & 
 CreateDataHunterJmeterReports.bat''
','N','','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport_LINUX','SSH_LINUX_UNIX','echo This script creates a set of JMeter reports from a DataHunter test run.
echo starting from $PWD;

{   # try  

    cd ../mark59-results-splitter
    gnome-terminal -- sh -c "./CreateDataHunterJmeterReports.sh; exec bash"

} || { # catch 
    echo attempt to generate JMeter Reports has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Selenium-GenJmeterReport.sh','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport_MAC','SSH_LINUX_UNIX','echo This script creates a set of JMeter reports from a DataHunter test run.
echo starting from $PWD;

{   # try  

    cd ../mark59-results-splitter
    sh CreateDataHunterJmeterReports.sh

} || { # catch 
    echo attempt to generate JMeter Reports has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Selenium-GenJmeterReport.sh','');


INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad','WMIC_WINDOWS','process call create ''cmd.exe /c 
 echo Load DataHunter Test Results into  Mark59 Trends Analysis h2 database. & 
 cd /D  %METRICS_BASE_DIR% & 
 cd ../mark59-trends-load &  
 
 java -jar ./target/mark59-trends-load.jar -a DataHunter -i C:\Mark59_Runs\Jmeter_Results\DataHunter -d h2 &
 PAUSE
''
','N','','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad_LINUX','SSH_LINUX_UNIX','echo This script runs mark59-trends-load,to load results from a DataHunter test run into the Metrics Trend Analysis Graph.
echo starting from $PWD;

{   # try  

    cd ../mark59-trends-load/target &&
    gnome-terminal -- sh -c "java -jar mark59-trends-load.jar -a DataHunter -i ~/Mark59_Runs/Jmeter_Results/DataHunter -d h2; exec bash"

} || { # catch 
    echo attempt to execute mark59-trends-load has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Selenium-metricsTrendsLoad.sh','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad_MAC','SSH_LINUX_UNIX','echo This script runs mark59-trends-load,to load results from a DataHunter test run into the Metrics Trend Analysis Graph.
echo starting from $PWD;

{   # try  

    cd ../mark59-trends-load/target &&
    java -jar mark59-trends-load.jar -a DataHunter -i ~/Mark59_Runs/Jmeter_Results/DataHunter -d h2

} || { # catch 
    echo attempt to execute mark59-trends-load has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Selenium-metricsTrendsLoad.sh','');

INSERT IGNORE INTO COMMANDS VALUES ('FreePhysicalMemory','WMIC_WINDOWS','OS get FreePhysicalMemory','N','','');
INSERT IGNORE INTO COMMANDS VALUES ('FreeVirtualMemory','WMIC_WINDOWS','OS get FreeVirtualMemory','N','','');
INSERT IGNORE INTO COMMANDS VALUES ('LINUX_free_m_1_1','SSH_LINUX_UNIX','free -m 1 1','N','linux memory','');
INSERT IGNORE INTO COMMANDS VALUES ('LINUX_mpstat_1_1','SSH_LINUX_UNIX','mpstat 1 1','N','','');
INSERT IGNORE INTO COMMANDS VALUES ('UNIX_lparstat_5_1','SSH_LINUX_UNIX','lparstat 5 1','N','','');

INSERT IGNORE INTO COMMANDS VALUES ('UNIX_Memory_Script','SSH_LINUX_UNIX','vmstat=$(vmstat -v); 
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
print "${pinned_percent},${numperm_percent},${pgsp_aggregate_util}"','N','','');

INSERT IGNORE INTO COMMANDS VALUES ('UNIX_VM_Memory','SSH_LINUX_UNIX','vmstat=$(vmstat -v); 
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
print "${pinned_percent},${numperm_percent},${pgsp_aggregate_util}"','N','','');

INSERT IGNORE INTO COMMANDS VALUES ('WinCpuCmd','WMIC_WINDOWS','cpu get loadpercentage','N','','');

INSERT IGNORE INTO COMMANDS VALUES ('SimpleScriptSampleCmd', 'GROOVY_SCRIPT', 
'import java.util.ArrayList;
import java.util.List;
import com.mark59.metrics.data.beans.ServerProfile;
import com.mark59.metrics.pojos.ParsedMetric;
import com.mark59.metrics.pojos.ScriptResponse;

ScriptResponse scriptResponse = new ScriptResponse();
List<ParsedMetric> parsedMetrics = new ArrayList<ParsedMetric>();

String commandLogDebug = "running script " + serverProfile.getServerProfileName() + "<br>" +  serverProfile.getComment();
commandLogDebug += "<br>passed parms : parm1=" + parm1 + ", parm2=" + parm2 + ", parm3=" + parm3

Number aNumber = 123;
parsedMetrics.add(new ParsedMetric("a_memory_txn", aNumber, "MEMORY"));
parsedMetrics.add(new ParsedMetric("a_cpu_util_txn", 33.3,  "CPU_UTIL"));
parsedMetrics.add(new ParsedMetric("some_datapoint", 66.6,  "DATAPOINT"));

scriptResponse.setCommandLog(commandLogDebug);
scriptResponse.setParsedMetrics(parsedMetrics);
return scriptResponse;', 'N', 'supplied basic groovy script sample', '["parm1","parm2","parm3"]');

INSERT IGNORE INTO COMMANDS VALUES ('NewRelicSampleCmd', 'GROOVY_SCRIPT', 'import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mark59.metrics.pojos.ParsedMetric;
import com.mark59.metrics.pojos.ScriptResponse;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

String newRelicApiUrl = "https://api.newrelic.com/v2/applications/";
String url = newRelicApiUrl + newRelicApiAppId + "/hosts.json";
ScriptResponse scriptResponse = new ScriptResponse();
List<ParsedMetric> parsedMetrics = new ArrayList<ParsedMetric>();

Request request; Response response = null; JSONObject jsonResponse = null;
Proxy proxy = StringUtils.isNotBlank(proxyServer + proxyPort) ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyServer , new Integer(proxyPort))) : null;
OkHttpClient client = proxy != null ? new OkHttpClient.Builder().proxy(proxy).build() : new OkHttpClient();
Headers headers = new Headers.Builder().add("X-Api-Key", newRelicXapiKey).add("Content-Type", "application/json").build();
String debugJsonResponses =  "running profile " + serverProfile.serverProfileName + ", init req : " + url ;

try {
	request = new Request.Builder().url(url).headers(headers).get().build();
	response = client.newCall(request).execute();
	jsonResponse = new JSONObject(response.body().string());
	debugJsonResponses =  debugJsonResponses + "<br>init res.: " + jsonResponse.toString();

	ZonedDateTime utcTimeNow = ZonedDateTime.now(ZoneOffset.UTC);
	String toHour 	= String.format("%02d", utcTimeNow.getHour());
	String toMinute	= String.format("%02d", utcTimeNow.getMinute());
	ZonedDateTime utcMinus1Min = utcTimeNow.minusMinutes(1);
	String fromHour	= String.format("%02d", utcMinus1Min.getHour());
	String fromMinute = String.format("%02d", utcMinus1Min.getMinute());
	String fromDate = utcMinus1Min.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	String toDate 	= utcTimeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	String urlDateRangeParmStr = "&from=" + fromDate + "T" + fromHour + "%3A" + fromMinute + "%3A00%2B00%3A00" + "&to=" + toDate + "T" + toHour + "%3A" + toMinute + "%3A00%2B00%3A00";

	JSONArray application_hosts = jsonResponse.getJSONArray("application_hosts");

	for (int i = 0; i < application_hosts.length(); i++) {
		JSONObject application_host = (JSONObject) application_hosts.get(i);
		Integer hostId = (Integer) application_host.get("id");
		String hostName = ((String)application_host.get("host")).replace(":","_");
		url = newRelicApiUrl + newRelicApiAppId  + "/hosts/" + hostId + "/metrics/data.json?names%5B%5D=Memory/Heap/Used&names%5B%5D=CPU/User Time&names%5B%5D=Memory/Physical" + urlDateRangeParmStr;
		debugJsonResponses =  debugJsonResponses + "<br><br>req." + i + ": " + url ;

		request = new Request.Builder().url(url).headers(headers).get().build();
		response = client.newCall(request).execute();
		jsonResponse = new JSONObject(response.body().string());
		debugJsonResponses =  debugJsonResponses + "<br>res." + i + ": " + jsonResponse.toString();

		Number memoryMetric = -1.0;
		memoryMetric =  (Number)((JSONObject)((JSONObject)jsonResponse.getJSONObject("metric_data").getJSONArray("metrics").get(0)).getJSONArray("timeslices").get(0)).getJSONObject("values").get("used_mb_by_host") ;
		parsedMetrics.add(new ParsedMetric("MEMORY_HEAP_USED_MB_" + hostName, memoryMetric, "MEMORY"));

		Number cpuMetric = -1.0;
		cpuMetric = (Number)((JSONObject)((JSONObject)jsonResponse.getJSONObject("metric_data").getJSONArray("metrics").get(1)).getJSONArray("timeslices").get(0)).getJSONObject("values").get("percent");
		parsedMetrics.add(new ParsedMetric("CPU_USER_TIME_%_" + hostName, cpuMetric, "CPU_UTIL"));
	}
} catch (Exception e) {
	debugJsonResponses =  debugJsonResponses + "<br>\n ERROR :  Exception last url: " + url + ", response of  : " + jsonResponse + ", message: "+ e.getMessage();
}
scriptResponse.setCommandLog(debugJsonResponses);
scriptResponse.setParsedMetrics(parsedMetrics);
return scriptResponse;', 'N', 'NewRelic Supplied Sample', '["newRelicApiAppId","newRelicXapiKey","proxyServer","proxyPort"]');

INSERT IGNORE INTO COMMANDS VALUES ('MAC_CPU','SSH_LINUX_UNIX','ps -A -o %cpu | awk ''{s+=$1} END {print s}''','N','','');
INSERT IGNORE INTO COMMANDS VALUES ('MAC_MEMSIZE','SSH_LINUX_UNIX','sysctl -n hw.memsize','N','','');


INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('LINUX_Memory_freeG','MEMORY','freeG','import org.apache.commons.lang3.StringUtils;
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
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('LINUX_Memory_totalG','MEMORY','totalG','import org.apache.commons.lang3.StringUtils;
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
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('LINUX_Memory_usedG','MEMORY','usedG','import org.apache.commons.lang3.StringUtils;
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
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreePhysicalG','MEMORY','FreePhysicalG','Math.round(Double.parseDouble(commandResponse.replaceAll("[^\\d.]", "")) / 1000000 )','','FreePhysicalG
22510400');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreeVirtualG','MEMORY','FreeVirtualG','Math.round(Double.parseDouble(commandResponse.replaceAll("[^\\d.]", "")) / 1000000 )','','FreeVirtualMemory
22510400');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Nix_CPU_Idle','CPU_UTIL','IDLE','import org.apache.commons.lang3.ArrayUtils;
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

INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Nix_CPU_UTIL','CPU_UTIL','','import org.apache.commons.lang3.ArrayUtils;
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

INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Return1','DATAPOINT','','return 1','','any rand junk');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('UNIX_Memory_numperm_percent','MEMORY','numperm_percent','commandResponse.split(",")[1].trim()','','1,35,4');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('UNIX_Memory_pgsp_aggregate_util','MEMORY','pgsp_aggregate_util','commandResponse.split(",")[2].trim()','','1,35,4');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('UNIX_Memory_pinned_percent','MEMORY','pinned_percent','commandResponse.split(",")[0].trim()','','1,35,4');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('WicnCpu','CPU_UTIL','','java.util.regex.Matcher m = java.util.regex.Pattern.compile("-?[0-9]+").matcher(commandResponse);
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
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('UNPARSED_CPU_UTIL','CPU_UTIL','',' return commandResponse','simply return the commandResponse','56.8');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Memory_convert_Gb','MEMORY','GB',' return Long.valueOf(commandResponse) / 1000000000;','imply return the commandResponse divided a by 10^9','517179869184');


INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterSeleniumDeployAndExecute','DataHunterSeleniumDeployAndExecute_LINUX');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterSeleniumGenJmeterReport','DataHunterSeleniumGenJmeterReport_LINUX');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterSeleniumTrendsLoad','DataHunterSeleniumTrendsLoad_LINUX');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoMAC-DataHunterSeleniumDeployAndExecute','DataHunterSeleniumDeployAndExecute_MAC');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoMAC-DataHunterSeleniumGenJmeterReport','DataHunterSeleniumGenJmeterReport_MAC');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoMAC-DataHunterSeleniumTrendsLoad','DataHunterSeleniumTrendsLoad_MAC');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterSeleniumDeployAndExecute','DataHunterSeleniumDeployAndExecute');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterSeleniumGenJmeterReport','DataHunterSeleniumGenJmeterReport');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterSeleniumTrendsLoad','DataHunterSeleniumTrendsLoad');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_LINUX','LINUX_free_m_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_LINUX','LINUX_mpstat_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_LINUX_HOSTID','LINUX_free_m_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_LINUX_HOSTID','LINUX_mpstat_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_MAC','MAC_CPU');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_MAC','MAC_MEMSIZE');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_MAC_HOSTID','MAC_CPU');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_MAC_HOSTID','MAC_MEMSIZE');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS','FreePhysicalMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS','FreeVirtualMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS','WinCpuCmd');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','FreePhysicalMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','FreeVirtualMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WinCpuCmd');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteLinuxServer','LINUX_free_m_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteLinuxServer','LINUX_mpstat_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteUnixVM','UNIX_lparstat_5_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteUnixVM','UNIX_Memory_Script');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer','FreePhysicalMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer','FreeVirtualMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer','WinCpuCmd');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('SimpleScriptSampleRunner', 'SimpleScriptSampleCmd');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('NewRelicSampleProfile','NewRelicSampleCmd');


INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumDeployAndExecute','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumDeployAndExecute_MAC','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumGenJmeterReport','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumGenJmeterReport_LINUX','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumGenJmeterReport_MAC','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumTrendsLoad','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumTrendsLoad_LINUX','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumTrendsLoad_MAC','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('FreePhysicalMemory','Memory_FreePhysicalG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('FreeVirtualMemory','Memory_FreeVirtualG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1','LINUX_Memory_freeG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1','LINUX_Memory_totalG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1','LINUX_Memory_usedG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('MAC_CPU','UNPARSED_CPU_UTIL');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('MAC_MEMSIZE','Memory_convert_Gb');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_mpstat_1_1','Nix_CPU_UTIL');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_lparstat_5_1','Nix_CPU_UTIL');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_Memory_Script','UNIX_Memory_numperm_percent');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_Memory_Script','UNIX_Memory_pgsp_aggregate_util');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_Memory_Script','UNIX_Memory_pinned_percent');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_VM_Memory','UNIX_Memory_numperm_percent');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_VM_Memory','UNIX_Memory_pgsp_aggregate_util');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_VM_Memory','UNIX_Memory_pinned_percent');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('WinCpuCmd','WicnCpu');
