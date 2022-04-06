
-- *************************************************************************************
-- **
-- **   from 4.1 to 4.2   
-- **
-- **   1. In the DataHunter MYSQL policies table, precision of DATATIME fields    
-- **   have changed from secs to nanosecs (bring in line with Postgress precision, and 
-- **   near H2 (msecs). 
-- **
-- **   2. Add a 'disabled' indicator to the metricsdb (Trend Analysis) SLA table.    
-- **
-- *************************************************************************************


SET SQL_SAFE_UPDATES = 0;

ALTER TABLE datahunterdb.POLICIES  MODIFY CREATED TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE datahunterdb.POLICIES  MODIFY UPDATED TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

ALTER TABLE metricsdb.SLA ADD COLUMN IS_ACTIVE CHAR(1) NOT NULL DEFAULT 'Y' AFTER COMMENT;

