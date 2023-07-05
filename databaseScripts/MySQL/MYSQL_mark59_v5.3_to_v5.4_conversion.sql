
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

SET SQL_SAFE_UPDATES = 0;

USE mark59metricsdb;

-- *************************************************************************************
--  Increased column lengths
-- *************************************************************************************

ALTER TABLE COMMANDS CHANGE COLUMN `COMMAND` `COMMAND` MEDIUMTEXT NOT NULL;

ALTER TABLE COMMANDRESPONSEPARSERS CHANGE COLUMN `SCRIPT` `SCRIPT` MEDIUMTEXT NOT NULL;
ALTER TABLE COMMANDRESPONSEPARSERS 
 CHANGE COLUMN `SAMPLE_COMMAND_RESPONSE` `SAMPLE_COMMAND_RESPONSE` VARCHAR(8196) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_bin' NOT NULL ;

ALTER TABLE SERVERPROFILES
  CHANGE COLUMN `PARAMETERS` `PARAMETERS` VARCHAR(8196) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_bin' NULL DEFAULT NULL ;

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
INSERT INTO SERVERPROFILES VALUES ('DemoWIN-DataHunterSeleniumTrendsLoad','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'Loads Trend Analysis (MYSQL database). See: <br>http://localhost:8083/mark59-trends/trending?reqApp=DataHunter','{\"DATABASE\":\"MYSQL\"}');
INSERT INTO SERVERPROFILES VALUES ('localhost_WINDOWS','POWERSHELL_WINDOWS','localhost','','','','',NULL,NULL,'','{\"SECURE_KEY_ARRAY\":\"\",\"SECURE_STRING_TXT\":\"\"}');
INSERT INTO SERVERPROFILES VALUES ('localhost_WINDOWS_HOSTID','POWERSHELL_WINDOWS','localhost','HOSTID','','','',NULL,NULL,'\'HOSTID\' will be subed with computername','{\"SECURE_KEY_ARRAY\":\"\",\"SECURE_STRING_TXT\":\"\"}');

-- new server profiles --

INSERT INTO SERVERPROFILES VALUES ('remoteLinuxServerViaSSH','SSH_LINUX_UNIX','LinuxServerName','','userid','','','22','60000','no entry for password','{\"SSH_IDENTITY\":\"full filename of the private key.  Note the key needs to be in Classic OpenSSH format (-m PEM). See overview. \",\"SSH_PASSPHRASE\":\"remove this param or leave blank if there is no passphrase\"}');
INSERT INTO SERVERPROFILES VALUES ('remoteWinServer_WMIC','WMIC_WINDOWS','WinServerName','','userid','password','',NULL,NULL,'','{}');
INSERT INTO SERVERPROFILES VALUES ('localhost_WMIC_WINDOWS','WMIC_WINDOWS','localhost','','','','',NULL,NULL,'','{}');
INSERT INTO SERVERPROFILES VALUES ('localhost_WMIC_WINDOWS_HOSTID','WMIC_WINDOWS','localhost','HOSTID','','','',NULL,NULL,'\'HOSTID\' will be subed <br> with computername  ','{}');
INSERT INTO SERVERPROFILES VALUES ('remoteWinServer_pwd','POWERSHELL_WINDOWS','WinServerName','','userid','password','',NULL,NULL,'win connect via user pwd','{\"SECURE_KEY_ARRAY\":\"\",\"SECURE_STRING_TXT\":\"\"}');
INSERT INTO SERVERPROFILES VALUES ('remoteWinServer_secureStr','POWERSHELL_WINDOWS','WinServerName','','userid','','',NULL,NULL,'win connect via secure string','{\"SECURE_KEY_ARRAY\":\"\",\"SECURE_STRING_TXT\":\"secure-string-goes-here\"}');
INSERT INTO SERVERPROFILES VALUES ('remoteWinServer_secureStr_key','POWERSHELL_WINDOWS','WinServerName','','userid','','',NULL,NULL,'win connect via secure string using user defined key <br>(a sample key shown)','{\"SECURE_KEY_ARRAY\":\"101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116\",\"SECURE_STRING_TXT\":\"secure-string-built-using-key-goes-here\"}');


-- changed commands --
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumDeployAndExecute';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumGenJmeterReport';
DELETE FROM COMMANDS WHERE COMMAND_NAME = 'DataHunterSeleniumTrendsLoad';

INSERT INTO COMMANDS VALUES ('DataHunterSeleniumDeployAndExecute','POWERSHELL_WINDOWS','Start-Process -FilePath \'${METRICS_BASE_DIR}\\..\\bin\\TestRunWIN-DataHunter-Selenium-DeployAndExecute.bat\'','N','refer DeployDataHunterTestArtifactsToJmeter.bat and DataHunterExecuteJmeterTest.bat in mark59-datahunter-samples ','[]');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumGenJmeterReport','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\\..\\mark59-results-splitter;\r\nStart-Process -FilePath \'.\\CreateDataHunterJmeterReports.bat\'\r\n','N','','[]');
INSERT INTO COMMANDS VALUES ('DataHunterSeleniumTrendsLoad','POWERSHELL_WINDOWS','cd -Path ${METRICS_BASE_DIR}\\..\\bin;\r\nStart-Process -FilePath \'.\\TestRunWIN-DataHunter-Selenium-TrendsLoad.bat\' -ArgumentList \'${DATABASE}\'\r\n','N','','[\"DATABASE\"]');

-- new commands --
INSERT INTO COMMANDS VALUES ('LINUX_free_m_1_1_ViaSSH','SSH_LINUX_UNIX','free -m 1 1','N','linux memory','[\"SSH_IDENTITY\",\"SSH_PASSPHRASE\"]');
INSERT INTO COMMANDS VALUES ('WIN_Core','POWERSHELL_WINDOWS','if (\'${PROFILE_SERVER}\' -eq \'localhost\'){\r\n\r\n    Write-Output \\\"get localhost metric \\\";\r\n    $ComputerCPU = Get-WmiObject -Class win32_processor -ErrorAction Stop;\r\n    $ComputerMemory = Get-WmiObject -Class win32_operatingsystem -ErrorAction Stop;\r\n\r\n} elseif ((![string]::IsNullOrEmpty(\'${SECURE_STRING_TXT}\')) -and ([string]::IsNullOrEmpty(\'${SECURE_KEY_ARRAY}\'))){\r\n\r\n    Write-Output \\\"  cpu using secure string \\\"; \r\n    <# To get the SECURE_STRING_TXT parameter value to store (no key):\r\n        $secureString = ConvertTo-SecureString \'plain-text-password\' -AsPlainText -Force\r\n        $secureStringTxt = Convertfrom-SecureString $secureString \r\n    #>\r\n\r\n    $securePwd = \'${SECURE_STRING_TXT}\' | ConvertTo-SecureString; \r\n    $credential = New-Object System.Management.Automation.PSCredential (\'${PROFILE_USERNAME}\', $securePwd);\r\n \r\n} elseif ((![string]::IsNullOrEmpty(\'${SECURE_STRING_TXT}\')) -and (![string]::IsNullOrEmpty(\'${SECURE_KEY_ARRAY}\'))){\r\n\r\n    Write-Output \\\"get remote server metric using using secure string with key \\\";\r\n    <# To get the SECURE_STRING_TXT parameter value to store (using key), \r\n        $secureString = ConvertTo-SecureString \'plain-text-password\' -AsPlainText -Force\r\n        [Byte[]] $key = ( your-comma-delimited-list-of-key-values-between-0-255 )\r\n        $secureStringTxt = Convertfrom-SecureString $secureString -key $key\r\n    #>\r\n \r\n    [Byte[]] $key = @(${SECURE_KEY_ARRAY}); \r\n    $securePwd = \'${SECURE_STRING_TXT}\' | ConvertTo-SecureString -Key $key;\r\n    $credential = New-Object System.Management.Automation.PSCredential (\'${PROFILE_USERNAME}\', $securePwd);\r\n  \r\n} else {\r\n\r\n    Write-Output \\\"get remote server metric using profile username and password \\\";\r\n    $password = ConvertTo-SecureString \'${PROFILE_PASSWORD}\' -AsPlainText -Force; \r\n    $credential = New-Object System.Management.Automation.PSCredential (\'${PROFILE_USERNAME}\', $password);\r\n}\r\n\r\nif (\'${PROFILE_SERVER}\' -ne \'localhost\'){ \r\n    $ComputerCPU = Get-WmiObject -Credential $credential -ComputerName ${PROFILE_SERVER} -Class win32_processor -ErrorAction Stop;\r\n    $ComputerMemory = Get-WmiObject -Credential $credential -ComputerName ${PROFILE_SERVER} -Class win32_operatingsystem -ErrorAction Stop;\r\n}\r\n\r\n$CPUUtil =  [math]::round(($ComputerCPU | Measure-Object -Property LoadPercentage -Average | Select-Object Average).Average);\r\n$FreePhysicalMemory= [math]::truncate($ComputerMemory.FreePhysicalMemory / 1MB);\r\n$FreeVirtualMemory = [math]::truncate($ComputerMemory.FreeVirtualMemory / 1MB);\r\n     \r\nWrite-Host \" CPU[\"$CPUUtil\"] FreePhysicalMemory[\"$FreePhysicalMemory\"] FreeVirtualMemory[\"$FreeVirtualMemory\"]\";\r\n','N','you should <# comment #> the debug Write-Output statements out to run in a real test :)','[\"SECURE_KEY_ARRAY\",\"SECURE_STRING_TXT\"]');
INSERT INTO COMMANDS VALUES ('WIN_DiskSpace_C','POWERSHELL_WINDOWS','if (\'${PROFILE_SERVER}\' -eq \'localhost\'){\r\n\r\n    $Drive =  Get-WMIObject Win32_LogicalDisk -Filter \\\"DeviceID=\'C:\'\\\" ;\r\n\r\n} elseif ((![string]::IsNullOrEmpty(\'${SECURE_STRING_TXT}\')) -and ([string]::IsNullOrEmpty(\'${SECURE_KEY_ARRAY}\'))){\r\n\r\n    $securePwd = \'${SECURE_STRING_TXT}\' | ConvertTo-SecureString; \r\n    $credential = New-Object System.Management.Automation.PSCredential (\'${PROFILE_USERNAME}\', $securePwd);\r\n \r\n} elseif ((![string]::IsNullOrEmpty(\'${SECURE_STRING_TXT}\')) -and (![string]::IsNullOrEmpty(\'${SECURE_KEY_ARRAY}\'))){\r\n\r\n    [Byte[]] $key = @(${SECURE_KEY_ARRAY}); \r\n    $securePwd = \'${SECURE_STRING_TXT}\' | ConvertTo-SecureString -Key $key;\r\n    $credential = New-Object System.Management.Automation.PSCredential (\'${PROFILE_USERNAME}\', $securePwd);\r\n  \r\n} else {\r\n\r\n    $password = ConvertTo-SecureString \'${PROFILE_PASSWORD}\' -AsPlainText -Force; \r\n    $credential = New-Object System.Management.Automation.PSCredential (\'${PROFILE_USERNAME}\', $password);\r\n}\r\n\r\nif (\'${PROFILE_SERVER}\' -ne \'localhost\'){ \r\n    $Drive = Get-WMIObject -Credential $credential -ComputerName ${PROFILE_SERVER} Win32_LogicalDisk -Filter \\\"DeviceID=\'C:\'\\\" ;\r\n}\r\n\r\n$FreeDiskSpace = $Drive | ForEach-Object {[math]::truncate($_.freespace / 1GB)};\r\nWrite-Host \"FreeDiskSpace[\"$FreeDiskSpace\"]\" ;','N','','[\"SECURE_KEY_ARRAY\",\"SECURE_STRING_TXT\"]');
INSERT INTO COMMANDS VALUES ('WIN_PerfRawData','POWERSHELL_WINDOWS','if (\'${PROFILE_SERVER}\' -eq \'localhost\'){\r\n\r\n    <# Write-Output \\\"get localhost metric \\\"; #>\r\n    $PerfRawData = Get-WmiObject Win32_PerfRawData_PerfOS_System;\r\n\r\n} elseif ((![string]::IsNullOrEmpty(\'${SECURE_STRING_TXT}\')) -and ([string]::IsNullOrEmpty(\'${SECURE_KEY_ARRAY}\'))){\r\n\r\n    <# Write-Output \\\"  cpu using secure string \\\"; #>\r\n    $securePwd = \'${SECURE_STRING_TXT}\' | ConvertTo-SecureString; \r\n    $credential = New-Object System.Management.Automation.PSCredential (\'${PROFILE_USERNAME}\', $securePwd);\r\n \r\n} elseif ((![string]::IsNullOrEmpty(\'${SECURE_STRING_TXT}\')) -and (![string]::IsNullOrEmpty(\'${SECURE_KEY_ARRAY}\'))){\r\n\r\n    <# Write-Output \\\"get remote server metric using using secure string with key \\\"; #>\r\n    [Byte[]] $key = @(${SECURE_KEY_ARRAY}); \r\n    $securePwd = \'${SECURE_STRING_TXT}\' | ConvertTo-SecureString -Key $key;\r\n    $credential = New-Object System.Management.Automation.PSCredential (\'${PROFILE_USERNAME}\', $securePwd);\r\n  \r\n} else {\r\n\r\n    <# Write-Output \\\"get remote server metric using profile username and password \\\"; #>\r\n    $password = ConvertTo-SecureString \'${PROFILE_PASSWORD}\' -AsPlainText -Force; \r\n    $credential = New-Object System.Management.Automation.PSCredential (\'${PROFILE_USERNAME}\', $password);\r\n}\r\n\r\nif (\'${PROFILE_SERVER}\' -ne \'localhost\'){ \r\n    $PerfRawData = Get-WmiObject -Credential $credential -ComputerName ${PROFILE_SERVER} Win32_PerfRawData_PerfOS_System\r\n}\r\n\r\n$CPUQlen = [math]::round(($PerfRawData | Measure-Object -Property ProcessorQueueLength -Average | Select-Object Average).Average);\r\n$Processes = [math]::round(($PerfRawData | Measure-Object -Property Processes -Average | Select-Object Average).Average);\r\nWrite-Host \" CPUQlen[\"$CPUQlen\"] Processes[\"$Processes\"] \" ;','N','','[\"SECURE_KEY_ARRAY\",\"SECURE_STRING_TXT\"]');

-- new command parsers --
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('FreePhysicalDiskSpaceGB_C','DATAPOINT','FreePhysicalDiskSpaceGB_C','import org.apache.commons.lang3.StringUtils;\r\nimport org.apache.commons.lang3.math.NumberUtils;\r\nString extractedMetric = \"-1\";\r\nif (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, \"FreeDiskSpace[\", \"]\"))){\r\n    extractedMetric = StringUtils.substringBetween(commandResponse, \"FreeDiskSpace[\", \"]\");  \r\n}\r\nreturn extractedMetric; ','..FreeDiskSpace[627].. ','any debug output (without the square brackets bit)\r\nFreeDiskSpace[627]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreePhysicalG_PS','MEMORY','FreePhysicalG','import org.apache.commons.lang3.StringUtils;\r\nimport org.apache.commons.lang3.math.NumberUtils;\r\nString extractedMetric = \"-1\";\r\nif (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, \"FreePhysicalMemory[\", \"]\"))){\r\n    extractedMetric = StringUtils.substringBetween(commandResponse, \"FreePhysicalMemory[\", \"]\");  \r\n}\r\nreturn extractedMetric; ','..FreePhysicalMemory[14].. ','get remote server metric using using secure string with key\r\nCPU[46] FreePhysicalMemory[14] FreeVirtualMemory[18] FreeDiskSpace[32]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Memory_FreeVirtualG_PS','MEMORY','FreeVirtualG','import org.apache.commons.lang3.StringUtils;\r\nimport org.apache.commons.lang3.math.NumberUtils;\r\nString extractedMetric = \"-1\";\r\nif (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, \"FreeVirtualMemory[\", \"]\"))){\r\n    extractedMetric = StringUtils.substringBetween(commandResponse, \"FreeVirtualMemory[\", \"]\");  \r\n}\r\nreturn extractedMetric; ','..FreeVirtualMemory[18]..','get remote server metric using using secure string with key\r\nCPU[46] FreePhysicalMemory[14] FreeVirtualMemory[18] FreeDiskSpace[32]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Cpu_Qlen_PS','DATAPOINT','Cpu_Qlen','import org.apache.commons.lang3.StringUtils;\r\nimport org.apache.commons.lang3.math.NumberUtils;\r\nString extractedMetric = \"-1\";\r\nif (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, \"CPUQlen[\", \"]\"))){\r\n    extractedMetric = StringUtils.substringBetween(commandResponse, \"CPUQlen[\", \"]\");  \r\n}\r\nreturn extractedMetric; ','..CPUQlen[1234].. ','some text\r\n CPUQlen[1234] Processes[367]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Cpu_Util_PS','CPU_UTIL','','import org.apache.commons.lang3.StringUtils;\r\nimport org.apache.commons.lang3.math.NumberUtils;\r\nString extractedMetric = \"-1\";\r\nif (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, \"CPU[\", \"]\"))){\r\n    extractedMetric = StringUtils.substringBetween(commandResponse, \"CPU[\", \"]\");  \r\n}\r\nreturn extractedMetric; ','..CPU[14].. ','get remote server metric using using secure string with key\r\nCPU[46] FreePhysicalMemory[14] FreeVirtualMemory[18] FreeDiskSpace[32]');
INSERT INTO COMMANDRESPONSEPARSERS VALUES ('Processes_PS','DATAPOINT','Processes','import org.apache.commons.lang3.StringUtils;\r\nimport org.apache.commons.lang3.math.NumberUtils;\r\nString extractedMetric = \"-1\";\r\nif (NumberUtils.isParsable(StringUtils.substringBetween(commandResponse, \"Processes[\", \"]\"))){\r\n    extractedMetric = StringUtils.substringBetween(commandResponse, \"Processes[\", \"]\");  \r\n}\r\nreturn extractedMetric; ','..Processes[367].. ','some text\r\n CPUQlen[1234] Processes[367]');


-- repointed profile to command links --
DELETE FROM SERVERCOMMANDLINKS WHERE SERVER_PROFILE_NAME = 'localhost_WINDOWS';
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS','WIN_Core');

DELETE FROM SERVERCOMMANDLINKS WHERE SERVER_PROFILE_NAME = 'localhost_WINDOWS_HOSTID';
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WIN_Core');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WIN_DiskSpace_C');
INSERT INTO SERVERCOMMANDLINKS VALUES ('localhost_WINDOWS_HOSTID','WIN_PerfRawData');

-- new profile to command links --
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('localhost_WMIC_WINDOWS','FreePhysicalMemory');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('localhost_WMIC_WINDOWS','FreeVirtualMemory');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('localhost_WMIC_WINDOWS','WinCpuCmd');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('localhost_WMIC_WINDOWS_HOSTID','FreePhysicalMemory');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('localhost_WMIC_WINDOWS_HOSTID','FreeVirtualMemory');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('localhost_WMIC_WINDOWS_HOSTID','WinCpuCmd');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteLinuxServerViaSSH','LINUX_free_m_1_1_ViaSSH');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_WMIC','FreePhysicalMemory');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_WMIC','FreeVirtualMemory');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_WMIC','WinCpuCmd');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_pwd','WIN_Core');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_pwd','WIN_DiskSpace_C');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_pwd','WIN_PerfRawData');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_secureStr','WIN_Core');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_secureStr','WIN_DiskSpace_C');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_secureStr','WIN_PerfRawData');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_secureStr_key','WIN_Core');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_secureStr_key','WIN_DiskSpace_C');
INSERT INTO `SERVERCOMMANDLINKS` VALUES ('remoteWinServer_secureStr_key','WIN_PerfRawData');

-- new command to parser links --

INSERT INTO `COMMANDPARSERLINKS` VALUES ('LINUX_free_m_1_1_ViaSSH','LINUX_Memory_freeG');
INSERT INTO `COMMANDPARSERLINKS` VALUES ('LINUX_free_m_1_1_ViaSSH','LINUX_Memory_totalG');
INSERT INTO `COMMANDPARSERLINKS` VALUES ('LINUX_free_m_1_1_ViaSSH','LINUX_Memory_usedG');
INSERT INTO `COMMANDPARSERLINKS` VALUES ('WIN_Core','Cpu_Util_PS');
INSERT INTO `COMMANDPARSERLINKS` VALUES ('WIN_Core','Memory_FreePhysicalG_PS');
INSERT INTO `COMMANDPARSERLINKS` VALUES ('WIN_Core','Memory_FreeVirtualG_PS');
INSERT INTO `COMMANDPARSERLINKS` VALUES ('WIN_DiskSpace_C','FreePhysicalDiskSpaceGB_C');
INSERT INTO `COMMANDPARSERLINKS` VALUES ('WIN_PerfRawData','Cpu_Qlen_PS');
INSERT INTO `COMMANDPARSERLINKS` VALUES ('WIN_PerfRawData','Processes_PS');

