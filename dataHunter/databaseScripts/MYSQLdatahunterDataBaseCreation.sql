/*
  Copyright 2019 Insurance Australia Group Limited
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/


DROP DATABASE IF EXISTS `datahunterdb`;
CREATE DATABASE `datahunterdb`;

USE `datahunterdb`;

DROP TABLE IF EXISTS `policies`;
CREATE TABLE `policies` (
  `application` varchar(64) NOT NULL,
  `identifier` 	varchar(512) NOT NULL,
  `lifecycle` 	varchar(64) NOT NULL,
  `useability` 	enum('UNUSED','USED','REUSABLE','UNPAIRED') NOT NULL,
  `otherdata` 	mediumtext NOT NULL,
  `created` 	datetime NOT NULL,
  `updated` 	timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `epochtime` 	bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`application`,`identifier`,`lifecycle`),
  KEY `application` (`application`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `reference`;
CREATE TABLE `reference` (
  `application` varchar(64) NOT NULL,
  `property`	varchar(128) NOT NULL,
  `value` 		varchar(128) NOT NULL,
  `description` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`application`,`property`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
  