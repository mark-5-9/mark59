/*
 *  Copyright 2019 Insurance Australia Group Limited
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

package com.mark59.servermetricsweb.data.base.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Philip Webb
 * Written: Australian Summer 2020  
 */
public class BaseDAOjdbcTemplateImpl implements BaseDAO 
{
	
	@Autowired  
	private DataSource dataSource;


	@Override
	public List<String> findColumnNamesForTable(String tableName){

		
		List<String> columnNames = new ArrayList<>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate  (dataSource);

		String findColumnNameSQL = "SELECT * FROM  information_schema.columns WHERE	table_name = '" + tableName + "' order by ordinal_position; ";		
		
//		System.out.println(" BaseDAOjdbcTemplateImpl sql : " + findColumnNameSQL ); 
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(findColumnNameSQL);
		
		for (Map<String, Object> row : rows) {
			columnNames.add((String)row.get("column_name"));
//			System.out.println("findColumnNamesForTable " + tableName + " : " + (String)row.get("column_name")  ) ;		
		}	
		return columnNames;
	}

}
