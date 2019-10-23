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

package com.mark59.core.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.log4j.Logger;

/**
 * General catch-all class for useful functions 
 */
public class Mark59Utils {
	
	private static final Logger LOG = Logger.getLogger(Mark59Utils.class);

	/**
	 * Allows a map with additional or entries that exist in a base map to be combined, or where the same key exists in both maps, the 
	 * value in the override map will be used.  
	 * <p>returned as Jmeter Arguments 
	 * 
	 * @param baseMap  Map of base key valuse 
	 * @param additionalEntriesMap  additional of override key values
	 * @return jmeterArguments
	 * 
	 * @author Philip Webb
	 * Written: Australian Winter 2019 
	 */
	public static Arguments mergeMapWithAnOverrideMap(Map<String,String> baseMap, Map<String, String> additionalEntriesMap) {
		Arguments jmeterArguments = new Arguments();
		Map<String, String> jmeterArgumentsMap = new LinkedHashMap<String,String>();
		Map<String,String> baseMapMergedWithAdditionalEntriesMap = new LinkedHashMap<String,String>(); 
		
		for (Map.Entry<String, String> defaultEntry : baseMap.entrySet()) {
			if (additionalEntriesMap.containsKey(defaultEntry.getKey())){
				baseMapMergedWithAdditionalEntriesMap.put(defaultEntry.getKey(), additionalEntriesMap.get(defaultEntry.getKey()));
				additionalEntriesMap.remove(defaultEntry.getKey());
			} else {
				baseMapMergedWithAdditionalEntriesMap.put(defaultEntry.getKey(), baseMap.get(defaultEntry.getKey()));
			}
		}
		
		jmeterArgumentsMap.putAll(additionalEntriesMap);
		jmeterArgumentsMap.putAll(baseMapMergedWithAdditionalEntriesMap);
		
		for (Map.Entry<String, String> parameter : jmeterArgumentsMap.entrySet()) {
			jmeterArguments.addArgument(parameter.getKey(), parameter.getValue());
		}
		
		if (LOG.isDebugEnabled()){LOG.debug("jmeter arguments at end of mergeMapWithAnOverrideMap : " + Arrays.toString(jmeterArguments.getArgumentsAsMap().entrySet().toArray()));} 
		return jmeterArguments;
	}
	

}
