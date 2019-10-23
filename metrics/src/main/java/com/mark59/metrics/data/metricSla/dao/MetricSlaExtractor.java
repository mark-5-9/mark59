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

package com.mark59.metrics.data.metricSla.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.mark59.metrics.data.beans.MetricSla;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class MetricSlaExtractor implements ResultSetExtractor<MetricSla> {

	 public MetricSla extractData(ResultSet resultSet) throws SQLException,  DataAccessException {
	  
		MetricSla metricSla = new MetricSla();
		  
		metricSla.setApplication(resultSet.getString(1));
		metricSla.setMetricName(resultSet.getString(2));
		try {
			metricSla.setMetricNameURLencoded(URLEncoder.encode(resultSet.getString(2), "UTF-8") ) ;
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();	}	  

		metricSla.setMetricTxnType(resultSet.getString(3));
		metricSla.setValueDerivation(resultSet.getString(4));
		metricSla.setSlaMin(resultSet.getBigDecimal(5));	  
		metricSla.setSlaMax(resultSet.getBigDecimal(6));	  
		metricSla.setComment(resultSet.getString(7));	  
	  
		return metricSla;
	 }
}
