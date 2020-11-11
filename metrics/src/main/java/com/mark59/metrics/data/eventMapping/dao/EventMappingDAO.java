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

import java.util.List;

import com.mark59.metrics.data.beans.EventMapping;

/**
 * @author Philip Webb
 * Written: Australian Winter 2019  
 */
public interface EventMappingDAO 
{
	
	public void insertData(EventMapping eventMapping);
	
	public void deleteData(String txnType, String metricSource, String matchWhenLike);

	public EventMapping getEventMapping(String metricSource, String matchWhenLike);
	
	public List<EventMapping> findEventMappings();
	
	public List<EventMapping> findEventMappingsForPerformanceTool(String performanceTool); 

	public List<EventMapping> findEventMappings(String selectionCol,  String selectionValue);
	
	public boolean doesLrEventMapEntryMatchThisEventMapping(String EventType, String EventName, EventMapping eventMapping);

	public EventMapping findAnEventForTxnIdAndSource(String txnId, String metricSource);
	
	public void updateData(EventMapping eventMapping);

	
}