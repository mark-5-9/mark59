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

package com.mark59.datahunter.application;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * @author Philip Webb
 * Written: Australian Spring 2021
 */
public class SqlWithParms  {

	String sql;
	MapSqlParameterSource sqlparameters;

	public SqlWithParms() {
	}
	
	public SqlWithParms(String sql, MapSqlParameterSource sqlparameters) {
		super();
		this.sql = sql;
		this.sqlparameters = sqlparameters;
	}

	
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}

	public MapSqlParameterSource getSqlparameters() {
		return sqlparameters;
	}
	public void setSqlparameters(MapSqlParameterSource sqlparameters) {
		this.sqlparameters = sqlparameters;
	}

	
	@Override
    public String toString() {
        return  sql + "<br>" + 
        		DataHunterUtils.prettyHttpPrintMap(sqlparameters.getValues()); 
	}

}