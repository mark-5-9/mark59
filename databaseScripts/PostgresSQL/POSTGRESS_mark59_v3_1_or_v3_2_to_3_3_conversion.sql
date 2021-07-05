-- note: No required postgres db changnes for 3.2, so this file applies to an upgrade from 3.1 or 3.2 to 3.3 

**********************************************
**
**  --- PLEASE REVIEW THE MYSQL 3.1 TO 3.2 SQL FILE AND THE CHANGES TO POSTGRESmetricsDataBaseCreation.sql FILE
**  --  TO SEE DETAILS OF THE NEW COLUMNS AND THEIR POSITIONS ---
**
**********************************************
**
**  -- metricsdb sla table NEEDS TO BE RE-CREATED AS POSTGRESS DOES NOT ALLOW FOR NEW COLUMNS OTHER THAN AT THE END OF THE TABLE
**  -- suggestion : save off data (sql format), then
**  --              edit sql data by inserting '0.000,' at the postion of the new columnn TXN_DELAY
**  --              edit sql data by inserting '0' at the postion of the new columnn XTRA_INT
**  --              note: columnn XTRA_NUM is now defined as  NOT NULL DEFAULT '0.000',
**
*************************************************
**
**  -- not required, but if you want to keep the dataHunter data the same is in the curent 'quick start' (and use new functionality): 
**  --               insert into the graphmapping table  id 15  'TXN_90TH_EX_DELAY'
**  --               insert into the graphmapping table  id 95  'TXN_MEDIAN' 
**  --               insert into the graphmapping table  id 115 'TXN_DELAY' 
**   
*************************************************

-- metricsdb transaction table changes:

**  -- suggestion : save off data (sql format), then
**  --              edit sql data by inserting '-1.000' at the postion of the new columnn TXN_MEDIAN
**  --  
**  -- not required, but if you want to keep the dataHunter data the same is in the curent 'quick start', 
**  --               change the TXN_DELAY for the most recent four SLA DataHnuter DH-lifecycle-0100-deleteMultiplePolicies txn to 0.200 

ALTER TABLE public.TRANSACTION ADD COLUMN TXN_DELAY numeric(18,3) NOT NULL DEFAULT '0.000';
