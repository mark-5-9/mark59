-- Note that all MAC relasted entries are at the bottom of the file 

-- DROP TABLE IF EXISTS  SERVERPROFILES;
-- DROP TABLE IF EXISTS  COMMANDS;
-- DROP TABLE IF EXISTS  SERVERCOMMANDLINKS;
-- DROP TABLE IF EXISTS  COMMANDRESPONSEPARSERS;
-- DROP TABLE IF EXISTS  COMMANDPARSERLINKS;

CREATE TABLE IF NOT EXISTS SERVERPROFILES  (
   SERVER_PROFILE_NAME  varchar(64)   NOT NULL,
   EXECUTOR             varchar(32)   NOT NULL,
   SERVER               varchar(64)   DEFAULT '',
   ALTERNATE_SERVER_ID  varchar(64)   DEFAULT '',
   USERNAME             varchar(64)   DEFAULT '',
   PASSWORD             varchar(64)   DEFAULT '',
   PASSWORD_CIPHER      varchar(64)   DEFAULT '',
   CONNECTION_PORT      varchar(8)    DEFAULT '',
   CONNECTION_TIMEOUT   varchar(8)    DEFAULT '',
   COMMENT              varchar(128)  DEFAULT NULL,
   PARAMETERS           varchar(8196) DEFAULT NULL,
  PRIMARY KEY ( SERVER_PROFILE_NAME )
); 


CREATE TABLE IF NOT EXISTS COMMANDS  (
   COMMAND_NAME   varchar(64)   NOT NULL,
   EXECUTOR       varchar(32)   NOT NULL,
   COMMAND        CLOB          NOT NULL,
   IGNORE_STDERR  varchar(1)    DEFAULT NULL,
   COMMENT        varchar(128)  DEFAULT NULL,
   PARAM_NAMES    varchar(1000) DEFAULT NULL,   
  PRIMARY KEY ( COMMAND_NAME )
); 


CREATE TABLE IF NOT EXISTS SERVERCOMMANDLINKS  (
   SERVER_PROFILE_NAME  varchar(64) NOT NULL,
   COMMAND_NAME  varchar(64) NOT NULL,
  PRIMARY KEY ( SERVER_PROFILE_NAME , COMMAND_NAME )
);


CREATE TABLE IF NOT EXISTS COMMANDRESPONSEPARSERS  (
   PARSER_NAME  varchar(64) NOT NULL,
   METRIC_TXN_TYPE  varchar(64) NOT NULL,
   METRIC_NAME_SUFFIX  varchar(64) NOT NULL,
   SCRIPT  CLOB NOT NULL,
   COMMENT  varchar(1024) NOT NULL,
   SAMPLE_COMMAND_RESPONSE  varchar(8196) NOT NULL,
  PRIMARY KEY ( PARSER_NAME )
); 


CREATE TABLE IF NOT EXISTS COMMANDPARSERLINKS  (
   COMMAND_NAME  varchar(64) NOT NULL,
   PARSER_NAME  varchar(64) NOT NULL,
  PRIMARY KEY ( COMMAND_NAME , PARSER_NAME )
); 

-- populate with initial data  -- 

INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterSeleniumDeployAndExecute','SSH_LINUX_UNIX','localhost','','','','','22','60000','',NULL);
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterPlaywrightDeployAndExecute','SSH_LINUX_UNIX','localhost','','','','','22','60000','',NULL);
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterTestGenJmeterReport','SSH_LINUX_UNIX','localhost','','','','','22','60000','Reports generated at   ~/Mark59_Runs/Jmeter_Reports/DataHunter/   <br>(open each index.html)   ',NULL);
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoLINUX-DataHunterTestTrendsLoad','SSH_LINUX_UNIX','localhost','','','','','22','60000','Loads Trend Analysis (H2 database).  See:<br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','{"DATABASE":"h2H2"}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumDeployAndExecute','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'','{}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterPlaywrightDeployAndExecute','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'','{}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterTestGenJmeterReport','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'Hint - in browser open this URL and go to each index.html: file:///C:/Mark59_Runs/Jmeter_Reports/DataHunter/','{}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterTestTrendsLoad','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'Loads Trend Analysis (H2 database). See: <br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','{"DATABASE":"H2"}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_LINUX','SSH_LINUX_UNIX','localhost','','','','','22','60000','',NULL);
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_WINDOWS','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'','{"SECURE_KEY_ARRAY":"","SECURE_STRING_TXT":""}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_WINDOWS_HOSTID','POWERSHELL_WINDOWS','localhost','HOSTID','','','',NULL,NULL,'''HOSTID'' will be subed with computername','{"SECURE_KEY_ARRAY":"","SECURE_STRING_TXT":""}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteLinuxServer','SSH_LINUX_UNIX','LinuxServerName','','userid','encryptMe','','22','60000','',NULL);
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteLinuxServerViaSSH','SSH_LINUX_UNIX','LinuxServerName','','userid','','','22','60000','no entry for password','{"SSH_IDENTITY":"full filename of the private key.  Note the key needs to be in Classic OpenSSH format (-m PEM). See overview. ","SSH_PASSPHRASE":"remove this param or leave blank if there is no passphrase"}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteUnixVM','SSH_LINUX_UNIX','UnixVMName','','userid','encryptMe','','22','60000','',NULL);
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteWinServer_WMIC','WMIC_WINDOWS','WinServerName','','userid','password','',NULL,NULL,'','{}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('NewRelicSampleProfile','GROOVY_SCRIPT',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'supplied sample New Relic API groovy script','{"proxyPort":"proxyPort","newRelicXapiKey":"newRelicXapiKey","proxyServer":"proxyServer","newRelicApiAppId":"newRelicApiAppId","parm1":"extraUnusedParm"}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('SimpleScriptSampleRunner','GROOVY_SCRIPT',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'runs a supplied, basic groovy script sample','{"parm4":"uused","parm1":"44","parm2":"55.7","parm3":"444"}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_WMIC_WINDOWS','WMIC_WINDOWS','localhost','','','','',NULL,NULL,'','{}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_WMIC_WINDOWS_HOSTID','WMIC_WINDOWS','localhost','HOSTID','','','',NULL,NULL,'''HOSTID'' will be subed <br> with computername  ','{}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteWinServer_pwd','POWERSHELL_WINDOWS','WinServerName','','userid','password','',NULL,NULL,'win connect via user pwd','{"SECURE_KEY_ARRAY":"","SECURE_STRING_TXT":""}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteWinServer_secureStr','POWERSHELL_WINDOWS','WinServerName','','userid','','',NULL,NULL,'win connect via secure string','{"SECURE_KEY_ARRAY":"","SECURE_STRING_TXT":"secure-string-goes-here"}');
INSERT IGNORE INTO SERVERPROFILES VALUES ('remoteWinServer_secureStr_key','POWERSHELL_WINDOWS','WinServerName','','userid','','',NULL,NULL,'win connect via secure string using user defined key <br>(a sample key shown)','{"SECURE_KEY_ARRAY":"101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116","SECURE_STRING_TXT":"secure-string-built-using-key-goes-here"}');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','POWERSHELL_WINDOWS','Start-Process -FilePath ''${METRICS_BASE_DIR}\..\bin\TestRunWIN-DataHunter-Selenium-DeployAndExecute.bat''','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterSeleniumTest.bat in mark59-scripting-samples ','[]');
INSERT IGNORE INTO COMMANDS VALUES ('DataHunterPlaywrightDeployAndExecute','POWERSHELL_WINDOWS','Start-Process -FilePath ''${METRICS_BASE_DIR}\..\bin\TestRunWIN-DataHunter-Playwright-DeployAndExecute.bat''','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterPlaywrightTest.bat in mark59-scripting-samples ','[]');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','SSH_LINUX_UNIX','echo This script runs the JMeter Selenium deploy in the background, then opens a terminal for JMeter execution.
echo starting from $PWD;

{   # try  

    cd ../mark59-scripting-samples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo mark59-scripting-samples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-scripting-samples.jar  ~/apache-jmeter/lib/ext/mark59-scripting-samples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
    cp -r ./target/mark59-scripting-samples-dependencies ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
 
    gnome-terminal -- sh -c "~/apache-jmeter/bin/jmeter -n -X -f  -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false; exec bash"

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/TestRunLINUX-DataHunter-Selenium-DeployAndExecute.sh','[]');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterPlaywrightDeployAndExecute_LINUX','SSH_LINUX_UNIX','echo This script runs the Playwright JMeter deploy in the background, then opens a terminal for JMeter execution.
echo starting from $PWD;

{   # try  

    cd ../mark59-scripting-samples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo mark59-scripting-samples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-scripting-samples.jar  ~/apache-jmeter/lib/ext/mark59-scripting-samples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
    cp -r ./target/mark59-scripting-samples-dependencies ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
 
    gnome-terminal -- sh -c "~/apache-jmeter/bin/jmeter -n -X -f  -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterPlaywrightTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false; exec bash"

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/TestRunLINUX-DataHunter-Playwright-DeployAndExecute.sh','[]');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterTestGenJmeterReport','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\..\mark59-results-splitter;
Start-Process -FilePath ''.\CreateDataHunterJmeterReports.bat''
','N','','[]');
INSERT IGNORE INTO COMMANDS VALUES ('DataHunterTestGenJmeterReport_LINUX','SSH_LINUX_UNIX','echo This script creates a set of JMeter reports from a DataHunter test run.
echo starting from $PWD;

{   # try  

    cd ../mark59-results-splitter
    gnome-terminal -- sh -c "./CreateDataHunterJmeterReports.sh; exec bash"

} || { # catch 
    echo attempt to generate JMeter Reports has failed! 
}
','Y','refer bin/TestRunLINUX-DataHunter-Test-GenJmeterReport.sh',NULL);
INSERT IGNORE INTO COMMANDS VALUES ('DataHunterTestTrendsLoad','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\..\bin;
Start-Process -FilePath ''.\TestRunWIN-DataHunter-Test-TrendsLoad.bat'' -ArgumentList ''${DATABASE}''
','N','','["DATABASE"]');
INSERT IGNORE INTO COMMANDS VALUES ('DataHunterTestTrendsLoad_LINUX','SSH_LINUX_UNIX','echo This script runs mark59-trends-load,to load results from a DataHunter test run into the Metrics Trends Graph.
echo starting from $PWD;

{   # try  

    cd ../mark59-trends-load/target &&
    gnome-terminal -- sh -c "java -jar mark59-trends-load.jar -a DataHunter -i ~/Mark59_Runs/Jmeter_Results/DataHunter -d h2; exec bash"

} || { # catch 
    echo attempt to execute mark59-trends-load has failed! 
}
','Y','refer bin/TestRunWIN-DataHunter-Test-TrendsLoad.sh',NULL);
INSERT IGNORE INTO COMMANDS VALUES ('FreePhysicalMemory','WMIC_WINDOWS','OS get FreePhysicalMemory','N','','[]');
INSERT IGNORE INTO COMMANDS VALUES ('FreeVirtualMemory','WMIC_WINDOWS','OS get FreeVirtualMemory','N','','[]');
INSERT IGNORE INTO COMMANDS VALUES ('LINUX_free_m_1_1','SSH_LINUX_UNIX','free -m 1 1','N','linux memory',NULL);
INSERT IGNORE INTO COMMANDS VALUES ('LINUX_free_m_1_1_ViaSSH','SSH_LINUX_UNIX','free -m 1 1','N','linux memory','["SSH_IDENTITY","SSH_PASSPHRASE"]');
INSERT IGNORE INTO COMMANDS VALUES ('LINUX_mpstat_1_1','SSH_LINUX_UNIX','mpstat 1 1','N','',NULL);
INSERT IGNORE INTO COMMANDS VALUES ('NewRelicSampleCmd','GROOVY_SCRIPT','import java.net.InetSocketAddress;
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
return scriptResponse;','N','NewRelic Supplied Sample','["newRelicApiAppId","newRelicXapiKey","parm1","proxyServer","proxyPort"]');
INSERT IGNORE INTO COMMANDS VALUES ('SimpleScriptSampleCmd','GROOVY_SCRIPT','import java.util.ArrayList;
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
/// scriptResponse.setParsedMetrics(parsedMetrics);
scriptResponse.parsedMetrics=parsedMetrics;
return scriptResponse;','N','supplied basic groovy script sample','["parm1","parm2","parm3","parm4"]');
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
print "${pinned_percent},${numperm_percent},${pgsp_aggregate_util}"','N','','[]');
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
print "${pinned_percent},${numperm_percent},${pgsp_aggregate_util}"','N','',NULL);
INSERT IGNORE INTO COMMANDS VALUES ('UNIX_lparstat_5_1','SSH_LINUX_UNIX','lparstat 5 1','N','','[]');
INSERT IGNORE INTO COMMANDS VALUES ('WinCpuCmd','WMIC_WINDOWS','cpu get loadpercentage','N','','[]');
INSERT IGNORE INTO COMMANDS VALUES ('WIN_Core','POWERSHELL_WINDOWS','if (''${PROFILE_SERVER}'' -eq ''localhost''){

    Write-Output \"get localhost metric \";
    $ComputerCPU = Get-WmiObject -Class win32_processor -ErrorAction Stop;
    $ComputerMemory = Get-WmiObject -Class win32_operatingsystem -ErrorAction Stop;

} elseif ((![string]::IsNullOrEmpty(''${SECURE_STRING_TXT}'')) -and ([string]::IsNullOrEmpty(''${SECURE_KEY_ARRAY}''))){

    Write-Output \"  cpu using secure string \"; 
    <# To get the SECURE_STRING_TXT parameter value to store (no key):
        $secureString = ConvertTo-SecureString ''plain-text-password'' -AsPlainText -Force
        $secureStringTxt = Convertfrom-SecureString $secureString 
    #>

    $securePwd = ''${SECURE_STRING_TXT}'' | ConvertTo-SecureString; 
    $credential = New-Object System.Management.Automation.PSCredential (''${PROFILE_USERNAME}'', $securePwd);
 
} elseif ((![string]::IsNullOrEmpty(''${SECURE_STRING_TXT}'')) -and (![string]::IsNullOrEmpty(''${SECURE_KEY_ARRAY}''))){

    Write-Output \"get remote server metric using using secure string with key \";
    <# To get the SECURE_STRING_TXT parameter value to store (using key), 
        $secureString = ConvertTo-SecureString ''plain-text-password'' -AsPlainText -Force
        [Byte[]] $key = ( your-comma-delimited-list-of-key-values-between-0-255 )
        $secureStringTxt = Convertfrom-SecureString $secureString -key $key
    #>
 
    [Byte[]] $key = @(${SECURE_KEY_ARRAY}); 
    $securePwd = ''${SECURE_STRING_TXT}'' | ConvertTo-SecureString -Key $key;
    $credential = New-Object System.Management.Automation.PSCredential (''${PROFILE_USERNAME}'', $securePwd);
  
} else {

    Write-Output \"get remote server metric using profile username and password \";
    $password = ConvertTo-SecureString ''${PROFILE_PASSWORD}'' -AsPlainText -Force; 
    $credential = New-Object System.Management.Automation.PSCredential (''${PROFILE_USERNAME}'', $password);
}

if (''${PROFILE_SERVER}'' -ne ''localhost''){ 
    $ComputerCPU = Get-WmiObject -Credential $credential -ComputerName ${PROFILE_SERVER} -Class win32_processor -ErrorAction Stop;
    $ComputerMemory = Get-WmiObject -Credential $credential -ComputerName ${PROFILE_SERVER} -Class win32_operatingsystem -ErrorAction Stop;
}

$CPUUtil =  [math]::round(($ComputerCPU | Measure-Object -Property LoadPercentage -Average | Select-Object Average).Average);
$FreePhysicalMemory= [math]::truncate($ComputerMemory.FreePhysicalMemory / 1MB);
$FreeVirtualMemory = [math]::truncate($ComputerMemory.FreeVirtualMemory / 1MB);
     
Write-Host " CPU["$CPUUtil"] FreePhysicalMemory["$FreePhysicalMemory"] FreeVirtualMemory["$FreeVirtualMemory"]";
','N','you should <# comment #> the debug Write-Output statements out to run in a real test :)','["SECURE_KEY_ARRAY","SECURE_STRING_TXT"]');
INSERT IGNORE INTO COMMANDS VALUES ('WIN_DiskSpace_C','POWERSHELL_WINDOWS','if (''${PROFILE_SERVER}'' -eq ''localhost''){

    $Drive =  Get-WMIObject Win32_LogicalDisk -Filter \"DeviceID=''C:''\" ;

} elseif ((![string]::IsNullOrEmpty(''${SECURE_STRING_TXT}'')) -and ([string]::IsNullOrEmpty(''${SECURE_KEY_ARRAY}''))){

    $securePwd = ''${SECURE_STRING_TXT}'' | ConvertTo-SecureString; 
    $credential = New-Object System.Management.Automation.PSCredential (''${PROFILE_USERNAME}'', $securePwd);
 
} elseif ((![string]::IsNullOrEmpty(''${SECURE_STRING_TXT}'')) -and (![string]::IsNullOrEmpty(''${SECURE_KEY_ARRAY}''))){

    [Byte[]] $key = @(${SECURE_KEY_ARRAY}); 
    $securePwd = ''${SECURE_STRING_TXT}'' | ConvertTo-SecureString -Key $key;
    $credential = New-Object System.Management.Automation.PSCredential (''${PROFILE_USERNAME}'', $securePwd);
  
} else {

    $password = ConvertTo-SecureString ''${PROFILE_PASSWORD}'' -AsPlainText -Force; 
    $credential = New-Object System.Management.Automation.PSCredential (''${PROFILE_USERNAME}'', $password);
}

if (''${PROFILE_SERVER}'' -ne ''localhost''){ 
    $Drive = Get-WMIObject -Credential $credential -ComputerName ${PROFILE_SERVER} Win32_LogicalDisk -Filter \"DeviceID=''C:''\" ;
}

$FreeDiskSpace = $Drive | ForEach-Object {[math]::truncate($_.freespace / 1GB)};
Write-Host "FreeDiskSpace["$FreeDiskSpace"]" ;','N','','["SECURE_KEY_ARRAY","SECURE_STRING_TXT"]');
INSERT IGNORE INTO COMMANDS VALUES ('WIN_PerfRawData','POWERSHELL_WINDOWS','if (''${PROFILE_SERVER}'' -eq ''localhost''){

    <# Write-Output \"get localhost metric \"; #>
    $PerfRawData = Get-WmiObject Win32_PerfRawData_PerfOS_System;

} elseif ((![string]::IsNullOrEmpty(''${SECURE_STRING_TXT}'')) -and ([string]::IsNullOrEmpty(''${SECURE_KEY_ARRAY}''))){

    <# Write-Output \"  cpu using secure string \"; #>
    $securePwd = ''${SECURE_STRING_TXT}'' | ConvertTo-SecureString; 
    $credential = New-Object System.Management.Automation.PSCredential (''${PROFILE_USERNAME}'', $securePwd);
 
} elseif ((![string]::IsNullOrEmpty(''${SECURE_STRING_TXT}'')) -and (![string]::IsNullOrEmpty(''${SECURE_KEY_ARRAY}''))){

    <# Write-Output \"get remote server metric using using secure string with key \"; #>
    [Byte[]] $key = @(${SECURE_KEY_ARRAY}); 
    $securePwd = ''${SECURE_STRING_TXT}'' | ConvertTo-SecureString -Key $key;
    $credential = New-Object System.Management.Automation.PSCredential (''${PROFILE_USERNAME}'', $securePwd);
  
} else {

    <# Write-Output \"get remote server metric using profile username and password \"; #>
    $password = ConvertTo-SecureString ''${PROFILE_PASSWORD}'' -AsPlainText -Force; 
    $credential = New-Object System.Management.Automation.PSCredential (''${PROFILE_USERNAME}'', $password);
}

if (''${PROFILE_SERVER}'' -ne ''localhost''){ 
    $PerfRawData = Get-WmiObject -Credential $credential -ComputerName ${PROFILE_SERVER} Win32_PerfRawData_PerfOS_System
}

$CPUQlen = [math]::round(($PerfRawData | Measure-Object -Property ProcessorQueueLength -Average | Select-Object Average).Average);
$Processes = [math]::round(($PerfRawData | Measure-Object -Property Processes -Average | Select-Object Average).Average);
Write-Host " CPUQlen["$CPUQlen"] Processes["$Processes"] " ;','N','','["SECURE_KEY_ARRAY","SECURE_STRING_TXT"]');

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
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('WicnCpu','CPU_UTIL','','java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(commandResponse);
Double sum = 0; 
int count = 0; 
while (m.find()){ 
    sum += Double.parseDouble(m.group()); 
    count++;
}; 
if (count==0) 
    return 0 ; 
else 
    return Math.round(sum/count);','avg a list of (dec) nums in text ','LoadPercentage.with a dot
1.99
3
2.99
3
');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('FreePhysicalDiskSpaceGB_C','DATAPOINT','FreePhysicalDiskSpaceGB_C','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "FreeDiskSpace[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "FreeDiskSpace[", "]");  
}
return extractedMetric; ','..FreeDiskSpace[627].. ','any debug output (without the square brackets bit)
FreeDiskSpace[627]');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreePhysicalG_PS','MEMORY','FreePhysicalG','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "FreePhysicalMemory[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "FreePhysicalMemory[", "]");  
}
return extractedMetric; ','..FreePhysicalMemory[14].. ','get remote server metric using using secure string with key
CPU[46] FreePhysicalMemory[14] FreeVirtualMemory[18] FreeDiskSpace[32]');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreeVirtualG_PS','MEMORY','FreeVirtualG','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "FreeVirtualMemory[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "FreeVirtualMemory[", "]");  
}
return extractedMetric; ','..FreeVirtualMemory[18]..','get remote server metric using using secure string with key
CPU[46] FreePhysicalMemory[14] FreeVirtualMemory[18] FreeDiskSpace[32]');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Cpu_Qlen_PS','DATAPOINT','Cpu_Qlen','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "CPUQlen[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "CPUQlen[", "]");  
}
return extractedMetric; ','..CPUQlen[1234].. ','some text
 CPUQlen[1234] Processes[367]');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Cpu_Util_PS','CPU_UTIL','','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "CPU[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "CPU[", "]");  
}
return extractedMetric; ','..CPU[14].. ','get remote server metric using using secure string with key
CPU[46] FreePhysicalMemory[14] FreeVirtualMemory[18] FreeDiskSpace[32]');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Processes_PS','DATAPOINT','Processes','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "Processes[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "Processes[", "]");  
}
return extractedMetric; ','..Processes[367].. ','some text
 CPUQlen[1234] Processes[367]');


INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('UNPARSED_CPU_UTIL','CPU_UTIL','',' return commandResponse','simply return the commandResponse','56.8');
INSERT IGNORE INTO COMMANDRESPONSEPARSERS VALUES ('Memory_convert_Gb','MEMORY','GB',' return Long.valueOf(commandResponse) / 1000000000;','imply return the commandResponse divided a by 10^9','517179869184');


INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterSeleniumDeployAndExecute','DataHunterSeleniumDeployAndExecute_LINUX');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterPlaywrightDeployAndExecute','DataHunterPlaywrightDeployAndExecute_LINUX');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterTestGenJmeterReport','DataHunterTestGenJmeterReport_LINUX');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoLINUX-DataHunterTestTrendsLoad','DataHunterTestTrendsLoad_LINUX');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterSeleniumDeployAndExecute','DataHunterSeleniumDeployAndExecute');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterPlaywrightDeployAndExecute','DataHunterPlaywrightDeployAndExecute');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterTestGenJmeterReport','DataHunterTestGenJmeterReport');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoWIN-DataHunterTestTrendsLoad','DataHunterTestTrendsLoad');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_LINUX','LINUX_free_m_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_LINUX','LINUX_mpstat_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS','FreePhysicalMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS','FreeVirtualMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS','WinCpuCmd');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS_HOSTID','FreePhysicalMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS_HOSTID','FreeVirtualMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS_HOSTID','WinCpuCmd');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteLinuxServer','LINUX_free_m_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteLinuxServer','LINUX_mpstat_1_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteLinuxServerViaSSH','LINUX_free_m_1_1_ViaSSH');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteUnixVM','UNIX_Memory_Script');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteUnixVM','UNIX_lparstat_5_1');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_WMIC','FreePhysicalMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_WMIC','FreeVirtualMemory');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_WMIC','WinCpuCmd');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('NewRelicSampleProfile','NewRelicSampleCmd');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('SimpleScriptSampleRunner','SimpleScriptSampleCmd');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_pwd','WIN_Core');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_pwd','WIN_DiskSpace_C');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_pwd','WIN_PerfRawData');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr','WIN_Core');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr','WIN_DiskSpace_C');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr','WIN_PerfRawData');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr_key','WIN_Core');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr_key','WIN_DiskSpace_C');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr_key','WIN_PerfRawData');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS','WIN_Core');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WIN_Core');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WIN_DiskSpace_C');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WIN_PerfRawData');

INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumDeployAndExecute','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterSeleniumDeployAndExecute_LINUX','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterPlaywrightDeployAndExecute','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterPlaywrightDeployAndExecute_LINUX','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterTestGenJmeterReport','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterTestGenJmeterReport_LINUX','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterTestTrendsLoad','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('DataHunterTestTrendsLoad_LINUX','Return1');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('FreePhysicalMemory','Memory_FreePhysicalG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('FreeVirtualMemory','Memory_FreeVirtualG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1','LINUX_Memory_freeG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1','LINUX_Memory_totalG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1','LINUX_Memory_usedG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1_ViaSSH','LINUX_Memory_freeG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1_ViaSSH','LINUX_Memory_totalG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1_ViaSSH','LINUX_Memory_usedG');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('LINUX_mpstat_1_1','Nix_CPU_UTIL');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_lparstat_5_1','Nix_CPU_UTIL');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_Memory_Script','UNIX_Memory_numperm_percent');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_Memory_Script','UNIX_Memory_pgsp_aggregate_util');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_Memory_Script','UNIX_Memory_pinned_percent');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_VM_Memory','UNIX_Memory_numperm_percent');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_VM_Memory','UNIX_Memory_pgsp_aggregate_util');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('UNIX_VM_Memory','UNIX_Memory_pinned_percent');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('WinCpuCmd','WicnCpu');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('WIN_Core','Cpu_Util_PS');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('WIN_Core','Memory_FreePhysicalG_PS');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('WIN_Core','Memory_FreeVirtualG_PS');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('WIN_DiskSpace_C','FreePhysicalDiskSpaceGB_C');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('WIN_PerfRawData','Cpu_Qlen_PS');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('WIN_PerfRawData','Processes_PS');

INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('MAC_CPU','UNPARSED_CPU_UTIL');
INSERT IGNORE INTO COMMANDPARSERLINKS VALUES ('MAC_MEMSIZE','Memory_convert_Gb');


-- all mac entries (these only exist on H2) 

INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoMAC-DataHunterSeleniumDeployAndExecute','SSH_LINUX_UNIX','localhost','','','','','22','60000','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoMAC-DataHunterPlaywrightDeployAndExecute','SSH_LINUX_UNIX','localhost','','','','','22','60000','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoMAC-DataHunterTestGenJmeterReport','SSH_LINUX_UNIX','localhost','','','','','22','60000','Reports generated at   ~/Mark59_Runs/Jmeter_Reports/DataHunter/   <br>(open each index.html)   ','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('DemoMAC-DataHunterTestTrendsLoad','SSH_LINUX_UNIX','localhost','','','','','22','60000','Loads Trend Analysis (H2 database).  See:<br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_MAC','SSH_LINUX_UNIX','localhost','','','','','22','60000','','');
INSERT IGNORE INTO SERVERPROFILES VALUES ('localhost_MAC_HOSTID','SSH_LINUX_UNIX','localhost','HOSTID','','','','22','60000','HOSTID will be subed <br> with computername','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute_MAC','SSH_LINUX_UNIX','echo This script runs the JMeter Selenium deploy and execution in the background, and displays output on completion.
echo starting from $PWD;

{   # try  

    cd ../mark59-scripting-samples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo mark59-scripting-samples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ./chromedriver ~/apache-jmeter/bin/chromedriver && 
    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-scripting-samples.jar  ~/apache-jmeter/lib/ext/mark59-scripting-samples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
    cp -r ./target/mark59-scripting-samples-dependencies ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
    cd ~/apache-jmeter/bin && 
    echo Starting JMeter execution from $PWD && 
    ~/apache-jmeter/bin/jmeter -n -X -f -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterSeleniumTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/mac_commands/TestRunMAC-DataHunter-Selenium-DeployAndExecute.command','');


INSERT IGNORE INTO COMMANDS VALUES ('DataHunterPlaywrightDeployAndExecute_MAC','SSH_LINUX_UNIX','echo This script runs the JMeter Playwright deploy and execution in the background, and displays output on completion.
echo starting from $PWD;

{   # try  

    cd ../mark59-scripting-samples && 
    DH_TEST_SAMPLES_DIR=$(pwd) && 
    echo mark59-scripting-samples base dir is $DH_TEST_SAMPLES_DIR &&

    cp ./mark59.properties ~/apache-jmeter/bin/mark59.properties &&
    cp ../mark59-metrics-api/target/mark59-metrics-api.jar  ~/apache-jmeter/lib/ext/mark59-metrics-api.jar && 
    cp ./target/mark59-scripting-samples.jar  ~/apache-jmeter/lib/ext/mark59-scripting-samples.jar && 
    mkdir -p ~/Mark59_Runs/Jmeter_Results/DataHunter &&
    rm -rf ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
    cp -r ./target/mark59-scripting-samples-dependencies ~/apache-jmeter/lib/ext/mark59-scripting-samples-dependencies &&
    cd ~/apache-jmeter/bin && 
    echo Starting JMeter execution from $PWD && 
    ~/apache-jmeter/bin/jmeter -n -X -f -t $DH_TEST_SAMPLES_DIR/test-plans/DataHunterPlaywrightTestPlan.jmx -l ~/Mark59_Runs/Jmeter_Results/DataHunter/DataHunterTestResults.csv -JForceTxnFailPercent=0 -JStartCdpListeners=false

} || { # catch 
    echo Deploy was unsuccessful! 
}','Y','refer bin/mac_commands/TestRunMAC-DataHunter-Playwright-DeployAndExecute.command','');



INSERT IGNORE INTO COMMANDS VALUES ('DataHunterTestGenJmeterReport_MAC','SSH_LINUX_UNIX','echo This script creates a set of JMeter reports from a DataHunter test run.
echo starting from $PWD;

{   # try  

    cd ../mark59-results-splitter
    sh CreateDataHunterJmeterReports.sh

} || { # catch 
    echo attempt to generate JMeter Reports has failed! 
}
','Y','refer bin/mac_commands/TestRunMAC-DataHunter-Test-GenJmeterReport.command','');

INSERT IGNORE INTO COMMANDS VALUES ('DataHunterTestTrendsLoad_MAC','SSH_LINUX_UNIX','echo This script runs mark59-trends-load,to load results from a DataHunter test run into the Metrics Trend Analysis Graph.
echo starting from $PWD;

{   # try  

    cd ../mark59-trends-load/target &&
    java -jar mark59-trends-load.jar -a DataHunter -i ~/Mark59_Runs/Jmeter_Results/DataHunter -d h2

} || { # catch 
    echo attempt to execute mark59-trends-load has failed! 
}
','Y','refer bin/mac_commands/TestRunMAC-DataHunter-Test-TrendsLoad.command','');

INSERT IGNORE INTO COMMANDS VALUES ('MAC_CPU','SSH_LINUX_UNIX','ps -A -o %cpu | awk ''{s+=$1} END {print s}''','N','','');
INSERT IGNORE INTO COMMANDS VALUES ('MAC_MEMSIZE','SSH_LINUX_UNIX','sysctl -n hw.memsize','N','','');

INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoMAC-DataHunterSeleniumDeployAndExecute','DataHunterSeleniumDeployAndExecute_MAC');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoMAC-DataHunterPlaywrightDeployAndExecute','DataHunterPlaywrightDeployAndExecute_MAC');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoMAC-DataHunterTestGenJmeterReport','DataHunterTestGenJmeterReport_MAC');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('DemoMAC-DataHunterTestTrendsLoad','DataHunterTestTrendsLoad_MAC');

INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_MAC','MAC_CPU');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_MAC','MAC_MEMSIZE');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_MAC_HOSTID','MAC_CPU');
INSERT IGNORE INTO SERVERCOMMANDLINKS VALUES ('localhost_MAC_HOSTID','MAC_MEMSIZE');
