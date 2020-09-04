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

--  you need need to be connected to the database using the 'root' user to execute

DROP USER IF EXISTS 'admin'@'localhost';
DROP USER IF EXISTS 'admin'@'%';

CREATE USER 'admin'@'localhost' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'localhost' WITH GRANT OPTION;
CREATE USER 'admin'@'%' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;


--  A Note for Linux Users --

--  Hint:  download mysql workbench directly from website  https://dev.mysql.com/downloads/workbench/  (losts of out-of-date stuff on the web)

--  You may also need to set a time_zone, depending on your install (you will find out when you attempt to connect start an applciaton and you see a message like " The server time zone value 'xxxx' is unrecognized or represents more than one time zone"  
--  refer https://dev.mysql.com/doc/refman/8.0/en/time-zone-support.html :
--     run command: mysql_tzinfo_to_sql /usr/share/zoneinfo | mysql -u root -p mysql              <- loads tz data into mysql 
--     restart server: sudo systemctl restart mysql
--     run sql (to confirm values are loaded):  SELECT * FROM mysql.time_zone_name;               <- you shoud see list of timezones
--     stop server:  sudo systemctl stop mysql
--     logon as su (UI "open as root") and edit /etc/mysql/my.conf adding line(s):                <- (above the !included... lines, no spaces) 
--        [mysqld]
--        default-time-zone='Australia/Melbourne'                                                 <- or whatever your timezone is  
--     start server:  sudo systemctl start mysql 
--     run sql (to confirm values are loaded):  SELECT @@GLOBAL.time_zone, @@SESSION.time_zone;   <- should return your new timezone 


