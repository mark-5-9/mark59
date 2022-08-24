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

package com.mark59.trends.data.sla.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.mark59.trends.data.beans.Sla;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class SlaExtractor implements ResultSetExtractor<Sla> {

	@Override
	public Sla extractData(ResultSet resultSet) throws SQLException, DataAccessException {

		Sla sla = new Sla();

		sla.setTxnId(resultSet.getString(1));
		sla.setIsCdpTxn(resultSet.getString(2));
		sla.setApplication(resultSet.getString(3));
		try {
			sla.setTxnIdURLencoded(URLEncoder.encode(resultSet.getString(1), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			System.out.println("SlaExtractor UnsupportedEncodingException (" + resultSet.getString(1) + ") " + e.getMessage());
		}
		sla.setIsTxnIgnored(resultSet.getString(4));
		sla.setSla90thResponse(resultSet.getBigDecimal(5));
		sla.setSla95thResponse(resultSet.getBigDecimal(6));
		sla.setSla99thResponse(resultSet.getBigDecimal(7));
		sla.setSlaPassCount(resultSet.getLong(8));
		sla.setSlaPassCountVariancePercent(resultSet.getBigDecimal(9));
		sla.setSlaFailCount(resultSet.getLong(10));
		sla.setSlaFailPercent(resultSet.getBigDecimal(11));
		sla.setTxnDelay(resultSet.getBigDecimal(12));
		sla.setXtraNum(resultSet.getBigDecimal(13));
		sla.setXtraInt(resultSet.getLong(14));
		sla.setSlaRefUrl(resultSet.getString(15));
		sla.setComment(resultSet.getString(16));
		sla.setIsActive(resultSet.getString(17));
		return sla;
	}
}
