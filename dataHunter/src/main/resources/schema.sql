CREATE TABLE IF NOT EXISTS policies (
  application 	varchar(64) NOT NULL,
  identifier 	varchar(512) NOT NULL,
  lifecycle 	varchar(64) NOT NULL,
  useability 	varchar(8) NOT NULL,
  otherdata 	varchar(64) NOT NULL,
  created 		datetime NOT NULL,
  updated 		timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  epochtime 	bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (application, identifier, lifecycle)
 ); 