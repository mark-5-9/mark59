
-- *************************************************************************************
-- **
-- **   from Mark59 versions 5.1, 5.2 or 5.3 to 5.4   
-- **
-- **   THIS IS TECHNICALLY AN OPTIONAL CHANGE  
-- **   --------------------------------------
-- **   However, as it:
-- **   -  allows for longer Groovy Scripts to be written (datatype to MEDIUMTEXT),
-- **      and greater allowance for parameter length 
-- **   -  loads useful examples of the use of POWERSHELL and parameterization
-- **      into the Metrics application database, 
-- **   we suggest you run these updates.
-- **   
-- **   Note that some of the existing Server Profile samples change from using WMIC
-- **   to using POWERSHELL, but should not be destructive to your existing profiles. 
-- **   If you have deleted some or all of the provided sample data, expect the corresponding
-- **   DELETE statements to fail.
-- **
-- **   Obviously, take a backup, just in case ... 
-- **
-- *************************************************************************************


-- *************************************************************************************
--  Increased column lengths
-- *************************************************************************************

ALTER TABLE COMMANDS ALTER COLUMN COMMAND TYPE TEXT;

ALTER TABLE COMMANDRESPONSEPARSERS ALTER COLUMN SCRIPT TYPE TEXT;
ALTER TABLE COMMANDRESPONSEPARSERS ALTER COLUMN SAMPLE_COMMAND_RESPONSE TYPE varchar(8196);

ALTER TABLE SERVERPROFILES ALTER COLUMN PARAMETERS TYPE varchar(8196);


-- *************************************************************************************
--  Row updates for the mark59metricsdb  tables
-- *************************************************************************************

-- changed server profiles --
DELETE FROM SERVERPROFILES WHERE SERVER_PROFILE_NAME = 'DemoWIN-DataHunterSeleniumDeployAndExecute';
DELETE FROM SERVERPROFILES WHERE SERVER_PROFILE_NAME = 'DemoWIN-DataHunterSeleniumGenJmeterReport';
DELETE FROM SERVERPROFILES WHERE SERVER_PROFILE_NAME = 'DemoWIN-DataHunterSeleniumTrendsLoad';
DELETE FROM SERVERPROFILES WHERE SERVER_PROFILE_NAME = 'localhost_WINDOWS';
DELETE FROM SERVERPROFILES WHERE SERVER_PROFILE_NAME = 'localhost_WINDOWS_HOSTID';

INSERT INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumDeployAndExecute','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'','{}');
INSERT INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumGenJmeterReport','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'Hint - in browser open this URL and go to each index.html: file:///C:/Mark59_Runs/Jmeter_Reports/DataHunter/','{}');
INSERT INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumTrendsLoad','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'Loads Trend Analysis (PG database). See: <br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','{"DATABASE":"POSTGRES"}');
INSERT INTO SERVERPROFILES VALUES ('localhost_WINDOWS','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'','{"SECURE_KEY_ARRAY":"","SECURE_STRING_TXT":""}');
INSERT INTO SERVERPROFILES VALUES ('localhost_WINDOWS_HOSTID','POWERSHELL_WINDOWS','localhost','HOSTID','','','',NULL,NULL,'''HOSTID'' will be subed with computername','{"SECURE_KEY_ARRAY":"","SECURE_STRING_TXT":""}');

-- new server profiles --
INSERT INTO SERVERPROFILES VALUES ('remoteLinuxServerViaSSH','SSH_LINUX_UNIX','LinuxServerName','','userid','','','22','60000','no entry for password','{"SSH_IDENTITY":"full filename of the private key.  Note the key needs to be in Classic OpenSSH format (-m PEM). See overview. ","SSH_PASSPHRASE":"remove this param or leave blank if there is no passphrase"}');
INSERT INTO SERVERPROFILES VALUES ('remoteWinServer_WMIC','WMIC_WINDOWS','WinServerName','','userid','password','',NULL,NULL,'','{}');
INSERT INTO SERVERPROFILES VALUES ('localhost_WMIC_WINDOWS','WMIC_WINDOWS','localhost','','','','',NULL,NULL,'','{}');
INSERT INTO SERVERPROFILES VALUES ('localhost_WMIC_WINDOWS_HOSTID','WMIC_WINDOWS','localhost','HOSTID','','','',NULL,NULL,'''HOSTID'' will be subed <br> with computername  ','{}');
INSERT INTO SERVERPROFILES VALUES ('remoteWinServer_pwd','POWERSHELL_WINDOWS','WinServerName','','userid','password','',NULL,NULL,'win connect via user pwd','{"SECURE_KEY_ARRAY":"","SECURE_STRING_TXT":""}');
INSERT INTO SERVERPROFILES VALUES ('remoteWinServer_secureStr','POWERSHELL_WINDOWS','WinServerName','','userid','','',NULL,NULL,'win connect via secure string','{"SECURE_KEY_ARRAY":"","SECURE_STRING_TXT":"secure-string-goes-here"}');
INSERT INTO SERVERPROFILES VALUES ('remoteWinServer_secureStr_key','POWERSHELL_WINDOWS','WinServerName','','userid','','',NULL,NULL,'win connect via secure string using user defined key <br>(a sample key shown)','{"SECURE_KEY_ARRAY":"101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116","SECURE_STRING_TXT":"secure-string-built-using-key-goes-here"}');


-- changed commands --
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumGenJmeterReport';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumTrendsLoad';

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','POWERSHELL_WINDOWS','Start-Process -FilePath ''${METRICS_BASE_DIR}\..\bin\TestRunWIN-DataHunter-Selenium-DeployAndExecute.bat''','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in mark59-datahunter-samples ','[]');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\..\mark59-results-splitter;
Start-Process -FilePath ''.\CreateDataHunterJmeterReports.bat''
','N','','[]');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\..\bin;
Start-Process -FilePath ''.\TestRunWIN-DataHunter-Selenium-TrendsLoad.bat'' -ArgumentList ''${DATABASE}''
','N','','["DATABASE"]');

-- new commands --
INSERT INTO COMMANDS VALUES ('LINUX_free_m_1_1_ViaSSH','SSH_LINUX_UNIX','free -m 1 1','N','linux memory','["SSH_IDENTITY","SSH_PASSPHRASE"]');
INSERT INTO COMMANDS VALUES ('WIN_Core','POWERSHELL_WINDOWS','if (''${PROFILE_SERVER}'' -eq ''localhost''){

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
INSERT INTO COMMANDS VALUES ('WIN_DiskSpace_C','POWERSHELL_WINDOWS','if (''${PROFILE_SERVER}'' -eq ''localhost''){

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
INSERT INTO COMMANDS VALUES ('WIN_PerfRawData','POWERSHELL_WINDOWS','if (''${PROFILE_SERVER}'' -eq ''localhost''){

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



-- new command parsers --
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('FreePhysicalDiskSpaceGB_C','DATAPOINT','FreePhysicalDiskSpaceGB_C','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "FreeDiskSpace[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "FreeDiskSpace[", "]");  
}
return extractedMetric; ','..FreeDiskSpace[627].. ','any debug output (without the square brackets bit)
FreeDiskSpace[627]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreePhysicalG_PS','MEMORY','FreePhysicalG','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "FreePhysicalMemory[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "FreePhysicalMemory[", "]");  
}
return extractedMetric; ','..FreePhysicalMemory[14].. ','get remote server metric using using secure string with key
CPU[46] FreePhysicalMemory[14] FreeVirtualMemory[18] FreeDiskSpace[32]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreeVirtualG_PS','MEMORY','FreeVirtualG','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "FreeVirtualMemory[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "FreeVirtualMemory[", "]");  
}
return extractedMetric; ','..FreeVirtualMemory[18]..','get remote server metric using using secure string with key
CPU[46] FreePhysicalMemory[14] FreeVirtualMemory[18] FreeDiskSpace[32]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Cpu_Qlen_PS','DATAPOINT','Cpu_Qlen','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "CPUQlen[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "CPUQlen[", "]");  
}
return extractedMetric; ','..CPUQlen[1234].. ','some text
 CPUQlen[1234] Processes[367]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Cpu_Util_PS','CPU_UTIL','','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "CPU[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "CPU[", "]");  
}
return extractedMetric; ','..CPU[14].. ','get remote server metric using using secure string with key
CPU[46] FreePhysicalMemory[14] FreeVirtualMemory[18] FreeDiskSpace[32]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Processes_PS','DATAPOINT','Processes','import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
String extractedMetric = "-1";
if (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, "Processes[", "]"))){
    extractedMetric = StringUtils.substringBetween(commandResponse, "Processes[", "]");  
}
return extractedMetric; ','..Processes[367].. ','some text
 CPUQlen[1234] Processes[367]');
 
-- repointed profile to command links --
DELETE FROM SERVERCOMMANDLINKS WHERE SERVER_PROFILE_NAME = 'localhost_WINDOWS';
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS','WIN_Core');

DELETE FROM SERVERCOMMANDLINKS WHERE SERVER_PROFILE_NAME = 'localhost_WINDOWS_HOSTID';
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WIN_Core');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WIN_DiskSpace_C');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WIN_PerfRawData');


-- new profile to command links --
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS','FreePhysicalMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS','FreeVirtualMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS','WinCpuCmd');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS_HOSTID','FreePhysicalMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS_HOSTID','FreeVirtualMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WMIC_WINDOWS_HOSTID','WinCpuCmd');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteLinuxServerViaSSH','LINUX_free_m_1_1_ViaSSH');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_WMIC','FreePhysicalMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_WMIC','FreeVirtualMemory');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_WMIC','WinCpuCmd');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_pwd','WIN_Core');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_pwd','WIN_DiskSpace_C');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_pwd','WIN_PerfRawData');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr','WIN_Core');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr','WIN_DiskSpace_C');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr','WIN_PerfRawData');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr_key','WIN_Core');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr_key','WIN_DiskSpace_C');
INSERT INTO SERVERCOMMANDLINKS VALUES ('remoteWinServer_secureStr_key','WIN_PerfRawData');

-- new command to parser links --

INSERT INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1_ViaSSH','LINUX_Memory_freeG');
INSERT INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1_ViaSSH','LINUX_Memory_totalG');
INSERT INTO COMMANDPARSERLINKS VALUES ('LINUX_free_m_1_1_ViaSSH','LINUX_Memory_usedG');
INSERT INTO COMMANDPARSERLINKS VALUES ('WIN_Core','Cpu_Util_PS');
INSERT INTO COMMANDPARSERLINKS VALUES ('WIN_Core','Memory_FreePhysicalG_PS');
INSERT INTO COMMANDPARSERLINKS VALUES ('WIN_Core','Memory_FreeVirtualG_PS');
INSERT INTO COMMANDPARSERLINKS VALUES ('WIN_DiskSpace_C','FreePhysicalDiskSpaceGB_C');
INSERT INTO COMMANDPARSERLINKS VALUES ('WIN_PerfRawData','Cpu_Qlen_PS');
INSERT INTO COMMANDPARSERLINKS VALUES ('WIN_PerfRawData','Processes_PS');
