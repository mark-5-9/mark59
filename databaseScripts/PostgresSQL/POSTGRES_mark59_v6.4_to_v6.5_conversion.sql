
-- *************************************************************************************
-- **
-- **   from 6.4 to 6.5   
-- **
-- **   This is required to be run only if you are using the Mark59 Metrics Application
-- **   -------------------------------------------------------------------------------
-- **   Due to the change from 'SimpleAES' to 'SecureAES', an increased encrypted password 
-- **   cipher length is needed. 
-- **   
-- **   Accounting for bat/sh file renames, and project renames 
-- **
-- *************************************************************************************

ALTER TABLE SERVERPROFILES ALTER COLUMN PASSWORD_CIPHER TYPE VARCHAR(512);
