/*
 *  Copyright 2019 Mark59.com
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mark59.trends.data.sla.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.mark59.trends.data.beans.Sla;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SlaRowMapper implements RowMapper<Sla> {

	 @Override
	 public Sla mapRow(ResultSet resultSet, int line) throws SQLException {
	  SlaExtractor slaExtractor = new SlaExtractor();
	  return slaExtractor.extractData(resultSet);
	 }

}
