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

package com.mark59.metrics.data.eventMapping.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.mark59.metrics.data.beans.EventMapping;

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
		
		System.out.println("EventMappingDAOjdbcTemplateImpl insert EVENTMAPPING : " +  eventMapping.toString() );
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.update(sql,
				new Object[] { eventMapping.getTxnType(),eventMapping.getPerformanceTool(), eventMapping.getMetricSource(), eventMapping.getMatchWhenLike(), eventMapping.getTargetNameLB(), eventMapping.getTargetNameRB(), 
				               eventMapping.getIsPercentage(), eventMapping.getIsInvertedPercentage(), eventMapping.getComment()});
	}
	
	
	@Override
	public void deleteData(String txnType, String metricSource, String matchhWenLike) {
		String sql = " DELETE FROM EVENTMAPPING where TXN_TYPE = '"	+ txnType
											+ "' and  METRIC_SOURCE = '" + metricSource
											+ "' and  MATCH_WHEN_LIKE = '" + matchhWenLike + "'"; 
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}
	
	
	@Override
	public void updateData(EventMapping eventMapping) {
		
		String sql = "UPDATE EVENTMAPPING SET TARGET_NAME_LB = '" 		+ eventMapping.getTargetNameLB() + "', "
										+ "TARGET_NAME_RB = '"    		+ eventMapping.getTargetNameRB() + "', "
										+ "IS_PERCENTAGE = '"    		+ eventMapping.getIsPercentage() +  "', "
										+ "IS_INVERTED_PERCENTAGE = '"	+ eventMapping.getIsInvertedPercentage() +  "', "
										+ "PERFORMANCE_TOOL = '" 		+ eventMapping.getPerformanceTool() +  "', "										
										+ "COMMENT = '"		    		+ eventMapping.getComment() +  "' "
										+ " where TXN_TYPE ='"       	+ eventMapping.getTxnType() + "' "
										+ "   and METRIC_SOURCE='"      + eventMapping.getMetricSource() + "'"
										+ "   and MATCH_WHEN_LIKE='" 	+ eventMapping.getMatchWhenLike() + "'";
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sql);
	}

	
	@Override
	public EventMapping getEventMapping(String metricSource, String matchhWenLike) {
		String sql = " SELECT * FROM EVENTMAPPING where METRIC_SOURCE = '" + metricSource
													  	 + "' and MATCH_WHEN_LIKE = '" + matchhWenLike + "'"; 
		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
		
		if (rows.isEmpty()){
			return null;
		} else {
			Map<String, Object> row = rows.get(0);
			EventMapping eventMapping =  populateFromResultSet(row);
			return eventMapping; 
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

		List<EventMapping> eventMappingList = new ArrayList<EventMapping>();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		List<Map<String, Object>> rows = jdbcTemplate.queryForList(getEventsMappingListSelectionSQL(selectionCol, selectionValue));
		
		for (Map<String, Object> row : rows) {
			EventMapping eventMapping = populateFromResultSet(row);
			eventMappingList.add(eventMapping);
			//System.out.println("values from EventMappingDAOjdbcTemplateImpl.findEventMappings  : " + eventMapping.toString()  ) ;
		}	
		return  eventMappingList;
	}
	
	private String getEventsMappingListSelectionSQL(String selectionCol, String selectionValue){	
		String eventsMappingListSelectionSQL               = "select TXN_TYPE, METRIC_SOURCE, MATCH_WHEN_LIKE, TARGET_NAME_LB, TARGET_NAME_RB, IS_PERCENTAGE, IS_INVERTED_PERCENTAGE, PERFORMANCE_TOOL, COMMENT from EVENTMAPPING ";
		if (!selectionValue.isEmpty()  ) {			
			eventsMappingListSelectionSQL += "  where " + selectionCol + " like '" + selectionValue + "' ";
		} 
		eventsMappingListSelectionSQL += " order by METRIC_SOURCE, " + orderingByMatchingProcess();
		//System.out.println("EventMappingDAOjdbcTemplateImpl.getEventsMappingListSelectionSQL: " + eventsMappingListSelectionSQL); 
		return  eventsMappingListSelectionSQL;
	}
	
	
	@Override
	public boolean doesLrEventMapEntryMatchThisEventMapping(String eventType, String eventName, EventMapping eventMapping) {
		
		Integer matchCount  = 0;
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		String[] matchWhenLikeSplit = eventMapping.getMetricSource().split("_", 2);
		
		if (matchWhenLikeSplit.length != 2) {
			throw new RuntimeException("Unexpected Metric_Source Format on EventMapping Table for a Loadrunner event. \n "
					+ "Expected underscord separated value (Loadrunncer_EventType) but got : " + eventMapping.getMetricSource()
					+ "\n   ,EventMapping : " + eventMapping.toString()
					+ "\n   ,for eventType = " + eventType + ", eventName = " + eventName );
	
		}
		
		String sql = "SELECT count(*) col FROM dual where '" + eventType + "'" + " = '" + matchWhenLikeSplit[1] + "' and '" + eventName + "' like  '" +  eventMapping.getMatchWhenLike() + "'"; 		
		matchCount = Integer.valueOf(jdbcTemplate.queryForObject(sql, String.class));
		
		if ( matchCount > 0 ){
//			System.out.println("     Event matched using sql: " + sql  + "     RESULT = " +  matchCount  );	
			return true;
		}
		return false;
	}


	/**
	 *   See if the passed transaction id / metric source type (eg 'Jmeter_DATAPONT') matches to an event on the event mapping table  
	 *   (if it does, it will be the mapped data type that will used for SLA checking with this transaction).
	 *   
	 *   <p>Selection is based on a "best-guess" algorithm as to what a user was attempting to match against when multiple rows 
	 *   match the passed transaction id / metric source:
	 *   <ul>
	 *   <li>any rows with no percent symbol (no free wild-cards) take precedence
	 *   <li>next is the length of the match (minus the number of free wild-cards - high to low)
	 *   <li>then next is the total length boundary characters (longest to shortest)   
	 *   </ul>	    
	 **/
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
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();	}	  
		
		eventMapping.setTargetNameLB((String)row.get("TARGET_NAME_LB"));
		eventMapping.setTargetNameRB((String)row.get("TARGET_NAME_RB"));
		eventMapping.setIsPercentage((String)row.get("IS_PERCENTAGE"));
		eventMapping.setIsInvertedPercentage((String)row.get("IS_INVERTED_PERCENTAGE"));
		eventMapping.setPerformanceTool((String)row.get("PERFORMANCE_TOOL"));		
		eventMapping.setComment((String)row.get("COMMENT"));
		return eventMapping;
	}



	
}
