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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.metrics.data.beans.MetricSla;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class MetricSlaDAOjdbcImpl implements MetricSlaDAO {

	@Autowired
	private DataSource dataSource;

	
	public void insertData(MetricSla metricSla) {
		String sql = "INSERT INTO METRICSLA "
				+ "(APPLICATION, METRIC_NAME, METRIC_TXN_TYPE, VALUE_DERIVATION, SLA_MIN, SLA_MAX, IS_ACTIVE, COMMENT) VALUES (?,?,?,?,?,?,?,?)";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] { metricSla.getApplication(),metricSla.getMetricName(),metricSla.getMetricTxnType(),metricSla.getValueDerivation(), 
						metricSla.getSlaMin(), metricSla.getSlaMax(), metricSla.getIsActive(), metricSla.getComment() });
	}
	

	@Override
	public void deleteAllSlasForApplication(String application) {
		String sql = "delete from METRICSLA where  APPLICATION='" + application + "'";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

	
	@Override
	public void deleteData(String application, String metricName, String metricTxnType, String valueDerivation ) {
		String sql = "delete from METRICSLA where APPLICATION='" + application + "' "
										  + " and METRIC_NAME='" + metricName + "'"
										  + " and METRIC_TXN_TYPE='" + metricTxnType + "'"
										  + " and VALUE_DERIVATION='" + valueDerivation + "'"   ;
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

	
	/*
	 * delete/insert (i.e. rename) if transaction does not exist within the given application,  otherwise update the new values for the passed transaction name
	 */
	@Override
	public void updateData(MetricSla metricSla) {

		MetricSla existingSla =  getMetricSla(metricSla.getApplication(), metricSla.getMetricName(), metricSla.getMetricTxnType(), metricSla.getValueDerivation()); 
//		System.out.println("MetricSlaDAOjdbcImpl.updateData: app=" + metricSla.getApplication() + ", Name=" + metricSla.getMetricName() +
//				",  txnType = " + metricSla.getMetricTxnType() + ",  field = " + metricSla.getValueDerivation() +", orig= " + metricSla.getOriginalMetricName() );	
		
		if (existingSla == null ){  //a MetricsName rename from the original name to the new one
			deleteData(metricSla.getApplication(), metricSla.getOriginalMetricName(),metricSla.getMetricTxnType(),metricSla.getValueDerivation() );
			insertData(metricSla);
			
		} else {  // update values for an existing transaction
				
			String sql = "UPDATE METRICSLA SET SLA_MIN = " + metricSla.getSlaMin()   + ", "
										+ "SLA_MAX= "      + metricSla.getSlaMax()   + ", "
										+ "IS_ACTIVE= '"   + metricSla.getIsActive() + "', "
										+ "COMMENT= '"     + metricSla.getComment()  + "'"										
										+ " where APPLICATION='" + metricSla.getApplication() + "' "
										  		+ " and METRIC_NAME='" + metricSla.getMetricName() + "'"
										  		+ " and METRIC_TXN_TYPE='" + metricSla.getMetricTxnType() + "'"
										  		+ " and VALUE_DERIVATION='" + metricSla.getValueDerivation() + "'" ;
			
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.update(sql);
		}
	}
	

	@Override
	public MetricSla getMetricSla(String application,String metricName, String metricTxnType, String valueDerivation) {
		List<MetricSla> slaList = new ArrayList<MetricSla>();
		String sql = "select * from METRICSLA where APPLICATION='" + application + "' "
									  		+ " and METRIC_NAME='" + metricName + "'"
									  		+ " and METRIC_TXN_TYPE='" + metricTxnType + "'"
									  		+ " and VALUE_DERIVATION='" + valueDerivation + "'" ;
//		System.out.println("getMetricSla sql = " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		slaList = jdbcTemplate.query(sql, new MetricSlaRowMapper());
		
		if (slaList.isEmpty() )
			return null;
		else
			return slaList.get(0);
	}
	
	
	public List<MetricSla> getMetricSlaList() {
		List<MetricSla> metricSlaList = new ArrayList<MetricSla>();
		String sql = "select * from METRICSLA order by APPLICATION, METRIC_NAME, METRIC_TXN_TYPE, VALUE_DERIVATION ";
//		System.out.println("getSla sql = " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		metricSlaList = jdbcTemplate.query(sql, new MetricSlaRowMapper());
		return metricSlaList;
	}
	
	@Override
	public List<MetricSla> getMetricSlaList(String application) {
		List<MetricSla> metricSlaList = new ArrayList<MetricSla>();
		String sql = "select * from METRICSLA where APPLICATION='" + application + "' order by METRIC_NAME, METRIC_TXN_TYPE, VALUE_DERIVATION ";
//		System.out.println("getSla sql = " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		metricSlaList = jdbcTemplate.query(sql, new MetricSlaRowMapper());
		return metricSlaList;
	}
	
	@Override
	public List<MetricSla> getMetricSlaList(String application,	String metricTxnType) {
		if (metricTxnType == null ){
			return getMetricSlaList(application);
		}
		List<MetricSla> metricSlaList = new ArrayList<MetricSla>();
		String sql = "select * from METRICSLA where APPLICATION='" + application + "' and METRIC_TXN_TYPE ='" + metricTxnType + "' order by METRIC_NAME, VALUE_DERIVATION ";
//		System.out.println("getSla sql = " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		metricSlaList = jdbcTemplate.query(sql, new MetricSlaRowMapper());
		return metricSlaList;
	}
	

	@Override
	@SuppressWarnings("rawtypes")
	public List<String> findApplications() {
		String sql = "SELECT distinct APPLICATION FROM METRICSLA";

		List<String> applications = new ArrayList<String>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		for (Map row : rows) {
			applications.add( (String)row.get("APPLICATION") );
//			System.out.println("populating application in drop down list : " + row.get("APPLICATION")  ) ;
		}	
		return  applications;
	}


}
