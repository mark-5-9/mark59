
--- this is due to a change in the behaviour of the MqSQL connector at 8.0.23 (DATETIME behaviour changed, so now using TIMESTAMP) ---
--- https://bugs.mysql.com/bug.php?id=102435 --

ALTER TABLE `datahunterdb`.`POLICIES` CHANGE COLUMN `CREATED` `CREATED` TIMESTAMP NOT NULL ;
