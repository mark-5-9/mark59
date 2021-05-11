

**********************************************
**
**  --- PLEASE REVIEW THE MYSQL 3.0 TO 3.1 SQL FILE TO SEE DETIALS OF THE NEW COLUMNS AND THEIR POSITIONS ---
**
**
**
**  mark59servermetricswebdb TABLE serverprofiles NEEDS TO BE RE-CREATED AS POSTGRESS DOES NOT ALLOW FOR A NEW COLUMN OTHER THAN AT THE END OF THE TABLE
**
**  ie, run 'CREATE TABLE IF NOT EXISTS SERVERPROFILES....' 
**
**  ----------------------------------------------
**
**  metricsdb  TABLE runs  NEEDS TO BE RE-CREATED AS POSTGRESS DOES NOT ALLOW FOR A NEW COLUMN OTHER THAN AT THE END OF THE TABLE
**
**  suggestion : save off data (sql format), edit data by inserting ,'N' at the positions of the new columnn (ignore 
**
**   note:  this table also has a column re-rame (see below..LRS_FILENAME TO RUN_REFERENCE)
**
**  ----------------------------------------------
**
**  metrics db TABLE transaction  NEEDS TO BE RE-CREATED AS POSTGRESS DOES NOT ALLOW FOR A NEW COLUMN OTHER THAN AT THE END OF THE TABLE
**
**  suggestion : save off data (sql format), edit data by insertin ',-1.000,-1.000,' at the positions of the new columnns
**    txn_95th and txn_99th 
**
**  ----------------------------------------------
**
**  metricsdb TABLE sla  NEEDS TO BE RE-CREATED AS POSTGRESS DOES NOT ALLOW FOR A NEW COLUMN OTHER THAN AT THE END OF THE TABLE
**
**  suggestion : save off data (sql format), edit sql data by inserting ',0.0' at the postion of the new columnn extra_num
**   and ',-1.000,-1.000,' at the positions of the new columnns SLA_95TH_RESPONSE,SLA_99TH_RESPONSE
**
**  ----------------------------------------------
**
**  metricsdb : Note/save  any custom graphs off GRAPHMAPPING and re-enter/re-insert
**
**
*************************************************


-- mark59servermetricswebdb changes

ALTER TABLE public.COMMANDS alter COLUMN COMMAND SET DATA TYPE character varying(8192)
ALTER TABLE public.COMMANDS ADD COLUMN PARAM_ANMES character varying(1000) COLLATE pg_catalog."default" DEFAULT NULL;


-- metricsdb changes

ALTER TABLE public.RUNS RENAME COLUMN LRS_FILENAME TO RUN_REFERENCE;

ALTER TABLE public.GRAPHMAPPING alter COLUMN VALUE_DERIVATION set data type character varying(2048);

DELETE FROM GRAPHMAPPING;
INSERT INTO GRAPHMAPPING VALUES (10,'TXN_90TH','TRANSACTION','90th','secs','SELECT TXN_ID, TXN_90TH - TXN_STD_DEVIATION AS BAR_MIN, TXN_90TH+TXN_STD_DEVIATION AS BAR_MAX FROM TRANSACTION WHERE TXN_TYPE = ''TRANSACTION'' AND RUN_TIME = @runTime AND APPLICATION = @application','+/- One Standard Deviation from 90th (secs)','');
INSERT INTO GRAPHMAPPING VALUES (20,'TXN_95TH','TRANSACTION','95th','secs','SELECT TXN_ID, TXN_90TH AS BAR_MIN, TXN_99TH BAR_MAX FROM TRANSACTION WHERE TXN_TYPE = ''TRANSACTION'' AND RUN_TIME = @runTime AND APPLICATION = @application','90th to 99th Percentiles','');
INSERT INTO GRAPHMAPPING VALUES (30,'TXN_99TH','TRANSACTION','99th','secs','SELECT TXN_ID, TXN_90TH AS BAR_MIN, TXN_99TH BAR_MAX FROM TRANSACTION WHERE TXN_TYPE = ''TRANSACTION'' AND RUN_TIME = @runTime AND APPLICATION = @application','90th to 99th Percentiles','');
INSERT INTO GRAPHMAPPING VALUES (40,'TXN_PASS','TRANSACTION','Pass','txn count','select TXN_ID, SLA_PASS_COUNT-SLA_PASS_COUNT*SLA_PASS_COUNT_VARIANCE_PERCENT*0.01 as BAR_MIN,SLA_PASS_COUNT+SLA_PASS_COUNT*SLA_PASS_COUNT_VARIANCE_PERCENT*0.01 as BAR_MAX from SLA where SLA_PASS_COUNT>0 and SLA.APPLICATION = @application ','SLA Pass Rates :  Minimum to Maximum','');
INSERT INTO GRAPHMAPPING VALUES (50,'TXN_FAIL','TRANSACTION','Fail','txn count','','','');
INSERT INTO GRAPHMAPPING VALUES (60,'TXN_FAIL_PERCENT','TRANSACTION','COALESCE( 100*TXN_FAIL/(TXN_PASS+TXN_FAIL) , -1)','% txn failed',NULL,NULL,'calculated  using pass and fail');
INSERT INTO GRAPHMAPPING VALUES (70,'TXN_STOP','TRANSACTION','Stop','txn count',NULL,NULL,'');
INSERT INTO GRAPHMAPPING VALUES (80,'TXN_MINIMUM','TRANSACTION','Minimum','secs',NULL,NULL,'');
INSERT INTO GRAPHMAPPING VALUES (90,'TXN_AVERAGE','TRANSACTION','Average','secs',' SELECT TXN_ID, TXN_AVERAGE - TXN_STD_DEVIATION AS BAR_MIN, TXN_AVERAGE + TXN_STD_DEVIATION AS BAR_MAX FROM TRANSACTION WHERE TXN_TYPE = ''TRANSACTION'' AND RUN_TIME = @runTime AND APPLICATION = @application','One Standard Deviation from Ave (secs)','');
INSERT INTO GRAPHMAPPING VALUES (100,'TXN_MAXIMUM','TRANSACTION','Maximum','secs',NULL,NULL,'');
INSERT INTO GRAPHMAPPING VALUES (110,'TXN_STD_DEVIATION','TRANSACTION','StdDeviation','StdDeviation',NULL,NULL,'');
INSERT INTO GRAPHMAPPING VALUES (120,'DATAPOINT_AVE','DATAPOINT','Average','ave','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA WHERE METRIC_TXN_TYPE = ''DATAPOINT'' AND VALUE_DERIVATION = ''Average'' AND APPLICATION = @application','Datapoint average : Minimum to Maximum','');
INSERT INTO GRAPHMAPPING VALUES (130,'DATAPOINT_SUM','DATAPOINT','Sum','total sum','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA WHERE METRIC_TXN_TYPE = ''DATAPOINT'' AND VALUE_DERIVATION = ''Sum'' AND APPLICATION = @application','Datapoint sum : Minimum to Maximum','sum of all the dp values');
INSERT INTO GRAPHMAPPING VALUES (140,'DATAPOINT_LAST','DATAPOINT','Last','final datapoint','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA WHERE METRIC_TXN_TYPE = ''DATAPOINT'' AND VALUE_DERIVATION = ''Last'' AND APPLICATION = @application','Final Datapoint : Minimum to Maximum','final datapoint of a set of datapoints');
INSERT INTO GRAPHMAPPING VALUES (150,'CPU_BOTTLENECKED','CPU_UTIL','PercentOver90','% time server bottlednecked (utilization > 90%)','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA WHERE METRIC_TXN_TYPE = ''CPU_UTIL'' AND VALUE_DERIVATION = ''PercentOver90'' AND APPLICATION = @application ','SLA % Time Server Bottlenecked  :  Minimum to Maximum','server botlenecked indicator');
INSERT INTO GRAPHMAPPING VALUES (160,'CPU_UTIL','CPU_UTIL','Average','% utilization','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA WHERE METRIC_TXN_TYPE = ''CPU_UTIL'' AND VALUE_DERIVATION = ''Average'' AND APPLICATION = @application','SLA % Server Utilizations  :  Minimum to Maximum','user util (= 100-idleW');
INSERT INTO GRAPHMAPPING VALUES (170,'MEMORY','MEMORY','Average','','SELECT METRIC_NAME AS TXN_ID, SLA_MIN AS BAR_MIN, SLA_MAX AS BAR_MAX FROM METRICSLA WHERE METRIC_TXN_TYPE = ''MEMORY'' AND VALUE_DERIVATION = ''Average'' AND APPLICATION = @application','Memory Metrics : Minimum to Maximum','Uom may vary');


