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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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

		metricSla = nullsToDefaultValues(metricSla);		
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql,
				new Object[] { metricSla.getApplication(),metricSla.getMetricName(),metricSla.getMetricTxnType(),metricSla.getValueDerivation(), 
						metricSla.getSlaMin(), metricSla.getSlaMax(), metricSla.getIsActive(), metricSla.getComment() });
	}
	

	@Override
	public void deleteAllSlasForApplication(String application) {
		
		String sql = "delete from METRICSLA where  APPLICATION = :application ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);		
		
//		System.out.println("metricSlaDao deleteAllSlasForApplication : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}


	@Override
	public void deleteData(String application, String metricName, String metricTxnType ) {

		String sql = "delete from METRICSLA where APPLICATION = :application "
										  + " and METRIC_NAME = :metricName "
										  + " and METRIC_TXN_TYPE = :metricTxnType ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("metricName", metricName)
				.addValue("metricTxnType", metricTxnType);
		
//		System.out.println("metricSlaDao deleteData : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}
	
	
	@Override
	public void deleteData(String application, String metricName, String metricTxnType, String valueDerivation ) {
		
		String sql = "delete from METRICSLA where APPLICATION = :application "
										  + " and METRIC_NAME = :metricName "
										  + " and METRIC_TXN_TYPE = :metricTxnType "
										  + " and VALUE_DERIVATION = :valueDerivation ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("metricName", metricName)
				.addValue("metricTxnType", metricTxnType)
				.addValue("valueDerivation", valueDerivation);		
		
//		System.out.println("metricSlaDao deleteData : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
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
			insertData(metricSla);
			deleteData(metricSla.getApplication(), metricSla.getOriginalMetricName(),metricSla.getMetricTxnType(),metricSla.getValueDerivation() );
			
		} else {  // update values for an existing transaction
			
			metricSla = nullsToDefaultValues(metricSla);				
			
			String sql = "UPDATE METRICSLA SET SLA_MIN = :slaMin, "
										+ "    SLA_MAX = :slaMax, "
										+ "  IS_ACTIVE = :isActive, "
										+ "    COMMENT = :comment "										
										+ " where APPLICATION = :application "
										  		+ " and METRIC_NAME = :metricName "
										  		+ " and METRIC_TXN_TYPE = :metricTxnType "
										  		+ " and VALUE_DERIVATION = :valueDerivation ";
			
			MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
					.addValue("slaMin", metricSla.getSlaMin())
					.addValue("slaMax", metricSla.getSlaMax())
					.addValue("isActive", metricSla.getIsActive())
					.addValue("comment", metricSla.getComment())
					.addValue("application", metricSla.getApplication())
					.addValue("metricName", metricSla.getMetricName())
					.addValue("metricTxnType", metricSla.getMetricTxnType())
					.addValue("valueDerivation", metricSla.getValueDerivation());		
			
//			System.out.println("metricSlaDao updateData : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
			NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
			jdbcTemplate.update(sql, sqlparameters);
		}
	}
	

	@Override
	public MetricSla getMetricSla(String application,String metricName, String metricTxnType, String valueDerivation) {

		String sql = "select * from METRICSLA where APPLICATION = :application "
									  		+ " and METRIC_NAME = :metricName "
									  		+ " and METRIC_TXN_TYPE = :metricTxnType "
									  		+ " and VALUE_DERIVATION = :valueDerivation " ;

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)		
				.addValue("metricName", metricName)		
				.addValue("metricTxnType", metricTxnType)		
				.addValue("valueDerivation", valueDerivation);		
		
//		System.out.println(" getMetricSla : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<MetricSla> slaList = jdbcTemplate.query(sql, sqlparameters, new MetricSlaRowMapper());	
		
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

		String sql = "select * from METRICSLA where APPLICATION = :application order by METRIC_NAME, METRIC_TXN_TYPE, VALUE_DERIVATION ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application);		
		
//		System.out.println(" getSlaList : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<MetricSla> metricSlaList = jdbcTemplate.query(sql, sqlparameters, new MetricSlaRowMapper());			

		return metricSlaList;
	}
	
	
	@Override
	public List<MetricSla> getMetricSlaList(String application,	String metricTxnType) {

		if (metricTxnType == null ){
			return getMetricSlaList(application);
		}
		String sql = "select * from METRICSLA"
				+ " where APPLICATION = :application "
				+ "  and METRIC_TXN_TYPE = :metricTxnType "
				+ "  order by METRIC_NAME, VALUE_DERIVATION ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("metricTxnType", metricTxnType);		
		
//		System.out.println(" getMetricSlaList : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<MetricSla> metricSlaList = jdbcTemplate.query(sql, sqlparameters, new MetricSlaRowMapper());	
		return metricSlaList;
	}

	
	@Override
	public List<MetricSla> getMetricSlaList(String application, String metricName, String metricTxnType) {

		if (metricTxnType == null ){
			return getMetricSlaList(application);
		}
		String sql = "select * from METRICSLA"
				+ " where APPLICATION = :application "
				+ "  and METRIC_NAME = :metricName "
				+ "  and METRIC_TXN_TYPE = :metricTxnType "
				+ "  order by VALUE_DERIVATION ";

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("application", application)
				.addValue("metricName", metricName)
				.addValue("metricTxnType", metricTxnType);		
		
//		System.out.println(" getMetricSlaList : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<MetricSla> metricSlaList = jdbcTemplate.query(sql, sqlparameters, new MetricSlaRowMapper());	
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

	/*
	 *   To prevent null exceptions during SLA processing
	 */
	private MetricSla nullsToDefaultValues(MetricSla metricSla) {
		if (metricSla.getSlaMin() == null) {
			metricSla.setSlaMin(new BigDecimal(0.0));
		}
		if (metricSla.getSlaMax() == null) {
			metricSla.setSlaMax(new BigDecimal(0.0));
		}
		return metricSla;
	}

}
