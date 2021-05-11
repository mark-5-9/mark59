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

package com.mark59.metrics.data.graphMapping.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.metrics.data.beans.BarRange;
import com.mark59.metrics.data.beans.GraphMapping;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class GraphMappingDAOjdbcTemplateImpl implements GraphMappingDAO 
{
	
	@Autowired  
	private DataSource dataSource;

		

	@Override
	@SuppressWarnings("rawtypes")
	public GraphMapping  findGraphMapping(String graph){

		List<GraphMapping> graphMappingList = new ArrayList<GraphMapping>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String selectGraphsSQL   = "select LISTORDER, GRAPH, TXN_TYPE, VALUE_DERIVATION, UOM_DESCRIPTION, BAR_RANGE_SQL, BAR_RANGE_LEGEND, COMMENT "
				+ "from GRAPHMAPPING where GRAPH = '" + graph + "'"
				+ " order by GRAPH asc;";
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectGraphsSQL);
		
		if (rows.size() == 0 ){
			return null;
		}
		
		Map row = rows.get(0);
		
		GraphMapping graphMapping = new GraphMapping();
		graphMapping.setListOrder((Integer)row.get("LISTORDER")); 		
		graphMapping.setGraph((String)row.get("GRAPH")); 
		graphMapping.setTxnType((String)row.get("TXN_TYPE")); 
		graphMapping.setValueDerivation((String)row.get("VALUE_DERIVATION")); 
		graphMapping.setUomDescription((String)row.get("UOM_DESCRIPTION")); 
		graphMapping.setBarRangeSql((String)row.get("BAR_RANGE_SQL"));
		graphMapping.setBarRangeLegend((String)row.get("BAR_RANGE_LEGEND"));		
		graphMapping.setComment((String)row.get("COMMENT")); 

		graphMappingList.add(graphMapping);
//		System.out.println("GraphMappingDAOjdbcTemplateImpl.findGraphMapping  : " + graphMapping.toString()  ) ;		
		
		return  graphMapping;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List<GraphMapping> getGraphMappings(){

		List<GraphMapping> graphMappingList = new ArrayList<GraphMapping>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String selectGraphsSQL   = "select LISTORDER, GRAPH, TXN_TYPE, VALUE_DERIVATION, UOM_DESCRIPTION, BAR_RANGE_SQL, BAR_RANGE_LEGEND, COMMENT from GRAPHMAPPING order by LISTORDER asc;";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectGraphsSQL);
		
		for (Map row : rows) {
			GraphMapping graphMapping = new GraphMapping();
			
			graphMapping.setListOrder((Integer)row.get("LISTORDER")); 
			graphMapping.setGraph((String)row.get("GRAPH")); 
			graphMapping.setTxnType((String)row.get("TXN_TYPE")); 
			graphMapping.setValueDerivation((String)row.get("VALUE_DERIVATION")); 
			graphMapping.setUomDescription((String)row.get("UOM_DESCRIPTION")); 
			graphMapping.setBarRangeSql((String)row.get("BAR_RANGE_SQL"));
			graphMapping.setBarRangeLegend((String)row.get("BAR_RANGE_LEGEND"));					
			graphMapping.setComment((String)row.get("COMMENT")); 

			graphMappingList.add(graphMapping);
//			System.out.println("values from GraphMappingDAOjdbcTemplateImpl.getGraphMappings  : " + graphMapping.toString()  ) ;
		}	
		return  graphMappingList;
	}
	
	
	@Override
	public void inserttGraphMapping(GraphMapping graphMapping) {
		String sql = "INSERT INTO GRAPHMAPPING "
				+ "(LISTORDER, GRAPH, TXN_TYPE, VALUE_DERIVATION, UOM_DESCRIPTION,  BAR_RANGE_SQL, BAR_RANGE_LEGEND, COMMENT) VALUES (?,?,?,?,?,?,?,?)";
//		System.out.println("performing : " + sql + " " +  graphMapping.getGraph() );
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] { graphMapping.getListOrder(), graphMapping.getGraph(), graphMapping.getTxnType(), 
								graphMapping.getValueDerivation(), graphMapping.getUomDescription(),
								graphMapping.getBarRangeSql(), graphMapping.getBarRangeLegend(), graphMapping.getComment()  });
	}
	
	
	@Override
	public void updateGraphMapping(GraphMapping graphMapping){

		String sql = "UPDATE GRAPHMAPPING set LISTORDER = ?, TXN_TYPE = ?, VALUE_DERIVATION = ?, UOM_DESCRIPTION = ?, BAR_RANGE_SQL = ?, BAR_RANGE_LEGEND = ?, COMMENT = ?"
				+ "where GRAPH = ? ";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] { graphMapping.getListOrder(), graphMapping.getTxnType(), graphMapping.getValueDerivation(), graphMapping.getUomDescription(),
								graphMapping.getBarRangeSql(), graphMapping.getBarRangeLegend(),graphMapping.getComment(), graphMapping.getGraph()  });
	}	
	
	
	@Override
	public void deleteGraphMapping(String graph) {
		String sql = "delete from GRAPHMAPPING where GRAPH='" + graph	+ "'";
		System.out.println("deleteRun sql: " + sql);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}	

	
	
	@Override
	public List<BarRange> getTransactionRangeBarDataForGraph(String application, String runTime, String graph) { 

		List<BarRange> trxnIdsSlaRanges = new ArrayList<BarRange>();
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		String  sqlObtainBarRangeSql = "SELECT BAR_RANGE_SQL FROM GRAPHMAPPING where graph = '" + graph + "'";
		List<Map<String, Object>> onerows = jdbcTemplate.queryForList(sqlObtainBarRangeSql);
		
		if (onerows.size() == 0 ){
			return trxnIdsSlaRanges;
		}

		Map<String, Object> onerow = onerows.get(0);
		String rangeSql = (String)onerow.get("BAR_RANGE_SQL"); 		
		if (StringUtils.isBlank(rangeSql) ){
			return trxnIdsSlaRanges;
		}		
		
		rangeSql = rangeSql.replaceAll("@runTime","'" + runTime + "'"  ).replaceAll("@application", "'" + application + "'" )  ; 
//		System.out.println("getSlaRangesForGraph: sql = " + rangeSql);
		
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(rangeSql);
		for (Map<String, Object> row : rows) {
			BarRange slaRange = new BarRange();
			slaRange.setTxnId((String)row.get("TXN_ID"));
			
//			if ( row.get("BAR_MIN").getClass().toString().contains("BigDecimal")  ){
//				slaRange.setBarMin((BigDecimal)row.get("BAR_MIN"));	
//			} else { // assume Double
//				slaRange.setBarMin(new BigDecimal((Double)row.get("BAR_MIN")));	   
//			}	
//			if ( row.get("BAR_MAX").getClass().toString().contains("BigDecimal")  ){
//				slaRange.setBarMax((BigDecimal)row.get("BAR_MAX"));	
//			} else { // assume Double
//				slaRange.setBarMax(new BigDecimal((Double)row.get("BAR_MAX")));	   
//			}	

			slaRange.setBarMin((BigDecimal)row.get("BAR_MIN"));		
			slaRange.setBarMax((BigDecimal)row.get("BAR_MAX"));				
			trxnIdsSlaRanges.add(slaRange);
//			System.out.println("getSlaRangesForGraph: txn=" + slaRange.getTxnId() + " min= " + slaRange.getBarMin() + " max=" + slaRange.getBarMax() ); 
		}
		return  trxnIdsSlaRanges;
	}
	
	
}
