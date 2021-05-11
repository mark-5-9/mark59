
--- save any special graphmapping you have done before running, and re-enter manually or via insert ---


ALTER TABLE `mark59servermetricswebdb`.`COMMANDS` 
CHANGE COLUMN `COMMAND` `COMMAND` VARCHAR(8192) NOT NULL ;

ALTER TABLE `mark59servermetricswebdb`.`COMMANDS` 
ADD COLUMN `PARAM_NAMES` VARCHAR(1000) NULL DEFAULT NULL AFTER `COMMENT`;

ALTER TABLE `mark59servermetricswebdb`.`SERVERPROFILES` 
ADD COLUMN `PARAMETERS` VARCHAR(2000) NULL DEFAULT NULL AFTER `COMMENT`;

ALTER TABLE `mark59servermetricswebdb`.`SERVERPROFILES` 
ADD COLUMN `EXECUTOR` VARCHAR(32) NOT NULL AFTER `SERVER_PROFILE_NAME`;

SET SQL_SAFE_UPDATES = 0;
UPDATE mark59servermetricswebdb.SERVERPROFILES SET EXECUTOR = 'WMIC_WINDOWS' WHERE OPERATING_SYSTEM = 'WINDOWS'; 
UPDATE mark59servermetricswebdb.SERVERPROFILES SET EXECUTOR = 'SSH_LINIX_UNIX' WHERE OPERATING_SYSTEM LIKE '%N_X'; 
ALTER TABLE `mark59servermetricswebdb`.`SERVERPROFILES` DROP COLUMN `OPERATING_SYSTEM`;
ALTER TABLE `mark59servermetricswebdb`.`SERVERPROFILES` CHANGE COLUMN `SERVER` `SERVER` VARCHAR(64) NULL DEFAULT '' ;


ALTER TABLE `metricsdb`.`RUNS` 
CHANGE COLUMN `LRS_FILENAME` `RUN_REFERENCE` VARCHAR(128) NULL DEFAULT NULL ;

ALTER TABLE `metricsdb`.`RUNS` 
ADD COLUMN `IS_RUN_IGNORED` VARCHAR(1) NULL DEFAULT NULL AFTER `RUN_TIME`;

UPDATE `metricsdb`.`RUNS` SET IS_RUN_IGNORED = 'N'; 


ALTER TABLE `metricsdb`.`SLA` 
ADD COLUMN `SLA_95TH_RESPONSE` DECIMAL(12,3) NULL DEFAULT NULL AFTER `SLA_90TH_RESPONSE`,
ADD COLUMN `SLA_99TH_RESPONSE` DECIMAL(12,3) NULL DEFAULT NULL AFTER `SLA_95TH_RESPONSE`,
ADD COLUMN `XTRA_NUM` DECIMAL(12,3) NULL DEFAULT NULL AFTER `SLA_FAIL_PERCENT`;

UPDATE `metricsdb`.`SLA` SET SLA_95TH_RESPONSE = -1.000; 
UPDATE `metricsdb`.`SLA` SET SLA_99TH_RESPONSE = -1.000; 
UPDATE `metricsdb`.`SLA` SET XTRA_NUM = 0.000; 


ALTER TABLE `metricsdb`.`TRANSACTION` 
ADD COLUMN `TXN_95TH` DECIMAL(18,3) NOT NULL DEFAULT '-1.000' AFTER `TXN_90TH`,
ADD COLUMN `TXN_99TH` DECIMAL(18,3) NOT NULL DEFAULT '-1.000' AFTER `TXN_95TH`;

UPDATE `metricsdb`.`TRANSACTION` SET TXN_95TH = -1.000; 
UPDATE `metricsdb`.`TRANSACTION` SET TXN_99TH = -1.000; 

ALTER TABLE `metricsdb`.`GRAPHMAPPING` CHANGE COLUMN `VALUE_DERIVATION` `VALUE_DERIVATION` VARCHAR(2048) NULL DEFAULT NULL ;

DELETE FROM `GRAPHMAPPING`;
INSERT INTO `GRAPHMAPPING` VALUES (10,'TXN_90TH','TRANSACTION','90th','secs','SELECT TXN_ID, TXN_90TH - TXN_STD_DEVIATION AS BAR_MIN, TXN_90TH+TXN_STD_DEVIATION AS BAR_MAX FROM TRANSACTION WHERE TXN_TYPE = \'TRANSACTION\' AND RUN_TIME = @runTime AND APPLICATION = @application','+/- One Standard Deviation from 90th (secs)','');
INSERT INTO `GRAPHMAPPING` VALUES (20,'TXN_95TH','TRANSACTION','95th','secs','SELECT TXN_ID, TXN_90TH AS BAR_MIN, TXN_99TH BAR_MAX FROM TRANSACTION  WHERE TXN_TYPE = ''TRANSACTION'' AND RUN_TIME = @runTime AND APPLICATION = @application','90th to 99th Percentiles','');
INSERT INTO `GRAPHMAPPING` VALUES (30,'TXN_99TH','TRANSACTION','99th','secs','SELECT TXN_ID, TXN_90TH AS BAR_MIN, TXN_99TH BAR_MAX FROM TRANSACTION  WHERE TXN_TYPE = ''TRANSACTION'' AND RUN_TIME = @runTime AND APPLICATION = @application','90th to 99th Percentiles','');
INSERT INTO `GRAPHMAPPING` VALUES (40,'TXN_PASS','TRANSACTION','Pass','txn count','select TXN_ID, SLA_PASS_COUNT-SLA_PASS_COUNT*SLA_PASS_COUNT_VARIANCE_PERCENT*0.01 as BAR_MIN,SLA_PASS_COUNT+SLA_PASS_COUNT*SLA_PASS_COUNT_VARIANCE_PERCENT*0.01 as BAR_MAX from SLA where SLA_PASS_COUNT>0 and SLA.APPLICATION = @application ','SLA Pass Rates :  Minimum to Maximum','');
INSERT INTO `GRAPHMAPPING` VALUES (50,'TXN_FAIL','TRANSACTION','Fail','txn count','','','');
INSERT INTO `GRAPHMAPPING` VALUES (60,'TXN_FAIL_PERCENT','TRANSACTION','COALESCE( 100*TXN_FAIL/(TXN_PASS+TXN_FAIL) , -1)','% txn failed',NULL,NULL,'calculated  using pass and fail');
INSERT INTO `GRAPHMAPPING` VALUES (70,'TXN_STOP','TRANSACTION','Stop','txn count',NULL,NULL,'');
INSERT INTO `GRAPHMAPPING` VALUES (80,'TXN_MINIMUM','TRANSACTION','Minimum','secs',NULL,NULL,'');
INSERT INTO `GRAPHMAPPING` VALUES (90,'TXN_AVERAGE','TRANSACTION','Average','secs',' SELECT TXN_ID, TXN_AVERAGE - TXN_STD_DEVIATION AS BAR_MIN, TXN_AVERAGE + TXN_STD_DEVIATION AS BAR_MAX FROM TRANSACTION WHERE TXN_TYPE = \'TRANSACTION\' AND RUN_TIME = @runTime AND APPLICATION = @application','One Standard Deviation from Ave (secs)','');
INSERT INTO `GRAPHMAPPING` VALUES (100,'TXN_MAXIMUM','TRANSACTION','Maximum','secs',NULL,NULL,'');
INSERT INTO `GRAPHMAPPING` VALUES (110,'TXN_STD_DEVIATION','TRANSACTION','StdDeviation','StdDeviation',NULL,NULL,'');
INSERT INTO `GRAPHMAPPING` VALUES (120,'DATAPOINT_AVE','DATAPOINT','Average','ave','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA\r\nWHERE METRIC_TXN_TYPE = \'DATAPOINT\'\r\nAND VALUE_DERIVATION = \'Average\'\r\nAND APPLICATION = @application','Datapoint average : Minimum to Maximum','');
INSERT INTO `GRAPHMAPPING` VALUES (130,'DATAPOINT_SUM','DATAPOINT','Sum','total sum','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA\r\nWHERE METRIC_TXN_TYPE = \'DATAPOINT\'\r\nAND VALUE_DERIVATION = \'Sum\'\r\nAND APPLICATION = @application','Datapoint sum : Minimum to Maximum','sum of all the dp values');
INSERT INTO `GRAPHMAPPING` VALUES (140,'DATAPOINT_LAST','DATAPOINT','Last','final datapoint','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA\r\nWHERE METRIC_TXN_TYPE = \'DATAPOINT\'\r\nAND VALUE_DERIVATION = \'Last\'\r\nAND APPLICATION = @application','Final Datapoint : Minimum to Maximum','final datapoint of a set of datapoints');
INSERT INTO `GRAPHMAPPING` VALUES (150,'CPU_BOTTLENECKED','CPU_UTIL','PercentOver90','% time server bottlednecked (utilization > 90%)','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA\r\nWHERE METRIC_TXN_TYPE = \'CPU_UTIL\'\r\nAND VALUE_DERIVATION = \'PercentOver90\'\r\nAND APPLICATION = @application ','SLA % Time Server Bottlenecked  :  Minimum to Maximum','server botlenecked indicator');
INSERT INTO `GRAPHMAPPING` VALUES (160,'CPU_UTIL','CPU_UTIL','Average','% utilization','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA \r\nWHERE METRIC_TXN_TYPE = \'CPU_UTIL\'\r\n AND VALUE_DERIVATION = \'Average\' \r\n AND APPLICATION = @application','SLA % Server Utilizations  :  Minimum to Maximum','user util (= 100-idleW');
INSERT INTO `GRAPHMAPPING` VALUES (170,'MEMORY','MEMORY','Average','','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA\r\nWHERE METRIC_TXN_TYPE = \'MEMORY\'\r\nAND VALUE_DERIVATION = \'Average\'\r\nAND APPLICATION = @application','Memory Metrics : Minimum to Maximum','Uom may vary');