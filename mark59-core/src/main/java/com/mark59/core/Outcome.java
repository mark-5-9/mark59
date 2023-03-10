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

package com.mark59.core;

/**
 * Used to indicate if a particular transaction was a success or a failure.
 * 
 * @author Michael Cohen
 * Written: Australian Winter 2019
 */
public enum Outcome {
	/**	 * PASS (successful test) */
	PASS ("PASS", true, "200"),
	/**	 * FAIL (failed test) */	
	FAIL ("FAIL", false, "-1");
	
	private final String outcomeText;
	private final boolean outcomeSuccess;
	private final String outcomeResponseCode;
	
	Outcome(String outcomeText, boolean outcomeSuccess, String outcomeResponseCode) {
		this.outcomeText = outcomeText;
		this.outcomeSuccess = outcomeSuccess;
		this.outcomeResponseCode = outcomeResponseCode;
	}
	
	/**
	 * @return PASS or FAIL
	 */
	public String getOutcomeText() {
		return outcomeText;
	}
	
	/**
	 * @return true for passed, false for failed
	 */
	public boolean isOutcomeSuccess() {
		return outcomeSuccess;
	}
	
	/**
	 * @return response code currently 200 (good) or -1 (bad)
	 */
	public String getOutcomeResponseCode() {
		return outcomeResponseCode;
	}
}

