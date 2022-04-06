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

/**
 * Un-Uninterruptible wrapper for Thread.sleep
 * /**
 * @author Michael Cohen
 * Written: Australian Winter 2019  
 */
public class SafeSleep {

	private SafeSleep() {}
	
	/**
	 * Pause the running thread for a given number of milliseconds<br>  
	 * (Uninterruptible wrapper for Thread.sleep)
	 * 
	 * @param sleepDuration  (milliseconds)
	 */
	public static void sleep(long sleepDuration) {
		Long startTime = System.currentTimeMillis();
		try {
			Thread.sleep(sleepDuration);
		} catch (InterruptedException e) {
			Long endTime = System.currentTimeMillis();			
			long remainingDuration = sleepDuration - (endTime - startTime);
			if(remainingDuration > 0) {
				sleep(remainingDuration);
			}
		}
	}
	
}
