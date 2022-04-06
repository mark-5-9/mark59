
-- *************************************************************************************
-- **
-- **   from 4.1 to 4.2 
-- **
-- **   1. Align TIMESTAMP defaults on the datahunter database closer to the H2 and MYSQL databases 
-- **      (its still not exactly the same as H2 and MYSQL as PG does not have an 'ON UPDATE' option) 
-- **
-- **   2. Add a 'disabled' indicator to the metricsdb (Trend Analysis) SLA table.   
-- **      (in PG, columns are always added to the end of the table ) 
-- **
-- *************************************************************************************

-- connect to the datahunterdb database and run:

 ALTER TABLE POLICIES ALTER CREATED SET DEFAULT CURRENT_TIMESTAMP;
 ALTER TABLE POLICIES ALTER UPDATED SET DEFAULT CURRENT_TIMESTAMP;
 
 
-- connect to the metricsdb database and run:
  
 ALTER TABLE SLA ADD COLUMN IS_ACTIVE CHAR(1) NOT NULL DEFAULT 'Y' 
 