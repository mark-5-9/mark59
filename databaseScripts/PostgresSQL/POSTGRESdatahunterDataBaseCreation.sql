-- in pgadmin run from 'postgres' db 
-- >  comment/uncomment as required 
CREATE USER admin SUPERUSER PASSWORD 'admin';
-- DROP DATABASE mark59datahunterdb;
CREATE DATABASE mark59datahunterdb WITH ENCODING='UTF8' OWNER=admin TEMPLATE=template0 LC_COLLATE='C' LC_CTYPE='C';
-- <

--   The utf8/C ecoding/collation is more in line with other mark59 database options (and how Java/JS sorts work). 
--   if you use the pgAdmin tool to load data, remember to hit the 'commit' icon to save the changes! 

-- in pgadmin run from 'mark59datahunterdb' db query  panel 

CREATE TABLE IF NOT EXISTS POLICIES (
  APPLICATION VARCHAR(64) NOT NULL,
  IDENTIFIER 	VARCHAR(512) NOT NULL,
  LIFECYCLE 	VARCHAR(64) NOT NULL,
  USEABILITY 	VARCHAR(16) NOT NULL,
  OTHERDATA  	VARCHAR(512) NOT NULL,
  CREATED   	TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UPDATED   	TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  EPOCHTIME  	BIGINT  NOT NULL DEFAULT '0',
  PRIMARY KEY (APPLICATION,IDENTIFIER,LIFECYCLE)
); 

CREATE TABLE IF NOT EXISTS REFERENCE (
  APPLICATION VARCHAR(64) NOT NULL,
  PROPERTY	  VARCHAR(128) NOT NULL,
  VALUE 	  VARCHAR(128) NOT NULL,
  DESCRIPTION VARCHAR(512) DEFAULT NULL,
  PRIMARY KEY (APPLICATION,PROPERTY)
); 
