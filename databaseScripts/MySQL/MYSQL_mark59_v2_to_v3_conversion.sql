--
--
--  Only required to be executed when converting an existing Mark59 v2.x databse to v3.0 
--
--
--
--  The Mark50 v3.0 release has renamed the 'pvmetrics' database to 'metricsdb', and made some changes to the tables and data
--  
--  To do the change-over for an existing application do the following:
--
--  	1. Take a backup of your pvmetrics database (just in case!)
--		2. Run the MYSQLmetricsDataBaseCreation.sql script in this folder (creates the new metricsdb database with sample data in it)
--		3. Run this script (copies your data over the sample data, into the new metricsdb database)
--      4. Once all looks OK, you can drop the pvmetrics database  
--        

SET SQL_SAFE_UPDATES = 0;

--  ** align the pvmetrics tables with the new metricsdb formats, so bulk copies can be done ** 
--  
--  The Comments column on th metricsdb applications table has been increase to 256 chars (from 128)
--
ALTER TABLE `pvmetrics`.`APPLICATIONS` CHANGE COLUMN `COMMENT` `COMMENT` VARCHAR(256) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_bin' NULL DEFAULT NULL ;
--  
--  The col of the SLA table changed to APPLICATION` FROM SLA_APPLICATION_KEY
--
ALTER TABLE `pvmetrics`.`SLA` CHANGE COLUMN `SLA_APPLICATION_KEY` `APPLICATION` VARCHAR(32) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_bin' NOT NULL DEFAULT '' ;
--  
--  Adding an Active flage to metrics sla
--
ALTER TABLE `pvmetrics`.`METRICSLA` ADD COLUMN `IS_ACTIVE` CHAR(1)  CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_bin' NULL DEFAULT NULL AFTER `SLA_MAX`;
update pvmetrics.METRICSLA  SET `IS_ACTIVE` = 'Y' 

--  
--  Adding an Comment col to sla
--
ALTER TABLE `pvmetrics`.`SLA` ADD COLUMN `COMMENT` VARCHAR(128) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_bin' NULL DEFAULT NULL AFTER `SLA_REF_URL`;
update pvmetrics.SLA  SET `COMMENT` = '' 



delete FROM metricsdb.applications;
insert into metricsdb.applications select * from pvmetrics.applications;   
delete FROM metricsdb.eventmapping;
insert into metricsdb.eventmapping select * from pvmetrics.eventmapping;  
-- **  graphmapping data hase been improved - you will need to do a manual merge if you have updated the graph mapping table ** 
--delete FROM metricsdb.graphmapping;
--insert into metricsdb.graphmapping select * from pvmetrics.graphmapping;   
delete FROM metricsdb.metricsla;
insert into metricsdb.metricsla select * from pvmetrics.metricsla;   
delete FROM metricsdb.runs;
insert into metricsdb.runs select * from pvmetrics.runs;   
delete FROM metricsdb.sla;
insert into metricsdb.sla select * from pvmetrics.sla;  
delete FROM metricsdb.transaction;
insert into metricsdb.transaction select * from pvmetrics.transaction; 
-- optional (should not be necessary - this table just temporarily holds detailed data for an applications last run)  : 
-- delete FROM metricsdb.testtransactions; 
-- insert into metricsdb.testtransactions select * from pvmetrics.testtransactions;  

