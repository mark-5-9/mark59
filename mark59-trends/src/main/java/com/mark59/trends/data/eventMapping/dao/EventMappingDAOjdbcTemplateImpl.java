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

package com.mark59.trends.data.eventMapping.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.mark59.trends.data.beans.EventMapping;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public class EventMappingDAOjdbcTemplateImpl implements EventMappingDAO 
{
	
	@Autowired  
	private DataSource dataSource;

 	
	@Override
	public void insertData(EventMapping eventMapping) {
		String sql = "INSERT INTO EVENTMAPPING "
				+ "(TXN_TYPE, PERFORMANCE_TOOL, METRIC_SOURCE, MATCH_WHEN_LIKE, TARGET_NAME_LB, TARGET_NAME_RB, IS_PERCENTAGE, IS_INVERTED_PERCENTAGE, COMMENT )"
				+ " VALUES (?,?,?,?,?,?,?,?,?)";

//		System.out.println("EventMappingDAOjdbcTemplateImpl insert EVENTMAPPING : " +  eventMapping.toString() );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql, eventMapping.getTxnType(),eventMapping.getPerformanceTool(), eventMapping.getMetricSource(),
				eventMapping.getMatchWhenLike(), eventMapping.getTargetNameLB(), eventMapping.getTargetNameRB(), eventMapping.getIsPercentage(),
				eventMapping.getIsInvertedPercentage(), eventMapping.getComment());
	}
	
	
	@Override
	public void deleteData(String txnType, String metricSource, String matchWhenLike) {

		String sql = " DELETE FROM EVENTMAPPING where TXN_TYPE = :txnType and METRIC_SOURCE = :metricSource and MATCH_WHEN_LIKE = :matchWhenLike ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("txnType", txnType)
				.addValue("metricSource", metricSource)
				.addValue("matchWhenLike", matchWhenLike);
		
//		System.out.println("metricSlaDao deleteAllSlasForApplication : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}
	
	
	@Override
	public void updateData(EventMapping eventMapping) {
		
		String sql = "UPDATE EVENTMAPPING SET TARGET_NAME_LB = :getTargetNameLB, "
										+ "TARGET_NAME_RB = :getTargetNameRB, "
										+ "IS_PERCENTAGE = :getIsPercentage, "
										+ "IS_INVERTED_PERCENTAGE = :getIsInvertedPercentage, "
										+ "PERFORMANCE_TOOL = :getPerformanceTool, "										
										+ "COMMENT = :getComment "
										+ " where TXN_TYPE = :getTxnType "
										+ "   and METRIC_SOURCE = :getMetricSource "
										+ "   and MATCH_WHEN_LIKE= :getMatchWhenLike ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("getTargetNameLB", 		eventMapping.getTargetNameLB())
				.addValue("getTargetNameRB", 		eventMapping.getTargetNameRB())
				.addValue("getIsPercentage", 		eventMapping.getIsPercentage())
				.addValue("getIsInvertedPercentage",eventMapping.getIsInvertedPercentage())
				.addValue("getPerformanceTool", 	eventMapping.getPerformanceTool())
				.addValue("getComment", 			eventMapping.getComment())
				.addValue("getTxnType",				eventMapping.getTxnType())
				.addValue("getMetricSource", 		eventMapping.getMetricSource())
				.addValue("getMatchWhenLike", 		eventMapping.getMatchWhenLike())
				;		
		
//		System.out.println("eventMappingDao updateData : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		jdbcTemplate.update(sql, sqlparameters);
	}

	
	@Override
	public EventMapping getEventMapping(String metricSource, String matchWhenLike) {
		
		String sql = " SELECT * FROM EVENTMAPPING where METRIC_SOURCE = :metricSource and MATCH_WHEN_LIKE = :matchWhenLike ";
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("metricSource", metricSource)
				.addValue("matchWhenLike", matchWhenLike);
		
//		System.out.println(" getEventMapping : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);	
		
		if (rows.isEmpty()){
			return null;
		} else {
			Map<String, Object> row = rows.get(0);
			return populateFromResultSet(row);
		}
	}		
	

	@Override
	public List<EventMapping> findEventMappingsForPerformanceTool(String performanceTool) {
		return findEventMappings("PERFORMANCE_TOOL", performanceTool);   
	}
	
	@Override
	public List<EventMapping> findEventMappings(){
		return  findEventMappings("","");
	}

	@Override
	public List<EventMapping> findEventMappings(String selectionCol, String selectionValue){

		String sql = "SELECT TXN_TYPE, METRIC_SOURCE, MATCH_WHEN_LIKE, TARGET_NAME_LB, TARGET_NAME_RB, IS_PERCENTAGE, "
				+ "IS_INVERTED_PERCENTAGE,PERFORMANCE_TOOL, COMMENT "
				+ "from EVENTMAPPING ";
		
		if (!selectionValue.isEmpty()  ) {			
			sql += "  where " + selectionCol + " like :selectionValue ";
		}
		sql += " order by METRIC_SOURCE, " + orderingByMatchingProcess();
		
		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("selectionValue", selectionValue);
		
		List<EventMapping> eventMappingList = new ArrayList<>();
//		System.out.println(" findEventMappings : " + sql + UtilsMetrics.prettyPrintParms(sqlparameters));
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlparameters);	
		
		for (Map<String, Object> row : rows) {
			EventMapping eventMapping = populateFromResultSet(row);
			eventMappingList.add(eventMapping);
			//System.out.println("values from EventMappingDAOjdbcTemplateImpl.findEventMappings  : " + eventMapping.toString()  ) ;
		}	
		return  eventMappingList;
	}
	
	
	@Override
	public boolean doesLrEventMapEntryMatchThisEventMapping(String mdbEventType, String mdbEventName, EventMapping eventMapping) {
//		System.out.println("doesLrEventMapEntryMatchThisEventMapping : " + mdbEventType + " : " + mdbEventName + " : " + eventMapping.getMetricSource() + ":" + eventMapping.getMatchWhenLike() ); 	
	
		String[] matchWhenLikeSplit = eventMapping.getMetricSource().split("_", 2);
		
		if (matchWhenLikeSplit.length != 2) {
			throw new RuntimeException("Unexpected Metric_Source Format on EventMapping Table for a Loadrunner event. \n "
					+ "Expected underscored separated value (Loadrunner_EventType) but got : " + eventMapping.getMetricSource()
					+ "\n   ,EventMapping : " + eventMapping.toString()
					+ "\n   ,for eventType = " + mdbEventType + ", eventName = " + mdbEventName );
		}
		
		String sql = "SELECT count(*) col FROM dual where '" + mdbEventType + "'" + " = :matchWhenLikeSplit and '" + mdbEventName + "' like :mdbEventName "; 		

		MapSqlParameterSource sqlparameters = new MapSqlParameterSource()
				.addValue("matchWhenLikeSplit", matchWhenLikeSplit[1] )
				.addValue("mdbEventName", eventMapping.getMatchWhenLike());
		
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		int matchCount = Integer.parseInt(Objects.requireNonNull(jdbcTemplate.queryForObject(sql, sqlparameters, String.class)));

		//			System.out.println("     Event matched using sql: " + sql  + "     RESULT = " +  matchCount  );
		return matchCount > 0;
	}


	@Override
	public EventMapping findAnEventForTxnIdAndSource(String txnId, String metricSource) {
			
		String sql = "SELECT  * FROM EVENTMAPPING" + 
					"  where '" + txnId + "'" + " like MATCH_WHEN_LIKE " +
					"    and  METRIC_SOURCE =  '" + metricSource + "' "  + 
					"  order by " + orderingByMatchingProcess();
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		if (rows.isEmpty()){
//			System.out.println("findAnEventForTxnIdByType sql : " + sql  + " : NULL RESULT "  );	
			return null;
		} else {
			Map<String, Object> row = rows.get(0);
			EventMapping eventMapping =  populateFromResultSet(row);
//			System.out.println("findAnEventForTxnIdByType sql : " + sql  + ",  event mapping : " + eventMapping  );	
			return eventMapping; 
		}
	}
	
	private String orderingByMatchingProcess() {
		
		return 	" case when position( '%' in MATCH_WHEN_LIKE) = 0 then 0 else 1 end," +   //  do any free wildcard_exists?" + ;
				" length(replace( MATCH_WHEN_LIKE, '%', '' )) desc, "   +             //  num_of_chars_exclude_wildcards  
				" length(TARGET_NAME_LB) + length(TARGET_NAME_RB) desc "; 
	}


	private EventMapping populateFromResultSet(Map<String, Object> row) {
		EventMapping eventMapping = new EventMapping();
		eventMapping.setTxnType((String)row.get("TXN_TYPE"));
		eventMapping.setMetricSource((String)row.get("METRIC_SOURCE"));
		eventMapping.setMatchWhenLike((String)row.get("MATCH_WHEN_LIKE"));
		try {
			eventMapping.setMatchWhenLikeURLencoded(URLEncoder.encode((String)row.get("MATCH_WHEN_LIKE"), "UTF-8") ) ;
		} catch (UnsupportedEncodingException e) {	
			System.out.println("EventMapping Dao UnsupportedEncodingException (" + eventMapping.getMatchWhenLike() + ") " + e.getMessage());
		}	  
		eventMapping.setTargetNameLB((String)row.get("TARGET_NAME_LB"));
		eventMapping.setTargetNameRB((String)row.get("TARGET_NAME_RB"));
		eventMapping.setIsPercentage((String)row.get("IS_PERCENTAGE"));
		eventMapping.setIsInvertedPercentage((String)row.get("IS_INVERTED_PERCENTAGE"));
		eventMapping.setPerformanceTool((String)row.get("PERFORMANCE_TOOL"));		
		eventMapping.setComment((String)row.get("COMMENT"));
		return eventMapping;
	}
	
}
