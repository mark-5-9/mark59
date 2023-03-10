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

package com.mark59.trends.data.eventMapping.dao;

import java.util.List;

import com.mark59.trends.data.beans.EventMapping;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface EventMappingDAO 
{
	
	void insertData(EventMapping eventMapping);
	
	void deleteData(String txnType, String metricSource, String matchWhenLike);

	EventMapping getEventMapping(String metricSource, String matchWhenLike);
	
	List<EventMapping> findEventMappings();
	
	List<EventMapping> findEventMappingsForPerformanceTool(String performanceTool);

	List<EventMapping> findEventMappings(String selectionCol, String selectionValue);
	

	boolean doesLrEventMapEntryMatchThisEventMapping(String mdbEventType, String mdbEventName, EventMapping mark59EventMapping);

	/**
	 *   See if the passed transaction id / metric source type (eg 'Jmeter_DATAPOINT' - refer to AppConstantsMetrics ) matches to an event 
	 *   on the event mapping table (if it does, it will be the mapped data type that will used for SLA checking with this transaction).
	 *   
	 *   <p>Selection is based on a "best-guess" algorithm as to what a user was attempting to match against when multiple rows 
	 *   match the passed transaction id / metric source:
	 *   <ul>
	 *   <li>any rows with no percent symbol (no free wild-cards) take precedence
	 *   <li>next is the length of the match (minus the number of free wild-cards - high to low)
	 *   <li>then next is the total length boundary characters (longest to shortest)   
	 *   </ul>
	 *   
	 * @param txnId  transaction id
	 * @param metricSource metric source type (eg 'Jmeter_DATAPOINT' refer to AppConstantsMetrics )
	 * @return matched EventMapping 
	 */
    EventMapping findAnEventForTxnIdAndSource(String txnId, String metricSource);
	
	void updateData(EventMapping eventMapping);

	
}