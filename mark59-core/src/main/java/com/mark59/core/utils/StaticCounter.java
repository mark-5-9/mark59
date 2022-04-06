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

import java.util.HashMap;
import java.util.Map;

/**
 * Utility static counter that can keep track and increment a number of counters
 * that can be shared between threads, ensuring that no to threads will receive
 * the same counter value.
 * 
 * @author Michael Cohen
 * Written: Australian Winter 2019 
 */
public class StaticCounter {

	private static StaticCounter instance;

	private final Map<String, Integer> counterMap = new HashMap<>();

	private StaticCounter() {
	}

	private static synchronized StaticCounter getInstance() {
		if (instance == null) {
			instance = new StaticCounter();
		}
		return instance;
	}

	/**
	 * Create a new or get existing Count for passed counter 
	 * @param counter counter
	 * @return count for the counter
	 */
	public static synchronized Integer readCount(String counter) {
		StaticCounter instance = getInstance();
		if (!instance.counterMap.containsKey(counter)) {
			instance.counterMap.put(counter, 0);
		}
		return instance.counterMap.get(counter);
	}

	/**
	 * increment count for the passed counter
	 * @param counter counter
	 */
	public static synchronized void incrementCount(String counter) {
		getInstance().counterMap.put(counter, readCount(counter) + 1);
	}

	/**
	 * @param counter counter
	 * @return next increment and read Count for the counter
	 */
	public static synchronized Integer getNext(String counter) {
		incrementCount(counter);
		return readCount(counter);
	}

}
