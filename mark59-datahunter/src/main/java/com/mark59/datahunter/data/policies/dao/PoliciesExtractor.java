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

package com.mark59.datahunter.data.policies.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.mark59.datahunter.data.beans.Policies;

/**
 * @author Philip Webb
 * Written: Australian Summer 2024/25  
 */
public class PoliciesExtractor implements ResultSetExtractor<Policies> {

	@Override
	public Policies extractData(ResultSet resultSet) throws SQLException, DataAccessException {
		
		// extract order given by :
		// public final String SELECT_POLICY_COLUMNS = 
		//   " application, identifier, lifecycle, useability, otherdata, created, updated, epochtime ";
		
		Policies policies = new Policies();
		policies.setApplication(resultSet.getString(1));
		policies.setIdentifier(resultSet.getString(2));
		policies.setLifecycle(resultSet.getString(3));
		policies.setUseability(resultSet.getString(4));
		policies.setOtherdata(resultSet.getString(5));
		policies.setCreated(resultSet.getTimestamp(6));		
		policies.setUpdated(resultSet.getTimestamp(7));			
		policies.setEpochtime(resultSet.getLong(8));		
		return policies;
	}
}
